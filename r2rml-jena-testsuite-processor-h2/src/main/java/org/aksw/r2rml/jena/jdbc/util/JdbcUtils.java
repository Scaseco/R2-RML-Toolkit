package org.aksw.r2rml.jena.jdbc.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.r2rml.jena.jdbc.api.RowToBinding;
import org.aksw.r2rml.jena.jdbc.api.RowToNode;
import org.aksw.r2rml.jena.jdbc.impl.RowToBindingImpl;
import org.apache.jena.ext.com.google.common.collect.HashBiMap;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.sparql.core.Var;


public class JdbcUtils {
	public static RowToBinding createBindingMapper(
			ResultSet rs,
			// Set<Var> usedVars,
			Map<Var, String> usedVarToColumnName,
			RowToNode nodeMapper
			) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
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
			}
		}
		
		Set<Var> invalidRefs = Sets.difference(usedVarToColumnName.keySet(), colNameToIdx.keySet());
		if (!invalidRefs.isEmpty()) {
			throw new RuntimeException("The following non-existent columns are referenced: "
					+ invalidRefs + "; available: " + availableColumns);
		}
		
		int n = colNameToIdx.size();
		int[] colIdxs = new int[n];
		Var[] vars = new Var[n];

		int i = 0;
		for (Entry<Var, Integer> e : colNameToIdx.entrySet()) {
			vars[i] = e.getKey();
			colIdxs[i] = e.getValue();
			++i;
		}
	
		RowToBinding result = new RowToBindingImpl(colIdxs, vars, nodeMapper);
		return result;
	}
	
}
