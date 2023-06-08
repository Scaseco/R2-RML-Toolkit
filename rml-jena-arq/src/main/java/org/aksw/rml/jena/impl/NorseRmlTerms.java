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

    /** Property IRI to introduce an alias for a reference expression */
    public static final String alias = NS + "alias";

    /** Property IRI to introduce an alias for a SPARQL expression. Variables can be aliases. */
    public static final String bind = NS + "bind";

    /** Property IRI to introduce a definition for an alias or macro name */
    public static final String definition = NS + "definition";
}
