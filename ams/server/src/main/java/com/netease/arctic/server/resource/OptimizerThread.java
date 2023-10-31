package com.netease.arctic.server.resource;

import java.util.Objects;

public class OptimizerThread {
  private final int threadId;
  private final OptimizerInstance optimizer;

  protected OptimizerThread(int threadId, OptimizerInstance optimizer) {
    this.threadId = threadId;
    this.optimizer = optimizer;
  }

  public String getToken() {
    return optimizer.getToken();
  }

  public int getThreadId() {
    return threadId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    OptimizerThread that = (OptimizerThread) o;
    return threadId == that.threadId && Objects.equals(optimizer, that.optimizer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(threadId, optimizer);
  }

  @Override
  public String toString() {
    return "OptimizerThread{" +
        "threadId=" + threadId +
        ", optimizer=" + optimizer +
        '}';
  }
}
