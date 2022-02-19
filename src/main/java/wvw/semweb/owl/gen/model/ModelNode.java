package wvw.semweb.owl.gen.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import wvw.semweb.owl.gen.graph.GraphNode;

public class ModelNode extends ModelElement {

	private List<GraphNode> or = new ArrayList<>();

	private Collection<ModelEdge> in = new HashSet<>();
	private Collection<ModelEdge> out = new HashSet<>();

	public ModelNode() {
	}

	public ModelNode(String name) {
		super(name);
	}

	public ModelNode(ModelNode node1, ModelNode node2) {
		out.addAll(node1.getOut());
		out.addAll(node2.getOut());
	}
	
	public void setOr(GraphNode or) {
		this.or.add(or);
	}

	public List<GraphNode> getOr() {
		return or;
	}

	public void replacing(ModelNode node2) {
		mergeWith(node2);
		node2.replaceWith(this);

		or.addAll(node2.getOr());
	}

	private void mergeWith(ModelNode node2) {
		in.addAll(node2.getIn());
		out.addAll(node2.getOut());
	}

	private void replaceWith(ModelNode node) {
		in.forEach(p -> p.replaceObjectType(node));
	}

	public void addOut(ModelEdge edge) {
		out.add(edge);
	}

	public Collection<ModelEdge> getOut() {
		return out;
	}

	public void addIn(ModelEdge edge) {
		in.add(edge);
	}

	public Collection<ModelEdge> getIn() {
		return in;
	}

	@Override
	public String toString() {
		return super.toString() + (!in.isEmpty() ? "\n\tin:" + in : "") + (!out.isEmpty() ? "\n\tout: " + out : "");
	}
}
