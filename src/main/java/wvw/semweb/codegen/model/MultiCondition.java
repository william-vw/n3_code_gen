package wvw.semweb.codegen.model;

import java.util.ArrayList;
import java.util.List;

public abstract class ConditionList extends Condition {

	protected List<Condition> conditions = new ArrayList<>();

	public void add(Condition condition) {
		conditions.add(condition);
	}

	public List<Condition> getConditions() {
		return conditions;
	}
}
