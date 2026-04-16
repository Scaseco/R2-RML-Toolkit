package org.aksw.xml_to_json;

import java.io.IOException;
import java.io.Writer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.StreamWriteFeature;

import com.ximpleware.NavException;
import com.ximpleware.VTDNav;

public class InstantConverter {
    protected final Writer writer;
    protected final JsonFactory jsonFactory;
    protected final VTDNav vn;

    public InstantConverter(Writer bw, VTDNav vn) {
        this.writer = bw;
        this.vn = vn;
        this.jsonFactory = JsonFactory.builder().disable(StreamWriteFeature.AUTO_CLOSE_TARGET)
                .disable(StreamWriteFeature.FLUSH_PASSED_TO_STREAM).build();
    }

    public void onNodeHit() {
        try {
            JsonGenerator jsonGenerator = jsonFactory.createGenerator(writer);
            toJson(jsonGenerator, true);
            jsonGenerator.flush();
        } catch (IOException | NavException e) {
            throw new RuntimeException(e);
        }
    }

    protected void toJson(JsonGenerator jsonGenerator) throws IOException, NavException {
        toJson(jsonGenerator, false);
    }

    protected void toJson(JsonGenerator jsonGenerator, boolean root) throws IOException, NavException {
        // VTD doesn't create text nodes as separate objects in standard traversal
        // unless asked.
        // We find out if this is an element.
        int tokenType = vn.getTokenType(vn.getCurrentIndex());

        if (tokenType == VTDNav.TOKEN_ATTR_NAME) {
            String attrName = vn.toNormalizedString(vn.getCurrentIndex());
            if (!"xmlns:xml".equals(attrName)) {
                jsonGenerator.writeFieldName("@" + attrName);
                // Move to the attribute value
                jsonGenerator.writeString(vn.toNormalizedString(vn.getCurrentIndex() + 1));
            }
            return;
        }

        // Handle Element Node
        String nodeName = vn.toNormalizedString(vn.getCurrentIndex());
        if (!root) {
            jsonGenerator.writeFieldName(nodeName);
        }

        // Check for attributes
        boolean hasAttributes = false;
        int attrCount = vn.getAttrCount();
        if (attrCount > 0) {
            hasAttributes = true;
        }

        // Try to move to the first child element
        vn.push(); // Save state before diving
        boolean hasChildElement = vn.toElement(VTDNav.FIRST_CHILD);

        if (hasChildElement) {
            jsonGenerator.writeStartObject();

            // Write attributes if any
            if (hasAttributes) {
                writeAttributes(jsonGenerator);
            }

            boolean array = false;

            do {
                // Peek ahead for arrays
                vn.push();
                boolean hasNext = vn.toElement(VTDNav.NEXT_SIBLING);
                String nextName = hasNext ? vn.toNormalizedString(vn.getCurrentIndex()) : null;
                vn.pop();

                String currentName = vn.toNormalizedString(vn.getCurrentIndex());

                if (currentName.equals(nextName)) {
                    if (!array) {
                        array = true;
                        jsonGenerator.writeFieldName(currentName);
                        jsonGenerator.writeStartArray();
                    }
                    toJson(jsonGenerator, true);
                    continue;
                }

                if (array) {
                    toJson(jsonGenerator, true);
                    array = false;
                    jsonGenerator.writeEndArray();
                    continue;
                }

                toJson(jsonGenerator);

            } while (vn.toElement(VTDNav.NEXT_SIBLING));

            if (array) {
                jsonGenerator.writeEndArray();
            }

            jsonGenerator.writeEndObject();
            vn.pop(); // Restore cursor state

        } else {
            vn.pop(); // Restore from first-child check

            // Check if it has attributes or text
            int textIndex = vn.getText(); // returns index of text if present

            if (hasAttributes) {
                jsonGenerator.writeStartObject();
                writeAttributes(jsonGenerator);

                if (textIndex != -1) {
                    String textValue = vn.toNormalizedString(textIndex);
                    // Handle empty string same as missing text
                    if (textValue != null && !textValue.isEmpty()) {
                        jsonGenerator.writeFieldName("#text");
                        jsonGenerator.writeString(textValue);
                    }
                }
                jsonGenerator.writeEndObject();
            } else if (textIndex != -1) {
                String textValue = vn.toNormalizedString(textIndex);

                // If the text is present but completely empty,
                // return JSON null instead of an empty JSON string.
                // XXX Perhaps this behavior should be configurable
                if (textValue == null || textValue.isEmpty()) {
                    jsonGenerator.writeNull();
                } else {
                    jsonGenerator.writeString(textValue);
                }
            } else {
                jsonGenerator.writeNull();
            }
        }
    }

    private void writeAttributes(JsonGenerator jsonGenerator) throws IOException, NavException {
        int attrCount = vn.getAttrCount();
        for (int i = 0; i < attrCount * 2; i += 2) {
            // Attributes are in pairs: Name (i) and Value (i+1) relative to the first
            // attribute index
            int attrIndex = vn.getCurrentIndex() + 1 + i;
            String attrName = vn.toNormalizedString(attrIndex);
            String attrValue = vn.toNormalizedString(attrIndex + 1);

            if (!"xmlns:xml".equals(attrName)) {
                jsonGenerator.writeFieldName("@" + attrName);
                jsonGenerator.writeString(attrValue);
            }
        }
    }
}
