package org.aksw.r2rml.jena.jdbc.util;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.aksw.r2rml.jena.jdbc.api.NodeMapper;
import org.aksw.r2rml.jena.jdbc.api.RowMapper;
import org.aksw.r2rml.jena.jdbc.impl.NaturalMappings;
import org.aksw.r2rml.jena.jdbc.impl.NodeMapperMultiplexer;
import org.aksw.r2rml.jena.jdbc.impl.RowToBindingImpl;
import org.aksw.r2rml.jena.jdbc.impl.SqlTypeMapper;
import org.apache.jena.ext.com.google.common.collect.HashBiMap;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.sparql.core.Var;


public class JdbcUtils {
	
	/**
	 * Create an array of node mappers whose size matches the number of columns + 1.
	 * The first entry is always null because SQL column indices start with 1.
	 * 
	 * Node mappers are only created for columns referred to by usedIdxs.
	 * Hence, the resulting array may have additional null entries.
	 * 
	 * 
	 * @param rsmd ResultSetMetadata
	 * @param usedIdxs The indices for which to create node mappers
	 * @param sqlTypeMapper SQL-to-RDF type mappings
	 * @return
	 * @throws SQLException
	 */
	public static NodeMapper createNodeMapper(ResultSetMetaData rsmd, int[] usedIdxs, SqlTypeMapper sqlTypeMapper) throws SQLException {
		
		int columnCount = rsmd.getColumnCount();
		NodeMapper[] targets = new NodeMapper[columnCount + 1];
		for (int i = 0; i < usedIdxs.length; ++i) {
			int colIdx = usedIdxs[i];			
			int sqlType = rsmd.getColumnType(colIdx);
		
			targets[colIdx] = NaturalMappings.createNodeMapper(sqlType, sqlTypeMapper);
		}
		return new NodeMapperMultiplexer(targets);
	}
	
	
	public static Map<Var, Integer> createVarMapping(ResultSetMetaData rsmd, Map<Var, String> usedVarToColumnName) throws SQLException {
		int columnCount = rsmd.getColumnCount();

		// Iterate the columns in the given order and map those columns
		// that have a variable in usedVars
		Set<String> availableColumns = new LinkedHashSet<>();
		Map<Var, Integer> colNameToIdx = new LinkedHashMap<>();

		Map<String, Var> columnNameToVar = HashBiMap.create(usedVarToColumnName).inverse();

		for (int i = 1; i <= columnCount; ++i) {
			String columnName = rsmd.getColumnName(i);			
			availableColumns.add(columnName);
			
			Var usedVar = columnNameToVar.get(columnName);
//			if (usedVars.contains(cv)) {
			if (usedVar != null) {
				colNameToIdx.put(usedVar, i);
			} else {
				// Secondary matching with ignore case
				Var secondaryMatch = usedVarToColumnName.entrySet().stream()
					.filter(e -> e.getValue().equalsIgnoreCase(columnName))
					.map(Entry::getKey)
					.findFirst()
					.orElse(null);
				
				if (secondaryMatch != null) {
					colNameToIdx.put(secondaryMatch, i);
				}
				
			}
		}
		
		Set<Var> invalidRefs = Sets.difference(usedVarToColumnName.keySet(), colNameToIdx.keySet());
		if (!invalidRefs.isEmpty()) {
			throw new RuntimeException("The following non-existent columns are referenced: "
					+ invalidRefs + "; available: " + availableColumns);
		}
		
		
		return colNameToIdx;
	}
	
	public static RowMapper createDefaultBindingMapper(
			ResultSetMetaData rsmd,
			Map<Var, String> usedVarToColumnName) throws SQLException {
		SqlTypeMapper sqlTypeMapper = SqlTypeMapper.getInstance();

		Map<Var, Integer> colNameToIdx = createVarMapping(rsmd, usedVarToColumnName);
		
		Function<int[], NodeMapper> nodeMapperFactory = usedIdxs -> {
			try {
				return createNodeMapper(rsmd, usedIdxs, sqlTypeMapper);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		};
		
		RowMapper result = createBindingMapper(colNameToIdx, nodeMapperFactory);
		return result;
	}
	
	public static RowMapper createBindingMapper(
			Map<Var, Integer> colNameToIdx,
			Function<int[], NodeMapper> nodeMapperFactory
			) throws SQLException {
		
		int n = colNameToIdx.size();
		int[] colIdxs = new int[n];
		Var[] vars = new Var[n];

		int i = 0;
		for (Entry<Var, Integer> e : colNameToIdx.entrySet()) {
			vars[i] = e.getKey();
			colIdxs[i] = e.getValue();
			++i;
		}
	
		NodeMapper nodeMapper = nodeMapperFactory.apply(colIdxs);

		RowMapper result = new RowToBindingImpl(colIdxs, vars, nodeMapper);
		return result;
	}
	
}
