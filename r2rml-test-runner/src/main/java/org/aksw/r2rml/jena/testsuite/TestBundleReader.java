package org.aksw.r2rml.jena.testsuite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.r2rml.jena.testsuite.domain.Database;
import org.aksw.r2rml.jena.testsuite.domain.R2rmlTestCase;
import org.apache.jena.ext.com.google.common.reflect.ClassPath;
import org.apache.jena.ext.com.google.common.reflect.ClassPath.ResourceInfo;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.SplitIRI;


public class TestBundleReader
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
	
	public static Collection<Database> importDatabases() throws IOException {
		ClassPath cp = ClassPath.from(TestBundleReader.class.getClassLoader());
		
		Set<ResourceInfo> resources = cp.getResources().stream()
				.filter(ri -> ri.getResourceName().endsWith("manifest.ttl"))
				.collect(Collectors.toSet());


		List<Database> allManifests = new ArrayList<>();
		for (ResourceInfo r : resources) {
			String name = r.getResourceName();
			Model model = readManifests(name, true);
			Collection<Database> contrib = R2rmlTestCaseLib.readDatabases(model);

			allManifests.addAll(contrib);
		}
		
		return allManifests;
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


