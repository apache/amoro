package com.netease.arctic.ams.api.exception;

import com.netease.arctic.ams.api.OptimizingTaskId;

public class TaskNotFoundException extends ArcticRuntimeException {
  private final OptimizingTaskId taskId;

  public TaskNotFoundException(OptimizingTaskId taskId) {
    super("Task " + taskId + " not found.");
    this.taskId = taskId;
  }

  public OptimizingTaskId getTaskId() {
    return taskId;
  }
}
