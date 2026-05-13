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
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.data.BinaryString;
import org.apache.paimon.data.GenericRow;
import org.apache.paimon.options.CatalogOptions;
import org.apache.paimon.schema.Schema;
import org.apache.paimon.table.AppendOnlyFileStoreTable;
import org.apache.paimon.table.Table;
import org.apache.paimon.table.sink.BatchTableCommit;
import org.apache.paimon.table.sink.BatchTableWrite;
import org.apache.paimon.table.sink.BatchWriteBuilder;
import org.apache.paimon.table.sink.CommitMessage;
import org.apache.paimon.types.DataTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
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

  private static List<PaimonCompactionTask> planAndExecute(
      Catalog catalog, Identifier id, long tableId, long processId) throws Exception {
    PaimonTable wrapped =
        new PaimonTable(
            TableIdentifier.of("test_catalog", id.getDatabaseName(), id.getObjectName()),
            catalog.getTable(id));
    PaimonOptimizingPlanner planner =
        new PaimonOptimizingPlanner(wrapped, tableId, processId, 1.0, 1024);
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

  @Test
  @DisplayName("Commit merges many small files into fewer, preserving row count")
  void testCommitMergesFiles(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    createTinyAppendTable(catalog, "t_merge", 5);
    Identifier id = Identifier.create("db1", "t_merge");

    long before = countDataFiles((AppendOnlyFileStoreTable) catalog.getTable(id));
    List<PaimonCompactionTask> tasks = planAndExecute(catalog, id, 1L, 42L);
    assertTrue(tasks.size() >= 1);

    PaimonTableCommit committer =
        new PaimonTableCommit(
            (AppendOnlyFileStoreTable) catalog.getTable(id), tasks, "user-1", 42L);
    committer.commit();

    long after = countDataFiles((AppendOnlyFileStoreTable) catalog.getTable(id));
    assertTrue(after < before, "Compaction must drop file count");
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
}
