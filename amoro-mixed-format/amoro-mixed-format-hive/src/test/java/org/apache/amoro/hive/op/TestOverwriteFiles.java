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

import static org.apache.amoro.hive.op.UpdateHiveFiles.DELETE_UNTRACKED_HIVE_FILE;

import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.hive.MixedHiveTableTestBase;
import org.apache.amoro.hive.TestHMS;
import org.apache.amoro.hive.catalog.HiveCatalogTestHelper;
import org.apache.amoro.hive.catalog.HiveTableTestHelper;
import org.apache.amoro.hive.exceptions.CannotAlterHiveLocationException;
import org.apache.amoro.hive.io.HiveDataTestHelpers;
import org.apache.amoro.properties.HiveTableProperties;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.MixedTableUtil;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.OverwriteFiles;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.UpdateProperties;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.expressions.Expressions;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

@RunWith(Parameterized.class)
public class TestOverwriteFiles extends MixedHiveTableTestBase {

  @ClassRule public static TestHMS TEST_HMS = new TestHMS();

  public TestOverwriteFiles(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
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
        new HiveTableTestHelper(
            true,
            false,
            ImmutableMap.of(HiveTableProperties.HIVE_CONSISTENT_WRITE_ENABLED, "false"))
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(false, true)
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(
            false,
            false,
            ImmutableMap.of(HiveTableProperties.HIVE_CONSISTENT_WRITE_ENABLED, "false"))
      }
    };
  }

  @Test
  public void testOverwriteWholeHiveTable() throws TException {
    List<Record> insertRecords = Lists.newArrayList();
    insertRecords.add(tableTestHelper().generateTestRecord(1, "john", 0, "2022-01-01T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(2, "lily", 0, "2022-01-02T12:00:00"));
    List<DataFile> dataFiles =
        HiveDataTestHelpers.writerOf(getMixedTable()).transactionId(1L).writeHive(insertRecords);
    HiveDataTestHelpers.assertWriteConsistentFilesName(getMixedTable(), dataFiles);
    UnkeyedTable baseStore = MixedTableUtil.baseStore(getMixedTable());
    OverwriteFiles overwriteFiles = baseStore.newOverwrite();
    dataFiles.forEach(overwriteFiles::addFile);
    overwriteFiles.commit();
    HiveDataTestHelpers.assertWriteConsistentFilesCommit(getMixedTable());

    dataFiles = HiveDataTestHelpers.lastedAddedFiles(baseStore);
    UpdateHiveFilesTestHelpers.validateHiveTableValues(
        TEST_HMS.getHiveClient(), getMixedTable(), dataFiles);

    // ================== test overwrite all table
    insertRecords.clear();
    insertRecords.add(tableTestHelper().generateTestRecord(2, "lily", 0, "2022-01-02T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(3, "john", 0, "2022-01-03T12:00:00"));
    dataFiles =
        HiveDataTestHelpers.writerOf(getMixedTable()).transactionId(1L).writeHive(insertRecords);
    overwriteFiles = baseStore.newOverwrite();
    overwriteFiles.overwriteByRowFilter(Expressions.alwaysTrue());
    dataFiles.forEach(overwriteFiles::addFile);
    overwriteFiles.commit();
    dataFiles = HiveDataTestHelpers.lastedAddedFiles(baseStore);

    UpdateHiveFilesTestHelpers.validateHiveTableValues(
        TEST_HMS.getHiveClient(), getMixedTable(), dataFiles);
  }

  @Test
  public void testOverwriteInTransaction() throws TException {
    List<Record> insertRecords = Lists.newArrayList();
    insertRecords.add(tableTestHelper().generateTestRecord(1, "john", 0, "2022-01-01T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(2, "lily", 0, "2022-01-02T12:00:00"));
    List<DataFile> dataFiles =
        HiveDataTestHelpers.writerOf(getMixedTable()).transactionId(1L).writeHive(insertRecords);
    HiveDataTestHelpers.assertWriteConsistentFilesName(getMixedTable(), dataFiles);
    UnkeyedTable baseStore = MixedTableUtil.baseStore(getMixedTable());
    Transaction transaction = baseStore.newTransaction();
    OverwriteFiles overwriteFiles = transaction.newOverwrite();
    dataFiles.forEach(overwriteFiles::addFile);
    overwriteFiles.commit();
    HiveDataTestHelpers.assertWriteConsistentFilesCommit(getMixedTable());

    String key = "test-overwrite-transaction";
    UpdateProperties updateProperties = transaction.updateProperties();
    updateProperties.set(key, "true");
    updateProperties.commit();

    Assert.assertFalse(getMixedTable().properties().containsKey(key));
    UpdateHiveFilesTestHelpers.validateHiveTableValues(
        TEST_HMS.getHiveClient(), getMixedTable(), Lists.newArrayList());

    transaction.commitTransaction();
    Assert.assertTrue(getMixedTable().properties().containsKey(key));

    dataFiles = HiveDataTestHelpers.lastedAddedFiles(baseStore);
    UpdateHiveFilesTestHelpers.validateHiveTableValues(
        TEST_HMS.getHiveClient(), getMixedTable(), dataFiles);
  }

  @Test
  public void testOverwriteByRowFilter() throws TException {
    List<Record> insertRecords = Lists.newArrayList();
    insertRecords.add(tableTestHelper().generateTestRecord(1, "john", 0, "2022-01-01T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(2, "lily", 0, "2022-01-02T12:00:00"));
    List<DataFile> dataFiles =
        HiveDataTestHelpers.writerOf(getMixedTable()).transactionId(1L).writeHive(insertRecords);
    UnkeyedTable baseStore = MixedTableUtil.baseStore(getMixedTable());
    OverwriteFiles overwriteFiles = baseStore.newOverwrite();
    dataFiles.forEach(overwriteFiles::addFile);
    overwriteFiles.commit();
    HiveDataTestHelpers.assertWriteConsistentFilesCommit(getMixedTable());
    dataFiles = HiveDataTestHelpers.lastedAddedFiles(baseStore);
    UpdateHiveFilesTestHelpers.validateHiveTableValues(
        TEST_HMS.getHiveClient(), getMixedTable(), dataFiles);

    // ================== test overwrite all table
    insertRecords.clear();
    insertRecords.add(tableTestHelper().generateTestRecord(2, "lily", 0, "2022-01-02T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(3, "john", 0, "2022-01-03T12:00:00"));
    dataFiles =
        HiveDataTestHelpers.writerOf(getMixedTable()).transactionId(1L).writeHive(insertRecords);
    overwriteFiles = baseStore.newOverwrite();
    overwriteFiles.overwriteByRowFilter(Expressions.alwaysTrue());
    dataFiles.forEach(overwriteFiles::addFile);
    overwriteFiles.commit();
    dataFiles = HiveDataTestHelpers.lastedAddedFiles(baseStore);
    UpdateHiveFilesTestHelpers.validateHiveTableValues(
        TEST_HMS.getHiveClient(), getMixedTable(), dataFiles);
  }

  /** add file to exist partition, overwrite success without create partition */
  @Test
  public void testOverwriteByAddFiles() throws TException {
    String hiveLocation = "test_hive_location";
    List<Record> insertRecords = Lists.newArrayList();
    insertRecords.add(tableTestHelper().generateTestRecord(1, "john", 0, "2022-01-01T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(2, "lily", 0, "2022-01-01T12:00:00"));
    List<DataFile> dataFiles =
        HiveDataTestHelpers.writerOf(getMixedTable())
            .transactionId(1L)
            .customHiveLocation(hiveLocation)
            .writeHive(insertRecords);
    UnkeyedTable baseStore = MixedTableUtil.baseStore(getMixedTable());
    OverwriteFiles overwriteFiles = baseStore.newOverwrite();
    dataFiles.forEach(overwriteFiles::addFile);
    overwriteFiles.commit();
    HiveDataTestHelpers.assertWriteConsistentFilesCommit(getMixedTable());
    dataFiles = HiveDataTestHelpers.lastedAddedFiles(baseStore);
    UpdateHiveFilesTestHelpers.validateHiveTableValues(
        TEST_HMS.getHiveClient(), getMixedTable(), dataFiles);

    insertRecords.clear();
    insertRecords.add(tableTestHelper().generateTestRecord(2, "lily", 0, "2022-01-01T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(3, "john", 0, "2022-01-01T12:00:00"));
    List<DataFile> newFiles =
        HiveDataTestHelpers.writerOf(getMixedTable())
            .transactionId(2L)
            .customHiveLocation(hiveLocation)
            .writeHive(insertRecords);
    overwriteFiles = baseStore.newOverwrite();
    newFiles.forEach(overwriteFiles::addFile);
    overwriteFiles.commit();
    newFiles = HiveDataTestHelpers.lastedAddedFiles(baseStore);
    List<DataFile> expectFiles = Lists.newArrayList(dataFiles);
    expectFiles.addAll(newFiles);
    UpdateHiveFilesTestHelpers.validateHiveTableValues(
        TEST_HMS.getHiveClient(), getMixedTable(), expectFiles);
  }

  @Test
  public void testOverwriteCommitHMSFailed() throws TException {
    List<Record> insertRecords = Lists.newArrayList();
    insertRecords.add(tableTestHelper().generateTestRecord(1, "john", 0, "2022-01-01T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(2, "lily", 0, "2022-01-02T12:00:00"));
    List<DataFile> dataFiles =
        HiveDataTestHelpers.writerOf(getMixedTable()).transactionId(1L).writeHive(insertRecords);
    UnkeyedTable baseStore = MixedTableUtil.baseStore(getMixedTable());
    OverwriteFiles overwriteFiles = baseStore.newOverwrite();
    dataFiles.forEach(overwriteFiles::addFile);

    // rename the hive table,
    Table hiveTable =
        TEST_HMS
            .getHiveClient()
            .getTable(getMixedTable().id().getDatabase(), getMixedTable().id().getTableName());
    hiveTable.setTableName("new_table");
    TEST_HMS
        .getHiveClient()
        .alter_table(
            getMixedTable().id().getDatabase(), getMixedTable().id().getTableName(), hiveTable);

    // commit should success even though hive table is not existed.
    overwriteFiles.commit();

    hiveTable.setTableName(getMixedTable().id().getTableName());
    TEST_HMS
        .getHiveClient()
        .alter_table(getMixedTable().id().getDatabase(), "new_table", hiveTable);
    String tableRootLocation = MixedTableUtil.tableRootLocation(getMixedTable());
    String newTableLocation =
        tableRootLocation.replace(getMixedTable().id().getTableName(), "new_table");
    getMixedTable().io().asFileSystemIO().deletePrefix(newTableLocation);
  }

  @Test
  public void testOverwriteCleanUntrackedFiles() throws TException {
    String hiveLocation = "test_hive_location";
    List<Record> insertRecords = Lists.newArrayList();
    insertRecords.add(tableTestHelper().generateTestRecord(1, "john", 0, "2022-01-01T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(2, "lily", 0, "2022-01-02T12:00:00"));
    HiveDataTestHelpers.writerOf(getMixedTable())
        .transactionId(1L)
        .customHiveLocation(hiveLocation)
        .writeHive(insertRecords);
    // rewrite data files
    List<DataFile> rewriteDataFiles =
        HiveDataTestHelpers.writerOf(getMixedTable())
            .transactionId(2L)
            .customHiveLocation(hiveLocation)
            .writeHive(insertRecords);
    UnkeyedTable baseStore = MixedTableUtil.baseStore(getMixedTable());
    OverwriteFiles overwriteFiles = baseStore.newOverwrite();
    rewriteDataFiles.forEach(overwriteFiles::addFile);
    overwriteFiles.set(DELETE_UNTRACKED_HIVE_FILE, "true");
    overwriteFiles.commit();
    rewriteDataFiles = HiveDataTestHelpers.lastedAddedFiles(baseStore);
    UpdateHiveFilesTestHelpers.validateHiveTableValues(
        TEST_HMS.getHiveClient(), getMixedTable(), rewriteDataFiles);
  }

  @Test
  public void testOverwritePartFiles() throws TException {
    // TODO should add cases for tables without partition spec
    Assume.assumeTrue(isPartitionedTable());
    getMixedTable()
        .updateProperties()
        .set(TableProperties.WRITE_TARGET_FILE_SIZE_BYTES, "1")
        .commit();
    List<Record> insertRecords = Lists.newArrayList();
    insertRecords.add(tableTestHelper().generateTestRecord(1, "john", 0, "2022-01-01T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(2, "lily", 0, "2022-01-01T12:00:00"));
    List<DataFile> dataFiles =
        HiveDataTestHelpers.writerOf(getMixedTable()).transactionId(1L).writeHive(insertRecords);
    Assert.assertEquals(2, dataFiles.size());
    DataFile deleteFile = dataFiles.get(0);
    UnkeyedTable baseStore = MixedTableUtil.baseStore(getMixedTable());
    OverwriteFiles overwriteFiles = baseStore.newOverwrite();
    dataFiles.forEach(overwriteFiles::addFile);
    overwriteFiles.commit();
    dataFiles = HiveDataTestHelpers.lastedAddedFiles(baseStore);
    UpdateHiveFilesTestHelpers.validateHiveTableValues(
        TEST_HMS.getHiveClient(), getMixedTable(), dataFiles);

    // ================== test overwrite part files
    insertRecords.clear();
    insertRecords.add(tableTestHelper().generateTestRecord(2, "lily", 0, "2022-01-01T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(3, "john", 0, "2022-01-01T12:00:00"));
    dataFiles =
        HiveDataTestHelpers.writerOf(getMixedTable()).transactionId(1L).writeHive(insertRecords);
    overwriteFiles = baseStore.newOverwrite();
    dataFiles.forEach(overwriteFiles::addFile);
    overwriteFiles.deleteFile(deleteFile);
    Assert.assertThrows(CannotAlterHiveLocationException.class, overwriteFiles::commit);
  }

  @Test
  public void testOverwriteWithFilesUnderDifferentDir() {
    // TODO should add cases for tables without partition spec
    Assume.assumeTrue(isPartitionedTable());
    UnkeyedTable baseStore = MixedTableUtil.baseStore(getMixedTable());
    OverwriteFiles overwriteFiles = baseStore.newOverwrite();

    List<Record> insertRecords = Lists.newArrayList();
    insertRecords.add(tableTestHelper().generateTestRecord(1, "john", 0, "2022-01-01T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(2, "lily", 0, "2022-01-02T12:00:00"));
    List<DataFile> dataFiles =
        HiveDataTestHelpers.writerOf(getMixedTable()).transactionId(1L).writeHive(insertRecords);
    dataFiles.forEach(overwriteFiles::addFile);

    // write data files under another dir
    dataFiles =
        HiveDataTestHelpers.writerOf(getMixedTable()).transactionId(1L).writeHive(insertRecords);
    dataFiles.forEach(overwriteFiles::addFile);

    Assert.assertThrows(CannotAlterHiveLocationException.class, overwriteFiles::commit);
  }

  @Test
  public void testOverwriteByAddFilesInDifferentDir() throws TException {
    // TODO should add cases for tables without partition spec
    Assume.assumeTrue(isPartitionedTable());
    List<Record> insertRecords = Lists.newArrayList();
    insertRecords.add(tableTestHelper().generateTestRecord(1, "john", 0, "2022-01-01T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(2, "lily", 0, "2022-01-02T12:00:00"));
    List<DataFile> dataFiles =
        HiveDataTestHelpers.writerOf(getMixedTable()).transactionId(1L).writeHive(insertRecords);
    UnkeyedTable baseStore = MixedTableUtil.baseStore(getMixedTable());
    OverwriteFiles overwriteFiles = baseStore.newOverwrite();
    dataFiles.forEach(overwriteFiles::addFile);
    overwriteFiles.commit();
    dataFiles = HiveDataTestHelpers.lastedAddedFiles(baseStore);
    UpdateHiveFilesTestHelpers.validateHiveTableValues(
        TEST_HMS.getHiveClient(), getMixedTable(), dataFiles);

    // ================== test add files only
    insertRecords.clear();
    insertRecords.add(tableTestHelper().generateTestRecord(2, "lily", 0, "2022-01-02T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(3, "john", 0, "2022-01-03T12:00:00"));
    dataFiles =
        HiveDataTestHelpers.writerOf(getMixedTable()).transactionId(1L).writeHive(insertRecords);
    overwriteFiles = baseStore.newOverwrite();
    dataFiles.forEach(overwriteFiles::addFile);

    Assert.assertThrows(CannotAlterHiveLocationException.class, overwriteFiles::commit);
  }

  @Test
  public void testOverwriteWithSameLocation() throws TException {
    // TODO should add cases for tables without partition spec
    Assume.assumeTrue(isPartitionedTable());
    List<Record> insertRecords = Lists.newArrayList();
    insertRecords.add(tableTestHelper().generateTestRecord(1, "john", 0, "2022-01-01T12:00:00"));
    insertRecords.add(tableTestHelper().generateTestRecord(2, "lily", 0, "2022-01-02T12:00:00"));
    List<DataFile> dataFiles =
        HiveDataTestHelpers.writerOf(getMixedTable()).transactionId(1L).writeHive(insertRecords);
    UnkeyedTable baseStore = MixedTableUtil.baseStore(getMixedTable());
    OverwriteFiles overwriteFiles = baseStore.newOverwrite();
    dataFiles.forEach(overwriteFiles::addFile);
    overwriteFiles.commit();
    dataFiles = HiveDataTestHelpers.lastedAddedFiles(baseStore);
    UpdateHiveFilesTestHelpers.validateHiveTableValues(
        TEST_HMS.getHiveClient(), getMixedTable(), dataFiles);

    overwriteFiles = baseStore.newOverwrite();
    dataFiles.forEach(overwriteFiles::deleteFile);
    dataFiles.forEach(overwriteFiles::addFile);

    Assert.assertThrows(CannotAlterHiveLocationException.class, overwriteFiles::commit);
  }
}
