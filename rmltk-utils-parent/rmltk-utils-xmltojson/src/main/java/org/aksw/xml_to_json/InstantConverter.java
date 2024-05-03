package org.aksw.xml_to_json;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.StreamWriteFeature;

import jlibs.xml.sax.dog.NodeItem;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.InstantEvaluationListener;

public class InstantConverter extends InstantEvaluationListener {
    protected final Writer writer;
    protected final JsonFactory jsonFactory;
    //protected final JsonGenerator jsonGenerator;

    public InstantConverter(BufferedWriter bw) throws IOException {
        this.writer = bw;
        jsonFactory = JsonFactory.builder()
                .disable(StreamWriteFeature.AUTO_CLOSE_TARGET)
                .disable(StreamWriteFeature.FLUSH_PASSED_TO_STREAM)
                .build();
    }

    @Override
    public void onNodeHit(Expression expression, NodeItem nodeItem) {
        try {
            Node node = (Node) nodeItem.xml;
            JsonGenerator jsonGenerator = jsonFactory.createGenerator(writer);
            toJson(node, jsonGenerator, true);
            jsonGenerator.flush();
            writer.write('\n');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static void toJson(Node node, JsonGenerator jsonGenerator) throws IOException {
        toJson(node, jsonGenerator, false);
    }

    protected static void toJson(Node node, JsonGenerator jsonGenerator, boolean root) throws IOException {
        switch (node.getNodeType()) {
            case Node.TEXT_NODE:
                jsonGenerator.writeString(node.getNodeValue());
                break;
            case Node.ATTRIBUTE_NODE:
                if (!"xmlns:xml".equals(node.getNodeName())) {
                    jsonGenerator.writeFieldName("@" + node.getNodeName());
                    jsonGenerator.writeString(node.getNodeValue());
                }
                break;
            case Node.ELEMENT_NODE:
                if (!root) {
                    jsonGenerator.writeFieldName(node.getNodeName());
                }
                Node firstElement = getFirstChildElement(node);
                if (firstElement != null) {
                    jsonGenerator.writeStartObject(firstElement);
                    if (node.hasAttributes()) {
                        NamedNodeMap attributes = node.getAttributes();
                        for (int i = 0; i < attributes.getLength(); i++) {
                            Node item = attributes.item(i);
                            toJson(item, jsonGenerator);
                        }
                    }
                    boolean array = false;
                    for (Node child = firstElement, peek = getNextSiblingElement(child);
                         child != null;
                         child = peek, peek = getNextSiblingElement(peek)) {

                        if (peek != null && child.getNodeName().equals(peek.getNodeName())) {
                            if (!array) {
                                array = true;
                                jsonGenerator.writeFieldName(child.getNodeName());
                                jsonGenerator.writeStartArray(child);
                            }

                            toJson(child, jsonGenerator, true);

                            continue;
                        }

                        if (array) {
                            toJson(child, jsonGenerator, true);

                            array = false;
                            jsonGenerator.writeEndArray();

                            continue;
                        }

                        toJson(child, jsonGenerator);
                    }
                    jsonGenerator.writeEndObject();
                } else if (node.hasAttributes()) {
                    jsonGenerator.writeStartObject(node);
                        NamedNodeMap attributes = node.getAttributes();
                        for (int i = 0; i < attributes.getLength(); i++) {
                            Node item = attributes.item(i);
                            toJson(item, jsonGenerator);
                        }
                        if (node.getFirstChild() != null) {
                            jsonGenerator.writeFieldName("#text");
                            toJson(node.getFirstChild(), jsonGenerator);
                        }
                        jsonGenerator.writeEndObject();
                } else if (node.getFirstChild() != null) {
                    toJson(node.getFirstChild(), jsonGenerator);
                } else {
                    jsonGenerator.writeNull();
                }
                break;
            default:
                node.getNodeType();
        }
    }

    protected static Node getFirstChildElement(Node node) {
        if (node == null) {
            return null;
        }
        for (node = node.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                return node;
            }
        }
        return null;
    }

    protected static Node getNextSiblingElement(Node node) {
        if (node == null) {
            return null;
        }
        for (node = node.getNextSibling(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                return node;
            }
        }
        return null;
    }

    @Override
    public void finishedNodeSet(Expression expression) {
    }

    @Override
    public void onResult(Expression expression, Object o) {

    }
}
