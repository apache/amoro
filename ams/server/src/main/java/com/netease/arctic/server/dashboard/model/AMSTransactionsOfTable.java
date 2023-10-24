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

import com.google.common.base.Objects;
import com.netease.arctic.server.dashboard.utils.AmsUtil;

import java.util.Map;

public class AMSTransactionsOfTable {
  private String transactionId;
  private int fileCount;
  private long fileSize;
  private long commitTime;
  private String snapshotId;
  private String operation;
  private Map<String, String> summary;

  private Map<String, String> recordsSummaryForChart;

  private Map<String, String> filesSummaryForChart;



  public AMSTransactionsOfTable() {
  }

  public AMSTransactionsOfTable(
      String transactionId,
      int fileCount,
      long fileSize,
      long commitTime,
      String operation,
      Map<String, String> summary) {
    this.transactionId = transactionId;
    this.fileCount = fileCount;
    this.fileSize = fileSize;
    this.commitTime = commitTime;
    this.snapshotId = this.transactionId;
    this.operation = operation;
    this.summary = summary;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public int getFileCount() {
    return fileCount;
  }

  public void setFileCount(int fileCount) {
    this.fileCount = fileCount;
  }

  public String getFileSize() {
    return AmsUtil.byteToXB(fileSize);
  }

  public long getOriginalFileSize() {
    return fileSize;
  }

  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }

  public long getCommitTime() {
    return commitTime;
  }

  public void setCommitTime(long commitTime) {
    this.commitTime = commitTime;
  }

  public String getSnapshotId() {
    return snapshotId;
  }

  public void setSnapshotId(String snapshotId) {
    this.snapshotId = snapshotId;
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public Map<String, String> getSummary() {
    return summary;
  }

  public void setSummary(Map<String, String> summary) {
    this.summary = summary;
  }

  public Map<String, String> getRecordsSummaryForChart() {
    return recordsSummaryForChart;
  }

  public void setRecordsSummaryForChart(Map<String, String> recordsSummaryForChart) {
    this.recordsSummaryForChart = recordsSummaryForChart;
  }

  public Map<String, String> getFilesSummaryForChart() {
    return filesSummaryForChart;
  }

  public void setFilesSummaryForChart(Map<String, String> filesSummaryForChart) {
    this.filesSummaryForChart = filesSummaryForChart;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AMSTransactionsOfTable)) {
      return false;
    }
    AMSTransactionsOfTable that = (AMSTransactionsOfTable) o;
    return fileCount == that.fileCount && commitTime == that.commitTime &&
        Objects.equal(transactionId, that.transactionId) &&
        Objects.equal(fileSize, that.fileSize) &&
        Objects.equal(snapshotId, that.snapshotId) &&
        Objects.equal(operation, that.operation) &&
        Objects.equal(summary, that.summary);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(transactionId, fileCount, fileSize, commitTime, snapshotId, operation, summary);
  }
}
