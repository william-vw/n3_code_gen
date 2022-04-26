package wvw.semweb.codegen.model.struct;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.apache.jen3.util.iterator.WrappedIterator;

public class ModelStruct extends ModelElement {

	private Set<ModelElement> types = new HashSet<>();
	private Set<ModelElement> values = new HashSet<>();
	private Set<ModelProperty> properties = new HashSet<>();

	public ModelStruct(String name) {
		super(name);

		properties.add(ModelProperty.typeProperty());
	}

	public void addType(ModelElement type) {
//		if (!type.getName().equals(name))
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

	public Iterator<ModelElement> getConstants() {
		return WrappedIterator.create(types.iterator()).andThen(values.iterator());
	}

	public void addProperty(ModelProperty prp) {
		properties.add(prp);
	}

	public Collection<ModelProperty> getProperties() {
		return properties;
	}

	public Optional<ModelProperty> getKeyProperty() {
		return properties.stream().filter(p -> p.isKey()).findAny();
	}

	public void replacing(ModelStruct struct2) {
		mergeWith(struct2);
		struct2.replaceWith(this);
	}

	private void mergeWith(ModelStruct struct2) {
		types.addAll(struct2.getTypes());
		values.addAll(struct2.getValues());
		properties.addAll(struct2.getProperties());
	}

	private void replaceWith(ModelStruct struct) {
		// TODO ideally we update all "incoming" properties
		// (i.e., with this struct as target) with the given struct
		// but that requires a bit more housekeeping and doesn't seem needed for code
		// generation
	}

	@Override
	public String toString() {
		String id = getString();

		return id + (!types.isEmpty() ? "\n\ttypes: " + types : "") + (!values.isEmpty() ? "\n\tvalues: " + values : "")
				+ (!properties.isEmpty() ? "\n\tproperties: " + properties : "");
	}
}
