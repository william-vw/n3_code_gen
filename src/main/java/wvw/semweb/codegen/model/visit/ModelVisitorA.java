package wvw.semweb.codegen.model.visit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import org.apache.jen3.vocabulary.OWL;
import org.apache.jen3.vocabulary.RDF;
import org.apache.jen3.vocabulary.RDFS;

import wvw.semweb.codegen.gen.Util;
import wvw.semweb.codegen.model.Assignment;
import wvw.semweb.codegen.model.Block;
import wvw.semweb.codegen.model.CodeStatement.Codes;
import wvw.semweb.codegen.model.Comparison;
import wvw.semweb.codegen.model.Comparison.Comparators;
import wvw.semweb.codegen.model.CreateStruct;
import wvw.semweb.codegen.model.IfThen;
import wvw.semweb.codegen.model.Literal;
import wvw.semweb.codegen.model.NodePath;
import wvw.semweb.codegen.model.Operand;
import wvw.semweb.codegen.model.Operand.Operands;
import wvw.semweb.codegen.model.StructConstant;
import wvw.semweb.codegen.model.Variable;
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

	public void visit(List<GraphNode> entryNodes) {
		Set<GraphNode> found = new HashSet<>();

		for (GraphNode entryNode : entryNodes) {
			Variable start = new Variable(((Node_Variable) entryNode.getId()).getName());
			doVisit(entryNode, null, new NodePath(start), found);
		}

//		post_checkForStructExist();
		post_removeExistChecksForLiterals();
	}

	private ModelType doVisit(GraphNode node, GraphEdge from, NodePath path, Set<GraphNode> found) {
		// this occurs in case of inverted properties (see RuleGraphFactory)
		if (found.contains(node))
			return null;

		found.add(node);

		ClauseTypes clauseType = null;
		if (from != null)
			clauseType = (ClauseTypes) from.getData();

		boolean addedCond = false;

		// - literal node
		log.info("literal? " + node.getId());
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

		if (!addedCond)
			newPath(path, clauseType);

		nodePropertiesStart();
		for (GraphEdge edge : node.getOut()) {
			ClauseTypes clauseType2 = (ClauseTypes) edge.getData();
			log.info("out? " + node.getId() + " - " + edge + " ? " + clauseType2);

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
				log.info("newPath: " + path);
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

				Block subBlock = new Block();
				block.add(subBlock);

				Variable v = new Variable();

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

		Operand cnst = new StructConstant(modelStruct, type);

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

	// - hooks for post-processing

	// comparisons are separate statements; another separate statement will refer to
	// a variable to be used in this comparison; an "exists" condition will
	// be added for the latter. but, this check is redundant, so remove those here.

	protected void post_removeExistChecksForLiterals() {
		List<Operand> toRemove = new ArrayList<>();
		// check for literal comparisons & keep found paths
		cond.getConditions().forEach(c -> {
			if (c.getCmp() != Comparators.EX && c.getOp1().getType() == Operands.NODE_PATH
					&& c.getOp2().getType() == Operands.LITERAL)
				toRemove.add(c.getOp1());
		});

		if (toRemove.isEmpty())
			return;

		Iterator<Comparison> it = cond.getConditions().iterator();
		while (it.hasNext()) {
			Comparison c = it.next();

			// remove "exists" checks for paths found in literal comparisons
			if (c.getCmp() == Comparators.EX && c.getOp1().getType() == Operands.NODE_PATH
					&& toRemove.contains(c.getOp1()))

				it.remove();
		}
	}

	// for create-struct assignment blocks:
	// modify the code block to check whether something already exists at end of
	// path (visitor code will simply assign a new struct each time)

	// (difficult to do this in visitor since we should only do this for
	// intermediary
	// assignments (i.e., not for the last assignment))

	protected void post_checkForStructExist() {
		// TODO currently only blocks will be created for create-struct assignment
		// blocks; should somehow tag these blocks for extensibility ..

		List<Block> subBlocks = block.getStatements().stream().filter(stmt -> stmt.getStatementType() == Codes.BLOCK)
				.map(stmt -> (Block) stmt).collect(Collectors.toList());

		for (int i = 0; i < subBlocks.size() - 1; i++) {
			Block subBlock = subBlocks.get(i);

			Assignment asn = (Assignment) subBlock.getStatements().get(0);
			Assignment asn2 = (Assignment) subBlock.getStatements().get(1);

			Variable var = (Variable) asn.getOp1();
			CreateStruct createStruct = (CreateStruct) asn.getOp2();
			NodePath path = (NodePath) asn2.getOp1();

			if (Util.involvesArrayAssign(path)) {

			} else {
				subBlock.clear();

				// check whether struct at current path exists
				// if not, create new struct and assign to path

				Comparison notExists = new Comparison(path, Comparators.NEX);
				Assignment newAsn = new Assignment(path, createStruct);

				subBlock.add(new IfThen(notExists, newAsn));

				// assign (possibly new) struct at end of path to variable

				Assignment newAsn2 = new Assignment(var, path);
				subBlock.add(newAsn2);
			}
		}
	}
}
