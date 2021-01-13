package org.aksw.r2rmlx.jena.test;

import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.aksw.r2rml.jena.vocab.RR;
import org.aksw.r2rmlx.domain.api.TermMapX;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;

public class R2rmlxApiTests {
	
	@Test
	public void testR2rmlApi() {
		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("rdfs", RDFS.uri);
		model.setNsPrefix("rr", RR.uri);
		
		TriplesMap triplesMap = model.createResource().as(TriplesMap.class); 
		
		triplesMap
			.setSubjectIri("urn:s")
			.addNewPredicateObjectMap()
				.addPredicate("urn:p")
				.addNewObjectMap()
					.setColumn("iri")
					.setDatatype(RR.IRI)
					.as(TermMapX.class)
					.addNewConstraint()
						.asPrefixConstraint()
						.addPrefixes(RDFS.getURI(), FOAF.NS);
		
		// All domain classes of the R2RML API *ARE* Jena Resources.
		// Hence, any information - such as types or custom attributes - can be freely attached:
		triplesMap
			.addProperty(RDF.type, RR.TriplesMap)
			.addProperty(RDFS.label, "My R2RML Mapping");
		
		RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
	}

}

