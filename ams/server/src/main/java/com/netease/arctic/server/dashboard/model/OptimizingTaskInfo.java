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

import static com.netease.arctic.server.ArcticServiceConstants.INVALID_TIME;

import com.netease.arctic.server.optimizing.TaskRuntime;

import java.util.Map;

public class OptimizingTaskInfo {
  public static String RETRY_COUNT_PROP = "retry-count";
  public static String OPTIMIZER_TOKEN_PROP = "optimizer.token";
  public static String OPTIMIZER_THREAD_ID_PROP = "optimizer.thread-id";
  private Long tableId;
  private Long processId;
  private int taskId;
  private String partitionData;
  private TaskRuntime.Status status;
  private int retryNum;
  private String optimizerToken;
  private int threadId;
  private long startTime;
  private long endTime;
  private long costTime;
  private String failReason;
  private FilesStatistics inputFiles;
  private FilesStatistics outputFiles;
  private Map<String, String> summary;
  private Map<String, String> properties;

  public OptimizingTaskInfo(
      Long tableId,
      Long processId,
      int taskId,
      String partitionData,
      TaskRuntime.Status status,
      int retryNum,
      String optimizerToken,
      int threadId,
      long startTime,
      long endTime,
      long costTime,
      String failReason,
      FilesStatistics inputFiles,
      FilesStatistics outputFiles,
      Map<String, String> summary,
      Map<String, String> properties) {
    this.tableId = tableId;
    this.processId = processId;
    this.taskId = taskId;
    this.partitionData = partitionData;
    this.status = status;
    this.retryNum = retryNum;
    this.optimizerToken = optimizerToken;
    this.threadId = threadId;
    this.startTime = startTime;
    this.endTime = endTime;
    if (costTime == 0 && startTime != INVALID_TIME && endTime == INVALID_TIME) {
      this.costTime = System.currentTimeMillis() - startTime;
    } else {
      this.costTime = costTime;
    }
    this.failReason = failReason;
    this.inputFiles = inputFiles;
    this.outputFiles = outputFiles;
    this.summary = summary;
    this.properties = properties;
    this.summary.put(RETRY_COUNT_PROP, String.valueOf(retryNum));
    if (this.optimizerToken != null) {
      this.summary.put(OPTIMIZER_TOKEN_PROP, optimizerToken);
      this.summary.put(OPTIMIZER_THREAD_ID_PROP, String.valueOf(threadId));
    }
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

  public String getOptimizerToken() {
    return optimizerToken;
  }

  public void setOptimizerToken(String optimizerToken) {
    this.optimizerToken = optimizerToken;
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

  public Map<String, String> getSummary() {
    return summary;
  }

  public void setSummary(Map<String, String> summary) {
    this.summary = summary;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public FilesStatistics getInputFiles() {
    return inputFiles;
  }

  public void setInputFiles(FilesStatistics inputFiles) {
    this.inputFiles = inputFiles;
  }

  public FilesStatistics getOutputFiles() {
    return outputFiles;
  }

  public void setOutputFiles(FilesStatistics outputFiles) {
    this.outputFiles = outputFiles;
  }
}
