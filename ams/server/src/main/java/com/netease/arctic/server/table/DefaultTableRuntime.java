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

package com.netease.arctic.server.table;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.ams.api.Action;
import com.netease.arctic.ams.api.ServerTableIdentifier;
import com.netease.arctic.ams.api.TableRuntime;
import com.netease.arctic.ams.api.config.TableConfiguration;
import com.netease.arctic.ams.api.process.AmoroProcess;
import com.netease.arctic.ams.api.process.OptimizingState;
import com.netease.arctic.ams.api.process.PendingInput;
import com.netease.arctic.ams.api.process.ProcessFactory;
import com.netease.arctic.ams.api.process.TableState;
import com.netease.arctic.server.persistence.StatedPersistentBase;
import com.netease.arctic.server.persistence.TableRuntimePersistency;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;
import com.netease.arctic.server.process.ArbitraryRunner;
import com.netease.arctic.server.process.DefaultOptimizingState;
import com.netease.arctic.server.process.OptimizingRunner;
import com.netease.arctic.server.process.QuotaProvider;
import com.netease.arctic.server.process.SingletonActionRunner;
import org.apache.iceberg.relocated.com.google.common.base.MoreObjects;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultTableRuntime extends StatedPersistentBase implements TableRuntime {

  private final TableManager tableManager;
  private final ServerTableIdentifier tableIdentifier;
  private final Map<Action, ArbitraryRunner> arbitraryRunnerMap =
      Collections.synchronizedMap(new HashMap<>());
  private final TableBlockerRuntime blockerRuntime;
  private volatile TableConfiguration tableConfiguration;
  private volatile PendingInput pendingInput;
  private OptimizingRunner optimizingRunner;

  protected DefaultTableRuntime(
      ServerTableIdentifier tableIdentifier,
      TableManager tableManager,
      Map<String, String> properties) {
    Preconditions.checkNotNull(tableIdentifier, tableManager);
    this.tableManager = tableManager;
    this.tableIdentifier = tableIdentifier;
    this.tableConfiguration = TableConfigurations.parseConfig(properties);
    this.blockerRuntime = new TableBlockerRuntime(tableIdentifier);
    persistTableRuntime();
  }

  protected DefaultTableRuntime(
      TableRuntimePersistency tableRuntimePersistency, TableManager tableManager) {
    Preconditions.checkNotNull(tableRuntimePersistency, tableManager);
    this.tableManager = tableManager;
    this.tableIdentifier =
        ServerTableIdentifier.of(
            tableRuntimePersistency.getTableId(),
            tableRuntimePersistency.getCatalogName(),
            tableRuntimePersistency.getDbName(),
            tableRuntimePersistency.getTableName(),
            tableRuntimePersistency.getFormat());
    this.tableConfiguration = tableRuntimePersistency.getTableConfig();
    this.blockerRuntime = new TableBlockerRuntime(tableIdentifier);
  }

  public void register(
      ProcessFactory<DefaultOptimizingState> defaultOptimizingFactory, boolean recover) {
    optimizingRunner = new OptimizingRunner(this, defaultOptimizingFactory, recover);
  }

  public void register(
      Set<Action> actions, ProcessFactory<TableState> processFactory, boolean recover) {
    actions.forEach(
        action ->
            arbitraryRunnerMap.put(
                action, new ArbitraryRunner(this, processFactory, action, recover)));
  }

  public AmoroProcess<OptimizingState> runMinorOptimizing() {
    return optimizingRunner.runMinorOptimizing();
  }

  public AmoroProcess<OptimizingState> runMajorOptimizing() {
    return optimizingRunner.runMajorOptimizing();
  }

  public AmoroProcess<? extends TableState> runArbitraryAction(Action action) {
    Preconditions.checkState(Action.isArbitrary(action));
    return arbitraryRunnerMap.get(action).run();
  }

  public AmoroProcess<? extends TableState> runArbitraryAction(
      Action action, ProcessFactory<TableState> processFactory) {
    Preconditions.checkState(Action.isArbitrary(action));
    return arbitraryRunnerMap.get(action).run(processFactory);
  }

  public void closeProcess(long processId) {
    optimizingRunner.close(processId);
    arbitraryRunnerMap.values().forEach(runner -> runner.close(processId));
  }

  public Map<Action, Long> getLastCompletedTimes(Set<Action> actions) {
    return actions.stream()
        .collect(
            Collectors.toMap(
                action -> action, action -> arbitraryRunnerMap.get(action).getLastCompletedTime()));
  }

  public long getLastTriggerTime(Action action) {
    if (Action.isArbitrary(action)) {
      return arbitraryRunnerMap.get(action).getLastTriggerTime();
    } else if (action == Action.MINOR_OPTIMIZING) {
      return optimizingRunner.getLastMinorTriggerTime();
    } else if (action == Action.MAJOR_OPTIMIZING) {
      return optimizingRunner.getLastMajorTriggerTime();
    } else {
      throw new IllegalArgumentException("Unsupported action: " + action);
    }
  }

  public long getLastTriggerOptimizingTime() {
    return Math.max(
        optimizingRunner.getLastMinorTriggerTime(), optimizingRunner.getLastMajorTriggerTime());
  }

  public boolean needMinorOptimizing() {
    return pendingInput.needMajorOptimizing() && optimizingRunner.isMinorAvailable();
  }

  public boolean needMajorOptimizing() {
    return pendingInput.needMajorOptimizing() && optimizingRunner.isMajorAvailable();
  }

  public TableBlockerRuntime getBlockerRuntime() {
    return blockerRuntime;
  }

  public DefaultOptimizingState getMinorOptimizingState() {
    return optimizingRunner.getMinorOptimizingState();
  }

  public DefaultOptimizingState getMajorOptimizingState() {
    return optimizingRunner.getMajorOptimizingState();
  }

  public QuotaProvider getOptimizingQuota() {
    return optimizingRunner.getQuotaProvider();
  }

  public PendingInput getPendingInput() {
    return pendingInput;
  }

  @Override
  public List<OptimizingState> getOptimizingStates() {
    return optimizingRunner.getStates();
  }

  @Override
  public List<TableState> getArbitraryStates() {
    return arbitraryRunnerMap.values().stream()
        .flatMap(runner -> runner.getStates().stream())
        .collect(Collectors.toList());
  }

  public AmoroTable<?> loadTable() {
    return tableManager.loadTable(tableIdentifier);
  }

  public void dispose() {
    arbitraryRunnerMap.values().forEach(SingletonActionRunner::close);
    doAs(TableMetaMapper.class, mapper -> mapper.deleteOptimizingRuntime(tableIdentifier.getId()));
  }

  public void refresh(PendingInput pending, Map<String, String> config) {
    doAsTransaction(
        () -> {
          TableConfiguration newConfig = TableConfigurations.parseConfig(config);
          if (!tableConfiguration.equals(newConfig)) {
            persistTableConfig(newConfig);
            TableConfiguration oldConfig = tableConfiguration;
            tableConfiguration = newConfig;
            tableManager.refresh(this, oldConfig);
            if (pending != null) {
              persistPendingInput(pending);
              pendingInput = pending;
              optimizingRunner.syncPending(pendingInput);
            }
          }
        });
  }

  private void persistTableRuntime() {
    doAs(TableMetaMapper.class, mapper -> mapper.insertTableRuntime(this));
  }

  private void persistPendingInput(PendingInput pending) {
    doAs(
        TableMetaMapper.class,
        mapper -> mapper.updatePendingInput(tableIdentifier.getId(), pending));
  }

  private void persistTableConfig(TableConfiguration tableConfig) {
    doAs(
        TableMetaMapper.class,
        mapper -> mapper.updateTableConfiguration(tableIdentifier.getId(), tableConfig));
  }

  @Override
  public ServerTableIdentifier getTableIdentifier() {
    return tableIdentifier;
  }

  @Override
  public TableConfiguration getTableConfiguration() {
    return tableConfiguration;
  }

  @Override
  public int getMaxExecuteRetryCount() {
    return tableConfiguration.getOptimizingConfig().getMaxExecuteRetryCount();
  }

  public String getOptimizerGroup() {
    return tableConfiguration.getOptimizingConfig().getOptimizerGroup();
  }

  public long getNewestProcessId() {
    return Math.max(
        optimizingRunner.getMinorOptimizingState().getId(),
        optimizingRunner.getMajorOptimizingState().getId());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("tableIdentifier", tableIdentifier)
        .add("tableConfiguration", tableConfiguration)
        .toString();
  }
}
