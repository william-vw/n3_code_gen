package wvw.semweb.owl.gen.model;

import org.apache.jen3.datatypes.RDFDatatype;

public class ModelProperty extends ModelElement {

	private RDFDatatype dataType;
	private ModelClass objectType;

	public ModelProperty(String name) {
		super(name);
	}

	public boolean hasType() {
		return hasDataType() || hasObjectType();
	}

	public boolean hasDataType() {
		return dataType != null;
	}

	public RDFDatatype getDataType() {
		return dataType;
	}

	public void setDataType(RDFDatatype dataType) {
		this.dataType = dataType;
	}

	public boolean hasObjectType() {
		return objectType != null;
	}

	public ModelClass getObjectType() {
		return objectType;
	}

	public void setObjectType(ModelClass objectType) {
		this.objectType = objectType;

		objectType.addIn(this);
	}

	public void replaceObjectType(ModelClass objectType) {
		this.objectType = objectType;
	}

	@Override
	public String toString() {
		return super.toString() + (hasType() ? " -> " + (hasDataType() ? dataType : objectType.getString()) : "");
	}
}
