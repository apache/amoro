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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionInput;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionOutput;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionTask;
import org.apache.amoro.optimizing.MetricsSummary;
import org.apache.amoro.optimizing.RewriteFilesInput;
import org.apache.amoro.optimizing.RewriteStageTask;
import org.apache.amoro.optimizing.TaskMetricsSummary;
import org.apache.amoro.process.StagedTaskDescriptor;
import org.apache.amoro.utils.SerializationUtil;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Multi-format regression guard around the C2 generalization of {@link OptimizingQueue}.
 *
 * <p>The production surface we are pinning down:
 *
 * <ul>
 *   <li>{@code StagedTaskDescriptor#toMetricsSummary()} works for both Iceberg ({@link
 *       RewriteStageTask}) and Paimon ({@link PaimonCompactionTask}) without the caller needing to
 *       know the concrete subclass.
 *   <li>{@link MetricsSummary#aggregate(List)} accepts a mixed list of {@link TaskMetricsSummary}
 *       instances (Iceberg {@code MetricsSummary} + Paimon adapter) without throwing {@link
 *       ClassCastException} and merges the numbers.
 *   <li>{@code OptimizingQueue.TableOptimizingProcess#taskMap} / {@code taskQueue} are now typed
 *       over {@code StagedTaskDescriptor<?,?,?>} — reflected on directly so a future regression
 *       that re-narrows the type back to {@code RewriteStageTask} fails fast here instead of at
 *       runtime on a Paimon plan.
 * </ul>
 *
 * <p>The test is intentionally light — no AMS bootstrap, no mappers, no persistence. Full
 * OptimizingQueue lifecycle coverage stays in {@code TestOptimizingQueue}; this class only guards
 * the multi-format contract.
 */
@DisplayName("OptimizingQueue multi-format generics")
class TestOptimizingQueueMultiFormat {

  // ---------- Scenario 1: Iceberg regression guard -----------------------------------------------

  @Test
  @DisplayName("Iceberg RewriteStageTask.toMetricsSummary() returns the backing MetricsSummary")
  void icebergTaskExposesMetricsSummaryUnchanged() {
    RewriteStageTask task = icebergTask();
    TaskMetricsSummary exposed = task.toMetricsSummary();

    // Zero-behaviour-change contract: it's the same field, not a copy.
    assertSame(task.getSummary(), exposed, "Iceberg descriptor must hand out its summary as-is");
    assertInstanceOf(MetricsSummary.class, exposed);

    Map<String, String> map = exposed.summaryAsMap(false);
    // The empty RewriteFilesInput contributes zero files; the canonical Iceberg key is present.
    assertEquals("0", map.get(MetricsSummary.INPUT_DATA_FILES));
    assertEquals("0", map.get(MetricsSummary.OUTPUT_DATA_FILES));
  }

  @Test
  @DisplayName(
      "Iceberg-only aggregation still produces a MetricsSummary (no regression on the happy path)")
  void icebergOnlyAggregationReturnsMetricsSummary() {
    RewriteStageTask task = icebergTask();
    MetricsSummary aggregated = MetricsSummary.aggregate(List.of(task.toMetricsSummary()));

    assertNotNull(aggregated);
    // The aggregate round-trips the zero counts — nothing exploded on the familiar Iceberg shape.
    assertEquals(0, aggregated.getRewriteDataFileCnt());
    assertEquals(0, aggregated.getNewDataFileCnt());
  }

  // ---------- Scenario 2: Paimon acceptance ------------------------------------------------------

  @Test
  @DisplayName(
      "Paimon PaimonCompactionTask.toMetricsSummary() emits Iceberg-compatible keys without CCE")
  void paimonTaskEmitsIcebergCompatibleKeys() {
    PaimonCompactionTask task = paimonTaskWithOutput(/* compactedFiles= */ 5, /* produced= */ 1);

    // No ClassCastException: the descriptor returns a TaskMetricsSummary directly, not a cast of a
    // Paimon-specific summary into something the queue does not understand.
    TaskMetricsSummary adapted = task.toMetricsSummary();
    assertNotNull(adapted);

    Map<String, String> map = adapted.summaryAsMap(false);
    assertTrue(
        map.containsKey("input-data-files"),
        "Paimon summary must expose input-data-files for dashboard compatibility");
    assertTrue(map.containsKey("input-data-size"));
    assertTrue(map.containsKey("output-data-files"));
    assertTrue(map.containsKey("output-data-size"));

    // Numbers should reflect the AppendCompactTask output — compacted -> input, produced -> output.
    assertEquals("5", map.get("input-data-files"));
    assertEquals("1", map.get("output-data-files"));
  }

  @Test
  @DisplayName("Paimon-only aggregation reads counts via the Paimon adapter, not cast")
  void paimonOnlyAggregation() {
    PaimonCompactionTask a = paimonTaskWithOutput(3, 1);
    PaimonCompactionTask b = paimonTaskWithOutput(4, 2);

    MetricsSummary aggregated =
        MetricsSummary.aggregate(List.of(a.toMetricsSummary(), b.toMetricsSummary()));

    assertNotNull(aggregated);
    // Paimon compacted counts funnel into the "input-data-files" slot of MetricsSummary (rewrite*
    // fields) via fromMap; the aggregate sums both tasks.
    assertEquals(3 + 4, aggregated.getRewriteDataFileCnt());
    assertEquals(1 + 2, aggregated.getNewDataFileCnt());
  }

  // ---------- Scenario 3: Mixed Iceberg + Paimon in the same aggregation -------------------------

  @Test
  @DisplayName("Mixed Iceberg + Paimon aggregation completes without ClassCastException")
  void mixedAggregationIsSafe() {
    RewriteStageTask iceberg = icebergTask();
    PaimonCompactionTask paimon = paimonTaskWithOutput(7, 2);

    // Order intentionally mixes an Iceberg MetricsSummary (which also implements
    // TaskMetricsSummary)
    // with a Paimon adapter. Prior to C2, OptimizingQueue collapsed the stream through a
    // RewriteStageTask-typed reference — the equivalent call now flows through
    // StagedTaskDescriptor::toMetricsSummary and must accept both.
    MetricsSummary aggregated =
        MetricsSummary.aggregate(List.of(iceberg.toMetricsSummary(), paimon.toMetricsSummary()));

    assertNotNull(aggregated);
    // Paimon contributes 7 compacted files as "input-data-files" (rewriteDataFileCnt slot on
    // MetricsSummary), Iceberg contributes 0 from its empty input.
    assertEquals(7, aggregated.getRewriteDataFileCnt());
    assertEquals(2, aggregated.getNewDataFileCnt());
  }

  // ---------- Queue-level generic pinning --------------------------------------------------------

  @Test
  @DisplayName(
      "TableOptimizingProcess.taskMap + taskQueue are now widened to StagedTaskDescriptor<?,?,?>")
  void queueInternalsAreGenericallyWidened() throws Exception {
    Class<?> processClass =
        Class.forName("org.apache.amoro.server.optimizing.OptimizingQueue$TableOptimizingProcess");

    Field taskMap = processClass.getDeclaredField("taskMap");
    Field taskQueue = processClass.getDeclaredField("taskQueue");

    // The generic signature string should no longer mention RewriteStageTask — if it does, someone
    // regressed the C2 widening. Checking the generic signature instead of the erased type avoids
    // relying on reflection for parameterized types in a way that can silently pass with raw Map.
    String taskMapSig = taskMap.getGenericType().getTypeName();
    String taskQueueSig = taskQueue.getGenericType().getTypeName();

    assertTrue(
        taskMapSig.contains("StagedTaskDescriptor"),
        "taskMap generic should reference StagedTaskDescriptor, got: " + taskMapSig);
    assertTrue(
        taskQueueSig.contains("StagedTaskDescriptor"),
        "taskQueue generic should reference StagedTaskDescriptor, got: " + taskQueueSig);
    assertTrue(
        !taskMapSig.contains("RewriteStageTask"),
        "taskMap must not be narrowed to RewriteStageTask, got: " + taskMapSig);
    assertTrue(
        !taskQueueSig.contains("RewriteStageTask"),
        "taskQueue must not be narrowed to RewriteStageTask, got: " + taskQueueSig);
  }

  // ---------- Fixtures ---------------------------------------------------------------------------

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

  private static PaimonCompactionTask paimonTaskWithOutput(long compactedFiles, long produced) {
    PaimonCompactionInput input =
        new PaimonCompactionInput(null, new byte[] {1}, 2, "commit-user", "p=1", 0L);
    PaimonCompactionTask task = new PaimonCompactionTask(2L, "p=1", input, new HashMap<>());

    PaimonCompactionOutput output =
        new PaimonCompactionOutput(
            /* snapshotBytes= */ new byte[] {9},
            /* serializerVersion= */ 3,
            /* compactedFileCount= */ compactedFiles,
            /* compactedFileSize= */ compactedFiles * 100L,
            /* producedFileCount= */ produced,
            /* producedFileSize= */ produced * 90L);
    ByteBuffer buffer = SerializationUtil.simpleSerialize(output);
    byte[] outBytes = new byte[buffer.remaining()];
    buffer.get(outBytes);
    task.setOutputBytes(outBytes);
    return task;
  }

  // Reference preserved so the test symbol is not treated as unused when IDEs refactor imports.
  @SuppressWarnings("unused")
  private static StagedTaskDescriptor<?, ?, ?> widenForCompile(StagedTaskDescriptor<?, ?, ?> d) {
    return Arrays.asList(d).get(0);
  }
}
