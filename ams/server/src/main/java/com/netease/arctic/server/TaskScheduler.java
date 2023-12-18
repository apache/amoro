package com.netease.arctic.server;

import com.netease.arctic.server.process.TaskRuntime;

public interface TaskScheduler {

  TaskRuntime<?, ?> scheduleTask();
}
