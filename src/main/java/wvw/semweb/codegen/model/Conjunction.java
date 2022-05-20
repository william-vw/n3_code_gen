package wvw.semweb.codegen.model;

import java.util.stream.Collectors;

public class Conjunction extends ConditionList {

	@Override
	public Conditions getConditionType() {
		return Conditions.CONJ;
	}

	@Override
	public String toString() {
		return conditions.stream().map(c -> c.toString()).collect(Collectors.joining(" &&\n"));
	}
}
