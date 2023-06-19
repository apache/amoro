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

import com.netease.arctic.server.optimizing.OptimizingType;
import com.netease.arctic.table.TableIdentifier;

/**
 * Keep it only for the compatibility before the front-end upgrade, and delete it after the upgrade.
 */
@Deprecated
public class OptimizedRecord {
  protected TableIdentifier tableIdentifier;
  protected long visibleTime;
  protected long commitTime;
  protected long planTime;
  protected long duration;
  protected FilesStatistics totalFilesStatBeforeCompact;
  protected FilesStatistics totalFilesStatAfterCompact;
  protected OptimizingType optimizeType;

  public OptimizedRecord() {
  }

  public TableIdentifier getTableIdentifier() {
    return tableIdentifier;
  }

  public void setTableIdentifier(TableIdentifier tableIdentifier) {
    this.tableIdentifier = tableIdentifier;
  }

  public long getVisibleTime() {
    return visibleTime;
  }

  public void setVisibleTime(long visibleTime) {
    this.visibleTime = visibleTime;
  }

  public long getCommitTime() {
    return commitTime;
  }

  public void setCommitTime(long commitTime) {
    this.commitTime = commitTime;
  }

  public long getPlanTime() {
    return planTime;
  }

  public void setPlanTime(long planTime) {
    this.planTime = planTime;
  }

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public FilesStatistics getTotalFilesStatBeforeCompact() {
    return totalFilesStatBeforeCompact;
  }

  public void setTotalFilesStatBeforeCompact(
          FilesStatistics totalFilesStatBeforeCompact) {
    this.totalFilesStatBeforeCompact = totalFilesStatBeforeCompact;
  }

  public FilesStatistics getTotalFilesStatAfterCompact() {
    return totalFilesStatAfterCompact;
  }

  public void setTotalFilesStatAfterCompact(
          FilesStatistics totalFilesStatAfterCompact) {
    this.totalFilesStatAfterCompact = totalFilesStatAfterCompact;
  }

  public OptimizingType getOptimizeType() {
    return optimizeType;
  }

  public void setOptimizeType(OptimizingType optimizeType) {
    this.optimizeType = optimizeType;
  }
}
