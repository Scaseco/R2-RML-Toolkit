package org.aksw.r2rml.jena.arq.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.E_StrConcat;
import org.apache.jena.sparql.expr.E_StrEncodeForURI;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueString;

/**
 * Parser for R2RML Templates such as:
 * 
 * <ul>
 *   <li>rr:template "http://data.example.com/department/{DEPTNO}";</li>
 *   <li>rr:template "\\{\\{\\{ \\\\o/ {TITLE} \\\\o/ \\}\\}\\}";</li>
 * </ul>
 * 
 * @author Claus Stadler
 *
 */
public class R2rmlTemplateLib {
	public static Expr parse(String str) {
		List<Expr> exprs = parseTemplateCore(str);
		
		Expr result = exprs.size() == 1
				? exprs.get(0)
				: new E_StrConcat(new ExprList(exprs))
				;

		return result;
	}
		
	public static List<Expr> parseTemplateCore(String str) {
		List<Expr> result = new ArrayList<>();

		char cs[] = str.toCharArray();

		boolean isInVarName = false;
		boolean escaped = false;
		
		int i = 0;

		StringBuilder builder = new StringBuilder();
		
		boolean repeat = true;
		while(repeat) {
			char c = i < cs.length ? cs[i++] : (char)-1;
			if(escaped) {
				builder.append(c);
				escaped = false;
			} else {
	
				switch(c) {
				case '\\':
					escaped = true;
					continue;
				case '{':
					if(isInVarName) {
						throw new RuntimeException("Unescaped '{' in var name not allowed");
					} else {
						if (builder.length() > 0) {
							result.add(NodeValue.makeString(builder.toString()));
							builder = new StringBuilder();
						}
						isInVarName = true;
					}
					break;
	
				case '}':
					if(isInVarName) {
						String varName = builder.toString();
						ExprVar ev = new ExprVar(varName);
						Expr es = new E_StrEncodeForURI(new E_Str(ev));
						result.add(es);
						builder = new StringBuilder();
						isInVarName = false;
					} else {
						throw new RuntimeException("Unescaped '}' not allowed");
					}
					break;
	
				case (char)-1:
					if(isInVarName) {
						throw new RuntimeException("End of string reached, but '}' missing");
					} else {
						if(builder.length() > 0) {
							result.add(NodeValue.makeString(builder.toString()));
						}
					}
					repeat = false;
					break;
				
				default:
					builder.append(c);
				}
			}
		}
		
		return result;
	}


	/**
	 * Convert an expression created from a R2RML template string  back to the R2RML template string.
	 * Raises an {@link IllegalArgumentException} if the conversion fails.
	 * 
	 * @param expr The expression to be serialized as an R2RML template string
	 * @return The R2RML template string
	 */
	public static String deparse(Expr expr) {
		String result;
		if (expr instanceof E_StrConcat) {
			StringBuilder sb = new StringBuilder();
			E_StrConcat e = (E_StrConcat)expr;
			for (Expr arg : e.getArgs()) {
				String str = deparse(arg);
				sb.append(str);
			}
			result = sb.toString();
		} else if (expr instanceof E_Str) {
			result = deparse(((E_Str)expr).getArg());
		} else if (expr instanceof E_StrEncodeForURI) {
			result = deparse(((E_StrEncodeForURI)expr).getArg());
		} else if (expr instanceof ExprVar) {
			ExprVar ev = (ExprVar)expr;
			String varName = ev.getVarName();
			result = "{" + escapeR2rml(varName) + "}";
		} else if (expr instanceof NodeValueString) {
			String tmp = ((NodeValueString)expr).asUnquotedString();
			result = escapeR2rml(tmp);
		} else {
			throw new IllegalArgumentException("Cannot deparse " + expr + " type = " + (expr == null ? null : expr.getClass()));
		}
		
		return result;
	}

	
	/**
	 * Escape a string to be safe for use in an R2RML template string
	 * 
	 * @param str
	 * @return
	 */
	public static String escapeR2rml(String str) {
		String result = str
				.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("{", "\\{")
				.replace("}", "\\}");
		return result;
	}
}
