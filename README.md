## A Jena-based R2RML API

An **complete**, **beautiful** and **extensible** R2RML API based on [Apache Jena](https://jena.apache.org/)'s native polymorphism system.

### Example

The following Java snippet demonstrates usage of the API:
```java
public class R2rmlApiExample {
	public static void main(String[] args) {
		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("rdfs", RDFS.uri);
		model.setNsPrefix("rr", RR.uri);
		
		TriplesMap triplesMap = model.createResource().as(TriplesMap.class); 
		triplesMap
			.setSubjectIri("urn:s")
			.addNewPredicateObjectMap()
				.addPredicate("urn:p")
				.addNewObjectMap()
					.setColumn("labels")
					.setLanguage("en");
		
		// All domain classes of the R2RML API *ARE* Jena Resources.
		// Hence, any information - such as types or custom attributes - can be freely attached:
		triplesMap
			.addProperty(RDF.type, RR.TriplesMap)
			.addProperty(RDFS.label, "My R2RML Mapping");
		
		RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
	}
}
```

The output in turtle syntax is shown below.
Note, that any of the many serialization formats supported by Jena could be used instead.

```turtle
@prefix rr:    <http://www.w3.org/ns/r2rml#> .
@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .

[ a                      rr:TriplesMap ;
  rdfs:label             "My R2RML Mapping" ;
  rr:predicateObjectMap  [ rr:objectMap  [ rr:column    "labels" ;
                                           rr:language  "en"
                                         ] ;
                           rr:predicate  <urn:p>
                         ] ;
  rr:subject             <urn:s>
] .
```

## Usage with Maven

Just include
```xml
<dependency>
  <groupId>org.aksw.r2rml</groupId>
  <artifactId>r2rml-jena-plugin</artifactId>
  <version><!-- Check the link below --></version>
</dependency>
```

[List versions published on Maven Central](https://search.maven.org/search?q=g:org.aksw.r2rml%20AND%20a:r2rml-jena-plugin)


### Modules
* r2rml-resource-ontology: A copy of the [R2RML ontology](https://www.w3.org/ns/r2rml) in turtle syntax
* r2rml-resource-w3c-testsuite: The W3C R2RML test suite resources (editor's draf)
* r2rml-resource-shacl: Shacl file for R2RML 
* r2rml-common-vocab: String-based R2RML vocabulary
* r2rml-jena-vocab: Jena-based R2RML vocabulary
* r2rml-jena-domain: Jena-based R2RML domain interfaces with RDF annotations
* r2rml-jena-arq: Utilities to import/export the R2RML model as jena constructs. Converts triples maps to jena triples, R2RML template strings to SPARQL expressions and computes effective triples maps from RefObjectMaps and more.
* r2rml-jena-plugin: Jena plugin; registers domain interfaces with jena; includes an annotation processor to create the implementations
* r2rml-jena-processor-jdbc: A standard conforming R2RML processor for JDBC datasources based on [r2rml-jena-arq](r2rml-jena-arq).
* r2rml-jena-testsuite-processor-core: Test harnish for [r2rml-resource-w3c-testsuite](r2rml-resource-w3c-testsuite).
* r2rml-jena-testsuite-processor-h2: Test harnish tied to a H2 database.
* r2rmlx-jena-api: Extensions of R2RML. Provides support for specification of prefix constraints for columns containing IRIs and support for and language tags from columns.
* r2rmlx-jena-plugin: Jena plugin that registers the r2rmlx extensions when added as a maven dependency



