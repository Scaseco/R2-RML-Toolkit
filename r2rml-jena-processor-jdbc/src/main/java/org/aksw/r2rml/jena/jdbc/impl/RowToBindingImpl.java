package org.aksw.r2rml.jena.jdbc.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.aksw.r2rml.jena.jdbc.api.BindingMapper;
import org.aksw.r2rml.jena.jdbc.api.NodeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;

/**
 * Default implementation of a RowToBinding by
 * delegating each used column to a RowToNode instance.
 * 
 * @author Claus Stadler
 *
 */
public class RowToBindingImpl
	implements BindingMapper
{
	// The used column indices
	protected int[] colIdxs;
	
	// The corresponding SPARQL variables (colIdxs.length == vars.length)
	protected Var[] vars;
	
	// Whether the var at the given index is nullable:
	// An exception is raised if a non-nullable variable is left unbound.
	protected boolean[] nullableVars;
	
	// The mapper that creates the variable's value
	protected NodeMapper nodeMapper;
	
	public RowToBindingImpl(
			int[] colIdxs,
			Var[] vars,
			boolean[] nullableVars,
			NodeMapper nodeMapper) {
		super();
		this.colIdxs = colIdxs;
		this.vars = vars;
		this.nullableVars = nullableVars;
		this.nodeMapper = nodeMapper;
	}

	public Binding map(ResultSet resultSet) throws SQLException {
		BindingMap result = BindingFactory.create();
		
		for (int i = 0; i < colIdxs.length; ++i) {
			int colIdx = colIdxs[i];
			Node node = nodeMapper.map(resultSet, colIdx);

			Var var = vars[i];
			if (node != null) {
				result.add(var, node);
			} else {
				boolean isNullable = nullableVars[i];
				if (!isNullable) {
					throw new RuntimeException("Attempted to leave non-nullable variable " + var + " unbound");
				}
			}
		}
		
		return result;
	}
}