package org.aksw.r2rml.jena.jdbc.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import org.aksw.r2rml.jena.jdbc.api.NodeMapper;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

/**
 * Generic mapper that retrieves each column's value as a
 * {@link Object} and then request an appropriate {@link RDFDatatype}
 * from Jena's {@link TypeMapper}.
 * 
 * The advantage of this approach is the robustness and independence of
 * SQL type information.
 * 
 * The disadvantage is the performance impact by going through the type
 *  mapper on every invocation of map.
 * 
 * 
 * @author Claus Stadler
 *
 */
public class RowToNodeViaTypeManager
	implements NodeMapper {

	protected TypeMapper typeMapper;
	
	public RowToNodeViaTypeManager() {
		this(TypeMapper.getInstance());
	}
	
	public RowToNodeViaTypeManager(TypeMapper typeMapper) {
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

// Canonicalize?
//			Object canonicalValue = dtype.cannonicalise(o);
//			RDFDatatype canonicaldtype = typeMapper.getTypeByValue(canonicalValue);
//			
//			Objects.requireNonNull(canonicaldtype, "Datatype became null after canonicalization from " + dtype + " for value " + o + " -> " + canonicalValue);			
//			result = NodeFactory.createLiteralByValue(canonicalValue, canonicaldtype);
		}
		
		return result;
	}
	
}