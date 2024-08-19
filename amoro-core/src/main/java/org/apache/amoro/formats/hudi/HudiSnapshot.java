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

package org.apache.amoro.formats.hudi;

import java.util.Map;

public class HudiSnapshot {

  private final String snapshotId;
  private final long commitTimestamp;

  private final String operationType;
  private final String operation;

  private final int totalFileCount;

  private final int baseFileCount;
  private final int logFileCount;

  private final long totalFileSize;
  private final long totalRecordCount;

  private final Map<String, String> summary;

  public HudiSnapshot(
      String snapshotId,
      long commitTimestamp,
      String operationType,
      String operation,
      int totalFileCount,
      int totalDataFileCount,
      int totalLogFileCount,
      long totalFileSize,
      long totalRecordCount,
      Map<String, String> summary) {
    this.snapshotId = snapshotId;
    this.commitTimestamp = commitTimestamp;
    this.operationType = operationType;
    this.operation = operation;
    this.totalFileCount = totalFileCount;
    this.baseFileCount = totalDataFileCount;
    this.logFileCount = totalLogFileCount;
    this.totalFileSize = totalFileSize;
    this.totalRecordCount = totalRecordCount;
    this.summary = summary;
  }

  public String getSnapshotId() {
    return snapshotId;
  }

  public long getCommitTimestamp() {
    return commitTimestamp;
  }

  public String getOperationType() {
    return operationType;
  }

  public String getOperation() {
    return operation;
  }

  public int getTotalFileCount() {
    return totalFileCount;
  }

  public int getBaseFileCount() {
    return baseFileCount;
  }

  public int getLogFileCount() {
    return logFileCount;
  }

  public long getTotalFileSize() {
    return totalFileSize;
  }

  public long getTotalRecordCount() {
    return totalRecordCount;
  }

  public Map<String, String> getSummary() {
    return summary;
  }
}
