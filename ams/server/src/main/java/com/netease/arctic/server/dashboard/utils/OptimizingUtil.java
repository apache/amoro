package com.netease.arctic.server.dashboard.utils;

import com.netease.arctic.server.dashboard.model.FilesStatistics;
import com.netease.arctic.server.dashboard.model.TableOptimizingInfo;
import com.netease.arctic.server.optimizing.MetricsSummary;
import com.netease.arctic.server.optimizing.OptimizingProcess;
import com.netease.arctic.server.optimizing.OptimizingStatus;
import com.netease.arctic.server.optimizing.plan.OptimizingEvaluator;
import com.netease.arctic.server.table.TableRuntime;
import org.apache.iceberg.ContentFile;

public class OptimizingUtil {

  /**
   * Build current table optimize info.
   *
   * @return TableOptimizeInfo
   */
  public static TableOptimizingInfo buildTableOptimizeInfo(TableRuntime optimizingTableRuntime) {
    OptimizingProcess process = optimizingTableRuntime.getOptimizingProcess();
    TableOptimizingInfo tableOptimizeInfo =
        new TableOptimizingInfo(optimizingTableRuntime.getTableIdentifier());
    tableOptimizeInfo.setOptimizeStatus(
        optimizingTableRuntime.getOptimizingStatus().displayValue());
    tableOptimizeInfo.setDuration(
        System.currentTimeMillis() - optimizingTableRuntime.getCurrentStatusStartTime());
    tableOptimizeInfo.setQuota(optimizingTableRuntime.getTargetQuota());
    tableOptimizeInfo.setQuotaOccupation(optimizingTableRuntime.calculateQuotaOccupy());
    FilesStatistics optimizeFileInfo;
    if (optimizingTableRuntime.getOptimizingStatus().isProcessing()) {
      optimizeFileInfo = collectOptimizingFileInfo(process == null ? null : process.getSummary());
    } else if (optimizingTableRuntime.getOptimizingStatus() == OptimizingStatus.PENDING) {
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

  private static FilesStatistics collectPendingFileInfo(
      OptimizingEvaluator.PendingInput pendingInput) {
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
