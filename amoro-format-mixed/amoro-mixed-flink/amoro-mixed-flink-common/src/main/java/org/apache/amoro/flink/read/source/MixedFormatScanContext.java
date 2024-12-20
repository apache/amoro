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

package org.apache.amoro.flink.read.source;

import static org.apache.iceberg.TableProperties.DEFAULT_NAME_MAPPING;

import org.apache.amoro.flink.table.descriptors.MixedFormatValidator;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Preconditions;
import org.apache.flink.util.TimeUtils;
import org.apache.iceberg.Schema;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.flink.FlinkConfigOptions;
import org.apache.iceberg.flink.FlinkReadOptions;
import org.apache.iceberg.flink.source.ScanContext;
import org.apache.iceberg.flink.source.StreamingStartingStrategy;

import java.io.Serializable;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/** This is an mixed-format source scan context. */
public class MixedFormatScanContext extends ScanContext implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String scanStartupMode;
  private final boolean batchMode;

  protected MixedFormatScanContext(Builder builder) {
    super(
        builder.caseSensitive,
        builder.snapshotId,
        builder.startingStrategy,
        builder.startSnapshotTimestamp,
        builder.startSnapshotId,
        builder.endSnapshotId,
        builder.asOfTimestamp,
        builder.splitSize,
        builder.splitLookback,
        builder.splitOpenFileCost,
        builder.isStreaming,
        builder.monitorInterval,
        builder.nameMapping,
        builder.projectedSchema,
        builder.filters,
        builder.limit,
        builder.includeColumnStats,
        builder.includeStatsForColumns,
        builder.exposeLocality,
        builder.planParallelism,
        builder.maxPlanningSnapshotCount,
        builder.maxAllowedPlanningFailures,
        builder.watermarkColumn,
        builder.watermarkColumnTimeUnit,
        builder.branch,
        builder.tag,
        builder.startTag,
        builder.endTag);
    this.scanStartupMode = builder.scanStartupMode;
    this.batchMode = builder.batchMode;
  }

  public boolean caseSensitive() {
    return caseSensitive;
  }

  public Long snapshotId() {
    return snapshotId;
  }

  public Long startSnapshotId() {
    return startSnapshotId;
  }

  public Long endSnapshotId() {
    return endSnapshotId;
  }

  public Long asOfTimestamp() {
    return asOfTimestamp;
  }

  public Long splitSize() {
    return splitSize;
  }

  public Integer splitLookback() {
    return splitLookback;
  }

  public Long splitOpenFileCost() {
    return splitOpenFileCost;
  }

  public boolean isStreaming() {
    return isStreaming;
  }

  public Duration monitorInterval() {
    return monitorInterval;
  }

  public String nameMapping() {
    return nameMapping;
  }

  public Schema project() {
    return schema;
  }

  /** Only working for base store right now. */
  public List<Expression> filters() {
    return filters;
  }

  public long limit() {
    return limit;
  }

  public static Builder contextBuilder() {
    return new Builder();
  }

  public String scanStartupMode() {
    return scanStartupMode;
  }

  public boolean isBatchMode() {
    return batchMode;
  }

  public static class Builder {
    private boolean caseSensitive = FlinkReadOptions.CASE_SENSITIVE_OPTION.defaultValue();
    private Long snapshotId = FlinkReadOptions.SNAPSHOT_ID.defaultValue();
    private StreamingStartingStrategy startingStrategy =
        FlinkReadOptions.STARTING_STRATEGY_OPTION.defaultValue();
    private Long startSnapshotTimestamp = FlinkReadOptions.START_SNAPSHOT_TIMESTAMP.defaultValue();
    private Long startSnapshotId = FlinkReadOptions.START_SNAPSHOT_ID.defaultValue();
    private Long endSnapshotId = FlinkReadOptions.END_SNAPSHOT_ID.defaultValue();
    private Long asOfTimestamp = FlinkReadOptions.AS_OF_TIMESTAMP.defaultValue();
    private Long splitSize = FlinkReadOptions.SPLIT_SIZE_OPTION.defaultValue();
    private Integer splitLookback = FlinkReadOptions.SPLIT_LOOKBACK_OPTION.defaultValue();
    private Long splitOpenFileCost = FlinkReadOptions.SPLIT_FILE_OPEN_COST_OPTION.defaultValue();
    private boolean isStreaming = FlinkReadOptions.STREAMING_OPTION.defaultValue();
    private Duration monitorInterval =
        TimeUtils.parseDuration(FlinkReadOptions.MONITOR_INTERVAL_OPTION.defaultValue());
    private String nameMapping;
    private Schema projectedSchema;
    private List<Expression> filters;
    private long limit = FlinkReadOptions.LIMIT_OPTION.defaultValue();
    private boolean includeColumnStats =
        FlinkReadOptions.INCLUDE_COLUMN_STATS_OPTION.defaultValue();
    private boolean exposeLocality;
    private Integer planParallelism =
        FlinkConfigOptions.TABLE_EXEC_ICEBERG_WORKER_POOL_SIZE.defaultValue();
    private int maxPlanningSnapshotCount = MAX_PLANNING_SNAPSHOT_COUNT.defaultValue();

    private int maxAllowedPlanningFailures =
        FlinkReadOptions.MAX_ALLOWED_PLANNING_FAILURES_OPTION.defaultValue();

    private String branch = FlinkReadOptions.BRANCH.defaultValue();

    private String tag = FlinkReadOptions.TAG.defaultValue();

    private String startTag = FlinkReadOptions.START_TAG.defaultValue();

    private String endTag = FlinkReadOptions.END_TAG.defaultValue();
    private String scanStartupMode;
    private Collection<String> includeStatsForColumns = null;
    private String watermarkColumn = FlinkReadOptions.WATERMARK_COLUMN_OPTION.defaultValue();
    private TimeUnit watermarkColumnTimeUnit =
        FlinkReadOptions.WATERMARK_COLUMN_TIME_UNIT_OPTION.defaultValue();

    private boolean batchMode = false;

    private Builder() {}

    public Builder caseSensitive(boolean newCaseSensitive) {
      this.caseSensitive = newCaseSensitive;
      return this;
    }

    public Builder useSnapshotId(Long newSnapshotId) {
      this.snapshotId = newSnapshotId;
      return this;
    }

    public Builder useTag(String tag) {
      this.tag = tag;
      return this;
    }

    public Builder useBranch(String branch) {
      this.branch = branch;
      return this;
    }

    public Builder startingStrategy(StreamingStartingStrategy newStartingStrategy) {
      this.startingStrategy = newStartingStrategy;
      return this;
    }

    public Builder startSnapshotTimestamp(Long newStartSnapshotTimestamp) {
      this.startSnapshotTimestamp = newStartSnapshotTimestamp;
      return this;
    }

    public Builder startSnapshotId(Long newStartSnapshotId) {
      this.startSnapshotId = newStartSnapshotId;
      return this;
    }

    public Builder endSnapshotId(Long newEndSnapshotId) {
      this.endSnapshotId = newEndSnapshotId;
      return this;
    }

    public Builder startTag(String startTag) {
      this.startTag = startTag;
      return this;
    }

    public Builder endTag(String endTag) {
      this.endTag = endTag;
      return this;
    }

    public Builder asOfTimestamp(Long newAsOfTimestamp) {
      this.asOfTimestamp = newAsOfTimestamp;
      return this;
    }

    public Builder splitSize(Long newSplitSize) {
      this.splitSize = newSplitSize;
      return this;
    }

    public Builder splitLookback(Integer newSplitLookback) {
      this.splitLookback = newSplitLookback;
      return this;
    }

    public Builder splitOpenFileCost(Long newSplitOpenFileCost) {
      this.splitOpenFileCost = newSplitOpenFileCost;
      return this;
    }

    public Builder streaming(boolean streaming) {
      this.isStreaming = streaming;
      return this;
    }

    public Builder monitorInterval(Duration newMonitorInterval) {
      this.monitorInterval = newMonitorInterval;
      return this;
    }

    public Builder nameMapping(String newNameMapping) {
      this.nameMapping = newNameMapping;
      return this;
    }

    public Builder project(Schema newProjectedSchema) {
      this.projectedSchema = newProjectedSchema;
      return this;
    }

    public Builder filters(List<Expression> newFilters) {
      this.filters = newFilters;
      return this;
    }

    public Builder limit(long newLimit) {
      this.limit = newLimit;
      return this;
    }

    public Builder exposeLocality(boolean newExposeLocality) {
      this.exposeLocality = newExposeLocality;
      return this;
    }

    public Builder planParallelism(Integer parallelism) {
      this.planParallelism = parallelism;
      return this;
    }

    public Builder maxPlanningSnapshotCount(int newMaxPlanningSnapshotCount) {
      this.maxPlanningSnapshotCount = newMaxPlanningSnapshotCount;
      return this;
    }

    Builder maxAllowedPlanningFailures(int newMaxAllowedPlanningFailures) {
      this.maxAllowedPlanningFailures = newMaxAllowedPlanningFailures;
      return this;
    }

    public Builder scanStartupMode(String scanStartupMode) {
      this.scanStartupMode = scanStartupMode;
      return this;
    }

    public Builder includeColumnStats(boolean newIncludeColumnStats) {
      this.includeColumnStats = newIncludeColumnStats;
      return this;
    }

    public Builder batchMode(boolean batchMode) {
      this.batchMode = batchMode;
      return this;
    }

    public Builder includeColumnStats(Collection<String> newIncludeStatsForColumns) {
      this.includeStatsForColumns = newIncludeStatsForColumns;
      return this;
    }

    public Builder watermarkColumn(String newWatermarkColumn) {
      this.watermarkColumn = newWatermarkColumn;
      return this;
    }

    public Builder watermarkColumnTimeUnit(TimeUnit newWatermarkTimeUnit) {
      this.watermarkColumnTimeUnit = newWatermarkTimeUnit;
      return this;
    }

    public Builder fromProperties(Map<String, String> properties) {
      Configuration config = new Configuration();
      properties.forEach(config::setString);

      return this.useSnapshotId(config.get(SNAPSHOT_ID))
          .useTag(config.get(TAG))
          .useBranch(config.get(BRANCH))
          .startTag(config.get(START_TAG))
          .endTag(config.get(END_TAG))
          .caseSensitive(config.get(CASE_SENSITIVE))
          .asOfTimestamp(config.get(AS_OF_TIMESTAMP))
          .startingStrategy(config.get(STARTING_STRATEGY))
          .startSnapshotTimestamp(config.get(START_SNAPSHOT_TIMESTAMP))
          .startSnapshotId(config.get(START_SNAPSHOT_ID))
          .endSnapshotId(config.get(END_SNAPSHOT_ID))
          .splitSize(config.get(SPLIT_SIZE))
          .splitLookback(config.get(SPLIT_LOOKBACK))
          .splitOpenFileCost(config.get(SPLIT_FILE_OPEN_COST))
          .streaming(config.get(STREAMING))
          .monitorInterval(config.get(MONITOR_INTERVAL))
          .nameMapping(properties.get(DEFAULT_NAME_MAPPING))
          .scanStartupMode(properties.get(MixedFormatValidator.SCAN_STARTUP_MODE.key()))
          .includeColumnStats(config.get(INCLUDE_COLUMN_STATS))
          .includeColumnStats(includeStatsForColumns)
          .maxPlanningSnapshotCount(config.get(MAX_PLANNING_SNAPSHOT_COUNT))
          .maxAllowedPlanningFailures(maxAllowedPlanningFailures)
          .watermarkColumn(watermarkColumn)
          .watermarkColumnTimeUnit(watermarkColumnTimeUnit);
    }

    public MixedFormatScanContext build() {
      scanStartupMode = scanStartupMode == null ? null : scanStartupMode.toLowerCase();
      Preconditions.checkArgument(
          Objects.isNull(scanStartupMode)
              || Objects.equals(scanStartupMode, MixedFormatValidator.SCAN_STARTUP_MODE_EARLIEST)
              || Objects.equals(scanStartupMode, MixedFormatValidator.SCAN_STARTUP_MODE_LATEST),
          String.format(
              "only support %s, %s when %s is %s",
              MixedFormatValidator.SCAN_STARTUP_MODE_EARLIEST,
              MixedFormatValidator.SCAN_STARTUP_MODE_LATEST,
              MixedFormatValidator.MIXED_FORMAT_READ_MODE,
              MixedFormatValidator.MIXED_FORMAT_READ_FILE));
      Preconditions.checkArgument(
          !(isStreaming && batchMode),
          String.format(
              "only support %s = false when execution.runtime-mode is batch", STREAMING.key()));
      return new MixedFormatScanContext(this);
    }
  }
}
