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
import org.apache.amoro.maintainer.MaintainerMetrics.CleanFailureReason;
import org.apache.amoro.metrics.Counter;
import org.apache.amoro.metrics.Gauge;
import org.apache.amoro.metrics.MetricDefine;
import org.apache.amoro.metrics.MetricRegistry;

/** Table Orphan Files Cleaning metrics. */
public class TableOrphanFilesCleaningMetrics extends AbstractTableMetrics
    implements MaintainerMetrics {
  private final Counter orphanDataFilesCount = new Counter();
  private final Counter expectedOrphanDataFilesCount = new Counter();

  private final Counter orphanMetadataFilesCount = new Counter();
  private final Counter expectedOrphanMetadataFilesCount = new Counter();

  // ---- last-value gauges ----
  private volatile int lastStatus = STATUS_SUCCESS;
  private volatile long lastFailureTimestampMs = 0L;

  // --- status constants ---
  public static final int STATUS_SUCCESS = 0;

  public TableOrphanFilesCleaningMetrics(ServerTableIdentifier identifier) {
    super(identifier);
  }

  public static final MetricDefine TABLE_ORPHAN_CONTENT_FILE_CLEANING_COUNT =
      defineCounter("table_orphan_content_file_cleaning_count")
          .withDescription("Count of orphan content files cleaned in the table since ams started")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_ORPHAN_METADATA_FILE_CLEANING_COUNT =
      defineCounter("table_orphan_metadata_file_cleaning_count")
          .withDescription("Count of orphan metadata files cleaned in the table since ams started")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_EXPECTED_ORPHAN_CONTENT_FILE_CLEANING_COUNT =
      defineCounter("table_expected_orphan_content_file_cleaning_count")
          .withDescription(
              "Expected count of orphan content files cleaned in the table since ams started")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_EXPECTED_ORPHAN_METADATA_FILE_CLEANING_COUNT =
      defineCounter("table_expected_orphan_metadata_file_cleaning_count")
          .withDescription(
              "Expected count of orphan metadata files cleaned in the table since ams started")
          .withTags("catalog", "database", "table")
          .build();

  // ---- new orphan-file-cleaning status metrics ----

  public static final MetricDefine TABLE_ORPHAN_FILE_CLEANING_LAST_STATUS =
      defineGauge("table_orphan_file_cleaning_last_status")
          .withDescription(
              "Status of the most recent orphan-file-cleaning attempt; "
                  + "see MaintainerMetrics.CleanFailureReason: "
                  + "0=SUCCESS, 1=LOCATION_CONFLICT, 2=LOCATION_CONFLICT_CHECK_FAILED, "
                  + "3=EXECUTION_FAILED")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_ORPHAN_FILE_CLEANING_LAST_FAILURE_TIMESTAMP_MS =
      defineGauge("table_orphan_file_cleaning_last_failure_timestamp_ms")
          .withDescription("Epoch millis of the last real orphan-file-cleaning failure")
          .withTags("catalog", "database", "table")
          .build();

  @Override
  public void registerMetrics(MetricRegistry registry) {
    if (globalRegistry == null) {
      registerMetric(registry, TABLE_ORPHAN_CONTENT_FILE_CLEANING_COUNT, orphanDataFilesCount);
      registerMetric(registry, TABLE_ORPHAN_METADATA_FILE_CLEANING_COUNT, orphanMetadataFilesCount);
      registerMetric(
          registry,
          TABLE_EXPECTED_ORPHAN_CONTENT_FILE_CLEANING_COUNT,
          expectedOrphanDataFilesCount);
      registerMetric(
          registry,
          TABLE_EXPECTED_ORPHAN_METADATA_FILE_CLEANING_COUNT,
          expectedOrphanMetadataFilesCount);

      // new gauges
      registerMetric(
          registry, TABLE_ORPHAN_FILE_CLEANING_LAST_STATUS, (Gauge<Integer>) () -> lastStatus);
      registerMetric(
          registry,
          TABLE_ORPHAN_FILE_CLEANING_LAST_FAILURE_TIMESTAMP_MS,
          (Gauge<Long>) () -> lastFailureTimestampMs);

      globalRegistry = registry;
    }
  }

  public void completeOrphanDataFiles(int expected, int cleaned) {
    expectedOrphanDataFilesCount.inc(expected);
    orphanDataFilesCount.inc(cleaned);
  }

  public void completeOrphanMetadataFiles(int expected, int cleaned) {
    expectedOrphanMetadataFilesCount.inc(expected);
    orphanMetadataFilesCount.inc(cleaned);
  }

  @Override
  public void recordOrphanDataFilesCleaned(int expected, int cleaned) {
    completeOrphanDataFiles(expected, cleaned);
  }

  @Override
  public void recordOrphanMetadataFilesCleaned(int expected, int cleaned) {
    completeOrphanMetadataFiles(expected, cleaned);
  }

  // ---- public mutation API ----

  /**
   * Record a successful orphan-file-cleaning run. Resets {@code last_status} to {@link
   * #STATUS_SUCCESS}.
   *
   * <p>Note: {@code lastFailureTimestampMs} is intentionally preserved across success runs. It
   * tracks the time of the most recent real failure and is needed by monitoring to alert on
   * stale-failure windows. Only an actual failure (via {@link #recordFailure(CleanFailureReason)})
   * refreshes it; a success run is independent.
   */
  @Override
  public void recordSuccess() {
    this.lastStatus = STATUS_SUCCESS;
  }

  /**
   * Record a failure event. Updates {@code last_status} to the failure reason and refreshes {@code
   * lastFailureTimestampMs}.
   */
  @Override
  public void recordFailure(CleanFailureReason reason) {
    this.lastStatus = reason.statusCode();
    this.lastFailureTimestampMs = System.currentTimeMillis();
  }

  // ---- package-private / test-visible accessors ----

  int getLastStatus() {
    return lastStatus;
  }

  long getLastFailureTimestampMs() {
    return lastFailureTimestampMs;
  }
}
