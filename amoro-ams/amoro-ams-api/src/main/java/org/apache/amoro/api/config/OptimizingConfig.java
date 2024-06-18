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

package org.apache.amoro.api.config;

import org.apache.amoro.properties.HiveTableProperties;
import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;
import org.apache.amoro.shade.guava32.com.google.common.base.Objects;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.utils.CompatiblePropertyUtil;
import org.apache.iceberg.util.PropertyUtil;

import java.util.Map;

/** Configuration for optimizing process scheduling and executing. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OptimizingConfig {

  // self-optimizing.enabled
  private boolean enabled;

  // self-optimizing.quota
  private double targetQuota;

  // self-optimizing.group
  private String optimizerGroup;

  // self-optimizing.execute.num-retries
  private int maxExecuteRetryCount;

  // self-optimizing.commit.num-retries
  private int maxCommitRetryCount;

  // self-optimizing.target-size
  private long targetSize;

  // self-optimizing.max-task-size-bytes
  private long maxTaskSize;

  // self-optimizing.max-file-count
  private int maxFileCount;

  // read.split.open-file-cost
  private long openFileCost;

  // self-optimizing.fragment-ratio
  private int fragmentRatio;

  // self-optimizing.min-target-size-ratio
  private double minTargetSizeRatio;

  // self-optimizing.minor.trigger.file-count
  private int minorLeastFileCount;

  // self-optimizing.minor.trigger.interval
  private int minorLeastInterval;

  // self-optimizing.major.trigger.duplicate-ratio
  private double majorDuplicateRatio;

  // self-optimizing.full.trigger.interval
  private int fullTriggerInterval;

  // self-optimizing.full.rewrite-all-files
  private boolean fullRewriteAllFiles;

  // base.file-index.hash-bucket
  private int baseHashBucket;

  // base.refresh-interval
  private long baseRefreshInterval;

  // base.hive.refresh-interval
  private long hiveRefreshInterval;

  // self-optimizing.min-plan-interval
  private long minPlanInterval;

  public OptimizingConfig() {}

  public boolean isEnabled() {
    return enabled;
  }

  public OptimizingConfig setEnabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public double getTargetQuota() {
    return targetQuota;
  }

  public OptimizingConfig setTargetQuota(double targetQuota) {
    this.targetQuota = targetQuota;
    return this;
  }

  public long getMinPlanInterval() {
    return minPlanInterval;
  }

  public OptimizingConfig setMinPlanInterval(long minPlanInterval) {
    this.minPlanInterval = minPlanInterval;
    return this;
  }

  public String getOptimizerGroup() {
    return optimizerGroup;
  }

  public OptimizingConfig setOptimizerGroup(String optimizerGroup) {
    this.optimizerGroup = optimizerGroup;
    return this;
  }

  public int getMaxExecuteRetryCount() {
    return maxExecuteRetryCount;
  }

  public OptimizingConfig setMaxExecuteRetryCount(int maxExecuteRetryCount) {
    this.maxExecuteRetryCount = maxExecuteRetryCount;
    return this;
  }

  public long getTargetSize() {
    return targetSize;
  }

  public OptimizingConfig setTargetSize(long targetSize) {
    this.targetSize = targetSize;
    return this;
  }

  public long getMaxTaskSize() {
    return maxTaskSize;
  }

  public OptimizingConfig setMaxTaskSize(long maxTaskSize) {
    this.maxTaskSize = maxTaskSize;
    return this;
  }

  public int getMaxFileCount() {
    return maxFileCount;
  }

  public OptimizingConfig setMaxFileCount(int maxFileCount) {
    this.maxFileCount = maxFileCount;
    return this;
  }

  public long getOpenFileCost() {
    return openFileCost;
  }

  public OptimizingConfig setOpenFileCost(long openFileCost) {
    this.openFileCost = openFileCost;
    return this;
  }

  public int getFragmentRatio() {
    return fragmentRatio;
  }

  public OptimizingConfig setFragmentRatio(int fragmentRatio) {
    this.fragmentRatio = fragmentRatio;
    return this;
  }

  public double getMinTargetSizeRatio() {
    return minTargetSizeRatio;
  }

  public OptimizingConfig setMinTargetSizeRatio(double minTargetSizeRatio) {
    this.minTargetSizeRatio = minTargetSizeRatio;
    return this;
  }

  public long maxFragmentSize() {
    return targetSize / fragmentRatio;
  }

  public long maxDuplicateSize() {
    return (long) (maxFragmentSize() * majorDuplicateRatio);
  }

  public int getMinorLeastFileCount() {
    return minorLeastFileCount;
  }

  public OptimizingConfig setMinorLeastFileCount(int minorLeastFileCount) {
    this.minorLeastFileCount = minorLeastFileCount;
    return this;
  }

  public int getMinorLeastInterval() {
    return minorLeastInterval;
  }

  public OptimizingConfig setMinorLeastInterval(int minorLeastInterval) {
    this.minorLeastInterval = minorLeastInterval;
    return this;
  }

  public double getMajorDuplicateRatio() {
    return majorDuplicateRatio;
  }

  public OptimizingConfig setMajorDuplicateRatio(double majorDuplicateRatio) {
    this.majorDuplicateRatio = majorDuplicateRatio;
    return this;
  }

  public int getFullTriggerInterval() {
    return fullTriggerInterval;
  }

  public OptimizingConfig setFullTriggerInterval(int fullTriggerInterval) {
    this.fullTriggerInterval = fullTriggerInterval;
    return this;
  }

  public boolean isFullRewriteAllFiles() {
    return fullRewriteAllFiles;
  }

  public OptimizingConfig setFullRewriteAllFiles(boolean fullRewriteAllFiles) {
    this.fullRewriteAllFiles = fullRewriteAllFiles;
    return this;
  }

  public int getBaseHashBucket() {
    return baseHashBucket;
  }

  public OptimizingConfig setBaseHashBucket(int baseHashBucket) {
    this.baseHashBucket = baseHashBucket;
    return this;
  }

  public long getBaseRefreshInterval() {
    return baseRefreshInterval;
  }

  public OptimizingConfig setBaseRefreshInterval(long baseRefreshInterval) {
    this.baseRefreshInterval = baseRefreshInterval;
    return this;
  }

  public long getHiveRefreshInterval() {
    return hiveRefreshInterval;
  }

  public OptimizingConfig setHiveRefreshInterval(long hiveRefreshInterval) {
    this.hiveRefreshInterval = hiveRefreshInterval;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OptimizingConfig that = (OptimizingConfig) o;
    return enabled == that.enabled
        && Double.compare(that.targetQuota, targetQuota) == 0
        && maxExecuteRetryCount == that.maxExecuteRetryCount
        && maxCommitRetryCount == that.maxCommitRetryCount
        && targetSize == that.targetSize
        && maxTaskSize == that.maxTaskSize
        && maxFileCount == that.maxFileCount
        && openFileCost == that.openFileCost
        && fragmentRatio == that.fragmentRatio
        && Double.compare(minTargetSizeRatio, that.minTargetSizeRatio) == 0
        && minorLeastFileCount == that.minorLeastFileCount
        && minorLeastInterval == that.minorLeastInterval
        && Double.compare(that.majorDuplicateRatio, majorDuplicateRatio) == 0
        && fullTriggerInterval == that.fullTriggerInterval
        && fullRewriteAllFiles == that.fullRewriteAllFiles
        && baseHashBucket == that.baseHashBucket
        && baseRefreshInterval == that.baseRefreshInterval
        && hiveRefreshInterval == that.hiveRefreshInterval
        && Objects.equal(optimizerGroup, that.optimizerGroup);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        enabled,
        targetQuota,
        optimizerGroup,
        maxExecuteRetryCount,
        maxCommitRetryCount,
        targetSize,
        maxTaskSize,
        maxFileCount,
        openFileCost,
        fragmentRatio,
        minTargetSizeRatio,
        minorLeastFileCount,
        minorLeastInterval,
        majorDuplicateRatio,
        fullTriggerInterval,
        fullRewriteAllFiles,
        baseHashBucket,
        baseRefreshInterval,
        hiveRefreshInterval);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("enabled", enabled)
        .add("targetQuota", targetQuota)
        .add("optimizerGroup", optimizerGroup)
        .add("maxExecuteRetryCount", maxExecuteRetryCount)
        .add("maxCommitRetryCount", maxCommitRetryCount)
        .add("targetSize", targetSize)
        .add("maxTaskSize", maxTaskSize)
        .add("maxFileCount", maxFileCount)
        .add("openFileCost", openFileCost)
        .add("fragmentRatio", fragmentRatio)
        .add("minorLeastFileCount", minorLeastFileCount)
        .add("minorLeastInterval", minorLeastInterval)
        .add("majorDuplicateRatio", majorDuplicateRatio)
        .add("fullTriggerInterval", fullTriggerInterval)
        .add("fullRewriteAllFiles", fullRewriteAllFiles)
        .add("baseHashBucket", baseHashBucket)
        .add("baseRefreshInterval", baseRefreshInterval)
        .add("hiveRefreshInterval", hiveRefreshInterval)
        .toString();
  }

  public static OptimizingConfig parse(Map<String, String> properties) {
    return new OptimizingConfig()
        .setEnabled(
            CompatiblePropertyUtil.propertyAsBoolean(
                properties,
                TableProperties.ENABLE_SELF_OPTIMIZING,
                TableProperties.ENABLE_SELF_OPTIMIZING_DEFAULT))
        .setMaxExecuteRetryCount(
            CompatiblePropertyUtil.propertyAsInt(
                properties,
                TableProperties.SELF_OPTIMIZING_EXECUTE_RETRY_NUMBER,
                TableProperties.SELF_OPTIMIZING_EXECUTE_RETRY_NUMBER_DEFAULT))
        .setOptimizerGroup(
            CompatiblePropertyUtil.propertyAsString(
                properties,
                TableProperties.SELF_OPTIMIZING_GROUP,
                TableProperties.SELF_OPTIMIZING_GROUP_DEFAULT))
        .setFragmentRatio(
            CompatiblePropertyUtil.propertyAsInt(
                properties,
                TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO,
                TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO_DEFAULT))
        .setMinTargetSizeRatio(
            CompatiblePropertyUtil.propertyAsDouble(
                properties,
                TableProperties.SELF_OPTIMIZING_MIN_TARGET_SIZE_RATIO,
                TableProperties.SELF_OPTIMIZING_MIN_TARGET_SIZE_RATIO_DEFAULT))
        .setMaxFileCount(
            CompatiblePropertyUtil.propertyAsInt(
                properties,
                TableProperties.SELF_OPTIMIZING_MAX_FILE_CNT,
                TableProperties.SELF_OPTIMIZING_MAX_FILE_CNT_DEFAULT))
        .setOpenFileCost(
            CompatiblePropertyUtil.propertyAsLong(
                properties,
                TableProperties.SPLIT_OPEN_FILE_COST,
                TableProperties.SPLIT_OPEN_FILE_COST_DEFAULT))
        .setTargetSize(
            CompatiblePropertyUtil.propertyAsLong(
                properties,
                TableProperties.SELF_OPTIMIZING_TARGET_SIZE,
                TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT))
        .setMaxTaskSize(
            CompatiblePropertyUtil.propertyAsLong(
                properties,
                TableProperties.SELF_OPTIMIZING_MAX_TASK_SIZE,
                TableProperties.SELF_OPTIMIZING_MAX_TASK_SIZE_DEFAULT))
        .setTargetQuota(
            CompatiblePropertyUtil.propertyAsDouble(
                properties,
                TableProperties.SELF_OPTIMIZING_QUOTA,
                TableProperties.SELF_OPTIMIZING_QUOTA_DEFAULT))
        .setMinorLeastFileCount(
            CompatiblePropertyUtil.propertyAsInt(
                properties,
                TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT,
                TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT_DEFAULT))
        .setMinorLeastInterval(
            CompatiblePropertyUtil.propertyAsInt(
                properties,
                TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_INTERVAL,
                TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_INTERVAL_DEFAULT))
        .setMajorDuplicateRatio(
            CompatiblePropertyUtil.propertyAsDouble(
                properties,
                TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_DUPLICATE_RATIO,
                TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_DUPLICATE_RATIO_DEFAULT))
        .setFullTriggerInterval(
            CompatiblePropertyUtil.propertyAsInt(
                properties,
                TableProperties.SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL,
                TableProperties.SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL_DEFAULT))
        .setFullRewriteAllFiles(
            CompatiblePropertyUtil.propertyAsBoolean(
                properties,
                TableProperties.SELF_OPTIMIZING_FULL_REWRITE_ALL_FILES,
                TableProperties.SELF_OPTIMIZING_FULL_REWRITE_ALL_FILES_DEFAULT))
        .setBaseHashBucket(
            CompatiblePropertyUtil.propertyAsInt(
                properties,
                TableProperties.BASE_FILE_INDEX_HASH_BUCKET,
                TableProperties.BASE_FILE_INDEX_HASH_BUCKET_DEFAULT))
        .setBaseRefreshInterval(
            PropertyUtil.propertyAsLong(
                properties,
                TableProperties.BASE_REFRESH_INTERVAL,
                TableProperties.BASE_REFRESH_INTERVAL_DEFAULT))
        .setHiveRefreshInterval(
            PropertyUtil.propertyAsLong(
                properties,
                HiveTableProperties.REFRESH_HIVE_INTERVAL,
                HiveTableProperties.REFRESH_HIVE_INTERVAL_DEFAULT))
        .setMinPlanInterval(
            PropertyUtil.propertyAsLong(
                properties,
                TableProperties.SELF_OPTIMIZING_MIN_PLAN_INTERVAL,
                TableProperties.SELF_OPTIMIZING_MIN_PLAN_INTERVAL_DEFAULT));
  }
}
