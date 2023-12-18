package com.netease.arctic.server.process;

import com.netease.arctic.server.exception.OptimizingClosedException;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.utils.SimpleFuture;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TableProcess<T extends TableState> implements AmoroProcess<T>, TaskQueue {

  protected static final Logger LOG = LoggerFactory.getLogger(TableProcess.class);

  protected final T state;
  protected final TableRuntime tableRuntime;
  private final SimpleFuture submitFuture = new SimpleFuture();
  private final SimpleFuture completeFuture = new SimpleFuture();
  private volatile ProcessStatus status = ProcessStatus.RUNNING;
  private volatile String failedReason;

  protected TableProcess(T state, TableRuntime tableRuntime) {
    this.state = state;
    this.tableRuntime = tableRuntime;
  }

  protected void completeSubmitting() {
    submitFuture.complete();
  }

  protected void complete() {
    status = ProcessStatus.SUCCESS;
    completeFuture.complete();
  }

  protected void complete(String errorMessage) {
    status = ProcessStatus.FAILED;
    failedReason = errorMessage;
    completeFuture.complete();
  }

  public TableRuntime getTableRuntime() {
    return tableRuntime;
  }

  @Override
  public T getState() {
    return state;
  }

  @Override
  public void close() {
    status = ProcessStatus.CLOSED;
    closeInternal();
    complete();
  }

  @Override
  public ProcessStatus getStatus() {
    return status;
  }

  @Override
  public String getFailedReason() {
    return failedReason;
  }

  protected abstract void closeInternal();

  @Override
  public SimpleFuture getSubmitFuture() {
    return submitFuture.anyOf(completeFuture);
  }

  @Override
  public SimpleFuture getCompleteFuture() {
    return completeFuture;
  }

  protected void handleAsyncTask(TaskRuntime<?, ?> taskRuntime, Runnable completedAction) {
    taskRuntime.whenCompleted(
        () -> {
          try {
            checkClosed();
            if (taskRuntime.getStatus() == TaskRuntime.Status.SUCCESS && completedAction != null) {
              completedAction.run();
            } else {
              retryTaskOrTerminateProcess(taskRuntime,
                  () -> handleAsyncTask(taskRuntime, completedAction));
            }
          } catch (Throwable throwable) {
            if (state.getFailedReason() == null) {
              LOG.error("Process {} unexpected failed", this, throwable);
              complete(throwable.getMessage());
            }
          }
        });
    submitTask(taskRuntime);
  }

  private void retryTaskOrTerminateProcess(TaskRuntime<?, ?> taskRuntime, Runnable completedAction) {
    Preconditions.checkState(taskRuntime.getStatus() == TaskRuntime.Status.FAILED);
    if (taskRuntime.getRetry() < tableRuntime.getMaxExecuteRetryCount()) {
      taskRuntime.whenCompleted(completedAction);
      retry(taskRuntime);
    } else {
      complete(taskRuntime.getFailReason());
    }
  }

  protected void checkClosed() {
    if (state.getStatus() == ProcessStatus.CLOSED) {
      throw new OptimizingClosedException(state.getId());
    }
  }

  protected abstract void retry(TaskRuntime<?, ?> taskRuntime);

  protected abstract void submitTask(TaskRuntime<?, ?> taskRuntime);
}
