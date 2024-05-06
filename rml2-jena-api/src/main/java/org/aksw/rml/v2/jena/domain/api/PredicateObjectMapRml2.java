package org.aksw.rml.v2.jena.domain.api;

import java.util.Set;

import org.aksw.commons.collections.IterableUtils;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.rml.v2.common.vocab.Rml2Terms;
import org.aksw.rmltk.model.backbone.common.IPredicateObjectMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import com.google.common.base.Preconditions;

@ResourceView
public interface PredicateObjectMapRml2
    extends IPredicateObjectMap, HasGraphMapRml2
{
    /**
     * @return A mutable set view of the predicat maps. Never null.
     */
    @Iri(Rml2Terms.predicateMap)
    Set<PredicateMapRml2> getPredicateMaps();

    default PredicateMapRml2 getPredicateMap() {
        return IterableUtils.expectZeroOrOneItems(getPredicateMaps());
    }

    /**
     * @return A mutable set view of the object map types. Never null.
     */
    @Iri(Rml2Terms.objectMap)
    @Override Set<ObjectMapTypeRml2> getObjectMaps();


    default ObjectMapTypeRml2 getObjectMap() {
        Set<ObjectMapTypeRml2> oms = getObjectMaps();
        Preconditions.checkState(oms.size() <= 1);
        ObjectMapTypeRml2 result = oms.isEmpty() ? null : oms.iterator().next();
        return result;
    }

    /** Shorthands for constant objects */
    @Iri(Rml2Terms.object)
    @Override Set<RDFNode> getObjects();

    /** Shorthands for constant predicates */
    @Iri(Rml2Terms.predicate)
    @Override Set<Resource> getPredicates();

    /** Shorthands for constant objects as strings*/
    @Iri(Rml2Terms.object)
    @IriType
    @Override Set<String> getObjectIris();

    /** Shorthands for constant predicates as strings */
    @Iri(Rml2Terms.predicate)
    @IriType
    @Override Set<String> getPredicateIris();

    @Override
    default String getPredicateIri() {
        return IterableUtils.expectZeroOrOneItems(getPredicateIris());
    }

    /** Shorthands for constant graphs as strings */
    @Iri(Rml2Terms.graph)
    @IriType
    @Override Set<Resource> getGraphIris();

    /**
     * Allocate a fresh blank node, add it to the set of predicate maps and return a view of it as a PredicateMap.
     *
     * @return
     */
    @Override
    default PredicateMapRml2 addNewPredicateMap() {
        PredicateMapRml2 result = getModel().createResource().as(PredicateMapRml2.class);
        getPredicateMaps().add(result);
        return result;
    }


    /**
     * Allocate a fresh blank node, add it to the set of object maps and return a view of it as a ObjectMap.
     *
     * @return
     */
    @Override
    default ObjectMapRml2 addNewObjectMap() {
        ObjectMapRml2 result = getModel().createResource().as(ObjectMapRml2.class);
        getObjectMaps().add(result);
        return result;
    }

    /**
     * Allocate a fresh blank node, add it to the set of object maps and return a view of it as a RefObjectMap.
     *
     * @return
     */
    @Override
    default RefObjectMapRml2 addNewRefObjectMap() {
        RefObjectMapRml2 result = getModel().createResource().as(RefObjectMapRml2.class);
        getObjectMaps().add(result);
        return result;
    }

    // Live filtered collection views would be easily possible if we added a dependency on jena-sparql-api-collections

//	default Set<ObjectMap> getTermObjectMaps() {
//		return Sets.filter(viewSetAs(getObjectMaps(), ObjectMap.class), ObjectMapType::qualifiesAsTermMap);
//	}
//
//	default Set<RefObjectMap> getRefObjectMaps() {
//		return Sets.filter(viewSetAs(getObjectMaps(), RefObjectMap.class), ObjectMapType::qualifiesAsTermMap);
//	}


    @Override
    default PredicateObjectMapRml2 addPredicate(String iri) {
        return addPredicate(NodeFactory.createURI(iri));
    }

    @Override
    default PredicateObjectMapRml2 addPredicate(Node node) {
        return addPredicate(getModel().wrapAsResource(node));
    }

    @Override
    default PredicateObjectMapRml2 addPredicate(Resource resource) {
        getPredicates().add(resource);
        return this;
    }

    @Override
    default PredicateObjectMapRml2 addObject(String iri) {
        return addObject(NodeFactory.createURI(iri));
    }

    @Override
    default PredicateObjectMapRml2 addObject(Node node) {
        return addObject(getModel().wrapAsResource(node));
    }

    @Override
    default PredicateObjectMapRml2 addObject(Resource resource) {
        getObjects().add(resource);
        return this;
    }

    @Override
    default PredicateObjectMapRml2 addGraph(String iri) {
        return addGraph(NodeFactory.createURI(iri));
    }

    @Override
    default PredicateObjectMapRml2 addGraph(Node node) {
        return addGraph(getModel().wrapAsResource(node));
    }

    @Override
    default PredicateObjectMapRml2 addGraph(Resource resource) {
        getGraphs().add(resource);
        return this;
    }


//	Resource getPredicate();
//	PredicateObjectMap setPredicate(Resource predicate);
//
//	TermMap getPredicateMap();
//	PredicateObjectMap setPredicateMap(TermMap termMap);
//
//	TermMap getObjectMap();
//	PredicateObjectMap setObjectMap(TermMap termMap);
}
