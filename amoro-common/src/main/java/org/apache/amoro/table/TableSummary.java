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

package org.apache.amoro.table;

public class TableSummary {
  private long totalFileSize = 0L;
  private int totalFileCount = 0;
  // -1 means not calculated
  private int healthScore = -1;
  private int smallFileScore = 0;
  private int equalityDeleteScore = 0;
  private int positionalDeleteScore = 0;
  private long pendingFileSize = 0L;
  private int pendingFileCount = 0;

  public TableSummary copy() {
    TableSummary summary = new TableSummary();
    summary.setTotalFileSize(this.totalFileSize);
    summary.setTotalFileCount(this.totalFileCount);
    summary.setHealthScore(this.healthScore);
    summary.setSmallFileScore(this.smallFileScore);
    summary.setEqualityDeleteScore(this.equalityDeleteScore);
    summary.setPositionalDeleteScore(this.positionalDeleteScore);
    summary.setPendingFileSize(this.pendingFileSize);
    summary.setPendingFileCount(this.pendingFileCount);
    return summary;
  }

  public long getTotalFileSize() {
    return totalFileSize;
  }

  public void setTotalFileSize(long totalFileSize) {
    this.totalFileSize = totalFileSize;
  }

  public int getTotalFileCount() {
    return totalFileCount;
  }

  public void setTotalFileCount(int totalFileCount) {
    this.totalFileCount = totalFileCount;
  }

  public int getHealthScore() {
    return healthScore;
  }

  public void setHealthScore(int healthScore) {
    this.healthScore = healthScore;
  }

  public int getSmallFileScore() {
    return smallFileScore;
  }

  public void setSmallFileScore(int smallFileScore) {
    this.smallFileScore = smallFileScore;
  }

  public int getEqualityDeleteScore() {
    return equalityDeleteScore;
  }

  public void setEqualityDeleteScore(int equalityDeleteScore) {
    this.equalityDeleteScore = equalityDeleteScore;
  }

  public int getPositionalDeleteScore() {
    return positionalDeleteScore;
  }

  public void setPositionalDeleteScore(int positionalDeleteScore) {
    this.positionalDeleteScore = positionalDeleteScore;
  }

  public long getPendingFileSize() {
    return pendingFileSize;
  }

  public void setPendingFileSize(long pendingFileSize) {
    this.pendingFileSize = pendingFileSize;
  }

  public int getPendingFileCount() {
    return pendingFileCount;
  }

  public void setPendingFileCount(int pendingFileCount) {
    this.pendingFileCount = pendingFileCount;
  }
}
