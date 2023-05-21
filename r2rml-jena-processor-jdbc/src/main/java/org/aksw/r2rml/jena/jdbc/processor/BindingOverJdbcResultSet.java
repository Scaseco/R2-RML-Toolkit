package org.aksw.r2rml.jena.jdbc.processor;

import java.util.Iterator;

import org.apache.jena.ext.com.google.common.base.Preconditions;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingBase;
import org.apache.jena.sparql.engine.binding.BindingFactory;

public class BindingOverJdbcResultSet extends BindingBase {

    protected ResultSetState state;
    protected long rowId;

//        protected BindingJdbc(Binding _parent) {
//            super(_parent);
//        }
    protected BindingOverJdbcResultSet(ResultSetState state) {
        super(BindingFactory.root());
        this.state = state;
        this.rowId = state.getCurrentRowId();
    }

    public void validate() {
        Preconditions.checkState(rowId == state.getCurrentRowId(),
                "Detecetd acces to a Binding view over an ResultSet but the ResultSet was moved");
    }

    @Override
    protected Iterator<Var> vars1() {
        validate();
        return state.getVarIterator();
    }

    @Override
    protected int size1() {
        validate();
        return state.getVarCount();
    }

    @Override
    protected boolean isEmpty1() {
        validate();
        return state.isEmpty();
    }

    @Override
    protected boolean contains1(Var var) {
        validate();
        return state.containsVar(var);
    }

    @Override
    protected Node get1(Var var) {
        validate();
        return state.getNode(var);
    }
}
