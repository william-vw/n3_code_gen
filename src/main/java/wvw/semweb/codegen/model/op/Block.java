package wvw.semweb.codegen.model.op;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Block {

	private List<Operation> operations = new ArrayList<>();

	public void add(Operation op) {
		operations.add(op);
	}

	@Override
	public String toString() {
		return operations.stream().map(o -> o.toString()).collect(Collectors.joining("\n"));
	}
}
