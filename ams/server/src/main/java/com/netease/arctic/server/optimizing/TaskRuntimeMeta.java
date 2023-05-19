package com.netease.arctic.server.optimizing;

import com.netease.arctic.ams.api.OptimizingTaskId;
import com.netease.arctic.server.ArcticServiceConstants;

import java.util.Map;

public class TaskRuntimeMeta {
  private String partition;
  private OptimizingTaskId taskId;
  private TaskRuntime.Status status;
  private int retry;
  private long startTime = ArcticServiceConstants.INVALID_TIME;
  private long endTime = ArcticServiceConstants.INVALID_TIME;
  private long costTime = 0;
  private OptimizingQueue.OptimizingThread optimizingThread;
  private String failReason;
  private Object outputBytes;
  private MetricsSummary summary;
  private long tableId;
  private Map<String, String> properties;

  public TaskRuntime constructTaskRuntime() {
    return new TaskRuntime(this);
  }

  public String getPartition() {
    return partition;
  }

  public void setPartition(String partition) {
    this.partition = partition;
  }

  public OptimizingTaskId getTaskId() {
    return taskId;
  }

  public void setTaskId(OptimizingTaskId taskId) {
    this.taskId = taskId;
  }

  public TaskRuntime.Status getStatus() {
    return status;
  }

  public void setStatus(TaskRuntime.Status status) {
    this.status = status;
  }

  public int getRetry() {
    return retry;
  }

  public void setRetry(int retry) {
    this.retry = retry;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public long getCostTime() {
    return costTime;
  }

  public void setCostTime(long costTime) {
    this.costTime = costTime;
  }

  public OptimizingQueue.OptimizingThread getOptimizingThread() {
    return optimizingThread;
  }

  public void setOptimizingThread(OptimizingQueue.OptimizingThread optimizingThread) {
    this.optimizingThread = optimizingThread;
  }

  public String getFailReason() {
    return failReason;
  }

  public void setFailReason(String failReason) {
    this.failReason = failReason;
  }

  public Object getOutputBytes() {
    return outputBytes;
  }

  public void setOutputBytes(Object outputBytes) {
    this.outputBytes = outputBytes;
  }

  public MetricsSummary getSummary() {
    return summary;
  }

  public void setSummary(MetricsSummary summary) {
    this.summary = summary;
  }

  public long getTableId() {
    return tableId;
  }

  public void setTableId(long tableId) {
    this.tableId = tableId;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }
}
