package org.aksw.rmltk.model.r2rml;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.r2rml.common.vocab.R2rmlTerms;
import org.aksw.rmltk.model.backbone.r2rml.IBaseTableOrView;

public interface BaseTableOrView
    extends IBaseTableOrView //, LogicalTable
{
    @Iri(R2rmlTerms.tableName)
    @Override String getTableName();
    @Override BaseTableOrView setTableName(String tableName);
}
