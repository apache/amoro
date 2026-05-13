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
import org.apache.paimon.options.CatalogOptions;
import org.apache.paimon.schema.Schema;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
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

  // ---- C7 applyQuota unit tests (synthetic AppendCompactTasks, stub size function) ----
  // These tests intentionally avoid spinning up a real Paimon filesystem / coordinator: they
  // exercise the quota predicate in isolation, which is all C7 is responsible for. The split
  // contract (§3.4) is: Amoro does NOT mutate / merge / split AppendCompactTask — only caps
  // count and defers oversized tasks.

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
  @DisplayName("C7: oversized task is deferred (skipped), plan returns empty under-tight quota")
  void testApplyQuotaDefersOversizedTask() {
    List<AppendCompactTask> tasks = synthesiseTasks(1);
    long maxInputSizePerThread = 64L * 1024 * 1024; // 64 MiB
    ToLongFunction<AppendCompactTask> sizeFn = fixedSize(200L * 1024 * 1024); // 200 MiB — oversized

    List<AppendCompactTask> out =
        PaimonOptimizingPlanner.applyQuota(
            tasks, 4.0, maxInputSizePerThread, sizeFn, "t_oversized");

    assertTrue(out.isEmpty(), "the only task exceeds maxInputSizePerThread and must be deferred");
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
  @DisplayName("C7: mixed sizes — oversized ones skipped, eligible ones fill up to cap")
  void testApplyQuotaMixedSizesRespectsBothFilters() {
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

    // cap=ceil(2.0)=2; oversized task1 skipped; task0 + task2 fill the cap; task3/task4 never
    // reached this tick (task3 would be released next tick; task4 deferred for size).
    assertEquals(2, out.size());
    assertTrue(out.get(0) == tasks.get(0));
    assertTrue(out.get(1) == tasks.get(2));
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
}
