package org.aksw.r2rml.jena.jdbc.processor;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Function;

import org.aksw.commons.util.exception.FinallyRunAll;
import org.aksw.jenax.arq.util.node.NodeMap;
import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;

import com.google.common.collect.AbstractIterator;

public class IteratorJdbcBinding<T>
    extends AbstractIterator<T> implements IteratorCloseable<T>
{
    // protected Connection conn;
    protected Statement stmt;
    protected ResultSetState state;
    protected Function<ResultSetState, T> stateToRow;

    public IteratorJdbcBinding(Statement stmt, ResultSetState state, Function<ResultSetState, T> stateToRow) {
        super();
        this.stmt = stmt;
        this.state = state;
        this.stateToRow = stateToRow;
    }

    @Override
    protected T computeNext() {
        T result;
        try {
            result = state.next()
                ? stateToRow.apply(state)
                : endOfData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public void close() {
        FinallyRunAll.run(stmt::close, state.getDelegate()::close);
    }

    public static IteratorCloseable<Binding> forBinding(Statement stmt, ResultSetState state) {
        return new IteratorJdbcBinding<>(stmt, state, x -> BindingFactory.copy(new BindingOverJdbcResultSet(x)));
    }

    public static IteratorCloseable<NodeMap> forNodeMap(Statement stmt, ResultSetState state) {
        return new IteratorJdbcBinding<>(stmt, state, ResultSetState::toNodeMap);
    }
}
