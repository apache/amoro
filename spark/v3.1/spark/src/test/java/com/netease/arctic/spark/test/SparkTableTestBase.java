/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.spark.test;

import com.netease.arctic.ams.api.properties.TableFormat;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.utils.CollectionHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.util.List;

public abstract class SparkTableTestBase extends SparkCatalogTestBase {

  protected String database() {
    return "spark_unit_test";
  }

  protected String table() {
    return "test_table";
  }

  protected TableFormat tableFormat() {
    if ("session".equalsIgnoreCase(catalogType()) || "hive".equalsIgnoreCase(catalogType())) {
      return TableFormat.MIXED_HIVE;
    } else {
      return TableFormat.MIXED_ICEBERG;
    }
  }

  @Before
  public void setupDatabase() {
    sql("USE " + catalog());
    sql("CREATE DATABASE IF NOT EXISTS " + database());
  }

  @After
  public void cleanTable() {
    sql("USE " + catalog());
    sql("DROP TABLE IF EXISTS " + database() + "." + table());
  }

  protected ArcticTable loadTable() {
    ArcticCatalog arcticCatalog = CatalogLoader.load(catalogUrl());
    return arcticCatalog.loadTable(TableIdentifier.of(arcticCatalog(), database(), table()));
  }

  int _id = 0;

  public int nextId() {
    return ++_id;
  }

  protected Types.NestedField optionalTimestampNestedField(String name) {
    return timestampNestedField(name, true);
  }

  protected Types.NestedField timestampNestedField(String name, Boolean optional) {

    switch (tableFormat()) {
      case MIXED_ICEBERG:
      case ICEBERG:
        if (useTimestampWithoutZoneInNewTable()) {
          return Types.NestedField.of(nextId(), optional, name, Types.TimestampType.withoutZone());
        }
      case MIXED_HIVE:
        return Types.NestedField.of(nextId(), optional, name, Types.TimestampType.withoutZone());
      default:
        return Types.NestedField.of(nextId(), optional, name, Types.TimestampType.withZone());
    }
  }

  /**
   * assert table schema equal to expect schema
   */
  protected void assertTableSchema(
      ArcticTable actual, Schema expectSchema, PartitionSpec expectPartitionSpec, PrimaryKeySpec expectKeySpec
  ) {
    Assert.assertEquals(expectKeySpec.primaryKeyExisted(), actual.isKeyedTable());
    Assert.assertEquals(expectPartitionSpec.isPartitioned(), actual.spec().isPartitioned());

    if (expectKeySpec.primaryKeyExisted()) {
      Assert.assertTrue("the table should be keyed table", actual.isKeyedTable());
      PrimaryKeySpec actualKeySpec = actual.asKeyedTable().primaryKeySpec();
      Assert.assertEquals(
          "the table keySpec size not expected",
          expectKeySpec.fieldNames().size(), actualKeySpec.fieldNames().size());

      List<Pair<String, String>> zipValues = CollectionHelper.zip(
          expectKeySpec.fieldNames(),
          actual.asKeyedTable().primaryKeySpec().fieldNames());
      for (Pair<String, String> assertPair : zipValues) {
        Assert.assertEquals("primary key spec should match",
            assertPair.getLeft(), assertPair.getRight());
      }
    }

    if (expectPartitionSpec.isPartitioned()) {
      Assert.assertEquals(expectPartitionSpec.fields().size(), actual.spec().fields().size());

      CollectionHelper.zip(expectPartitionSpec.fields(), actual.spec().fields())
          .forEach(x -> {
            Assert.assertEquals(x.getLeft().name(), x.getRight().name());
            Assert.assertEquals(x.getLeft().transform(), x.getRight().transform());
          });
    }

    assertType(expectSchema.asStruct(), actual.schema().asStruct());
  }

  protected void assertType(Type expect, Type actual) {
    Assert.assertEquals(
        "type should be same",
        expect.isPrimitiveType(), actual.isPrimitiveType());
    if (expect.isPrimitiveType()) {
      Assert.assertEquals(expect, actual);
    } else {
      List<Types.NestedField> expectFields = expect.asNestedType().fields();
      List<Types.NestedField> actualFields = actual.asNestedType().fields();
      Assert.assertEquals(expectFields.size(), actualFields.size());

      CollectionHelper.zip(expectFields, actualFields)
          .forEach(x -> {
            Assert.assertEquals(x.getLeft().name(), x.getRight().name());
            Assert.assertEquals(x.getLeft().isOptional(), x.getRight().isOptional());
            Assert.assertEquals(x.getLeft().doc(), x.getRight().doc());
            assertType(x.getLeft().type(), x.getRight().type());
          });
    }
  }
}
