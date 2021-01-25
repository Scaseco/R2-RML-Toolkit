package org.aksw.r2rml.jena.jdbc.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import org.aksw.r2rml.jena.jdbc.api.NodeMapper;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public class NodeMapperViaSqlDatatype
	implements NodeMapper
{
	protected SqlDatatype sqlDatatype;
	
	public NodeMapperViaSqlDatatype(SqlDatatype sqlDatatype) {
		super();
		Objects.requireNonNull(sqlDatatype);
		this.sqlDatatype = sqlDatatype;
	}

	@Override
	public Node map(ResultSet resultSet, int columnIdx) throws SQLException {
		Object o = resultSet.getObject(columnIdx);
		
		Node result;
		if (o == null) {
			result = null;
		} else {
			RDFDatatype dtype = sqlDatatype.getRdfDatatype();
			result = NodeFactory.createLiteralByValue(o, dtype);
		}
		
		return result;
	}

}
