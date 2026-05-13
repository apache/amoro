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

package org.apache.amoro.formats.paimon.optimizing.plan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.formats.paimon.PaimonCatalogFactory;
import org.apache.amoro.formats.paimon.PaimonTable;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionExecutorFactory;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionTask;
import org.apache.amoro.optimizing.OptimizingPlanResult;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.amoro.optimizing.TaskProperties;
import org.apache.amoro.table.TableIdentifier;
import org.apache.hadoop.conf.Configuration;
import org.apache.paimon.append.AppendCompactTask;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.data.BinaryRow;
import org.apache.paimon.data.BinaryString;
import org.apache.paimon.data.GenericRow;
import org.apache.paimon.io.DataFileMeta;
import org.apache.paimon.operation.BaseAppendFileStoreWrite;
import org.apache.paimon.options.CatalogOptions;
import org.apache.paimon.schema.Schema;
import org.apache.paimon.table.AppendOnlyFileStoreTable;
import org.apache.paimon.table.Table;
import org.apache.paimon.table.sink.AppendCompactTaskSerializer;
import org.apache.paimon.table.sink.BatchTableCommit;
import org.apache.paimon.table.sink.BatchTableWrite;
import org.apache.paimon.table.sink.BatchWriteBuilder;
import org.apache.paimon.table.sink.CommitMessage;
import org.apache.paimon.table.sink.TableCommitImpl;
import org.apache.paimon.types.DataTypes;
import org.apache.paimon.types.RowType;
import org.apache.paimon.utils.PartitionPathUtils;
import org.apache.paimon.utils.RowDataToObjectArrayConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToLongFunction;

@DisplayName("PaimonOptimizingPlanner")
public class TestPaimonOptimizingPlanner {

  private static Catalog fsCatalog(Path warehouse) {
    Map<String, String> props = new HashMap<>();
    props.put(CatalogOptions.WAREHOUSE.key(), warehouse.toUri().toString());
    return PaimonCatalogFactory.paimonCatalog(props, new Configuration());
  }

  private static void writeRecords(Table table, List<GenericRow> rowsInOneCommit) throws Exception {
    BatchWriteBuilder builder = table.newBatchWriteBuilder();
    try (BatchTableWrite write = builder.newWrite()) {
      for (GenericRow row : rowsInOneCommit) {
        write.write(row);
      }
      List<CommitMessage> messages = write.prepareCommit();
      try (BatchTableCommit commit = builder.newCommit()) {
        commit.commit(messages);
      }
    }
  }

  private static PaimonTable wrap(Table table, String name) {
    return new PaimonTable(TableIdentifier.of("test_catalog", "db1", name), table);
  }

  private static Table createAppendOnlyTable(
      Catalog catalog, String tableName, Map<String, String> extraOptions) throws Exception {
    catalog.createDatabase("db1", true);
    Schema.Builder builder =
        Schema.newBuilder()
            .column("id", DataTypes.INT())
            .column("name", DataTypes.STRING())
            .option("bucket", "-1");
    for (Map.Entry<String, String> e : extraOptions.entrySet()) {
      builder.option(e.getKey(), e.getValue());
    }
    Identifier id = Identifier.create("db1", tableName);
    catalog.createTable(id, builder.build(), true);
    return catalog.getTable(id);
  }

  private static Table createPartitionedAppendOnlyTable(
      Catalog catalog, String tableName, Map<String, String> extraOptions) throws Exception {
    catalog.createDatabase("db1", true);
    Schema.Builder builder =
        Schema.newBuilder()
            .column("id", DataTypes.INT())
            .column("name", DataTypes.STRING())
            .column("dt", DataTypes.STRING())
            .partitionKeys("dt")
            .option("bucket", "-1");
    for (Map.Entry<String, String> e : extraOptions.entrySet()) {
      builder.option(e.getKey(), e.getValue());
    }
    Identifier id = Identifier.create("db1", tableName);
    catalog.createTable(id, builder.build(), true);
    return catalog.getTable(id);
  }

  @Test
  @DisplayName("Append-only BUCKET_UNAWARE table with many small commits is picked up")
  void testUnawareTablePicksUpSmallFiles(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    // Force a tiny target-file-size so every commit produces a "small" file candidate.
    Map<String, String> opts = new HashMap<>();
    opts.put("target-file-size", "1 kb");
    opts.put("compaction.min.file-num", "2");
    Table table = createAppendOnlyTable(catalog, "t_small", opts);
    for (int i = 0; i < 5; i++) {
      writeRecords(
          table, Collections.singletonList(GenericRow.of(i, BinaryString.fromString("name-" + i))));
    }
    PaimonTable paimonTable =
        wrap(catalog.getTable(Identifier.create("db1", "t_small")), "t_small");

    // maxInputSizePerThread comfortably above any plausible per-task input size in a small unit
    // test (64 MiB); availableCore=4.0 so the 5-task supply can surface multiple tasks per tick.
    PaimonOptimizingPlanner planner =
        new PaimonOptimizingPlanner(
            paimonTable, 100L /* tableId */, 1L /* processId */, 4.0, 64L * 1024 * 1024);
    assertTrue(planner.isNecessary(), "Small-file-heavy table should need compaction");

    OptimizingPlanResult<PaimonCompactionTask> result = planner.plan();
    assertNotNull(result);
    assertFalse(result.getTasks().isEmpty(), "plan() must produce at least one task");
    assertEquals(OptimizingType.MINOR, result.getOptimizingType());
    for (PaimonCompactionTask t : result.getTasks()) {
      assertEquals(100L, t.getTableId());
      assertNotNull(t.getInput());
      assertNotNull(t.getInput().getTaskBytes());
      assertTrue(t.getInput().getTaskBytes().length > 0);
      assertEquals(
          PaimonCompactionExecutorFactory.class.getName(),
          t.getProperties().get(TaskProperties.TASK_EXECUTOR_FACTORY_IMPL));
    }
  }

  @Test
  @DisplayName("Planner keeps the Paimon 1.3.1 AppendCompactTask execution primitive")
  void testPlannerProducesOnlyPaimonAppendCompactTasks(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> opts = new HashMap<>();
    opts.put("target-file-size", "1 kb");
    opts.put("compaction.min.file-num", "2");
    Table table = createAppendOnlyTable(catalog, "t_append_task_primitive", opts);
    for (int i = 0; i < 3; i++) {
      writeRecords(
          table, Collections.singletonList(GenericRow.of(i, BinaryString.fromString("r-" + i))));
    }
    PaimonTable paimonTable =
        wrap(
            catalog.getTable(Identifier.create("db1", "t_append_task_primitive")),
            "t_append_task_primitive");

    PaimonOptimizingPlanner planner =
        new PaimonOptimizingPlanner(paimonTable, 1L, 1L, 4.0, 64L * 1024 * 1024);
    OptimizingPlanResult<PaimonCompactionTask> result = planner.plan();

    assertFalse(result.getTasks().isEmpty());
    // Paimon 1.3.1 compactUnAwareBucketTable plans AppendCompactTask via
    // AppendCompactCoordinator; Amoro may scan and quota tasks in AMS, but the executor primitive
    // must stay the same.
    for (AppendCompactTask appendTask : appendTasks(result)) {
      assertNotNull(appendTask);
      assertTrue(
          appendTask.compactBefore().size() > 1,
          "Paimon 1.3.1 append compact tasks must keep Paimon's compact-before primitive");
    }
  }

  @Test
  @DisplayName("Planner returns MAJOR when table has undersized files")
  void testMajorForUndersizedFiles(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> opts = new HashMap<>();
    opts.put("target-file-size", "1 mb");
    opts.put("compaction.small-file-ratio", "0.001");
    opts.put("compaction.min.file-num", "2");
    Table table = createAppendOnlyTable(catalog, "t_major_under", opts);
    for (int i = 0; i < 4; i++) {
      writeRecords(
          table,
          Collections.singletonList(
              GenericRow.of(i, BinaryString.fromString(valueWithLength(64 * 1024)))));
    }
    PaimonTable paimonTable =
        wrap(catalog.getTable(Identifier.create("db1", "t_major_under")), "t_major_under");

    PaimonOptimizingPlanner planner =
        new PaimonOptimizingPlanner(
            paimonTable,
            1L,
            1L,
            4.0,
            64L * 1024 * 1024,
            defaultOptimizingConfig().setFullTriggerInterval(-1),
            0L,
            0L,
            0L,
            null);

    OptimizingPlanResult<PaimonCompactionTask> result = planner.plan();

    assertEquals(OptimizingType.MAJOR, result.getOptimizingType());
    assertFalse(result.getTasks().isEmpty());
  }

  @Test
  @DisplayName("Plan result type follows tasks released after quota")
  void testPlanTypeFollowsQuotaReleasedTasks(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> opts = new HashMap<>();
    opts.put("target-file-size", "1 mb");
    opts.put("compaction.small-file-ratio", "0.001");
    opts.put("compaction.min.file-num", "2");
    Table table = createPartitionedAppendOnlyTable(catalog, "t_quota_type", opts);

    writeRecords(
        table,
        Collections.singletonList(
            GenericRow.of(1, BinaryString.fromString("small-1"), BinaryString.fromString("p1"))));
    writeRecords(
        table,
        Collections.singletonList(
            GenericRow.of(2, BinaryString.fromString("small-2"), BinaryString.fromString("p1"))));
    writeRecords(
        table,
        Collections.singletonList(
            GenericRow.of(
                3,
                BinaryString.fromString(valueWithLength(64 * 1024)),
                BinaryString.fromString("p2"))));
    writeRecords(
        table,
        Collections.singletonList(
            GenericRow.of(
                4,
                BinaryString.fromString(valueWithLength(64 * 1024)),
                BinaryString.fromString("p2"))));
    PaimonTable paimonTable =
        wrap(catalog.getTable(Identifier.create("db1", "t_quota_type")), "t_quota_type");

    PaimonOptimizingPlanner planner =
        new PaimonOptimizingPlanner(
            paimonTable,
            1L,
            1L,
            1.0,
            64L * 1024 * 1024,
            defaultOptimizingConfig().setFullTriggerInterval(-1),
            0L,
            0L,
            0L,
            null);

    OptimizingPlanResult<PaimonCompactionTask> result = planner.plan();

    assertEquals(1, result.getTasks().size());
    assertEquals(OptimizingType.MINOR, result.getOptimizingType());
    AppendOnlyFileStoreTable appendTable =
        (AppendOnlyFileStoreTable) catalog.getTable(Identifier.create("db1", "t_quota_type"));
    assertEquals(Collections.singleton("dt=p1/"), partitionPaths(result, appendTable));
  }

  @Test
  @DisplayName("Planner returns FULL when full interval is reached")
  void testFullRewriteAllFiles(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> opts = new HashMap<>();
    opts.put("target-file-size", "2 kb");
    opts.put("compaction.small-file-ratio", "0.7");
    opts.put("compaction.min.file-num", "2");
    Table table = createAppendOnlyTable(catalog, "t_full", opts);
    for (int i = 0; i < 3; i++) {
      writeRecords(
          table, Collections.singletonList(GenericRow.of(i, BinaryString.fromString("r-" + i))));
    }
    PaimonTable paimonTable = wrap(catalog.getTable(Identifier.create("db1", "t_full")), "t_full");

    PaimonOptimizingPlanner planner =
        new PaimonOptimizingPlanner(
            paimonTable,
            1L,
            1L,
            4.0,
            64L * 1024 * 1024,
            defaultOptimizingConfig().setFullTriggerInterval(1).setFullRewriteAllFiles(true),
            0L,
            0L,
            0L,
            null);

    OptimizingPlanResult<PaimonCompactionTask> result = planner.plan();

    assertEquals(OptimizingType.FULL, result.getOptimizingType());
    assertFalse(result.getTasks().isEmpty());
  }

  @Test
  @DisplayName("FULL rewrite-all plans only partitions with problem files")
  void testFullRewriteAllFilesOnlyPlansProblemPartitions(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> opts = new HashMap<>();
    opts.put("target-file-size", "1 kb");
    opts.put("compaction.small-file-ratio", "0.7");
    opts.put("compaction.min.file-num", "2");
    Table table = createPartitionedAppendOnlyTable(catalog, "t_full_partitions", opts);
    writeRecords(
        table,
        Collections.singletonList(
            GenericRow.of(
                1, BinaryString.fromString("small-1"), BinaryString.fromString("problem"))));
    writeRecords(
        table,
        Collections.singletonList(
            GenericRow.of(
                2, BinaryString.fromString("small-2"), BinaryString.fromString("problem"))));
    writeRecords(
        table,
        Collections.singletonList(
            GenericRow.of(
                3,
                BinaryString.fromString(valueWithLength(128 * 1024)),
                BinaryString.fromString("healthy"))));
    writeRecords(
        table,
        Collections.singletonList(
            GenericRow.of(
                4,
                BinaryString.fromString(valueWithLength(128 * 1024)),
                BinaryString.fromString("healthy"))));
    Identifier id = Identifier.create("db1", "t_full_partitions");
    PaimonTable paimonTable = wrap(catalog.getTable(id), "t_full_partitions");

    PaimonOptimizingPlanner planner =
        new PaimonOptimizingPlanner(
            paimonTable,
            1L,
            1L,
            4.0,
            64L * 1024 * 1024,
            defaultOptimizingConfig().setFullTriggerInterval(1).setFullRewriteAllFiles(true),
            0L,
            0L,
            0L,
            null);

    OptimizingPlanResult<PaimonCompactionTask> result = planner.plan();

    assertEquals(OptimizingType.FULL, result.getOptimizingType());
    AppendOnlyFileStoreTable appendTable = (AppendOnlyFileStoreTable) catalog.getTable(id);
    assertEquals(Collections.singleton("dt=problem/"), partitionPaths(result, appendTable));
  }

  @Test
  @DisplayName("FULL rewrite-all keeps healthy large files in final packed task")
  void testFullRewriteAllFilesSurvivesTaskPacking(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> opts = new HashMap<>();
    opts.put("target-file-size", "1 kb");
    opts.put("compaction.small-file-ratio", "0.7");
    opts.put("compaction.min.file-num", "2");
    Table table = createAppendOnlyTable(catalog, "t_full_large", opts);
    writeRecords(table, Collections.singletonList(GenericRow.of(1, BinaryString.fromString("s"))));
    writeRecords(
        table,
        Collections.singletonList(
            GenericRow.of(2, BinaryString.fromString(valueWithLength(128 * 1024)))));
    PaimonTable paimonTable =
        wrap(catalog.getTable(Identifier.create("db1", "t_full_large")), "t_full_large");

    PaimonOptimizingPlanner planner =
        new PaimonOptimizingPlanner(
            paimonTable,
            1L,
            1L,
            4.0,
            512L,
            defaultOptimizingConfig()
                .setFullTriggerInterval(1)
                .setFullRewriteAllFiles(true)
                .setMaxTaskSize(512L),
            0L,
            0L,
            0L,
            null);

    OptimizingPlanResult<PaimonCompactionTask> result = planner.plan();

    assertEquals(OptimizingType.FULL, result.getOptimizingType());
    List<DataFileMeta> compactBefore = compactBeforeFiles(result);
    assertEquals(2, compactBefore.size());
    assertTrue(
        compactBefore.stream().anyMatch(file -> file.fileSize() >= 1024L),
        "rewrite-all-files=true must keep the healthy large file after task packing");
  }

  @Test
  @DisplayName("Planner does not replan compact-before files deleted by previous compact")
  void testPlannerDoesNotReplanFilesDeletedByPreviousCompact(@TempDir Path warehouse)
      throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> opts = new HashMap<>();
    opts.put("target-file-size", "1 kb");
    opts.put("compaction.min.file-num", "2");
    Table table = createAppendOnlyTable(catalog, "t_replan_compacted", opts);
    for (int i = 0; i < 5; i++) {
      writeRecords(
          table, Collections.singletonList(GenericRow.of(i, BinaryString.fromString("r-" + i))));
    }
    Identifier id = Identifier.create("db1", "t_replan_compacted");

    OptimizingPlanResult<PaimonCompactionTask> firstPlan =
        new PaimonOptimizingPlanner(
                wrap(catalog.getTable(id), "t_replan_compacted"), 1L, 1L, 4.0, 64L * 1024 * 1024)
            .plan();
    List<AppendCompactTask> firstTasks = appendTasks(firstPlan);
    Set<String> firstCompactBeforeNames = fileNames(compactBeforeFiles(firstPlan));
    assertTrue(firstCompactBeforeNames.size() > 1);

    compactAppendTasks(
        (AppendOnlyFileStoreTable) catalog.getTable(id), firstTasks, "test-planner-compact");
    writeRecords(
        catalog.getTable(id),
        Collections.singletonList(GenericRow.of(100, BinaryString.fromString("new-1"))));
    writeRecords(
        catalog.getTable(id),
        Collections.singletonList(GenericRow.of(101, BinaryString.fromString("new-2"))));

    OptimizingPlanResult<PaimonCompactionTask> secondPlan =
        new PaimonOptimizingPlanner(
                wrap(catalog.getTable(id), "t_replan_compacted"), 1L, 2L, 4.0, 64L * 1024 * 1024)
            .plan();
    Set<String> secondCompactBeforeNames = fileNames(compactBeforeFiles(secondPlan));

    assertFalse(secondCompactBeforeNames.isEmpty());
    assertTrue(
        Collections.disjoint(firstCompactBeforeNames, secondCompactBeforeNames),
        "Planner must not serialize compact-before files deleted by an earlier COMPACT snapshot");
  }

  @Test
  @DisplayName("Planner target snapshot is bound to the scan used by isNecessary")
  void testTargetSnapshotBoundToNecessaryScan(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> opts = new HashMap<>();
    opts.put("target-file-size", "1 kb");
    opts.put("compaction.min.file-num", "2");
    Table table = createAppendOnlyTable(catalog, "t_snapshot", opts);
    for (int i = 0; i < 3; i++) {
      writeRecords(
          table, Collections.singletonList(GenericRow.of(i, BinaryString.fromString("r-" + i))));
    }
    Identifier id = Identifier.create("db1", "t_snapshot");
    PaimonTable paimonTable = wrap(catalog.getTable(id), "t_snapshot");
    PaimonOptimizingPlanner planner =
        new PaimonOptimizingPlanner(paimonTable, 1L, 1L, 4.0, 64L * 1024 * 1024);

    assertTrue(planner.isNecessary());
    long scannedSnapshotId =
        ((AppendOnlyFileStoreTable) catalog.getTable(id)).snapshotManager().latestSnapshot().id();
    writeRecords(
        table, Collections.singletonList(GenericRow.of(99, BinaryString.fromString("new-row"))));
    long latestSnapshotId =
        ((AppendOnlyFileStoreTable) catalog.getTable(id)).snapshotManager().latestSnapshot().id();

    OptimizingPlanResult<PaimonCompactionTask> result = planner.plan();

    assertTrue(latestSnapshotId > scannedSnapshotId);
    assertEquals(scannedSnapshotId, result.getTargetSnapshotId());
  }

  @Test
  @DisplayName("Primary-key (HASH_FIXED) table is skipped without throwing")
  void testPrimaryKeyTableSkipped(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    catalog.createDatabase("db1", true);
    Schema schema =
        Schema.newBuilder()
            .column("id", DataTypes.INT())
            .column("name", DataTypes.STRING())
            .primaryKey("id")
            .option("bucket", "2")
            .build();
    Identifier id = Identifier.create("db1", "t_pk");
    catalog.createTable(id, schema, true);
    PaimonTable paimonTable = wrap(catalog.getTable(id), "t_pk");

    PaimonOptimizingPlanner planner =
        new PaimonOptimizingPlanner(paimonTable, 1L, 1L, 1.0, 64L * 1024 * 1024);
    assertFalse(planner.isNecessary());
    OptimizingPlanResult<PaimonCompactionTask> result = planner.plan();
    assertNotNull(result);
    assertTrue(result.getTasks().isEmpty());
    assertEquals(OptimizingType.MINOR, result.getOptimizingType());
    assertNull(planner.getCommitUser());
  }

  @Test
  @DisplayName("Fixed-bucket append-only table is skipped without throwing")
  void testFixedBucketAppendOnlyTableSkipped(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    catalog.createDatabase("db1", true);
    Schema schema =
        Schema.newBuilder()
            .column("id", DataTypes.INT())
            .column("name", DataTypes.STRING())
            .option("bucket", "2")
            .option("bucket-key", "id")
            .build();
    Identifier id = Identifier.create("db1", "t_fixed_bucket");
    catalog.createTable(id, schema, true);
    PaimonTable paimonTable = wrap(catalog.getTable(id), "t_fixed_bucket");

    PaimonOptimizingPlanner planner =
        new PaimonOptimizingPlanner(paimonTable, 1L, 1L, 1.0, 64L * 1024 * 1024);
    assertFalse(planner.isNecessary());
    OptimizingPlanResult<PaimonCompactionTask> result = planner.plan();
    assertNotNull(result);
    assertTrue(result.getTasks().isEmpty());
    assertEquals(OptimizingType.MINOR, result.getOptimizingType());
    assertNull(planner.getCommitUser());
  }

  @Test
  @DisplayName("Empty append table (no snapshot) is skipped")
  void testEmptyTableSkipped(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Table table = createAppendOnlyTable(catalog, "t_empty", new HashMap<>());
    PaimonTable paimonTable = wrap(table, "t_empty");

    PaimonOptimizingPlanner planner =
        new PaimonOptimizingPlanner(paimonTable, 1L, 1L, 1.0, 64L * 1024 * 1024);
    assertFalse(planner.isNecessary());
    OptimizingPlanResult<PaimonCompactionTask> result = planner.plan();
    assertTrue(result.getTasks().isEmpty());
  }

  @Test
  @DisplayName("Plan metadata (processId/planTime/targetSnapshotId/commitUser) is populated")
  void testPlanResultMetadata(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> opts = new HashMap<>();
    opts.put("target-file-size", "1 kb");
    opts.put("compaction.min.file-num", "2");
    Table table = createAppendOnlyTable(catalog, "t_meta", opts);
    for (int i = 0; i < 3; i++) {
      writeRecords(
          table, Collections.singletonList(GenericRow.of(i, BinaryString.fromString("r-" + i))));
    }
    PaimonTable paimonTable = wrap(catalog.getTable(Identifier.create("db1", "t_meta")), "t_meta");

    long before = System.currentTimeMillis();
    PaimonOptimizingPlanner planner =
        new PaimonOptimizingPlanner(paimonTable, 77L, 9L, 2.0, 64L * 1024 * 1024);
    OptimizingPlanResult<PaimonCompactionTask> result = planner.plan();
    long after = System.currentTimeMillis();

    assertEquals(9L, result.getProcessId());
    assertEquals(9L, planner.getProcessId());
    assertTrue(result.getPlanTime() >= before && result.getPlanTime() <= after);
    assertTrue(result.getTargetSnapshotId() >= 1L);
    assertEquals(-1L, result.getTargetChangeSnapshotId());
    assertNotNull(planner.getCommitUser());
    assertFalse(planner.getCommitUser().isEmpty());
    assertFalse(result.getTasks().isEmpty());
    for (PaimonCompactionTask task : result.getTasks()) {
      assertEquals(9L, task.getInput().getCommitIdentifier());
      assertEquals(result.getTargetSnapshotId(), task.getInput().getTargetSnapshotId());
    }
  }

  @Test
  @DisplayName("commitUser stays stable across repeated plan() calls for the same Planner instance")
  void testCommitUserStable(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Map<String, String> opts = new HashMap<>();
    opts.put("target-file-size", "1 kb");
    opts.put("compaction.min.file-num", "2");
    Table table = createAppendOnlyTable(catalog, "t_user", opts);
    for (int i = 0; i < 3; i++) {
      writeRecords(
          table, Collections.singletonList(GenericRow.of(i, BinaryString.fromString("r-" + i))));
    }
    PaimonTable paimonTable = wrap(catalog.getTable(Identifier.create("db1", "t_user")), "t_user");

    PaimonOptimizingPlanner planner =
        new PaimonOptimizingPlanner(paimonTable, 1L, 1L, 1.0, 64L * 1024 * 1024);
    OptimizingPlanResult<PaimonCompactionTask> r1 = planner.plan();
    OptimizingPlanResult<PaimonCompactionTask> r2 = planner.plan();
    assertFalse(r1.getTasks().isEmpty());
    assertFalse(r2.getTasks().isEmpty());
    String commitUser = planner.getCommitUser();
    for (PaimonCompactionTask t : r1.getTasks()) {
      assertEquals(commitUser, t.getInput().getCommitUser());
    }
    for (PaimonCompactionTask t : r2.getTasks()) {
      assertEquals(commitUser, t.getInput().getCommitUser());
    }
  }

  // ---- applyQuota unit tests (synthetic AppendCompactTasks, stub size function) ----
  // Packer owns best-effort input-size splitting. applyQuota only caps how many already-packed
  // atomic Paimon tasks are released in one planning tick.

  private static List<AppendCompactTask> synthesiseTasks(int n) {
    List<AppendCompactTask> list = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      // BinaryRow.EMPTY_ROW is a shared sentinel — good enough for an unaware / unpartitioned
      // table's partition field. compactBefore is empty because the size function below is
      // looked up by task identity, not by iterating DataFileMeta (keeps the test hermetic).
      list.add(new AppendCompactTask(BinaryRow.EMPTY_ROW, Collections.emptyList()));
    }
    return list;
  }

  private static ToLongFunction<AppendCompactTask> fixedSize(long size) {
    return task -> size;
  }

  private static List<DataFileMeta> compactBeforeFiles(
      OptimizingPlanResult<PaimonCompactionTask> result) throws Exception {
    List<DataFileMeta> files = new ArrayList<>();
    for (AppendCompactTask appendTask : appendTasks(result)) {
      files.addAll(appendTask.compactBefore());
    }
    return files;
  }

  private static Set<String> fileNames(List<DataFileMeta> files) {
    Set<String> names = new HashSet<>();
    for (DataFileMeta file : files) {
      names.add(file.fileName());
    }
    return names;
  }

  private static void compactAppendTasks(
      AppendOnlyFileStoreTable table, List<AppendCompactTask> tasks, String commitUser)
      throws Exception {
    List<CommitMessage> messages = new ArrayList<>();
    for (AppendCompactTask task : tasks) {
      BaseAppendFileStoreWrite write = table.store().newWrite(commitUser);
      try {
        messages.add(task.doCompact(table, write));
      } finally {
        write.close();
      }
    }
    try (TableCommitImpl commit = table.newCommit(commitUser)) {
      commit.commit(messages);
    }
  }

  private static List<AppendCompactTask> appendTasks(
      OptimizingPlanResult<PaimonCompactionTask> result) throws Exception {
    AppendCompactTaskSerializer serializer = new AppendCompactTaskSerializer();
    List<AppendCompactTask> appendTasks = new ArrayList<>();
    for (PaimonCompactionTask task : result.getTasks()) {
      appendTasks.add(
          serializer.deserialize(
              task.getInput().getSerializerVersion(), task.getInput().getTaskBytes()));
    }
    return appendTasks;
  }

  private static Set<String> partitionPaths(
      OptimizingPlanResult<PaimonCompactionTask> result, AppendOnlyFileStoreTable table)
      throws Exception {
    Set<String> paths = new HashSet<>();
    for (AppendCompactTask appendTask : appendTasks(result)) {
      paths.add(partitionPath(appendTask, table));
    }
    return paths;
  }

  private static String partitionPath(
      AppendCompactTask appendTask, AppendOnlyFileStoreTable table) {
    RowType partitionType = table.schema().logicalPartitionType();
    if (partitionType.getFieldCount() == 0) {
      return "";
    }
    Object[] values =
        new RowDataToObjectArrayConverter(partitionType).convert(appendTask.partition());
    Map<String, String> partitionSpec = new LinkedHashMap<>();
    List<String> fieldNames = partitionType.getFieldNames();
    for (int i = 0; i < fieldNames.size(); i++) {
      if (values[i] != null) {
        partitionSpec.put(fieldNames.get(i), values[i].toString());
      }
    }
    return PartitionPathUtils.generatePartitionPath(partitionSpec, partitionType, false);
  }

  @Test
  @DisplayName("C7: large supply — cap to ceil(availableCore); remainder deferred to next tick")
  void testApplyQuotaCapsCountToAvailableCore() {
    List<AppendCompactTask> tasks = synthesiseTasks(5);
    long maxInputSizePerThread = 10L * 1024 * 1024 * 1024; // 10 GiB — far above any task size
    ToLongFunction<AppendCompactTask> sizeFn = fixedSize(1024L); // 1 KiB each — well within limit

    List<AppendCompactTask> out =
        PaimonOptimizingPlanner.applyQuota(tasks, 2.0, maxInputSizePerThread, sizeFn, "t_large");

    assertEquals(2, out.size(), "cap=ceil(2.0)=2, remainder (3) defers to next tick");
    // Order-preserving: first two tasks are released.
    assertTrue(out.get(0) == tasks.get(0));
    assertTrue(out.get(1) == tasks.get(1));
  }

  @Test
  @DisplayName("applyQuota releases oversized atomic tasks after packer best effort")
  void testApplyQuotaDoesNotDropOversizedAtomicTask() {
    List<AppendCompactTask> tasks = synthesiseTasks(1);
    long maxInputSizePerThread = 64L * 1024 * 1024; // 64 MiB
    ToLongFunction<AppendCompactTask> sizeFn = fixedSize(200L * 1024 * 1024); // 200 MiB — oversized

    List<AppendCompactTask> out =
        PaimonOptimizingPlanner.applyQuota(
            tasks, 4.0, maxInputSizePerThread, sizeFn, "t_oversized");

    assertEquals(1, out.size());
    assertTrue(out.get(0) == tasks.get(0));
  }

  @Test
  @DisplayName("C7: supply < demand — all eligible tasks released, no artificial cap")
  void testApplyQuotaUnderQuotaReturnsAll() {
    List<AppendCompactTask> tasks = synthesiseTasks(3);
    long maxInputSizePerThread = 1024L * 1024; // 1 MiB
    ToLongFunction<AppendCompactTask> sizeFn = fixedSize(512L); // 512 B each — well under limit

    List<AppendCompactTask> out =
        PaimonOptimizingPlanner.applyQuota(
            tasks, 4.0, maxInputSizePerThread, sizeFn, "t_small_supply");

    assertEquals(3, out.size(), "availableCore=4 but only 3 supplied; no padding / no truncation");
    for (int i = 0; i < 3; i++) {
      assertTrue(out.get(i) == tasks.get(i));
    }
  }

  @Test
  @DisplayName("applyQuota preserves order and does not skip oversized tasks")
  void testApplyQuotaPreservesOrderWithMixedSizes() {
    List<AppendCompactTask> tasks = synthesiseTasks(5);
    long maxInputSizePerThread = 100L;
    // task0: ok, task1: oversized, task2: ok, task3: ok, task4: oversized
    Map<AppendCompactTask, Long> sizes = new IdentityHashMap<>();
    sizes.put(tasks.get(0), 50L);
    sizes.put(tasks.get(1), 500L);
    sizes.put(tasks.get(2), 50L);
    sizes.put(tasks.get(3), 50L);
    sizes.put(tasks.get(4), 500L);

    List<AppendCompactTask> out =
        PaimonOptimizingPlanner.applyQuota(
            tasks, 2.0, maxInputSizePerThread, sizes::get, "t_mixed");

    assertEquals(2, out.size());
    assertTrue(out.get(0) == tasks.get(0));
    assertTrue(out.get(1) == tasks.get(1));
  }

  @Test
  @DisplayName("C7: empty / null input yields empty result")
  void testApplyQuotaEmptyInput() {
    assertTrue(
        PaimonOptimizingPlanner.applyQuota(
                Collections.emptyList(), 1.0, 1024, fixedSize(0L), "t_empty")
            .isEmpty());
    assertTrue(
        PaimonOptimizingPlanner.applyQuota(null, 1.0, 1024, fixedSize(0L), "t_null").isEmpty());
  }

  private static OptimizingConfig defaultOptimizingConfig() {
    return new OptimizingConfig()
        .setEnabled(true)
        .setMinorLeastInterval(3600000)
        .setFullTriggerInterval(-1)
        .setFullRewriteAllFiles(false)
        .setMaxTaskSize(64L * 1024 * 1024);
  }

  private static String valueWithLength(int length) {
    StringBuilder builder = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      int value = i * 1103515245 + 12345;
      builder.append((char) (33 + Math.floorMod(value >>> 16, 94)));
    }
    return builder.toString();
  }
}
