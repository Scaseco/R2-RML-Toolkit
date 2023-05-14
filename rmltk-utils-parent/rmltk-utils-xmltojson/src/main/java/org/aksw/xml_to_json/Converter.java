package org.aksw.xml_to_json;

import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.sax.dog.XMLDog;
import jlibs.xml.sax.dog.sniff.DOMBuilder;
import jlibs.xml.sax.dog.sniff.Event;
import org.jaxen.saxpath.SAXPathException;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPathException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Converter {
    protected final String output_file;
    protected final String input_file;
    protected final boolean use_stax;
    protected final XMLDog dog;
    protected final Event event;

    public Converter(String input_file, String xpath_expr, String output_file, boolean use_stax) throws SAXPathException, IOException, XPathException {
        this.input_file = input_file;
        this.output_file = output_file;
        this.use_stax = use_stax;

        DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
        this.dog = new XMLDog(nsContext);

        dog.addXPath(xpath_expr);
        this.event = dog.createEvent();
    }

    public void convert() throws IOException, XPathException {
        event.setXMLBuilder(new DOMBuilder());

        try (FileWriter fw = new FileWriter(this.output_file);
             BufferedWriter bw = new BufferedWriter(fw)) {
            event.setListener(new InstantConverter(bw));

            InputSource inputSource = new InputSource(input_file);
            dog.sniff(event,
                    inputSource,
                    use_stax);
        }
    }
}
