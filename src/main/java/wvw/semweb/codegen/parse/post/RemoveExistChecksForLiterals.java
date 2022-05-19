package wvw.semweb.codegen.parse.post;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import wvw.semweb.codegen.model.Comparison;
import wvw.semweb.codegen.model.Comparison.Comparators;
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
		// check for literal comparisons & keep found paths
		cond.getConditions().forEach(c -> {
			if (c.getCmp() != Comparators.EX && c.getOp1().getType() == Operands.NODE_PATH
					&& c.getOp2().getType() == Operands.LITERAL)
				toRemove.add(c.getOp1());
		});

		if (toRemove.isEmpty())
			return;

		Iterator<Comparison> cmpIt = cond.getConditions().iterator();
		while (cmpIt.hasNext()) {
			Comparison cmp = cmpIt.next();

			// remove "exists" checks for paths found in literal comparisons
			if (cmp.getCmp() == Comparators.EX && cmp.getOp1().getType() == Operands.NODE_PATH
					&& toRemove.contains(cmp.getOp1()))

				cmpIt.remove();
		}
	}
}
