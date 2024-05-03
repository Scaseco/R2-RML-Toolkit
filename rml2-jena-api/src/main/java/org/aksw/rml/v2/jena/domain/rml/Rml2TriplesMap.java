package org.aksw.rml.v2.jena.domain.rml;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rml.v2.common.vocab.Rml2Terms;
import org.aksw.rmltk.model.backbone.common.ITriplesMap;
import org.apache.jena.rdf.model.Resource;

/**
 * Rml's extended attribute of rr:TriplesMap
 */
@ResourceView
public interface Rml2TriplesMap
    extends ITriplesMap
{
    @Iri(Rml2Terms.logicalSource)
    Rml2LogicalSource getLogicalSource();
    Rml2TriplesMap setLogicalSource(Resource logicalSource);
}
