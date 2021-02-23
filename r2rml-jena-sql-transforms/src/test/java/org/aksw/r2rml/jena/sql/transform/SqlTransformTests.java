package org.aksw.r2rml.jena.sql.transform;

import org.aksw.commons.sql.codec.api.SqlCodec;
import org.aksw.commons.sql.codec.util.SqlCodecUtils;
import org.aksw.r2rml.sql.transform.SqlUtils;
import org.junit.Test;

public class SqlTransformTests {

		
	
	@Test
	public void test() throws SqlParseException {
		SqlCodec sqlCodec = SqlCodecUtils.createSqlCodecForApacheSpark();
		String sqlStr = "SELECT tableAlias.columnName AS columnAlias FROM schemaName.TableName AS tableAlias WHERE columnAlias = 'foo'";
		String str = SqlUtils.harmonizeIdentifiers(sqlStr, sqlCodec);
		
		str = SqlUtils.harmonizeIdentifiers(str, sqlCodec);
		
		System.out.println(str);
	}
}
