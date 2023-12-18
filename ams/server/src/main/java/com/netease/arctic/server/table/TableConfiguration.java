package com.netease.arctic.server.table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Objects;
import com.netease.arctic.ams.api.Action;
import com.netease.arctic.server.process.optimizing.OptimizingConfig;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.CompatiblePropertyUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TableConfiguration {

  private boolean expireSnapshotEnabled;
  private long snapshotTTLMinutes;
  private long changeDataTTLMinutes;
  private boolean cleanOrphanEnabled;
  private long orphanExistingMinutes;
  private boolean deleteDanglingDeleteFilesEnabled;
  private OptimizingConfig optimizingConfig;
  private DataExpirationConfig expiringDataConfig;
  private TagConfiguration tagConfiguration;

  public TableConfiguration() {}

  public Map<Action, Long> getActionMinIntervals(Set<Action> actions) {
    Map<Action, Long> minIntervals = new HashMap<>();
    if (actions.contains(Action.REFRESH_SNAPSHOT)) {
      minIntervals.put(Action.REFRESH_SNAPSHOT, optimizingConfig.getRefreshMinInterval());
    } else if (actions.contains(Action.EXPIRE_SNAPSHOTS) && isExpireSnapshotEnabled()) {
      minIntervals.put(Action.EXPIRE_SNAPSHOTS, getSnapshotTTLMinutes() * 60 * 1000);
    } else if (actions.contains(Action.CLEAN_ORPHANED_FILES)) {
      minIntervals.put(Action.CLEAN_ORPHANED_FILES, getOrphanExistingMinutes() * 60 * 1000);
    }
    return minIntervals;
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

  public static TableConfiguration parseConfig(Map<String, String> properties) {
    return new TableConfiguration()
        .setExpireSnapshotEnabled(
            CompatiblePropertyUtil.propertyAsBoolean(
                properties,
                TableProperties.ENABLE_TABLE_EXPIRE,
                TableProperties.ENABLE_TABLE_EXPIRE_DEFAULT))
        .setSnapshotTTLMinutes(
            CompatiblePropertyUtil.propertyAsLong(
                properties,
                TableProperties.BASE_SNAPSHOT_KEEP_MINUTES,
                TableProperties.BASE_SNAPSHOT_KEEP_MINUTES_DEFAULT))
        .setChangeDataTTLMinutes(
            CompatiblePropertyUtil.propertyAsLong(
                properties,
                TableProperties.CHANGE_DATA_TTL,
                TableProperties.CHANGE_DATA_TTL_DEFAULT))
        .setCleanOrphanEnabled(
            CompatiblePropertyUtil.propertyAsBoolean(
                properties,
                TableProperties.ENABLE_ORPHAN_CLEAN,
                TableProperties.ENABLE_ORPHAN_CLEAN_DEFAULT))
        .setOrphanExistingMinutes(
            CompatiblePropertyUtil.propertyAsLong(
                properties,
                TableProperties.MIN_ORPHAN_FILE_EXISTING_TIME,
                TableProperties.MIN_ORPHAN_FILE_EXISTING_TIME_DEFAULT))
        .setDeleteDanglingDeleteFilesEnabled(
            CompatiblePropertyUtil.propertyAsBoolean(
                properties,
                TableProperties.ENABLE_DANGLING_DELETE_FILES_CLEAN,
                TableProperties.ENABLE_DANGLING_DELETE_FILES_CLEAN_DEFAULT))
        .setOptimizingConfig(OptimizingConfig.parseOptimizingConfig(properties))
        .setExpiringDataConfig(DataExpirationConfig.parse(properties))
        .setTagConfiguration(TagConfiguration.parse(properties));
  }
}
