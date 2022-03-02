package wvw.semweb.codegen.model;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.jen3.datatypes.RDFDatatype;
import org.apache.jen3.datatypes.TypeMapper;
import org.apache.jen3.datatypes.xsd.XSDDatatype;
import org.apache.jen3.graph.Node_URI;
import org.apache.jen3.n3.N3Model;
import org.apache.jen3.rdf.model.Resource;
import org.apache.jen3.vocabulary.RDF;
import org.apache.jen3.vocabulary.RDFS;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import wvw.semweb.codegen.rule.GraphEdge;
import wvw.semweb.codegen.rule.GraphNode;
import wvw.semweb.owl.OntologyUtil;

public class ModelVisitor {

	private static final Logger log = LogManager.getLogger(ModelVisitor.class);

	private N3Model ontology;

	private CodeModel model = new CodeModel();

	public ModelVisitor(N3Model ontology) {
		this.ontology = ontology;
	}

	public CodeModel visit(GraphNode entryNode) {
		doVisit(entryNode);

		return model;
	}

	private ModelType doVisit(GraphNode node) {
		List<String> in = node.getIn().stream().map(e -> e.getId().toString()).collect(Collectors.toList());
		List<String> out = node.getOut().stream().map(e -> e.getId().toString()).collect(Collectors.toList());

		List<Resource> types = OntologyUtil.findDomainRangeTypes(in, out, false, ontology);

		log.debug("\nnode? " + node.getId());
		log.debug("found ontology types: " + types);

		if (types.size() > 1)
			log.warn("found multiple domain/range types for node (using first one)");

		else if (types.size() == 0) {
			log.error("did not find any domain/range types for node");
			return null;
		}

		Resource nodeType = types.get(0);
		if (nodeType.getURI().startsWith(XSDDatatype.XSD)) {
			RDFDatatype dt = TypeMapper.getInstance().getTypeByName(nodeType.getURI());

			if (dt == null) {
				log.debug("could not find datatype for XSD range " + nodeType);
				return null;

			} else
				return new ModelType(dt);
		}

		String nodeName = nodeType.getLocalName();
		ModelStruct modelStruct = model.newStruct(nodeName);
		loadAnnotations(nodeType.getURI(), modelStruct);

		if (node.getId() instanceof Node_URI) {
			Node_URI valueUri = (Node_URI) node.getId();
			ModelElement value = new ModelElement(valueUri.getLocalName());
			loadAnnotations(valueUri.getURI(), value);

			log.debug("value: " + value);
			modelStruct.addValue(value);

			if (!node.getOut().isEmpty())
				log.error("currently assuming that URI nodes are endpoints: " + value);

		} else {
			for (GraphEdge edge : node.getOut()) {

				GraphNode target = edge.getTarget();
				if (edge.getId().equals(RDF.type.asNode())) {

					// if a URI, this graph node indicates a concrete type
					if (target.getId() instanceof Node_URI) {
						Node_URI typeUri = (Node_URI) target.getId();
						ModelElement type = new ModelElement(typeUri.getLocalName());
						loadAnnotations(typeUri.getURI(), type);

						log.debug("type: " + type);
						modelStruct.addType(type);
					}

				} else {
					Node_URI nodePrp = (Node_URI) edge.getId();
					String prpName = nodePrp.getLocalName();
					if (edge.isInverse())
						prpName = invertProperty(prpName);

					log.debug("\nproperty: " + node.getId() + " - " + prpName);

					ModelType prpType = doVisit(edge.getTarget());
					if (prpType != null) {
						ModelProperty modelPrp = new ModelProperty(prpName);
						loadAnnotations(nodePrp.getURI(), modelPrp);
						modelPrp.setTarget(prpType);

						modelStruct.addProperty(modelPrp);
					}
				}
			}
		}

		return new ModelType(modelStruct);
	}

	private void loadAnnotations(String uri, ModelElement el) {
		Resource res = ontology.createResource(uri);

		if (res.hasProperty(RDFS.label))
			el.setLabel(res.getPropertyResourceValue(RDFS.label).asLiteral().getString());
	}

	private String invertProperty(String name) {
		if (name.startsWith("has")) {
			if (name.startsWith("has_"))
				return "is_" + name.substring("has_".length()) + "_of";
			else
				return "is" + name.substring("has".length()) + "Of";

		} else {
			return "inverse_" + name;
		}
	}
}
