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
import org.apache.amoro.AmoroTable;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.ConfigOption;
import org.apache.amoro.config.ConfigOptions;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.formats.paimon.PaimonTable;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionTask;
import org.apache.amoro.formats.paimon.optimizing.commit.PaimonTableCommit;
import org.apache.amoro.formats.paimon.optimizing.plan.PaimonOptimizingPlanner;
import org.apache.amoro.optimizing.OptimizationContext;
import org.apache.amoro.optimizing.TableOptimizingCommitter;
import org.apache.amoro.optimizing.TableOptimizingPlanner;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.process.RecoverProcessFailedException;
import org.apache.amoro.process.StagedTaskDescriptor;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.utils.SnowflakeIdGenerator;
import org.apache.paimon.table.AppendOnlyFileStoreTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * {@link ProcessFactory} for Paimon BUCKET_UNAWARE (AppendOnly) compaction. Discovered via
 * ServiceLoader through {@code META-INF/services/org.apache.amoro.process.ProcessFactory} in this
 * module.
 *
 * <p>Feature-flagged: {@link #OPTIMIZER_ENABLED} defaults to {@code false}. When disabled, {@link
 * #supportedFormats()} returns an empty set so {@code OptimizingQueue} cannot route any Paimon
 * table into this factory — a clean kill-switch for grey-scale rollout.
 */
public class PaimonProcessFactory implements ProcessFactory {

  private static final Logger LOG = LoggerFactory.getLogger(PaimonProcessFactory.class);

  public static final String PLUGIN_NAME = "paimon";

  public static final ConfigOption<Boolean> OPTIMIZER_ENABLED =
      ConfigOptions.key("paimon-optimizer.enabled")
          .booleanType()
          .defaultValue(false)
          .withDescription(
              "Enable Paimon optimizing (BUCKET_UNAWARE small-file compaction). Default off.");

  private boolean enabled;

  @Override
  public String name() {
    return PLUGIN_NAME;
  }

  @Override
  public void open(Map<String, String> properties) {
    if (properties == null || properties.isEmpty()) {
      this.enabled = OPTIMIZER_ENABLED.defaultValue();
    } else {
      this.enabled = Configurations.fromMap(properties).getBoolean(OPTIMIZER_ENABLED);
    }
    LOG.info("PaimonProcessFactory initialised: paimon-optimizer.enabled={}", enabled);
  }

  @Override
  public void close() {}

  @Override
  public Set<TableFormat> supportedFormats() {
    return enabled ? Collections.singleton(TableFormat.PAIMON) : Collections.emptySet();
  }

  @Override
  public Map<TableFormat, Set<Action>> supportedActions() {
    if (!enabled) {
      return Collections.emptyMap();
    }
    Map<TableFormat, Set<Action>> map = new HashMap<>();
    map.put(TableFormat.PAIMON, Collections.emptySet());
    return map;
  }

  @Override
  public TableOptimizingPlanner createPlanner(
      TableRuntime tableRuntime,
      AmoroTable<?> table,
      double availableCore,
      long maxInputSizePerThread) {
    if (!(table instanceof PaimonTable)) {
      throw new IllegalStateException(
          "PaimonProcessFactory.createPlanner requires PaimonTable, got "
              + (table == null ? "null" : table.getClass().getName()));
    }
    PaimonTable paimonTable = (PaimonTable) table;
    long tableId = extractTableId(tableRuntime);
    // processId: uniquely identifies this plan attempt; used as Paimon commit identifier.
    long processId = generateProcessId();
    OptimizingConfig optimizingConfig = optimizingConfig(tableRuntime);
    long lastMinor = 0L;
    long lastMajor = 0L;
    long lastFull = 0L;
    if (tableRuntime instanceof OptimizationContext) {
      OptimizationContext context = (OptimizationContext) tableRuntime;
      lastMinor = context.getLastMinorOptimizingTime();
      lastMajor = context.getLastMajorOptimizingTime();
      lastFull = context.getLastFullOptimizingTime();
    }
    return new PaimonOptimizingPlanner(
        paimonTable,
        tableId,
        processId,
        availableCore,
        maxInputSizePerThread,
        optimizingConfig,
        lastMinor,
        lastMajor,
        lastFull,
        null);
  }

  @Override
  @SuppressWarnings("unchecked")
  public TableOptimizingCommitter createCommitter(
      AmoroTable<?> table,
      long targetSnapshotId,
      long targetChangeSnapshotId,
      Collection<? extends StagedTaskDescriptor<?, ?, ?>> successTasks,
      Map<String, Long> fromSequence,
      Map<String, Long> toSequence) {
    if (!(table instanceof PaimonTable)) {
      throw new IllegalStateException(
          "PaimonProcessFactory.createCommitter requires PaimonTable, got "
              + (table == null ? "null" : table.getClass().getName()));
    }
    PaimonTable paimonTable = (PaimonTable) table;
    Object raw = paimonTable.originalTable();
    if (!(raw instanceof AppendOnlyFileStoreTable)) {
      throw new IllegalStateException(
          "PaimonProcessFactory.createCommitter requires AppendOnlyFileStoreTable, got "
              + (raw == null ? "null" : raw.getClass().getName()));
    }
    AppendOnlyFileStoreTable fileStoreTable = (AppendOnlyFileStoreTable) raw;
    Collection<PaimonCompactionTask> paimonTasks =
        new ArrayList<>((Collection<PaimonCompactionTask>) successTasks);

    String commitUser =
        paimonTasks.stream()
            .filter(t -> t.getInput() != null && t.getInput().getCommitUser() != null)
            .map(t -> t.getInput().getCommitUser())
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Cannot create PaimonTableCommit: no commitUser found on any success task."));

    // targetSnapshotId is the Paimon commit identifier — Paimon requires a strictly monotonic
    // value per commitUser so that FileStoreCommitImpl.filterCommitted() can dedupe replays.
    // The planner's target snapshot id is already monotonic across plans.
    return new PaimonTableCommit(
        fileStoreTable, paimonTasks, commitUser, /* commitIdentifier= */ targetSnapshotId);
  }

  @Override
  public Optional<TableProcess> trigger(TableRuntime tableRuntime, Action action) {
    // Paimon optimizing is driven entirely by METADATA_TRIGGER (OptimizingQueue), not by action
    // fire-and-forget scheduling.
    return Optional.empty();
  }

  /**
   * Paimon optimizing does not use the generic {@link ProcessFactory#recover} entry point. AMS
   * re-hydrates in-flight work via {@code TableOptimizingProcess(runtime, meta, state)} directly
   * from persistent storage (see {@code DefaultOptimizingService}); any un-completed plan is simply
   * re-planned on the next scheduler tick. This method is therefore dead code in the happy path and
   * is kept only to satisfy the interface contract. If AMS ever routes a Paimon action through the
   * generic {@code recover} path the failure is fatal and we want a loud exception rather than a
   * silent no-op that masks a routing bug.
   */
  @Override
  public TableProcess recover(TableRuntime tableRuntime, TableProcessStore store)
      throws RecoverProcessFailedException {
    throw new RecoverProcessFailedException(
        "Recovery is not supported by PaimonProcessFactory; rely on re-planning on next tick.");
  }

  private static long extractTableId(TableRuntime runtime) {
    ServerTableIdentifier id = runtime == null ? null : runtime.getTableIdentifier();
    return id == null || id.getId() == null ? 0L : id.getId();
  }

  private static OptimizingConfig optimizingConfig(TableRuntime runtime) {
    TableConfiguration tableConfiguration =
        runtime == null ? null : runtime.getTableConfiguration();
    OptimizingConfig config =
        tableConfiguration == null ? null : tableConfiguration.getOptimizingConfig();
    if (config == null && runtime instanceof OptimizationContext) {
      return ((OptimizationContext) runtime).getOptimizingConfig();
    }
    return config;
  }

  // --- processId generator ---------------------------------------------------
  // Uses the shared SnowflakeIdGenerator from amoro-common so all formats produce
  // IDs with the same bit layout (54-bit JS-safe, 40-bit timestamp/10ms + 5-bit
  // machineId + 8-bit sequence). This ensures extractTimestamp() works uniformly.
  private static final SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator();

  static long generateProcessId() {
    return snowflakeIdGenerator.generateId();
  }
}
