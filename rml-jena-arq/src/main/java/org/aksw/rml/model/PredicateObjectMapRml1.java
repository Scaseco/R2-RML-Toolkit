package org.aksw.rml.model;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.r2rml.common.vocab.R2rmlTerms;
import org.aksw.rmltk.model.backbone.common.IObjectMapType;
import org.aksw.rmltk.model.backbone.common.IPredicateObjectMap;
import org.aksw.rmltk.model.r2rml.HasGraphMap;
import org.aksw.rmltk.model.r2rml.ObjectMap;
import org.aksw.rmltk.model.r2rml.PredicateMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import com.google.common.collect.Iterables;

public interface PredicateObjectMapRml1
    extends IPredicateObjectMap, HasGraphMap
{
    /**
     * @return A mutable set view of the predicat maps. Never null.
     */
    @Iri(R2rmlTerms.predicateMap)
    @Override Set<PredicateMap> getPredicateMaps();
    @Override
    default PredicateMap getPredicateMap() {
        return Iterables.getOnlyElement(getPredicateMaps(), null);
    }

    /**
     * @return A mutable set view of the object map types. Never null.
     */
    @Iri(R2rmlTerms.objectMap)
    @Override Set<IObjectMapType> getObjectMaps();

    /** Shorthands for constant objects */
    @Iri(R2rmlTerms.object)
    @Override Set<RDFNode> getObjects();

    /** Shorthands for constant predicates */
    @Iri(R2rmlTerms.predicate)
    @Override Set<Resource> getPredicates();

    /** Shorthands for constant objects as strings*/
    @Iri(R2rmlTerms.object)
    @IriType
    @Override Set<String> getObjectIris();

    /** Shorthands for constant predicates as strings */
    @Iri(R2rmlTerms.predicate)
    @IriType
    @Override Set<String> getPredicateIris();

    /** Shorthands for constant graphs as strings */
    @Iri(R2rmlTerms.graph)
    @IriType
    @Override
    Set<Resource> getGraphIris();

    /**
     * Allocate a fresh blank node, add it to the set of predicate maps and return a view of it as a PredicateMap.
     *
     * @return
     */
    @Override
    default PredicateMap addNewPredicateMap() {
        PredicateMap result = getModel().createResource().as(PredicateMap.class);
        getPredicateMaps().add(result);
        return result;
    }


    /**
     * Allocate a fresh blank node, add it to the set of object maps and return a view of it as a ObjectMap.
     *
     * @return
     */
    @Override
    default ObjectMap addNewObjectMap() {
        ObjectMap result = getModel().createResource().as(ObjectMap.class);
        getObjectMaps().add(result);
        return result;
    }

    /**
     * Allocate a fresh blank node, add it to the set of object maps and return a view of it as a RefObjectMap.
     *
     * @return
     */
    @Override
    default RefObjectMapRml1 addNewRefObjectMap() {
        RefObjectMapRml1 result = getModel().createResource().as(RefObjectMapRml1.class);
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
    default PredicateObjectMapRml1 addPredicate(String iri) {
        return addPredicate(NodeFactory.createURI(iri));
    }

    @Override
    default PredicateObjectMapRml1 addPredicate(Node node) {
        return addPredicate(getModel().wrapAsResource(node));
    }

    @Override
    default PredicateObjectMapRml1 addPredicate(Resource resource) {
        getPredicates().add(resource);
        return this;
    }

    @Override
    default PredicateObjectMapRml1 addObject(String iri) {
        return addObject(NodeFactory.createURI(iri));
    }

    @Override
    default PredicateObjectMapRml1 addObject(Node node) {
        return addObject(getModel().wrapAsResource(node));
    }

    @Override
    default PredicateObjectMapRml1 addObject(Resource resource) {
        getObjects().add(resource);
        return this;
    }

    @Override
    default PredicateObjectMapRml1 addGraph(String iri) {
        return addGraph(NodeFactory.createURI(iri));
    }

    @Override
    default PredicateObjectMapRml1 addGraph(Node node) {
        return addGraph(getModel().wrapAsResource(node));
    }

    @Override
    default PredicateObjectMapRml1 addGraph(Resource resource) {
        getGraphs().add(resource);
        return this;
    }
}
