package org.aksw.fno.model;

import java.util.List;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface Function
    extends Resource
{
    @Iri(FnoTerms.expects)
    List<Param> getExpects();
    Function setExpects(List<Param> params);

    @Iri(FnoTerms.returns)
    List<Param> getReturns();
    Function setReturns(List<Param> outputs);
}
