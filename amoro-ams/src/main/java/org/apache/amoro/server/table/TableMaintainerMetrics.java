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

import static org.apache.amoro.metrics.MetricDefine.defineCounter;
import static org.apache.amoro.metrics.MetricDefine.defineGauge;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.maintainer.MaintainerMetrics;
import org.apache.amoro.maintainer.MaintainerOperationType;
import org.apache.amoro.metrics.Counter;
import org.apache.amoro.metrics.Gauge;
import org.apache.amoro.metrics.Metric;
import org.apache.amoro.metrics.MetricDefine;
import org.apache.amoro.metrics.MetricKey;
import org.apache.amoro.metrics.MetricRegistry;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Table maintenance operation metrics implementation.
 *
 * <p>This class handles metrics recording for table maintenance operations such as orphan file
 * cleaning, snapshot expiration, data expiration, and tag creation.
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
public class TableMaintainerMetrics implements MaintainerMetrics {

  // ========== Orphan Files Related MetricDefine ==========

  /**
   * Count of orphan content files cleaned.
   *
   * <p>Note: This metric name is retained for backward compatibility. The "content" terminology
   * refers to data files (as opposed to metadata files).
   */
  public static final MetricDefine TABLE_ORPHAN_CONTENT_FILE_CLEANING_COUNT =
      defineCounter("table_orphan_content_file_cleaning_count")
          .withDescription("Count of orphan content files cleaned")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  /**
   * Expected count of orphan content files to clean.
   *
   * <p>Note: This metric name is retained for backward compatibility. The "content" terminology
   * refers to data files (as opposed to metadata files).
   */
  public static final MetricDefine TABLE_EXPECTED_ORPHAN_CONTENT_FILE_CLEANING_COUNT =
      defineCounter("table_expected_orphan_content_file_cleaning_count")
          .withDescription("Expected count of orphan content files to clean")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  /** Count of orphan metadata files cleaned */
  public static final MetricDefine TABLE_ORPHAN_METADATA_FILES_CLEANED_COUNT =
      defineCounter("table_orphan_metadata_files_cleaned_count")
          .withDescription("Count of orphan metadata files cleaned")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  /** Expected count of orphan metadata files to clean */
  public static final MetricDefine TABLE_ORPHAN_METADATA_FILES_CLEANED_EXPECTED_COUNT =
      defineCounter("table_orphan_metadata_files_cleaned_expected_count")
          .withDescription("Expected count of orphan metadata files to clean")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  /** Duration of orphan files cleaning operation (milliseconds) */
  public static final MetricDefine TABLE_ORPHAN_FILES_CLEANING_DURATION =
      defineGauge("table_orphan_files_cleaning_duration_millis")
          .withDescription("Duration of orphan files cleaning operation in milliseconds")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  // ========== Dangling Delete Files Related MetricDefine (Iceberg) ==========

  /** Count of dangling delete files cleaned */
  public static final MetricDefine TABLE_DANGLING_DELETE_FILES_CLEANED_COUNT =
      defineCounter("table_dangling_delete_files_cleaned_count")
          .withDescription("Count of dangling delete files cleaned")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  /** Duration of dangling delete files cleaning operation (milliseconds) */
  public static final MetricDefine TABLE_DANGLING_DELETE_FILES_CLEANING_DURATION =
      defineGauge("table_dangling_delete_files_cleaning_duration_millis")
          .withDescription("Duration of dangling delete files cleaning operation in milliseconds")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  // ========== Snapshot Expiration Related MetricDefine ==========

  /** Count of snapshots expired */
  public static final MetricDefine TABLE_SNAPSHOTS_EXPIRED_COUNT =
      defineCounter("table_snapshots_expired_count")
          .withDescription("Count of snapshots expired")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  /** Count of data files deleted during snapshot expiration */
  public static final MetricDefine TABLE_SNAPSHOTS_EXPIRED_DATA_FILES_DELETED =
      defineCounter("table_snapshots_expired_data_files_deleted")
          .withDescription("Count of data files deleted during snapshot expiration")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  /** Duration of snapshot expiration operation (milliseconds) */
  public static final MetricDefine TABLE_SNAPSHOTS_EXPIRATION_DURATION =
      defineGauge("table_snapshots_expiration_duration_millis")
          .withDescription("Duration of snapshot expiration operation in milliseconds")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  // ========== Data Expiration Related MetricDefine (Iceberg) ==========

  /** Count of data files expired */
  public static final MetricDefine TABLE_DATA_EXPIRED_DATA_FILES_COUNT =
      defineCounter("table_data_expired_data_files_count")
          .withDescription("Count of data files expired")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  /** Count of delete files expired */
  public static final MetricDefine TABLE_DATA_EXPIRED_DELETE_FILES_COUNT =
      defineCounter("table_data_expired_delete_files_count")
          .withDescription("Count of delete files expired")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  /** Duration of data expiration operation (milliseconds) */
  public static final MetricDefine TABLE_DATA_EXPIRATION_DURATION =
      defineGauge("table_data_expiration_duration_millis")
          .withDescription("Duration of data expiration operation in milliseconds")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  // ========== Tag Creation Related MetricDefine (Iceberg) ==========

  /** Count of tags created */
  public static final MetricDefine TABLE_TAGS_CREATED_COUNT =
      defineCounter("table_tags_created_count")
          .withDescription("Count of tags created")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  /** Duration of tag creation operation (milliseconds) */
  public static final MetricDefine TABLE_TAG_CREATION_DURATION =
      defineGauge("table_tag_creation_duration_millis")
          .withDescription("Duration of tag creation operation in milliseconds")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  // ========== Partition Expiration Related MetricDefine (Paimon) ==========

  /** Count of partitions expired */
  public static final MetricDefine TABLE_PARTITIONS_EXPIRED_COUNT =
      defineCounter("table_partitions_expired_count")
          .withDescription("Count of partitions expired")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  /** Count of files expired during partition expiration */
  public static final MetricDefine TABLE_PARTITIONS_EXPIRED_FILES_COUNT =
      defineCounter("table_partitions_expired_files_count")
          .withDescription("Count of files expired during partition expiration")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  /** Duration of partition expiration operation (milliseconds) */
  public static final MetricDefine TABLE_PARTITION_EXPIRATION_DURATION =
      defineGauge("table_partition_expiration_duration_millis")
          .withDescription("Duration of partition expiration operation in milliseconds")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  // ========== General Operation Status Related MetricDefine ==========

  /** Count of successful maintainer operations */
  public static final MetricDefine TABLE_MAINTAINER_OPERATION_SUCCESS_COUNT =
      defineCounter("table_maintainer_operation_success_count")
          .withDescription("Count of successful maintainer operations")
          .withTags("catalog", "database", "table", "table_format", "operation_type")
          .build();

  /** Count of failed maintainer operations */
  public static final MetricDefine TABLE_MAINTAINER_OPERATION_FAILURE_COUNT =
      defineCounter("table_maintainer_operation_failure_count")
          .withDescription("Count of failed maintainer operations")
          .withTags("catalog", "database", "table", "table_format", "operation_type")
          .build();

  /** Duration of maintainer operation (milliseconds) */
  public static final MetricDefine TABLE_MAINTAINER_OPERATION_DURATION =
      defineGauge("table_maintainer_operation_duration_millis")
          .withDescription("Duration of maintainer operation in milliseconds")
          .withTags("catalog", "database", "table", "table_format", "operation_type")
          .build();

  // ========== Instance Fields ==========

  private final ServerTableIdentifier identifier;
  private final String tableFormat;
  private final List<MetricKey> registeredMetricKeys = Lists.newArrayList();
  private MetricRegistry globalRegistry;

  // ========== Orphan Files Metrics ==========
  private final Counter orphanContentFileCleaningCount = new Counter();
  private final Counter expectedOrphanContentFileCleaningCount = new Counter();
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
  public TableMaintainerMetrics(ServerTableIdentifier identifier) {
    this.identifier = identifier;
    this.tableFormat = identifier.getFormat().name().toLowerCase();
    // Initialize operation type counters and gauges
    for (MaintainerOperationType type : MaintainerOperationType.values()) {
      successCounters.put(type, new Counter());
      failureCounters.put(type, new Counter());
      durationGauges.put(type, new OperationDurationGauge());
    }
  }

  /**
   * Get the table identifier.
   *
   * @return ServerTableIdentifier
   */
  public ServerTableIdentifier getIdentifier() {
    return identifier;
  }

  public void register(MetricRegistry registry) {
    if (globalRegistry != null) {
      return;
    }
    registerMetrics(registry);
    globalRegistry = registry;
  }

  public void unregister() {
    if (globalRegistry != null) {
      registeredMetricKeys.forEach(globalRegistry::unregister);
      registeredMetricKeys.clear();
      globalRegistry = null;
    }
  }

  private void registerMetrics(MetricRegistry registry) {
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
        registry,
        TABLE_ORPHAN_CONTENT_FILE_CLEANING_COUNT,
        orphanContentFileCleaningCount,
        baseTags);
    registerMetricWithTags(
        registry,
        TABLE_EXPECTED_ORPHAN_CONTENT_FILE_CLEANING_COUNT,
        expectedOrphanContentFileCleaningCount,
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
    expectedOrphanContentFileCleaningCount.inc(expected);
    orphanContentFileCleaningCount.inc(cleaned);
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
