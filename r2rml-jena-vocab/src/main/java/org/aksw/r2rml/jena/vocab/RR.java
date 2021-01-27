package org.aksw.r2rml.jena.vocab;
/**
 * 
 */


import org.aksw.r2rml.common.vocab.R2rmlTerms;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * The R2RML vocabulary
 * 
 * @author Claus Stadler
 *
 */
public class RR {
	public static final String uri = R2rmlTerms.uri;

	public static String getURI() { return uri; }
	public static Resource resource(String name) { return ResourceFactory.createResource(name); }
	public static Property property(String name) { return ResourceFactory.createProperty(name); }
	
	
	// Classes based on https://www.w3.org/TR/r2rml/#vocabulary
	
	public static final Resource TriplesMap = resource(R2rmlTerms.TriplesMap);
	
	public static final Resource LogicalTable = resource(R2rmlTerms.LogicalTable);
	public static final Resource R2RMLView = resource(R2rmlTerms.R2RMLView);
	public static final Resource BaseTableOrView = resource(R2rmlTerms.BaseTableOrView);
	
	public static final Resource TermMap = resource(R2rmlTerms.TermMap);
	public static final Resource GraphMap = resource(R2rmlTerms.GraphMap);
	public static final Resource SubjectMap = resource(R2rmlTerms.SubjectMap);
	public static final Resource PredicateMap = resource(R2rmlTerms.PredicateMap);
	public static final Resource ObjectMap = resource(R2rmlTerms.ObjectMap);
	
	public static final Resource PredicateObjectMap = resource(R2rmlTerms.PredicateObjectMap);

	public static final Resource RefObjectMap = resource(R2rmlTerms.RefObjectMap);

	public static final Resource Join = resource(R2rmlTerms.Join);

	
	public static final Property child 				= property(R2rmlTerms.child);
	public static final Property xclass 	        = property(R2rmlTerms.xclass);
	public static final Property column 			= property(R2rmlTerms.column);
	public static final Property datatype 			= property(R2rmlTerms.datatype);
	public static final Property constant 			= property(R2rmlTerms.constant);
	public static final Property graph 				= property(R2rmlTerms.graph);
	public static final Property graphMap 			= property(R2rmlTerms.graphMap);
	public static final Property inverseExpression 	= property(R2rmlTerms.inverseExpression);
	public static final Property joinCondition 		= property(R2rmlTerms.joinCondition);
	public static final Property language 			= property(R2rmlTerms.language);
	public static final Property logicalTable 		= property(R2rmlTerms.logicalTable);
	public static final Property object 			= property(R2rmlTerms.object);
	public static final Property objectMap 			= property(R2rmlTerms.objectMap);
	public static final Property parent 			= property(R2rmlTerms.parent);
	public static final Property parentTriplesMap 	= property(R2rmlTerms.parentTriplesMap);
	public static final Property predicate 			= property(R2rmlTerms.predicate);
	public static final Property predicateMap 		= property(R2rmlTerms.predicateMap);
	public static final Property predicateObjectMap	= property(R2rmlTerms.predicateObjectMap);
	public static final Property sqlQuery 			= property(R2rmlTerms.sqlQuery);
	public static final Property sqlVersion 		= property(R2rmlTerms.sqlVersion);
	public static final Property subject 			= property(R2rmlTerms.subject);
	public static final Property subjectMap 		= property(R2rmlTerms.subjectMap);
	public static final Property tableName			= property(R2rmlTerms.tableName);
	public static final Property template 			= property(R2rmlTerms.template);
	public static final Property termType 			= property(R2rmlTerms.termType);
	public static final Resource BlankNode			= resource(R2rmlTerms.BlankNode);	
	
	// Other Terms
	public static final Resource defaultGraph 		= resource(R2rmlTerms.defaultGraph);
	public static final Resource SQL2008 			= resource(R2rmlTerms.SQL2008);
	public static final Resource IRI 				= resource(R2rmlTerms.IRI);

	public static final Resource Literal 			= resource(R2rmlTerms.Literal);
}