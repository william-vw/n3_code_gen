package wvw.semweb.codegen.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.jen3.graph.Node;

public class RuleGraph {

	public static enum ClauseTypes {
		BODY, HEAD
	}
	
	public Map<Node, GraphNode> termNode = new HashMap<>();

	public void add(Node term, GraphNode node) {
		termNode.put(term, node);
	}

	public GraphNode get(Node term) {
		return termNode.get(term);
	}

	public Collection<GraphNode> getAllNodes() {
		return termNode.values();
	}

	@Override
	public String toString() {
		return getAllNodes().stream().map(v -> v.toString()).collect(Collectors.joining("\n"));
	}
}
