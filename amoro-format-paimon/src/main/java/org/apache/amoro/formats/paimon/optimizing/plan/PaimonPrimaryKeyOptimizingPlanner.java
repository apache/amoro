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
import org.apache.amoro.formats.paimon.optimizing.primary.PaimonBucketCompactionUnit;
import org.apache.amoro.formats.paimon.optimizing.primary.PaimonPrimaryKeyCompactionExecutorFactory;
import org.apache.amoro.formats.paimon.optimizing.primary.PaimonPrimaryKeyCompactionInput;
import org.apache.amoro.formats.paimon.optimizing.primary.PaimonPrimaryKeyCompactionTask;
import org.apache.amoro.formats.paimon.optimizing.primary.PaimonPrimaryKeyOptions;
import org.apache.amoro.optimizing.OptimizingPlanResult;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.amoro.optimizing.TableOptimizingPlanner;
import org.apache.amoro.optimizing.TaskProperties;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.paimon.CoreOptions;
import org.apache.paimon.Snapshot;
import org.apache.paimon.data.BinaryRow;
import org.apache.paimon.manifest.BucketEntry;
import org.apache.paimon.manifest.PartitionEntry;
import org.apache.paimon.options.Options;
import org.apache.paimon.predicate.Predicate;
import org.apache.paimon.table.AppendOnlyFileStoreTable;
import org.apache.paimon.table.BucketMode;
import org.apache.paimon.table.FileStoreTable;
import org.apache.paimon.utils.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** {@link TableOptimizingPlanner} for Paimon primary-key HASH_FIXED/HASH_DYNAMIC tables. */
public class PaimonPrimaryKeyOptimizingPlanner implements TableOptimizingPlanner {

  private static final Logger LOG =
      LoggerFactory.getLogger(PaimonPrimaryKeyOptimizingPlanner.class);

  private static final String NUM_SORTED_RUN_COMPACTION_TRIGGER =
      "num-sorted-run.compaction-trigger";
  private static final String NUM_SORTED_RUN_STOP_TRIGGER = "num-sorted-run.stop-trigger";

  private final PaimonTable paimonTable;
  private final long tableId;
  private final long processId;
  private final long planTime;
  private final OptimizingConfig optimizingConfig;
  private final long lastMinorOptimizingTime;
  private final long lastFullOptimizingTime;
  private final Predicate partitionFilter;

  private Boolean necessary;
  private List<PaimonBucketCompactionUnit> cachedUnits;
  private OptimizingType optimizingType = OptimizingType.MINOR;
  private boolean fullCompaction;
  private long targetSnapshotId = -1L;
  private String commitUser;

  public static boolean supports(PaimonTable paimonTable) {
    if (paimonTable == null) {
      return false;
    }
    Object raw = paimonTable.originalTable();
    if (!(raw instanceof FileStoreTable) || raw instanceof AppendOnlyFileStoreTable) {
      return false;
    }
    FileStoreTable table = (FileStoreTable) raw;
    if (table.primaryKeys() == null || table.primaryKeys().isEmpty()) {
      return false;
    }
    if (table.bucketMode() != BucketMode.HASH_FIXED
        && table.bucketMode() != BucketMode.HASH_DYNAMIC) {
      return false;
    }
    return PaimonPrimaryKeyOptions.enabled(table.options());
  }

  public PaimonPrimaryKeyOptimizingPlanner(
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

  public PaimonPrimaryKeyOptimizingPlanner(
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
    this.optimizingConfig = optimizingConfig == null ? defaultOptimizingConfig() : optimizingConfig;
    this.lastMinorOptimizingTime = lastMinorOptimizingTime;
    this.lastFullOptimizingTime = lastFullOptimizingTime;
    this.partitionFilter = partitionFilter;
  }

  private static OptimizingConfig defaultOptimizingConfig() {
    return new OptimizingConfig()
        .setEnabled(true)
        .setMinorLeastFileCount(1)
        .setMinorLeastInterval(0)
        .setFullTriggerInterval(-1)
        .setFullRewriteAllFiles(false)
        .setMaxTaskSize(Long.MAX_VALUE);
  }

  @Override
  public boolean isNecessary() {
    return paimonTable.doAs(this::isNecessaryInternal);
  }

  private boolean isNecessaryInternal() {
    if (necessary != null) {
      return necessary;
    }
    FileStoreTable table = unwrapPrimaryKeyHashTable();
    if (table == null) {
      return cacheEmpty(false);
    }
    if (hasUnsupportedFilter()) {
      return cacheEmpty(false);
    }

    PaimonPrimaryKeyOptions primaryKeyOptions;
    try {
      primaryKeyOptions = PaimonPrimaryKeyOptions.from(table.options());
    } catch (RuntimeException e) {
      LOG.warn(
          "Paimon primary-key optimizing options are invalid for table [{}], skip planning.",
          paimonTable.id().getTableName(),
          e);
      return cacheEmpty(false);
    }
    if (!primaryKeyOptions.enabled()) {
      return cacheEmpty(false);
    }

    Optional<Snapshot> latestSnapshot = table.latestSnapshot();
    if (!latestSnapshot.isPresent()) {
      return cacheEmpty(false);
    }
    targetSnapshotId = latestSnapshot.get().id();

    CoreOptions coreOptions = CoreOptions.fromMap(table.options());
    int effectiveMinor = effectiveMinor(table, coreOptions);
    if (primaryKeyOptions.majorFileCountThreshold().isPresent()
        && primaryKeyOptions.majorFileCountThreshold().get() < effectiveMinor) {
      LOG.warn(
          "Paimon primary-key table [{}] has {}={} smaller than effective minor threshold {}, "
              + "skip planning.",
          paimonTable.id().getTableName(),
          PaimonPrimaryKeyOptions.MAJOR_FILE_COUNT_THRESHOLD,
          primaryKeyOptions.majorFileCountThreshold().get(),
          effectiveMinor);
      return cacheEmpty(false);
    }
    long effectiveMajor = effectiveMajor(table, coreOptions, primaryKeyOptions, effectiveMinor);

    List<PaimonBucketCompactionUnit> allUnits = bucketUnits(table);
    List<PaimonBucketCompactionUnit> minorCandidates = new ArrayList<>();
    List<PaimonBucketCompactionUnit> majorCandidates = new ArrayList<>();
    for (PaimonBucketCompactionUnit unit : allUnits) {
      if (unit.getFileCount() >= effectiveMinor) {
        minorCandidates.add(unit);
        if (unit.getFileCount() >= effectiveMajor) {
          majorCandidates.add(unit);
        }
      }
    }

    if (!majorCandidates.isEmpty()) {
      return cache(majorCandidates, OptimizingType.MAJOR, true);
    }
    if (!minorCandidates.isEmpty()) {
      if (reachMinorInterval()) {
        return cache(minorCandidates, OptimizingType.MINOR, false);
      }
      return cacheEmpty(false);
    }
    return planFullIfNeeded(table, allUnits, primaryKeyOptions);
  }

  @Override
  public OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> plan() {
    if (!isNecessary()) {
      return emptyResult();
    }
    FileStoreTable table = unwrapPrimaryKeyHashTable();
    if (table == null) {
      return emptyResult();
    }
    if (commitUser == null) {
      commitUser = CoreOptions.createCommitUser(Options.fromMap(table.options()));
    }

    PaimonPrimaryKeyOptions primaryKeyOptions;
    try {
      primaryKeyOptions = PaimonPrimaryKeyOptions.from(table.options());
    } catch (RuntimeException e) {
      LOG.warn(
          "Paimon primary-key optimizing options are invalid for table [{}], skip planning.",
          paimonTable.id().getTableName(),
          e);
      return emptyResult();
    }

    List<PaimonPrimaryKeyCompactionTask> tasks =
        packTasks(cachedUnits, primaryKeyOptions.maxBucketsPerTask());
    return new OptimizingPlanResult<>(
        processId,
        getOptimizingType(),
        planTime,
        targetSnapshotId,
        -1L,
        tasks,
        Collections.emptyMap(),
        Collections.emptyMap());
  }

  public String getCommitUser() {
    return commitUser;
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

  private boolean planFullIfNeeded(
      FileStoreTable table,
      List<PaimonBucketCompactionUnit> allUnits,
      PaimonPrimaryKeyOptions primaryKeyOptions) {
    int fullTriggerInterval = optimizingConfig.getFullTriggerInterval();
    if (fullTriggerInterval <= 0
        || planTime - lastFullOptimizingTime < fullTriggerInterval
        || allUnits.isEmpty()) {
      return cacheEmpty(false);
    }
    if (!primaryKeyOptions.partitionIdleTime().isPresent()) {
      LOG.warn(
          "Paimon primary-key table [{}] requires {} for FULL planning, skip planning.",
          paimonTable.id().getTableName(),
          PaimonPrimaryKeyOptions.PARTITION_IDLE_TIME);
      return cacheEmpty(false);
    }

    Duration idleTime = primaryKeyOptions.partitionIdleTime().get();
    List<PaimonBucketCompactionUnit> fullCandidates = idleUnits(table, allUnits, idleTime);
    if (fullCandidates.isEmpty()) {
      return cacheEmpty(false);
    }
    return cache(fullCandidates, OptimizingType.FULL, true);
  }

  private List<PaimonBucketCompactionUnit> idleUnits(
      FileStoreTable table, List<PaimonBucketCompactionUnit> units, Duration idleTime) {
    if (table.partitionKeys().isEmpty()) {
      return idleBucketUnits(units, idleTime);
    }
    return idlePartitionBucketUnits(table, units, idleTime);
  }

  private boolean reachMinorInterval() {
    return optimizingConfig.getMinorLeastInterval() >= 0
        && planTime - lastMinorOptimizingTime > optimizingConfig.getMinorLeastInterval();
  }

  private List<PaimonBucketCompactionUnit> idleBucketUnits(
      List<PaimonBucketCompactionUnit> units, Duration idleTime) {
    List<PaimonBucketCompactionUnit> idleUnits = new ArrayList<>();
    for (PaimonBucketCompactionUnit unit : units) {
      if (isIdle(unit.getLastFileCreationTime(), idleTime)) {
        idleUnits.add(unit);
      }
    }
    return idleUnits;
  }

  private List<PaimonBucketCompactionUnit> idlePartitionBucketUnits(
      FileStoreTable table, List<PaimonBucketCompactionUnit> units, Duration idleTime) {
    Set<ByteBuffer> idlePartitions = new HashSet<>();
    for (PartitionEntry entry : table.newSnapshotReader().partitionEntries()) {
      if (isIdle(entry.lastFileCreationTime(), idleTime)) {
        idlePartitions.add(partitionKey(entry.partition()));
      }
    }
    List<PaimonBucketCompactionUnit> idleUnits = new ArrayList<>();
    for (PaimonBucketCompactionUnit unit : units) {
      if (idlePartitions.contains(ByteBuffer.wrap(unit.getPartitionBytes()))) {
        idleUnits.add(unit);
      }
    }
    return idleUnits;
  }

  private boolean isIdle(long lastFileCreationTime, Duration idleTime) {
    long idleMillis = idleTime.toMillis();
    return idleMillis == 0 || planTime - lastFileCreationTime >= idleMillis;
  }

  private List<PaimonPrimaryKeyCompactionTask> packTasks(
      List<PaimonBucketCompactionUnit> units, int maxBucketsPerTask) {
    List<PaimonPrimaryKeyCompactionTask> tasks = new ArrayList<>();
    for (int offset = 0; offset < units.size(); offset += maxBucketsPerTask) {
      int end = Math.min(offset + maxBucketsPerTask, units.size());
      List<PaimonBucketCompactionUnit> taskUnits = new ArrayList<>(units.subList(offset, end));
      PaimonPrimaryKeyCompactionInput input =
          new PaimonPrimaryKeyCompactionInput(
              paimonTable,
              taskUnits,
              getOptimizingType(),
              fullCompaction,
              targetSnapshotId,
              commitUser,
              processId);
      Map<String, String> props = Maps.newHashMap();
      props.put(
          TaskProperties.TASK_EXECUTOR_FACTORY_IMPL,
          PaimonPrimaryKeyCompactionExecutorFactory.class.getName());
      tasks.add(
          PaimonPrimaryKeyCompactionTask.buildTask(tableId, "primary-key-buckets", input, props));
    }
    return tasks;
  }

  private List<PaimonBucketCompactionUnit> bucketUnits(FileStoreTable table) {
    List<PaimonBucketCompactionUnit> units = new ArrayList<>();
    for (BucketEntry entry : table.newSnapshotReader().bucketEntries()) {
      BinaryRow partition = entry.partition();
      units.add(
          new PaimonBucketCompactionUnit(
              SerializationUtils.serializeBinaryRow(partition),
              entry.bucket(),
              entry.fileCount(),
              entry.fileSizeInBytes(),
              entry.recordCount(),
              entry.lastFileCreationTime()));
    }
    return units;
  }

  private int effectiveMinor(FileStoreTable table, CoreOptions coreOptions) {
    int configured =
        table.options().containsKey(NUM_SORTED_RUN_COMPACTION_TRIGGER)
            ? coreOptions.numSortedRunCompactionTrigger()
            : optimizingConfig.getMinorLeastFileCount();
    return Math.max(1, configured);
  }

  private long effectiveMajor(
      FileStoreTable table,
      CoreOptions coreOptions,
      PaimonPrimaryKeyOptions primaryKeyOptions,
      int effectiveMinor) {
    if (primaryKeyOptions.majorFileCountThreshold().isPresent()) {
      return primaryKeyOptions.majorFileCountThreshold().get();
    }
    if (table.options().containsKey(NUM_SORTED_RUN_STOP_TRIGGER)) {
      return coreOptions.numSortedRunStopTrigger();
    }
    return effectiveMinor + 3L;
  }

  private FileStoreTable unwrapPrimaryKeyHashTable() {
    Object raw = paimonTable.originalTable();
    if (!(raw instanceof FileStoreTable)) {
      LOG.info(
          "Paimon table [{}] is not FileStoreTable; skip primary-key optimizing.",
          paimonTable.id().getTableName());
      return null;
    }
    if (raw instanceof AppendOnlyFileStoreTable) {
      LOG.info(
          "Paimon table [{}] is append-only; skip primary-key optimizing.",
          paimonTable.id().getTableName());
      return null;
    }
    FileStoreTable table = (FileStoreTable) raw;
    if (table.primaryKeys() == null || table.primaryKeys().isEmpty()) {
      LOG.info(
          "Paimon table [{}] does not have primary key; skip primary-key optimizing.",
          paimonTable.id().getTableName());
      return null;
    }
    if (table.bucketMode() != BucketMode.HASH_FIXED
        && table.bucketMode() != BucketMode.HASH_DYNAMIC) {
      LOG.info(
          "Paimon table [{}] bucketMode={} is not HASH_FIXED/HASH_DYNAMIC; skip.",
          paimonTable.id().getTableName(),
          table.bucketMode());
      return null;
    }
    return table;
  }

  private boolean hasUnsupportedFilter() {
    if (optimizingConfig.getFilter() != null && !optimizingConfig.getFilter().trim().isEmpty()) {
      LOG.warn(
          "Paimon primary-key table [{}] does not support self-optimizing.filter yet, skip "
              + "planning.",
          paimonTable.id().getTableName());
      return true;
    }
    if (partitionFilter != null) {
      LOG.warn(
          "Paimon primary-key table [{}] does not support partition filter yet, skip planning.",
          paimonTable.id().getTableName());
      return true;
    }
    return false;
  }

  private boolean cache(
      List<PaimonBucketCompactionUnit> units, OptimizingType type, boolean fullCompaction) {
    this.cachedUnits = units;
    this.optimizingType = type;
    this.fullCompaction = fullCompaction;
    this.necessary = !units.isEmpty();
    return this.necessary;
  }

  private boolean cacheEmpty(boolean fullCompaction) {
    this.cachedUnits = Collections.emptyList();
    this.fullCompaction = fullCompaction;
    this.necessary = false;
    return false;
  }

  private OptimizingPlanResult<PaimonPrimaryKeyCompactionTask> emptyResult() {
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

  private ByteBuffer partitionKey(BinaryRow partition) {
    return ByteBuffer.wrap(SerializationUtils.serializeBinaryRow(partition));
  }
}
