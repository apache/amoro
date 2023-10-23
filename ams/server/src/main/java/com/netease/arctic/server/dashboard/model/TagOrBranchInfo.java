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

public class TagOrBranchInfo {
  private String name;
  private long snapshotId;
  private Integer minSnapshotsToKeep;
  private Long maxSnapshotAgeMs;
  private Long maxRefAgeMs;

  public TagOrBranchInfo() {
  }

  public TagOrBranchInfo(
      String name,
      long snapshotId,
      Integer minSnapshotsToKeep,
      Long maxSnapshotAgeMs,
      Long maxRefAgeMs) {
    this.name = name;
    this.snapshotId = snapshotId;
    this.minSnapshotsToKeep = minSnapshotsToKeep;
    this.maxSnapshotAgeMs = maxSnapshotAgeMs;
    this.maxRefAgeMs = maxRefAgeMs;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getSnapshotId() {
    return snapshotId;
  }

  public void setSnapshotId(long snapshotId) {
    this.snapshotId = snapshotId;
  }

  public Integer getMinSnapshotsToKeep() {
    return minSnapshotsToKeep;
  }

  public void setMinSnapshotsToKeep(Integer minSnapshotsToKeep) {
    this.minSnapshotsToKeep = minSnapshotsToKeep;
  }

  public Long getMaxSnapshotAgeMs() {
    return maxSnapshotAgeMs;
  }

  public void setMaxSnapshotAgeMs(Long maxSnapshotAgeMs) {
    this.maxSnapshotAgeMs = maxSnapshotAgeMs;
  }

  public Long getMaxRefAgeMs() {
    return maxRefAgeMs;
  }

  public void setMaxRefAgeMs(Long maxRefAgeMs) {
    this.maxRefAgeMs = maxRefAgeMs;
  }
}
