package org.aksw.fnox.model;

import org.aksw.fno.model.Function;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface JavaFunction
    extends Function
{
    @Iri(LibTerms.providedBy)
    JavaMethodReference getProvidedBy();
    JavaFunction setProvidedBy(Resource providedBy);
}
