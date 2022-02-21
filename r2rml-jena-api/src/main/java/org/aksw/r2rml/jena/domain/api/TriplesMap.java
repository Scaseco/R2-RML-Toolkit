package org.aksw.r2rml.jena.domain.api;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.r2rml.common.vocab.R2rmlTerms;
import org.apache.jena.rdf.model.Resource;

public interface TriplesMap
    extends MappingComponent
{
    @Iri(R2rmlTerms.subject)
    Resource getSubject();
    TriplesMap setSubject(Resource subject);

    @Iri(R2rmlTerms.subject)
    @IriType
    String getSubjectIri();
    TriplesMap setSubjectIri(String subjectIri);

    @Iri(R2rmlTerms.subjectMap)
    SubjectMap getSubjectMap();
    TriplesMap setSubjectMap(SubjectMap subjectMap);

    @Iri(R2rmlTerms.predicateObjectMap)
    Set<PredicateObjectMap> getPredicateObjectMaps();

    @Iri(R2rmlTerms.logicalTable)
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
