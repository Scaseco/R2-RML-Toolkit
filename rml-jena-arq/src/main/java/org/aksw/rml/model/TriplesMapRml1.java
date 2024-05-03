package org.aksw.rml.model;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.r2rml.common.vocab.R2rmlTerms;
import org.aksw.rmltk.model.backbone.common.IAbstractSource;
import org.aksw.rmltk.model.backbone.common.ISubjectMap;
import org.aksw.rmltk.model.backbone.rml.ITriplesMapRml;
import org.aksw.rmltk.model.r2rml.SubjectMap;
import org.apache.jena.rdf.model.Resource;

/**
 * Rml's extended attribute of rr:TriplesMap
 */
@ResourceView
public interface TriplesMapRml1
    extends ITriplesMapRml
{
    @Iri(R2rmlTerms.subject)
    @Override Resource getSubject();
    @Override TriplesMapRml1 setSubject(Resource subject);

    @Iri(R2rmlTerms.subject)
    @IriType
    @Override String getSubjectIri();
    @Override  TriplesMapRml1 setSubjectIri(String subjectIri);

    @Iri(R2rmlTerms.subjectMap)
    @Override  SubjectMap getSubjectMap();
    @Override  TriplesMapRml1 setSubjectMap(ISubjectMap subjectMap);

    @Iri(R2rmlTerms.predicateObjectMap)
    @Override Set<PredicateObjectMapRml1> getPredicateObjectMaps();

    // @Iri(R2rmlTerms.logicalTable)
    @Iri(RmlTerms.logicalSource)
    @Override LogicalSourceRml1 getLogicalSource();
    @Override TriplesMapRml1 setLogicalSource(Resource logicalSource);

    /** Get an existing subject map or allocate a new blank node for it */
    @Override
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
    @Override
    default PredicateObjectMapRml1 addNewPredicateObjectMap() {
        PredicateObjectMapRml1 result = getModel().createResource().as(PredicateObjectMapRml1.class);
        getPredicateObjectMaps().add(result);
        return result;
    }

    /**
     * Get an existing subject map or allocate a new blank node for it
     */
    default LogicalSourceRml1 getOrSetLogicalSource() {
        LogicalSourceRml1 result = getLogicalSource();

        if (result == null) {
            result = getModel().createResource().as(LogicalSourceRml1.class);
            setLogicalSource(result);
        }

        return result;
    }

//    @Override
//    default IAbstractSource getAbstractSource() {
//        return getLogicalTable();
//    }

    @Override
    default TriplesMapRml1 setAbstractSource(IAbstractSource abstractSource) {
        return setLogicalSource(abstractSource);
    }
//    @Iri(RmlTerms.logicalSource)
//    @Override LogicalSourceRml1 getLogicalSource();
//    @Override TriplesMapRml1 setLogicalSource(Resource logicalSource);

//    @Override
//    default IAbstractSource getAbstractSource() {
//        return getLogicalSource();
//    }
//
//    @Override
//    default TriplesMap setAbstractSource(IAbstractSource abstractSource) {
//        return setLogicalSource(abstractSource);
//    }
}
