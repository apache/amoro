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
import com.netease.arctic.ams.api.process.ProcessState;
import com.netease.arctic.ams.api.process.ProcessStatus;
import com.netease.arctic.ams.api.process.TableState;
import com.netease.arctic.server.persistence.StatedPersistentBase;
import com.netease.arctic.server.persistence.TableRuntimePersistency;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;
import com.netease.arctic.server.persistence.mapper.TableProcessMapper;
import com.netease.arctic.server.process.DefaultOptimizingState;
import org.apache.iceberg.relocated.com.google.common.base.MoreObjects;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class DefaultTableRuntime extends StatedPersistentBase implements TableRuntime {

  private final TableManager tableManager;
  private final ServerTableIdentifier tableIdentifier;
  private final DefaultOptimizingState optimizingState;
  private final Map<Action, SingletonActionRunner<TableState>> arbitraryRunnerMap =
      Collections.synchronizedMap(new HashMap<>());
  private final TableBlockerRuntime blockerRuntime;
  private volatile TableConfiguration tableConfiguration;
  private OptimizingRunner optimizingRunner;

  protected DefaultTableRuntime(
      ServerTableIdentifier tableIdentifier,
      TableManager tableManager,
      Map<String, String> properties) {
    Preconditions.checkNotNull(tableIdentifier, tableManager);
    this.tableManager = tableManager;
    this.tableIdentifier = tableIdentifier;
    this.tableConfiguration = TableConfigurations.parseConfig(properties);
    this.optimizingState = new DefaultOptimizingState(tableIdentifier);
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
    this.optimizingState = new DefaultOptimizingState(tableIdentifier, tableRuntimePersistency);
    this.tableConfiguration = tableRuntimePersistency.getTableConfig();
    this.blockerRuntime = new TableBlockerRuntime(tableIdentifier);
  }

  public void register(ProcessFactory<DefaultOptimizingState> defaultOptimizingFactory) {
    optimizingRunner = new OptimizingRunner(Action.OPTIMIZING, defaultOptimizingFactory);
  }

  public void register(Set<Action> actions, ProcessFactory<TableState> processFactory) {
    actions.forEach(
        action ->
            arbitraryRunnerMap.put(action, new ArbitraryActionRunner(action, processFactory)));
  }

  public AmoroProcess<OptimizingState> runOptimizing() {
    return optimizingRunner.run();
  }

  public AmoroProcess<OptimizingState> runOptimizing(
      ProcessFactory<OptimizingState> processFactory) {
    return optimizingRunner.run(processFactory);
  }

  public AmoroProcess<? extends TableState> runAction(Action action) {
    Preconditions.checkState(action != null && action != Action.OPTIMIZING);
    return arbitraryRunnerMap.get(action).run();
  }

  public AmoroProcess<? extends TableState> runAction(
      Action action, ProcessFactory<TableState> processFactory) {
    Preconditions.checkState(action != null && action != Action.OPTIMIZING);
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

  public TableBlockerRuntime getBlockerRuntime() {
    return blockerRuntime;
  }

  public DefaultOptimizingState getDefaultOptimizingState() {
    return optimizingState;
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

  public void refresh(PendingInput pendingInput, Map<String, String> config) {
    doAsTransaction(
        () -> {
          if (pendingInput != null) {
            optimizingState.savePendingInput(pendingInput);
          }
          TableConfiguration newConfig = TableConfigurations.parseConfig(config);
          if (!tableConfiguration.equals(newConfig)) {
            doAs(
                TableMetaMapper.class,
                mapper ->
                    mapper.updateTableConfiguration(tableIdentifier.getId(), tableConfiguration));
            tableConfiguration = newConfig;
          }
        });
  }

  private void persistTableRuntime() {
    doAs(TableMetaMapper.class, mapper -> mapper.insertTableRuntime(this));
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
    return optimizingState.getId();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("tableIdentifier", tableIdentifier)
        .add("tableConfiguration", tableConfiguration)
        .toString();
  }

  public void setTargetQuota(double targetQuota) {
    optimizingState.setTargetQuota(targetQuota);
  }

  private abstract class SingletonActionRunner<T extends ProcessState> {

    protected final Map<Long, AmoroProcess<T>> externalProcesses = new HashMap<>();
    protected final Lock lock = new ReentrantLock();
    protected final ProcessFactory<T> defaultProcessFactory;
    protected final Action action;
    protected volatile AmoroProcess<T> defaultProcess;
    private volatile long lastTriggerTime;
    private volatile long lastCompletedTime;
    private int retryCount;

    public SingletonActionRunner(Action action, ProcessFactory<T> defaultProcessFactory) {
      this.action = action;
      this.defaultProcessFactory = defaultProcessFactory;
      recover();
    }

    protected abstract void recover();

    public AmoroProcess<T> run() {
      lock.lock();
      try {
        Preconditions.checkState(externalProcesses.isEmpty());
        closeDefaultProcess();
        defaultProcess = defaultProcessFactory.create(DefaultTableRuntime.this, action);
        if (defaultProcess != null) {
          submitProcess(defaultProcess);
        }
        return defaultProcess;
      } finally {
        lock.unlock();
      }
    }

    // TODO persist action table
    public AmoroProcess<T> run(ProcessFactory<T> processFactory) {
      lock.lock();
      try {
        Preconditions.checkState(processFactory != defaultProcessFactory);
        closeDefaultProcess();
        AmoroProcess<T> process = processFactory.create(DefaultTableRuntime.this, action);
        if (process != null) {
          submitProcess(process);
        }
        return process;
      } finally {
        lock.unlock();
      }
    }

    protected void submitProcess(AmoroProcess<T> process) {
      if (process != defaultProcess && externalProcesses.containsKey(process.getId())) {
        throw new IllegalStateException(
            "Process " + process.getId() + " has already been submitted");
      }
      doAsTransaction(
          () -> {
            doAs(
                TableProcessMapper.class,
                mapper ->
                    mapper.insertLiveProcess(
                        tableIdentifier.getId(),
                        action,
                        process.getId(),
                        defaultProcess == process));
            process.whenCompleted(() -> processCompleted(process));
            if (process == defaultProcess) {
              defaultProcess = process;
              lastTriggerTime = process.getStartTime();
            } else {
              externalProcesses.put(process.getId(), process);
            }
            process.submit();
          });
    }

    private void processCompleted(AmoroProcess<T> process) {
      if (process.getStatus() == ProcessStatus.SUCCESS || defaultProcess != process) {
        doAs(
            TableProcessMapper.class,
            mapper -> mapper.deleteLiveProcess(tableIdentifier.getId(), action, process.getId()));
        if (defaultProcess == process && process.getStatus() == ProcessStatus.SUCCESS) {
          lastCompletedTime = System.currentTimeMillis();
          retryCount = 0;
          defaultProcess = null;
        } else {
          externalProcesses.remove(process.getId());
        }
      } else {
        doAs(
            TableProcessMapper.class,
            mapper ->
                mapper.updateProcessAction(
                    tableIdentifier.getId(), action, process.getId(), retryCount + 1));
        retryCount++;
      }
    }

    public long getLastTriggerTime() {
      return lastTriggerTime;
    }

    public long getLastCompletedTime() {
      return lastCompletedTime;
    }

    public int getRetryCount() {
      return retryCount;
    }

    public AmoroProcess<T> getDefaultProcess() {
      return defaultProcess;
    }

    public void closeDefaultProcess() {
      AmoroProcess<T> defaultProcess = this.defaultProcess;
      if (defaultProcess != null) {
        /** this operation should trigger defaultProcess = null in whenCompleted callback */
        defaultProcess.close();
      }
    }

    public void close() {
      lock.lock();
      try {
        closeDefaultProcess();
        externalProcesses.values().forEach(AmoroProcess::close);
      } finally {
        lock.unlock();
      }
    }

    public void close(long processId) {
      lock.lock();
      try {
        if (defaultProcess != null && defaultProcess.getId() == processId) {
          closeDefaultProcess();
        } else {
          Optional.ofNullable(externalProcesses.remove(processId)).ifPresent(AmoroProcess::close);
        }
      } finally {
        lock.unlock();
      }
    }

    public List<T> getStates() {
      AmoroProcess<T> process = this.defaultProcess;
      if (process != null) {
        return Lists.newArrayList(process.getState());
      } else {
        return getExternalStates();
      }
    }

    private List<T> getExternalStates() {
      lock.lock();
      try {
        return externalProcesses.values().stream()
            .map(AmoroProcess::getState)
            .collect(Collectors.toList());
      } finally {
        lock.unlock();
      }
    }
  }

  private class ArbitraryActionRunner extends SingletonActionRunner<TableState> {

    public ArbitraryActionRunner(Action action, ProcessFactory<TableState> processFactory) {
      super(action, processFactory);
    }

    /** recover nothing for right now. */
    protected void recover() {}
  }

  private class OptimizingRunner extends SingletonActionRunner<OptimizingState> {

    @SuppressWarnings("unchecked")
    public OptimizingRunner(
        Action action, ProcessFactory<? extends OptimizingState> defaultProcessFactory) {
      super(action, (ProcessFactory<OptimizingState>) defaultProcessFactory);
    }

    @Override
    protected void recover() {
      Preconditions.checkState(defaultProcess == null);
      defaultProcess =
          defaultProcessFactory.recover(DefaultTableRuntime.this, action, optimizingState);
      if (defaultProcess != null) {
        submitProcess(defaultProcess);
      }
    }
  }
}
