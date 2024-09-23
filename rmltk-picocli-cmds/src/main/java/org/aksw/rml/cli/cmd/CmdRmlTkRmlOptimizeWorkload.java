package org.aksw.rml.cli.cmd;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.commons.io.util.FileUtils;
import org.aksw.jenax.arq.picocli.CmdMixinOutput;
import org.aksw.rml.jena.impl.RmlWorkloadOptimizer;
import org.apache.jena.query.Query;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "workload", description = "Optimize a workload of sparql queries by merging queries having the same RML source")
public class CmdRmlTkRmlOptimizeWorkload
    implements Callable<Integer>
{
    @Parameters(arity = "1..n", description = "Input RML file(s)")
    public List<String> inputFiles = new ArrayList<>();

//    @Option(names = { "--no-distinct" }, description = "Apply intra-query distinct", defaultValue = "false")
//    public boolean distinct = false;

    @Option(names = { "--cluster-by-predicate" }, description = "Cluster only by predicates. This is inferior to the default clustering by subject's value spaces.", defaultValue = "false")
    public boolean clusterByPredicate = false;

    @Option(names = { "--no-order" }, description = "Do not sort the result", defaultValue = "false")
    public boolean noOrder = false;

    @Option(names = { "--no-group" }, description = "Do not group queries that have the same source", defaultValue = "false")
    public boolean noGroup = false;


    @Option(names = { "--verbose" }, description = "Display additional information such as the partition tree", defaultValue = "false")
    public boolean verbose = false;


    /** TODO This method fits better in the to-sparql command */
    @Option(names = { "--pre-distinct" }, description = "Whenever DISTINCT is applied to the outcome of a UNION then also apply distinct to each member first", defaultValue = "false")
    public boolean preDistinct = false;

    @Mixin
    public CmdMixinOutput outputConfig = new CmdMixinOutput();

    @Override
    public Integer call() throws Exception {
        List<Query> queries = RmlWorkloadOptimizer.newInstance()
            .addSourceFiles(inputFiles)
            .setClusterByPredicate(clusterByPredicate)
            .setNoOrder(noOrder)
            .setNoGroup(noGroup)
            .setVerbose(verbose)
            .setPreDistinct(preDistinct)
            .process();

        try (PrintStream out = new PrintStream(FileUtils.newOutputStream(outputConfig), false, StandardCharsets.UTF_8)) {
            for (Query query : queries) {
                out.println(query);
            }
        }
        return 0;
    }
}
