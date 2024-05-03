package org.aksw.rmltk.model.backbone.common;


import java.util.Set;

import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface IPredicateObjectMap
    extends IMappingComponent, IHasGraphMap
{
    /**
     * @return A mutable set view of the predicat maps. Never null.
     */
    Set<? extends IPredicateMap> getPredicateMaps();

    IPredicateMap getPredicateMap();

    /**
     * @return A mutable set view of the object map types. Never null.
     */
    Set<? extends IObjectMapType> getObjectMaps();


    IObjectMapType getObjectMap();

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
    IPredicateMap addNewPredicateMap();

    /**
     * Allocate a fresh blank node, add it to the set of object maps and return a view of it as a ObjectMap.
     *
     * @return
     */
    IObjectMap addNewObjectMap();

    /**
     * Allocate a fresh blank node, add it to the set of object maps and return a view of it as a RefObjectMap.
     *
     * @return
     */
    IRefObjectMap addNewRefObjectMap();

    // Live filtered collection views would be easily possible if we added a dependency on jena-sparql-api-collections

//	default Set<ObjectMap> getTermObjectMaps() {
//		return Sets.filter(viewSetAs(getObjectMaps(), ObjectMap.class), ObjectMapType::qualifiesAsTermMap);
//	}
//
//	default Set<RefObjectMap> getRefObjectMaps() {
//		return Sets.filter(viewSetAs(getObjectMaps(), RefObjectMap.class), ObjectMapType::qualifiesAsTermMap);
//	}


    IPredicateObjectMap addPredicate(String iri);

    IPredicateObjectMap addPredicate(Node node);

    IPredicateObjectMap addPredicate(Resource resource);


    IPredicateObjectMap addObject(String iri);

    IPredicateObjectMap addObject(Node node);

    IPredicateObjectMap addObject(Resource resource);


    IPredicateObjectMap addGraph(String iri);

    IPredicateObjectMap addGraph(Node node);

    IPredicateObjectMap addGraph(Resource resource);


//	Resource getPredicate();
//	PredicateObjectMap setPredicate(Resource predicate);
//
//	TermMap getPredicateMap();
//	PredicateObjectMap setPredicateMap(TermMap termMap);
//
//	TermMap getObjectMap();
//	PredicateObjectMap setObjectMap(TermMap termMap);
}
