package com.netease.arctic.optimizer.common;

import com.netease.arctic.ams.api.OptimizingTask;
import com.netease.arctic.ams.api.OptimizingTaskResult;
import com.netease.arctic.optimizing.OptimizingExecutor;
import com.netease.arctic.optimizing.OptimizingExecutorFactory;
import com.netease.arctic.optimizing.OptimizingInputProperties;
import com.netease.arctic.optimizing.TableOptimizing;
import com.netease.arctic.utils.ExceptionUtil;
import com.netease.arctic.utils.SerializationUtil;
import org.apache.iceberg.common.DynConstructors;
import org.apache.spark.api.java.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class SparkFunction implements Function<OptimizingTask, OptimizingTaskResult> {
  private static final Logger LOG = LoggerFactory.getLogger(SparkFunction.class);

  private int threadId;

  private OptimizerConfig config;

  public SparkFunction(OptimizerConfig config, int threadId) {
    this.threadId = Thread.currentThread().hashCode();
    this.config = config;
    this.threadId = threadId;
  }

  @Override
  public OptimizingTaskResult call(OptimizingTask task) throws Exception {
    LOG.info("Start to execute Task {}", task);
    return executeTask(task);
  }

  private OptimizingTaskResult executeTask(OptimizingTask task) {
    try {
      OptimizingInputProperties properties = OptimizingInputProperties.parse(task.getProperties());
      String executorFactoryImpl = properties.getExecutorFactoryImpl();
      TableOptimizing.OptimizingInput input =
          SerializationUtil.simpleDeserialize(task.getTaskInput());
      DynConstructors.Ctor<OptimizingExecutorFactory> ctor =
          DynConstructors.builder(OptimizingExecutorFactory.class)
              .impl(executorFactoryImpl)
              .buildChecked();
      OptimizingExecutorFactory factory = ctor.newInstance();

      if (config.isExtendDiskStorage()) {
        properties.enableSpillMap();
      }
      properties.setMaxSizeInMemory(config.getMemoryStorageSize() * 1024 * 1024);
      properties.setSpillMapPath(config.getDiskStoragePath());
      factory.initialize(properties.getProperties());

      OptimizingExecutor executor = factory.createExecutor(input);
      TableOptimizing.OptimizingOutput output = executor.execute();
      ByteBuffer outputByteBuffer = SerializationUtil.simpleSerialize(output);
      OptimizingTaskResult result = new OptimizingTaskResult(task.getTaskId(), threadId);
      result.setTaskOutput(outputByteBuffer);
      result.setSummary(output.summary());
      LOG.info("Optimizer executor[{}] executed task[{}]", threadId, task.getTaskId());
      return result;
    } catch (Throwable t) {
      LOG.error("Optimizer executor[{}] executed task[{}] failed", threadId, task.getTaskId(), t);
      OptimizingTaskResult errorResult = new OptimizingTaskResult(task.getTaskId(), threadId);
      errorResult.setErrorMessage(ExceptionUtil.getErrorMessage(t, 4000));
      return errorResult;
    }
  }
}
