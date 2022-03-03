package wvw.semweb.codegen.model;

import java.util.LinkedList;
import java.util.stream.Collectors;

import wvw.semweb.codegen.model.struct.ModelProperty;

public class NodePath implements Operand {

	private Operand start;
	private LinkedList<ModelProperty> path = new LinkedList<>();

	public NodePath(Operand start) {
		this.start = start;
	}

	private NodePath(Operand start, LinkedList<ModelProperty> path) {
		this.start = start;
		this.path = path;
	}

	public Operand getStart() {
		return start;
	}

	public void add(ModelProperty prp) {
		path.add(prp);
	}

	public LinkedList<ModelProperty> getPath() {
		return path;
	}

	public NodePath copy() {
		return new NodePath(start, new LinkedList<>(path));
	}

	@Override
	public String toString() {
		return start + "." + path.stream().map(p -> p.getString()).collect(Collectors.joining("."));
	}
}
