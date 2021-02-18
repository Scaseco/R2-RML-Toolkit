package org.aksw.r2rml.jena.jdbc.api;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.jena.graph.Node;

/**
 * Interface for obtaining the (typically natural) RDF term for the value in the
 * result set's current row at the given index.
 * See also information related to <a href="https://www.w3.org/TR/r2rml/#dfn-natural-rdf-datatype">https://www.w3.org/TR/r2rml/#dfn-natural-rdf-datatype</a>.
 * 
 * @author Claus Stadler
 *
 */
@FunctionalInterface
public interface NodeMapper {
	Node map(ResultSet resultSet, int columnIdx) throws SQLException;
}