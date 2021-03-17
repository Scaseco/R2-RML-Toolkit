package org.aksw.r2rml.jena.sql.transform;

import java.io.ByteArrayInputStream;

import org.aksw.commons.sql.codec.util.SqlCodecUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sys.JenaSystem;
import org.junit.Test;

public class R2rmlLibTests {
	public static final String r2rmlStr = "\n"
			+ "@base          <http://www.w3.org/ns/r2rml#> .\n"
			+ "[ <#logicalTable>  [ <#tableName>  \"\\\"rdf_data\\\".\\\"httpp3ap2fp2fdbpediacorgp2fontologyp2finfluenced\\\"\" ] ;\n"
			+ "  <#predicateObjectMap>  [ <#objectMap>  [ <#column>  \"\\\"o\\\"\" ;\n"
			+ "                                           <#termType>  <#IRI>\n"
			+ "                                         ] ;\n"
			+ "                           <#predicate>  <http://dbpedia.org/ontology/influenced>\n"
			+ "                         ] ;\n"
			+ "  <#subjectMap>  [ <#column>  \"\\\"s\\\"\" ;\n"
			+ "                   <#termType>  <#IRI>\n"
			+ "                 ]\n"
			+ "] .\n"
			+ "\n"
			+ "[ <#logicalTable>  [ <#sqlQuery>  \"SELECT \\\"s\\\", \\\"o\\\" FROM \\\"rdf_data\\\".\\\"httpp3ap2fp2fxmlnsccomp2ffoafp2f0c1p2fgivenname_xmlschemap23string_lang\\\" WHERE \\\"l\\\" = ''\" ] ;\n"
			+ "  <#predicateObjectMap>  [ <#objectMap>  [ <#column>  \"\\\"o\\\"\" ] ;\n"
			+ "                           <#predicate>  <http://xmlns.com/foaf/0.1/givenName> ] ] .";
	
	@Test
	public void testHarmonize() throws SqlParseException {
		Model model = ModelFactory.createDefaultModel();
		RDFDataMgr.read(model, new ByteArrayInputStream(r2rmlStr.getBytes()), Lang.TURTLE);
		
		R2rmlSqlLib.harmonizeSqlIdentifiers(model, SqlCodecUtils.createSqlCodecForApacheSpark());
		
		RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
		
	}
	

}
