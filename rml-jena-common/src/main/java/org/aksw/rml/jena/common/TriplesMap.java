package org.aksw.rml.jena.common;


import java.util.Set;

import org.apache.jena.rdf.model.Resource;

public interface TriplesMap
    extends MappingComponent
{
    Resource getSubject();
    TriplesMap setSubject(Resource subject);

    String getSubjectIri();
    TriplesMap setSubjectIri(String subjectIri);

    SubjectMap getSubjectMap();
    TriplesMap setSubjectMap(SubjectMap subjectMap);

    Set<PredicateObjectMap> getPredicateObjectMaps();

    LogicalTable getLogicalTable();
    TriplesMap setLogicalTable(LogicalTable logicalTable);

    /** Get an existing subject map or allocate a new blank node for it */
    SubjectMap getOrSetSubjectMap();

    /**
     * Allocate a fresh predicate object map and add it to this triples map.
     *
     */
    PredicateObjectMap addNewPredicateObjectMap();

    /**
     * Get an existing subject map or allocate a new blank node for it
     */
    LogicalTable getOrSetLogicalTable();
}
