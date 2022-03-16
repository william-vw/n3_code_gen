package wvw.semweb.codegen;

import java.io.File;

import org.apache.jen3.util.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import wvw.semweb.codegen.gen.GenerateCode;
import wvw.semweb.codegen.gen.GenerateJsCode;

public class CodeGen {

	private static final Logger log = LogManager.getLogger(CodeGen.class);

	public static void main(String[] args) throws Exception {
		log.info("-- parsing model and logic");

		ParseModelLogic parser = new ParseModelLogic();
		parser.parseClassModel("diabetes-iot.n3", "DMTO2.n3");
//		parser.parseClassModel("test.n3", "ontology.n3", "x");
//		parser.parseClassModel("test2.n3", "ontology.n3", "x");

		log.info("\n");
		log.info("-- generating code");

		GenerateCode genCode = new GenerateJsCode();
		genCode.generate(parser.getModel(), parser.getLogic());

		File output = new File("src/main/resources/out.js");
		IOUtils.writeToFile(genCode.getClasses(), output);
		IOUtils.writeToFile(genCode.getLogic(), output, true);

		log.info("code written to: " + output.getAbsolutePath());
	}
}
