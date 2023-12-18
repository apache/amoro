/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.netease.arctic.server.process.optimizing;

import com.netease.arctic.server.persistence.TableRuntimePersistency;
import com.netease.arctic.server.persistence.mapper.OptimizingMapper;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;
import com.netease.arctic.ams.api.Action;
import com.netease.arctic.server.process.OptimizingState;
import com.netease.arctic.server.process.ProcessStatus;
import com.netease.arctic.server.process.TableState;
import com.netease.arctic.server.table.ServerTableIdentifier;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

public class DefaultOptimizingState extends TableState implements OptimizingState {

  private volatile long targetSnapshotId;
  private volatile long targetChangeSnapshotId;
  private volatile long lastOptimizedSnapshotId;
  private volatile long lastOptimizedChangeSnapshotId;
  private volatile long lastMinorOptimizingTime;
  private volatile long lastMajorOptimizingTime;
  private volatile long lastFullOptimizingTime;
  private volatile OptimizingType optimizingType;
  private volatile int taskCount;
  private volatile OptimizingStage stage;
  private volatile long currentStageStartTime;
  private volatile DefaultOptimizingProcess optimizingProcess;
  private volatile long quotaRuntime;
  private volatile int quotaTarget;
  private volatile PendingInput pendingInput;

  public DefaultOptimizingState(ServerTableIdentifier tableIdentifier,
                         TableRuntimePersistency tableRuntimePersistency) {
    super(Action.OPTIMIZING, tableIdentifier);
    this.targetSnapshotId = tableRuntimePersistency.getCurrentSnapshotId();
    this.targetChangeSnapshotId = tableRuntimePersistency.getCurrentChangeSnapshotId();
    this.lastOptimizedSnapshotId = tableRuntimePersistency.getLastOptimizedSnapshotId();
    this.lastOptimizedChangeSnapshotId = tableRuntimePersistency.getLastOptimizedChangeSnapshotId();
    this.currentStageStartTime = tableRuntimePersistency.getCurrentStatusStartTime();
    this.lastMinorOptimizingTime = tableRuntimePersistency.getLastMinorOptimizingTime();
    this.lastMajorOptimizingTime = tableRuntimePersistency.getLastMajorOptimizingTime();
    this.lastFullOptimizingTime = tableRuntimePersistency.getLastFullOptimizingTime();
  }

  public DefaultOptimizingState(ServerTableIdentifier tableIdentifier) {
    super(Action.OPTIMIZING, tableIdentifier);
  }

  public void release() {
    if (optimizingProcess != null) {
      optimizingProcess.close();
    }
  }

  @Override
  public String getName() {
    return stage.displayValue();
  }

  public long getCurrentStageStartTime() {
    return currentStageStartTime;
  }

  @Override
  public long getQuotaRuntime() {
    return optimizingProcess == null ? quotaRuntime : optimizingProcess.getQuotaRuntime();
  }

  public PendingInput getPendingInput() {
    return pendingInput;
  }

  @Override
  public double getQuotaValue() {
    return (double) getQuotaRuntime() / (System.currentTimeMillis() - getStartTime());
  }

  public double getQuotaOccupy() {
    return getQuotaValue() / quotaTarget;
  }

  public OptimizingStage getStage() {
    return stage;
  }


  public long getTargetSnapshotId() {
    return targetSnapshotId;
  }

  public long getTargetChangeSnapshotId() {
    return targetChangeSnapshotId;
  }

  public long getLastMinorOptimizingTime() {
    return lastMinorOptimizingTime;
  }

  public long getLastMajorOptimizingTime() {
    return lastMajorOptimizingTime;
  }

  public long getLastFullOptimizingTime() {
    return lastFullOptimizingTime;
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

  public int getQuotaTarget() {
    return quotaTarget;
  }

  public void setQuotaTarget(int quotaTarget) {
    this.quotaTarget = quotaTarget;
  }

  protected void saveProcessCreated(DefaultOptimizingProcess process) {
    Preconditions.checkState(stage == OptimizingStage.PENDING);
    invokeConsisitency(
        () -> {
          setId(process.getId());
          setStatus(ProcessStatus.RUNNING);
          optimizingProcess = process;
          savePlanningStage();
          process.whenCompleted(() -> saveProcessCompleted(process));
        }
    );
  }

  protected void saveProcessRecoverd(DefaultOptimizingProcess process) {
    optimizingProcess = process;
    process.whenCompleted(() -> saveProcessCompleted(process));
  }

  private void savePlanningStage() {
    setStage(OptimizingStage.PLANNING);
    setStartTime(currentStageStartTime);
    persistProcessCreated();
    persistRuntimeStage();
  }

  protected void saveProcessCompleted(DefaultOptimizingProcess process) {
    this.quotaRuntime = process.getQuotaRuntime();
    this.optimizingProcess = null;
    if (process.getStatus() == ProcessStatus.SUCCESS) {
      saveStatusSuccess(process.getSummary());
    } else if (process.getStatus() == ProcessStatus.FAILED) {
      saveStatusFailed(process.getFailedReason());
    }
  }

  public void savePendingInput(PendingInput input) {
    invokeConsisitency(
        () -> {
          targetSnapshotId = input.getCurrentSnapshotId();
          targetChangeSnapshotId = input.getCurrentChangeSnapshotId();
          pendingInput = input;
          if (stage != OptimizingStage.PENDING) {
            setStage(OptimizingStage.PENDING);
          }
          persistRuntimeStage();
        }
    );
  }

  protected void saveCommittingStage() {
    invokeConsisitency(
        () ->  {
          setStage(OptimizingStage.COMMITTING);
          persistRuntimeStage();
        }
    );
  }

  protected void saveOptimizingStage(OptimizingType optimizingType, int taskCount, String summary) {
    invokeConsisitency(
        () ->  {
          this.optimizingType = optimizingType;
          this.taskCount = taskCount;
          if (optimizingType == OptimizingType.MINOR) {
            lastMinorOptimizingTime = getStartTime();
          } else if (optimizingType == OptimizingType.MAJOR) {
            lastMajorOptimizingTime = getStartTime();
          } else if (optimizingType == OptimizingType.FULL) {
            lastFullOptimizingTime = getStartTime();
          }
          setStage(optimizingType.getStatus());
          persistRuntimeStage();
          persistProcessSummary(summary);
        }
    );
  }

  private void saveStatusSuccess(String summary) {
    invokeConsisitency(
        () -> {
          persistRuntimeSuccess();
          persistProcessSummary(summary);
        }
    );
  }

  private void saveStatusClosed() {
    Preconditions.checkState(getStatus() != ProcessStatus.SUCCESS,
        "Use saveStatusSuccess instead.");
    invokeConsisitency(
        () -> {
          setStatus(ProcessStatus.CLOSED);
          persistProcessStatus();
          persistRuntimeStage();
        }
    );
  }

  private void saveStatusFailed(String failedReason) {
    invokeConsisitency(
        () -> {
          setFailedReason(failedReason);
          persistProcessStatus();
          persistRuntimeStage();
        }
    );
  }

  private void setStage(OptimizingStage stage) {
    this.stage = stage;
    this.currentStageStartTime = System.currentTimeMillis();
  }

  @Override
  public void setStatus(ProcessStatus status) {
    super.setStatus(status);
    stage = OptimizingStage.IDLE;
    currentStageStartTime = getEndTime();
  }

  @Override
  public void setFailedReason(String failedReason) {
    super.setFailedReason(failedReason);
    this.stage = OptimizingStage.IDLE;
    this.currentStageStartTime = System.currentTimeMillis();
  }

  private void persistProcessCreated() {
    doAs(
        OptimizingMapper.class,
        mapper ->
            mapper.insertOptimizingProcess(
                getTableIdentifier(),
                getId(),
                targetSnapshotId,
                targetChangeSnapshotId,
                getStatus(),
                null,
                getStartTime(),
                null));
  }

  private void persistProcessStatus() {
    doAs(
        OptimizingMapper.class,
        mapper ->
            mapper.updateOptimizingProcess(
                getTableIdentifier().getId(),
                getId(),
                getStatus(),
                getEndTime(),
                getFailedReason()));
  }

  private void persistProcessSummary(String summary) {
    doAs(
        OptimizingMapper.class,
        mapper ->
            mapper.updateOptimizingProcess(
                getTableIdentifier().getId(),
                getId(),
                optimizingType,
                getEndTime(),
                getStatus(),
                taskCount,
                summary,
                getFailedReason()));
  }

  private void persistRuntimeSuccess() {
    doAs(
        TableMetaMapper.class,
        mapper ->
            mapper.updateTableOptimizingSuccess(
                getTableIdentifier().getId(),
                getId(),
                stage,
                lastOptimizedSnapshotId,
                lastOptimizedChangeSnapshotId,
                lastMinorOptimizingTime,
                lastMajorOptimizingTime,
                lastFullOptimizingTime,
                getStartTime())
    );
  }

  private void persistRuntimeStage() {
    doAs(
        TableMetaMapper.class,
        mapper ->
            mapper.updateTableStage(
                getTableIdentifier().getId(),
                getId(),
                stage,
                currentStageStartTime)
    );
  }
}
