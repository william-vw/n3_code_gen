package wvw.semweb.owl.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jen3.datatypes.RDFDatatype;
import org.apache.jen3.datatypes.TypeMapper;
import org.apache.jen3.datatypes.xsd.XSDDatatype;
import org.apache.jen3.n3.N3Model;
import org.apache.jen3.rdf.model.Resource;
import org.apache.jen3.rdf.model.Statement;
import org.apache.jen3.rdf.model.StmtIterator;
import org.apache.jen3.vocabulary.RDFS;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import wvw.semweb.owl.OntologyAnalyser;
import wvw.semweb.owl.gen.model.ModelEdge;
import wvw.semweb.owl.gen.model.ModelElement;
import wvw.semweb.owl.gen.model.ModelNode;

public class ModelOntologyAnalyser extends OntologyAnalyser {

	private static final Logger log = LogManager.getLogger(ModelOntologyAnalyser.class);

	private N3Model ontology;

	public ModelOntologyAnalyser(N3Model ontology) {
		this.ontology = ontology;
	}

	public List<Resource> findTypes(ModelNode cls) {
//		log.debug("finding domain for: " + properties);

		List<Resource> allDomains = cls.getOut().stream()
				.flatMap(
						p -> ontology.listStatements(ontology.createResource(p.getName()), RDFS.domain, (Resource) null)
								.toList().stream().map(stmt -> stmt.getObject()))
				.collect(Collectors.toList());

		List<Resource> allRanges = cls.getIn().stream()
				.flatMap(p -> ontology.listStatements(ontology.createResource(p.getName()), RDFS.range, (Resource) null)
						.toList().stream().map(stmt -> stmt.getObject()))
				.collect(Collectors.toList());

		List<Resource> allTypes = new ArrayList<>();
		allTypes.addAll(allDomains);
		allTypes.addAll(allRanges);
		filterSuperClasses(allTypes); // (also takes care of duplicates)

		log.debug("> class:\n" + cls);
		log.debug("> domains:\n" + allDomains);
		log.debug("> ranges:\n" + allRanges);
		log.debug("> most specific types:\n" + allTypes);

		log.debug("\n");

		return allTypes;
	}

	public void findAnnotations(ModelElement el) {
		Resource label = ontology.createResource(el.getName()).getPropertyResourceValue(RDFS.label);
		if (label != null)
			el.setLabel(label.asLiteral().getString());
	}

	public void findPropertyType(ModelEdge property) {
		StmtIterator it = ontology.listStatements(ontology.createResource(property.getName()), RDFS.range,
				(Resource) null);

		while (it.hasNext()) {
			Statement stmt = it.next();

			Resource type = stmt.getObject();
			if (!type.isURI())
				log.debug("found non-URI range for " + property);

			else {
				if (type.getURI().startsWith(XSDDatatype.XSD)) {
					RDFDatatype dt = TypeMapper.getInstance().getTypeByName(type.getURI());

					if (dt == null)
						log.debug("could not find datatype for XSD range " + type);
					else
						property.setDataType(dt);
				}
			}
		}

		if (!property.hasType())
			log.error("no (usable) object or data type found for " + property);
	}
}
