package org.aksw.rml.jena.impl;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map.Entry;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * rml:childMap and rml:parentMap of RML-Core (test cases RMLTC0030a-f,
 * kg-construct/rml-core): the expression maps of a join condition.
 */
public class TestRmlJoinTermMaps {

    static final String MAPPING = String.join("\n",
        "@prefix rml: <http://w3id.org/rml/> .",
        "<http://example.com/base/TM1> a rml:TriplesMap;",
        "  rml:logicalSource [ a rml:LogicalSource; rml:iterator \"$.students[*]\"; rml:referenceFormulation rml:JSONPath;",
        "      rml:source [ a rml:RelativePathSource; rml:root rml:CurrentWorkingDirectory; rml:path \"%1$s\" ] ];",
        "  rml:subjectMap [ rml:template \"http://example.com/student/{$.ID}\" ];",
        "  rml:predicateObjectMap [ rml:predicate <http://example.com/practises>;",
        "      rml:objectMap [ rml:parentTriplesMap <http://example.com/base/TM2>;",
        "          rml:joinCondition [ rml:childMap [ %2$s ]; rml:parentMap [ %3$s ] ] ] ] .",
        "<http://example.com/base/TM2> a rml:TriplesMap;",
        "  rml:logicalSource [ a rml:LogicalSource; rml:iterator \"$.sports[*]\"; rml:referenceFormulation rml:JSONPath;",
        "      rml:source [ a rml:RelativePathSource; rml:root rml:CurrentWorkingDirectory; rml:path \"%1$s\" ] ];",
        "  rml:subjectMap [ rml:template \"http://example.com/sport/{$.ID}\" ] .");

    static long practises(String childMap, String parentMap) throws Exception {
        Path json = Files.createTempFile("join", ".json");
        try {
            Files.writeString(json, "{ \"students\": [ { \"ID\": 10, \"Sport\": 100 }, { \"ID\": 20 } ],"
                    + " \"sports\": [ { \"ID\": 100 } ] }", StandardCharsets.UTF_8);
            Model rml = ModelFactory.createDefaultModel();
            rml.read(new StringReader(String.format(MAPPING, json.toAbsolutePath(), childMap, parentMap)), null, "TURTLE");
            List<Entry<Query, String>> queries = new RmlToSparqlRewriteBuilder()
                    .addRmlModel(org.aksw.rml.v2.jena.domain.api.TriplesMapRml2.class, rml)
                    .generate();
            Model actual = ModelFactory.createDefaultModel();
            for (Entry<Query, String> entry : queries) {
                actual.add(QueryExecutionFactory.create(entry.getKey(), ModelFactory.createDefaultModel()).execConstruct());
            }
            return actual.listStatements(actual.createResource("http://example.com/student/10"),
                    actual.createProperty("http://example.com/practises"),
                    actual.createResource("http://example.com/sport/100")).toList().size();
        } finally {
            Files.deleteIfExists(json);
        }
    }

    @Test
    public void testReferenceMaps() throws Exception {
        Assert.assertEquals(1, practises("rml:reference \"$.Sport\"", "rml:reference \"$.ID\""));
    }

    @Test
    public void testTemplateMaps() throws Exception {
        Assert.assertEquals(1, practises("rml:template \"http://example.com/{$.Sport}\"", "rml:template \"http://example.com/{$.ID}\""));
    }
}
