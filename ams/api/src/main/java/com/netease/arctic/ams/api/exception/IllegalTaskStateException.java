package com.netease.arctic.ams.api.exception;

import com.netease.arctic.ams.api.OptimizingTaskId;
import com.netease.arctic.server.process.TaskRuntime;

public class IllegalTaskStateException extends ArcticRuntimeException {

  private final TaskRuntime.Status preStatus;
  private final TaskRuntime.Status targetStatus;
  private final OptimizingTaskId taskId;

  public IllegalTaskStateException(
      OptimizingTaskId taskId, TaskRuntime.Status preStatus, TaskRuntime.Status targetStatus) {
    super(
        String.format("Illegal Task of %s status from %s to %s", taskId, preStatus, targetStatus));
    this.taskId = taskId;
    this.preStatus = preStatus;
    this.targetStatus = targetStatus;
  }

  public TaskRuntime.Status getPreStatus() {
    return preStatus;
  }

  public TaskRuntime.Status getTargetStatus() {
    return targetStatus;
  }

  public OptimizingTaskId getTaskId() {
    return taskId;
  }
}
