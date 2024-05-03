package org.aksw.r2rml.jena.jdbc.processor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.sql.codec.api.SqlCodec;
import org.aksw.commons.util.algebra.GenericDag;
import org.aksw.r2rml.jena.arq.impl.R2rmlImporterLib;
import org.aksw.r2rml.jena.arq.impl.TriplesMapToSparqlMapping;
import org.aksw.r2rml.jena.arq.lib.R2rmlLib;
import org.aksw.r2rml.jena.jdbc.api.BindingMapper;
import org.aksw.r2rml.jena.jdbc.util.JdbcUtils;
import org.aksw.rmltk.model.backbone.common.IRefObjectMap;
import org.aksw.rmltk.model.backbone.common.ITriplesMap;
import org.aksw.rmltk.model.r2rml.LogicalTable;
import org.aksw.rmltk.model.r2rml.TriplesMap;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.jena.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class R2rmlProcessorJdbc {
    private static final Logger logger = LoggerFactory.getLogger(R2rmlProcessorJdbc.class);

    public static Dataset processR2rml(
            Connection conn,
            Model r2rmlDocument,
            String baseIri,
            SqlCodec sqlCodec) throws SQLException {

        //							RDFDataMgr.write(System.out, r2rmlDocument, RDFFormat.TURTLE_PRETTY);
        List<TriplesMap> rawTms = R2rmlLib.streamTriplesMaps(r2rmlDocument).collect(Collectors.toList());

        // The effective triples maps include expanded joins
        List<TriplesMap> tms = new ArrayList<>(rawTms);


        //							boolean usesJoin = rawTms.stream()
        //								.anyMatch(x -> x.getModel().listSubjectsWithProperty(RR.parentTriplesMap).toList().size() > 0);
        //
        //							if (!usesJoin) {
        //								System.err.println("Skipping mapping without join");
        //								continue;
        //							}

        // Expand joins
        for (TriplesMap tm : rawTms) {
            R2rmlLib.expandShortcuts(tm);
            Map<IRefObjectMap, ITriplesMap> map = R2rmlLib.expandRefObjectMapsInPlace(TriplesMap.class, tm, sqlCodec);
            if (!map.isEmpty()) {
                map.values().forEach(R2rmlLib::expandShortcuts);
            }

        //								for (TriplesMap joinTm : map.values()) {
        //									RDFDataMgr.write(System.out, ResourceUtils.reachableClosure(joinTm), RDFFormat.TURTLE_PRETTY);
        //								}

            for (ITriplesMap tmp : map.values()) {
                tms.add(tmp.as(TriplesMap.class));
            }
            //tms.addAll(map.values());
        }

        // RDFDataMgr.write(System.out, r2rmlDocument, RDFFormat.TURTLE_PRETTY);

        Dataset actualOutput = DatasetFactory.create();

        FunctionEnv env = createDefaultEnv();
        Query dummyQueryForBaseIri = new Query();
        dummyQueryForBaseIri.setBaseURI(baseIri);
        env.getContext().set(ARQConstants.sysCurrentQuery, dummyQueryForBaseIri);


        for (TriplesMap tm : tms) {

            Model closureModel = ResourceUtils.reachableClosure(tm);
            // RDFDataMgr.write(System.out, closureModel, RDFFormat.TURTLE_PRETTY);

            LogicalTable lt = tm.getLogicalTable();
            TriplesMapToSparqlMapping mapping = R2rmlImporterLib.read(tm, baseIri);

            //								RDFDataMgr.write(System.out, ResourceUtils.reachableClosure(tm), RDFFormat.TURTLE_PRETTY);

            // logger.debug("Generated Mapping: " + mapping);
            // System.out.println(mapping);

            // TODO This needs to be the set of undefined vars
            // Set<Var> usedVars = new HashSet<>();
            Set<Var> usedVars = GenericDag.getUndefinedVars(mapping.getExprDag());
            // mapping.getVarToExpr().getExprs().values().stream().forEach(e -> ExprVars.varsMentioned(usedVars, e));
            Map<Var, String> usedVarToColumnName = usedVars.stream()
                    .collect(Collectors.toMap(
                            v -> v,
                            v -> sqlCodec.forColumnName().decodeOrGetAsGiven(v.getName())
                    ));


            String sqlQuery;
            if (lt.qualifiesAsBaseTableOrView()) {
                sqlQuery = "SELECT * FROM " + lt.asBaseTableOrView().getTableName();
            } else if (lt.qualifiesAsR2rmlView()) {
                sqlQuery = lt.asR2rmlView().getSqlQuery();
            } else {
                System.err.println("No logical table present");
                continue;
            }

            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sqlQuery);
                ResultSetMetaData rsmd = rs.getMetaData();

                Set<Var> nullableVars = usedVarToColumnName.keySet(); // Collections.emptySet();
                BindingMapper bindingMapper = JdbcUtils.createDefaultBindingMapper(
                        rsmd,
                        usedVarToColumnName,
                        nullableVars); // .createBindingMapper(rs, usedVarToColumnName, new RowToNodeViaTypeManager());

                while (rs.next()) {
                     Binding b = bindingMapper.map(rs);
                     Binding effectiveBinding = mapping.evalVars(b, env, true);

                     List<Quad> generatedQuads = mapping.evalQuads(effectiveBinding).collect(Collectors.toList());

                     generatedQuads.forEach(actualOutput.asDatasetGraph()::add);
                }
            }
        }

        return actualOutput;
    }

    /** Create a fresh function environment / context on which to evaluate the
     * ARQ expressions used in the R2RML processor */
    public static FunctionEnv createDefaultEnv() {
        Context context = ARQ.getContext().copy() ;
        context.set(ARQConstants.sysCurrentTime, NodeFactoryExtra.nowAsDateTime()) ;
        FunctionEnv env = new ExecutionContext(context, null, null, null) ;

        return env;
    }
}
