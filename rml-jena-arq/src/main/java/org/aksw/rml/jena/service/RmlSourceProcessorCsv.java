package org.aksw.rml.jena.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.commons.model.csvw.domain.api.DialectMutable;
import org.aksw.commons.model.csvw.domain.impl.DialectMutableImpl;
import org.aksw.commons.model.csvw.univocity.UnivocityCsvwConf;
import org.aksw.commons.model.csvw.univocity.UnivocityParserFactory;
import org.aksw.commons.model.csvw.univocity.UnivocityUtils;
import org.aksw.jena_sparql_api.sparql.ext.json.JenaJsonUtils;
import org.aksw.jena_sparql_api.sparql.ext.url.JenaUrlUtils;
import org.aksw.jenax.arq.util.exec.query.QueryExecUtils;
import org.aksw.jenax.model.csvw.domain.api.Dialect;
import org.aksw.jenax.model.csvw.domain.api.Table;
import org.aksw.rml.rso.model.SourceOutput;
import org.aksw.rml.v2.io.RelativePathSource;
import org.aksw.rmltk.model.backbone.rml.ILogicalSource;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.expr.NodeValue;

import com.google.common.io.ByteSource;
import com.google.gson.JsonObject;
import com.univocity.parsers.common.record.RecordMetaData;

public class RmlSourceProcessorCsv
    implements RmlSourceProcessor
{
    private static final RmlSourceProcessorCsv INSTANCE = new RmlSourceProcessorCsv();

    public static RmlSourceProcessorCsv getInstance() {
        return INSTANCE;
    }

    @Override
    public QueryIterator eval(ILogicalSource logicalSource, Binding parentBinding, ExecutionContext execCxt) {
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

        ByteSource byteSource = null;
        RDFNode source = logicalSource.getSource();
        String sourceDoc;
        DialectMutable effectiveDialect = new DialectMutableImpl();
        String[] nullValues = null;
        if (source.isLiteral()) {
            sourceDoc = logicalSource.getSourceAsString();
        } else { // source is a resource
            // First try to interpret the source as a CSVW model
            // If that fails then try to interpret as RML2
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

            // Try RML 2
            if (sourceDoc == null) {
                RelativePathSource rps = csvwtSource.as(RelativePathSource.class);
                if (rps.getPath() != null) {
                    byteSource = InitRmlService.asByteSource(rps, execCxt);
                }
            }
        }

        if (sourceDoc != null && byteSource == null) {
            //LogicalTable logicalTable = toLogicalTable(logicalSource);
            byteSource = new ByteSource() {
                @Override
                public InputStream openStream() throws IOException {
                    try {
                        return JenaUrlUtils.openInputStream(NodeValue.makeString(sourceDoc), execCxt);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }


        Objects.requireNonNull(byteSource, "Could not create a byte source from the model");
        // Callable<InputStream> inSupp = () -> JenaUrlUtils.openInputStream(NodeValue.makeString(sourceDoc), execCxt);

        UnivocityCsvwConf csvwConf = new UnivocityCsvwConf(effectiveDialect, nullValues);
        UnivocityParserFactory parserFactory = UnivocityParserFactory
                .createDefault(true);

        parserFactory.getCsvSettings().setDelimiterDetectionEnabled(true, ',', ';', '|');

        // FIXME Configuring from csvwConf may yet need to disable auto detection (delimiters, terminators, quotes)
        //       if explicit values are provided via csvw
        parserFactory.configure(csvwConf);

        QueryIterator result;

        // FIXME If the output var is bound to a constant then filter the source to that value

        boolean jsonMode = finalHeaderVars == null;
        if (jsonMode) {
            Stream<JsonObject> stream = UnivocityUtils.readCsvElements(byteSource::openStream, parserFactory, parser -> {
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
            Stream<Binding> stream = UnivocityUtils.readCsvElements(byteSource::openStream, parserFactory, parser -> {
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
                Node node = NodeFactory.createLiteralString(value);
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
}
