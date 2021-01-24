package org.aksw.r2rml.jena.testsuite.processor.h2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public class NodeMapperViaTypeManager
	implements NodeMapper {

	protected TypeMapper typeMapper;
	
	public NodeMapperViaTypeManager() {
		this(TypeMapper.getInstance());
	}
	
	public NodeMapperViaTypeManager(TypeMapper typeMapper) {
		super();
		this.typeMapper = typeMapper;
	}



	@Override
	public Node map(ResultSet resultSet, int columnIdx) throws SQLException {
		Object o = resultSet.getObject(columnIdx);
		
		Node result;
		
		if (o == null) {
			result = null;
		} else {
			RDFDatatype dtype = typeMapper.getTypeByValue(o);
			Objects.requireNonNull(dtype, "Could not obtain RDFDatatype for " + o + "of type " + o.getClass());
			result = NodeFactory.createLiteralByValue(o, dtype);
		}
		
		return result;
	}
	
}