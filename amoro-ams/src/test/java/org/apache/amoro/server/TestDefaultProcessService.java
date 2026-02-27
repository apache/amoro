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
import org.apache.amoro.server.process.MockActionCoordinator;
import org.apache.amoro.server.process.MockExecuteEngine;
import org.apache.amoro.server.table.AMSTableTestBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.BooleanSupplier;

/**
 * Integration tests for running, canceling, and recovering processes in the default process
 * service.
 */
@RunWith(Parameterized.class)
public class TestDefaultProcessService extends AMSTableTestBase {

  private static final long WAIT_TIMEOUT_MS = 60_000L;
  private static final long POLL_INTERVAL_MS = 3_000L;

  /**
   * Parameterization for catalog and table helpers.
   *
   * @return parameter matrix
   */
  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[][] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(false, true)}
    };
  }

  public TestDefaultProcessService(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper, false);
  }

  /** Prepare the database before tests. */
  @Before
  public void prepare() {
    createDatabase();

    MockActionCoordinator mockActionCoordinator = new MockActionCoordinator();
    processServiceService().installActionCoordinator(mockActionCoordinator);

    MockExecuteEngine mockExecuteEngine = new MockExecuteEngine();
    processServiceService().installExecuteEngine(mockExecuteEngine);
  }

  /** Clear resources after tests. */
  @After
  public void clear() {
    try {
      optimizerManager()
          .listOptimizers()
          .forEach(
              optimizer ->
                  optimizingService()
                      .deleteOptimizer(optimizer.getGroupName(), optimizer.getResourceId()));
      processServiceService().unInstallAllActionCoordinators();
      processServiceService().unInstallAllExecuteEngines();
      dropDatabase();
    } catch (Exception e) {
      // ignore
    }
  }

  /** Verify a table process runs and an engine instance is created. */
  @Test(timeout = 60_000)
  public void testRunTableProcess() {
    try {
      createTable();

      MockExecuteEngine executeEngine = getExecuteEngine();

      // Wait until the engine has active instances
      awaitActiveInstances(executeEngine);

      // Get the current active TableProcess
      TableProcess tableProcess = getAnyActiveTableProcess();

      // Wait again for active instances to preserve the original semantics
      awaitActiveInstances(executeEngine);

      // Assert status and engine instance
      Assert.assertEquals(ProcessStatus.RUNNING, tableProcess.getStatus());
      Future<?> future =
          executeEngine.getActiveInstances().get(tableProcess.getExternalProcessIdentifier());
      Assert.assertNotNull(future);
      Assert.assertFalse(future.isDone());
      dropTable();
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /** Verify that canceling a process releases engine instances and clears tracking. */
  @Test(timeout = 60_000)
  public void testCancelTableProcess() {
    MockExecuteEngine executeEngine = getExecuteEngine();
    createTable();
    try {
      awaitActiveInstances(executeEngine);

      TableProcess tableProcess = getAnyActiveTableProcess();
      dropTable();

      // Wait until both active and canceling queues are empty
      awaitEngineDrained(executeEngine);

      Assert.assertTrue(
          processServiceService()
              .getTableProcessInstances(tableProcess.getTableRuntime().getTableIdentifier())
              .isEmpty());
      Assert.assertTrue(executeEngine.getActiveInstances().isEmpty());
      Assert.assertEquals(ProcessStatus.CANCELED, tableProcess.getStatus());
    } catch (Throwable t) {
      if (!processServiceService().getActiveTableProcess().isEmpty()) {
        throw new IllegalStateException(
            "Table process map in actionCoordinator should be clear down if process has been canceled.");
      }
      if (!executeEngine.getActiveInstances().isEmpty()
          || !executeEngine.getCancelingInstances().isEmpty()) {
        throw new IllegalStateException(
            "Table process executing task in execute engine should be clear down if process has been canceled.");
      }
      throw new RuntimeException(t);
    }
  }

  /** Verify recovery of active processes from persistence. */
  @Test(timeout = 60_000)
  public void testRecoverTableProcess() {

    MockExecuteEngine executeEngine = getExecuteEngine();
    try {
      createTable();

      awaitActiveInstances(executeEngine);

      TableProcess tableProcess = getAnyActiveTableProcess();

      awaitEngineStatus(
          executeEngine, tableProcess.getExternalProcessIdentifier(), ProcessStatus.RUNNING);

      Assert.assertEquals(ProcessStatus.RUNNING, tableProcess.getStatus());

      processServiceService()
          .untrackTableProcessInstance(
              tableProcess.getTableRuntime().getTableIdentifier(), tableProcess.getId());

      processServiceService()
          .recoverProcesses(
              new ArrayList<>(Collections.singletonList(tableProcess.getTableRuntime())));

      // Wait for the active table process to reappear
      awaitCondition(
          () -> !processServiceService().getActiveTableProcess().isEmpty(),
          WAIT_TIMEOUT_MS,
          POLL_INTERVAL_MS);

      tableProcess = getAnyActiveTableProcess();

      awaitEngineStatus(
          executeEngine, tableProcess.getExternalProcessIdentifier(), ProcessStatus.RUNNING);

      Assert.assertEquals(ProcessStatus.RUNNING, tableProcess.getStatus());
      Future<?> future =
          executeEngine.getActiveInstances().get(tableProcess.getExternalProcessIdentifier());
      Assert.assertNotNull(future);
      Assert.assertFalse(future.isDone());

      dropTable();

      // Preserve the original 'wait while both non-empty' semantics: stop waiting once either queue
      // is empty
      awaitCondition(
          () ->
              executeEngine.getActiveInstances().isEmpty()
                  || executeEngine.getCancelingInstances().isEmpty(),
          WAIT_TIMEOUT_MS,
          POLL_INTERVAL_MS);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  // ---------------------- Private helpers ----------------------

  /** Return the first available execute engine and validate its presence. */
  private MockExecuteEngine getExecuteEngine() {
    Object engine =
        processServiceService().getExecuteEngines().values().stream().findFirst().orElse(null);
    if (engine == null) {
      throw new IllegalStateException("No execute engine available");
    }
    if (!(engine instanceof MockExecuteEngine)) {
      throw new IllegalStateException(
          "Execute engine is not a MockExecuteEngine: " + engine.getClass().getName());
    }
    return (MockExecuteEngine) engine;
  }

  /** Poll until the condition holds, with timeout and interval. */
  private void awaitCondition(BooleanSupplier condition, long maxWaitMs, long intervalMs)
      throws InterruptedException {
    long start = System.currentTimeMillis();
    while (!condition.getAsBoolean()) {
      if (System.currentTimeMillis() - start >= maxWaitMs) {
        throw new AssertionError("Condition not met within " + maxWaitMs + " ms");
      }
      Thread.sleep(intervalMs);
    }
  }

  /** Wait until the engine has active instances. */
  private void awaitActiveInstances(MockExecuteEngine engine) throws InterruptedException {
    awaitCondition(() -> !engine.getActiveInstances().isEmpty(), WAIT_TIMEOUT_MS, POLL_INTERVAL_MS);
  }

  /** Wait until both the active and canceling queues are empty. */
  private void awaitEngineDrained(MockExecuteEngine engine) throws InterruptedException {
    awaitCondition(
        () -> engine.getActiveInstances().isEmpty() && engine.getCancelingInstances().isEmpty(),
        WAIT_TIMEOUT_MS,
        POLL_INTERVAL_MS);
  }

  /** Get any active TableProcess; throw a clear error if none exists. */
  private TableProcess getAnyActiveTableProcess() {
    Map<ServerTableIdentifier, Map<Long, TableProcess>> active =
        processServiceService().getActiveTableProcess();
    if (active == null || active.isEmpty()) {
      throw new IllegalStateException("No active table process present");
    }
    Map<?, TableProcess> inner = active.values().stream().findFirst().orElse(null);
    if (inner == null || inner.isEmpty()) {
      throw new IllegalStateException("No active table process present");
    }
    TableProcess tp = inner.values().stream().findFirst().orElse(null);
    if (tp == null) {
      throw new IllegalStateException("No active table process present");
    }
    return tp;
  }

  /** Wait until the given externalProcessIdentifier reaches the specified status. */
  private void awaitEngineStatus(MockExecuteEngine engine, String externalId, ProcessStatus status)
      throws InterruptedException {
    awaitCondition(() -> engine.getStatus(externalId) == status, WAIT_TIMEOUT_MS, POLL_INTERVAL_MS);
  }
}
