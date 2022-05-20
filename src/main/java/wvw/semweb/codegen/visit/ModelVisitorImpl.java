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
import org.apache.jen3.vocabulary.N3Log;
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
import wvw.semweb.codegen.model.StructConstant;
import wvw.semweb.codegen.model.Variable;
import wvw.semweb.codegen.model.struct.ModelElement;
import wvw.semweb.codegen.model.struct.ModelProperty;
import wvw.semweb.codegen.model.struct.ModelStruct;
import wvw.semweb.codegen.model.struct.ModelType;
import wvw.semweb.codegen.parse.rule.GraphEdge;
import wvw.semweb.codegen.parse.rule.GraphNode;
import wvw.semweb.codegen.parse.rule.RuleGraph;
import wvw.semweb.codegen.parse.rule.RuleGraph.ClauseTypes;
import wvw.semweb.owl.OntologyUtil;

public class ModelVisitorImpl extends ModelVisitor {

	public ModelVisitorImpl(N3Model ontology) {
		super(ontology);
	}

	@Override
	public void visit(RuleGraph ruleGraph) throws VisitModelException {
		for (GraphNode root : ruleGraph.getGraphRoots()) {
			Variable start = new Variable(((Node_Variable) root.getId()).getName());

			constructModel(root, new HashSet<>());
			constructLogic(root, null, new NodePath(start), new HashSet<>());
		}
	}

	private ModelType constructModel(GraphNode node, Set<GraphNode> found) throws VisitModelException {
		if (found.contains(node))
			return null;

		found.add(node);

		// - literal node
		if (node.getId() instanceof Node_Literal) {
			Node_Literal nl = (Node_Literal) node.getId();

			// return literal's datatype as target for prior property
			// (assumed this is an end-point in model)
			return new ModelType(nl.getLiteralDatatype());
		}

		// - get the node's type(s) based on the domains and ranges of outgoing,
		// incoming properties from the rule graph
		List<Resource> nodeTypes = getNodeTypes(node);

//		log.debug("\nfrom? " + from);
//		log.debug("node? " + node);
//		log.debug("found ontology types: " + nodeTypes);

		if (nodeTypes.size() > 1)
			log.warn("found multiple domain/range types for node " + node.getId() + " (using first one): " + nodeTypes);

		else if (nodeTypes.size() == 0)
			throw new VisitModelException("did not find any domains/ranges or super-types for node: " + node.getId());

		Resource nodeType = nodeTypes.get(0);

		// - literal datatype
		if (nodeType.getURI().startsWith(XSDDatatype.XSD)) {
			RDFDatatype dt = TypeMapper.getInstance().getTypeByName(nodeType.getURI());

			if (dt == null)
				throw new VisitModelException("could not find datatype for XSD range " + nodeType);

			// return datatype as target
			// (idem above; assumed this is an end-point in model)
			return new ModelType(dt);
		}

		// - object type

		ModelType ret = null;

		// add a new struct to our model
		// (or get previously created one)
		ModelStruct modelStruct = model.getOrCreateStruct(nodeType.getLocalName(), node);
		loadAnnotations(nodeType.getURI(), modelStruct);

		// return struct as target
		ret = new ModelType(modelStruct);

		// -- URI node
		if (node.getId() instanceof Node_URI)
			// add as constant to our struct
			addConstant((Node_URI) node.getId(), true, modelStruct);

		for (GraphEdge edge : node.getOut()) {
//			log.info("edge? " + node.getId() + " -> " + edge.getId());

			GraphNode target = edge.getTarget();

			// -- outgoing "type" edge
			// if a URI, this node is an explicit type in the rule

			if (edge.getId().equals(RDF.type.asNode()) && target.getId() instanceof Node_URI) {
				Node_URI typeUri = (Node_URI) target.getId();

				// e.g., Patient struct with 'patient' type doesn't make a lot of sense
				if (!typeUri.getLocalName().equals(modelStruct.getName()))

					// add as constant to our struct
					addConstant(typeUri, false, modelStruct);

			} else {
				Node_URI nodePrp = (Node_URI) edge.getId();

				// - not a regular property (builtin) but builtin
				// (as w/ literals; assumed this is an end-point in model;
				// builtins on literal datatypes are already ignored)

				if (toUriComparator(edge) != null) {

					// if the target is concrete, add those as values in the struct
					if (target.getId() instanceof Node_URI) {
						addConstant((Node_URI) target.getId(), true, modelStruct);

						model.setStruct(target, modelStruct);
					}

				} else {
					// -- regular property
					ModelProperty modelPrp = new ModelProperty(nodePrp.getLocalName());
					loadAnnotations(nodePrp.getURI(), modelPrp);
					loadCardinality(nodePrp.getURI(), modelPrp);

					// then, recursively call this method on the edge target

					ModelType prpType = constructModel(edge.getTarget(), found);
					// returned model-type will serve as type for our property
					if (prpType != null) {
						modelPrp.setTarget(prpType);
						modelStruct.addProperty(modelPrp);
					}
				}
			}
		}

		return ret;
	}

	private void constructLogic(GraphNode node, GraphEdge from, NodePath path, Set<GraphNode> found)
			throws VisitModelException {

		if (found.contains(node))
			return;

		found.add(node);

		ClauseTypes clauseType = (from != null ? (ClauseTypes) from.getData() : ClauseTypes.BODY);

		// - literal node
		if (node.getId() instanceof Node_Literal) {
			Node_Literal nl = (Node_Literal) node.getId();

			Literal lit = new Literal(nl.getLiteralValue());
			literalNode(node, from, path, clauseType, lit);
		}

		ModelStruct modelStruct = model.getStruct(node);

		// - object type
		// (else: literal datatype)

		if (modelStruct != null) {

			// -- URI node
			if (node.getId() instanceof Node_URI) {
				Node_URI valueUri = (Node_URI) node.getId();
				ModelElement value = modelStruct.getConstant(valueUri.getLocalName());

				constantNode(node, from, path, clauseType, modelStruct, value);

				// -- variable node
			} else {
				switch (clauseType) {

				case BODY:
					// (only needed in case of object datatype)
					if (!path.getPath().isEmpty() && !path.getPath().getLast().requiresArray())
						newPath(path);

					break;

				case HEAD:
					Node n = (Node) node.getId();

					// if the (original) node is a blank node
					// (only needed in case of object datatype)

					if (n.isRuleVariable() && ((Node_RuleVariable) n).getOriginal().isBlank()) {
						// deal with existentials in rule head (constructors)
						// (will likely involve updating the path)

						path = newStruct(path, modelStruct);
					}

					break;
				}
			}
		}

		nodePropertiesStart();

		// make sure type properties come first
		// (needed when dealing w/ array-like properties;
		// (existence check should come before other conditions)

		List<GraphEdge> sortedEdges = new ArrayList<>(node.getOut());
		sortedEdges.sort((e1, e2) -> {
			Node n1 = (Node) e1.getId();
			if (n1.getURI().equals(RDF.type.getURI()))
				return -1;
			else
				return 1;
		});

		for (GraphEdge edge : sortedEdges) {
//			log.info("edge? " + node.getId() + " -> " + edge.getId() + " -> " + edge.getTarget().getId());

			ClauseTypes clauseType2 = (ClauseTypes) edge.getData();
			GraphNode target = edge.getTarget();

			// - outgoing "type" edge
			// if a URI, this node is an explicit type in the rule
			if (edge.getId().equals(RDF.type.asNode()) && target.getId() instanceof Node_URI) {
				Node_URI typeUri = (Node_URI) target.getId();

				// e.g., Patient struct with 'patient' type doesn't make a lot of sense
				if (!typeUri.getLocalName().equals(modelStruct.getName())) {
					ModelElement type = modelStruct.getConstant(typeUri.getLocalName());

					constantNode(target, edge, path, clauseType2, modelStruct, type);
				}

			} else {
				NodePath path2 = path;

				// - object type & regular property (i.e., not a builtin):
				// add to our path

				if (modelStruct != null && toUriComparator(edge) == null) {
					Node_URI nodePrp = (Node_URI) edge.getId();
					// (needs to be copied; key-type can be overridden)
					ModelProperty modelPrp = modelStruct.getProperty(nodePrp.getLocalName()).copy();

					// copy our current path and add this property to it
					path2 = path.copy();
					path2.add(modelPrp);
				}

				// recursively call this method on the edge target
				constructLogic(edge.getTarget(), edge, path2, found);
			}
		}

		nodePropertiesEnd();
	}

	private void addConstant(Node_URI uri, boolean isValue, ModelStruct modelStruct) {
		ModelElement cnst = new ModelElement(uri.getLocalName());
		loadAnnotations(uri.getURI(), cnst);

		// add as constant to our struct
		if (isValue)
			modelStruct.addValue(cnst);
		else
			modelStruct.addType(cnst);
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

	private void newPath(NodePath path) {
		Comparison con = new Comparison(path, Comparators.EX);
		cond.add(con);
	}

	// support existential rules
	// if the node represents a blank node, then instantiate a new struct

	private NodePath newStruct(NodePath path, ModelStruct modelStruct) {
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
			return new NodePath();

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
			return new NodePath();
		}
	}

	// TODO three methods below have a lot in common

	private void literalNode(GraphNode node, GraphEdge from, NodePath path, ClauseTypes clauseType, Literal lit)
			throws VisitModelException {

		// check whether incoming property constitutes a builtin (i.e., comparison)
		Comparators cmp = toLiteralComparator(from, lit.getValue());

		switch (clauseType) {

		case BODY:
			Comparison con = null;
			if (cmp != null)
				// create comparison
				con = new Comparison(path, lit, cmp);
			else
				// if not, then we're checking equality with this literal
				con = new Comparison(path, lit, Comparators.EQ);

			newComparison(con);

			break;

		case HEAD:
			if (cmp != null)
				throw new VisitModelException("not expecting builtin in rule head: " + from.getId());
			else {
				// create assignment with literal
				Assignment assign = new Assignment(path, lit);
				newAssignment(assign);
			}

			break;
		}

		if (!node.getOut().isEmpty())
			throw new VisitModelException("currently assuming that literal nodes are endpoints: " + lit);
	}

	private void constantNode(GraphNode node, GraphEdge from, NodePath path, ClauseTypes clauseType,
			ModelStruct modelStruct, ModelElement value) throws VisitModelException {

		// check whether incoming property constitutes a builtin (i.e., comparison)
		Comparators cmp = toUriComparator(from);

		// assumed that this value is a constant of the model-struct
		StructConstant cnst = new StructConstant(modelStruct, value);

		// type required to index an array-like property
		// (in subsequent copies of this path: key will be used to index the array)

		if (path.size() > 0 && path.getLast().requiresArray()) {
			if (cmp != null)
				throw new VisitModelException("currently not supporting builtins on array-like properties: "
						+ path.getLast().getString() + " (" + from.getId() + ")");

			path.getLast().setKeyType(cnst);

			// also, in this case, no need for comparison w/ uri:
			// only need to check for existance of mapping
			// (this is an endpoint)

			Comparison con = new Comparison(path, Comparators.EX);
			newComparison(con);

		} else {
			NodePath path2 = path.copy();
			path2.add(ModelProperty.typeProperty());

			switch (clauseType) {

			case BODY:
				Comparison con = null;
				if (cmp != null)
					// create comparison
					con = new Comparison(path2, cnst, cmp);
				else
					// if not, then we're checking equality with this uri
					con = new Comparison(path2, cnst, Comparators.EQ);

				newComparison(con);

				break;

			case HEAD:
				if (cmp != null)
					throw new VisitModelException("not expecting builtin in rule head: " + from.getId());

				else {
					Assignment asn = new Assignment(path2, cnst);
					newAssignment(asn);
				}

				break;
			}
		}

		if (!node.getOut().isEmpty())
			throw new VisitModelException("currently assuming that URI nodes are endpoints: " + value);
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

	private Comparators toLiteralComparator(GraphEdge edge, Object literal) throws VisitModelException {
		Node_URI node = (Node_URI) edge.getId();

		if (literal instanceof Integer || literal instanceof Double) {
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
		}

		return null;
	}

	private Comparators toUriComparator(GraphEdge edge) throws VisitModelException {
		Node_URI node = (Node_URI) edge.getId();

		if (node.equals(N3Log.equalTo.asNode()))
			return Comparators.EQ;
		if (node.equals(N3Log.notEqualTo.asNode()))
			return Comparators.NEQ;
		else
			return null;
	}
}
