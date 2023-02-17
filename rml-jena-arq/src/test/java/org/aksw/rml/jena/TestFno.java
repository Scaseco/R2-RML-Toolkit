package org.aksw.rml.jena;

import org.aksw.fnox.model.JavaFunction;
import org.aksw.fnox.model.JavaMethodReference;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.junit.Test;

public class TestFno {
    @Test
    public void test01() {
        Model rmlModel = RDFDataMgr.loadModel("gtfsde-rml.ttl");
        RmlLib.normalizeNamespace(rmlModel);
        RmlLib.renameRmlToR2rml(rmlModel);
        Resource map = rmlModel.getResource("http://data.gtfs.org/TimeNormalizeFunctionMap");

        // RDFDataMgr.write(System.out, rmlModel, RDFFormat.TURTLE_PRETTY);
        // if (true) { return; }
        System.out.println(map);
        map = map.getProperty(ResourceFactory.createProperty("http://semweb.mmlab.be/ns/fnml#functionValue")).getObject().asResource();
        // System.out.println(map);

        Model fnModel = RDFDataMgr.loadModel("functions_moin.ttl");
        JavaFunction fn = fnModel.getResource("http://example.com/moin/function/timeNormalize").as(JavaFunction.class);
        JavaMethodReference ref = fn.getProvidedBy();
        // System.out.println(ref.toUri());

        System.out.println(RmlLib.buildFunctionCall(fnModel, map.as(TriplesMap.class)));
//        System.out.println(fn.getExpects().iterator().next().getPredicate());
//        System.out.println(fn.getReturns());

        // RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
    }
}
