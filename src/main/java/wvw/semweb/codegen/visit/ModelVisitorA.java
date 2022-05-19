package wvw.semweb.codegen.visit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import org.apache.jen3.vocabulary.OWL;
import org.apache.jen3.vocabulary.RDF;
import org.apache.jen3.vocabulary.RDFS;

import wvw.semweb.codegen.model.Assignment;
import wvw.semweb.codegen.model.Block;
import wvw.semweb.codegen.model.Comparison;
import wvw.semweb.codegen.model.Comparison.Comparators;
import wvw.semweb.codegen.model.CreateStruct;
import wvw.semweb.codegen.model.Literal;
import wvw.semweb.codegen.model.NodePath;
import wvw.semweb.codegen.model.Operand;
import wvw.semweb.codegen.model.StructConstant;
import wvw.semweb.codegen.model.Variable;
import wvw.semweb.codegen.model.struct.ModelElement;
import wvw.semweb.codegen.model.struct.ModelProperty;
import wvw.semweb.codegen.model.struct.ModelStruct;
import wvw.semweb.codegen.model.struct.ModelType;
import wvw.semweb.codegen.parse.post.ModelPostprocessor;
import wvw.semweb.codegen.parse.rule.GraphEdge;
import wvw.semweb.codegen.parse.rule.GraphNode;
import wvw.semweb.codegen.parse.rule.RuleGraph;
import wvw.semweb.codegen.parse.rule.RuleGraph.ClauseTypes;
import wvw.semweb.owl.OntologyUtil;

public class ModelVisitorA extends ModelVisitor {

	public ModelVisitorA(N3Model ontology) {
		super(ontology);
	}

	@Override
	public void visit(RuleGraph ruleGraph, ModelPostprocessor.PostprocessTypes... postprocesses) {
		Set<GraphNode> found = new HashSet<>();

		for (GraphNode root : ruleGraph.getGraphRoots()) {
			Variable start = new Variable(((Node_Variable) root.getId()).getName());
			doVisit(root, null, new NodePath(start), found);
		}
	}

	private ModelType doVisit(GraphNode node, GraphEdge from, NodePath path, Set<GraphNode> found) {
		if (found.contains(node))
			return null;

		found.add(node);

		ClauseTypes clauseType = null;
		if (from != null)
			clauseType = (ClauseTypes) from.getData();

		boolean addedCond = false;

		// - literal node
		if (node.getId() instanceof Node_Literal) {
			Node_Literal nl = (Node_Literal) node.getId();

			Literal lit = new Literal(nl.getLiteralValue());
			literalNode(node, from, path, clauseType, lit);

			// return literal's datatype as target for prior property
			return new ModelType(nl.getLiteralDatatype());
		}

		// - get the node's type(s) based on the domains and ranges of outgoing,
		// incoming properties from the rule graph
		List<Resource> nodeTypes = getNodeTypes(node);

//		log.info("\nfrom? " + from);
//		log.info("node? " + node);
//		log.debug("found ontology types: " + nodeTypes);

		if (nodeTypes.size() > 1)
			log.warn("found multiple domain/range types for node " + node.getId() + " (using first one): " + nodeTypes);

		else if (nodeTypes.size() == 0) {
			log.error("did not find any domains/ranges or super-types for node: " + node.getId());
			return null;
		}

		ModelStruct modelStruct = null;
		ModelType ret = null;

		Resource nodeType = nodeTypes.get(0);

		// - node has literal datatype
		// (but is not an actual literal; likely a variable)
		if (nodeType.getURI().startsWith(XSDDatatype.XSD)) {
			RDFDatatype dt = TypeMapper.getInstance().getTypeByName(nodeType.getURI());

			if (dt == null) {
				log.error("could not find datatype for XSD range " + nodeType);
				return null;

			} else
				// return datatype as target
				// TODO can't we just return directly here?
				ret = new ModelType(dt);

			// - node has object type
		} else {
			String nodeName = nodeType.getLocalName();

			// add a new struct to our model
			// (or get previously created one)
			modelStruct = model.getOrCreateStruct(nodeName, node);
			loadAnnotations(nodeType.getURI(), modelStruct);

			// return struct as target
			ret = new ModelType(modelStruct);
		}

		// do things with the struct that was (possibly) created above
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

		// - if no outgoing edges (and not a URI or literal),
		// then this node is an endpoint
		if (node.getOut().isEmpty()) {
			endNode(path, clauseType);
			addedCond = true;
		}

		// TODO is this needed? (cfr. the code above)
		if (!addedCond)
			newPath(path, clauseType);

		nodePropertiesStart();

		// make sure type properties come first
		// (needed when dealing w/ array-like properties;
		// (existence check should come before other conditions)

		List<GraphEdge> sortedEdges = new ArrayList<>(node.getOut());
		sortedEdges.sort((e1, e2) -> {
			Node n1 = (Node) e1.getId();
			if (n1.getURI().equals(RDF.type.getURI()))
				return -1;
			return 1;
		});

		for (GraphEdge edge : sortedEdges) {
			log.info("edge? " + node.getId() + " -> " + edge.getId());

			ClauseTypes clauseType2 = (ClauseTypes) edge.getData();

			GraphNode target = edge.getTarget();

			// - outgoing "type" edge
			if (edge.getId().equals(RDF.type.asNode())) {

				if (modelStruct != null) {

					// if a URI, this node is an explicit type in the rule

					if (target.getId() instanceof Node_URI) {
						Node_URI typeUri = (Node_URI) target.getId();
						ModelElement type = new ModelElement(typeUri.getLocalName());
						loadAnnotations(typeUri.getURI(), type);

						// e.g., Patient struct with 'patient' type
						// doesn't make a lot of sense
						if (type.getString() != modelStruct.getString()) {
//							log.debug("type: " + type);
							// add as constant to our struct
							modelStruct.addType(type);

							// TODO should add all sub-types of the nodeType here
							// since the input data could have any of those sub-types

							typeNode(path, edge, clauseType2, modelStruct, type);
						}
					}
				}

			} else {
				Node_URI nodePrp = (Node_URI) edge.getId();
				String prpName = nodePrp.getLocalName();

				// copy our current path and add this property to it

				ModelProperty modelPrp = new ModelProperty(prpName);

				loadAnnotations(nodePrp.getURI(), modelPrp);
				loadCardinality(nodePrp.getURI(), modelPrp);

				// if this is an inverse edge, then "invert" its name
				// (NOTE currently no longer used)
				if (edge.isInverse()) {
					modelPrp.setString(invertProperty(modelPrp.getString()));
					// (assuming a maxCardinality of 1 on inverse properties
					// (i.e., a one-to-many))
					modelPrp.setMaxCardinality(1);
				}

				NodePath path2 = path.copy();
				path2.add(modelPrp);

				// then, recursively call this method on the edge target

				ModelType prpType = doVisit(edge.getTarget(), edge, path2, found);
				// returned model-type will serve as type for our property
				if (prpType != null && modelStruct != null) {
					modelPrp.setTarget(prpType);
					modelStruct.addProperty(modelPrp);
				}
			}
		}
		nodePropertiesEnd();

		return ret;
	}

	private List<Resource> getNodeTypes(GraphNode node) {
		List<String> in = new ArrayList<>();
		List<String> out = new ArrayList<>();

		// add incoming edges to appropriate list (may be inverse)
		node.getIn().stream().forEach(e -> {
			String id = e.getId().toString();
			if (!e.isInverse())
				in.add(id);
			else
				out.add(id);
		});

		// idem for outgoing edges
		node.getOut().stream().forEach(e -> {
			String id = e.getId().toString();
			if (!e.isInverse())
				out.add(id);
			else
				in.add(id);
		});

		List<Resource> types = OntologyUtil.findDomainRangeTypes(in, out, true, ontology);
		if (types.isEmpty()) {
			// get first specified type in rule
			Optional<GraphEdge> found = node.getOut().stream().filter(edge -> edge.getId().equals(RDF.type.asNode()))
					.findFirst();

			if (found.isPresent()) {
				String res = found.get().getTarget().getId().toString();
				types = OntologyUtil.findSuperTypes(res, ontology);
			}
		}

		return types;
	}

	// keep track of current struct constructors
	// (any property will be added as constructor parameter)

	private LinkedList<CreateStruct> newStructs = new LinkedList<>();

	private void nodePropertiesStart() {
	}

	private void nodePropertiesEnd() {
		// only consider direct properties as constructor parameters
		// (see #structNode())
		if (!newStructs.isEmpty()) {
			newStructs.removeLast();
		}
	}

	// - hooks for updating the logic - i.e., cond and block

	private void newPath(NodePath path, ClauseTypes clauseType) {
		if (clauseType == ClauseTypes.BODY) {
			if (!path.getPath().getLast().requiresArray()) {

				Comparison con = new Comparison(path, Comparators.EX);
				cond.add(con);
			}
		}
	}

	// support existential rules
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
				NodePath ret = null;

				if (modelStruct == null)
					log.error("should create new instance but non object-type found: " + nodeType);

				// create new struct
				CreateStruct newStruct = new CreateStruct(modelStruct);

				// use as constructor parameter
				// (this is a bit more involved, so don't use new[..] methods)

				if (!newStructs.isEmpty()) {
					CreateStruct curStruct = newStructs.getLast();
					curStruct.add(new Assignment(path, newStruct));

					// properties of blank node will serve as constructor parameters
					// add this struct to the stack; use new assignments as parameters

					// (node path doesn't matter here)
					newStructs.add(newStruct);

					// following struct parameters will have "empty" path
					// (params are identified by predicates)
					ret = new NodePath();

					// treat as regular statement
				} else {
					Block subBlock = new Block();
					block.add(subBlock);

					Variable v = new Variable();

					// assign to var
					Assignment asn = new Assignment(v, newStruct);
					subBlock.add(asn);

					// assign end of path to newly created struct
					Assignment asn2 = new Assignment(path, v);
					subBlock.add(asn2);

					// properties of blank node will serve as constructor parameters
					// add this struct to the stack; use new assignments as parameters
					// (see first option above)

					// (once constructor is done, new node path starts from variable)
					newStructs.add(newStruct);

					// but, following struct parameters, will have "empty" path
					// (params are identified by predicates)
					ret = new NodePath();
				}

				return ret;
			}
		}

		return path;
	}

	private void literalNode(GraphNode node, GraphEdge from, NodePath path, ClauseTypes clauseType, Literal lit) {
		// check whether incoming property constitutes a builtin (i.e., comparison)
		Comparators cmp = toComparator(from);

		switch (clauseType) {

		case BODY:
			Comparison con = null;
			if (cmp != null) {
				// if so, then remove the property as it's really a builtin
				path.getPath().removeLast();

				// create comparison
				con = new Comparison(path, lit, cmp);

			} else {
				// if not, then we're checking equality with this literal
				con = new Comparison(path, lit, Comparators.EQ);
			}
			newComparison(con);

			break;

		case HEAD:
			if (cmp != null)
				log.error("not expecting builtin in rule head: " + from.getId());

			else {
				// create assignment with literal
				Assignment assign = new Assignment(path, lit);
				newAssignment(assign);
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
			newComparison(con);

			break;

		case HEAD:
			Assignment asn = new Assignment(path, cnst);
			newAssignment(asn);

			break;
		}

		if (!node.getOut().isEmpty())
			log.error("currently assuming that URI nodes are endpoints: " + value);
	}

	private void typeNode(NodePath path, GraphEdge edge, ClauseTypes clauseType, ModelStruct modelStruct,
			ModelElement type) {

		StructConstant cnst = new StructConstant(modelStruct, type);

		// whether type is required to index an array-like property
		// (i.e., property w/ cardinality > 1)

		if (path.requiresKeyType())
			path.setKeyType(cnst);

		NodePath path2 = path.copy();
		path2.add(ModelProperty.typeProperty());

		switch (clauseType) {

		case BODY:
			Comparison con = new Comparison(path2, cnst, Comparators.EQ);
			newComparison(con);

			break;

		case HEAD:
			Assignment asn = new Assignment(path2, cnst);
			newAssignment(asn);

			break;
		}

		if (!edge.getTarget().getOut().isEmpty())
			log.error("currently assuming that types are endpoints: " + type);
	}

	private void endNode(NodePath path, ClauseTypes clauseType) {
		switch (clauseType) {

		case BODY:
			Comparison con = new Comparison(path, Comparators.EX);
			newComparison(con);

			break;

		case HEAD:
			log.error("not expecting an endpoint in rule head: " + path);
			break;
		}
	}

	private void newAssignment(Assignment assn) {
		if (!newStructs.isEmpty()) {
			CreateStruct curStruct = newStructs.getLast();
			curStruct.add(assn);

		} else {
			block.add(assn);
		}
	}

	private void newComparison(Comparison con) {
		cond.add(con);
	}

	private void loadAnnotations(String uri, ModelElement el) {
		Resource res = ontology.createResource(uri);

		if (res.hasProperty(RDFS.label))
			el.setLabel(res.getPropertyResourceValue(RDFS.label).asLiteral().getString());
	}

	private void loadCardinality(String uri, ModelProperty prp) {
		// NOTE assume maxCardinality of 1 for label
		if (uri.equals(RDFS.label.getURI())) {
			prp.setMaxCardinality(1);
			return;
		}

		Resource prpRes = ontology.createResource(uri);

		ontology.listStatements(null, ontology.createResource(OWL.onProperty), prpRes).forEachRemaining(stmt -> {

			Resource restr = stmt.getSubject();

			if (restr.hasProperty(OWL.maxCardinality)) {
				if (prp.hasMaxCardinality())
					log.warn("found multiple maxCardinality constraints for property " + uri);

				int value = restr.getPropertyResourceValue(OWL.maxCardinality).asLiteral().getInt();
				prp.setMaxCardinality(value);
			}
		});

		if (prpRes.hasProperty(RDF.type, OWL.FunctionalProperty)) {
			if (prp.hasMaxCardinality())
				log.warn("found multiple cardinality-related constraints for property " + uri);

			prp.setMaxCardinality(1);
		}

		if (prp.hasMaxCardinality())
			log.info("found max cardinality for " + uri + ": " + prp.getMaxCardinality());
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
