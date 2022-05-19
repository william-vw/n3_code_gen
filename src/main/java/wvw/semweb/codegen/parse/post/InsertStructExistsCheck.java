package wvw.semweb.codegen.parse.post;

import java.util.List;
import java.util.stream.Collectors;

import wvw.semweb.codegen.gen.Util;
import wvw.semweb.codegen.model.Assignment;
import wvw.semweb.codegen.model.Block;
import wvw.semweb.codegen.model.CodeStatement.Codes;
import wvw.semweb.codegen.model.Comparison;
import wvw.semweb.codegen.model.Comparison.Comparators;
import wvw.semweb.codegen.model.CreateStruct;
import wvw.semweb.codegen.model.IfThen;
import wvw.semweb.codegen.model.NodePath;
import wvw.semweb.codegen.model.Variable;
import wvw.semweb.codegen.model.struct.CodeModel;
import wvw.semweb.codegen.parse.rule.RuleGraph;

public class InsertStructExistsCheck extends ModelPostprocessor {

	// for create-struct assignment blocks:
	// modify the code block to check whether something already exists at end of
	// path (visitor code will simply assign a new struct each time)

	// (difficult to do this in visitor since we should only do this for
	// intermediary assignments (i.e., not for the last assignment))

	@Override
	public void postprocess(CodeModel model, IfThen it, RuleGraph ruleGraph) {
		// TODO currently only blocks will be created for create-struct assignment
		// blocks; should somehow tag these blocks for extensibility ..

		Block block = (Block) it.getThen();
		List<Block> subBlocks = block.getStatements().stream().filter(stmt -> stmt.getStatementType() == Codes.BLOCK)
				.map(stmt -> (Block) stmt).collect(Collectors.toList());

		for (int i = 0; i < subBlocks.size() - 1; i++) {
			Block subBlock = subBlocks.get(i);

			Assignment asn = (Assignment) subBlock.getStatements().get(0);
			Assignment asn2 = (Assignment) subBlock.getStatements().get(1);

			Variable var = (Variable) asn.getOp1();
			CreateStruct createStruct = (CreateStruct) asn.getOp2();
			NodePath path = (NodePath) asn2.getOp1();

			if (Util.involvesArrayAssign(path)) {

			} else {
				subBlock.clear();

				// check whether struct at current path exists
				// if not, create new struct and assign to path

				Comparison notExists = new Comparison(path, Comparators.NEX);
				Assignment newAsn = new Assignment(path, createStruct);

				subBlock.add(new IfThen(notExists, newAsn));

				// assign (possibly new) struct at end of path to variable

				Assignment newAsn2 = new Assignment(var, path);
				subBlock.add(newAsn2);
			}
		}
	}
}
