package org.aksw.rml.model;

import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface FunctionMap
    extends Resource
{
    TriplesMap getFunctionValue();
    FunctionMap setFunctionValue(Resource functionValue);
}
