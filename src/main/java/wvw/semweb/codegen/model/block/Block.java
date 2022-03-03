package wvw.semweb.codegen.model.block;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Block {

	private List<Operation> operations = new ArrayList<>();

	public void add(Operation op) {
		operations.add(op);
	}

	public List<Operation> getOperations() {
		return operations;
	}

	@Override
	public String toString() {
		return operations.stream().map(o -> o.toString()).collect(Collectors.joining("\n"));
	}
}
