package com.netease.arctic.ams.api.process;

import com.netease.arctic.ams.api.Action;
import com.netease.arctic.ams.api.ServerTableIdentifier;

public abstract class OptimizingState extends TableState {

  private volatile long targetSnapshotId;
  private volatile long targetChangeSnapshotId;
  private volatile OptimizingStage stage;
  private volatile long currentStageStartTime;

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

  protected void setTargetChangeSnapshotId(long targetChangeSnapshotId) {
    this.targetChangeSnapshotId = targetChangeSnapshotId;
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

  public long getCurrentStageStartTime() {
    return currentStageStartTime;
  }

  @Override
  public String getName() {
    return stage.displayValue();
  }

  public abstract long getQuotaRuntime();

  public double getQuotaValue() {
    return (double) getQuotaRuntime() / System.currentTimeMillis() - getStartTime();
  }
}
