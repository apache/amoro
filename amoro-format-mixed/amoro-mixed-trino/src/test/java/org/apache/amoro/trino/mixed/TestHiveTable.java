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

package org.apache.amoro.trino.mixed;

import static org.apache.amoro.MockAmoroManagementServer.TEST_CATALOG_NAME;
import static org.apache.amoro.table.TableProperties.BASE_FILE_FORMAT;
import static org.apache.amoro.table.TableProperties.CHANGE_FILE_FORMAT;
import static org.apache.amoro.table.TableProperties.DEFAULT_FILE_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;

import io.trino.sql.query.QueryAssertions;
import io.trino.testing.QueryRunner;
import org.apache.amoro.MockAmoroManagementServer;
import org.apache.amoro.data.ChangeAction;
import org.apache.amoro.hive.io.writer.AdaptHiveGenericTaskWriterBuilder;
import org.apache.amoro.hive.table.HiveLocationKind;
import org.apache.amoro.hive.table.KeyedHiveTable;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableList;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.BaseLocationKind;
import org.apache.amoro.table.ChangeLocationKind;
import org.apache.amoro.table.LocationKind;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableProperties;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.Files;
import org.apache.iceberg.OverwriteFiles;
import org.apache.iceberg.Schema;
import org.apache.iceberg.UpdateProperties;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.data.parquet.AdaptHiveGenericParquetReaders;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.parquet.AdaptHiveParquet;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class TestHiveTable extends TestHiveTableBaseForTrino {

  private final String TEST_HIVE_TABLE_FULL_NAME =
      MixedFormatQueryRunner.MIXED_FORMAT_CATALOG_PREFIX
          + HIVE_TABLE_ID.getDatabase()
          + "."
          + HIVE_TABLE_ID.getTableName();

  private final String TEST_HIVE_PK_TABLE_FULL_NAME =
      MixedFormatQueryRunner.MIXED_FORMAT_CATALOG_PREFIX
          + HIVE_PK_TABLE_ID.getDatabase()
          + "."
          + HIVE_PK_TABLE_ID.getTableName();

  private final String TEST_HIVE_PK_TABLE_FULL_NAME_BASE =
      MixedFormatQueryRunner.MIXED_FORMAT_CATALOG_PREFIX
          + HIVE_PK_TABLE_ID.getDatabase()
          + "."
          + "\""
          + HIVE_PK_TABLE_ID.getTableName()
          + "#base\"";

  private final String TEST_UN_PARTITION_HIVE_TABLE_FULL_NAME =
      MixedFormatQueryRunner.MIXED_FORMAT_CATALOG_PREFIX
          + UN_PARTITION_HIVE_TABLE_ID.getDatabase()
          + "."
          + UN_PARTITION_HIVE_TABLE_ID.getTableName();

  private final String TEST_UN_PARTITION_HIVE_PK_TABLE_FULL_NAME =
      MixedFormatQueryRunner.MIXED_FORMAT_CATALOG_PREFIX
          + UN_PARTITION_HIVE_PK_TABLE_ID.getDatabase()
          + "."
          + UN_PARTITION_HIVE_PK_TABLE_ID.getTableName();

  private final String TEST_UN_PARTITION_HIVE_PK_TABLE_FULL_NAME_BASE =
      MixedFormatQueryRunner.MIXED_FORMAT_CATALOG_PREFIX
          + UN_PARTITION_HIVE_PK_TABLE_ID.getDatabase()
          + "."
          + "\""
          + UN_PARTITION_HIVE_PK_TABLE_ID.getTableName()
          + "#base\"";

  private static final TableIdentifier HIVE_PK_TABLE_ORC_ID =
      TableIdentifier.of(TEST_CATALOG_NAME, HIVE_DB_NAME, "test_pk_hive_table_orc");

  private static final TableIdentifier HIVE_PK_TABLE_PARQUET_ID =
      TableIdentifier.of(TEST_CATALOG_NAME, HIVE_DB_NAME, "test_pk_hive_table_parquet");

  private final String TEST_HIVE_PK_TABLE_ORC_FULL_NAME =
      MixedFormatQueryRunner.MIXED_FORMAT_CATALOG_PREFIX
          + HIVE_PK_TABLE_ID.getDatabase()
          + "."
          + HIVE_PK_TABLE_ORC_ID.getTableName();

  private final String TEST_HIVE_PK_TABLE_PARQUET_FULL_NAME =
      MixedFormatQueryRunner.MIXED_FORMAT_CATALOG_PREFIX
          + HIVE_PK_TABLE_ID.getDatabase()
          + "."
          + HIVE_PK_TABLE_PARQUET_ID.getTableName();

  private KeyedHiveTable testKeyedHiveTableOrc;
  private KeyedHiveTable testKeyedHiveTableParquet;

  private long txid = 1;

  @Override
  protected void setupTables() throws Exception {
    super.setupTables();
    // added for test parquet and orc metrics
    testKeyedHiveTableOrc =
        (KeyedHiveTable)
            hiveCatalog
                .newTableBuilder(HIVE_PK_TABLE_ORC_ID, HIVE_TABLE_SCHEMA)
                .withProperty(TableProperties.LOCATION, warehousePath() + "/pk_table_orc")
                .withPartitionSpec(HIVE_SPEC)
                .withPrimaryKeySpec(PRIMARY_KEY_SPEC)
                .create()
                .asKeyedTable();

    testKeyedHiveTableParquet =
        (KeyedHiveTable)
            hiveCatalog
                .newTableBuilder(HIVE_PK_TABLE_PARQUET_ID, HIVE_TABLE_SCHEMA)
                .withProperty(TableProperties.LOCATION, warehousePath() + "/pk_table_parquet")
                .withPartitionSpec(HIVE_SPEC)
                .withPrimaryKeySpec(PRIMARY_KEY_SPEC)
                .create()
                .asKeyedTable();
  }

  @Override
  protected QueryRunner createQueryRunner() throws Exception {
    AMS = MockAmoroManagementServer.getInstance();
    startMetastore();
    setupTables();
    initData();
    return MixedFormatQueryRunner.builder()
        .setExtraProperties(ImmutableMap.of("http-server.http.port", "8080"))
        .setIcebergProperties(
            ImmutableMap.of(
                "amoro.url",
                String.format("thrift://localhost:%s/%s", AMS.port(), TEST_CATALOG_NAME)))
        .build();
  }

  private void initData() throws IOException {
    write(testHiveTable, BaseLocationKind.INSTANT, HiveTestRecords.baseRecords());
    write(testHiveTable, HiveLocationKind.INSTANT, HiveTestRecords.hiveRecords());

    write(testKeyedHiveTable, ChangeLocationKind.INSTANT, HiveTestRecords.changeInsertRecords());
    write(testKeyedHiveTable, BaseLocationKind.INSTANT, HiveTestRecords.baseRecords());
    write(testKeyedHiveTable, HiveLocationKind.INSTANT, HiveTestRecords.hiveRecords());
    write(
        testKeyedHiveTable,
        ChangeLocationKind.INSTANT,
        HiveTestRecords.changeDeleteRecords(),
        ChangeAction.DELETE);

    write(testUnPartitionHiveTable, BaseLocationKind.INSTANT, HiveTestRecords.baseRecords());
    write(testUnPartitionHiveTable, HiveLocationKind.INSTANT, HiveTestRecords.hiveRecords());

    write(
        testUnPartitionKeyedHiveTable,
        ChangeLocationKind.INSTANT,
        HiveTestRecords.changeInsertRecords());
    write(testUnPartitionKeyedHiveTable, BaseLocationKind.INSTANT, HiveTestRecords.baseRecords());
    write(testUnPartitionKeyedHiveTable, HiveLocationKind.INSTANT, HiveTestRecords.hiveRecords());
    write(
        testUnPartitionKeyedHiveTable,
        ChangeLocationKind.INSTANT,
        HiveTestRecords.changeDeleteRecords(),
        ChangeAction.DELETE);

    write(
        testKeyedHiveTableOrc,
        ChangeLocationKind.INSTANT,
        HiveTestRecords.changeInsertRecords(),
        FileFormat.ORC);
    write(
        testKeyedHiveTableOrc,
        BaseLocationKind.INSTANT,
        HiveTestRecords.baseRecords(),
        FileFormat.ORC);
    write(
        testKeyedHiveTableOrc,
        HiveLocationKind.INSTANT,
        HiveTestRecords.hiveRecords(),
        FileFormat.ORC);
    write(
        testKeyedHiveTableOrc,
        ChangeLocationKind.INSTANT,
        HiveTestRecords.changeDeleteRecords(),
        ChangeAction.DELETE,
        FileFormat.ORC);

    write(
        testKeyedHiveTableParquet,
        ChangeLocationKind.INSTANT,
        HiveTestRecords.changeInsertRecords(),
        FileFormat.PARQUET);
    write(
        testKeyedHiveTableParquet,
        BaseLocationKind.INSTANT,
        HiveTestRecords.baseRecords(),
        FileFormat.PARQUET);
    write(
        testKeyedHiveTableParquet,
        HiveLocationKind.INSTANT,
        HiveTestRecords.hiveRecords(),
        FileFormat.PARQUET);
    write(
        testKeyedHiveTableParquet,
        ChangeLocationKind.INSTANT,
        HiveTestRecords.changeDeleteRecords(),
        ChangeAction.DELETE,
        FileFormat.PARQUET);
  }

  @Test
  public void testHiveTableMOR() throws InterruptedException {
    assertCommon(
        "select id, name, op_time, \"d$d\", map_name, array_name, struct_name from "
            + TEST_HIVE_TABLE_FULL_NAME,
        ImmutableList.of(v1, v2, v3, v4));
  }

  @Test
  public void testKeyedHiveTableMOR() {
    assertCommon(
        "select id, name, op_time, \"d$d\", map_name, array_name, struct_name from "
            + TEST_HIVE_PK_TABLE_FULL_NAME,
        ImmutableList.of(v2, v4, v6));
  }

  @Test
  public void testKeyedHiveTableBase() {
    assertCommon(
        "select id, name, op_time, \"d$d\", map_name, array_name, struct_name from "
            + TEST_HIVE_PK_TABLE_FULL_NAME_BASE,
        ImmutableList.of(v1, v2, v3, v4));
  }

  @Test
  public void testNoPartitionHiveTableMOR() {
    assertCommon(
        "select id, name, op_time, \"d$d\", map_name, array_name, struct_name from "
            + TEST_HIVE_PK_TABLE_FULL_NAME_BASE,
        ImmutableList.of(v1, v2, v3, v4));
  }

  @Test
  public void testNoPartitionKeyedHiveTableMOR() {
    assertCommon(
        "select id, name, op_time, \"d$d\", map_name, array_name, struct_name from "
            + TEST_UN_PARTITION_HIVE_PK_TABLE_FULL_NAME,
        ImmutableList.of(v2, v4, v6));
  }

  @Test
  public void testNoPartitionKeyedHiveTableBase() {
    assertCommon(
        "select id, name, op_time, \"d$d\", map_name, array_name, struct_name from "
            + TEST_UN_PARTITION_HIVE_PK_TABLE_FULL_NAME_BASE,
        ImmutableList.of(v1, v2, v3, v4));
  }

  @Test
  public void testParquetStats() {
    assertThat(query("SHOW STATS FOR " + TEST_HIVE_PK_TABLE_PARQUET_FULL_NAME))
        .skippingTypesCheck()
        .matches(
            "VALUES "
                + "('id', NULL, NULL, 0e0, NULL, '1', '4'), "
                + "('op_time', NULL, NULL, 0e0, NULL, '2022-01-01 12:00:00.000000', '2022-01-04 12:00:00.000000'), "
                + "('op_time_with_zone', NULL, NULL, 0e0, NULL,'2022-01-01 12:00:00.000 UTC', '2022-01-04 12:00:00.000 UTC'), "
                + "('d$d', NULL, NULL, 0e0, NULL, '100.0', '103.0'), "
                + "('map_name', NULL, NULL, NULL, NULL, NULL, NULL), "
                + "('array_name', NULL, NULL, NULL, NULL, NULL, NULL), "
                + "('struct_name', NULL, NULL, NULL, NULL, NULL, NULL), "
                + "('name', 548e0, NULL, 0e0, NULL, NULL, NULL), "
                + "(NULL, NULL, NULL, NULL, 4e0, NULL, NULL)");
  }

  @Test
  public void testOrcStats() {
    assertThat(query("SHOW STATS FOR " + TEST_HIVE_PK_TABLE_ORC_FULL_NAME))
        .skippingTypesCheck()
        .matches(
            "VALUES "
                + "('id', NULL, NULL, 0e0, NULL, '1', '4'), "
                + "('op_time', NULL, NULL, 0e0, NULL, '2022-01-01 12:00:00.000000',"
                + " '2022-01-04 12:00:00.000000'), "
                + "('op_time_with_zone', NULL, NULL, 0e0, NULL, "
                + "'2022-01-01 12:00:00.000 UTC', '2022-01-04 12:00:00.000 UTC'), "
                + "('d$d', NULL, NULL, 0e0, NULL, '100.0', '103.0'), "
                + "('map_name', 24e0, NULL, 0e0, NULL, NULL, NULL), "
                + "('array_name', 24e0, NULL, 0e0, NULL, NULL, NULL), "
                + "('struct_name', 0e0, NULL, 0e0, NULL, NULL, NULL), "
                + "('name', 137e0, NULL, 0e0, NULL, NULL, NULL), "
                + "(NULL, NULL, NULL, NULL, 4e0, NULL, NULL)");
  }

  private void assertCommon(String query, List<List<String>> values) {
    QueryAssertions.QueryAssert queryAssert = assertThat(query(query));
    StringJoiner stringJoiner = new StringJoiner(",", "VALUES", "");
    for (List<String> value : values) {
      stringJoiner.add(value.stream().collect(Collectors.joining(",", "(", ")")));
    }
    queryAssert.skippingTypesCheck().matches(stringJoiner.toString());
  }

  @AfterClass(alwaysRun = true)
  public void clear() {
    clearTable();
    stopMetastore();
  }

  private void write(MixedTable table, LocationKind locationKind, List<Record> records)
      throws IOException {
    write(table, locationKind, records, ChangeAction.INSERT, null);
  }

  private void write(
      MixedTable table, LocationKind locationKind, List<Record> records, ChangeAction changeAction)
      throws IOException {
    write(table, locationKind, records, changeAction, null);
  }

  private void write(
      MixedTable table, LocationKind locationKind, List<Record> records, FileFormat fileFormat)
      throws IOException {
    write(table, locationKind, records, ChangeAction.INSERT, fileFormat);
  }

  private void write(
      MixedTable table,
      LocationKind locationKind,
      List<Record> records,
      ChangeAction changeAction,
      FileFormat fileFormat)
      throws IOException {
    List<TaskWriter<Record>> writers;
    if (fileFormat == null) {
      writers = genWriters(table, locationKind, changeAction, FileFormat.PARQUET, FileFormat.ORC);
    } else {
      writers = genWriters(table, locationKind, changeAction, fileFormat);
    }

    for (int i = 0; i < records.size(); i++) {
      writers.get(i % writers.size()).write(records.get(i));
    }
    for (TaskWriter<Record> writer : writers) {
      WriteResult complete = writer.complete();
      if (locationKind == ChangeLocationKind.INSTANT) {
        AppendFiles appendFiles = table.asKeyedTable().changeTable().newAppend();
        Arrays.stream(complete.dataFiles()).forEach(appendFiles::appendFile);
        appendFiles.commit();
      } else {
        if (table.isUnkeyedTable()) {
          OverwriteFiles overwriteFiles = table.asUnkeyedTable().newOverwrite();
          Arrays.stream(complete.dataFiles()).forEach(overwriteFiles::addFile);
          overwriteFiles.commit();
        } else {
          OverwriteFiles overwriteFiles = table.asKeyedTable().baseTable().newOverwrite();
          Arrays.stream(complete.dataFiles()).forEach(overwriteFiles::addFile);
          overwriteFiles.commit();
        }
      }
    }
  }

  private List<TaskWriter<Record>> genWriters(
      MixedTable table,
      LocationKind locationKind,
      ChangeAction changeAction,
      FileFormat... fileFormat) {
    List<TaskWriter<Record>> result = Lists.newArrayList();
    for (FileFormat format : fileFormat) {
      UpdateProperties updateProperties = table.updateProperties();
      updateProperties.set(BASE_FILE_FORMAT, format.name());
      updateProperties.set(CHANGE_FILE_FORMAT, format.name());
      updateProperties.set(DEFAULT_FILE_FORMAT, format.name());
      updateProperties.commit();
      AdaptHiveGenericTaskWriterBuilder builder =
          AdaptHiveGenericTaskWriterBuilder.builderFor(table);
      TaskWriter<Record> writer =
          builder
              .withChangeAction(changeAction)
              .withTransactionId(table.isKeyedTable() ? txid++ : null)
              .buildWriter(locationKind);
      result.add(writer);
    }
    return result;
  }

  private CloseableIterable<Record> readParquet(Schema schema, String path) {
    AdaptHiveParquet.ReadBuilder builder =
        AdaptHiveParquet.read(Files.localInput(new File(path)))
            .project(schema)
            .createReaderFunc(
                fileSchema ->
                    AdaptHiveGenericParquetReaders.buildReader(schema, fileSchema, new HashMap<>()))
            .caseSensitive(false);

    CloseableIterable<Record> iterable = builder.build();
    return iterable;
  }

  private String base(String table) {
    return "\"" + table + "#base\"";
  }

  List<String> v1 =
      ImmutableList.of(
          "1",
          "varchar 'john'",
          "TIMESTAMP'2022-01-01 12:00:00.000000'",
          "CAST(100 AS decimal(10,0))",
          "map(ARRAY[varchar 'map_key'],ARRAY[varchar 'map_value'])",
          "ARRAY[varchar 'array_element']",
          "CAST(ROW(varchar 'struct_sub1', varchar 'struct_sub2') "
              + "AS ROW(struct_name_sub_1 varchar, struct_name_sub_2 varchar))");

  List<String> v2 =
      ImmutableList.of(
          "2",
          "varchar 'lily'",
          "TIMESTAMP'2022-01-02 12:00:00.000000'",
          "CAST(101 AS decimal(10,0))",
          "map(ARRAY[varchar 'map_key'],ARRAY[varchar 'map_value'])",
          "ARRAY[varchar 'array_element']",
          "CAST(ROW(varchar 'struct_sub1', varchar 'struct_sub2') "
              + "AS ROW(struct_name_sub_1 varchar, struct_name_sub_2 varchar))");

  List<String> v3 =
      ImmutableList.of(
          "3",
          "varchar 'jake'",
          "TIMESTAMP'2022-01-03 12:00:00.000000'",
          "CAST(102 AS decimal(10,0))",
          "map(ARRAY[varchar 'map_key'],ARRAY[varchar 'map_value'])",
          "ARRAY[varchar 'array_element']",
          "CAST(ROW(varchar 'struct_sub1', varchar 'struct_sub2') "
              + "AS ROW(struct_name_sub_1 varchar, struct_name_sub_2 varchar))");

  List<String> v4 =
      ImmutableList.of(
          "4",
          "varchar 'sam'",
          "TIMESTAMP'2022-01-04 12:00:00.000000'",
          "CAST(103 AS decimal(10,0))",
          "map(ARRAY[varchar 'map_key'],ARRAY[varchar 'map_value'])",
          "ARRAY[varchar 'array_element']",
          "CAST(ROW(varchar 'struct_sub1', varchar 'struct_sub2') "
              + "AS ROW(struct_name_sub_1 varchar, struct_name_sub_2 varchar))");

  List<String> v6 =
      ImmutableList.of(
          "6",
          "varchar 'mack'",
          "TIMESTAMP'2022-01-01 12:00:00.000000'",
          "CAST(105 AS decimal(10,0))",
          "map(ARRAY[varchar 'map_key'],ARRAY[varchar 'map_value'])",
          "ARRAY[varchar 'array_element']",
          "CAST(ROW(varchar 'struct_sub1', varchar 'struct_sub2') "
              + "AS ROW(struct_name_sub_1 varchar, struct_name_sub_2 varchar))");
}
