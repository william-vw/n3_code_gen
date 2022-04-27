package wvw.semweb.codegen.model.struct;

public class ModelProperty extends ModelElement {

	private ModelType target;
	private Integer maxCardinality;
	private boolean isKey;
	private boolean isTypePrp;

	public ModelProperty(String name) {
		super(name);
	}

	public ModelProperty(String name, Integer maxCardinality) {
		super(name);

		this.maxCardinality = maxCardinality;
	}

	public static ModelProperty typeProperty() {
		ModelProperty typePrp = new ModelProperty("type", 1);
		typePrp.setTypePrp(true);

		return typePrp;
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

	public ModelProperty(String name, ModelType target, Integer maxCardinality, boolean isKey) {
		super(name);

		this.target = target;
		this.maxCardinality = maxCardinality;
		this.isKey = isKey;
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

	public boolean requiresArray() {
		return !hasMaxCardinality() || getMaxCardinality() > 1;
	}

	public boolean isKey() {
		return isKey;
	}

	public void setTypePrp(boolean isTypePrp) {
		this.isTypePrp = isTypePrp;
	}

	public boolean isTypePrp() {
		return isTypePrp;
	}

	@Override
	public String toString() {
		String cardStr = (hasMaxCardinality() && maxCardinality == 1 ? "[1]" : "");

		return super.toString() + cardStr + (hasTarget() ? " -> " + target : "");
	}
}
