package wvw.semweb.codegen.parse.post;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import wvw.semweb.codegen.model.IfThen;
import wvw.semweb.codegen.model.struct.CodeModel;
import wvw.semweb.codegen.model.struct.ModelProperty;
import wvw.semweb.codegen.model.struct.ModelStruct;
import wvw.semweb.codegen.rule.GraphNode;

public class MergeStructsWithArraysIntoRoot extends ModelPostprocessor {

	protected CodeModel model;
	protected IfThen it;

	@Override
	public void postprocess(CodeModel model, IfThen it, List<GraphNode> roots) {
		this.model = model;
		this.it = it;

		for (GraphNode root : roots) {
			ModelStruct struct = model.getStruct(root);

			consumeStructsRecursively(struct, new ArrayList<>(), new ArrayList<>(), mergePred,
					new MergeConsumer(struct));
		}
	}

	private void consumeStructsRecursively(ModelStruct curStruct, List<ModelProperty> prpPath,
			List<ModelStruct> structPath, Predicate<ModelStruct> filter,
			BiConsumer<List<ModelProperty>, List<ModelStruct>> collect) {

		// not dealing with root
		if (!prpPath.isEmpty()) {

			if (filter.test(curStruct)) {
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

		public MergeConsumer(ModelStruct root) {
			this.root = root;
		}

		@Override
		public void accept(List<ModelProperty> prpPath, List<ModelStruct> structPath) {
			log.debug("merging into " + root.getString() + ": "
					+ structPath.stream().map(s -> s.getString()).collect(Collectors.joining(", ")) + " (removing: "
					+ prpPath.stream().map(s -> s.getString()).collect(Collectors.joining(", ")) + ")");

			structPath.forEach(s -> {
				root.replacing(s);
				model.removeStruct(s);
			});

			// remove original properties
			root.getProperties().removeAll(prpPath);
		}
	}
}
