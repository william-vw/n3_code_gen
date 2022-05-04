package wvw.semweb.codegen;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
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
import wvw.semweb.codegen.model.IfThen;
import wvw.semweb.codegen.model.struct.CodeModel;
import wvw.semweb.codegen.model.visit.ModelVisitor;
import wvw.semweb.codegen.model.visit.ModelVisitorA;
import wvw.semweb.codegen.rule.GraphNode;
import wvw.semweb.codegen.rule.RuleGraph;
import wvw.semweb.codegen.rule.RuleGraphFactory;
import wvw.utils.rdf.NS;

// (major)

// TODO CDS use case: automatically configure 'exam' with corresponding & persisted 'patient'
// if needed by the rules (i.e., a rule that references both the input exam & related patient)

// TODO currently assuming that the rule ordering reflects the chaining sequence

// (minor)

//TODO parametrize ModelVisitorImpl code (e.g., CodeLogicVisitor, CodeModelVisitor)

//TODO post-processing where structs sharing a (non-trivial) superclass (i.e., not owl:Thing, entity, ..)
//are merged together

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

	public void parseClassModel(File rulesFile, File ontologyFile) throws Exception {
		logic.setRulesName(FilenameUtils.removeExtension(rulesFile.getName()));

		N3Model ontology = ModelFactory.createN3Model(N3ModelSpec.get(Types.N3_MEM_FP_INF));
		ontology.read(IOUtils.getResourceInputStream(getClass(), ontologyFile.getPath()), null);

		N3Model ruleset = ModelFactory.createN3Model(N3ModelSpec.get(Types.N3_MEM_FP_INF));
		ruleset.setListener(this);
		ruleset.read(IOUtils.getResourceInputStream(getClass(), rulesFile.getPath()), null);

		if (parsedRules.isEmpty())
			log.error("no rules found in " + rulesFile.getPath());

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

		List<Node> allEntries = new ArrayList<>(Arrays.asList(entryTerms));
		RuleGraph ruleGraph = graphFactory.createGraph(r, allEntries);

		log.debug("- rule graph:");
		log.debug(ruleGraph);
		log.debug("");

		List<GraphNode> entryNodes = allEntries.stream().map(t -> ruleGraph.get(t)).collect(Collectors.toList());

		ModelVisitor visitor = new ModelVisitorA(ontology);
		visitor.visit(entryNodes);

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

		IfThen it = new IfThen(visitor.getCondition(), visitor.getBlock());
		logic.add(it);
	}
}