package wvw.semweb.codegen.model;

import org.apache.jen3.n3.N3Model;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import wvw.semweb.codegen.model.code.CodeModel;
import wvw.semweb.codegen.model.cond.Conjunction;
import wvw.semweb.codegen.model.op.Block;
import wvw.semweb.codegen.rule.GraphNode;

public abstract class ModelVisitor {

	protected static final Logger log = LogManager.getLogger(ModelVisitorA.class);

	protected N3Model ontology;

	protected CodeModel model = new CodeModel();
	protected Conjunction cond = new Conjunction();
	protected Block code = new Block();

	public ModelVisitor(N3Model ontology) {
		this.ontology = ontology;
	}

	public CodeModel getModel() {
		return model;
	}

	public Conjunction getCondition() {
		return cond;
	}

	public Block getCode() {
		return code;
	}

	public abstract void visit(GraphNode entryNode);
}
