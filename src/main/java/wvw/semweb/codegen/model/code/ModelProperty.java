package wvw.semweb.codegen.model.code;

public class ModelProperty extends ModelElement {

	private ModelType target;

	public ModelProperty(String name) {
		super(name);
	}
	
	public boolean hasTarget() {
		return target != null;
	}

	public ModelType getTarget() {
		return target;
	}

	public void setTarget(ModelType target) {
		this.target = target;
	}
	
	@Override
	public String toString() {
		return super.toString() + (hasTarget() ? " -> " + target : "");
	}
}
