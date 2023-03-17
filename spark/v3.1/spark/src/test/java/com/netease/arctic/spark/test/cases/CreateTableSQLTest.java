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

package com.netease.arctic.spark.test.cases;

import com.netease.arctic.spark.test.SessionCatalog;
import com.netease.arctic.spark.test.SparkTestBase;
import com.netease.arctic.table.PrimaryKeySpec;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.types.Types;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

/**
 * SQL Test for: Create Table <br>
 * <p>
 * Check 1: auto convert timestamp.
 * case1: mixed-hive, timestamp must be without-zone
 * case2: mixed-iceberg, timestamp depend on global config.
 */
@SessionCatalog(usingArcticSessionCatalog = true)
public class CreateTableSQLTest extends SparkTestBase {

  public static Stream<Arguments> createTableSQLTestArgs() {
    return Stream.of(
        Arguments.arguments("session", true, true, false),
        Arguments.arguments("session", true, false, false),
        Arguments.arguments("session", false, true, false),
        Arguments.arguments("session", false, false, false),

        Arguments.arguments("hive", true, true, false),
        Arguments.arguments("hive", true, false, false),
        Arguments.arguments("hive", false, true, false),
        Arguments.arguments("hive", false, false, false),

        Arguments.arguments("arctic", true, true, true),
        Arguments.arguments("arctic", true, false, true),
        Arguments.arguments("arctic", false, true, true),
        Arguments.arguments("arctic", false, false, true),

        Arguments.arguments("arctic", true, true, false),
        Arguments.arguments("arctic", true, false, false),
        Arguments.arguments("arctic", false, true, false),
        Arguments.arguments("arctic", false, false, false)
    );
  }

  boolean isArcticHiveCatalog(String sparkCatalogType) {
    return "SESSION".equalsIgnoreCase(sparkCatalogType) || "HIVE".equalsIgnoreCase(sparkCatalogType);
  }

  @ParameterizedTest(name = "{index} ==> Catalog:{0} WithKey:{1} WithPartition{2} TimestampWithoutZone:{3}")
  @MethodSource("createTableSQLTestArgs")
  @DisplayName("SQL Tests: CREATE TABLE ")
  public void createTableSQLTest(
      String catalogType, boolean withKey, boolean withPartition, boolean timestampWithoutZone
  ) {
    final String commonStructDDL = "id INT,\n" +
        "data string NOT NULL,\n" +
        "point struct<x: double NOT NULL, y: double NOT NULL>,\n" +
        "maps map<string, string>,\n" +
        "arrays array<string>,\n" +
        "ts timestamp,\n";
    final String mixedHiveStructDDL = commonStructDDL + "pt string";
    final String mixedIcebergStructDDL = commonStructDDL + "pt timestamp";
    final String primaryKeyDDL = ",\nPRIMARY KEY(id) ";
    final boolean isMixedHiveTable = isArcticHiveCatalog(catalogType);

    String structDDL = isMixedHiveTable ? mixedHiveStructDDL : mixedIcebergStructDDL;
    final Schema expectSchema = expectSchema(
        id(withKey), commonFields, partitionField(isMixedHiveTable, timestampWithoutZone),
        isMixedHiveTable, timestampWithoutZone
    );
    final String keyDDL = withKey ? primaryKeyDDL : "";
    PrimaryKeySpec.Builder keyBuilder = PrimaryKeySpec.builderFor(expectSchema);
    final PrimaryKeySpec keySpec = withKey ? keyBuilder.addColumn("id").build() : keyBuilder.build();
    String partitionDDL = "";
    PartitionSpec spec = PartitionSpec.unpartitioned();
    if (withPartition && isMixedHiveTable) {
      partitionDDL = "PARTITIONED BY (pt)";
      spec = PartitionSpec.builderFor(expectSchema).identity("pt").build();
    } else if (withPartition) {
      partitionDDL = "PARTITIONED BY (days(pt))";
      spec = PartitionSpec.builderFor(expectSchema).day("pt").build();
    }

    final String sqlText = "CREATE TABLE ";
  }

  private static Types.NestedField partitionField(
      boolean mixedHive, boolean timestampWithoutZone
  ) {
    if (mixedHive) {
      return Types.NestedField.optional(20, "pt", Types.StringType.get());
    } else if (timestampWithoutZone) {
      return Types.NestedField.optional(20, "pt", Types.TimestampType.withoutZone());
    } else {
      return Types.NestedField.optional(20, "pt", Types.TimestampType.withZone());
    }
  }

  List<Types.NestedField> commonFields = Lists.newArrayList(
      Types.NestedField.required(2, "data", Types.StringType.get()),
      Types.NestedField.optional(
          3, "point", Types.StructType.of(
              Types.NestedField.required(4, "x", Types.DoubleType.get()),
              Types.NestedField.required(5, "y", Types.DoubleType.get())
          )),
      Types.NestedField.optional(
          6, "maps", Types.MapType.ofOptional(
              7, 8,
              Types.StringType.get(), Types.StringType.get()
          )),
      Types.NestedField.optional(
          9, "arrays", Types.ListType.ofOptional(
              10, Types.StringType.get()
          ))
  );

  Types.NestedField id(boolean isPrimaryKey) {
    return Types.NestedField.of(1, !isPrimaryKey, "id", Types.IntegerType.get());
  }

  private static Schema expectSchema(
      Types.NestedField keyField, List<Types.NestedField> commonField,
      Types.NestedField partitionField, boolean mixedHive, boolean timestampWithoutZone
  ) {
    List<Types.NestedField> fieldLists = Lists.newArrayList();
    fieldLists.add(keyField);
    fieldLists.addAll(commonField);
    if (mixedHive || timestampWithoutZone) {
      fieldLists.add(Types.NestedField.optional(11, "ts", Types.TimestampType.withoutZone()));
    } else {
      fieldLists.add(Types.NestedField.optional(11, "ts", Types.TimestampType.withZone()));
    }
    fieldLists.add(partitionField);
    return new Schema(fieldLists);
  }

  public void testTimestampZoneHandleInCreateSQL(
      String catalog, boolean usingTimestampWithoutZone, Types.TimestampType expectTimestampType
  ) {
    String sqlText = "CREATE TABLE " + database + "." + table + "(\n" +
        "id INT, \n" +
        "ts TIMESTAMP \n) using arctic ";

    Schema schema = new Schema(
        Types.NestedField.optional(1, "id", Types.IntegerType.get()),
        Types.NestedField.optional(2, "ts", expectTimestampType)
    );

    sql(sqlText);
  }
}
