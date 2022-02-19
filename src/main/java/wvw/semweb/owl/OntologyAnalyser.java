package wvw.semweb.owl;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jen3.rdf.model.Resource;
import org.apache.jen3.vocabulary.RDFS;

public class OntologyAnalyser {

	protected void filterSuperClasses(List<Resource> clses) {
		l0: for (int i = 0; i < clses.size(); i++) {
			Resource cls1 = clses.get(i);

			for (int j = 0; j < clses.size(); j++) {
				if (i == j)
					continue;

				Resource cls2 = clses.get(j);
				if (isMoreGeneral(cls1, cls2)) {
					clses.remove(i--);

					continue l0;
				}
			}
		}
	}

	protected boolean isMoreGeneral(Resource cls1, Resource cls2) {
		Set<Resource> found = new HashSet<>();

		LinkedList<Resource> supClses = new LinkedList<>();
		supClses.add(cls2);

		while (!supClses.isEmpty()) {
			Resource supCls = supClses.removeFirst();

			if (found.contains(supCls))
				continue;
			else
				found.add(supCls);

			if (supCls.equals(cls1))
				return true;

			supClses.addAll(supCls.listProperties(RDFS.subClassOf).toList().stream().map(stmt -> stmt.getObject())
					.collect(Collectors.toList()));
		}

		return false;
	}
}
