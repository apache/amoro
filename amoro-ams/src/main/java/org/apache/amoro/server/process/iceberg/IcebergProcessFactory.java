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
      ConfigOptions.key("expire-snapshot.interval")
          .durationType()
          .defaultValue(Duration.ofHours(1));

  private ExecuteEngine localEngine;
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
    }
    return Optional.empty();
  }

  @Override
  public TableProcess recover(TableRuntime tableRuntime, TableProcessStore store)
      throws RecoverProcessFailedException {
    throw new RecoverProcessFailedException(
        "Unsupported action for IcebergProcessFactory: " + store.getAction());
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

  @Override
  public void close() {}

  @Override
  public String name() {
    return PLUGIN_NAME;
  }
}
