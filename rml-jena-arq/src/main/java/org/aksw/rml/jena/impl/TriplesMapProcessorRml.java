package org.aksw.rml.jena.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.aksw.fnml.model.FunctionMap;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.r2rml.jena.arq.impl.TriplesMapProcessorR2rml;
import org.aksw.r2rml.jena.arq.impl.TriplesMapToSparqlMapping;
import org.aksw.r2rml.jena.domain.api.TermMap;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.aksw.rml.model.LogicalSource;
import org.aksw.rml.model.QlTerms;
import org.aksw.rml.model.RmlTermMap;
import org.aksw.rml.model.RmlTriplesMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Element;

/**
 * The RML processor adds a bit of funcionality over the R2RML processor:
 * <ul>
 *   <li>The triples map contains the reference formulation which controls how turn rml:reference's into SPARQL Exprs</li>
 *   <li>TermMaps may be FunctionMaps
 * </ul>
 *
 */
public class TriplesMapProcessorRml
    extends TriplesMapProcessorR2rml
{
    protected Model fnmlModel;
    protected Map<String, ReferenceFormulation> refs = new LinkedHashMap<>();

    // Initialized on call()
    protected ReferenceFormulation referenceFormulation;
    protected Var itemVar; // Variable for an item of the source document

    public TriplesMapProcessorRml(TriplesMap triplesMap, String baseIri, Model fnmlModel) {
        super(triplesMap, baseIri);
        this.fnmlModel = fnmlModel;

        refs.put(QlTerms.CSV, new ReferenceFormulationCsv());
        refs.put(QlTerms.JSONPath, new ReferenceFormulationJson());
    }

    @Override
    public TriplesMapToSparqlMapping call() {
        LogicalSource logicalSource = triplesMap.as(RmlTriplesMap.class).getLogicalSource();
        if (logicalSource != null) {
            String rfi = logicalSource.getReferenceFormulationIri();
            referenceFormulation = refs.get(rfi);
            itemVar = Vars.x;
            Element elt = referenceFormulation.source(logicalSource, itemVar);
            System.out.println(elt);
        }

        // TODO Wire up the source element
        TriplesMapToSparqlMapping base = super.call();
        return base;
    }

    @Override
    protected Expr termMapToExpr(TermMap tm, Resource fallbackTermType) {
        Expr result;

        // TODO We need access to (1) the item var and (2) the reference formulation
        // We either need a context object or some form of worker

        // Check for reference
        RmlTermMap tm2 = tm.as(RmlTermMap.class);
        String ref = tm2.getReference();
        if (ref != null) {
            result = referenceToExpr(ref);
        } else {
            // Check for function call
            FunctionMap fm = tm.as(FunctionMap.class);
            TriplesMap fntm = fm.getFunctionValue();
            if (fntm != null) {
                result = RmlLib.buildFunctionCall(fnmlModel, fntm);
                // FIXME The resulting expression needs to be post-processed by
                // the R2RML layer, because e.g. rr:termType rr:IRI may need to be applied
            } else {
                result = super.termMapToExpr(tm, fallbackTermType);
            }
        }
        return result;
    }

    protected Expr referenceToExpr(String colName) {
        Expr result = referenceFormulation.reference(itemVar, colName);
        return result;
    }
}
