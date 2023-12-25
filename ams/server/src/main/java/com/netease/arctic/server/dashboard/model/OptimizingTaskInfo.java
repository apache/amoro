/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.server.dashboard.model;

import com.netease.arctic.server.process.OptimizingSummary;
import com.netease.arctic.server.process.TaskRuntime;

import java.util.Map;

public class OptimizingTaskInfo {
  private Long tableId;
  private Long processId;
  private int taskId;
  private String partitionData;
  private TaskRuntime.Status status;
  private int retryNum;
  private int threadId;
  private long startTime;
  private long endTime;
  private long costTime;
  private String failReason;
  private OptimizingSummary summary;
  private Map<String, String> properties;

  public OptimizingTaskInfo(
      Long tableId,
      Long processId,
      int taskId,
      String partitionData,
      TaskRuntime.Status status,
      int retryNum,
      int threadId,
      long startTime,
      long endTime,
      long costTime,
      String failReason,
      OptimizingSummary summary,
      Map<String, String> properties) {
    this.tableId = tableId;
    this.processId = processId;
    this.taskId = taskId;
    this.partitionData = partitionData;
    this.status = status;
    this.retryNum = retryNum;
    this.threadId = threadId;
    this.startTime = startTime;
    this.endTime = endTime;
    this.costTime = costTime;
    this.failReason = failReason;
    this.summary = summary;
    this.properties = properties;
  }

  public Long getTableId() {
    return tableId;
  }

  public void setTableId(Long tableId) {
    this.tableId = tableId;
  }

  public Long getProcessId() {
    return processId;
  }

  public void setProcessId(Long processId) {
    this.processId = processId;
  }

  public int getTaskId() {
    return taskId;
  }

  public void setTaskId(int taskId) {
    this.taskId = taskId;
  }

  public String getPartitionData() {
    return partitionData;
  }

  public void setPartitionData(String partitionData) {
    this.partitionData = partitionData;
  }

  public TaskRuntime.Status getStatus() {
    return status;
  }

  public void setStatus(TaskRuntime.Status status) {
    this.status = status;
  }

  public int getRetryNum() {
    return retryNum;
  }

  public void setRetryNum(int retryNum) {
    this.retryNum = retryNum;
  }

  public int getThreadId() {
    return threadId;
  }

  public void setThreadId(int threadId) {
    this.threadId = threadId;
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

  public String getFailReason() {
    return failReason;
  }

  public void setFailReason(String failReason) {
    this.failReason = failReason;
  }

  public OptimizingSummary getSummary() {
    return summary;
  }

  public void setSummary(OptimizingSummary summary) {
    this.summary = summary;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }
}
