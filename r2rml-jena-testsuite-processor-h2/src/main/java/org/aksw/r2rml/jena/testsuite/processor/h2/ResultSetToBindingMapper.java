package org.aksw.r2rml.jena.testsuite.processor.h2;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;

public class ResultSetToBindingMapper
	implements BindingMapper
{
	protected int[] colIdxs;
	protected Var[] vars;
	protected NodeMapper nodeMapper;
	
	public ResultSetToBindingMapper(int[] colIdxs, Var[] vars, NodeMapper nodeMapper) {
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