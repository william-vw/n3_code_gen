package wvw.semweb.codegen.model;

import org.apache.jen3.datatypes.RDFDatatype;

public class ModelType {

	private RDFDatatype dataType;
	private ModelStruct objectType;
	
	public ModelType(RDFDatatype dataType) {
		this.dataType = dataType;
	}

	public ModelType(ModelStruct objectType) {
		this.objectType = objectType;
	}

	public boolean hasDataType() {
		return dataType != null;
	}
	
	public RDFDatatype getDataType() {
		return dataType;
	}
	
	public boolean hasObjectType() {
		return objectType != null;
	}

	public ModelStruct getObjectType() {
		return objectType;
	}
	
	@Override
	public String toString() {
		return (hasDataType() ? dataType.toString() : objectType.getString());
	}
}
