package org.aksw.r2rml.jena.jdbc.api;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.jena.graph.Node;

public interface RowToNode {
	Node map(ResultSet resultSet, int columnIdx) throws SQLException;
}