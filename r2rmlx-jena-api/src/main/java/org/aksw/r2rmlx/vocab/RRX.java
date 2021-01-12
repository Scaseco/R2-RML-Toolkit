package org.aksw.r2rmlx.vocab;

import org.aksw.r2rmlx.common.vocab.R2RMLXStrings;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class RRX {
	public static final String uri = R2RMLXStrings.uri;

	public static String getURI() { return uri; }
	public static Resource resource(String name) { return ResourceFactory.createResource(name); }
	public static Property property(String name) { return ResourceFactory.createProperty(name); }


	public static final Property languageColumn = property(R2RMLXStrings.languageColumn);
}
