package org.aksw.rml.v2.jena.domain.api;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rml.v2.common.vocab.Rml2Terms;
import org.aksw.rml2.vocab.jena.RML2;
import org.aksw.rmltk.model.backbone.rml.ITermMapRml;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * A TermMap with all attributes according to the R2RML specification.
 *
 * @author Claus Stadler
 *
 */
@ResourceView
public interface TermMapRml2
    extends ITermMapRml, TermSpecRml2
{
    @Iri(Rml2Terms.termType)
    @Override Resource getTermType();
    @Override TermMapRml2 setTermType(Resource termType);

    @Iri(Rml2Terms.column)
    @Override String getColumn();
    @Override TermMapRml2 setColumn(String columnName);

    @Iri(Rml2Terms.language)
    @Override String getLanguage();
    @Override TermMapRml2 setLanguage(String language);

    @Iri(Rml2Terms.datatype)
    @Override Resource getDatatype();
    @Override TermMapRml2 setDatatype(Resource datatype);

    @Iri(Rml2Terms.constant)
    @Override RDFNode getConstant();
    @Override TermMapRml2 setConstant(RDFNode constant);

    @Iri(Rml2Terms.template)
    @Override String getTemplate();
    @Override TermMapRml2 setTemplate(String template);

    @Iri(Rml2Terms.inverseExpression)
    @Override String getInverseExpression();
    @Override TermMapRml2 setInverseExpression(String inverseExpression);

    @Iri(Rml2Terms.reference)
    @Override String getReference();
    @Override TermMapRml2 setReference(String reference);

    /**
     *
     * https://www.w3.org/TR/r2rml/#dfn-column-valued-term-map
     *
     */
    @Override default boolean isColumnValued() {
        return hasProperty(RML2.column);
    }

    /**
     * Predicate to test whether this term map qualifies according to
     *
     * https://www.w3.org/TR/r2rml/#dfn-template-valued-term-map
     *
     * @return
     */
    @Override default boolean isTemplateValued() {
        return hasProperty(RML2.template);
    }
}
