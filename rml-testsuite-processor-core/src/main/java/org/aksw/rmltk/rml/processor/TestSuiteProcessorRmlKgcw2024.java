package org.aksw.rmltk.rml.processor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

public class TestSuiteProcessorRmlKgcw2024 {

    public static void main(String[] args) throws URISyntaxException, IOException {
        List<String> suiteNames = List.of(
                "rml-core"
                // "rml-fnml",
                // "rml-cc", //collections
                // "rml-io",
                // "rml-star"
            );

        // Path p = toPath(Object.class.getResource("Object.class").toURI());
        Path basePath = toPath(TestSuiteProcessorRmlKgcw2024.class.getResource("/kgcw/2024/track1").toURI());

        for (String suiteName : suiteNames) {
            Path suitePath = basePath.resolve(suiteName);

            List<Path> testCases = Files.list(suitePath).toList();
            for (Path testCase : testCases) {
                String name = testCase.getFileName().toString();

                // Skip non-test case folders
                if (!name.startsWith("RMLTC")) {
                    continue;
                }

                Path data = testCase.resolve("data");
                Path shared = data.resolve("shared");
                Path mappingTtl = shared.resolve("mapping.ttl");

                Model model = ModelFactory.createDefaultModel();
                try (InputStream in = Files.newInputStream(mappingTtl)) {
                    RDFDataMgr.read(model, in, Lang.TURTLE);
                }
                RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
            }

        }

        // Files.list(p).forEach(x -> System.out.println("x: " +x));
        // System.out.println(p);
    }

//    public static Path toFileSystem(URI uri) {
//        // FileSystem fs = FileSystems.getFileSystem(uri);
//
//    }

    // https://stackoverflow.com/a/36021165/160790
    public static Path toPath(URI uri) throws IOException{
        Path result;
        try {
            Path rawPath = Paths.get(uri);
            result = fixPath(rawPath);
        }
        catch(FileSystemNotFoundException ex) {
            // TODO FileSystem needs to be closed
            FileSystem fs = FileSystems.newFileSystem(uri, Collections.<String,Object>emptyMap());
            result = fs.provider().getPath(uri);
        }
        return result;
    }

    public static Path fixPath(Path path) {
        Path result = path;
        Path parentPath = path.getParent();
        if(parentPath != null && !Files.exists(parentPath)) {
            Path fixedCandidatePath = path.resolve("/modules").resolve(path.getRoot().relativize(path));
            result = Files.exists(fixedCandidatePath) ? fixedCandidatePath : path;
        }
        return result;
    }
}
