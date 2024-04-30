package org.aksw.rml.jena.common;


import java.util.Set;

import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface PredicateObjectMap
    extends MappingComponent, HasGraphMap
{
    /**
     * @return A mutable set view of the predicat maps. Never null.
     */
    Set<PredicateMap> getPredicateMaps();

    PredicateMap getPredicateMap();

    /**
     * @return A mutable set view of the object map types. Never null.
     */
    Set<ObjectMapType> getObjectMaps();


    ObjectMapType getObjectMap();

    /** Shorthands for constant objects */
    Set<RDFNode> getObjects();

    /** Shorthands for constant predicates */
    Set<Resource> getPredicates();

    /** Shorthands for constant objects as strings*/
    Set<String> getObjectIris();

    /** Shorthands for constant predicates as strings */
    Set<String> getPredicateIris();

    String getPredicateIri();

    /** Shorthands for constant graphs as strings */
    Set<Resource> getGraphIris();

    /**
     * Allocate a fresh blank node, add it to the set of predicate maps and return a view of it as a PredicateMap.
     *
     * @return
     */
    PredicateMap addNewPredicateMap();

    /**
     * Allocate a fresh blank node, add it to the set of object maps and return a view of it as a ObjectMap.
     *
     * @return
     */
    ObjectMap addNewObjectMap();

    /**
     * Allocate a fresh blank node, add it to the set of object maps and return a view of it as a RefObjectMap.
     *
     * @return
     */
    RefObjectMap addNewRefObjectMap();

    // Live filtered collection views would be easily possible if we added a dependency on jena-sparql-api-collections

//	default Set<ObjectMap> getTermObjectMaps() {
//		return Sets.filter(viewSetAs(getObjectMaps(), ObjectMap.class), ObjectMapType::qualifiesAsTermMap);
//	}
//
//	default Set<RefObjectMap> getRefObjectMaps() {
//		return Sets.filter(viewSetAs(getObjectMaps(), RefObjectMap.class), ObjectMapType::qualifiesAsTermMap);
//	}


    PredicateObjectMap addPredicate(String iri);

    PredicateObjectMap addPredicate(Node node);

    PredicateObjectMap addPredicate(Resource resource);


    PredicateObjectMap addObject(String iri);

    PredicateObjectMap addObject(Node node);

    PredicateObjectMap addObject(Resource resource);


    PredicateObjectMap addGraph(String iri);

    PredicateObjectMap addGraph(Node node);

    PredicateObjectMap addGraph(Resource resource);


//	Resource getPredicate();
//	PredicateObjectMap setPredicate(Resource predicate);
//
//	TermMap getPredicateMap();
//	PredicateObjectMap setPredicateMap(TermMap termMap);
//
//	TermMap getObjectMap();
//	PredicateObjectMap setObjectMap(TermMap termMap);
}
