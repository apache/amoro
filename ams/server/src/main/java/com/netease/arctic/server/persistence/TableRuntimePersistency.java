package com.netease.arctic.server.persistence;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.process.ProcessStatus;
import com.netease.arctic.server.process.optimizing.OptimizingStage;
import com.netease.arctic.server.process.optimizing.OptimizingType;
import com.netease.arctic.ams.api.process.PendingInput;
import com.netease.arctic.ams.api.config.TableConfiguration;
import com.netease.arctic.server.table.DefaultTableRuntime;

public class TableRuntimePersistency {
  private long tableId;
  private String catalogName;
  private String dbName;
  private String tableName;
  private TableFormat format;
  private long currentSnapshotId;
  private long lastOptimizedSnapshotId;
  private long lastOptimizedChangeSnapshotId;
  private long currentChangeSnapshotId;
  private long lastMajorOptimizingTime;
  private long lastMinorOptimizingTime;
  private long lastFullOptimizingTime;
  private OptimizingStage tableStatus;
  private long currentStatusStartTime;
  private String optimizerGroup;
  private TableConfiguration tableConfig;
  private PendingInput pendingInput;
  private long optimizingProcessId = 0;
  private ProcessStatus processStatus;
  private OptimizingType optimizingType;
  private long targetSnapshotId;
  private long targetChangeSnapshotId;
  private long planTime;
  private long endTime;
  private String failReason;
  private String summary;
  private DefaultTableRuntime tableRuntime;

  public TableRuntimePersistency() {}

  public long getTargetSnapshotId() {
    return targetSnapshotId;
  }

  public OptimizingType getOptimizingType() {
    return optimizingType;
  }

  public long getLastOptimizedSnapshotId() {
    return lastOptimizedSnapshotId;
  }

  public long getLastOptimizedChangeSnapshotId() {
    return lastOptimizedChangeSnapshotId;
  }

  public long getTargetChangeSnapshotId() {
    return targetChangeSnapshotId;
  }

  public long getTableId() {
    return tableId;
  }

  public String getCatalogName() {
    return catalogName;
  }

  public String getDbName() {
    return dbName;
  }

  public String getTableName() {
    return tableName;
  }

  public TableFormat getFormat() {
    return format;
  }

  public long getCurrentSnapshotId() {
    return currentSnapshotId;
  }

  public long getCurrentChangeSnapshotId() {
    return currentChangeSnapshotId;
  }

  public long getLastMajorOptimizingTime() {
    return lastMajorOptimizingTime;
  }

  public long getLastMinorOptimizingTime() {
    return lastMinorOptimizingTime;
  }

  public long getLastFullOptimizingTime() {
    return lastFullOptimizingTime;
  }

  public OptimizingStage getTableStatus() {
    return tableStatus;
  }

  public long getCurrentStatusStartTime() {
    return currentStatusStartTime;
  }

  public String getOptimizerGroup() {
    return optimizerGroup;
  }

  public TableConfiguration getTableConfig() {
    return tableConfig;
  }

  public long getOptimizingProcessId() {
    return optimizingProcessId;
  }

  public ProcessStatus getProcessStatus() {
    return processStatus;
  }

  public String getSummary() {
    return summary;
  }

  public void setTableId(long tableId) {
    this.tableId = tableId;
  }

  public void setCatalogName(String catalogName) {
    this.catalogName = catalogName;
  }

  public void setDbName(String dbName) {
    this.dbName = dbName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public void setFormat(TableFormat format) {
    this.format = format;
  }

  public void setTableStatus(OptimizingStage tableStatus) {
    this.tableStatus = tableStatus;
  }

  public void setOptimizerGroup(String optimizerGroup) {
    this.optimizerGroup = optimizerGroup;
  }

  public void setTableConfig(TableConfiguration tableConfig) {
    this.tableConfig = tableConfig;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public long getPlanTime() {
    return planTime;
  }
}
