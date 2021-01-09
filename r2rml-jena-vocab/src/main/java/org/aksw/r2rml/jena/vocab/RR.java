package org.aksw.r2rml.jena.vocab;
/**
 * 
 */


import org.aksw.r2rml.common.vocab.R2RMLStrings;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * The R2RML vocabulary
 * 
 * @author sherif
 *
 */
public class RR {
	public static final String rr = R2RMLStrings.uri;

	public static Resource resource(String name) {
		Resource result = ResourceFactory.createResource(rr + name);
		return result;
	}

	public static Property property(String name) {
		Property result = ResourceFactory.createProperty(rr + name);
		return result;
	}
	
	public static String getURI() { return rr; }
	
	// Classes based on https://www.w3.org/TR/r2rml/#vocabulary
	
	public static final Resource TriplesMap = resource(R2RMLStrings.TriplesMap);
	
	public static final Resource LogicalTable = resource(R2RMLStrings.LogicalTable);
	public static final Resource R2RMLView = resource(R2RMLStrings.R2RMLView);
	public static final Resource BaseTableOrView = resource(R2RMLStrings.BaseTableOrView);
	
	public static final Resource TermMap = resource(R2RMLStrings.TermMap);
	public static final Resource GraphMap = resource(R2RMLStrings.GraphMap);
	public static final Resource SubjectMap = resource(R2RMLStrings.SubjectMap);
	public static final Resource PredicateMap = resource(R2RMLStrings.PredicateMap);
	public static final Resource ObjectMap = resource(R2RMLStrings.ObjectMap);
	
	public static final Resource PredicateObjectMap = resource(R2RMLStrings.PredicateObjectMap);

	public static final Resource RefObjectMap = resource(R2RMLStrings.RefObjectMap);

	public static final Resource Join = resource(R2RMLStrings.Join);

	
	public static final Property child 				= property(R2RMLStrings.child);
	public static final Property xclass 	        = property(R2RMLStrings.xclass);
	public static final Property column 			= property(R2RMLStrings.column);
	public static final Property datatype 			= property(R2RMLStrings.datatype);
	public static final Property constant 			= property(R2RMLStrings.constant);
	public static final Property graph 				= property(R2RMLStrings.graph);
	public static final Property graphMap 			= property(R2RMLStrings.graphMap);
	public static final Property inverseExpression 	= property(R2RMLStrings.inverseExpression);
	public static final Property joinCondition 		= property(R2RMLStrings.joinCondition);
	public static final Property language 			= property(R2RMLStrings.language);
	public static final Property logicalTable 		= property(R2RMLStrings.logicalTable);
	public static final Property object 			= property(R2RMLStrings.object);
	public static final Property objectMap 			= property(R2RMLStrings.objectMap);
	public static final Property parent 			= property(R2RMLStrings.parent);
	public static final Property parentTriplesMap 	= property(R2RMLStrings.parentTriplesMap);
	public static final Property predicate 			= property(R2RMLStrings.predicate);
	public static final Property predicateMap 		= property(R2RMLStrings.predicateMap);
	public static final Property predicateObjectMap	= property(R2RMLStrings.predicateObjectMap);
	public static final Property sqlQuery 			= property(R2RMLStrings.sqlQuery);
	public static final Property sqlVersion 		= property(R2RMLStrings.sqlVersion);
	public static final Property subject 			= property(R2RMLStrings.subject);
	public static final Property subjectMap 		= property(R2RMLStrings.subjectMap);
	public static final Property tableName			= property(R2RMLStrings.tableName);
	public static final Property template 			= property(R2RMLStrings.template);
	public static final Property termType 			= property(R2RMLStrings.termType);
	public static final Property BlankNode			= property(R2RMLStrings.BlankNode);	
	
	// Other Terms
	public static final Property defaultGraph 		= property(R2RMLStrings.defaultGraph);
	public static final Property SQL2008 			= property(R2RMLStrings.SQL2008);
	public static final Property IRI 				= property(R2RMLStrings.IRI);

	public static final Property Literal 			= property(R2RMLStrings.Literal);
}