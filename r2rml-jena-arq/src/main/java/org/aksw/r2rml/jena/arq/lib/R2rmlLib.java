package org.aksw.r2rml.jena.arq.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.r2rml.jena.arq.impl.R2rmlTemplateLib;
import org.aksw.r2rml.jena.domain.api.GraphMap;
import org.aksw.r2rml.jena.domain.api.LogicalTable;
import org.aksw.r2rml.jena.domain.api.ObjectMap;
import org.aksw.r2rml.jena.domain.api.ObjectMapType;
import org.aksw.r2rml.jena.domain.api.PredicateMap;
import org.aksw.r2rml.jena.domain.api.PredicateObjectMap;
import org.aksw.r2rml.jena.domain.api.RefObjectMap;
import org.aksw.r2rml.jena.domain.api.SubjectMap;
import org.aksw.r2rml.jena.domain.api.TermMap;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.aksw.r2rml.jena.vocab.RR;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.other.BatchedStreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ExprTransformNodeElement;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;


/** Move to a dedicated utils package? */
public class R2rmlLib {
	
	/** Stream all TriplesMaps in a model (based on the rr:logicalTable predicate) */
	public static Stream<TriplesMap> streamTriplesMaps(Model model) {
		ExtendedIterator<Resource> it = model.listResourcesWithProperty(RR.logicalTable);
		return Streams.stream(it).map(r -> r.as(TriplesMap.class)).onClose(it::close);
	}
	
	
	  /**
	   * Returns all triples maps for the given predicate.
	   *
	   * @param predicate the predicate
	   * @param model the model
	   * @return the [[TriplesMap]]s that use the given predicate
	   */
//	public static Stream<TriplesMap> triplesMapsHavingPredicate(Model model, Property predicate) {
//		return streamTriplesMaps(model)
//				.filters
//		
//	    model
//	      .listResourcesWithProperty(RR.subjectMap).asScala
//	      .map(_.as(classOf[TriplesMap]))
//	      .filter(tm =>
//	        tm.getPredicateObjectMaps.asScala.exists(_.getPredicateMaps.asScala.exists(pm => Option(pm.getConstant).contains(predicate))))
//	  }
	
	
	/** Create a new TriplesMap for each RefObjectMap. The new TriplesMap will have a generated SQL
	 * query string that performs the appropriate join. */
	public static Map<RefObjectMap, TriplesMap> expandRefObjectMapsInPlace(TriplesMap triplesMap) {
		Model outModel = triplesMap.getModel();

		Model tmp = ModelFactory.createDefaultModel();
		Map<RefObjectMap, TriplesMap> result = expandRefObjectMaps(tmp, triplesMap);
		
		outModel.add(tmp);

		result.entrySet().forEach(e -> {
			TriplesMap newTm = e.getValue();
			e.setValue(newTm.inModel(outModel).as(TriplesMap.class));
		});
		
		return result;
	}

	
	/**
	 * Create a new triples map where the ref object map has been turned into a ObjectMap
	 * 
	 * 
	 * 
	 * 
	 * @param refObjectMap
	 * @return
	 */
	public static Map<RefObjectMap, TriplesMap> expandRefObjectMaps(Model outModel, TriplesMap tm) {
		// TriplesMap result = ModelFactory.createDefaultModel().createResource().as(TriplesMap.class);
		Map<RefObjectMap, TriplesMap> result = new LinkedHashMap<>();
		
		for (PredicateObjectMap pom : tm.getPredicateObjectMaps()) {
			for (ObjectMapType om : pom.getObjectMaps()) {
				if (om.qualifiesAsRefObjectMap()) {
					RefObjectMap rom = om.asRefObjectMap();

					Set<Var> childVars = new HashSet<>();

					TriplesMap targetTm = outModel.createResource().as(TriplesMap.class);
					SubjectMap sm = tm.getSubjectMap();

					
					targetTm.setSubject(tm.getSubject());
					SubjectMap targetSm = copyProperties(targetTm::getOrSetSubjectMap, sm);
					collectReferencedColumns(childVars, targetSm);
					
					// Copy graphs of the subject map
					if (targetSm != null) {
						targetSm.getGraphs().addAll(sm.getGraphs());
						
						for (GraphMap gm : sm.getGraphMaps()) {
							GraphMap targetGm = targetSm.addNewGraphMap();
							copyResource(targetGm, gm);
//							renameVariables(targetGm, "child");
							collectReferencedColumns(childVars, targetGm);
						}
					}
										
					PredicateObjectMap targetPom = targetTm.addNewPredicateObjectMap();
					targetPom.getPredicates().addAll(pom.getPredicates());
					targetPom.getGraphs().addAll(pom.getGraphs());
					for (GraphMap pomgm : pom.getGraphMaps()) {
						GraphMap targetGm = targetPom.addNewGraphMap();
						copyResource(targetGm, pomgm);	
//						renameVariables(targetGm, "child");
						collectReferencedColumns(childVars, targetGm);
					}
					
					for (PredicateMap pm : pom.getPredicateMaps()) {
						PredicateMap targetPm = targetPom.addNewPredicateMap();
						copyResource(targetPm, pm);	
//						renameVariables(targetPm, "child");
						collectReferencedColumns(childVars, targetPm);
					}

					
					TriplesMap parentTm = rom.getParentTriplesMap();
					Set<Var> parentVars = new LinkedHashSet<>();
					Map<Var, Var> parentRenames = new LinkedHashMap<>();

					Optional.ofNullable(parentTm.getSubject())
						.ifPresent(targetPom.getObjects()::add);
					
					SubjectMap parentSm = parentTm.getSubjectMap();
					if (parentSm != null) {
						ObjectMap targetOm = targetPom.addNewObjectMap();
						
						targetOm
							.setColumn(parentSm.getColumn())
							.setConstant(parentSm.getConstant())
							.setDatatype(parentSm.getDatatype())
							.setTemplate(parentSm.getTemplate())
							.setTermType(parentSm.getTermType());
						
						// Unless stated otherwise, an object map derived from a subject map denotes an IRI
						if (targetOm.getTermType() == null) {
							targetOm.setTermType(RR.IRI);
						}
						
						parentVars = collectReferencedColumns(new HashSet<>(), targetOm);

						// Rename vars in parent that clashed
						for (Var parentVar : parentVars) {
							if (childVars.contains(parentVar)) {
								String parentName = parentVar.getName();
								String unquoted = dequoteColumnName(parentName);

								String tmp = "parent_" + unquoted; 
								String newVarName = unquoted.equals(parentName)
									? tmp
									: quoteColumnName(tmp);
								
								parentRenames.put(parentVar, Var.alloc(newVarName));
							}
						}
						
						renameVariables(targetOm, n -> Optional.<Node>ofNullable(parentRenames.get(n)).orElse(n));
						// R2rmlTemplateParser.parseTemplate(null);
					}
					

					LogicalTable childTable = tm.getLogicalTable();
					LogicalTable parentTable = parentTm.getLogicalTable();
					
					if (childTable != null) {
						String jointSqlQuery = createJointSqlQuery(rom, childVars, parentVars, parentRenames, childTable, parentTable);
						targetTm.getOrSetLogicalTable().asR2rmlView()
							.setSqlQuery(jointSqlQuery);
					} else {
						targetTm.setLogicalTable(parentTable);
					}
					
					// FIXME Adjust variables in the subject and object maps (i.e. parent.var / child.var)
					// Now I wonder whether it actually make sense to resolve rr:joins using
					// r2rml model transformations or whether it is actually more reasonable to resolve this on the arq
					// level alone.
					// Essentially we now have to go through every mentioned variable and insert the alias 
					
					
					// RDFDataMgr.write(System.out, targetTm.getModel(), RDFFormat.TURTLE_PRETTY);
					
					
					result.put(rom, targetTm);
				}
			}
		}

		return result;
	}
	
	/** Return the set of columns (as Vars) referenced within a term map */
	public static <T extends Collection<Var>> T collectReferencedColumns(T out, TermMap termMap) {
		
		if (termMap.getColumn() != null) {
			out.add(Var.alloc(termMap.getColumn()));
		}
		
		String templateStr = termMap.getTemplate();
		if (templateStr != null) {
			Expr expr = R2rmlTemplateLib.parse(templateStr);

			Set<Var> varsMentioned = ExprVars.getVarsMentioned(expr);
			out.addAll(varsMentioned);
		}
		
		return out;
	}
	
	public static void renameVariables(TermMap termMap, NodeTransform nodeTransform) {
			
		if (termMap.getColumn() != null) {
			termMap.setColumn(nodeTransform.apply(Var.alloc(termMap.getColumn())).getName());
		}
		
		String templateStr = termMap.getTemplate();
		if (templateStr != null) {
			Expr expr = R2rmlTemplateLib.parse(templateStr);
			Expr newExpr = ExprTransformer.transform(new ExprTransformNodeElement(nodeTransform, null), expr);
			
			String newTemplatStr = R2rmlTemplateLib.deparse(newExpr);
			termMap.setTemplate(newTemplatStr);
		}			
	}
//	public static void renameVariables(TermMap termMap, String rawPrefix) {
//		if (!Strings.isNullOrEmpty(rawPrefix)) {
//			String prefix = rawPrefix + ".";
//			
//			if (termMap.getColumn() != null) {
//				termMap.setColumn(prefix + termMap.getColumn());
//			}
//			
//			String templateStr = termMap.getTemplate();
//			if (templateStr != null) {
//				Expr expr = R2rmlTemplateLib.parse(templateStr);
//				Expr newExpr = ExprTransformer.transform(new ExprTransformNodeElement(
//						n -> n.isVariable() ? Var.alloc(prefix + n.getName()) : n
//						, null), expr);
//				
//				String newTemplatStr = R2rmlTemplateLib.deparse(newExpr);
//				termMap.setTemplate(newTemplatStr);
//			}			
//		}
//	}
	
	
	/**
	 * Decompose triples map<b>s</b> such that they become triple (singular!) maps:
	 * I.e. one triple maps for every triple that gets generated or in other words
	 * just one subjectMap, one predicteObjectMap, one predicateMap and one objectMap
	 */
	public static void decompose() {
		// TODO Implement me
	}
	
	
	
	public static String createJointSqlQuery(
			RefObjectMap rom,
			Set<Var> childVars,
			Set<Var> parentVars,
			Map<Var, Var> parentRemap,
			LogicalTable childTable,
			LogicalTable parentTable) {
		
		String conditionPart = rom.getJoinConditions().stream()
				.map(jc -> "child." + jc.getChild() + " = parent." + jc.getParent() + "")
				.collect(Collectors.joining(" AND "));
		
		String projection =
				Streams.concat(
					parentVars.stream()
						.map(k -> {
							Var v = parentRemap.get(k);
							String effectiveVar = v == null || v.equals(k)
									? "parent." + k.getName() + " AS " + k.getName()
									: "parent." + k.getName() + " AS " + v.getName();
							return effectiveVar;
						}),
					childVars.stream().map(v -> "child." + v.getName() + " AS " + v.getName())
				)
				.collect(Collectors.joining(", "));
		
		String jointSqlQuery = "SELECT " + projection + " FROM "
				+ toSqlString(parentTable) + " AS parent, "
				+ toSqlString(childTable) + " AS child"
				+ (conditionPart.isEmpty() ? "" : " WHERE " + conditionPart);

		return jointSqlQuery;
	}

	public static String toSqlString(LogicalTable logicalTable) {
		String result = logicalTable.qualifiesAsBaseTableOrView()
				? logicalTable.asBaseTableOrView().getTableName()
				: logicalTable.qualifiesAsR2rmlView()
					? "(" + logicalTable.asR2rmlView().getSqlQuery() + ")"
					: null;
		return result;
	}
	
	/**
	 * Copy outgoing properties of a non-null source to a target.
	 * 
	 * Used to copy term maps
	 */
	public static <T extends Resource> T copyProperties(Supplier<T> targetSupplier, Resource source) {
		T target = null;
		if (source != null) {
			target = targetSupplier.get();
			copyResource(target, source);
		}
		return target;
	}
	
	public static <T extends Resource> T copyResource(T target, Resource source) {
		source.listProperties()
			.filterDrop(stmt -> stmt.getObject().isAnon())
			.forEach(stmt -> target.addProperty(stmt.getPredicate(), stmt.getObject()));
		return target;
	}
	
	public static String dequoteColumnName(String columnName) {
		String result = columnName.replaceAll("(^(\"))|((\")$)", "");
		return result;
	}

	public static String quoteColumnName(String columnName) {
		String result = "\"" + columnName + "\"";
		return result;
	}

	/**
	 * Expands rr:class, rr:subject, rr:predicate, rr:object and rr:graph to term maps
	 * in order to allow for uniform processing
	 * 
	 * @param tm The triple map for which to expant all mentioned short cuts
	 */
	public static void expandShortcuts(TriplesMap tm) {

		// Implementation note: The wrapping with new ArrayList<>(...) is needed
		// because all objects are backed by the same graph which in general
		// does not allow for concurrent modification.
		// I.e. In general it is not possible to have an iterator open on a graph
		// and have another operation modify the same graph (even if the involved triples
		// are completely unrelated)
		
		// 
		// rr:subject
		Resource s = tm.getSubject();
		if (s != null) {
			tm.getOrSetSubjectMap().setConstant(s);
			tm.setSubject(null);
		}
		
		
		// rr:graph on subject map
		SubjectMap sm = tm.getSubjectMap();		
		Set<Resource> smgs = sm.getGraphs();
		for (Resource smg : new ArrayList<>(smgs)) {
			sm.addNewGraphMap().setConstant(smg);
		}
		smgs.clear();

		
		// Note: Classes are expanded here using short cuts that
		// get expanded again in the immediately following code
		List<Resource> classes = new ArrayList<>(sm.getClasses());
		if (!classes.isEmpty()) {
			PredicateObjectMap typePom = tm.addNewPredicateObjectMap();			
			typePom.addPredicate(RDF.Nodes.type);
			
			for (Resource c : classes) {
				typePom.addObject(c);					
			}
		}
		
		
		Set<PredicateObjectMap> poms = tm.getPredicateObjectMaps();
		for (PredicateObjectMap pom : new ArrayList<>(poms)) {
			
			// rr:graph on predicate-object map
			Set<Resource> gs = pom.getGraphs();
			for (Resource g : new ArrayList<>(gs)) {
				pom.addNewGraphMap().setConstant(g);
			}
			gs.clear();

			Set<Resource> ps = pom.getPredicates();
			for (Resource p : new ArrayList<>(ps)) {
				pom.addNewPredicateMap().setConstant(p);
			}			
			ps.clear();

			Set<RDFNode> os = pom.getObjects();
			for (RDFNode o : new ArrayList<>(os)) {
				pom.addNewObjectMap().setConstant(o);
			}
			os.clear();
		}
//		RDFDataMgr.write(System.out, ResourceUtils.reachableClosure(tm), RDFFormat.TURTLE_PRETTY);
	}
	
}
