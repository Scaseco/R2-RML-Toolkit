package org.aksw.rml.v2.jena.domain.api;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.rml.v2.common.vocab.Rml2Terms;

public interface BaseTableOrView
    extends LogicalTable
{
    @Iri(Rml2Terms.tableName)
    String getTableName();
    BaseTableOrView setTableName(String tableName);
}
