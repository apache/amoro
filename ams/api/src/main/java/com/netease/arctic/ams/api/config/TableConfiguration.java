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

package com.netease.arctic.ams.api.config;

import com.google.common.base.Objects;
import com.netease.arctic.ams.api.Action;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Configuration for a table, containing OptimizingConfig, DataExpirationConfig, and
 * TagConfiguration.
 */
public class TableConfiguration {

  // The maximum retry count for executing process.
  private int maxExecuteRetryCount;
  // Whether to expire snapshots.
  private boolean expireSnapshotEnabled;
  // The time to live for snapshots.
  private long snapshotTTLMinutes;
  // The time to live for change store data.
  private long changeDataTTLMinutes;
  // Whether to clean orphaned files.
  private boolean cleanOrphanEnabled;
  // The time to live for orphaned files.
  private long orphanExistingMinutes;
  // Whether to delete dangling delete files.
  private boolean deleteDanglingDeleteFilesEnabled;
  // The optimizing configuration.
  private OptimizingConfig optimizingConfig;
  // The data expiration configuration.
  private DataExpirationConfig expiringDataConfig;
  // The tag configuration.
  private TagConfiguration tagConfiguration;

  public TableConfiguration() {}

  public Map<Action, Long> getActionMinIntervals(Set<Action> actions) {
    Map<Action, Long> minIntervals = new HashMap<>();
    if (actions.contains(Action.REFRESH_METADATA)) {
      minIntervals.put(Action.REFRESH_METADATA, optimizingConfig.getRefreshMinInterval());
    } else if (actions.contains(Action.EXPIRE_SNAPSHOTS) && isExpireSnapshotEnabled()) {
      minIntervals.put(Action.EXPIRE_SNAPSHOTS, getSnapshotTTLMinutes() * 60 * 1000);
    } else if (actions.contains(Action.CLEAN_ORPHANED_FILES)) {
      minIntervals.put(Action.CLEAN_ORPHANED_FILES, getOrphanExistingMinutes() * 60 * 1000);
    }
    return minIntervals;
  }

  /**
   * Get the maximum retry count for executing process.
   *
   * @return the maximum retry count
   */
  public int getMaxExecuteRetryCount() {
    return maxExecuteRetryCount;
  }

  public boolean isExpireSnapshotEnabled() {
    return expireSnapshotEnabled;
  }

  public long getSnapshotTTLMinutes() {
    return snapshotTTLMinutes;
  }

  public long getChangeDataTTLMinutes() {
    return changeDataTTLMinutes;
  }

  public boolean isCleanOrphanEnabled() {
    return cleanOrphanEnabled;
  }

  public long getOrphanExistingMinutes() {
    return orphanExistingMinutes;
  }

  public OptimizingConfig getOptimizingConfig() {
    return optimizingConfig;
  }

  public TableConfiguration setMaxExecuteRetryCount(int maxExecuteRetryCount) {
    this.maxExecuteRetryCount = maxExecuteRetryCount;
    return this;
  }

  public TableConfiguration setOptimizingConfig(OptimizingConfig optimizingConfig) {
    this.optimizingConfig = optimizingConfig;
    return this;
  }

  public TableConfiguration setExpireSnapshotEnabled(boolean expireSnapshotEnabled) {
    this.expireSnapshotEnabled = expireSnapshotEnabled;
    return this;
  }

  public TableConfiguration setSnapshotTTLMinutes(long snapshotTTLMinutes) {
    this.snapshotTTLMinutes = snapshotTTLMinutes;
    return this;
  }

  public TableConfiguration setChangeDataTTLMinutes(long changeDataTTLMinutes) {
    this.changeDataTTLMinutes = changeDataTTLMinutes;
    return this;
  }

  public TableConfiguration setCleanOrphanEnabled(boolean cleanOrphanEnabled) {
    this.cleanOrphanEnabled = cleanOrphanEnabled;
    return this;
  }

  public TableConfiguration setOrphanExistingMinutes(long orphanExistingMinutes) {
    this.orphanExistingMinutes = orphanExistingMinutes;
    return this;
  }

  public boolean isDeleteDanglingDeleteFilesEnabled() {
    return deleteDanglingDeleteFilesEnabled;
  }

  public TableConfiguration setDeleteDanglingDeleteFilesEnabled(
      boolean deleteDanglingDeleteFilesEnabled) {
    this.deleteDanglingDeleteFilesEnabled = deleteDanglingDeleteFilesEnabled;
    return this;
  }

  public DataExpirationConfig getExpiringDataConfig() {
    return Optional.ofNullable(expiringDataConfig).orElse(new DataExpirationConfig());
  }

  public TableConfiguration setExpiringDataConfig(DataExpirationConfig expiringDataConfig) {
    this.expiringDataConfig = expiringDataConfig;
    return this;
  }

  public TagConfiguration getTagConfiguration() {
    return Optional.ofNullable(tagConfiguration).orElse(new TagConfiguration());
  }

  public TableConfiguration setTagConfiguration(TagConfiguration tagConfiguration) {
    this.tagConfiguration = tagConfiguration;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TableConfiguration that = (TableConfiguration) o;
    return expireSnapshotEnabled == that.expireSnapshotEnabled
        && snapshotTTLMinutes == that.snapshotTTLMinutes
        && changeDataTTLMinutes == that.changeDataTTLMinutes
        && cleanOrphanEnabled == that.cleanOrphanEnabled
        && orphanExistingMinutes == that.orphanExistingMinutes
        && deleteDanglingDeleteFilesEnabled == that.deleteDanglingDeleteFilesEnabled
        && Objects.equal(optimizingConfig, that.optimizingConfig)
        && Objects.equal(expiringDataConfig, that.expiringDataConfig)
        && Objects.equal(tagConfiguration, that.tagConfiguration);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        expireSnapshotEnabled,
        snapshotTTLMinutes,
        changeDataTTLMinutes,
        cleanOrphanEnabled,
        orphanExistingMinutes,
        deleteDanglingDeleteFilesEnabled,
        optimizingConfig,
        expiringDataConfig,
        tagConfiguration);
  }
}
