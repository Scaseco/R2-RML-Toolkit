package org.aksw.xml_to_json;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestConverter {

    private Path tempOutputFile;

    @BeforeEach
    public void setUp() throws IOException {
        tempOutputFile = Files.createTempFile("vtd_test_output", ".json");
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(tempOutputFile);
    }

    /**
     * Test Case 1: Checks simple text node extraction
     */
    @Test
    public void testSimpleTextNode() throws Exception {
        String xml = "<root><item>Hello World</item></root>";
        String xpath = "/root/item";

        Converter converter = new Converter(xml, xpath, tempOutputFile.toString());
        converter.convertFromMemory(xml);

        List<String> lines = Files.readAllLines(tempOutputFile);
        assertEquals(1, lines.size());
        assertEquals("\"Hello World\"", lines.get(0).trim());
    }

    /**
     * Test Case 2: Checks attribute parsing and formatting with '@'
     */
    @Test
    public void testAttributesWithText() throws Exception {
        String xml = "<root><item id=\"123\" category=\"books\">Item Content</item></root>";
        String xpath = "/root/item";

        Converter converter = new Converter(xml, xpath, tempOutputFile.toString());
        converter.convertFromMemory(xml);

        List<String> lines = Files.readAllLines(tempOutputFile);
        assertEquals(1, lines.size());

        // Expected structure is an object with @attr keys and #text for inner text
        String expectedJson = "{\"@id\":\"123\",\"@category\":\"books\",\"#text\":\"Item Content\"}";
        assertEquals(expectedJson, lines.get(0).trim());
    }

    /**
     * Test Case 3: Checks array collapse detection (where identical adjacent siblings generate an array)
     */
    @Test
    public void testArrayInference() throws Exception {
        String xml = "<root>" +
                     "  <record>" +
                     "    <name>John</name>" +
                     "    <tag>friend</tag>" +
                     "    <tag>colleague</tag>" +
                     "  </record>" +
                     "</root>";
        String xpath = "/root/record";

        Converter converter = new Converter(xml, xpath, tempOutputFile.toString());
        converter.convertFromMemory(xml);

        List<String> lines = Files.readAllLines(tempOutputFile);
        assertEquals(1, lines.size());

        // The converter is written to bundle matching sibling elements into arrays
        String expectedJson = "{\"name\":\"John\",\"tag\":[\"friend\",\"colleague\"]}";
        assertEquals(expectedJson, lines.get(0).trim());
    }

    /**
     * Test Case 4: Checks fallback handling for empty nodes (null resolution)
     */
    @Test
    public void testNullFallbacks() throws Exception {
        String xml = "<root><item></item></root>";
        String xpath = "/root/item";

        Converter converter = new Converter(xml, xpath, tempOutputFile.toString());
        converter.convertFromMemory(xml);

        List<String> lines = Files.readAllLines(tempOutputFile);
        assertEquals(1, lines.size());
        assertEquals("null", lines.get(0).trim());
    }
}

