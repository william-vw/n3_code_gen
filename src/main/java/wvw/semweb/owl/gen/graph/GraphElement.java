package wvw.semweb.owl.gen.graph;

import java.util.Objects;

public class GraphElement {

	protected String id;

	public GraphElement(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
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

	@Override
	public String toString() {
		return id;
	}
}
