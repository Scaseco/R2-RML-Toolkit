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
public class R2rmlTemplateParser {
	public static Expr parseTemplate(String str) {
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
}
