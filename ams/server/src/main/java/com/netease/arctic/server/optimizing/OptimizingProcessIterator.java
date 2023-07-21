package com.netease.arctic.server.optimizing;

import com.google.common.collect.Maps;
import com.netease.arctic.server.optimizing.plan.OptimizingPlanner;
import com.netease.arctic.server.optimizing.plan.ProcessGroup;
import com.netease.arctic.server.optimizing.plan.TaskDescriptor;

import com.netease.arctic.table.TableProperties;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OptimizingProcessIterator implements Iterator<OptimizingProcess> {
  private final Logger LOG = LoggerFactory.getLogger(OptimizingProcessIterator.class);
  private final OptimizingPlanner planner;
  private final Queue<ProcessGroup> orderedTasks;
  private final static String ungroupedId = UUID.randomUUID().toString();
  private final Consumer<OptimizingProcess> clearTask;
  private final BiConsumer<TaskRuntime, Boolean> retryTask;
  private final Consumer<TaskRuntime> taskOffer;

  private OptimizingProcessIterator(
      OptimizingPlanner planner,
      Consumer<OptimizingProcess> clearTask,
      BiConsumer<TaskRuntime, Boolean> retryTask,
      Consumer<TaskRuntime> taskOffer) {
    this.planner = planner;
    OptimizingConfig config = planner.getTableRuntime().getOptimizingConfig();

    Comparator<TaskDescriptor> taskComparator = ProcessGroup.taskComparator(
        Optional.ofNullable(config.getTaskOrder())
            .orElse(TableProperties.SELF_OPTIMIZING_TASK_ORDER_DEFAULT));
    Comparator<ProcessGroup> groupComparator = ProcessGroup.processComparator(
        Optional.ofNullable(config.getProcessOrder())
            .orElse(TableProperties.SELF_OPTIMIZING_PROCESS_ORDER_DEFAULT));

      switch (ProcessSplitter.fromName(Optional.ofNullable(config.getProcessSplitter())
          .orElse(TableProperties.SELF_OPTIMIZING_PROCESS_SPLITTER_DEFAULT))) {
        case PARTITION:
          if (planner.getArcticTable().isKeyedTable()) {
            orderedTasks = getFlatGroup(taskComparator);
          } else {
            orderedTasks = getPartitionedGroup(groupComparator, taskComparator);
          }
          break;
        default:
          orderedTasks = getFlatGroup(taskComparator);
      }
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
    ProcessGroup group = orderedTasks.poll();
    long newProcessId = planner.getNewProcessId();
      TableOptimizingProcess process = new TableOptimizingProcess(
          newProcessId,
          group.id().equals(ungroupedId)
              ? planner.getOptimizingType()
              : planner.getOptTypeByPartition(group.id()),
          planner.getTableRuntime(),
          newProcessId,
          planner.getTargetSnapshotId(),
          planner.getTargetChangeSnapshotId(),
          group.getTaskDescriptors(),
          group.getFromSequence(),
          group.getToSequence())
          .handleTaskRetry(retryTask)
          .handleTaskClear(clearTask);

      process.getTaskMap().values().forEach(taskOffer);
      LOG.info("Iterate new optimizing process {} belong to {} and the group is {}", process.getProcessId(),
          planner.getArcticTable().id(), group.id());
      return process;
  }

  private Queue<ProcessGroup> getFlatGroup(Comparator<TaskDescriptor> taskComparator) {
    Queue<ProcessGroup> group = new ConcurrentLinkedQueue<>();
    group.add(new ProcessGroup(ungroupedId, planner.planTasks(),
        planner.getFromSequence(), planner.getToSequence(), taskComparator));
    return group;
  }

  private Queue<ProcessGroup> getPartitionedGroup(Comparator<ProcessGroup> groupComparator,
      Comparator<TaskDescriptor> taskComparator) {
    return planner.planPartitionedTasks()
        .entrySet()
        .stream()
        .map(kv -> new ProcessGroup(kv.getKey(), kv.getValue(),
            Maps.filterEntries(planner.getFromSequence(), e -> e.getKey().equals(kv.getKey())),
            Maps.filterEntries(planner.getToSequence(), e -> e.getKey().equals(kv.getKey())),
            taskComparator))
        .sorted(groupComparator)
        .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
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

  public enum ProcessSplitter {
    PARTITION("partition"),
    NONE("none");

    private final String splitterName;

    ProcessSplitter(String splitterName) {
      this.splitterName = splitterName;
    }

    public String splitterName() {
      return splitterName;
    }

    public static ProcessSplitter fromName(String splitterName) {
      Preconditions.checkArgument(splitterName != null, "Invalid process splitter name: null");
      try {
        return ProcessSplitter.valueOf(splitterName.replaceFirst("-", "_").toUpperCase(Locale.ENGLISH));
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            String.format("Invalid process splitter name: %s", splitterName), e);
      }
    }
  }
}
