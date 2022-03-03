package wvw.semweb.codegen.model;

public class Literal implements Operand {

	private Object value;

	public Literal(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
