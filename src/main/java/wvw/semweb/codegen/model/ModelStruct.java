package wvw.semweb.codegen.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModelStruct extends ModelElement {

	private Set<ModelElement> types = new HashSet<>();
	private Set<ModelElement> values = new HashSet<>();

	private List<ModelProperty> properties = new ArrayList<>();

	public ModelStruct(String name) {
		super(name);
	}

	public void addType(ModelElement type) {
		types.add(type);
	}

	public Collection<ModelElement> getTypes() {
		return types;
	}

	public void addValue(ModelElement value) {
		values.add(value);
	}

	public Collection<ModelElement> getValues() {
		return values;
	}

	public void addProperty(ModelProperty prp) {
		properties.add(prp);
	}

	public List<ModelProperty> getProperties() {
		return properties;
	}

	@Override
	public String toString() {
		String id = getString();

		return id + (!types.isEmpty() ? "\n\ttypes: " + types : "") + (!values.isEmpty() ? "\n\tvalues: " + values : "")
				+ (!properties.isEmpty() ? "\n\tproperties: " + properties : "");
	}
}
