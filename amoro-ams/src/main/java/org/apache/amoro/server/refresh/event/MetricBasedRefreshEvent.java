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
import org.apache.iceberg.util.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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
    if (table.format() != TableFormat.ICEBERG) {
      logger.debug(
          "EventBasedRefreshEvent only support ICEBERG tables. Always return true for other table formats.");
      return true;
    }

    // Perform periodic scheduling according to the minor/full interval to avoid false positives or
    // missed triggers based on metadata metric-driven evaluation
    OptimizingConfig config = tableRuntime.getOptimizingConfig();
    if (reachMinorInterval(
            tableRuntime.getLastMinorOptimizingTime(), config.getMinorLeastInterval())
        || reachFullInterval(
            tableRuntime.getLastFullOptimizingTime(), config.getFullTriggerInterval())) {
      logger.info("Maximum interval for minor optimization/full optimization has reached.");
      return true;
    }

    long minTargetSize = config.getTargetSize();
    int minPartitionCountToOptimized = 1;
    double mseTolerance = minTargetSize - config.getAverageFileSizeTolerance().getBytes();
    if (config.getAverageFileSizeTolerance().getBytes() == 0) {
      logger.warn(
          "The minimum tolerance value for the average partition file size is set to 0, which will prevent any partition from being pended.");
      return false;
    }

    ExecutorService executorService = ThreadPools.getWorkerPool();
    MixedAndIcebergTableDescriptor formatTableDescriptor = new MixedAndIcebergTableDescriptor();
    formatTableDescriptor.withIoExecutor(executorService);
    // Calculate detail properties for each partition of a table including the sum of squared errors
    // of file sizes
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
        config.getAverageFileSizeTolerance(),
        stats.mseTotalMin,
        stats.mseTotalMax);
    return filteredPartitionBaseInfos.size() >= minPartitionCountToOptimized;
  }

  /** Determines if the minor optimization interval has been reached. */
  private static boolean reachMinorInterval(long lastMinorOptimizingTime, long minorLeastInterval) {
    return minorLeastInterval >= 0
        && System.currentTimeMillis() - lastMinorOptimizingTime > minorLeastInterval;
  }

  /** Determines if the full optimization interval has been reached. */
  private static boolean reachFullInterval(long lastFullOptimizingTime, long fullTriggerInterval) {
    return fullTriggerInterval >= 0
        && System.currentTimeMillis() - lastFullOptimizingTime > fullTriggerInterval;
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
