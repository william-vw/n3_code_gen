package wvw.semweb.codegen.model.struct;

public class ModelProperty extends ModelElement {

	private ModelType target;
	private Integer maxCardinality;

	public ModelProperty(String name) {
		super(name);
	}

	public ModelProperty(String name, Integer maxCardinality) {
		super(name);
		
		this.maxCardinality = maxCardinality;
	}

	public ModelProperty(String name, ModelType target) {
		super(name);

		this.target = target;
	}

	public ModelProperty(String name, ModelType target, Integer maxCardinality) {
		super(name);

		this.target = target;
		this.maxCardinality = maxCardinality;
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

	// semweb4j determines whether get() vs. getAll()
	// depending on existence of maxCardinality = 1
	// (unqualified!)

	public Integer getMaxCardinality() {
		return maxCardinality;
	}

	public void setMaxCardinality(Integer maxCardinality) {
		this.maxCardinality = maxCardinality;
	}

	public boolean hasMaxCardinality() {
		return maxCardinality != null;
	}

	@Override
	public String toString() {
		String cardStr = (hasMaxCardinality() && maxCardinality == 1 ? "[1]" : "");

		return super.toString() + cardStr + (hasTarget() ? " -> " + target : "");
	}
}
