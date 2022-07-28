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

import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * test for arctic keyed table
 */
public class TestKeyedTableDDL {

  @ClassRule
  public static SparkTestContext sparkTestContext = SparkTestContext.getSparkTestContext();

  private final String database = "db_def";
  private final String table = "testA";

  @Before
  public void prepare() {
    sparkTestContext.sql("use " + SparkTestContext.catalogName);
    sparkTestContext.sql("create database if not exists " + database);
  }

  @After
  public void cleanUp() {
    sparkTestContext.sql("DROP DATABASE IF EXISTS {0}", database);
  }

  @Test
  public void testCreateKeyedTable() {
    TableIdentifier identifier = TableIdentifier.of(SparkTestContext.catalogName, database, table);

    sparkTestContext.sql("create table {0}.{1} ( \n" +
        " id int , \n" +
        " name string , \n " +
        " ts timestamp , \n" +
        " primary key (id) \n" +
        ") using arctic \n" +
        " partitioned by ( days(ts) ) \n" +
        " tblproperties ( \n" +
        " ''props.test1'' = ''val1'', \n" +
        " ''props.test2'' = ''val2'' ) ", database, table);
    sparkTestContext.assertTableExist(identifier);
    sparkTestContext.sql("desc table {0}.{1}", database, table);
    assertDescResult(sparkTestContext.rows, Lists.newArrayList("id"));

    sparkTestContext.sql("desc table extended {0}.{1}", database, table);
    assertDescResult(sparkTestContext.rows, Lists.newArrayList("id"));

    ArcticTable keyedTable = SparkTestContext.loadTable(identifier);
    Assert.assertTrue(keyedTable.properties().containsKey("props.test1"));
    Assert.assertEquals("val1", keyedTable.properties().get("props.test1"));
    Assert.assertTrue(keyedTable.properties().containsKey("props.test2"));
    Assert.assertEquals("val2", keyedTable.properties().get("props.test2"));

    sparkTestContext.sql("drop table {0}.{1}", database, table);
    sparkTestContext.assertTableNotExist(identifier);
  }

  private void assertDescResult(List<Object[]> rows, List<String> primaryKeys) {
    boolean primaryKeysBlock = false;
    List<String> descPrimaryKeys = Lists.newArrayList();
    for (Object[] row : rows) {
      if (StringUtils.equalsIgnoreCase("# Primary keys", row[0].toString())) {
        primaryKeysBlock = true;
      } else if (StringUtils.startsWith(row[0].toString(), "# ") && primaryKeysBlock) {
        primaryKeysBlock = false;
      } else if (primaryKeysBlock){
        descPrimaryKeys.add(row[0].toString());
      }
    }

    Assert.assertEquals(primaryKeys.size(), descPrimaryKeys.size());
    Assert.assertArrayEquals(primaryKeys.toArray(), descPrimaryKeys.toArray());
  }
}
