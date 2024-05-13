package org.aksw.r2rml.jena.jdbc.processor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.jenax.arq.util.var.VarUtils;
import org.aksw.r2rml.jena.jdbc.api.NodeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

public class ResultSetState {
    protected ResultSet resultSet;
    protected ResultSetMetaData metaData;

    protected long currentRowId;

    protected Map<Var, Integer> varToIdx;
    protected NodeMapper nodeMapper;

//        public ResultSetState(ResultSet resultSet) {
//            this(resultSet, resultSet.getMetaData(), 0);
//        }

    public ResultSetState(ResultSet resultSet, ResultSetMetaData metaData, long currentRowId, NodeMapper nodeMapper)
            throws SQLException {
        super();
        this.resultSet = resultSet;
        this.metaData = metaData;
        this.nodeMapper = nodeMapper;
        this.currentRowId = currentRowId;

        varToIdx = new LinkedHashMap<>();
        int n = metaData.getColumnCount();
        for (int i = 1; i <= n; i++) {
            String colNameRaw = metaData.getColumnName(i);
            String colName = VarUtils.safeVarName(colNameRaw);
            Var colVar = Var.alloc(colName);
            varToIdx.put(colVar, i);
        }
    }

    public ResultSetMetaData getMetaData() {
        return metaData;
    }

    public int getVarCount() {
        return getVars().size();
    }

    public Iterator<Var> getVarIterator() {
        return getVars().iterator();
    }

    public Set<Var> getVars() {
        return varToIdx.keySet();
    }

    public boolean containsVar(Var var) {
        boolean result = varToIdx.get(var) != null;
        return result;
    }

    public boolean isEmpty() {
        return varToIdx.isEmpty();
    }

    public Node getNode(int columnIdx) {
        Node result;
        try {
            result = nodeMapper.map(resultSet, columnIdx);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public Node getNode(Var var) {
        int idx = getVarIdx(var);
        Node result = getNode(idx);
        return result;
    }

    public int getVarIdx(Var var) {
        Integer tmp = varToIdx.get(var);
        int result = tmp == null ? -1 : tmp;
        return result;
    }

    public Map<Var, Integer> getVarToIdx() {
        return varToIdx;
    }

    public ResultSet getDelegate() {
        return resultSet;
    }

    public long getCurrentRowId() {
        return currentRowId;
    }

    /**
     * Use this method rather than getDelegate().next() in order to correctly update
     * the currentRowId
     *
     * @throws SQLException
     */
    public boolean next() throws SQLException {
        boolean result = resultSet.next();
        if (result) {
            ++currentRowId;
        }
        return result;
    }
}
