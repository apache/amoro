package com.netease.arctic.ams.api.exception;

import com.netease.arctic.ams.api.OptimizingTaskId;

public class IllegalTaskStateException extends ArcticRuntimeException {

  private final String preStatus;
  private final String targetStatus;
  private final OptimizingTaskId taskId;

  public IllegalTaskStateException(OptimizingTaskId taskId, String preStatus, String targetStatus) {
    super(
        String.format("Illegal Task of %s status from %s to %s", taskId, preStatus, targetStatus));
    this.taskId = taskId;
    this.preStatus = preStatus;
    this.targetStatus = targetStatus;
  }

  public String getPreStatus() {
    return preStatus;
  }

  public String getTargetStatus() {
    return targetStatus;
  }

  public OptimizingTaskId getTaskId() {
    return taskId;
  }
}
