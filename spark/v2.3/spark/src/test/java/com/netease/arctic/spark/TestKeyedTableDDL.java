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
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.types.Types;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

/**
 * test for arctic keyed table
 */
public class TestKeyedTableDDL extends SparkTestBase {

  private final String database = "db_def";
  private final String tableA = "testA";
  private final String tableB = "testB";

  @Before
  public void prepare() {
    sql("create database " + database);
  }

  @After
  public void clean() {
    sql("drop database " + database);
  }


  @Test
  public void testCreateKeyedTablePartitioned() {
    sql("show databases");
    TableIdentifier identifierA = TableIdentifier.of(catalogName, database, tableA);

    sql("create table {0}.{1} ( \n" +
        " id int , \n" +
        " name string , \n " +
        " primary key (id) \n" +
        ") using arctic \n" +
        " partitioned by (ts string) \n" +
        " tblproperties ( \n" +
        " ''props.test1'' = ''val1'', \n" +
        " ''props.test2'' = ''val2'' ) ", database, tableA);
    assertTableExist(identifierA);
    ArcticTable keyedTableA = loadTable(identifierA);
    Types.StructType expectedSchema = Types.StructType.of(
        Types.NestedField.required(1, "id", Types.IntegerType.get()),
        Types.NestedField.optional(2, "name", Types.StringType.get()));
    Assert.assertEquals("Schema should match expected",
        expectedSchema, keyedTableA.schema().asStruct());
    sql("desc table {0}.{1}", database, tableA);
    assertPartitionResult(rows, Lists.newArrayList("ts"));

    Assert.assertArrayEquals("Primary should match expected",
        new List[]{Collections.singletonList("id")},
        new List[]{keyedTableA.asKeyedTable().primaryKeySpec().fieldNames()});
    Assert.assertTrue(keyedTableA.properties().containsKey("props.test1"));
    Assert.assertEquals("val1", keyedTableA.properties().get("props.test1"));
    Assert.assertTrue(keyedTableA.properties().containsKey("props.test2"));
    Assert.assertEquals("val2", keyedTableA.properties().get("props.test2"));

    sql("drop table {0}.{1}", database, tableA);
    assertTableNotExist(identifierA);

    TableIdentifier identifierB = TableIdentifier.of(catalogName, database, tableB);

    sql("create table {0}.{1} ( \n" +
        " id int , \n" +
        " name string , \n" +
        " ts string ,\n " +
        " primary key (id) \n" +
        ") using arctic \n" +
        " partitioned by (ts) \n" +
        " tblproperties ( \n" +
        " ''props.test1'' = ''val1'', \n" +
        " ''props.test2'' = ''val2'' ) ", database, tableB);

    ArcticTable keyedTableB = loadTable(identifierB);
    Types.StructType expectedSchemaB = Types.StructType.of(
        Types.NestedField.required(1, "id", Types.IntegerType.get()),
        Types.NestedField.optional(2, "name", Types.StringType.get()));
    Assert.assertEquals("Schema should match expected",
        expectedSchemaB, keyedTableB.schema().asStruct());

    sql("desc table {0}.{1}", database, tableB);
    assertPartitionResult(rows, Lists.newArrayList("ts"));

    Assert.assertArrayEquals("Primary should match expected",
        new List[]{Collections.singletonList("id")},
        new List[]{keyedTableB.asKeyedTable().primaryKeySpec().fieldNames()});

    Assert.assertTrue(keyedTableB.properties().containsKey("props.test1"));
    Assert.assertEquals("val1", keyedTableB.properties().get("props.test1"));
    Assert.assertTrue(keyedTableB.properties().containsKey("props.test2"));
    Assert.assertEquals("val2", keyedTableB.properties().get("props.test2"));

    sql("drop table {0}.{1}", database, tableB);
    assertTableNotExist(identifierB);
  }

  @Test
  public void testCreateKeyedTableUnpartitioned() {
    TableIdentifier identifierA = TableIdentifier.of(catalogName, database, tableA);

    sql("create table {0}.{1} ( \n" +
        " id int , \n" +
        " name string , \n " +
        " primary key (id) \n" +
        ") using arctic \n" +
        " tblproperties ( \n" +
        " ''props.test1'' = ''val1'', \n" +
        " ''props.test2'' = ''val2'' ) ", database, tableA);

    assertTableExist(identifierA);
    ArcticTable keyedTable = loadTable(identifierA);
    Types.StructType expectedSchema = Types.StructType.of(
        Types.NestedField.required(1, "id", Types.IntegerType.get()),
        Types.NestedField.optional(2, "name", Types.StringType.get()));
    Assert.assertEquals("Schema should match expected",
        expectedSchema, keyedTable.schema().asStruct());

    Assert.assertArrayEquals("Primary should match expected",
        new List[]{Collections.singletonList("id")},
        new List[]{keyedTable.asKeyedTable().primaryKeySpec().fieldNames()});
    Assert.assertTrue(keyedTable.properties().containsKey("props.test1"));
    Assert.assertEquals("val1", keyedTable.properties().get("props.test1"));
    Assert.assertTrue(keyedTable.properties().containsKey("props.test2"));
    Assert.assertEquals("val2", keyedTable.properties().get("props.test2"));

    sql("drop table {0}.{1}", database, tableA);
    assertTableNotExist(identifierA);
  }


  @Test
  public void testCreateUnKeyedTablePartitioned() {
    TableIdentifier identifierA = TableIdentifier.of(catalogName, database, tableA);

    sql("create table {0}.{1} ( \n" +
        " id int , \n" +
        " name string \n " +
        ") using arctic \n" +
        " partitioned by (ts string) \n" +
        " tblproperties ( \n" +
        " ''props.test1'' = ''val1'', \n" +
        " ''props.test2'' = ''val2'' ) ", database, tableA);

    assertTableExist(identifierA);
    ArcticTable keyedTable = loadTable(identifierA);
    Types.StructType expectedSchema = Types.StructType.of(
        Types.NestedField.optional(1, "id", Types.IntegerType.get()),
        Types.NestedField.optional(2, "name", Types.StringType.get()));
    Assert.assertEquals("Schema should match expected",
        expectedSchema, keyedTable.schema().asStruct());

    Assert.assertTrue(keyedTable.properties().containsKey("props.test1"));
    Assert.assertEquals("val1", keyedTable.properties().get("props.test1"));
    Assert.assertTrue(keyedTable.properties().containsKey("props.test2"));
    Assert.assertEquals("val2", keyedTable.properties().get("props.test2"));

    sql("drop table {0}.{1}", database, tableA);
    assertTableNotExist(identifierA);
  }
}
