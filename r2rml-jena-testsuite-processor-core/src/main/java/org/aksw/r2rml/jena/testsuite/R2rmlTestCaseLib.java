package org.aksw.r2rml.jena.testsuite;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.aksw.r2rml.jena.testsuite.domain.Database;
import org.aksw.r2rml.jena.testsuite.domain.R2rmlTestCase;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;

public class R2rmlTestCaseLib {
	public static final String uri = "http://purl.org/NET/rdb2rdf-test#";
	
	public static final String md = uri + "mappingDocument";
	public static final Property mappingDocument = ResourceFactory.createProperty(md);

	public static final String ss = uri + "sqlScriptFile";
	public static final Property sqlScriptFile = ResourceFactory.createProperty(ss);

	public static <T extends RDFNode> Set<T> listResourcesWithPropertyAs(Model model, Property property, Class<T> clazz) {
		Set<T> result = model.listResourcesWithProperty(property)
			.mapWith(r -> r.as(clazz))
			.toSet();
		
		return result;
	}

	public static Collection<R2rmlTestCase> readTestCases(Model model) {
		return listResourcesWithPropertyAs(model, mappingDocument, R2rmlTestCase.class);
	}

	public static Collection<Database> readDatabases(Model model) {
		return listResourcesWithPropertyAs(model, sqlScriptFile, Database.class);
	}

		
	
//	public static String loadSqlScript(Database database) {
//		String sqlScriptFile = database.getSqlScriptFile();
//		
////		String result = sqlScriptFile == null ? null
////				: (sqlScriptFile, StandardCharsets.UTF_8);
////		
//		String result = null;
//		return result;
//	}
	
	public static Model loadMappingDocument(R2rmlTestCase manifest) {
		Model result = Optional.ofNullable(manifest.getMappingDocument())
			.map(RDFDataMgr::loadModel)
			.orElse(null);
	
		return result;
	}

	
	public static Dataset loadOutput(R2rmlTestCase manifest) {
		Dataset result = Optional.ofNullable(manifest.getOutput())
				.map(RDFDataMgr::loadDataset)
				.orElse(null);
		
		return result;
	}

	
	/** Adjust relative references so that resources can be accessed from the e.g. classpath */
	public static R2rmlTestCase adjustRelativeReferences(String base, R2rmlTestCase manifest) {
		Optional.ofNullable(manifest.getOutput())
			.ifPresent(value -> manifest.setOutput(base + value));
		
		Optional.ofNullable(manifest.getMappingDocument())
			.ifPresent(value -> manifest.setMappingDocument(base + value));
		
		return manifest;
	}

	/** Adjust relative references so that resources can be accessed from the e.g. classpath */
	public static Database adjustRelativeReferences(String base, Database manifest) {
		Optional.ofNullable(manifest.getSqlScriptFile())
			.ifPresent(value -> manifest.setSqlScriptFile(base + value));
		
		return manifest;
	}
	
	/** Adjust relative references so that resources can be accessed from the e.g. classpath */
	public static Model adjustRelativeReferences(String base, Model model) {
		readDatabases(model).forEach(db -> adjustRelativeReferences(base, db));
		readTestCases(model).forEach(testCase -> adjustRelativeReferences(base, testCase));
		return model;
	}

}
