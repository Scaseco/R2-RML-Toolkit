package org.aksw.r2rmlx.domain.api;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.r2rml.jena.domain.api.TermMap;
import org.aksw.r2rmlx.common.vocab.R2RMLXStrings;

public interface TermMapX
	extends TermMap
{
	@Iri(R2RMLXStrings.langColumn)
	String getLangColumn();
	TermMapX setLangColumn(String langColumn);
}
