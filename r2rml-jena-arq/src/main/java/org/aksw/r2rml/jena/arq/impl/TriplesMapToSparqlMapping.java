package org.aksw.r2rml.jena.arq.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.aksw.r2rml.jena.domain.api.TermMap;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.modify.TemplateLib;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.sparql.util.ExprUtils;

/**
 * A mapping of a single TriplesMaps to the triples and SPARQL expressions
 * is corresponds to.
 * 
 * @author Claus Stadler
 *
 */
public class TriplesMapToSparqlMapping {
	// The triples map from which this mapping was created
	protected TriplesMap triplesMaps;
	
	// The triples / quads constructed from the triples map
	protected Template template;
	
	// The mapping of variables to the term maps from which they were derived
	protected Map<TermMap, Var> termMapToVar;
	
	// The mapping for term maps' variables to the corresponding sparql expression
	// E.g. a rr:template "foo{bar}" becomes IRI(CONCAT("foo", STR(?var)))
	protected Map<Var, Expr> varToExpr;

	public TriplesMapToSparqlMapping(TriplesMap triplesMaps, Template template, Map<TermMap, Var> termMapToVar,
			Map<Var, Expr> varToExpr) {
		super();
		this.triplesMaps = triplesMaps;
		this.template = template;
		this.termMapToVar = termMapToVar;
		this.varToExpr = varToExpr;
	}

	public TriplesMap getTriplesMaps() {
		return triplesMaps;
	}

	public Template getTemplate() {
		return template;
	}

	public Map<TermMap, Var> getTermMapToVar() {
		return termMapToVar;
	}

	public Map<Var, Expr> getVarToExpr() {
		return varToExpr;
	}

	public Stream<Quad> evalQuads(Binding binding) {
		Binding eb = evalVars(binding);
		Iterator<Quad> it = TemplateLib.calcQuads(
				template.getQuads(),
				Collections.singleton(eb).iterator());		
		return Streams.stream(it);
	}

	public Stream<Triple> evalTriples(Binding binding) {
		Binding eb = evalVars(binding);
		Iterator<Triple> it = TemplateLib.calcTriples(
				template.getTriples(),
				Collections.singleton(eb).iterator());		
		return Streams.stream(it);
	}

	public Binding evalVars(Binding binding) {
		return evalVars(varToExpr, binding);
	}	
	
	public static Binding evalVars(Map<Var, Expr> varToExpr, Binding binding) {
		BindingMap result = BindingFactory.create();
		for (Entry<Var, Expr> e : varToExpr.entrySet()) {
			Var v = e.getKey();
			Expr expr = e.getValue();
			Node node = null;
			try {
				node = ExprUtils.eval(expr, binding).asNode();
			} catch (ExprEvalException ex) {
				// Treat as evaluation to null
			}
			
			if (node != null) {
				result.add(v, node);
			}
		}
		
		return result;
	}
	
	public Query getAsQuery() {
		Query result = new Query();
		result.setQueryConstructType();
		result.setConstructTemplate(template);
		
		ElementGroup elt = new ElementGroup();
		for (Entry<Var, Expr> e : varToExpr.entrySet()) {
			elt.addElement(new ElementBind(e.getKey(), e.getValue()));
		}
		result.setQueryPattern(elt);

		return result;
	}
	
	@Override
	public String toString() {
		return getAsQuery().toString();
	}
}
