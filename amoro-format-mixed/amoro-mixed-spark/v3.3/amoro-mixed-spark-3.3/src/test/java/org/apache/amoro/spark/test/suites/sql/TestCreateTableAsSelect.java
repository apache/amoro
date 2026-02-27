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
import org.apache.amoro.spark.test.extensions.EnableCatalogSelect;
import org.apache.amoro.spark.test.utils.Asserts;
import org.apache.amoro.spark.test.utils.DataComparator;
import org.apache.amoro.spark.test.utils.TableFiles;
import org.apache.amoro.spark.test.utils.TestTableUtil;
import org.apache.amoro.spark.test.utils.TestTables;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.types.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@EnableCatalogSelect
@EnableCatalogSelect.SelectCatalog(byTableFormat = true)
public class TestCreateTableAsSelect extends MixedTableTestBase {

  public static final Schema SIMPLE_SOURCE_SCHEMA = TestTables.MixedIceberg.NO_PK_PT.schema;
  public static final List<Record> SIMPLE_SOURCE_DATA =
      TestTables.MixedIceberg.PK_PT.newDateGen().records(10);

  public static Stream<Arguments> testTimestampZoneHandle() {
    return Stream.of(
        Arguments.of(
            TableFormat.MIXED_ICEBERG,
            "PRIMARY KEY(id, pt)",
            true,
            Types.TimestampType.withoutZone()),
        Arguments.of(TableFormat.MIXED_ICEBERG, "", false, Types.TimestampType.withZone()),
        Arguments.of(
            TableFormat.MIXED_HIVE, "PRIMARY KEY(id, pt)", true, Types.TimestampType.withoutZone()),
        Arguments.of(TableFormat.MIXED_HIVE, "", false, Types.TimestampType.withoutZone()));
  }

  @ParameterizedTest
  @MethodSource
  public void testTimestampZoneHandle(
      TableFormat format,
      String primaryKeyDDL,
      boolean timestampWithoutZone,
      Types.TimestampType expectType) {
    createViewSource(SIMPLE_SOURCE_SCHEMA, SIMPLE_SOURCE_DATA);

    spark()
        .conf()
        .set(
            SparkSQLProperties.USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES, timestampWithoutZone);

    String sqlText =
        "CREATE TABLE "
            + target()
            + " "
            + primaryKeyDDL
            + " USING "
            + provider(format)
            + " AS SELECT * FROM "
            + source();
    sql(sqlText);

    MixedTable table = loadTable();
    Types.NestedField f = table.schema().findField("ts");
    Asserts.assertType(expectType, f.type());
  }

  private static PartitionSpec.Builder ptBuilder() {
    return PartitionSpec.builderFor(SIMPLE_SOURCE_SCHEMA);
  }

  public static Stream<Arguments> testSchemaAndData() {
    PrimaryKeySpec keyIdPtSpec =
        PrimaryKeySpec.builderFor(SIMPLE_SOURCE_SCHEMA).addColumn("id").addColumn("pt").build();
    PrimaryKeySpec keyIdSpec =
        PrimaryKeySpec.builderFor(SIMPLE_SOURCE_SCHEMA).addColumn("id").build();

    return Stream.of(
        Arguments.of(
            TableFormat.MIXED_HIVE,
            "PRIMARY KEY(id, pt)",
            "PARTITIONED BY(pt)",
            keyIdPtSpec,
            ptBuilder().identity("pt").build()),
        Arguments.of(
            TableFormat.MIXED_HIVE,
            "PRIMARY KEY(id, pt)",
            "",
            keyIdPtSpec,
            PartitionSpec.unpartitioned()),
        Arguments.of(
            TableFormat.MIXED_HIVE,
            "",
            "PARTITIONED BY(pt)",
            PrimaryKeySpec.noPrimaryKey(),
            ptBuilder().identity("pt").build()),
        Arguments.of(
            TableFormat.MIXED_HIVE,
            "",
            "",
            PrimaryKeySpec.noPrimaryKey(),
            PartitionSpec.unpartitioned()),
        Arguments.of(
            TableFormat.MIXED_ICEBERG,
            "PRIMARY KEY(id, pt)",
            "",
            keyIdPtSpec,
            PartitionSpec.unpartitioned()),
        Arguments.of(
            TableFormat.MIXED_ICEBERG,
            "",
            "PARTITIONED BY(pt,id)",
            PrimaryKeySpec.noPrimaryKey(),
            ptBuilder().identity("pt").identity("id").build()),
        Arguments.of(
            TableFormat.MIXED_ICEBERG,
            "PRIMARY KEY(id)",
            "PARTITIONED BY(years(ts))",
            keyIdSpec,
            ptBuilder().year("ts").build()),
        Arguments.of(
            TableFormat.MIXED_ICEBERG,
            "PRIMARY KEY(id)",
            "PARTITIONED BY(months(ts))",
            keyIdSpec,
            ptBuilder().month("ts").build()),
        Arguments.of(
            TableFormat.MIXED_ICEBERG,
            "PRIMARY KEY(id)",
            "PARTITIONED BY(days(ts))",
            keyIdSpec,
            ptBuilder().day("ts").build()),
        Arguments.of(
            TableFormat.MIXED_ICEBERG,
            "PRIMARY KEY(id)",
            "PARTITIONED BY(date(ts))",
            keyIdSpec,
            ptBuilder().day("ts").build()),
        Arguments.of(
            TableFormat.MIXED_ICEBERG,
            "PRIMARY KEY(id)",
            "PARTITIONED BY(hours(ts))",
            keyIdSpec,
            ptBuilder().hour("ts").build()),
        Arguments.of(
            TableFormat.MIXED_ICEBERG,
            "PRIMARY KEY(id)",
            "PARTITIONED BY(date_hour(ts))",
            keyIdSpec,
            ptBuilder().hour("ts").build()),
        Arguments.of(
            TableFormat.MIXED_ICEBERG,
            "PRIMARY KEY(id)",
            "PARTITIONED BY(bucket(10, id))",
            keyIdSpec,
            ptBuilder().bucket("id", 10).build()),
        Arguments.of(
            TableFormat.MIXED_ICEBERG,
            "PRIMARY KEY(id)",
            "PARTITIONED BY(truncate(10, data))",
            keyIdSpec,
            ptBuilder().truncate("data", 10).build()));
  }

  @ParameterizedTest
  @MethodSource
  public void testSchemaAndData(
      TableFormat format,
      String primaryKeyDDL,
      String partitionDDL,
      PrimaryKeySpec keySpec,
      PartitionSpec ptSpec) {
    spark().conf().set("spark.sql.session.timeZone", "UTC");
    createViewSource(SIMPLE_SOURCE_SCHEMA, SIMPLE_SOURCE_DATA);

    spark().conf().set(SparkSQLProperties.USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES, true);

    String sqlText =
        "CREATE TABLE "
            + target()
            + " "
            + primaryKeyDDL
            + " USING "
            + provider(format)
            + " "
            + partitionDDL
            + " AS SELECT * FROM "
            + source();
    sql(sqlText);

    Schema expectSchema = TestTableUtil.toSchemaWithPrimaryKey(SIMPLE_SOURCE_SCHEMA, keySpec);
    expectSchema = TestTableUtil.timestampToWithoutZone(expectSchema);

    MixedTable table = loadTable();
    Asserts.assertPartition(ptSpec, table.spec());
    Assertions.assertEquals(keySpec.primaryKeyExisted(), table.isKeyedTable());
    if (table.isKeyedTable()) {
      Asserts.assertPrimaryKey(keySpec, table.asKeyedTable().primaryKeySpec());
    }
    Asserts.assertType(expectSchema.asStruct(), table.schema().asStruct());
    TableFiles files = TestTableUtil.files(table);
    Asserts.assertAllFilesInBaseStore(files);

    if (TableFormat.MIXED_HIVE.equals(format)) {
      Table hiveTable = loadHiveTable();
      Asserts.assertHiveColumns(expectSchema, ptSpec, hiveTable.getSd().getCols());
      Asserts.assertHivePartition(ptSpec, hiveTable.getPartitionKeys());
      Asserts.assertAllFilesInHiveLocation(files, hiveTable.getSd().getLocation());
    }

    List<Record> records = TestTableUtil.tableRecords(table);
    DataComparator.build(SIMPLE_SOURCE_DATA, records)
        .ignoreOrder(Comparator.comparing(r -> (Integer) r.get(0)))
        .assertRecordsEqual();
  }

  public static Stream<Arguments> testSourceDuplicateCheck() {
    List<Record> duplicateSource = Lists.newArrayList(SIMPLE_SOURCE_DATA);
    duplicateSource.add(SIMPLE_SOURCE_DATA.get(0));

    return Stream.of(
        Arguments.of(TableFormat.MIXED_ICEBERG, SIMPLE_SOURCE_DATA, "PRIMARY KEY(id, pt)", false),
        Arguments.of(TableFormat.MIXED_ICEBERG, SIMPLE_SOURCE_DATA, "", false),
        Arguments.of(TableFormat.MIXED_ICEBERG, duplicateSource, "", false),
        Arguments.of(TableFormat.MIXED_ICEBERG, duplicateSource, "PRIMARY KEY(id, pt)", true),
        Arguments.of(TableFormat.MIXED_HIVE, SIMPLE_SOURCE_DATA, "PRIMARY KEY(id, pt)", false),
        Arguments.of(TableFormat.MIXED_HIVE, SIMPLE_SOURCE_DATA, "", false),
        Arguments.of(TableFormat.MIXED_HIVE, duplicateSource, "", false),
        Arguments.of(TableFormat.MIXED_HIVE, duplicateSource, "PRIMARY KEY(id, pt)", true));
  }

  @ParameterizedTest(name = "{index} {0} {2} {3}")
  @MethodSource
  public void testSourceDuplicateCheck(
      TableFormat format,
      List<Record> sourceData,
      String primaryKeyDDL,
      boolean duplicateCheckFailed) {
    spark().conf().set(SparkSQLProperties.CHECK_SOURCE_DUPLICATES_ENABLE, "true");
    createViewSource(SIMPLE_SOURCE_SCHEMA, sourceData);
    String sqlText =
        "CREATE TABLE "
            + target()
            + " "
            + primaryKeyDDL
            + " USING "
            + provider(format)
            + " "
            + " AS SELECT * FROM "
            + source();

    boolean exceptionCatched = false;
    try {
      sql(sqlText);
    } catch (Exception e) {
      exceptionCatched = true;
    }

    Assertions.assertEquals(duplicateCheckFailed, exceptionCatched);
  }

  public static Stream<Arguments> testAdditionProperties() {
    String propertiesDDL = "TBLPROPERTIES('k1'='v1', 'k2'='v2')";
    Map<String, String> expectProperties = ImmutableMap.of("k1", "v1", "k2", "v2");
    Map<String, String> emptyProperties = Collections.emptyMap();
    return Stream.of(
        Arguments.of(TableFormat.MIXED_ICEBERG, "PRIMARY KEY(id, pt)", "", emptyProperties),
        Arguments.of(
            TableFormat.MIXED_ICEBERG, "PRIMARY KEY(id, pt)", propertiesDDL, expectProperties),
        Arguments.of(TableFormat.MIXED_ICEBERG, "", propertiesDDL, expectProperties),
        Arguments.of(TableFormat.MIXED_HIVE, "PRIMARY KEY(id, pt)", "", emptyProperties),
        Arguments.of(
            TableFormat.MIXED_HIVE, "PRIMARY KEY(id, pt)", propertiesDDL, expectProperties),
        Arguments.of(TableFormat.MIXED_HIVE, "", propertiesDDL, expectProperties));
  }

  @ParameterizedTest
  @MethodSource
  public void testAdditionProperties(
      TableFormat format,
      String primaryKeyDDL,
      String propertiesDDL,
      Map<String, String> expectProperties) {
    createViewSource(SIMPLE_SOURCE_SCHEMA, SIMPLE_SOURCE_DATA);
    String sqlText =
        "CREATE TABLE "
            + target()
            + " "
            + primaryKeyDDL
            + " USING "
            + provider(format)
            + " PARTITIONED BY (pt) "
            + propertiesDDL
            + " AS SELECT * FROM "
            + source();
    sql(sqlText);
    MixedTable table = loadTable();
    Map<String, String> tableProperties = table.properties();
    Asserts.assertHashMapContainExpect(expectProperties, tableProperties);
  }

  // TODO: test optimize write for ctas.
}
