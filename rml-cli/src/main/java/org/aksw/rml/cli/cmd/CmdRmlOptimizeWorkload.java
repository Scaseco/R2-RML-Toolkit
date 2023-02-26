package org.aksw.rml.cli.cmd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.rx.script.SparqlScriptProcessor;
import org.aksw.jenax.arq.util.quad.QuadUtils;
import org.aksw.jenax.arq.util.syntax.QueryGenerationUtils;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.rml.jena.impl.Clusters;
import org.aksw.rml.jena.impl.Clusters.Cluster;
import org.aksw.rml.jena.impl.RmlLib;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryType;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.Template;

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


        // boolean intraDistinct;
        boolean distinct = true;
        // Adding ?s ?p ?o should collapse all clusters into a single one
        // queries.add(QueryFactory.create("CONSTRUCT WHERE { ?s ?p ?o }"));
        if (distinct) {
            // TODO Clustering has yet to be handled
            // Convert to construct to lateral union
            Quad quadVars = Quad.create(Var.alloc("__g__"), Var.alloc("__s__"), Var.alloc("__p__"), Var.alloc("__o__"));

            Clusters<Quad, Query> clusters = RmlLib.groupConstructQueriesByTemplate(queries);




            List<Query> newQueries = new ArrayList<>();
            for (Entry<Integer, Cluster<Quad, Query>> e : clusters.entrySet()) {
                System.err.println("Cluster " + e.getKey() + ": " + e.getValue().getValues().size() + " entries");

                // If cluster has only one query then just add distinct to the projection
                // Otherwise create a union with distinct
                Collection<Query> members = e.getValue().getValues();
                if (members.size() == 1) {
                    Query member = members.iterator().next();
                    Query newQuery = QueryGenerationUtils.constructToLateral(member, quadVars, QueryType.SELECT, true, true);
                    newQueries.add(newQuery);
                } else {
                    List<Element> tmp = members.stream()
                            .map(member -> QueryGenerationUtils.constructToLateral(member, quadVars, QueryType.SELECT, false, true))
                            .map(ElementSubQuery::new)
                            .collect(Collectors.toList());

                    Query newQuery = createUnionQuery(tmp, quadVars, true);
                    newQueries.add(newQuery);
                }
            }


            Query innerQuery;
            if (newQueries.size() == 1) {
                innerQuery = newQueries.iterator().next();
            } else {
                List<Element> ms = newQueries.stream().map(ElementSubQuery::new).collect(Collectors.toList());
                innerQuery = createUnionQuery(ms, quadVars, false);
            }

            Query finalQuery = new Query();
            finalQuery.setQueryConstructType();
            finalQuery.setConstructTemplate(new Template(new QuadAcc(Collections.singletonList(quadVars))));
            finalQuery.setQueryPattern(new ElementSubQuery(innerQuery));

            queries = Collections.singletonList(finalQuery);
        }

        for (Query query : queries) {
            System.out.println(query);
        }
        return 0;
    }

    public static Query createUnionQuery(List<Element> members, Quad quadVars, boolean distinct) {
        Query result = new Query();
        result.setQuerySelectType();
        result.setDistinct(distinct);
        QuadUtils.streamNodes(quadVars).forEach(v -> result.getProject().add((Var)v));
        ElementUnion unionElt = new ElementUnion();
        for (Element member : members) {
            unionElt.addElement(member);
        }
        result.setQueryPattern(unionElt);
        return result;
    }

}
