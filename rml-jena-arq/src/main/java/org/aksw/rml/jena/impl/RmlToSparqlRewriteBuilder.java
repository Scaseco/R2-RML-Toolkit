package org.aksw.rml.jena.impl;

import java.io.ByteArrayInputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jenax.arq.util.syntax.QueryGenerationUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.r2rml.jena.arq.impl.JoinDeclaration;
import org.aksw.r2rml.jena.arq.impl.TriplesMapToSparqlMapping;
import org.aksw.rml.jena.plugin.ReferenceFormulationRegistry;
import org.aksw.rml.jena.plugin.ReferenceFormulationService;
import org.aksw.rml.model.LogicalSourceRml1;
import org.aksw.rml.model.TriplesMapRml1;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
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

/**
 * A class to rewrite RML inputs to SPARQL.
 */
public class RmlToSparqlRewriteBuilder {

    protected static record Input(String file, Model model, String baseIri) {}

    // protected List<String> fnmlFiles = new ArrayList<>();
    protected ReferenceFormulationService registry = null;
    protected boolean denormalize = false;
    protected boolean merge = false;
    protected boolean cache = false;
    protected boolean distinct = false;
    protected boolean preDistinct = false;
    protected List<String> triplesMapIds = new ArrayList<>();
    // protected List<String> inputFiles = new ArrayList<>();
    protected List<RmlToSparqlRewriteBuilder.Input> modelAndBaseIriList = new ArrayList<>();

    protected Model fnmlModel;

    // protected Collection<TriplesMapToSparqlMapping> maps;

    protected int globalQueryId = 0;

    public RmlToSparqlRewriteBuilder() {
        this.fnmlModel = ModelFactory.createDefaultModel();
    }

    public ReferenceFormulationService getRegistry() {
        return registry;
    }

    public RmlToSparqlRewriteBuilder setRegistry(ReferenceFormulationService registry) {
        this.registry = registry;
        return this;
    }

    public boolean isDenormalize() {
        return denormalize;
    }

    public RmlToSparqlRewriteBuilder setDenormalize(boolean denormalize) {
        this.denormalize = denormalize;
        return this;
    }

    public boolean isMerge() {
        return merge;
    }

    public RmlToSparqlRewriteBuilder setMerge(boolean merge) {
        this.merge = merge;
        return this;
    }

    public boolean isCache() {
        return cache;
    }

    public RmlToSparqlRewriteBuilder setCache(boolean cache) {
        this.cache = cache;
        return this;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public RmlToSparqlRewriteBuilder setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    public boolean isPreDistinct() {
        return preDistinct;
    }

    public RmlToSparqlRewriteBuilder setPreDistinct(boolean preDistinct) {
        this.preDistinct = preDistinct;
        return this;
    }

    public RmlToSparqlRewriteBuilder addFnmlFiles(Collection<String> fnmlFiles) {
        for (String fnmlFile : fnmlFiles) {
            addFnmlFile(fnmlFile);
        }
        return this;
    }

    public RmlToSparqlRewriteBuilder addFnmlFile(String fnmlFile) {
        Model model = RDFDataMgr.loadModel(fnmlFile);
        fnmlModel.add(model);
        return this;
    }

    public RmlToSparqlRewriteBuilder addFnmlModel(Model contrib) {
        fnmlModel.add(contrib);
        return this;
    }

    public RmlToSparqlRewriteBuilder addRmlString(String str) {
        Input input = processInput("inline string",
                () -> AsyncParser.of(new ByteArrayInputStream(str.getBytes()), Lang.TURTLE, null).streamElements());
        modelAndBaseIriList.add(input);
        return this;
    }

    public RmlToSparqlRewriteBuilder addRmlFiles(Collection<String> rmlFiles) {
        for (String rmlFile : rmlFiles) {
            addRmlFile(rmlFile);
        }
        return this;
    }

    public RmlToSparqlRewriteBuilder addRmlFile(String rmlFile) {
        // Model model = RDFDataMgr.loadModel(rmlFile);
        Input input = processInput(rmlFile, () -> AsyncParser.of(rmlFile).streamElements());
        modelAndBaseIriList.add(input);
        return this;
    }

    public RmlToSparqlRewriteBuilder addRmlModel(Model contrib) {
        modelAndBaseIriList.add(new Input(null, contrib, null));
        return this;
    }

    public static Input processInput(String inputLabel, Supplier<Stream<EltStreamRDF>> streamSupplier) {

        // Extract the base IRI needed to succeed on test cases such as RMLTC0020a-CSV and RMLTC0020b-CSV
        String base = null;
        Graph graph = GraphFactory.createDefaultGraph();
        try (Stream<EltStreamRDF> stream = streamSupplier.get()) {
            Iterator<EltStreamRDF> it = stream.iterator();
            while (it.hasNext()) {
                EltStreamRDF elt = it.next();
                if (elt.isBase()) {
                    base = elt.iri();
                } else if (elt.isTriple()) {
                    graph.add(elt.triple());
                } else if (elt.isException()) {
                    throw new RuntimeException("Failed to process input " + inputLabel, elt.exception());
                }
            }
        }

        Model model = ModelFactory.createModelForGraph(graph);

        // modelAndBaseIriList.add(new Input(inputFile, model, base));
        return new Input(inputLabel, model, base);
    }


    public List<Entry<Query, String>> generate() {

        ReferenceFormulationService finalRegistry = registry != null
                ? registry
                : ReferenceFormulationRegistry.get();

        if (cache) {
            // XXX We may eventually want to add a generic SPARQL transformer to inject cache operations
            ReferenceFormulationService tmp = finalRegistry;
            finalRegistry = iri -> {
                ReferenceFormulation rf = tmp.getOrThrow(iri);
                return new ReferenceFormulationWrapper(rf) {
                    @Override
                    public Element source(LogicalSourceRml1 source, Var sourceVar) {
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

        List<Entry<Query, String>> labeledQueries = new ArrayList<>();

        boolean pushDistinct = false;

        for (RmlToSparqlRewriteBuilder.Input input : modelAndBaseIriList) {
            Model model = input.model();
            String base = input.baseIri();

            // Model model = RDFDataMgr.loadModel(inputFile);
            Collection<TriplesMapToSparqlMapping> maps = RmlImporterLib.readSpecificOrAll(TriplesMapRml1.class, model, fnmlModel, triplesMapIds, null);

            // RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
            for (TriplesMapToSparqlMapping item : maps) {
                String tmId = NodeFmtLib.strNT(item.getTriplesMap().asNode());
                List<Query> queries;
                if (denormalize) {
                    queries = List.of(RmlQueryGenerator.createQuery(item, finalRegistry));
                } else {
                    queries = RmlQueryGenerator.createCanonicalQueries(item, pushDistinct, finalRegistry);
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
                    Query joinQuery = RmlQueryGenerator.createQuery(join, preDistinct, finalRegistry);
                    QueryUtils.optimizePrefixes(joinQuery);
                    labeledQueries.add(Map.entry(joinQuery, "# " + (globalQueryId++) + ": " + tmId + " -> " + NodeFmtLib.strNT(join.getParentTriplesMap().asNode())));
                }
            }
        }

        // TODO Ensure the variables aren't mentioned/visible in the query
        Quad quadVars = Quad.create(Var.alloc("__g__"), Var.alloc("__s__"), Var.alloc("__p__"), Var.alloc("__o__"));

        List<Entry<Query, String>> result = new ArrayList<>();

        if (merge) { // If merge ...
            List<Query> queries = labeledQueries.stream().map(Entry::getKey).collect(Collectors.toList());
            RmlLib.optimizeRmlWorkloadInPlace(queries);
            for (Query query : queries) {
                RmlLib.wrapServiceWithSubQueryInPlace(query);
                if (distinct) {
                    query = QueryGenerationUtils.constructToLateral(query, quadVars, QueryType.CONSTRUCT, distinct, false);
                }
                // System.out.println(query);
                result.add(new SimpleEntry<>(query, null));
            }
        } else {
            // Without optimization we can output a comment about the origin of a query
            for (Entry<Query, String> entry : labeledQueries) {
                RmlLib.wrapServiceWithSubQueryInPlace(entry.getKey());

                result.add(entry);
//                    System.out.println(entry.getValue());
//                    System.out.println(entry.getKey());
            }
        }

        return labeledQueries;
    }
}
