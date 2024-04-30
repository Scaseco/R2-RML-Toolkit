package org.aksw.rml2.vocab.jena;
/**
 *
 */


import org.aksw.rml.v2.common.vocab.Rml2Terms;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * The R2RML vocabulary
 *
 * @author Claus Stadler
 *
 */
public class RML2 {
    public static final String uri = Rml2Terms.uri;

    public static String getURI() { return uri; }
    public static Resource resource(String name) { return ResourceFactory.createResource(name); }
    public static Property property(String name) { return ResourceFactory.createProperty(name); }


    // Classes based on https://www.w3.org/TR/r2rml/#vocabulary

    public static final Resource TriplesMap = resource(Rml2Terms.TriplesMap);

    public static final Resource LogicalTable = resource(Rml2Terms.LogicalTable);
    public static final Resource R2RMLView = resource(Rml2Terms.R2RMLView);
    public static final Resource BaseTableOrView = resource(Rml2Terms.BaseTableOrView);

    public static final Resource TermMap = resource(Rml2Terms.TermMap);
    public static final Resource GraphMap = resource(Rml2Terms.GraphMap);
    public static final Resource SubjectMap = resource(Rml2Terms.SubjectMap);
    public static final Resource PredicateMap = resource(Rml2Terms.PredicateMap);
    public static final Resource ObjectMap = resource(Rml2Terms.ObjectMap);

    public static final Resource PredicateObjectMap = resource(Rml2Terms.PredicateObjectMap);

    public static final Resource RefObjectMap = resource(Rml2Terms.RefObjectMap);

    public static final Resource Join = resource(Rml2Terms.Join);


    public static final Property child 				= property(Rml2Terms.child);
    public static final Property xclass 	        = property(Rml2Terms.xclass);
    public static final Property column 			= property(Rml2Terms.column);
    public static final Property datatype 			= property(Rml2Terms.datatype);
    public static final Property constant 			= property(Rml2Terms.constant);
    public static final Property graph 				= property(Rml2Terms.graph);
    public static final Property graphMap 			= property(Rml2Terms.graphMap);
    public static final Property inverseExpression 	= property(Rml2Terms.inverseExpression);
    public static final Property joinCondition 		= property(Rml2Terms.joinCondition);
    public static final Property language 			= property(Rml2Terms.language);
    public static final Property logicalTable 		= property(Rml2Terms.logicalTable);
    public static final Property object 			= property(Rml2Terms.object);
    public static final Property objectMap 			= property(Rml2Terms.objectMap);
    public static final Property parent 			= property(Rml2Terms.parent);
    public static final Property parentTriplesMap 	= property(Rml2Terms.parentTriplesMap);
    public static final Property predicate 			= property(Rml2Terms.predicate);
    public static final Property predicateMap 		= property(Rml2Terms.predicateMap);
    public static final Property predicateObjectMap	= property(Rml2Terms.predicateObjectMap);
    public static final Property sqlQuery 			= property(Rml2Terms.sqlQuery);
    public static final Property sqlVersion 		= property(Rml2Terms.sqlVersion);
    public static final Property subject 			= property(Rml2Terms.subject);
    public static final Property subjectMap 		= property(Rml2Terms.subjectMap);
    public static final Property tableName			= property(Rml2Terms.tableName);
    public static final Property template 			= property(Rml2Terms.template);
    public static final Property termType 			= property(Rml2Terms.termType);
    public static final Resource BlankNode			= resource(Rml2Terms.BlankNode);

    // Other Terms
    public static final Resource defaultGraph 		= resource(Rml2Terms.defaultGraph);
    public static final Resource SQL2008 			= resource(Rml2Terms.SQL2008);
    public static final Resource IRI 				= resource(Rml2Terms.IRI);

    public static final Resource Literal 			= resource(Rml2Terms.Literal);

    // RML2 specific terms

    public static final Property logicalSource        = property(Rml2Terms.logicalSource);
    public static final Property reference            = property(Rml2Terms.reference);

    public static final Property source               = property(Rml2Terms.source);
    public static final Property referenceFormulation = property(Rml2Terms.referenceFormulation);
    public static final Property iterator             = property(Rml2Terms.iterator);
}
