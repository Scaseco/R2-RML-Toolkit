package org.aksw.r2rml.jena.testsuite.processor.h2;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.jena.graph.Node;

public interface NodeMapper {
	Node map(ResultSet resultSet, int columnIdx) throws SQLException;
}