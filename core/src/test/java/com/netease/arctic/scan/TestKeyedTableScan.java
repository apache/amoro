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

package com.netease.arctic.scan;

import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.io.MixedDataTestHelpers;
import com.netease.arctic.io.TableDataTestBase;
import com.netease.arctic.io.writer.GenericChangeTaskWriter;
import com.netease.arctic.io.writer.GenericTaskWriters;
import com.netease.arctic.table.BaseTable;
import com.netease.arctic.table.ChangeTable;
import com.netease.arctic.utils.ArcticDataFiles;
import com.netease.arctic.utils.ArcticTableUtil;
import com.netease.arctic.utils.StatisticsFileUtil;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StatisticsFile;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.CloseableIterator;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.util.StructLikeMap;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestKeyedTableScan extends TableDataTestBase {

  @Test
  public void testScanWithInsertFileInBaseStore() throws IOException {
    assertFileCount(getArcticTable().asKeyedTable().newScan().planTasks(), 4, 2, 1);
    // write 2 base files
    writeInsertFileIntoBaseStore();
    assertFileCount(getArcticTable().asKeyedTable().newScan().planTasks(), 6, 2, 1);
  }

  @Test
  public void testScanWithOptimizedSequence() throws IOException {
    changeOptimizedSequence();
    assertFileCount(getArcticTable().asKeyedTable().newScan().planTasks(), 4, 0, 1);
  }

  @Test
  public void testScanWithRef() throws IOException {
    BaseTable baseTable = getArcticTable().asKeyedTable().baseTable();
    changeOptimizedSequence();
    String branchName = "test_branch";
    baseTable.manageSnapshots().createBranch(branchName, baseTable.currentSnapshot().snapshotId()).commit();
    ChangeTable changeTable = getArcticTable().asKeyedTable().changeTable();
    changeTable.manageSnapshots().createBranch(branchName, changeTable.currentSnapshot().snapshotId()).commit();
    writeChangeStore(4L, ChangeAction.INSERT, changeInsertRecords(allRecords));
    assertFileCount(getArcticTable().asKeyedTable().newScan().planTasks(), 4, 2, 1);
    assertFileCount(getArcticTable().asKeyedTable().newScan().useRef(branchName).planTasks(), 4, 0, 1);
  }

  private void changeOptimizedSequence() {
    BaseTable baseTable = getArcticTable().asKeyedTable().baseTable();
    Snapshot baseSnapshot = baseTable.currentSnapshot();
    StructLikeMap<Long> fromSequence = StructLikeMap.create(getArcticTable().spec().partitionType());
    StructLike partitionData = ArcticDataFiles.data(getArcticTable().spec(), "op_time_day=2022-01-01");
    fromSequence.put(partitionData, 1L);
    StatisticsFile file = StatisticsFileUtil.writer(baseTable, baseSnapshot.snapshotId(), baseSnapshot.sequenceNumber())
        .add(ArcticTableUtil.BLOB_TYPE_OPTIMIZED_SEQUENCE, fromSequence,
            StatisticsFileUtil.createPartitionDataSerializer(getArcticTable().spec(), Long.class))
        .complete();
    baseTable.updateStatistics()
        .setStatistics(baseSnapshot.snapshotId(), file)
        .commit();
  }

  private void assertFileCount(CloseableIterable<CombinedScanTask> combinedScanTasks, int baseFileCnt,
                               int insertFileCnt, int equDeleteFileCnt) throws IOException {
    final List<ArcticFileScanTask> allBaseTasks = new ArrayList<>();
    final List<ArcticFileScanTask> allInsertTasks = new ArrayList<>();
    final List<ArcticFileScanTask> allEquDeleteTasks = new ArrayList<>();
    try (CloseableIterator<CombinedScanTask> initTasks = combinedScanTasks.iterator()) {
      while (initTasks.hasNext()) {
        CombinedScanTask combinedScanTask = initTasks.next();
        combinedScanTask
            .tasks()
            .forEach(
                task -> {
                  allBaseTasks.addAll(task.baseTasks());
                  allInsertTasks.addAll(task.insertTasks());
                  allEquDeleteTasks.addAll(task.arcticEquityDeletes());
                });
      }
    }
    Assert.assertEquals(baseFileCnt, allBaseTasks.size());
    Assert.assertEquals(insertFileCnt, allInsertTasks.size());
    Assert.assertEquals(equDeleteFileCnt, allEquDeleteTasks.size());
  }

  private void writeInsertFileIntoBaseStore() throws IOException {
    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    builder.add(MixedDataTestHelpers.createRecord(7, "mary", 0, "2022-01-01T12:00:00"));
    builder.add(MixedDataTestHelpers.createRecord(8, "mack", 0, "2022-01-01T12:00:00"));
    ImmutableList<Record> records = builder.build();

    GenericChangeTaskWriter writer =
        GenericTaskWriters.builderFor(getArcticTable().asKeyedTable())
            .withTransactionId(5L)
            .buildChangeWriter();
    for (Record record : records) {
      writer.write(record);
    }
    WriteResult result = writer.complete();
    AppendFiles baseAppend = getArcticTable().asKeyedTable().baseTable().newAppend();
    Arrays.stream(result.dataFiles()).forEach(baseAppend::appendFile);
    baseAppend.commit();
  }
}
