package org.aksw.rml.cli.cmd;

import org.aksw.xml_to_json.Converter;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.update.UpdateAction;
import picocli.CommandLine;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "xmltojson", description = "Convert XML sources to JSON sources")
public class CmdRmlTkRmlXmlToJson
        implements Callable<Integer> {

    @CommandLine.ArgGroup(multiplicity = "0..*", exclusive = false)
    List<ArgGroup> args;

    static class ArgGroup {
        @CommandLine.Parameters(description = "Input RML file(s)")
        public String inputFile;
        @CommandLine.Option(names = {"-o", "--out-file"}, description = "output file")
        public String outFile;
    }

    final static String sparql_prefixes = "" +
            "PREFIX rml: <http://semweb.mmlab.be/ns/rml#>\n" +
            "PREFIX rr:  <http://www.w3.org/ns/r2rml#>\n" +
            "PREFIX ql: <http://semweb.mmlab.be/ns/ql#>\n";

    final static String filename_generation = "" +
            "bind(concat(" +
            "       replace(?file,\".xml$\",\"\")," +
            "       replace(encode_for_uri(?xpath),\"%\",\"x\")," +
            "       \".json\") as ?newfile)";

    final static String xml_pattern = "" +
            " ?source rml:referenceFormulation ql:XPath" +
            " ; rml:source ?file" +
            " ; rml:iterator ?xpath . ";

    @Override
    public Integer call() throws Exception {
        LinkedHashMap<XpathSpec, String> seenMap = new LinkedHashMap<>();
        if (args == null) return null;
        for (ArgGroup arg : args) {
            Model model = RDFDataMgr.loadModel(arg.inputFile);

            try (QueryExecution qe = QueryExecutionFactory.create(
                    sparql_prefixes +
                            "select distinct ?file ?xpath ?newfile where { " +
                            xml_pattern +
                            filename_generation +
                            "}", model)) {
                ResultSet rs = qe.execSelect();
                while (rs.hasNext()) {
                    QuerySolution qs = rs.next();
                    XpathSpec spec = new XpathSpec(qs.get("file").toString(), qs.get("xpath").toString(), qs.get("newfile").toString());
                    System.err.println(spec);
                    // stax exception mit hadoop:
                    /// Null InputStream is not a valid argument
                    //	at org.apache.hadoop.shaded.com.ctc.wstx.stax.WstxInputFactory.createSR(WstxInputFactory.java:643)
                    new Converter(spec.file, spec.xpath, spec.newfile, false).convert();
                }
            }

            UpdateAction.parseExecute(
                    sparql_prefixes +
                            "delete { " +
                                xml_pattern +
                            "}" +
                            "insert { " +
                                "?source rml:source ?newfile ; " +
                                "rml:referenceFormulation ql:JSONPath . " +
                            "}" +
                            "where { " +
                                xml_pattern +
                                filename_generation +
                            "}", model);


            OutputStream out;
            if (arg.outFile != null) out = new BufferedOutputStream(new FileOutputStream(arg.outFile));
            else out = System.out;
            RDFDataMgr.write(out, model, RDFFormat.TTL);
        }

        return null;
    }

    public static class XpathSpec {
        protected final String file;
        protected final String xpath;
        protected final String newfile;

        public XpathSpec(String file, String xpath, String newfile) {
            this.file = file;
            this.xpath = xpath;
            this.newfile = newfile;
        }

        @Override
        public String toString() {
            return "XpathSpec{" +
                    "file='" + file + '\'' +
                    ", xpath='" + xpath + '\'' +
                    ", newfile='" + newfile + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            XpathSpec xpathSpec = (XpathSpec) o;
            return Objects.equals(file, xpathSpec.file) && Objects.equals(xpath, xpathSpec.xpath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(file, xpath);
        }
    }
}
