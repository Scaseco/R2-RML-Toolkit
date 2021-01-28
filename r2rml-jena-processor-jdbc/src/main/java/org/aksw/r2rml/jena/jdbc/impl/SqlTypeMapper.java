package org.aksw.r2rml.jena.jdbc.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.jena.datatypes.TypeMapper;

public class SqlTypeMapper {

	private static SqlTypeMapper INSTANCE = null;
	
	public static SqlTypeMapper getInstance() {
		if (INSTANCE == null) {
			synchronized (SqlTypeMapper.class) {
				if (INSTANCE == null) {
					INSTANCE = SqlTypeMapper.createDefault();
				}
			}
		}
		
		return INSTANCE;
	}
	
	protected Map<Integer, SqlDatatype> indexBySqlType = new LinkedHashMap<>();
	
	public SqlTypeMapper registerDatatype(SqlDatatype sqlDatatype) {
		int sqlType = sqlDatatype.getSqlType();
		indexBySqlType.put(sqlType, sqlDatatype);
		return this;
	}
	
	public SqlDatatype getTypeBySqlType(int sqlType) {
		return indexBySqlType.get(sqlType);
	}
		
	public static SqlTypeMapper createDefault() {
		TypeMapper typeMapper = TypeMapper.getInstance();
		Set<SqlDatatype> sqlDatatypes = NaturalMappings.getDefaultDatatypes(typeMapper);
		
		SqlTypeMapper result = new SqlTypeMapper();
		sqlDatatypes.forEach(result::registerDatatype);
		
		return result;
	}
}
