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

package org.apache.amoro.formats.paimon.optimizing.primary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.formats.paimon.PaimonCatalogFactory;
import org.apache.amoro.formats.paimon.PaimonTable;
import org.apache.amoro.formats.paimon.optimizing.plan.PaimonPrimaryKeyOptimizingPlanner;
import org.apache.amoro.optimizing.OptimizingExecutor;
import org.apache.amoro.optimizing.OptimizingPlanResult;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.amoro.table.TableIdentifier;
import org.apache.hadoop.conf.Configuration;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.data.BinaryString;
import org.apache.paimon.data.GenericRow;
import org.apache.paimon.options.CatalogOptions;
import org.apache.paimon.schema.Schema;
import org.apache.paimon.table.FileStoreTable;
import org.apache.paimon.table.Table;
import org.apache.paimon.table.sink.BatchTableCommit;
import org.apache.paimon.table.sink.BatchTableWrite;
import org.apache.paimon.table.sink.BatchWriteBuilder;
import org.apache.paimon.table.sink.CommitMessage;
import org.apache.paimon.types.DataTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DisplayName("PaimonPrimaryKeyCompactionExecutor")
class TestPaimonPrimaryKeyCompactionExecutor {

  @Test
  @DisplayName("Factory creates a primary-key compaction executor")
  void factoryCreatesExecutor() {
    PaimonPrimaryKeyCompactionExecutorFactory factory =
        new PaimonPrimaryKeyCompactionExecutorFactory();
    factory.initialize(new HashMap<>());

    OptimizingExecutor<PaimonPrimaryKeyCompactionOutput> executor =
        factory.createExecutor(new PaimonPrimaryKeyCompactionInput());

    assertNotNull(executor);
    assertTrue(executor instanceof PaimonPrimaryKeyCompactionExecutor);
  }

  @Test
  @DisplayName("execute rejects missing required input")
  void executorRejectsMissingInput() {
    IllegalStateException nullInput =
        assertThrows(
            IllegalStateException.class,
            () -> new PaimonPrimaryKeyCompactionExecutor(null).execute());
    assertTrue(nullInput.getMessage().contains("missing required fields"));

    IllegalStateException emptyInput =
        assertThrows(
            IllegalStateException.class,
            () ->
                new PaimonPrimaryKeyCompactionExecutor(new PaimonPrimaryKeyCompactionInput())
                    .execute());
    assertTrue(emptyInput.getMessage().contains("missing required fields"));
  }

  @Test
  @DisplayName("execute rejects missing commit identity")
  void executorRejectsMissingCommitIdentity(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Identifier id = createPrimaryKeyTable(catalog, "t_bad_identity", primaryKeyOptions());
    writeCommits(catalog.getTable(id), 2);
    PaimonPrimaryKeyCompactionInput valid = planMinorTasks(catalog, id).get(0).getInput();

    PaimonPrimaryKeyCompactionInput missingUser =
        copyInput(valid, valid.getUnits(), "", valid.getCommitIdentifier());
    IllegalStateException missingUserEx =
        assertThrows(
            IllegalStateException.class,
            () -> new PaimonPrimaryKeyCompactionExecutor(missingUser).execute());
    assertTrue(missingUserEx.getMessage().contains("missing commitUser"));

    PaimonPrimaryKeyCompactionInput invalidIdentifier =
        copyInput(valid, valid.getUnits(), valid.getCommitUser(), 0L);
    IllegalStateException invalidIdentifierEx =
        assertThrows(
            IllegalStateException.class,
            () -> new PaimonPrimaryKeyCompactionExecutor(invalidIdentifier).execute());
    assertTrue(invalidIdentifierEx.getMessage().contains("invalid commitIdentifier"));
  }

  @Test
  @DisplayName("MINOR executor produces commit messages without committing a snapshot")
  void minorExecutorProducesCommitMessages(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Identifier id = createPrimaryKeyTable(catalog, "t_minor_execute", primaryKeyOptions());
    writeCommits(catalog.getTable(id), 2);
    List<PaimonPrimaryKeyCompactionTask> tasks = planMinorTasks(catalog, id);
    assertFalse(tasks.isEmpty());
    PaimonPrimaryKeyCompactionTask task = tasks.get(0);
    long snapshotBefore = latestSnapshotId(catalog, id);

    PaimonPrimaryKeyCompactionOutput output =
        new PaimonPrimaryKeyCompactionExecutor(task.getInput()).execute();

    assertFalse(output.getCommitMessageBytesList().isEmpty());
    assertEquals(task.getInput().getUnits().size(), output.getCompactedBucketCount());
    assertEquals(sumFiles(task.getInput().getUnits()), output.getCompactedFileCount());
    assertEquals(sumBytes(task.getInput().getUnits()), output.getCompactedFileSize());
    assertEquals(sumRecords(task.getInput().getUnits()), output.getCompactedRecordCount());
    assertTrue(output.getProducedFileCount() > 0);
    assertTrue(output.getProducedFileSize() > 0);
    assertEquals(snapshotBefore, latestSnapshotId(catalog, id));
  }

  @Test
  @DisplayName("MAJOR executor produces commit messages with full compaction")
  void majorExecutorProducesCommitMessages(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> options = primaryKeyOptions();
    options.put("num-sorted-run.compaction-trigger", "99");
    Identifier id = createPrimaryKeyTable(catalog, "t_major_execute", options);
    writeCommits(catalog.getTable(id), 3);
    List<PaimonPrimaryKeyCompactionTask> tasks = planMajorTasks(catalog, id);
    assertFalse(tasks.isEmpty());
    PaimonPrimaryKeyCompactionTask task = tasks.get(0);
    assertTrue(task.getInput().isFullCompaction());
    long snapshotBefore = latestSnapshotId(catalog, id);

    PaimonPrimaryKeyCompactionOutput output =
        new PaimonPrimaryKeyCompactionExecutor(task.getInput()).execute();

    assertFalse(output.getCommitMessageBytesList().isEmpty());
    assertEquals(task.getInput().getUnits().size(), output.getCompactedBucketCount());
    assertTrue(output.getCompactedFileCount() >= output.getProducedFileCount());
    assertTrue(output.getCompactedFileSize() > 0);
    assertTrue(output.getProducedFileSize() > 0);
    assertEquals(snapshotBefore, latestSnapshotId(catalog, id));
  }

  private static PaimonPrimaryKeyCompactionInput copyInput(
      PaimonPrimaryKeyCompactionInput input,
      List<PaimonBucketCompactionUnit> units,
      String commitUser,
      long commitIdentifier) {
    return new PaimonPrimaryKeyCompactionInput(
        input.getTable(),
        units,
        input.getOptimizingType(),
        input.isFullCompaction(),
        input.getTargetSnapshotId(),
        commitUser,
        commitIdentifier);
  }

  private static List<PaimonPrimaryKeyCompactionTask> planMinorTasks(Catalog catalog, Identifier id)
      throws Exception {
    OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> result =
        planner(catalog, id, runtimeOptions("num-sorted-run.compaction-trigger", "2")).plan();
    assertEquals(OptimizingType.MINOR, result.getOptimizingType());
    return result.getTasks();
  }

  private static List<PaimonPrimaryKeyCompactionTask> planMajorTasks(Catalog catalog, Identifier id)
      throws Exception {
    OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> result =
        planner(
                catalog,
                id,
                runtimeOptions(
                    "num-sorted-run.compaction-trigger",
                    "2",
                    PaimonPrimaryKeyOptions.MAJOR_FILE_COUNT_THRESHOLD,
                    "3"))
            .plan();
    assertEquals(OptimizingType.MAJOR, result.getOptimizingType());
    return result.getTasks();
  }

  private static PaimonPrimaryKeyOptimizingPlanner planner(
      Catalog catalog, Identifier id, Map<String, String> runtimeOptions) throws Exception {
    PaimonTable table = wrap(catalog.getTable(id).copy(runtimeOptions), id.getObjectName());
    return new PaimonPrimaryKeyOptimizingPlanner(
        table, 100L, 7L, 4.0, 64L * 1024 * 1024, defaultConfig(), 0L, 0L, 0L, null);
  }

  private static Catalog fsCatalog(Path warehouse) {
    Map<String, String> props = new HashMap<>();
    props.put(CatalogOptions.WAREHOUSE.key(), warehouse.toUri().toString());
    return PaimonCatalogFactory.paimonCatalog(props, new Configuration());
  }

  private static Identifier createPrimaryKeyTable(
      Catalog catalog, String tableName, Map<String, String> options) throws Exception {
    catalog.createDatabase("db1", true);
    Schema.Builder builder =
        Schema.newBuilder()
            .column("id", DataTypes.INT())
            .column("name", DataTypes.STRING())
            .primaryKey("id");
    options.forEach(builder::option);
    Identifier id = Identifier.create("db1", tableName);
    catalog.createTable(id, builder.build(), true);
    return id;
  }

  private static void writeCommits(Table table, int count) throws Exception {
    for (int i = 0; i < count; i++) {
      BatchWriteBuilder builder = table.newBatchWriteBuilder();
      try (BatchTableWrite write = builder.newWrite()) {
        write.write(GenericRow.of(i, BinaryString.fromString("name-" + i)));
        List<CommitMessage> messages = write.prepareCommit();
        try (BatchTableCommit commit = builder.newCommit()) {
          commit.commit(messages);
        }
      }
    }
  }

  private static long latestSnapshotId(Catalog catalog, Identifier id) throws Exception {
    return ((FileStoreTable) catalog.getTable(id)).snapshotManager().latestSnapshot().id();
  }

  private static long sumFiles(List<PaimonBucketCompactionUnit> units) {
    return units.stream().mapToLong(PaimonBucketCompactionUnit::getFileCount).sum();
  }

  private static long sumBytes(List<PaimonBucketCompactionUnit> units) {
    return units.stream().mapToLong(PaimonBucketCompactionUnit::getFileSizeInBytes).sum();
  }

  private static long sumRecords(List<PaimonBucketCompactionUnit> units) {
    return units.stream().mapToLong(PaimonBucketCompactionUnit::getRecordCount).sum();
  }

  private static Map<String, String> primaryKeyOptions() {
    Map<String, String> options = new HashMap<>();
    options.put(PaimonPrimaryKeyOptions.ENABLED, "true");
    options.put("bucket", "1");
    return options;
  }

  private static Map<String, String> runtimeOptions(String... keyValues) {
    Map<String, String> options = new HashMap<>();
    for (int i = 0; i < keyValues.length; i += 2) {
      options.put(keyValues[i], keyValues[i + 1]);
    }
    return options;
  }

  private static PaimonTable wrap(Table table, String name) {
    return new PaimonTable(TableIdentifier.of("test_catalog", "db1", name), table);
  }

  private static OptimizingConfig defaultConfig() {
    return new OptimizingConfig()
        .setEnabled(true)
        .setMinorLeastFileCount(2)
        .setMinorLeastInterval(0)
        .setFullTriggerInterval(-1)
        .setFullRewriteAllFiles(false)
        .setMaxTaskSize(64L * 1024 * 1024);
  }
}
