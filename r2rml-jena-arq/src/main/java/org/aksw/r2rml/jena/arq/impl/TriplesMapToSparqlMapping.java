package org.aksw.r2rml.jena.arq.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.aksw.r2rml.jena.domain.api.TermMap;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.Template;

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

	@Override
	public String toString() {
		Query q = new Query();
		q.setQueryConstructType();
		q.setConstructTemplate(template);
		
		ElementGroup elt = new ElementGroup();
		for (Entry<Var, Expr> e : varToExpr.entrySet()) {
			elt.addElement(new ElementBind(e.getKey(), e.getValue()));
		}
		q.setQueryPattern(elt);

		return q.toString();
	}
}
