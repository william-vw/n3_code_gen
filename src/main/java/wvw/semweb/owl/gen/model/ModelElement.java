package wvw.semweb.owl.gen.model;

import java.util.Objects;

public class ModelElement {

	protected String name;
	protected String label;

	public ModelElement() {
	}

	public ModelElement(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean hasName() {
		return this.name != null;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean hasLabel() {
		return label != null;
	}

	public String getString() {
		return (hasLabel() ? label : name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModelElement other = (ModelElement) obj;
		return Objects.equals(name, other.name);
	}

	@Override
	public String toString() {
		return getString();
	}
}
