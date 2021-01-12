package org.aksw.r2rml.jena.arq.domain.impl;

import org.aksw.r2rml.common.domain.api.PlainLogicalTable;

public class LogicalTableTableName
	implements PlainLogicalTable
{
	protected String tableName;
	
	public LogicalTableTableName(String tableName) {
		super();
		this.tableName = tableName;
	}
	
	@Override
	public String getTableName() {
		return tableName;
	}

	@Override
	public String getSqlQuery() {
		return null;
	}

	@Override
	public String toString() {
		return "LogicalTableTableName [tableName=" + tableName + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LogicalTableTableName other = (LogicalTableTableName) obj;
		if (tableName == null) {
			if (other.tableName != null)
				return false;
		} else if (!tableName.equals(other.tableName))
			return false;
		return true;
	}
}
