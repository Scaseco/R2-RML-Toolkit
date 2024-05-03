package org.aksw.r2rml.jena.jdbc.processor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.aksw.commons.sql.codec.api.SqlCodec;
import org.aksw.r2rml.jena.jdbc.api.NodeMapper;
import org.aksw.r2rml.jena.jdbc.impl.SqlTypeMapper;
import org.aksw.r2rml.jena.jdbc.util.JdbcUtils;
import org.aksw.rmltk.model.r2rml.LogicalTable;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.sparql.engine.binding.Binding;

public class R2rmlJdbcUtils {

    public static IteratorCloseable<Binding> processR2rml(DataSource dataSource, LogicalTable logicalTable,
            NodeMapper nodeMapper, SqlCodec sqlCodec) throws SQLException {
        Connection conn = dataSource.getConnection();
        IteratorCloseable<Binding> it = processR2rml(conn, logicalTable, nodeMapper, sqlCodec);
        return Iter.onCloseIO(it, () -> {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new IOException(e);
            }
        });
    }

    public static IteratorCloseable<Binding> processR2rml(Connection conn, LogicalTable logicalTable,
            NodeMapper nodeMapper, SqlCodec sqlCodec) throws SQLException {

        String sqlQuery;
        if (logicalTable.qualifiesAsBaseTableOrView()) {
            String tableName = logicalTable.asBaseTableOrView().getTableName();
            String encodedTableName = sqlCodec.forTableName().encode(tableName);
            sqlQuery = "SELECT * FROM " + encodedTableName;
        } else if (logicalTable.qualifiesAsR2rmlView()) {
            sqlQuery = logicalTable.asR2rmlView().getSqlQuery();
        } else {
            throw new IllegalArgumentException("No logical table present");
        }

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sqlQuery);
        ResultSetMetaData rsmd = rs.getMetaData();

        if (nodeMapper == null) {
            nodeMapper = JdbcUtils.createNodeMapper(rsmd, SqlTypeMapper.getInstance());
        }

        ResultSetState state = new ResultSetState(rs, rsmd, 0, nodeMapper);
        IteratorJdbcBinding result = new IteratorJdbcBinding(stmt, state);

        return result;
    }
}
