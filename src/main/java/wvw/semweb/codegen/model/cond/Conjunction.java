package wvw.semweb.codegen.model.cond;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Conjunction {

	private List<Comparison> conditions = new ArrayList<>();

	public void add(Comparison condition) {
		conditions.add(condition);
	}

	public List<Comparison> getConditions() {
		return conditions;
	}

	@Override
	public String toString() {
		return conditions.stream().map(c -> c.toString()).collect(Collectors.joining(" &&\n"));
	}
}
