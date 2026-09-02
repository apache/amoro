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
import org.apache.amoro.TableRuntime;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.process.ActionCoordinatorScheduler;
import org.apache.amoro.server.process.MockActionCoordinator;
import org.apache.amoro.server.process.MockExecuteEngine;
import org.apache.amoro.server.process.ProcessService;
import org.apache.amoro.server.process.TableProcessMeta;
import org.apache.amoro.server.process.ThrowingRecoverActionCoordinator;
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
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

/**
 * Integration tests for running, canceling, and recovering processes in the default process
 * service.
 */
@RunWith(Parameterized.class)
public class TestDefaultProcessService extends AMSTableTestBase {

  private static final long WAIT_TIMEOUT_MS = 60_000L;
  private static final long POLL_INTERVAL_MS = 3_000L;
  private static final Persistence PERSISTENCE = new Persistence();

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

    MockExecuteEngine mockExecuteEngine = new MockExecuteEngine();
    processServiceService().installExecuteEngine(mockExecuteEngine);

    MockActionCoordinator mockActionCoordinator = new MockActionCoordinator(mockExecuteEngine);
    processServiceService().installActionCoordinator(mockActionCoordinator);
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
      TableProcessStore store = getAnyActiveTableProcess();

      // Wait again for active instances to preserve the original semantics
      awaitActiveInstances(executeEngine);

      // Assert status and engine instance
      Assert.assertEquals(ProcessStatus.RUNNING, store.getStatus());
      Future<?> future =
          executeEngine.getActiveInstances().get(store.getExternalProcessIdentifier());
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

      ProcessService.TableProcessHolder holder = getAnyActiveTableProcessHolder();
      ServerTableIdentifier tableIdentifier =
          holder.getProcess().getTableRuntime().getTableIdentifier();
      TableProcessStore store = holder.getStore();
      dropTable();

      // Wait until both active and canceling queues are empty
      awaitEngineDrained(executeEngine);

      Assert.assertTrue(
          processServiceService().getTableProcessInstances(tableIdentifier).isEmpty());
      Assert.assertTrue(executeEngine.getActiveInstances().isEmpty());
      Assert.assertEquals(ProcessStatus.CANCELED, store.getStatus());
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

      ProcessService.TableProcessHolder holder = getAnyActiveTableProcessHolder();
      TableProcessStore store = holder.getStore();
      TableRuntime tableRuntime = holder.getProcess().getTableRuntime();

      awaitEngineStatus(executeEngine, store.getExternalProcessIdentifier(), ProcessStatus.RUNNING);
      Assert.assertEquals(ProcessStatus.RUNNING, store.getStatus());

      processServiceService()
          .untrackTableProcessInstance(tableRuntime.getTableIdentifier(), store.getProcessId());

      processServiceService()
          .recoverProcesses(new ArrayList<>(Collections.singletonList(tableRuntime)));

      // Wait for the active table process to reappear
      awaitCondition(
          () -> !processServiceService().getActiveTableProcess().isEmpty(),
          WAIT_TIMEOUT_MS,
          POLL_INTERVAL_MS);

      holder = getAnyActiveTableProcessHolder();
      store = holder.getStore();

      awaitEngineStatus(executeEngine, store.getExternalProcessIdentifier(), ProcessStatus.RUNNING);
      Assert.assertEquals(ProcessStatus.RUNNING, store.getStatus());
      Future<?> future =
          executeEngine.getActiveInstances().get(store.getExternalProcessIdentifier());
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

  /** Verify active processes are recovered when table ownership moves to this AMS. */
  @Test(timeout = 60_000)
  public void testRecoverTableProcessWhenTableAdded() {
    MockExecuteEngine executeEngine = getExecuteEngine();
    ExecutorService recoveryExecutor = Executors.newSingleThreadExecutor();
    BlockingRecoverActionCoordinator coordinator =
        new BlockingRecoverActionCoordinator(executeEngine);
    try {
      // Start a process as the old table owner and capture its persisted identity.
      createTable();
      awaitActiveInstances(executeEngine);

      ProcessService.TableProcessHolder originalHolder = getAnyActiveTableProcessHolder();
      TableProcessStore originalStore = originalHolder.getStore();
      TableRuntime tableRuntime = originalHolder.getProcess().getTableRuntime();
      long processId = originalStore.getProcessId();
      String originalExternalId = originalStore.getExternalProcessIdentifier();

      // Simulate losing the old owner: stop its external process and wait until the local active
      // process entry has been removed.
      executeEngine.tryCancelTableProcess(originalHolder.getProcess(), originalExternalId);
      awaitCondition(
          () -> originalStore.getStatus() == ProcessStatus.CANCELED,
          WAIT_TIMEOUT_MS,
          POLL_INTERVAL_MS);
      awaitCondition(
          () ->
              processServiceService()
                  .getTableProcessInstances(tableRuntime.getTableIdentifier())
                  .isEmpty(),
          WAIT_TIMEOUT_MS,
          POLL_INTERVAL_MS);
      Assert.assertFalse(
          processServiceService()
              .getTableProcessInstances(tableRuntime.getTableIdentifier())
              .containsValue(originalHolder));

      // Recreate the database state observed after an abrupt owner loss. The process remains
      // RUNNING, but its external identifier is unavailable to the new owner.
      markProcessRunningWithoutExternalIdentifier(processId);
      processServiceService().unInstallAllActionCoordinators();
      processServiceService().installActionCoordinator(coordinator);

      // Hold the first table-added recovery inside the coordinator, then deliver the same event
      // again. The second event MUST see the atomic recovery reservation and return without
      // recovering the process a second time.
      Future<?> firstRecovery =
          recoveryExecutor.submit(
              () ->
                  processServiceService()
                      .getTableHandlerChain()
                      .fireTableAdded(
                          tableService().loadTable(tableRuntime.getTableIdentifier()),
                          tableRuntime));
      coordinator.awaitRecoveryStarted();

      processServiceService()
          .getTableHandlerChain()
          .fireTableAdded(
              tableService().loadTable(tableRuntime.getTableIdentifier()), tableRuntime);
      Assert.assertEquals(1, coordinator.getRecoveryCount());

      coordinator.releaseRecovery();
      firstRecovery.get(WAIT_TIMEOUT_MS, TimeUnit.MILLISECONDS);

      // The new owner MUST track the same persisted process ID with a newly submitted external
      // process. The old store must not remain in the active-process map.
      awaitCondition(
          () ->
              processServiceService()
                  .getTableProcessInstances(tableRuntime.getTableIdentifier())
                  .containsKey(processId),
          WAIT_TIMEOUT_MS,
          POLL_INTERVAL_MS);

      ProcessService.TableProcessHolder recoveredHolder =
          processServiceService()
              .getTableProcessInstances(tableRuntime.getTableIdentifier())
              .get(processId);
      awaitCondition(
          () ->
              recoveredHolder.getStore().getStatus() == ProcessStatus.RUNNING
                  && !recoveredHolder.getStore().getExternalProcessIdentifier().isEmpty(),
          WAIT_TIMEOUT_MS,
          POLL_INTERVAL_MS);

      String recoveredExternalId = recoveredHolder.getStore().getExternalProcessIdentifier();
      Assert.assertNotEquals(originalExternalId, recoveredExternalId);
      Assert.assertNotSame(originalStore, recoveredHolder.getStore());
      Assert.assertEquals(
          1,
          processServiceService()
              .getTableProcessInstances(tableRuntime.getTableIdentifier())
              .size());
      Assert.assertEquals(1, executeEngine.getActiveInstances().size());

      // handleTableAdded also starts the periodic scheduler. Wait until it actually triggers and
      // verify that the recovered RUNNING process prevents a second process from being submitted.
      coordinator.awaitSchedulerTriggered();
      Assert.assertEquals(1, executeEngine.getActiveInstances().size());
      Assert.assertEquals(
          1,
          processServiceService()
              .getTableProcessInstances(tableRuntime.getTableIdentifier())
              .size());

      dropTable();
    } catch (Throwable t) {
      throw new RuntimeException(t);
    } finally {
      coordinator.releaseRecovery();
      recoveryExecutor.shutdownNow();
    }
  }

  /**
   * Verify that a single un-recoverable process record does not abort AMS startup: {@code
   * recoverProcesses} must not propagate the failure, the bad record is skipped and persisted as
   * FAILED so a later restart neither throws nor re-picks it. Regression test for AMORO-4223.
   */
  @Test(timeout = 60_000)
  public void testRecoverProcessFailSafe() {
    MockExecuteEngine executeEngine = getExecuteEngine();
    try {
      createTable();

      awaitActiveInstances(executeEngine);

      ProcessService.TableProcessHolder holder = getAnyActiveTableProcessHolder();
      TableProcessStore store = holder.getStore();
      TableRuntime tableRuntime = holder.getProcess().getTableRuntime();

      awaitEngineStatus(executeEngine, store.getExternalProcessIdentifier(), ProcessStatus.RUNNING);
      Assert.assertEquals(ProcessStatus.RUNNING, store.getStatus());

      // Simulate an AMS restart where the process can no longer be recovered (the exact
      // condition that bricked AMS in AMORO-4223): stop tracking the live instance, then
      // swap in a coordinator whose recover() always fails.
      processServiceService()
          .untrackTableProcessInstance(tableRuntime.getTableIdentifier(), store.getProcessId());
      processServiceService().unInstallAllActionCoordinators();
      processServiceService()
          .installActionCoordinator(new ThrowingRecoverActionCoordinator(executeEngine));

      // Must NOT throw: the un-recoverable record is contained and AMS keeps starting up.
      processServiceService()
          .recoverProcesses(new ArrayList<>(Collections.singletonList(tableRuntime)));
      Assert.assertTrue(processServiceService().getActiveTableProcess().isEmpty());

      // The bad record is now persisted as FAILED, so it is no longer "active": a subsequent
      // restart neither throws nor re-picks it.
      processServiceService()
          .recoverProcesses(new ArrayList<>(Collections.singletonList(tableRuntime)));
      Assert.assertTrue(processServiceService().getActiveTableProcess().isEmpty());

      dropTable();
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Verify that a process which keeps failing is retried a bounded number of times and then
   * dropped, instead of being resubmitted in an infinite loop. The process store must be created
   * with the retry limit rather than the current retry count, otherwise a FAILED process is treated
   * as terminal immediately (retryNumber >= maxRetryTime), every RETRY_REQUESTED transition is
   * rejected, retryNumber never increases and the process is resubmitted forever.
   */
  @Test(timeout = 60_000)
  public void testFailedProcessRetryIsBounded() throws InterruptedException {
    AlwaysFailingExecuteEngine failingEngine = new AlwaysFailingExecuteEngine();
    processServiceService().unInstallAllExecuteEngines();
    processServiceService().installExecuteEngine(failingEngine);
    processServiceService().unInstallAllActionCoordinators();
    processServiceService().installActionCoordinator(new MockActionCoordinator(failingEngine));
    try {
      createTable();

      // Wait until the failing process has been submitted at least once.
      awaitCondition(() -> failingEngine.getSubmitAttempts() >= 1, WAIT_TIMEOUT_MS, 100L);

      // The process must exhaust its bounded retries and be untracked. With the infinite
      // retry loop, the active process map never empties and this wait times out.
      awaitCondition(
          () -> processServiceService().getActiveTableProcess().isEmpty(), 15_000L, 100L);

      Assert.assertEquals(
          1 + ActionCoordinatorScheduler.PROCESS_MAX_RETRY_NUMBER,
          failingEngine.getSubmitAttempts());

      dropTable();
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /** Execute engine whose submissions always fail. */
  private static class AlwaysFailingExecuteEngine extends MockExecuteEngine {
    private final AtomicInteger submitAttempts = new AtomicInteger();

    @Override
    public String submitTableProcess(org.apache.amoro.process.TableProcess tableProcess) {
      submitAttempts.incrementAndGet();
      throw new IllegalStateException("Submission failure");
    }

    private int getSubmitAttempts() {
      return submitAttempts.get();
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

  /** Get any active table process holder; throw a clear error if none exists. */
  private ProcessService.TableProcessHolder getAnyActiveTableProcessHolder() {
    Map<ServerTableIdentifier, Map<Long, ProcessService.TableProcessHolder>> active =
        processServiceService().getActiveTableProcess();
    if (active == null || active.isEmpty()) {
      throw new IllegalStateException("No active table process present");
    }
    Map<?, ProcessService.TableProcessHolder> inner =
        active.values().stream().findFirst().orElse(null);
    if (inner == null || inner.isEmpty()) {
      throw new IllegalStateException("No active table process present");
    }
    ProcessService.TableProcessHolder tp = inner.values().stream().findFirst().orElse(null);
    if (tp == null) {
      throw new IllegalStateException("No active table process present");
    }
    return tp;
  }

  private TableProcessStore getAnyActiveTableProcess() {
    return getAnyActiveTableProcessHolder().getStore();
  }

  private void markProcessRunningWithoutExternalIdentifier(long processId) {
    PERSISTENCE.markProcessRunningWithoutExternalIdentifier(processId);
  }

  private static class Persistence extends PersistentBase {
    private void markProcessRunningWithoutExternalIdentifier(long processId) {
      doAs(
          TableProcessMapper.class,
          mapper -> {
            TableProcessMeta meta = mapper.getProcessMeta(processId);
            mapper.updateProcess(
                meta.getTableId(),
                processId,
                "",
                ProcessStatus.RUNNING,
                meta.getProcessStage(),
                meta.getRetryNumber(),
                0L,
                "",
                meta.getProcessParameters(),
                meta.getSummary());
          });
    }
  }

  private static class BlockingRecoverActionCoordinator extends MockActionCoordinator {
    private final CountDownLatch recoveryStarted = new CountDownLatch(1);
    private final CountDownLatch releaseRecovery = new CountDownLatch(1);
    private final CountDownLatch schedulerTriggered = new CountDownLatch(1);
    private final AtomicInteger recoveryCount = new AtomicInteger();

    private BlockingRecoverActionCoordinator(MockExecuteEngine executeEngine) {
      super(executeEngine);
    }

    @Override
    public TableProcess recoverTableProcess(
        TableRuntime tableRuntime, TableProcessStore processStore) {
      recoveryCount.incrementAndGet();
      recoveryStarted.countDown();
      try {
        if (!releaseRecovery.await(WAIT_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
          throw new AssertionError("Timed out waiting to release process recovery");
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Interrupted while waiting to recover process", e);
      }
      return super.recoverTableProcess(tableRuntime, processStore);
    }

    @Override
    public Optional<TableProcess> trigger(TableRuntime tableRuntime) {
      schedulerTriggered.countDown();
      return super.trigger(tableRuntime);
    }

    private void awaitRecoveryStarted() throws InterruptedException {
      if (!recoveryStarted.await(WAIT_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
        throw new AssertionError("Process recovery did not start");
      }
    }

    private void releaseRecovery() {
      releaseRecovery.countDown();
    }

    private void awaitSchedulerTriggered() throws InterruptedException {
      if (!schedulerTriggered.await(WAIT_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
        throw new AssertionError("Table scheduler did not trigger");
      }
    }

    private int getRecoveryCount() {
      return recoveryCount.get();
    }
  }

  /** Wait until the given externalProcessIdentifier reaches the specified status. */
  private void awaitEngineStatus(MockExecuteEngine engine, String externalId, ProcessStatus status)
      throws InterruptedException {
    awaitCondition(() -> engine.getStatus(externalId) == status, WAIT_TIMEOUT_MS, POLL_INTERVAL_MS);
  }
}
