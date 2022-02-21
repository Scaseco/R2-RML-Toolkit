package org.aksw.r2rml.jena.testsuite.domain;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.Namespace;
import org.aksw.jenax.annotation.reprogen.Namespaces;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;


/**
 * Domain classes for R2rml test cases from the w3c testsuite
 *
 * @author Claus Stadler
 *
 */
@ResourceView
@Namespaces({
    @Namespace(prefix = "dcterms", value = "http://purl.org/dc/elements/1.1/"),
    @Namespace(prefix = "test", value = "http://www.w3.org/2006/03/test-description#"),
    @Namespace(prefix = "rdb2rdftest", value = "http://purl.org/NET/rdb2rdf-test#")
})
public interface R2rmlTestCase
    extends Resource
{
    @IriNs("dcterms")
    String getTitle();
    R2rmlTestCase setTitle(String title);

    @IriNs("dcterms")
    String getIdentifier();
    R2rmlTestCase setIdentifier(String identifier);

    @IriNs("test")
    String getPurpose();
    R2rmlTestCase setPurpose(String purpose);

    @IriNs("test")
    String getSpecificationReference();
    R2rmlTestCase setSpecificationReference(String specificationReference);

    @IriNs("test")
    String getReviewStatus();
    R2rmlTestCase setReviewStatus(String reviewStatus);

    @IriNs("rdb2rdftest")
    Database getDatabase();
    R2rmlTestCase setDatabase(Resource database);

    @IriNs("rdb2rdftest")
    Boolean getHasExpectedOutput();
    R2rmlTestCase setHasExpectedOutput(Boolean hasExpectedOutput);

    @IriNs("rdb2rdftest")
    String getFailMessage();
    R2rmlTestCase setFailMessage(String failMessage);

    @IriNs("rdb2rdftest")
    String getMappingDocument();
    R2rmlTestCase setMappingDocument(String mappingDocument);

    @IriNs("rdb2rdftest")
    String getOutput();
    R2rmlTestCase setOutput(String output);
}
