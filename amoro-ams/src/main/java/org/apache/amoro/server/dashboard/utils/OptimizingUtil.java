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

package org.apache.amoro.server.dashboard.utils;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.optimizing.MetricsSummary;
import org.apache.amoro.optimizing.plan.AbstractOptimizingEvaluator;
import org.apache.amoro.server.dashboard.model.TableOptimizingInfo;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.optimizing.OptimizingTaskMeta;
import org.apache.amoro.server.optimizing.TaskRuntime;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.table.descriptor.FilesStatistics;
import org.apache.iceberg.ContentFile;

import java.util.List;
import java.util.stream.Collectors;

public class OptimizingUtil {

  /**
   * Build current table optimize info.
   *
   * @return TableOptimizeInfo
   */
  public static TableOptimizingInfo buildTableOptimizeInfo(
      TableRuntimeMeta optimizingTableRuntime,
      List<OptimizingTaskMeta> processTasks,
      List<TaskRuntime.TaskQuota> quotas) {
    ServerTableIdentifier identifier =
        ServerTableIdentifier.of(
            optimizingTableRuntime.getTableId(),
            optimizingTableRuntime.getCatalogName(),
            optimizingTableRuntime.getDbName(),
            optimizingTableRuntime.getTableName(),
            optimizingTableRuntime.getFormat());
    TableOptimizingInfo tableOptimizeInfo = new TableOptimizingInfo(identifier);
    OptimizingStatus optimizingStatus = optimizingTableRuntime.getTableStatus();
    tableOptimizeInfo.setOptimizeStatus(optimizingStatus.displayValue());
    tableOptimizeInfo.setDuration(
        System.currentTimeMillis() - optimizingTableRuntime.getCurrentStatusStartTime());
    OptimizingConfig optimizingConfig =
        optimizingTableRuntime.getTableConfig().getOptimizingConfig();
    tableOptimizeInfo.setQuota(optimizingConfig.getTargetQuota());
    double quotaOccupy =
        calculateQuotaOccupy(
            processTasks,
            quotas,
            optimizingTableRuntime.getCurrentStatusStartTime(),
            System.currentTimeMillis());
    tableOptimizeInfo.setQuotaOccupation(quotaOccupy);
    FilesStatistics optimizeFileInfo;
    if (optimizingStatus.isProcessing()) {
      MetricsSummary summary = null;
      if (processTasks != null && !processTasks.isEmpty()) {
        List<MetricsSummary> taskSummary =
            processTasks.stream()
                .map(OptimizingTaskMeta::getMetricsSummary)
                .collect(Collectors.toList());
        summary = new MetricsSummary(taskSummary);
      }
      optimizeFileInfo = collectOptimizingFileInfo(summary);
    } else if (optimizingStatus == OptimizingStatus.PENDING) {
      optimizeFileInfo = collectPendingFileInfo(optimizingTableRuntime.getPendingInput());
    } else {
      optimizeFileInfo = null;
    }
    if (optimizeFileInfo != null) {
      tableOptimizeInfo.setFileCount(optimizeFileInfo.getFileCnt());
      tableOptimizeInfo.setFileSize(optimizeFileInfo.getTotalSize());
    }
    tableOptimizeInfo.setGroupName(optimizingTableRuntime.getOptimizerGroup());
    return tableOptimizeInfo;
  }

  private static double calculateQuotaOccupy(
      List<OptimizingTaskMeta> processTasks,
      List<TaskRuntime.TaskQuota> quotas,
      long startTime,
      long endTime) {
    double finishedOccupy = 0;
    if (quotas != null) {
      finishedOccupy = quotas.stream().mapToDouble(q -> q.getQuotaTime(startTime)).sum();
    }
    double runningOccupy = 0;
    if (processTasks != null) {
      runningOccupy =
          processTasks.stream()
              .mapToDouble(
                  t ->
                      TaskRuntime.taskRunningQuotaTime(
                          startTime, endTime, t.getStartTime(), t.getCostTime()))
              .sum();
    }
    return finishedOccupy + runningOccupy;
  }

  private static FilesStatistics collectPendingFileInfo(
      AbstractOptimizingEvaluator.PendingInput pendingInput) {
    if (pendingInput == null) {
      return null;
    }
    return FilesStatistics.builder()
        .addFiles(pendingInput.getDataFileSize(), pendingInput.getDataFileCount())
        .addFiles(pendingInput.getEqualityDeleteBytes(), pendingInput.getEqualityDeleteFileCount())
        .addFiles(
            pendingInput.getPositionalDeleteBytes(), pendingInput.getPositionalDeleteFileCount())
        .build();
  }

  private static FilesStatistics collectOptimizingFileInfo(MetricsSummary metricsSummary) {
    if (metricsSummary == null) {
      return null;
    }
    return FilesStatistics.builder()
        .addFiles(metricsSummary.getEqualityDeleteSize(), metricsSummary.getEqDeleteFileCnt())
        .addFiles(
            Math.max(
                metricsSummary.getPositionalDeleteSize(), metricsSummary.getPositionDeleteSize()),
            metricsSummary.getPosDeleteFileCnt())
        .addFiles(metricsSummary.getRewriteDataSize(), metricsSummary.getRewriteDataFileCnt())
        .build();
  }

  public static long getFileSize(ContentFile<?>[] contentFiles) {
    long size = 0;
    if (contentFiles != null) {
      for (ContentFile<?> contentFile : contentFiles) {
        size += contentFile.fileSizeInBytes();
      }
    }
    return size;
  }

  public static int getFileCount(ContentFile<?>[] contentFiles) {
    return contentFiles == null ? 0 : contentFiles.length;
  }

  public static long getRecordCnt(ContentFile<?>[] contentFiles) {
    int recordCnt = 0;
    if (contentFiles != null) {
      for (ContentFile<?> contentFile : contentFiles) {
        recordCnt += contentFile.recordCount();
      }
    }
    return recordCnt;
  }
}
