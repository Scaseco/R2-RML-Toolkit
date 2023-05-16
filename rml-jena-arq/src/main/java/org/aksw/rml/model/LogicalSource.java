package org.aksw.rml.model;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface LogicalSource
    extends Resource
{
    @Iri(RmlTerms.source)
    RDFNode getSource();
    LogicalSource setSource(RDFNode source);

    default String getSourceAsString() {
        return getSource().asLiteral().getLexicalForm();
    }
    default Resource getSourceAsResource() {
        return getSource().asResource();
    }

    @Iri(RmlTerms.iterator)
    String getIterator();
    LogicalSource setIterator(String iterator);

    @Iri(RmlTerms.referenceFormulation)
    @IriType
    String getReferenceFormulationIri();
    LogicalSource setReferenceFormulationIri(String iri);

}
