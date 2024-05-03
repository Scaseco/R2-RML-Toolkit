package org.aksw.rml.v2.jena.domain.api;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.rml.v2.common.vocab.Rml2Terms;
import org.aksw.rmltk.model.backbone.r2rml.IBaseTableOrView;

public interface BaseTableOrView
    extends IBaseTableOrView, LogicalTable
{
    @Iri(Rml2Terms.tableName)
    @Override String getTableName();
    @Override  BaseTableOrView setTableName(String tableName);
}
