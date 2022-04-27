package wvw.semweb.codegen.gen;

import wvw.semweb.codegen.model.NodePath;
import wvw.semweb.codegen.model.Operand;
import wvw.semweb.codegen.model.Operand.Operands;
import wvw.semweb.codegen.model.struct.ModelProperty;

public class Util {

	public static boolean involvesArrayCheck(Operand op1) {
		if (op1.getType() == Operands.NODE_PATH) {
			NodePath path = (NodePath) op1;

			if (path.size() > 1) {
				ModelProperty prp = path.getPath().get(path.size() - 2);
				return prp.requiresArray();
			}
		}

		return false;
	}

	public static boolean involvesArrayAssign(Operand op1) {
		if (op1.getType() == Operands.NODE_PATH) {
			ModelProperty lastPrp = ((NodePath) op1).getPath().getLast();
			return lastPrp.requiresArray();
		}

		return false;
	}
}
