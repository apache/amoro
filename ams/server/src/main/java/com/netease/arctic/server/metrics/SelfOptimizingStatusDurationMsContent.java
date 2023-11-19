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

package com.netease.arctic.server.metrics;

import com.codahale.metrics.Counter;
import com.netease.arctic.ams.api.metrics.MetricType;
import com.netease.arctic.ams.api.metrics.MetricsContent;
import com.netease.arctic.ams.api.metrics.TaggedMetrics;
import org.apache.iceberg.relocated.com.google.common.annotations.VisibleForTesting;
import org.apache.iceberg.relocated.com.google.common.base.MoreObjects;

public class SelfOptimizingStatusDurationMsContent
    implements MetricsContent<SelfOptimizingStatusDurationMsContent> {
  public static final String SELF_OPTIMIZING_STATUS_DURATION_MS_REPORT_NAME =
      "self_optimizing_status_duration_ms";

  public static final String TABLE_NAME = "table-name";
  public static final String OPTIMIZING_STATUS = "optimizing-status";
  public static final String OPTIMIZING_PROCESS_ID = "optimizing-process-id";
  public static final String OPTIMIZING_TYPE = "optimizing-type";
  public static final String TARGET_SNAPSHOT_ID = "target-snapshot-id";

  @VisibleForTesting
  public static final String TABLE_OPTIMIZING_STATUS_DURATION_MS =
      "table-optimizing-status-duration-ms";

  private final String tableName;
  private final String optimizingStatus;
  private Long optimizingProcessId;
  private String optimizingType;
  private Long targetSnapshotId;

  private final Counter tableOptimizingStatusDurationMs = new Counter();

  public SelfOptimizingStatusDurationMsContent(String tableName, String optimizingStatus) {
    this.tableName = tableName;
    this.optimizingStatus = optimizingStatus;
  }

  public void setOptimizingProcessId(Long optimizingProcessId) {
    this.optimizingProcessId = optimizingProcessId;
  }

  public void setOptimizingType(String optimizingType) {
    this.optimizingType = optimizingType;
  }

  public void setTargetSnapshotId(Long targetSnapshotId) {
    this.targetSnapshotId = targetSnapshotId;
  }

  @TaggedMetrics.Tag(name = TABLE_NAME)
  public String tableName() {
    return tableName;
  }

  @TaggedMetrics.Tag(name = OPTIMIZING_STATUS)
  public String optimizingStatus() {
    return optimizingStatus;
  }

  @TaggedMetrics.Tag(name = OPTIMIZING_PROCESS_ID)
  public Long optimizingProcessId() {
    return optimizingProcessId;
  }

  @TaggedMetrics.Tag(name = OPTIMIZING_TYPE)
  public String optimizingType() {
    return optimizingType;
  }

  @TaggedMetrics.Tag(name = TARGET_SNAPSHOT_ID)
  public Long targetSnapshotId() {
    return targetSnapshotId;
  }

  @TaggedMetrics.Metric(name = TABLE_OPTIMIZING_STATUS_DURATION_MS)
  public Counter tableOptimizingStatusDurationMs() {
    return tableOptimizingStatusDurationMs;
  }

  @Override
  public String name() {
    return SELF_OPTIMIZING_STATUS_DURATION_MS_REPORT_NAME;
  }

  @Override
  public MetricType type() {
    return MetricType.SERVICE;
  }

  @Override
  public SelfOptimizingStatusDurationMsContent data() {
    return this;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add(TABLE_NAME, tableName)
        .add(OPTIMIZING_STATUS, optimizingStatus)
        .add(OPTIMIZING_PROCESS_ID, optimizingProcessId)
        .add(OPTIMIZING_TYPE, optimizingType)
        .add(TARGET_SNAPSHOT_ID, targetSnapshotId)
        .add(TABLE_OPTIMIZING_STATUS_DURATION_MS, tableOptimizingStatusDurationMs.getCount())
        .toString();
  }
}
