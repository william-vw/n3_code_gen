package wvw.semweb.owl.gen.graph;

import java.util.Objects;

import wvw.utils.rdf.NS;

public class GraphElement {

	protected Object id;

	public GraphElement(Object id) {
		this.id = id;
	}

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GraphElement other = (GraphElement) obj;
		return Objects.equals(id, other.id);
	}

	public String prettyPrint() {
		return NS.toQname(id.toString());
	}

	@Override
	public String toString() {
		return prettyPrint();
	}
}
