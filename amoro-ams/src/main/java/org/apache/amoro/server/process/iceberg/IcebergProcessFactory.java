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
import org.apache.amoro.config.ConfigHelpers;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.process.ProcessTriggerStrategy;
import org.apache.amoro.process.RecoverProcessFailedException;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.server.process.DefaultTableProcessStore;
import org.apache.amoro.server.process.TableProcessMeta;
import org.apache.amoro.server.process.executor.LocalExecutionEngine;
import org.apache.amoro.server.utils.SnowflakeIdGenerator;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Default process factory for Iceberg-related maintenance actions in AMS. */
public class IcebergProcessFactory implements ProcessFactory {

  public static final String PLUGIN_NAME = "iceberg";

  private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

  private boolean expireSnapshotsEnabled = true;
  private int expireSnapshotsThreadCount = 10;
  private Duration expireSnapshotsInterval = Duration.ofHours(1);

  @Override
  public Map<TableFormat, Set<Action>> supportedActions() {
    Set<Action> actions = new HashSet<>();
    actions.add(IcebergActions.EXPIRE_SNAPSHOTS);

    Map<TableFormat, Set<Action>> supported = new HashMap<>();
    supported.put(TableFormat.ICEBERG, actions);
    supported.put(TableFormat.MIXED_ICEBERG, actions);
    supported.put(TableFormat.MIXED_HIVE, actions);
    return supported;
  }

  @Override
  public ProcessTriggerStrategy triggerStrategy(TableFormat format, Action action) {
    if (IcebergActions.EXPIRE_SNAPSHOTS.equals(action)) {
      return new ProcessTriggerStrategy(
          expireSnapshotsInterval, false, Math.max(expireSnapshotsThreadCount, 1));
    }

    return ProcessTriggerStrategy.METADATA_TRIGGER;
  }

  @Override
  public boolean enabled(TableRuntime tableRuntime, Action action) {
    if (!IcebergActions.EXPIRE_SNAPSHOTS.equals(action)) {
      return true;
    }

    return expireSnapshotsEnabled && tableRuntime.getTableConfiguration().isExpireSnapshotEnabled();
  }

  @Override
  public Optional<TableProcess> trigger(TableRuntime tableRuntime, Action action) {
    if (!enabled(tableRuntime, action)) {
      return Optional.empty();
    }

    long processId = idGenerator.generateId();
    TableProcessMeta meta =
        TableProcessMeta.of(
            processId,
            tableRuntime.getTableIdentifier().getId(),
            action.getName(),
            LocalExecutionEngine.ENGINE_NAME,
            Collections.emptyMap());

    TableProcessStore store = new DefaultTableProcessStore(tableRuntime, meta, action);

    if (IcebergActions.EXPIRE_SNAPSHOTS.equals(action)) {
      return Optional.of(new SnapshotsExpiringProcess(tableRuntime, store));
    }

    return Optional.empty();
  }

  @Override
  public TableProcess recover(TableRuntime tableRuntime, TableProcessStore store)
      throws RecoverProcessFailedException {
    if (IcebergActions.EXPIRE_SNAPSHOTS.equals(store.getAction())) {
      return new SnapshotsExpiringProcess(tableRuntime, store);
    }

    throw new RecoverProcessFailedException(
        "Unsupported action for IcebergProcessFactory: " + store.getAction());
  }

  @Override
  public void open(Map<String, String> properties) {
    if (properties == null || properties.isEmpty()) {
      return;
    }

    expireSnapshotsEnabled =
        parseBoolean(properties.get("expire-snapshots.enabled"), expireSnapshotsEnabled);
    expireSnapshotsThreadCount =
        parseInt(properties.get("expire-snapshots.thread-count"), expireSnapshotsThreadCount);
    expireSnapshotsInterval =
        parseDuration(properties.get("expire-snapshots.interval"), expireSnapshotsInterval);
  }

  private boolean parseBoolean(String value, boolean defaultValue) {
    if (value == null) {
      return defaultValue;
    }
    return Boolean.parseBoolean(value.trim());
  }

  private int parseInt(String value, int defaultValue) {
    if (value == null) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  private Duration parseDuration(String value, Duration defaultValue) {
    if (value == null) {
      return defaultValue;
    }
    try {
      return ConfigHelpers.TimeUtils.parseDuration(value);
    } catch (Exception e) {
      return defaultValue;
    }
  }

  @Override
  public void close() {}

  @Override
  public String name() {
    return PLUGIN_NAME;
  }
}
