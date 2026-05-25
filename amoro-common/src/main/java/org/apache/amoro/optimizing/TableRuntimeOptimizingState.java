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

package org.apache.amoro.optimizing;

/**
 * Optimizing state tracked per table runtime. Moved to amoro-common so that format-agnostic
 * interfaces (e.g., AmoroTable.refreshOptimizingState) can reference it without depending on
 * amoro-format-iceberg.
 */
public class TableRuntimeOptimizingState {

  public static final long INVALID_SNAPSHOT_ID = -1L;

  private long currentSnapshotId = INVALID_SNAPSHOT_ID;
  private long currentChangeSnapshotId = INVALID_SNAPSHOT_ID;
  private long lastOptimizedSnapshotId = INVALID_SNAPSHOT_ID;
  private long lastOptimizedChangeSnapshotId = INVALID_SNAPSHOT_ID;
  private long lastMajorOptimizingTime;
  private long lastFullOptimizingTime;
  private long lastMinorOptimizingTime;

  public long getCurrentSnapshotId() {
    return currentSnapshotId;
  }

  public void setCurrentSnapshotId(long currentSnapshotId) {
    this.currentSnapshotId = currentSnapshotId;
  }

  public long getCurrentChangeSnapshotId() {
    return currentChangeSnapshotId;
  }

  public void setCurrentChangeSnapshotId(long currentChangeSnapshotId) {
    this.currentChangeSnapshotId = currentChangeSnapshotId;
  }

  public long getLastOptimizedSnapshotId() {
    return lastOptimizedSnapshotId;
  }

  public void setLastOptimizedSnapshotId(long lastOptimizedSnapshotId) {
    this.lastOptimizedSnapshotId = lastOptimizedSnapshotId;
  }

  public long getLastOptimizedChangeSnapshotId() {
    return lastOptimizedChangeSnapshotId;
  }

  public void setLastOptimizedChangeSnapshotId(long lastOptimizedChangeSnapshotId) {
    this.lastOptimizedChangeSnapshotId = lastOptimizedChangeSnapshotId;
  }

  public long getLastMajorOptimizingTime() {
    return lastMajorOptimizingTime;
  }

  public void setLastMajorOptimizingTime(long lastMajorOptimizingTime) {
    this.lastMajorOptimizingTime = lastMajorOptimizingTime;
  }

  public long getLastFullOptimizingTime() {
    return lastFullOptimizingTime;
  }

  public void setLastFullOptimizingTime(long lastFullOptimizingTime) {
    this.lastFullOptimizingTime = lastFullOptimizingTime;
  }

  public long getLastMinorOptimizingTime() {
    return lastMinorOptimizingTime;
  }

  public void setLastMinorOptimizingTime(long lastMinorOptimizingTime) {
    this.lastMinorOptimizingTime = lastMinorOptimizingTime;
  }
}
