package org.aksw.xml_to_json;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

public class Converter {
    protected final String outputFile;
    protected final String inputFile;
    protected final String xpathExpr;

    // Add this to your Converter.java class
    public Converter(String xmlContent, String xpathExpr, String outputFile) {
        this.outputFile = outputFile;
        this.inputFile = null; // not reading from file
        this.xpathExpr = xpathExpr;
    }

    // Overload your convert() method to support the direct byte approach
    public void convertFromMemory(String xmlContent) throws IOException, VTDException {
        VTDGen vg = new VTDGen();
        vg.setDoc(xmlContent.getBytes("UTF-8")); // Emulates reading a file
        vg.parse(true);

        VTDNav vn = vg.getNav();
        com.ximpleware.AutoPilot ap = new com.ximpleware.AutoPilot(vn);
        ap.selectXPath(xpathExpr);

        try (FileWriter fw = new FileWriter(this.outputFile); BufferedWriter bw = new BufferedWriter(fw)) {

            InstantConverter instantConverter = new InstantConverter(bw, vn);

            int result;
            while ((result = ap.evalXPath()) != -1) {
                instantConverter.onNodeHit();
                bw.write('\n');
            }
        }
    }

    public void convert() throws IOException, VTDException {
        VTDGen vg = new VTDGen();

        // Use parseFile for memory efficiency with large files
        if (!vg.parseFile(inputFile, true)) {
            throw new IOException("Failed to parse XML file: " + inputFile);
        }

        VTDNav vn = vg.getNav();
        AutoPilot ap = new AutoPilot(vn);
        ap.selectXPath(xpathExpr);

        try (FileWriter fw = new FileWriter(this.outputFile); BufferedWriter bw = new BufferedWriter(fw)) {

            InstantConverter instantConverter = new InstantConverter(bw, vn);

            // Loop through every XPath hit
            int result;
            while ((result = ap.evalXPath()) != -1) {
                instantConverter.onNodeHit();
                bw.write('\n');
            }
        }
    }
}
