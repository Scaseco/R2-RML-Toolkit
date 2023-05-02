package org.aksw.rml.model;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface LogicalSource
    extends Resource
{
    @Iri(RmlTerms.source)
    String getSource();
    LogicalSource setSource(String source);

    @Iri(RmlTerms.iterator)
    String getIterator();
    LogicalSource setIterator(String iterator);

    @Iri(RmlTerms.referenceFormulation)
    @IriType
    String getReferenceFormulationIri();
    LogicalSource setReferenceFormulationIri(String iri);
}
