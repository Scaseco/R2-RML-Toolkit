package org.aksw.rml.cli.cmd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jenax.arq.util.syntax.QueryGenerationUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.r2rml.jena.arq.impl.JoinDeclaration;
import org.aksw.r2rml.jena.arq.impl.TriplesMapToSparqlMapping;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.aksw.rml.jena.impl.RmlImporterLib;
import org.aksw.rml.jena.impl.RmlLib;
import org.aksw.rml.jena.impl.RmlQueryGenerator;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.riot.system.AsyncParser;
import org.apache.jena.riot.system.EltStreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.GraphFactory;

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

    @Option(names = { "--distinct" }, description = "Apply intra-query distinct", defaultValue = "false")
    public boolean distinct = false;

    @Option(names = { "--tm" }, description = "Only convert specific triple maps")
    public List<String> triplesMapIds = new ArrayList<>();

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

            // Extract the base IRI needed to succeed on test cases such as RMLTC0020a-CSV and RMLTC0020b-CSV
            String base = null;
            Graph graph = GraphFactory.createDefaultGraph();
            try (Stream<EltStreamRDF> stream = AsyncParser.of(inputFile).streamElements()) {
                Iterator<EltStreamRDF> it = stream.iterator();
                while (it.hasNext()) {
                    EltStreamRDF elt = it.next();
                    if (elt.isBase()) {
                        base = elt.iri();
                    } else if (elt.isTriple()) {
                        graph.add(elt.triple());
                    }
                }
            }
            Model model = ModelFactory.createModelForGraph(graph);

            // Model model = RDFDataMgr.loadModel(inputFile);
            Collection<TriplesMapToSparqlMapping> maps = RmlImporterLib.readSpecificOrAll(model, fnmlModel, triplesMapIds);

            // RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
            for (TriplesMapToSparqlMapping item : maps) {
                String tmId = NodeFmtLib.strNT(item.getTriplesMap().asNode());
                Query query = RmlQueryGenerator.createQuery(item, null);
                if (base != null) {
                    query.setBaseURI(base);
                }

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

        // TODO Ensure the variables aren't mentioned/visible in the query
        Quad quadVars = Quad.create(Var.alloc("__g__"), Var.alloc("__s__"), Var.alloc("__p__"), Var.alloc("__o__"));

        if (!noOptimize) { // If optimize ...
            List<Query> queries = labeledQueries.stream().map(Entry::getKey).collect(Collectors.toList());
            RmlLib.optimizeRmlWorkloadInPlace(queries);
            for (Query query : queries) {
                RmlLib.wrapServiceWithSubQueryInPlace(query);
                if (distinct) {
                    query = QueryGenerationUtils.constructToLateral(query, quadVars, QueryType.CONSTRUCT, distinct, false);
                }
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
