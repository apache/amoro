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
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.process.ActionCoordinator;
import org.apache.amoro.process.EngineType;
import org.apache.amoro.process.ExecuteEngine;
import org.apache.amoro.process.ProcessEvent;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.server.manager.AbstractPluginManager;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
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

  private final ExecuteEngineManager executeEngineManager;
  private final List<ActionCoordinator> actionCoordinatorList;
  private final ProcessRuntimeHandler tableRuntimeHandler = new ProcessRuntimeHandler();
  private final ThreadPoolExecutor processExecutionPool =
      new ThreadPoolExecutor(10, 100, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

  private final Map<ServerTableIdentifier, Map<Long, TableProcessHolder>> activeTableProcess =
      new ConcurrentHashMap<>();

  public ProcessService(TableService tableService) {
    this(tableService, Collections.emptyList(), new ExecuteEngineManager());
  }

  public ProcessService(
      TableService tableService,
      List<ActionCoordinator> actionCoordinators,
      ExecuteEngineManager executeEngineManager) {
    this.tableService = tableService;
    this.actionCoordinatorList = actionCoordinators;
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
    TableProcessStore store = persistTableProcess(process);
    trackTableProcess(tableRuntime.getTableIdentifier(), store, process);
    executeOrTraceProcess(store, process);
  }

  /** Dispose the service, shutdown engines and clear active processes. */
  public void dispose() {
    executeEngineManager.close();
    processExecutionPool.shutdown();
    activeTableProcess.clear();
  }

  private void initialize(List<TableRuntime> tableRuntimes) {
    LOG.info("Initializing process service");
    // Pre-configured coordinators built from TableRuntimeFactory / ProcessFactory
    for (ActionCoordinator actionCoordinator : actionCoordinatorList) {
      actionCoordinators.put(
          actionCoordinator.action().getName(),
          new ActionCoordinatorScheduler(actionCoordinator, tableService, ProcessService.this));
    }
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
            DefaultTableProcessStore store =
                new DefaultTableProcessStore(
                    processMeta.getProcessId(),
                    tableRuntime,
                    processMeta,
                    scheduler.getAction(),
                    processMeta.getRetryNumber());
            TableProcess process = scheduler.recover(tableRuntime, store);
            trackTableProcess(tableRuntime.getTableIdentifier(), store, process);
            executeOrTraceProcess(store, process);
          }
        });
  }

  /**
   * Execute the process or trace it if not executable.
   *
   * @param process table process
   */
  private void executeOrTraceProcess(TableProcessStore store, TableProcess process) {
    if (!isExecutable(store)) {
      LOG.info(
          "Table process {} with identifier {} may have been in canceling or canceled, cancel execute process.",
          store.getProcessId(),
          store.getExternalProcessIdentifier());
      return;
    }

    ExecuteEngine executeEngine = executeEngines.get(EngineType.of(store.getExecutionEngine()));

    TableProcessExecutor executor = new TableProcessExecutor(process, store, executeEngine);
    executor.onProcessFinished(
        () -> {
          ActionCoordinatorScheduler scheduler =
              actionCoordinators.get(store.getAction().getName());
          if (scheduler != null
              && store.getStatus() == ProcessStatus.FAILED
              && store.getRetryNumber() < ActionCoordinatorScheduler.PROCESS_MAX_RETRY_NUMBER
              && process.getTableRuntime() != null) {
            store.tryTransitState(
                ProcessStatus.PENDING,
                ProcessEvent.RETRY_REQUESTED,
                store.getExternalProcessIdentifier(),
                "Regular Retry.",
                process.getProcessParameters(),
                process.getSummary());
            executeOrTraceProcess(store, process);
          } else {
            untrackTableProcessInstance(
                process.getTableRuntime().getTableIdentifier(), store.getProcessId());
          }
        });

    processExecutionPool.submit(executor);

    LOG.info(
        "Submit table process {} to engine {}, process id:{}",
        process,
        executeEngine.engineType(),
        store.getProcessId());
  }

  /**
   * Cancel the given process and untrack it.
   *
   * @param process table process
   */
  private void cancelProcess(TableProcessStore store, TableProcess process) {

    store.tryTransitState(
        ProcessStatus.CANCELED,
        ProcessEvent.CANCEL_REQUESTED,
        store.getExternalProcessIdentifier(),
        "Gracefully Canceled.",
        process.getProcessParameters(),
        process.getSummary());
    untrackTableProcessInstance(
        process.getTableRuntime().getTableIdentifier(), store.getProcessId());

    ExecuteEngine executeEngine = executeEngines.get(EngineType.of(process.getExecutionEngine()));

    executeEngine.tryCancelTableProcess(process, store.getExternalProcessIdentifier());

    LOG.info(
        "Cancel table process {} in engine {}, process id:{}",
        process,
        executeEngine.engineType(),
        store.getProcessId());
  }

  /**
   * Whether the process can be executed.
   *
   * @param process table process
   * @return true if executable
   */
  private boolean isExecutable(TableProcessStore process) {
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
    List<TableProcessHolder> processes =
        getTableProcessInstances(tableRuntime.getTableIdentifier()).values().stream()
            .filter(
                tableProcess ->
                    tableProcess.store.getAction().getName().equalsIgnoreCase(action.getName()))
            .collect(Collectors.toList());

    return processes.stream()
        .anyMatch(
            process -> {
              return (process != null
                  && (process.store.getStatus() == ProcessStatus.RUNNING
                      || process.store.getStatus() == ProcessStatus.SUBMITTED
                      || process.store.getStatus() == ProcessStatus.PENDING));
            });
  }

  /**
   * Persist a process to storage and return its metadata snapshot.
   *
   * @param process table process
   * @return metadata snapshot
   */
  protected DefaultTableProcessStore persistTableProcess(TableProcess process) {
    TableProcessMeta processMeta = TableProcessMeta.createProcessMeta(process);
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
    return new DefaultTableProcessStore(
        processMeta.getProcessId(),
        process.getTableRuntime(),
        processMeta,
        process.getAction(),
        processMeta.getRetryNumber());
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
  public Map<ServerTableIdentifier, Map<Long, TableProcessHolder>> getActiveTableProcess() {
    return activeTableProcess;
  }

  /**
   * Get all table process instances for the given table identifier.
   *
   * @param serverTableIdentifier table identifier
   * @return unmodifiable map of processes
   */
  @VisibleForTesting
  public Map<Long, TableProcessHolder> getTableProcessInstances(
      ServerTableIdentifier serverTableIdentifier) {
    Map<Long, TableProcessHolder> inner = activeTableProcess.get(serverTableIdentifier);
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
      ServerTableIdentifier serverTableIdentifier,
      TableProcessStore store,
      TableProcess tableProcess) {
    activeTableProcess
        .computeIfAbsent(serverTableIdentifier, key -> new ConcurrentHashMap<>())
        .put(store.getProcessId(), new TableProcessHolder(tableProcess, store));
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
    Map<Long, TableProcessHolder> inner = activeTableProcess.get(serverTableIdentifier);
    if (inner == null) {
      return null;
    }
    TableProcessHolder removed = inner.remove(processId);
    if (inner.isEmpty()) {
      activeTableProcess.remove(serverTableIdentifier, inner);
    }
    return removed.process;
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
      List<TableProcessHolder> processes =
          getTableProcessInstances(tableRuntime.getTableIdentifier()).values().stream()
              .collect(Collectors.toList());
      for (TableProcessHolder holder : processes) {
        if (holder != null) {
          cancelProcess(holder.store, holder.process);
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

  @VisibleForTesting
  public static class TableProcessHolder {
    public final TableProcess process;
    public final TableProcessStore store;

    public TableProcessHolder(TableProcess process, TableProcessStore store) {
      this.process = process;
      this.store = store;
    }
  }

  /** Manager for {@link ExecuteEngine} plugins. */
  public static class ExecuteEngineManager extends AbstractPluginManager<ExecuteEngine> {
    public ExecuteEngineManager() {
      super("execute-engines");
    }
  }
}
