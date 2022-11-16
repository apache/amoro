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

package com.netease.arctic.ams.server.optimize;

import com.netease.arctic.ams.api.DataFileInfo;
import com.netease.arctic.hive.table.KeyedHiveTable;
import com.netease.arctic.hive.table.UnkeyedHiveTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableIdentifier;
import org.apache.commons.io.FileUtils;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableMap;
import org.apache.iceberg.types.Types;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static com.netease.arctic.ams.server.AmsTestBase.AMS_TEST_HIVE_CATALOG_NAME;
import static com.netease.arctic.ams.server.AmsTestBase.AMS_TEST_HIVE_DB_NAME;
import static com.netease.arctic.ams.server.AmsTestBase.hiveCatalog;

public class TestSupportHiveBase implements TestOptimizeBase {

  private static final File testHiveTableBaseDir = new File("/hive_tmp");
  private static final File testHiveBaseDir = new File("unit_test_hive_base_tmp");

  public static final TableIdentifier HIVE_TABLE_ID =
      TableIdentifier.of(AMS_TEST_HIVE_CATALOG_NAME, AMS_TEST_HIVE_DB_NAME, "test_hive_table");
  public static final TableIdentifier HIVE_PK_TABLE_ID =
      TableIdentifier.of(AMS_TEST_HIVE_CATALOG_NAME, AMS_TEST_HIVE_DB_NAME, "test_pk_hive_table");

  public static final TableIdentifier UN_PARTITION_HIVE_TABLE_ID =
      TableIdentifier.of(AMS_TEST_HIVE_CATALOG_NAME, AMS_TEST_HIVE_DB_NAME, "un_partition_test_hive_table");
  public static final TableIdentifier UN_PARTITION_HIVE_PK_TABLE_ID =
      TableIdentifier.of(AMS_TEST_HIVE_CATALOG_NAME, AMS_TEST_HIVE_DB_NAME, "un_partition_test_pk_hive_table");

  public static final Schema HIVE_TABLE_SCHEMA = new Schema(
      Types.NestedField.required(1, "id", Types.IntegerType.get()),
      Types.NestedField.required(2, "op_time", Types.TimestampType.withoutZone()),
      Types.NestedField.required(3, "op_time_with_zone", Types.TimestampType.withZone()),
      Types.NestedField.required(4, "d", Types.DecimalType.of(10, 0)),
      Types.NestedField.required(5, "name", Types.StringType.get())
  );

  protected static final PartitionSpec HIVE_SPEC =
      PartitionSpec.builderFor(HIVE_TABLE_SCHEMA).identity("name").build();
  protected static final PrimaryKeySpec PRIMARY_KEY_SPEC = PrimaryKeySpec.builderFor(HIVE_TABLE_SCHEMA)
      .addColumn("id").build();

  protected UnkeyedHiveTable testHiveTable;
  protected KeyedHiveTable testKeyedHiveTable;

  protected UnkeyedHiveTable testUnPartitionHiveTable;
  protected KeyedHiveTable testUnPartitionKeyedHiveTable;

  protected List<DataFileInfo> baseDataFilesInfo = new ArrayList<>();
  protected List<DataFileInfo> posDeleteFilesInfo = new ArrayList<>();

  @BeforeClass
  public static void init() throws Exception {
    FileUtils.deleteQuietly(testHiveBaseDir);
    FileUtils.deleteQuietly(testHiveTableBaseDir);
    testHiveBaseDir.mkdirs();
  }

  @AfterClass
  public static void clear() {
    FileUtils.deleteQuietly(testHiveBaseDir);
    FileUtils.deleteQuietly(testHiveTableBaseDir);
    testHiveBaseDir.mkdirs();
  }

  @Before
  public void initDataFileInfo() {
    setupTables();
    baseDataFilesInfo = new ArrayList<>();
    posDeleteFilesInfo = new ArrayList<>();
  }

  @After
  public void clearDataFileInfo() {
    clearTable();
    baseDataFilesInfo.clear();
    posDeleteFilesInfo.clear();
  }

  public void setupTables() {
    testHiveTable = (UnkeyedHiveTable) hiveCatalog
        .newTableBuilder(HIVE_TABLE_ID, HIVE_TABLE_SCHEMA)
        .withPartitionSpec(HIVE_SPEC)
        .create().asUnkeyedTable();

    testUnPartitionHiveTable = (UnkeyedHiveTable) hiveCatalog
        .newTableBuilder(UN_PARTITION_HIVE_TABLE_ID, HIVE_TABLE_SCHEMA)
        .create().asUnkeyedTable();

    testKeyedHiveTable = (KeyedHiveTable) hiveCatalog
        .newTableBuilder(HIVE_PK_TABLE_ID, HIVE_TABLE_SCHEMA)
        .withPartitionSpec(HIVE_SPEC)
        .withPrimaryKeySpec(PRIMARY_KEY_SPEC)
        .create().asKeyedTable();

    testUnPartitionKeyedHiveTable = (KeyedHiveTable) hiveCatalog
        .newTableBuilder(UN_PARTITION_HIVE_PK_TABLE_ID, HIVE_TABLE_SCHEMA)
        .withPrimaryKeySpec(PRIMARY_KEY_SPEC)
        .create().asKeyedTable();
  }

  public void clearTable() {
    hiveCatalog.dropTable(HIVE_TABLE_ID, true);
    hiveCatalog.dropTable(UN_PARTITION_HIVE_TABLE_ID, true);
    hiveCatalog.dropTable(HIVE_PK_TABLE_ID, true);
    hiveCatalog.dropTable(UN_PARTITION_HIVE_PK_TABLE_ID, true);
  }

  public List<Record> baseRecords(int start, int length, Schema tableSchema) {
    GenericRecord record = GenericRecord.create(tableSchema);

    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    for (int i = start; i < start + length; i++) {
      builder.add(record.copy(ImmutableMap.of("id", i,
          "op_time", LocalDateTime.of(2022, 1, 1, 12, 0, 0),
          "op_time_with_zone", LocalDateTime.of(2022, 1, i % 2 + 1, 12, 0, 0).atOffset(ZoneOffset.UTC),
          "d", new BigDecimal(i), "name", "name" + 1)));
    }

    return builder.build();
  }
}
