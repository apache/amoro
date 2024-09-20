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

package org.apache.amoro.hive.op;

import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.catalog.TableTestBase;
import org.apache.amoro.hive.TestHMS;
import org.apache.amoro.hive.catalog.HiveCatalogTestHelper;
import org.apache.amoro.hive.catalog.HiveTableTestHelper;
import org.apache.amoro.hive.io.HiveDataTestHelpers;
import org.apache.amoro.hive.utils.HivePartitionUtil;
import org.apache.amoro.hive.utils.HiveSchemaUtil;
import org.apache.amoro.properties.HiveTableProperties;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.MixedTableUtil;
import org.apache.amoro.utils.TableFileUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.OverwriteFiles;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.hadoop.Util;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(Parameterized.class)
public class TestSyncHiveMeta extends TableTestBase {

  @ClassRule public static TestHMS TEST_HMS = new TestHMS();

  public TestSyncHiveMeta(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(true, true)
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(true, false)
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(false, true)
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(false, false)
      }
    };
  }

  @Test
  public void testSyncHiveSchemaChange() throws TException {
    Table hiveTable =
        TEST_HMS
            .getHiveClient()
            .getTable(getMixedTable().id().getDatabase(), getMixedTable().id().getTableName());
    hiveTable.getSd().getCols().add(new FieldSchema("add_column", "bigint", "add column"));
    TEST_HMS
        .getHiveClient()
        .alter_table(
            getMixedTable().id().getDatabase(), getMixedTable().id().getTableName(), hiveTable);
    hiveTable =
        TEST_HMS
            .getHiveClient()
            .getTable(getMixedTable().id().getDatabase(), getMixedTable().id().getTableName());
    getMixedTable().refresh();
    Assert.assertEquals(
        hiveTable.getSd().getCols(),
        HiveSchemaUtil.hiveTableFields(getMixedTable().schema(), getMixedTable().spec()));
  }

  @Test
  public void testSyncHiveDataChange() throws TException, IOException, InterruptedException {
    getMixedTable()
        .updateProperties()
        .set(HiveTableProperties.AUTO_SYNC_HIVE_DATA_WRITE, "true")
        .commit();
    List<Record> insertRecords =
        org.apache.amoro.shade.guava32.com.google.common.collect.Lists.newArrayList();
    insertRecords.add(tableTestHelper().generateTestRecord(1, "john", 0, "2022-01-01T12:00:00"));

    List<DataFile> dataFiles =
        HiveDataTestHelpers.writerOf(getMixedTable()).transactionId(1L).writeHive(insertRecords);
    UnkeyedTable baseStore = MixedTableUtil.baseStore(getMixedTable());
    OverwriteFiles overwriteFiles = baseStore.newOverwrite();
    dataFiles.forEach(overwriteFiles::addFile);
    overwriteFiles.commit();

    dataFiles = HiveDataTestHelpers.lastedAddedFiles(getBaseStore());
    Assert.assertEquals(1, dataFiles.size());
    String dataFilePath = dataFiles.get(0).path().toString();
    FileSystem fs = Util.getFs(new Path(dataFilePath), new Configuration());
    fs.rename(new Path(dataFilePath), new Path(dataFilePath + ".bak"));
    if (isPartitionedTable()) {
      Partition partition =
          TEST_HMS
              .getHiveClient()
              .getPartition(
                  getMixedTable().id().getDatabase(),
                  getMixedTable().id().getTableName(),
                  Lists.newArrayList("2022-01-01"));
      TimeUnit.SECONDS.sleep(1);
      partition.getParameters().clear();
      TEST_HMS
          .getHiveClient()
          .alter_partition(
              getMixedTable().id().getDatabase(),
              getMixedTable().id().getTableName(),
              partition,
              null);
    } else {
      Table hiveTable =
          TEST_HMS
              .getHiveClient()
              .getTable(getMixedTable().id().getDatabase(), getMixedTable().id().getTableName());
      hiveTable.putToParameters("transient_lastDdlTime", "0");
      TEST_HMS
          .getHiveClient()
          .alter_table(
              getMixedTable().id().getDatabase(), getMixedTable().id().getTableName(), hiveTable);
    }

    getMixedTable().refresh();
    Assert.assertEquals(
        Sets.newHashSet(dataFilePath + ".bak"),
        listTableFiles(baseStore).stream().map(DataFile::path).collect(Collectors.toSet()));
  }

  @Test
  public void testSyncHivePartitionChange() throws TException {
    Assume.assumeTrue(isPartitionedTable());
    getMixedTable()
        .updateProperties()
        .set(HiveTableProperties.AUTO_SYNC_HIVE_DATA_WRITE, "true")
        .commit();
    List<Record> insertRecords = Lists.newArrayList();
    insertRecords.add(tableTestHelper().generateTestRecord(1, "john", 0, "2022-01-01T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(2, "lily", 0, "2022-01-02T12:00:00"));
    List<DataFile> dataFiles =
        HiveDataTestHelpers.writerOf(getMixedTable()).transactionId(1L).writeHive(insertRecords);
    UnkeyedTable baseStore = MixedTableUtil.baseStore(getMixedTable());
    OverwriteFiles overwriteFiles = baseStore.newOverwrite();
    dataFiles.forEach(overwriteFiles::addFile);
    overwriteFiles.commit();
    dataFiles = HiveDataTestHelpers.lastedAddedFiles(getBaseStore());

    Table hiveTable =
        TEST_HMS
            .getHiveClient()
            .getTable(getMixedTable().id().getDatabase(), getMixedTable().id().getTableName());

    // test add new hive partition
    insertRecords.clear();
    insertRecords.add(tableTestHelper().generateTestRecord(3, "lily", 0, "2022-01-03T12:00:00"));
    List<DataFile> newFiles =
        HiveDataTestHelpers.writerOf(getMixedTable())
            .transactionId(1L)
            .consistentWriteEnabled(false)
            .writeHive(insertRecords);
    Assert.assertEquals(1, newFiles.size());
    Partition newPartition =
        HivePartitionUtil.newPartition(
            hiveTable,
            Lists.newArrayList("2022-01-03"),
            TableFileUtil.getFileDir(newFiles.get(0).path().toString()),
            newFiles,
            (int) (System.currentTimeMillis() / 1000));
    newPartition.getParameters().remove(HiveTableProperties.MIXED_TABLE_FLAG);
    TEST_HMS.getHiveClient().add_partition(newPartition);
    getMixedTable().refresh();

    List<DataFile> listTableFiles = listTableFiles(baseStore);
    Assert.assertEquals(
        Stream.concat(dataFiles.stream(), newFiles.stream())
            .map(DataFile::path)
            .collect(Collectors.toSet()),
        listTableFiles.stream().map(DataFile::path).collect(Collectors.toSet()));
  }

  private List<DataFile> listTableFiles(UnkeyedTable table) {
    List<DataFile> dataFiles = Lists.newArrayList();
    table.newScan().planFiles().forEach(fileScanTask -> dataFiles.add(fileScanTask.file()));
    return dataFiles;
  }
}
