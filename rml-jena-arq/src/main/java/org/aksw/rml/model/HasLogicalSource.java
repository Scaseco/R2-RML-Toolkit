package org.aksw.rml.model;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

/** Rml's extended attribute of rr:TriplesMap */
@ResourceView
public interface HasLogicalSource
    extends Resource
{
    @Iri(RmlTerms.logicalSource)
    LogicalSource getLogicalSource();
    HasLogicalSource setLogicalSource(Resource logicalSource);
}
