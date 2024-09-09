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

package org.apache.amoro.server.optimizing.maintainer;

import static org.apache.amoro.server.optimizing.maintainer.IcebergTableMaintainer.FLINK_MAX_COMMITTED_CHECKPOINT_ID;
import static org.apache.amoro.utils.MixedTableUtil.BLOB_TYPE_OPTIMIZED_SEQUENCE_EXIST;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.data.ChangeAction;
import org.apache.amoro.server.dashboard.utils.AmsUtil;
import org.apache.amoro.server.optimizing.OptimizingProcess;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.table.TableConfigurations;
import org.apache.amoro.server.table.TableRuntime;
import org.apache.amoro.server.table.executor.ExecutorTestBase;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterables;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterators;
import org.apache.amoro.table.BaseTable;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.MixedTableUtil;
import org.apache.amoro.utils.StatisticsFileUtil;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StatisticsFile;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.util.StructLikeMap;
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

@RunWith(Parameterized.class)
public class TestSnapshotExpire extends ExecutorTestBase {

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, true)},
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, false)
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(false, true)
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(false, false)
      }
    };
  }

  public TestSnapshotExpire(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Test
  public void testExpireChangeTableFiles() {
    Assume.assumeTrue(isKeyedTable());
    KeyedTable testKeyedTable = getMixedTable().asKeyedTable();
    testKeyedTable.updateProperties().set(TableProperties.BASE_SNAPSHOT_KEEP_MINUTES, "0").commit();
    testKeyedTable.updateProperties().set(TableProperties.CHANGE_DATA_TTL, "0").commit();
    List<DataFile> s1Files = insertChangeDataFiles(testKeyedTable, 1);
    long l = testKeyedTable.changeTable().currentSnapshot().timestampMillis();
    List<StructLike> partitions =
        new ArrayList<>(
            s1Files.stream().collect(Collectors.groupingBy(ContentFile::partition)).keySet());
    if (isPartitionedTable()) {
      Assert.assertEquals(2, partitions.size());
    } else {
      Assert.assertEquals(1, partitions.size());
    }

    StructLikeMap<Long> optimizedSequence =
        StructLikeMap.create(testKeyedTable.spec().partitionType());
    optimizedSequence.put(partitions.get(0), 3L);
    if (isPartitionedTable()) {
      optimizedSequence.put(partitions.get(1), 1L);
    }
    writeOptimizedSequence(testKeyedTable, optimizedSequence);
    s1Files.forEach(
        file ->
            Assert.assertTrue(testKeyedTable.changeTable().io().exists(file.path().toString())));

    MixedTableMaintainer tableMaintainer = new MixedTableMaintainer(testKeyedTable);
    tableMaintainer.getChangeMaintainer().expireFiles(l + 1);

    // In order to advance the snapshot
    insertChangeDataFiles(testKeyedTable, 2);

    tableMaintainer.getChangeMaintainer().expireSnapshots(System.currentTimeMillis(), 1);

    Assert.assertEquals(1, Iterables.size(testKeyedTable.changeTable().snapshots()));
    s1Files.forEach(
        file ->
            Assert.assertFalse(testKeyedTable.changeTable().io().exists(file.path().toString())));
  }

  private void writeOptimizedSequence(
      KeyedTable testKeyedTable, StructLikeMap<Long> optimizedSequence) {
    BaseTable baseTable = testKeyedTable.baseTable();
    baseTable.newAppend().set(BLOB_TYPE_OPTIMIZED_SEQUENCE_EXIST, "true").commit();
    Snapshot snapshot = baseTable.currentSnapshot();
    StatisticsFile statisticsFile =
        StatisticsFileUtil.writerBuilder(baseTable)
            .withSnapshotId(snapshot.snapshotId())
            .build()
            .add(
                MixedTableUtil.BLOB_TYPE_OPTIMIZED_SEQUENCE,
                optimizedSequence,
                StatisticsFileUtil.createPartitionDataSerializer(baseTable.spec(), Long.class))
            .complete();
    baseTable.updateStatistics().setStatistics(snapshot.snapshotId(), statisticsFile).commit();
  }

  @Test
  public void testNotExpireFlinkLatestCommit4ChangeTable() {
    Assume.assumeTrue(isKeyedTable());
    KeyedTable testKeyedTable = getMixedTable().asKeyedTable();
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
    Mockito.when(tableRuntime.getTableIdentifier())
        .thenReturn(
            ServerTableIdentifier.of(
                AmsUtil.toTableIdentifier(testKeyedTable.id()), getTestFormat()));
    Mockito.when(tableRuntime.getOptimizingStatus()).thenReturn(OptimizingStatus.IDLE);
    Mockito.when(tableRuntime.getTableConfiguration())
        .thenReturn(TableConfigurations.parseTableConfig((testKeyedTable.properties())));

    Assert.assertEquals(5, Iterables.size(testKeyedTable.changeTable().snapshots()));

    MixedTableMaintainer tableMaintainer = new MixedTableMaintainer(testKeyedTable);
    tableMaintainer.expireSnapshots(tableRuntime);

    Assert.assertEquals(2, Iterables.size(testKeyedTable.changeTable().snapshots()));
    List<Snapshot> expectedSnapshots = new ArrayList<>();
    expectedSnapshots.add(checkpointTime2Snapshot);
    expectedSnapshots.add(lastSnapshot);
    Assert.assertTrue(
        Iterators.elementsEqual(
            expectedSnapshots.iterator(), testKeyedTable.changeTable().snapshots().iterator()));
  }

  @Test
  public void testNotExpireFlinkLatestCommit4All() {
    UnkeyedTable table =
        isKeyedTable()
            ? getMixedTable().asKeyedTable().baseTable()
            : getMixedTable().asUnkeyedTable();
    writeAndCommitBaseStore(table);

    AppendFiles appendFiles = table.newAppend();
    appendFiles.set(FLINK_MAX_COMMITTED_CHECKPOINT_ID, "100");
    appendFiles.commit();

    AppendFiles appendFiles2 = table.newAppend();
    appendFiles2.set(FLINK_MAX_COMMITTED_CHECKPOINT_ID, "101");
    appendFiles2.commit();
    Snapshot checkpointTime2Snapshot = table.currentSnapshot();

    writeAndCommitBaseStore(table);
    Snapshot lastSnapshot = table.currentSnapshot();

    table.updateProperties().set(TableProperties.BASE_SNAPSHOT_KEEP_MINUTES, "0").commit();
    TableRuntime tableRuntime = Mockito.mock(TableRuntime.class);
    Mockito.when(tableRuntime.getTableIdentifier())
        .thenReturn(
            ServerTableIdentifier.of(AmsUtil.toTableIdentifier(table.id()), getTestFormat()));
    Mockito.when(tableRuntime.getOptimizingStatus()).thenReturn(OptimizingStatus.IDLE);
    Mockito.when(tableRuntime.getTableConfiguration())
        .thenReturn(TableConfigurations.parseTableConfig(table.properties()));

    Assert.assertEquals(4, Iterables.size(table.snapshots()));

    MixedTableMaintainer tableMaintainer = new MixedTableMaintainer(getMixedTable());
    tableMaintainer.expireSnapshots(tableRuntime);

    Assert.assertEquals(2, Iterables.size(table.snapshots()));
    List<Snapshot> expectedSnapshots = new ArrayList<>();
    expectedSnapshots.add(checkpointTime2Snapshot);
    expectedSnapshots.add(lastSnapshot);
    Assert.assertTrue(
        Iterators.elementsEqual(expectedSnapshots.iterator(), table.snapshots().iterator()));
  }

  @Test
  public void testNotExpireOptimizedSequenceCommit4All() {
    Assume.assumeTrue(isKeyedTable());
    BaseTable table = getMixedTable().asKeyedTable().baseTable();
    writeAndCommitBaseStore(table);

    AppendFiles appendFiles = table.newAppend();
    appendFiles.set(BLOB_TYPE_OPTIMIZED_SEQUENCE_EXIST, "true");
    appendFiles.commit();

    AppendFiles appendFiles2 = table.newAppend();
    appendFiles2.set(BLOB_TYPE_OPTIMIZED_SEQUENCE_EXIST, "true");
    appendFiles2.commit();
    Snapshot checkpointTime2Snapshot = table.currentSnapshot();

    writeAndCommitBaseStore(table);
    Snapshot lastSnapshot = table.currentSnapshot();

    table.updateProperties().set(TableProperties.BASE_SNAPSHOT_KEEP_MINUTES, "0").commit();
    TableRuntime tableRuntime = Mockito.mock(TableRuntime.class);
    Mockito.when(tableRuntime.getTableIdentifier())
        .thenReturn(
            ServerTableIdentifier.of(AmsUtil.toTableIdentifier(table.id()), getTestFormat()));
    Mockito.when(tableRuntime.getOptimizingStatus()).thenReturn(OptimizingStatus.IDLE);
    Mockito.when(tableRuntime.getTableConfiguration())
        .thenReturn(TableConfigurations.parseTableConfig(table.properties()));

    Assert.assertEquals(4, Iterables.size(table.snapshots()));

    MixedTableMaintainer tableMaintainer = new MixedTableMaintainer(getMixedTable());
    tableMaintainer.expireSnapshots(tableRuntime);

    Assert.assertEquals(2, Iterables.size(table.snapshots()));
    List<Snapshot> expectedSnapshots = new ArrayList<>();
    expectedSnapshots.add(checkpointTime2Snapshot);
    expectedSnapshots.add(lastSnapshot);
    Assert.assertTrue(
        Iterators.elementsEqual(expectedSnapshots.iterator(), table.snapshots().iterator()));
  }

  @Test
  public void testNotExpireOptimizeCommit4All() {
    UnkeyedTable table =
        isKeyedTable()
            ? getMixedTable().asKeyedTable().baseTable()
            : getMixedTable().asUnkeyedTable();
    table.newAppend().commit();
    table.newAppend().commit();
    table.updateProperties().set(TableProperties.BASE_SNAPSHOT_KEEP_MINUTES, "0").commit();

    TableRuntime tableRuntime = Mockito.mock(TableRuntime.class);
    Mockito.when(tableRuntime.getTableIdentifier())
        .thenReturn(
            ServerTableIdentifier.of(AmsUtil.toTableIdentifier(table.id()), getTestFormat()));
    Mockito.when(tableRuntime.getOptimizingStatus()).thenReturn(OptimizingStatus.IDLE);
    Mockito.when(tableRuntime.getTableConfiguration())
        .thenReturn(TableConfigurations.parseTableConfig(table.properties()));

    new MixedTableMaintainer(table).expireSnapshots(tableRuntime);
    Assert.assertEquals(1, Iterables.size(table.snapshots()));

    table.newAppend().commit();

    // mock tableRuntime which has optimizing task not committed
    long optimizeSnapshotId = table.currentSnapshot().snapshotId();
    OptimizingProcess optimizingProcess = Mockito.mock(OptimizingProcess.class);
    Mockito.when(optimizingProcess.getTargetSnapshotId()).thenReturn(optimizeSnapshotId);
    Mockito.when(tableRuntime.getOptimizingStatus()).thenReturn(OptimizingStatus.COMMITTING);
    Mockito.when(tableRuntime.getOptimizingProcess()).thenReturn(optimizingProcess);
    List<Snapshot> expectedSnapshots = new ArrayList<>();
    expectedSnapshots.add(table.currentSnapshot());

    table.newAppend().commit();
    expectedSnapshots.add(table.currentSnapshot());
    table.newAppend().commit();
    expectedSnapshots.add(table.currentSnapshot());

    new MixedTableMaintainer(table).expireSnapshots(tableRuntime);
    Assert.assertEquals(3, Iterables.size(table.snapshots()));
    Assert.assertTrue(
        Iterators.elementsEqual(expectedSnapshots.iterator(), table.snapshots().iterator()));
  }

  @Test
  public void testExpireTableFiles4All() {
    UnkeyedTable table =
        isKeyedTable()
            ? getMixedTable().asKeyedTable().baseTable()
            : getMixedTable().asUnkeyedTable();
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
    new MixedTableMaintainer(table).expireSnapshots(System.currentTimeMillis(), 1);
    Assert.assertEquals(1, Iterables.size(table.snapshots()));

    dataFiles.forEach(file -> Assert.assertFalse(table.io().exists(file.path().toString())));
    newDataFiles.forEach(file -> Assert.assertTrue(table.io().exists(file.path().toString())));
  }

  @Test
  public void testExpireTableFilesRepeatedly() {
    Assume.assumeTrue(isKeyedTable());
    KeyedTable testKeyedTable = getMixedTable().asKeyedTable();

    testKeyedTable.updateProperties().set(TableProperties.BASE_SNAPSHOT_KEEP_MINUTES, "0").commit();
    testKeyedTable.updateProperties().set(TableProperties.CHANGE_DATA_TTL, "0").commit();

    List<DataFile> s1Files = insertChangeDataFiles(testKeyedTable, 1);
    insertChangeDataFiles(testKeyedTable, 2);
    long secondCommitTime = testKeyedTable.changeTable().currentSnapshot().timestampMillis();

    List<StructLike> partitions =
        new ArrayList<>(
            s1Files.stream().collect(Collectors.groupingBy(ContentFile::partition)).keySet());
    if (isPartitionedTable()) {
      Assert.assertEquals(2, partitions.size());
    } else {
      Assert.assertEquals(1, partitions.size());
    }

    StructLikeMap<Long> optimizedSequence =
        StructLikeMap.create(testKeyedTable.spec().partitionType());
    optimizedSequence.put(partitions.get(0), 3L);
    if (isPartitionedTable()) {
      optimizedSequence.put(partitions.get(1), 3L);
    }
    writeOptimizedSequence(testKeyedTable, optimizedSequence);

    Set<DataFile> top8Files = new HashSet<>();
    testKeyedTable.changeTable().newScan().planFiles().forEach(task -> top8Files.add(task.file()));
    Assert.assertEquals(8, top8Files.size());
    Assert.assertEquals(2, Iterables.size(testKeyedTable.changeTable().snapshots()));

    Set<CharSequence> last4File =
        insertChangeDataFiles(testKeyedTable, 3).stream()
            .map(DataFile::path)
            .collect(Collectors.toSet());
    Assert.assertEquals(12, Iterables.size(testKeyedTable.changeTable().newScan().planFiles()));
    Assert.assertEquals(3, Iterables.size(testKeyedTable.changeTable().snapshots()));

    MixedTableMaintainer tableMaintainer = new MixedTableMaintainer(testKeyedTable);
    tableMaintainer.getChangeMaintainer().expireFiles(secondCommitTime + 1);
    tableMaintainer.getChangeMaintainer().expireSnapshots(secondCommitTime + 1, 1);

    Set<CharSequence> dataFiles = getDataFiles(testKeyedTable);
    Assert.assertEquals(last4File, dataFiles);

    Assert.assertEquals(2, Iterables.size(testKeyedTable.changeTable().snapshots()));
  }

  @NotNull
  private static Set<CharSequence> getDataFiles(KeyedTable testKeyedTable) {
    Set<CharSequence> dataFiles = new HashSet<>();
    testKeyedTable
        .changeTable()
        .newScan()
        .planFiles()
        .forEach(task -> dataFiles.add(task.file().path()));
    return dataFiles;
  }

  @Test
  public void testExpireStatisticsFiles() {
    Assume.assumeTrue(isKeyedTable());
    KeyedTable testKeyedTable = getMixedTable().asKeyedTable();
    BaseTable baseTable = testKeyedTable.baseTable();
    testKeyedTable.updateProperties().set(TableProperties.BASE_SNAPSHOT_KEEP_MINUTES, "0").commit();
    // commit an empty snapshot and its statistic file
    baseTable.newAppend().commit();
    Snapshot s1 = baseTable.currentSnapshot();
    StatisticsFile file1 =
        StatisticsFileUtil.writerBuilder(baseTable)
            .withSnapshotId(s1.snapshotId())
            .build()
            .add(
                MixedTableUtil.BLOB_TYPE_OPTIMIZED_SEQUENCE,
                StructLikeMap.create(baseTable.spec().partitionType()),
                StatisticsFileUtil.createPartitionDataSerializer(baseTable.spec(), Long.class))
            .complete();
    baseTable.updateStatistics().setStatistics(s1.snapshotId(), file1).commit();

    // commit an empty snapshot and its statistic file
    baseTable.newAppend().commit();
    Snapshot s2 = baseTable.currentSnapshot();
    StatisticsFile file2 =
        StatisticsFileUtil.writerBuilder(baseTable)
            .withSnapshotId(s2.snapshotId())
            .build()
            .add(
                MixedTableUtil.BLOB_TYPE_OPTIMIZED_SEQUENCE,
                StructLikeMap.create(baseTable.spec().partitionType()),
                StatisticsFileUtil.createPartitionDataSerializer(baseTable.spec(), Long.class))
            .complete();
    baseTable.updateStatistics().setStatistics(s2.snapshotId(), file2).commit();

    long expireTime = waitUntilAfter(s2.timestampMillis());

    // commit an empty snapshot and its statistic file
    baseTable.newAppend().commit();
    Snapshot s3 = baseTable.currentSnapshot();
    // note: s2 ans s3 use the same statistics file
    StatisticsFile file3 = StatisticsFileUtil.copyToSnapshot(file2, s3.snapshotId());
    baseTable.updateStatistics().setStatistics(s3.snapshotId(), file3).commit();

    Assert.assertEquals(3, Iterables.size(baseTable.snapshots()));
    Assert.assertTrue(baseTable.io().exists(file1.path()));
    Assert.assertTrue(baseTable.io().exists(file2.path()));
    Assert.assertTrue(baseTable.io().exists(file3.path()));
    new MixedTableMaintainer(testKeyedTable).expireSnapshots(expireTime, 1);

    Assert.assertEquals(1, Iterables.size(baseTable.snapshots()));
    Assert.assertFalse(baseTable.io().exists(file1.path()));
    // file2 should not be removed, since it is used by s3
    Assert.assertTrue(baseTable.io().exists(file2.path()));
    Assert.assertTrue(baseTable.io().exists(file3.path()));
  }

  @Test
  public void testChangeTableGcDisabled() {
    Assume.assumeTrue(isKeyedTable());
    KeyedTable testKeyedTable = getMixedTable().asKeyedTable();
    testKeyedTable.updateProperties().set("gc.enabled", "false").commit();

    insertChangeDataFiles(testKeyedTable, 1);
    insertChangeDataFiles(testKeyedTable, 2);

    Assert.assertEquals(2, Iterables.size(testKeyedTable.changeTable().snapshots()));

    TableRuntime tableRuntime = Mockito.mock(TableRuntime.class);
    Mockito.when(tableRuntime.getTableIdentifier())
        .thenReturn(
            ServerTableIdentifier.of(
                AmsUtil.toTableIdentifier(testKeyedTable.id()), getTestFormat()));
    Mockito.when(tableRuntime.getOptimizingStatus()).thenReturn(OptimizingStatus.IDLE);
    Mockito.when(tableRuntime.getTableConfiguration())
        .thenReturn(TableConfigurations.parseTableConfig(testKeyedTable.properties()));

    MixedTableMaintainer tableMaintainer = new MixedTableMaintainer(testKeyedTable);
    testKeyedTable.updateProperties().set(TableProperties.CHANGE_DATA_TTL, "0").commit();
    tableMaintainer.expireSnapshots(tableRuntime);
    Assert.assertEquals(2, Iterables.size(testKeyedTable.changeTable().snapshots()));

    testKeyedTable.updateProperties().set("gc.enabled", "true").commit();
    Mockito.when(tableRuntime.getTableConfiguration())
        .thenReturn(TableConfigurations.parseTableConfig(testKeyedTable.properties()));
    tableMaintainer.expireSnapshots(tableRuntime);
    Assert.assertEquals(1, Iterables.size(testKeyedTable.changeTable().snapshots()));
  }

  @Test
  public void testBaseTableGcDisabled() {
    Assume.assumeFalse(isKeyedTable());
    UnkeyedTable testUnkeyedTable = getMixedTable().asUnkeyedTable();
    testUnkeyedTable.updateProperties().set("gc.enabled", "false").commit();

    testUnkeyedTable.newAppend().commit();
    testUnkeyedTable.newAppend().commit();

    Assert.assertEquals(2, Iterables.size(testUnkeyedTable.snapshots()));

    TableRuntime tableRuntime = Mockito.mock(TableRuntime.class);
    Mockito.when(tableRuntime.getTableIdentifier())
        .thenReturn(
            ServerTableIdentifier.of(
                AmsUtil.toTableIdentifier(testUnkeyedTable.id()), getTestFormat()));
    Mockito.when(tableRuntime.getOptimizingStatus()).thenReturn(OptimizingStatus.IDLE);
    Mockito.when(tableRuntime.getTableConfiguration())
        .thenReturn(TableConfigurations.parseTableConfig(testUnkeyedTable.properties()));

    MixedTableMaintainer tableMaintainer = new MixedTableMaintainer(testUnkeyedTable);
    testUnkeyedTable
        .updateProperties()
        .set(TableProperties.BASE_SNAPSHOT_KEEP_MINUTES, "0")
        .commit();
    tableMaintainer.expireSnapshots(tableRuntime);
    Assert.assertEquals(2, Iterables.size(testUnkeyedTable.snapshots()));

    testUnkeyedTable.updateProperties().set("gc.enabled", "true").commit();
    Mockito.when(tableRuntime.getTableConfiguration())
        .thenReturn(TableConfigurations.parseTableConfig(testUnkeyedTable.properties()));
    tableMaintainer.expireSnapshots(tableRuntime);
    Assert.assertEquals(1, Iterables.size(testUnkeyedTable.snapshots()));
  }

  @Test
  public void testRetainMinSnapshot() {
    UnkeyedTable table =
        isKeyedTable()
            ? getMixedTable().asKeyedTable().baseTable()
            : getMixedTable().asUnkeyedTable();
    table.newAppend().commit();
    table.newAppend().commit();
    List<Snapshot> expectedSnapshots = new ArrayList<>();
    expectedSnapshots.add(table.currentSnapshot());

    table.updateProperties().set(TableProperties.SNAPSHOT_KEEP_DURATION, "0s").commit();
    table.updateProperties().set(TableProperties.SNAPSHOT_MIN_COUNT, "3").commit();

    TableRuntime tableRuntime = Mockito.mock(TableRuntime.class);
    Mockito.when(tableRuntime.getTableIdentifier())
        .thenReturn(
            ServerTableIdentifier.of(AmsUtil.toTableIdentifier(table.id()), getTestFormat()));
    Mockito.when(tableRuntime.getTableConfiguration())
        .thenReturn(TableConfigurations.parseTableConfig(table.properties()));
    Mockito.when(tableRuntime.getOptimizingStatus()).thenReturn(OptimizingStatus.IDLE);

    new MixedTableMaintainer(table).expireSnapshots(tableRuntime);
    Assert.assertEquals(2, Iterables.size(table.snapshots()));

    table.newAppend().commit();
    expectedSnapshots.add(table.currentSnapshot());
    table.newAppend().commit();
    expectedSnapshots.add(table.currentSnapshot());

    new MixedTableMaintainer(table).expireSnapshots(tableRuntime);
    Assert.assertEquals(3, Iterables.size(table.snapshots()));
    Assert.assertTrue(
        Iterators.elementsEqual(expectedSnapshots.iterator(), table.snapshots().iterator()));
  }

  @Test
  public void testSnapshotExpireConfig() {
    UnkeyedTable table =
        isKeyedTable()
            ? getMixedTable().asKeyedTable().baseTable()
            : getMixedTable().asUnkeyedTable();
    table.updateProperties().set(TableProperties.SNAPSHOT_KEEP_DURATION, "180s").commit();
    Assert.assertEquals(
        3L, TableConfigurations.parseTableConfig(table.properties()).getSnapshotTTLMinutes());

    // using default time unit: minutes
    table.updateProperties().set(TableProperties.SNAPSHOT_KEEP_DURATION, "720").commit();
    Assert.assertEquals(
        720L, TableConfigurations.parseTableConfig(table.properties()).getSnapshotTTLMinutes());

    table.updateProperties().set(TableProperties.SNAPSHOT_MIN_COUNT, "10").commit();
    Assert.assertEquals(
        10, TableConfigurations.parseTableConfig(table.properties()).getSnapshotMinCount());
  }

  private long waitUntilAfter(long timestampMillis) {
    long current = System.currentTimeMillis();
    while (current <= timestampMillis) {
      current = System.currentTimeMillis();
    }
    return current;
  }

  private List<DataFile> insertChangeDataFiles(KeyedTable testKeyedTable, long transactionId) {
    return writeAndCommitChangeStore(
        testKeyedTable, transactionId, ChangeAction.INSERT, createRecords(1, 100));
  }
}
