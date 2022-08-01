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

package com.netease.arctic.ams.server.model;

import com.netease.arctic.table.TableIdentifier;

public class TableTaskHistory {
  private TableIdentifier tableIdentifier;
  private String taskGroupId;
  private String taskHistoryId;
  private long startTime;
  private long endTime;
  private long costTime;
  private int queueId;

  public TableIdentifier getTableIdentifier() {
    return tableIdentifier;
  }

  public void setTableIdentifier(TableIdentifier tableIdentifier) {
    this.tableIdentifier = tableIdentifier;
  }

  public String getTaskGroupId() {
    return taskGroupId;
  }

  public void setTaskGroupId(String taskGroupId) {
    this.taskGroupId = taskGroupId;
  }

  public String getTaskHistoryId() {
    return taskHistoryId;
  }

  public void setTaskHistoryId(String taskHistoryId) {
    this.taskHistoryId = taskHistoryId;
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

  public int getQueueId() {
    return queueId;
  }

  public void setQueueId(int queueId) {
    this.queueId = queueId;
  }

  @Override
  public String toString() {
    return "TableTaskHistory{" +
        "tableIdentifier=" + tableIdentifier +
        ", taskGroupId='" + taskGroupId + '\'' +
        ", taskHistoryId='" + taskHistoryId + '\'' +
        ", startTime=" + startTime +
        ", endTime=" + endTime +
        ", costTime=" + costTime +
        ", queueId=" + queueId +
        '}';
  }
}
