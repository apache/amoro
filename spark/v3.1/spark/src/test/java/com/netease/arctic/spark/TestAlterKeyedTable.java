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
import org.apache.iceberg.types.Types;
import org.apache.spark.SparkException;
import org.apache.spark.sql.AnalysisException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * test for alter arctic keyed table
 */
public class TestAlterKeyedTable {
  private final String database = "db_def";
  private final String tableName = "testA";
  private final TableIdentifier tableIdent = TableIdentifier.of(SparkTestContext.catalogName, database, tableName);

  @ClassRule
  public static SparkTestContext sparkTestContext = SparkTestContext.getSparkTestContext();

  @Before
  public void prepare() {
    sparkTestContext.sql("use " + SparkTestContext.catalogName);
    sparkTestContext.sql("create database if not exists " + database);
    sparkTestContext.sql("CREATE TABLE {0}.{1} \n" +
            "(id bigint, data string, ts timestamp, primary key (id)) \n" +
            "using arctic partitioned by ( days(ts) ) \n" +
            "tblproperties ( 'test.props1' = 'val1' , 'test.props2' = 'val2' )",
        database,
        tableName);
  }

  @After
  public void cleanUp() {
    sparkTestContext.sql("DROP TABLE IF EXISTS {0}.{1}", database, tableName);
    sparkTestContext.sql("DROP DATABASE IF EXISTS {0}", database);
  }

  @Test
  public void testKeyedColumnNotNull() {
    Types.StructType expectedSchema = Types.StructType.of(
        Types.NestedField.required(1, "id", Types.LongType.get()),
        Types.NestedField.optional(2, "data", Types.StringType.get()),
        Types.NestedField.optional(3, "ts", Types.TimestampType.withZone()));
    Assert.assertEquals("Schema should match expected",
        expectedSchema, SparkTestContext.loadTable(tableIdent).schema().asStruct());
  }

  @Test
  public void testAddColumnNotNull() {
    Assert.assertThrows(
        SparkException.class,
        () -> sparkTestContext.sql("ALTER TABLE {0}.{1} ADD COLUMN c3 INT NOT NULL", database, tableName));
  }

  @Test
  public void testAddColumn() {
    sparkTestContext.sql(
        "ALTER TABLE {0}.{1} ADD COLUMN point struct<x: double NOT NULL, y: double NOT NULL> AFTER id",
        database,
        tableName);

    Types.StructType expectedSchema = Types.StructType.of(
        Types.NestedField.required(1, "id", Types.LongType.get()),
        Types.NestedField.optional(4, "point", Types.StructType.of(
            Types.NestedField.required(5, "x", Types.DoubleType.get()),
            Types.NestedField.required(6, "y", Types.DoubleType.get())
        )),
        Types.NestedField.optional(2, "data", Types.StringType.get()),
        Types.NestedField.optional(3, "ts", Types.TimestampType.withZone()));

    Assert.assertEquals("Schema should match expected",
        expectedSchema, SparkTestContext.loadTable(tableIdent).schema().asStruct());

    sparkTestContext.sql("ALTER TABLE {0}.{1} ADD COLUMN point.z double COMMENT ''May be null'' FIRST", database, tableName);

    Types.StructType expectedSchema2 = Types.StructType.of(
        Types.NestedField.required(1, "id", Types.LongType.get()),
        Types.NestedField.optional(4, "point", Types.StructType.of(
            Types.NestedField.optional(7, "z", Types.DoubleType.get(), "May be null"),
            Types.NestedField.required(5, "x", Types.DoubleType.get()),
            Types.NestedField.required(6, "y", Types.DoubleType.get())
        )),
        Types.NestedField.optional(2, "data", Types.StringType.get()),
        Types.NestedField.optional(3, "ts", Types.TimestampType.withZone()));

    Assert.assertEquals("Schema should match expected",
        expectedSchema2, SparkTestContext.loadTable(tableIdent).schema().asStruct());
  }

  @Test
  public void testDropColumn() {
    sparkTestContext.sql("ALTER TABLE {0}.{1} DROP COLUMN data", database, tableName);

    Types.StructType expectedSchema = Types.StructType.of(
        Types.NestedField.required(1, "id", Types.LongType.get()),
        Types.NestedField.optional(3, "ts", Types.TimestampType.withZone()));

    Assert.assertEquals("Schema should match expected",
        expectedSchema, SparkTestContext.loadTable(tableIdent).schema().asStruct());
  }

  @Test
  public void testRenameColumn() {
    sparkTestContext.sql("ALTER TABLE {0}.{1} RENAME COLUMN data TO row_data", database, tableName);

    Types.StructType expectedSchema = Types.StructType.of(
        Types.NestedField.required(1, "id", Types.LongType.get()),
        Types.NestedField.optional(2, "row_data", Types.StringType.get()),
        Types.NestedField.optional(3, "ts", Types.TimestampType.withZone()));

    Assert.assertEquals("Schema should match expected",
        expectedSchema, SparkTestContext.loadTable(tableIdent).schema().asStruct());
  }

  @Test
  public void testAlterColumnComment() {
    sparkTestContext.sql("ALTER TABLE {0}.{1} ALTER COLUMN id COMMENT ''Record id''", database, tableName);

    Types.StructType expectedSchema = Types.StructType.of(
        Types.NestedField.required(1, "id", Types.LongType.get(), "Record id"),
        Types.NestedField.optional(2, "data", Types.StringType.get()),
        Types.NestedField.optional(3, "ts", Types.TimestampType.withZone()));

    Assert.assertEquals("Schema should match expected",
        expectedSchema, SparkTestContext.loadTable(tableIdent).schema().asStruct());
  }

  @Test
  public void testAlterColumnType() {
    sparkTestContext.sql("ALTER TABLE {0}.{1} ADD COLUMN count int", database, tableName);
    sparkTestContext.sql("ALTER TABLE {0}.{1} ALTER COLUMN count TYPE bigint", database, tableName);

    Types.StructType expectedSchema = Types.StructType.of(
        Types.NestedField.required(1, "id", Types.LongType.get()),
        Types.NestedField.optional(2, "data", Types.StringType.get()),
        Types.NestedField.optional(3, "ts", Types.TimestampType.withZone()),
        Types.NestedField.optional(4, "count", Types.LongType.get()));

    Assert.assertEquals("Schema should match expected",
        expectedSchema, SparkTestContext.loadTable(tableIdent).schema().asStruct());
  }

  @Test
  public void testAlterColumnDropNotNull() {
    sparkTestContext.sql("ALTER TABLE {0}.{1} ALTER COLUMN data DROP NOT NULL", database, tableName);

    Types.StructType expectedSchema = Types.StructType.of(
        Types.NestedField.required(1, "id", Types.LongType.get()),
        Types.NestedField.optional(2, "data", Types.StringType.get()),
        Types.NestedField.optional(3, "ts", Types.TimestampType.withZone()));

    Assert.assertEquals("Schema should match expected",
        expectedSchema, SparkTestContext.loadTable(tableIdent).schema().asStruct());
  }

  @Test
  public void testAlterColumnSetNotNull() {
    Assert.assertThrows(
        AnalysisException.class,
        () -> sparkTestContext.sql("ALTER TABLE {0}.{1} ALTER COLUMN data SET NOT NULL", database, tableName));
  }

  @Test
  public void testAlterColumnPositionAfter() {
    sparkTestContext.sql("ALTER TABLE {0}.{1} ADD COLUMN count int", database, tableName);
    sparkTestContext.sql("ALTER TABLE {0}.{1} ALTER COLUMN count AFTER id", database, tableName);

    Types.StructType expectedSchema = Types.StructType.of(
        Types.NestedField.required(1, "id", Types.LongType.get()),
        Types.NestedField.optional(4, "count", Types.IntegerType.get()),
        Types.NestedField.optional(2, "data", Types.StringType.get()),
        Types.NestedField.optional(3, "ts", Types.TimestampType.withZone()));

    Assert.assertEquals("Schema should match expected",
        expectedSchema, SparkTestContext.loadTable(tableIdent).schema().asStruct());
  }

  @Test
  public void testAlterTableProperties() {
    sparkTestContext.sql("ALTER TABLE {0}.{1} SET TBLPROPERTIES " +
        "( ''test.props2'' = ''new-value'', ''test.props3'' = ''val3'' )", database, tableName);

    ArcticTable keyedTable = SparkTestContext.loadTable(SparkTestContext.catalogName, database, tableName);
    //Assert.assertEquals(3, keyedTable.properties().size());

    Assert.assertEquals("val1", keyedTable.properties().get("test.props1"));
    Assert.assertEquals("new-value", keyedTable.properties().get("test.props2"));
    Assert.assertEquals("val3", keyedTable.properties().get("test.props3"));


  }
}
