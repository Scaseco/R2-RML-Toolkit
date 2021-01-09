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
}
