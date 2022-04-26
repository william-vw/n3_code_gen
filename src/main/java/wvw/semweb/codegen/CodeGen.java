package wvw.semweb.codegen;

import java.io.File;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import wvw.semweb.codegen.gen.GenerateCode;
import wvw.semweb.codegen.gen.GenerateSolidity;

public class CodeGen {

	private static final Logger log = LogManager.getLogger(CodeGen.class);

	public static void main(String[] args) throws Exception {
		log.info("-- parsing model and logic");

		ParseModelLogic parser = new ParseModelLogic();
		parser.parseClassModel(new File("diabetes-iot.n3"), new File("DMTO2.n3"));
//		parser.parseClassModel(new File("test.n3"), new File("ontology.n3"));
//		parser.parseClassModel(new File("test2.n3"), new File("ontology.n3"));

		log.info("\n");
		log.info("-- generating code");

//		GenerateCode genCode = new GenerateJavaScript();
//		File output = new File("src/main/resources/out.js");

		GenerateCode genCode = new GenerateSolidity();
		File output = new File("src/main/resources/out.sol");

		genCode.generate(parser.getModel(), parser.getLogic(), parser.getEntryPoints(), output);

		log.info("\n\ncode written to: " + output.getAbsolutePath());
	}
}
