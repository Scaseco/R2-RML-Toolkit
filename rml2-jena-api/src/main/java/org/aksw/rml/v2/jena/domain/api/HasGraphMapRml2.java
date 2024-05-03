package org.aksw.rml.v2.jena.domain.api;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rml.v2.common.vocab.Rml2Terms;
import org.aksw.rmltk.model.backbone.common.IHasGraphMap;
import org.apache.jena.rdf.model.Resource;

/**
 * Interface common to SubjectMap and PredicateObjectMap
 *
 * @author raven
 *
 */
@ResourceView
public interface HasGraphMapRml2
    extends IHasGraphMap
{
    @Iri(Rml2Terms.graphMap)
    @Override Set<GraphMapRml2> getGraphMaps();


    @Iri(Rml2Terms.graph)
    @Override Set<Resource> getGraphs();

    @Override
    default GraphMapRml2 addNewGraphMap() {
        GraphMapRml2 result = getModel().createResource().as(GraphMapRml2.class);
        getGraphMaps().add(result);
        return result;
    }
}
