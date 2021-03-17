package org.aksw.r2rml.jena.jdbc.api;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.jena.sparql.engine.binding.Binding;

/**
 * Interface for mapping the current {@link ResultSet}'s row to a {@link Binding}
 * 
 * @author Claus Stadler
 */
@FunctionalInterface
public interface BindingMapper {
	Binding map(ResultSet rs) throws SQLException;
}