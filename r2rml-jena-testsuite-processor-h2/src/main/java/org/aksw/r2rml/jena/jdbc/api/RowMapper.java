package org.aksw.r2rml.jena.jdbc.api;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.jena.sparql.engine.binding.Binding;

@FunctionalInterface
public interface RowMapper {
	Binding map(ResultSet rs) throws SQLException;
}