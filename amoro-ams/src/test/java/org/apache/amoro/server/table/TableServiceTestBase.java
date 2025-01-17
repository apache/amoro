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

package org.apache.amoro.server.table;

import org.apache.amoro.config.Configurations;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.DefaultOptimizingService;
import org.apache.amoro.server.catalog.DefaultCatalogManager;
import org.apache.amoro.server.manager.EventsManager;
import org.apache.amoro.server.manager.MetricManager;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;

public abstract class TableServiceTestBase {

  @ClassRule public static DerbyPersistence DERBY = new DerbyPersistence();

  protected static DefaultCatalogManager CATALOG_MANAGER = null;
  private static DefaultTableService TABLE_SERVICE = null;
  private static DefaultOptimizingService OPTIMIZING_SERVICE = null;

  @BeforeClass
  public static void initTableService() {
    try {
      Configurations configurations = new Configurations();
      configurations.set(AmoroManagementConf.OPTIMIZER_HB_TIMEOUT, 800L);
      CATALOG_MANAGER = new DefaultCatalogManager(configurations);
      TABLE_SERVICE = new DefaultTableService(new Configurations(), CATALOG_MANAGER);
      OPTIMIZING_SERVICE =
          new DefaultOptimizingService(configurations, CATALOG_MANAGER, TABLE_SERVICE);
      TABLE_SERVICE.addHandlerChain(OPTIMIZING_SERVICE.getTableRuntimeHandler());
      TABLE_SERVICE.initialize();
      try {
        OPTIMIZING_SERVICE.createResourceGroup(defaultResourceGroup());
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

  protected DefaultOptimizingService optimizingService() {
    return OPTIMIZING_SERVICE;
  }

  protected static ResourceGroup defaultResourceGroup() {
    return new ResourceGroup.Builder("default", "local").build();
  }
}
