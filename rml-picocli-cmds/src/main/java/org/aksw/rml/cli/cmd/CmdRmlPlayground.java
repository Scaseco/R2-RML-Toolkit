package org.aksw.rml.cli.cmd;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aksw.commons.util.algebra.GenericDag;
import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.aksw.jena_sparql_api.algebra.utils.OpVar;
import org.aksw.jenax.arq.util.syntax.QueryGenerationUtils;
import org.aksw.jenax.stmt.core.SparqlStmtMgr;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryType;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpLateral;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarAlloc;
import org.apache.jena.sparql.lang.arq.ParseException;

public class CmdRmlPlayground {

    public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
        Quad quadVars = Quad.create(Var.alloc("__g__"), Var.alloc("__s__"), Var.alloc("__p__"), Var.alloc("__o__"));

        List<Query> queries = SparqlStmtMgr.loadQueries("/home/raven/Datasets/gtfsbench/grouped.rq", new PrefixMappingImpl());

        List<Query> lateralConstructQueries = queries.stream()
                .map(query -> QueryGenerationUtils.constructToLateral(query, quadVars, QueryType.CONSTRUCT, false, false))
                .collect(Collectors.toList());

        GenericDag<Op, Var> dag = buildDag(lateralConstructQueries);
        Op rootOp = dag.getRoots().iterator().next();


//        cxt = cxt == null ? ARQ.getContext().copy() : cxt.copy();
//        cxt.set(ARQConstants.sysCurrentTime, NodeFactoryExtra.nowAsDateTime());
//        ExecutionContext execCxt = new ExecutionContext(cxt, null, null, null);
//        OpExecutor opExec = new OpExecutorImpl(execCxt, dag.getVarToExpr());
//        ExecutionDispatch executionDispatch = new ExecutionDispatch(opExec);
//
//        // An RDD with a single binding that doesn't bind any variables
//        JavaSparkContext sparkContext = JavaSparkContextUtils.fromRdd(initialRdd);
//
//        JavaRDD<Binding> rdd = executionDispatch.exec(rootOp, initialRdd);

//        List<Quad> quads = Arrays.asList(quadVars);
//        JavaRDD<Quad> result = rdd.mapPartitions(it -> TemplateLib.calcQuads(quads, it));
    }


    public static GenericDag<Op, Var> buildDag(Collection<Query> queries) {
        List<Op> ops = queries.stream().map(Algebra::compile).collect(Collectors.toList());
        OpDisjunction union = OpDisjunction.create();
        ops.forEach(union::add);
        // Do not descend into the rhs of laterals
        GenericDag<Op, Var> dag = new GenericDag<>(OpUtils.getOpOps(),  new VarAlloc("op")::allocVar, (p, i, c) -> p instanceof OpService || (p instanceof OpLateral && i != 0));
        dag.addRoot(union);

        // Insert cache nodes
        // vx := someOp(vy) becomes
        // vx := cache(vxCache)
        // vxCache := someOp(vy)
        for (Map.Entry<Var, Collection<Var>> entry : dag.getChildToParent().asMap().entrySet()) {
            if (entry.getValue().size() > 1) {
                // System.out.println("Multiple parents on: " + entry.getKey());
                Var v = entry.getKey();
                Op def = dag.getVarToExpr().get(v);
                Var uncachedVar = Var.alloc(v.getName() + "_cached");
                dag.getVarToExpr().remove(v);
                dag.getVarToExpr().put(uncachedVar, def);
                dag.getVarToExpr().put(v, new OpService(NodeFactory.createURI("rdd:cache"), new OpVar(uncachedVar), false));
            }
        }

        dag.collapse();
        System.err.println("Roots: " + dag.getRoots());
        for (Map.Entry<Var, Op> e : dag.getVarToExpr().entrySet()) {
            // logger.info(e.toString());
            System.err.println(e.toString());
        }
        return dag;
    }
}
