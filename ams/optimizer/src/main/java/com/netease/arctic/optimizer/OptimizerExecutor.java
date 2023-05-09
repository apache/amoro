package com.netease.arctic.optimizer;

import com.netease.arctic.ams.api.OptimizingTask;
import com.netease.arctic.ams.api.OptimizingTaskResult;
import com.netease.arctic.ams.api.properties.OptimizingTaskProperties;
import com.netease.arctic.optimizer.util.PropertyUtil;
import com.netease.arctic.optimizing.OptimizingExecutor;
import com.netease.arctic.optimizing.OptimizingExecutorFactory;
import com.netease.arctic.optimizing.TableOptimizing;
import com.netease.arctic.utils.SerializationUtil;
import org.apache.iceberg.common.DynConstructors;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class OptimizerExecutor extends AbstractOptimizerOperator {

  private static final Logger LOG = LoggerFactory.getLogger(OptimizerExecutor.class);

  private final int threadId;

  public OptimizerExecutor(OptimizerConfig config, int threadId) {
    super(config);
    this.threadId = threadId;
  }

  public void start() {
    while (isStarted()) {
      try {
        OptimizingTask task = pollTask();
        if (task != null) {
          ackTask(task);
          OptimizingTaskResult result = executeTask(task);
          completeTask(result);
        }
      } catch (Throwable t) {
        LOG.error(String.format("Optimizer executor[%d] got an unexpected error", threadId), t);
      }
    }
  }

  public int getThreadId() {
    return threadId;
  }

  private OptimizingTask pollTask() {
    OptimizingTask task = null;
    while (isStarted()) {
      try {
        task = callAuthenticatedAms((client, token) -> client.pollTask(token, threadId));
      } catch (TException exception) {
        LOG.error(String.format("Optimizer executor[%d] polled task failed", threadId), exception);
      }
      if (task != null) {
        LOG.info("Optimizer executor[{}] polled task[{}] from ams", threadId, task.getTaskId());
        break;
      } else {
        waitAShortTime();
      }
    }
    return task;
  }

  private void ackTask(OptimizingTask task) {
    while (isStarted()) {
      try {
        callAuthenticatedAms((client, token) -> {
          client.ackTask(token, threadId, task.getTaskId());
          return null;
        });
        LOG.info("Optimizer executor[{}] acknowledged task[{}] to ams", threadId, task.getTaskId());
        return;
      } catch (TException exception) {
        LOG.error(
            String.format("Optimizer executor[%d] acknowledged task[%s] failed", threadId, task.getTaskId()),
            exception);
        waitAShortTime();
      }
    }
    throw new IllegalStateException("Operator is stopped");
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private OptimizingTaskResult executeTask(OptimizingTask task) {
    try {
      String executorFactoryImpl = PropertyUtil.checkAndGetProperty(
          task.getProperties(),
          OptimizingTaskProperties.TASK_EXECUTOR_FACTORY_IMPL);
      TableOptimizing.OptimizingInput input = SerializationUtil.simpleDeserialize(task.getTaskInput());
      DynConstructors.Ctor<OptimizingExecutorFactory> ctor = DynConstructors.builder(OptimizingExecutorFactory.class)
          .impl(executorFactoryImpl).buildChecked();
      OptimizingExecutorFactory factory = ctor.newInstance();
      factory.initialize(task.getProperties());
      OptimizingExecutor executor = factory.createExecutor(input);
      TableOptimizing.OptimizingOutput output = executor.execute();
      ByteBuffer outputByteBuffer = SerializationUtil.simpleSerialize(output);
      OptimizingTaskResult result = new OptimizingTaskResult(task.getTaskId(), threadId);
      result.setTaskOutput(outputByteBuffer);
      result.setSummary(result.summary);
      LOG.info("Optimizer executor[{}] executed task[{}]", threadId, task.getTaskId());
      return result;
    } catch (Throwable t) {
      LOG.error(String.format("Optimizer executor[%s] executed task[%s] failed", threadId,
          task.getTaskId()), t);
      OptimizingTaskResult errorResult = new OptimizingTaskResult(task.getTaskId(), threadId);
      errorResult.setErrorMessage(t.getMessage());
      return errorResult;
    }
  }

  private void completeTask(OptimizingTaskResult optimizingTaskResult) {
    while (isStarted()) {
      try {
        callAuthenticatedAms((client, token) -> {
          client.completeTask(token, optimizingTaskResult);
          return null;
        });
        LOG.info("Optimizer executor[{}] completed task[{}] to ams", threadId, optimizingTaskResult.getTaskId());
        return;
      } catch (TException exception) {
        LOG.error(String.format("Optimizer executor[%d] completed task[%s] failed", threadId,
            optimizingTaskResult.getTaskId()), exception);
        waitAShortTime();
      }
    }
    throw new IllegalStateException("Operator is stopped");
  }
}
