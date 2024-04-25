package org.aksw.rml.cli.cmd;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.io.util.FileUtils;
import org.aksw.jenax.arq.picocli.CmdMixinOutput;
import org.aksw.jenax.arq.util.syntax.QueryGenerationUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.r2rml.jena.arq.impl.JoinDeclaration;
import org.aksw.r2rml.jena.arq.impl.TriplesMapToSparqlMapping;
import org.aksw.rml.jena.impl.ReferenceFormulation;
import org.aksw.rml.jena.impl.ReferenceFormulationWrapper;
import org.aksw.rml.jena.impl.RmlImporterLib;
import org.aksw.rml.jena.impl.RmlLib;
import org.aksw.rml.jena.impl.RmlQueryGenerator;
import org.aksw.rml.jena.impl.RmlToSparqlRewriteBuilder;
import org.aksw.rml.jena.plugin.ReferenceFormulationRegistry;
import org.aksw.rml.jena.plugin.ReferenceFormulationService;
import org.aksw.rml.model.LogicalSource;
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
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementSubQuery;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


@Command(name = "sparql", description = "Convert RML mappings to corresponding SPARQL queries")
public class CmdRmlTkRmlToSparql
//     extends CmdCommonBase
    implements Callable<Integer>
{
    @Option(names = { "--fnml" }, description = "Function Mapping Language models")
    public List<String> fnmlFiles = new ArrayList<>();

    @Option(names = { "--denorm" }, description = "Allow creation of denormalized CONSTRUCT queries. Such queries may have more than one quad in their template.", defaultValue = "false")
    public boolean denormalize = false;

    @Option(names = { "--merge" }, description = "Merge queries that have the same logical source. Prevents future partitioning and optimization.", defaultValue = "false")
    public boolean merge = false;

    @Option(names = { "--cache" }, description = "Add cache operators around all sources", defaultValue = "false")
    public boolean cache = false;

    @Option(names = { "--distinct" }, description = "Experimental. Apply intra-query distinct", defaultValue = "false")
    public boolean distinct = false;

    @Option(names = { "--pre-distinct" }, description = "Experimental. Whenever DISTINCT is applied to the outcome of a UNION then also apply distinct to each member first.", defaultValue = "false")
    public boolean preDistinct = false;

    @Option(names = { "--tm" }, description = "Only convert specific triple maps whose IRIs match the given as arguments")
    public List<String> triplesMapIds = new ArrayList<>();

    @Mixin
    public CmdMixinOutput outputConfig = new CmdMixinOutput();

    @Parameters(arity = "1..n", description = "Input RML file(s)")
    public List<String> inputFiles = new ArrayList<>();

    @Override
    public Integer call() throws Exception {
        RmlToSparqlRewriteBuilder builder = new RmlToSparqlRewriteBuilder()
                .setCache(cache)
                .addFnmlFiles(fnmlFiles)
                .addRmlFiles(inputFiles)
                .setDenormalize(denormalize)
                .setMerge(merge)
                ;

        List<Entry<Query, String>> labeledQueries = builder.generate();

        // XXX Validate that the output format refers to something meaningful
        try (PrintStream out = new PrintStream(FileUtils.newOutputStream(outputConfig), false, StandardCharsets.UTF_8)) {
            write(out, labeledQueries);
        }

        return 0;
    }

    public static void write(PrintStream ps, List<Entry<Query, String>> labeledQueries) {
        for (Entry<Query, String> entry : labeledQueries) {
            String label = entry.getValue();
            if (label != null) {
                ps.println(label);
            }
            ps.println(entry.getKey());
        }
    }

    // @Override
    public Integer callOld() throws Exception {
        ReferenceFormulationService registry = ReferenceFormulationRegistry.get();

        if (cache) {
            // XXX We may eventually want to add a generic SPARQL transformer to inject cache operations
            ReferenceFormulationService tmp = registry;
            registry = iri -> {
                ReferenceFormulation rf = tmp.getOrThrow(iri);
                return new ReferenceFormulationWrapper(rf) {
                    @Override
                    public Element source(LogicalSource source, Var sourceVar) {
                        Element baseElt = delegate.source(source, sourceVar);
                        Query q = new Query();
                        q.setQuerySelectType();
                        q.getProject().add(sourceVar);
                        q.setQueryPattern(baseElt);
                        Element r = new ElementService("cache:", new ElementSubQuery(q));
                        return r;
                    }
                };
            };
        }


        Model fnmlModel = ModelFactory.createDefaultModel();
        for (String fnmlFile : fnmlFiles) {
            Model model = RDFDataMgr.loadModel(fnmlFile);
            fnmlModel.add(model);
        }

        int globalQueryId = 0;
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
                    } else if (elt.isException()) {
                        throw new RuntimeException("Failed to process input " + inputFile, elt.exception());
                    }
                }
            }
            Model model = ModelFactory.createModelForGraph(graph);

            // Model model = RDFDataMgr.loadModel(inputFile);
            Collection<TriplesMapToSparqlMapping> maps = RmlImporterLib.readSpecificOrAll(model, fnmlModel, triplesMapIds, null);


            boolean pushDistinct = false;

            // RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
            for (TriplesMapToSparqlMapping item : maps) {
                String tmId = NodeFmtLib.strNT(item.getTriplesMap().asNode());
                List<Query> queries;
                if (denormalize) {
                    queries = List.of(RmlQueryGenerator.createQuery(item, registry));
                } else {
                    queries = RmlQueryGenerator.createCanonicalQueries(item, pushDistinct, registry);
                }

                // Query query = RmlQueryGenerator.createQuery(item, null);
                int queryIdInTriplesMap = 1;
                for (Query query : queries) {
                    if (base != null) {
                        query.setBaseURI(base);
                    }

                    // Do not emit queries that do not produce anything (e.g. if there are only RefObjectMaps)
                    if (!query.getConstructTemplate().getQuads().isEmpty()) {
                        QueryUtils.optimizePrefixes(query);
                        labeledQueries.add(Map.entry(query, "# " + (globalQueryId++) + ": " + tmId + " (" + (queryIdInTriplesMap++) + "/" + queries.size() + ")"));
                    }
                }
                for (JoinDeclaration join : item.getJoins()) {
                    Query joinQuery = RmlQueryGenerator.createQuery(join, preDistinct, registry);
                    QueryUtils.optimizePrefixes(joinQuery);
                    labeledQueries.add(Map.entry(joinQuery, "# " + (globalQueryId++) + ": " + tmId + " -> " + NodeFmtLib.strNT(join.getParentTriplesMap().asNode())));
                }
            }
        }

        // TODO Ensure the variables aren't mentioned/visible in the query
        Quad quadVars = Quad.create(Var.alloc("__g__"), Var.alloc("__s__"), Var.alloc("__p__"), Var.alloc("__o__"));

        if (merge) { // If merge ...
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
                RmlLib.wrapServiceWithSubQueryInPlace(entry.getKey());

                System.out.println(entry.getValue());
                System.out.println(entry.getKey());
            }
        }

        for (Entry<Query, String> entry : labeledQueries) {
            String label = entry.getValue();
            if (label != null) {
                System.out.println(label);
            }
            System.out.println(entry.getKey());
        }

        return 0;
    }


//    public static class Input<I, O> {
//    	protected I specification;
//    	protected O value;
//		public Input(I specification, O value) {
//			super();
//			this.specification = specification;
//			this.value = value;
//		}
//
//
//
//    }


}
