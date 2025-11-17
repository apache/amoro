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

package org.apache.amoro.server.process;

import org.apache.amoro.Action;
import org.apache.amoro.AmoroTable;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.server.manager.AbstractPluginManager;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.process.executor.EngineType;
import org.apache.amoro.server.process.executor.ExecuteEngine;
import org.apache.amoro.server.process.executor.TableProcessExecutor;
import org.apache.amoro.server.table.AbstractTableRuntime;
import org.apache.amoro.server.table.RuntimeHandlerChain;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ProcessService extends PersistentBase {
  private static final Logger LOG = LoggerFactory.getLogger(ProcessService.class);
  private final TableService tableService;

  private final Map<String, ActionCoordinatorScheduler> actionCoordinators =
      new ConcurrentHashMap<>();
  private final Map<EngineType, ExecuteEngine> executeEngines = new ConcurrentHashMap<>();

  private final ActionCoordinatorManager actionCoordinatorManager;
  private final ExecuteEngineManager executeEngineManager;
  private final ProcessRuntimeHandler tableRuntimeHandler = new ProcessRuntimeHandler();
  private final ThreadPoolExecutor processExecutionPool =
      new ThreadPoolExecutor(10, 100, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

  private final Map<ServerTableIdentifier, TableRuntime> activeTableRuntimes =
      new ConcurrentHashMap<>();

  private final Map<ServerTableIdentifier, Map<Long, TableProcess>> activeTableProcess =
      new ConcurrentHashMap<>();

  public ProcessService(Configurations serviceConfig, TableService tableService) {
    this(serviceConfig, tableService, new ActionCoordinatorManager(), new ExecuteEngineManager());
  }

  public ProcessService(
      Configurations serviceConfig,
      TableService tableService,
      ActionCoordinatorManager actionCoordinatorManager,
      ExecuteEngineManager executeEngineManager) {
    this.tableService = tableService;
    this.actionCoordinatorManager = actionCoordinatorManager;
    this.executeEngineManager = executeEngineManager;
  }

  public RuntimeHandlerChain getTableHandlerChain() {
    return tableRuntimeHandler;
  }

  public void register(TableRuntime tableRuntime, TableProcess process) {
    synchronized (process) {
      if (hasAliveTableProcess(tableRuntime, process.getAction())) {
        LOG.warn(
            "Detect alive table process exists for table runtime: {}, skip schedule {} action this time.",
            tableRuntime.getTableIdentifier(),
            process.getAction());
        return;
      }
      persistTableProcess(process);
      trackTableProcess(tableRuntime.getTableIdentifier(), process);
      executeOrTraceProcess(process);
    }
  }

  public void recover(TableRuntime tableRuntime, TableProcess process) {
    synchronized (process) {
      // TODO: init some status
      trackTableRuntime(tableRuntime.getTableIdentifier(), tableRuntime);
      trackTableProcess(tableRuntime.getTableIdentifier(), process);
      executeOrTraceProcess(process);
    }
  }

  public void retry(TableProcess process) {
    synchronized (process) {
      // TODO: init some status
      executeOrTraceProcess(process);
    }
  }

  public void cancel(TableProcess process) {
    synchronized (process) {
      // TODO: init some status
      cancelProcess(process);
    }
  }

  public void dispose() {
    actionCoordinatorManager.close();
    executeEngineManager.close();
    processExecutionPool.shutdown();
    activeTableRuntimes.clear();
    activeTableProcess.clear();
  }

  private void initialize(List<TableRuntime> tableRuntimes) {
    LOG.info("Initializing process service");
    actionCoordinatorManager.initialize();
    actionCoordinatorManager
        .installedPlugins()
        .forEach(
            actionCoordinator -> {
              actionCoordinators.put(
                  actionCoordinator.action().getName(),
                  new ActionCoordinatorScheduler(
                      actionCoordinator, tableService, ProcessService.this));
            });
    executeEngineManager.initialize();
    executeEngineManager
        .installedPlugins()
        .forEach(
            executeEngine -> {
              executeEngines.put(executeEngine.engineType(), executeEngine);
            });
    recoverProcesses(tableRuntimes);
    actionCoordinators.values().forEach(s -> s.initialize(tableRuntimes));
  }

  @VisibleForTesting
  public void recoverProcesses(List<TableRuntime> tableRuntimes) {
    Map<Long, TableRuntime> tableIdToRuntimes =
        tableRuntimes.stream()
            .collect(Collectors.toMap(t -> t.getTableIdentifier().getId(), t -> t));
    List<TableProcessMeta> activeProcesses =
        getAs(TableProcessMapper.class, TableProcessMapper::selectAllActiveProcesses);
    activeProcesses.forEach(
        processMeta -> {
          TableRuntime tableRuntime = tableIdToRuntimes.get(processMeta.getTableId());
          ActionCoordinatorScheduler scheduler =
              actionCoordinators.get(processMeta.getProcessType());
          if (tableRuntime != null && scheduler != null) {
            scheduler.recover(
                tableRuntime,
                new DefaultTableProcessStore(
                    processMeta.getProcessId(), tableRuntime, processMeta, scheduler.getAction()));
          }
        });
  }

  private void executeOrTraceProcess(TableProcess process) {

    if (!isExecutable(process)) {
      LOG.info(
          "Table process {} with identifier {} may have been in canceling or canceled, cancel execute process.",
          process.getId(),
          process.getExternalProcessIdentifier());
      return;
    }

    ExecuteEngine executeEngine =
        executeEngines.get(EngineType.valueOf(process.store().getExecutionEngine()));

    TableProcessExecutor executor = new TableProcessExecutor(process, executeEngine);
    executor.onProcessFinished(
        () -> {
          ActionCoordinatorScheduler scheduler =
              actionCoordinators.get(process.store().getAction().getName());
          if (scheduler != null
              && process.getStatus() == ProcessStatus.FAILED
              && process.store().getRetryNumber() < scheduler.PROCESS_MAX_RETRY_NUMBER
              && process.getTableRuntime() != null) {
            process.updateTableProcessRetryTimes(process.store().getRetryNumber() + 1);
            scheduler.retry(process);
          } else {
            untrackTableProcessInstance(
                process.getTableRuntime().getTableIdentifier(), process.getId());
          }
        });

    processExecutionPool.submit(executor);

    LOG.info(
        "Submit table process {} to engine {}, process id:{}",
        process,
        executeEngine.engineType(),
        process.getId());
  }

  private void cancelProcess(TableProcess process) {

    process.updateTableProcessStatus(ProcessStatus.CANCELING);
    untrackTableProcessInstance(process.getTableRuntime().getTableIdentifier(), process.getId());

    ExecuteEngine executeEngine =
        executeEngines.get(EngineType.valueOf(process.store().getExecutionEngine()));

    executeEngine.tryCancelTableProcess(process, process.getExternalProcessIdentifier());

    process.updateTableProcessStatus(ProcessStatus.CANCELED);

    LOG.info(
        "Cancel table process {} in engine {}, process id:{}",
        process,
        executeEngine.engineType(),
        process.getId());
  }

  private boolean isExecutable(TableProcess process) {
    if (process.getStatus() == ProcessStatus.CANCELING
        || process.getStatus() == ProcessStatus.CANCELED) {
      return false;
    } else {
      return true;
    }
  }

  private boolean hasAliveTableProcess(TableRuntime tableRuntime, Action action) {
    List<TableProcess> processes =
        getTableProcessInstances(tableRuntime.getTableIdentifier()).values().stream()
            .filter(
                tableProcess ->
                    tableProcess.getAction().getName().equalsIgnoreCase(action.getName()))
            .collect(Collectors.toList());
    return processes.stream()
        .anyMatch(
            process -> {
              return (process != null
                  && (process.getStatus() == ProcessStatus.RUNNING
                      || process.getStatus() == ProcessStatus.SUBMITTED
                      || process.getStatus() == ProcessStatus.PENDING));
            });
  }

  public TableProcessMeta persistTableProcess(TableProcess process) {
    TableProcessMeta processMeta = TableProcessMeta.fromTableProcessStore(process.store());
    doAs(
        TableProcessMapper.class,
        mapper ->
            mapper.insertProcess(
                processMeta.getTableId(),
                processMeta.getProcessId(),
                processMeta.getExternalProcessIdentifier(),
                processMeta.getStatus(),
                processMeta.getProcessType(),
                processMeta.getProcessStage(),
                processMeta.getExecutionEngine(),
                processMeta.getRetryNumber(),
                processMeta.getCreateTime(),
                processMeta.getProcessParameters(),
                processMeta.getSummary()));
    return processMeta;
  }

  @VisibleForTesting
  public Map<String, ActionCoordinatorScheduler> getActionCoordinators() {
    return actionCoordinators;
  }

  @VisibleForTesting
  public Map<EngineType, ExecuteEngine> getExecuteEngines() {
    return executeEngines;
  }

  @VisibleForTesting
  public Map<ServerTableIdentifier, TableRuntime> getActiveTableRuntimes() {
    return activeTableRuntimes;
  }

  private void trackTableRuntime(
      ServerTableIdentifier serverTableIdentifier, TableRuntime tableRuntime) {
    TableRuntime activeTableRuntime =
        activeTableRuntimes.computeIfAbsent(serverTableIdentifier, key -> tableRuntime);
    ((AbstractTableRuntime) activeTableRuntime)
        .store()
        .getTableConfig()
        .putAll(((AbstractTableRuntime) tableRuntime).store().getTableConfig());
  }

  @VisibleForTesting
  public TableRuntime untrackTableRuntime(ServerTableIdentifier serverTableIdentifier) {
    return activeTableRuntimes.remove(serverTableIdentifier);
  }

  @VisibleForTesting
  public Map<ServerTableIdentifier, Map<Long, TableProcess>> getActiveTableProcess() {
    return activeTableProcess;
  }

  @VisibleForTesting
  public TableProcess getTableProcessInstance(
      ServerTableIdentifier serverTableIdentifier, long processId) {
    Map<Long, TableProcess> inner = activeTableProcess.get(serverTableIdentifier);
    return inner != null ? inner.get(processId) : null;
  }

  @VisibleForTesting
  public Map<Long, TableProcess> getTableProcessInstances(
      ServerTableIdentifier serverTableIdentifier) {
    Map<Long, TableProcess> inner = activeTableProcess.get(serverTableIdentifier);
    if (inner == null || inner.isEmpty()) {
      return Collections.emptyMap();
    }
    return Collections.unmodifiableMap(inner);
  }

  private void trackTableProcess(
      ServerTableIdentifier serverTableIdentifier, TableProcess tableProcess) {
    activeTableProcess
        .computeIfAbsent(serverTableIdentifier, key -> new ConcurrentHashMap<>())
        .put(tableProcess.getId(), tableProcess);
  }

  @VisibleForTesting
  public TableProcess untrackTableProcessInstance(
      ServerTableIdentifier serverTableIdentifier, long processId) {
    Map<Long, TableProcess> inner = activeTableProcess.get(serverTableIdentifier);
    if (inner == null) {
      return null;
    }
    TableProcess removed = inner.remove(processId);
    if (inner.isEmpty()) {
      activeTableProcess.remove(serverTableIdentifier, inner);
    }
    return removed;
  }

  private class ProcessRuntimeHandler extends RuntimeHandlerChain {

    @Override
    protected boolean formatSupported(TableFormat format) {
      return true;
    }

    @Override
    protected void handleStatusChanged(TableRuntime tableRuntime, OptimizingStatus originalStatus) {
      actionCoordinators.values().forEach(s -> s.handleStatusChanged(tableRuntime, originalStatus));
      trackTableRuntime(tableRuntime.getTableIdentifier(), tableRuntime);
    }

    @Override
    protected void handleConfigChanged(
        TableRuntime tableRuntime, TableConfiguration originalConfig) {
      actionCoordinators.values().forEach(s -> s.handleConfigChanged(tableRuntime, originalConfig));
      trackTableRuntime(tableRuntime.getTableIdentifier(), tableRuntime);
    }

    @Override
    protected void handleTableAdded(AmoroTable<?> table, TableRuntime tableRuntime) {
      actionCoordinators.values().forEach(s -> s.handleTableAdded(table, tableRuntime));
      trackTableRuntime(tableRuntime.getTableIdentifier(), tableRuntime);
    }

    @Override
    protected void handleTableRemoved(TableRuntime tableRuntime) {
      actionCoordinators.values().forEach(s -> s.handleTableRemoved(tableRuntime));
      untrackTableRuntime(tableRuntime.getTableIdentifier());
      List<TableProcess> processes =
          getTableProcessInstances(tableRuntime.getTableIdentifier()).values().stream()
              .collect(Collectors.toList());
      for (TableProcess process : processes) {
        if (process != null) {
          cancel(process);
        }
      }
    }

    @Override
    protected void initHandler(List<TableRuntime> tableRuntimeList) {
      ProcessService.this.initialize(tableRuntimeList);
    }

    @Override
    protected void doDispose() {
      // TODO: dispose
    }
  }

  public static class ActionCoordinatorManager extends AbstractPluginManager<ActionCoordinator> {
    public ActionCoordinatorManager() {
      super("action-coordinators");
    }
  }

  public static class ExecuteEngineManager extends AbstractPluginManager<ExecuteEngine> {
    public ExecuteEngineManager() {
      super("execute-engines");
    }
  }
}
