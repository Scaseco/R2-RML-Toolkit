package org.aksw.r2rml.jena.domain.api;

import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.r2rml.common.vocab.R2RMLStrings;

public interface TriplesMap
	extends MappingComponent
{
	@Iri(R2RMLStrings.subjectMap)
	SubjectMap getSubjectMap();
	TriplesMap setSubjectMap(SubjectMap subjectMap);

	@Iri(R2RMLStrings.predicateObjectMap)
	Set<PredicateObjectMap> getPredicateObjectMaps();

	@Iri(R2RMLStrings.logicalTable)
	LogicalTable getLogicalTable();
	TriplesMap setLogicalTable(LogicalTable logicalTable);
	
	/** Get an existing subject map or allocate a new blank node for it */
	default SubjectMap getOrSetSubjectMap() {
		SubjectMap result = getSubjectMap();
		
		if (result == null) {
			result = getModel().createResource().as(SubjectMap.class);
			setSubjectMap(result);
		}
		
		return result;
	}
	
	/**
	 * Allocate a fresh predicate object map and add it to this triples map.
	 * 
	 */
	default PredicateObjectMap addNewPredicateObjectMap() {
		PredicateObjectMap result = getModel().createResource().as(PredicateObjectMap.class);
		getPredicateObjectMaps().add(result);
		return result;
	}

	/**
	 * Get an existing subject map or allocate a new blank node for it
	 */
	default LogicalTable getOrSetLogicalTable() {
		LogicalTable result = getLogicalTable();
		
		if (result == null) {
			result = getModel().createResource().as(LogicalTable.class);
			setLogicalTable(result);
		}
		
		return result;
	}
}
