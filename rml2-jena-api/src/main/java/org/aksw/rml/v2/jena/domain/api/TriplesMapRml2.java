package org.aksw.rml.v2.jena.domain.api;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.rml.v2.common.vocab.Rml2Terms;
import org.aksw.rmltk.model.backbone.common.ISubjectMap;
import org.aksw.rmltk.model.backbone.rml.ITriplesMapRml;
import org.apache.jena.rdf.model.Resource;



public interface TriplesMapRml2
    extends ITriplesMapRml, MappingComponentRml2
{
    @Iri(Rml2Terms.subject)
    @Override Resource getSubject();
    @Override TriplesMapRml2 setSubject(Resource subject);

    @Iri(Rml2Terms.subject)
    @IriType
    @Override String getSubjectIri();
    @Override TriplesMapRml2 setSubjectIri(String subjectIri);

    @Iri(Rml2Terms.subjectMap)
    @Override SubjectMapRml2 getSubjectMap();
    @Override TriplesMapRml2 setSubjectMap(ISubjectMap subjectMap);

    @Iri(Rml2Terms.predicateObjectMap)
    @Override Set<PredicateObjectMapRml2> getPredicateObjectMaps();

    @Iri(Rml2Terms.logicalSource)
    @Override LogicalSourceRml2 getLogicalSource();
    @Override TriplesMapRml2 setLogicalSource(Resource logicalSource);

    /** Get an existing subject map or allocate a new blank node for it */
    @Override
    default SubjectMapRml2 getOrSetSubjectMap() {
        SubjectMapRml2 result = getSubjectMap();

        if (result == null) {
            result = getModel().createResource().as(SubjectMapRml2.class);
            setSubjectMap(result);
        }

        return result;
    }

    /**
     * Allocate a fresh predicate object map and add it to this triples map.
     *
     */
    @Override
    default PredicateObjectMapRml2 addNewPredicateObjectMap() {
        PredicateObjectMapRml2 result = getModel().createResource().as(PredicateObjectMapRml2.class);
        getPredicateObjectMaps().add(result);
        return result;
    }

    /**
     * Get an existing subject map or allocate a new blank node for it
     */
//    @Override
//    default LogicalTable getOrSetLogicalTable() {
//        LogicalTable result = getLogicalTable();
//
//        if (result == null) {
//            result = getModel().createResource().as(LogicalTable.class);
//            setLogicalTable(result);
//        }
//
//        return result;
//    }
}
