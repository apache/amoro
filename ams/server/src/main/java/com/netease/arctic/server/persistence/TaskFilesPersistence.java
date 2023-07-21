package com.netease.arctic.server.persistence;

import com.netease.arctic.optimizing.RewriteFilesInput;
import com.netease.arctic.optimizing.RewriteFilesOutput;
import com.netease.arctic.server.optimizing.TaskRuntime;
import com.netease.arctic.server.persistence.mapper.OptimizingMapper;
import com.netease.arctic.server.utils.CompressUtil;
import com.netease.arctic.utils.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskFilesPersistence {

  private static final Logger LOG = LoggerFactory.getLogger(TaskFilesPersistence.class);

  private static final DatabasePersistence persistence = new DatabasePersistence();

  public static void persistTaskInputs(long processId, Collection<TaskRuntime> tasks) {
    persistence.persistTaskInputs(processId, tasks.stream().collect(Collectors.toMap(e -> e.getTaskId().getTaskId(),
        TaskRuntime::getInput)));
  }

  public static Map<Integer, RewriteFilesInput> loadTaskInputs(long processId) {
    List<byte[]> bytes =
        persistence.getAs(OptimizingMapper.class, mapper -> mapper.selectProcessInputFiles(processId));
    if (bytes == null) {
      return Collections.emptyMap();
    } else {
      byte[] originalBytes;
      try {
        originalBytes = CompressUtil.unGzip(bytes.get(0));
      } catch (RuntimeException e) {
        LOG.warn("Fail to unzip, use original bytes", e);
        originalBytes = bytes.get(0);
      }
      return SerializationUtil.simpleDeserialize(originalBytes);
    }
  }

  public static RewriteFilesOutput loadTaskOutput(byte[] content) {
    return SerializationUtil.simpleDeserialize(content);
  }

  private static class DatabasePersistence extends PersistentBase {

    public void persistTaskInputs(long processId, Map<Integer, RewriteFilesInput> tasks) {
      doAs(OptimizingMapper.class, mapper ->
          mapper.updateProcessInputFiles(processId, tasks));
    }
  }
}
