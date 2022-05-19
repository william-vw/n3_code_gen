package wvw.semweb.codegen.model;

import wvw.semweb.codegen.parse.rule.ann.Annotated;

public class CodeLogic extends Block {

	private String rulesName;

	private Annotated annotations = new Annotated();

	public String getRulesName() {
		return rulesName;
	}

	public void setRulesName(String rulesName) {
		this.rulesName = rulesName;
	}

	public Annotated getAnnotations() {
		return annotations;
	}
}
