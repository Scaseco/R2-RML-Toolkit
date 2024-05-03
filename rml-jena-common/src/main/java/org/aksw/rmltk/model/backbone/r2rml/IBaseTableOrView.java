package org.aksw.rmltk.model.backbone.r2rml;

public interface IBaseTableOrView
    extends ILogicalTableR2rml
{
    String getTableName();
    IBaseTableOrView setTableName(String tableName);
}
