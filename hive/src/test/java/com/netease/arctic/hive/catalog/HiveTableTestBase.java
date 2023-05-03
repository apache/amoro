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

package com.netease.arctic.hive.catalog;

public abstract class HiveTableTestBase {

  // @ClassRule
  // public static TestHMS TEST_HMS = new TestHMS();
  //
  // public HiveTableTestBase(Schema tableSchema, PrimaryKeySpec primaryKeySpec,
  //                          PartitionSpec partitionSpec,
  //                          Map<String, String> tableProperties) {
  //   super(TableFormat.MIXED_HIVE, tableSchema, primaryKeySpec, partitionSpec, tableProperties);
  // }
  //
  // public HiveTableTestBase(boolean keyedTable, boolean partitionedTable,
  //                          Map<String, String> tableProperties) {
  //   this(HIVE_TABLE_SCHEMA,
  //       keyedTable ? BasicTableTestHelper.PRIMARY_KEY_SPEC : PrimaryKeySpec.noPrimaryKey(),
  //       partitionedTable ? SPEC : PartitionSpec.unpartitioned(),
  //       tableProperties);
  // }
  //
  // public HiveTableTestBase(boolean keyedTable, boolean partitionedTable) {
  //   this(keyedTable, partitionedTable, Maps.newHashMap());
  // }
  //
  // @Override
  // protected CatalogMeta buildCatalogMeta() {
  //   Map<String, String> properties = Maps.newHashMap();
  //   return CatalogTestHelpers.buildHiveCatalogMeta(
  //       CatalogTestHelper.TEST_CATALOG_NAME,
  //       properties, TEST_HMS.getHiveConf());
  // }
  //
  // public void assertFilesName(List<String> exceptedFiles, ArcticTable table) throws TException {
  //   List<String> fileNameList = new ArrayList<>();
  //   if (isPartitionedTable()) {
  //     TableIdentifier identifier = table.id();
  //     final String database = identifier.getDatabase();
  //     final String tableName = identifier.getTableName();
  //
  //     List<Partition> partitions = TEST_HMS.getHiveClient().listPartitions(
  //         database,
  //         tableName,
  //         (short) -1);
  //     for (Partition p : partitions) {
  //       fileNameList.addAll(table.io().list(p.getSd().
  //           getLocation()).stream().map(f -> f.getPath().getName()).sorted().collect(Collectors.toList()));
  //     }
  //   } else {
  //     Table hiveTable = TEST_HMS.getHiveClient().getTable(table.id().getDatabase(), table.name());
  //     fileNameList.addAll(table.io().list(hiveTable.getSd().
  //         getLocation()).stream().map(f -> f.getPath().getName()).sorted().collect(Collectors.toList()));
  //   }
  //   Assert.assertEquals(exceptedFiles, fileNameList);
  // }

}
