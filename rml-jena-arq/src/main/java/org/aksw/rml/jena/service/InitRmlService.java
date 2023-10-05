package org.aksw.rml.jena.service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
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
import org.aksw.r2rml.jena.domain.api.LogicalTable;
import org.aksw.r2rml.jena.jdbc.api.NodeMapper;
import org.aksw.r2rml.jena.jdbc.processor.R2rmlJdbcUtils;
import org.aksw.rml.jena.impl.NorseRmlTerms;
import org.aksw.rml.jena.impl.RmlLib;
import org.aksw.rml.model.LogicalSource;
import org.aksw.rml.model.QlTerms;
import org.aksw.rml.rso.model.SourceOutput;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
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

import com.github.jsonldjava.shaded.com.google.common.base.Preconditions;
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
                LogicalSource logicalSource = RmlLib.getOnlyLogicalSource(model);
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

    public static QueryIterator processSource(LogicalSource logicalSource, Binding parentBinding, ExecutionContext execCxt) {
        Map<String, RmlSourceProcessor> registry = new HashMap<>();
        registry.put(QlTerms.CSV, InitRmlService::processSourceAsCsv);
        registry.put(QlTerms.JSONPath, InitRmlService::processSourceAsJson);
        registry.put(QlTerms.XPath, InitRmlService::processSourceAsXml);
        registry.put(QlTerms.RDB, InitRmlService::processSourceAsJdbc);

        String iri = logicalSource.getReferenceFormulationIri();
        Preconditions.checkArgument(iri != null, "Reference formulation not specified on source. " + logicalSource);

        RmlSourceProcessor processor = registry.get(iri);
        Preconditions.checkArgument(processor != null, "No processor found for reference formulation: " + iri);

        QueryIterator result = processor.eval(logicalSource, parentBinding, execCxt);
        return result;
    }

    public static QueryIterator processSourceAsJdbc(LogicalSource logicalSource, Binding parentBinding, ExecutionContext execCxt) {
        SourceOutput output = logicalSource.as(SourceOutput.class);
        Var outVar = output.getOutputVar();

        // TODO Register data source with execCxt and reuse if present
        LogicalTable logicalTable = logicalSource.as(LogicalTable.class);
        D2rqDatabase dataSourceSpec = logicalSource.getSource().as(D2rqDatabase.class);

        SqlCodec sqlCodec = SqlCodecUtils.createSqlCodecForApacheSpark(); // SqlCodecUtils.createSqlCodecDefault();

        DataSource dataSource = D2rqHikariUtils.configureDataSource(dataSourceSpec);
        // Connection conn = dataSource.getConnection();

        NodeMapper nodeMapper = null; // create on demand TODO This is hacky
        IteratorCloseable<Binding> it;
        try {
            it = R2rmlJdbcUtils.processR2rml(dataSource, logicalTable, nodeMapper, sqlCodec);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Iter<Binding> it2 = Iter.iter(it).map(b -> {
            Binding bb = BindingFactory.copy(b); // The binding is just a view over the SQL result set - better copy
            Binding r = BindingFactory.binding(parentBinding, outVar, new NodeValueBinding(bb).asNode());
            return r;
        });

        return QueryIterPlainWrapper.create(it2, execCxt);
    }

    public static QueryIterator processSourceAsJson(LogicalSource logicalSource, Binding parentBinding, ExecutionContext execCxt) {
        String source = logicalSource.getSourceAsString();
        SourceOutput output = logicalSource.as(SourceOutput.class);

        Var outVar = output.getOutputVar();
        String iterator = logicalSource.getIterator();

        Gson gson = RDFDatatypeJson.get().getGson();
        JsonElement jsonElement;
        try (Reader reader = new InputStreamReader(JenaUrlUtils.openInputStream(NodeValue.makeString(source), execCxt), StandardCharsets.UTF_8)) {
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

    public static QueryIterator processSourceAsXml(LogicalSource logicalSource, Binding parentBinding, ExecutionContext execCxt) {
        String source = logicalSource.getSourceAsString();
        SourceOutput output = logicalSource.as(SourceOutput.class);

        Var outVar = output.getOutputVar();
        String iterator = logicalSource.getIterator();
        Preconditions.checkArgument(iterator != null, "rml:iterator (an XPath expresion string) must always be specified for XML sources");

        NodeValue xmlNv;
        try {
            xmlNv = JenaXmlUtils.resolve(NodeValue.makeString(source), execCxt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        XPathFactory xPathFactory = XPathFactory.newInstance();
        QueryIterator result = JenaXmlUtils.evalXPath(xPathFactory, parentBinding, execCxt,
                xmlNv.asNode(), NodeFactory.createLiteral(iterator), outVar);

        return result;
    }

    public static QueryIterator processSourceAsCsv(LogicalSource logicalSource, Binding parentBinding, ExecutionContext execCxt) {
        SourceOutput output = logicalSource.as(SourceOutput.class);

        Var[] headerVars = null;
        // Try to get the outputs as an RDF list (may raise an exception)
        try {
            List<Var> headerVarList = output.getOutputVars();
            headerVars = headerVarList == null ? null : headerVarList.toArray(new Var[0]);
        } catch (Throwable e) {
            // Ignore
        }
        Var[] finalHeaderVars = headerVars;

        Var jsonVar = output.getOutputVar();

        if (jsonVar == null && headerVars == null) {
            throw new RuntimeException("No output specified");
        }

        RDFNode source = logicalSource.getSource();
        String sourceDoc;
        DialectMutable effectiveDialect = new DialectMutableImpl();
        String[] nullValues = null;
        if (source.isLiteral()) {
            sourceDoc = logicalSource.getSourceAsString();
        } else {
            Table csvwtSource = source.as(Table.class);

            Dialect dialect = csvwtSource.getDialect();
            if (dialect != null) {
                dialect.copyInto(effectiveDialect, false);
            }
            Set<String> nullSet = csvwtSource.getNull();
            if (nullSet != null && !nullSet.isEmpty()) {
                nullValues = nullSet.toArray(new String[0]);
            }
            sourceDoc = csvwtSource.getUrl();
        }
        Callable<InputStream> inSupp = () -> JenaUrlUtils.openInputStream(NodeValue.makeString(sourceDoc), execCxt);

        UnivocityCsvwConf csvConf = new UnivocityCsvwConf(effectiveDialect, nullValues);
        UnivocityParserFactory parserFactory = UnivocityParserFactory
                .createDefault(true)
                .configure(csvConf);
        QueryIterator result;

        // FIXME If the output var is bound to a constant then filter the source to that value

        boolean jsonMode = finalHeaderVars == null;
        if (jsonMode) {
            Stream<JsonObject> stream = UnivocityUtils.readCsvElements(inSupp, parserFactory, parser -> {
                String[] row = parser.parseNext();
                JsonObject r = null;
                if (row != null) {
                    RecordMetaData meta = parser.getRecordMetadata();
                    String[] headers = meta.headers();
                    r = csvRecordToJsonObject(row, headers);
                }
                return r;
            });
            result = QueryExecUtils.fromStream(stream, jsonVar, parentBinding, execCxt, JenaJsonUtils::createLiteralByValue);
        } else {
            Stream<Binding> stream = UnivocityUtils.readCsvElements(inSupp, parserFactory, parser -> {
                String[] row = parser.parseNext();
                Binding r = csvRecordToBinding(parentBinding, row, finalHeaderVars);
                return r;
            });
            result = QueryExecUtils.fromStream(stream, execCxt);
        }
        return result;
    }

    public static Binding csvRecordToBinding(Binding parent, String[] row, Var[] vars) {
        BindingBuilder bb = Binding.builder(parent);
        for(int i = 0; i < row.length; ++i) {
            String value = row[i];
            if (value != null) {
                Node node = NodeFactory.createLiteral(value);
                Var var = vars != null && i < vars.length ? vars[i] : null;
                if (var == null) {
                    var = Var.alloc("col" + i);
                }
                bb.add(var, node);
            }
        }
        return bb.build();
    }

    public static JsonObject csvRecordToJsonObject(String[] row, String[] labels) {
        JsonObject obj = new JsonObject();
        for(int i = 0; i < row.length; ++i) {
            String value = row[i];

            String label = labels != null && i < labels.length ? labels[i] : null;
            label = label == null ? "" + "col" + i : label;

            obj.addProperty(label, value);
        }
        return obj;
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
