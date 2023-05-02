package org.aksw.fnml.model;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface FunctionMap
    extends Resource // extend TermMap?
{
    @Iri(FnmlTerms.functionValue)
    TriplesMap getFunctionValue();
    FunctionMap setFunctionValue(Resource functionValue);
}
