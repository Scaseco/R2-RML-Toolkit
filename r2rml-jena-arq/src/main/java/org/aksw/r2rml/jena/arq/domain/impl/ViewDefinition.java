package org.aksw.r2rml.jena.arq.domain.impl;

import java.util.List;
import java.util.Map;

import org.aksw.r2rml.jena.domain.api.LogicalTable;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

import com.google.common.collect.Multimap;

// FIXME This class should not exist in this form in r2rml-jena-arq; However:
// Refactoring this class to static util methods for
// extracting the construct template and the var-definitions would be adequate
public class ViewDefinition {

	protected String name;
	protected List<Quad> constructTemplate;	
	protected Map<Var, Expr> varDefinition;
	protected Multimap<Var, Constraint> constraints;

	
	protected LogicalTable logicalTable;
	
	public ViewDefinition(
			String name,
			List<Quad> constructTemplate,
			Map<Var, Expr> varDefinition,
			Multimap<Var, Constraint> constraints,
			LogicalTable logicalTable) {
		super();
		this.name = name;
		this.constructTemplate = constructTemplate;
		this.varDefinition = varDefinition;
		this.constraints = constraints;
		this.logicalTable = logicalTable;
	}

	public String getName() {
		return name;
	}

	public List<Quad> getConstructTemplate() {
		return constructTemplate;
	}

	public Map<Var, Expr> getVarDefinition() {
		return varDefinition;
	}

	public Multimap<Var, Constraint> getConstraints() {
		return constraints;
	}

	public LogicalTable getLogicalTable() {
		return logicalTable;
	}
	
	
	@Override
	public String toString() {
		return "ViewDefinition [name=" + name + ", constructTemplate=" + constructTemplate + ", varDefinition="
				+ varDefinition + ", constraints=" + constraints + ", logicalTable=" + logicalTable + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((constraints == null) ? 0 : constraints.hashCode());
		result = prime * result + ((constructTemplate == null) ? 0 : constructTemplate.hashCode());
		result = prime * result + ((logicalTable == null) ? 0 : logicalTable.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((varDefinition == null) ? 0 : varDefinition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ViewDefinition other = (ViewDefinition) obj;
		if (constraints == null) {
			if (other.constraints != null)
				return false;
		} else if (!constraints.equals(other.constraints))
			return false;
		if (constructTemplate == null) {
			if (other.constructTemplate != null)
				return false;
		} else if (!constructTemplate.equals(other.constructTemplate))
			return false;
		if (logicalTable == null) {
			if (other.logicalTable != null)
				return false;
		} else if (!logicalTable.equals(other.logicalTable))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (varDefinition == null) {
			if (other.varDefinition != null)
				return false;
		} else if (!varDefinition.equals(other.varDefinition))
			return false;
		return true;
	}

	
}
