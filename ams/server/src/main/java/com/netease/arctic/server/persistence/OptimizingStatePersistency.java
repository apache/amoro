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

package com.netease.arctic.server.persistence;

import com.netease.arctic.ams.api.process.OptimizingStage;
import com.netease.arctic.ams.api.process.ProcessStatus;
import com.netease.arctic.server.process.OptimizingType;

/** Meta of optimizing process. */
public class OptimizingStatePersistency {

  private Long processId;
  private Long tableId;
  private String catalogName;
  private String dbName;
  private String tableName;
  private Long targetSnapshotId;
  private Long lastSnapshotId;
  private long watermark;
  private Long lastOptimizingTime;
  private ProcessStatus status;
  private OptimizingStage stage;
  private Long currentStageStartTime;
  private OptimizingType optimizingType;
  private long planTime;
  private long endTime;
  private String failReason;
  private String summary;

  public OptimizingStatePersistency() {}

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

  public ProcessStatus getStatus() {
    return status;
  }

  public void setStatus(ProcessStatus status) {
    this.status = status;
  }

  public OptimizingType getOptimizingType() {
    return optimizingType;
  }

  public void setOptimizingType(OptimizingType optimizingType) {
    this.optimizingType = optimizingType;
  }

  public long getPlanTime() {
    return planTime;
  }

  public void setPlanTime(long planTime) {
    this.planTime = planTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public String getFailReason() {
    return failReason;
  }

  public void setFailReason(String failReason) {
    this.failReason = failReason;
  }

  public String getSummary() {
    return summary;
  }

  public long getLastOptimizingTime() {
    return lastOptimizingTime;
  }

  public long getLastSnapshotId() {
    return lastSnapshotId;
  }

  public long getWatermark() {
    return watermark;
  }

  public OptimizingStage getStage() {
    return stage;
  }

  public long getCurrentStageStartTime() {
    return currentStageStartTime;
  }
}
