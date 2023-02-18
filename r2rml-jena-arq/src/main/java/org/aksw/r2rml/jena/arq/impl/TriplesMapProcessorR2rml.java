package org.aksw.r2rml.jena.arq.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.aksw.commons.util.obj.ObjectUtils;
import org.aksw.r2rml.jena.arq.lib.R2rmlLib;
import org.aksw.r2rml.jena.domain.api.GraphMap;
import org.aksw.r2rml.jena.domain.api.ObjectMapType;
import org.aksw.r2rml.jena.domain.api.PredicateMap;
import org.aksw.r2rml.jena.domain.api.PredicateObjectMap;
import org.aksw.r2rml.jena.domain.api.SubjectMap;
import org.aksw.r2rml.jena.domain.api.TermMap;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.aksw.r2rml.jena.vocab.RR;
import org.apache.jena.ext.com.google.common.collect.BiMap;
import org.apache.jena.ext.com.google.common.collect.HashBiMap;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarAlloc;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_StrLang;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.vocabulary.XSD;

public class TriplesMapProcessorR2rml {
    protected TriplesMap triplesMap;
    protected String baseIri;

    protected BiMap<Var, Expr> varToExpr = HashBiMap.create();
    protected Map<TermMap, Var> termMapToVar = new HashMap<>();

    // Accumulator for generated quads
    protected QuadAcc quadAcc = new QuadAcc();

    // TODO Allow customization of variable allocation
    protected VarAlloc varGen = new VarAlloc("v");

    public TriplesMapProcessorR2rml(TriplesMap triplesMap, String baseIri) {
        // this(tm, new VarAlloc("v"), HashBiMap.create(), new HashMap<>(), new QuadAcc());
        this.triplesMap = triplesMap;
        this.baseIri = baseIri;
    }

//    public TriplesMapProcessorR2rml(TriplesMap triplesMap, BiMap<Var, Expr> varToExpr, Map<TermMap, Var> termMapToVar, QuadAcc quadAcc) {
//        super();
//        this.triplesMap = triplesMap;
//        this.varToExpr = varToExpr;
//        this.termMapToVar = termMapToVar;
//        this.quadAcc = quadAcc;
//        this.varGen = varGen;
//    }

    public TriplesMapToSparqlMapping call() {
        R2rmlLib.expandShortcuts(triplesMap);

        SubjectMap sm = triplesMap.getSubjectMap();
        Objects.requireNonNull(sm, "SubjectMap was null on " + triplesMap);

        Set<GraphMap> sgms = sm.getGraphMaps();

        for(PredicateObjectMap pom : triplesMap.getPredicateObjectMaps()) {
            Set<GraphMap> pogms = pom.getGraphMaps();

            // egms = effective graph maps
            Set<GraphMap> egms = Sets.union(sgms, pogms);

            if(egms.isEmpty()) {
                egms = Collections.singleton(null);
            }

            Set<PredicateMap> pms = pom.getPredicateMaps();
            Set<ObjectMapType> oms = pom.getObjectMaps();

            Node s = allocateVarTracked(sm, RR.IRI);
            for(GraphMap gm : egms) {
                Node g = gm == null ? RR.defaultGraph.asNode() : allocateVarTracked(gm, RR.IRI);
                for(PredicateMap pm : pms) {
                    Node p = allocateVarTracked(pm, RR.IRI);
                    for(ObjectMapType om : oms) {
                        if (!om.qualifiesAsRefObjectMap()) {
                            Node o = allocateVarTracked(om.asTermMap(), RR.Literal);

                            // Template: creates triples using Quad.defaultGraphNodeGenerated
                            // RDFDataMgr.loadDataset: loads default graph tripls with Quad.defaultGraphIRI
                            // So the output does not match exactly...

    //						if (g.equals(RR.defaultGraph.asNode())) {
    //							g = Quad.defaultGraphIRI;
    //						}

                            if (g.equals(RR.defaultGraph.asNode())) {
                                Triple triple = Triple.create(s, p, o);
                                quadAcc.addTriple(triple);
                            } else {
                                Quad quad = Quad.create(g, s, p, o);
                                quadAcc.addQuad(quad);
                            }
                        }
                    }
                }
            }
        }

        VarExprList vel = new VarExprList();
        varToExpr.forEach(vel::add);

        Template template = new Template(quadAcc);
        TriplesMapToSparqlMapping result = new TriplesMapToSparqlMapping(
            triplesMap, template, termMapToVar, vel);

        return result;
    }

    /** Calls {@link #allocateVar(TermMap, Resource) and tracks the result in the #termMapToVar */
    protected Node allocateVarTracked(
            TermMap tm,
            Resource fallbackTermType) {
        Node result = allocateVar(tm, fallbackTermType);
        if (result.isVariable()) {
            termMapToVar.put(tm, (Var)result);
        }
        return result;
    }

    /**
     * Allocates a variable for a given term map and maps it to the term map's corresponding
     * SPARQL expression.
     * If a variable maps to a constant expression then no entry is made to nodeToExpr.
     *
     * @param tm
     * @param nodeToExpr
     * @param varGen
     * @return
     */
    protected Node allocateVar(
            TermMap tm,
            Resource fallbackTermType) {
        Node result;
        BiMap<Expr, Var> exprToNode = varToExpr.inverse();

        Expr expr = termMapToExpr(tm, fallbackTermType);
        result = exprToNode.get(expr);

        if(result == null) {
            // If the expr is a constant, just use its node as the result; no need to track this in the map
            if(expr.isConstant()) {
                result = expr.getConstant().asNode();
            } else {
                // Allocate a new variable
                Var v = varGen.allocVar();
                varToExpr.put(v, expr);
                result = v;
            }
        }

        return result;
    }

    /**
     * Convert a term map into a corresponding SPARQL expression
     *
     * @param tm
     * @return
     */
    protected Expr termMapToExpr(TermMap tm, Resource fallbackTermType) {
        Expr result;

        // In the following derive the effective term type based on the term map's attributes
        Node effectiveTermType = null;


        // If a datatype has been specified then get its node
        // and validate that its an IRI
        String template = tm.getTemplate();
        RDFNode constant = tm.getConstant();
        Node datatypeNode = R2rmlImporterLib.getIriNodeOrNull(tm.getDatatype());
        Node termTypeNode = R2rmlImporterLib.getIriNodeOrNull(tm.getTermType());

        String colName = tm.getColumn();
        String langValue = Optional.ofNullable(tm.getLanguage()).map(String::trim).orElse(null);

        // Infer the effective term type

        if (termTypeNode != null) {
            effectiveTermType = termTypeNode;
        }

        if (constant != null) {
            effectiveTermType = ObjectUtils.requireNullOrEqual(effectiveTermType, R2rmlImporterLib.classifyTermType(constant.asNode()).asNode());
        }

        if (langValue != null) {
            effectiveTermType = ObjectUtils.requireNullOrEqual(effectiveTermType, RR.Literal.asNode());
        }

        if (effectiveTermType == null && template != null) {
            effectiveTermType = RR.IRI.asNode();
        }

        if (effectiveTermType == null) {
            effectiveTermType = fallbackTermType.asNode();
        }


        if((template = tm.getTemplate()) != null) {
            Expr arg = R2rmlTemplateLib.parse(template);
            result = R2rmlImporterLib.applyTermType(arg, effectiveTermType, XSD.xstring.asNode());
        } else if((constant = tm.getConstant()) != null) {
            result = NodeValue.makeNode(constant.asNode());
        } else {
            if(colName != null) {
                Expr column = new ExprVar(colName);

                if (langValue != null) {
                    result = new E_StrLang(column, NodeValue.makeString(langValue));
                } else {
                    result = R2rmlImporterLib.applyTermType(column, effectiveTermType, datatypeNode);
                }
            } else {
                throw new RuntimeException("TermMap does neither define rr:template, rr:constant nor rr:column " + tm);
            }

        }

        // Resolve all variable names. This gives e.g. an RML processor to process all variable names (=references)
        // with the reference formulation.
        result = ExprTransformer.transform(new ExprTransformCopy() {
            @Override
            public Expr transform(ExprVar exprVar) {
                String varName = exprVar.getVarName();
                Expr r = referenceToExpr(varName);
                return r;
            }
        }, result);

        return result;
    }

    protected Expr referenceToExpr(String colName) {
        ExprVar column = new ExprVar(colName);
        return column;
    }
}
