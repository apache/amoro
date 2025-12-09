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

import org.apache.amoro.TableFormat;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.table.MixedTable;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.SnapshotSummary;
import org.apache.iceberg.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MetricBasedEvaluationEvent {
  private static final Logger logger = LoggerFactory.getLogger(MetricBasedEvaluationEvent.class);

  public static boolean isReachFallbackInterval(OptimizingConfig config, long lastPlanTime) {
    long fallbackInterval = config.getEvaluationFallbackInterval();
    return fallbackInterval >= 0 && System.currentTimeMillis() - lastPlanTime >= fallbackInterval;
  }

  public static boolean isEvaluatingNecessary(
      OptimizingConfig config, MixedTable table, long lastPlanTime) {
    if (table.format() != TableFormat.ICEBERG && table.format() != TableFormat.MIXED_ICEBERG) {
      logger.debug(
          "MetricBasedEvaluationEvent only support ICEBERG/MIXED_ICEBERG tables. Always return true for other table formats.");
      return true;
    }
    // Step 1: Perform periodic scheduling according to the fallback interval to avoid false
    // positives or
    // missed triggers based on metadata metric-driven evaluation
    if (isReachFallbackInterval(config, lastPlanTime)) {
      logger.info("Maximum interval for evaluating table {} has reached.", table.id());
      return true;
    }

    // Step 2: Empty table should skip evaluating pending input
    BasicTableStats basicStats = new BasicTableStats(table);
    if (basicStats.dataFileCnt == 0) {
      logger.info("Table {} contains no data files, skip evaluating pending input.", table.id());
      return false;
    }

    // Step 3: If the condition `delete file=0 && avg file size > target size * ratio` is satisfied,
    // then evaluating the pending input is considered unnecessary and will be skipped.
    long minTargetSize = (long) (config.getTargetSize() * config.getMinTargetSizeRatio());
    double avgFileSize =
        basicStats.dataFileCnt > 0 ? (double) basicStats.fileSize / basicStats.dataFileCnt : 0;

    if (basicStats.deleteFileCnt == 0 && avgFileSize > minTargetSize) {
      logger.info(
          "Table {} contains only appended data and no deleted files (average file size: {}), skip evaluating pending input.",
          table.id(),
          avgFileSize);
      return false;
    }

    return true;
  }

  public static boolean isPartitionPendingNecessary(
      OptimizingConfig config, long partitionSquaredErrorSum, long partitionFileCount) {
    long mseTolerance = config.getEvaluationMseTolerance();
    return partitionFileCount > 1
        && (mseTolerance == 0
            || (double) partitionSquaredErrorSum / partitionFileCount
                >= (double) mseTolerance * mseTolerance);
  }

  static class BasicTableStats {
    int deleteFileCnt = 0;
    int dataFileCnt = 0;
    long fileSize = 0;

    @VisibleForTesting
    BasicTableStats() {}

    BasicTableStats(MixedTable table) {
      if (table.isUnkeyedTable()) {
        acceptSnapshotIfPresent(table.asUnkeyedTable().currentSnapshot());
      } else {
        acceptSnapshotIfPresent(table.asKeyedTable().baseTable().currentSnapshot());
        acceptSnapshotIfPresent(table.asKeyedTable().changeTable().currentSnapshot());
      }
    }

    private void acceptSnapshotIfPresent(Snapshot snapshot) {
      if (snapshot != null) {
        accept(snapshot.summary());
      }
    }

    void accept(Map<String, String> summary) {
      deleteFileCnt +=
          PropertyUtil.propertyAsInt(summary, SnapshotSummary.TOTAL_DELETE_FILES_PROP, 0);
      dataFileCnt += PropertyUtil.propertyAsInt(summary, SnapshotSummary.TOTAL_DATA_FILES_PROP, 0);
      fileSize += PropertyUtil.propertyAsLong(summary, SnapshotSummary.TOTAL_FILE_SIZE_PROP, 0);
    }
  }
}
