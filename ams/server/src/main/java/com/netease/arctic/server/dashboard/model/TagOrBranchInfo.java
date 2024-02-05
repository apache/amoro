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

import org.apache.iceberg.SnapshotRef;

public class TagOrBranchInfo {
  public static final String TAG = "tag";
  public static final String BRANCH = "branch";
  public static final TagOrBranchInfo MAIN_BRANCH =
      new TagOrBranchInfo(SnapshotRef.MAIN_BRANCH, -1, -1, 0L, 0L, BRANCH);

  private String name;
  private long snapshotId;
  private Integer minSnapshotsToKeep;
  private Long maxSnapshotAgeMs;
  private Long maxRefAgeMs;
  private String type;

  public TagOrBranchInfo() {}

  public TagOrBranchInfo(
      String name,
      long snapshotId,
      Integer minSnapshotsToKeep,
      Long maxSnapshotAgeMs,
      Long maxRefAgeMs,
      String type) {
    this.name = name;
    this.snapshotId = snapshotId;
    this.minSnapshotsToKeep = minSnapshotsToKeep;
    this.maxSnapshotAgeMs = maxSnapshotAgeMs;
    this.maxRefAgeMs = maxRefAgeMs;
    this.type = type;
  }

  public TagOrBranchInfo(String name, SnapshotRef snapshotRef) {
    this.name = name;
    this.snapshotId = snapshotRef.snapshotId();
    this.minSnapshotsToKeep = snapshotRef.minSnapshotsToKeep();
    this.maxSnapshotAgeMs = snapshotRef.maxSnapshotAgeMs();
    this.maxRefAgeMs = snapshotRef.maxRefAgeMs();
    if (snapshotRef.isTag()) {
      this.type = TAG;
    } else if (snapshotRef.isBranch()) {
      this.type = BRANCH;
    } else {
      throw new RuntimeException("Invalid snapshot ref: " + snapshotRef);
    }
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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
