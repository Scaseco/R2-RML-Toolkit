package org.aksw.r2rml.jena.jdbc.impl;

import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.aksw.r2rml.jena.jdbc.api.NodeMapper;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_StrReplace;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.user.UserDefinedFunctionDefinition;

class XsdTerms {
	public static final String uri = "http://www.w3.org/2001/XMLSchema#";
	
	public static final String hexBinary = uri + "hexBinary";
	public static final String decimal = uri + "decimal";
	public static final String integer = uri + "integer";
	public static final String xdouble = uri + "double";
	public static final String xboolean = uri + "boolean";
	public static final String date = uri + "date";
	public static final String time = uri + "time";
	public static final String dateTime = uri + "dateTime";
	public static final String string = uri + "string";
}

public class NaturalMappings {
	
	private static final Var x = Var.alloc("x");
	
	public static final UserDefinedFunctionDefinition udfd_timestamp =
		new UserDefinedFunctionDefinition(
			"urn:r2rml:timestamp",
			new E_StrReplace(
				new ExprVar(x), NodeValue.makeString(" "), NodeValue.makeString("T"), null), Arrays.asList(x));


	/** Put the same value for multiple keys */
	public static <K, V> void putValue(Map<K, V> map, V value, K ... keys) {
		for (K key : keys) {
			map.put(key, value);
		}
	}
	
//	public static void registerNodeMappers(Map<String, NodeMapper> resultSetToNode) {
//		resultSetToNode.put(XsdTerms.decimal, (rs, idx) -> NodeValue.makeDecimal(rs.getBigDecimal(idx)).asNode());
//		resultSetToNode.put(XsdTerms.integer, (rs, idx) -> NodeValue.makeDecimal(rs.get(idx)).asNode());
//	}
//	
	public static NodeMapper createNodeMapper(int sqlType) {
		SqlTypeMapper sqlTypeMapper = SqlTypeMapper.getInstance();
		return createNodeMapper(sqlType, sqlTypeMapper);
	}

	public static NodeMapper createNodeMapper(int sqlType, SqlTypeMapper sqlTypeMapper) {
		SqlDatatype sqlDatatype = sqlTypeMapper.getTypeBySqlType(sqlType);

		Objects.requireNonNull(sqlDatatype, "No SqlDatatype found for sqlType " + sqlType);
		
		return new NodeMapperViaSqlDatatype(sqlDatatype);
		
	}
	
	public static Set<SqlDatatype> getDefaultDatatypes(TypeMapper typeMapper) {
		Map<Integer, String> sqlToXsd = new LinkedHashMap<>();
		registerSqlToXsdTypeMaps(sqlToXsd);

		Set<SqlDatatype> result = new LinkedHashSet<>();
		
		for (Entry<Integer, String> e : sqlToXsd.entrySet()) {
			int sqlType = e.getKey();
			String xsdIri = e.getValue();
			
			// Really use safe here - or rather bail out early?
			RDFDatatype dtype = typeMapper.getTypeByName(xsdIri); // .getSafeTypeByName(xsdIri);
			
			// TODO Verify that the sqlType-to-java class is actually consistent with specs
			// when we just use the type mapper
			Class<?> sqlJavaClass = dtype.getJavaClass();
			
			
			Function<Object, Object> compatibilizer = null;
			Function<Object, String> lexicalFormizer = null;
			
			
			if (xsdIri.equals(XsdTerms.xdouble) && sqlType != Types.DOUBLE) {
				lexicalFormizer = o -> Float.toString((Float)o);
				// compatibilizer = o -> o instanceof Float ? ((Float)o).doubleValue() : o;
			}
			
			if (sqlType == Types.TIMESTAMP) {
				
				if (false) {
					// Jena keeps appending a 'Z' to the timestamps for GMT
					// It seems there is no way to turn it off so we have to go
					// via the lexical form for r2rml compliance
					compatibilizer = o -> {
						Timestamp timestamp = (Timestamp)o;
				        Calendar calendar = Calendar.getInstance();
				        calendar.setTimeInMillis(timestamp.getTime());
				        calendar.clear(Calendar.ZONE_OFFSET);
				        calendar.clear(Calendar.DST_OFFSET);
				        XSDDateTime r = new XSDDateTime(calendar);
				        return r;
					};
				} else {
					lexicalFormizer = o -> {
						String r = o.toString().replace(' ', 'T');
						return r;
					};
				}
			}
			
			result.add(new SqlDatatypeImpl(sqlType, sqlJavaClass, dtype, compatibilizer, lexicalFormizer, null));  
		}

		return result;
	}
	
	/**
	 * Natural mappings according to https://www.w3.org/TR/r2rml/#natural-mapping
	 * 
	 * @param sqlToXsd The map to which the mappings are added
	 */
	public static void registerSqlToXsdTypeMaps(Map<Integer, String> sqlToXsd) {
		String xsd = "http://www.w3.org/2001/XMLSchema#";

		putValue(sqlToXsd, XsdTerms.hexBinary,
				Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY);

		putValue(sqlToXsd, XsdTerms.decimal,
				Types.NUMERIC, Types.DECIMAL);

		putValue(sqlToXsd, XsdTerms.integer,
				Types.SMALLINT, Types.INTEGER, Types.BIGINT);
		
		putValue(sqlToXsd, XsdTerms.xdouble,
				Types.FLOAT, Types.REAL, Types.DOUBLE);

		putValue(sqlToXsd, XsdTerms.xboolean, Types.BOOLEAN);
		putValue(sqlToXsd, XsdTerms.date, Types.DATE);
		putValue(sqlToXsd, XsdTerms.time, Types.TIME);
		putValue(sqlToXsd, XsdTerms.dateTime, Types.TIMESTAMP);
		
		putValue(sqlToXsd, XsdTerms.string,
				Types.VARCHAR, Types.LONGVARCHAR, Types.LONGNVARCHAR);

		// Further mappings
		
		putValue(sqlToXsd, XsdTerms.string,
				Types.CHAR, Types.NCHAR);
	}
	
//	public static String getXsdTypeForColumnType(int columnType) {
//		String xsd = "http://www.w3.org/2001/XMLSchema#";
//
//		String result;
//		switch (columnType) {
//		case Types.BINARY:
//		case Types.VARBINARY:
//		case Types.LONGVARBINARY:			
//			result = xsd + "hexBinary";
//			break;		
//		
//		case Types.NUMERIC:
//		case Types.DECIMAL:
//			result = xsd + "decimal";
//			break;
//		
//		case Types.SMALLINT:
//		case Types.INTEGER:
//		case Types.BIGINT:
//			result = xsd + "integer";
//			break;
//		
//		case Types.FLOAT:
//		case Types.REAL:
//		case Types.DOUBLE:
//			result = xsd + "double";
//			break;
//		
//		case Types.BOOLEAN:
//			result = xsd + "boolean";
//			break;
//		
//		case Types.DATE:
//			result = xsd + "date";
//			break;
//		
//		case Types.TIME:
//			result = xsd + "time";
//			break;
//
//		case Types.TIMESTAMP:
//			result = xsd + "dateTime";
//			break;
//			
//		default:
//			result = null;
//			break;
//		}
//		
//		return result;
//	}
}
