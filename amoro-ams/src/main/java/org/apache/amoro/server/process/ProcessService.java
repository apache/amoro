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
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.process.coordinator.ActionScheduleCoordinator;
import org.apache.amoro.server.process.service.BaseService;
import org.apache.amoro.server.process.service.ExecutorService;
import org.apache.amoro.server.process.service.MonitorService;
import org.apache.amoro.server.process.service.RetryService;
import org.apache.amoro.server.process.service.SubmitService;
import org.apache.amoro.server.resource.DefaultOptimizerManager;
import org.apache.amoro.server.table.TableService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessService {
  private static final int START_SERVICE_MAX_RETRIES = 16;

  private int serverPort;
  private Configurations serviceConfig;
  private CatalogManager catalogManager;
  private TableService tableService;

  private List<BaseService> services = new ArrayList<>();
  private List<ActionScheduleCoordinator> actionCoordinators = new ArrayList<>();

  private Map<ServerTableIdentifier, TableRuntime> tableRuntimeMap = new HashMap<>();

  public ProcessService(
      Configurations serviceConfig, CatalogManager catalogManager, TableService tableService) {
    this.serviceConfig = serviceConfig;
    this.catalogManager = catalogManager;
    this.tableService = tableService;
  }

  public void init() {
    ActionScheduleCoordinator actionScheduleCoordinator =
        new ActionScheduleCoordinator(
            new DefaultOptimizerManager(serviceConfig, catalogManager),
            null,
            new Action(new TableFormat[] {TableFormat.PAIMON}, 0, "default"),
            tableService,
            -1);
    registerActionCoordinator(actionScheduleCoordinator);

    ExecutorService executorService = new ExecutorService();
    SubmitService submitService = new SubmitService(executorService);
    RetryService retryService = new RetryService();
    MonitorService monitorService = new MonitorService();
    registerService(executorService);
    registerService(submitService);
    registerService(retryService);
    registerService(monitorService);

    restore();
  }

  private void restore() {}

  public void start() {
      for (BaseService service : services) {
          service.startService();
      }
  }

  public void close() {

    for (ActionScheduleCoordinator actionScheduleCoordinator : actionCoordinators) {
      unregisterActionCoordinator(actionScheduleCoordinator);
    }

    for (BaseService service : services) {
      unregisterService(service);
    }

    dispose();
  }

  public void dispose() {}

  private void registerService(BaseService service) {
    service.init();
    services.add(service);
  }

  private void unregisterService(BaseService service) {
    service.stop();
    services.remove(service);
  }

  private void registerActionCoordinator(ActionScheduleCoordinator actionCoordinator) {
    // TODO: Support remove handler chain
    tableService.addHandlerChain(actionCoordinator);
    actionCoordinators.add(actionCoordinator);
  }

  private void unregisterActionCoordinator(ActionScheduleCoordinator actionCoordinator) {
    actionCoordinator.dispose();
    actionCoordinators.add(actionCoordinator);
  }

  public List<TableProcess> listTableProcess() {
    return null;
  }

  public Map<ServerTableIdentifier, TableRuntime> getTableRuntimeMap() {
    return tableRuntimeMap;
  }

  public List<BaseService> getServices() {
    return services;
  }

  public List<ActionScheduleCoordinator> getActionCoordinators() {
    return actionCoordinators;
  }
}
