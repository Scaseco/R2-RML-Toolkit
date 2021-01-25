package org.aksw.r2rml.jena.jdbc.api;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.jena.graph.Node;

@FunctionalInterface
public interface NodeMapper {
	Node map(ResultSet resultSet, int columnIdx) throws SQLException;
}