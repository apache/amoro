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

package org.apache.amoro.server.process.paimon;

import static org.apache.amoro.server.AmoroManagementConf.EXPIRE_SNAPSHOTS_ENABLED;
import static org.apache.amoro.server.AmoroManagementConf.EXPIRE_SNAPSHOTS_INTERVAL;

import org.apache.amoro.Action;
import org.apache.amoro.PaimonActions;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.ConfigOption;
import org.apache.amoro.config.ConfigOptions;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.process.ExecuteEngine;
import org.apache.amoro.process.HttpRemoteSparkStandAloneSubmit;
import org.apache.amoro.process.LocalExecutionEngine;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.process.ProcessTriggerStrategy;
import org.apache.amoro.process.RecoverProcessFailedException;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Process factory for Paimon table maintenance actions including metadata synchronization and
 * snapshot expiration.
 */
public class PaimonProcessFactory implements ProcessFactory {

  private static final Logger LOG = LoggerFactory.getLogger(PaimonProcessFactory.class);

  public static final String PLUGIN_NAME = "paimon";

  public static final ConfigOption<Boolean> SYNC_TABLE_META_ENABLED =
      ConfigOptions.key("sync-table-meta.enabled").booleanType().defaultValue(true);

  public static final ConfigOption<Duration> SYNC_TABLE_META_INTERVAL =
      ConfigOptions.key("sync-table-meta.interval")
          .durationType()
          .defaultValue(Duration.ofHours(1));

  public static final ConfigOption<Integer> SYNC_TABLE_META_TRIGGER_PARALLELISM =
      ConfigOptions.key("sync-table-meta.trigger-parallelism").intType().defaultValue(1);

  public static final ConfigOption<Integer> EXPIRE_SNAPSHOTS_SPARK_VERSION =
      ConfigOptions.key("expire-snapshots.spark-version").intType().defaultValue(321);

  private ExecuteEngine sparkEngine;
  private final Map<Action, ProcessTriggerStrategy> actions = Maps.newHashMap();
  private int expireSnapshotsSparkVersion = EXPIRE_SNAPSHOTS_SPARK_VERSION.defaultValue();

  @Override
  public void availableExecuteEngines(Collection<ExecuteEngine> allAvailableEngines) {
    for (ExecuteEngine engine : allAvailableEngines) {
      if (engine instanceof HttpRemoteSparkStandAloneSubmit) {
        this.sparkEngine = engine;
      }
    }
  }

  @Override
  public Map<TableFormat, Set<Action>> supportedActions() {
    return Collections.singletonMap(TableFormat.PAIMON, actions.keySet());
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

    if (PaimonActions.SYNC_TABLE_META.equals(action)) {
      return Optional.of(new PaimonTableMetaSyncProcess(tableRuntime));
    }

    if (PaimonActions.EXPIRE_SNAPSHOTS.equals(action)) {
      return triggerExpireSnapshots(tableRuntime);
    }

    return Optional.empty();
  }

  @Override
  public TableProcess recover(TableRuntime tableRuntime, TableProcessStore store)
      throws RecoverProcessFailedException {
    if (PaimonActions.SYNC_TABLE_META.equals(store.getAction())) {
      return new PaimonTableMetaSyncProcess(tableRuntime);
    }
    if (PaimonActions.EXPIRE_SNAPSHOTS.equals(store.getAction())) {
      if (sparkEngine == null) {
        throw new RecoverProcessFailedException(
            "Spark engine not available for expire-paimon-snapshots recovery");
      }
      return new PaimonExpireSnapshotProcess(
          tableRuntime, sparkEngine, expireSnapshotsSparkVersion);
    }
    throw new RecoverProcessFailedException(
        "Unsupported action for PaimonProcessFactory: " + store.getAction());
  }

  @Override
  public void open(Map<String, String> properties) {
    resetConfiguredState();
    Map<String, String> safeProperties = properties == null ? Collections.emptyMap() : properties;
    Configurations configs = Configurations.fromMap(safeProperties);

    if (configs.getBoolean(SYNC_TABLE_META_ENABLED)) {
      Duration interval = configs.getDuration(SYNC_TABLE_META_INTERVAL);
      int parallelism = configs.getInteger(SYNC_TABLE_META_TRIGGER_PARALLELISM);
      this.actions.put(
          PaimonActions.SYNC_TABLE_META,
          new ProcessTriggerStrategy(interval, false, Math.max(parallelism, 1)));
    }

    if (configs.getBoolean(EXPIRE_SNAPSHOTS_ENABLED)) {
      Duration interval = configs.getDuration(EXPIRE_SNAPSHOTS_INTERVAL);
      this.actions.put(
          PaimonActions.EXPIRE_SNAPSHOTS, ProcessTriggerStrategy.triggerAtFixRate(interval));
    }
    this.expireSnapshotsSparkVersion = configs.getInteger(EXPIRE_SNAPSHOTS_SPARK_VERSION);
    LOG.info("Apache Paimon Process Factory initialized actions {}.", this.actions);
  }

  @Override
  public void close() {
    resetConfiguredState();
  }

  @Override
  public String name() {
    return PLUGIN_NAME;
  }

  public String executionEngine() {
    return LocalExecutionEngine.ENGINE_NAME;
  }

  private Optional<TableProcess> triggerExpireSnapshots(TableRuntime tableRuntime) {
    if (sparkEngine == null) {
      return Optional.empty();
    }
    ProcessTriggerStrategy strategy = actions.get(PaimonActions.EXPIRE_SNAPSHOTS);
    return PaimonExpireSnapshotProcess.trigger(
            tableRuntime, sparkEngine, expireSnapshotsSparkVersion, strategy.getTriggerInterval())
        .map(p -> p);
  }

  private void resetConfiguredState() {
    actions.clear();
    expireSnapshotsSparkVersion = EXPIRE_SNAPSHOTS_SPARK_VERSION.defaultValue();
  }
}
