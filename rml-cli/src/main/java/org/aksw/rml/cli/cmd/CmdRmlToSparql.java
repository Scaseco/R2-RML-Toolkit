package org.aksw.rml.cli.cmd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.r2rml.jena.arq.impl.JoinDeclaration;
import org.aksw.r2rml.jena.arq.impl.TriplesMapToSparqlMapping;
import org.aksw.rml.jena.impl.RmlImporterLib;
import org.aksw.rml.jena.impl.RmlLib;
import org.aksw.rml.jena.impl.RmlQueryGenerator;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.out.NodeFmtLib;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


@Command(name = "to-sparql", description = "Convert RML mappings to corresponding SPARQL queries")
public class CmdRmlToSparql
//     extends CmdCommonBase
    implements Callable<Integer>
{
    @Option(names = { "--fnml" }, description = "Function Mapping Language models")
    public List<String> fnmlFiles = new ArrayList<>();

    @Option(names = { "--no-optimize" }, description = "Disables merging of queries originating from multiple triples maps", defaultValue = "false")
    public boolean noOptimize = false;

    @Parameters(arity = "1..n", description = "Input RML file(s)")
    public List<String> inputFiles = new ArrayList<>();

    @Override
    public Integer call() throws Exception {
        Model fnmlModel = ModelFactory.createDefaultModel();
        for (String fnmlFile : fnmlFiles) {
            Model model = RDFDataMgr.loadModel(fnmlFile);
            fnmlModel.add(model);
        }

        List<Entry<Query, String>> labeledQueries = new ArrayList<>();
        for (String inputFile : inputFiles) {
            Model model = RDFDataMgr.loadModel(inputFile);
            Collection<TriplesMapToSparqlMapping> maps = RmlImporterLib.read(model, fnmlModel);
            // RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
            for (TriplesMapToSparqlMapping item : maps) {
                String tmId = NodeFmtLib.strNT(item.getTriplesMap().asNode());
                Query query = RmlQueryGenerator.createQuery(item, null);

                // Do not emit queries that do not produce anything (e.g. if there are only RefObjectMaps)
                if (!query.getConstructTemplate().getQuads().isEmpty()) {
                    QueryUtils.optimizePrefixes(query);
                    labeledQueries.add(Map.entry(query, "# " + tmId));
                }

                for (JoinDeclaration join : item.getJoins()) {
                    Query joinQuery = RmlQueryGenerator.createQuery(join, null);
                    QueryUtils.optimizePrefixes(joinQuery);
                    labeledQueries.add(Map.entry(joinQuery, "# " + tmId + " -> " + NodeFmtLib.strNT(join.getParentTriplesMap().asNode())));
                }
            }
        }

        if (!noOptimize) { // If optimize ...
            List<Query> queries = labeledQueries.stream().map(Entry::getKey).collect(Collectors.toList());
            RmlLib.optimizeRmlWorkloadInPlace(queries);
            for (Query query : queries) {
                System.out.println(query);
            }
        } else {
            // Without optimization we can output a comment about the origin of a query
            for (Entry<Query, String> entry : labeledQueries) {
                System.out.println(entry.getValue());
                System.out.println(entry.getKey());
            }
        }

        return 0;
    }
}
