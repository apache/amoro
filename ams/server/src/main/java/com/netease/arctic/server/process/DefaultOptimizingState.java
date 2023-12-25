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

package com.netease.arctic.server.process;

import com.netease.arctic.ams.api.Action;
import com.netease.arctic.ams.api.ServerTableIdentifier;
import com.netease.arctic.ams.api.process.OptimizingStage;
import com.netease.arctic.ams.api.process.OptimizingState;
import com.netease.arctic.ams.api.process.PendingInput;
import com.netease.arctic.ams.api.process.ProcessStatus;
import com.netease.arctic.server.persistence.StatedPersistentBase;
import com.netease.arctic.server.persistence.TableRuntimePersistency;
import com.netease.arctic.server.persistence.mapper.OptimizingMapper;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

public class DefaultOptimizingState extends OptimizingState {

  private final PersistenceHelper persistenceHelper = new PersistenceHelper();

  @StatedPersistentBase.StateField private volatile long lastOptimizedSnapshotId;
  private volatile long lastOptimizedChangeSnapshotId;
  private volatile long lastMinorOptimizingTime;
  private volatile long lastMajorOptimizingTime;
  private volatile long lastFullOptimizingTime;
  private volatile OptimizingType optimizingType;
  private volatile int taskCount;
  private volatile DefaultOptimizingProcess optimizingProcess;
  private volatile long quotaRuntime;
  private volatile double quotaTarget;
  private volatile PendingInput pendingInput;

  public DefaultOptimizingState(
      ServerTableIdentifier tableIdentifier, TableRuntimePersistency tableRuntimePersistency) {
    super(Action.OPTIMIZING, tableIdentifier);
    this.lastOptimizedSnapshotId = tableRuntimePersistency.getLastOptimizedSnapshotId();
    this.lastOptimizedChangeSnapshotId = tableRuntimePersistency.getLastOptimizedChangeSnapshotId();
    this.lastMinorOptimizingTime = tableRuntimePersistency.getLastMinorOptimizingTime();
    this.lastMajorOptimizingTime = tableRuntimePersistency.getLastMajorOptimizingTime();
    this.lastFullOptimizingTime = tableRuntimePersistency.getLastFullOptimizingTime();
    setTargetSnapshotId(tableRuntimePersistency.getCurrentSnapshotId());
    setTargetChangeSnapshotId(tableRuntimePersistency.getCurrentChangeSnapshotId());
    setStage(
        tableRuntimePersistency.getOptimizingStage(),
        tableRuntimePersistency.getCurrentStatusStartTime());
  }

  public DefaultOptimizingState(ServerTableIdentifier tableIdentifier) {
    super(Action.OPTIMIZING, tableIdentifier);
  }

  public void release() {
    if (optimizingProcess != null) {
      optimizingProcess.close();
    }
  }

  public PendingInput getPendingInput() {
    return pendingInput;
  }

  @Override
  public long getQuotaRuntime() {
    return quotaRuntime;
  }

  @Override
  public double getQuotaValue() {
    return (double) getQuotaRuntime() / (System.currentTimeMillis() - getStartTime());
  }

  public double getQuotaOccupy() {
    return getQuotaValue() / quotaTarget;
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

  public double getTargetQuota() {
    return quotaTarget;
  }

  public void setQuotaTarget(double quotaTarget) {
    this.quotaTarget = quotaTarget;
  }

  protected void saveProcessCreated(DefaultOptimizingProcess process) {
    Preconditions.checkState(getStage() == OptimizingStage.PENDING);
    persistenceHelper.invoke(
        () -> {
          setId(process.getId());
          setStatus(ProcessStatus.RUNNING);
          optimizingProcess = process;
          savePlanningStage();
          process.whenCompleted(() -> saveProcessCompleted(process));
        });
  }

  protected void saveProcessRecoverd(DefaultOptimizingProcess process) {
    optimizingProcess = process;
    process.whenCompleted(() -> saveProcessCompleted(process));
  }

  private void savePlanningStage() {
    setStage(OptimizingStage.PLANNING);
    setStartTime(getCurrentStageStartTime());
    persistenceHelper.persistProcessCreated();
    persistenceHelper.persistRuntimeStage();
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
    persistenceHelper.invoke(
        () -> {
          setTargetSnapshotId(input.getCurrentSnapshotId());
          setTargetChangeSnapshotId(input.getCurrentChangeSnapshotId());
          pendingInput = input;
          if (getStage() != OptimizingStage.PENDING) {
            setStage(OptimizingStage.PENDING);
          }
          persistenceHelper.persistRuntimeStage();
        });
  }

  protected void saveCommittingStage() {
    persistenceHelper.invoke(
        () -> {
          setStage(OptimizingStage.COMMITTING);
          persistenceHelper.persistRuntimeStage();
        });
  }

  protected void saveOptimizingStage(OptimizingType optimizingType, int taskCount, String summary) {
    persistenceHelper.invoke(
        () -> {
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
          persistenceHelper.persistRuntimeStage();
          persistenceHelper.persistProcessSummary(summary);
        });
  }

  private void saveStatusSuccess(String summary) {
    persistenceHelper.invoke(
        () -> {
          persistenceHelper.persistRuntimeSuccess();
          persistenceHelper.persistProcessSummary(summary);
        });
  }

  private void saveStatusClosed() {
    Preconditions.checkState(
        getStatus() != ProcessStatus.SUCCESS, "Use saveStatusSuccess instead.");
    persistenceHelper.invoke(
        () -> {
          setStatus(ProcessStatus.CLOSED);
          persistenceHelper.persistProcessStatus();
          persistenceHelper.persistRuntimeStage();
        });
  }

  private void saveStatusFailed(String failedReason) {
    persistenceHelper.invoke(
        () -> {
          setFailedReason(failedReason);
          persistenceHelper.persistProcessStatus();
          persistenceHelper.persistRuntimeStage();
        });
  }

  @Override
  public void setStatus(ProcessStatus status) {
    super.setStatus(status);
    setStage(OptimizingStage.IDLE, getEndTime());
  }

  @Override
  public void setFailedReason(String failedReason) {
    super.setFailedReason(failedReason);
    setStage(OptimizingStage.IDLE, getEndTime());
  }

  public void setTargetQuota(double targetQuota) {
    this.quotaTarget = targetQuota;
  }

  private class PersistenceHelper extends StatedPersistentBase {

    public void invoke(Runnable runnable) {
      persistenceHelper.invoke(runnable);
    }

    public void persistProcessCreated() {
      doAs(
          OptimizingMapper.class,
          mapper ->
              mapper.insertOptimizingProcess(
                  getTableIdentifier(),
                  getId(),
                  getTargetSnapshotId(),
                  getTargetChangeSnapshotId(),
                  getStatus(),
                  null,
                  getStartTime(),
                  null));
    }

    public void persistProcessStatus() {
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

    public void persistProcessSummary(String summary) {
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

    public void persistRuntimeSuccess() {
      doAs(
          TableMetaMapper.class,
          mapper ->
              mapper.updateTableOptimizingSuccess(
                  getTableIdentifier().getId(),
                  getId(),
                  getStage(),
                  lastOptimizedSnapshotId,
                  lastOptimizedChangeSnapshotId,
                  lastMinorOptimizingTime,
                  lastMajorOptimizingTime,
                  lastFullOptimizingTime,
                  getStartTime()));
    }

    public void persistRuntimeStage() {
      doAs(
          TableMetaMapper.class,
          mapper ->
              mapper.updateTableStage(
                  getTableIdentifier().getId(), getId(), getStage(), getCurrentStageStartTime()));
    }
  }
}
