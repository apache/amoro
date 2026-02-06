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
import org.apache.amoro.process.ProcessEvent;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.server.manager.AbstractPluginManager;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.process.executor.EngineType;
import org.apache.amoro.server.process.executor.ExecuteEngine;
import org.apache.amoro.server.process.executor.TableProcessExecutor;
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

/**
 * Service to coordinate table processes: registering, recovering, retrying and canceling. It
 * manages installed {@link ActionCoordinator}s and {@link ExecuteEngine}s and tracks active
 * processes.
 */
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

  private final Map<ServerTableIdentifier, Map<Long, TableProcess>> activeTableProcess =
      new ConcurrentHashMap<>();

  public ProcessService(Configurations serviceConfig, TableService tableService) {
    this(serviceConfig, tableService, new ActionCoordinatorManager(), new ExecuteEngineManager());
  }

  public ProcessService(
      org.apache.amoro.config.DynamicConfigurations dynamicConfigurations,
      TableService tableService) {
    this(
        dynamicConfigurations,
        tableService,
        new ActionCoordinatorManager(),
        new ExecuteEngineManager());
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

  /**
   * Get the runtime handler chain for process-related table events.
   *
   * @return handler chain
   */
  public RuntimeHandlerChain getTableHandlerChain() {
    return tableRuntimeHandler;
  }

  /**
   * Register and submit a table process for a given table runtime.
   *
   * @param tableRuntime table runtime
   * @param process table process
   */
  public void register(TableRuntime tableRuntime, TableProcess process) {
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

  /**
   * Recover a table process for a given table runtime.
   *
   * @param tableRuntime table runtime
   * @param process table process to recover
   */
  public void recover(TableRuntime tableRuntime, TableProcess process) {
    // TODO: init some status
    trackTableProcess(tableRuntime.getTableIdentifier(), process);
    executeOrTraceProcess(process);
  }

  /**
   * Retry a failed table process.
   *
   * @param process process to retry
   */
  public void retry(TableProcess process) {
    executeOrTraceProcess(process);
  }

  /**
   * Cancel a table process and release related resources.
   *
   * @param process process to cancel
   */
  public void cancel(TableProcess process) {
    // TODO: init some status
    cancelProcess(process);
  }

  /** Dispose the service, shutdown engines and clear active processes. */
  public void dispose() {
    actionCoordinatorManager.close();
    executeEngineManager.close();
    processExecutionPool.shutdown();
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

  /**
   * Recover active processes from persistence for given table runtimes.
   *
   * @param tableRuntimes runtimes to recover
   */
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
                    processMeta.getProcessId(),
                    tableRuntime,
                    processMeta,
                    scheduler.getAction(),
                    scheduler.PROCESS_MAX_RETRY_NUMBER));
          }
        });
  }

  /**
   * Execute the process or trace it if not executable.
   *
   * @param process table process
   */
  private void executeOrTraceProcess(TableProcess process) {

    if (!isExecutable(process)) {
      LOG.info(
          "Table process {} with identifier {} may have been in canceling or canceled, cancel execute process.",
          process.getId(),
          process.getExternalProcessIdentifier());
      return;
    }

    ExecuteEngine executeEngine =
        executeEngines.get(EngineType.of(process.store().getExecutionEngine()));

    TableProcessExecutor executor = new TableProcessExecutor(process, executeEngine);
    executor.onProcessFinished(
        () -> {
          ActionCoordinatorScheduler scheduler =
              actionCoordinators.get(process.store().getAction().getName());
          if (scheduler != null
              && process.getStatus() == ProcessStatus.FAILED
              && process.store().getRetryNumber() < scheduler.PROCESS_MAX_RETRY_NUMBER
              && process.getTableRuntime() != null) {
            process
                .store()
                .tryTransitState(
                    ProcessStatus.PENDING,
                    ProcessEvent.RETRY_REQUESTED,
                    process.getExternalProcessIdentifier(),
                    "Regular Retry.",
                    process.getProcessParameters(),
                    process.getSummary());
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

  /**
   * Cancel the given process and untrack it.
   *
   * @param process table process
   */
  private void cancelProcess(TableProcess process) {

    process
        .store()
        .tryTransitState(
            ProcessStatus.CANCELED,
            ProcessEvent.CANCEL_REQUESTED,
            process.getExternalProcessIdentifier(),
            "Gracefully Canceled.",
            process.getProcessParameters(),
            process.getSummary());
    untrackTableProcessInstance(process.getTableRuntime().getTableIdentifier(), process.getId());

    ExecuteEngine executeEngine =
        executeEngines.get(EngineType.of(process.store().getExecutionEngine()));

    executeEngine.tryCancelTableProcess(process, process.getExternalProcessIdentifier());

    LOG.info(
        "Cancel table process {} in engine {}, process id:{}",
        process,
        executeEngine.engineType(),
        process.getId());
  }

  /**
   * Whether the process can be executed.
   *
   * @param process table process
   * @return true if executable
   */
  private boolean isExecutable(TableProcess process) {
    if (process.getStatus() == ProcessStatus.CANCELING
        || process.getStatus() == ProcessStatus.CANCELED) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Check whether there is any alive process for the table runtime and action.
   *
   * @param tableRuntime table runtime
   * @param action action type
   * @return true if exists
   */
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

  /**
   * Persist a process to storage and return its metadata snapshot.
   *
   * @param process table process
   * @return metadata snapshot
   */
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

  /**
   * Get installed action coordinators.
   *
   * @return coordinators map
   */
  @VisibleForTesting
  public Map<String, ActionCoordinatorScheduler> getActionCoordinators() {
    return actionCoordinators;
  }

  /**
   * Get installed execute engines.
   *
   * @return engines map
   */
  @VisibleForTesting
  public Map<EngineType, ExecuteEngine> getExecuteEngines() {
    return executeEngines;
  }

  /**
   * Get all active table processes tracked by service.
   *
   * @return active process map
   */
  @VisibleForTesting
  public Map<ServerTableIdentifier, Map<Long, TableProcess>> getActiveTableProcess() {
    return activeTableProcess;
  }

  /**
   * Get a table process instance by table identifier and process id.
   *
   * @param serverTableIdentifier table identifier
   * @param processId process id
   * @return table process instance or null
   */
  @VisibleForTesting
  public TableProcess getTableProcessInstance(
      ServerTableIdentifier serverTableIdentifier, long processId) {
    Map<Long, TableProcess> inner = activeTableProcess.get(serverTableIdentifier);
    return inner != null ? inner.get(processId) : null;
  }

  /**
   * Get all table process instances for the given table identifier.
   *
   * @param serverTableIdentifier table identifier
   * @return unmodifiable map of processes
   */
  @VisibleForTesting
  public Map<Long, TableProcess> getTableProcessInstances(
      ServerTableIdentifier serverTableIdentifier) {
    Map<Long, TableProcess> inner = activeTableProcess.get(serverTableIdentifier);
    if (inner == null || inner.isEmpty()) {
      return Collections.emptyMap();
    }
    return Collections.unmodifiableMap(inner);
  }

  /**
   * Track a table process in active map.
   *
   * @param serverTableIdentifier table identifier
   * @param tableProcess process instance
   */
  private void trackTableProcess(
      ServerTableIdentifier serverTableIdentifier, TableProcess tableProcess) {
    activeTableProcess
        .computeIfAbsent(serverTableIdentifier, key -> new ConcurrentHashMap<>())
        .put(tableProcess.getId(), tableProcess);
  }

  /**
   * Untrack a process instance and return the removed entry.
   *
   * @param serverTableIdentifier table identifier
   * @param processId process id
   * @return removed process or null
   */
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

  @VisibleForTesting
  public void installActionCoordinator(ActionCoordinator actionCoordinator) {
    this.actionCoordinators.put(
        actionCoordinator.action().getName(),
        new ActionCoordinatorScheduler(actionCoordinator, tableService, ProcessService.this));
  }

  @VisibleForTesting
  public void unInstallAllActionCoordinators() {
    this.actionCoordinators.clear();
  }

  @VisibleForTesting
  public void installExecuteEngine(ExecuteEngine executeEngine) {
    this.executeEngines.put(executeEngine.engineType(), executeEngine);
  }

  @VisibleForTesting
  public void unInstallAllExecuteEngines() {
    this.executeEngines.clear();
  }

  /** Runtime handler that forwards table events to all installed action coordinators. */
  private class ProcessRuntimeHandler extends RuntimeHandlerChain {

    /**
     * Accept all formats for process handler.
     *
     * @param format table format
     * @return always true
     */
    @Override
    protected boolean formatSupported(TableFormat format) {
      return true;
    }

    /**
     * Handle table status changed.
     *
     * @param tableRuntime table runtime
     * @param originalStatus original optimizing status
     */
    @Override
    protected void handleStatusChanged(TableRuntime tableRuntime, OptimizingStatus originalStatus) {
      actionCoordinators.values().forEach(s -> s.handleStatusChanged(tableRuntime, originalStatus));
    }

    /**
     * Handle table configuration changed.
     *
     * @param tableRuntime table runtime
     * @param originalConfig original configuration
     */
    @Override
    protected void handleConfigChanged(
        TableRuntime tableRuntime, TableConfiguration originalConfig) {
      actionCoordinators.values().forEach(s -> s.handleConfigChanged(tableRuntime, originalConfig));
    }

    /**
     * Handle table added event.
     *
     * @param table table
     * @param tableRuntime table runtime
     */
    @Override
    protected void handleTableAdded(AmoroTable<?> table, TableRuntime tableRuntime) {
      actionCoordinators.values().forEach(s -> s.handleTableAdded(table, tableRuntime));
    }

    /**
     * Handle table removed event: cancel all related processes.
     *
     * @param tableRuntime table runtime
     */
    @Override
    protected void handleTableRemoved(TableRuntime tableRuntime) {
      actionCoordinators.values().forEach(s -> s.handleTableRemoved(tableRuntime));
      List<TableProcess> processes =
          getTableProcessInstances(tableRuntime.getTableIdentifier()).values().stream()
              .collect(Collectors.toList());
      for (TableProcess process : processes) {
        if (process != null) {
          cancel(process);
        }
      }
    }

    /**
     * Initialize handler with all table runtimes.
     *
     * @param tableRuntimeList runtime list
     */
    @Override
    protected void initHandler(List<TableRuntime> tableRuntimeList) {
      ProcessService.this.initialize(tableRuntimeList);
    }

    /** Dispose handler. */
    @Override
    protected void doDispose() {
      // TODO: dispose
    }
  }

  /** Manager for {@link ActionCoordinator} plugins. */
  public static class ActionCoordinatorManager extends AbstractPluginManager<ActionCoordinator> {
    public ActionCoordinatorManager() {
      super("action-coordinators");
    }
  }

  /** Manager for {@link ExecuteEngine} plugins. */
  public static class ExecuteEngineManager extends AbstractPluginManager<ExecuteEngine> {
    public ExecuteEngineManager() {
      super("execute-engines");
    }
  }
}
