package wvw.semweb.codegen.model.struct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CodeModel {

	private static int structCnt = 0;
	
	private Map<String, ModelStruct> structs = new HashMap<>();

	public ModelStruct getStruct(String name) {
		ModelStruct ret = structs.get(name);
		if (ret == null) {
			ret = new ModelStruct(name, structCnt++);
			structs.put(name, ret);
		}

		return ret;
	}

	public void mergeWith(CodeModel newModel) {
		if (this.structs.isEmpty())
			this.structs = newModel.structs;
		else {
			newModel.structs.entrySet().stream().forEach(e -> {
				String name = e.getKey();
				ModelStruct struct = e.getValue();

				if (structs.containsKey(name)) {
					ModelStruct struct0 = structs.get(name);
					struct0.replacing(struct);

				} else
					structs.put(name, struct);
			});
		}
	}

	public Collection<ModelStruct> getAllStructs() {
		List<ModelStruct> sorted = new ArrayList<>(structs.values());
		sorted.sort(null);
		
		return sorted;
	}

	@Override
	public String toString() {
		return structs.values().stream().map(s -> s.toString()).collect(Collectors.joining("\n"));
	}
}
