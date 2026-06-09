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

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.formats.iceberg.maintainer.IcebergTableMaintainer;
import org.apache.amoro.server.scheduler.inline.ExecutorTestBase;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterables;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.data.Record;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

/**
 * Reproduces a data-file loss observed in production during snapshot expiration.
 *
 * <p>When a table has a single ref (the common case after a tag is dropped), {@code
 * RemoveSnapshots} auto-selects {@link org.apache.iceberg.IncrementalFileCleanup}. That strategy
 * walks the current snapshot's ancestor chain via {@code SnapshotUtil.ancestorIds}; if a parent
 * snapshot is missing from metadata (e.g. expired by an earlier cycle) the walk terminates
 * silently. Snapshots below that break are then treated as "not an ancestor", so the ADDED entries
 * in their (superseded but still referenced) manifests are reverted and the data files are
 * physically deleted - even though those files are still carried over as EXISTING entries in the
 * current snapshot's manifest. The result is a current snapshot that references a missing file.
 *
 * <p>This test builds exactly that state and asserts the invariant that expiration must never
 * delete a data file referenced by the current snapshot. It fails on the buggy incremental path and
 * passes once {@link IcebergTableMaintainer} detects the off-main snapshot and forces the reachable
 * cleanup strategy, which never walks the ancestor chain.
 */
@RunWith(Parameterized.class)
public class TestExpireSnapshotsKeepReferencedFiles extends ExecutorTestBase {

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(false, false)}
    };
  }

  public TestExpireSnapshotsKeepReferencedFiles(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Test
  public void testExpireKeepsFilesReferencedByCurrentSnapshot() {
    UnkeyedTable table = getMixedTable().asUnkeyedTable();
    // Merge manifests explicitly below; keep auto-merge off so the fixture is deterministic.
    table
        .updateProperties()
        .set("commit.manifest-merge.enabled", "false")
        .set(TableProperties.SNAPSHOT_KEEP_DURATION, "0")
        .commit();

    // S0, S1: each append writes one data file in its own manifest (F0 in m0, F1 in m1).
    DataFile f0 = appendOneRecord(table, 1, 1L);
    DataFile f1 = appendOneRecord(table, 2, 2L);

    // S2: rewrite manifests into a single merged manifest holding F0, F1 as EXISTING entries.
    // m0/m1 are now superseded but remain referenced by the manifest lists of S0/S1.
    table.rewriteManifests().clusterBy(file -> 0).commit();
    long midSnapshotId = table.currentSnapshot().snapshotId();

    // S3 (head): append F2. The current snapshot still references F0, F1 via the merged manifest.
    DataFile f2 = appendOneRecord(table, 3, 3L);

    Assert.assertTrue(table.io().exists(f0.path().toString()));
    Assert.assertTrue(table.io().exists(f1.path().toString()));

    // Explicitly expire the middle snapshot S2. This breaks head's ancestor chain
    // (head.parent == S2, now absent), so a later ancestor walk truncates above S0/S1.
    // Snapshot-id expiration uses reachable cleanup, so the baseline files stay safe here.
    table.expireSnapshots().expireSnapshotId(midSnapshotId).cleanExpiredFiles(true).commit();
    Assert.assertTrue(table.io().exists(f0.path().toString()));
    Assert.assertTrue(table.io().exists(f1.path().toString()));

    // Only the main ref remains, which is what makes iceberg auto-select incremental cleanup.
    Assert.assertEquals(1, table.refs().size());

    IcebergTableMaintainer maintainer =
        new IcebergTableMaintainer(table, table.id(), TestTableMaintainerContext.of(table));
    maintainer.expireSnapshots(System.currentTimeMillis(), 1);

    // Sanity: head retained, history expired.
    Assert.assertEquals(1, Iterables.size(table.snapshots()));
    Assert.assertTrue(table.io().exists(f2.path().toString()));

    // Invariant: F0 and F1 are still referenced by the current snapshot, so they must survive.
    Assert.assertTrue(
        "F0 is referenced by the current snapshot and must not be deleted by expiration",
        table.io().exists(f0.path().toString()));
    Assert.assertTrue(
        "F1 is referenced by the current snapshot and must not be deleted by expiration",
        table.io().exists(f1.path().toString()));
  }

  private DataFile appendOneRecord(UnkeyedTable table, int id, long txId) {
    Record record = tableTestHelper().generateTestRecord(id, "name" + id, 0, "2022-01-01T00:00:00");
    List<DataFile> dataFiles =
        tableTestHelper().writeBaseStore(table, txId, Lists.newArrayList(record), false);
    Assert.assertEquals(1, dataFiles.size());
    AppendFiles appendFiles = table.newAppend();
    dataFiles.forEach(appendFiles::appendFile);
    appendFiles.commit();
    return dataFiles.get(0);
  }
}
