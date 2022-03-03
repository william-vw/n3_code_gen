package wvw.semweb.codegen.model.op;

import wvw.semweb.codegen.model.Operand;
import wvw.semweb.codegen.model.code.ModelStruct;

public class CreateStruct implements Operand {

	private ModelStruct struct;

	public CreateStruct(ModelStruct struct) {
		this.struct = struct;
	}

	public ModelStruct getStruct() {
		return struct;
	}
	
	@Override
	public String toString() {
		return "create " + struct.getString();
	}
}
