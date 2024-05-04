package org.aksw.rml.v2.jena.domain.api;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rml.v2.common.vocab.Rml2Terms;
import org.aksw.rmltk.model.backbone.rml.ILogicalSource;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface LogicalSourceRml2
    extends ILogicalSource
{
    @Iri(Rml2Terms.source)
    @Override RDFNode getSource();
    @Override LogicalSourceRml2 setSource(RDFNode source);

    @Override
    default String getSourceAsString() {
        return getSource().asLiteral().getLexicalForm();
    }

    @Override
    default Resource getSourceAsResource() {
        return getSource().asResource();
    }

    @Iri(Rml2Terms.iterator)
    @Override String getIterator();
    @Override LogicalSourceRml2 setIterator(String iterator);

    @Iri(Rml2Terms.referenceFormulation)
    @IriType
    @Override String getReferenceFormulationIri();
    @Override LogicalSourceRml2 setReferenceFormulationIri(String iri);
}
