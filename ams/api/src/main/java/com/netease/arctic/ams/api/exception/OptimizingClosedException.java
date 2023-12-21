package com.netease.arctic.ams.api.exception;

public class OptimizingClosedException extends ArcticRuntimeException {

  private final long processId;

  public OptimizingClosedException(long processId) {
    super("Optimizing process already closed, ignore " + processId);
    this.processId = processId;
  }

  public long getProcessId() {
    return processId;
  }
}
