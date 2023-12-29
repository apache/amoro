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

import com.netease.arctic.ams.api.ServerTableIdentifier;
import com.netease.arctic.ams.api.StateField;
import com.netease.arctic.ams.api.process.OptimizingStage;
import com.netease.arctic.ams.api.process.OptimizingState;
import com.netease.arctic.ams.api.process.ProcessStatus;
import com.netease.arctic.server.persistence.OptimizingStatePersistency;
import com.netease.arctic.server.persistence.StatedPersistentBase;
import com.netease.arctic.server.persistence.mapper.ProcessMapper;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

// TODO persist codes
public class DefaultOptimizingState extends OptimizingState {

  private final PersistenceHelper persistenceHelper = new PersistenceHelper();
  private final OptimizingType optimizingType;
  @StateField private volatile long lastOptimizedSnapshotId;
  @StateField private volatile long lastOptimizingTime;
  @StateField private volatile int taskCount;
  private volatile DefaultOptimizingProcess optimizingProcess;

  public DefaultOptimizingState(
      ServerTableIdentifier tableIdentifier,
      OptimizingType optimizingType,
      OptimizingStatePersistency statePersistency) {
    super(optimizingType.getAction(), tableIdentifier);
    this.optimizingType = optimizingType;
    this.lastOptimizedSnapshotId = statePersistency.getLastSnapshotId();
    this.lastOptimizingTime = statePersistency.getLastOptimizingTime();
    setTargetSnapshotId(statePersistency.getTargetSnapshotId());
    setStage(statePersistency.getStage(), statePersistency.getCurrentStageStartTime());
  }

  public DefaultOptimizingState(
      ServerTableIdentifier tableIdentifier, OptimizingType optimizingType) {
    super(optimizingType.getAction(), tableIdentifier);
    this.optimizingType = optimizingType;
  }

  public boolean isPending() {
    return getStatus() != ProcessStatus.RUNNING && getStage() == OptimizingStage.PENDING;
  }

  public void release() {
    if (optimizingProcess != null) {
      optimizingProcess.close();
    }
  }

  public long getLastOptimizingTime() {
    return lastOptimizingTime;
  }

  public OptimizingType getOptimizingType() {
    return optimizingType;
  }

  public long getLastOptimizedSnapshotId() {
    return lastOptimizedSnapshotId;
  }

  protected void saveProcessCreated(DefaultOptimizingProcess process) {
    Preconditions.checkState(getStage() == OptimizingStage.PENDING);
    persistenceHelper.invoke(
        () -> {
          setId(process.getId());
          optimizingProcess = process;
          setStage(OptimizingStage.PLANNING);
          setStartTime(getCurrentStageStartTime());
          persistenceHelper.persistCreated();
        });
  }

  protected void saveProcessRecoverd(DefaultOptimizingProcess process) {
    optimizingProcess = process;
  }

  public void savePending(long targetSnapshotId, long watermark) {
    persistenceHelper.invoke(
        () -> {
          setTargetSnapshotId(targetSnapshotId);
          setWatermark(watermark);
          if (getStage() != OptimizingStage.PENDING) {
            setStage(OptimizingStage.PENDING);
          }
          persistenceHelper.persistStage();
        });
  }

  protected void saveCommittingStage() {
    persistenceHelper.invoke(
        () -> {
          setStage(OptimizingStage.COMMITTING);
          persistenceHelper.persistStage();
        });
  }

  protected void saveOptimizingStage(OptimizingType optimizingType, int taskCount, String summary) {
    persistenceHelper.invoke(
        () -> {
          this.taskCount = taskCount;
          setStage(optimizingType.getStatus());
          lastOptimizingTime = getStartTime();
          persistenceHelper.persistSummary(summary);
        });
  }

  @Override
  public void setFailedReason(String failedReason) {
    persistenceHelper.invoke(
        () -> {
          super.setFailedReason(failedReason);
          setStage(OptimizingStage.IDLE);
          lastOptimizingTime = getStartTime();
          persistenceHelper.persistStatus();
        });
  }

  protected void setStatus(ProcessStatus status) {
    Preconditions.checkState(status == ProcessStatus.CLOSED || status == ProcessStatus.SUCCESS);
    if (status == ProcessStatus.CLOSED) {
      persistenceHelper.invoke(
          () -> {
            super.setStatus(ProcessStatus.CLOSED);
            setStage(OptimizingStage.IDLE, getEndTime());
            lastOptimizingTime = getStartTime();
            persistenceHelper.persistStatus();
            optimizingProcess = null;
          });
    } else {
      persistenceHelper.invoke(
          () -> {
            super.setStatus(ProcessStatus.SUCCESS);
            setStage(OptimizingStage.IDLE, getEndTime());
            lastOptimizingTime = getStartTime();
            persistenceHelper.persistSummary(optimizingProcess.getSummary());
            optimizingProcess = null;
          });
    }
  }

  private class PersistenceHelper extends StatedPersistentBase {

    public void invoke(Runnable runnable) {
      invokeConsistency(runnable);
    }

    public void persistCreated() {
      doAs(
          ProcessMapper.class,
          mapper -> mapper.insertDefaultOptimizingProcess(DefaultOptimizingState.this));
    }

    public void persistSummary(String summary) {
      doAs(
          ProcessMapper.class,
          mapper ->
              mapper.updateOptimizingProcess(
                  getTableIdentifier().getId(),
                  getId(),
                  getStatus(),
                  getStage(),
                  getCurrentStageStartTime(),
                  getEndTime(),
                  taskCount,
                  summary,
                  getFailedReason()));
    }

    public void persistStatus() {
      doAs(
          ProcessMapper.class,
          mapper ->
              mapper.updateOptimizingProcess(
                  getTableIdentifier().getId(),
                  getId(),
                  getStatus(),
                  getStage(),
                  getCurrentStageStartTime(),
                  getEndTime(),
                  getFailedReason()));
    }

    public void persistStage() {
      doAs(
          ProcessMapper.class,
          mapper ->
              mapper.updateOptimizingProcess(
                  getTableIdentifier().getId(), getId(), getStage(), getCurrentStageStartTime()));
    }
  }
}
