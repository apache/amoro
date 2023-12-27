package com.netease.arctic.ams.api.process;

import com.netease.arctic.ams.api.Action;
import com.netease.arctic.ams.api.ServerTableIdentifier;
import com.netease.arctic.ams.api.StateField;

public abstract class OptimizingState extends TableState {

  @StateField private volatile long targetSnapshotId;
  @StateField private volatile long watermark;
  @StateField private volatile OptimizingStage stage;
  @StateField private volatile long currentStageStartTime;

  public OptimizingState(Action action, ServerTableIdentifier tableIdentifier) {
    super(action, tableIdentifier);
  }

  public OptimizingState(long id, Action action, ServerTableIdentifier tableIdentifier) {
    super(id, action, tableIdentifier);
  }

  protected void setStage(OptimizingStage stage) {
    this.stage = stage;
    this.currentStageStartTime = System.currentTimeMillis();
  }

  protected void setStage(OptimizingStage stage, long stageStartTime) {
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

  public OptimizingStage getStage() {
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
    return stage.displayValue();
  }
}
