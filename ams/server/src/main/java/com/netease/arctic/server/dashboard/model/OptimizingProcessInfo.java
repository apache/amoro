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

package com.netease.arctic.server.dashboard.model;

import com.netease.arctic.ams.api.process.ProcessStatus;
import com.netease.arctic.server.dashboard.utils.FilesStatisticsBuilder;
import com.netease.arctic.server.persistence.OptimizingProcessPersistency;
import com.netease.arctic.server.persistence.TaskRuntimePersistency;
import com.netease.arctic.server.process.OptimizingSummary;
import com.netease.arctic.server.process.OptimizingType;

import java.util.List;

public class OptimizingProcessInfo {
  private Long tableId;
  private String catalogName;
  private String dbName;
  private String tableName;

  private Long processId;
  private long startTime;
  private OptimizingType optimizingType;
  private ProcessStatus status;
  private String failReason;
  private long duration;
  private int successTasks;
  private int totalTasks;
  private int runningTasks;
  private long finishTime;
  private FilesStatistics inputFiles;
  private FilesStatistics outputFiles;

  private OptimizingSummary summary;

  public Long getTableId() {
    return tableId;
  }

  public void setTableId(Long tableId) {
    this.tableId = tableId;
  }

  public String getCatalogName() {
    return catalogName;
  }

  public void setCatalogName(String catalogName) {
    this.catalogName = catalogName;
  }

  public String getDbName() {
    return dbName;
  }

  public void setDbName(String dbName) {
    this.dbName = dbName;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public Long getProcessId() {
    return processId;
  }

  public void setProcessId(Long processId) {
    this.processId = processId;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public OptimizingType getOptimizingType() {
    return optimizingType;
  }

  public void setOptimizingType(OptimizingType optimizingType) {
    this.optimizingType = optimizingType;
  }

  public ProcessStatus getStatus() {
    return status;
  }

  public void setStatus(ProcessStatus status) {
    this.status = status;
  }

  public String getFailReason() {
    return failReason;
  }

  public void setFailReason(String failReason) {
    this.failReason = failReason;
  }

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public int getSuccessTasks() {
    return successTasks;
  }

  public void setSuccessTasks(int successTasks) {
    this.successTasks = successTasks;
  }

  public int getTotalTasks() {
    return totalTasks;
  }

  public void setTotalTasks(int totalTasks) {
    this.totalTasks = totalTasks;
  }

  public int getRunningTasks() {
    return runningTasks;
  }

  public void setRunningTasks(int runningTasks) {
    this.runningTasks = runningTasks;
  }

  public long getFinishTime() {
    return finishTime;
  }

  public void setFinishTime(long finishTime) {
    this.finishTime = finishTime;
  }

  public FilesStatistics getInputFiles() {
    return inputFiles;
  }

  public void setInputFiles(FilesStatistics inputFiles) {
    this.inputFiles = inputFiles;
  }

  public FilesStatistics getOutputFiles() {
    return outputFiles;
  }

  public void setOutputFiles(FilesStatistics outputFiles) {
    this.outputFiles = outputFiles;
  }

  public OptimizingSummary getSummary() {
    return summary;
  }

  public void setSummary(OptimizingSummary summary) {
    this.summary = summary;
  }

  public static OptimizingProcessInfo build(
      OptimizingProcessPersistency meta, List<TaskRuntimePersistency> optimizingTaskStats) {
    if (meta == null) {
      return null;
    }
    OptimizingProcessInfo result = new OptimizingProcessInfo();

    if (optimizingTaskStats != null) {
      int successTasks = 0;
      int runningTasks = 0;
      for (TaskRuntimePersistency optimizingTaskStat : optimizingTaskStats) {
        switch (optimizingTaskStat.getStatus()) {
          case SUCCESS:
            successTasks++;
            break;
          case SCHEDULED:
          case ACKED:
            runningTasks++;
            break;
        }
      }
      result.setTotalTasks(optimizingTaskStats.size());
      result.setSuccessTasks(successTasks);
      result.setRunningTasks(runningTasks);
    }
    FilesStatisticsBuilder inputBuilder = new FilesStatisticsBuilder();
    FilesStatisticsBuilder outputBuilder = new FilesStatisticsBuilder();
    OptimizingSummary summary = meta.getSummary();
    if (summary != null) {
      inputBuilder.addFiles(summary.getEqualityDeleteSize(), summary.getEqDeleteFileCnt());
      inputBuilder.addFiles(
          summary.getPositionDeleteSize() + summary.getPositionalDeleteSize(),
          summary.getPosDeleteFileCnt());
      inputBuilder.addFiles(summary.getRewriteDataSize(), summary.getRewriteDataFileCnt());
      inputBuilder.addFiles(
          summary.getRewritePosDataSize(),
          summary.getReRowDeletedDataFileCnt() + summary.getRewritePosDataFileCnt());
      outputBuilder.addFiles(summary.getNewFileSize(), summary.getNewFileCnt());
      outputBuilder.addFiles(summary.getNewDataSize(), summary.getNewDataFileCnt());
      outputBuilder.addFiles(summary.getNewDeleteSize(), summary.getNewDeleteFileCnt());
    }
    result.setInputFiles(inputBuilder.build());
    result.setOutputFiles(outputBuilder.build());

    result.setTableId(meta.getTableId());
    result.setCatalogName(meta.getCatalogName());
    result.setDbName(meta.getDbName());
    result.setTableName(meta.getTableName());

    result.setProcessId(meta.getProcessId());
    result.setStartTime(meta.getPlanTime());
    result.setOptimizingType(meta.getOptimizingType());
    result.setStatus(meta.getStatus());
    result.setFailReason(meta.getFailReason());
    result.setDuration(
        meta.getEndTime() > 0
            ? meta.getEndTime() - meta.getPlanTime()
            : System.currentTimeMillis() - meta.getPlanTime());
    result.setFinishTime(meta.getEndTime());
    result.setSummary(meta.getSummary());
    return result;
  }
}
