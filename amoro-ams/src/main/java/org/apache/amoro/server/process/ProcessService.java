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
import org.apache.amoro.server.table.RuntimeHandlerChain;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private final TableProcessTracker tableProcessTracker = new TableProcessTracker();

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
    this.tableProcessTracker.configure(serviceConfig.toMap());
  }

  public RuntimeHandlerChain getTableHandlerChain() {
    return tableRuntimeHandler;
  }

  public void register(TableRuntime tableRuntime, TableProcess process) {
    synchronized (process) {
      persistTableProcess(process);
      tableProcessTracker.trackTableProcess(tableRuntime.getTableIdentifier(), process);
      executeOrTraceProcess(process);
    }
  }

  public void recover(TableRuntime tableRuntime, TableProcess process) {
    synchronized (process) {
      // TODO: init some status
      tableProcessTracker.trackTableProcess(tableRuntime.getTableIdentifier(), process);
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
    tableProcessTracker.close();
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
            scheduler.recover(tableRuntime, processMeta);
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
            scheduler.retry(process.getTableRuntime(), process);
          } else {
            tableProcessTracker.untrackTableProcessInstance(
                process.getTableRuntime().getTableIdentifier());
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
    tableProcessTracker.untrackTableProcessInstance(process.getTableRuntime().getTableIdentifier());

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

  public TableProcessMeta persistTableProcess(TableProcess process) {
    TableProcessMeta processMeta = TableProcessMeta.fromTableProcessStore(process.store());
    doAs(
            TableProcessMapper.class,
            mapper ->
                    mapper.insertProcess(
                            processMeta.getTableId(),
                            processMeta.getProcessId(),
                            processMeta.getStatus(),
                            processMeta.getProcessType(),
                            processMeta.getProcessStage(),
                            processMeta.getExecutionEngine(),
                            processMeta.getCreateTime(),
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
  public TableProcessTracker getTableProcessTracker() {
    return tableProcessTracker;
  }

  public static class TableProcessTracker {
    private final Map<ServerTableIdentifier, TableProcess> activeTableProcess =
        new ConcurrentHashMap<>();
    private Map<String, String> properties;

    private void configure(Map<String, String> properties) {
      this.properties = properties;
    }

    private void close() {
      activeTableProcess.clear();
    }

    private boolean isTableProcessTracked(ServerTableIdentifier serverTableIdentifier) {
      return activeTableProcess.containsKey(serverTableIdentifier);
    }

    @VisibleForTesting
    public TableProcess getTableProcessInstance(ServerTableIdentifier serverTableIdentifier) {
      return activeTableProcess.get(serverTableIdentifier);
    }

    private void trackTableProcess(
        ServerTableIdentifier serverTableIdentifier, TableProcess tableProcess) {
      activeTableProcess.put(serverTableIdentifier, tableProcess);
    }

    @VisibleForTesting
    public TableProcess untrackTableProcessInstance(ServerTableIdentifier serverTableIdentifier) {
      return activeTableProcess.remove(serverTableIdentifier);
    }

    @VisibleForTesting
    public boolean isEmpty() {
      return activeTableProcess.isEmpty();
    }
  }

  private class ProcessRuntimeHandler extends RuntimeHandlerChain {

    @Override
    protected boolean formatSupported(TableFormat format) {
      return true;
    }

    @Override
    protected void handleStatusChanged(TableRuntime tableRuntime, OptimizingStatus originalStatus) {
      actionCoordinators.values().forEach(s -> s.handleStatusChanged(tableRuntime, originalStatus));
    }

    @Override
    protected void handleConfigChanged(
        TableRuntime tableRuntime, TableConfiguration originalConfig) {
      actionCoordinators.values().forEach(s -> s.handleConfigChanged(tableRuntime, originalConfig));
    }

    @Override
    protected void handleTableAdded(AmoroTable<?> table, TableRuntime tableRuntime) {
      actionCoordinators.values().forEach(s -> s.handleTableAdded(table, tableRuntime));
    }

    @Override
    protected void handleTableRemoved(TableRuntime tableRuntime) {
      actionCoordinators.values().forEach(s -> s.handleTableRemoved(tableRuntime));
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
