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

package org.apache.amoro.server.dashboard.model;

import org.apache.amoro.api.ServerTableIdentifier;
import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;

public class OverviewUnhealthTable {
  private final ServerTableIdentifier tableIdentifier;
  private String tableName;
  private int healthScore;
  private int fileCount;
  private long totalSize;
  private long averageFileSize;

  public OverviewUnhealthTable(
      ServerTableIdentifier tableIdentifier,
      int healthScore,
      int fileCount,
      long totalSize,
      long averageFileSize) {
    this.tableIdentifier = tableIdentifier;
    this.tableName =
        tableIdentifier
            .getCatalog()
            .concat(".")
            .concat(tableIdentifier.getDatabase())
            .concat(".")
            .concat(tableIdentifier.getTableName());
    this.healthScore = healthScore;
    this.fileCount = fileCount;
    this.totalSize = totalSize;
    this.averageFileSize = averageFileSize;
  }

  public OverviewUnhealthTable(
      ServerTableIdentifier tableIdentifier, int healthScore, FilesStatistics filesStatistics) {
    this(
        tableIdentifier,
        healthScore,
        filesStatistics.getFileCnt(),
        filesStatistics.getTotalSize(),
        filesStatistics.getAverageSize());
  }

  public ServerTableIdentifier getTableIdentifier() {
    return tableIdentifier;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public int getHealthScore() {
    return healthScore;
  }

  public void setHealthScore(int healthScore) {
    this.healthScore = healthScore;
  }

  public int getFileCount() {
    return fileCount;
  }

  public void setFileCount(int fileCount) {
    this.fileCount = fileCount;
  }

  public long getTotalSize() {
    return totalSize;
  }

  public void setTotalSize(long totalSize) {
    this.totalSize = totalSize;
  }

  public long getAverageFileSize() {
    return averageFileSize;
  }

  public void setAverageFileSize(long averageFileSize) {
    this.averageFileSize = averageFileSize;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("tableName", tableName)
        .add("healthScore", healthScore)
        .add("fileCount", fileCount)
        .add("totalSize", totalSize)
        .add("averageFileSize", averageFileSize)
        .toString();
  }
}
