package org.aksw.r2rml.jena.jdbc.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Function;

import org.aksw.r2rml.jena.jdbc.api.NodeMapper;
import org.apache.jena.graph.Node;

public class NodeMapperViaSqlDatatype
	implements NodeMapper
{
	protected SqlDatatype sqlDatatype;
	protected Function<Object, Node> cachedNodeMapper;
	
	public NodeMapperViaSqlDatatype(SqlDatatype sqlDatatype) {
		super();
		Objects.requireNonNull(sqlDatatype);
		this.sqlDatatype = sqlDatatype;
		this.cachedNodeMapper = sqlDatatype.getNodeMapper();
	}

	@Override
	public Node map(ResultSet resultSet, int columnIdx) throws SQLException {
		Object o = resultSet.getObject(columnIdx);
		
		Node result;
		if (o == null) {
			result = null;
		} else {
//			if (dtype.getURI().equals(XSD.dateTime.getURI())) {
//				System.out.println("here");
//			}
			result = cachedNodeMapper.apply(o);
			// result = NodeFactory.createLiteralByValue(o, dtype);
		}
		
		return result;
	}

}
