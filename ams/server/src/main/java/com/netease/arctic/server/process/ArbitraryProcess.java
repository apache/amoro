package com.netease.arctic.server.process;

import com.netease.arctic.ams.api.Action;
import com.netease.arctic.ams.api.OptimizingTaskId;
import com.netease.arctic.ams.api.TableRuntime;
import com.netease.arctic.ams.api.process.TableState;
import com.netease.arctic.server.persistence.PersistentBase;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ArbitraryProcess extends ManagedProcess<TableState> {

  private final Lock lock = new ReentrantLock();
  private final TaskRuntime<?, ?> taskRuntime;
  private volatile String summary;
  private volatile boolean isTaskAvailable = false;

  public ArbitraryProcess(
      long id, Action action, TableRuntime tableRuntime, TaskRuntime<?, ?> taskRuntime) {
    super(new TableState(id, action, tableRuntime.getTableIdentifier()), tableRuntime);
    this.taskRuntime = taskRuntime;
  }

  public ArbitraryProcess(TableState state, TableRuntime tableRuntime) {
    super(state, tableRuntime);
    taskRuntime = new PersistenceHelper().recoverTaskRuntime(state.getId());
  }

  @Override
  public void submit() {
    lock.lock();
    try {
      summary = taskRuntime.getSummary();
      handleAsyncTask(taskRuntime, () -> summary = taskRuntime.getSummary());
      isTaskAvailable = true;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void closeInternal() {
    // TODO close persistency
  }

  @Override
  public TaskRuntime<?, ?> pollTask() {
    lock.lock();
    try {
      return isTaskAvailable ? taskRuntime : null;
    } finally {
      isTaskAvailable = false;
      lock.unlock();
    }
  }

  @Override
  public TaskRuntime<?, ?> getTaskRuntime(OptimizingTaskId taskId) {
    return Objects.equals(taskRuntime.getTaskId(), taskId) ? taskRuntime : null;
  }

  @Override
  public List<TaskRuntime<?, ?>> getTaskRuntimes() {
    return Lists.newArrayList(taskRuntime);
  }

  @Override
  protected void retry(TaskRuntime<?, ?> taskRuntime) {
    submitTask(taskRuntime);
  }

  @Override
  protected void submitTask(TaskRuntime<?, ?> taskRuntime) {
    isTaskAvailable = true;
  }

  @Override
  public String getSummary() {
    return summary;
  }

  private static class PersistenceHelper extends PersistentBase {
    public TaskRuntime<?, ?> recoverTaskRuntime(long processId) {
      return null;
    }
  }
}
