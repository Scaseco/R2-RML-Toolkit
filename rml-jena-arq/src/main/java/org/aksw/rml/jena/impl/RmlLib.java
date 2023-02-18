package org.aksw.rml.jena.impl;

import java.util.HashMap;
import java.util.Map;

import org.aksw.fno.model.FnoTerms;
import org.aksw.fno.model.Param;
import org.aksw.fnox.model.JavaFunction;
import org.aksw.fnox.model.JavaMethodReference;
import org.aksw.jenax.arq.util.update.UpdateUtils;
import org.aksw.r2rml.jena.arq.impl.R2rmlImporterLib;
import org.aksw.r2rml.jena.arq.impl.TriplesMapToSparqlMapping;
import org.aksw.r2rml.jena.domain.api.ObjectMap;
import org.aksw.r2rml.jena.domain.api.ObjectMapType;
import org.aksw.r2rml.jena.domain.api.PredicateObjectMap;
import org.aksw.r2rml.jena.domain.api.TermMap;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;


public class RmlLib {
    public static void normalizeNamespace(Model model) {
        UpdateUtils.renameNamespace(model.getGraph(), FnoTerms.OLD_NS, FnoTerms.NS);
    }

    public static void renameRmlToR2rml(Model model) {
        UpdateUtils.renameProperty(model.getGraph(), "http://semweb.mmlab.be/ns/rml#reference", "http://www.w3.org/ns/r2rml#column");
        UpdateUtils.renameProperty(model.getGraph(), "http://semweb.mmlab.be/ns/rml#source", "http://www.w3.org/ns/r2rml#tableName");
        UpdateUtils.renameProperty(model.getGraph(), "http://semweb.mmlab.be/ns/rml#logicalSource", "http://www.w3.org/ns/r2rml#logicalTable");
    }

    public static Expr buildFunctionCall(Model fnmlModel, TriplesMap rawFnMap) {
        Model extra = ModelFactory.createDefaultModel();
        Model union = ModelFactory.createUnion(rawFnMap.getModel(), extra);

        TriplesMap fnMap = rawFnMap.inModel(union).as(TriplesMap.class);
        fnMap.setSubjectIri("urn:x-r2rml:dummy-subject");


        TriplesMapToSparqlMapping mapping = R2rmlImporterLib.read(fnMap);
        Map<TermMap, Var> tmToVar = mapping.getTermMapToVar();
        VarExprList varToExpr = mapping.getVarToExpr();

        Map<String, ObjectMapType> args = new HashMap<>();
        for (PredicateObjectMap pom : fnMap.getPredicateObjectMaps()) {
            String p = pom.getPredicateIri();
            if (p == null) {
                p = pom.getPredicateMap().getConstant().asNode().getURI();
            }

            // TODO Check for re-assignment
            args.put(p, pom.getObjectMap());
        }

        ObjectMap om = args.get(FnoTerms.executes).asTermMap();
        Node fnId = om.asTermMap().getConstant().asNode();
        RDFNode fnn = fnmlModel.asRDFNode(fnId);
        if (fnn == null) {
            throw new RuntimeException("No function declaration found for" + fnId);
        }

        ExprList el = new ExprList();
        JavaFunction fn = fnn.as(JavaFunction.class);
        JavaMethodReference ref = fn.getProvidedBy();
        String javaFunctionIri = ref.toUri();

        for (Param param : fn.getExpects()) {
            String p = param.getPredicateIri();
            ObjectMapType omt = args.get(p);
            Var var = tmToVar.get(omt);
            Expr expr = varToExpr.getExpr(var);
            el.add(expr);
        }

        Expr result = new E_Function(javaFunctionIri, el);
        return result;
    }
}
