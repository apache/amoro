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

package com.netease.arctic.server.excutors;

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.TableTestBase;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.io.DataTestHelpers;
import com.netease.arctic.op.UpdatePartitionProperties;
import com.netease.arctic.server.dashboard.utils.AmsUtil;
import com.netease.arctic.server.optimizing.OptimizingStatus;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.table.executor.SnapshotsExpiringExecutor;
import com.netease.arctic.server.utils.IcebergTableUtil;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.relocated.com.google.common.collect.Iterables;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@RunWith(Parameterized.class)
public class TestSnapshotExpireMix extends TableTestBase {

  private final List<DataFile> changeTableFiles = new ArrayList<>();

  public TestSnapshotExpireMix(boolean ifKeyed, boolean ifPartitioned) {
    super(new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(ifKeyed, ifPartitioned));
  }

  @Parameterized.Parameters(name = "ifKeyed = {0}, ifPartitioned = {1}")
  public static Object[][] parameters() {
    return new Object[][]{
        {true, true},
        {true, false},
        {false, true},
        {false, false}};
  }

  @Test
  public void testDeleteChangeFiles() throws Exception {
    Assume.assumeTrue(isKeyedTable());
    Assume.assumeTrue(isPartitionedTable());
    KeyedTable testKeyedTable = getArcticTable().asKeyedTable();
    List<DataFile> s1Files = insertChangeDataFiles(testKeyedTable, 1);
    List<StructLike> partitions =
        new ArrayList<>(s1Files.stream().collect(Collectors.groupingBy(ContentFile::partition)).keySet());
    Assert.assertEquals(2, partitions.size());

    UpdatePartitionProperties updateProperties = testKeyedTable.baseTable().updatePartitionProperties(null);
    updateProperties.set(partitions.get(0), TableProperties.PARTITION_OPTIMIZED_SEQUENCE, "3");
    updateProperties.set(partitions.get(1), TableProperties.PARTITION_OPTIMIZED_SEQUENCE, "0");
    updateProperties.commit();
    List<DataFile> existedDataFiles = new ArrayList<>();
    try (CloseableIterable<FileScanTask> fileScanTasks = testKeyedTable.changeTable().newScan().planFiles()) {
      fileScanTasks.forEach(fileScanTask -> existedDataFiles.add(fileScanTask.file()));
    }
    Assert.assertEquals(4, existedDataFiles.size());

    SnapshotsExpiringExecutor.deleteChangeFile(
        testKeyedTable, changeTableFiles, testKeyedTable.changeTable().currentSnapshot().sequenceNumber());
    List<DataFile> currentDataFiles = new ArrayList<>();
    try (CloseableIterable<FileScanTask> fileScanTasks = testKeyedTable.changeTable().newScan().planFiles()) {
      fileScanTasks.forEach(fileScanTask -> currentDataFiles.add(fileScanTask.file()));
    }
    Assert.assertEquals(2, currentDataFiles.size());
    changeTableFiles.forEach(file -> Assert.assertTrue(testKeyedTable.io().exists(file.path().toString())));
  }

  @Test
  public void testExpireTableFiles() throws Exception {
    Assume.assumeTrue(isKeyedTable());
    KeyedTable testKeyedTable = getArcticTable().asKeyedTable();
    List<DataFile> s1Files = insertChangeDataFiles(testKeyedTable, 1);
    List<StructLike> partitions =
        new ArrayList<>(s1Files.stream().collect(Collectors.groupingBy(ContentFile::partition)).keySet());
    if (isPartitionedTable()) {
      Assert.assertEquals(2, partitions.size());
    } else {
      Assert.assertEquals(1, partitions.size());
    }

    UpdatePartitionProperties updateProperties = testKeyedTable.baseTable().updatePartitionProperties(null);
    updateProperties.set(partitions.get(0), TableProperties.PARTITION_OPTIMIZED_SEQUENCE, "3");
    if (isPartitionedTable()) {
      updateProperties.set(partitions.get(1), TableProperties.PARTITION_OPTIMIZED_SEQUENCE, "1");
    }
    updateProperties.commit();
    s1Files.forEach(file -> Assert.assertTrue(testKeyedTable.io().exists(file.path().toString())));
    SnapshotsExpiringExecutor.deleteChangeFile(
        testKeyedTable, changeTableFiles, testKeyedTable.changeTable().currentSnapshot().sequenceNumber());
    Assert.assertEquals(2, Iterables.size(testKeyedTable.changeTable().snapshots()));
    List<DataFile> existedDataFiles = new ArrayList<>();
    try (CloseableIterable<FileScanTask> fileScanTasks = testKeyedTable.changeTable().newScan().planFiles()) {
      fileScanTasks.forEach(fileScanTask -> existedDataFiles.add(fileScanTask.file()));
    }
    Assert.assertEquals(0, existedDataFiles.size());

    insertChangeDataFiles(testKeyedTable, 2);
    SnapshotsExpiringExecutor.expireSnapshots(testKeyedTable.changeTable(), System.currentTimeMillis(), new HashSet<>());
    Assert.assertEquals(1, Iterables.size(testKeyedTable.changeTable().snapshots()));
    s1Files.forEach(file -> Assert.assertFalse(testKeyedTable.io().exists(file.path().toString())));
  }

  @Test
  public void testExpiredChangeTableFilesInBase() throws Exception {
    Assume.assumeTrue(isKeyedTable());
    Assume.assumeTrue(isPartitionedTable());
    KeyedTable testKeyedTable = getArcticTable().asKeyedTable();
    List<DataFile> s1Files = insertChangeDataFiles(testKeyedTable, 1);
    testKeyedTable.baseTable().newAppend().appendFile(s1Files.get(0)).commit();
    List<StructLike> partitions =
        new ArrayList<>(s1Files.stream().collect(Collectors.groupingBy(ContentFile::partition)).keySet());
    Assert.assertEquals(2, partitions.size());

    UpdatePartitionProperties updateProperties = testKeyedTable.baseTable().updatePartitionProperties(null);
    updateProperties.set(partitions.get(0), TableProperties.PARTITION_OPTIMIZED_SEQUENCE, "3");
    updateProperties.set(partitions.get(1), TableProperties.PARTITION_OPTIMIZED_SEQUENCE, "1");
    updateProperties.commit();
    Assert.assertTrue(testKeyedTable.io().exists((String) s1Files.get(0).path()));
    SnapshotsExpiringExecutor.deleteChangeFile(
        testKeyedTable, changeTableFiles, testKeyedTable.changeTable().currentSnapshot().sequenceNumber());
    Assert.assertEquals(2, Iterables.size(testKeyedTable.changeTable().snapshots()));

    Set<String> exclude = IcebergTableUtil.getAllContentFilePath(testKeyedTable.baseTable());
    insertChangeDataFiles(testKeyedTable, 2);
    SnapshotsExpiringExecutor.expireSnapshots(testKeyedTable.changeTable(), System.currentTimeMillis(), exclude);
    Assert.assertEquals(1, Iterables.size(testKeyedTable.changeTable().snapshots()));
    Assert.assertTrue(testKeyedTable.io().exists((String) s1Files.get(0).path()));
    Assert.assertFalse(testKeyedTable.io().exists((String) s1Files.get(1).path()));
  }

  @Test
  public void testNotExpireFlinkLatestCommit() throws IOException {
    Assume.assumeTrue(isKeyedTable());
    KeyedTable testKeyedTable = getArcticTable().asKeyedTable();
    insertChangeDataFiles(testKeyedTable, 1);
    insertChangeDataFiles(testKeyedTable, 2);
    Assert.assertEquals(Long.MAX_VALUE,
        SnapshotsExpiringExecutor.fetchLatestFlinkCommittedSnapshotTime(testKeyedTable.changeTable()));

    AppendFiles appendFiles = testKeyedTable.changeTable().newAppend();
    appendFiles.set(SnapshotsExpiringExecutor.FLINK_MAX_COMMITTED_CHECKPOINT_ID, "100");
    appendFiles.commit();
    long checkpointTime = testKeyedTable.changeTable().currentSnapshot().timestampMillis();
    Assert.assertEquals(checkpointTime,
        SnapshotsExpiringExecutor.fetchLatestFlinkCommittedSnapshotTime(testKeyedTable.changeTable()));

    AppendFiles appendFiles2 = testKeyedTable.changeTable().newAppend();
    appendFiles2.set(SnapshotsExpiringExecutor.FLINK_MAX_COMMITTED_CHECKPOINT_ID, "101");
    appendFiles2.commit();
    long checkpointTime2 = testKeyedTable.changeTable().currentSnapshot().timestampMillis();
    Assert.assertEquals(checkpointTime2,
        SnapshotsExpiringExecutor.fetchLatestFlinkCommittedSnapshotTime(testKeyedTable.changeTable()));

    insertChangeDataFiles(testKeyedTable, 2);
    Assert.assertEquals(checkpointTime2,
        SnapshotsExpiringExecutor.fetchLatestFlinkCommittedSnapshotTime(testKeyedTable.changeTable()));

    testKeyedTable.updateProperties().set(TableProperties.CHANGE_SNAPSHOT_KEEP_MINUTES, "0").commit();
    testKeyedTable.updateProperties().set(TableProperties.CHANGE_DATA_TTL, "0").commit();
    TableRuntime tableRuntime = Mockito.mock(TableRuntime.class);
    Mockito.when(tableRuntime.getTableIdentifier()).thenReturn(
        ServerTableIdentifier.of(AmsUtil.toTableIdentifier(testKeyedTable.id())));
    Mockito.when(tableRuntime.getOptimizingStatus()).thenReturn(OptimizingStatus.IDLE);

    Assert.assertEquals(5, Iterables.size(testKeyedTable.changeTable().snapshots()));
    SnapshotsExpiringExecutor.expireArcticTable(
        testKeyedTable, tableRuntime);

    Assert.assertEquals(2, Iterables.size(testKeyedTable.changeTable().snapshots()));
  }


  @Test
  public void testGetClosestFiles() throws IOException {
    Assume.assumeTrue(isKeyedTable());
    KeyedTable testKeyedTable = getArcticTable().asKeyedTable();
    insertChangeDataFiles(testKeyedTable, 1);
    Snapshot firstSnapshot = testKeyedTable.changeTable().currentSnapshot();
    insertChangeDataFiles(testKeyedTable, 2);
    long secondCommitTime = testKeyedTable.changeTable().currentSnapshot().timestampMillis();
    testKeyedTable.changeTable().newAppend().commit();

    Set<DataFile> top8Files = new HashSet<>();
    testKeyedTable.changeTable().newScan().planFiles().forEach(task -> top8Files.add(task.file()));
    Assert.assertEquals(8, top8Files.size());
    Assert.assertEquals(3, Iterables.size(testKeyedTable.changeTable().snapshots()));

    long thirdCommitTime = testKeyedTable.changeTable().currentSnapshot().timestampMillis();
    Snapshot thirdSnapshot = testKeyedTable.changeTable().currentSnapshot();

    insertChangeDataFiles(testKeyedTable, 3);
    Assert.assertEquals(12, Iterables.size(testKeyedTable.changeTable().newScan().planFiles()));
    Assert.assertEquals(4, Iterables.size(testKeyedTable.changeTable().snapshots()));

    Snapshot closestExpireSnapshot = SnapshotsExpiringExecutor.getClosestExpireSnapshot(
        testKeyedTable.changeTable(), thirdCommitTime);
    Snapshot closestExpireSnapshot2 = SnapshotsExpiringExecutor.getClosestExpireSnapshot(
        testKeyedTable.changeTable(), thirdCommitTime + 1);
    Snapshot closestExpireSnapshot3 = SnapshotsExpiringExecutor.getClosestExpireSnapshot(
        testKeyedTable.changeTable(), secondCommitTime - 1);
    Assert.assertEquals(thirdSnapshot, closestExpireSnapshot);
    Assert.assertEquals(thirdSnapshot, closestExpireSnapshot2);
    Assert.assertEquals(firstSnapshot, closestExpireSnapshot3);

    Set<CharSequence> ClosestFilesPath = new HashSet<>(SnapshotsExpiringExecutor.getClosestExpireDataFiles(
        testKeyedTable.changeTable(), closestExpireSnapshot))
        .stream().map(ContentFile::path).collect(Collectors.toSet());
    Set<CharSequence> top8FilesPath = top8Files.stream().map(ContentFile::path).collect(Collectors.toSet());

    Assert.assertTrue(top8FilesPath.equals(ClosestFilesPath));
  }

  @Test
  public void testNotExpireOptimizeCommit() {
    Assume.assumeFalse(isKeyedTable());
    UnkeyedTable testTable = getArcticTable().asUnkeyedTable();
    testTable.newAppend().commit();
    testTable.newAppend().commit();
    testTable.updateProperties().set(TableProperties.BASE_SNAPSHOT_KEEP_MINUTES, "0").commit();

    TableRuntime tableRuntime = Mockito.mock(TableRuntime.class);
    Mockito.when(tableRuntime.getTableIdentifier()).thenReturn(
        ServerTableIdentifier.of(AmsUtil.toTableIdentifier(testTable.id())));
    Mockito.when(tableRuntime.getOptimizingStatus()).thenReturn(OptimizingStatus.IDLE);
    SnapshotsExpiringExecutor.expireArcticTable(testTable, tableRuntime);
    Assert.assertEquals(1, Iterables.size(testTable.snapshots()));

    testTable.newAppend().commit();

    // mock tableRuntime which has optimizing task not committed
    long optimizeSnapshotId = testTable.currentSnapshot().snapshotId();
    Mockito.when(tableRuntime.getOptimizingStatus()).thenReturn(OptimizingStatus.COMMITTING);
    Mockito.when(tableRuntime.getCurrentSnapshotId()).thenReturn(optimizeSnapshotId);


    testTable.newAppend().commit();
    testTable.newAppend().commit();

    SnapshotsExpiringExecutor.expireArcticTable(testTable, tableRuntime);
    Assert.assertEquals(3, Iterables.size(testTable.snapshots()));
  }

  private List<DataFile> insertChangeDataFiles(KeyedTable testKeyedTable, long transactionId) throws IOException {
    List<DataFile> changeInsertFiles = DataTestHelpers.writeAndCommitChangeStore(
        testKeyedTable, transactionId, ChangeAction.INSERT, ExecutorTestUtil.createRecords(1, 100));
    changeTableFiles.addAll(changeInsertFiles);
    return changeInsertFiles;
  }

}
