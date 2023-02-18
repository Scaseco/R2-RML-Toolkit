package org.aksw.rml.jena.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.aksw.fnml.model.FunctionMap;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.r2rml.jena.arq.impl.R2rmlImporter;
import org.aksw.r2rml.jena.arq.impl.TriplesMapToSparqlMapping;
import org.aksw.r2rml.jena.domain.api.TermMap;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.aksw.rml.model.HasLogicalSource;
import org.aksw.rml.model.LogicalSource;
import org.aksw.rml.model.QlTerms;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Element;

public class RmlImporter
    extends R2rmlImporter
{
    protected Model fnmlModel;
    protected Map<String, ReferenceFormulation> refs = new LinkedHashMap<>();

    public RmlImporter(Model fnmlModel) {
        super();
        this.fnmlModel = fnmlModel;

        refs.put(QlTerms.CSV, new ReferenceFormulationCsv());
        refs.put(QlTerms.JSONPath, new ReferenceFormulationJson());
    }

    @Override
    public TriplesMapToSparqlMapping read(TriplesMap tm, String baseIri) {
        TriplesMapToSparqlMapping base = super.read(tm, baseIri);

        LogicalSource logicalSource = tm.as(HasLogicalSource.class).getLogicalSource();
        if (logicalSource != null) {
            String rfi = logicalSource.getReferenceFormulationIri();
            ReferenceFormulation rf = refs.get(rfi);
            Element elt = rf.source(logicalSource, Vars.x);
            System.out.println(elt);
        }

        // TODO Wire up the source element
        return base;
    }

    @Override
    protected Expr termMapToExpr(TermMap tm, Resource fallbackTermType) {
        // Check for function call
        FunctionMap fm = tm.as(FunctionMap.class);
        TriplesMap fntm = fm.getFunctionValue();
        Expr result;
        if (fntm != null) {
            result = RmlLib.buildFunctionCall(fnmlModel, fntm);
            // FIXME The resulting expression needs to be post-processed by
            // the R2RML layer, because e.g. rr:termType rr:IRI may need to be applied
        } else {
            result = super.termMapToExpr(tm, fallbackTermType);
        }
        return result;
    }
}
