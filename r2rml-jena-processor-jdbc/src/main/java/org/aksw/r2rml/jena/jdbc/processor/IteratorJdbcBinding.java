package org.aksw.r2rml.jena.jdbc.processor;

import java.sql.SQLException;
import java.sql.Statement;

import org.aksw.commons.util.exception.FinallyRunAll;
import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.sparql.engine.binding.Binding;

import com.google.common.collect.AbstractIterator;

public class IteratorJdbcBinding extends AbstractIterator<Binding> implements IteratorCloseable<Binding> {
    // protected Connection conn;
    protected Statement stmt;
    protected ResultSetState state;

    public IteratorJdbcBinding(Statement stmt, ResultSetState state) {
        super();
        this.stmt = stmt;
        this.state = state;
    }

    @Override
    protected Binding computeNext() {
        Binding result;
        try {
            result = state.next() ? new BindingOverJdbcResultSet(state) : endOfData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public void close() {
        FinallyRunAll.run(stmt::close, state.getDelegate()::close);
    }
}
