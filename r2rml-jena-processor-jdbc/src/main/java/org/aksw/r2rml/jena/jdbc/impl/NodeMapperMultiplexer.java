package org.aksw.r2rml.jena.jdbc.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.aksw.r2rml.jena.jdbc.api.NodeMapper;
import org.apache.jena.graph.Node;

public class NodeMapperMultiplexer
	implements NodeMapper
{
	protected NodeMapper[] delegates;
	
	public NodeMapperMultiplexer(NodeMapper[] delegates) {
		super();
		this.delegates = delegates;
	}

	@Override
	public Node map(ResultSet resultSet, int columnIdx) throws SQLException {
		NodeMapper target = delegates[columnIdx];
		Node result = target.map(resultSet, columnIdx);
		return result;
	}
}
