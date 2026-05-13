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
 * Process factory for Paimon table <em>maintenance</em> actions (currently metadata synchronization
 * only; the snapshot-expiration path lives on the {@code czy006/paimon-factory-refactor} feature
 * branch and is intentionally excluded from {@code dev-paimon-compact} until that branch merges).
 *
 * <p>Renamed from {@code PaimonProcessFactory} in AMORO-4200 to disambiguate from the optimizing
 * factory {@code org.apache.amoro.formats.paimon.process.PaimonProcessFactory} in the {@code
 * amoro-format-paimon} module. This factory does <b>not</b> declare any {@code supportedFormats} so
 * {@code ProcessFactoryRouter} never picks it up for optimizing routing — it only feeds the
 * action-triggered scheduler.
 */
public class PaimonMaintainProcessFactory implements ProcessFactory {

  private static final Logger LOG = LoggerFactory.getLogger(PaimonMaintainProcessFactory.class);

  public static final String PLUGIN_NAME = "paimon-maintain";

  public static final ConfigOption<Boolean> SYNC_TABLE_META_ENABLED =
      ConfigOptions.key("sync-table-meta.enabled").booleanType().defaultValue(true);

  public static final ConfigOption<Duration> SYNC_TABLE_META_INTERVAL =
      ConfigOptions.key("sync-table-meta.interval")
          .durationType()
          .defaultValue(Duration.ofHours(1));

  public static final ConfigOption<Integer> SYNC_TABLE_META_TRIGGER_PARALLELISM =
      ConfigOptions.key("sync-table-meta.trigger-parallelism").intType().defaultValue(1);

  private final Map<Action, ProcessTriggerStrategy> actions = Maps.newHashMap();

  @Override
  public void availableExecuteEngines(Collection<ExecuteEngine> allAvailableEngines) {
    // No remote engines needed — sync-meta is an in-process periodic job.
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

    return Optional.empty();
  }

  @Override
  public TableProcess recover(TableRuntime tableRuntime, TableProcessStore store)
      throws RecoverProcessFailedException {
    if (PaimonActions.SYNC_TABLE_META.equals(store.getAction())) {
      return new PaimonTableMetaSyncProcess(tableRuntime);
    }
    throw new RecoverProcessFailedException(
        "Unsupported action for PaimonMaintainProcessFactory: " + store.getAction());
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

  private void resetConfiguredState() {
    actions.clear();
  }
}
