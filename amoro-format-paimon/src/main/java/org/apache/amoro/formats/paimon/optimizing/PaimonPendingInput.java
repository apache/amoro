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

package org.apache.amoro.formats.paimon.optimizing;

import java.util.Collection;
import java.util.Map;

/** Paimon-specific pending input metrics collected during the refresh phase. */
public class PaimonPendingInput implements org.apache.amoro.table.FormatPendingInput {

  // ---- Workload dimension ----
  private int dataFileCount;
  private long dataFileSize;
  private long dataRecordCount;

  // ---- Urgency dimension ----
  private int smallFileCount;
  private long smallFileSize;
  private int partitionCount;

  // ---- Delete vector dimension ----
  private int fileWithDeleteCount;
  private long deleteRecordCount;

  // ---- Health score ----
  private int healthScore;

  public PaimonPendingInput() {}

  public PaimonPendingInput(
      int dataFileCount,
      long dataFileSize,
      long dataRecordCount,
      int smallFileCount,
      long smallFileSize,
      int partitionCount,
      int fileWithDeleteCount,
      long deleteRecordCount,
      int healthScore) {
    this.dataFileCount = dataFileCount;
    this.dataFileSize = dataFileSize;
    this.dataRecordCount = dataRecordCount;
    this.smallFileCount = smallFileCount;
    this.smallFileSize = smallFileSize;
    this.partitionCount = partitionCount;
    this.fileWithDeleteCount = fileWithDeleteCount;
    this.deleteRecordCount = deleteRecordCount;
    this.healthScore = healthScore;
  }

  /**
   * Compute a composite health score (0-100, 100 = fully healthy).
   *
   * <p>Formula: {@code smallFileScore * 0.60 + deleteScore * 0.20 + distributionScore * 0.20}
   *
   * <p>Edge cases: empty table (dataFileCount == 0 or partitionFiles empty) returns 100.
   */
  public static int computeHealthScore(
      int dataFileCount,
      long dataFileSize,
      int smallFileCount,
      long smallFileSize,
      long dataRecordCount,
      long deleteRecordCount,
      Map<?, ? extends Collection<?>> partitionFiles) {
    if (dataFileCount == 0 || partitionFiles.isEmpty()) {
      return 100;
    }

    double smallFileCountScore = healthyPercentage(smallFileCount, dataFileCount);
    double smallFileSizeScore =
        dataFileSize <= 0 ? smallFileCountScore : healthyPercentage(smallFileSize, dataFileSize);
    double smallFileScore = clampScore(smallFileCountScore * 0.5 + smallFileSizeScore * 0.5);

    double deleteScore = healthyPercentage(deleteRecordCount, Math.max(dataRecordCount, 1));

    // distributionScore (weight 20%): evenness of file distribution across partitions
    int partitionCount = partitionFiles.size();
    double avgFilesPerPartition = (double) dataFileCount / partitionCount;
    double variance = 0.0;
    for (Collection<?> files : partitionFiles.values()) {
      double diff = files.size() - avgFilesPerPartition;
      variance += diff * diff;
    }
    variance /= partitionCount;
    double stdDev = Math.sqrt(variance);
    double distributionScore =
        clampScore(100.0 * (1.0 - Math.min(stdDev / Math.max(avgFilesPerPartition, 1.0), 1.0)));

    return (int)
        Math.round(
            clampScore(smallFileScore * 0.60 + deleteScore * 0.20 + distributionScore * 0.20));
  }

  private static double healthyPercentage(long unhealthyValue, long totalValue) {
    if (totalValue <= 0) {
      return 100.0;
    }
    return clampScore(100.0 * (1.0 - (double) unhealthyValue / totalValue));
  }

  private static double clampScore(double score) {
    if (Double.isNaN(score) || score < 0.0) {
      return 0.0;
    }
    if (score > 100.0) {
      return 100.0;
    }
    return score;
  }

  public int getDataFileCount() {
    return dataFileCount;
  }

  public void setDataFileCount(int dataFileCount) {
    this.dataFileCount = dataFileCount;
  }

  public long getDataFileSize() {
    return dataFileSize;
  }

  public void setDataFileSize(long dataFileSize) {
    this.dataFileSize = dataFileSize;
  }

  public long getDataRecordCount() {
    return dataRecordCount;
  }

  public void setDataRecordCount(long dataRecordCount) {
    this.dataRecordCount = dataRecordCount;
  }

  public int getSmallFileCount() {
    return smallFileCount;
  }

  public void setSmallFileCount(int smallFileCount) {
    this.smallFileCount = smallFileCount;
  }

  public long getSmallFileSize() {
    return smallFileSize;
  }

  public void setSmallFileSize(long smallFileSize) {
    this.smallFileSize = smallFileSize;
  }

  public int getPartitionCount() {
    return partitionCount;
  }

  public void setPartitionCount(int partitionCount) {
    this.partitionCount = partitionCount;
  }

  public int getFileWithDeleteCount() {
    return fileWithDeleteCount;
  }

  public void setFileWithDeleteCount(int fileWithDeleteCount) {
    this.fileWithDeleteCount = fileWithDeleteCount;
  }

  public long getDeleteRecordCount() {
    return deleteRecordCount;
  }

  public void setDeleteRecordCount(long deleteRecordCount) {
    this.deleteRecordCount = deleteRecordCount;
  }

  public int getHealthScore() {
    return healthScore;
  }

  public void setHealthScore(int healthScore) {
    this.healthScore = healthScore;
  }

  @Override
  public int getTotalFileCount() {
    return getDataFileCount();
  }

  @Override
  public long getTotalFileSize() {
    return getDataFileSize();
  }
}
