package org.aksw.r2rml.jena.domain.api;

import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.r2rml.jena.vocab.RR;


/**
 * Common base class for {@link ObjectMap} and {@link RefObjectMap}.
 * R2RML implicitly specifies the range of rr:objectMap to be the union of
 * rr:ObjectMap and rr:RefObjectMap however it does not give this union a name.
 * 
 * @author raven
 *
 */
@ResourceView
public interface ObjectMapType
	extends MappingComponent
{
	/**
	 * R2RML specifies that the condition for an entity to qualify as an "ref object map" is
	 * "Having an rr:parentTriplesMap property"
	 * 
	 * @return
	 */
	default boolean qualifiesAsRefObjectMap() {
		return hasProperty(RR.parentTriplesMap);
	}
	
	/**
	 * Obtain a RefObjectMap view of this resource.
	 * Calling this method does NOT require {@link #qualifiesAsRefObjectMap()} to yield true.
	 * 
	 * @return
	 */
	default RefObjectMap asRefObjectMap() {
		return as(RefObjectMap.class);
	}

	/**
	 * R2RML specifies that the condition for an entity to qualify as a "term map" is
	 * "Having exactly one of rr:constant, rr:column, rr:template"
	 * 
	 * @return
	 */
	default boolean qualifiesAsTermMap() {
		return hasProperty(RR.constant) || hasProperty(RR.column) || hasProperty(RR.template);
	}

	/**
	 * Obtain an ObjectMap view of this resource.
	 * Calling this method does NOT require {@link #qualifiesAsObjectMap()} to yield true.
	 * 
	 * @return
	 */
	default ObjectMap asTermMap() {
		return as(ObjectMap.class);
	}
}
