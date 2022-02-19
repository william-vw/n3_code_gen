package wvw.semweb.owl.gen;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import wvw.semweb.owl.gen.graph.GraphEdge;
import wvw.semweb.owl.gen.graph.GraphNode;
import wvw.semweb.owl.gen.model.ModelEdge;
import wvw.semweb.owl.gen.model.ModelNode;

public class ModelGraphFactory {

	public static Collection<ModelNode> toModel(GraphNode entry) {
		Map<GraphNode, ModelNode> found = new HashMap<>();
		toModel(entry, found);
		
		return found.values();
	}

	private static ModelNode toModel(GraphNode gNode, Map<GraphNode, ModelNode> found) {
		if (found.containsKey(gNode))
			return found.get(gNode);

		ModelNode mNode = modelNode(gNode);
		found.put(gNode, mNode);

		for (GraphEdge gEdge : gNode.getIn()) {
			ModelEdge mEdge = modelEdge(gEdge);
			mNode.addIn(mEdge);

			mEdge.setObjectType(mNode);
			toModel(gEdge.getSource(), found);
		}

		for (GraphEdge gEdge : gNode.getOut()) {
			ModelEdge mEdge = modelEdge(gEdge);
			mNode.addOut(mEdge);

			ModelNode mNode2 = toModel(gEdge.getTarget(), found);
			mEdge.setObjectType(mNode2);
		}

		return mNode;
	}

	private static ModelNode modelNode(GraphNode n) {
		ModelNode n2 = new ModelNode();
		n2.setOr(n);
		
		return n2;
	}

	private static ModelEdge modelEdge(GraphEdge e) {
		String id = e.getId();
		if (e.isInverse())
			id = invertProperty(id);
		
		return new ModelEdge(id);
	}

	private static String invertProperty(String name) {
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
