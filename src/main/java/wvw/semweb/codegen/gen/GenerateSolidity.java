package wvw.semweb.codegen.gen;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.apache.jen3.datatypes.RDFDatatype;
import org.apache.jen3.util.IOUtils;

import wvw.semweb.codegen.model.Assignment;
import wvw.semweb.codegen.model.Block;
import wvw.semweb.codegen.model.CodeLogic;
import wvw.semweb.codegen.model.CodeStatement;
import wvw.semweb.codegen.model.Comparison;
import wvw.semweb.codegen.model.Comparison.Comparators;
import wvw.semweb.codegen.model.Condition;
import wvw.semweb.codegen.model.Condition.Conditions;
import wvw.semweb.codegen.model.Conjunction;
import wvw.semweb.codegen.model.CreateStruct;
import wvw.semweb.codegen.model.IfThen;
import wvw.semweb.codegen.model.Literal;
import wvw.semweb.codegen.model.NodePath;
import wvw.semweb.codegen.model.Operand;
import wvw.semweb.codegen.model.StructConstant;
import wvw.semweb.codegen.model.Variable;
import wvw.semweb.codegen.model.struct.CodeModel;
import wvw.semweb.codegen.model.struct.ModelElement;
import wvw.semweb.codegen.model.struct.ModelProperty;
import wvw.semweb.codegen.model.struct.ModelStruct;
import wvw.semweb.codegen.model.struct.ModelType;

public class GenerateSolidity extends GenerateCode {

	private StringBuffer structs = new StringBuffer();
	private StringBuffer logic = new StringBuffer();

	@Override
	public void generate(CodeModel codeModel, CodeLogic codeLogic, Collection<String> entryPoints, File output)
			throws IOException {

		generateStructs(codeModel);
		generateFunction(codeLogic, entryPoints);

		StringBuffer out = new StringBuffer();
		// @formatter:off
		out.append("// Specifies the version of Solidity, using semantic versioning.\n")
			.append("// Learn more: https://solidity.readthedocs.io/en/v0.5.10/layout-of-source-files.html#pragma\n")
			.append("pragma solidity ^0.7.0;")
			.append("\n\n");

		out.append("contract ").append(contractName(codeLogic.getRulesName())).append(" {\n");
		
		StringBuffer contents = new StringBuffer()
			.append("// Declares a state variable `message` of type `string`.\n")
			.append("// State variables are variables whose values are permanently stored in contract storage. The keyword `public` makes variables accessible from outside a contract and creates a function that other contracts or clients can call to access the value.\n")
			.append("string public message;\n\n")
			.append("// Similar to many class-based object-oriented languages, a constructor is a special function that is only executed upon contract creation.\n")
			.append("// Constructors are used to initialize the contract's data. Learn more:https://solidity.readthedocs.io/en/v0.5.10/contracts.html#constructors\n")
			.append("constructor(string memory initMessage) {\n\n")
			.append("	// Accepts a string argument `initMessage` and sets the value into the contract's `message` storage variable).\n")
			.append("	message = initMessage;\n")
			.append("}");
		contents.append("\n\n");
		
		contents.append("function containsWithType(list, el) {\n");
		contents.append("	for (uint i = 0; i < list.length; i++) {\n");
		contents.append("		if (el == list[i].type) {\n");
		contents.append("			return true;\n");
		contents.append("		}\n");
		contents.append("	}\n");
		contents.append("	return false;\n");
		contents.append("}");
		contents.append("\n\n");
		
		contents.append(structs).append("\n\n").append(logic).append("\n\n");
		
		contents.append("// A public function that accepts a string argument and updates the `message` storage variable.\n")
			.append("function update(string memory newMessage) public {\n")
			.append("	message = newMessage;\n")
			.append("}");
		// @formatter:on

		String contentStr = contents.toString();
		contentStr = "\t" + contentStr.replace("\n", "\n\t");

		out.append(contentStr);

		out.append("}");

		IOUtils.writeToFile(out.toString(), output);
	}

	private void generateStructs(CodeModel codeModel) {
		codeModel.getAllStructs().forEach(s -> genStruct(s));
	}

	private void generateFunction(CodeLogic codeLogic, Collection<String> entryPoints) {
		logic.append("function doSomething(").append(entryPoints.stream().collect(Collectors.joining(", ")))
				.append(") {\n");

		String contents = codeLogic.getStatements().stream().map(it -> genStatement(it))
				.collect(Collectors.joining("\n\n"));
		// indent contents of the function
		contents = "\t" + contents.replace("\n", "\n\t");
		logic.append(contents);

		logic.append("\n}");
	}

	private String genStatement(CodeStatement stmt) {
		switch (stmt.getStatementType()) {

		case COND:
			return genCondition((Condition) stmt);

		case IF_THEN:
			return genIfThen((IfThen) stmt);

		case BLOCK:
			return genBlock((Block) stmt);

		case ASSIGN:
			return genAssignment((Assignment) stmt);

		default:
			return null;
		}
	}

	private String genCondition(Condition cond) {
		switch (cond.getConditionType()) {

		case CONJ:
			return genConjunction((Conjunction) cond);

		case CMP:
			return genComparison((Comparison) cond);

		default:
			return null;
		}
	}

	private String genConjunction(Conjunction conj) {
		return conj.getConditions().stream().map(c -> genCondition(c)).collect(Collectors.joining("\n\t&& "));
	}

	private String genComparison(Comparison cmp) {
		String cpr = genCmp(cmp.getCmp());
		String op2 = genOperand(cmp.getOp2());

		String part2;
		switch (cmp.getCmp()) {

		case EX:
			part2 = genCmp(Comparators.NEQ) + " " + "false";
			break;

		case NEX:
			part2 = genCmp(Comparators.EQ) + " " + "false";
			break;

		default:
			part2 = cpr + " " + op2;
			break;
		}

		if (Util.involvesArrayCheck(cmp.getOp1())) {
			NodePath path = (NodePath) cmp.getOp1();

			String subPath = genOperand(path.subPath(path.size() - 1));
			String prp = fieldName(path.getPath().getLast());

			if (prp.equals("type"))
				return "containsWithType(" + subPath + ", " + op2 + ")";

			else {
				log.error("unsupported property for contains: " + prp);
				return "null";
			}

		} else {
			String op1 = genOperand(cmp.getOp1());
			return op1 + " " + part2;
		}
	}

	private String genIfThen(IfThen ifThen) {
		String ifContents = genCondition(ifThen.getCondition());

		String thenContents = genStatement(ifThen.getThen());
		// indent 'then' part of the ifThen
		thenContents = "\t" + thenContents.replace("\n", "\n\t").trim();

		String ret = "if (" + ifContents + ") {\n";
		// add extra newline after conditions
		if (ifThen.getCondition().getConditionType() == Conditions.CONJ)
			ret += "\n";
		ret += thenContents + "\n}";

		return ret;
	}

	private String genBlock(Block block) {
		return block.getStatements().stream().map(stmt -> genStatement(stmt)).collect(Collectors.joining("\n")) + "\n";
	}

	private String genAssignment(Assignment assign) {
		String op1 = genOperand(assign.getOp1());

		// assigning to variable

		if (assign.getOp1() instanceof Variable) {
			String typeName = null;

			switch (assign.getOp2().getType()) {

			case CREATE_STRUCT:
				// get type of new struct that we're creating; this will be the variable's type
				ModelStruct struct = ((CreateStruct) assign.getOp2()).getStruct();
				typeName = structName(struct);

				break;

			case NODE_PATH:
				NodePath path = (NodePath) assign.getOp2();
				ModelType lastTarget = path.getPath().getLast().getTarget();
				if (lastTarget.hasObjectType())
					typeName = structName(lastTarget.getObjectType());
				else
					typeName = solDatatype(lastTarget.getDataType());

				break;

			default:
				log.error("unsupported operand type for assignment: " + assign.getOp1().getType());
				break;
			}

			op1 = typeName + " memory " + op1;
		}

		String op2 = genOperand(assign.getOp2());

		if (Util.involvesArrayAssign(assign.getOp1()))
			return op1 + ".push(" + op2 + ");";
		else
			return op1 + " = " + op2 + ";";
	}

	private String genOperand(Operand op) {
		if (op == null)
			return null;

		switch (op.getType()) {

		case LITERAL:
			Object o = ((Literal) op).getValue();
			if (o instanceof String)
				return "\"" + o + "\"";
			else
				return o.toString();

		case VAR:
			return varName((Variable) op);

		case STRUCT_CONSTANT:
			StructConstant cnst = (StructConstant) op;

			// get corresponding enum name
			String enumName = enumName(cnst.getStruct());
			// refer to enum field
			return enumName + "." + enumFieldName(cnst.getConstant());

		case CREATE_STRUCT:
			CreateStruct cstr = (CreateStruct) op;
			return structName(cstr.getStruct()) + "()";

		case NODE_PATH:
			NodePath np = (NodePath) op;

			return genOperand(np.getStart()) + (!np.getPath().isEmpty() ? "." : "")
					+ np.getPath().stream().map(p -> fieldName(p)).collect(Collectors.joining("."));

		default:
			return null;
		}
	}

	private String genCmp(Comparators cpr) {
		switch (cpr) {
		case LT:
			return "<";
		case LE:
			return "<=";
		case EQ:
			return "==";
		case GE:
			return ">=";
		case GT:
			return ">";
		case NEQ:
			return "!=";
		case NGT:
			return "<=";
		case NLT:
			return ">=";
		default:
			return null;
		}
	}

	private void genStruct(ModelStruct struct) {
		if (!structs.isEmpty())
			structs.append("\n\n");

		// add any required enum to keep types & uris

		Iterator<ModelElement> cnsts = struct.getConstants();
		if (cnsts.hasNext()) {
			String enumName = enumName(struct);
			structs.append("enum ").append(enumName).append("{ ");

			int cnt = 0;
			while (cnsts.hasNext()) {
				if (cnt++ > 0)
					structs.append(", ");

				ModelElement cnst = cnsts.next();
				structs.append(enumFieldName(cnst));
			}

			structs.append(" }");
			structs.append("\n\n");
		}

		// add the actual struct

		structs.append("struct ").append(structName(struct)).append(" {\n");

		for (ModelProperty prp : struct.getProperties())
			genField(prp, struct);

		structs.append("}");
	}

	private void genField(ModelProperty prp, ModelStruct ofStruct) {
		structs.append("\t");

		if (prp.isTypePrp()) {
			String structName = enumName(ofStruct);
			structs.append(structName);

		} else {
			ModelType target = prp.getTarget();
			if (target.hasObjectType())
				structs.append(structName(target.getObjectType()));
			else
				structs.append(solDatatype(target.getDataType()));
		}

		if (Util.involvesArray(prp))
			structs.append("[]");

		structs.append(" ").append(fieldName(prp));

		structs.append(";\n");
	}

	private String contractName(String name) {
		return solName(name, true);
	}
	
	private String structName(ModelStruct struct) {
		return solName(struct, true);
	}

	private String enumName(ModelStruct struct) {
		return structName(struct) + "Constants";
	}

	private String fieldName(ModelElement el) {
		return solName(el, false);
	}

	private String enumFieldName(ModelElement el) {
		return StringUtils.capitalize(fieldName(el)); //.toUpperCase();
	}

	private String varName(Variable var) {
		return solName(var.getName(), false);
	}

	private String solName(ModelElement el, boolean clsName) {
		return solName(el.getString(), clsName);
	}

	private String solName(String str, boolean clsName) {
		return CaseUtils.toCamelCase(str, clsName, new char[] { ' ', '_', '-' });
	}

	private String solDatatype(RDFDatatype dt) {
		log.info("rdf datatype: " + dt.getURI());

		String ln = dt.getURI().substring(dt.getURI().lastIndexOf("#") + 1);
		if (ln.equalsIgnoreCase("string"))
			return "string";
		if (ln.equalsIgnoreCase("integer") || ln.equalsIgnoreCase("int"))
			return "int";
		if (ln.equalsIgnoreCase("boolean"))
			return "bool";
		if (ln.equals("float")) {
			log.warn("no float support in solidity, returning int");
			return "int";
		}

		log.error("currently unsupported datatype: " + dt.getURI());

		return ln;
	}
}
