package org.aksw.r2rml.jena.domain.api;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.r2rml.common.vocab.R2RMLStrings;

public interface BaseTableOrView
	extends LogicalTable
{
	@Iri(R2RMLStrings.tableName)
	String getTableName();
	BaseTableOrView setTableName(String tableName);
}
