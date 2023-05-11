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
import com.netease.arctic.server.dashboard.utils.AmsUtil;
import com.netease.arctic.server.optimizing.OptimizingStatus;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.table.executor.SnapshotsExpiringExecutor;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.relocated.com.google.common.collect.Iterables;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.List;

import static com.netease.arctic.server.excutors.ExecutorTestUtil.writeAndCommitBaseStore;

@RunWith(Parameterized.class)
public class TestSnapshotExpireIceberg extends TableTestBase {

  public TestSnapshotExpireIceberg(boolean ifKeyed, boolean ifPartitioned) {
    super(new BasicCatalogTestHelper(TableFormat.ICEBERG),
        new BasicTableTestHelper(ifKeyed, ifPartitioned));
  }

  @Parameterized.Parameters(name = "ifKeyed = {0}, ifPartitioned = {1}")
  public static Object[][] parameters() {
    return new Object[][]{
        {false, true},
        {false, false}};
  }


  @Test
  public void testExpireTableFiles() {
    UnkeyedTable table = getArcticTable().asUnkeyedTable();
    List<DataFile> dataFiles = writeAndCommitBaseStore(table);

    DeleteFiles deleteFiles = table.newDelete();
    for (DataFile dataFile : dataFiles) {
      Assert.assertTrue(table.io().exists(dataFile.path().toString()));
      deleteFiles.deleteFile(dataFile);
    }
    deleteFiles.commit();

    List<DataFile> newDataFiles = writeAndCommitBaseStore(table);
    Assert.assertEquals(3, Iterables.size(table.snapshots()));
    SnapshotsExpiringExecutor.expireSnapshots(table, System.currentTimeMillis(),
        new HashSet<>());
    Assert.assertEquals(1, Iterables.size(table.snapshots()));

    dataFiles.forEach(file -> Assert.assertFalse(table.io().exists(file.path().toString())));
    newDataFiles.forEach(file -> Assert.assertTrue(table.io().exists(file.path().toString())));
  }

  @Test
  public void testNotExpireFlinkLatestCommit() {
    UnkeyedTable table = getArcticTable().asUnkeyedTable();
    writeAndCommitBaseStore(table);
    Assert.assertEquals(Long.MAX_VALUE,
        SnapshotsExpiringExecutor.fetchLatestFlinkCommittedSnapshotTime(table));

    AppendFiles appendFiles = table.newAppend();
    appendFiles.set(SnapshotsExpiringExecutor.FLINK_MAX_COMMITTED_CHECKPOINT_ID, "100");
    appendFiles.commit();
    long checkpointTime = table.currentSnapshot().timestampMillis();
    Assert.assertEquals(checkpointTime,
        SnapshotsExpiringExecutor.fetchLatestFlinkCommittedSnapshotTime(table));

    AppendFiles appendFiles2 = table.newAppend();
    appendFiles2.set(SnapshotsExpiringExecutor.FLINK_MAX_COMMITTED_CHECKPOINT_ID, "101");
    appendFiles2.commit();
    long checkpointTime2 = table.currentSnapshot().timestampMillis();
    Assert.assertEquals(checkpointTime2,
        SnapshotsExpiringExecutor.fetchLatestFlinkCommittedSnapshotTime(table));

    writeAndCommitBaseStore(table);
    Assert.assertEquals(checkpointTime2,
        SnapshotsExpiringExecutor.fetchLatestFlinkCommittedSnapshotTime(table));

    table.updateProperties().set(TableProperties.BASE_SNAPSHOT_KEEP_MINUTES, "0").commit();
    TableRuntime tableRuntime = Mockito.mock(TableRuntime.class);
    Mockito.when(tableRuntime.getTableIdentifier()).thenReturn(
        ServerTableIdentifier.of(AmsUtil.toTableIdentifier(table.id())));
    Mockito.when(tableRuntime.getOptimizingStatus()).thenReturn(OptimizingStatus.IDLE);

    Assert.assertEquals(4, Iterables.size(table.snapshots()));
    SnapshotsExpiringExecutor.expireArcticTable(
        table, tableRuntime);

    Assert.assertEquals(2, Iterables.size(table.snapshots()));
  }

  @Test
  public void testNotExpireOptimizeCommit() {
    UnkeyedTable testTable = getArcticTable().asUnkeyedTable();
    // commit snapshot
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

}
