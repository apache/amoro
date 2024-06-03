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

package org.apache.amoro.server.optimizing.scan;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.data.ChangeAction;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.server.optimizing.OptimizingTestHelpers;
import org.apache.amoro.server.table.KeyedTableSnapshot;
import org.apache.amoro.server.utils.IcebergTableUtil;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.utils.ExpressionUtil;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(Parameterized.class)
public class TestKeyedTableFileScanHelper extends TableFileScanHelperTestBase {
  public TestKeyedTableFileScanHelper(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[][] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, true)},
      {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, false)}
    };
  }

  @Test
  public void testScanEmpty() {
    List<TableFileScanHelper.FileScanResult> scan = scanFiles();
    assertScanResult(scan, 0);
  }

  @Test
  public void testScanEmptySnapshot() {
    long transactionId = getMixedTable().beginTransaction("");
    OptimizingTestHelpers.appendBase(
        getMixedTable(),
        tableTestHelper()
            .writeBaseStore(getMixedTable(), transactionId, Collections.emptyList(), false));

    List<TableFileScanHelper.FileScanResult> scan = scanFiles();
    assertScanResult(scan, 0);
  }

  @Test
  public void testScanOnlyBase() {
    ArrayList<Record> newRecords =
        Lists.newArrayList(
            tableTestHelper().generateTestRecord(1, "111", 0, "2022-01-01T12:00:00"),
            tableTestHelper().generateTestRecord(2, "222", 0, "2022-01-01T12:00:00"),
            tableTestHelper().generateTestRecord(3, "333", 0, "2022-01-01T12:00:00"),
            tableTestHelper().generateTestRecord(4, "444", 0, "2022-01-01T12:00:00"));
    long transactionId = getMixedTable().beginTransaction("");
    OptimizingTestHelpers.appendBase(
        getMixedTable(),
        tableTestHelper().writeBaseStore(getMixedTable(), transactionId, newRecords, false));

    List<TableFileScanHelper.FileScanResult> scan = scanFiles();

    assertScanResult(scan, 4, transactionId, 0);
  }

  @Test
  public void testScanOnlyChange() {
    ArrayList<Record> newRecords =
        Lists.newArrayList(
            tableTestHelper().generateTestRecord(1, "111", 0, "2022-01-01T12:00:00"),
            tableTestHelper().generateTestRecord(2, "222", 0, "2022-01-01T12:00:00"),
            tableTestHelper().generateTestRecord(3, "333", 0, "2022-01-01T12:00:00"),
            tableTestHelper().generateTestRecord(4, "444", 0, "2022-01-01T12:00:00"));
    long transactionId = getMixedTable().beginTransaction("");
    appendChange(
        tableTestHelper()
            .writeChangeStore(
                getMixedTable(), transactionId, ChangeAction.INSERT, newRecords, false));
    long sequenceNumber = getMixedTable().changeTable().currentSnapshot().sequenceNumber();

    List<TableFileScanHelper.FileScanResult> scan = scanFiles();

    assertScanResult(scan, 4, sequenceNumber, 0);
  }

  @Test
  public void testScanChangeAndBase() {
    ArrayList<Record> newRecords =
        Lists.newArrayList(
            tableTestHelper().generateTestRecord(1, "111", 0, "2022-01-01T12:00:00"),
            tableTestHelper().generateTestRecord(2, "222", 0, "2022-01-01T12:00:00"),
            tableTestHelper().generateTestRecord(3, "333", 0, "2022-01-02T12:00:00"),
            tableTestHelper().generateTestRecord(4, "444", 0, "2022-01-02T12:00:00"));

    long transactionId = getMixedTable().beginTransaction("");
    List<DataFile> dataFiles =
        OptimizingTestHelpers.appendBase(
            getMixedTable(),
            tableTestHelper().writeBaseStore(getMixedTable(), transactionId, newRecords, false));
    // partition field = "2022-01-01T12:00:00"
    DataFile sampleFile = dataFiles.get(0);

    transactionId = getMixedTable().beginTransaction("");
    List<DataFile> dataFiles1 =
        tableTestHelper()
            .writeChangeStore(
                getMixedTable(), transactionId, ChangeAction.DELETE, newRecords, false);
    appendChange(dataFiles1);

    newRecords =
        Lists.newArrayList(
            tableTestHelper().generateTestRecord(1, "1111", 0, "2022-01-01T12:00:00"),
            tableTestHelper().generateTestRecord(2, "2222", 0, "2022-01-01T12:00:00"),
            tableTestHelper().generateTestRecord(3, "3333", 0, "2022-01-02T12:00:00"),
            tableTestHelper().generateTestRecord(4, "4444", 0, "2022-01-02T12:00:00"));

    transactionId = getMixedTable().beginTransaction("");
    appendChange(
        tableTestHelper()
            .writeChangeStore(
                getMixedTable(), transactionId, ChangeAction.INSERT, newRecords, false));

    List<TableFileScanHelper.FileScanResult> scan = scanFiles();

    assertScanResult(scan, 8, 1);

    // test partition filter
    scan =
        scanFiles(
            buildFileScanHelper()
                .withPartitionFilter(
                    ExpressionUtil.convertPartitionDataToDataFilter(
                        getMixedTable(), sampleFile.specId(), sampleFile.partition())));
    if (isPartitionedTable()) {
      assertScanResult(scan, 4, 1);
    } else {
      assertScanResult(scan, 8, 1);
    }
  }

  @Test
  public void testScanWithPosDelete() {
    ArrayList<Record> newRecords =
        Lists.newArrayList(
            tableTestHelper().generateTestRecord(1, "111", 0, "2022-01-01T12:00:00"),
            tableTestHelper().generateTestRecord(2, "222", 0, "2022-01-01T12:00:00"),
            tableTestHelper().generateTestRecord(3, "333", 0, "2022-01-01T12:00:00"),
            tableTestHelper().generateTestRecord(4, "444", 0, "2022-01-01T12:00:00"));
    long transactionId = getMixedTable().beginTransaction("");
    List<DataFile> dataFiles =
        OptimizingTestHelpers.appendBase(
            getMixedTable(),
            tableTestHelper().writeBaseStore(getMixedTable(), transactionId, newRecords, false));
    List<DeleteFile> posDeleteFiles = Lists.newArrayList();
    for (DataFile dataFile : dataFiles) {
      posDeleteFiles.addAll(
          MixedDataTestHelpers.writeBaseStorePosDelete(
              getMixedTable(), transactionId, dataFile, Collections.singletonList(0L)));
    }
    OptimizingTestHelpers.appendBasePosDelete(getMixedTable(), posDeleteFiles);

    List<TableFileScanHelper.FileScanResult> scan = scanFiles();

    assertScanResult(scan, 4, transactionId, 1);
  }

  @Test
  public void testScanPartialChange() {
    ArrayList<Record> newRecords =
        Lists.newArrayList(
            tableTestHelper().generateTestRecord(1, "111", 0, "2022-01-01T12:00:00"),
            tableTestHelper().generateTestRecord(2, "222", 0, "2022-01-01T12:00:00"),
            tableTestHelper().generateTestRecord(3, "333", 0, "2022-01-02T12:00:00"),
            tableTestHelper().generateTestRecord(4, "444", 0, "2022-01-02T12:00:00"));

    appendChange(
        tableTestHelper()
            .writeChangeStore(getMixedTable(), null, ChangeAction.INSERT, newRecords, false));
    long sequenceNumber = getMixedTable().changeTable().currentSnapshot().sequenceNumber();

    appendChange(
        tableTestHelper()
            .writeChangeStore(getMixedTable(), null, ChangeAction.INSERT, newRecords, false));

    appendChange(
        tableTestHelper()
            .writeChangeStore(getMixedTable(), null, ChangeAction.INSERT, newRecords, false));

    // check all files
    List<TableFileScanHelper.FileScanResult> scan = scanFiles();

    assertScanResult(scan, 12, 0);

    // keep at most 5 files, actually 4 files
    getMixedTable()
        .updateProperties()
        .set(TableProperties.SELF_OPTIMIZING_MAX_FILE_CNT, "5")
        .commit();

    scan = scanFiles();

    assertScanResult(scan, 4, sequenceNumber, 0);

    // keep at most 3 files, actually 0 files
    getMixedTable()
        .updateProperties()
        .set(TableProperties.SELF_OPTIMIZING_MAX_FILE_CNT, "3")
        .commit();

    scan = scanFiles();

    assertScanResult(scan, 0);
  }

  protected KeyedTableFileScanHelper buildFileScanHelper() {
    long baseSnapshotId = IcebergTableUtil.getSnapshotId(getMixedTable().baseTable(), true);
    long changeSnapshotId = IcebergTableUtil.getSnapshotId(getMixedTable().changeTable(), true);
    return new KeyedTableFileScanHelper(
        getMixedTable(), new KeyedTableSnapshot(baseSnapshotId, changeSnapshotId));
  }

  private void appendChange(List<DataFile> dataFiles) {
    AppendFiles appendFiles = getMixedTable().changeTable().newAppend();
    dataFiles.forEach(appendFiles::appendFile);
    appendFiles.commit();
  }

  @Override
  protected KeyedTable getMixedTable() {
    return super.getMixedTable().asKeyedTable();
  }

  @Test
  public void testGetMaxSequenceLimit() {
    List<KeyedTableFileScanHelper.SnapshotFileGroup> sequenceGroups = new ArrayList<>();
    sequenceGroups.add(buildSequenceGroup(1, 100, 2));
    Assert.assertEquals(
        Long.MIN_VALUE,
        KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 0));
    Assert.assertEquals(
        Long.MIN_VALUE,
        KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 1));
    Assert.assertEquals(
        Long.MAX_VALUE,
        KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 2));
    Assert.assertEquals(
        Long.MAX_VALUE,
        KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 3));

    sequenceGroups.add(buildSequenceGroup(2, 101, 1));
    Assert.assertEquals(
        1, KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 2));
    Assert.assertEquals(
        Long.MAX_VALUE,
        KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 3));

    // disorder
    sequenceGroups.add(buildSequenceGroup(5, 103, 2));
    sequenceGroups.add(buildSequenceGroup(4, 102, 2));
    sequenceGroups.add(buildSequenceGroup(3, 99, 1));
    Assert.assertEquals(
        Long.MIN_VALUE,
        KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 2));
    Assert.assertEquals(
        Long.MIN_VALUE,
        KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 3));
    Assert.assertEquals(
        3, KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 4));
    Assert.assertEquals(
        3, KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 5));
    Assert.assertEquals(
        4, KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 6));
    Assert.assertEquals(
        4, KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 7));
    Assert.assertEquals(
        Long.MAX_VALUE,
        KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 8));
    Assert.assertEquals(
        Long.MAX_VALUE,
        KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 9));
  }

  private static KeyedTableFileScanHelper.SnapshotFileGroup buildSequenceGroup(
      long sequence, long txId, int cnt) {
    return new KeyedTableFileScanHelper.SnapshotFileGroup(sequence, txId, cnt);
  }
}
