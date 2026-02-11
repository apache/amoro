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

package org.apache.amoro.server;

import org.apache.amoro.config.Configurations;
import org.apache.amoro.process.ActionCoordinator;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.server.manager.EventsManager;
import org.apache.amoro.server.manager.MetricManager;
import org.apache.amoro.server.process.ProcessService;
import org.apache.amoro.server.process.executor.ExecuteEngineManager;
import org.apache.amoro.server.table.CompatibleTableRuntime;
import org.apache.amoro.server.table.DefaultTableRuntimeFactory;
import org.apache.amoro.server.table.DefaultTableService;
import org.apache.amoro.server.table.TableRuntimeFactoryManager;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AMSServiceTestBase extends AMSManagerTestBase {
  private static DefaultTableService TABLE_SERVICE = null;
  private static DefaultOptimizingService OPTIMIZING_SERVICE = null;
  private static ProcessService PROCESS_SERVICE = null;

  private static TableRuntimeFactoryManager tableRuntimeFactoryManager = null;

  @BeforeClass
  public static void initTableService() {
    DefaultTableRuntimeFactory runtimeFactory = new DefaultTableRuntimeFactory();
    tableRuntimeFactoryManager = Mockito.mock(TableRuntimeFactoryManager.class);
    Mockito.when(tableRuntimeFactoryManager.installedPlugins())
        .thenReturn(Lists.newArrayList(runtimeFactory));
    List<ActionCoordinator> actionCoordinators =
        tableRuntimeFactoryManager.installedPlugins().stream()
            .flatMap(factory -> factory.supportedCoordinators().stream())
            .collect(Collectors.toList());
    try {
      Configurations configurations = new Configurations();
      configurations.set(AmoroManagementConf.OPTIMIZER_HB_TIMEOUT, Duration.ofMillis(800L));
      configurations.set(
          AmoroManagementConf.OPTIMIZER_TASK_EXECUTE_TIMEOUT, Duration.ofMillis(30000L));
      TABLE_SERVICE =
          new DefaultTableService(
              new Configurations(), CATALOG_MANAGER, Lists.newArrayList(runtimeFactory));
      OPTIMIZING_SERVICE =
          new DefaultOptimizingService(
              configurations, CATALOG_MANAGER, OPTIMIZER_MANAGER, TABLE_SERVICE);
      PROCESS_SERVICE =
          new ProcessService(
              configurations, TABLE_SERVICE, actionCoordinators, new ExecuteEngineManager());

      TABLE_SERVICE.addHandlerChain(OPTIMIZING_SERVICE.getTableRuntimeHandler());
      TABLE_SERVICE.addHandlerChain(PROCESS_SERVICE.getTableHandlerChain());
      TABLE_SERVICE.initialize();
      try {
        ResourceGroup group = defaultResourceGroup();
        OPTIMIZER_MANAGER.createResourceGroup(group);
        OPTIMIZING_SERVICE.createResourceGroup(group);
      } catch (Throwable ignored) {
      }
    } catch (Throwable throwable) {
      Assert.fail(throwable.getMessage());
    }
  }

  @AfterClass
  public static void disposeTableService() {
    TABLE_SERVICE.dispose();
    MetricManager.dispose();
    EventsManager.dispose();
    tableRuntimeFactoryManager = null;
  }

  protected DefaultTableService tableService() {
    return TABLE_SERVICE;
  }

  protected CompatibleTableRuntime getDefaultTableRuntime(long tableId) {
    return (CompatibleTableRuntime) tableService().getRuntime(tableId);
  }

  protected DefaultOptimizingService optimizingService() {
    return OPTIMIZING_SERVICE;
  }

  protected ProcessService processServiceService() {
    return PROCESS_SERVICE;
  }

  protected static ResourceGroup defaultResourceGroup() {
    return new ResourceGroup.Builder("default", "local").build();
  }
}
