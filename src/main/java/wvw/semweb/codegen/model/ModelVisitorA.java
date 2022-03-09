package wvw.semweb.codegen.model;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.jen3.datatypes.RDFDatatype;
import org.apache.jen3.datatypes.TypeMapper;
import org.apache.jen3.datatypes.xsd.XSDDatatype;
import org.apache.jen3.graph.Node;
import org.apache.jen3.graph.Node_Literal;
import org.apache.jen3.graph.Node_URI;
import org.apache.jen3.graph.Node_Variable;
import org.apache.jen3.n3.N3Model;
import org.apache.jen3.rdf.model.Resource;
import org.apache.jen3.reasoner.rulesys.Node_RuleVariable;
import org.apache.jen3.vocabulary.N3Math;
import org.apache.jen3.vocabulary.RDF;
import org.apache.jen3.vocabulary.RDFS;

import wvw.semweb.codegen.model.block.Assignment;
import wvw.semweb.codegen.model.block.CreateStruct;
import wvw.semweb.codegen.model.cond.Comparison;
import wvw.semweb.codegen.model.cond.Comparison.Comparators;
import wvw.semweb.codegen.model.struct.ModelElement;
import wvw.semweb.codegen.model.struct.ModelProperty;
import wvw.semweb.codegen.model.struct.ModelStruct;
import wvw.semweb.codegen.model.struct.ModelType;
import wvw.semweb.codegen.rule.GraphEdge;
import wvw.semweb.codegen.rule.GraphNode;
import wvw.semweb.codegen.rule.RuleGraph.ClauseTypes;
import wvw.semweb.owl.OntologyUtil;

public class ModelVisitorA extends ModelVisitor {

	public ModelVisitorA(N3Model ontology) {
		super(ontology);
	}

	public void visit(GraphNode entryNode) {
		Variable start = new Variable(((Node_Variable) entryNode.getId()).getName());

		doVisit(entryNode, null, new NodePath(start));
	}

	private ModelType doVisit(GraphNode node, GraphEdge from, NodePath path) {
		ClauseTypes clauseType = null;
		if (from != null)
			clauseType = (ClauseTypes) from.getData();

		boolean addedCond = false;

		// - literal node
		if (node.getId() instanceof Node_Literal) {
			Node_Literal nl = (Node_Literal) node.getId();

			Literal lit = new Literal(nl.getLiteralValue());
			literalNode(node, from, path, clauseType, lit);
			addedCond = true;

			// return literal's datatype as target for prior property
			return new ModelType(nl.getLiteralDatatype());
		}

		// - get the node's type(s) based on the domains and ranges of outgoing,
		// incoming properties from the rule graph
		List<Resource> nodeTypes = getNodeTypes(node);

//		log.debug("\nfrom? " + from);
//		log.debug("node? " + node.getId());
//		log.debug("found ontology types: " + nodeTypes);

		if (nodeTypes.size() > 1)
			log.warn("found multiple domain/range types for node " + node.getId() + " (using first one): " + nodeTypes);

		else if (nodeTypes.size() == 0) {
			log.error("did not find any domain/range types for node: " + node.getId());
			return null;
		}

		ModelStruct modelStruct = null;
		ModelType ret = null;

		Resource nodeType = nodeTypes.get(0);

		// - node has literal datatype
		// (but not an actual literal; likely a variable)
		if (nodeType.getURI().startsWith(XSDDatatype.XSD)) {
			RDFDatatype dt = TypeMapper.getInstance().getTypeByName(nodeType.getURI());

			if (dt == null) {
				log.error("could not find datatype for XSD range " + nodeType);
				return null;

			} else
				// return datatype as target
				ret = new ModelType(dt);

			// - node has object type
		} else {
			String nodeName = nodeType.getLocalName();

			// add a new struct to our model
			// (or get previously created one)
			modelStruct = model.getStruct(nodeName);
			loadAnnotations(nodeType.getURI(), modelStruct);

			// return struct as target
			ret = new ModelType(modelStruct);
		}

		// hook for doing things with the struct that was (possibly) created above
		path = structNode(node, from, path, clauseType, nodeType, modelStruct);

		if (modelStruct != null) {

			// - URI node
			if (node.getId() instanceof Node_URI) {
				Node_URI valueUri = (Node_URI) node.getId();
				ModelElement value = new ModelElement(valueUri.getLocalName());
				loadAnnotations(valueUri.getURI(), value);

				// log.debug("value: " + value);

				// add as constant to our struct
				modelStruct.addValue(value);

				uriNode(node, from, path, clauseType, modelStruct, value);
				addedCond = true;

				return ret;
			}
		}

		// - if no outgoing edges, then this node is an endpoint
		if (node.getOut().isEmpty()) {
			endNode(path, clauseType);
			addedCond = true;
		}

		if (!addedCond)
			newPath(path, clauseType);

		for (GraphEdge edge : node.getOut()) {
			ClauseTypes clauseType2 = (ClauseTypes) edge.getData();

			GraphNode target = edge.getTarget();

			// - outgoing "type" edge
			if (edge.getId().equals(RDF.type.asNode())) {

				if (modelStruct != null) {

					// if a URI, this node is a explicit type in the rule

					if (target.getId() instanceof Node_URI) {
						Node_URI typeUri = (Node_URI) target.getId();
						ModelElement type = new ModelElement(typeUri.getLocalName());
						loadAnnotations(typeUri.getURI(), type);

//						log.debug("type: " + type);
						// add as constant to our struct
						modelStruct.addType(type);

						// TODO should add all sub-types of the nodeType here
						// since the input data could have any of those sub-types

						typeNode(path, edge, clauseType2, modelStruct, type);
					}
				}

			} else {
				Node_URI nodePrp = (Node_URI) edge.getId();
				String prpName = nodePrp.getLocalName();
				// if this is an inverse edge, then "invert" its name
				if (edge.isInverse())
					prpName = invertProperty(prpName);

				// log.debug("\nproperty: " + node.getId() + " - " + prpName);

				// copy our current path and add this property to it

				ModelProperty modelPrp = new ModelProperty(prpName);
				loadAnnotations(nodePrp.getURI(), modelPrp);

				NodePath path2 = path.copy();
				path2.add(modelPrp);

				// then, recursively call this method on the edge target

				ModelType prpType = doVisit(edge.getTarget(), edge, path2);
				// returned model-type will serve as type for our property
				if (prpType != null && modelStruct != null) {
					modelPrp.setTarget(prpType);
					modelStruct.addProperty(modelPrp);
				}
			}
		}

		return ret;
	}

	private List<Resource> getNodeTypes(GraphNode node) {
		List<String> in = node.getIn().stream().map(e -> e.getId().toString()).collect(Collectors.toList());
		List<String> out = node.getOut().stream().map(e -> e.getId().toString()).collect(Collectors.toList());

		return OntologyUtil.findDomainRangeTypes(in, out, false, ontology);
	}

	// - these hooks will update cond and block

	private void newPath(NodePath path, ClauseTypes clauseType) {
		if (clauseType == ClauseTypes.BODY) {
			Comparison con = new Comparison(path, Comparators.EX);
			cond.add(con);
		}
	}

	// currently meant to support existential rules
	// if the node represents a blank node, then instantiate a new struct

	private NodePath structNode(GraphNode node, GraphEdge from, NodePath path, ClauseTypes clauseType,
			Resource nodeType, ModelStruct modelStruct) {

		if (clauseType != ClauseTypes.HEAD)
			return path;

		Node n = (Node) node.getId();
		if (n.isRuleVariable()) {
			Node or = ((Node_RuleVariable) n).getOriginal();

			// if the (original) node is a blank node

			if (or.isBlank()) {
				if (modelStruct == null)
					log.error("should create new instance but non object-type found: " + nodeType);

				// create variable and create-struct operands

				Variable v = new Variable();
				CreateStruct cs = new CreateStruct(modelStruct);

				Assignment asn2 = new Assignment(v, cs);
				block.add(asn2);

				Assignment asn = new Assignment(path, v);
				block.add(asn);

				// new node path starts from variable

				return new NodePath(v);
			}
		}

		return path;
	}

	private void literalNode(GraphNode node, GraphEdge from, NodePath path, ClauseTypes clauseType, Literal lit) {
		// check whether incoming property constitutes a builtin (i.e., comparison)
		Comparators cmp = toComparator(from);

		switch (clauseType) {

		case BODY:
			if (cmp != null) {
				// if so, then remove the property as it's really a builtin
				path.getPath().removeLast();

				// create comparison
				Comparison con = new Comparison(path, lit, cmp);
				cond.add(con);

			} else {
				// if not, then we're checking equality with this literal
				Comparison con = new Comparison(path, lit, Comparators.EQ);
				cond.add(con);
			}

			break;

		case HEAD:
			if (cmp != null)
				log.error("not expecting builtin in rule head: " + from.getId());

			else {
				// create assignment with literal
				Assignment assign = new Assignment(path, lit);
				block.add(assign);
			}

			break;
		}

		if (!node.getOut().isEmpty())
			log.error("currently assuming that literal nodes are endpoints: " + lit);
	}

	private void uriNode(GraphNode node, GraphEdge from, NodePath path, ClauseTypes clauseType, ModelStruct modelStruct,
			ModelElement value) {

		Operand cnst = new StructConstant(modelStruct, value);
		switch (clauseType) {

		case BODY:
			Comparison con = new Comparison(path, cnst, Comparators.EQ);
			cond.add(con);

			break;

		case HEAD:
			Assignment asn = new Assignment(path, cnst);
			block.add(asn);

			break;
		}

		if (!node.getOut().isEmpty())
			log.error("currently assuming that URI nodes are endpoints: " + value);
	}

	private void typeNode(NodePath path, GraphEdge edge, ClauseTypes clauseType, ModelStruct modelStruct,
			ModelElement type) {

		Operand cnst = new StructConstant(modelStruct, type);

		NodePath path2 = path.copy();
		path2.add(new ModelProperty("type"));

		switch (clauseType) {

		case BODY:
			Comparison con = new Comparison(path2, cnst, Comparators.EQ);
			cond.add(con);

			break;

		case HEAD:

			Assignment asn = new Assignment(path2, cnst);
			block.add(asn);

			break;
		}

		if (!edge.getTarget().getOut().isEmpty())
			log.error("currently assuming that types are endpoints: " + type);
	}

	private void endNode(NodePath path, ClauseTypes clauseType) {
		switch (clauseType) {

		case BODY:
			Comparison con = new Comparison(path, Comparators.EX);
			cond.add(con);
			break;

		case HEAD:
			log.error("not expecting an endpoint in rule head: " + path);
			break;
		}
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

	private Comparators toComparator(GraphEdge edge) {
		Node_URI node = (Node_URI) edge.getId();

		if (node.equals(N3Math.lessThan.asNode()))
			return Comparators.LT;
		if (node.equals(N3Math.lessThanOrEqual.asNode()))
			return Comparators.LE;
		if (node.equals(N3Math.lessThanOrEqual.asNode()))
			return Comparators.LE;
		if (node.equals(N3Math.greaterThan.asNode()))
			return Comparators.GT;
		if (node.equals(N3Math.greaterThanOrEqual.asNode()))
			return Comparators.GE;
		if (node.equals(N3Math.notGreaterThan.asNode()))
			return Comparators.NGT;
		if (node.equals(N3Math.notLessThan.asNode()))
			return Comparators.NLT;
		if (node.equals(N3Math.notEqualTo.asNode()))
			return Comparators.NEQ;

		return null;
	}
}
