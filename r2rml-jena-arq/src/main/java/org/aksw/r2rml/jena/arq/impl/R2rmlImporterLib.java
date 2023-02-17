package org.aksw.r2rml.jena.arq.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.langtag.validator.api.LangTagValidationException;
import org.aksw.jena_sparql_api.langtag.validator.api.LangTagValidator;
import org.aksw.jena_sparql_api.langtag.validator.impl.LangTagValidators;
import org.aksw.r2rml.common.vocab.R2rmlTerms;
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
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarAlloc;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_BNode;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.E_IRI;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.E_StrLang;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.apache.jena.vocabulary.XSD;


/**
 * Methods to import and validate R2RML.
 *
 * @author raven
 *
 */
public class R2rmlImporterLib {

    public static Set<String> checkForUsageOfUndefinedTerms(Model ontology, Model data, String namespace) {
        Set<String> knownTerms = GraphUtil.listSubjects(ontology.getGraph(), null, null)
                .filterKeep(Node::isURI)
                .mapWith(Node::getURI)
                .filterKeep(str -> str.startsWith(namespace))
                .toSet();

        Set<String> usedTerms = Streams.stream(GraphUtils.allNodes(data.getGraph()))
                .filter(Node::isURI)
                .map(Node::getURI)
                .filter(str -> str.startsWith(namespace))
                .collect(Collectors.toSet());

        Set<String> result = Sets.difference(usedTerms, knownTerms);
        return result;
    }

    public static void validateR2rml(Model dataModel) {
        Model r2rmlOntModel = RDFDataMgr.loadModel("www.w3.org/ns/r2rml.ttl");

        Set<String> undefinedUses = checkForUsageOfUndefinedTerms(r2rmlOntModel, dataModel, R2rmlTerms.uri);

        if (!undefinedUses.isEmpty()) {
            throw new RuntimeException("Used terms without definition: " + undefinedUses);
        }

        Model shaclModel = RDFDataMgr.loadModel("r2rml.core.shacl.ttl");

        // Perform the validation of everything, using the data model
        // also as the shapes model - you may have them separated
//		Resource result = ValidationUtil.validateModel(dataModel, shaclModel, true);

        ValidationReport report;
        try {
            report = ShaclValidator.get().validate(shaclModel.getGraph(), dataModel.getGraph());
        } catch (Exception e) {
            RDFDataMgr.write(System.err, shaclModel, RDFFormat.NTRIPLES);
            throw new RuntimeException("Internal error during shacl validation - model printed to stderr", e);
        }


//	    ShLib.printReport(report);
//	    System.out.println();
//	    RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);

        boolean conforms = report.conforms();
//		boolean conforms = result.getProperty(SH.conforms).getBoolean();

        if(!conforms) {
            // Print violations
            ShLib.printReport(report);
            RDFDataMgr.write(System.err, report.getModel(), RDFFormat.TURTLE_PRETTY);
            throw new RuntimeException("Shacl validation failed; see report above");
        }

        // Check all values given for rr:languages (demanded by test case R2RMLTC0015b)
        // this could be handled with sparql extension functions + shacl in the future
        validateR2rmlLanguage(dataModel);

    }

    public static void validateR2rmlLanguage(Model model) {
//		Set<String> usedLangTags = model.listObjects()
//			.filterKeep(RDFNode::isLiteral)
//			.mapWith(RDFNode::asLiteral)
//			.mapWith(Literal::getLanguage)
//			.filterDrop(String::isEmpty)
//			.toSet();
        Set<String> usedLangTags = model.listObjectsOfProperty(RR.language)
                .filterKeep(RDFNode::isLiteral)
                .mapWith(RDFNode::asLiteral)
                .mapWith(Literal::getLexicalForm)
                .filterDrop(String::isEmpty)
                .toSet();

        validateLangTags(usedLangTags);
    }

    public static void validateLangTags(Set<String> langTags) {
        LangTagValidator langTagValidator = LangTagValidators.getDefault();
        Map<String, String> invalidLangTags = new LinkedHashMap<>();
        for (String langTag : langTags) {
            try {
                langTagValidator.validate(langTag);
            } catch (LangTagValidationException e) {
                invalidLangTags.put(langTag, e.getMessage());
            }
        }

        if (!invalidLangTags.isEmpty()) {
            String msg = invalidLangTags.entrySet().stream()
            .map(e -> e.getKey() + ": " + e.getValue())
            .collect(Collectors.joining("\n"));

            throw new RuntimeException("The following used lang tags failed to validate:\n" + msg);
        }
    }

    // It makes sense to have the validation method part of the object - instead of just using a static method
    public void validate(Model dataModel) {
        validateR2rml(dataModel);
    }

    public static Collection<TriplesMapToSparqlMapping> read(Model rawModel) {
        Model model = ModelFactory.createDefaultModel();
        model.add(rawModel);


        List<TriplesMap> triplesMaps = model.listSubjectsWithProperty(RR.logicalTable).mapWith(r -> r.as(TriplesMap.class)).toList();

//		for(TriplesMap tm : triplesMaps) {
            // TODO Integrate validation with shacl, as this gives us free reports of violations
//		}

        List<TriplesMapToSparqlMapping> result = triplesMaps.stream()
                .map(tm -> read(tm))
                .collect(Collectors.toList());

        return result;
    }

    /** Variant of {@link #allocateVar(TermMap, Resource, BiMap, VarAlloc)} that tracks var-termMap mapping */
    public static Node allocateVar(
            TermMap tm,
            Resource fallbackTermType,
            BiMap<Var, Expr> nodeToExpr,
            VarAlloc varGen,
            Map<TermMap, Var> termMapToVar) {
        Node result = allocateVar(tm, fallbackTermType, nodeToExpr, varGen);
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
    public static Node allocateVar(
            TermMap tm,
            Resource fallbackTermType,
            BiMap<Var, Expr> nodeToExpr,
            VarAlloc varGen) {
        Node result;
        BiMap<Expr, Var> exprToNode = nodeToExpr.inverse();

        Expr expr = termMapToExpr(tm, fallbackTermType);
        result = exprToNode.get(expr);

        if(result == null) {
            // If the expr is a constant, just use its node as the result; no need to track this in the map
            if(expr.isConstant()) {
                result = expr.getConstant().asNode();
            } else {
                // Allocate a new variable
                Var v = varGen.allocVar();
                nodeToExpr.put(v, expr);
                result = v;
            }
        }

        return result;
    }



    /**
     * Construct triples by creating the cartesian product between g, s, p, and o term maps
     *
     * https://www.w3.org/TR/r2rml/#generated-triples
     *
     * Note on graphs: the spec states: "If sgm and pogm are empty: rr:defaultGraph; otherwise:
     * union of subject_graphs and predicate-object_graphs"
     *
     * @param tm
     * @return
     */
    public static TriplesMapToSparqlMapping read(TriplesMap tm) {
        return read(tm, null);
    }

    public static TriplesMapToSparqlMapping read(TriplesMap tm, String baseIri) {

        R2rmlLib.expandShortcuts(tm);

        SubjectMap sm = tm.getSubjectMap();
        Objects.requireNonNull(sm, "SubjectMap was null on " + tm);

        Set<GraphMap> sgms = sm.getGraphMaps();

        // Mapping of expressions to allocated variables
        BiMap<Var, Expr> varToExpr = HashBiMap.create();
        Map<TermMap, Var> termMapToVar = new HashMap<>();

        // Accumulator for generated quads
        QuadAcc quadAcc = new QuadAcc();

        // TODO Allow customization of variable allocation
        VarAlloc varGen = new VarAlloc("v");

        for(PredicateObjectMap pom : tm.getPredicateObjectMaps()) {
            Set<GraphMap> pogms = pom.getGraphMaps();

            // egms = effective graph maps
            Set<GraphMap> egms = Sets.union(sgms, pogms);

            if(egms.isEmpty()) {
                egms = Collections.singleton(null);
            }

            Set<PredicateMap> pms = pom.getPredicateMaps();
            Set<ObjectMapType> oms = pom.getObjectMaps();

            for(GraphMap gm : egms) {
                for(PredicateMap pm : pms) {
                    for(ObjectMapType om : oms) {

                        if (!om.qualifiesAsRefObjectMap()) {

                            Node g = gm == null ? RR.defaultGraph.asNode() : allocateVar(gm, RR.IRI, varToExpr, varGen, termMapToVar);
                            Node s = allocateVar(sm, RR.IRI, varToExpr, varGen, termMapToVar);
                            Node p = allocateVar(pm, RR.IRI, varToExpr, varGen, termMapToVar);
                            Node o = allocateVar(om.asTermMap(), RR.Literal, varToExpr, varGen, termMapToVar);

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
            tm, template, termMapToVar, vel);

        return result;
    }


    /** Raises an exception if both arguments are non-null and they differ.
     *  Otherwise returns the first non-null argument. */
    public static <T> T requireNullOrEqual(T a, T b) {
        T result;

        boolean isInconsistent = a != null && b != null && !Objects.equals(a, b);
        if (isInconsistent) {
            throw new IllegalArgumentException(String.format("Arguments %s and %s must both be equal or one must be null", a, b));
        } else {
            result = a == null ? b : a;
        }

        return result;
    }

    /**
     * Convert a term map to a corresponing SPARQL expression
     *
     * @param tm
     * @return
     */
    public static Expr termMapToExpr(TermMap tm, Resource fallbackTermType) {
        Expr result;

        // In the following derive the effective term type based on the term map's attributes
        Node effectiveTermType = null;


        // If a datatype has been specified then get its node
        // and validate that its an IRI
        String template = tm.getTemplate();
        RDFNode constant = tm.getConstant();
        Node datatypeNode = getIriNodeOrNull(tm.getDatatype());
        Node termTypeNode = getIriNodeOrNull(tm.getTermType());

        String colName = tm.getColumn();
        String langValue = Optional.ofNullable(tm.getLanguage()).map(String::trim).orElse(null);

        // Infer the effective term type

        if (termTypeNode != null) {
            effectiveTermType = termTypeNode;
        }

        if (constant != null) {
            effectiveTermType = requireNullOrEqual(effectiveTermType, classifyTermType(constant.asNode()).asNode());
        }

        if (langValue != null) {
            effectiveTermType = requireNullOrEqual(effectiveTermType, RR.Literal.asNode());
        }

        if (effectiveTermType == null && template != null) {
            effectiveTermType = RR.IRI.asNode();
        }

        if (effectiveTermType == null) {
            effectiveTermType = fallbackTermType.asNode();
        }


        if((template = tm.getTemplate()) != null) {
            Expr arg = R2rmlTemplateLib.parse(template);
            result = applyTermType(arg, effectiveTermType, XSD.xstring.asNode());

        } else if((constant = tm.getConstant()) != null) {
            result = NodeValue.makeNode(constant.asNode());
        } else {
            if(colName != null) {
                ExprVar column = new ExprVar(colName);

                if (langValue != null) {
                    result = new E_StrLang(column, NodeValue.makeString(langValue));
                } else {
                    result = applyTermType(column, effectiveTermType, datatypeNode);
                }
            } else {
                throw new RuntimeException("TermMap does neither define rr:template, rr:constant nor rr:column " + tm);
            }

        }

        return result;
    }

    public static Node getIriNodeOrNull(RDFNode rdfNode) {
        Node result = null;
        if (rdfNode != null) {
            result = rdfNode.asNode();
            if (!result.isURI()) {
                throw new RuntimeException(result + " is not an IRI");
            }
        }

        return result;
    }

    /* base iri handling makes more sense on the R2RML processor rather than trying to capture it
     * with algebraic expressions here
     * note that jena's E_IRI supports getting the base IRI out of the ARQ context
    public static Expr applyIriType(Expr expr, String baseIri) {
        Expr result = baseIri == null
                ? new E_IRI(expr)
                : applyIriWithFallbackToBaseIri(expr, baseIri);
        return result;
    }

    public static Expr applyIriWithFallbackToBaseIri(Expr expr, String baseIri) {
        return new E_Coalesce(new ExprList(Arrays.asList(
                new E_IRI(expr),
                new E_IRI(new E_StrConcat(new ExprList(Arrays.asList(NodeValue.makeString(baseIri), expr)))))));
    }
    */

    public static Expr applyTermType(Expr column, Node termType, Node knownDatatype) {
        String termTypeIri = termType.getURI();

        Expr result;
        result = termTypeIri.equals(R2rmlTerms.IRI)
                    // ? applyIriType(applyDatatype(column, XSD.xstring.asNode(), knownDatatype), baseIri)
                    ? new E_IRI(applyDatatype(column, XSD.xstring.asNode(), knownDatatype))
                    : termTypeIri.equals(R2rmlTerms.BlankNode)
                        ? E_BNode.create(applyDatatype(column, XSD.xstring.asNode(), knownDatatype))
                        : termTypeIri.equals(R2rmlTerms.Literal)
                            ? knownDatatype == null
                                ? column
                                // : new E_StrDatatype(column, NodeValue.makeNode(knownDatatype))
                                : new E_Function(knownDatatype.getURI(), new ExprList(column))
                            : null;

//		if (result == column) {
//			System.out.println("unknown term type - assuming literal");
//		}

        return result;
    }

    public static Expr applyDatatype(Expr column, Node expectedDatatype, Node knownDatatype) {
        Objects.requireNonNull(expectedDatatype, "Need an expected datatype");

        Expr result = expectedDatatype.equals(knownDatatype)
                ? column
                : expectedDatatype.equals(XSD.xstring.asNode())
                    ? new E_Str(column)
                    : new E_Function(knownDatatype.getURI(), new ExprList(column));

        return result;
    }


    public static Resource classifyTermType(Node node) {
        Resource result =
            node.isURI()
                ? RR.IRI
                : node.isBlank()
                    ? RR.BlankNode
                    : node.isLiteral()
                        ? RR.Literal
                        : null;

        return result;
    }

// TODO Add util function to derive a name for the TriplesMapToSparqlMapping?
//	String name = Optional.ofNullable(tm.getProperty(RDFS.label))
//			.map(Statement::getString)
//			.orElseGet(() -> tm.isURIResource() ? tm.getURI() : "" + tm);

}
