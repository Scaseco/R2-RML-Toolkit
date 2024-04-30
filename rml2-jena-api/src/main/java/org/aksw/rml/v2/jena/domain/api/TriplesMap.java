package org.aksw.rml.v2.jena.domain.api;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.rml.v2.common.vocab.Rml2Terms;
import org.apache.jena.rdf.model.Resource;

public interface TriplesMap
    extends MappingComponent
{
    @Iri(Rml2Terms.subject)
    Resource getSubject();
    TriplesMap setSubject(Resource subject);

    @Iri(Rml2Terms.subject)
    @IriType
    String getSubjectIri();
    TriplesMap setSubjectIri(String subjectIri);

    @Iri(Rml2Terms.subjectMap)
    SubjectMap getSubjectMap();
    TriplesMap setSubjectMap(SubjectMap subjectMap);

    @Iri(Rml2Terms.predicateObjectMap)
    Set<PredicateObjectMap> getPredicateObjectMaps();

    @Iri(Rml2Terms.logicalTable)
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
