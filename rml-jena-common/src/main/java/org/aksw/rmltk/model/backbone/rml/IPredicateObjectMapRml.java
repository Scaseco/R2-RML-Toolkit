package org.aksw.rmltk.model.backbone.rml;


import java.util.Set;

import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rmltk.model.backbone.common.IPredicateObjectMap;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface IPredicateObjectMapRml
    extends IMappingComponentRml, IPredicateObjectMap, IHasGraphMapRml
{
    /**
     * @return A mutable set view of the predicat maps. Never null.
     */
    @Override Set<? extends IPredicateMapRml> getPredicateMaps();

    @Override IPredicateMapRml getPredicateMap();

    /**
     * @return A mutable set view of the object map types. Never null.
     */
    @Override Set<? extends IObjectMapTypeRml> getObjectMaps();

    @Override IObjectMapTypeRml getObjectMap();

    /** Shorthands for constant objects */
    @Override Set<RDFNode> getObjects();

    /** Shorthands for constant predicates */
    @Override Set<Resource> getPredicates();

    /** Shorthands for constant objects as strings*/
    @Override Set<String> getObjectIris();

    /** Shorthands for constant predicates as strings */
    @Override Set<String> getPredicateIris();

    @Override String getPredicateIri();

    /** Shorthands for constant graphs as strings */
    @Override Set<Resource> getGraphIris();

    /**
     * Allocate a fresh blank node, add it to the set of predicate maps and return a view of it as a PredicateMap.
     *
     * @return
     */
    @Override IPredicateMapRml addNewPredicateMap();

    /**
     * Allocate a fresh blank node, add it to the set of object maps and return a view of it as a ObjectMap.
     *
     * @return
     */
    @Override IObjectMapRml addNewObjectMap();

    /**
     * Allocate a fresh blank node, add it to the set of object maps and return a view of it as a RefObjectMap.
     *
     * @return
     */
    @Override IRefObjectMapRml addNewRefObjectMap();

    // Live filtered collection views would be easily possible if we added a dependency on jena-sparql-api-collections

//	default Set<ObjectMap> getTermObjectMaps() {
//		return Sets.filter(viewSetAs(getObjectMaps(), ObjectMap.class), ObjectMapType::qualifiesAsTermMap);
//	}
//
//	default Set<RefObjectMap> getRefObjectMaps() {
//		return Sets.filter(viewSetAs(getObjectMaps(), RefObjectMap.class), ObjectMapType::qualifiesAsTermMap);
//	}


    @Override IPredicateObjectMapRml addPredicate(String iri);

    @Override IPredicateObjectMapRml addPredicate(Node node);

    @Override IPredicateObjectMapRml addPredicate(Resource resource);


    @Override IPredicateObjectMapRml addObject(String iri);

    @Override IPredicateObjectMapRml addObject(Node node);

    @Override IPredicateObjectMapRml addObject(Resource resource);


    @Override IPredicateObjectMapRml addGraph(String iri);

    @Override IPredicateObjectMapRml addGraph(Node node);

    @Override IPredicateObjectMapRml addGraph(Resource resource);


//	Resource getPredicate();
//	PredicateObjectMap setPredicate(Resource predicate);
//
//	TermMap getPredicateMap();
//	PredicateObjectMap setPredicateMap(TermMap termMap);
//
//	TermMap getObjectMap();
//	PredicateObjectMap setObjectMap(TermMap termMap);
}
