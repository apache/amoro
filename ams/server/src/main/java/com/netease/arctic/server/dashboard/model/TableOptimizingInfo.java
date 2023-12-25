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

import com.netease.arctic.ams.api.ServerTableIdentifier;
import com.netease.arctic.server.process.DefaultOptimizingState;
import org.apache.iceberg.relocated.com.google.common.base.MoreObjects;

/** Current optimize state of an ArcticTable. */
public class TableOptimizingInfo {

  public TableOptimizingInfo(DefaultOptimizingState optimizingState, String resourceGroup) {
    this.tableIdentifier = optimizingState.getTableIdentifier();
    this.tableName =
        tableIdentifier
            .getCatalog()
            .concat(".")
            .concat(tableIdentifier.getDatabase())
            .concat(".")
            .concat(tableIdentifier.getTableName());
    this.stage = optimizingState.getName();
    this.duration = optimizingState.getDuration();
    this.fileCount = optimizingState.getPendingInput().getInputFileCount();
    this.fileSize = optimizingState.getPendingInput().getInputFileSize();
    this.quota = optimizingState.getTargetQuota();
    this.quotaOccupation = optimizingState.getQuotaOccupy();
    this.groupName = resourceGroup;
  }

  private final ServerTableIdentifier tableIdentifier;
  private final String tableName;
  private final String stage;
  private final long duration;
  private final long fileCount;
  private final long fileSize;
  private final double quota;
  private final double quotaOccupation;
  private final String groupName;

  public ServerTableIdentifier getTableIdentifier() {
    return tableIdentifier;
  }

  public String getTableName() {
    return tableName;
  }

  public String getOptimizingStage() {
    return stage;
  }

  public long getDuration() {
    return duration;
  }

  public long getFileCount() {
    return fileCount;
  }

  public long getFileSize() {
    return fileSize;
  }

  public double getQuota() {
    return quota;
  }

  public double getQuotaOccupation() {
    return quotaOccupation;
  }

  public String getGroupName() {
    return groupName;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("tableIdentifier", tableIdentifier)
        .add("tableName", tableName)
        .add("optimizingStage", stage)
        .add("duration", duration)
        .add("fileCount", fileCount)
        .add("fileSize", fileSize)
        .add("quota", quota)
        .add("quotaOccupation", quotaOccupation)
        .add("groupName", groupName)
        .toString();
  }
}
