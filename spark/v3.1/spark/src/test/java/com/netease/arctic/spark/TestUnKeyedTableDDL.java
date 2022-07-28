/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.spark;

import com.netease.arctic.ams.api.NoSuchObjectException;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import org.apache.commons.lang.StringUtils;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.types.Types;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class TestUnKeyedTableDDL {
  private static final Logger LOG = LoggerFactory.getLogger(TestUnKeyedTableDDL.class);

  @ClassRule
  public static SparkTestContext sparkTestContext = SparkTestContext.getSparkTestContext();

  @Test
  public void testDatabaseDDL() throws Exception {
    sparkTestContext.sql("use " + SparkTestContext.catalogName);
    sparkTestContext.sql("show databases");
    Assert.assertEquals(0, sparkTestContext.rows.size());

    sparkTestContext.sql("create database db1");
    sparkTestContext.sql("show databases");
    Assert.assertEquals(1, sparkTestContext.rows.size());
    Assert.assertEquals("db1", sparkTestContext.rows.get(0)[0]);

    sparkTestContext.sql("drop database db1");
    Assert.assertEquals(0, sparkTestContext.rows.size());
  }

  @Test
  public void testArcticCatalogTableDDL() throws Exception {
    TableIdentifier ident = TableIdentifier.of(SparkTestContext.catalogName, "def", "t1");
    doTableCreateTest(ident);
    doTablePropertiesAlterTest(ident);
    doTableColumnAlterTest(ident);
    doTableDropTest(ident);
  }


  private void doTableCreateTest(TableIdentifier ident)   {
    sparkTestContext.sql("use " + ident.getCatalog());
    if (SparkTestContext.catalogName.equals(ident.getCatalog())){
      sparkTestContext.sql("create database " + ident.getDatabase());
    }

    List<Object[]> rows;

    Assert.assertThrows(
        NoSuchObjectException.class,
        () -> SparkTestContext.ams.handler().getTable(ident.buildTableIdentifier()));

    sparkTestContext.sql("create table " + ident.getDatabase() + "." + ident.getTableName() + " ( \n" +
        " id int , \n" +
        " name string, \n" +
        " birthday date ) \n" +
        " using arctic \n " +
        " partitioned by ( days(birthday) )");
    sparkTestContext.assertTableExist(ident);
    rows = sparkTestContext.sql("desc table {0}.{1}", ident.getDatabase(), ident.getTableName());
    Assert.assertEquals(6, rows.size());

    rows = sparkTestContext.sql("show tables");
    Assert.assertEquals(1, rows.size());
    // result: |namespace|tableName|
    Assert.assertEquals(ident.getDatabase(), rows.get(0)[0]);
    Assert.assertEquals(ident.getTableName(), rows.get(0)[1]);
  }

  protected void doTablePropertiesAlterTest(TableIdentifier ident) {
    sparkTestContext.sql("use " + ident.getCatalog());

    sparkTestContext.assertTableExist(ident);
    sparkTestContext.sql("alter table " + ident.getDatabase() + "." + ident.getTableName()
        + " set tblproperties ('test-props' = 'val')");

    ArcticTable table = SparkTestContext.loadTable(ident);
    Map<String, String> props = table.properties();
    Assert.assertEquals("val",props.get("test-props"));
    props = SparkTestContext.loadTablePropertiesFromAms(ident);
    Assert.assertTrue(props.containsKey("test-props"));

    sparkTestContext.sql("alter table " + ident.getDatabase() + "." + ident.getTableName()
      + " unset tblproperties ('test-props') ");
    table = SparkTestContext.loadTable(ident);
    Assert.assertFalse(table.properties().containsKey("test-props"));
    props = SparkTestContext.loadTablePropertiesFromAms(ident);
    Assert.assertFalse(props.containsKey("test-props"));

  }

  protected void doTableColumnAlterTest(TableIdentifier ident){
    sparkTestContext.sql("use " + ident.getCatalog());

    sparkTestContext.assertTableExist(ident);
    sparkTestContext.sql("alter table " + ident.getDatabase() + "." + ident.getTableName()
        + " add column col double ");

    ArcticTable table = SparkTestContext.loadTable(ident);
    Types.NestedField field = table.schema().findField("col");
    Assert.assertNotNull(field);
    Assert.assertEquals(Types.DoubleType.get(), field.type());

    sparkTestContext.sql("alter table " + ident.getDatabase() + "." + ident.getTableName()
        + " drop column col");
    table = SparkTestContext.loadTable(ident);
    field = table.schema().findField("col");
    Assert.assertNull(field);
  }

  protected void doTableDropTest(TableIdentifier ident)  {
    sparkTestContext.sql("use " + ident.getCatalog());
    sparkTestContext.assertTableExist(ident);

    sparkTestContext.sql("drop table " + ident.getDatabase()  + "." + ident.getTableName());
    sparkTestContext.rows = sparkTestContext.sql("show tables");
    Assert.assertEquals(0, sparkTestContext.rows.size());

    Assert.assertThrows(
        NoSuchObjectException.class,
        () -> SparkTestContext.ams.handler().getTable(ident.buildTableIdentifier()));
  }
}
