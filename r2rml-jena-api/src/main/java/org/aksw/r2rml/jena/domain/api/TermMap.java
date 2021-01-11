package org.aksw.r2rml.jena.domain.api;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.r2rml.common.vocab.R2RMLStrings;
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
	extends MappingComponent
{
	@Iri(R2RMLStrings.termType)
	Resource getTermType();
	TermMap setTermType(Resource termType);

	@Iri(R2RMLStrings.column)
	String getColumn();
	TermMap setColumn(String columnName);
	
	@Iri(R2RMLStrings.language)
	String getLanguage();
	TermMap setLanguage(String language);
	
	@Iri(R2RMLStrings.datatype)
	Resource getDatatype();
	TermMap setDatatype(Resource datatype);
	
	@Iri(R2RMLStrings.constant)
	RDFNode getConstant();
	TermMap setConstant(RDFNode constant);

	@Iri(R2RMLStrings.template)
	String getTemplate();
	TermMap setTemplate(String template);
}
