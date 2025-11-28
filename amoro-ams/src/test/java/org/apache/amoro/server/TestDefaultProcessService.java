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

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.server.process.MockExecuteEngine;
import org.apache.amoro.server.table.AMSTableTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Future;

@RunWith(Parameterized.class)
public class TestDefaultProcessService extends AMSTableTestBase {

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(false, true)}
    };
  }

  public TestDefaultProcessService(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper, false);
  }

  @Before
  public void prepare() {
    createDatabase();
  }

  @After
  public void clear() {
    try {
      optimizerManager()
          .listOptimizers()
          .forEach(
              optimizer ->
                  optimizingService()
                      .deleteOptimizer(optimizer.getGroupName(), optimizer.getResourceId()));
      dropDatabase();
    } catch (Exception e) {
      // ignore
    }
  }

  @Test
  @Timeout(60)
  public void testRunTableProcess() {
    try {
      createTable();

      MockExecuteEngine executeEngine =
          (MockExecuteEngine)
              processServiceService().getExecuteEngines().values().stream().findFirst().get();
      ServerTableIdentifier serverTableIdentifier =
          ServerTableIdentifier.of(
              1L,
              TableTestHelper.TEST_CATALOG_NAME,
              TableTestHelper.TEST_DB_NAME,
              TableTestHelper.TEST_TABLE_NAME,
              TableFormat.ICEBERG);
      while (processServiceService().getActiveTableProcess().isEmpty()) {
        Thread.sleep(3000);
      }
      TableProcess tableProcess =
          processServiceService().getTableProcessInstances(serverTableIdentifier).values().stream()
              .findFirst()
              .get();
      while (executeEngine.getActiveInstances().isEmpty()) {
        Thread.sleep(3000);
      }
      Assertions.assertEquals(tableProcess.getStatus(), ProcessStatus.RUNNING);
      Future<?> future =
          executeEngine.getActiveInstances().get(tableProcess.getExternalProcessIdentifier());
      Assertions.assertTrue(future != null);
      Assertions.assertEquals(future.isDone(), false);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    } finally {
      dropTable();
    }
  }

  @Test
  @Timeout(60)
  public void testCancelTableProcess() {
    MockExecuteEngine executeEngine =
        (MockExecuteEngine)
            processServiceService().getExecuteEngines().values().stream().findFirst().get();
    try {
      createTable();
      ServerTableIdentifier serverTableIdentifier =
          ServerTableIdentifier.of(
              1L,
              TableTestHelper.TEST_CATALOG_NAME,
              TableTestHelper.TEST_DB_NAME,
              TableTestHelper.TEST_TABLE_NAME,
              TableFormat.ICEBERG);

      while (processServiceService().getActiveTableProcess().isEmpty()) {
        Thread.sleep(3000);
      }
      TableProcess tableProcess =
          processServiceService().getTableProcessInstances(serverTableIdentifier).values().stream()
              .findFirst()
              .get();
      dropTable();

      while (!executeEngine.getActiveInstances().isEmpty()) {
        Thread.sleep(3000);
      }
      while (!executeEngine.getCancelingInstances().isEmpty()) {
        Thread.sleep(3000);
      }

      Assertions.assertTrue(processServiceService().getActiveTableProcess().isEmpty());
      Assertions.assertTrue(executeEngine.getActiveInstances().isEmpty());
      Assertions.assertTrue(tableProcess.getStatus() == ProcessStatus.CANCELED);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    } finally {
      if (!processServiceService().getActiveTableProcess().isEmpty()) {
        throw new IllegalStateException(
            "Table process map in actionCoordinator should be clear down if process has been canceled.");
      }
      if (!executeEngine.getActiveInstances().isEmpty()
          || !executeEngine.getCancelingInstances().isEmpty()) {
        throw new IllegalStateException(
            "Table process executing task in execute engine should be clear down if process has been canceled.");
      }
    }
  }

  @Test
  @Timeout(60)
  public void testRecoverTableProcess() {

    MockExecuteEngine executeEngine =
        (MockExecuteEngine)
            processServiceService().getExecuteEngines().values().stream().findFirst().get();
    try {
      createTable();
      ServerTableIdentifier serverTableIdentifier =
          ServerTableIdentifier.of(
              1L,
              TableTestHelper.TEST_CATALOG_NAME,
              TableTestHelper.TEST_DB_NAME,
              TableTestHelper.TEST_TABLE_NAME,
              TableFormat.ICEBERG);

      while (processServiceService().getActiveTableProcess().isEmpty()) {
        Thread.sleep(3000);
      }

      TableProcess tableProcess =
          processServiceService().getTableProcessInstances(serverTableIdentifier).values().stream()
              .findFirst()
              .get();

      while (executeEngine.getStatus(tableProcess.getExternalProcessIdentifier())
          != ProcessStatus.RUNNING) {
        Thread.sleep(3000);
      }

      Assertions.assertEquals(tableProcess.getStatus(), ProcessStatus.RUNNING);
      processServiceService()
          .untrackTableProcessInstance(serverTableIdentifier, tableProcess.getId());

      processServiceService()
          .recoverProcesses(
              new ArrayList<>(Collections.singletonList(tableProcess.getTableRuntime())));
      while (processServiceService().getActiveTableProcess().isEmpty()) {
        Thread.sleep(3000);
      }

      tableProcess =
          processServiceService().getTableProcessInstances(serverTableIdentifier).values().stream()
              .findFirst()
              .get();

      while (executeEngine.getStatus(tableProcess.getExternalProcessIdentifier())
          != ProcessStatus.RUNNING) {
        Thread.sleep(3000);
      }

      Assertions.assertEquals(tableProcess.getStatus(), ProcessStatus.RUNNING);
      Future<?> future =
          executeEngine.getActiveInstances().get(tableProcess.getExternalProcessIdentifier());
      Assertions.assertTrue(future != null);
      Assertions.assertEquals(future.isDone(), false);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    } finally {
      dropTable();
    }
  }
}
