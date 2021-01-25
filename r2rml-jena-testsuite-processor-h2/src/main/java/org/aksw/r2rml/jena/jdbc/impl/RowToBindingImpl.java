package org.aksw.r2rml.jena.jdbc.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.aksw.r2rml.jena.jdbc.api.RowToBinding;
import org.aksw.r2rml.jena.jdbc.api.RowToNode;
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
	implements RowToBinding
{
	// The used column indices
	protected int[] colIdxs;
	
	// The corresponding SPARQL variables (colIdxs.length == vars.length)
	protected Var[] vars;
	
	// The mapper that creates the variable's value
	protected RowToNode nodeMapper;
	
	public RowToBindingImpl(int[] colIdxs, Var[] vars, RowToNode nodeMapper) {
		super();
		this.colIdxs = colIdxs;
		this.vars = vars;
		this.nodeMapper = nodeMapper;
	}

	public Binding map(ResultSet resultSet) throws SQLException {
		BindingMap result = BindingFactory.create();
		
		for (int i = 0; i < colIdxs.length; ++i) {
			int colIdx = colIdxs[i];
			Node node = nodeMapper.map(resultSet, colIdx);

			if (node != null) {
				Var var = vars[i];
				result.add(var, node);
			}
		}
		
		return result;
	}
}