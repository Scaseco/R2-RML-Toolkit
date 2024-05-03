package org.aksw.fnml.model;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rml.model.TriplesMapRml1;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface FunctionMap
    extends Resource // extend TermMap?
{
    @Iri(FnmlTerms.functionValue)
    TriplesMapRml1 getFunctionValue();
    FunctionMap setFunctionValue(Resource functionValue);
}
