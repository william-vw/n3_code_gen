package wvw.semweb.codegen.model.block;

import wvw.semweb.codegen.model.Operand;

public class Assignment implements Operation {

	private Operand op1;
	private Operand op2;

	public Assignment(Operand op1, Operand op2) {
		this.op1 = op1;
		this.op2 = op2;
	}

	public Operand getOp1() {
		return op1;
	}

	public Operand getOp2() {
		return op2;
	}

	@Override
	public String toString() {
		return op1 + " = " + op2;
	}
}
