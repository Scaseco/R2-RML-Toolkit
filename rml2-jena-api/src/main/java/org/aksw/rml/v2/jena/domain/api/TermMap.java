package org.aksw.rml.v2.jena.domain.api;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rml.v2.common.vocab.Rml2Terms;
import org.aksw.rml2.vocab.jena.RML2;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * A TermMap with all attributes according to the R2RML specification.
 *
 * @author Claus Stadler
 *
 */
@ResourceView
public interface TermMap
    extends TermSpec
{
    @Iri(Rml2Terms.termType)
    Resource getTermType();
    TermMap setTermType(Resource termType);

    @Iri(Rml2Terms.column)
    String getColumn();
    TermMap setColumn(String columnName);

    @Iri(Rml2Terms.language)
    String getLanguage();
    TermMap setLanguage(String language);

    @Iri(Rml2Terms.datatype)
    Resource getDatatype();
    TermMap setDatatype(Resource datatype);

    @Iri(Rml2Terms.constant)
    RDFNode getConstant();
    TermMap setConstant(RDFNode constant);

    @Iri(Rml2Terms.template)
    String getTemplate();
    TermMap setTemplate(String template);

    @Iri(Rml2Terms.inverseExpression)
    String getInverseExpression();
    TermMap setInverseExpression(String inverseExpression);

    /**
     *
     * https://www.w3.org/TR/r2rml/#dfn-column-valued-term-map
     *
     */
    default boolean isColumnValued() {
        return hasProperty(RML2.column);
    }

    /**
     * Predicate to test whether this term map qualifies according to
     *
     * https://www.w3.org/TR/r2rml/#dfn-template-valued-term-map
     *
     * @return
     */
    default boolean isTemplateValued() {
        return hasProperty(RML2.template);
    }
}
