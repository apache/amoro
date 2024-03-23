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

package org.apache.iceberg.flink.source;

import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.util.Preconditions;
import org.apache.flink.util.TimeUtils;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.flink.FlinkConfigOptions;
import org.apache.iceberg.flink.FlinkReadConf;
import org.apache.iceberg.flink.FlinkReadOptions;

import java.io.Serializable;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Copy from Iceberg {@link ScanContext}. only change line 115 and expand the modifier. Context
 * object with optional arguments for a Flink Scan.
 */
public class ScanContext implements Serializable {

  private static final long serialVersionUID = 1L;

  public static final ConfigOption<Long> SNAPSHOT_ID =
      ConfigOptions.key("snapshot-id")
          .longType()
          .defaultValue(null)
          .withDescription(
              "Retrieve the full data of the specified snapshot by ID, used for batch scan mode");

  public static final ConfigOption<String> TAG =
      ConfigOptions.key("tag").stringType().defaultValue(null);

  public static final ConfigOption<String> BRANCH =
      ConfigOptions.key("branch").stringType().defaultValue(null);

  public static final ConfigOption<String> START_TAG =
      ConfigOptions.key("start-tag").stringType().defaultValue(null);

  public static final ConfigOption<String> END_TAG =
      ConfigOptions.key("end-tag").stringType().defaultValue(null);

  public static final ConfigOption<Boolean> CASE_SENSITIVE =
      ConfigOptions.key("case-sensitive")
          .booleanType()
          .defaultValue(false)
          .withDescription("Set if column names are case-sensitive");

  public static final ConfigOption<Long> AS_OF_TIMESTAMP =
      ConfigOptions.key("as-of-timestamp")
          .longType()
          .defaultValue(null)
          .withDescription(
              "Retrieve the full data of the specified snapshot at the given timestamp, "
                  + "used for batch scan mode");

  public static final ConfigOption<StreamingStartingStrategy> STARTING_STRATEGY =
      ConfigOptions.key("starting-strategy")
          .enumType(StreamingStartingStrategy.class)
          .defaultValue(StreamingStartingStrategy.INCREMENTAL_FROM_LATEST_SNAPSHOT)
          .withDescription("Specific the starting strategy for streaming execution");

  public static final ConfigOption<Long> START_SNAPSHOT_TIMESTAMP =
      ConfigOptions.key("start-snapshot-timestamp")
          .longType()
          .defaultValue(null)
          .withDescription("Specific the snapshot timestamp that streaming job starts from");

  public static final ConfigOption<Long> START_SNAPSHOT_ID =
      ConfigOptions.key("start-snapshot-id")
          .longType()
          .defaultValue(null)
          .withDescription("Specific the snapshot id that streaming job starts from");

  public static final ConfigOption<Long> END_SNAPSHOT_ID =
      ConfigOptions.key("end-snapshot-id")
          .longType()
          .defaultValue(null)
          .withDescription("Specific the snapshot id that streaming job to end");

  public static final ConfigOption<Long> SPLIT_SIZE =
      ConfigOptions.key("split-size")
          .longType()
          .defaultValue(null)
          .withDescription("Specific the target size when combining data input splits");

  public static final ConfigOption<Integer> SPLIT_LOOKBACK =
      ConfigOptions.key("split-lookback")
          .intType()
          .defaultValue(null)
          .withDescription("Specify the number of bins to consider when combining input splits");

  public static final ConfigOption<Long> SPLIT_FILE_OPEN_COST =
      ConfigOptions.key("split-file-open-cost")
          .longType()
          .defaultValue(null)
          .withDescription(
              "The estimated cost to open a file, used as a minimum weight when combining splits");

  public static final ConfigOption<Boolean> STREAMING =
      ConfigOptions.key("streaming")
          .booleanType()
          .defaultValue(true)
          .withDescription("Set if job is bounded or unbounded");

  public static final ConfigOption<Duration> MONITOR_INTERVAL =
      ConfigOptions.key("monitor-interval")
          .durationType()
          .defaultValue(Duration.ofSeconds(10))
          .withDescription(
              "Specify the time interval for consecutively monitoring newly committed data files");

  public static final ConfigOption<Boolean> INCLUDE_COLUMN_STATS =
      ConfigOptions.key("include-column-stats")
          .booleanType()
          .defaultValue(false)
          .withDescription("Set if loads the column stats with each file");

  public static final ConfigOption<Integer> MAX_PLANNING_SNAPSHOT_COUNT =
      ConfigOptions.key("max-planning-snapshot-count")
          .intType()
          .defaultValue(Integer.MAX_VALUE)
          .withDescription("Specify the max planning snapshot count");

  public static final ConfigOption<Long> LIMIT_OPTION =
      ConfigOptions.key("limit").longType().defaultValue(-1L);

  public static final ConfigOption<Integer> MAX_ALLOWED_PLANNING_FAILURES_OPTION =
      ConfigOptions.key("max-allowed-planning-failures").intType().defaultValue(3);

  protected final boolean caseSensitive;
  protected final boolean exposeLocality;
  protected final Long snapshotId;
  protected final String branch;
  protected final String tag;
  protected final StreamingStartingStrategy startingStrategy;
  protected final Long startSnapshotId;
  protected final Long startSnapshotTimestamp;
  protected final Long endSnapshotId;
  protected final Long asOfTimestamp;
  protected final String startTag;
  protected final String endTag;
  protected final Long splitSize;
  protected final Integer splitLookback;
  protected final Long splitOpenFileCost;
  protected final boolean isStreaming;
  protected final Duration monitorInterval;

  protected final String nameMapping;
  protected final Schema schema;
  protected final List<Expression> filters;
  protected final long limit;
  protected final boolean includeColumnStats;
  protected final Integer planParallelism;
  protected final int maxPlanningSnapshotCount;
  protected final int maxAllowedPlanningFailures;

  protected ScanContext(
      boolean caseSensitive,
      Long snapshotId,
      StreamingStartingStrategy startingStrategy,
      Long startSnapshotTimestamp,
      Long startSnapshotId,
      Long endSnapshotId,
      Long asOfTimestamp,
      Long splitSize,
      Integer splitLookback,
      Long splitOpenFileCost,
      boolean isStreaming,
      Duration monitorInterval,
      String nameMapping,
      Schema schema,
      List<Expression> filters,
      long limit,
      boolean includeColumnStats,
      boolean exposeLocality,
      Integer planParallelism,
      int maxPlanningSnapshotCount,
      int maxAllowedPlanningFailures,
      String branch,
      String tag,
      String startTag,
      String endTag) {
    this.caseSensitive = caseSensitive;
    this.snapshotId = snapshotId;
    this.tag = tag;
    this.branch = branch;
    this.startingStrategy = startingStrategy;
    this.startSnapshotTimestamp = startSnapshotTimestamp;
    this.startSnapshotId = startSnapshotId;
    this.endSnapshotId = endSnapshotId;
    this.asOfTimestamp = asOfTimestamp;
    this.startTag = startTag;
    this.endTag = endTag;
    this.splitSize = splitSize;
    this.splitLookback = splitLookback;
    this.splitOpenFileCost = splitOpenFileCost;
    this.isStreaming = isStreaming;
    this.monitorInterval = monitorInterval;

    this.nameMapping = nameMapping;
    this.schema = schema;
    this.filters = filters;
    this.limit = limit;
    this.includeColumnStats = includeColumnStats;
    this.exposeLocality = exposeLocality;
    this.planParallelism = planParallelism;
    this.maxPlanningSnapshotCount = maxPlanningSnapshotCount;
    this.maxAllowedPlanningFailures = maxAllowedPlanningFailures;

    validate();
  }

  private void validate() {
    if (isStreaming) {
      if (startingStrategy == StreamingStartingStrategy.INCREMENTAL_FROM_SNAPSHOT_ID) {
        Preconditions.checkArgument(
            startSnapshotId != null,
            "Invalid starting snapshot id for SPECIFIC_START_SNAPSHOT_ID strategy: null");
        Preconditions.checkArgument(
            startSnapshotTimestamp == null,
            "Invalid starting snapshot timestamp for SPECIFIC_START_SNAPSHOT_ID strategy: not null");
      }
      if (startingStrategy == StreamingStartingStrategy.INCREMENTAL_FROM_SNAPSHOT_TIMESTAMP) {
        Preconditions.checkArgument(
            startSnapshotTimestamp != null,
            "Invalid starting snapshot timestamp for SPECIFIC_START_SNAPSHOT_TIMESTAMP strategy: null");
        Preconditions.checkArgument(
            startSnapshotId == null,
            "Invalid starting snapshot id for SPECIFIC_START_SNAPSHOT_ID strategy: not null");
      }
      Preconditions.checkArgument(
          branch == null,
          String.format(
              "Cannot scan table using ref %s configured for streaming reader yet", branch));

      Preconditions.checkArgument(
          tag == null,
          String.format("Cannot scan table using ref %s configured for streaming reader", tag));
    }
    Preconditions.checkArgument(
        !(startTag != null && startSnapshotId() != null),
        "START_SNAPSHOT_ID and START_TAG cannot both be set.");

    Preconditions.checkArgument(
        !(endTag != null && endSnapshotId() != null),
        "END_SNAPSHOT_ID and END_TAG cannot both be set.");

    Preconditions.checkArgument(
        maxAllowedPlanningFailures >= -1,
        "Cannot set maxAllowedPlanningFailures to a negative number other than -1.");
  }

  boolean caseSensitive() {
    return caseSensitive;
  }

  Long snapshotId() {
    return snapshotId;
  }

  String branch() {
    return branch;
  }

  String tag() {
    return tag;
  }

  String startTag() {
    return startTag;
  }

  String endTag() {
    return endTag;
  }

  StreamingStartingStrategy streamingStartingStrategy() {
    return startingStrategy;
  }

  Long startSnapshotTimestamp() {
    return startSnapshotTimestamp;
  }

  Long startSnapshotId() {
    return startSnapshotId;
  }

  Long endSnapshotId() {
    return endSnapshotId;
  }

  Long asOfTimestamp() {
    return asOfTimestamp;
  }

  Long splitSize() {
    return splitSize;
  }

  Integer splitLookback() {
    return splitLookback;
  }

  Long splitOpenFileCost() {
    return splitOpenFileCost;
  }

  boolean isStreaming() {
    return isStreaming;
  }

  Duration monitorInterval() {
    return monitorInterval;
  }

  String nameMapping() {
    return nameMapping;
  }

  Schema project() {
    return schema;
  }

  List<Expression> filters() {
    return filters;
  }

  long limit() {
    return limit;
  }

  boolean includeColumnStats() {
    return includeColumnStats;
  }

  boolean exposeLocality() {
    return exposeLocality;
  }

  Integer planParallelism() {
    return planParallelism;
  }

  int maxPlanningSnapshotCount() {
    return maxPlanningSnapshotCount;
  }

  int maxAllowedPlanningFailures() {
    return maxAllowedPlanningFailures;
  }

  ScanContext copyWithAppendsBetween(Long newStartSnapshotId, long newEndSnapshotId) {
    return ScanContext.builder()
        .caseSensitive(caseSensitive)
        .useSnapshotId(null)
        .useBranch(branch)
        .useTag(null)
        .startSnapshotId(newStartSnapshotId)
        .endSnapshotId(newEndSnapshotId)
        .startTag(null)
        .endTag(null)
        .asOfTimestamp(null)
        .splitSize(splitSize)
        .splitLookback(splitLookback)
        .splitOpenFileCost(splitOpenFileCost)
        .streaming(isStreaming)
        .monitorInterval(monitorInterval)
        .nameMapping(nameMapping)
        .project(schema)
        .filters(filters)
        .limit(limit)
        .includeColumnStats(includeColumnStats)
        .exposeLocality(exposeLocality)
        .planParallelism(planParallelism)
        .maxPlanningSnapshotCount(maxPlanningSnapshotCount)
        .maxAllowedPlanningFailures(maxAllowedPlanningFailures)
        .build();
  }

  ScanContext copyWithSnapshotId(long newSnapshotId) {
    return ScanContext.builder()
        .caseSensitive(caseSensitive)
        .useSnapshotId(newSnapshotId)
        .useBranch(branch)
        .useTag(null)
        .startSnapshotId(null)
        .endSnapshotId(null)
        .startTag(null)
        .endTag(null)
        .asOfTimestamp(null)
        .splitSize(splitSize)
        .splitLookback(splitLookback)
        .splitOpenFileCost(splitOpenFileCost)
        .streaming(isStreaming)
        .monitorInterval(monitorInterval)
        .nameMapping(nameMapping)
        .project(schema)
        .filters(filters)
        .limit(limit)
        .includeColumnStats(includeColumnStats)
        .exposeLocality(exposeLocality)
        .planParallelism(planParallelism)
        .maxPlanningSnapshotCount(maxPlanningSnapshotCount)
        .maxAllowedPlanningFailures(maxAllowedPlanningFailures)
        .build();
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {
    private boolean caseSensitive = FlinkReadOptions.CASE_SENSITIVE_OPTION.defaultValue();
    private Long snapshotId = FlinkReadOptions.SNAPSHOT_ID.defaultValue();
    private String branch = FlinkReadOptions.BRANCH.defaultValue();
    private String tag = FlinkReadOptions.TAG.defaultValue();
    private String startTag = FlinkReadOptions.START_TAG.defaultValue();
    private String endTag = FlinkReadOptions.END_TAG.defaultValue();
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
    private int maxPlanningSnapshotCount =
        FlinkReadOptions.MAX_PLANNING_SNAPSHOT_COUNT_OPTION.defaultValue();
    private int maxAllowedPlanningFailures =
        FlinkReadOptions.MAX_ALLOWED_PLANNING_FAILURES_OPTION.defaultValue();

    private Builder() {}

    Builder caseSensitive(boolean newCaseSensitive) {
      this.caseSensitive = newCaseSensitive;
      return this;
    }

    Builder useSnapshotId(Long newSnapshotId) {
      this.snapshotId = newSnapshotId;
      return this;
    }

    Builder useTag(String newTag) {
      this.tag = newTag;
      return this;
    }

    Builder useBranch(String newBranch) {
      this.branch = newBranch;
      return this;
    }

    Builder startingStrategy(StreamingStartingStrategy newStartingStrategy) {
      this.startingStrategy = newStartingStrategy;
      return this;
    }

    Builder startSnapshotTimestamp(Long newStartSnapshotTimestamp) {
      this.startSnapshotTimestamp = newStartSnapshotTimestamp;
      return this;
    }

    Builder startSnapshotId(Long newStartSnapshotId) {
      this.startSnapshotId = newStartSnapshotId;
      return this;
    }

    Builder endSnapshotId(Long newEndSnapshotId) {
      this.endSnapshotId = newEndSnapshotId;
      return this;
    }

    Builder startTag(String newStartTag) {
      this.startTag = newStartTag;
      return this;
    }

    Builder endTag(String newEndTag) {
      this.endTag = newEndTag;
      return this;
    }

    Builder asOfTimestamp(Long newAsOfTimestamp) {
      this.asOfTimestamp = newAsOfTimestamp;
      return this;
    }

    Builder splitSize(Long newSplitSize) {
      this.splitSize = newSplitSize;
      return this;
    }

    Builder splitLookback(Integer newSplitLookback) {
      this.splitLookback = newSplitLookback;
      return this;
    }

    Builder splitOpenFileCost(Long newSplitOpenFileCost) {
      this.splitOpenFileCost = newSplitOpenFileCost;
      return this;
    }

    Builder streaming(boolean streaming) {
      this.isStreaming = streaming;
      return this;
    }

    Builder monitorInterval(Duration newMonitorInterval) {
      this.monitorInterval = newMonitorInterval;
      return this;
    }

    Builder nameMapping(String newNameMapping) {
      this.nameMapping = newNameMapping;
      return this;
    }

    Builder project(Schema newProjectedSchema) {
      this.projectedSchema = newProjectedSchema;
      return this;
    }

    Builder filters(List<Expression> newFilters) {
      this.filters = newFilters;
      return this;
    }

    Builder limit(long newLimit) {
      this.limit = newLimit;
      return this;
    }

    Builder includeColumnStats(boolean newIncludeColumnStats) {
      this.includeColumnStats = newIncludeColumnStats;
      return this;
    }

    Builder exposeLocality(boolean newExposeLocality) {
      this.exposeLocality = newExposeLocality;
      return this;
    }

    Builder planParallelism(Integer parallelism) {
      this.planParallelism = parallelism;
      return this;
    }

    Builder maxPlanningSnapshotCount(int newMaxPlanningSnapshotCount) {
      this.maxPlanningSnapshotCount = newMaxPlanningSnapshotCount;
      return this;
    }

    Builder maxAllowedPlanningFailures(int newMaxAllowedPlanningFailures) {
      this.maxAllowedPlanningFailures = newMaxAllowedPlanningFailures;
      return this;
    }

    Builder resolveConfig(
        Table table, Map<String, String> readOptions, ReadableConfig readableConfig) {
      FlinkReadConf flinkReadConf = new FlinkReadConf(table, readOptions, readableConfig);

      return this.useSnapshotId(flinkReadConf.snapshotId())
          .useTag(flinkReadConf.tag())
          .useBranch(flinkReadConf.branch())
          .startTag(flinkReadConf.startTag())
          .endTag(flinkReadConf.endTag())
          .caseSensitive(flinkReadConf.caseSensitive())
          .asOfTimestamp(flinkReadConf.asOfTimestamp())
          .startingStrategy(flinkReadConf.startingStrategy())
          .startSnapshotTimestamp(flinkReadConf.startSnapshotTimestamp())
          .startSnapshotId(flinkReadConf.startSnapshotId())
          .endSnapshotId(flinkReadConf.endSnapshotId())
          .splitSize(flinkReadConf.splitSize())
          .splitLookback(flinkReadConf.splitLookback())
          .splitOpenFileCost(flinkReadConf.splitFileOpenCost())
          .streaming(flinkReadConf.streaming())
          .monitorInterval(flinkReadConf.monitorInterval())
          .nameMapping(flinkReadConf.nameMapping())
          .limit(flinkReadConf.limit())
          .planParallelism(flinkReadConf.workerPoolSize())
          .includeColumnStats(flinkReadConf.includeColumnStats())
          .maxPlanningSnapshotCount(flinkReadConf.maxPlanningSnapshotCount())
          .maxAllowedPlanningFailures(maxAllowedPlanningFailures);
    }

    public ScanContext build() {
      return new ScanContext(
          caseSensitive,
          snapshotId,
          startingStrategy,
          startSnapshotTimestamp,
          startSnapshotId,
          endSnapshotId,
          asOfTimestamp,
          splitSize,
          splitLookback,
          splitOpenFileCost,
          isStreaming,
          monitorInterval,
          nameMapping,
          projectedSchema,
          filters,
          limit,
          includeColumnStats,
          exposeLocality,
          planParallelism,
          maxPlanningSnapshotCount,
          maxAllowedPlanningFailures,
          branch,
          tag,
          startTag,
          endTag);
    }
  }
}
