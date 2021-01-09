package org.aksw.r2rml.jena.plugin;

import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

public class R2rmlApiTests {
	

	@Test
	public void testForR2rmlClassAvailability() {
		TriplesMap triplesMap = ModelFactory.createDefaultModel().createResource().as(TriplesMap.class);
	}

}
