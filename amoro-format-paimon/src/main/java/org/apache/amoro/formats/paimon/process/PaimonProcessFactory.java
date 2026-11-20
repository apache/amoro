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

package org.apache.amoro.formats.paimon.process;

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
import org.apache.amoro.table.StateKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Paimon maintenance, scheduled and recovered by the common Process framework. */
public class PaimonProcessFactory implements ProcessFactory {
  private static final Logger LOG = LoggerFactory.getLogger(PaimonProcessFactory.class);
  public static final ConfigOption<Boolean> EXPIRE_ENABLED =
      ConfigOptions.key("expire-snapshots.enabled").booleanType().defaultValue(true);
  public static final ConfigOption<Duration> EXPIRE_INTERVAL =
      ConfigOptions.key("expire-snapshots.interval").durationType().noDefaultValue();
  public static final StateKey<Long> LAST_SUCCESS_TIME =
      StateKey.stateKey("paimon.snapshot-expiration.last-success-time").longType().defaultValue(0L);

  private final Clock clock;

  /** SPI entry point using the system clock. */
  public PaimonProcessFactory() {
    this(Clock.systemUTC());
  }

  /** Allow subclasses to supply a clock without changing scheduling or retention policy. */
  protected PaimonProcessFactory(Clock clock) {
    this.clock = Objects.requireNonNull(clock, "clock");
  }

  private ExecuteEngine localEngine;
  private final Map<Action, ProcessTriggerStrategy> actions = new HashMap<>();

  @Override
  public String name() {
    return "paimon";
  }

  @Override
  public void open(Map<String, String> properties) {
    actions.clear();
    Configurations configs =
        Configurations.fromMap(properties == null ? Collections.emptyMap() : properties);
    if (!configs.getBoolean(EXPIRE_ENABLED)) {
      return;
    }
    if (properties == null || !properties.containsKey(EXPIRE_INTERVAL.key())) {
      LOG.info("Paimon snapshot expiration is inactive: no explicit {}", EXPIRE_INTERVAL.key());
      return;
    }
    Duration interval = configs.getDuration(EXPIRE_INTERVAL);
    if (interval.toMillis() < 1) {
      throw new IllegalArgumentException("Paimon expire-snapshots.interval must be at least 1 ms");
    }
    actions.put(PaimonActions.EXPIRE_SNAPSHOTS, ProcessTriggerStrategy.triggerAtFixRate(interval));
  }

  @Override
  public void close() {
    actions.clear();
  }

  @Override
  public List<StateKey<?>> requiredStates() {
    return Collections.singletonList(LAST_SUCCESS_TIME);
  }

  @Override
  public Map<TableFormat, Set<Action>> supportedActions() {
    return actions.isEmpty()
        ? Collections.emptyMap()
        : Collections.singletonMap(TableFormat.PAIMON, Set.copyOf(actions.keySet()));
  }

  @Override
  public ProcessTriggerStrategy triggerStrategy(TableFormat format, Action action) {
    return format == TableFormat.PAIMON
        ? actions.getOrDefault(action, ProcessTriggerStrategy.METADATA_TRIGGER)
        : ProcessTriggerStrategy.METADATA_TRIGGER;
  }

  @Override
  public void availableExecuteEngines(Collection<ExecuteEngine> engines) {
    localEngine = null;
    for (ExecuteEngine engine : engines) {
      if (engine instanceof LocalExecutionEngine) {
        localEngine = engine;
        break;
      }
    }
  }

  @Override
  public Optional<TableProcess> trigger(TableRuntime runtime, Action action) {
    if (runtime.getFormat() != TableFormat.PAIMON || !actions.containsKey(action)) {
      return Optional.empty();
    }

    if (PaimonActions.EXPIRE_SNAPSHOTS.equals(action)) {
      return triggerExpireSnapshot(runtime);
    }

    return Optional.empty();
  }

  private Optional<TableProcess> triggerExpireSnapshot(TableRuntime runtime) {
    if (localEngine == null) {
      return Optional.empty();
    }
    ProcessTriggerStrategy strategy = actions.get(PaimonActions.EXPIRE_SNAPSHOTS);
    if (clock.millis() - runtime.getState(LAST_SUCCESS_TIME)
        < strategy.getTriggerInterval().toMillis()) {
      return Optional.empty();
    }
    String reason = PaimonSnapshotExpiration.skipReason(PaimonSnapshotExpiration.refresh(runtime));
    if (reason != null) {
      if ("governance-disabled".equals(reason)) {
        LOG.debug(
            "Skip Paimon snapshot expiration for {}: {}", runtime.getTableIdentifier(), reason);
      } else {
        LOG.info(
            "Skip Paimon snapshot expiration for {}: {}", runtime.getTableIdentifier(), reason);
      }
      return Optional.empty();
    }
    return Optional.of(new PaimonSnapshotsExpiringProcess(runtime, localEngine));
  }

  @Override
  public TableProcess recover(TableRuntime runtime, TableProcessStore store)
      throws RecoverProcessFailedException {
    if (localEngine == null
        || runtime.getFormat() != TableFormat.PAIMON
        || !PaimonActions.EXPIRE_SNAPSHOTS.equals(store.getAction())) {
      throw new RecoverProcessFailedException(
          "Cannot recover Paimon snapshot expiration: unsupported action/format or no local engine");
    }
    return new PaimonSnapshotsExpiringProcess(runtime, localEngine);
  }
}
