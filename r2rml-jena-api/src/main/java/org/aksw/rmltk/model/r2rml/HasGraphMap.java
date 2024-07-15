package org.aksw.rmltk.model.r2rml;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.r2rml.common.vocab.R2rmlTerms;
import org.aksw.rmltk.model.backbone.common.IHasGraphMap;
import org.apache.jena.rdf.model.Resource;

/**
 * Interface common to SubjectMap and PredicateObjectMap
 *
 * @author raven
 *
 */
@ResourceView
public interface HasGraphMap
    extends IHasGraphMap //, IGraphMap
{
    @Iri(R2rmlTerms.graphMap)
    @Override Set<GraphMap> getGraphMaps();


    @Iri(R2rmlTerms.graph)
    @Override Set<Resource> getGraphs();

    @Override
    default GraphMap addNewGraphMap() {
        GraphMap result = getModel().createResource().as(GraphMap.class);
        getGraphMaps().add(result);
        return result;
    }
}
