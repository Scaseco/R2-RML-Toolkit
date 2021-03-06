package org.aksw.r2rml.jena.sql.transform;

import org.aksw.commons.sql.codec.api.SqlCodec;
import org.aksw.commons.sql.codec.util.SqlCodecUtils;
import org.aksw.r2rml.sql.transform.SqlUtils;
import org.junit.Assert;
import org.junit.Test;


public class SqlTransformTests {
	
	@Test
	public void test() throws SqlParseException {
		SqlCodec sqlCodec = SqlCodecUtils.createSqlCodecForApacheSpark();
		String sqlStr = "SELECT tableAlias.columnName AS columnAlias FROM schemaName.TableName AS tableAlias WHERE columnAlias = 'foo'";
		String str = SqlUtils.harmonizeQueryString(sqlStr, sqlCodec);
		
		str = SqlUtils.harmonizeQueryString(str, sqlCodec);
		
		System.out.println(str);
	}

	@Test
	public void testHarmonizeOfTableNameWithDash() throws SqlParseException {
		SqlCodec sqlCodec = SqlCodecUtils.createSqlCodecForApacheSpark();
		String tableName = "\"schema\".\"table-name\"";
		String str = SqlUtils.harmonizeTableName(tableName, sqlCodec);
		Assert.assertEquals("`schema`.`table-name`", str);
	}

}
