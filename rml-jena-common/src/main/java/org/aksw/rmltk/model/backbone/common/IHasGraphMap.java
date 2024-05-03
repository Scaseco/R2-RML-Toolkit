package org.aksw.rmltk.model.backbone.common;

import java.util.Set;

import org.apache.jena.rdf.model.Resource;

/**
 * Interface common to SubjectMap and PredicateObjectMap
 *
 * @author raven
 *
 */
public interface IHasGraphMap
    extends Resource
{
    Set<? extends IGraphMap> getGraphMaps();
    Set<Resource> getGraphs();

    IGraphMap addNewGraphMap();
}
