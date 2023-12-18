package com.netease.arctic.server.process;

import com.netease.arctic.ams.api.OptimizingTaskId;

import java.util.List;

public interface TaskQueue {

  TaskRuntime<?, ?> pollTask();

  TaskRuntime<?, ?> getTaskRuntime(OptimizingTaskId taskId);

  List<TaskRuntime<?, ?>> getTaskRuntimes();
}
