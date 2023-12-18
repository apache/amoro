package com.netease.arctic.server;

import com.netease.arctic.ams.api.Action;
import com.netease.arctic.ams.api.OptimizingTaskId;
import com.netease.arctic.ams.api.resource.ResourceGroup;
import com.netease.arctic.server.process.AmoroProcess;
import com.netease.arctic.server.process.ProcessFactory;
import com.netease.arctic.server.process.TableProcess;
import com.netease.arctic.server.process.TableState;
import com.netease.arctic.server.process.TaskQueue;
import com.netease.arctic.server.process.TaskRuntime;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.table.TableScheduler;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.base.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public abstract class AbstractScheduler<T extends TableState>
    implements TaskScheduler, TableScheduler, ProcessFactory<T> {

  protected static final Logger LOG = LoggerFactory.getLogger(AbstractScheduler.class);

  protected final Queue<TableProcess<T>> tableProcessQueue = new LinkedTransferQueue<>();
  protected final Lock schedulerLock = new ReentrantLock();
  protected ResourceGroup optimizerGroup;

  public AbstractScheduler(ResourceGroup optimizerGroup) {
    Preconditions.checkNotNull(optimizerGroup, "Resource group can not be null");
    this.optimizerGroup = optimizerGroup;
  }

  public String getContainerName() {
    return optimizerGroup.getContainer();
  }

  public TaskRuntime<?, ?> scheduleTask() {
    schedulerLock.lock();
    try {
      TaskRuntime<?, ?> task = fetchTask();
      if (task == null) {
        Optional.ofNullable(scheduleTable())
            .ifPresent(scheduled -> run(scheduled.getLeft(), scheduled.getRight()));
        task = fetchTask();
      }
      return task;
    } finally {
      schedulerLock.unlock();
    }
  }

  private void run(TableRuntime tableRuntime, Action action) {
    if (action == Action.OPTIMIZING) {
      tableRuntime.runOptimizing();
    } else {
      tableRuntime.runAction(action);
    }
  }

  private TaskRuntime<?, ?> fetchTask() {
    return tableProcessQueue.stream()
        .map(TaskQueue::pollTask)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  public TaskRuntime<?, ?> getTask(OptimizingTaskId taskId) {
    return tableProcessQueue.stream()
        .filter(p -> p.getId() == taskId.getProcessId())
        .findFirst()
        .map(p -> p.getTaskRuntime(taskId))
        .orElse(null);
  }

  public List<TaskRuntime<?, ?>> collectTasks() {
    return tableProcessQueue.stream()
        .flatMap(p -> p.getTaskRuntimes().stream())
        .collect(Collectors.toList());
  }

  public List<TaskRuntime<?, ?>> collectTasks(Predicate<TaskRuntime<?, ?>> predicate) {
    return tableProcessQueue.stream()
        .flatMap(p -> p.getTaskRuntimes().stream())
        .filter(predicate)
        .collect(Collectors.toList());
  }

  public void updateOptimizerGroup(ResourceGroup optimizerGroup) {
    Preconditions.checkArgument(
        this.optimizerGroup.getName().equals(optimizerGroup.getName()),
        "optimizer group name mismatch");
    this.optimizerGroup = optimizerGroup;
  }

  protected abstract TableProcess<T> createProcess(TableRuntime tableRuntime, Action action);

  protected abstract TableProcess<T> recoverProcess(TableRuntime tableRuntime, Action action, T state);

  @Override
  public AmoroProcess<T> create(TableRuntime tableRuntime, Action action) {
    TableProcess<T> process = createProcess(tableRuntime, action);
    if (process != null) {
      process.whenCompleted(() -> {
        tableProcessQueue.removeIf(p -> p.getId() == process.getId());
        refreshTable(tableRuntime);
      });
    }
    return process;
  }

  @Override
  public AmoroProcess<T> recover(TableRuntime tableRuntime, Action action, T state) {
    TableProcess<T> process = recoverProcess(tableRuntime, action, state);
    if (process != null) {
      process.whenCompleted(() ->  {
        tableProcessQueue.removeIf(p -> p.getId() == process.getId());
        refreshTable(tableRuntime);
      });
      process.submit();
      tableProcessQueue.offer(process);
      return process;
    } else {
      refreshTable(tableRuntime);
      return null;
    }
  }
}
