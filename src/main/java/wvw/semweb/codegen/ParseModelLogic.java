package wvw.semweb.codegen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.jen3.graph.Node;
import org.apache.jen3.graph.NodeFactory;
import org.apache.jen3.graph.n3.Node_QuickVariable;
import org.apache.jen3.n3.N3Model;
import org.apache.jen3.n3.N3ModelSpec;
import org.apache.jen3.n3.N3ModelSpec.Types;
import org.apache.jen3.n3.impl.N3ModelImpl.N3EventListener;
import org.apache.jen3.n3.impl.N3Rule;
import org.apache.jen3.rdf.model.ModelFactory;
import org.apache.jen3.util.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import wvw.semweb.codegen.model.CodeLogic;
import wvw.semweb.codegen.model.CodeLogic.IfThen;
import wvw.semweb.codegen.model.ModelVisitor;
import wvw.semweb.codegen.model.ModelVisitorA;
import wvw.semweb.codegen.model.struct.CodeModel;
import wvw.semweb.codegen.rule.GraphNode;
import wvw.semweb.codegen.rule.RuleGraph;
import wvw.semweb.codegen.rule.RuleGraphFactory;
import wvw.utils.rdf.NS;

// TODO CDS use case: automatically configure 'exam' with corresponding & persisted 'patient'
// if needed by the rules (i.e., a rule that references both the input exam & related patient)

public class ParseModelLogic implements N3EventListener {

	private static final Logger log = LogManager.getLogger(ParseModelLogic.class);

	private List<N3Rule> parsedRules = new ArrayList<>();
	private Collection<String> entryPoints = new HashSet<>();

	private CodeModel model = new CodeModel();
	private CodeLogic logic = new CodeLogic();

	@Override
	public void newRule(N3Rule r) {
		parsedRules.add(r);
	}

	public Collection<String> getEntryPoints() {
		return entryPoints;
	}

	public CodeModel getModel() {
		return model;
	}

	public CodeLogic getLogic() {
		return logic;
	}

	public void parseClassModel(String rulesPath, String ontologyPath) throws Exception {
		N3Model ontology = ModelFactory.createN3Model(N3ModelSpec.get(Types.N3_MEM_FP_INF));
		ontology.read(IOUtils.getResourceInputStream(getClass(), ontologyPath), null);

		N3Model ruleset = ModelFactory.createN3Model(N3ModelSpec.get(Types.N3_MEM_FP_INF));
		ruleset.setListener(this);
		ruleset.read(IOUtils.getResourceInputStream(getClass(), rulesPath), null);

		if (parsedRules.isEmpty())
			log.error("no rules found in " + rulesPath);

		for (N3Rule r : parsedRules) {
			log.debug("- parsed rule:\n" + r);

			List<Node> entryTerms = findEntryPoints(r, ruleset);
			if (entryTerms.isEmpty())
				throw new ParseModelException("no entry terms found for rule");

			log.debug("\n- entry points: " + entryTerms);
			entryTerms.stream().map(n -> n.getName()).forEach(e -> entryPoints.add(e));

			processRule(r, ontology, entryTerms.toArray(Node[]::new));

			log.debug("");
		}
	}

	private List<Node> findEntryPoints(N3Rule r, N3Model ruleset) {
		List<Node> ret = new ArrayList<>();

		ruleset.getGraph().find(r.getBodyNode(), NodeFactory.createURI(NS.toUri("cg:entryPoint")), null)
				.forEachRemaining(stmt -> {
					Node n = stmt.getObject();
					n = r.toRuleVar((Node_QuickVariable) n);

					ret.add(n);
				});

		return ret;
	}

	private void processRule(N3Rule r, N3Model ontology, Node[] entryTerms) throws ParseModelException {
		RuleGraphFactory graphFactory = new RuleGraphFactory();
		RuleGraph ruleGraph = graphFactory.createGraph(r, entryTerms);

		log.debug("- rule graph:");
		log.debug(ruleGraph);
		log.debug("");

		ModelVisitor visitor = new ModelVisitorA(ontology);
		for (Node entryTerm : entryTerms) {
			GraphNode entryNode = ruleGraph.get(entryTerm);

			visitor.visit(entryNode);
		}

		CodeModel newModel = visitor.getModel();
		model.mergeWith(newModel);

		log.debug("- code model:");
		log.debug(model);
		log.debug("");

		log.debug("- condition:");
		log.debug(visitor.getCondition());
		log.debug("");

		log.debug("- code:");
		log.debug(visitor.getBlock());
		log.debug("");

		// TODO currently assuming that the rule ordering reflects the chaining sequence
		IfThen it = new IfThen(visitor.getCondition(), visitor.getBlock());
		logic.add(it);
	}
}
