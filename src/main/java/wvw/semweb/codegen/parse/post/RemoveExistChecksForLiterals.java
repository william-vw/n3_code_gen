package wvw.semweb.codegen.parse.post;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import wvw.semweb.codegen.model.Comparison;
import wvw.semweb.codegen.model.Comparison.Comparators;
import wvw.semweb.codegen.model.Condition;
import wvw.semweb.codegen.model.ConditionList;
import wvw.semweb.codegen.model.Conjunction;
import wvw.semweb.codegen.model.IfThen;
import wvw.semweb.codegen.model.Operand;
import wvw.semweb.codegen.model.Operand.Operands;
import wvw.semweb.codegen.model.struct.CodeModel;
import wvw.semweb.codegen.parse.rule.RuleGraph;

public class RemoveExistChecksForLiterals extends ModelPostprocessor {

	// comparisons are separate statements; another separate statement will refer to
	// a variable to be used in this comparison; an "exists" condition will
	// be added for the latter. but, this check is redundant, so remove those here.

	@Override
	public void postprocess(CodeModel model, IfThen it, RuleGraph ruleGraph) {
		Conjunction cond = (Conjunction) it.getCondition();

		List<Operand> toRemove = new ArrayList<>();
		collectToRemove(cond, toRemove);

		if (toRemove.isEmpty())
			return;

		doRemove(cond, toRemove);
	}

	private void collectToRemove(ConditionList list, List<Operand> toRemove) {
		list.getConditions().forEach(c -> {

			switch (c.getConditionType()) {

			case CONJ:
			case DISJ:
				collectToRemove((ConditionList) c, toRemove);
				break;

			case CMP:
				Comparison cmp = (Comparison) c;
				if (cmp.getCmp() != Comparators.EX && cmp.getOp1().getType() == Operands.NODE_PATH
						&& cmp.getOp2().getType() == Operands.LITERAL)
					toRemove.add(cmp.getOp1());

				break;
			}
		});
	}

	private void doRemove(ConditionList list, List<Operand> toRemove) {
		Iterator<Condition> condIt = list.getConditions().iterator();
		while (condIt.hasNext()) {
			Condition cond = condIt.next();

			switch (cond.getConditionType()) {

			case CONJ:
			case DISJ:
				doRemove((ConditionList) cond, toRemove);
				break;

			case CMP:
				Comparison cmp = (Comparison) cond;

				// remove "exists" checks for paths found in literal comparisons
				if (cmp.getCmp() == Comparators.EX && cmp.getOp1().getType() == Operands.NODE_PATH
						&& toRemove.contains(cmp.getOp1()))

					condIt.remove();

				break;
			}
		}
	}
}
