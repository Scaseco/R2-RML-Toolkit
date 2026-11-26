package org.aksw.rml.jena.impl;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map.Entry;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * rml:languageMap of RML-Core (test cases RMLTC0031a-c, kg-construct/rml-core): the
 * term map that generates the language tag of a literal.
 */
public class TestRmlLanguageMap {

    static final String MAPPING = String.join("\n",
        "@prefix rml: <http://w3id.org/rml/> .",
        "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .",
        "<http://example.com/base/TriplesMap1> a rml:TriplesMap;",
        "  rml:logicalSource [ a rml:LogicalSource; rml:iterator \"$[*]\"; rml:referenceFormulation rml:JSONPath;",
        "      rml:source [ a rml:RelativePathSource; rml:root rml:CurrentWorkingDirectory; rml:path \"%s\" ] ];",
        "  rml:subjectMap [ rml:template \"http://example.com/{$.id}\" ];",
        "  rml:predicateObjectMap [ rml:predicate rdfs:label;",
        "      rml:objectMap [ rml:reference \"$.label\"; rml:languageMap [ %s ] ] ] .");

    static List<Literal> labels(String languageMap) throws Exception {
        Path json = Files.createTempFile("labels", ".json");
        try {
            Files.writeString(json, "[ { \"id\": 10, \"label\": \"apple\", \"lang\": \"en\" } ]", StandardCharsets.UTF_8);
            Model rml = ModelFactory.createDefaultModel();
            rml.read(new StringReader(String.format(MAPPING, json.toAbsolutePath(), languageMap)), null, "TURTLE");
            List<Entry<Query, String>> queries = new RmlToSparqlRewriteBuilder()
                    .addRmlModel(org.aksw.rml.v2.jena.domain.api.TriplesMapRml2.class, rml)
                    .generate();
            Model actual = ModelFactory.createDefaultModel();
            for (Entry<Query, String> entry : queries) {
                actual.add(QueryExecutionFactory.create(entry.getKey(), ModelFactory.createDefaultModel()).execConstruct());
            }
            return actual.listObjects().filterKeep(o -> o.isLiteral()).mapWith(o -> o.asLiteral()).toList();
        } finally {
            Files.deleteIfExists(json);
        }
    }

    @Test
    public void testConstantLanguageMap() throws Exception {
        List<Literal> labels = labels("rml:constant \"en\"");
        Assert.assertEquals(1, labels.size());
        Assert.assertEquals("apple", labels.get(0).getLexicalForm());
        Assert.assertEquals("en", labels.get(0).getLanguage());
    }

    @Test
    public void testReferenceLanguageMap() throws Exception {
        List<Literal> labels = labels("rml:reference \"$.lang\"");
        Assert.assertEquals(1, labels.size());
        Assert.assertEquals("en", labels.get(0).getLanguage());
    }
}
