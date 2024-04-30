package org.aksw.rml.jena.common;

import java.util.Set;

import org.apache.jena.rdf.model.Resource;

/**
 * Interface common to SubjectMap and PredicateObjectMap
 *
 * @author raven
 *
 */
public interface HasGraphMap
    extends Resource
{
    Set<GraphMap> getGraphMaps();
    Set<Resource> getGraphs();

    GraphMap addNewGraphMap();
}
