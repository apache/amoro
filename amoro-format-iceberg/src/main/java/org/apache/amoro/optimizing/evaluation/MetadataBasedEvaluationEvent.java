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

package org.apache.amoro.optimizing.evaluation;

import static org.apache.amoro.TableFormat.ICEBERG;
import static org.apache.amoro.TableFormat.MIXED_HIVE;
import static org.apache.amoro.TableFormat.MIXED_ICEBERG;

import org.apache.amoro.TableFormat;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.table.MixedTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataBasedEvaluationEvent {
  private static final Logger logger = LoggerFactory.getLogger(MetadataBasedEvaluationEvent.class);

  public static boolean isReachFallbackInterval(OptimizingConfig config, long lastPlanTime) {
    long fallbackInterval = config.getEvaluationFallbackInterval();
    return fallbackInterval >= 0 && System.currentTimeMillis() - lastPlanTime >= fallbackInterval;
  }

  public static boolean isEvaluatingNecessary(
      OptimizingConfig config, MixedTable table, long lastPlanTime) {
    // Step 1: Perform periodic scheduling according to the fallback interval to avoid false
    // positives or
    // missed triggers based on metadata metric-driven evaluation
    if (isReachFallbackInterval(config, lastPlanTime)) {
      logger.debug("Maximum interval for evaluating table {} has reached.", table.id());
      return true;
    }

    TableStatsProvider.BasicFileStats basicStats = getTableStats(table);
    // Currently only supports ICEBERG and mixed formats (MIXED_ICEBERG, MIXED_HIVE).
    // For other formats this will return null.
    if (basicStats != null) {
      // Step 2: Empty table should skip evaluating pending input
      //    TableStatsProvider provider = getTableStatsProvider(table);
      if (basicStats.dataFileCnt == 0) {
        logger.debug("Table {} contains no data files, skip evaluating pending input.", table.id());
        return false;
      }

      // Step 3: If the condition `delete file=0 && avg file size > target size * ratio` is
      // satisfied,
      // then evaluating the pending input is considered unnecessary and will be skipped.
      long minTargetSize = (long) (config.getTargetSize() * config.getMinTargetSizeRatio());
      double avgFileSize =
          basicStats.dataFileCnt > 0
              ? (double) basicStats.totalFileSize / basicStats.dataFileCnt
              : 0;

      if (basicStats.deleteFileCnt == 0 && avgFileSize > minTargetSize) {
        logger.debug(
            "Table {} contains only appended data and no deleted files (average file size: {}), skip evaluating pending input.",
            table.id(),
            avgFileSize);
        return false;
      }
    }
    return true;
  }

  private static TableStatsProvider.BasicFileStats getTableStats(MixedTable table) {
    TableFormat format = table.format();
    if (format.equals(ICEBERG) || format.equals(MIXED_ICEBERG) || format.equals(MIXED_HIVE)) {
      TableStatsProvider provider = MixedAndIcebergTableStatsProvider.INSTANCE;
      return provider.collect(table);
    } else {
      // Unsupported table format for metadata-based evaluation
      // PAIMON and HUDI formats are currently not supported because obtaining basic statistics
      // requires traversing manifest files, which would add extra planning overhead.
      // In the future, if there is a more efficient way to get these stats,
      // we can extend TableStatsProvider to support these formats.
      return null;
    }
  }

  public static boolean isPartitionPendingNecessary(
      OptimizingConfig config, long partitionSquaredErrorSum, long partitionFileCount) {
    long mseTolerance = config.getEvaluationMseTolerance();
    return partitionFileCount > 1
        && (mseTolerance == 0
            || (double) partitionSquaredErrorSum / partitionFileCount
                >= (double) mseTolerance * mseTolerance);
  }
}
