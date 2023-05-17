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

package com.netease.arctic.server.optimizing.scan;

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.catalog.TableTestBase;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.io.DataTestHelpers;
import com.netease.arctic.server.utils.IcebergTableUtil;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.util.StructLikeMap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(Parameterized.class)
public class TestKeyedTableFileScanHelper extends TableTestBase {
  public TestKeyedTableFileScanHelper(CatalogTestHelper catalogTestHelper,
                                      TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[][] parameters() {
    return new Object[][] {
        {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(true, true)},
        {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(true, false)}};
  }

  @Test
  public void testScanEmpty() {
    KeyedTableFileScanHelper keyedTableFileScanHelper = getKeyedTableFileScanHelper();
    List<TableFileScanHelper.FileScanResult> scan = keyedTableFileScanHelper.scan();
    Assert.assertTrue(scan.isEmpty());
  }

  @Test
  public void testScanEmptySnapshot() {
    long transactionId = getArcticTable().asKeyedTable().beginTransaction("");
    DataTestHelpers.writeAndCommitBaseStore(getArcticTable(), transactionId, Collections.emptyList(), false);

    KeyedTableFileScanHelper keyedTableFileScanHelper = getKeyedTableFileScanHelper();
    List<TableFileScanHelper.FileScanResult> scan = keyedTableFileScanHelper.scan();
    Assert.assertTrue(scan.isEmpty());
  }

  @Test
  public void testScanOnlyBase() {
    ArrayList<Record> newRecords = Lists.newArrayList(
        tableTestHelper().generateTestRecord(1, "111", 0, "2022-01-01T12:00:00"),
        tableTestHelper().generateTestRecord(2, "222", 0, "2022-01-01T12:00:00"),
        tableTestHelper().generateTestRecord(3, "333", 0, "2022-01-01T12:00:00"),
        tableTestHelper().generateTestRecord(4, "444", 0, "2022-01-01T12:00:00")
    );
    long transactionId = getArcticTable().asKeyedTable().beginTransaction("");
    DataTestHelpers.writeAndCommitBaseStore(getArcticTable(), transactionId, newRecords, false);

    KeyedTableFileScanHelper keyedTableFileScanHelper = getKeyedTableFileScanHelper();
    List<TableFileScanHelper.FileScanResult> scan = keyedTableFileScanHelper.scan();

    Assert.assertEquals(4, scan.size());
    for (TableFileScanHelper.FileScanResult fileScanResult : scan) {
      Assert.assertEquals(transactionId, fileScanResult.file().getSequenceNumber());
      Assert.assertTrue(fileScanResult.deleteFiles().isEmpty());
    }
  }

  @Test
  public void testScanOnlyChange() {
    ArrayList<Record> newRecords = Lists.newArrayList(
        tableTestHelper().generateTestRecord(1, "111", 0, "2022-01-01T12:00:00"),
        tableTestHelper().generateTestRecord(2, "222", 0, "2022-01-01T12:00:00"),
        tableTestHelper().generateTestRecord(3, "333", 0, "2022-01-01T12:00:00"),
        tableTestHelper().generateTestRecord(4, "444", 0, "2022-01-01T12:00:00")
    );
    long transactionId = getArcticTable().asKeyedTable().beginTransaction("");
    DataTestHelpers.writeAndCommitChangeStore(getArcticTable().asKeyedTable(), transactionId, ChangeAction.INSERT,
        newRecords);
    long sequenceNumber = getArcticTable().asKeyedTable().changeTable().currentSnapshot().sequenceNumber();

    KeyedTableFileScanHelper keyedTableFileScanHelper = getKeyedTableFileScanHelper();
    List<TableFileScanHelper.FileScanResult> scan = keyedTableFileScanHelper.scan();

    Assert.assertEquals(4, scan.size());
    for (TableFileScanHelper.FileScanResult fileScanResult : scan) {
      Assert.assertEquals(sequenceNumber, fileScanResult.file().getSequenceNumber());
      Assert.assertTrue(fileScanResult.deleteFiles().isEmpty());
    }
  }

  @Test
  public void testScanChangeAndBase() {
    ArrayList<Record> newRecords = Lists.newArrayList(
        tableTestHelper().generateTestRecord(1, "111", 0, "2022-01-01T12:00:00"),
        tableTestHelper().generateTestRecord(2, "222", 0, "2022-01-01T12:00:00"),
        tableTestHelper().generateTestRecord(3, "333", 0, "2022-01-02T12:00:00"),
        tableTestHelper().generateTestRecord(4, "444", 0, "2022-01-02T12:00:00")
    );

    long transactionId = getArcticTable().asKeyedTable().beginTransaction("");
    DataTestHelpers.writeAndCommitBaseStore(getArcticTable(), transactionId, newRecords, false);

    transactionId = getArcticTable().asKeyedTable().beginTransaction("");
    DataTestHelpers.writeAndCommitChangeStore(getArcticTable().asKeyedTable(), transactionId, ChangeAction.DELETE,
        newRecords);

    newRecords = Lists.newArrayList(
        tableTestHelper().generateTestRecord(1, "1111", 0, "2022-01-01T12:00:00"),
        tableTestHelper().generateTestRecord(2, "2222", 0, "2022-01-01T12:00:00"),
        tableTestHelper().generateTestRecord(3, "3333", 0, "2022-01-02T12:00:00"),
        tableTestHelper().generateTestRecord(4, "4444", 0, "2022-01-02T12:00:00")
    );

    transactionId = getArcticTable().asKeyedTable().beginTransaction("");
    DataTestHelpers.writeAndCommitChangeStore(getArcticTable().asKeyedTable(), transactionId, ChangeAction.INSERT,
        newRecords);

    KeyedTableFileScanHelper keyedTableFileScanHelper = getKeyedTableFileScanHelper();
    List<TableFileScanHelper.FileScanResult> scan = keyedTableFileScanHelper.scan();

    Assert.assertEquals(8, scan.size());
    for (TableFileScanHelper.FileScanResult fileScanResult : scan) {
      Assert.assertEquals("file:" + fileScanResult.file().path().toString(), 1, fileScanResult.deleteFiles().size());
    }

    // test partition filter
    KeyedTableFileScanHelper keyedTableFileScanHelper2 = getKeyedTableFileScanHelper().withPartitionFilter(
        partition -> getPartition().equals(partition));
    List<TableFileScanHelper.FileScanResult> scan1 = keyedTableFileScanHelper2.scan();
    if (isPartitionedTable()) {
      Assert.assertEquals(4, scan1.size());
    } else {
      Assert.assertEquals(8, scan1.size());
    }

    for (TableFileScanHelper.FileScanResult fileScanResult : scan1) {
      Assert.assertEquals("file:" + fileScanResult.file().path().toString(), 1, fileScanResult.deleteFiles().size());
    }
  }

  protected String getPartition() {
    return isPartitionedTable() ? "op_time_day=2022-01-01" : "";
  }

  private KeyedTableFileScanHelper getKeyedTableFileScanHelper() {
    long baseSnapshotId = IcebergTableUtil.getSnapshotId(getArcticTable().asKeyedTable().baseTable(), true);
    long changeSnapshotId = IcebergTableUtil.getSnapshotId(getArcticTable().asKeyedTable().changeTable(), true);
    StructLikeMap<Long> partitionOptimizedSequence =
        TablePropertyUtil.getPartitionOptimizedSequence(getArcticTable().asKeyedTable());
    StructLikeMap<Long> legacyPartitionMaxTransactionId =
        TablePropertyUtil.getLegacyPartitionMaxTransactionId(getArcticTable().asKeyedTable());
    return new KeyedTableFileScanHelper(getArcticTable().asKeyedTable(), baseSnapshotId, changeSnapshotId,
        partitionOptimizedSequence, legacyPartitionMaxTransactionId);
  }

  @Test
  public void testGetMaxSequenceLimit() {
    List<KeyedTableFileScanHelper.SnapshotFileGroup> sequenceGroups = new ArrayList<>();
    sequenceGroups.add(buildSequenceGroup(1, 100, 2));
    Assert.assertEquals(Long.MIN_VALUE, KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 0));
    Assert.assertEquals(Long.MIN_VALUE, KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 1));
    Assert.assertEquals(Long.MAX_VALUE, KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 2));
    Assert.assertEquals(Long.MAX_VALUE, KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 3));

    sequenceGroups.add(buildSequenceGroup(2, 101, 1));
    Assert.assertEquals(1, KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 2));
    Assert.assertEquals(Long.MAX_VALUE, KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 3));

    // disorder
    sequenceGroups.add(buildSequenceGroup(5, 103, 2));
    sequenceGroups.add(buildSequenceGroup(4, 102, 2));
    sequenceGroups.add(buildSequenceGroup(3, 99, 1));
    Assert.assertEquals(Long.MIN_VALUE, KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 2));
    Assert.assertEquals(Long.MIN_VALUE, KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 3));
    Assert.assertEquals(3, KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 4));
    Assert.assertEquals(3, KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 5));
    Assert.assertEquals(4, KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 6));
    Assert.assertEquals(4, KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 7));
    Assert.assertEquals(Long.MAX_VALUE, KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 8));
    Assert.assertEquals(Long.MAX_VALUE, KeyedTableFileScanHelper.getMaxSequenceKeepingTxIdInOrder(sequenceGroups, 9));
  }


  private static KeyedTableFileScanHelper.SnapshotFileGroup buildSequenceGroup(long sequence, long txId, int cnt) {
    return new KeyedTableFileScanHelper.SnapshotFileGroup(sequence, txId, cnt);
  }

}