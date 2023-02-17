package org.aksw.fno.model;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface Param
    extends Resource
{
    @Iri(FnoTerms.predicate)
    @IriType
    String getPredicateIri();
    Param setPredicate(String predicate);
}
