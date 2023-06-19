package com.netease.arctic.server.dashboard.model;

import com.netease.arctic.server.dashboard.utils.FilesStatisticsBuilder;
import com.netease.arctic.server.optimizing.MetricsSummary;
import com.netease.arctic.server.optimizing.OptimizingProcess;
import com.netease.arctic.server.optimizing.OptimizingType;

public class TableOptimizingProcess {

  private Long tableId;
  private String catalogName;
  private String dbName;
  private String tableName;

  private Long processId;
  private long startTime;
  private OptimizingType optimizingType;
  private OptimizingProcess.Status status;
  private String failReason;
  private long duration;
  private int successTasks;
  private int totalTasks;
  private int runningTasks;
  private long finishTime;
  private FilesStatistics inputFiles;
  private FilesStatistics outputFiles;

  private Long targetSnapshotId;
  private Long targetChangeSnapshotId;
  private MetricsSummary summary;

  public TableOptimizingProcess() {
  }

  public Long getProcessId() {
    return processId;
  }

  public void setProcessId(Long processId) {
    this.processId = processId;
  }

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

  public Long getTargetSnapshotId() {
    return targetSnapshotId;
  }

  public void setTargetSnapshotId(Long targetSnapshotId) {
    this.targetSnapshotId = targetSnapshotId;
  }

  public OptimizingProcess.Status getStatus() {
    return status;
  }

  public void setStatus(OptimizingProcess.Status status) {
    this.status = status;
  }

  public OptimizingType getOptimizingType() {
    return optimizingType;
  }

  public void setOptimizingType(OptimizingType optimizingType) {
    this.optimizingType = optimizingType;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getFinishTime() {
    return finishTime;
  }

  public void setFinishTime(long finishTime) {
    this.finishTime = finishTime;
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

  public String getFailReason() {
    return failReason;
  }

  public void setFailReason(String failReason) {
    this.failReason = failReason;
  }

  public MetricsSummary getSummary() {
    return summary;
  }

  public void setSummary(MetricsSummary summary) {
    this.summary = summary;
  }

  public Long getTargetChangeSnapshotId() {
    return targetChangeSnapshotId;
  }

  public void setTargetChangeSnapshotId(Long targetChangeSnapshotId) {
    this.targetChangeSnapshotId = targetChangeSnapshotId;
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

  public void init() {
    if (finishTime > 0) {
      duration = finishTime - startTime;
    } else {
      duration = System.currentTimeMillis() - startTime;
    }
    duration = duration > 0 ? duration : 0;
    FilesStatisticsBuilder inputBuilder = new FilesStatisticsBuilder();
    inputBuilder.addFiles(summary.getEqualityDeleteSize(), summary.getEqDeleteFileCnt());
    inputBuilder.addFiles(summary.getPositionalDeleteSize(), summary.getPosDeleteFileCnt());
    inputBuilder.addFiles(summary.getRewriteDataSize(), summary.getRewriteDataFileCnt());
    inputBuilder.addFiles(summary.getRewritePosDataSize(), summary.getReRowDeletedDataFileCnt());
    FilesStatisticsBuilder outputBuilder = new FilesStatisticsBuilder();
    outputBuilder.addFiles(summary.getNewFileSize(), summary.getNewFileCnt());
    
    inputFiles = inputBuilder.build();
    outputFiles = outputBuilder.build();
  }
}
