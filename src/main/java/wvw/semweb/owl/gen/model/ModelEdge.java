package wvw.semweb.owl.gen.model;

import org.apache.jen3.datatypes.RDFDatatype;

public class ModelEdge extends ModelElement {

	private RDFDatatype dataType;
	private ModelNode objectType;

	public ModelEdge(String name) {
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

	public ModelNode getObjectType() {
		return objectType;
	}

	public void setObjectType(ModelNode objectType) {
		this.objectType = objectType;
	}

	public void replaceObjectType(ModelNode objectType) {
		this.objectType = objectType;
	}

	@Override
	public String toString() {
		return super.toString() + (hasType() ? " -> " + (hasDataType() ? dataType : objectType.getString()) : "");
	}
}
