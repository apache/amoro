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

package org.apache.amoro.server.table;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.maintainer.MaintainerOperationType;
import org.apache.amoro.metrics.Counter;
import org.apache.amoro.metrics.Gauge;
import org.apache.amoro.metrics.Metric;
import org.apache.amoro.metrics.MetricDefine;
import org.apache.amoro.metrics.MetricKey;
import org.apache.amoro.metrics.MetricRegistry;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Concrete implementation of table maintenance operation metrics
 *
 * <p>Design notes:
 *
 * <ul>
 *   <li>Each maintenance operation corresponds to a set of metrics (Counter + Gauge)
 *   <li>Counter is used for cumulative counting (e.g., file count)
 *   <li>Gauge is used to record current state (e.g., execution duration)
 *   <li>Supports thread safety in concurrent scenarios
 *   <li>Includes table_format tag to distinguish Iceberg and Paimon tables
 * </ul>
 */
public class TableMaintainerMetricsImpl extends AbstractTableMaintainerMetrics {

  // ========== Orphan Files Metrics ==========
  private final Counter orphanDataFilesCount = new Counter();
  private final Counter orphanDataFilesExpectedCount = new Counter();
  private final Counter orphanMetadataFilesCount = new Counter();
  private final Counter orphanMetadataFilesExpectedCount = new Counter();
  private final LastOperationDurationGauge orphanFilesCleaningDuration =
      new LastOperationDurationGauge();

  // ========== Dangling Delete Files Metrics ==========
  private final Counter danglingDeleteFilesCount = new Counter();
  private final LastOperationDurationGauge danglingDeleteFilesCleaningDuration =
      new LastOperationDurationGauge();

  // ========== Snapshot Expiration Metrics ==========
  private final Counter snapshotsExpiredCount = new Counter();
  private final Counter snapshotsExpiredDataFilesDeleted = new Counter();
  private final LastOperationDurationGauge snapshotsExpirationDuration =
      new LastOperationDurationGauge();

  // ========== Data Expiration Metrics ==========
  private final Counter dataExpiredDataFilesCount = new Counter();
  private final Counter dataExpiredDeleteFilesCount = new Counter();
  private final LastOperationDurationGauge dataExpirationDuration =
      new LastOperationDurationGauge();

  // ========== Tag Creation Metrics ==========
  private final Counter tagsCreatedCount = new Counter();
  private final LastOperationDurationGauge tagCreationDuration = new LastOperationDurationGauge();

  // ========== Partition Expiration Metrics ==========
  private final Counter partitionsExpiredCount = new Counter();
  private final Counter partitionsExpiredFilesCount = new Counter();
  private final LastOperationDurationGauge partitionExpirationDuration =
      new LastOperationDurationGauge();

  // ========== Operation Status Metrics ==========
  private final ConcurrentHashMap<MaintainerOperationType, Counter> successCounters =
      new ConcurrentHashMap<>();
  private final ConcurrentHashMap<MaintainerOperationType, Counter> failureCounters =
      new ConcurrentHashMap<>();
  private final ConcurrentHashMap<MaintainerOperationType, OperationDurationGauge> durationGauges =
      new ConcurrentHashMap<>();

  /**
   * Constructor
   *
   * @param identifier Table identifier (contains format information via getFormat())
   */
  public TableMaintainerMetricsImpl(ServerTableIdentifier identifier) {
    super(identifier);
    // Initialize operation type counters and gauges
    for (MaintainerOperationType type : MaintainerOperationType.values()) {
      successCounters.put(type, new Counter());
      failureCounters.put(type, new Counter());
      durationGauges.put(type, new OperationDurationGauge());
    }
  }

  @Override
  public void registerMetrics(MetricRegistry registry) {
    if (globalRegistry != null) {
      return;
    }

    // Build base tags (including table_format)
    Map<String, String> baseTags =
        ImmutableMap.of(
            "catalog",
            identifier.getCatalog(),
            "database",
            identifier.getDatabase(),
            "table",
            identifier.getTableName(),
            "table_format",
            tableFormat);

    // Orphan files
    registerMetricWithTags(
        registry, TABLE_ORPHAN_DATA_FILES_CLEANED_COUNT, orphanDataFilesCount, baseTags);
    registerMetricWithTags(
        registry,
        TABLE_ORPHAN_DATA_FILES_CLEANED_EXPECTED_COUNT,
        orphanDataFilesExpectedCount,
        baseTags);
    registerMetricWithTags(
        registry, TABLE_ORPHAN_METADATA_FILES_CLEANED_COUNT, orphanMetadataFilesCount, baseTags);
    registerMetricWithTags(
        registry,
        TABLE_ORPHAN_METADATA_FILES_CLEANED_EXPECTED_COUNT,
        orphanMetadataFilesExpectedCount,
        baseTags);
    registerMetricWithTags(
        registry, TABLE_ORPHAN_FILES_CLEANING_DURATION, orphanFilesCleaningDuration, baseTags);

    // Dangling delete files
    registerMetricWithTags(
        registry, TABLE_DANGLING_DELETE_FILES_CLEANED_COUNT, danglingDeleteFilesCount, baseTags);
    registerMetricWithTags(
        registry,
        TABLE_DANGLING_DELETE_FILES_CLEANING_DURATION,
        danglingDeleteFilesCleaningDuration,
        baseTags);

    // Snapshot expiration
    registerMetricWithTags(
        registry, TABLE_SNAPSHOTS_EXPIRED_COUNT, snapshotsExpiredCount, baseTags);
    registerMetricWithTags(
        registry,
        TABLE_SNAPSHOTS_EXPIRED_DATA_FILES_DELETED,
        snapshotsExpiredDataFilesDeleted,
        baseTags);
    registerMetricWithTags(
        registry, TABLE_SNAPSHOTS_EXPIRATION_DURATION, snapshotsExpirationDuration, baseTags);

    // Data expiration
    registerMetricWithTags(
        registry, TABLE_DATA_EXPIRED_DATA_FILES_COUNT, dataExpiredDataFilesCount, baseTags);
    registerMetricWithTags(
        registry, TABLE_DATA_EXPIRED_DELETE_FILES_COUNT, dataExpiredDeleteFilesCount, baseTags);
    registerMetricWithTags(
        registry, TABLE_DATA_EXPIRATION_DURATION, dataExpirationDuration, baseTags);

    // Tag creation
    registerMetricWithTags(registry, TABLE_TAGS_CREATED_COUNT, tagsCreatedCount, baseTags);
    registerMetricWithTags(registry, TABLE_TAG_CREATION_DURATION, tagCreationDuration, baseTags);

    // Partition expiration
    registerMetricWithTags(
        registry, TABLE_PARTITIONS_EXPIRED_COUNT, partitionsExpiredCount, baseTags);
    registerMetricWithTags(
        registry, TABLE_PARTITIONS_EXPIRED_FILES_COUNT, partitionsExpiredFilesCount, baseTags);
    registerMetricWithTags(
        registry, TABLE_PARTITION_EXPIRATION_DURATION, partitionExpirationDuration, baseTags);

    // Operation status (needs to include operation_type tag)
    for (MaintainerOperationType type : MaintainerOperationType.values()) {
      Map<String, String> operationTags =
          ImmutableMap.<String, String>builder()
              .putAll(baseTags)
              .put("operation_type", type.getMetricName())
              .build();
      registerMetricWithTags(
          registry,
          TABLE_MAINTAINER_OPERATION_SUCCESS_COUNT,
          successCounters.get(type),
          operationTags);
      registerMetricWithTags(
          registry,
          TABLE_MAINTAINER_OPERATION_FAILURE_COUNT,
          failureCounters.get(type),
          operationTags);
      registerMetricWithTags(
          registry, TABLE_MAINTAINER_OPERATION_DURATION, durationGauges.get(type), operationTags);
    }

    globalRegistry = registry;
  }

  /**
   * Register metric with specified tags
   *
   * @param registry MetricRegistry
   * @param define MetricDefine
   * @param metric Metric instance
   * @param tags Tags
   */
  private void registerMetricWithTags(
      MetricRegistry registry, MetricDefine define, Metric metric, Map<String, String> tags) {
    MetricKey key = registry.register(define, tags, metric);
    registeredMetricKeys.add(key);
  }

  // ========== MaintainerMetrics Interface Implementation ==========

  @Override
  public void recordOrphanDataFilesCleaned(int expected, int cleaned) {
    orphanDataFilesExpectedCount.inc(expected);
    orphanDataFilesCount.inc(cleaned);
  }

  @Override
  public void recordOrphanMetadataFilesCleaned(int expected, int cleaned) {
    orphanMetadataFilesExpectedCount.inc(expected);
    orphanMetadataFilesCount.inc(cleaned);
  }

  @Override
  public void recordDanglingDeleteFilesCleaned(int cleaned) {
    danglingDeleteFilesCount.inc(cleaned);
  }

  @Override
  public void recordSnapshotsExpired(int snapshotCount, int dataFilesDeleted, long durationMillis) {
    snapshotsExpiredCount.inc(snapshotCount);
    snapshotsExpiredDataFilesDeleted.inc(dataFilesDeleted);
    snapshotsExpirationDuration.setValue(durationMillis);
  }

  @Override
  public void recordDataExpired(int dataFilesExpired, int deleteFilesExpired, long durationMillis) {
    dataExpiredDataFilesCount.inc(dataFilesExpired);
    dataExpiredDeleteFilesCount.inc(deleteFilesExpired);
    dataExpirationDuration.setValue(durationMillis);
  }

  @Override
  public void recordTagsCreated(int tagsCreated, long durationMillis) {
    tagsCreatedCount.inc(tagsCreated);
    tagCreationDuration.setValue(durationMillis);
  }

  @Override
  public void recordPartitionsExpired(
      int partitionsExpired, int filesExpired, long durationMillis) {
    partitionsExpiredCount.inc(partitionsExpired);
    partitionsExpiredFilesCount.inc(filesExpired);
    partitionExpirationDuration.setValue(durationMillis);
  }

  @Override
  public void recordOperationStart(MaintainerOperationType operationType) {
    durationGauges.get(operationType).recordStart();
  }

  @Override
  public void recordOperationSuccess(MaintainerOperationType operationType, long durationMillis) {
    successCounters.get(operationType).inc();
    durationGauges.get(operationType).setValue(durationMillis);
  }

  @Override
  public void recordOperationFailure(
      MaintainerOperationType operationType, long durationMillis, Throwable throwable) {
    failureCounters.get(operationType).inc();
    durationGauges.get(operationType).setValue(durationMillis);
  }

  // ========== Internal Helper Classes ==========

  /** Gauge implementation for recording last operation duration */
  private static class LastOperationDurationGauge implements Gauge<Long> {
    private volatile long value = 0L;

    public void setValue(long value) {
      this.value = value;
    }

    @Override
    public Long getValue() {
      return value;
    }
  }

  /** Gauge implementation for recording operation duration (supports recording start time) */
  private static class OperationDurationGauge implements Gauge<Long> {
    private volatile long value = 0L;
    private volatile long startTime = 0L;

    public void recordStart() {
      this.startTime = System.currentTimeMillis();
    }

    public void setValue(long value) {
      this.value = value;
    }

    @Override
    public Long getValue() {
      return value;
    }
  }
}
