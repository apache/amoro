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

package org.apache.amoro.process;

import org.apache.amoro.Action;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.StateField;

/** The state of the optimizing process. */
public abstract class OptimizingState extends TableProcessState {

  @StateField private volatile long targetSnapshotId;
  @StateField private volatile long watermark;
  @StateField private volatile ProcessStage stage;
  @StateField private volatile long currentStageStartTime;

  public OptimizingState(Action action, ServerTableIdentifier tableIdentifier) {
    super(action, tableIdentifier);
  }

  public OptimizingState(long id, Action action, ServerTableIdentifier tableIdentifier) {
    super(id, action, tableIdentifier);
  }

  protected void setStage(ProcessStage stage) {
    this.stage = stage;
    this.currentStageStartTime = System.currentTimeMillis();
  }

  protected void setStage(ProcessStage stage, long stageStartTime) {
    this.stage = stage;
    this.currentStageStartTime = stageStartTime;
  }

  protected void setTargetSnapshotId(long targetSnapshotId) {
    this.targetSnapshotId = targetSnapshotId;
  }

  protected void setWatermark(long watermark) {
    this.watermark = watermark;
  }

  public long getWatermark() {
    return watermark;
  }

  @Override
  public ProcessStage getStage() {
    return stage;
  }

  public long getTargetSnapshotId() {
    return targetSnapshotId;
  }

  public long getCurrentStageStartTime() {
    return currentStageStartTime;
  }

  @Override
  public String getName() {
    return stage.getDesc();
  }
}
