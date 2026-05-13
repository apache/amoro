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

import org.apache.amoro.OptimizerProperties;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.paimon.CoreOptions;
import org.apache.paimon.options.Options;

final class PaimonPlanContext {

  private static final String COMPACTION_FILE_NUM_LIMIT = "compaction.file-num-limit";
  private static final String COMPACTION_SMALL_FILE_RATIO = "compaction.small-file-ratio";
  private static final int DEFAULT_COMPACTION_FILE_NUM_LIMIT = 100_000;

  private final CoreOptions coreOptions;
  private final OptimizingConfig optimizingConfig;
  private final long lastMinorOptimizingTime;
  private final long lastMajorOptimizingTime;
  private final long lastFullOptimizingTime;
  private final double availableCore;
  private final long maxInputSizePerThread;
  private final long planTime;
  private final long targetSize;
  private final long smallFileBoundary;
  private final int minorMinFileNum;
  private final int fileNumLimit;
  private final double deleteRatioThreshold;
  private final long openFileCost;

  private PaimonPlanContext(
      CoreOptions coreOptions,
      OptimizingConfig optimizingConfig,
      long lastMinorOptimizingTime,
      long lastMajorOptimizingTime,
      long lastFullOptimizingTime,
      double availableCore,
      long maxInputSizePerThread,
      long planTime) {
    this.coreOptions = coreOptions;
    this.optimizingConfig = optimizingConfig == null ? new OptimizingConfig() : optimizingConfig;
    this.lastMinorOptimizingTime = lastMinorOptimizingTime;
    this.lastMajorOptimizingTime = lastMajorOptimizingTime;
    this.lastFullOptimizingTime = lastFullOptimizingTime;
    this.availableCore = availableCore;
    this.maxInputSizePerThread = maxInputSizePerThread;
    this.planTime = planTime;
    Options options = Options.fromMap(coreOptions.toMap());
    this.targetSize = coreOptions.targetFileSize(false);
    this.smallFileBoundary = smallFileBoundary(coreOptions, options, targetSize);
    this.minorMinFileNum = coreOptions.compactionMinFileNum();
    this.fileNumLimit =
        options.getInteger(COMPACTION_FILE_NUM_LIMIT, DEFAULT_COMPACTION_FILE_NUM_LIMIT);
    this.deleteRatioThreshold = coreOptions.compactionDeleteRatioThreshold();
    this.openFileCost = coreOptions.splitOpenFileCost();
  }

  private static long smallFileBoundary(CoreOptions coreOptions, Options options, long targetSize) {
    if (options.containsKey(COMPACTION_SMALL_FILE_RATIO)) {
      return (long) (targetSize * options.getDouble(COMPACTION_SMALL_FILE_RATIO, 0.7D));
    }
    return coreOptions.compactionFileSize(false);
  }

  static PaimonPlanContext forOptions(
      CoreOptions coreOptions,
      OptimizingConfig optimizingConfig,
      long lastMinorOptimizingTime,
      long lastMajorOptimizingTime,
      long lastFullOptimizingTime,
      double availableCore,
      long maxInputSizePerThread,
      long planTime) {
    return new PaimonPlanContext(
        coreOptions,
        optimizingConfig,
        lastMinorOptimizingTime,
        lastMajorOptimizingTime,
        lastFullOptimizingTime,
        availableCore,
        maxInputSizePerThread,
        planTime);
  }

  CoreOptions coreOptions() {
    return coreOptions;
  }

  OptimizingConfig optimizingConfig() {
    return optimizingConfig;
  }

  long lastMinorOptimizingTime() {
    return lastMinorOptimizingTime;
  }

  long lastMajorOptimizingTime() {
    return lastMajorOptimizingTime;
  }

  long lastFullOptimizingTime() {
    return lastFullOptimizingTime;
  }

  double availableCore() {
    return availableCore;
  }

  long maxInputSizePerThread() {
    return maxInputSizePerThread;
  }

  long planTime() {
    return planTime;
  }

  long targetSize() {
    return targetSize;
  }

  long smallFileBoundary() {
    return smallFileBoundary;
  }

  int minorMinFileNum() {
    return minorMinFileNum;
  }

  int fileNumLimit() {
    return fileNumLimit;
  }

  double deleteRatioThreshold() {
    return deleteRatioThreshold;
  }

  long openFileCost() {
    return openFileCost;
  }

  boolean reachMinorInterval() {
    return optimizingConfig.getMinorLeastInterval() >= 0
        && planTime - lastMinorOptimizingTime > optimizingConfig.getMinorLeastInterval();
  }

  boolean reachFullInterval() {
    return optimizingConfig.getFullTriggerInterval() >= 0
        && planTime - lastFullOptimizingTime > optimizingConfig.getFullTriggerInterval();
  }

  boolean fullRewriteAllFiles() {
    return optimizingConfig.isFullRewriteAllFiles();
  }

  long effectiveTaskInputLimit() {
    long configLimit = optimizingConfig.getMaxTaskSize();
    boolean hasConfigLimit = configLimit > 0;
    boolean hasThreadLimit = maxInputSizePerThread > 0;
    if (hasConfigLimit && hasThreadLimit) {
      return Math.min(configLimit, maxInputSizePerThread);
    }
    if (hasConfigLimit) {
      return configLimit;
    }
    if (hasThreadLimit) {
      return maxInputSizePerThread;
    }
    return OptimizerProperties.MAX_INPUT_FILE_SIZE_PER_THREAD_DEFAULT;
  }
}
