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

package com.netease.arctic.spark.test.sql;

import com.netease.arctic.spark.SparkSQLProperties;
import com.netease.arctic.spark.test.Asserts;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.utils.CollectionHelper;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SessionCatalog(usingArcticSessionCatalog = true)
public class TestCreateTableSQL extends SparkTableTestBase {

  public static Stream<Arguments> argsConfUsingTimestampWithoutZoneInNewTables() {
    return Stream.of(
        Arguments.of(SESSION_CATALOG, false, Types.TimestampType.withoutZone()),
        Arguments.of(SESSION_CATALOG, true, Types.TimestampType.withoutZone()),
        Arguments.of(HIVE_CATALOG, false, Types.TimestampType.withoutZone()),
        Arguments.of(HIVE_CATALOG, true, Types.TimestampType.withoutZone()),
        Arguments.of(INTERNAL_CATALOG, false, Types.TimestampType.withZone()),
        Arguments.of(INTERNAL_CATALOG, true, Types.TimestampType.withoutZone())
    );
  }

  @DisplayName("Test `use-timestamp-without-zone-in-new-tables`")
  @ParameterizedTest
  @MethodSource("argsConfUsingTimestampWithoutZoneInNewTables")
  public void testConfUsingTimestampWithoutZoneInNewTables(
      String catalog, boolean usingTimestampWithoutZone, Types.TimestampType expectType
  ) {

    test().inSparkCatalog(catalog)
        .execute(c -> {
          sql("SET `" + SparkSQLProperties.USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES
              + "`=" + usingTimestampWithoutZone);

          String sqlText = "CREATE TABLE " + c.databaseAndTable + "(\n" +
              "id INT, \n" +
              "ts TIMESTAMP \n) using arctic ";

          Schema schema = new Schema(
              Types.NestedField.optional(1, "id", Types.IntegerType.get()),
              Types.NestedField.optional(2, "ts", expectType)
          );
          sql(sqlText);
          ArcticTable actual = c.loadTable();
          Type actualType = actual.schema().findField("ts").type();
          Assertions.assertEquals(expectType, actualType);
        });
  }

  public static Stream<Arguments> argsPrimaryKeyFieldNotNull() {
    return Stream.of(
        Arguments.of(SESSION_CATALOG, "INT", "", false),
        Arguments.of(SESSION_CATALOG, "INT NOT NULL", "", true),
        Arguments.of(SESSION_CATALOG, "INT", ", PRIMARY KEY(id)", true),
        Arguments.of(SESSION_CATALOG, "INT NOT NULL", ", PRIMARY KEY(id)", true),
        Arguments.of(HIVE_CATALOG, "INT", "", false),
        Arguments.of(HIVE_CATALOG, "INT NOT NULL", "", true),
        Arguments.of(HIVE_CATALOG, "INT", ", PRIMARY KEY(id)", true),
        Arguments.of(HIVE_CATALOG, "INT NOT NULL", ", PRIMARY KEY(id)", true),
        Arguments.of(INTERNAL_CATALOG, "INT", "", false),
        Arguments.of(INTERNAL_CATALOG, "INT NOT NULL", "", true),
        Arguments.of(INTERNAL_CATALOG, "INT", ", PRIMARY KEY(id)", true),
        Arguments.of(INTERNAL_CATALOG, "INT NOT NULL", ", PRIMARY KEY(id)", true)
    );
  }

  /**
   * primary key field should auto convert to not null
   */
  @DisplayName("Test auto add `NOT NULL` for primary key")
  @ParameterizedTest
  @MethodSource("argsPrimaryKeyFieldNotNull")
  public void testPrimaryKeyFieldNotNull(
      String catalog, String idFieldTypeDDL, String primaryKeyDDL, boolean expectRequired
  ) {
    test().inSparkCatalog(catalog)
        .execute(c -> {
          String sqlText = "CREATE TABLE " + c.databaseAndTable + "(\n" +
              "id " + idFieldTypeDDL + ",\n" +
              "DATA string " + primaryKeyDDL + "\n" +
              ") using arctic";

          sql(sqlText);
          Schema actualSchema = c.loadTable().schema();
          Types.NestedField idField = actualSchema.findField("id");
          Assertions.assertEquals(idField.isRequired(), expectRequired);
        });
  }

  public static Stream<Arguments> argsPrimaryKeySpecExist() {
    return Stream.of(
        Arguments.of(INTERNAL_CATALOG, ", PRIMARY KEY(id)", true),
        Arguments.of(INTERNAL_CATALOG, "", false),
        Arguments.of(SESSION_CATALOG, ", PRIMARY KEY(id)", true),
        Arguments.of(SESSION_CATALOG, "", false)
    );
  }

  @DisplayName("Test PRIMARY KEY spec exists.")
  @ParameterizedTest
  @MethodSource("argsPrimaryKeySpecExist")
  public void testPrimaryKeySpecExist(
      String catalog, String primaryKeyDDL, boolean expectKeyedTable
  ) {
    test().inSparkCatalog(catalog)
        .execute(c -> {
          String sqlText = "CREATE TABLE " + c.databaseAndTable + " ( \n" +
              "id int, data string " + primaryKeyDDL + " ) using arctic";
          sql(sqlText);

          ArcticTable actualTable = c.loadTable();

          Assertions.assertEquals(actualTable.isKeyedTable(), expectKeyedTable);
          if (expectKeyedTable) {
            PrimaryKeySpec keySpec = actualTable.asKeyedTable().primaryKeySpec();
            Assertions.assertEquals(1, keySpec.fields().size());
            Assertions.assertTrue(keySpec.fieldNames().contains("id"));
          }
        });
  }

  static Schema schema = new Schema(
      Types.NestedField.required(0, "id", Types.IntegerType.get()),
      Types.NestedField.required(1, "data", Types.StringType.get()),
      Types.NestedField.required(2, "ts", Types.TimestampType.withoutZone()),
      Types.NestedField.required(3, "pt", Types.StringType.get())
  );

  public static Stream<Arguments> testPartitionSpec() {

    return Stream.of(
        Arguments.of(SESSION_CATALOG, "", PartitionSpec.unpartitioned()),
        Arguments.of(SESSION_CATALOG, "PARTITIONED BY (pt)",
            PartitionSpec.builderFor(schema).identity("pt").build()),

        Arguments.of(INTERNAL_CATALOG, "PARTITIONED BY (years(ts))",
            PartitionSpec.builderFor(schema).year("ts").build()),
        Arguments.of(INTERNAL_CATALOG, "PARTITIONED BY (months(ts))",
            PartitionSpec.builderFor(schema).month("ts").build()),
        Arguments.of(INTERNAL_CATALOG, "PARTITIONED BY (days(ts))",
            PartitionSpec.builderFor(schema).day("ts").build()),
        Arguments.of(INTERNAL_CATALOG, "PARTITIONED BY (date(ts))",
            PartitionSpec.builderFor(schema).day("ts").build()),
        Arguments.of(INTERNAL_CATALOG, "PARTITIONED BY (hours(ts))",
            PartitionSpec.builderFor(schema).hour("ts").build()),
        Arguments.of(INTERNAL_CATALOG, "PARTITIONED BY (date_hour(ts))",
            PartitionSpec.builderFor(schema).hour("ts").build()),

        Arguments.of(INTERNAL_CATALOG, "PARTITIONED BY (bucket(4, id))",
            PartitionSpec.builderFor(schema).bucket("id", 4).build()),
        Arguments.of(INTERNAL_CATALOG, "PARTITIONED BY (truncate(10, data))",
            PartitionSpec.builderFor(schema).truncate("data", 10).build()),
        Arguments.of(INTERNAL_CATALOG, "PARTITIONED BY (truncate(10, id))",
            PartitionSpec.builderFor(schema).truncate("id", 10).build())
    );
  }

  @DisplayName("Test PartitionSpec is right")
  @ParameterizedTest
  @MethodSource()
  public void testPartitionSpec(
      String catalog, String partitionDDL, PartitionSpec expectSpec
  ) {
    test().inSparkCatalog(catalog)
        .execute(c -> {
          String sqlText = "CREATE TABLE " + c.databaseAndTable + " ( \n" +
              "id int, " +
              "data string, " +
              "ts timestamp, " +
              "pt string, " +
              " PRIMARY KEY(id) ) using arctic " + partitionDDL;

          sql(sqlText);

          ArcticTable actualTable = c.loadTable();
          assertEquals(actualTable.spec().isPartitioned(), expectSpec.isPartitioned());
          if (expectSpec.isPartitioned()) {
            PartitionSpec spec = actualTable.spec();
            assertEquals(expectSpec.fields().size(), spec.fields().size());
            CollectionHelper.zip(expectSpec.fields(), spec.fields())
                .forEach(x -> {
                  assertEquals(x.getLeft().name(), x.getRight().name());
                  assertEquals(x.getLeft().transform(), x.getRight().transform());
                });
          }
          if (c.isHiveCatalog()) {
            Table hiveTable = c.loadHiveTable();
            List<String> hivePartitions =
                hiveTable.getPartitionKeys().stream()
                    .map(FieldSchema::getName)
                    .collect(Collectors.toList());
            assertEquals(expectSpec.fields().size(), hivePartitions.size());
            CollectionHelper.zip(expectSpec.fields(), hivePartitions)
                .forEach(x -> assertEquals(x.getLeft().name(), x.getRight()));

            List<String> hiveCols = hiveTable.getSd().getCols().stream()
                .map(FieldSchema::getName)
                .collect(Collectors.toList());
            expectSpec.fields().forEach(x -> {
              String specField = schema.findField(x.sourceId()).name();
              assertFalse(hiveCols.contains(specField));
            });
          }
        });
  }

  public static Stream<Arguments> testSchemaAndProperties() {
    Types.NestedField id = Types.NestedField.optional(1, "id", Types.IntegerType.get());
    Types.NestedField data = Types.NestedField.required(2, "data", Types.StringType.get());
    Types.NestedField point = Types.NestedField.optional(3, "point", Types.StructType.of(
        Types.NestedField.required(4, "x", Types.DoubleType.get()),
        Types.NestedField.required(5, "y", Types.DoubleType.get())
    ));
    Types.NestedField map = Types.NestedField.optional(6, "maps", Types.MapType.ofOptional(
        7, 8, Types.StringType.get(), Types.StringType.get()));
    Types.NestedField array = Types.NestedField.optional(
        9, "arrays", Types.ListType.ofOptional(10, Types.StringType.get()));
    Types.NestedField ptStr = Types.NestedField.optional(11, "pt", Types.StringType.get());
    Types.NestedField ptTs = Types.NestedField.optional(11, "pt", Types.TimestampType.withoutZone());

    String ptStringStructDDL = "id INT,\n" +
        "data string NOT NULL,\n" +
        "point struct<x: double NOT NULL, y: double NOT NULL>,\n" +
        "maps map<string, string>,\n" +
        "arrays array<string>,\n" +
        "pt string ";
    String ptTimestampStructDDL = "id INT,\n" +
        "data string NOT NULL,\n" +
        "point struct<x: double NOT NULL, y: double NOT NULL>,\n" +
        "maps map<string, string>,\n" +
        "arrays array<string>,\n" +
        "pt timestamp ";

    return Stream.of(
        Arguments.of(
            INTERNAL_CATALOG,
            ptTimestampStructDDL,
            "TBLPROPERTIES('key'='value1', 'catalog'='INTERNAL')",
            new Schema(Lists.newArrayList(id, data, point, map, array, ptTs)),
            asMap("key", "value1", "catalog", "INTERNAL")),
        Arguments.of(
            INTERNAL_CATALOG,
            ptTimestampStructDDL + ", PRIMARY KEY(id)",
            "TBLPROPERTIES('key'='value1', 'catalog'='INTERNAL')",
            new Schema(Lists.newArrayList(id.asRequired(), data, point, map, array, ptTs)),
            asMap("key", "value1", "catalog", "INTERNAL")),

        Arguments.of(
            HIVE_CATALOG,
            ptStringStructDDL,
            "tblproperties('key'='value1', 'catalog'='hive')",
            new Schema(Lists.newArrayList(id, data, point, map, array, ptStr)),
            asMap("key", "value1", "catalog", "hive")),
        Arguments.of(
            HIVE_CATALOG,
            ptStringStructDDL + ", PRIMARY KEY(id)",
            "tblproperties('key'='value1', 'catalog'='hive')",
            new Schema(Lists.newArrayList(id.asRequired(), data, point, map, array, ptStr)),
            asMap("key", "value1", "catalog", "hive")),

        Arguments.of(
            SESSION_CATALOG,
            ptStringStructDDL,
            "TBLPROPERTIES('key'='value1', 'CATALOG'='session')",
            new Schema(Lists.newArrayList(id, data, point, map, array, ptStr)),
            asMap("key", "value1", "CATALOG", "session")),
        Arguments.of(
            SESSION_CATALOG,
            ptStringStructDDL + ", PRIMARY KEY(id)",
            "tblproperties('key'='value1', 'CATALOG'='session')",
            new Schema(Lists.newArrayList(id.asRequired(), data, point, map, array, ptStr)),
            asMap("key", "value1", "CATALOG", "session")
        ));
  }

  @DisplayName("Test primary key, schema and properties")
  @ParameterizedTest
  @MethodSource
  public void testSchemaAndProperties(
      String catalog, String structDDL, String propertiesDDL,
      Schema expectSchema, Map<String, String> expectProperties
  ) {

    test().inSparkCatalog(catalog)
        .execute(c -> {
          sql("SET `" + SparkSQLProperties.USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES
              + "`= true ");
          String sqlText = "CREATE TABLE " + c.databaseAndTable + "(" +
              structDDL + ") using arctic " + propertiesDDL;
          sql(sqlText);

          ArcticTable tbl = c.loadTable();

          Asserts.assertType(expectSchema.asStruct(), tbl.schema().asStruct());

          Asserts.assertHashMapContainExpect(expectProperties, tbl.properties());
          if (isHiveCatalog(catalog)) {
            Table hiveTable = c.loadHiveTable();
            Asserts.assertHiveSchema(hiveTable, expectSchema);
          }
        });
  }
}
