package org.aksw.rml.rso.model;

import java.util.List;
import java.util.stream.Collectors;

import org.aksw.fno.model.FnoTerms;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.rml.jena.impl.SparqlX_Rml_Terms;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;

/**
 * Extension for rml:Source in order to capture their the output variable(s)
 *
 * <pre>
 * SERVICE <rml:source:> {[
 *   rml:source "file.csv" ;
 *   oso:output ?row
 * ]}
 * </pre>
 *
 * For sources that produce lists of items(notably CSV) an RDF lists of variables
 * can be specified to map them directly:
 *
 * <pre>
 * [] oso:outputs (?firstName ?lastName)
 * </pre>
 */
public interface SourceOutput
    extends Resource
{
    @Iri(SparqlX_Rml_Terms.output)
    SourceOutput setOutput(Resource output);
    Resource getOutput();

    default Var getOutputVar() {
        Var result = null;
        Resource r = getOutput();
        if (r != null) {
            Node node = r.asNode();
            result = node.isVariable() ? (Var)node : null;
        }
        return result;
    }

    @Iri(SparqlX_Rml_Terms.output)
    List<Node> getOutputs();

    default List<Var> getOutputVars() {
        List<Node> nodes = getOutputs();
        List<Var> result = null;
        if (nodes != null) {
            boolean isAllVars = nodes.stream().allMatch(Node::isVariable);
            if (!isAllVars) {
                throw new RuntimeException("Outputs must all be variables - got: " + nodes);
            }
            result = nodes.stream().map(n -> (Var)n).collect(Collectors.toList());
        }
        return result;
    }
}
