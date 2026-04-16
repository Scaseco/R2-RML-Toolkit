package org.aksw.xml_to_json;

import java.io.IOException;

import com.ximpleware.VTDException;

public class Main {
    public static void main(String[] args) throws IOException, VTDException {
        if (args.length < 3 || args.length > 4) {
            System.err.println("xml-to-json input.xml xpath output.json");
            System.exit(1);
        }
        new Converter(args[0], args[1], args[2]).convert();
    }
}
