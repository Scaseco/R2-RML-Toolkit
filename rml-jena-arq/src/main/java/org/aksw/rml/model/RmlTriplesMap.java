package org.aksw.rml.model;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.apache.jena.rdf.model.Resource;

/**
 * Rml's extended attribute of rr:TriplesMap
 */
@ResourceView
public interface RmlTriplesMap
    extends TriplesMap
{
    @Iri(RmlTerms.logicalSource)
    LogicalSource getLogicalSource();
    RmlTriplesMap setLogicalSource(Resource logicalSource);
}
