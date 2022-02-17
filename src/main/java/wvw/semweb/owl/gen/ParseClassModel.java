package wvw.semweb.owl.gen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jen3.datatypes.RDFDatatype;
import org.apache.jen3.datatypes.TypeMapper;
import org.apache.jen3.datatypes.xsd.XSDDatatype;
import org.apache.jen3.graph.Node;
import org.apache.jen3.n3.N3Model;
import org.apache.jen3.n3.N3ModelSpec;
import org.apache.jen3.n3.N3ModelSpec.Types;
import org.apache.jen3.n3.impl.N3ModelImpl.N3EventListener;
import org.apache.jen3.n3.impl.N3Rule;
import org.apache.jen3.rdf.model.ModelFactory;
import org.apache.jen3.rdf.model.Resource;
import org.apache.jen3.rdf.model.Statement;
import org.apache.jen3.rdf.model.StmtIterator;
import org.apache.jen3.reasoner.TriplePattern;
import org.apache.jen3.util.IOUtils;
import org.apache.jen3.vocabulary.RDF;
import org.apache.jen3.vocabulary.RDFS;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import wvw.semweb.owl.gen.model.ModelClass;
import wvw.semweb.owl.gen.model.ModelElement;
import wvw.semweb.owl.gen.model.ModelProperty;

public class ParseClassModel implements N3EventListener {

	private static final Logger log = LogManager.getLogger(ParseClassModel.class);

	private List<N3Rule> parsedRules = new ArrayList<>();

	private Map<String, ModelClass> allClsMap = new HashMap<>();

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

		log.debug("- parsed rules:");
		parsedRules.forEach(r -> {
			log.debug(r + "\n");
		});
		log.debug("");

		if (parsedRules.isEmpty())
			log.error("no rules found in " + rulesPath);
		else
			parsedRules.stream().forEach(r -> processRule(r, ontology));

		log.info("> final model:");
		log.info(allClsMap.values().stream().map(v -> v.toString()).collect(Collectors.joining("\n")));
	}

	private void processRule(N3Rule r, N3Model ontology) {
		ParsedRuleVisitor visitor = new ParsedRuleVisitor();
		Map<Node, ModelClass> termCls = visitor.visit(r);

		log.debug("- terms to model:");
		log.debug(termCls.entrySet().stream().map(e -> e.toString()).collect(Collectors.joining("\n")));
		log.debug("");

		// - try and find types for terms without explicit type
		// based on their properties in the rule

		log.debug("- trying to find types from ontology");
		termCls.entrySet().forEach(e -> {
			Node term = e.getKey();
			ModelClass cls = e.getValue();
			if (!cls.hasName()) {
				Resource type = findSingleType(cls, ontology, term);
				if (type != null)
					cls.setName(type.getURI());
			}
		});
		log.debug("");

		log.debug("- trying to find annotations");
		termCls.values().forEach(cls -> {
			if (cls.hasName()) {
				findAnnotations(cls, ontology);
				cls.getOut().forEach(p -> findAnnotations(p, ontology));
			}
		});
		log.debug("");

		Map<String, ModelClass> clsMap = termCls.values().stream()
				.collect(Collectors.toMap(v -> v.getName(), v -> v, (v1, v2) -> {
					v1.replacing(v2);
					return v1;
				}));

		log.debug("- updated model:");
		log.debug(clsMap.values().stream().map(v -> v.toString()).collect(Collectors.joining("\n")));
		log.debug("");

		// - find ranges for the found properties

		log.debug("- trying to find all property types");
		clsMap.values().forEach(cls -> cls.getOut().forEach(p -> findPropertyType(p, ontology, clsMap)));
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
			ModelClass cls = e.getValue();

			if (allClsMap.containsKey(name)) {
				ModelClass cls0 = allClsMap.get(cls.getName());
				cls0.replacing(cls);

			} else
				allClsMap.put(name, cls);
		});
	}

	private Resource findSingleType(ModelClass cls, N3Model ontology, Node term) {
//		log.debug("finding domain for: " + properties);

		List<Resource> allDomains = cls.getOut().stream()
				.flatMap(
						p -> ontology.listStatements(ontology.createResource(p.getName()), RDFS.domain, (Resource) null)
								.toList().stream().map(stmt -> stmt.getObject()))
				.collect(Collectors.toList());

		List<Resource> allRanges = cls.getIn().stream()
				.flatMap(p -> ontology.listStatements(ontology.createResource(p.getName()), RDFS.range, (Resource) null)
						.toList().stream().map(stmt -> stmt.getObject()))
				.collect(Collectors.toList());

		List<Resource> allTypes = new ArrayList<>();
		allTypes.addAll(allDomains);
		allTypes.addAll(allRanges);
		filterSuperClasses(allTypes);

		log.debug("> class:\n" + cls);
		log.debug("> domains:\n" + allDomains);
		log.debug("> ranges:\n" + allRanges);
		log.debug("> most specific types:\n" + allTypes);

		log.debug("\n");

		if (allTypes.size() == 1) {
			Resource type = allTypes.get(0);
			if (type.getURI().startsWith(XSDDatatype.XSD))
				return null;
			else
				return type;
		}

		else {
			log.error("found multiple domains and/or ranges for " + cls + ":\ndomains: " + allDomains + "\nranges: "
					+ allRanges);
			return null;
		}
	}

	private void findAnnotations(ModelElement el, N3Model ontology) {
		Resource label = ontology.createResource(el.getName()).getPropertyResourceValue(RDFS.label);
		if (label != null)
			el.setLabel(label.asLiteral().getString());
	}

	private void findPropertyType(ModelProperty property, N3Model ontology, Map<String, ModelClass> clsMap) {
		StmtIterator it = ontology.listStatements(ontology.createResource(property.getName()), RDFS.range,
				(Resource) null);

		while (it.hasNext()) {
			Statement stmt = it.next();

			Resource type = stmt.getObject();
			if (!type.isURI())
				log.debug("found non-URI range for " + property);

			else {
				if (type.getURI().startsWith(XSDDatatype.XSD)) {
					RDFDatatype dt = TypeMapper.getInstance().getTypeByName(type.getURI());

					if (dt == null)
						log.debug("could not find datatype for XSD range " + type);
					else
						property.setDataType(dt);
				}
			}
		}

		if (!property.hasType())
			log.error("no (usable) object or data type found for " + property);
	}

	private void filterSuperClasses(List<Resource> clses) {
		l0: for (int i = 0; i < clses.size(); i++) {
			Resource cls1 = clses.get(i);

			for (int j = 0; j < clses.size(); j++) {
				if (i == j)
					continue;

				Resource cls2 = clses.get(j);
				if (isMoreGeneral(cls1, cls2)) {
					clses.remove(i--);

					continue l0;
				}
			}
		}
	}

	private boolean isMoreGeneral(Resource cls1, Resource cls2) {
		Set<Resource> found = new HashSet<>();

		LinkedList<Resource> supClses = new LinkedList<>();
		supClses.add(cls2);

		while (!supClses.isEmpty()) {
			Resource supCls = supClses.removeFirst();

			if (found.contains(supCls))
				continue;
			else
				found.add(supCls);

			if (supCls.equals(cls1))
				return true;

			supClses.addAll(supCls.listProperties(RDFS.subClassOf).toList().stream().map(stmt -> stmt.getObject())
					.collect(Collectors.toList()));
		}

		return false;
	}

	private static class ParsedRuleVisitor {

		private Map<Node, ModelClass> termCls = new HashMap<>();

		public Map<Node, ModelClass> visit(N3Rule rule) {
			Stream.concat(Arrays.stream(rule.getBody()), Arrays.stream(rule.getHead())).forEach(c -> {
				if (c instanceof TriplePattern) {
					TriplePattern tp = (TriplePattern) c;

					if (!tp.getPredicate().isURI())
						log.error("found non-URI predicate: " + tp.getPredicate());

					else {
						if (tp.getPredicate().equals(RDF.type.asNode())) {
							if (!tp.getObject().isURI())
								log.error("found non-URI type: " + tp.getObject());

							else {
								ModelClass cls = getModelCls(tp.getSubject());
								if (cls.hasName())
									log.debug("found multiple types for: " + tp.getSubject() + ", using first one: "
											+ cls.getName());
								else
									cls.setName(tp.getObject().getURI());
							}

						} else if (!isBuiltin(tp.getPredicate())) {
							ModelClass cls = getModelCls(tp.getSubject());
							ModelProperty prp = new ModelProperty(tp.getPredicate().getURI());
							cls.addOut(prp);

							if (!tp.getObject().isLiteral()) {
								ModelClass cls2 = getModelCls(tp.getObject());
								prp.setObjectType(cls2);
							}
						}
					}
				}
			});

			return termCls;
		}

		private ModelClass getModelCls(Node term) {
			ModelClass cls = termCls.get(term);
			if (cls == null) {
				cls = new ModelClass();
				termCls.put(term, cls);
			}

			return cls;
		}

		private boolean isBuiltin(Node predicate) {
			return predicate.getURI().startsWith("http://www.w3.org/2000/10/swap/");
		}
	}
}
