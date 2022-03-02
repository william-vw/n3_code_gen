package wvw.semweb.codegen.rule;

public class GraphEdge extends GraphElement {

	private GraphNode source;
	private GraphNode target;

	private boolean inverse;

	public GraphEdge(Object id) {
		super(id);
	}

	public GraphEdge(Object id, GraphNode source, GraphNode target) {
		super(id);
		this.source = source;
		this.target = target;
	}

	public GraphNode getSource() {
		return source;
	}

	public void setSource(GraphNode source) {
		this.source = source;
	}

	public GraphNode getTarget() {
		return target;
	}

	public void setTarget(GraphNode target) {
		this.target = target;
	}

	public boolean isInverse() {
		return inverse;
	}

	public void setInverse(boolean inverse) {
		this.inverse = inverse;
	}

	@Override
	public String toString() {
		return "--" + super.toString() + (inverse ? "(i)" : "") + "-> " + target.prettyPrint();
	}
}
