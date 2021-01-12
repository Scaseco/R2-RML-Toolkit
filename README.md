## A Jena-based R2RML API

An **complete**, **beautiful** and **extensible** R2RML API based on Jena's native polymorphism system.

```java
	@Test
	public void testR2rmlApi() {
		Model model = ModelFactory.createDefaultModel();
		
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
```

```turtle
[ a       <http://www.w3.org/ns/r2rml#TriplesMap> ;
  <http://www.w3.org/2000/01/rdf-schema#label>
          "My R2RML Mapping" ;
  <http://www.w3.org/ns/r2rml#predicateObjectMap>
          [ <http://www.w3.org/ns/r2rml#objectMap>
                    [ <http://www.w3.org/ns/r2rml#column>
                              "labels" ;
                      <http://www.w3.org/ns/r2rml#language>
                              "en"
                    ] ;
            <http://www.w3.org/ns/r2rml#predicate>
                    <urn:p>
          ] ;
  <http://www.w3.org/ns/r2rml#subject>
          <urn:s>
] .

```

## Usage with Maven

Just include
```xml
<dependency>
  <groupId>org.aksw.r2rml</groupId>
  <artifactId>r2rml-jena-plugin</artifactId>
  <version><!- Check the link below --></version>
</dependency>
```

[List versions published on Maven Central](https://search.maven.org/search?q=g:org.aksw.r2rml%20AND%20a:r2rml-jena-plugin)


### Modules
* r2rml-resource-w3c-testsuite: The W3C R2RML test suite resources (no code)
* r2rml-reousrce-shacl: Shacl file for R2RML (no code) - work in progress
* r2rml-common-vocab: String-based R2RML vocabulary
* r2rml-jena-vocab: Jena-based R2RML vocabulary
* r2rml-jena-domain: Jena-based R2RML domain interfaces with RDF annotations
* r2rml-jena-api-arq: Utilities to import/export the R2RML model as jena constructs. For example in order to export the triple patterns expressed in an R2RML mapping as jena triples.
* r2rml-jena-api-extensions: Extensions of R2RML. Provides support for specification of prefix constraints for columns containing IRIs and support for and language tags from columns.
* r2rml-jena-plugin: Jena plugin; registers domain interfaces with jena; includes an annotation processor to create the implementations



