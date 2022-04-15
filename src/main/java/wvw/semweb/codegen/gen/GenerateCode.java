package wvw.semweb.codegen.gen;

import java.util.Collection;

import wvw.semweb.codegen.model.CodeLogic;
import wvw.semweb.codegen.model.struct.CodeModel;

public abstract class GenerateCode {

	public abstract void generate(CodeModel model, CodeLogic logic, Collection<String> entryPoints);

	public abstract String getClasses();

	public abstract String getLogic();
}
