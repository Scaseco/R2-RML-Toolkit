## A Jena-based R2RML API

An extensible API based on Jena's native polymorphism system.

`ModelFactory.crateResource().as(TriplesMap.class)`


### Modules
* r2rml-common-vocab: String-based R2RML vocabulary
* r2rml-jena-vocab: Jena-based R2RML vocabulary
* r2rml-jena-domain: Jena-based R2RML domain interfaces with RDF annotations
* r2rml-jena-api-arq: Utilities to import/export the R2RML model as jena constructs. For example in order to export the triple patterns expressed in an R2RML mapping as jena triples.
* r2rml-jena-api-extensions: Extensions of R2RML. Provides support for specification of prefix constraints for columns containing IRIs and support for and language tags from columns.
* r2rml-jena-plugin: Jena plugin; registers domain interfaces with jena; includes an annotation processor to create the implementations



