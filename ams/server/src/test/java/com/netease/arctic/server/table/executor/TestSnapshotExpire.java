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

package com.netease.arctic.server.table.executor;

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.op.UpdatePartitionProperties;
import com.netease.arctic.server.dashboard.utils.AmsUtil;
import com.netease.arctic.server.optimizing.OptimizingProcess;
import com.netease.arctic.server.optimizing.OptimizingStatus;
import com.netease.arctic.server.optimizing.maintainer.MixedTableMaintainer;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableConfiguration;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.relocated.com.google.common.collect.Iterables;
import org.apache.iceberg.relocated.com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.netease.arctic.server.optimizing.maintainer.IcebergTableMaintainer.FLINK_MAX_COMMITTED_CHECKPOINT_ID;

@RunWith(Parameterized.class)
public class TestSnapshotExpire extends ExecutorTestBase {

  private final List<DataFile> changeTableFiles = new ArrayList<>();

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][]{
        {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(true, true)},
        {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(true, false)},
        {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(false, true)},
        {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(false, false)}};
  }

  public TestSnapshotExpire(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Test
  public void testExpireChangeTableFiles() throws Exception {
    Assume.assumeTrue(isKeyedTable());
    KeyedTable testKeyedTable = getArcticTable().asKeyedTable();
    testKeyedTable.updateProperties().set(TableProperties.BASE_SNAPSHOT_KEEP_MINUTES, "0").commit();
    testKeyedTable.updateProperties().set(TableProperties.CHANGE_DATA_TTL, "0").commit();
    List<DataFile> s1Files = insertChangeDataFiles(testKeyedTable, 1);
    long l = testKeyedTable.changeTable().currentSnapshot().timestampMillis();
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
    s1Files.forEach(file -> Assert.assertTrue(testKeyedTable.changeTable().io().exists(file.path().toString())));

    MixedTableMaintainer tableMaintainer = new MixedTableMaintainer(testKeyedTable);
    tableMaintainer.getChangeMaintainer().expireFiles(l + 1);

    //In order to advance the snapshot
    insertChangeDataFiles(testKeyedTable, 2);

    tableMaintainer.getChangeMaintainer().expireSnapshots(System.currentTimeMillis());

    Assert.assertEquals(1, Iterables.size(testKeyedTable.changeTable().snapshots()));
    s1Files.forEach(file -> Assert.assertFalse(testKeyedTable.changeTable().io().exists(file.path().toString())));
  }

  @Test
  public void testExpiredChangeTableFilesInBase() {
    Assume.assumeTrue(isKeyedTable());
    Assume.assumeTrue(isPartitionedTable());
    KeyedTable testKeyedTable = getArcticTable().asKeyedTable();
    testKeyedTable.updateProperties().set(TableProperties.BASE_SNAPSHOT_KEEP_MINUTES, "0").commit();
    testKeyedTable.updateProperties().set(TableProperties.CHANGE_DATA_TTL, "0").commit();
    List<DataFile> s1Files = insertChangeDataFiles(testKeyedTable, 1);
    long l = testKeyedTable.changeTable().currentSnapshot().timestampMillis();
    testKeyedTable.baseTable().newAppend().appendFile(s1Files.get(0)).commit();
    List<StructLike> partitions =
        new ArrayList<>(s1Files.stream().collect(Collectors.groupingBy(ContentFile::partition)).keySet());
    Assert.assertEquals(2, partitions.size());

    UpdatePartitionProperties updateProperties = testKeyedTable.baseTable().updatePartitionProperties(null);
    updateProperties.set(partitions.get(0), TableProperties.PARTITION_OPTIMIZED_SEQUENCE, "3");
    updateProperties.set(partitions.get(1), TableProperties.PARTITION_OPTIMIZED_SEQUENCE, "1");
    updateProperties.commit();
    s1Files.forEach(file -> Assert.assertTrue(testKeyedTable.io().exists(file.path().toString())));

    MixedTableMaintainer tableMaintainer = new MixedTableMaintainer(testKeyedTable);
    tableMaintainer.getChangeMaintainer().expireFiles(l + 1);
    //In order to advance the snapshot
    insertChangeDataFiles(testKeyedTable, 2);
    tableMaintainer.getChangeMaintainer().expireSnapshots(System.currentTimeMillis());

    Assert.assertEquals(1, Iterables.size(testKeyedTable.changeTable().snapshots()));
    Assert.assertTrue(testKeyedTable.io().exists(s1Files.get(0).path().toString()));
    Assert.assertFalse(testKeyedTable.io().exists(s1Files.get(1).path().toString()));
  }

  @Test
  public void testNotExpireFlinkLatestCommit4ChangeTable() {
    Assume.assumeTrue(isKeyedTable());
    KeyedTable testKeyedTable = getArcticTable().asKeyedTable();
    insertChangeDataFiles(testKeyedTable, 1);
    insertChangeDataFiles(testKeyedTable, 2);

    AppendFiles appendFiles = testKeyedTable.changeTable().newAppend();
    appendFiles.set(FLINK_MAX_COMMITTED_CHECKPOINT_ID, "100");
    appendFiles.commit();

    AppendFiles appendFiles2 = testKeyedTable.changeTable().newAppend();
    appendFiles2.set(FLINK_MAX_COMMITTED_CHECKPOINT_ID, "101");
    appendFiles2.commit();
    Snapshot checkpointTime2Snapshot = testKeyedTable.changeTable().currentSnapshot();

    insertChangeDataFiles(testKeyedTable, 2);
    Snapshot lastSnapshot = testKeyedTable.changeTable().currentSnapshot();

    testKeyedTable.updateProperties().set(TableProperties.CHANGE_DATA_TTL, "0").commit();
    TableRuntime tableRuntime = Mockito.mock(TableRuntime.class);
    Mockito.when(tableRuntime.getTableIdentifier()).thenReturn(
        ServerTableIdentifier.of(AmsUtil.toTableIdentifier(testKeyedTable.id())));
    Mockito.when(tableRuntime.getOptimizingStatus()).thenReturn(OptimizingStatus.IDLE);
    Mockito.when(tableRuntime.getTableConfiguration()).thenReturn(
        TableConfiguration.parseConfig(testKeyedTable.properties()));

    Assert.assertEquals(5, Iterables.size(testKeyedTable.changeTable().snapshots()));

    MixedTableMaintainer tableMaintainer = new MixedTableMaintainer(testKeyedTable);
    tableMaintainer.expireSnapshots(tableRuntime);

    Assert.assertEquals(2, Iterables.size(testKeyedTable.changeTable().snapshots()));
    HashSet<Snapshot> expectedSnapshots = new HashSet<>();
    expectedSnapshots.add(checkpointTime2Snapshot);
    expectedSnapshots.add(lastSnapshot);
    Iterators.elementsEqual(expectedSnapshots.iterator(), testKeyedTable.changeTable().snapshots().iterator());
  }

  @Test
  public void testNotExpireFlinkLatestCommit4All() {
    UnkeyedTable table = isKeyedTable() ? getArcticTable().asKeyedTable().baseTable() :
        getArcticTable().asUnkeyedTable();
    writeAndCommitBaseStore(table);

    AppendFiles appendFiles = table.newAppend();
    appendFiles.set(FLINK_MAX_COMMITTED_CHECKPOINT_ID, "100");
    appendFiles.commit();
    long checkpointTime = table.currentSnapshot().timestampMillis();

    AppendFiles appendFiles2 = table.newAppend();
    appendFiles2.set(FLINK_MAX_COMMITTED_CHECKPOINT_ID, "101");
    appendFiles2.commit();
    Snapshot checkpointTime2Snapshot = table.currentSnapshot();

    writeAndCommitBaseStore(table);
    Snapshot lastSnapshot = table.currentSnapshot();

    table.updateProperties().set(TableProperties.BASE_SNAPSHOT_KEEP_MINUTES, "0").commit();
    TableRuntime tableRuntime = Mockito.mock(TableRuntime.class);
    Mockito.when(tableRuntime.getTableIdentifier()).thenReturn(
        ServerTableIdentifier.of(AmsUtil.toTableIdentifier(table.id())));
    Mockito.when(tableRuntime.getOptimizingStatus()).thenReturn(OptimizingStatus.IDLE);
    Mockito.when(tableRuntime.getTableConfiguration()).thenReturn(
        TableConfiguration.parseConfig(table.properties()));

    Assert.assertEquals(4, Iterables.size(table.snapshots()));

    MixedTableMaintainer tableMaintainer = new MixedTableMaintainer(table);
    tableMaintainer.expireSnapshots(tableRuntime);

    Assert.assertEquals(2, Iterables.size(table.snapshots()));
    HashSet<Snapshot> expectedSnapshots = new HashSet<>();
    expectedSnapshots.add(checkpointTime2Snapshot);
    expectedSnapshots.add(lastSnapshot);
    Iterators.elementsEqual(expectedSnapshots.iterator(), table.snapshots().iterator());
  }

  @Test
  public void testNotExpireOptimizeCommit4All() {
    UnkeyedTable table = isKeyedTable() ? getArcticTable().asKeyedTable().baseTable() :
        getArcticTable().asUnkeyedTable();
    table.newAppend().commit();
    table.newAppend().commit();
    table.updateProperties().set(TableProperties.BASE_SNAPSHOT_KEEP_MINUTES, "0").commit();

    TableRuntime tableRuntime = Mockito.mock(TableRuntime.class);
    Mockito.when(tableRuntime.getTableIdentifier()).thenReturn(
        ServerTableIdentifier.of(AmsUtil.toTableIdentifier(table.id())));
    Mockito.when(tableRuntime.getOptimizingStatus()).thenReturn(OptimizingStatus.IDLE);
    Mockito.when(tableRuntime.getTableConfiguration()).thenReturn(
        TableConfiguration.parseConfig(table.properties()));

    new MixedTableMaintainer(table).expireSnapshots(tableRuntime);
    Assert.assertEquals(1, Iterables.size(table.snapshots()));

    table.newAppend().commit();

    // mock tableRuntime which has optimizing task not committed
    long optimizeSnapshotId = table.currentSnapshot().snapshotId();
    OptimizingProcess optimizingProcess = Mockito.mock(OptimizingProcess.class);
    Mockito.when(optimizingProcess.getTargetSnapshotId()).thenReturn(optimizeSnapshotId);
    Mockito.when(tableRuntime.getOptimizingStatus()).thenReturn(OptimizingStatus.COMMITTING);
    Mockito.when(tableRuntime.getOptimizingProcess()).thenReturn(optimizingProcess);
    HashSet<Snapshot> expectedSnapshots = new HashSet<>();
    expectedSnapshots.add(table.currentSnapshot());

    table.newAppend().commit();
    expectedSnapshots.add(table.currentSnapshot());
    table.newAppend().commit();
    expectedSnapshots.add(table.currentSnapshot());

    new MixedTableMaintainer(table).expireSnapshots(tableRuntime);
    Assert.assertEquals(3, Iterables.size(table.snapshots()));
    Iterators.elementsEqual(expectedSnapshots.iterator(), table.snapshots().iterator());
  }

  @Test
  public void testExpireTableFiles4All() {
    UnkeyedTable table = isKeyedTable() ? getArcticTable().asKeyedTable().baseTable() :
        getArcticTable().asUnkeyedTable();
    table.updateProperties().set(TableProperties.BASE_SNAPSHOT_KEEP_MINUTES, "0").commit();
    List<DataFile> dataFiles = writeAndCommitBaseStore(table);

    DeleteFiles deleteFiles = table.newDelete();
    for (DataFile dataFile : dataFiles) {
      Assert.assertTrue(table.io().exists(dataFile.path().toString()));
      deleteFiles.deleteFile(dataFile);
    }
    deleteFiles.commit();

    List<DataFile> newDataFiles = writeAndCommitBaseStore(table);
    Assert.assertEquals(3, Iterables.size(table.snapshots()));
    new MixedTableMaintainer(table).expireSnapshots(System.currentTimeMillis());
    Assert.assertEquals(1, Iterables.size(table.snapshots()));

    dataFiles.forEach(file -> Assert.assertFalse(table.io().exists(file.path().toString())));
    newDataFiles.forEach(file -> Assert.assertTrue(table.io().exists(file.path().toString())));
  }

  @Test
  public void testExpireTableFilesRepeatedly() {
    Assume.assumeTrue(isKeyedTable());
    KeyedTable testKeyedTable = getArcticTable().asKeyedTable();

    testKeyedTable.updateProperties().set(TableProperties.BASE_SNAPSHOT_KEEP_MINUTES, "0").commit();
    testKeyedTable.updateProperties().set(TableProperties.CHANGE_DATA_TTL, "0").commit();

    List<DataFile> s1Files = insertChangeDataFiles(testKeyedTable, 1);
    insertChangeDataFiles(testKeyedTable, 2);
    long secondCommitTime = testKeyedTable.changeTable().currentSnapshot().timestampMillis();

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
      updateProperties.set(partitions.get(1), TableProperties.PARTITION_OPTIMIZED_SEQUENCE, "3");
    }
    updateProperties.commit();

    Set<DataFile> top8Files = new HashSet<>();
    testKeyedTable.changeTable().newScan().planFiles().forEach(task -> top8Files.add(task.file()));
    Assert.assertEquals(8, top8Files.size());
    Assert.assertEquals(2, Iterables.size(testKeyedTable.changeTable().snapshots()));

    Set<CharSequence> last4File = insertChangeDataFiles(testKeyedTable, 3).stream()
        .map(DataFile::path).collect(Collectors.toSet());
    long thirdCommitTime = testKeyedTable.changeTable().currentSnapshot().timestampMillis();
    Assert.assertEquals(12, Iterables.size(testKeyedTable.changeTable().newScan().planFiles()));
    Assert.assertEquals(3, Iterables.size(testKeyedTable.changeTable().snapshots()));

    MixedTableMaintainer tableMaintainer = new MixedTableMaintainer(testKeyedTable);
    tableMaintainer.getChangeMaintainer().expireFiles(secondCommitTime + 1);
    tableMaintainer.getChangeMaintainer().expireSnapshots(secondCommitTime + 1);

    Set<CharSequence> dataFiles = getDataFiles(testKeyedTable);
    Assert.assertEquals(last4File, dataFiles);

    Assert.assertEquals(2, Iterables.size(testKeyedTable.changeTable().snapshots()));
  }

  @NotNull
  private static Set<CharSequence> getDataFiles(KeyedTable testKeyedTable) {
    Set<CharSequence> dataFiles = new HashSet<>();
    testKeyedTable.changeTable().newScan().planFiles().forEach(
        task -> dataFiles.add(task.file().path())
    );
    return dataFiles;
  }

  private List<DataFile> insertChangeDataFiles(KeyedTable testKeyedTable, long transactionId) {
    List<DataFile> changeInsertFiles = writeAndCommitChangeStore(
        testKeyedTable, transactionId, ChangeAction.INSERT, createRecords(1, 100));
    changeTableFiles.addAll(changeInsertFiles);
    return changeInsertFiles;
  }
}
