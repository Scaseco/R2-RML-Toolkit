package org.aksw.rml.cli.cmd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.aksw.commons.util.algebra.GenericDag;
import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.aksw.jena_sparql_api.algebra.utils.OpVar;
import org.aksw.jena_sparql_api.rx.script.SparqlScriptProcessor;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.rml.jena.impl.Clusters;
import org.aksw.rml.jena.impl.Clusters.Cluster;
import org.aksw.rml.jena.impl.RmlLib;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarAlloc;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "workload", description = "Optimize a workload of sparql queries by merging queries having the same RML source")
public class CmdRmlOptimizeWorkload
    implements Callable<Integer>
{
    @Parameters(arity = "1..n", description = "Input RML file(s)")
    public List<String> inputFiles = new ArrayList<>();

    @Override
    public Integer call() throws Exception {
        SparqlScriptProcessor processor = SparqlScriptProcessor.createPlain(null, null);
        processor.process(inputFiles);
        List<SparqlStmt> stmts = new ArrayList<>(processor.getPlainSparqlStmts());

        // TODO Only queries are supported - add a meaningful error message on violation
        List<Query> queries = stmts.stream().map(SparqlStmt::getQuery).collect(Collectors.toList());
        RmlLib.optimizeRmlWorkloadInPlace(queries);



        // Adding ?s ?p ?o should collapse all clusters into a single one
        // queries.add(QueryFactory.create("CONSTRUCT WHERE { ?s ?p ?o }"));
        if (false) {
            // TODO Clustering has yet to be handled
            Clusters<Quad, Query> clusters = RmlLib.groupConstructQueriesByGP(queries);
            for (Entry<Integer, Cluster<Quad, Query>> e : clusters.entrySet()) {
                System.err.println("Cluster " + e.getKey() + ": " + e.getValue().getValues().size() + " entries");
                for (Query q : e.getValue().getValues()) {
                    // System.err.println(q);
                }
            }
        }

        for (Query query : queries) {
            System.out.println(query);
        }
        return 0;
    }
}
