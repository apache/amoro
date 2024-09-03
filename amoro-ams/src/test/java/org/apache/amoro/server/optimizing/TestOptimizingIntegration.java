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

package org.apache.amoro.server.optimizing;

import org.apache.amoro.server.AmsEnvironment;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableBuilder;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableProperties;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;
import org.apache.thrift.TException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@Disabled
public class TestOptimizingIntegration {

  private static final AmsEnvironment amsEnv = AmsEnvironment.getIntegrationInstances();

  private static final String DATABASE = "optimizing_integration_test_db";

  private static final TableIdentifier MIXED_ICEBERG_TB_1 =
      TableIdentifier.of(
          AmsEnvironment.INTERNAL_MIXED_ICEBERG_CATALOG, DATABASE, "mix_iceberg_table1");
  private static final TableIdentifier MIXED_ICEBERG_TB_2 =
      TableIdentifier.of(
          AmsEnvironment.INTERNAL_MIXED_ICEBERG_CATALOG, DATABASE, "mix_iceberg_table2");
  private static final TableIdentifier MIXED_ICEBERG_TB_3 =
      TableIdentifier.of(
          AmsEnvironment.INTERNAL_MIXED_ICEBERG_CATALOG, DATABASE, "mix_iceberg_table3");
  private static final TableIdentifier MIXED_ICEBERG_TB_4 =
      TableIdentifier.of(
          AmsEnvironment.INTERNAL_MIXED_ICEBERG_CATALOG, DATABASE, "mix_iceberg_table4");
  private static final TableIdentifier MIXED_ICEBERG_TB_5 =
      TableIdentifier.of(
          AmsEnvironment.INTERNAL_MIXED_ICEBERG_CATALOG, DATABASE, "mix_iceberg_table5");
  private static final TableIdentifier MIXED_ICEBERG_TB_6 =
      TableIdentifier.of(
          AmsEnvironment.INTERNAL_MIXED_ICEBERG_CATALOG, DATABASE, "mix_iceberg_table6");
  private static final TableIdentifier MIXED_ICEBERG_TB_7 =
      TableIdentifier.of(
          AmsEnvironment.INTERNAL_MIXED_ICEBERG_CATALOG, DATABASE, "mix_iceberg_table7");
  private static final TableIdentifier MIXED_HIVE_TB_1 =
      TableIdentifier.of(AmsEnvironment.MIXED_HIVE_CATALOG, DATABASE, "mix_hive_table1");
  private static final TableIdentifier MIXED_HIVE_TB_2 =
      TableIdentifier.of(AmsEnvironment.MIXED_HIVE_CATALOG, DATABASE, "mix_hive_table2");
  private static final Schema SCHEMA =
      new Schema(
          Types.NestedField.required(1, "id", Types.IntegerType.get()),
          Types.NestedField.required(2, "name", Types.StringType.get()),
          Types.NestedField.required(3, "op_time", Types.TimestampType.withZone()));
  private static final PartitionSpec SPEC = PartitionSpec.builderFor(SCHEMA).day("op_time").build();
  private static final PrimaryKeySpec PRIMARY_KEY =
      PrimaryKeySpec.builderFor(SCHEMA).addColumn("id").build();

  @BeforeAll
  public static void before() throws Exception {
    amsEnv.start();
    amsEnv.startOptimizer();
    amsEnv.createDatabaseIfNotExists(AmsEnvironment.ICEBERG_CATALOG, DATABASE);
    amsEnv.createDatabaseIfNotExists(AmsEnvironment.INTERNAL_MIXED_ICEBERG_CATALOG, DATABASE);
    amsEnv.createDatabaseIfNotExists(AmsEnvironment.MIXED_HIVE_CATALOG, DATABASE);
  }

  @AfterAll
  public static void after() throws IOException {
    amsEnv.stop();
  }

  @Test
  public void testPkTableOptimizing() {
    MixedTable mixedTable =
        createMixedTable(MIXED_ICEBERG_TB_1, PRIMARY_KEY, PartitionSpec.unpartitioned());
    assertTableExist(MIXED_ICEBERG_TB_1);
    TestMixedIcebergOptimizing testCase = new TestMixedIcebergOptimizing(mixedTable);
    testCase.testKeyedTableContinueOptimizing();
  }

  @Test
  public void testPkPartitionTableOptimizing() {
    MixedTable mixedTable = createMixedTable(MIXED_ICEBERG_TB_2, PRIMARY_KEY, SPEC);
    assertTableExist(MIXED_ICEBERG_TB_2);
    TestMixedIcebergOptimizing testCase = new TestMixedIcebergOptimizing(mixedTable);
    testCase.testKeyedTableContinueOptimizing();
  }

  @Test
  public void testPkTableMajorOptimizeLeftPosDelete() {
    MixedTable mixedTable =
        createMixedTable(MIXED_ICEBERG_TB_3, PRIMARY_KEY, PartitionSpec.unpartitioned());
    assertTableExist(MIXED_ICEBERG_TB_3);
    TestMixedIcebergOptimizing testCase = new TestMixedIcebergOptimizing(mixedTable);
    testCase.testPkTableMajorOptimizeLeftPosDelete();
  }

  @Test
  public void testNoPkTableOptimizing() {
    MixedTable mixedTable =
        createMixedTable(
            MIXED_ICEBERG_TB_4, PrimaryKeySpec.noPrimaryKey(), PartitionSpec.unpartitioned());
    assertTableExist(MIXED_ICEBERG_TB_4);
    TestMixedIcebergOptimizing testCase = new TestMixedIcebergOptimizing(mixedTable);
    testCase.testNoPkTableOptimizing();
  }

  @Test
  public void testNoPkPartitionTableOptimizing() {
    MixedTable mixedTable =
        createMixedTable(MIXED_ICEBERG_TB_5, PrimaryKeySpec.noPrimaryKey(), SPEC);
    assertTableExist(MIXED_ICEBERG_TB_5);
    TestMixedIcebergOptimizing testCase = new TestMixedIcebergOptimizing(mixedTable);
    testCase.testNoPkPartitionTableOptimizing();
  }

  @Test
  public void testKeyedTableTxIdNotInOrder() {
    MixedTable mixedTable =
        createMixedTable(MIXED_ICEBERG_TB_6, PRIMARY_KEY, PartitionSpec.unpartitioned());
    assertTableExist(MIXED_ICEBERG_TB_6);
    TestMixedIcebergOptimizing testCase = new TestMixedIcebergOptimizing(mixedTable);
    testCase.testKeyedTableTxIdNotInOrder();
  }

  @Test
  public void testHiveKeyedTableMajorOptimizeNotMove() throws TException, IOException {
    createMixedHiveTable(MIXED_HIVE_TB_1, PRIMARY_KEY, PartitionSpec.unpartitioned());
    assertTableExist(MIXED_HIVE_TB_1);
    KeyedTable table =
        amsEnv.catalog(AmsEnvironment.MIXED_HIVE_CATALOG).loadTable(MIXED_HIVE_TB_1).asKeyedTable();
    TestMixedHiveOptimizing testCase =
        new TestMixedHiveOptimizing(table, amsEnv.getTestHMS().getClient());
    testCase.testHiveKeyedTableMajorOptimizeNotMove();
  }

  @Test
  public void testHiveKeyedTableMajorOptimizeAndMove() throws TException, IOException {
    createMixedHiveTable(MIXED_HIVE_TB_2, PRIMARY_KEY, PartitionSpec.unpartitioned());
    assertTableExist(MIXED_HIVE_TB_2);
    KeyedTable table =
        amsEnv.catalog(AmsEnvironment.MIXED_HIVE_CATALOG).loadTable(MIXED_HIVE_TB_2).asKeyedTable();
    TestMixedHiveOptimizing testCase =
        new TestMixedHiveOptimizing(table, amsEnv.getTestHMS().getClient());
    testCase.testHiveKeyedTableMajorOptimizeAndMove();
  }

  private MixedTable createMixedTable(
      TableIdentifier tableIdentifier, PrimaryKeySpec primaryKeySpec, PartitionSpec partitionSpec) {

    TableBuilder tableBuilder =
        amsEnv
            .catalog(AmsEnvironment.INTERNAL_MIXED_ICEBERG_CATALOG)
            .newTableBuilder(tableIdentifier, SCHEMA)
            .withPrimaryKeySpec(primaryKeySpec)
            .withPartitionSpec(partitionSpec)
            .withProperty(TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_INTERVAL, "1000");

    return tableBuilder.create();
  }

  private void createMixedHiveTable(
      TableIdentifier tableIdentifier, PrimaryKeySpec primaryKeySpec, PartitionSpec partitionSpec) {
    TableBuilder tableBuilder =
        amsEnv
            .catalog(AmsEnvironment.MIXED_HIVE_CATALOG)
            .newTableBuilder(tableIdentifier, SCHEMA)
            .withPrimaryKeySpec(primaryKeySpec)
            .withPartitionSpec(partitionSpec)
            .withProperty(TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_INTERVAL, "1000");

    tableBuilder.create();
  }

  private void assertTableExist(TableIdentifier tableIdentifier) {
    Assertions.assertTrue(amsEnv.tableExist(tableIdentifier));
  }
}
