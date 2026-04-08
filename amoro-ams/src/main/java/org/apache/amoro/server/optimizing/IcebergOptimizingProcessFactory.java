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

package org.apache.amoro.server.optimizing;

import org.apache.amoro.Action;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.optimizing.RewriteStageTask;
import org.apache.amoro.optimizing.TableOptimizingCommitter;
import org.apache.amoro.optimizing.TableOptimizingPlanner;
import org.apache.amoro.optimizing.plan.AbstractOptimizingPlanner;
import org.apache.amoro.process.ExecuteEngine;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.process.ProcessTriggerStrategy;
import org.apache.amoro.process.RecoverProcessFailedException;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.persistence.OptimizingProcessState;
import org.apache.amoro.server.process.TableProcessMeta;
import org.apache.amoro.server.resource.QuotaProvider;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.utils.IcebergTableUtil;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.MixedTable;
import org.apache.iceberg.util.StructLikeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ProcessFactory for Iceberg-format optimizing. Creates {@link OptimizingTableProcess} instances
 * and wraps them in {@link OptimizingProcessAdapter} for compatibility with the Process framework.
 */
public class IcebergOptimizingProcessFactory implements ProcessFactory {

  private static final Logger LOG = LoggerFactory.getLogger(IcebergOptimizingProcessFactory.class);

  private double availableCore = 1;
  private long maxInputSizePerThread = Long.MAX_VALUE;
  private CatalogManager catalogManager;
  private QuotaProvider quotaProvider;
  private Map<ServerTableIdentifier, AtomicInteger> optimizingTasksMap;
  private Runnable onProcessCompleted;
  private ExecuteEngine executeEngine;

  public void setAvailableCore(double availableCore) {
    this.availableCore = availableCore;
  }

  public void setMaxInputSizePerThread(long maxInputSizePerThread) {
    this.maxInputSizePerThread = maxInputSizePerThread;
  }

  public void setCatalogManager(CatalogManager catalogManager) {
    this.catalogManager = catalogManager;
  }

  public void setQuotaProvider(QuotaProvider quotaProvider) {
    this.quotaProvider = quotaProvider;
  }

  public void setOptimizingTasksMap(Map<ServerTableIdentifier, AtomicInteger> optimizingTasksMap) {
    this.optimizingTasksMap = optimizingTasksMap;
  }

  public void setOnProcessCompleted(Runnable onProcessCompleted) {
    this.onProcessCompleted = onProcessCompleted;
  }

  @Override
  public void availableExecuteEngines(Collection<ExecuteEngine> allAvailableEngines) {
    for (ExecuteEngine engine : allAvailableEngines) {
      if (engine instanceof OptimizerExecuteEngine) {
        this.executeEngine = engine;
        break;
      }
    }
  }

  @Override
  public String name() {
    return "iceberg-optimizing-process-factory";
  }

  @Override
  public Map<TableFormat, Set<Action>> supportedActions() {
    Set<Action> actions = Sets.newHashSet(OptimizingActionCoordinator.OPTIMIZING_ACTION);
    Map<TableFormat, Set<Action>> result = new HashMap<>();
    result.put(TableFormat.ICEBERG, actions);
    result.put(TableFormat.MIXED_ICEBERG, actions);
    result.put(TableFormat.MIXED_HIVE, actions);
    return result;
  }

  @Override
  public ProcessTriggerStrategy triggerStrategy(TableFormat format, Action action) {
    return ProcessTriggerStrategy.METADATA_TRIGGER;
  }

  @Override
  public Optional<TableProcess> trigger(TableRuntime tableRuntime, Action action) {
    DefaultTableRuntime dtr = (DefaultTableRuntime) tableRuntime;
    dtr.beginPlanning();
    try {
      AbstractOptimizingPlanner planner =
          (AbstractOptimizingPlanner) createPlanner(dtr, loadTable(dtr));
      if (!planner.isNecessary()) {
        dtr.completeEmptyProcess();
        return Optional.empty();
      }

      // Plan completes, create OptimizingTableProcess
      OptimizingTableProcess process =
          new OptimizingTableProcess(
              planner, dtr, catalogManager, quotaProvider, optimizingTasksMap, onProcessCompleted);
      process.setProcessFactory(this);

      LOG.info(
          "Iceberg optimizing process created for table {} with type {}, {} tasks",
          dtr.getTableIdentifier(),
          planner.getOptimizingType(),
          process.getTaskMap().size());

      return Optional.of(new OptimizingProcessAdapter(tableRuntime, executeEngine, process));
    } catch (Throwable t) {
      dtr.planFailed();
      throw t;
    }
  }

  @Override
  public TableProcess recover(TableRuntime tableRuntime, TableProcessStore store)
      throws RecoverProcessFailedException {
    DefaultTableRuntime dtr = (DefaultTableRuntime) tableRuntime;
    try {
      // Load persisted process metadata
      TableProcessMeta processMeta = loadProcessMeta(dtr, store.getProcessId());
      OptimizingProcessState processState = loadProcessState(dtr, store.getProcessId());

      OptimizingTableProcess process =
          new OptimizingTableProcess(
              dtr,
              processMeta,
              processState,
              catalogManager,
              quotaProvider,
              optimizingTasksMap,
              onProcessCompleted);

      LOG.info(
          "Recovered optimizing process {} for table {}",
          process.getProcessId(),
          dtr.getTableIdentifier());

      return new OptimizingProcessAdapter(tableRuntime, executeEngine, process);
    } catch (Exception e) {
      throw new RecoverProcessFailedException(
          "Failed to recover optimizing process for table "
              + tableRuntime.getTableIdentifier()
              + ": "
              + e.getMessage());
    }
  }

  /**
   * Create a committer for the given table and task results. Called by OptimizingTableProcess via
   * factory reference.
   */
  public TableOptimizingCommitter createCommitter(
      MixedTable table,
      long targetSnapshotId,
      Collection<TaskRuntime<RewriteStageTask>> tasks,
      Map<String, Long> fromSequence,
      Map<String, Long> toSequence) {
    if (table.isUnkeyedTable()) {
      return new UnKeyedTableCommit(targetSnapshotId, table, tasks);
    } else {
      StructLikeMap<Long> fromSeqMap = convertToStructLikeMap(table, fromSequence);
      StructLikeMap<Long> toSeqMap = convertToStructLikeMap(table, toSequence);
      Long fromSnapshotId = null;
      return new KeyedTableCommit(table, tasks, fromSnapshotId, fromSeqMap, toSeqMap);
    }
  }

  private TableOptimizingPlanner createPlanner(DefaultTableRuntime tableRuntime, MixedTable table) {
    return IcebergTableUtil.createOptimizingPlanner(
        tableRuntime, table, availableCore, maxInputSizePerThread);
  }

  private MixedTable loadTable(DefaultTableRuntime tableRuntime) {
    if (catalogManager == null) {
      throw new IllegalStateException("CatalogManager not set, call setCatalogManager() first");
    }
    return (MixedTable)
        catalogManager.loadTable(tableRuntime.getTableIdentifier().getIdentifier()).originalTable();
  }

  private TableProcessMeta loadProcessMeta(DefaultTableRuntime dtr, long processId) {
    TableProcessMeta meta = new TableProcessMeta();
    meta.setTableId(dtr.getTableIdentifier().getId());
    meta.setProcessId(processId);
    meta.setStatus(org.apache.amoro.process.ProcessStatus.RUNNING);
    meta.setCreateTime(System.currentTimeMillis());
    return meta;
  }

  private OptimizingProcessState loadProcessState(DefaultTableRuntime dtr, long processId) {
    OptimizingProcessState state = new OptimizingProcessState();
    state.setTableId(dtr.getTableIdentifier().getId());
    state.setProcessId(processId);
    return state;
  }

  private StructLikeMap<Long> convertToStructLikeMap(
      MixedTable table, Map<String, Long> sequenceMap) {
    if (sequenceMap == null || sequenceMap.isEmpty()) {
      return StructLikeMap.create(table.spec().partitionType());
    }
    StructLikeMap<Long> result = StructLikeMap.create(table.spec().partitionType());
    return result;
  }

  @Override
  public void open(Map<String, String> properties) {
    // No initialization needed
  }

  @Override
  public void close() {
    // No cleanup needed
  }
}
