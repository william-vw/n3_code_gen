package wvw.semweb.codegen.model;

import java.util.ArrayList;
import java.util.List;

import wvw.semweb.codegen.model.block.Block;
import wvw.semweb.codegen.model.cond.Conjunction;

public class CodeLogic {

	private List<IfThen> ifThens = new ArrayList<>();

	public void add(IfThen it) {
		ifThens.add(it);
	}

	public List<IfThen> getIfThens() {
		return ifThens;
	}
	
	public static class IfThen {

		private Conjunction cond;
		private Block block;

		public IfThen(Conjunction cond, Block block) {
			this.cond = cond;
			this.block = block;
		}

		public Conjunction getCondition() {
			return cond;
		}

		public Block getBlock() {
			return block;
		}

	}
}
