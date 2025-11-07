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
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessState;
import org.apache.amoro.server.manager.AbstractPluginManager;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.process.executor.EngineType;
import org.apache.amoro.server.process.executor.ExecuteEngine;
import org.apache.amoro.server.process.executor.TableProcessExecutor;
import org.apache.amoro.server.table.RuntimeHandlerChain;
import org.apache.amoro.server.table.TableService;
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
  private static final int PROCESS_MAX_RETRY_NUMBER = 3;
  private final TableService tableService;

  private final Map<String, ActionCoordinatorScheduler> actionCoordinators =
      new ConcurrentHashMap<>();
  private final Map<EngineType, ExecuteEngine> executeEngines = new ConcurrentHashMap<>();

  private final ActionCoordinatorManager actionCoordinatorManager = new ActionCoordinatorManager();
  private final ExecuteEngineManager executeEngineManager = new ExecuteEngineManager();
  private final ProcessRuntimeHandler tableRuntimeHandler = new ProcessRuntimeHandler();
  private final ThreadPoolExecutor processExecutionPool =
      new ThreadPoolExecutor(10, 100, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

  public ProcessService(Configurations serviceConfig, TableService tableService) {
    this.tableService = tableService;
  }

  public RuntimeHandlerChain getTableHandlerChain() {
    return tableRuntimeHandler;
  }

  public void register(TableProcess<TableProcessState> process) {
    TableProcessMeta processMeta = persistentProcess(process);
    executeOrTraceProcess(process, processMeta);
  }

  public void dispose() {
    // TODO: dispose
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
    actionCoordinators.values().forEach(s -> s.initialize(tableRuntimes));
    recoverProcesses(tableRuntimes);
  }

  private void executeOrTraceProcess(
      TableProcess<TableProcessState> process, TableProcessMeta processMeta) {
    ExecuteEngine executeEngine =
        executeEngines.get(EngineType.valueOf(processMeta.getExecutionEngine()));
    TableProcessExecutor executor = new TableProcessExecutor(process, executeEngine);
    executor.onProcessFinished(
        () -> {
          if (process.getStatus() == ProcessStatus.FAILED
              && process.getState().getRetryNumber() < PROCESS_MAX_RETRY_NUMBER) {
            process.getState().addRetryNumber();
            // TODO persist process
            // rerun process
            executeOrTraceProcess(process, processMeta);
          }
        });

    processExecutionPool.execute(new TableProcessExecutor(process, executeEngine));
    LOG.info(
        "Submit table process {} to engine {}, process id:{}",
        process,
        executeEngine.engineType(),
        process.getId());
  }

  private TableProcessMeta persistentProcess(TableProcess<TableProcessState> process) {
    // TODO: persistent a process
    return new TableProcessMeta();
  }

  private void recoverProcesses(List<TableRuntime> tableRuntimes) {
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
            TableProcess<TableProcessState> process =
                scheduler.getCoordinator().recoverTableProcess(tableRuntime, processMeta);
            executeOrTraceProcess(process, processMeta);
          }
        });
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
      actionCoordinators.values().forEach(RuntimeHandlerChain::dispose);
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
