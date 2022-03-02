package wvw.semweb.codegen.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CodeModel {

	private Map<String, ModelStruct> structs = new HashMap<>();

	public ModelStruct newStruct(String name) {
		ModelStruct ret = structs.get(name);
		if (ret == null) {
			ret = new ModelStruct(name);
			structs.put(name, ret);
		}

		return ret;
	}

	public Collection<ModelStruct> getAllStructs() {
		return structs.values();
	}

	@Override
	public String toString() {
		return structs.values().stream().map(s -> s.toString()).collect(Collectors.joining("\n"));
	}
}
