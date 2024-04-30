package org.aksw.rml.v2.jena.domain.rml;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rml.v2.common.vocab.Rml2Terms;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface LogicalSource
    extends Resource
{
    @Iri(Rml2Terms.source)
    RDFNode getSource();
    LogicalSource setSource(RDFNode source);

    default String getSourceAsString() {
        return getSource().asLiteral().getLexicalForm();
    }
    default Resource getSourceAsResource() {
        return getSource().asResource();
    }

    @Iri(Rml2Terms.iterator)
    String getIterator();
    LogicalSource setIterator(String iterator);

    @Iri(Rml2Terms.referenceFormulation)
    @IriType
    String getReferenceFormulationIri();
    LogicalSource setReferenceFormulationIri(String iri);

}
