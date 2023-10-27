package com.netease.arctic.optimizer.common;

import com.google.common.collect.ImmutableList;
import com.netease.arctic.ams.api.OptimizingTask;
import com.netease.arctic.ams.api.OptimizingTaskResult;
import org.apache.spark.api.java.JavaSparkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SparkExecutor implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(SparkExecutor.class);
  private final SparkFunction sparkFunction;

  private final OptimizingTask task;

  private final JavaSparkContext jsc;

  private final SparkOptimizer.TaskPuller taskPuller;

  public SparkExecutor(
      JavaSparkContext jsc,
      SparkOptimizer.TaskPuller taskPuller,
      OptimizingTask task,
      OptimizerConfig config) {
    this.jsc = jsc;
    this.task = task;
    this.taskPuller = taskPuller;
    this.sparkFunction = new SparkFunction(config, this.taskPuller.getThreadId());
  }

  @Override
  public void run() {
    try {
      String threadName = Thread.currentThread().getName();
      long startTime = System.currentTimeMillis();
      LOG.info("Now [{}] execute task {}", threadName, task);
      ImmutableList<OptimizingTask> of = ImmutableList.of(task);
      String jobDesc = String.format("%s-%s", task.getTaskId(), task.getProperties());
      jsc.setJobDescription(jobDesc);
      List<OptimizingTaskResult> result = jsc.parallelize(of, 1).map(sparkFunction).collect();
      result.stream()
          .forEach(
              r -> {
                this.taskPuller.completeTask(r);
              });
      LOG.info(
          "[{}] execute task {}, completed {} and time {} ms",
          threadName,
          task,
          result.get(0),
          System.currentTimeMillis() - startTime);

    } catch (Throwable r) {
      LOG.error("Failed to execute optimizing task.", r);
    }
  }
}
