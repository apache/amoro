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

package org.apache.amoro.config;

import org.apache.amoro.shade.guava32.com.google.common.base.Objects;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TableConfiguration {
  private boolean expireSnapshotEnabled;
  private long snapshotTTLMinutes;
  private int snapshotMinCount;
  private long changeDataTTLMinutes;
  private boolean cleanOrphanEnabled;
  private long orphanExistingMinutes;
  private boolean deleteDanglingDeleteFilesEnabled;
  private OptimizingConfig optimizingConfig;
  private DataExpirationConfig expiringDataConfig;
  private TagConfiguration tagConfiguration;

  public TableConfiguration() {}

  public boolean isExpireSnapshotEnabled() {
    return expireSnapshotEnabled;
  }

  public long getSnapshotTTLMinutes() {
    return snapshotTTLMinutes;
  }

  public int getSnapshotMinCount() {
    return snapshotMinCount;
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

  public TableConfiguration setSnapshotMinCount(int snapshotMinCount) {
    this.snapshotMinCount = snapshotMinCount;
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
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TableConfiguration that = (TableConfiguration) o;
    return expireSnapshotEnabled == that.expireSnapshotEnabled
        && snapshotTTLMinutes == that.snapshotTTLMinutes
        && snapshotMinCount == that.snapshotMinCount
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
        snapshotMinCount,
        changeDataTTLMinutes,
        cleanOrphanEnabled,
        orphanExistingMinutes,
        deleteDanglingDeleteFilesEnabled,
        optimizingConfig,
        expiringDataConfig,
        tagConfiguration);
  }
}
