package org.aksw.r2rml.jena.arq.lib;

import java.util.stream.Stream;

import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.aksw.r2rml.jena.vocab.RR;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

/** Move to a dedicated utils package? */
public class R2rmlLib {
	
	/** Stream all TriplesMaps in a model (based on the rr:logicalTable predicate) */
	public static Stream<TriplesMap> streamTriplesMaps(Model model) {
		ExtendedIterator<Resource> it = model.listResourcesWithProperty(RR.logicalTable);
		return Streams.stream(it).map(r -> r.as(TriplesMap.class)).onClose(it::close);
	}
}
