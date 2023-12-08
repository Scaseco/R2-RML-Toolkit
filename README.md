## A Jena-based (R2)RML API

A modular R2RML suite built on Apache Jena. Featuring a complete domain API built on Jena's polymorphism system, SHACL validation, an R2RML processor with 100% standard conformance based an Jena's ARQ plus common tooling every R2RML project needs. 

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

## SPARQL Extensions in RML
The RML toolkit provides extensions for the use SPARQL expressions to
* add computed 'reference names' (columns) using `norse:rml.bind`
* filter RDF terms using `norse:rml.filter`

An `norse:rml.bind` expression can override an existing column once. The existing column becomes 'shadowed' by the new value,
so all other references will then refer to the shadowed values.

Prefixes inside of these SPARQL expressions are specified using the SHACL vocabulary.

```turtle
PREFIX rml: <http://semweb.mmlab.be/ns/rml#>

PREFIX sh: <http://www.w3.org/ns/shacl#>
PREFIX norse: <https://w3id.org/aksw/norse#>

_:prefixes
  sh:declare [ sh:prefix "xsd"  ; sh:namespace "http://www.w3.org/2001/XMLSchema#" ] ;
  sh:declare [ sh:prefix "geo"  ; sh:namespace "http://www.opengis.net/ont/geosparql#" ] ;
  sh:declare [ sh:prefix "geof" ; sh:namespace "http://www.opengis.net/def/function/geosparql/" ] ;
  .

<#AssetEmission>
  a rr:TriplesMap;
    rml:logicalSource [
      rml:source "asset_shipping_emissions_year.csv";
      rml:referenceFormulation ql:CSV ;
      sh:prefixes _:prefixes ;

      # 'Shadow' the references of ?start_time based on the expression below.
      # All rml:reference instances will refer to the shadowed value
      norse:rml.bind "xsd:dateTime(replace(?start_time, ' ', 'T')) AS ?start_time" ;
      norse:rml.bind "xsd:dateTime(replace(?end_time, ' ', 'T')) AS ?end_time" ;
      norse:rml.bind "geof:simplifyDp(strdt(?st_astext, geo:wktLiteral), 0.0001) AS ?st_astext" ;

      # Compute a new column
      norse:rml.bind "xsd:gYear(?start_time) AS ?year" ;
    ] ;
    rr:subjectMap [
      rr:template "https://data.coypu.org/ClimateTrace/{asset_id}-{iso3_country}-{gas}-{year}";
      rr:class coy:AssetEmission
    ] ;
    rr:predicateObjectMap [
      rr:predicate coy:hasAssetId;
      rr:objectMap [
        rml:reference "asset_id";
        rr:datatype xsd:string ;
        # Omit generation of this term (and thus the corresponding triples)
        # if the condition evaluates to boolean false.
        norse:rml.filter "?assert_id != ''" ;
      ]
    ] ;
    # ...
    .
```


## Jena Compatibility

|           r2rml-api |  jena  |
|---------------------|--------|
|               0.9.0 | 3.17.0 |
|               0.9.1 |  4.4.0 |
|               0.9.2 |  4.4.0 |
|               0.9.3 |  4.5.0 |
|             4.8.0-X |  4.8.0 |

Starting with Jena 4.8.0 we aligned the version of this project with Jena to make it easier to determine the compatibility.
For example, `r2rml-jena-api` version `4.8.0-2` indicates the second release developed against Jena 4.8.0.

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

## Usage of the CLI Tool

### Conversion of RML to SPARQL Construct Queries
```cli
rmltk rml to sparql mapping.rml.ttl > mapping.raw.rq
rmltk optimize workload mapping.raw.rq --no-order > mapping.rq
```


### How to Execute the Mapping

The [RDF processing toolkit (RPT)](https://github.com/SmartDataAnalytics/RdfProcessingToolkit) supports execution of the generated mapping. RPT uses this repository.

Using the single threaded Jena engine:
```cli
rpt integrate mapping.rq
```

Using RPT's parallel Spark-based executor:
```cli
rpt sansa query mapping.rq
```



### Modules
* [r2rml-resource-ontology](r2rml-resource-ontology): A copy of the [R2RML ontology](https://www.w3.org/ns/r2rml) in turtle syntax
* [r2rml-resource-w3c-testsuite](r2rml-resource-w3c-testsuite): The W3C R2RML test suite resources (editor's draft)
* [r2rml-resource-shacl](r2rml-resource-shacl): Shacl file for R2RML 
* [r2rml-common-vocab](r2rml-common-vocab): String-based R2RML vocabulary
* [r2rml-jena-vocab](r2rml-jena-vocab): Jena-based R2RML vocabulary
* [r2rml-jena-api](r2rml-jena-api): Jena-based R2RML domain interfaces with RDF annotations
* [r2rml-jena-arq](r2rml-jena-arq): Utilities to import/export the R2RML model as jena constructs. Converts triples maps to jena triples, R2RML template strings to SPARQL expressions and computes effective triples maps from RefObjectMaps and more.
* [r2rml-jena-plugin](r2rml-jena-plugin): Jena plugin; registers domain interfaces with jena; includes an annotation processor to create the implementations
* [r2rml-jena-processor-jdbc](r2rml-jena-processor-jdbc): A standard conforming R2RML processor for JDBC datasources based on [r2rml-jena-arq](r2rml-jena-arq).
* [r2rml-jena-testsuite-processor-core](r2rml-jena-testsuite-processor-core): Test harnish for [r2rml-resource-w3c-testsuite](r2rml-resource-w3c-testsuite).
* [r2rml-jena-testsuite-processor-h2](r2rml-jena-testsuite-processor-h2): Test harnish tied to a H2 database.
* [r2rml-jena-sql-transforms](r2rml-jena-sql-transforms): R2RML model transformations that affect the SQL query strings using the JSQL parser.
* [r2rmlx-jena-api](r2rmlx-jena-api): Extensions of R2RML. Provides support for specification of prefix constraints for columns containing IRIs and support for and language tags from columns. Registers the r2rmlx extensions when added as a maven dependency.


## License
The **source code** and **shacl** specification of this repo is published under the [Apache License Version 2.0](LICENSE).


* The R2RML ontology is under http://creativecommons.org/licenses/by/3.0/
* The w3c test suite is under its respective license.

