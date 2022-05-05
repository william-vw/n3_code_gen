package wvw.semweb.codegen.model.struct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import wvw.semweb.codegen.rule.GraphNode;

public class CodeModel {

	private static int structCnt = 0;

	private Map<String, ModelStruct> nameStructs = new HashMap<>();
	private Map<GraphNode, ModelStruct> nodeStructs = new HashMap<>();

	public ModelStruct getOrCreateStruct(String name, GraphNode node) {
		ModelStruct ret = nameStructs.get(name);
		if (ret == null) {
			ret = new ModelStruct(name, structCnt++);

			nameStructs.put(name, ret);
			nodeStructs.put(node, ret);
		}

		return ret;
	}

	public void mergeWith(CodeModel newModel) {
		if (this.nameStructs.isEmpty())
			this.nameStructs = newModel.nameStructs;
		else {
			newModel.nameStructs.entrySet().stream().forEach(e -> {
				String name = e.getKey();
				ModelStruct struct = e.getValue();

				if (nameStructs.containsKey(name)) {
					ModelStruct struct0 = nameStructs.get(name);
					struct0.replacing(struct);

				} else
					nameStructs.put(name, struct);
			});
		}
	}

	// TODO
	public void removeStruct(ModelStruct struct) {
		nameStructs.entrySet().removeIf(e -> e.getValue().equals(struct));
		nodeStructs.entrySet().removeIf(e -> e.getValue().equals(struct));
	}

	public ModelStruct getStruct(GraphNode node) {
		return nodeStructs.get(node);
	}

	public Collection<ModelStruct> getAllStructs() {
		List<ModelStruct> sorted = new ArrayList<>(nameStructs.values());
		sorted.sort(null);

		return sorted;
	}

	@Override
	public String toString() {
		return nameStructs.values().stream().map(s -> s.toString()).collect(Collectors.joining("\n"));
	}
}
