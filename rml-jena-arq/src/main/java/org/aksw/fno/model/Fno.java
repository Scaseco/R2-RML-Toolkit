package org.aksw.fno.model;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class Fno {
    public static final Property predicate = ResourceFactory.createProperty(FnoTerms.NS + "predicate");
    public static final Property expects = ResourceFactory.createProperty(FnoTerms.NS + "expects");
    public static final Property returns = ResourceFactory.createProperty(FnoTerms.NS + "returns");
    public static final Property executes = ResourceFactory.createProperty(FnoTerms.NS + "executes");
}
