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
import org.apache.paimon.Snapshot;
import org.apache.paimon.append.AppendCompactCoordinator;
import org.apache.paimon.append.AppendCompactTask;
import org.apache.paimon.io.DataFileMeta;
import org.apache.paimon.options.Options;
import org.apache.paimon.partition.PartitionPredicate;
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
 *   <li>{@link #isNecessary()} guards the BUCKET_UNAWARE / AppendOnly shape and probes for
 *       candidate small files by executing {@link AppendCompactCoordinator#run()} once; the probe
 *       result is cached and reused by {@link #plan()}.
 *   <li>{@link #plan()} wraps each {@link AppendCompactTask} into a {@link PaimonCompactionTask}
 *       carrying the serialized task bytes, the stable {@code commitUser} for this plan (a UUID
 *       generated via {@link CoreOptions#createCommitUser(org.apache.paimon.options.Options)}), the
 *       serializer version, and the target snapshot id.
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
  private final Predicate partitionFilter;

  // Memoised state built the first time isNecessary() / plan() runs.
  private Boolean necessary;
  private List<AppendCompactTask> cachedTasks;
  private String commitUser;
  private long targetSnapshotId = -1L;

  public PaimonOptimizingPlanner(
      PaimonTable paimonTable,
      long tableId,
      long processId,
      double availableCore,
      long maxInputSizePerThread) {
    this(paimonTable, tableId, processId, availableCore, maxInputSizePerThread, null);
  }

  public PaimonOptimizingPlanner(
      PaimonTable paimonTable,
      long tableId,
      long processId,
      double availableCore,
      long maxInputSizePerThread,
      Predicate partitionFilter) {
    this.paimonTable = paimonTable;
    this.tableId = tableId;
    this.processId = processId;
    this.planTime = System.currentTimeMillis();
    this.availableCore = availableCore;
    this.maxInputSizePerThread = maxInputSizePerThread;
    this.partitionFilter = partitionFilter;
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
    // AppendCompactCoordinator.run() throws when no snapshot exists; short-circuit here.
    if (table.snapshotManager().latestSnapshot() == null) {
      LOG.info(
          "Paimon table [{}] has no snapshot yet — skip optimizing.",
          paimonTable.id().getTableName());
      necessary = false;
      return false;
    }

    AppendCompactCoordinator coordinator;
    if (partitionFilter == null) {
      coordinator = new AppendCompactCoordinator(table, /* streamingMode */ false);
    } else {
      PartitionPredicate partitionPredicate =
          PartitionPredicate.fromPredicate(table.schema().logicalPartitionType(), partitionFilter);
      coordinator = new AppendCompactCoordinator(table, false, partitionPredicate);
    }
    List<AppendCompactTask> tasks = coordinator.run();
    cachedTasks = tasks;
    necessary = !tasks.isEmpty();
    if (!necessary) {
      LOG.info(
          "Paimon table [{}] has no candidate small files — skip optimizing.",
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
    Snapshot snapshot = table.snapshotManager().latestSnapshot();
    targetSnapshotId = snapshot == null ? -1L : snapshot.id();

    // Apply plan-tick quota: cap count to ceil(availableCore) and defer oversized tasks.
    // NOTE (§3.4 split contract): this does NOT re-split, merge, or otherwise mutate Paimon's
    // native AppendCompactTask — we only decide how many of the coordinator's tasks get
    // released this tick, and defer any single task whose total input size would violate
    // maxInputSizePerThread. The remainder will be re-produced by the next plan tick because
    // each Planner instance re-invokes AppendCompactCoordinator.run() in isNecessary().
    List<AppendCompactTask> eligible =
        applyQuota(
            cachedTasks,
            availableCore,
            maxInputSizePerThread,
            PaimonOptimizingPlanner::totalCompactBeforeSize,
            paimonTable.id().getTableName());

    // K2 self-honesty: if every coordinator task was deferred this tick, flip the cached
    // necessary flag back to false so a subsequent isNecessary() call (e.g. the AMS generic
    // guard or a re-entrant check) reports "nothing to do" and does not spin up an empty
    // TableOptimizingProcess. The next plan tick re-enters isNecessary() with a fresh
    // coordinator probe and will pick the task up again when it fits.
    if (eligible.isEmpty()) {
      LOG.info(
          "Paimon table [{}] plan tick produced 0 eligible tasks (all deferred) — "
              + "reset isNecessary() to false for this planner instance.",
          paimonTable.id().getTableName());
      necessary = false;
      return emptyResult();
    }

    List<PaimonCompactionTask> wrapped = new ArrayList<>(eligible.size());
    for (AppendCompactTask task : eligible) {
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
   * Apply the plan-tick quota to a list of {@link AppendCompactTask}s produced by {@link
   * AppendCompactCoordinator#run()}.
   *
   * <p>Two independent filters are applied:
   *
   * <ul>
   *   <li><b>Oversized-task deferral:</b> any task whose {@code compactBefore()} total size is
   *       strictly greater than {@code maxInputSizePerThread} is skipped (deferred) on this tick
   *       and an INFO log line is emitted. Re-planning on the next tick will re-evaluate.
   *   <li><b>Count cap:</b> at most {@code ceil(availableCore)} eligible tasks are released per
   *       tick; remaining tasks will be re-produced by the next {@code AppendCompactCoordinator
   *       .run()} invocation in the following plan tick.
   * </ul>
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
    if (tasks == null || tasks.isEmpty()) {
      return Collections.emptyList();
    }
    // ceil(availableCore), but guarded against non-positive core values — in those degenerate
    // cases we still allow at most one task through so progress is not completely stalled.
    int cap = Math.max(1, (int) Math.ceil(availableCore));
    List<AppendCompactTask> out = new ArrayList<>(Math.min(cap, tasks.size()));
    int skippedOversized = 0;
    for (AppendCompactTask task : tasks) {
      if (out.size() >= cap) {
        break;
      }
      long totalSize = sizeFn.applyAsLong(task);
      if (totalSize > maxInputSizePerThread) {
        skippedOversized++;
        continue;
      }
      out.add(task);
    }
    if (skippedOversized > 0) {
      boolean allDeferred = out.isEmpty() && skippedOversized == tasks.size();
      if (allDeferred) {
        LOG.warn(
            "Paimon table [{}] ALL {} tasks deferred as oversized — "
                + "every AppendCompactTask exceeds maxInputSizePerThread={} bytes. "
                + "Compaction is stalled; consider raising max-input-size-per-thread.",
            tableNameForLog,
            tasks.size(),
            maxInputSizePerThread);
      } else {
        LOG.info(
            "Paimon table [{}] deferred {}/{} tasks as oversized "
                + "(maxInputSizePerThread={} bytes).",
            tableNameForLog,
            skippedOversized,
            tasks.size(),
            maxInputSizePerThread);
      }
    }
    int deferredByCap = Math.max(0, tasks.size() - out.size() - skippedOversized);
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
    return OptimizingType.MINOR;
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
}
