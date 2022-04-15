package wvw.semweb.codegen.model;

import wvw.semweb.codegen.model.struct.ModelStruct;

public class CreateStruct implements Operand {

	private ModelStruct struct;

	public CreateStruct(ModelStruct struct) {
		this.struct = struct;
	}

	public ModelStruct getStruct() {
		return struct;
	}

	@Override
	public Operands getType() {
		return Operands.CREATE_STRUCT;
	}
	
	@Override
	public String toString() {
		return "create " + struct.getString();
	}
}
