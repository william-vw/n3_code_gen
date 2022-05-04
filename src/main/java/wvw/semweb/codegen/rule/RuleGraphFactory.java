package wvw.semweb.codegen.rule;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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

	public RuleGraph createGraph(N3Rule rule, List<Node> allEntries) throws ParseModelException {
		buildGraph(rule);

		checkGraph(rule, allEntries);

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

			GraphNode node = graph.getOrCreate(tp.getSubject());

			GraphEdge edge = new GraphEdge(tp.getPredicate());
			edge.setSource(node);

			edge.setData(type);

			GraphNode node2 = null;
			if (!tp.getObject().isLiteral())
				node2 = graph.getOrCreate(tp.getObject(), true);
			else
				node2 = new GraphNode(tp.getObject());

			edge.setTarget(node2);

			node.addOut(edge);
			node2.addIn(edge);
		}
	}

	protected void checkGraph(N3Rule rule, List<Node> allEntries) throws ParseModelException {
		Set<GraphNode> found = new HashSet<>();

		for (Node entryTerm : allEntries) {
			if (!graph.contains(entryTerm))
				throw new ParseModelException("entry term " + entryTerm + " not found in rule: " + rule);

			GraphNode node = graph.get(entryTerm);
			coverGraph(found, node);
		}

		Collection<Node> roots = graph.getRoots();
		for (Node root : roots) {
			GraphNode node = graph.get(root);

			if (!allEntries.contains(root)) {
				log.info("found root that is not entry term (" + root + ") - adding as entry term");
				allEntries.add(root);
			}

			coverGraph(found, node);
		}

		graph.getAllNodes().forEach(n -> {
			if (!found.contains(n))
				log.error("term " + n.prettyPrint() + " not reachable from entry points");
		});
	}

	protected void coverGraph(Set<GraphNode> found, GraphNode curNode) {
		if (curNode.isLiteral())
			return;

		found.add(curNode);

		for (GraphEdge out : curNode.getOut()) {

			if (!found.contains(out.getTarget()))
				coverGraph(found, out.getTarget());
		}
	}
}
