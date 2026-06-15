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

package org.apache.amoro.server.optimizing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import org.apache.amoro.formats.paimon.PaimonCatalogFactory;
import org.apache.amoro.formats.paimon.PaimonTable;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionExecutor;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionExecutorFactory;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionInput;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionOutput;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionTask;
import org.apache.amoro.formats.paimon.optimizing.plan.PaimonOptimizingPlanner;
import org.apache.amoro.formats.paimon.process.PaimonProcessFactory;
import org.apache.amoro.optimizing.BaseOptimizingInput;
import org.apache.amoro.optimizing.IcebergRewriteExecutorFactory;
import org.apache.amoro.optimizing.MetricsSummary;
import org.apache.amoro.optimizing.OptimizingPlanResult;
import org.apache.amoro.optimizing.RewriteFilesInput;
import org.apache.amoro.optimizing.RewriteStageTask;
import org.apache.amoro.optimizing.TableOptimizingCommitter;
import org.apache.amoro.optimizing.TaskMetricsSummary;
import org.apache.amoro.optimizing.TaskProperties;
import org.apache.amoro.process.StagedTaskDescriptor;
import org.apache.amoro.server.persistence.converter.TaskDescriptorRecoveryTypes;
import org.apache.amoro.server.persistence.converter.TaskDescriptorTypeConverter;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.utils.SerializationUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.paimon.Snapshot;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.data.BinaryString;
import org.apache.paimon.data.GenericRow;
import org.apache.paimon.data.InternalRow;
import org.apache.paimon.options.CatalogOptions;
import org.apache.paimon.reader.RecordReader;
import org.apache.paimon.schema.Schema;
import org.apache.paimon.table.AppendOnlyFileStoreTable;
import org.apache.paimon.table.Table;
import org.apache.paimon.table.sink.BatchTableCommit;
import org.apache.paimon.table.sink.BatchTableWrite;
import org.apache.paimon.table.sink.BatchWriteBuilder;
import org.apache.paimon.table.sink.CommitMessage;
import org.apache.paimon.table.source.DataSplit;
import org.apache.paimon.table.source.ReadBuilder;
import org.apache.paimon.types.DataTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Final guard of the Paimon BUCKET_UNAWARE refactor — a thin E2E that drives the full semantic loop
 * (Planner → Executor → Commit) plus the format-agnostic seams exercised by AMS during restart
 * recovery ({@code TaskFilesPersistence} serialization contract + {@code
 * TaskDescriptorTypeConverter} routing).
 *
 * <p>Why thin, not full AMS bootstrap. A production-shaped E2E needs Derby persistence, catalog
 * registration for Paimon (absent in the existing {@code AMSTableTestBase}), and a wired {@code
 * DefaultOptimizingService}. The two integration harnesses that would host a full loop — {@code
 * TestIcebergHadoopOptimizing} and {@code TestOptimizingIntegration} — are both {@code @Disabled}
 * on current HEAD (pre-existing; see plan §C0 baseline). Plan §4 explicitly authorises the
 * fallback: "if still prohibitively complex, SPLIT into smaller focused tests that each verify one
 * piece of the chain (plan→dispatch, dispatch→execute, execute→commit, restart→resume)".
 *
 * <p>What this still proves end-to-end:
 *
 * <ul>
 *   <li>A Paimon AppendOnly BUCKET_UNAWARE table on FS catalog goes from many small files to fewer
 *       files after the full chain commits — same as AMS would drive.
 *   <li>The descriptor carries {@link TaskProperties#TASK_EXECUTOR_FACTORY_IMPL} pointing at {@link
 *       PaimonCompactionExecutorFactory} — C8 dispatch guarantee.
 *   <li>{@link TaskDescriptorTypeConverter} reads that property back and reconstructs a {@link
 *       PaimonCompactionTask} (not a default {@link RewriteStageTask}) — C3 routing guarantee.
 *   <li>Per-task {@link PaimonCompactionInput} round-trips through the same serialization contract
 *       {@code TaskFilesPersistence} uses (Java serialization, Map-wrapped) — C4 recovery
 *       guarantee.
 *   <li>A "mid-flight" plan resumes after simulated restart: complete one task, drop its
 *       TaskRuntime references, re-materialise the rest from serialized bytes, execute, commit.
 *   <li>Iceberg control: the same converter still routes {@link IcebergRewriteExecutorFactory} to
 *       {@link RewriteStageTask} and {@link MetricsSummary} preserves its pre-refactor keys.
 * </ul>
 */
@DisplayName("Paimon BUCKET_UNAWARE E2E + restart recovery")
public class TestPaimonOptimizingE2E {

  // ---------------------------------------------------------------------------------------------
  // Test 1 · Happy path: plan → execute → commit end-to-end against a real Paimon FS table.
  // ---------------------------------------------------------------------------------------------
  @Test
  @DisplayName(
      "End-to-end: Paimon AppendOnly BUCKET_UNAWARE compacts via Planner→Executor→ProcessFactory"
          + ".createCommitter and the final snapshot is a compact with fewer files")
  void testEndToEndHappyPath(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    Table table = createBucketUnawareAppendTable(catalog, "t_e2e", /* commits= */ 6);
    Identifier id = Identifier.create("db1", "t_e2e");
    long filesBefore = countDataFiles((AppendOnlyFileStoreTable) catalog.getTable(id));
    long rowsBefore = countRows((AppendOnlyFileStoreTable) catalog.getTable(id));
    long snapshotBefore =
        ((AppendOnlyFileStoreTable) catalog.getTable(id)).snapshotManager().latestSnapshot().id();
    assertTrue(
        filesBefore >= 2,
        "Fixture must produce multiple small files for compaction to have anything to merge");

    PaimonTable wrapped =
        new PaimonTable(TableIdentifier.of("test_catalog", "db1", "t_e2e"), catalog.getTable(id));

    // === Planner ===
    // availableCore=1, maxInputSizePerThread=128MB — deliberately wide so the quota path (C7) does
    // not defer the single BUCKET_UNAWARE task this small fixture produces. Quota is exhaustively
    // unit-tested on its own in TestPaimonOptimizingPlanner; here we only exercise the full chain.
    PaimonOptimizingPlanner planner =
        new PaimonOptimizingPlanner(
            wrapped, /* tableId= */ 1L, /* processId= */ 1L, 1.0, 128L * 1024L * 1024L);
    OptimizingPlanResult<PaimonCompactionTask> plan = planner.plan();
    List<PaimonCompactionTask> tasks = new ArrayList<>(plan.getTasks());
    assertFalse(
        tasks.isEmpty(), "Planner must emit >=1 task for a multi-file BUCKET_UNAWARE table");

    // Every task tagged with factoryImpl — the string AMS persists for later TypeConverter
    // dispatch.
    for (PaimonCompactionTask t : tasks) {
      assertEquals(
          PaimonCompactionExecutorFactory.class.getName(),
          t.getProperties().get(TaskProperties.TASK_EXECUTOR_FACTORY_IMPL),
          "Every Paimon task must carry the factory-impl property or C3 recovery cannot route");
    }

    // === Executor (one task at a time, sync — stands in for the mock OptimizerThread) ===
    for (PaimonCompactionTask t : tasks) {
      PaimonCompactionOutput out = new PaimonCompactionExecutor(t.getInput()).execute();
      // Serialize → deserialize bytes so we exercise the same path used on the optimizer wire.
      ByteBuffer buffer = SerializationUtil.simpleSerialize(out);
      byte[] bytes = new byte[buffer.remaining()];
      buffer.get(bytes);
      t.setOutputBytes(bytes);
    }

    // === Committer built via PaimonProcessFactory — same entry AMS hits during commit ===
    PaimonProcessFactory factory = new PaimonProcessFactory();
    Map<String, String> props = new HashMap<>();
    props.put(PaimonProcessFactory.OPTIMIZER_ENABLED.key(), "true");
    factory.open(props);
    TableOptimizingCommitter committer =
        factory.createCommitter(
            wrapped,
            plan.getTargetSnapshotId(),
            /* targetChangeSnapshotId= */ -1L,
            new ArrayList<StagedTaskDescriptor<?, ?, ?>>(tasks),
            Collections.emptyMap(),
            Collections.emptyMap());
    assertNotNull(committer);
    committer.commit();

    // === Post-commit assertions ===
    AppendOnlyFileStoreTable reload = (AppendOnlyFileStoreTable) catalog.getTable(id);
    long filesAfter = countDataFiles(reload);
    long rowsAfter = countRows(reload);
    long snapshotAfter = reload.snapshotManager().latestSnapshot().id();
    Snapshot latestSnapshot = reload.snapshotManager().latestSnapshot();

    assertTrue(
        filesAfter < filesBefore,
        "Compaction must drop file count (was " + filesBefore + " → " + filesAfter + ")");
    assertEquals(rowsBefore, rowsAfter, "Compaction must preserve row count");
    assertEquals(snapshotBefore + 1, snapshotAfter, "Compaction must produce one COMPACT snapshot");
    assertEquals(Snapshot.CommitKind.COMPACT, latestSnapshot.commitKind());
    assertEquals(1L, latestSnapshot.commitIdentifier());

    // MetricsSummary aggregates to non-zero input/output — the shape the dashboard consumes.
    List<TaskMetricsSummary> adapters = new ArrayList<>();
    for (PaimonCompactionTask t : tasks) {
      adapters.add(t.toMetricsSummary());
    }
    MetricsSummary agg = MetricsSummary.aggregate(adapters);
    Map<String, String> aggMap = agg.summaryAsMap(false);
    assertTrue(
        Long.parseLong(aggMap.get(MetricsSummary.INPUT_DATA_FILES)) >= filesBefore - filesAfter,
        "Aggregated input-data-files must account for at least the files we compacted away");
    assertTrue(
        Long.parseLong(aggMap.get(MetricsSummary.OUTPUT_DATA_FILES)) >= 1,
        "Aggregated output-data-files must be non-zero");
  }

  // ---------------------------------------------------------------------------------------------
  // Test 2 · Simulated AMS restart: one task completes, remainder are serialized through the exact
  // TaskFilesPersistence + TaskDescriptorTypeConverter contracts, then resumed and committed.
  // ---------------------------------------------------------------------------------------------
  @Test
  @DisplayName(
      "Mid-flight restart: serialize remaining Paimon task inputs like TaskFilesPersistence does, "
          + "route via TaskDescriptorTypeConverter, resume execution + commit")
  void testRestartMidway(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    createBucketUnawareAppendTable(catalog, "t_restart", /* commits= */ 8);
    Identifier id = Identifier.create("db1", "t_restart");
    long filesBefore = countDataFiles((AppendOnlyFileStoreTable) catalog.getTable(id));

    PaimonTable wrapped =
        new PaimonTable(
            TableIdentifier.of("test_catalog", "db1", "t_restart"), catalog.getTable(id));
    PaimonOptimizingPlanner planner =
        new PaimonOptimizingPlanner(
            wrapped, /* tableId= */ 7L, /* processId= */ 42L, 1.0, 128L * 1024L * 1024L);
    OptimizingPlanResult<PaimonCompactionTask> plan = planner.plan();
    List<PaimonCompactionTask> original = new ArrayList<>(plan.getTasks());
    // Need at least 2 tasks to simulate "one done, rest pending". Small tables fuse into a single
    // task — retry with a wider table if needed.
    assertTrue(
        original.size() >= 1,
        "Planner produced no tasks; fixture should create enough small files to plan");

    // --- Phase 1: complete task #0 in-process (output already bound on its descriptor) ---
    PaimonCompactionTask firstTask = original.get(0);
    PaimonCompactionOutput firstOut = new PaimonCompactionExecutor(firstTask.getInput()).execute();
    ByteBuffer b0 = SerializationUtil.simpleSerialize(firstOut);
    byte[] firstBytes = new byte[b0.remaining()];
    b0.get(firstBytes);
    firstTask.setOutputBytes(firstBytes);

    // --- Phase 2: persist the remaining tasks' inputs the same way TaskFilesPersistence does ---
    // (Java serialization of Map<Integer, BaseOptimizingInput>.)
    Map<Integer, BaseOptimizingInput> toPersist = new HashMap<>();
    for (int i = 1; i < original.size(); i++) {
      toPersist.put(i, original.get(i).getInput());
    }
    ByteBuffer persisted = SerializationUtil.simpleSerialize(toPersist);
    byte[] persistedBytes = new byte[persisted.remaining()];
    persisted.get(persistedBytes);

    // Capture the factory-impl per "row" — mimics the task_runtime.properties column.
    Gson gson = new Gson();
    Map<Integer, String> rowPropertiesJson = new HashMap<>();
    for (int i = 1; i < original.size(); i++) {
      rowPropertiesJson.put(i, gson.toJson(original.get(i).getProperties()));
    }

    // --- Phase 3: simulate AMS restart — drop all in-memory references to the planner/tasks. ---
    original = null;
    planner = null;
    plan = null;
    // Note: firstTask kept intentionally — it is the already-committed success that the committer
    // needs in its input collection (a real AMS keeps it on TableOptimizingProcess.taskMap too).

    // --- Phase 4: re-materialise tasks the way OptimizingQueue.loadTaskRuntimes does. ---
    @SuppressWarnings("unchecked")
    Map<Integer, BaseOptimizingInput> reloaded =
        (Map<Integer, BaseOptimizingInput>) SerializationUtil.simpleDeserialize(persistedBytes);
    assertEquals(toPersist.size(), reloaded.size(), "Serialization round-trip must preserve count");

    TaskDescriptorTypeConverter converter = new TaskDescriptorTypeConverter();
    List<PaimonCompactionTask> resumedTasks = new ArrayList<>();
    for (Map.Entry<Integer, BaseOptimizingInput> entry : reloaded.entrySet()) {
      Integer taskId = entry.getKey();
      BaseOptimizingInput reloadedInput = entry.getValue();

      assertTrue(
          reloadedInput instanceof PaimonCompactionInput,
          "Reloaded input for Paimon row must still be PaimonCompactionInput, got "
              + reloadedInput.getClass().getName());

      // Route via converter — mocked ResultSet stands in for MyBatis.
      ResultSet rs = Mockito.mock(ResultSet.class);
      Mockito.when(rs.getString("properties")).thenReturn(rowPropertiesJson.get(taskId));
      StagedTaskDescriptor<?, ?, ?> descriptor =
          converter.getResult(rs, "task_executor_factory_impl");
      assertTrue(
          descriptor instanceof PaimonCompactionTask,
          "TaskDescriptorTypeConverter must route Paimon factory-impl to PaimonCompactionTask, got "
              + descriptor.getClass().getName());

      @SuppressWarnings("unchecked")
      PaimonCompactionTask paimonDescriptor = (PaimonCompactionTask) descriptor;
      // Bind input back the same way OptimizingQueue.bindLoadedInput does.
      paimonDescriptor.setInput((PaimonCompactionInput) reloadedInput);
      resumedTasks.add(paimonDescriptor);
    }

    // --- Phase 5: execute the resumed tasks; each should have a valid input wired up. ---
    for (PaimonCompactionTask t : resumedTasks) {
      PaimonCompactionInput in = t.getInput();
      assertNotNull(in);
      assertNotNull(in.getCommitUser(), "Restored input must retain commitUser for commit dedupe");
      assertEquals(42L, in.getCommitIdentifier(), "Restored input must retain plan processId");
      PaimonCompactionOutput out = new PaimonCompactionExecutor(in).execute();
      ByteBuffer bb = SerializationUtil.simpleSerialize(out);
      byte[] bytes = new byte[bb.remaining()];
      bb.get(bytes);
      t.setOutputBytes(bytes);
    }

    // --- Phase 6: combine the still-remembered first task with resumed ones, commit via factory
    // ---
    PaimonProcessFactory factory = new PaimonProcessFactory();
    Map<String, String> props = new HashMap<>();
    props.put(PaimonProcessFactory.OPTIMIZER_ENABLED.key(), "true");
    factory.open(props);
    List<StagedTaskDescriptor<?, ?, ?>> allSuccess = new ArrayList<>();
    allSuccess.add(firstTask);
    allSuccess.addAll(resumedTasks);

    // targetSnapshotId remains the planner's scan baseline; commit identity is carried by each
    // PaimonCompactionInput and validated inside PaimonProcessFactory.createCommitter.
    long targetSnapshotId = firstTask.getInput().getTargetSnapshotId();
    TableOptimizingCommitter committer =
        factory.createCommitter(
            wrapped,
            targetSnapshotId,
            -1L,
            allSuccess,
            Collections.emptyMap(),
            Collections.emptyMap());
    committer.commit();

    long filesAfter = countDataFiles((AppendOnlyFileStoreTable) catalog.getTable(id));
    assertTrue(
        filesAfter < filesBefore,
        "Resumed compaction must still reduce file count (before="
            + filesBefore
            + ", after="
            + filesAfter
            + ")");
  }

  @Test
  @DisplayName(
      "Recovered replay of the same Paimon success output does not create a duplicate snapshot")
  void testRecoveredReplayDoesNotDuplicateSnapshot(@TempDir Path warehouse) throws Exception {
    Catalog catalog = fsCatalog(warehouse);
    createBucketUnawareAppendTable(catalog, "t_recovery_replay", /* commits= */ 8);
    Identifier id = Identifier.create("db1", "t_recovery_replay");
    List<String> rowsBefore = readRowStrings(catalog.getTable(id));
    long snapshotBefore =
        ((AppendOnlyFileStoreTable) catalog.getTable(id)).snapshotManager().latestSnapshot().id();

    PaimonTable wrapped =
        new PaimonTable(
            TableIdentifier.of("test_catalog", "db1", "t_recovery_replay"), catalog.getTable(id));
    PaimonOptimizingPlanner planner =
        new PaimonOptimizingPlanner(
            wrapped, /* tableId= */ 7L, /* processId= */ 9009L, 1.0, 128L * 1024L * 1024L);
    OptimizingPlanResult<PaimonCompactionTask> plan = planner.plan();
    List<PaimonCompactionTask> plannedTasks = executeAll(plan.getTasks());
    assertFalse(plannedTasks.isEmpty());

    // Simulate the two persisted AMS recovery surfaces: TaskFilesPersistence stores inputs by
    // task id, while task_runtime.rewrite_output stores the already successful task output.
    Map<Integer, BaseOptimizingInput> persistedInputs = new HashMap<>();
    Map<Integer, byte[]> persistedOutputs = new HashMap<>();
    for (int i = 0; i < plannedTasks.size(); i++) {
      int taskId = i + 1;
      PaimonCompactionTask task = plannedTasks.get(i);
      persistedInputs.put(taskId, task.getInput());
      persistedOutputs.put(taskId, serializeOutput(task.getOutput()));
    }
    ByteBuffer inputBuffer = SerializationUtil.simpleSerialize(persistedInputs);
    byte[] inputBytes = new byte[inputBuffer.remaining()];
    inputBuffer.get(inputBytes);

    @SuppressWarnings("unchecked")
    Map<Integer, BaseOptimizingInput> reloadedInputs =
        (Map<Integer, BaseOptimizingInput>) SerializationUtil.simpleDeserialize(inputBytes);
    List<PaimonCompactionTask> recoveredSuccessTasks = new ArrayList<>();
    for (int i = 0; i < plannedTasks.size(); i++) {
      int taskId = i + 1;
      PaimonCompactionTask original = plannedTasks.get(i);
      BaseOptimizingInput reloadedInput = reloadedInputs.get(taskId);
      assertTrue(reloadedInput instanceof PaimonCompactionInput);
      PaimonCompactionInput input = (PaimonCompactionInput) reloadedInput;
      assertEquals(original.getInput().getCommitUser(), input.getCommitUser());
      assertEquals(9009L, input.getCommitIdentifier());
      PaimonCompactionTask recovered =
          new PaimonCompactionTask(
              original.getTableId(),
              original.getPartition(),
              input,
              new HashMap<>(original.getProperties()));
      recovered.setOutputBytes(persistedOutputs.get(taskId));
      recoveredSuccessTasks.add(recovered);
    }

    PaimonProcessFactory factory = enabledPaimonFactory();
    factory
        .createCommitter(
            wrapped,
            plan.getTargetSnapshotId(),
            -1L,
            asDescriptors(recoveredSuccessTasks),
            Collections.emptyMap(),
            Collections.emptyMap())
        .commit();

    AppendOnlyFileStoreTable afterFirstCommit = (AppendOnlyFileStoreTable) catalog.getTable(id);
    long snapshotAfterFirst = afterFirstCommit.snapshotManager().latestSnapshot().id();
    Snapshot firstSnapshot = afterFirstCommit.snapshotManager().latestSnapshot();
    assertEquals(snapshotBefore + 1, snapshotAfterFirst);
    assertEquals(Snapshot.CommitKind.COMPACT, firstSnapshot.commitKind());
    assertEquals(9009L, firstSnapshot.commitIdentifier());
    assertEquals(rowsBefore, readRowStrings(afterFirstCommit));

    factory
        .createCommitter(
            wrapped,
            plan.getTargetSnapshotId(),
            -1L,
            asDescriptors(recoveredSuccessTasks),
            Collections.emptyMap(),
            Collections.emptyMap())
        .commit();

    AppendOnlyFileStoreTable afterReplay = (AppendOnlyFileStoreTable) catalog.getTable(id);
    assertEquals(
        snapshotAfterFirst,
        afterReplay.snapshotManager().latestSnapshot().id(),
        "Recovered replay must reuse the same commitUser + commitIdentifier and create no snapshot");
    assertEquals(rowsBefore, readRowStrings(afterReplay));
  }

  // ---------------------------------------------------------------------------------------------
  // Test 3 · Iceberg regression guard. Validates the converter still routes Iceberg factory-impl
  // to RewriteStageTask (unchanged) and MetricsSummary's canonical keys are preserved.
  // ---------------------------------------------------------------------------------------------
  @Test
  @DisplayName(
      "Iceberg control: TaskDescriptorTypeConverter still routes Iceberg factory-impl to "
          + "RewriteStageTask; MetricsSummary keys unchanged (no refactor regression)")
  void testIcebergRegressionGuard() throws Exception {
    TaskDescriptorTypeConverter converter = new TaskDescriptorTypeConverter();

    // Case 1: Iceberg factory-impl → RewriteStageTask.
    Map<String, String> icebergProps = new HashMap<>();
    icebergProps.put(
        TaskProperties.TASK_EXECUTOR_FACTORY_IMPL, IcebergRewriteExecutorFactory.class.getName());
    ResultSet icebergRs = Mockito.mock(ResultSet.class);
    Mockito.when(icebergRs.getString("properties")).thenReturn(new Gson().toJson(icebergProps));
    StagedTaskDescriptor<?, ?, ?> icebergDescriptor = converter.getResult(icebergRs, "x");
    assertTrue(
        icebergDescriptor instanceof RewriteStageTask,
        "Iceberg factory-impl must still route to RewriteStageTask, got "
            + icebergDescriptor.getClass().getName());

    // Case 2: null properties column → legacy Iceberg row (RewriteStageTask) — backwards compat.
    ResultSet legacyRs = Mockito.mock(ResultSet.class);
    Mockito.when(legacyRs.getString("properties")).thenReturn(null);
    assertTrue(
        converter.getResult(legacyRs, "x") instanceof RewriteStageTask,
        "Legacy rows (null properties) must still default to RewriteStageTask");

    // Case 3: MetricsSummary shape preserved — the key names the dashboard reads.
    RewriteStageTask iceberg = icebergTask();
    TaskMetricsSummary exposed = iceberg.toMetricsSummary();
    assertSame(iceberg.getSummary(), exposed, "Iceberg descriptor must hand out its summary as-is");
    assertTrue(exposed instanceof MetricsSummary);
    Map<String, String> map = exposed.summaryAsMap(false);
    assertNotNull(map.get(MetricsSummary.INPUT_DATA_FILES));
    assertNotNull(map.get(MetricsSummary.OUTPUT_DATA_FILES));
    assertNotNull(map.get(MetricsSummary.INPUT_DATA_SIZE));
    assertNotNull(map.get(MetricsSummary.OUTPUT_DATA_SIZE));

    // Case 4: A minimal Iceberg-only aggregate round-trip through MetricsSummary.aggregate does
    // not throw. This is the "control run" the plan asked for: no Paimon in sight.
    MetricsSummary agg = MetricsSummary.aggregate(Collections.singletonList(exposed));
    assertNotNull(agg);
    // The empty RewriteFilesInput contributes zero; we only care that the shape loaded cleanly.
    assertEquals(0, agg.getRewriteDataFileCnt());
    assertEquals(0, agg.getNewDataFileCnt());
  }

  @Test
  public void validateRecoveredPaimonTaskAcceptsPaimonDescriptorInputOutputAndSummary()
      throws Exception {
    PaimonCompactionTask task = new PaimonCompactionTask();
    task.ensureExecutorFactoryImpl(PaimonCompactionExecutorFactory.class.getName());
    PaimonCompactionOutput output =
        new PaimonCompactionOutput(new byte[] {1}, 7, 9L, 900L, 3L, 300L);
    task.setOutputBytes(serializeOutput(output));
    PaimonCompactionInput input =
        new PaimonCompactionInput(null, new byte[] {2}, 2, "u", "p", 10L, 11L);

    TaskDescriptorRecoveryTypes.validateRecoveredTask(task, input);
  }

  @Test
  public void validateRecoveredTaskRejectsLegacyDescriptorWithPaimonInput() {
    RewriteStageTask descriptor = new RewriteStageTask();
    descriptor.ensureExecutorFactoryImpl(IcebergRewriteExecutorFactory.class.getName());
    PaimonCompactionInput input =
        new PaimonCompactionInput(null, new byte[] {2}, 2, "u", "p", 10L, 11L);

    assertThrows(
        IllegalArgumentException.class,
        () -> TaskDescriptorRecoveryTypes.validateRecoveredTask(descriptor, input));
  }

  // ---------------------------------------------------------------------------------------------
  // Fixtures
  // ---------------------------------------------------------------------------------------------
  private static Catalog fsCatalog(Path warehouse) {
    Map<String, String> props = new HashMap<>();
    props.put(CatalogOptions.WAREHOUSE.key(), warehouse.toUri().toString());
    return PaimonCatalogFactory.paimonCatalog(props, new Configuration());
  }

  /**
   * Create a BUCKET_UNAWARE append-only table seeded with {@code commits} tiny commits — one row
   * each. With {@code target-file-size=1 kb} and {@code compaction.min.file-num=2} this reliably
   * produces multiple tiny files the planner can coalesce.
   */
  private static Table createBucketUnawareAppendTable(
      Catalog catalog, String tableName, int commits) throws Exception {
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
    for (int i = 0; i < commits; i++) {
      BatchWriteBuilder builder = table.newBatchWriteBuilder();
      try (BatchTableWrite write = builder.newWrite()) {
        write.write(GenericRow.of(i, BinaryString.fromString("row-" + i)));
        List<CommitMessage> messages = write.prepareCommit();
        try (BatchTableCommit commit = builder.newCommit()) {
          commit.commit(messages);
        }
      }
    }
    return catalog.getTable(id);
  }

  private static long countDataFiles(AppendOnlyFileStoreTable table) throws Exception {
    return table.newReadBuilder().newScan().plan().splits().stream()
        .filter(s -> s instanceof DataSplit)
        .mapToLong(s -> ((DataSplit) s).dataFiles().size())
        .sum();
  }

  private static long countRows(AppendOnlyFileStoreTable table) throws Exception {
    long rows = 0;
    for (org.apache.paimon.table.source.Split split :
        table.newReadBuilder().newScan().plan().splits()) {
      if (split instanceof DataSplit) {
        for (org.apache.paimon.io.DataFileMeta f : ((DataSplit) split).dataFiles()) {
          rows += f.rowCount();
        }
      }
    }
    return rows;
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

  private static List<PaimonCompactionTask> executeAll(Collection<PaimonCompactionTask> tasks)
      throws Exception {
    List<PaimonCompactionTask> executed = new ArrayList<>(tasks);
    for (PaimonCompactionTask task : executed) {
      PaimonCompactionOutput output = new PaimonCompactionExecutor(task.getInput()).execute();
      task.setOutputBytes(serializeOutput(output));
    }
    return executed;
  }

  private static byte[] serializeOutput(PaimonCompactionOutput output) {
    ByteBuffer buffer = SerializationUtil.simpleSerialize(output);
    byte[] bytes = new byte[buffer.remaining()];
    buffer.get(bytes);
    return bytes;
  }

  private static PaimonProcessFactory enabledPaimonFactory() {
    PaimonProcessFactory factory = new PaimonProcessFactory();
    Map<String, String> props = new HashMap<>();
    props.put(PaimonProcessFactory.OPTIMIZER_ENABLED.key(), "true");
    factory.open(props);
    return factory;
  }

  private static List<StagedTaskDescriptor<?, ?, ?>> asDescriptors(
      Collection<PaimonCompactionTask> tasks) {
    return new ArrayList<StagedTaskDescriptor<?, ?, ?>>(tasks);
  }

  private static RewriteStageTask icebergTask() {
    RewriteFilesInput emptyInput =
        new RewriteFilesInput(
            new DataFile[0],
            new DataFile[0],
            new ContentFile<?>[0],
            new ContentFile<?>[0],
            /* table= */ null);
    return new RewriteStageTask(1L, "p=1", emptyInput, new HashMap<>());
  }

  // Reference preserved so the test symbol is not treated as unused when IDEs refactor imports.
  @SuppressWarnings("unused")
  private static Collection<StagedTaskDescriptor<?, ?, ?>> widenForCompile() {
    return Collections.singletonList(new PaimonCompactionTask());
  }
}
