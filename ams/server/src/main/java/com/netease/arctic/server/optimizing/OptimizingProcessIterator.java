package com.netease.arctic.server.optimizing;

import com.netease.arctic.server.optimizing.plan.OptimizingPlanner;
import com.netease.arctic.server.optimizing.plan.PlannedTasks;

import com.netease.arctic.utils.TableTypeUtil;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

public class OptimizingProcessIterator implements Iterator<OptimizingProcess> {
  private final OptimizingPlanner planner;
  private Queue<PlannedTasks> orderedTasks;
  private final Consumer<OptimizingProcess> clearTask;
  private final BiConsumer<TaskRuntime, Boolean> retryTask;
  private final Consumer<TaskRuntime> taskOffer;

  private OptimizingProcessIterator(
      OptimizingPlanner planner,
      Consumer<OptimizingProcess> clearTask,
      BiConsumer<TaskRuntime, Boolean> retryTask,
      Consumer<TaskRuntime> taskOffer) {
      this.planner = planner;
      orderedTasks = planner.planPartitionedTasks()
          .entrySet()
          .stream()
          .map(kv -> new PlannedTasks(kv.getKey(), kv.getValue(), planner.getFromSequence().get(kv.getKey())))
          .sorted(PlannedTasks.processComparator(planner.getTableRuntime().getOptimizingConfig().getTaskProcessOrder()))
          .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
      this.clearTask = clearTask;
      this.retryTask = retryTask;
      this.taskOffer = taskOffer;
    }

    public int size() {
    return orderedTasks.size();
    }

  @Override
  public boolean hasNext() {
    return !orderedTasks.isEmpty();
  }

  @Override
  public OptimizingProcess next() {
  PlannedTasks pts = orderedTasks.poll();
    TableOptimizingProcess process = new TableOptimizingProcess(
        planner.getNewProcessId(),
        planner.getOptTypeByPartition(pts.partition()),
        planner.getTableRuntime(),
        planner.getPlanTime(),
        planner.getTargetSnapshotId(),
        planner.getTargetChangeSnapshotId(),
        pts.getTaskDescriptors(),
        planner.getFromSequence(),
        planner.getToSequence())
        .handleTaskRetry(retryTask)
        .handleTaskClear(clearTask);

    process.getTaskMap().values().forEach(taskOffer);
    return process;
  }

  public static class Builder {
    private OptimizingPlanner planner;
    private Consumer<OptimizingProcess> clearTask;
    private BiConsumer<TaskRuntime, Boolean> retryTask;
    private Consumer<TaskRuntime> taskOffer;

    public Builder fromPlanner(OptimizingPlanner planner) {
      this.planner = planner;
      return this;
    }

    public Builder handleTaskClear(Consumer<OptimizingProcess> clearTask) {
      this.clearTask = clearTask;
      return this;
    }

    public Builder handleTaskRetry(BiConsumer<TaskRuntime, Boolean> retryTask) {
      this.retryTask = retryTask;
      return this;
    }

    public Builder handleTaskOffer(Consumer<TaskRuntime> taskOffer) {
      this.taskOffer = taskOffer;
      return this;
    }

    public OptimizingProcessIterator iterator() {
      Preconditions.checkArgument(planner != null, "Optimizing planner is required");
      Preconditions.checkArgument(clearTask != null, "Clear task function is required");
      Preconditions.checkArgument(retryTask != null, "Retry task function is required");
      Preconditions.checkArgument(taskOffer != null, "Offer task function is required");
      return new OptimizingProcessIterator(planner, clearTask, retryTask, taskOffer);
    }
  }
}
