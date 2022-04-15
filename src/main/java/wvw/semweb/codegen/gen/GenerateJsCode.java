package wvw.semweb.codegen.gen;

import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.text.CaseUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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
import wvw.semweb.codegen.model.visit.ModelVisitorA;

public class GenerateJsCode extends GenerateCode {

	protected static final Logger log = LogManager.getLogger(ModelVisitorA.class);

	private StringBuffer classes = new StringBuffer();
	private StringBuffer logic = new StringBuffer();

	public void generate(CodeModel codeModel, CodeLogic codeLogic, Collection<String> entryPoints) {
		codeModel.getAllStructs().forEach(s -> genClass(s));

		logic.append("function doSomething(").append(entryPoints.stream().collect(Collectors.joining(", ")))
				.append(") {\n");

		String contents = codeLogic.getStatements().stream().map(it -> genStatement(it))
				.collect(Collectors.joining("\n\n"));
		// indent contents of the function
		contents = "\t" + contents.replace("\n", "\n\t");
		logic.append(contents);

		logic.append("\n}");

		classes.append("\n\n");

//		log.info(classes);
//		log.info(ifThens);
	}

	public String getClasses() {
		return classes.toString();
	}

	public String getLogic() {
		return logic.toString();
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
			part2 = genCmp(Comparators.NEQ) + " " + "undefined";
			break;

		case NEX:
			part2 = genCmp(Comparators.EQ) + " " + "undefined";
			break;

		default:
			part2 = cpr + " " + op2;
			break;
		}

		if (Util.involvesArrayCheck(cmp.getOp1())) {
			NodePath path = (NodePath) cmp.getOp1();

			String subPath = genOperand(path.subPath(path.size() - 1));
			String prp = jsName(path.getPath().getLast().getString(), false);

			return subPath + ".some((e) => e." + prp + " " + part2 + ")";

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
		if (assign.getOp1() instanceof Variable)
			op1 = "var " + op1;

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
			return jsName(((Variable) op).getName(), false);

		case STRUCT_CONSTANT:
			StructConstant cnst = (StructConstant) op;
			return jsName(cnst.getStruct(), true) + "." + jsName(cnst.getConstant(), false);

		case CREATE_STRUCT:
			CreateStruct cstr = (CreateStruct) op;
			return "new " + jsName(cstr.getStruct(), true) + "()";

		case NODE_PATH:
			NodePath np = (NodePath) op;

			return genOperand(np.getStart()) + (!np.getPath().isEmpty() ? "." : "")
					+ np.getPath().stream().map(p -> jsName(p, false)).collect(Collectors.joining("."));

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
			return "===";
		case GE:
			return ">=";
		case GT:
			return ">";
		case NEQ:
			return "!==";
		case NGT:
			return "<=";
		case NLT:
			return ">=";
		default:
			return null;
		}
	}

	private void genClass(ModelStruct struct) {
		if (!classes.isEmpty())
			classes.append("\n\n");

		String clsName = jsName(struct, true);
		classes.append("class ").append(clsName).append(" {\n");

		for (ModelElement type : struct.getTypes())
			genStaticField(type);

		if (!struct.getTypes().isEmpty())
			classes.append("\n");

		for (ModelElement value : struct.getValues())
			genStaticField(value);

		if (!struct.getValues().isEmpty())
			classes.append("\n");

		for (ModelProperty prp : struct.getProperties()) {
			classes.append("\t").append(jsName(prp, false));
			if (Util.involvesArray(prp))
				classes.append(" = []");
			classes.append(";\n");
		}

		classes.append("}");
	}

	private void genStaticField(ModelElement el) {
		String jsName = jsName(el, false);

		classes.append("\tstatic ").append(jsName).append(" = '").append(jsName).append("';\n");
	}

	private String jsName(ModelElement el, boolean cls) {
		return jsName(el.getString(), cls);
	}

	private String jsName(String str, boolean cls) {
		return CaseUtils.toCamelCase(str, cls, new char[] { ' ', '_' });
	}
}
