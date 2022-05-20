package wvw.semweb.codegen;

import java.io.File;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import wvw.semweb.codegen.gen.GenerateCode;
import wvw.semweb.codegen.gen.GenerateCode.CodeTypes;
import wvw.semweb.codegen.parse.ParseModelLogic;

//(major)

// TODO in JS, use same mapping solution as in solidity
// ("some" solution doesn't work)

//TODO should add all sub-types of a given type as constants
//(input data could have any of those sub-types)
//(see ModelVisitorA#doVisit)

//TODO limitations of struct merging for solidity: 
//combination of lifestyle with drug subplans will likely not work

// TODO assuming that the rule ordering reflects the chaining sequence

// TODO can only create new struct with *non* array-like properties
// (e.g., try DrugSubPlan; remove functional property type)
// (in solidity: "TypeError: Struct containing a (nested) mapping cannot be constructed")

//(minor)

//TODO properly parametrize ModelVisitorImpl code (e.g., CodeLogicVisitor, CodeModelVisitor)

//TODO post-processing where structs sharing a (non-trivial) superclass (i.e., not owl:Thing, entity, ..)
// are merged together

public class CodeGen {

	private static final Logger log = LogManager.getLogger(CodeGen.class);

	public static void main(String[] args) throws Exception {
		generateCode(new File("diabetes-iot-4.n3"), new File("DMTO2.n3"), new File("src/main/resources/"),
				CodeTypes.SOLIDITY);
	}

	public static void generateCode(File ruleFile, File ontologyFile, File outFolder, CodeTypes codeType)
			throws Exception {

		log.info("-- parsing model and logic");

		ParseModelLogic parser = new ParseModelLogic();
		parser.parse(ruleFile, ontologyFile, codeType.getRequirements());

		log.info("\n");
		log.info("-- generating code");

		GenerateCode genCode = GenerateCode.create(codeType);

		String outFile = ruleFile.getName().substring(0, ruleFile.getName().lastIndexOf(".")) + "." + codeType.getExt();
		outFolder = new File(outFolder, outFile);
		genCode.generate(parser.getModel(), parser.getLogic(), outFolder);

		log.info("\ncode written to: " + outFolder.getAbsolutePath());
	}
}
