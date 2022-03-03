package wvw.semweb.codegen.rule;

import java.util.HashSet;
import java.util.Set;

import org.apache.jen3.graph.Node;
import org.apache.jen3.n3.impl.N3Rule;
import org.apache.jen3.reasoner.TriplePattern;
import org.apache.jen3.reasoner.rulesys.ClauseEntry;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import wvw.semweb.codegen.ParseModelException;
import wvw.semweb.codegen.rule.RuleGraph.ClauseTypes;

public class RuleGraphFactory {

	private static final Logger log = LogManager.getLogger(RuleGraphFactory.class);

	private RuleGraph graph = new RuleGraph();

	public RuleGraph createGraph(N3Rule rule, Node entryTerm) throws ParseModelException {
		buildGraph(rule);

		Set<GraphNode> found = expandGraph(entryTerm, rule);
		checkGraph(found, entryTerm);

		return graph;
	}

	protected void buildGraph(N3Rule rule) {
		buildGraph(rule.getBody(), ClauseTypes.BODY);
		buildGraph(rule.getHead(), ClauseTypes.HEAD);
	}

	protected void buildGraph(ClauseEntry[] clauses, ClauseTypes type) {
		for (ClauseEntry c : clauses) {
			if (!(c instanceof TriplePattern))
				return;

			TriplePattern tp = (TriplePattern) c;

			if (!tp.getPredicate().isURI())
				log.error("found non-URI predicate: " + tp.getPredicate());

			GraphNode node = uniqueGraphNode(tp.getSubject());

			GraphEdge edge = new GraphEdge(tp.getPredicate());
			edge.setSource(node);

			edge.setData(type);
			node.addOut(edge);

			GraphNode node2 = null;
			if (!tp.getObject().isLiteral())
				node2 = uniqueGraphNode(tp.getObject());
			else
				node2 = new GraphNode(tp.getObject());

			edge.setTarget(node2);
			node2.addIn(edge);
		}
	}

	protected Set<GraphNode> expandGraph(Node entryTerm, N3Rule rule) throws ParseModelException {
		GraphNode entryNode = graph.get(entryTerm);
		if (entryNode == null)
			throw new ParseModelException("entry term " + entryTerm + " not found in rule: " + rule);

		Set<GraphNode> found = new HashSet<>();
		expandGraph(found, entryNode);

		return found;
	}

	protected void expandGraph(Set<GraphNode> found, GraphNode cur) {
		if (cur.isLiteral())
			return;

		found.add(cur);

		for (GraphEdge in : cur.getIn()) {
			// means we didn't get here via this edge
			// (so, rule does not have a simple "path" structure)

			if (!found.contains(in.getSource())) {
				GraphEdge inverse = new GraphEdge(in.getId(), cur, in.getSource());
				inverse.setInverse(true);

				log.debug("adding inverse: " + inverse);
				cur.addOut(inverse);
			}
		}

		for (GraphEdge out : cur.getOut()) {

			if (!found.contains(out.getTarget()))
				expandGraph(found, out.getTarget());
		}
	}

	protected void checkGraph(Set<GraphNode> found, Node entryTerm) {
		graph.getAllNodes().forEach(n -> {
			if (!found.contains(n))
				System.err.println("term " + n.prettyPrint() + " not reachable from entry point " + entryTerm);
		});
	}

	protected GraphNode uniqueGraphNode(Node term) {
		GraphNode node = graph.get(term);

		if (node == null) {
			node = new GraphNode(term);
			graph.add(term, node);
		}

		return node;
	}
}
