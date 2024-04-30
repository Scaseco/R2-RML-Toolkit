package org.aksw.rml.jena.common;


import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * A TermMap with all attributes according to the R2RML specification.
 *
 * @author Claus Stadler
 *
 */
public interface TermMap
    extends TermSpec
{
    Resource getTermType();
    TermMap setTermType(Resource termType);

    String getColumn();
    TermMap setColumn(String columnName);

    String getLanguage();
    TermMap setLanguage(String language);

    Resource getDatatype();
    TermMap setDatatype(Resource datatype);

    RDFNode getConstant();
    TermMap setConstant(RDFNode constant);

    String getTemplate();
    TermMap setTemplate(String template);

    String getInverseExpression();
    TermMap setInverseExpression(String inverseExpression);

    /**
     *
     * https://www.w3.org/TR/r2rml/#dfn-column-valued-term-map
     *
     */
    boolean isColumnValued();

    /**
     * Predicate to test whether this term map qualifies according to
     *
     * https://www.w3.org/TR/r2rml/#dfn-template-valued-term-map
     *
     * @return
     */
    boolean isTemplateValued();
}
