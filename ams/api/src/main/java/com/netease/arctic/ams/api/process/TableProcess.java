package com.netease.arctic.ams.api.process;

import com.netease.arctic.ams.api.TableRuntime;

public abstract class TableProcess<T extends TableState> implements AmoroProcess<T> {

  protected final T state;
  protected final TableRuntime tableRuntime;
  private final SimpleFuture submitFuture = new SimpleFuture();
  private final SimpleFuture completeFuture = new SimpleFuture();
  private volatile ProcessStatus status = ProcessStatus.RUNNING;
  private volatile String failedReason;

  protected TableProcess(T state, TableRuntime tableRuntime) {
    this.state = state;
    this.tableRuntime = tableRuntime;
    this.completeFuture.whenCompleted(
        () -> {
          if (status == ProcessStatus.FAILED) {
            state.setFailedReason(failedReason);
          } else {
            state.setStatus(status);
          }
        });
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

  protected abstract void closeInternal();

  @Override
  public SimpleFuture getSubmitFuture() {
    return submitFuture.anyOf(completeFuture);
  }

  @Override
  public SimpleFuture getCompleteFuture() {
    return completeFuture;
  }
}
