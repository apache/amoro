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

package org.apache.amoro.spark.test.suites.sql;

import org.apache.amoro.TableFormat;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.spark.mixed.SparkSQLProperties;
import org.apache.amoro.spark.test.MixedTableTestBase;
import org.apache.amoro.spark.test.TestIdentifier;
import org.apache.amoro.spark.test.extensions.EnableCatalogSelect;
import org.apache.amoro.spark.test.utils.Asserts;
import org.apache.amoro.spark.test.utils.TestTable;
import org.apache.amoro.spark.test.utils.TestTables;
import org.apache.amoro.table.MixedTable;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

@EnableCatalogSelect
@EnableCatalogSelect.SelectCatalog(byTableFormat = true)
public class TestCreateTableLikeSQL extends MixedTableTestBase {

  public static Stream<Arguments> testTimestampZoneHandle() {
    return Stream.of(
        Arguments.of(TableFormat.MIXED_ICEBERG, false, Types.TimestampType.withZone()),
        Arguments.of(TableFormat.MIXED_ICEBERG, true, Types.TimestampType.withoutZone()),
        Arguments.of(TableFormat.MIXED_HIVE, false, Types.TimestampType.withoutZone()),
        Arguments.of(TableFormat.MIXED_HIVE, true, Types.TimestampType.withoutZone()));
  }

  @DisplayName("TestSQL: CREATE TABLE LIKE handle timestamp type in new table.")
  @ParameterizedTest
  @MethodSource
  public void testTimestampZoneHandle(
      TableFormat format, boolean newTableTimestampWithoutZone, Type expectTimestampType) {
    Schema schema =
        new Schema(
            Types.NestedField.required(1, "id", Types.IntegerType.get()),
            Types.NestedField.optional(2, "ts", Types.TimestampType.withZone()));
    createMixedFormatSource(schema, x -> {});

    spark()
        .conf()
        .set(
            SparkSQLProperties.USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES,
            newTableTimestampWithoutZone);
    sql("CREATE TABLE " + target() + " LIKE " + source() + " USING " + provider(format));

    MixedTable table = loadTable();
    Types.NestedField tsField = table.schema().findField("ts");
    Asserts.assertType(expectTimestampType, tsField.type());
  }

  public static Stream<Arguments> testCreateTableLikeHiveTable() {
    return Stream.of(
        Arguments.of(TestTables.MixedHive.NO_PK_NO_PT),
        Arguments.of(TestTables.MixedHive.NO_PK_PT));
  }

  @DisplayName("Test SQL: CREATE TABLE LIKE hive table")
  @ParameterizedTest
  @MethodSource
  @EnableCatalogSelect.SelectCatalog(use = SPARK_SESSION_CATALOG)
  public void testCreateTableLikeHiveTable(TestTable source) {
    createHiveSource(source.hiveSchema, source.hivePartitions, ImmutableMap.of("k1", "v1"));

    String sqlText = "CREATE TABLE " + target() + " LIKE " + source() + " USING arctic";
    sql(sqlText);
    MixedTable table = loadTable();
    Asserts.assertType(source.schema.asStruct(), table.schema().asStruct());
    Asserts.assertPartition(source.ptSpec, table.spec());
    // CREATE TABLE LIKE do not copy properties.
    Assertions.assertFalse(table.properties().containsKey("k1"));
  }

  public static Stream<Arguments> testCreateTableLikeDataLakeTable() {
    List<TestTable> tables =
        Lists.newArrayList(
            TestTables.MixedHive.NO_PK_NO_PT,
            TestTables.MixedHive.NO_PK_PT,
            TestTables.MixedHive.PK_NO_PT,
            TestTables.MixedHive.PK_PT,
            TestTables.MixedIceberg.NO_PK_NO_PT,
            TestTables.MixedIceberg.PK_NO_PT,
            TestTables.MixedIceberg.PK_PT,
            TestTables.MixedIceberg.NO_PK_PT);
    return tables.stream().map(t -> Arguments.of(t.format, t));
  }

  @DisplayName("Test SQL: CREATE TABLE LIKE data-lake table")
  @ParameterizedTest
  @MethodSource
  public void testCreateTableLikeDataLakeTable(TableFormat format, TestTable source) {
    MixedTable expect =
        createMixedFormatSource(
            source.schema,
            builder ->
                builder
                    .withPartitionSpec(source.ptSpec)
                    .withPrimaryKeySpec(source.keySpec)
                    .withProperty("k1", "v1"));

    spark().conf().set(SparkSQLProperties.USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES, true);
    String sqlText =
        "CREATE TABLE " + target() + " LIKE " + source() + " USING " + provider(format);
    sql(sqlText);

    MixedTable table = loadTable();
    Asserts.assertType(expect.schema().asStruct(), table.schema().asStruct());
    Asserts.assertPartition(expect.spec(), table.spec());
    Assertions.assertEquals(expect.isKeyedTable(), table.isKeyedTable());
    // CREATE TABLE LIKE do not copy properties.
    Assertions.assertFalse(table.properties().containsKey("k1"));
    if (expect.isKeyedTable()) {
      Asserts.assertPrimaryKey(
          expect.asKeyedTable().primaryKeySpec(), table.asKeyedTable().primaryKeySpec());
    }
  }

  public static Stream<Arguments> testCreateTableWithoutProviderInSessionCatalog() {
    return Stream.of(Arguments.of("", false), Arguments.of("USING arctic", true));
  }

  @DisplayName("TestSQL: CREATE TABLE LIKE without USING ARCTIC")
  @ParameterizedTest(name = "{index} provider = {0} ")
  @MethodSource
  @EnableCatalogSelect.SelectCatalog(use = SPARK_SESSION_CATALOG)
  public void testCreateTableWithoutProviderInSessionCatalog(
      String provider, boolean expectCreate) {
    TestTable source = TestTables.MixedHive.PK_PT;
    createHiveSource(source.hiveSchema, source.hivePartitions);

    sql("CREATE TABLE " + target() + " LIKE " + source() + " " + provider);
    Assertions.assertEquals(expectCreate, tableExists());
    if (!expectCreate) {
      // not a mixed-format table.
      TestIdentifier target = target();
      CONTEXT.dropHiveTable(target.database, target.table);
    }
  }
}
