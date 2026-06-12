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

package org.apache.amoro.server.process.iceberg;

import org.apache.amoro.Action;
import org.apache.amoro.IcebergActions;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.ConfigOption;
import org.apache.amoro.config.ConfigOptions;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.process.ExecuteEngine;
import org.apache.amoro.process.LocalExecutionEngine;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.process.ProcessTriggerStrategy;
import org.apache.amoro.process.RecoverProcessFailedException;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Default process factory for Iceberg-related maintenance actions in AMS. */
public class IcebergProcessFactory implements ProcessFactory {

  public static final String PLUGIN_NAME = "iceberg";
  public static final ConfigOption<Boolean> SNAPSHOT_EXPIRE_ENABLED =
      ConfigOptions.key("expire-snapshots.enabled").booleanType().defaultValue(true);

  public static final ConfigOption<Duration> SNAPSHOT_EXPIRE_INTERVAL =
      ConfigOptions.key("expire-snapshots.interval")
          .durationType()
          .defaultValue(Duration.ofHours(1));

  public static final ConfigOption<Boolean> ORPHAN_FILES_CLEANING_ENABLED =
      ConfigOptions.key("clean-orphan-files.enabled").booleanType().defaultValue(true);

  public static final ConfigOption<Duration> ORPHAN_FILES_CLEANING_INTERVAL =
      ConfigOptions.key("clean-orphan-files.interval")
          .durationType()
          .defaultValue(Duration.ofDays(1));

  public static final ConfigOption<Boolean> DANGLING_DELETE_FILES_CLEANING_ENABLED =
      ConfigOptions.key("clean-dangling-delete-files.enabled").booleanType().defaultValue(true);

  public static final ConfigOption<Duration> DANGLING_DELETE_FILES_CLEANING_INTERVAL =
      ConfigOptions.key("clean-dangling-delete-files.interval")
          .durationType()
          .defaultValue(Duration.ofDays(1));

  public static final ConfigOption<Boolean> DATA_EXPIRE_ENABLED =
      ConfigOptions.key("expire-data.enabled").booleanType().defaultValue(true);

  public static final ConfigOption<Duration> DATA_EXPIRE_INTERVAL =
      ConfigOptions.key("expire-data.interval").durationType().defaultValue(Duration.ofDays(1));

  public static final ConfigOption<Boolean> AUTO_CREATE_TAGS_ENABLED =
      ConfigOptions.key("auto-create-tags.enabled").booleanType().defaultValue(true);

  public static final ConfigOption<Duration> AUTO_CREATE_TAGS_INTERVAL =
      ConfigOptions.key("auto-create-tags.interval")
          .durationType()
          .defaultValue(Duration.ofMinutes(1));

  public static final ConfigOption<Boolean> SYNC_HIVE_TABLES_ENABLED =
      ConfigOptions.key("sync-hive-tables.enabled").booleanType().defaultValue(false);

  public static final ConfigOption<Duration> SYNC_HIVE_TABLES_INTERVAL =
      ConfigOptions.key("sync-hive-tables.interval")
          .durationType()
          .defaultValue(Duration.ofMinutes(10));

  public static final ConfigOption<Duration> EXPIRE_PROCESS_DATA_RUNTIME_DATA_EXPIRE_INTERVAL =
      ConfigOptions.key("expire-process-data.runtime-data-expire-interval")
          .durationType()
          .defaultValue(Duration.ofHours(1))
          .withDescription(
              "The interval for expiring process runtime data, "
                  + "including optimizing runtime data and completed process metadata.");

  public static final ConfigOption<Duration> EXPIRE_PROCESS_DATA_RUNTIME_DATA_KEEP_TIME =
      ConfigOptions.key("expire-process-data.runtime-data-keep-time")
          .durationType()
          .defaultValue(Duration.ofDays(30))
          .withDescription(
              "The maximum retention time for process runtime data "
                  + "(e.g., process records, process states, task runtimes, optimizing quotas). "
                  + "Data older than this will be cleaned up during expire-process-data execution.");

  public static final ConfigOption<Duration> EXPIRE_PROCESS_DATA_HISTORY_DATA_KEEP_TIME =
      ConfigOptions.key("expire-process-data.history-data-keep-time")
          .durationType()
          .defaultValue(Duration.ofDays(7))
          .withDescription(
              "The maximum retention time for completed process history data. "
                  + "Only applies when it is shorter than runtime-data-keep-time,"
                  + " and only affects terminal process records (SUCCESS/FAILED). "
                  + "Active processes (RUNNING/SUBMITTED/PENDING/CANCELING) are never deleted.");

  private ExecuteEngine localEngine;
  private long expireProcessDataRuntimeKeepTimeMs;
  private long expireProcessDataHistoryKeepTimeMs;
  private final Map<Action, ProcessTriggerStrategy> actions = Maps.newHashMap();
  private final List<TableFormat> formats =
      Lists.newArrayList(TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG, TableFormat.MIXED_HIVE);

  @Override
  public void availableExecuteEngines(Collection<ExecuteEngine> allAvailableEngines) {
    for (ExecuteEngine engine : allAvailableEngines) {
      if (engine instanceof LocalExecutionEngine) {
        this.localEngine = engine;
      }
    }
  }

  @Override
  public Map<TableFormat, Set<Action>> supportedActions() {
    return formats.stream()
        .map(f -> Pair.of(f, actions.keySet()))
        .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
  }

  @Override
  public ProcessTriggerStrategy triggerStrategy(TableFormat format, Action action) {
    return actions.getOrDefault(action, ProcessTriggerStrategy.METADATA_TRIGGER);
  }

  @Override
  public Optional<TableProcess> trigger(TableRuntime tableRuntime, Action action) {
    if (!actions.containsKey(action)) {
      return Optional.empty();
    }

    if (IcebergActions.EXPIRE_SNAPSHOTS.equals(action)) {
      return triggerExpireSnapshot(tableRuntime);
    } else if (IcebergActions.CLEAN_ORPHAN.equals(action)) {
      return triggerCleanOrphans(tableRuntime);
    } else if (IcebergActions.CLEAN_DANGLING_DELETE.equals(action)) {
      return triggerCleanDanglingDelete(tableRuntime);
    } else if (IcebergActions.EXPIRE_DATA.equals(action)) {
      return triggerDataExpiring(tableRuntime);
    } else if (IcebergActions.AUTO_CREATE_TAGS.equals(action)) {
      return triggerAutoCreateTag(tableRuntime);
    } else if (IcebergActions.SYNC_HIVE_TABLES.equals(action)) {
      return triggerHiveCommitSync(tableRuntime);
    } else if (IcebergActions.EXPIRE_PROCESS_DATA.equals(action)) {
      return triggerProcessDataExpiring(tableRuntime);
    } else if (IcebergActions.EXPIRE_BLOCKER.equals(action)) {
      return triggerExpireBlocker(tableRuntime);
    } else if (IcebergActions.OPTIMIZING_COMMIT.equals(action)) {
      return triggerOptimizingCommit(tableRuntime);
    }

    return Optional.empty();
  }

  @Override
  public TableProcess recover(TableRuntime tableRuntime, TableProcessStore store)
      throws RecoverProcessFailedException {
    Action action = store.getAction();
    if (localEngine == null) {
      throw new RecoverProcessFailedException(
          "Local execution engine is not available for IcebergProcessFactory, "
              + "cannot recover action: "
              + action);
    }

    // The following processes are stateless, idempotent one-shot local maintenance tasks
    // (no checkpoint), so recovery simply rebuilds the process so it can run again.
    // The store/processId/tracking is owned by ProcessService.
    if (IcebergActions.EXPIRE_SNAPSHOTS.equals(action)) {
      return new SnapshotsExpiringProcess(tableRuntime, localEngine);
    } else if (IcebergActions.CLEAN_ORPHAN.equals(action)) {
      return new OrphanFilesCleaningProcess(tableRuntime, localEngine);
    } else if (IcebergActions.CLEAN_DANGLING_DELETE.equals(action)) {
      return new DanglingDeleteFilesCleaningProcess(tableRuntime, localEngine);
    } else if (IcebergActions.EXPIRE_DATA.equals(action)) {
      return new DataExpiringProcess(tableRuntime, localEngine);
    } else if (IcebergActions.AUTO_CREATE_TAGS.equals(action)) {
      return new TagsAutoCreatingProcess(tableRuntime, localEngine);
    } else if (IcebergActions.SYNC_HIVE_TABLES.equals(action)) {
      return new HiveCommitSyncProcess(tableRuntime, localEngine);
    } else if (IcebergActions.EXPIRE_PROCESS_DATA.equals(action)) {
      return new ProcessDataExpiringProcess(
          tableRuntime,
          localEngine,
          expireProcessDataRuntimeKeepTimeMs,
          expireProcessDataHistoryKeepTimeMs);
    } else if (IcebergActions.EXPIRE_BLOCKER.equals(action)) {
      return new BlockerExpiringProcess(tableRuntime, localEngine);
    } else if (IcebergActions.OPTIMIZING_COMMIT.equals(action)) {
      return new OptimizingCommitProcess(tableRuntime, localEngine);
    }

    throw new RecoverProcessFailedException(
        "Unsupported action for IcebergProcessFactory: " + action);
  }

  @Override
  public void open(Map<String, String> properties) {
    if (properties == null || properties.isEmpty()) {
      return;
    }
    Configurations configs = Configurations.fromMap(properties);
    if (configs.getBoolean(SNAPSHOT_EXPIRE_ENABLED)) {
      Duration interval = configs.getDuration(SNAPSHOT_EXPIRE_INTERVAL);
      this.actions.put(
          IcebergActions.EXPIRE_SNAPSHOTS, ProcessTriggerStrategy.triggerAtFixRate(interval));
    }

    if (configs.getBoolean(ORPHAN_FILES_CLEANING_ENABLED)) {
      Duration interval = configs.getDuration(ORPHAN_FILES_CLEANING_INTERVAL);
      this.actions.put(
          IcebergActions.CLEAN_ORPHAN, ProcessTriggerStrategy.triggerAtFixRate(interval));
    }

    if (configs.getBoolean(DANGLING_DELETE_FILES_CLEANING_ENABLED)) {
      Duration interval = configs.getDuration(DANGLING_DELETE_FILES_CLEANING_INTERVAL);
      this.actions.put(
          IcebergActions.CLEAN_DANGLING_DELETE, ProcessTriggerStrategy.triggerAtFixRate(interval));
    }

    if (configs.getBoolean(DATA_EXPIRE_ENABLED)) {
      Duration interval = configs.getDuration(DATA_EXPIRE_INTERVAL);
      this.actions.put(
          IcebergActions.EXPIRE_DATA, ProcessTriggerStrategy.triggerAtFixRate(interval));
    }

    if (configs.getBoolean(AUTO_CREATE_TAGS_ENABLED)) {
      Duration interval = configs.getDuration(AUTO_CREATE_TAGS_INTERVAL);
      this.actions.put(
          IcebergActions.AUTO_CREATE_TAGS, ProcessTriggerStrategy.triggerAtFixRate(interval));
    }

    if (configs.getBoolean(SYNC_HIVE_TABLES_ENABLED)) {
      Duration interval = configs.getDuration(SYNC_HIVE_TABLES_INTERVAL);
      this.actions.put(
          IcebergActions.SYNC_HIVE_TABLES, ProcessTriggerStrategy.triggerAtFixRate(interval));
    }

    Duration expireProcessDataInterval =
        configs.getDuration(EXPIRE_PROCESS_DATA_RUNTIME_DATA_EXPIRE_INTERVAL);
    this.actions.put(
        IcebergActions.EXPIRE_PROCESS_DATA,
        ProcessTriggerStrategy.triggerAtFixRate(expireProcessDataInterval));
    this.expireProcessDataRuntimeKeepTimeMs =
        configs.getDuration(EXPIRE_PROCESS_DATA_RUNTIME_DATA_KEEP_TIME).toMillis();
    this.expireProcessDataHistoryKeepTimeMs =
        configs.getDuration(EXPIRE_PROCESS_DATA_HISTORY_DATA_KEEP_TIME).toMillis();

    this.actions.put(
        IcebergActions.EXPIRE_BLOCKER,
        ProcessTriggerStrategy.triggerAtFixRate(Duration.ofHours(1)));

    this.actions.put(
        IcebergActions.OPTIMIZING_COMMIT,
        ProcessTriggerStrategy.triggerAtFixRate(Duration.ofMinutes(1)));
  }

  private Optional<TableProcess> triggerExpireSnapshot(TableRuntime tableRuntime) {
    if (localEngine == null || !tableRuntime.getTableConfiguration().isExpireSnapshotEnabled()) {
      return Optional.empty();
    }

    long lastExecuteTime =
        tableRuntime.getState(DefaultTableRuntime.CLEANUP_STATE_KEY).getLastSnapshotsExpiringTime();
    ProcessTriggerStrategy strategy = actions.get(IcebergActions.EXPIRE_SNAPSHOTS);
    if (System.currentTimeMillis() - lastExecuteTime < strategy.getTriggerInterval().toMillis()) {
      return Optional.empty();
    }

    return Optional.of(new SnapshotsExpiringProcess(tableRuntime, localEngine));
  }

  private Optional<TableProcess> triggerCleanOrphans(TableRuntime tableRuntime) {
    if (localEngine == null || !tableRuntime.getTableConfiguration().isCleanOrphanEnabled()) {
      return Optional.empty();
    }

    long lastExecuteTime =
        tableRuntime.getState(DefaultTableRuntime.CLEANUP_STATE_KEY).getLastOrphanFilesCleanTime();
    ProcessTriggerStrategy strategy = actions.get(IcebergActions.CLEAN_ORPHAN);
    if (System.currentTimeMillis() - lastExecuteTime < strategy.getTriggerInterval().toMillis()) {
      return Optional.empty();
    }

    return Optional.of(new OrphanFilesCleaningProcess(tableRuntime, localEngine));
  }

  private Optional<TableProcess> triggerCleanDanglingDelete(TableRuntime tableRuntime) {
    if (localEngine == null
        || !tableRuntime.getTableConfiguration().isDeleteDanglingDeleteFilesEnabled()) {
      return Optional.empty();
    }

    long lastExecuteTime =
        tableRuntime
            .getState(DefaultTableRuntime.CLEANUP_STATE_KEY)
            .getLastDanglingDeleteFilesCleanTime();
    ProcessTriggerStrategy strategy = actions.get(IcebergActions.CLEAN_DANGLING_DELETE);
    if (System.currentTimeMillis() - lastExecuteTime < strategy.getTriggerInterval().toMillis()) {
      return Optional.empty();
    }

    return Optional.of(new DanglingDeleteFilesCleaningProcess(tableRuntime, localEngine));
  }

  private Optional<TableProcess> triggerDataExpiring(TableRuntime tableRuntime) {
    if (localEngine == null
        || !tableRuntime.getTableConfiguration().getExpiringDataConfig().isEnabled()) {
      return Optional.empty();
    }

    long lastExecuteTime =
        tableRuntime.getState(DefaultTableRuntime.CLEANUP_STATE_KEY).getLastDataExpiringTime();
    ProcessTriggerStrategy strategy = actions.get(IcebergActions.EXPIRE_DATA);
    if (System.currentTimeMillis() - lastExecuteTime < strategy.getTriggerInterval().toMillis()) {
      return Optional.empty();
    }

    return Optional.of(new DataExpiringProcess(tableRuntime, localEngine));
  }

  private Optional<TableProcess> triggerAutoCreateTag(TableRuntime tableRuntime) {
    if (localEngine == null
        || tableRuntime.getFormat() != TableFormat.ICEBERG
        || !tableRuntime.getTableConfiguration().getTagConfiguration().isAutoCreateTag()) {
      return Optional.empty();
    }

    return Optional.of(new TagsAutoCreatingProcess(tableRuntime, localEngine));
  }

  private Optional<TableProcess> triggerHiveCommitSync(TableRuntime tableRuntime) {
    if (localEngine == null || tableRuntime.getFormat() != TableFormat.MIXED_HIVE) {
      return Optional.empty();
    }

    return Optional.of(new HiveCommitSyncProcess(tableRuntime, localEngine));
  }

  private Optional<TableProcess> triggerProcessDataExpiring(TableRuntime tableRuntime) {
    if (localEngine == null) {
      return Optional.empty();
    }

    return Optional.of(
        new ProcessDataExpiringProcess(
            tableRuntime,
            localEngine,
            expireProcessDataRuntimeKeepTimeMs,
            expireProcessDataHistoryKeepTimeMs));
  }

  private Optional<TableProcess> triggerExpireBlocker(TableRuntime tableRuntime) {
    if (localEngine == null) {
      return Optional.empty();
    }

    return Optional.of(new BlockerExpiringProcess(tableRuntime, localEngine));
  }

  private Optional<TableProcess> triggerOptimizingCommit(TableRuntime tableRuntime) {
    if (localEngine == null || !(tableRuntime instanceof DefaultTableRuntime)) {
      return Optional.empty();
    }
    DefaultTableRuntime runtime = (DefaultTableRuntime) tableRuntime;
    if (runtime.getOptimizingStatus() != OptimizingStatus.COMMITTING
        || runtime.getOptimizingProcess() == null) {
      return Optional.empty();
    }
    return Optional.of(new OptimizingCommitProcess(tableRuntime, localEngine));
  }

  @Override
  public void close() {}

  @Override
  public String name() {
    return PLUGIN_NAME;
  }
}
