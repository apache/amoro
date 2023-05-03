package com.netease.arctic.ams.server.persistence;

import com.google.common.collect.Maps;
import com.netease.arctic.ams.server.exception.UndefinedException;
import com.netease.arctic.ams.server.optimizing.TaskRuntime;
import com.netease.arctic.ams.server.persistence.mapper.OptimizingMapper;
import com.netease.arctic.ams.server.table.TableRuntime;
import com.netease.arctic.optimizing.RewriteFilesInput;
import com.netease.arctic.optimizing.RewriteFilesOutput;
import com.netease.arctic.utils.SerializationUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskFilesPersistency {

  private static final DatabasePersistency persistency = new DatabasePersistency();

  public static void persistTaskInputs(TableRuntime tableRuntime, long processId, Collection<TaskRuntime> tasks) {
    persistency.persistTaskInputs(processId, tasks.stream().map(TaskRuntime::getInput).collect(Collectors.toList()));
  }

  public static RewriteFilesInput loadTaskInputs(long processId) {
    byte[] input = persistency.getAs(OptimizingMapper.class, mapper -> mapper.selectProcessInputFiles(processId));
    return (RewriteFilesInput) SerializationUtils.toObject(input);
  }

  public static RewriteFilesOutput loadTaskOutput(byte[] content) {
    return (RewriteFilesOutput) SerializationUtils.toObject(content);
  }

  private static class DatabasePersistency extends PersistentBase {

    public void persistTaskInputs(long processId, List<RewriteFilesInput> tasks) {
      doAs(OptimizingMapper.class, mapper -> {
        mapper.updateProcessInputFiles(processId, SerializationUtils.toByteBuffer(tasks).array());
      });
    }
  }
}
