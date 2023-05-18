package org.aksw.rml.jena.impl;

import org.aksw.jena_sparql_api.sparql.ext.init.NorseTerms;

/** Terms for the norse extensions of RML */
public class NorseRmlTerms {
    public static final String NS = NorseTerms.NS + "rml.";

    /** IRI for SERVICE <> {} blocks that contain RML source specifications */
    public static final String RML_SOURCE_SERVICE_IRI = NS + "source";

    /**
     * The variable for specifying the variable that should be bound
     * to the records of a logical source.
     * <pre>
     * :x
     *   a rml:LogicalSource ;
     *   rml:source "myfile.csv" ;
     *   asx:rml.output ?output .
     * </pre>
     */
    public static final String output = NS + "output";

    public static final String alias = NS + "alias";
}
