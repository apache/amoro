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

package org.apache.amoro.formats.paimon.optimizing.commit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.exception.OptimizingCommitException;
import org.apache.amoro.formats.paimon.PaimonCatalogFactory;
import org.apache.amoro.formats.paimon.PaimonTable;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionExecutor;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionOutput;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionTask;
import org.apache.amoro.formats.paimon.optimizing.plan.PaimonOptimizingPlanner;
import org.apache.amoro.optimizing.OptimizingPlanResult;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.utils.SerializationUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.paimon.Snapshot;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.data.BinaryString;
import org.apache.paimon.data.GenericRow;
import org.apache.paimon.data.InternalRow;
import org.apache.paimon.io.DataFileMeta;
import org.apache.paimon.options.CatalogOptions;
import org.apache.paimon.reader.RecordReader;
import org.apache.paimon.schema.Schema;
import org.apache.paimon.table.AppendOnlyFileStoreTable;
import org.apache.paimon.table.Table;
import org.apache.paimon.table.sink.BatchTableCommit;
import org.apache.paimon.table.sink.BatchTableWrite;
import org.apache.paimon.table.sink.BatchWriteBuilder;
import org.apache.paimon.table.sink.CommitMessage;
import org.apache.paimon.table.sink.CommitMessageImpl;
import org.apache.paimon.table.sink.CommitMessageSerializer;
import org.apache.paimon.table.source.ReadBuilder;
import org.apache.paimon.types.DataTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DisplayName("PaimonTableCommit")
public class TestPaimonTableCommit {

  private static Catalog fsCatalog(Path warehouse) {
    Map<String, String> props = new HashMap<>();
    props.put(CatalogOptions.WAREHOUSE.key(), warehouse.toUri().toString());
    return PaimonCatalogFactory.paimonCatalog(props, new Configuration());
  }

  private static Table createTinyAppendTable(Catalog catalog, String tableName, int rows)
      throws Exception {
    catalog.createDatabase("db1", true);
    Schema schema =
        Schema.newBuilder()
            .column("id", DataTypes.INT())
            .column("name", DataTypes.STRING())
            .option("bucket", "-1")
            .option("target-file-size", "1 kb")
            .option("compaction.min.file-num", "2")
            .build();
    Identifier id = Identifier.create("db1", tableName);
    catalog.createTable(id, schema, true);
    Table table = catalog.getTable(id);
    for (int i = 0; i < rows; i++) {
      BatchWriteBuilder builder = table.newBatchWriteBuilder();
      try (BatchTableWrite write = builder.newWrite()) {
        write.write(GenericRow.of(i, BinaryString.fromString("r-" + i)));
        List<CommitMessage> messages = write.prepareCommit();
        try (BatchTableCommit commit = builder.newCommit()) {
          commit.commit(messages);
        }
      }
    }
    return catalog.getTable(id);
  }

  private static void appendRows(Catalog catalog, Identifier id, GenericRow... rows)
      throws Exception {
    Table table = catalog.getTable(id);
    BatchWriteBuilder builder = table.newBatchWriteBuilder();
    try (BatchTableWrite write = builder.newWrite()) {
      for (GenericRow row : rows) {
        write.write(row);
      }
      List<CommitMessage> messages = write.prepareCommit();
      try (BatchTableCommit commit = builder.newCommit()) {
        commit.commit(messages);
      }
    }
  }

  private static List<PaimonCompactionTask> planAndExecute(
      Catalog catalog, Identifier id, long tableId, long processId) throws Exception {
    PaimonTable wrapped =
        new PaimonTable(
            TableIdentifier.of("test_catalog", id.getDatabaseName(), id.getObjectName()),
            catalog.getTable(id));
    PaimonOptimizingPlanner planner =
        new PaimonOptimizingPlanner(
            wrapped,
            tableId,
            processId,
            1.0,
            64L * 1024 * 1024,
            new org.apache.amoro.config.OptimizingConfig()
                .setEnabled(true)
                .setMinorLeastInterval(1)
                .setFullTriggerInterval(-1)
                .setFullRewriteAllFiles(false)
                .setMaxTaskSize(64L * 1024 * 1024),
            0L,
            0L,
            0L,
            null);
    OptimizingPlanResult<PaimonCompactionTask> result = planner.plan();
    List<PaimonCompactionTask> tasks = new ArrayList<>(result.getTasks());
    for (PaimonCompactionTask task : tasks) {
      PaimonCompactionOutput output = new PaimonCompactionExecutor(task.getInput()).execute();
      ByteBuffer buffer = SerializationUtil.simpleSerialize(output);
      byte[] bytes = new byte[buffer.remaining()];
      buffer.get(bytes);
      task.setOutputBytes(bytes);
    }
    return tasks;
  }

  private static long countDataFiles(AppendOnlyFileStoreTable table) throws Exception {
    return table.newReadBuilder().newScan().plan().splits().stream()
        .filter(s -> s instanceof org.apache.paimon.table.source.DataSplit)
        .mapToLong(s -> ((org.apache.paimon.table.source.DataSplit) s).dataFiles().size())
        .sum();
  }

  private static List<String> readRowStrings(Table table) throws Exception {
    ReadBuilder readBuilder = table.newReadBuilder();
    List<String> rows = new ArrayList<>();
    RecordReader<InternalRow> reader =
        readBuilder.newRead().createReader(readBuilder.newScan().plan());
    reader.forEachRemaining(row -> rows.add(row.getInt(0) + "|" + row.getString(1).toString()));
    Collections.sort(rows);
    return rows;
  }

  private static void assertCompactMessageRowCountPreserved(List<PaimonCompactionTask> tasks)
      throws Exception {
    CommitMessageSerializer serializer = new CommitMessageSerializer();
    for (PaimonCompactionTask task : tasks) {
      PaimonCompactionOutput output = task.getOutput();
      CommitMessageImpl message =
          (CommitMessageImpl)
              serializer.deserialize(
                  output.getCommitMessageVersion(), output.getCommitMessageBytes());
      long beforeRows =
          message.compactIncrement().compactBefore().stream()
              .mapToLong(DataFileMeta::rowCount)
              .sum();
      long afterRows =
          message.compactIncrement().compactAfter().stream()
              .mapToLong(DataFileMeta::rowCount)
              .sum();
      assertEquals(beforeRows, afterRows, "Compact message must preserve row count");
    }
  }

  private static long commitIdentifier(List<PaimonCompactionTask> tasks) {
    return tasks.get(0).getInput().getCommitIdentifier();
  }

  private static void assertSingleCompactSnapshot(
      AppendOnlyFileStoreTable table, long snapshotBefore, long commitIdentifier) {
    Snapshot snapshot = table.snapshotManager().latestSnapshot();
    assertEquals(
        snapshotBefore + 1,
        snapshot.id(),
        "Compaction commit must create exactly one snapshot; no empty APPEND snapshot is allowed");
    assertEquals(Snapshot.CommitKind.COMPACT, snapshot.commitKind());
    assertEquals(commitIdentifier, snapshot.commitIdentifier());
  }

  @Test
  @DisplayName("Commit merges many small files into fewer, preserving row count")
  void testCommitMergesFiles(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    createTinyAppendTable(catalog, "t_merge", 5);
    Identifier id = Identifier.create("db1", "t_merge");

    List<String> rowsBefore = readRowStrings(catalog.getTable(id));
    long before = countDataFiles((AppendOnlyFileStoreTable) catalog.getTable(id));
    List<PaimonCompactionTask> tasks = planAndExecute(catalog, id, 1L, 42L);
    assertTrue(tasks.size() >= 1);
    assertCompactMessageRowCountPreserved(tasks);

    PaimonTableCommit committer =
        new PaimonTableCommit(
            (AppendOnlyFileStoreTable) catalog.getTable(id),
            tasks,
            "user-1",
            commitIdentifier(tasks));
    committer.commit();

    long after = countDataFiles((AppendOnlyFileStoreTable) catalog.getTable(id));
    List<String> rowsAfter = readRowStrings(catalog.getTable(id));
    assertSingleCompactSnapshot(
        (AppendOnlyFileStoreTable) catalog.getTable(id),
        tasks.get(0).getInput().getTargetSnapshotId(),
        commitIdentifier(tasks));
    assertTrue(after < before, "Compaction must drop file count");
    assertEquals(rowsBefore.size(), rowsAfter.size(), "Compaction must preserve row count");
    assertEquals(rowsBefore, rowsAfter, "Compaction must preserve row content");
  }

  @Test
  @DisplayName("Replay with same commit user and identifier is filtered without duplicating data")
  void testReplaySameCommitIdentifierIsIdempotent(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    createTinyAppendTable(catalog, "t_retry", 5);
    Identifier id = Identifier.create("db1", "t_retry");
    List<String> rowsBefore = readRowStrings(catalog.getTable(id));
    List<PaimonCompactionTask> tasks = planAndExecute(catalog, id, 1L, 43L);
    assertTrue(tasks.size() >= 1);
    assertCompactMessageRowCountPreserved(tasks);

    String commitUser = "user-retry";
    long commitIdentifier = commitIdentifier(tasks);
    PaimonTableCommit committer =
        new PaimonTableCommit(
            (AppendOnlyFileStoreTable) catalog.getTable(id), tasks, commitUser, commitIdentifier);
    committer.commit();

    AppendOnlyFileStoreTable afterFirstCommit = (AppendOnlyFileStoreTable) catalog.getTable(id);
    long snapshotAfterFirstCommit = afterFirstCommit.snapshotManager().latestSnapshot().id();
    assertSingleCompactSnapshot(
        afterFirstCommit, tasks.get(0).getInput().getTargetSnapshotId(), commitIdentifier);
    List<String> rowsAfterFirstCommit = readRowStrings(afterFirstCommit);
    assertEquals(rowsBefore, rowsAfterFirstCommit, "First commit must preserve row content");

    PaimonTableCommit replay =
        new PaimonTableCommit(
            (AppendOnlyFileStoreTable) catalog.getTable(id), tasks, commitUser, commitIdentifier);
    replay.commit();

    AppendOnlyFileStoreTable afterReplay = (AppendOnlyFileStoreTable) catalog.getTable(id);
    assertEquals(
        snapshotAfterFirstCommit,
        afterReplay.snapshotManager().latestSnapshot().id(),
        "Replay with the same commit identifier must not create a new snapshot");
    assertEquals(rowsBefore, readRowStrings(afterReplay), "Replay must preserve row content");
  }

  @Test
  @DisplayName("Commit after concurrent append preserves newly appended rows")
  void testCommitAfterConcurrentAppendPreservesRows(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    createTinyAppendTable(catalog, "t_concurrent_append", 5);
    Identifier id = Identifier.create("db1", "t_concurrent_append");
    List<String> rowsBeforePlan = readRowStrings(catalog.getTable(id));
    List<PaimonCompactionTask> tasks = planAndExecute(catalog, id, 1L, 50L);
    assertTrue(tasks.size() >= 1);

    appendRows(
        catalog,
        id,
        GenericRow.of(9001, BinaryString.fromString("new-a")),
        GenericRow.of(9002, BinaryString.fromString("new-b")));
    List<String> rowsAfterAppend = readRowStrings(catalog.getTable(id));

    PaimonCompactionTask first = tasks.get(0);
    new PaimonTableCommit(
            (AppendOnlyFileStoreTable) catalog.getTable(id),
            tasks,
            first.getInput().getCommitUser(),
            first.getInput().getCommitIdentifier())
        .commit();

    List<String> rowsAfterCommit = readRowStrings(catalog.getTable(id));
    assertEquals(
        rowsAfterAppend,
        rowsAfterCommit,
        "Compact commit must preserve rows appended after planning");
    assertTrue(rowsAfterCommit.containsAll(rowsBeforePlan));
    assertTrue(
        rowsAfterCommit.containsAll(Arrays.asList("9001|new-a", "9002|new-b")),
        "Rows appended after planning must remain visible after compact commit");
  }

  @Test
  @DisplayName("Commit fails when planned compact-before files were already compacted")
  void testCommitFailsWhenPlannedFilesWereAlreadyCompacted(@TempDir Path warehouse)
      throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    createTinyAppendTable(catalog, "t_stale_plan", 5);
    Identifier id = Identifier.create("db1", "t_stale_plan");
    List<PaimonCompactionTask> staleTasks = planAndExecute(catalog, id, 1L, 60L);
    List<PaimonCompactionTask> winningTasks = planAndExecute(catalog, id, 1L, 61L);
    assertTrue(staleTasks.size() >= 1);
    assertTrue(winningTasks.size() >= 1);

    PaimonCompactionTask winning = winningTasks.get(0);
    new PaimonTableCommit(
            (AppendOnlyFileStoreTable) catalog.getTable(id),
            winningTasks,
            winning.getInput().getCommitUser(),
            winning.getInput().getCommitIdentifier())
        .commit();

    PaimonCompactionTask stale = staleTasks.get(0);
    OptimizingCommitException ex =
        assertThrows(
            OptimizingCommitException.class,
            () ->
                new PaimonTableCommit(
                        (AppendOnlyFileStoreTable) catalog.getTable(id),
                        staleTasks,
                        stale.getInput().getCommitUser(),
                        stale.getInput().getCommitIdentifier())
                    .commit());
    assertTrue(ex.getMessage().contains("Paimon commit failed"));
  }

  @Test
  @DisplayName(
      "Invalid CommitMessage bytes are surfaced as OptimizingCommitException (AMS re-plans)")
  void testInvalidCommitMessageBytesSurfacesException(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    createTinyAppendTable(catalog, "t_bad_bytes", 5);
    Identifier id = Identifier.create("db1", "t_bad_bytes");
    List<PaimonCompactionTask> tasks = planAndExecute(catalog, id, 1L, 33L);

    // Corrupt the CommitMessage bytes carried by every task so the committer cannot deserialise.
    for (PaimonCompactionTask task : tasks) {
      PaimonCompactionOutput corrupted =
          new PaimonCompactionOutput(
              new byte[] {0x7F, 0x7F, 0x7F}, /* version */ 99, 0L, 0L, 0L, 0L);
      ByteBuffer buffer = SerializationUtil.simpleSerialize(corrupted);
      byte[] bytes = new byte[buffer.remaining()];
      buffer.get(bytes);
      task.setOutputBytes(bytes);
    }

    AppendOnlyFileStoreTable t = (AppendOnlyFileStoreTable) catalog.getTable(id);
    PaimonTableCommit committer = new PaimonTableCommit(t, tasks, "user", 33L);
    org.apache.amoro.exception.OptimizingCommitException ex =
        org.junit.jupiter.api.Assertions.assertThrows(
            org.apache.amoro.exception.OptimizingCommitException.class, committer::commit);
    assertTrue(
        ex.getMessage().contains("deserialize") || ex.getMessage().contains("Paimon commit"));
  }

  @Test
  @DisplayName("Empty task collection is a no-op and creates no snapshot")
  void testEmptyCommitIsNoOp(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    createTinyAppendTable(catalog, "t_noop", 3);
    Identifier id = Identifier.create("db1", "t_noop");
    AppendOnlyFileStoreTable t = (AppendOnlyFileStoreTable) catalog.getTable(id);
    long snapshotBefore = t.snapshotManager().latestSnapshot().id();

    new PaimonTableCommit(t, Collections.emptyList(), "user", 1L).commit();

    AppendOnlyFileStoreTable after = (AppendOnlyFileStoreTable) catalog.getTable(id);
    assertEquals(snapshotBefore, after.snapshotManager().latestSnapshot().id());
  }

  @Test
  @DisplayName("Non-empty commit rejects non-positive commit identifier")
  void testCommitRejectsNonPositiveCommitIdentifier(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    createTinyAppendTable(catalog, "t_bad_identifier", 5);
    Identifier id = Identifier.create("db1", "t_bad_identifier");
    List<PaimonCompactionTask> tasks = planAndExecute(catalog, id, 1L, 71L);
    assertTrue(tasks.size() >= 1);
    AppendOnlyFileStoreTable table = (AppendOnlyFileStoreTable) catalog.getTable(id);
    long snapshotBefore = table.snapshotManager().latestSnapshot().id();

    OptimizingCommitException ex =
        assertThrows(
            OptimizingCommitException.class,
            () ->
                new PaimonTableCommit(table, tasks, tasks.get(0).getInput().getCommitUser(), 0L)
                    .commit());
    assertTrue(ex.getMessage().contains("must be > 0"));
    assertEquals(
        snapshotBefore,
        ((AppendOnlyFileStoreTable) catalog.getTable(id)).snapshotManager().latestSnapshot().id(),
        "Invalid commit identifier must not create a snapshot");
  }

  @Test
  @DisplayName("Non-empty commit rejects missing commit user")
  void testCommitRejectsMissingCommitUser(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    createTinyAppendTable(catalog, "t_bad_user", 5);
    Identifier id = Identifier.create("db1", "t_bad_user");
    List<PaimonCompactionTask> tasks = planAndExecute(catalog, id, 1L, 72L);
    assertTrue(tasks.size() >= 1);
    AppendOnlyFileStoreTable table = (AppendOnlyFileStoreTable) catalog.getTable(id);
    long snapshotBefore = table.snapshotManager().latestSnapshot().id();

    OptimizingCommitException ex =
        assertThrows(
            OptimizingCommitException.class,
            () -> new PaimonTableCommit(table, tasks, "", commitIdentifier(tasks)).commit());
    assertTrue(ex.getMessage().contains("commit user must not be empty"));
    assertEquals(
        snapshotBefore,
        ((AppendOnlyFileStoreTable) catalog.getTable(id)).snapshotManager().latestSnapshot().id(),
        "Missing commit user must not create a snapshot");
  }

  @Test
  @DisplayName("Missing CommitMessage on a success task fails instead of partially committing")
  void testMissingCommitMessageFailsFast(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    createTinyAppendTable(catalog, "t_missing_output", 5);
    Identifier id = Identifier.create("db1", "t_missing_output");
    List<PaimonCompactionTask> tasks = planAndExecute(catalog, id, 1L, 70L);
    assertTrue(tasks.size() >= 1);
    AppendOnlyFileStoreTable table = (AppendOnlyFileStoreTable) catalog.getTable(id);
    long snapshotBefore = table.snapshotManager().latestSnapshot().id();

    PaimonCompactionTask missingOutput =
        new PaimonCompactionTask(1L, "missing", tasks.get(0).getInput(), new HashMap<>());
    List<PaimonCompactionTask> tasksWithMissingOutput = new ArrayList<>(tasks);
    tasksWithMissingOutput.add(missingOutput);

    OptimizingCommitException ex =
        assertThrows(
            OptimizingCommitException.class,
            () ->
                new PaimonTableCommit(
                        table,
                        tasksWithMissingOutput,
                        tasks.get(0).getInput().getCommitUser(),
                        commitIdentifier(tasks))
                    .commit());
    assertTrue(ex.getMessage().contains("has no Paimon CommitMessage"));
    assertEquals(
        snapshotBefore,
        ((AppendOnlyFileStoreTable) catalog.getTable(id)).snapshotManager().latestSnapshot().id(),
        "Commit must not create a snapshot when any success task output is missing");
  }
}
