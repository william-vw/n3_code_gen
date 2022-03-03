package wvw.semweb.codegen.model;

import wvw.semweb.codegen.model.struct.ModelElement;
import wvw.semweb.codegen.model.struct.ModelStruct;

public class StructConstant implements Operand {

	private ModelStruct struct;
	private ModelElement constant;

	public StructConstant(ModelStruct struct, ModelElement constant) {
		this.struct = struct;
		this.constant = constant;
	}

	public ModelStruct getStruct() {
		return struct;
	}

	public ModelElement getConstant() {
		return constant;
	}

	@Override
	public String toString() {
		return struct.getString() + ":" + constant.getString();
	}
}
