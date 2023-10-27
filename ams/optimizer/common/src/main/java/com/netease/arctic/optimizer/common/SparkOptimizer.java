package com.netease.arctic.optimizer.common;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.netease.arctic.ams.api.OptimizingTask;
import com.netease.arctic.ams.api.OptimizingTaskResult;
import com.netease.arctic.ams.api.PropertyNames;
import com.netease.arctic.ams.api.resource.Resource;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class SparkOptimizer {
  private static final Logger LOG = LoggerFactory.getLogger(SparkOptimizer.class);

  public static void main(String[] args) throws Exception {
    OptimizerConfig config = new OptimizerConfig();
    JavaSparkContext jsc =
        new JavaSparkContext(new SparkConf().setAppName("Amoro Iceberg Optimizer"));
    SparkConf sparkConf = jsc.getConf();
    config.setAmsUrl(sparkConf.get("spark.amoro.amsUrl"));
    config.setExecutionParallel(sparkConf.getInt("spark.amoro.executorParallel", 4));
    config.setGroupName(sparkConf.get("spark.amoro.groupName"));
    config.setMemorySize(sparkConf.getInt("spark.amoro.memorySize", 4096));

    OptimizerToucher toucher = new OptimizerToucher(config);
    if (config.getResourceId() != null) {
      toucher.withRegisterProperty(PropertyNames.RESOURCE_ID, config.getResourceId());
    }
    toucher.withRegisterProperty(Resource.PROPERTY_JOB_ID, jsc.appName());
    LOG.info("Starting optimizer with configuration:{}", config);

    ThreadFactory consumerFactory =
        new ThreadFactoryBuilder().setDaemon(false).setNameFormat("Puller %d").build();
    ThreadFactory executorFactory =
        new ThreadFactoryBuilder().setDaemon(false).setNameFormat("Executor %d").build();

    ScheduledExecutorService pullerService =
        Executors.newSingleThreadScheduledExecutor(consumerFactory);
    ThreadPoolExecutor executorService =
        new ThreadPoolExecutor(
            0,
            config.getExecutionParallel(),
            30L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(config.getExecutionParallel()),
            executorFactory);

    TaskPuller puller = new TaskPuller(config, toucher, jsc, executorService);
    pullerService.scheduleWithFixedDelay(puller, 3000, 60000L, TimeUnit.MILLISECONDS);
    Thread.sleep(Long.MAX_VALUE);
  }

  static class TaskPuller extends AbstractOptimizerOperator implements Runnable {
    private final ThreadPoolExecutor executor;
    private final OptimizerConfig config;
    private final JavaSparkContext jsc;
    private final int threadId = Thread.currentThread().hashCode();

    private TaskPuller(
        OptimizerConfig config,
        OptimizerToucher toucher,
        JavaSparkContext jsc,
        ThreadPoolExecutor executor) {
      super(config);
      this.config = config;
      this.jsc = jsc;
      this.executor = executor;
      new Thread(() -> toucher.withTokenChangeListener(newToken -> setToken(newToken)).start())
          .start();
    }

    public int getThreadId() {
      return threadId;
    }

    @Override
    public void run() {
      try {
        while (true) {
          long startTime = System.currentTimeMillis();
          int activeCount = executor.getActiveCount();
          if (activeCount >= executor.getMaximumPoolSize()) {
            LOG.info("Polling task[{}] is full", activeCount);
            break;
          }
          OptimizingTask task = pollTask();
          if (task == null || !ackTask(task)) {
            LOG.info("Polled no task and wait for {} ms", System.currentTimeMillis() - startTime);
            break;
          }
          executor.execute(new SparkExecutor(jsc, this, task, config));
          LOG.info(
              "Polled task {}, and wait for {} ms", task, System.currentTimeMillis() - startTime);
        }
      } catch (Throwable e) {
        LOG.error("Failed to poll task", e);
      }
    }

    public void completeTask(OptimizingTaskResult optimizingTaskResult) {
      try {
        callAuthenticatedAms(
            (client, token) -> {
              client.completeTask(token, optimizingTaskResult);
              return null;
            });
        LOG.info(
            "Optimizer executor[{}] completed task[{}] to ams",
            threadId,
            optimizingTaskResult.getTaskId());
      } catch (TException exception) {
        LOG.error(
            "Optimizer executor[{}] completed task[{}] failed",
            threadId,
            optimizingTaskResult.getTaskId(),
            exception);
      }
    }

    private boolean ackTask(OptimizingTask task) {
      try {
        callAuthenticatedAms(
            (client, token) -> {
              client.ackTask(token, threadId, task.getTaskId());
              return null;
            });
        LOG.info("Optimizer executor[{}] acknowledged task[{}] to ams", threadId, task.getTaskId());
        return true;
      } catch (TException exception) {
        LOG.error(
            "Optimizer executor[{}] acknowledged task[{}] failed",
            threadId,
            task.getTaskId(),
            exception);
        return false;
      }
    }

    private OptimizingTask pollTask() {
      OptimizingTask task = null;
      while (isStarted()) {
        try {
          task = callAuthenticatedAms((client, token) -> client.pollTask(token, threadId));
        } catch (TException exception) {
          LOG.error("Optimizer executor[{}] polled task failed", threadId, exception);
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
  }
}
