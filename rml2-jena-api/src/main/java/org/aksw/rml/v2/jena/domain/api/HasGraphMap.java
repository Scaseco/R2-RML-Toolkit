package org.aksw.rml.v2.jena.domain.api;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rml.v2.common.vocab.Rml2Terms;
import org.apache.jena.rdf.model.Resource;

/**
 * Interface common to SubjectMap and PredicateObjectMap
 *
 * @author raven
 *
 */
@ResourceView
public interface HasGraphMap
    extends Resource
{
    @Iri(Rml2Terms.graphMap)
    Set<GraphMap> getGraphMaps();


    @Iri(Rml2Terms.graph)
    Set<Resource> getGraphs();

    default GraphMap addNewGraphMap() {
        GraphMap result = getModel().createResource().as(GraphMap.class);
        getGraphMaps().add(result);
        return result;
    }
}
