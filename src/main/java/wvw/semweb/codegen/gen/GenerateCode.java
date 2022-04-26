package wvw.semweb.codegen.gen;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import wvw.semweb.codegen.model.CodeLogic;
import wvw.semweb.codegen.model.struct.CodeModel;

public abstract class GenerateCode {

	protected static final Logger log = LogManager.getLogger(GenerateCode.class);

	public abstract void generate(CodeModel model, CodeLogic logic, Collection<String> entryPoints, File output)
			throws IOException;
}
