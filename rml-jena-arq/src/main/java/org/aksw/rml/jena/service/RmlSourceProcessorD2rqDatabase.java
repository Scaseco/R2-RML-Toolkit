package org.aksw.rml.jena.service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.sql.DataSource;

import org.aksw.commons.sql.codec.api.SqlCodec;
import org.aksw.commons.sql.codec.util.SqlCodecUtils;
import org.aksw.commons.util.lifecycle.ResourceMgr;
import org.aksw.commons.util.obj.ObjectUtils;
import org.aksw.jena_sparql_api.sparql.ext.nodemap.NodeValueNodeMap;
import org.aksw.jenax.arq.util.exec.query.JenaXSymbols;
import org.aksw.jenax.arq.util.node.NodeMap;
import org.aksw.jenax.model.d2rq.domain.api.D2rqDatabase;
import org.aksw.jenax.reprogen.util.Skolemize;
import org.aksw.r2rml.jena.jdbc.api.NodeMapper;
import org.aksw.r2rml.jena.jdbc.processor.R2rmlJdbcUtils;
import org.aksw.rml.model.LogicalSourceRml1;
import org.aksw.rml.rso.model.SourceOutput;
import org.aksw.rml.v2.common.vocab.RmlIoTerms;
import org.aksw.rml.v2.jena.domain.api.LogicalSourceRml2;
import org.aksw.rmltk.model.backbone.rml.ILogicalSource;
import org.aksw.rmltk.model.r2rml.LogicalTable;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.graph.compose.Delta;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

public class RmlSourceProcessorD2rqDatabase
    implements RmlSourceProcessor
{
    private static final RmlSourceProcessorD2rqDatabase INSTANCE = new RmlSourceProcessorD2rqDatabase();

    public static final Symbol symJdbcDataSources = Symbol.create("JdbcDataSources");

    public static RmlSourceProcessorD2rqDatabase getInstance() {
        return INSTANCE;
    }

    public static LogicalTable toLogicalTable(ILogicalSource logicalSource) {
        LogicalTable result;
        if (logicalSource instanceof LogicalTable || logicalSource instanceof LogicalSourceRml1) {
            result = logicalSource.as(LogicalTable.class);
//            result = ModelFactory.createDefaultModel().createResource().as(LogicalSourceRml2.class);
//            LogicalTable lt = logicalSource.as(LogicalTable.class);
//            if (lt.qualifiesAsBaseTableOrView()) {
//                result.setReferenceFormulationIri(RmlIoTerms.SQL2008Table);
//                String tableName = lt.asBaseTableOrView().getTableName();
//                result.setIterator(tableName);
//            } else if (lt.qualifiesAsR2rmlView()) {
//                result.setReferenceFormulationIri(RmlIoTerms.SQL2008Query);
//                String sqlQuery = lt.asR2rmlView().getSqlQuery();
//                result.setIterator(sqlQuery);
//            } else {
//                throw new RuntimeException("Unknown logical table type: " + logicalSource);
//            }
        } else if (logicalSource instanceof LogicalSourceRml2) {
            // result = (LogicalSourceRml2)logicalSource;
            LogicalSourceRml2 rml2Ls = logicalSource.as(LogicalSourceRml2.class);
            String rf = rml2Ls.getReferenceFormulationIri();

            result = ModelFactory.createDefaultModel().createResource().as(LogicalTable.class);

            switch (rf) {
            case RmlIoTerms.SQL2008Table:
                String tableName = rml2Ls.getIterator();
                result.asBaseTableOrView().setTableName(tableName);
                break;
            case RmlIoTerms.SQL2008Query:
                String sqlQueryString = rml2Ls.getIterator();
                result.asR2rmlView().setSqlQuery(sqlQueryString);
                break;
            default:
                throw new RuntimeException("Unknown reference formulation set: " + rf);
            }
        } else {
            throw new RuntimeException("Unknown source: " + ObjectUtils.getClass(logicalSource) + " - " + logicalSource);
        }
        return result;
    }

    @Override
    public QueryIterator eval(ILogicalSource logicalSource, Binding parentBinding, ExecutionContext execCxt) {
        // TODO Check for RML 2 reference formulations
        LogicalTable logicalTable = toLogicalTable(logicalSource);

        SourceOutput output = logicalSource.as(SourceOutput.class);
        Var outVar = output.getOutputVar();

        // TODO Register data source with execCxt and reuse if present
        D2rqDatabase dataSourceSpecRaw = logicalSource.getSource().as(D2rqDatabase.class);

        Context cxt = execCxt.getContext();

        ResourceMgr resourceMgr = cxt.get(JenaXSymbols.symResourceMgr);
        if (resourceMgr == null) {
            throw new RuntimeException("No resource manager configured for query execution.");
        }

        Map<D2rqDatabase, DataSource> configToDataSource = cxt.get(symJdbcDataSources);
        if (configToDataSource == null) {
            configToDataSource = new ConcurrentHashMap<>();
            cxt.set(symJdbcDataSources, configToDataSource);
        }

        Consumer<D2rqDatabase> d2rqDatabaseResolver = cxt.get(RmlSymbols.symD2rqDatabaseResolver);

        D2rqDatabase dataSourceSpec = dataSourceSpecRaw;
        if (d2rqDatabaseResolver != null) {
            dataSourceSpec = dataSourceSpecRaw.inModel(ModelFactory.createModelForGraph(
                    new Delta(dataSourceSpecRaw.getModel().getGraph()))).as(D2rqDatabase.class);

            d2rqDatabaseResolver.accept(dataSourceSpec);
        }

        // Create an ID for the database config
        // XXX Code should be enhanced to use proper equality check
        D2rqDatabase finalSpec = Skolemize.skolemize(dataSourceSpec, "urn:", D2rqDatabase.class, null);

        // Get or create the data source. If it needs to be created then also register
        // the close action with the resource manager
        DataSource dataSource = configToDataSource.computeIfAbsent(finalSpec, k -> {
            DataSource r = D2rqHikariUtils.configureDataSource(finalSpec);
            resourceMgr.register(r, ds -> D2rqHikariUtils.close(ds));
            return r;
        });

        SqlCodec sqlCodec;
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String quoteChar = metaData.getIdentifierQuoteString();
            sqlCodec = SqlCodecUtils.createSqlCodec("'", "\"", quoteChar, "'");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Connection conn = dataSource.getConnection();

        NodeMapper nodeMapper = null; // create on demand TODO This is hacky
        IteratorCloseable<NodeMap> it;
        try {
            it = R2rmlJdbcUtils.processR2rml(dataSource, logicalTable, nodeMapper, sqlCodec);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Iter<Binding> it2 = Iter.iter(it).map(b -> {
            // Binding bb = BindingFactory.copy(b); // The binding is just a view over the SQL result set - better copy
            // Binding r = BindingFactory.binding(parentBinding, outVar, new NodeValueBinding(b).asNode());
            Binding r = BindingFactory.binding(parentBinding, outVar, new NodeValueNodeMap(b).asNode());
            return r;
        });

        return QueryIterPlainWrapper.create(it2, execCxt);
    }
}
