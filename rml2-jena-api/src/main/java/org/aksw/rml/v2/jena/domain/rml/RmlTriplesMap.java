package org.aksw.rml.v2.jena.domain.rml;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rml.v2.common.vocab.Rml2Terms;
import org.aksw.rml.v2.jena.domain.api.TriplesMap;
import org.apache.jena.rdf.model.Resource;

/**
 * Rml's extended attribute of rr:TriplesMap
 */
@ResourceView
public interface RmlTriplesMap
    extends TriplesMap
{
    @Iri(Rml2Terms.logicalSource)
    LogicalSource getLogicalSource();
    RmlTriplesMap setLogicalSource(Resource logicalSource);
}
