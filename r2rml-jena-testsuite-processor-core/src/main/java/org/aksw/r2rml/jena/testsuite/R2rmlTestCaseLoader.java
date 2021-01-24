package org.aksw.r2rml.jena.testsuite;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.r2rml.jena.testsuite.domain.Database;
import org.aksw.r2rml.jena.testsuite.domain.R2rmlTestCase;
import org.apache.jena.ext.com.google.common.reflect.ClassPath;
import org.apache.jena.ext.com.google.common.reflect.ClassPath.ResourceInfo;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.SplitIRI;


public class R2rmlTestCaseLoader
{
	public static void main(String[] args) throws IOException {
		Collection<Database> databases = importDatabases();
		
		for(Database database : databases) {
			System.out.println("Database: " + database.getIdentifier());
			
			for (R2rmlTestCase testCase : database.getRelatedTestCases()) {
				System.out.println("  " + testCase.getIdentifier());
			}
		}
		
	}
	
	public static Collection<R2rmlTestCase> importTestCases() throws IOException {
		Model model = importTestSuite();
		Collection<R2rmlTestCase> result = R2rmlTestCaseLib.readTestCases(model);
		return result;
	}
	
	public static Collection<Database> importDatabases() throws IOException {
		Model model = importTestSuite();
		Collection<Database> result = R2rmlTestCaseLib.readDatabases(model);
		return result;
	}
	
	public static Model importTestSuite() throws IOException {
		ClassPath cp = ClassPath.from(R2rmlTestCaseLoader.class.getClassLoader());
		
		Set<ResourceInfo> resources = cp.getResources().stream()
				.filter(ri -> ri.getResourceName().endsWith("manifest.ttl"))
				.collect(Collectors.toSet());


		Model result = ModelFactory.createDefaultModel();
		for (ResourceInfo r : resources) {
			String name = r.getResourceName();
			Model contrib = readManifests(name, true);
			result.add(contrib);
		}
		
		return result;
	}

	public static Model readManifests(
			String resource,
			boolean adjustRelativeReferences) {
		Model result = RDFDataMgr.loadModel(resource);

		if (adjustRelativeReferences) {
			String base = SplitIRI.namespace(resource);
			R2rmlTestCaseLib.adjustRelativeReferences(base, result);
		}
		
		return result;
	}


}


