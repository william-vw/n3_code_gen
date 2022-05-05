package wvw.semweb.codegen;

import java.io.File;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import wvw.semweb.codegen.gen.GenerateCode;
import wvw.semweb.codegen.gen.GenerateCode.CodeTypes;
import wvw.semweb.codegen.parse.ParseModelLogic;

public class CodeGen {

	private static final Logger log = LogManager.getLogger(CodeGen.class);

	public static void main(String[] args) throws Exception {
		generateCode(new File("diabetes-iot.n3"), new File("DMTO2.n3"), new File("src/main/resources/out"),
				CodeTypes.SOLIDITY);
	}

	public static void generateCode(File ruleFile, File ontologyFile, File outFile, CodeTypes codeType)
			throws Exception {

		log.info("-- parsing model and logic");

		ParseModelLogic parser = new ParseModelLogic();
		parser.parseClassModel(ruleFile, ontologyFile, codeType.getRequirements());

		log.info("\n");
		log.info("-- generating code");

		GenerateCode genCode = GenerateCode.create(codeType);

		outFile = new File(outFile.getParentFile(), outFile.getName() + "." + codeType.getExt());
		genCode.generate(parser.getModel(), parser.getLogic(), parser.getEntryPoints(), outFile);

		log.info("\ncode written to: " + outFile.getAbsolutePath());
	}
}
