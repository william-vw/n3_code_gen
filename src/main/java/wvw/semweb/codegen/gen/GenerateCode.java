package wvw.semweb.codegen.gen;

import java.util.Collection;

import wvw.semweb.codegen.model.CodeLogic;
import wvw.semweb.codegen.model.struct.CodeModel;

public interface GenerateCode {

	public void generate(CodeModel model, CodeLogic logic, Collection<String> entryPoints);

	public String getClasses();

	public String getLogic();
}
