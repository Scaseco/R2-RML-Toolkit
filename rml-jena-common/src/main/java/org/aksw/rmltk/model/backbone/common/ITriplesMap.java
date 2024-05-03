package org.aksw.rmltk.model.backbone.common;

import java.util.Set;

import org.apache.jena.rdf.model.Resource;

public interface ITriplesMap
    extends IMappingComponent
{
    Resource getSubject();
    ITriplesMap setSubject(Resource subject);

    String getSubjectIri();
    ITriplesMap setSubjectIri(String subjectIri);

    ISubjectMap getSubjectMap();
    ITriplesMap setSubjectMap(ISubjectMap subjectMap);

    Set<? extends IPredicateObjectMap> getPredicateObjectMaps();

    /** Get an existing subject map or allocate a new blank node for it */
    ISubjectMap getOrSetSubjectMap();

    /**
     * Allocate a fresh predicate object map and add it to this triples map.
     *
     */
    IPredicateObjectMap addNewPredicateObjectMap();

    /**
     * Get an existing subject map or allocate a new blank node for it
     */
    IAbstractSource getOrSetAbstractSource();

    IAbstractSource getAbstractSource();
    ITriplesMap setAbstractSource(IAbstractSource abstractSource);
}
