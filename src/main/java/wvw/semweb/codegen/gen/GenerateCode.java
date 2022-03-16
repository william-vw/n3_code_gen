package wvw.semweb.codegen.gen;

import wvw.semweb.codegen.model.CodeLogic;
import wvw.semweb.codegen.model.struct.CodeModel;

public interface GenerateCode {

	public void generate(CodeModel model, CodeLogic logic, String... entries);
	
	public String getClasses();
	
	public String getLogic();
}
