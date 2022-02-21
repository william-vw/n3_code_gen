package wvw.semweb.owl.gen;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jen3.graph.Node;
import org.apache.jen3.n3.impl.N3Rule;
import org.apache.jen3.reasoner.TriplePattern;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import wvw.semweb.owl.gen.graph.GraphEdge;
import wvw.semweb.owl.gen.graph.GraphNode;

public class RuleGraphFactory {

	private static final Logger log = LogManager.getLogger(RuleGraphFactory.class);

	private Map<Node, GraphNode> termNode = new HashMap<>();

	public Map<Node, GraphNode> createGraph(N3Rule rule, Node entryTerm) throws ParseModelException {
		buildGraph(rule);

		Set<GraphNode> found = expandGraph(entryTerm, rule);
		checkGraph(found, entryTerm);

		return termNode;
	}

	private void buildGraph(N3Rule rule) {
		Stream.concat(Arrays.stream(rule.getBody()), Arrays.stream(rule.getHead())).forEach(c -> {
			if (!(c instanceof TriplePattern))
				return;

			TriplePattern tp = (TriplePattern) c;

			if (!tp.getPredicate().isURI())
				log.error("found non-URI predicate: " + tp.getPredicate());

			GraphNode node = uniqueGraphNode(tp.getSubject());

			GraphEdge edge = new GraphEdge(tp.getPredicate());
			edge.setSource(node);

			node.addOut(edge);

			GraphNode node2 = null;
			if (!tp.getObject().isLiteral())
				node2 = uniqueGraphNode(tp.getObject());
			else
				node2 = new GraphNode(tp.getObject().getLiteralValue().toString());

			edge.setTarget(node2);
			node2.addIn(edge);
		});
	}

	private Set<GraphNode> expandGraph(Node entryTerm, N3Rule rule) throws ParseModelException {
		GraphNode entryNode = termNode.get(entryTerm);
		if (entryNode == null)
			throw new ParseModelException("entry term " + entryTerm + " not found in rule: " + rule);

		Set<GraphNode> found = new HashSet<>();
		expandGraph(found, entryNode);

		return found;
	}

	private void expandGraph(Set<GraphNode> found, GraphNode cur) {
		if (cur.isLiteral())
			return;

		found.add(cur);

		for (GraphEdge in : cur.getIn()) {

			// means we didn't get here via this edge
			// (so, rule likely does not have a simple "path" structure)
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

	private void checkGraph(Set<GraphNode> found, Node entryTerm) {
		termNode.values().forEach(n -> {
			if (!found.contains(n))
				System.err.println("term " + n.prettyPrint() + " not reachable from entry point " + entryTerm);
		});
	}

	private GraphNode uniqueGraphNode(Node term) {
		GraphNode node = termNode.get(term);

		if (node == null) {
			node = new GraphNode(term);
			termNode.put(term, node);
		}

		return node;
	}
}
