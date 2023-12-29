package com.netease.arctic.server.process;

import com.netease.arctic.ams.api.TableRuntime;
import com.netease.arctic.ams.api.exception.OptimizingClosedException;
import com.netease.arctic.ams.api.process.ProcessStatus;
import com.netease.arctic.ams.api.process.TableProcess;
import com.netease.arctic.ams.api.process.TableState;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ManagedProcess<T extends TableState> extends TableProcess<T>
    implements TaskQueue {

  protected static final Logger LOG = LoggerFactory.getLogger(ManagedProcess.class);

  protected ManagedProcess(T state, TableRuntime tableRuntime) {
    super(state, tableRuntime);
  }

  private void handleAsyncTask(TaskRuntime<?, ?> taskRuntime, Runnable completedAction) {
    taskRuntime.whenCompleted(
        () -> {
          try {
            checkClosed();
            if (taskRuntime.getStatus() == TaskRuntime.Status.SUCCESS && completedAction != null) {
              completedAction.run();
            } else {
              retryTaskOrTerminateProcess(
                  taskRuntime, () -> handleAsyncTask(taskRuntime, completedAction));
            }
          } catch (Throwable throwable) {
            if (state.getFailedReason() == null) {
              LOG.error("Process {} unexpected failed", this, throwable);
              complete(throwable.getMessage());
            }
          }
        });
  }

  protected void submitAsyncTask(TaskRuntime<?, ?> taskRuntime, Runnable completedAction) {
    handleAsyncTask(taskRuntime, completedAction);
    submitTask(taskRuntime);
  }

  private void retryTaskOrTerminateProcess(
      TaskRuntime<?, ?> taskRuntime, Runnable completedAction) {
    Preconditions.checkState(taskRuntime.getStatus() == TaskRuntime.Status.FAILED);
    if (taskRuntime.getRetry() < tableRuntime.getMaxExecuteRetryCount()) {
      handleAsyncTask(taskRuntime, completedAction);
      retry(taskRuntime);
    } else {
      handleTaskFailed(taskRuntime);
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

  protected abstract void handleTaskFailed(TaskRuntime<?, ?> taskRuntime);
}
