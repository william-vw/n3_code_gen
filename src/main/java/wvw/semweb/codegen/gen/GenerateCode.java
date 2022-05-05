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
import wvw.semweb.codegen.parse.post.ModelPostprocessor.PostprocessTypes;

public abstract class GenerateCode {

	protected static final Logger log = LogManager.getLogger(GenerateCode.class);

	public enum CodeTypes {

		JAVASCRIPT("js"), SOLIDITY("sol", PostprocessTypes.MERGE_STRUCTS_W_ARRAYS_IN_ROOT);

		private String ext;
		private PostprocessTypes[] requirements;

		private CodeTypes(String ext, PostprocessTypes... requirements) {
			this.ext = ext;
			this.requirements = requirements;
		}

		public String getExt() {
			return ext;
		}

		public PostprocessTypes[] getRequirements() {
			return requirements;
		}
	}

	public static GenerateCode create(CodeTypes type) {
		switch (type) {

		case JAVASCRIPT:
			return new GenerateJavaScript();

		case SOLIDITY:
			return new GenerateSolidity();
		}

		return null;
	}

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
