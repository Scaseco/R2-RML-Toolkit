package org.aksw.rmltk.model.r2rml;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.r2rml.common.vocab.R2rmlTerms;
import org.aksw.rmltk.model.backbone.common.IAbstractSource;
import org.aksw.rmltk.model.backbone.common.ISubjectMap;
import org.aksw.rmltk.model.backbone.common.ITriplesMap;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface TriplesMap
    extends ITriplesMap, MappingComponent
{
    @Iri(R2rmlTerms.subject)
    @Override Resource getSubject();
    @Override TriplesMap setSubject(Resource subject);

    @Iri(R2rmlTerms.subject)
    @IriType
    @Override String getSubjectIri();
    @Override  TriplesMap setSubjectIri(String subjectIri);

    @Iri(R2rmlTerms.subjectMap)
    @Override  SubjectMap getSubjectMap();
    @Override  TriplesMap setSubjectMap(ISubjectMap subjectMap);

    @Iri(R2rmlTerms.predicateObjectMap)
    @Override Set<PredicateObjectMap> getPredicateObjectMaps();

    @Iri(R2rmlTerms.logicalTable)
    LogicalTable getLogicalTable();
    TriplesMap setLogicalTable(IAbstractSource logicalTable);

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

    @Override
    default IAbstractSource getOrSetAbstractSource() {
        return getOrSetLogicalTable();
    }

    @Override
    default IAbstractSource getAbstractSource() {
        return getLogicalTable();
    }

    @Override
    default TriplesMap setAbstractSource(IAbstractSource abstractSource) {
        return setLogicalTable(abstractSource);
    }
}
