package org.aksw.rml.jena.common;

public interface BaseTableOrView
    extends LogicalTable
{
    String getTableName();
    BaseTableOrView setTableName(String tableName);
}
