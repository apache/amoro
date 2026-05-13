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

import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.formats.paimon.PaimonTable;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionExecutorFactory;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionInput;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionTask;
import org.apache.amoro.optimizing.OptimizingPlanResult;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.amoro.optimizing.TableOptimizingPlanner;
import org.apache.amoro.optimizing.TaskProperties;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.paimon.CoreOptions;
import org.apache.paimon.append.AppendCompactTask;
import org.apache.paimon.data.BinaryRow;
import org.apache.paimon.io.DataFileMeta;
import org.apache.paimon.options.Options;
import org.apache.paimon.predicate.Predicate;
import org.apache.paimon.table.AppendOnlyFileStoreTable;
import org.apache.paimon.table.sink.AppendCompactTaskSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.ToLongFunction;

/**
 * {@link TableOptimizingPlanner} for Paimon BUCKET_UNAWARE (AppendOnly, bucket=-1) tables.
 *
 * <p>Implements the interface directly instead of extending {@code AbstractOptimizingPlanner}
 * because the latter is deeply coupled to Iceberg-specific types ({@code StructLike}, {@code
 * PartitionSpec}, {@code Expression}).
 *
 * <p>Planning flow:
 *
 * <ol>
 *   <li>{@link #isNecessary()} guards the BUCKET_UNAWARE / AppendOnly shape, scans active ADD files
 *       through {@link PaimonAppendFileScanner}, evaluates partitions, and builds Amoro-built
 *       {@link AppendCompactTask}s via {@link PaimonAppendTaskPacker}.
 *   <li>{@link #plan()} wraps each {@link AppendCompactTask} into a {@link PaimonCompactionTask}
 *       carrying serialized task bytes, a stable commit user, serializer version, and target
 *       snapshot id.
 * </ol>
 *
 * <p>The generated {@link PaimonCompactionTask}'s {@code properties} carry the executor-factory
 * implementation class name under {@link TaskProperties#TASK_EXECUTOR_FACTORY_IMPL} so the
 * Optimizer side can reflectively load {@link PaimonCompactionExecutorFactory}.
 *
 * <p>The {@code commitUser} returned via {@link #getCommitUser()} is generated once per {@link
 * #plan()} call and is carried end-to-end inside each {@link
 * org.apache.amoro.formats.paimon.optimizing.PaimonCompactionInput} (flushed through {@code
 * TaskFilesPersistence}). Retries and AMS restarts therefore re-use the same value automatically —
 * callers do not need to persist it separately to {@code TableProcessStore.properties}.
 */
public class PaimonOptimizingPlanner implements TableOptimizingPlanner {

  private static final Logger LOG = LoggerFactory.getLogger(PaimonOptimizingPlanner.class);

  public static final String COMMIT_USER_PROPERTY = "paimon.commit.user";

  private final PaimonTable paimonTable;
  private final long tableId;
  private final long processId;
  private final long planTime;
  private final double availableCore;
  private final long maxInputSizePerThread;
  private final OptimizingConfig optimizingConfig;
  private final long lastMinorOptimizingTime;
  private final long lastMajorOptimizingTime;
  private final long lastFullOptimizingTime;
  private final Predicate partitionFilter;

  // Memoised state built the first time isNecessary() / plan() runs.
  private Boolean necessary;
  private List<PlannedAppendTask> cachedTasks;
  private String commitUser;
  private long targetSnapshotId = -1L;
  private OptimizingType optimizingType = OptimizingType.MINOR;

  public PaimonOptimizingPlanner(
      PaimonTable paimonTable,
      long tableId,
      long processId,
      double availableCore,
      long maxInputSizePerThread) {
    this(
        paimonTable,
        tableId,
        processId,
        availableCore,
        maxInputSizePerThread,
        defaultOptimizingConfig(),
        0L,
        0L,
        0L,
        null);
  }

  public PaimonOptimizingPlanner(
      PaimonTable paimonTable,
      long tableId,
      long processId,
      double availableCore,
      long maxInputSizePerThread,
      Predicate partitionFilter) {
    this(
        paimonTable,
        tableId,
        processId,
        availableCore,
        maxInputSizePerThread,
        defaultOptimizingConfig(),
        0L,
        0L,
        0L,
        partitionFilter);
  }

  public PaimonOptimizingPlanner(
      PaimonTable paimonTable,
      long tableId,
      long processId,
      double availableCore,
      long maxInputSizePerThread,
      OptimizingConfig optimizingConfig,
      long lastMinorOptimizingTime,
      long lastMajorOptimizingTime,
      long lastFullOptimizingTime,
      Predicate partitionFilter) {
    this.paimonTable = paimonTable;
    this.tableId = tableId;
    this.processId = processId;
    this.planTime = System.currentTimeMillis();
    this.availableCore = availableCore;
    this.maxInputSizePerThread = maxInputSizePerThread;
    this.optimizingConfig = optimizingConfig == null ? defaultOptimizingConfig() : optimizingConfig;
    this.lastMinorOptimizingTime = lastMinorOptimizingTime;
    this.lastMajorOptimizingTime = lastMajorOptimizingTime;
    this.lastFullOptimizingTime = lastFullOptimizingTime;
    this.partitionFilter = partitionFilter;
  }

  private static OptimizingConfig defaultOptimizingConfig() {
    return new OptimizingConfig()
        .setEnabled(true)
        .setMinorLeastInterval(3600000)
        .setFullTriggerInterval(-1)
        .setFullRewriteAllFiles(false)
        .setMaxTaskSize(Long.MAX_VALUE);
  }

  @Override
  public boolean isNecessary() {
    if (necessary != null) {
      return necessary;
    }
    AppendOnlyFileStoreTable table = unwrapBucketUnawareTable();
    if (table == null) {
      necessary = false;
      return false;
    }

    PaimonPlanContext context =
        PaimonPlanContext.forOptions(
            CoreOptions.fromMap(table.options()),
            optimizingConfig,
            lastMinorOptimizingTime,
            lastMajorOptimizingTime,
            lastFullOptimizingTime,
            availableCore,
            maxInputSizePerThread,
            planTime);
    PaimonAppendFileScanner.ScanResult scanResult =
        new PaimonAppendFileScanner(table, context, partitionFilter).scan();
    targetSnapshotId = scanResult.snapshotId();
    Map<BinaryRow, List<PaimonFileCandidate>> filesByPartition = scanResult.files();
    List<PlannedAppendTask> tasks = new ArrayList<>();
    PaimonPartitionEvaluator evaluator = new PaimonPartitionEvaluator(context);
    PaimonAppendTaskPacker packer = new PaimonAppendTaskPacker(context);
    for (Map.Entry<BinaryRow, List<PaimonFileCandidate>> entry : filesByPartition.entrySet()) {
      PaimonPartitionEvaluation evaluation = evaluator.evaluate(entry.getKey(), entry.getValue());
      if (!evaluation.necessary()) {
        continue;
      }
      List<AppendCompactTask> packed = packer.pack(evaluation);
      if (packed.isEmpty()) {
        continue;
      }
      for (AppendCompactTask task : packed) {
        tasks.add(new PlannedAppendTask(task, evaluation.optimizingType()));
      }
    }
    cachedTasks = tasks;
    optimizingType = highestType(cachedTasks);
    necessary = !cachedTasks.isEmpty();
    if (!necessary) {
      LOG.info(
          "Paimon table [{}] has no eligible optimizing tasks — skip optimizing.",
          paimonTable.id().getTableName());
    }
    return necessary;
  }

  @Override
  public OptimizingPlanResult<PaimonCompactionTask> plan() {
    if (!isNecessary()) {
      return emptyResult();
    }
    AppendOnlyFileStoreTable table = unwrapBucketUnawareTable();
    if (table == null) {
      return emptyResult();
    }

    AppendCompactTaskSerializer serializer = new AppendCompactTaskSerializer();
    int serializerVersion = serializer.getVersion();

    // Generate commitUser once per Planner instance — retries within the same plan MUST reuse
    // the same value so Paimon's (user + identifier) idempotency can dedupe stale commits.
    if (commitUser == null) {
      commitUser = CoreOptions.createCommitUser(Options.fromMap(table.options()));
    }

    // Apply plan-tick quota: cap count to ceil(availableCore). Input-size splitting is handled by
    // PaimonAppendTaskPacker before this point.
    // NOTE (§3.4 split contract): this does NOT re-split, merge, or otherwise mutate the
    // Amoro-built AppendCompactTask — we only decide how many tasks get released this tick.
    List<PlannedAppendTask> eligible =
        applyQuotaInternal(
            cachedTasks,
            availableCore,
            maxInputSizePerThread,
            plannedTask -> totalCompactBeforeSize(plannedTask.task()),
            paimonTable.id().getTableName());

    if (eligible.isEmpty()) {
      LOG.info(
          "Paimon table [{}] plan tick produced 0 eligible tasks — "
              + "reset isNecessary() to false for this planner instance.",
          paimonTable.id().getTableName());
      necessary = false;
      return emptyResult();
    }
    optimizingType = highestType(eligible);

    List<PaimonCompactionTask> wrapped = new ArrayList<>(eligible.size());
    for (PlannedAppendTask plannedTask : eligible) {
      AppendCompactTask task = plannedTask.task();
      byte[] bytes;
      try {
        bytes = serializer.serialize(task);
      } catch (IOException e) {
        throw new IllegalStateException(
            "Failed to serialize Paimon AppendCompactTask for partition " + task.partition(), e);
      }
      PaimonCompactionInput input =
          new PaimonCompactionInput(
              paimonTable,
              bytes,
              serializerVersion,
              commitUser,
              task.partition() == null ? "" : task.partition().toString(),
              targetSnapshotId);
      Map<String, String> props = Maps.newHashMap();
      props.put(
          TaskProperties.TASK_EXECUTOR_FACTORY_IMPL,
          PaimonCompactionExecutorFactory.class.getName());
      wrapped.add(new PaimonCompactionTask(tableId, input.getPartitionPath(), input, props));
    }

    return new OptimizingPlanResult<>(
        processId,
        getOptimizingType(),
        planTime,
        targetSnapshotId,
        -1L /* targetChangeSnapshotId — Paimon has no change snapshot */,
        wrapped,
        Collections.emptyMap() /* fromSequence */,
        Collections.emptyMap() /* toSequence */);
  }

  /**
   * Return the {@code commitUser} generated for this plan, or {@code null} if {@link #plan()} has
   * not yet been invoked or if no compaction was necessary.
   *
   * <p>The value is authoritative only <em>after</em> {@link #plan()} runs: it is propagated into
   * every task via {@link
   * org.apache.amoro.formats.paimon.optimizing.PaimonCompactionInput#getCommitUser()} and survives
   * AMS restarts through {@code TaskFilesPersistence}. External callers do not need to persist it
   * separately — {@link #COMMIT_USER_PROPERTY} is retained as a property-key constant for future
   * integrations that might want to expose the value on {@code TableProcessStore.properties}.
   */
  public String getCommitUser() {
    return commitUser;
  }

  /**
   * Apply the plan-tick quota to a list of {@link AppendCompactTask}s built by the Amoro Paimon
   * planner.
   *
   * <p>Two independent filters are applied:
   *
   * <p>At most {@code ceil(availableCore)} eligible tasks are released per tick. Remaining tasks
   * are not persisted. The next planning tick scans the latest snapshot again and rebuilds eligible
   * tasks from current metadata.
   *
   * <p>Critically, this method NEVER mutates a task's internal {@code compactBefore()} list —
   * Paimon's atomic commit unit is preserved. Merging or splitting is explicitly disallowed per the
   * plan-document §3.4 task-splitting contract.
   *
   * <p>Visible for testing so unit tests can inject a synthetic {@code sizeFn} without standing up
   * a real Paimon file system.
   */
  static List<AppendCompactTask> applyQuota(
      List<AppendCompactTask> tasks,
      double availableCore,
      long maxInputSizePerThread,
      ToLongFunction<AppendCompactTask> sizeFn,
      String tableNameForLog) {
    return applyQuotaInternal(tasks, availableCore, maxInputSizePerThread, sizeFn, tableNameForLog);
  }

  private static <T> List<T> applyQuotaInternal(
      List<T> tasks,
      double availableCore,
      long maxInputSizePerThread,
      ToLongFunction<T> sizeFn,
      String tableNameForLog) {
    if (tasks == null || tasks.isEmpty()) {
      return Collections.emptyList();
    }
    // ceil(availableCore), but guarded against non-positive core values — in those degenerate
    // cases we still allow at most one task through so progress is not completely stalled.
    int cap = Math.max(1, (int) Math.ceil(availableCore));
    List<T> out = new ArrayList<>(Math.min(cap, tasks.size()));
    int observedOversized = 0;
    for (T task : tasks) {
      if (out.size() >= cap) {
        break;
      }
      long totalSize = sizeFn.applyAsLong(task);
      if (maxInputSizePerThread > 0 && totalSize > maxInputSizePerThread) {
        observedOversized++;
      }
      out.add(task);
    }
    if (observedOversized > 0) {
      LOG.warn(
          "Paimon table [{}] releases {} task(s) above maxInputSizePerThread={} bytes. "
              + "The packer already applied best-effort splitting; remaining oversized tasks are "
              + "treated as atomic Paimon compact units.",
          tableNameForLog,
          observedOversized,
          maxInputSizePerThread);
    }
    int deferredByCap = Math.max(0, tasks.size() - out.size());
    if (deferredByCap > 0) {
      LOG.info(
          "Paimon table [{}] plan-tick quota: released {} task(s), deferred {} to next tick "
              + "(cap={}, availableCore={}).",
          tableNameForLog,
          out.size(),
          deferredByCap,
          cap,
          availableCore);
    }
    return out;
  }

  /** Sum the file sizes of the {@code compactBefore} list of an {@link AppendCompactTask}. */
  static long totalCompactBeforeSize(AppendCompactTask task) {
    List<DataFileMeta> before = task.compactBefore();
    if (before == null || before.isEmpty()) {
      return 0L;
    }
    long sum = 0L;
    for (DataFileMeta meta : before) {
      sum += meta.fileSize();
    }
    return sum;
  }

  @Override
  public OptimizingType getOptimizingType() {
    return optimizingType;
  }

  @Override
  public long getProcessId() {
    return processId;
  }

  @Override
  public long getPlanTime() {
    return planTime;
  }

  @Override
  public long getTargetSnapshotId() {
    return targetSnapshotId;
  }

  @Override
  public long getTargetChangeSnapshotId() {
    // Paimon has no separate change snapshot concept.
    return -1L;
  }

  @Override
  public Map<String, Long> getFromSequence() {
    return Collections.emptyMap();
  }

  @Override
  public Map<String, Long> getToSequence() {
    return Collections.emptyMap();
  }

  private AppendOnlyFileStoreTable unwrapBucketUnawareTable() {
    Object raw = paimonTable.originalTable();
    if (!(raw instanceof AppendOnlyFileStoreTable)) {
      LOG.info(
          "Paimon table [{}] is not AppendOnly; skip (got {}).",
          paimonTable.id().getTableName(),
          raw == null ? "null" : raw.getClass().getSimpleName());
      return null;
    }
    AppendOnlyFileStoreTable table = (AppendOnlyFileStoreTable) raw;
    if (table.bucketMode() != org.apache.paimon.table.BucketMode.BUCKET_UNAWARE) {
      LOG.info(
          "Paimon table [{}] bucketMode={} is not BUCKET_UNAWARE; skip.",
          paimonTable.id().getTableName(),
          table.bucketMode());
      return null;
    }
    return table;
  }

  private OptimizingPlanResult<PaimonCompactionTask> emptyResult() {
    return new OptimizingPlanResult<>(
        processId,
        getOptimizingType(),
        planTime,
        targetSnapshotId,
        -1L,
        Collections.emptyList(),
        Collections.emptyMap(),
        Collections.emptyMap());
  }

  private static OptimizingType higherType(OptimizingType current, OptimizingType candidate) {
    if (candidate == null) {
      return current;
    }
    if (current == null || candidate == OptimizingType.FULL) {
      return candidate;
    }
    if (current == OptimizingType.MINOR && candidate == OptimizingType.MAJOR) {
      return candidate;
    }
    return current;
  }

  private static OptimizingType highestType(List<PlannedAppendTask> tasks) {
    OptimizingType type = null;
    for (PlannedAppendTask task : tasks) {
      type = higherType(type, task.optimizingType());
    }
    return type == null ? OptimizingType.MINOR : type;
  }

  private static final class PlannedAppendTask {
    private final AppendCompactTask task;
    private final OptimizingType optimizingType;

    private PlannedAppendTask(AppendCompactTask task, OptimizingType optimizingType) {
      this.task = task;
      this.optimizingType = optimizingType;
    }

    private AppendCompactTask task() {
      return task;
    }

    private OptimizingType optimizingType() {
      return optimizingType;
    }
  }
}
