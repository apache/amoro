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

package org.apache.amoro.server.optimizing.commit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.apache.amoro.exception.OptimizingCommitException;
import org.apache.amoro.formats.iceberg.IcebergV3TestTables;
import org.apache.amoro.io.IcebergDataTestHelpers;
import org.apache.amoro.optimizing.RewriteFilesInput;
import org.apache.amoro.optimizing.RewriteFilesOutput;
import org.apache.amoro.optimizing.RewriteStageTask;
import org.apache.amoro.server.optimizing.TaskRuntime;
import org.apache.amoro.server.optimizing.UnKeyedTableCommit;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.BaseTable;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.HasTableOperations;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.data.FileHelpers;
import org.apache.iceberg.exceptions.CommitFailedException;
import org.apache.iceberg.hadoop.HadoopTables;
import org.apache.iceberg.io.OutputFileFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

class TestIcebergV3OptimizingCommit {
  @TempDir private Path temp;

  @Test
  void testV3OptimizingCommitDoesNotChangeMetadata() throws Exception {
    UnkeyedTable table = IcebergV3TestTables.create(temp.resolve("v3"), 3);
    assertCommitRejected(table, prepareCommit(table));
  }

  @ParameterizedTest(name = "external V{1} update at {0}")
  @CsvSource({
    "transaction-commit, 3",
    "first-storage-commit, 3",
    "transaction-commit, 2",
    "first-storage-commit, 2"
  })
  void testOptimizingCommitAcrossConcurrentMetadataChange(String upgradePoint, int externalVersion)
      throws Exception {
    UnkeyedTable original = IcebergV3TestTables.create(temp.resolve("race"), 2);
    TableOperations operations = spy(((HasTableOperations) original).operations());
    UnkeyedTable table = spy(IcebergV3TestTables.wrap(new BaseTable(operations, original.name())));
    UnKeyedTableCommit commit = prepareCommit(table);
    List<Integer> attemptedVersions = new ArrayList<>();
    List<Integer> committedVersions = new ArrayList<>();
    AtomicInteger conflicts = new AtomicInteger();
    AtomicReference<UnkeyedTable> external = new AtomicReference<>();
    AtomicReference<String> externalLocation = new AtomicReference<>();
    AtomicReference<byte[]> externalBytes = new AtomicReference<>();

    // This hook observes real storage commits. It does not manufacture a conflict:
    // the independent upgrade occupies the next Hadoop metadata version, causing the
    // stale commit to fail and Iceberg's real transaction retry to refresh its base.
    doAnswer(
            invocation -> {
              TableMetadata base = invocation.getArgument(0);
              TableMetadata updated = invocation.getArgument(1);
              attemptedVersions.add(updated.formatVersion());
              if (external.get() == null) {
                assertEquals("first-storage-commit", upgradePoint);
                assertEquals(2, base.formatVersion());
                external.set(commitExternalChange(table, externalVersion));
                externalLocation.set(IcebergV3TestTables.metadataLocation(external.get()));
                externalBytes.set(IcebergV3TestTables.metadataBytes(external.get()));
              }
              try {
                Object result = invocation.callRealMethod();
                committedVersions.add(updated.formatVersion());
                return result;
              } catch (CommitFailedException conflict) {
                conflicts.incrementAndGet();
                throw conflict;
              }
            })
        .when(operations)
        .commit(any(TableMetadata.class), any(TableMetadata.class));

    if (upgradePoint.equals("transaction-commit")) {
      long originalSnapshotId = table.currentSnapshot().snapshotId();
      doAnswer(
              invocation -> {
                TableMetadata staged = invocation.getArgument(0);
                // Observe the staged rewrite through the underlying operations, independent
                // of which Table facade the production code uses for its transaction.
                if (external.get() == null
                    && staged.currentSnapshot().snapshotId() != originalSnapshotId) {
                  assertEquals(2, staged.formatVersion());
                  external.set(commitExternalChange(table, externalVersion));
                  externalLocation.set(IcebergV3TestTables.metadataLocation(external.get()));
                  externalBytes.set(IcebergV3TestTables.metadataBytes(external.get()));
                }
                return invocation.callRealMethod();
              })
          .when(operations)
          .temp(any(TableMetadata.class));
    }

    long snapshotId = table.currentSnapshot().snapshotId();
    Set<String> files = filePaths(table);
    if (externalVersion == 2) {
      // Exercise exactly the same hooks and retry while the table remains supported.
      commit.commit();
      assertNotNull(external.get(), "The external update hook was not reached");
      external.get().refresh();
      assertAll(
          () -> assertEquals(upgradePoint.equals("first-storage-commit") ? 1 : 0, conflicts.get()),
          () ->
              assertEquals(
                  upgradePoint.equals("first-storage-commit")
                      ? Arrays.asList(2, 2)
                      : Collections.singletonList(2),
                  attemptedVersions),
          () -> assertEquals(Collections.singletonList(2), committedVersions),
          () -> assertEquals("true", external.get().properties().get("test.concurrent-update")),
          () -> assertNotEquals(snapshotId, external.get().currentSnapshot().snapshotId()),
          () -> assertEquals(1, filePaths(external.get()).size()),
          () -> assertEquals(Arrays.asList(1, 2), IcebergV3TestTables.readIds(external.get())));
      return;
    }
    assertAll(
        () -> assertUnsupportedCommit(commit),
        () -> assertNotNull(external.get(), "The upgrade hook was not reached"),
        () ->
            assertEquals(
                upgradePoint.equals("first-storage-commit") ? 1 : 0,
                conflicts.get(),
                "The storage conflict must exercise the real Iceberg retry"),
        () ->
            assertEquals(
                Collections.emptyList(),
                committedVersions,
                "V3 metadata was committed; attempted versions: " + attemptedVersions),
        () ->
            IcebergV3TestTables.assertV3MetadataUnchanged(
                external.get(), externalLocation.get(), externalBytes.get()),
        () -> assertEquals(snapshotId, external.get().currentSnapshot().snapshotId()),
        () -> assertEquals(files, filePaths(external.get())),
        () -> assertEquals(Arrays.asList(1, 2), IcebergV3TestTables.readIds(external.get())));
  }

  private UnkeyedTable commitExternalChange(UnkeyedTable table, int formatVersion)
      throws Exception {
    if (formatVersion == 3) {
      return IcebergV3TestTables.upgradeToV3(table);
    }
    Table external = new HadoopTables(new Configuration()).load(table.location());
    external.updateProperties().set("test.concurrent-update", "true").commit();
    return IcebergV3TestTables.wrap(external);
  }

  private void assertCommitRejected(UnkeyedTable table, UnKeyedTableCommit commit)
      throws Exception {
    String metadataLocation = IcebergV3TestTables.metadataLocation(table);
    byte[] metadataBytes = IcebergV3TestTables.metadataBytes(table);
    Set<String> files = filePaths(table);
    long snapshotId = table.currentSnapshot().snapshotId();

    assertAll(
        () -> assertUnsupportedCommit(commit),
        () -> IcebergV3TestTables.assertV3MetadataUnchanged(table, metadataLocation, metadataBytes),
        () -> assertEquals(snapshotId, table.currentSnapshot().snapshotId()),
        () -> assertEquals(files, filePaths(table), "Optimizing replaced committed data files"),
        () -> assertEquals(Arrays.asList(1, 2), IcebergV3TestTables.readIds(table)));
  }

  private void assertUnsupportedCommit(UnKeyedTableCommit commit) {
    OptimizingCommitException failure =
        assertThrows(OptimizingCommitException.class, commit::commit);
    assertTrue(
        failure.getCause() instanceof IllegalArgumentException,
        "Reject unsupported metadata explicitly, not because the fixture failed");
    assertTrue(failure.getCause().getMessage().contains("Unsupported Iceberg format version: 3"));
  }

  @Test
  void testV2OptimizingCommitReplacesFiles() throws Exception {
    UnkeyedTable table = IcebergV3TestTables.create(temp.resolve("v2"), 2);
    String metadataLocation = IcebergV3TestTables.metadataLocation(table);
    long snapshotId = table.currentSnapshot().snapshotId();
    assertEquals(2, filePaths(table).size());

    prepareCommit(table).commit();
    table.refresh();

    assertAll(
        () -> assertNotEquals(metadataLocation, IcebergV3TestTables.metadataLocation(table)),
        () -> assertNotEquals(snapshotId, table.currentSnapshot().snapshotId()),
        () -> assertEquals(1, filePaths(table).size()),
        () -> assertEquals(Arrays.asList(1, 2), IcebergV3TestTables.readIds(table)));
  }

  @ParameterizedTest(name = "{0} on V{1}")
  @CsvSource({
    "delete-rewrite,3",
    "rewrite-and-add-delete,3",
    "delete-rewrite,2",
    "rewrite-and-add-delete,2"
  })
  void testDeleteCommitBranches(String mode, int version) throws Exception {
    UnkeyedTable initial = IcebergV3TestTables.create(temp.resolve("deletes"), 2);
    List<DataFile> inputs = IcebergV3TestTables.dataFiles(initial);
    DeleteFile oldDelete = null;
    if (mode.equals("delete-rewrite")) {
      oldDelete = equalityDelete(initial, inputs.get(0), 1);
      initial.newRowDelta().addDeletes(oldDelete).commit();
    }
    UnkeyedTable table = version == 3 ? IcebergV3TestTables.upgradeToV3(initial) : initial;
    List<Integer> expectedIds = IcebergV3TestTables.readIds(table);
    DataFile[] outputData;
    DeleteFile outputDelete;
    RewriteFilesInput input;
    if (oldDelete != null) {
      outputData = new DataFile[0];
      outputDelete = equalityDelete(table, inputs.get(0), 1);
      input =
          new RewriteFilesInput(
              new DataFile[0],
              new DataFile[0],
              new DeleteFile[0],
              new DeleteFile[] {oldDelete},
              table);
    } else {
      // A valid staged output with one redundant row removed by the new equality delete.
      outputData =
          IcebergDataTestHelpers.insert(table, IcebergV3TestTables.records(table, 1, 2, 3))
              .dataFiles();
      outputDelete = equalityDelete(table, outputData[0], 3);
      input =
          new RewriteFilesInput(
              inputs.toArray(new DataFile[0]),
              new DataFile[0],
              new DeleteFile[0],
              new DeleteFile[0],
              table);
    }
    UnKeyedTableCommit commit =
        commitFor(
            table,
            input,
            new RewriteFilesOutput(outputData, new DeleteFile[] {outputDelete}, null));
    String location = IcebergV3TestTables.metadataLocation(table);
    byte[] bytes = IcebergV3TestTables.metadataBytes(table);
    long snapshotId = table.currentSnapshot().snapshotId();
    if (version == 3) {
      assertAll(
          () -> assertUnsupportedCommit(commit),
          () -> IcebergV3TestTables.assertV3MetadataUnchanged(table, location, bytes),
          () -> assertEquals(snapshotId, table.currentSnapshot().snapshotId()),
          () -> assertEquals(expectedIds, IcebergV3TestTables.readIds(table)));
    } else {
      commit.commit();
      table.refresh();
      assertNotEquals(snapshotId, table.currentSnapshot().snapshotId());
      assertEquals(
          mode.equals("delete-rewrite") ? 2 : 1, IcebergV3TestTables.dataFiles(table).size());
      assertEquals(expectedIds, IcebergV3TestTables.readIds(table));
    }
  }

  private DeleteFile equalityDelete(UnkeyedTable table, DataFile data, int id) throws Exception {
    return FileHelpers.writeDeleteFile(
        table,
        OutputFileFactory.builderFor(table, 1, 1)
            .build()
            .newOutputFile(data.partition())
            .encryptingOutputFile(),
        data.partition(),
        IcebergV3TestTables.records(table, id),
        table.schema());
  }

  private UnKeyedTableCommit prepareCommit(UnkeyedTable table) throws Exception {
    List<DataFile> inputs = IcebergV3TestTables.dataFiles(table);
    assertEquals(2, inputs.size());
    // Prepare real output without committing it; this test targets the AMS commit boundary.
    DataFile[] outputs =
        IcebergDataTestHelpers.insert(table, IcebergV3TestTables.records(table, 1, 2)).dataFiles();
    assertEquals(1, outputs.length);
    RewriteFilesInput input =
        new RewriteFilesInput(
            inputs.toArray(new DataFile[0]),
            new DataFile[0],
            new DeleteFile[0],
            new DeleteFile[0],
            table);
    return commitFor(table, input, new RewriteFilesOutput(outputs, new DeleteFile[0], null));
  }

  @SuppressWarnings("unchecked")
  private UnKeyedTableCommit commitFor(
      UnkeyedTable table, RewriteFilesInput input, RewriteFilesOutput output) {
    RewriteStageTask task = mock(RewriteStageTask.class);
    when(task.getInput()).thenReturn(input);
    when(task.getOutput()).thenReturn(output);
    when(task.getPartition()).thenReturn("");
    TaskRuntime<RewriteStageTask> runtime = mock(TaskRuntime.class);
    when(runtime.getStatus()).thenReturn(TaskRuntime.Status.SUCCESS);
    when(runtime.getTaskDescriptor()).thenReturn(task);
    when(runtime.getProperties()).thenReturn(Collections.emptyMap());
    return new UnKeyedTableCommit(
        table.currentSnapshot().snapshotId(), table, Collections.singletonList(runtime));
  }

  private Set<String> filePaths(UnkeyedTable table) throws Exception {
    return IcebergV3TestTables.dataFiles(table).stream()
        .map(file -> file.path().toString())
        .collect(Collectors.toSet());
  }
}
