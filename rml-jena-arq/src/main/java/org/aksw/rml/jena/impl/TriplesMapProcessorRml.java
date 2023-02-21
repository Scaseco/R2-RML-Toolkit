package org.aksw.rml.jena.impl;

import org.aksw.fnml.model.FunctionMap;
import org.aksw.r2rml.jena.arq.impl.MappingCxt;
import org.aksw.r2rml.jena.arq.impl.TriplesMapProcessorR2rml;
import org.aksw.r2rml.jena.arq.impl.TriplesMapToSparqlMapping;
import org.aksw.r2rml.jena.domain.api.TermMap;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.aksw.rml.jena.plugin.ReferenceFormulationRegistry;
import org.aksw.rml.model.LogicalSource;
import org.aksw.rml.model.RmlTermMap;
import org.aksw.rml.model.RmlTriplesMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.Expr;

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

    // Initialized on call()
    // protected ReferenceFormulation referenceFormulation;
    // protected Var itemVar; // Variable for an item of the source document
    protected ReferenceFormulationRegistry registry;
    protected ReferenceFormulation referenceFormulation;


    public TriplesMapProcessorRml(TriplesMap triplesMap,  Model fnmlModel) {
        this(triplesMap, null, fnmlModel, null);
    }

    public TriplesMapProcessorRml(TriplesMap triplesMap, String baseIri, Model fnmlModel, ReferenceFormulationRegistry registry) {
        super(triplesMap, baseIri);
        this.fnmlModel = fnmlModel;

        if (registry == null) {
            registry = ReferenceFormulationRegistry.get();
        }
        this.registry = registry;
    }

    @Override
    public TriplesMapToSparqlMapping call() {
        LogicalSource logicalSource = triplesMap.as(RmlTriplesMap.class).getLogicalSource();
        if (logicalSource != null) {
            String rfi = logicalSource.getReferenceFormulationIri();
            referenceFormulation = registry.getOrThrow(rfi);
        }

        TriplesMapToSparqlMapping base = super.call();
        return base;
    }

    @Override
    protected Expr termMapToExpr(MappingCxt cxt, TermMap tm, Resource fallbackTermType) {
        Expr result;

        // TODO We need access to (1) the item var and (2) the reference formulation
        // We either need a context object or some form of worker

        // Check for reference
        RmlTermMap tm2 = tm.as(RmlTermMap.class);
        String ref = tm2.getReference();
        if (ref != null) {
            result = referenceToExpr(cxt, ref);
        } else {
            // Check for function call
            FunctionMap fm = tm.as(FunctionMap.class);
            TriplesMap fntm = fm.getFunctionValue();
            if (fntm != null) {
                result = RmlLib.buildFunctionCall(fnmlModel, fntm);
                // FIXME The resulting expression needs to be post-processed by
                // the R2RML layer, because e.g. rr:termType rr:IRI may need to be applied
            } else {
                result = super.termMapToExpr(cxt, tm, fallbackTermType);
            }
        }
        return result;
    }

    @Override
    protected Expr referenceToExpr(MappingCxt cxt, String colName) {
        Expr result = referenceFormulation.reference(cxt.getTriplesMapVar(), colName);
        return result;
    }
}
