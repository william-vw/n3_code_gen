package wvw.semweb.codegen.parse.post;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import wvw.semweb.codegen.model.IfThen;
import wvw.semweb.codegen.model.struct.CodeModel;
import wvw.semweb.codegen.parse.rule.RuleGraph;

public abstract class ModelPostprocessor {

	protected static final Logger log = LogManager.getLogger(ModelPostprocessor.class);

	public enum PostprocessTypes {
		// @formatter:off
		INSERT_STRUCT_EXISTS_CHECK, 
		REMOVE_EXISTS_CHECK_LITERALS, // (this one is done by default)
		MERGE_STRUCTS_W_ARRAYS_IN_ROOT
		// @formatter:on
	}

	public static ModelPostprocessor create(PostprocessTypes type) {
		switch (type) {

		case INSERT_STRUCT_EXISTS_CHECK:
			return new InsertStructExistsCheck();

		case REMOVE_EXISTS_CHECK_LITERALS:
			return new RemoveExistChecksForLiterals();

		case MERGE_STRUCTS_W_ARRAYS_IN_ROOT:
			return new MergeStructsWithArraysIntoRoot();
		}

		return null;
	}

	public abstract void postprocess(CodeModel model, IfThen it, RuleGraph ruleGraph);
}
