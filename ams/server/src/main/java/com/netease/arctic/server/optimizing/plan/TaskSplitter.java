package com.netease.arctic.server.optimizing.plan;

import java.util.List;

public interface TaskSplitter {
  List<IcebergSplitTask> splitTasks(int targetTaskCount);
}
