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

package org.apache.amoro.server.refresh.event;

import static org.apache.amoro.table.TablePartitionDetailProperties.FILE_SIZE_SQUARED_ERROR_SUM;
import static org.apache.amoro.table.TablePartitionDetailProperties.FILE_SIZE_SQUARED_ERROR_SUM_DEFAULT;

import org.apache.amoro.TableFormat;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.server.dashboard.MixedAndIcebergTableDescriptor;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.descriptor.PartitionBaseInfo;
import org.apache.amoro.utils.MemorySize;
import org.apache.iceberg.SnapshotSummary;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.iceberg.util.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class MetricBasedRefreshEvent {
  private static final Logger logger = LoggerFactory.getLogger(MetricBasedRefreshEvent.class);

  /**
   * Determines if evaluating pending input is necessary.
   *
   * @param tableRuntime The runtime information of the table.
   * @param table The table to be evaluated.
   * @return true if evaluation is necessary, otherwise false.
   */
  public static boolean isEvaluatingPendingInputNecessary(
      DefaultTableRuntime tableRuntime, MixedTable table) {
    if (table.format() != TableFormat.ICEBERG && table.format() != TableFormat.MIXED_ICEBERG) {
      logger.debug(
          "MetricBasedRefreshEvent only support ICEBERG/MIXED_ICEBERG tables. Always return true for other table formats.");
      return true;
    }

    // Perform periodic scheduling according to the fallback interval to avoid false positives or
    // missed triggers based on metadata metric-driven evaluation
    OptimizingConfig config = tableRuntime.getOptimizingConfig();
    if (reachFallbackInterval(
        tableRuntime.getLastPlanTime(), config.getEvaluationFallbackInterval())) {
      logger.info("Maximum interval for evaluating table {} has reached.", table.id());
      return true;
    }

    ExecutorService executorService = ThreadPools.getWorkerPool();
    MixedAndIcebergTableDescriptor formatTableDescriptor = new MixedAndIcebergTableDescriptor();
    formatTableDescriptor.withIoExecutor(executorService);

    // Step 1: If the condition `delete file=0 && avg file size > target size * ratio` is satisfied,
    // then evaluating the pending input is considered unnecessary and will be skipped.
    BasicStats basicStats = new BasicStats();
    if (table.isUnkeyedTable()) {
      basicStats.accept(table.asUnkeyedTable().currentSnapshot().summary());
    } else {
      basicStats.accept(table.asKeyedTable().baseTable().currentSnapshot().summary());
      basicStats.accept(table.asKeyedTable().changeTable().currentSnapshot().summary());
    }

    double avgFileSize =
        basicStats.deleteFileCnt + basicStats.dataFileCnt > 0
            ? (double) basicStats.fileSize / (basicStats.deleteFileCnt + basicStats.dataFileCnt)
            : 0;
    long minTargetSize = config.getTargetSize();

    if (basicStats.deleteFileCnt == 0
        && avgFileSize > config.getTargetSize() * config.getMinTargetSizeRatio()) {
      logger.info(
          "Table {} contains only appended data and no deleted files (average file size: {}), skip evaluating pending input.",
          table.id(),
          avgFileSize);
      return false;
    }

    double mseTolerance = minTargetSize - config.getAverageFileSizeTolerance().getBytes();
    if (config.getAverageFileSizeTolerance().getBytes() == 0) {
      logger.warn(
          "The minimum tolerance value for the average partition file size is set to 0, which will prevent any partition from being pended.");
      return false;
    }

    // Step 2: Calculate detail properties for each partition of a table including the sum of
    // squared errors of file sizes
    List<PartitionBaseInfo> partitionBaseInfos =
        formatTableDescriptor.getTablePartitionsWithDetailProperties(table, minTargetSize);

    List<PartitionBaseInfo> filteredPartitionBaseInfos =
        filterOutPartitionToBeOptimized(table, partitionBaseInfos, mseTolerance);

    long filteredPartitionCount = filteredPartitionBaseInfos.size();
    long filteredFileCount =
        filteredPartitionBaseInfos.stream().mapToLong(PartitionBaseInfo::getFileCount).sum();
    long totalPartitionCount = partitionBaseInfos.size();
    MseStats stats =
        partitionBaseInfos.stream().collect(MseStats::new, MseStats::accept, MseStats::combine);

    logger.info(
        "Filter out {} partitions ({} files in total) from {} ({} files in total) of table {} that have reached the tolerance value(T={}), MSE for total files: [{},{}].",
        filteredPartitionCount,
        filteredFileCount,
        totalPartitionCount,
        stats.totalFileCount,
        table.id(),
        mseTolerance,
        stats.mseTotalMin,
        stats.mseTotalMax);
    return !filteredPartitionBaseInfos.isEmpty();
  }

  /** Determines if the metric-based evalutation fallback interval has been reached. */
  private static boolean reachFallbackInterval(long lastPlanTime, long fallbackInterval) {
    return fallbackInterval >= 0 && System.currentTimeMillis() - lastPlanTime > fallbackInterval;
  }

  /**
   * Filters out partitions that need to be optimized based on the MSE tolerance.
   *
   * @param table The table being evaluated.
   * @param partitionBaseInfos Base information of the partitions.
   * @param tolerance The MSE tolerance value.
   * @return A list of partitions that need optimization.
   */
  @VisibleForTesting
  public static List<PartitionBaseInfo> filterOutPartitionToBeOptimized(
      MixedTable table, List<PartitionBaseInfo> partitionBaseInfos, double tolerance) {
    return partitionBaseInfos.stream()
        .filter(
            partition -> {
              double meanSquaredError =
                  (double)
                          partition.getPropertyOrDefault(
                              FILE_SIZE_SQUARED_ERROR_SUM, FILE_SIZE_SQUARED_ERROR_SUM_DEFAULT)
                      / partition.getFileCount();
              boolean isFiltered =
                  partition.getFileCount() > 1 && meanSquaredError >= tolerance * tolerance;
              logger.debug(
                  "Table: {}, Partition: {}, isFiltered: {}", table.id(), partition, isFiltered);
              return isFiltered;
            })
        .collect(Collectors.toList());
  }

  static class BasicStats {
    int deleteFileCnt = 0;
    int dataFileCnt = 0;
    long fileSize = 0;

    void accept(Map<String, String> summary) {
      deleteFileCnt +=
          PropertyUtil.propertyAsInt(summary, SnapshotSummary.TOTAL_DELETE_FILES_PROP, 0);
      dataFileCnt += PropertyUtil.propertyAsInt(summary, SnapshotSummary.TOTAL_DATA_FILES_PROP, 0);
      fileSize +=
          MemorySize.parse(
                  PropertyUtil.propertyAsString(summary, SnapshotSummary.TOTAL_FILE_SIZE_PROP, "0"))
              .getBytes();
    }
  }

  static class MseStats {
    double mseTotalMin = Double.MAX_VALUE;
    double mseTotalMax = Double.MIN_VALUE;
    long totalFileCount = 0L;

    void accept(PartitionBaseInfo p) {
      long fileCount = p.getFileCount();
      totalFileCount += fileCount;
      double mseTotal =
          ((double)
                  p.getPropertyOrDefault(
                      FILE_SIZE_SQUARED_ERROR_SUM, FILE_SIZE_SQUARED_ERROR_SUM_DEFAULT))
              / fileCount;
      mseTotalMin = Math.min(mseTotalMin, mseTotal);
      mseTotalMax = Math.max(mseTotalMax, mseTotal);
    }

    void combine(MseStats other) {
      mseTotalMin = Math.min(mseTotalMin, other.mseTotalMin);
      mseTotalMax = Math.max(mseTotalMax, other.mseTotalMax);
      totalFileCount += other.totalFileCount;
    }
  }
}
