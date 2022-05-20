package wvw.semweb.codegen.parse.post;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.jen3.graph.Node;

import wvw.semweb.codegen.model.Assignment;
import wvw.semweb.codegen.model.Block;
import wvw.semweb.codegen.model.Comparison;
import wvw.semweb.codegen.model.ConditionList;
import wvw.semweb.codegen.model.Condition;
import wvw.semweb.codegen.model.Conjunction;
import wvw.semweb.codegen.model.IfThen;
import wvw.semweb.codegen.model.NodePath;
import wvw.semweb.codegen.model.Operand.Operands;
import wvw.semweb.codegen.model.struct.CodeModel;
import wvw.semweb.codegen.model.struct.ModelProperty;
import wvw.semweb.codegen.model.struct.ModelStruct;
import wvw.semweb.codegen.parse.rule.GraphNode;
import wvw.semweb.codegen.parse.rule.RuleGraph;

public class MergeStructsWithArraysIntoRoot extends ModelPostprocessor {

	protected CodeModel model;
	protected IfThen it;

	@Override
	public void postprocess(CodeModel model, IfThen it, RuleGraph ruleGraph) {
		this.model = model;
		this.it = it;

		for (GraphNode root : ruleGraph.getGraphRoots()) {
			ModelStruct struct = model.getStruct((Node) root.getId());

			consumeStructsRecursively(struct, new ArrayList<>(), new ArrayList<>(), mergePred,
					new MergeConsumer(struct, it));
		}
	}

	private void consumeStructsRecursively(ModelStruct curStruct, List<ModelProperty> prpPath,
			List<ModelStruct> structPath, Predicate<ModelStruct> filter,
			BiConsumer<List<ModelProperty>, List<ModelStruct>> collect) {

		// not dealing with root
		if (!prpPath.isEmpty()) {

			// does the struct have any array-like properties?
			if (filter.test(curStruct)) {
				// if so, let's merge them
				collect.accept(prpPath, structPath);

				prpPath.clear();
				structPath.clear();
			}
		}

		// avoid concurrent modification error
		List<ModelProperty> curPrps = new ArrayList<>(curStruct.getProperties());

		curPrps.forEach(newPrp -> {
			if (newPrp.hasTarget() && newPrp.getTarget().hasObjectType()) {
				ModelStruct newStruct = newPrp.getTarget().getObjectType();

				List<ModelStruct> newStructPath = new ArrayList<>(structPath);
				newStructPath.add(newStruct);

				List<ModelProperty> newPrpPath = new ArrayList<>(prpPath);
				newPrpPath.add(newPrp);

				consumeStructsRecursively(newStruct, newPrpPath, newStructPath, filter, collect);
			}
		});
	}

	private Predicate<ModelStruct> mergePred = new Predicate<ModelStruct>() {

		@Override
		public boolean test(ModelStruct t) {
			return t.getProperties().stream().anyMatch(p -> p.requiresArray());
		}
	};

	private class MergeConsumer implements BiConsumer<List<ModelProperty>, List<ModelStruct>> {

		private ModelStruct root;
		private IfThen it;

		public MergeConsumer(ModelStruct root, IfThen it) {
			this.root = root;
			this.it = it;
		}

		@Override
		public void accept(List<ModelProperty> prpPath, List<ModelStruct> structPath) {
			log.info("merging into " + root.getString() + ": "
					+ structPath.stream().map(s -> s.getString()).collect(Collectors.joining(", ")) + " (removing: "
					+ prpPath.stream().map(s -> s.getString()).collect(Collectors.joining(", ")) + ")");

			structPath.forEach(s -> {
				root.replacing(s);
				model.removeStruct(s);
			});

			// remove original properties
			root.getProperties().removeAll(prpPath);

			// update the if-then blocks as well

			updateComparisons((Conjunction) it.getCondition(), prpPath);
			updateAssignments((Block) it.getThen(), prpPath);
		}

		private void updateComparisons(ConditionList list, List<ModelProperty> prpPath) {
			Iterator<Condition> condIt = list.getConditions().iterator();
			while (condIt.hasNext()) {
				
				Condition cond = condIt.next();
				switch (cond.getConditionType()) {

				case CONJ:
				case DISJ:
					updateComparisons((ConditionList) cond, prpPath);
					break;

				case CMP:
					Comparison cmp = (Comparison) cond;

					if (cmp.getOp1().getType() == Operands.NODE_PATH) {
						NodePath p = (NodePath) cmp.getOp1();

						p.getPath().removeAll(prpPath);
						if (p.getPath().isEmpty())
							condIt.remove();
					}

					break;
				}
			}
		}

		private void updateAssignments(Block b, List<ModelProperty> prpPath) {
			b.getStatements().forEach(s -> {
				switch (s.getStatementType()) {

				case BLOCK:
					updateAssignments((Block) s, prpPath);
					break;

				case ASSIGN:
					Assignment assn = (Assignment) s;

					if (assn.getOp1().getType() == Operands.NODE_PATH) {
						NodePath p = (NodePath) assn.getOp1();
						p.getPath().removeAll(prpPath);
					}
					break;

				default:
					break;
				}
			});
		}
	}
}
