package org.aksw.r2rml.jena.arq.domain.impl;

import org.aksw.r2rml.common.domain.api.PlainLogicalTable;

public class LogicalTableQueryString
	implements PlainLogicalTable
{
	protected String queryString;
	
	public LogicalTableQueryString(String queryString) {
		super();
		this.queryString = queryString;
	}

	@Override
	public String getTableName() {
		return null;
	}

	@Override
	public String getSqlQuery() {
		return queryString;
	}

	@Override
	public String toString() {
		return "LogicalTableQueryString [queryString=" + queryString + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((queryString == null) ? 0 : queryString.hashCode());
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
		LogicalTableQueryString other = (LogicalTableQueryString) obj;
		if (queryString == null) {
			if (other.queryString != null)
				return false;
		} else if (!queryString.equals(other.queryString))
			return false;
		return true;
	}
}
