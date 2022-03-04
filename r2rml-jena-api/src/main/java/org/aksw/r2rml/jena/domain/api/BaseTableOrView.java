package org.aksw.r2rml.jena.domain.api;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.r2rml.common.vocab.R2rmlTerms;

public interface BaseTableOrView
	extends LogicalTable
{
	@Iri(R2rmlTerms.tableName)
	String getTableName();
	BaseTableOrView setTableName(String tableName);
}
