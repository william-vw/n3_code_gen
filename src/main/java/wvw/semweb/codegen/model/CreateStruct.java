package wvw.semweb.codegen.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import wvw.semweb.codegen.model.struct.ModelStruct;

public class CreateStruct implements Operand {

	private ModelStruct struct;
	private List<Assignment> constructParams = new ArrayList<>();

	public CreateStruct(ModelStruct struct) {
		this.struct = struct;
	}

	public ModelStruct getStruct() {
		return struct;
	}

	public void add(Assignment constructParam) {
		constructParams.add(constructParam);
	}

	public List<Assignment> getConstructParams() {
		return constructParams;
	}

	@Override
	public Operands getType() {
		return Operands.CREATE_STRUCT;
	}

	@Override
	public String toString() {
		return "create " + struct.getString() + "("
				+ constructParams.stream().map(p -> p.toString()).collect(Collectors.joining(", ")) + ")";
	}
}
