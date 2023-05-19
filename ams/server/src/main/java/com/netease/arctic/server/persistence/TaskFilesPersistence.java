package com.netease.arctic.server.persistence;

import com.netease.arctic.optimizing.RewriteFilesInput;
import com.netease.arctic.optimizing.RewriteFilesOutput;
import com.netease.arctic.server.optimizing.TaskRuntime;
import com.netease.arctic.server.persistence.mapper.OptimizingMapper;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.utils.SerializationUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskFilesPersistence {

  private static final DatabasePersistence persistence = new DatabasePersistence();

  public static void persistTaskInputs(long processId, Collection<TaskRuntime> tasks) {
    persistence.persistTaskInputs(processId, tasks.stream().collect(Collectors.toMap(e -> e.getTaskId().getTaskId(),
        TaskRuntime::getInput)));
  }

  public static Map<Integer, RewriteFilesInput> loadTaskInputs(long processId) {
    byte[] input = persistence.getAs(OptimizingMapper.class, mapper -> mapper.selectProcessInputFiles(processId));
    return SerializationUtil.simpleDeserialize(input);
  }

  public static RewriteFilesOutput loadTaskOutput(byte[] content) {
    return SerializationUtil.simpleDeserialize(content);
  }

  private static class DatabasePersistence extends PersistentBase {

    public void persistTaskInputs(long processId, Map<Integer, RewriteFilesInput> tasks) {
      doAs(OptimizingMapper.class, mapper ->
          mapper.updateProcessInputFiles(processId, SerializationUtil.simpleSerialize(tasks).array()));
    }
  }
}
