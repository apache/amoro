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
import org.apache.amoro.PaimonActions;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.process.ActionCoordinator;
import org.apache.amoro.process.LocalExecutionEngine;
import org.apache.amoro.process.LocalProcess;
import org.apache.amoro.process.ProcessEvent;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.server.DefaultOptimizingService;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.DefaultTableRuntimeStore;
import org.apache.amoro.server.table.DerbyPersistence;
import org.apache.amoro.server.table.TableService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/** Deterministic cancellation races using the real service, store and local execution engine. */
class TestProcessCancellationConcurrency {
  static Stream<TableFormat> formats() {
    return Stream.of(TableFormat.ICEBERG, TableFormat.PAIMON);
  }

  @ParameterizedTest
  @MethodSource("formats")
  void cancellationBeforeIdentifierPublicationCancelsQueuedWork(TableFormat format)
      throws Exception {
    try (Fixture f = new Fixture(format)) {
      Work blocker = f.work(() -> await(f.releasePool, 30));
      f.engine.submitDirect(blocker);
      await(blocker.started);
      Work target = f.work(() -> {});
      f.service.register(f.runtime, target);
      await(f.engine.submitted);
      Assertions.assertEquals(0, target.runs.get());

      f.cancelCleanup();
      Assertions.assertEquals(ProcessStatus.CANCELED, f.service.store.getStatus());
      f.engine.releaseSubmit.countDown();
      await(f.engine.canceled);
      Assertions.assertEquals(ProcessStatus.CANCELED, f.engine.getStatus(f.engine.id));

      // A marker behind the canceled task proves the queue drained without invoking its body.
      Work marker = f.work(() -> {});
      f.engine.submitDirect(marker);
      f.releasePool.countDown();
      await(marker.finished);
      Assertions.assertEquals(0, target.runs.get());
      Assertions.assertEquals(1, f.engine.cancelRequests.get());
      Assertions.assertTrue(f.service.getTableProcessInstances(f.identifier).isEmpty());
    }
  }

  @ParameterizedTest
  @MethodSource("formats")
  void cancellationBetweenFailureAndRetryBecomesCanceled(TableFormat format) throws Exception {
    try (Fixture f = new Fixture(format)) {
      f.service.pause = Pause.AFTER_FAILURE;
      f.engine.releaseSubmit.countDown();
      Work target =
          f.work(
              () -> {
                throw new IllegalStateException("controlled failure");
              });
      f.service.register(f.runtime, target);
      await(f.service.transitionReached);
      Assertions.assertEquals(ProcessStatus.FAILED, f.service.store.getStatus());

      f.cancelCleanup();
      Assertions.assertEquals(ProcessStatus.CANCELED, f.service.store.getStatus());
      f.service.releaseTransition.countDown();
      await(f.service.callbackFinished);
      Assertions.assertEquals(ProcessStatus.CANCELED, f.service.store.getStatus());
      Assertions.assertEquals(0, f.service.store.getRetryNumber());
      Assertions.assertEquals(1, f.engine.submissions.get());
      Assertions.assertTrue(f.service.getTableProcessInstances(f.identifier).isEmpty());
    }
  }

  @ParameterizedTest
  @MethodSource("formats")
  void rejectedCancellationDoesNotPreventRetry(TableFormat format) throws Exception {
    try (Fixture f = new Fixture(format)) {
      f.service.pause = Pause.AFTER_FAILURE;
      f.engine.releaseSubmit.countDown();
      AtomicInteger attempts = new AtomicInteger();
      Work target =
          f.work(
              () -> {
                if (attempts.getAndIncrement() == 0) {
                  throw new IllegalStateException("first attempt fails");
                }
              });
      f.service.register(f.runtime, target);
      await(f.service.transitionReached);
      Assertions.assertFalse(
          f.service.store.tryTransitState(
              ProcessStatus.RUNNING,
              ProcessEvent.CANCEL_REQUESTED,
              f.service.store.getExternalProcessIdentifier(),
              "invalid target",
              Collections.emptyMap(),
              Collections.emptyMap()));
      Assertions.assertEquals(ProcessStatus.FAILED, f.service.store.getStatus());
      Assertions.assertFalse(f.service.getTableProcessInstances(f.identifier).isEmpty());
      f.service.releaseTransition.countDown();
      await(f.service.successRecorded);
      await(f.service.callbackFinished);
      Assertions.assertEquals(ProcessStatus.SUCCESS, f.service.store.getStatus());
      Assertions.assertEquals(1, f.service.store.getRetryNumber());
      Assertions.assertEquals(2, f.engine.submissions.get());
    }
  }

  @ParameterizedTest
  @MethodSource("formats")
  void tableRemovalBetweenFailureAndRetryCancels(TableFormat format) throws Exception {
    try (Fixture f = new Fixture(format)) {
      f.service.pause = Pause.AFTER_FAILURE;
      f.engine.releaseSubmit.countDown();
      Work target =
          f.work(
              () -> {
                throw new IllegalStateException("controlled failure");
              });
      f.service.register(f.runtime, target);
      await(f.service.transitionReached);
      f.service.getTableHandlerChain().fireTableRemoved(f.runtime);
      Assertions.assertEquals(ProcessStatus.CANCELED, f.service.store.getStatus());
      f.service.releaseTransition.countDown();
      await(f.service.callbackFinished);
      Assertions.assertEquals(0, f.service.store.getRetryNumber());
      Assertions.assertEquals(1, f.engine.submissions.get());
      Assertions.assertTrue(f.service.getTableProcessInstances(f.identifier).isEmpty());
    }
  }

  @ParameterizedTest
  @MethodSource("formats")
  void tableRemovalCancelsExecutionAfterFinalPollingFailure(TableFormat format) throws Exception {
    try (Fixture f = new Fixture(format)) {
      f.service.pause = Pause.AFTER_FAILURE;
      f.engine.failPolling = true;
      Work target = f.work(() -> await(f.releasePool, 30));
      f.service.register(f.runtime, target);
      await(f.engine.submitted);
      await(target.started);
      // Submit is gated, so the last-attempt state is persisted before polling starts.
      f.service
          .store
          .begin()
          .updateRetryNumber(ActionCoordinatorScheduler.PROCESS_MAX_RETRY_NUMBER)
          .commit();
      f.engine.releaseSubmit.countDown();
      await(f.service.transitionReached);

      Assertions.assertEquals(ProcessStatus.FAILED, f.service.store.getStatus());
      Assertions.assertEquals(f.engine.id, f.service.store.getExternalProcessIdentifier());
      Assertions.assertEquals(1, target.finished.getCount());
      Assertions.assertFalse(f.endpoint.cancelProcess(f.service.store.getProcessId()));
      Assertions.assertEquals(0, f.engine.cancelRequests.get());
      Assertions.assertFalse(f.service.getTableProcessInstances(f.identifier).isEmpty());

      f.service.getTableHandlerChain().fireTableRemoved(f.runtime);
      Assertions.assertEquals(1, f.engine.cancelRequests.get());
      await(f.engine.canceled);
      Assertions.assertTrue(f.service.getTableProcessInstances(f.identifier).isEmpty());
      // Table removal preserves the recorded terminal result.
      Assertions.assertEquals(ProcessStatus.FAILED, f.service.store.getStatus());
      f.service.releaseTransition.countDown();
      await(f.service.callbackFinished);
      Assertions.assertEquals(1, f.engine.submissions.get());
      Assertions.assertEquals(
          ActionCoordinatorScheduler.PROCESS_MAX_RETRY_NUMBER, f.service.store.getRetryNumber());
    }
  }

  @ParameterizedTest
  @MethodSource("formats")
  void completionWinsCancellation(TableFormat format) throws Exception {
    try (Fixture f = new Fixture(format)) {
      f.service.pause = Pause.AFTER_SUCCESS;
      f.engine.releaseSubmit.countDown();
      f.service.register(f.runtime, f.work(() -> {}));
      await(f.service.transitionReached);
      Assertions.assertFalse(f.endpoint.cancelProcess(f.service.store.getProcessId()));
      Assertions.assertEquals(ProcessStatus.SUCCESS, f.service.store.getStatus());
      Assertions.assertFalse(f.service.getTableProcessInstances(f.identifier).isEmpty());
      f.service.releaseTransition.countDown();
      await(f.service.callbackFinished);
      Assertions.assertEquals(ProcessStatus.SUCCESS, f.service.store.getStatus());
      Assertions.assertTrue(f.service.getTableProcessInstances(f.identifier).isEmpty());
    }
  }

  @ParameterizedTest
  @MethodSource("formats")
  void cancellationWinsCompletion(TableFormat format) throws Exception {
    try (Fixture f = new Fixture(format)) {
      f.service.pause = Pause.BEFORE_SUCCESS;
      f.engine.releaseSubmit.countDown();
      f.service.register(f.runtime, f.work(() -> {}));
      await(f.service.transitionReached);
      f.cancelCleanup();
      f.service.releaseTransition.countDown();
      await(f.service.callbackFinished);
      Assertions.assertEquals(ProcessStatus.CANCELED, f.service.store.getStatus());
      Assertions.assertEquals(0, f.service.store.getRetryNumber());
      Assertions.assertEquals(1, f.engine.submissions.get());
      Assertions.assertTrue(f.service.getTableProcessInstances(f.identifier).isEmpty());
    }
  }

  private enum Pause {
    NONE,
    AFTER_FAILURE,
    BEFORE_SUCCESS,
    AFTER_SUCCESS
  }

  private static void await(CountDownLatch latch) {
    await(latch, 10);
  }

  private static void await(CountDownLatch latch, int seconds) {
    try {
      Assertions.assertTrue(
          latch.await(seconds, TimeUnit.SECONDS), "Timed out waiting for test gate");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    }
  }

  private static class Fixture extends PersistentBase implements AutoCloseable {
    final DerbyPersistence persistence = new DerbyPersistence();
    final CountDownLatch releasePool = new CountDownLatch(1);
    final GatedEngine engine = new GatedEngine();
    final ServerTableIdentifier identifier;
    final DefaultTableRuntime runtime;
    final ControlledService service;
    final DefaultOptimizingService endpoint;

    Fixture(TableFormat format) throws Exception {
      identifier = ServerTableIdentifier.of("cancel-races", "db", "tbl", format);
      doAs(TableMetaMapper.class, mapper -> mapper.insertTable(identifier));
      TableRuntimeMeta meta = new TableRuntimeMeta();
      meta.setTableId(identifier.getId());
      meta.setGroupName("default");
      meta.setStatusCode(0);
      DefaultTableRuntimeStore state =
          new DefaultTableRuntimeStore(
              identifier, meta, DefaultTableRuntime.REQUIRED_STATES, Collections.emptyList());
      runtime = new DefaultTableRuntime(state, () -> null);
      state.setTableRuntime(runtime);
      TableService tables = Mockito.mock(TableService.class);
      Mockito.when(tables.getRuntime(identifier.getId())).thenReturn(runtime);
      engine.open(Map.of("pool.default.thread-count", "1"));
      ProcessService.ExecuteEngineManager engines =
          Mockito.mock(ProcessService.ExecuteEngineManager.class);
      Mockito.when(engines.installedPlugins()).thenReturn(List.of(engine));
      ActionCoordinator coordinator =
          new MockActionCoordinator(engine) {
            @Override
            public Action action() {
              return PaimonActions.EXPIRE_SNAPSHOTS;
            }

            @Override
            public int parallelism() {
              return 1;
            }
          };
      service = new ControlledService(tables, List.of(coordinator), engines);
      service.getTableHandlerChain().initialize(Collections.emptyList());
      endpoint =
          new DefaultOptimizingService(
              new Configurations(), null, null, tables, service, null, null);
    }

    void cancelCleanup() {
      boolean canceled = endpoint.cancelProcess(service.store.getProcessId());
      if (identifier.getFormat() == TableFormat.ICEBERG) {
        Assertions.assertFalse(canceled);
        service.getTableHandlerChain().fireTableRemoved(runtime);
      } else {
        Assertions.assertTrue(canceled);
      }
    }

    Work work(Runnable action) {
      return new Work(runtime, engine, action);
    }

    @Override
    public void close() throws InterruptedException {
      engine.releaseSubmit.countDown();
      service.releaseTransition.countDown();
      service.getTableHandlerChain().fireTableRemoved(runtime);
      releasePool.countDown();
      Work marker = work(() -> {});
      engine.submitDirect(marker);
      await(marker.finished);
      service
          .getActionCoordinators()
          .values()
          .forEach(schedulers -> schedulers.forEach(ActionCoordinatorScheduler::dispose));
      endpoint.dispose();
      service.dispose();
      engine.close();
      Assertions.assertTrue(
          service.awaitTermination(10, TimeUnit.SECONDS), "Process callbacks did not terminate");
      persistence.clearTable(identifier.getId());
    }
  }

  private static class ControlledService extends ProcessService {
    volatile DefaultTableProcessStore store;
    volatile Pause pause = Pause.NONE;
    final AtomicBoolean paused = new AtomicBoolean();
    final CountDownLatch transitionReached = new CountDownLatch(1);
    final CountDownLatch releaseTransition = new CountDownLatch(1);
    final CountDownLatch completionObserved = new CountDownLatch(1);
    final CountDownLatch callbackFinished = new CountDownLatch(1);
    final CountDownLatch successRecorded = new CountDownLatch(1);

    ControlledService(
        TableService tables, List<ActionCoordinator> coordinators, ExecuteEngineManager engines) {
      super(tables, coordinators, engines);
    }

    @Override
    public TableProcess untrackTableProcessInstance(ServerTableIdentifier table, long processId) {
      TableProcess process = super.untrackTableProcessInstance(table, processId);
      if (completionObserved.getCount() == 0) {
        callbackFinished.countDown();
      }
      return process;
    }

    void pauseTransition() {
      if (paused.compareAndSet(false, true)) {
        transitionReached.countDown();
        await(releaseTransition);
      }
    }

    @Override
    protected DefaultTableProcessStore persistTableProcess(TableProcess process) {
      DefaultTableProcessStore initial = super.persistTableProcess(process);
      TableProcessMeta meta =
          getAs(TableProcessMapper.class, mapper -> mapper.getProcessMeta(initial.getProcessId()));
      store =
          new DefaultTableProcessStore(
              initial.getProcessId(),
              process.getTableRuntime(),
              meta,
              process.getAction(),
              ActionCoordinatorScheduler.PROCESS_MAX_RETRY_NUMBER) {
            @Override
            public boolean tryTransitState(
                ProcessStatus status,
                ProcessEvent event,
                String external,
                String reason,
                Map<String, String> parameters,
                Map<String, String> summary) {
              if (pause == Pause.BEFORE_SUCCESS && status == ProcessStatus.SUCCESS) {
                pauseTransition();
              }
              boolean accepted =
                  super.tryTransitState(status, event, external, reason, parameters, summary);
              if (accepted
                  && ((pause == Pause.AFTER_FAILURE && status == ProcessStatus.FAILED)
                      || (pause == Pause.AFTER_SUCCESS && status == ProcessStatus.SUCCESS))) {
                pauseTransition();
              }
              if (event == ProcessEvent.COMPLETE_SUCCESS || event == ProcessEvent.COMPLETE_FAILED) {
                completionObserved.countDown();
              }
              if (accepted && status == ProcessStatus.SUCCESS) {
                successRecorded.countDown();
              }
              return accepted;
            }
          };
      return store;
    }
  }

  private static class GatedEngine extends LocalExecutionEngine {
    final CountDownLatch submitted = new CountDownLatch(1);
    final CountDownLatch releaseSubmit = new CountDownLatch(1);
    final CountDownLatch canceled = new CountDownLatch(1);
    volatile String id;
    final AtomicInteger submissions = new AtomicInteger();
    final AtomicInteger cancelRequests = new AtomicInteger();

    boolean failPolling;
    final AtomicInteger polls = new AtomicInteger();

    @Override
    public ProcessStatus getStatus(String identifier) {
      if (failPolling && polls.incrementAndGet() > 1) {
        throw new IllegalStateException("Status query failed while execution is still running");
      }
      return super.getStatus(identifier);
    }

    String submitDirect(TableProcess process) {
      return super.submitTableProcess(process);
    }

    @Override
    public String submitTableProcess(TableProcess process) {
      submissions.incrementAndGet();
      id = super.submitTableProcess(process);
      submitted.countDown();
      await(releaseSubmit);
      return id;
    }

    @Override
    public ProcessStatus tryCancelTableProcess(TableProcess process, String identifier) {
      cancelRequests.incrementAndGet();
      ProcessStatus status = super.tryCancelTableProcess(process, identifier);
      if (status == ProcessStatus.CANCELED) {
        canceled.countDown();
      }
      return status;
    }
  }

  private static class Work extends TableProcess implements LocalProcess {
    final AtomicInteger runs = new AtomicInteger();
    final CountDownLatch started = new CountDownLatch(1);
    final CountDownLatch finished = new CountDownLatch(1);
    final Runnable action;

    Work(TableRuntime runtime, LocalExecutionEngine engine, Runnable action) {
      super(runtime, engine);
      this.action = action;
    }

    @Override
    public Action getAction() {
      return PaimonActions.EXPIRE_SNAPSHOTS;
    }

    @Override
    public Map<String, String> getProcessParameters() {
      return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getSummary() {
      return Collections.emptyMap();
    }

    @Override
    public void run() {
      runs.incrementAndGet();
      started.countDown();
      try {
        action.run();
      } finally {
        finished.countDown();
      }
    }
  }
}
