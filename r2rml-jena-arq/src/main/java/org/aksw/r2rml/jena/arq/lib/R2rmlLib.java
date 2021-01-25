package org.aksw.r2rml.jena.arq.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.r2rml.jena.domain.api.PredicateObjectMap;
import org.aksw.r2rml.jena.domain.api.SubjectMap;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.aksw.r2rml.jena.vocab.RR;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;
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
		// get expanded again in the immediatly following code
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

			Set<Resource> os = pom.getObjects();
			for (Resource o : new ArrayList<>(os)) {
				pom.addNewObjectMap().setConstant(o);
			}
			os.clear();
		}
	}
	
}
