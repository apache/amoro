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

import org.apache.amoro.Action;
import org.apache.amoro.PaimonActions;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.ConfigHelpers;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.process.ProcessTriggerStrategy;
import org.apache.amoro.process.RecoverProcessFailedException;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.server.process.executor.LocalExecutionEngine;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Process factory for periodic Paimon table metadata synchronization. */
public class PaimonProcessFactory implements ProcessFactory {

  public static final String PLUGIN_NAME = "paimon";

  private boolean syncTableMetaEnabled = true;
  private Duration syncTableMetaInterval = Duration.ofHours(1);
  private int triggerParallelism = 1;

  @Override
  public Map<TableFormat, Set<Action>> supportedActions() {
    Map<TableFormat, Set<Action>> supported = new HashMap<>();
    supported.put(TableFormat.PAIMON, Collections.singleton(PaimonActions.SYNC_TABLE_META));
    return supported;
  }

  @Override
  public ProcessTriggerStrategy triggerStrategy(TableFormat format, Action action) {
    if (TableFormat.PAIMON.equals(format) && PaimonActions.SYNC_TABLE_META.equals(action)) {
      return new ProcessTriggerStrategy(
          syncTableMetaInterval, false, Math.max(triggerParallelism, 1));
    }
    return ProcessTriggerStrategy.METADATA_TRIGGER;
  }

  @Override
  public Optional<TableProcess> trigger(TableRuntime tableRuntime, Action action) {
    if (!TableFormat.PAIMON.equals(tableRuntime.getFormat())
        || !PaimonActions.SYNC_TABLE_META.equals(action)
        || !syncTableMetaEnabled) {
      return Optional.empty();
    }

    return Optional.of(new PaimonTableMetaSyncProcess(tableRuntime));
  }

  @Override
  public TableProcess recover(TableRuntime tableRuntime, TableProcessStore store)
      throws RecoverProcessFailedException {
    if (!PaimonActions.SYNC_TABLE_META.equals(store.getAction())) {
      throw new RecoverProcessFailedException(
          "Unsupported action for PaimonProcessFactory: " + store.getAction());
    }
    return new PaimonTableMetaSyncProcess(tableRuntime);
  }

  @Override
  public void open(Map<String, String> properties) {
    if (properties == null || properties.isEmpty()) {
      return;
    }
    syncTableMetaEnabled =
        parseBoolean(properties.get("sync-table-meta.enabled"), syncTableMetaEnabled);
    syncTableMetaInterval =
        parseDuration(properties.get("sync-table-meta.interval"), syncTableMetaInterval);
    triggerParallelism =
        parseInt(properties.get("sync-table-meta.trigger-parallelism"), triggerParallelism);
  }

  @Override
  public void close() {}

  @Override
  public String name() {
    return PLUGIN_NAME;
  }

  public String executionEngine() {
    return LocalExecutionEngine.ENGINE_NAME;
  }

  private boolean parseBoolean(String value, boolean defaultValue) {
    return value == null ? defaultValue : Boolean.parseBoolean(value.trim());
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
}
