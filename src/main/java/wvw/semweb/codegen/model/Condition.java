package wvw.semweb.codegen.model;

public abstract class Condition implements CodeStatement {

	public enum Conditions {
		CONJ, CMP
	}

	@Override
	public Codes getStatementType() {
		return Codes.COND;
	}

	public abstract Conditions getConditionType();
}
