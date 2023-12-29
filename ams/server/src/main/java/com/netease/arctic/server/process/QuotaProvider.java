package com.netease.arctic.server.process;

import com.netease.arctic.ams.api.process.ProcessState;
import com.netease.arctic.server.persistence.StatedPersistentBase;
import com.netease.arctic.server.persistence.mapper.QuotaMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QuotaProvider extends StatedPersistentBase {

  private final List<QuotaConsumer> consumers = new ArrayList<>();
  private long tableId;
  private long startProcessId;
  private long startTime;
  private boolean reset;
  private long quotaRuntime;
  private double quotaTarget;

  private QuotaProvider() {}

  public QuotaProvider(long tableId) {
    this.tableId = tableId;
    doAs(QuotaMapper.class, mapper -> mapper.insertQuota(this));
  }

  protected void addConsumer(ProcessState state, Collection<? extends QuotaConsumer> consumerList) {
    invokeConsistency(
        () -> {
          if (reset) {
            consumers.clear();
            quotaRuntime = 0;
            startProcessId = state.getId();
            startTime = state.getStartTime();
            reset = false;
            doAs(QuotaMapper.class, mapper -> mapper.updateQuota(this));
          }
          consumers.addAll(consumerList);
        });
  }

  /**
   * set quota target for every reboot time
   *
   * @param quotaTarget
   */
  public void setQuotaTarget(double quotaTarget) {
    stateLock.lock();
    try {
      this.quotaTarget = quotaTarget;
    } finally {
      stateLock.unlock();
    }
  }

  protected void removeConsumer(QuotaConsumer quotaConsumer, boolean isReset) {
    invokeConsistency(
        () -> {
          reset = isReset;
          quotaRuntime += quotaConsumer.getQuotaRuntime();
          doAs(QuotaMapper.class, mapper -> mapper.updateQuota(this));
          consumers.remove(quotaConsumer);
        });
  }

  public long getQuotaRuntime() {
    stateLock.lock();
    try {
      return quotaRuntime;
    } finally {
      stateLock.unlock();
    }
  }

  public double getQuotaTarget() {
    stateLock.lock();
    try {
      return quotaTarget;
    } finally {
      stateLock.unlock();
    }
  }

  public long getTableId() {
    return tableId;
  }

  public long getStartProcessId() {
    return startProcessId;
  }

  public double getQuotaValue() {
    stateLock.lock();
    try {
      return (double)
              (quotaRuntime + consumers.stream().mapToLong(QuotaConsumer::getQuotaRuntime).sum())
          / (System.currentTimeMillis() - startTime);
    } finally {
      stateLock.unlock();
    }
  }

  public double getQuotaOccupy() {
    stateLock.lock();
    try {
      return (quotaRuntime + consumers.stream().mapToLong(QuotaConsumer::getQuotaRuntime).sum())
          * quotaTarget
          / (System.currentTimeMillis() - startTime);
    } finally {
      stateLock.unlock();
    }
  }

  public void release() {
    doAs(QuotaMapper.class, mapper -> mapper.deleteQuota(tableId));
  }
}
