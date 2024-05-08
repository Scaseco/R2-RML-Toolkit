package org.aksw.rml.jena.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import javax.sql.DataSource;
import javax.xml.xpath.XPathFactory;

import org.aksw.commons.jena.graph.GraphVarImpl;
import org.aksw.commons.model.csvw.domain.api.DialectMutable;
import org.aksw.commons.model.csvw.domain.impl.DialectMutableImpl;
import org.aksw.commons.model.csvw.univocity.UnivocityCsvwConf;
import org.aksw.commons.model.csvw.univocity.UnivocityParserFactory;
import org.aksw.commons.model.csvw.univocity.UnivocityUtils;
import org.aksw.commons.sql.codec.api.SqlCodec;
import org.aksw.commons.sql.codec.util.SqlCodecUtils;
import org.aksw.jena_sparql_api.sparql.ext.binding.NodeValueBinding;
import org.aksw.jena_sparql_api.sparql.ext.json.JenaJsonUtils;
import org.aksw.jena_sparql_api.sparql.ext.json.RDFDatatypeJson;
import org.aksw.jena_sparql_api.sparql.ext.url.JenaUrlUtils;
import org.aksw.jena_sparql_api.sparql.ext.xml.JenaXmlUtils;
import org.aksw.jenax.arq.util.exec.query.QueryExecUtils;
import org.aksw.jenax.arq.util.security.ArqSecurity;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.model.csvw.domain.api.Dialect;
import org.aksw.jenax.model.csvw.domain.api.Table;
import org.aksw.jenax.model.d2rq.domain.api.D2rqDatabase;
import org.aksw.r2rml.jena.jdbc.api.NodeMapper;
import org.aksw.r2rml.jena.jdbc.processor.R2rmlJdbcUtils;
import org.aksw.rml.jena.impl.RmlLib;
import org.aksw.rml.model.QlTerms;
import org.aksw.rml.rso.model.SourceOutput;
import org.aksw.rml.v2.common.vocab.RmlIoTerms;
import org.aksw.rml.v2.io.RelativePathSource;
import org.aksw.rmltk.model.backbone.rml.ILogicalSource;
import org.aksw.rmltk.model.r2rml.LogicalTable;
import org.aksw.rmlx.model.NorseRmlTerms;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.vocabulary.XSD;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteSource;
import com.google.common.io.MoreFiles;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.univocity.parsers.common.record.RecordMetaData;

public class InitRmlService {

    public static void registerServiceRmlSource(ServiceExecutorRegistry registry) {
        registry.addSingleLink((opExecute, opOriginal, binding, execCxt, chain) -> {
            QueryIterator r;
            Node serviceNode = opExecute.getService();
            if (serviceNode.isURI() && serviceNode.getURI().equals(NorseRmlTerms.RML_SOURCE_SERVICE_IRI)) {
                Op subOp = opExecute.getSubOp();
                Query query = OpAsQuery.asQuery(subOp);
                Element elt = query.getQueryPattern();
                // GraphVar treats variables as constants - so we can navigate the triples of variables
                Graph graph = new GraphVarImpl();
                ElementUtils.toGraph(elt, graph);
                Model model = ModelFactory.createModelForGraph(graph);
                // RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
                ILogicalSource logicalSource = RmlLib.getOnlyLogicalSource(model);
                r = processSource(logicalSource, binding, execCxt);
            } else {
                return chain.createExecution(opExecute, opOriginal, binding, execCxt);
            }
            return r;
        });
    }

//    public static QueryIterator parseCsvAsJson(LogicalSource logicalSource, Binding parentBinding, ExecutionContext execCxt) {
//
//    }

//    public static QueryIterator parseCsvAsJson(LogicalSource logicalSource, Var outVar, Binding parentBinding, ExecutionContext execCxt) {
//
//        Stream<JsonObject> stream = parseCsvAsJson(logicalSource, execCxt);
//        return QueryExecUtils.fromStream(stream, outVar, parentBinding, execCxt, RDFDatatypeJson::jsonToNode);
//    }

    public static void initRegistryRml1(Map<String, RmlSourceProcessor> registry) {
        registry.put(QlTerms.CSV, RmlSourceProcessorCsv.getInstance());
        registry.put(QlTerms.JSONPath, InitRmlService::processSourceAsJson);
        registry.put(QlTerms.XPath, InitRmlService::processSourceAsXml);
        registry.put(QlTerms.RDB, RmlSourceProcessorD2rqDatabase.getInstance());
    }

    public static void initRegistryRml2(Map<String, RmlSourceProcessor> registry) {
        registry.put(RmlIoTerms.SQL2008Query, RmlSourceProcessorD2rqDatabase.getInstance());
        registry.put(RmlIoTerms.SQL2008Table, RmlSourceProcessorD2rqDatabase.getInstance());
        registry.put(RmlIoTerms.JSONPath, InitRmlService::processSourceAsJson);
        registry.put(RmlIoTerms.XPath, InitRmlService::processSourceAsXml);
        registry.put(RmlIoTerms.CSV, RmlSourceProcessorCsv.getInstance());
    }


    public static QueryIterator processSource(ILogicalSource logicalSource, Binding parentBinding, ExecutionContext execCxt) {
        Map<String, RmlSourceProcessor> registry = new HashMap<>();
        initRegistryRml1(registry);
        initRegistryRml2(registry);

        String iri = logicalSource.getReferenceFormulationIri();
        Preconditions.checkArgument(iri != null, "Reference formulation not specified on source. " + logicalSource);

        RmlSourceProcessor processor = registry.get(iri);
        Preconditions.checkArgument(processor != null, "No processor found for reference formulation: " + iri);

        QueryIterator result = processor.eval(logicalSource, parentBinding, execCxt);
        return result;
    }

    public static ByteSource asByteSource(RelativePathSource source, ExecutionContext execCxt) {
        // TODO Get some resolver from the context
        Path basePath = null;
        Context cxt = execCxt.getContext();
        Object obj = cxt.get(RmlSymbols.symMappingDirectory);
        if (obj == null) {
            // Resolve to the current directory
            basePath = Path.of("").toAbsolutePath();
            // throw new NullPointerException("MappingDirectory is null in the context");
        } else if (obj instanceof Path) {
            basePath = (Path)obj;
        } else if (obj instanceof String) {
            String str = (String)obj;
            basePath = Path.of(str);
        } else {
            throw new IllegalArgumentException("Don't know how to handle: " + obj);
        }

        String pathStr = source.getPath();
        if (pathStr == null) {
            throw new NullPointerException("No path provided");
        }

        Path finalPath = basePath.resolve(pathStr);
        ByteSource result = MoreFiles.asByteSource(finalPath);
        return result;
    }


    // TODO Resolve to an inputstream supplier?
    public static ByteSource newByteSource(ILogicalSource logicalSource, Binding parentBinding, ExecutionContext execCxt) {
        ByteSource result;
        RDFNode source = logicalSource.getSource();

        if (source == null) {
            throw new NullPointerException("No source specified (got null)");
        } else if (source.isLiteral()) {
            Literal literal = source.asLiteral();
            String datatypeUri = literal.getDatatypeURI();
            if (datatypeUri.equals(XSD.xstring.getURI())) {
                // String sourceStr = literal.getString();
                // result = JenaUrlUtils.openInputStream(literal.asNode(), execCxt);
                result = new ByteSource() {
                    @Override
                    public InputStream openStream() throws IOException {
                        try {
                            return JenaUrlUtils.openInputStream(NodeValue.makeNode(source.asNode()), execCxt);
                        } catch (Exception e) {
                            throw new IOException(e);
                        }
                    }
                };
            } else {
                throw new RuntimeException("Unsupported literal type: " + datatypeUri + " - " + literal);
            }
        } else if (source.isResource()) {
            RelativePathSource r = source.as(RelativePathSource.class);
            result = asByteSource(r, execCxt);
        } else {
            throw new RuntimeException("Unsupported logical source: " + logicalSource);
        }

        return result;
    }

    public static QueryIterator processSourceAsJson(ILogicalSource logicalSource, Binding parentBinding, ExecutionContext execCxt) {
        ByteSource byteSource = newByteSource(logicalSource, parentBinding, execCxt);

        // String source = logicalSource.getSourceAsString();
        SourceOutput output = logicalSource.as(SourceOutput.class);

        Var outVar = output.getOutputVar();
        String iterator = logicalSource.getIterator();

        Gson gson = RDFDatatypeJson.get().getGson();
        JsonElement jsonElement;

        try (Reader reader = byteSource.asCharSource(StandardCharsets.UTF_8).openStream()) {
            jsonElement = gson.fromJson(reader, JsonElement.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (!jsonElement.isJsonArray()) {
            Preconditions.checkArgument(iterator != null, "rr:iterator must be specified for non-array json sources");
        }

        Node jsonNode = NodeFactory.createLiteralByValue(jsonElement, RDFDatatypeJson.get());
        NodeValue nv = NodeValue.makeNode(jsonNode);
        NodeValue arr = iterator == null
                ? nv
                : JenaJsonUtils.evalJsonPath(gson, nv, NodeValue.makeString(iterator));

        QueryIterator result = JenaJsonUtils.unnestJsonArray(gson, parentBinding, null, execCxt, arr.asNode(), outVar);
        return result;
    }

    public static QueryIterator processSourceAsXml(ILogicalSource logicalSource, Binding parentBinding, ExecutionContext execCxt) {
        ByteSource byteSource = newByteSource(logicalSource, parentBinding, execCxt);

        SourceOutput output = logicalSource.as(SourceOutput.class);

        Var outVar = output.getOutputVar();
        String iterator = logicalSource.getIterator();
        Preconditions.checkArgument(iterator != null, "rml:iterator (an XPath expresion string) must always be specified for XML sources");

        NodeValue xmlNv;
        try {
            xmlNv = JenaXmlUtils.parse(byteSource::openStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        XPathFactory xPathFactory = XPathFactory.newInstance();
        QueryIterator result = JenaXmlUtils.evalXPath(xPathFactory, parentBinding, execCxt,
                xmlNv.asNode(), NodeFactory.createLiteralString(iterator), outVar);

        return result;
    }


    public static void main(String[] args) {
        try (QueryExec qe = QueryExec.newBuilder()
            .graph(GraphFactory.createDefaultGraph())
            .set(ArqSecurity.symAllowFileAccess, true)
            .query(String.join("\n",
                "PREFIX rml: <http://semweb.mmlab.be/ns/rml#>",
                "PREFIX ql: <http://semweb.mmlab.be/ns/ql#>",
                "PREFIX fno: <https://w3id.org/function/ontology#>",
                "SELECT * {",
                "  SERVICE <rml.source:> {[",
                "    rml:source '/home/raven/Repositories/coypu-data-sources/world_bank/target/clean/Metadata_Indicator_API_9_DS2_en_csv_v2_4775410.csv' ;",
                "    rml:referenceFormulation ql:CSV ;",
                "    fno:returns (?x ?y)",
                "  ]}",
                "} LIMIT 3"
            ))
            .build()) {
            System.out.println(ResultSetFormatter.asText(ResultSet.adapt(qe.select())));
        }

        try (QueryExec qe = QueryExec.newBuilder()
            .graph(GraphFactory.createDefaultGraph())
            .set(ArqSecurity.symAllowFileAccess, true)
            .query(String.join("\n",
                "PREFIX rml: <http://semweb.mmlab.be/ns/rml#>",
                "PREFIX ql: <http://semweb.mmlab.be/ns/ql#>",
                "PREFIX fno: <https://w3id.org/function/ontology#>",
                "SELECT * {",
                "  SERVICE <rml.source:> {[",
                "    rml:source '/home/raven/Projects/Eclipse/sansa-stack-parent/pom.xml' ;",
                "    rml:referenceFormulation ql:XPath ;",
                "    rml:iterator '//:dependency' ;",
                "    fno:returns ?x",
                "  ]}",
                "} LIMIT 3"
            ))
            .build()) {
            System.out.println(ResultSetFormatter.asText(ResultSet.adapt(qe.select())));
        }

        try (QueryExec qe = QueryExec.newBuilder()
                .graph(GraphFactory.createDefaultGraph())
                .set(ArqSecurity.symAllowFileAccess, true)
                .query(String.join("\n",
                    "PREFIX rml: <http://semweb.mmlab.be/ns/rml#>",
                    "PREFIX ql: <http://semweb.mmlab.be/ns/ql#>",
                    "PREFIX fno: <https://w3id.org/function/ontology#>",
                    "SELECT * {",
                    "  SERVICE <rml.source:> {[",
                    "    rml:source '/home/raven/Repositories/aksw-jena/jena-arq/testing/ResultSet/rs-datatype-string.srj' ;",
                    "    rml:referenceFormulation ql:JSONPath ;",
                    "    rml:iterator '$..type' ;",
                    "    fno:returns ?x",
                    "  ]}",
                    "} LIMIT 3"
                ))
                .build()) {
                System.out.println(ResultSetFormatter.asText(ResultSet.adapt(qe.select())));
            }
    }
}
