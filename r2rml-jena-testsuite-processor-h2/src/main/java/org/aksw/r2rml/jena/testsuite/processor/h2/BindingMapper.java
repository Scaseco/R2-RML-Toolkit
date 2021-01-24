package org.aksw.r2rml.jena.testsuite.processor.h2;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.jena.sparql.engine.binding.Binding;

public interface BindingMapper {
	Binding map(ResultSet rs) throws SQLException;
}