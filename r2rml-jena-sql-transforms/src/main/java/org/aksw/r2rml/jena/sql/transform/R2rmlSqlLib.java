package org.aksw.r2rml.jena.sql.transform;

import java.util.List;
import java.util.stream.Collectors;

import org.aksw.commons.codec.entity.util.EntityCodecUtils;
import org.aksw.commons.sql.codec.api.SqlCodec;
import org.aksw.r2rml.jena.arq.lib.R2rmlLib;
import org.aksw.r2rml.jena.domain.api.LogicalTable;
import org.aksw.r2rml.jena.domain.api.ObjectMapType;
import org.aksw.r2rml.jena.domain.api.PredicateMap;
import org.aksw.r2rml.jena.domain.api.PredicateObjectMap;
import org.aksw.r2rml.jena.domain.api.R2rmlView;
import org.aksw.r2rml.jena.domain.api.SubjectMap;
import org.aksw.r2rml.jena.domain.api.TermMap;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.aksw.r2rml.sql.transform.SqlUtils;
import org.apache.jena.rdf.model.Model;

import net.sf.jsqlparser.JSQLParserException;

public class R2rmlSqlLib {
	
//	public static Model harmonizeIdentifiers(
//			Model model,
//			SqlCodec sqlCodec) throws SqlParseException {
//		List<TriplesMap> tms = R2rmlLib.streamTriplesMaps(model).collect(Collectors.toList());
//
//		for (TriplesMap tm : tms) {
//			LogicalTable lt = tm.getOrSetLogicalTable();
//			if (lt.qualifiesAsBaseTableOrView()) {
//				// FIXME We need to check whether the table name is already qualified
//				String oldTableName = lt.asBaseTableOrView().getTableName();
//				String newTableName = SqlUtils.harmonizeTableName(oldTableName, sqlCodec);
//				
//				lt.asBaseTableOrView().setTableName(newTableName);
//			} else {
//				R2rmlView view = lt.asR2rmlView();
//				String queryStr = view.getSqlQuery();
//				queryStr = SqlUtils.harmonizeIdentifiers(queryStr, sqlCodec);
//				view.setSqlQuery(queryStr);
//			}
//		}
//		;
//		return model;
//	}

	
	/**
	 * Make all table identifiers being qualified with the given database resp.
	 * schema name.
	 *
	 * @param database the database schema name
	 * @param model    the R2RML mappings
	 * @return the modified R2RML mappings
	 * @throws JSQLParserException
	 * @throws SqlParseException 
	 */
	public static Model makeQualifiedTableIdentifiers(
			Model model,
			String schemaName,
			SqlCodec sqlCodec,
			boolean replaceAll) throws SqlParseException {
		List<TriplesMap> tms = R2rmlLib.streamTriplesMaps(model).collect(Collectors.toList());

		for (TriplesMap tm : tms) {
			LogicalTable lt = tm.getOrSetLogicalTable();
			if (lt.qualifiesAsBaseTableOrView()) {
				// FIXME We need to check whether the table name is already qualified
				String schemaPart = schemaName == null ? "" : EntityCodecUtils.harmonize(schemaName, sqlCodec::forSchemaName) + ".";
				String tablePart = EntityCodecUtils.harmonize(lt.asBaseTableOrView().getTableName(), sqlCodec::forTableName);
				
				lt.asBaseTableOrView().setTableName(schemaPart + tablePart);
			} else {
				R2rmlView view = lt.asR2rmlView();
				String queryStr = view.getSqlQuery();
				queryStr = SqlUtils.setSchemaForTables(queryStr, schemaName, replaceAll);
				view.setSqlQuery(queryStr);
			}
		}
		;
		return model;
	}

	
	public static Model harmonizeSqlIdentifiers(Model model, SqlCodec sqlCodec)
			throws SqlParseException {
		List<TriplesMap> triplesMaps = R2rmlLib.streamTriplesMaps(model).collect(Collectors.toList());

		for (TriplesMap tm : triplesMaps) {
			LogicalTable lt = tm.getOrSetLogicalTable();

			if (lt.qualifiesAsBaseTableOrView()) {
				String oldTableName = lt.asBaseTableOrView().getTableName();
				String newTableName = SqlUtils.harmonizeTableName(oldTableName, sqlCodec);
				lt.asBaseTableOrView().setTableName(newTableName);
			} else {
				R2rmlView view = lt.asR2rmlView();
				String queryStr = view.getSqlQuery();
				queryStr = SqlUtils.harmonizeQueryString(queryStr, sqlCodec);
				view.setSqlQuery(queryStr);
			}

			// column names
			// s
			SubjectMap sm = tm.getSubjectMap();
			if (sm != null) {
				String col = sm.getColumn();
				if (col != null) {
					sm.setColumn(SqlUtils.harmonizeColumnName(col, sqlCodec));
				}
			}

			for (PredicateObjectMap pom : tm.getPredicateObjectMaps()) {
				// p
				for (PredicateMap pm : pom.getPredicateMaps()) {
					String col = pm.getColumn();
					if (col != null) {
						sm.setColumn(SqlUtils.harmonizeColumnName(col, sqlCodec));
					}
				}

				// o
				for (ObjectMapType om : pom.getObjectMaps()) {
					if (om.qualifiesAsTermMap()) {
						TermMap otm = om.asTermMap();
						String col = otm.getColumn();
						if (col != null) {
							sm.setColumn(SqlUtils.harmonizeColumnName(col, sqlCodec));
						}
					}
				}
			}
		}

		return model;
	}

	/**
	 * Replaces the escape chars of all SQL identifiers, i.e. the table and column
	 * names.
	 *
	 * @param model         the R2RML mappings
	 * @param oldEscapeChar the old escape char
	 * @param newEscapeChar the new escape char
	 * @return the modified R2RML mappings
	 * @throws JSQLParserException
	 */
	@Deprecated
	public static Model replaceEscapeChars(Model model, String oldEscapeChar, String newEscapeChar)
			throws SqlParseException {
		List<TriplesMap> triplesMaps = R2rmlLib.streamTriplesMaps(model).collect(Collectors.toList());

		for (TriplesMap tm : triplesMaps) {
			LogicalTable lt = tm.getOrSetLogicalTable();

			if (lt.qualifiesAsBaseTableOrView()) {// tables
				String tn = lt.asBaseTableOrView().getTableName();
				lt.asBaseTableOrView().setTableName(SqlUtils.replaceIdentifier(tn, oldEscapeChar, newEscapeChar));
			} else { // views
				R2rmlView view = lt.asR2rmlView();
				String queryStr = view.getSqlQuery();
				view.setSqlQuery(SqlUtils.replaceQueryIdentifiers(queryStr, oldEscapeChar, newEscapeChar));
			}

			// column names
			// s
			SubjectMap sm = tm.getSubjectMap();
			if (sm != null) {
				String col = sm.getColumn();
				if (col != null) {
					sm.setColumn(SqlUtils.replaceIdentifier(col, oldEscapeChar, newEscapeChar));
				}
			}

			for (PredicateObjectMap pom : tm.getPredicateObjectMaps()) {
				// p
				for (PredicateMap pm : pom.getPredicateMaps()) {
					String col = pm.getColumn();
					if (col != null) {
						pm.setColumn(SqlUtils.replaceIdentifier(col, oldEscapeChar, newEscapeChar));
					}
				}

				// o
				for (ObjectMapType om : pom.getObjectMaps()) {
					if (om.qualifiesAsTermMap()) {
						TermMap otm = om.asTermMap();
						String col = otm.getColumn();
						if (col != null) {
							otm.setColumn(SqlUtils.replaceIdentifier(col, oldEscapeChar, newEscapeChar));
						}
					}
				}
			}
		}

		return model;
	}
}
