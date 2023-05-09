package com.netease.arctic.server.optimizing;

import com.google.common.base.Objects;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.CompatiblePropertyUtil;

import java.util.Map;

public class OptimizingConfig {

  //self-optimizing.enabled
  private boolean enabled;

  //self-optimizing.quota
  private double targetQuota;

  //self-optimizing.group
  private String optimizerGroup;

  //self-optimizing.num-retries
  private int maxRetryCount;

  //self-optimizing.target-size
  private long targetSize;

  //self-optimizing.max-file-count
  private int maxFileCount;

  //self-optimizing.fragment-ratio
  private int fragmentRatio;

  //self-optimizing.minor.trigger.file-count
  private int minorLeastFileCount;

  //self-optimizing.minor.trigger.interval
  private int minorLeastInterval;

  //self-optimizing.major.trigger.file-count
  private int majorLeastFileCount;

  //self-optimizing.major.trigger.duplicate-ratio
  private double majorDuplicateRatio;

  //self-optimizing.full.trigger.interval
  private int fullTriggerInterval;
  private long maxFragmentSize;
  private long maxDuplicateSize;

  public OptimizingConfig() {
  }

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

  public String getOptimizerGroup() {
    return optimizerGroup;
  }

  public OptimizingConfig setOptimizerGroup(String optimizerGroup) {
    this.optimizerGroup = optimizerGroup;
    return this;
  }

  public int getMaxRetryCount() {
    return maxRetryCount;
  }

  public OptimizingConfig setMaxRetryCount(int maxRetryCount) {
    this.maxRetryCount = maxRetryCount;
    return this;
  }

  public long getTargetSize() {
    return targetSize;
  }

  public OptimizingConfig setTargetSize(long targetSize) {
    this.targetSize = targetSize;
    return this;
  }

  public int getMaxFileCount() {
    return maxFileCount;
  }

  public OptimizingConfig setMaxFileCount(int maxFileCount) {
    this.maxFileCount = maxFileCount;
    return this;
  }

  public int getFragmentRatio() {
    return fragmentRatio;
  }

  public long maxFragmentSize() {
    return targetSize / fragmentRatio;
  }

  public long maxDuplicateSize() {
    return (long) (maxFragmentSize() * majorDuplicateRatio);
  }

  public OptimizingConfig setFragmentRatio(int fragmentRatio) {
    this.fragmentRatio = fragmentRatio;
    return this;
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

  public int getMajorLeastFileCount() {
    return majorLeastFileCount;
  }

  public OptimizingConfig setMajorLeastFileCount(int majorLeastFileCount) {
    this.majorLeastFileCount = majorLeastFileCount;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OptimizingConfig that = (OptimizingConfig) o;
    return enabled == that.enabled && Double.compare(that.targetQuota, targetQuota) == 0 &&
        maxRetryCount == that.maxRetryCount && targetSize == that.targetSize && maxFileCount == that.maxFileCount &&
        fragmentRatio == that.fragmentRatio && minorLeastFileCount == that.minorLeastFileCount &&
        minorLeastInterval == that.minorLeastInterval && majorLeastFileCount == that.majorLeastFileCount &&
        Double.compare(that.majorDuplicateRatio, majorDuplicateRatio) == 0 &&
        fullTriggerInterval == that.fullTriggerInterval && Objects.equal(optimizerGroup, that.optimizerGroup);
  }

  public static OptimizingConfig parseOptimizingConfig(Map<String, String> properties) {
    return new OptimizingConfig().setEnabled(CompatiblePropertyUtil.propertyAsBoolean(
            properties,
            TableProperties.ENABLE_SELF_OPTIMIZING,
            TableProperties.ENABLE_SELF_OPTIMIZING_DEFAULT))
        .setMaxRetryCount(CompatiblePropertyUtil.propertyAsInt(
            properties,
            TableProperties.SELF_OPTIMIZING_RETRY_NUMBER,
            TableProperties.SELF_OPTIMIZING_RETRY_NUMBER_DEFAULT))
        .setOptimizerGroup(CompatiblePropertyUtil.propertyAsString(
            properties,
            TableProperties.SELF_OPTIMIZING_GROUP,
            TableProperties.SELF_OPTIMIZING_GROUP_DEFAULT))
        .setFragmentRatio(CompatiblePropertyUtil.propertyAsInt(
            properties,
            TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO,
            TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO_DEFAULT))
        .setMaxFileCount(CompatiblePropertyUtil.propertyAsInt(
            properties,
            TableProperties.SELF_OPTIMIZING_MAX_FILE_CNT,
            TableProperties.SELF_OPTIMIZING_MAX_FILE_CNT_DEFAULT))
        .setTargetSize(CompatiblePropertyUtil.propertyAsLong(
            properties,
            TableProperties.SELF_OPTIMIZING_TARGET_SIZE,
            TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT))
        .setTargetQuota(CompatiblePropertyUtil.propertyAsDouble(
            properties,
            TableProperties.SELF_OPTIMIZING_QUOTA,
            TableProperties.SELF_OPTIMIZING_QUOTA_DEFAULT))
        .setMinorLeastFileCount(CompatiblePropertyUtil.propertyAsInt(
            properties,
            TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT,
            TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT_DEFAULT))
        .setMinorLeastInterval(CompatiblePropertyUtil.propertyAsInt(
            properties,
            TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_INTERVAL,
            TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_INTERVAL_DEFAULT))
        .setMajorDuplicateRatio(CompatiblePropertyUtil.propertyAsDouble(
            properties,
            TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_DUPLICATE_RATIO,
            TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_DUPLICATE_RATIO_DEFAULT))
        .setMajorLeastFileCount(CompatiblePropertyUtil.propertyAsInt(
            properties,
            TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_FILE_CNT,
            TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_FILE_CNT_DEFAULT))
        .setFullTriggerInterval(CompatiblePropertyUtil.propertyAsInt(
            properties,
            TableProperties.SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL,
            TableProperties.SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL_DEFAULT));
  }
}