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
import org.apache.commons.collections.MapUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TableOptimizeRuntime {
  public static final long INVALID_SNAPSHOT_ID = -1L;

  private TableIdentifier tableIdentifier;
  // for unKeyedTable or base table
  private long currentSnapshotId = INVALID_SNAPSHOT_ID;
  // for change table
  private long currentChangeSnapshotId = INVALID_SNAPSHOT_ID;
  private TableOptimizeInfo.OptimizeStatus optimizeStatus = TableOptimizeInfo.OptimizeStatus.Idle;
  private long optimizeStatusStartTime = -1;
  private final Map<String, Long> latestMajorOptimizeTime = new HashMap<>();
  private final Map<String, Long> latestMinorOptimizeTime = new HashMap<>();
  private String latestTaskHistoryId;
  private volatile boolean isRunning;

  public TableOptimizeRuntime() {
  }

  public TableOptimizeRuntime(TableIdentifier tableIdentifier) {
    this.tableIdentifier = tableIdentifier;
  }

  public TableOptimizeRuntime(String catalog, String database, String tableName) {
    this.tableIdentifier = TableIdentifier.of(catalog, database, tableName);
  }

  public TableIdentifier getTableIdentifier() {
    return tableIdentifier;
  }

  public void setTableIdentifier(TableIdentifier tableIdentifier) {
    this.tableIdentifier = tableIdentifier;
  }

  public long getCurrentSnapshotId() {
    return currentSnapshotId;
  }

  public void setCurrentSnapshotId(long currentSnapshotId) {
    this.currentSnapshotId = currentSnapshotId;
  }

  public void putLatestMajorOptimizeTime(String partition, long time) {
    Long oldValue = latestMajorOptimizeTime.putIfAbsent(partition, time);
    if (oldValue != null) {
      if (time > oldValue) {
        latestMajorOptimizeTime.put(partition, time);
      }
    }
  }

  public TableOptimizeInfo.OptimizeStatus getOptimizeStatus() {
    return optimizeStatus;
  }

  public void setOptimizeStatus(
      TableOptimizeInfo.OptimizeStatus optimizeStatus) {
    this.optimizeStatus = optimizeStatus;
  }

  public long getOptimizeStatusStartTime() {
    return optimizeStatusStartTime;
  }

  public void setOptimizeStatusStartTime(long optimizeStatusStartTime) {
    this.optimizeStatusStartTime = optimizeStatusStartTime;
  }

  public long getLatestMajorOptimizeTime(String partition) {
    Long time = latestMajorOptimizeTime.get(partition);
    return time == null ? -1 : time;
  }

  public void putLatestMinorOptimizeTime(String partition, long time) {
    Long oldValue = latestMinorOptimizeTime.putIfAbsent(partition, time);
    if (oldValue != null) {
      if (time > oldValue) {
        latestMinorOptimizeTime.put(partition, time);
      }
    }
  }

  public long getLatestMinorOptimizeTime(String partition) {
    Long time = latestMinorOptimizeTime.get(partition);
    return time == null ? -1 : time;
  }

  public Set<String> getPartitions() {
    Set<String> result = new HashSet<>();
    if (MapUtils.isNotEmpty(latestMajorOptimizeTime)) {
      result.addAll(latestMajorOptimizeTime.keySet());
    }
    if (MapUtils.isNotEmpty(latestMinorOptimizeTime)) {
      result.addAll(latestMinorOptimizeTime.keySet());
    }

    return result;
  }

  public long getCurrentChangeSnapshotId() {
    return currentChangeSnapshotId;
  }

  public void setCurrentChangeSnapshotId(long currentChangeSnapshotId) {
    this.currentChangeSnapshotId = currentChangeSnapshotId;
  }

  public String getLatestTaskHistoryId() {
    return latestTaskHistoryId;
  }

  public void setLatestTaskHistoryId(String latestTaskHistoryId) {
    this.latestTaskHistoryId = latestTaskHistoryId;
  }

  public boolean isRunning() {
    return isRunning;
  }

  public void setRunning(boolean running) {
    isRunning = running;
  }

  @Override
  public String toString() {
    return "TableOptimizeRuntime{" +
        "tableIdentifier=" + tableIdentifier +
        ", currentSnapshotId=" + currentSnapshotId +
        ", currentChangeSnapshotId=" + currentChangeSnapshotId +
        ", optimizeStatus=" + optimizeStatus +
        ", optimizeStatusStartTime=" + optimizeStatusStartTime +
        ", latestMajorOptimizeTime=" + latestMajorOptimizeTime +
        ", latestMinorOptimizeTime=" + latestMinorOptimizeTime +
        ", latestTaskHistoryId='" + latestTaskHistoryId + '\'' +
        ", isRunning=" + isRunning +
        '}';
  }
}
