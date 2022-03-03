package wvw.semweb.codegen;

import java.util.ArrayList;
import java.util.List;

import org.apache.jen3.graph.Node;
import org.apache.jen3.graph.NodeFactory;
import org.apache.jen3.n3.N3Model;
import org.apache.jen3.n3.N3ModelSpec;
import org.apache.jen3.n3.N3ModelSpec.Types;
import org.apache.jen3.n3.impl.N3ModelImpl.N3EventListener;
import org.apache.jen3.n3.impl.N3Rule;
import org.apache.jen3.rdf.model.ModelFactory;
import org.apache.jen3.util.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import wvw.semweb.codegen.model.ModelVisitor;
import wvw.semweb.codegen.model.ModelVisitorA;
import wvw.semweb.codegen.model.code.CodeModel;
import wvw.semweb.codegen.rule.GraphNode;
import wvw.semweb.codegen.rule.RuleGraph;
import wvw.semweb.codegen.rule.RuleGraphFactory;

public class ParseClassModel implements N3EventListener {

	private static final Logger log = LogManager.getLogger(ParseClassModel.class);

	private List<N3Rule> parsedRules = new ArrayList<>();
	private CodeModel model = new CodeModel();

	public static void main(String[] args) throws Exception {
		ParseClassModel parser = new ParseClassModel();

		parser.parseClassModel("diabetes-iot.n3", "p", "DMTO2.n3");
//		parser.parseClassModel("test.n3", "x", "ontology.n3");
//		parser.parseClassModel("test2.n3", "x", "ontology.n3");
	}

	@Override
	public void newRule(N3Rule r) {
		parsedRules.add(r);
	}

	public void parseClassModel(String rulesPath, String entry, String ontologyPath) throws Exception {
		N3Model ontology = ModelFactory.createN3Model(N3ModelSpec.get(Types.N3_MEM_FP_INF));
		ontology.read(IOUtils.getResourceInputStream(getClass(), ontologyPath), null);

		N3Model ruleset = ModelFactory.createN3Model(N3ModelSpec.get(Types.N3_MEM_FP_INF));
		ruleset.setListener(this);
		ruleset.read(IOUtils.getResourceInputStream(getClass(), rulesPath), null);

		log.debug("- parsed rules:");
		parsedRules.forEach(r -> log.debug(r + "\n"));
		log.debug("");

		if (parsedRules.isEmpty())
			log.error("no rules found in " + rulesPath);

		Node entryTerm = NodeFactory.createVariable(entry);

		for (N3Rule r : parsedRules)
			processRule(r, entryTerm, ontology);
	}

	private void processRule(N3Rule r, Node entryTerm, N3Model ontology) throws ParseModelException {
		RuleGraphFactory graphFactory = new RuleGraphFactory();
		RuleGraph ruleGraph = graphFactory.createGraph(r, entryTerm);

		log.debug("- rule graph:");
		log.debug(ruleGraph);
		log.debug("");

		GraphNode entryNode = ruleGraph.get(entryTerm);

		ModelVisitor visitor = new ModelVisitorA(ontology);
		visitor.visit(entryNode);

		CodeModel newModel = visitor.getModel();
		model.mergeWith(newModel);

		log.debug("- code model:");
		log.debug(model);
		log.debug("");

		log.debug("- condition:");
		log.debug(visitor.getCondition());
		log.debug("");

		log.debug("- code:");
		log.debug(visitor.getCode());
		log.debug("");
	}
}
