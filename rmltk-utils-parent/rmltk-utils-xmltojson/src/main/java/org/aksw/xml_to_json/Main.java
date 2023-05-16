package org.aksw.xml_to_json;

import org.jaxen.saxpath.SAXPathException;

import javax.xml.xpath.XPathException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws SAXPathException, IOException, XPathException {
        if (args.length < 3 || args.length > 4) {
            System.err.println("xml-to-json input.xml xpath output.json [1|0(useStax)]");
            System.exit(1);
        }

        new Converter(args[0], args[1], args[2], args.length > 3 && "1".equals(args[3])).convert();
    }
}