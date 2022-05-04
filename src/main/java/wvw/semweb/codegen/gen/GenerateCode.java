package wvw.semweb.codegen.gen;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import wvw.semweb.codegen.model.CodeLogic;
import wvw.semweb.codegen.model.struct.CodeModel;
import wvw.semweb.codegen.model.struct.ModelProperty;
import wvw.semweb.codegen.model.struct.ModelStruct;

public abstract class GenerateCode {

	protected static final Logger log = LogManager.getLogger(GenerateCode.class);

	public abstract void generate(CodeModel model, CodeLogic logic, Collection<String> entryPoints, File output)
			throws IOException;

	protected boolean isInitField(ModelProperty prp, ModelStruct ofStruct) {
		return includeField(prp, ofStruct) && !prp.requiresArray();
	}

	// type property only needed/ possible if struct has constants
	// (e.g., in solidity, these will be added to a separate enum)

	protected boolean includeField(ModelProperty prp, ModelStruct ofStruct) {
		return (!prp.isTypePrp() || ofStruct.hasConstants());
	}
}
