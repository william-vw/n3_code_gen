package wvw.semweb.codegen.gen;

import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.text.CaseUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import wvw.semweb.codegen.model.CodeLogic;
import wvw.semweb.codegen.model.CodeLogic.IfThen;
import wvw.semweb.codegen.model.Literal;
import wvw.semweb.codegen.model.ModelVisitorA;
import wvw.semweb.codegen.model.NodePath;
import wvw.semweb.codegen.model.Operand;
import wvw.semweb.codegen.model.StructConstant;
import wvw.semweb.codegen.model.Variable;
import wvw.semweb.codegen.model.block.Assignment;
import wvw.semweb.codegen.model.block.CreateStruct;
import wvw.semweb.codegen.model.block.Operation;
import wvw.semweb.codegen.model.cond.Comparison;
import wvw.semweb.codegen.model.cond.Comparison.Comparators;
import wvw.semweb.codegen.model.struct.CodeModel;
import wvw.semweb.codegen.model.struct.ModelElement;
import wvw.semweb.codegen.model.struct.ModelProperty;
import wvw.semweb.codegen.model.struct.ModelStruct;

public class GenerateJsCode implements GenerateCode {

	protected static final Logger log = LogManager.getLogger(ModelVisitorA.class);

	private StringBuffer classes = new StringBuffer();
	private StringBuffer logic = new StringBuffer();

	public void generate(CodeModel codeModel, CodeLogic codeLogic, Collection<String> entryPoints) {
		codeModel.getAllStructs().forEach(s -> genClass(s));

		logic.append("function doSomething(").append(entryPoints.stream().collect(Collectors.joining(", ")))
				.append(") {");
		codeLogic.getIfThens().forEach(it -> genIfThen(it));
		logic.append("\n}");

		classes.append("\n\n");
		logic.append("\n\n");

//		log.info(classes);
//		log.info(ifThens);
	}

	public String getClasses() {
		return classes.toString();
	}

	public String getLogic() {
		return logic.toString();
	}

	private void genIfThen(IfThen ifThen) {
		String ifContents = ifThen.getCondition().getConditions().stream().map(c -> genCondition(c))
				.collect(Collectors.joining("\n\t\t&& "));
		String thenContents = ifThen.getBlock().getOperations().stream().map(o -> genOperation(o))
				.collect(Collectors.joining("\n\t\t"));

		if (!logic.isEmpty())
			logic.append("\n\n");

		logic.append("\tif (").append(ifContents).append(") {\n\n\t\t").append(thenContents).append("\n\t}");
	}

	private String genCondition(Comparison cmp) {
		String cpr = genCmp(cmp.getCmp());
		String op1 = genOperand(cmp.getOp1());
		String op2 = genOperand(cmp.getOp2());

		return op1 + " " + cpr + " " + op2;
	}

	private String genOperation(Operation op) {
		Assignment asn = (Assignment) op;

		String cmp = "=";
		String op1 = genOperand(asn.getOp1());
		if (asn.getOp1() instanceof Variable)
			op1 = "var " + op1;

		String op2 = genOperand(asn.getOp2());

		return op1 + " " + cmp + " " + op2 + ";";
	}

	private String genOperand(Operand op) {
		if (op instanceof Literal) {
			Object o = ((Literal) op).getValue();
			if (o instanceof String)
				return "\"" + o + "\"";
			else
				return o.toString();
		}

		if (op instanceof Variable)
			return jsName(((Variable) op).getName(), false);

		if (op instanceof StructConstant) {
			StructConstant cnst = (StructConstant) op;
			return jsName(cnst.getStruct(), true) + "." + jsName(cnst.getConstant(), false);
		}

		if (op instanceof CreateStruct) {
			CreateStruct cstr = (CreateStruct) op;
			return "new " + jsName(cstr.getStruct(), true) + "()";
		}

		if (op instanceof NodePath) {
			NodePath np = (NodePath) op;
			return genOperand(np.getStart()) + (!np.getPath().isEmpty() ? "." : "")
					+ np.getPath().stream().map(p -> jsName(p, false)).collect(Collectors.joining("."));
		}

		return null;
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
		case EX:
			return "!==";
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
			if (!prp.hasMaxCardinality() || prp.getMaxCardinality() != 1)
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
