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
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.server.manager.EventsManager;
import org.apache.amoro.server.manager.MetricManager;
import org.apache.amoro.server.process.ProcessService;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.DefaultTableRuntimeFactory;
import org.apache.amoro.server.table.DefaultTableService;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.time.Duration;

public abstract class AMSServiceTestBase extends AMSManagerTestBase {
  private static DefaultTableService TABLE_SERVICE = null;
  private static DefaultOptimizingService OPTIMIZING_SERVICE = null;
  private static ProcessService PROCESS_SERVICE = null;

  @BeforeClass
  public static void initTableService() {
    DefaultTableRuntimeFactory runtimeFactory = new DefaultTableRuntimeFactory();
    try {
      Configurations configurations = new Configurations();
      configurations.set(AmoroManagementConf.OPTIMIZER_HB_TIMEOUT, Duration.ofMillis(800L));
      configurations.set(
          AmoroManagementConf.OPTIMIZER_TASK_EXECUTE_TIMEOUT, Duration.ofMillis(30000L));
      configurations.set(
          AmoroManagementConf.OPTIMIZER_GROUP_MIN_PARALLELISM_CHECK_INTERVAL,
          Duration.ofMillis(10L));
      // Note: auto-restart is intentionally left at its production default (disabled) here so
      // that sibling AMS tests are not affected by the keeper's orphan-detection queries. The
      // TestOptimizerGroupKeeper class enables auto-restart on the shared OPTIMIZING_SERVICE
      // instance only for its own lifetime via reflection and resets it afterwards.
      TABLE_SERVICE =
          new DefaultTableService(new Configurations(), CATALOG_MANAGER, runtimeFactory);
      OPTIMIZING_SERVICE =
          new DefaultOptimizingService(
              configurations, CATALOG_MANAGER, OPTIMIZER_MANAGER, TABLE_SERVICE, null, null);
      PROCESS_SERVICE = new ProcessService(TABLE_SERVICE);

      TABLE_SERVICE.addHandlerChain(OPTIMIZING_SERVICE.getTableRuntimeHandler());
      TABLE_SERVICE.addHandlerChain(PROCESS_SERVICE.getTableHandlerChain());
      TABLE_SERVICE.initialize();
      ResourceGroup group = defaultResourceGroup();
      try {
        OPTIMIZER_MANAGER.createResourceGroup(group);
      } catch (Throwable ignored) {
      }
      try {
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
  }

  protected DefaultTableService tableService() {
    return TABLE_SERVICE;
  }

  protected DefaultTableRuntime getDefaultTableRuntime(long tableId) {
    return (DefaultTableRuntime) tableService().getRuntime(tableId);
  }

  protected DefaultOptimizingService optimizingService() {
    return OPTIMIZING_SERVICE;
  }

  /**
   * Static accessor for the shared {@link DefaultOptimizingService}. Used by subclasses that need
   * to read or mutate service state from a {@code @BeforeClass} / {@code @AfterClass} hook, where
   * the instance-level {@link #optimizingService()} is not reachable.
   */
  protected static DefaultOptimizingService optimizingServiceStatic() {
    return OPTIMIZING_SERVICE;
  }

  protected ProcessService processServiceService() {
    return PROCESS_SERVICE;
  }

  protected static ResourceGroup defaultResourceGroup() {
    return new ResourceGroup.Builder("default", "local").build();
  }
}
