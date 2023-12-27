package com.netease.arctic.server.persistence;

import com.netease.arctic.ams.api.process.ProcessStatus;

public class ArbitraryStatePersistence {

  private long tableId;
  private int tableAction;
  private long processId;
  private ProcessStatus status;
  private int retryNum;
  private long startTime;
  private long endTime;
  private String processName;
  private String failReason;
  private String summary;

  ArbitraryStatePersistence() {}

  // Getter methods
  public long getTableId() {
    return tableId;
  }

  public int getTableAction() {
    return tableAction;
  }

  public long getProcessId() {
    return processId;
  }

  public int getRetryNum() {
    return retryNum;
  }

  public long getStartTime() {
    return startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public String getProcessName() {
    return processName;
  }

  public ProcessStatus getStatus() {
    return status;
  }

  public String getFailReason() {
    return failReason;
  }

  public String getSummary() {
    return summary;
  }
}
