package org.aksw.rml.cli.cmd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.r2rml.jena.arq.impl.TriplesMapToSparqlMapping;
import org.aksw.rml.jena.impl.RmlImporter;
import org.aksw.rml.jena.plugin.ReferenceFormulationRegistry;
import org.aksw.rml.jena.ref.impl.ReferenceFormulationTarql;
import org.aksw.rml.model.QlTerms;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.out.NodeFmtLib;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "tarql", description = "Convert join-less RML/CSV mappings to Tarql/SPARQL queries")
public class CmdRmlTkRmlToTarql
//     extends CmdCommonBase
    implements Callable<Integer>
{
    @Option(names = { "--fnml" }, description = "Function Mapping Language models")
    public List<String> fnmlFiles = new ArrayList<>();


    @Parameters(arity = "1..n", description = "Input RML file(s)")
    public List<String> inputFiles = new ArrayList<>();

    @Override
    public Integer call() throws Exception {
        Model fnmlModel = ModelFactory.createDefaultModel();
        for (String fnmlFile : fnmlFiles) {
            Model model = RDFDataMgr.loadModel(fnmlFile);
            fnmlModel.add(model);
        }

        // Create a registry that maps CSV to the tarql resolver
        ReferenceFormulationRegistry tarqlRegistry = new ReferenceFormulationRegistry();
        tarqlRegistry.put(QlTerms.CSV, new ReferenceFormulationTarql());

        for (String inputFile : inputFiles) {
            Model model = RDFDataMgr.loadModel(inputFile);

            // For TARQL we need to configure the RML processor with a different reference resolver
            Collection<TriplesMapToSparqlMapping> maps = RmlImporter.from(model)
                    .setReferenceFormulationRegistry(tarqlRegistry)
                    .process();

            for (TriplesMapToSparqlMapping item : maps) {
                String tmId = NodeFmtLib.strNT(item.getTriplesMap().asNode());
                System.out.println("# " + tmId);
                Query query = item.getAsQuery(true);
                QueryUtils.optimizePrefixes(query);
                System.out.println(query);

//                for (JoinDeclaration join : item.getJoins()) {
//                    System.out.println("# " + tmId + " -> " + NodeFmtLib.strNT(join.getParentTriplesMap().asNode()));
//                    Query joinQuery = RmlQueryGenerator.createQuery(join, null);
//                    QueryUtils.optimizePrefixes(joinQuery);
//                    System.out.println(joinQuery);
//                }
            }
        }
        return 0;
    }
}
