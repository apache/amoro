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
 * <p>The {@code commitUser} returned via {@link #getCommitUser()} must be persisted by the caller
 * to {@code TableProcessStore.properties} (key {@code "paimon.commit.user"}) so that retries /
 * replays reuse the same value and benefit from Paimon's built-in idempotent commit deduplication.
 */
public class PaimonOptimizingPlanner implements TableOptimizingPlanner {

  private static final Logger LOG = LoggerFactory.getLogger(PaimonOptimizingPlanner.class);

  public static final String COMMIT_USER_PROPERTY = "paimon.commit.user";

  private final PaimonTable paimonTable;
  private final long tableId;
  private final long processId;
  private final long planTime;

  @SuppressWarnings("unused")
  private final double availableCore;

  @SuppressWarnings("unused")
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

    AppendCompactCoordinator coordinator =
        partitionFilter == null
            ? new AppendCompactCoordinator(table, /* streamingMode */ false)
            : new AppendCompactCoordinator(table, false, partitionFilter);
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

    List<PaimonCompactionTask> wrapped = new ArrayList<>(cachedTasks.size());
    for (AppendCompactTask task : cachedTasks) {
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
   * not yet been invoked or if no compaction was necessary. Callers should persist this to {@code
   * TableProcessStore.properties} under {@link #COMMIT_USER_PROPERTY} and pass the same value to
   * the Committer on retries.
   */
  public String getCommitUser() {
    return commitUser;
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
