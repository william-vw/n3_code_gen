package wvw.semweb.owl.gen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.jen3.graph.Node;
import org.apache.jen3.n3.N3Model;
import org.apache.jen3.n3.N3ModelSpec;
import org.apache.jen3.n3.N3ModelSpec.Types;
import org.apache.jen3.n3.impl.N3ModelImpl.N3EventListener;
import org.apache.jen3.n3.impl.N3Rule;
import org.apache.jen3.rdf.model.ModelFactory;
import org.apache.jen3.rdf.model.Resource;
import org.apache.jen3.util.IOUtils;
import org.apache.jen3.vocabulary.RDF;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import wvw.semweb.owl.gen.graph.GraphNode;
import wvw.semweb.owl.gen.model.ModelEdge;
import wvw.semweb.owl.gen.model.ModelNode;

public class ParseClassModel implements N3EventListener {

	private static final Logger log = LogManager.getLogger(ParseClassModel.class);

	private List<N3Rule> parsedRules = new ArrayList<>();

	private Map<String, ModelNode> allClsMap = new HashMap<>();

	public static void main(String[] args) throws Exception {
		ParseClassModel parser = new ParseClassModel();

		parser.parseClassModel("diabetes-iot.n3", "DMTO.n3");
	}

	@Override
	public void newRule(N3Rule r) {
		parsedRules.add(r);
	}

	public void parseClassModel(String rulesPath, String ontologyPath) throws Exception {
		N3Model ontology = ModelFactory.createN3Model(N3ModelSpec.get(Types.N3_MEM_FP_INF));
		ontology.read(IOUtils.getResourceInputStream(getClass(), ontologyPath), null);

		N3Model ruleset = ModelFactory.createN3Model(N3ModelSpec.get(Types.N3_MEM_FP_INF));
		ruleset.setListener(this);
		ruleset.read(IOUtils.getResourceInputStream(getClass(), rulesPath), null);

		String entryTerm = "p";

		log.debug("- parsed rules:");
		parsedRules.forEach(r -> log.debug(r + "\n"));
		log.debug("");

		if (parsedRules.isEmpty())
			log.error("no rules found in " + rulesPath);

		else {
			for (N3Rule r : parsedRules)
				processRule(r, entryTerm, ontology);
		}

		log.info("> final model:");
		log.info(allClsMap.values().stream().map(v -> v.toString()).collect(Collectors.joining("\n")));
	}

	private void processRule(N3Rule r, String entryTerm, N3Model ontology) throws ParseModelException {
		RuleGraphFactory graphFactory = new RuleGraphFactory();
		Map<String, GraphNode> termNode = graphFactory.createGraph(r, entryTerm);

		log.debug("- terms to nodes:");
		log.debug(termNode.entrySet().stream().map(e -> e.toString()).collect(Collectors.joining("\n")));
		log.debug("");

		GraphNode entryNode = termNode.get(entryTerm);
		Collection<ModelNode> modelNodes = ModelGraphFactory.toModel(entryNode);

		ModelOntologyAnalyser analyser = new ModelOntologyAnalyser(ontology);

		// - try and find types for terms

		log.debug("- trying to find types for model nodes");
		modelNodes.forEach(node -> {
			if (!node.hasName()) {

				// -- look for explicit types given in the rule

				List<String> types = new ArrayList<>();

				Iterator<ModelEdge> edges = node.getOut().iterator();
				while (edges.hasNext()) {
					ModelEdge edge = edges.next();

					if (edge.getName().equals(RDF.type.getURI())) {
						edges.remove();
						types.add(edge.getObjectType().getName());
					}
				}

				if (types.size() > 0) {
					if (types.size() > 1) {
						log.error("found multiple types specified in rule for " + node.getOr() + ": " + types
								+ ", using first one");
					}

					node.setName(types.get(0));

				} else {
					// else, look into ontology (check domains, ranges of properties)

					List<Resource> ontologyTypes = analyser.findTypes(node);

					if (ontologyTypes.size() > 0) {
						if (ontologyTypes.size() > 1) {
							log.error("found multiple domains and/or ranges for " + node.getOr() + ": " + ontologyTypes
									+ ", using first one");
						}

						node.setName(ontologyTypes.get(0).getURI());
					}
				}
			}
		});
		log.debug("");

		log.debug("- trying to find annotations for model");
		modelNodes.forEach(node -> {
			if (node.hasName()) {
				analyser.findAnnotations(node);
				node.getOut().forEach(p -> analyser.findAnnotations(p));
			}
		});
		log.debug("");

		// - at this point, we've collected all possible information about the rule's
		// nodes; let's now merge nodes with same types into a single node

		Map<String, ModelNode> clsMap = modelNodes.stream()
				.collect(Collectors.toMap(v -> v.getName(), v -> v, (v1, v2) -> {
					v1.replacing(v2);
					return v1;
				}));

		log.debug("- merged model:");
		log.debug(clsMap.values().stream().map(v -> v.toString()).collect(Collectors.joining("\n")));
		log.debug("");

		// - find ranges for the found properties

		log.debug("- trying to find all property types");
		clsMap.values().forEach(node -> node.getOut().forEach(p -> analyser.findPropertyType(p)));
		log.debug("");

		log.debug("- updated model (2):");
		log.debug(clsMap.values().stream().map(v -> v.toString()).collect(Collectors.joining("\n")));
		log.debug("");

		log.debug("");
		log.debug("");

		// - add found model classes
		// (if needed, merge with prior classes)

		clsMap.entrySet().forEach(e -> {
			String name = e.getKey();
			ModelNode cls = e.getValue();

			if (allClsMap.containsKey(name)) {
				ModelNode cls0 = allClsMap.get(cls.getName());
				cls0.replacing(cls);

			} else
				allClsMap.put(name, cls);
		});
	}

	private boolean isBuiltin(Node predicate) {
		return predicate.getURI().startsWith("http://www.w3.org/2000/10/swap/");
	}
}
