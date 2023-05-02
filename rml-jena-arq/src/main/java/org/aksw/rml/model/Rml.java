package org.aksw.rml.model;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class Rml {
    public static final Property logicalSource = ResourceFactory.createProperty(RmlTerms.NS + "logicalSource");
    public static final Property reference = ResourceFactory.createProperty(RmlTerms.NS + "reference");

    public static final Property source = ResourceFactory.createProperty(RmlTerms.NS + "source");
    public static final Property referenceFormulation = ResourceFactory.createProperty(RmlTerms.NS + "referenceFormulation");
    public static final Property iterator = ResourceFactory.createProperty(RmlTerms.NS + "iterator");
}
