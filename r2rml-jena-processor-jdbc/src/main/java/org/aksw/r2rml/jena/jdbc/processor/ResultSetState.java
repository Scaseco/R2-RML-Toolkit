package org.aksw.r2rml.jena.jdbc.processor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jenax.arq.util.node.NodeMap;
import org.aksw.jenax.arq.util.node.NodeMapImpl;
import org.aksw.jenax.arq.util.var.VarUtils;
import org.aksw.r2rml.jena.jdbc.api.NodeMapper;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import com.google.common.collect.Iterators;

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
            // String colNameRaw = metaData.getColumnName(i);
            String colNameRaw = metaData.getColumnLabel(i);
            // String colName = VarUtils.safeVarName(colNameRaw);
            Var colVar = Var.alloc(colNameRaw);
            varToIdx.put(colVar, i);
        }
    }

    public ResultSetMetaData getMetaData() {
        return metaData;
    }

    public int getVarCount() {
        int result = Iterators.size(getVarIterator());
        return result;
        // return getVars().size();
    }

    /** Return an iterator over all bound variables */
    public Iterator<Var> getVarIterator() {
        return Iter.iter(varToIdx.entrySet())
            .filter(e -> getNode(e.getValue().intValue()) != null)
            .map(Entry::getKey);
        // return getVars().iterator();
    }

    public Set<Var> getVarsIternal() {
        return varToIdx.keySet();
    }

//    public Set<Var> getVars() {
//        return varToIdx.keySet();
//    }


    public boolean containsVarInternal(Var var) {
        boolean result = varToIdx.get(var) != null;
        return result;
    }

    public NodeMap toNodeMap() {
        Map<String, Node> map = new LinkedHashMap<>();
        for (Var var : getVarsIternal()) {
            String k = var.getName();
            Node v = getNode(var);
            map.put(k, v);
        }
        return new NodeMapImpl(Collections.unmodifiableMap(map));
    }

    /** Returns true if the variable's value is bound */
    public boolean containsVar(Var var) {
        Integer idx = varToIdx.get(var);
        Node node = idx == null ? null : getNode(idx.intValue());
        boolean result = node != null;
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
