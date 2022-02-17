package wvw.semweb.owl.gen.model;

import java.util.Collection;
import java.util.HashSet;

public class ModelClass extends ModelElement {

	private Collection<ModelProperty> in = new HashSet<>();
	private Collection<ModelProperty> out = new HashSet<>();

	public ModelClass() {
		super();
	}

	public ModelClass(String name) {
		super(name);
	}

	public ModelClass(ModelClass cls1, ModelClass cls2) {
		super();

		out.addAll(cls1.getOut());
		out.addAll(cls2.getOut());
	}

	public void replacing(ModelClass cls2) {
		mergeWith(cls2);

		cls2.replaceWith(this);
	}

	private void mergeWith(ModelClass cls2) {
		in.addAll(cls2.getIn());
		out.addAll(cls2.getOut());
	}

	private void replaceWith(ModelClass cls) {
		in.forEach(p -> p.replaceObjectType(cls));
	}

	public void addOut(ModelProperty prp) {
		out.add(prp);
	}

	public Collection<ModelProperty> getOut() {
		return out;
	}

	public void addIn(ModelProperty prp) {
		in.add(prp);
	}

	public Collection<ModelProperty> getIn() {
		return in;
	}

	@Override
	public String toString() {
		return super.toString() + (!in.isEmpty() ? "\n\tin:" + in : "") + (!out.isEmpty() ? "\n\tout: " + out : "");
	}
}
