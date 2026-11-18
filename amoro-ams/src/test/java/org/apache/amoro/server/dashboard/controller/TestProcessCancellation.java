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

package org.apache.amoro.server.dashboard.controller;

import io.javalin.http.Context;
import org.apache.amoro.Action;
import org.apache.amoro.Constants;
import org.apache.amoro.IcebergActions;
import org.apache.amoro.PaimonActions;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.api.OptimizingService;
import org.apache.amoro.client.OptimizingClientPools;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.process.LocalExecutionEngine;
import org.apache.amoro.process.LocalProcess;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.server.DefaultOptimizingService;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.server.persistence.mapper.CatalogMetaMapper;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.process.ProcessService;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.DefaultTableRuntimeStore;
import org.apache.amoro.server.table.DerbyPersistence;
import org.apache.amoro.server.table.TableManager;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.shade.thrift.org.apache.thrift.TMultiplexedProcessor;
import org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.amoro.shade.thrift.org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.amoro.shade.thrift.org.apache.thrift.server.TThreadPoolServer;
import org.apache.amoro.shade.thrift.org.apache.thrift.transport.TServerSocket;
import org.apache.amoro.shade.thrift.org.apache.thrift.transport.TSocket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

/**
 * Exercise the dashboard controller through real Thrift and the production cancellation service.
 */
class TestProcessCancellation extends PersistentBase {
  private final DerbyPersistence persistence = new DerbyPersistence();
  private final AwaitableEngine engine = new AwaitableEngine();
  private final TableService tables = Mockito.mock(TableService.class);
  private final TableManager tableManager = Mockito.mock(TableManager.class);
  private final Context ctx = Mockito.mock(Context.class);
  private final CountDownLatch entered = new CountDownLatch(1);
  private final CountDownLatch release = new CountDownLatch(1);
  private final CountDownLatch finished = new CountDownLatch(1);
  private final ExecutorService workers = Executors.newFixedThreadPool(2);
  private ServerTableIdentifier identifier;
  private ProcessService processes;
  private DefaultOptimizingService endpoint;
  private TableController controller;
  private TThreadPoolServer server;
  private Thread serverThread;
  private TSocket connection;
  private OptimizingService.Client client;
  private MockedStatic<OptimizingClientPools> clientLookup;
  private long processId;
  private boolean closed;
  private final CountDownLatch completionEntered = new CountDownLatch(1);
  private final CountDownLatch releaseCompletion = new CountDownLatch(1);
  private volatile boolean pauseBeforePersistence;
  private volatile boolean pauseCallback;

  @BeforeEach
  void setup() throws Exception {
    engine.open(Collections.emptyMap());
    ProcessService.ExecuteEngineManager engines =
        Mockito.mock(ProcessService.ExecuteEngineManager.class);
    Mockito.when(engines.installedPlugins()).thenReturn(List.of(engine));
    processes =
        new ProcessService(tables, Collections.emptyList(), engines) {
          @Override
          public TableProcess untrackTableProcessInstance(ServerTableIdentifier table, long id) {
            TableProcess process = super.untrackTableProcessInstance(table, id);
            if (pauseCallback && process != null) {
              awaitCompletionRelease();
            }
            return process;
          }
        };
    processes.getTableHandlerChain().initialize(Collections.emptyList());
    endpoint =
        new DefaultOptimizingService(
            new Configurations(), null, null, tables, processes, null, null);
    TMultiplexedProcessor processor = new TMultiplexedProcessor();
    processor.registerProcessor(
        Constants.THRIFT_OPTIMIZING_SERVICE_NAME, new OptimizingService.Processor<>(endpoint));
    ServerSocket socket = new ServerSocket(0);
    server =
        new TThreadPoolServer(
            new TThreadPoolServer.Args(new TServerSocket(socket))
                .processor(processor)
                .executorService(workers));
    serverThread = new Thread(server::serve, "cancel-regression-thrift");
    serverThread.start();
    await(server::isServing);
    connection = new TSocket("localhost", socket.getLocalPort(), 5000);
    connection.open();
    client =
        new OptimizingService.Client(
            new TMultiplexedProtocol(
                new TBinaryProtocol(connection), Constants.THRIFT_OPTIMIZING_SERVICE_NAME));
    // Replace only client discovery; the controller uses a real transport and service
    // implementation.
    clientLookup = Mockito.mockStatic(OptimizingClientPools.class);
    clientLookup
        .when(() -> OptimizingClientPools.getClient(Mockito.anyString()))
        .thenReturn(client);
    CatalogManager catalogs = Mockito.mock(CatalogManager.class);
    Mockito.when(catalogs.catalogExist("catalog")).thenReturn(true);
    Mockito.when(tableManager.getServerTableIdentifier(Mockito.any()))
        .thenAnswer(call -> identifier);
    Mockito.when(tableManager.getTableProcessMeta(Mockito.anyLong()))
        .thenAnswer(
            call ->
                getAs(
                    TableProcessMapper.class,
                    mapper -> mapper.getProcessMeta(call.getArgument(0))));
    controller = new TableController(catalogs, tableManager, null, new Configurations());
    Mockito.when(ctx.pathParam("catalog")).thenReturn("catalog");
    Mockito.when(ctx.pathParam("db")).thenReturn("db");
    Mockito.when(ctx.pathParam("table")).thenReturn("tbl");
  }

  @AfterEach
  void close() throws Exception {
    close(10, TimeUnit.SECONDS);
  }

  private void close(long timeout, TimeUnit unit) throws Exception {
    if (closed) {
      return;
    }
    release.countDown();
    if (clientLookup != null) {
      clientLookup.close();
      clientLookup = null;
    }
    if (connection != null) {
      connection.close();
    }
    if (server != null) {
      server.stop();
    }
    workers.shutdownNow();
    if (serverThread != null) {
      serverThread.join(5000);
      Assertions.assertFalse(serverThread.isAlive(), "Thrift server did not terminate");
    }
    Assertions.assertTrue(
        workers.awaitTermination(10, TimeUnit.SECONDS), "Thrift workers did not terminate");
    if (endpoint != null) {
      endpoint.dispose();
    }
    if (processes != null) {
      processes.dispose();
    }
    engine.close();
    engine.awaitWorkers();
    if (processes != null) {
      Assertions.assertTrue(
          processes.awaitTermination(timeout, unit),
          "Process callbacks did not terminate; Derby must remain available");
    }
    // A timed-out or interrupted drain leaves persistence intact so teardown can be retried.
    persistence.clearTable(identifier == null ? null : identifier.getId());
    closed = true;
  }

  @Test
  void dashboardCancelsPaimonSnapshotsAndRejectsRepeatedCancellation() throws Exception {
    startCleanup(TableFormat.PAIMON);
    controller.cancelOptimizingProcess(ctx);
    Assertions.assertEquals(ProcessStatus.CANCELED, status());
    Assertions.assertTrue(processes.getTableProcessInstances(identifier).isEmpty());
    Assertions.assertThrows(
        IllegalStateException.class, () -> controller.cancelOptimizingProcess(ctx));
    Mockito.verify(ctx, Mockito.times(1)).json(Mockito.any());
    Assertions.assertFalse(client.cancelProcess(Long.MAX_VALUE));
    close();
    Assertions.assertEquals(1, engine.cancelRequests.get());
  }

  @ParameterizedTest
  @MethodSource("unsupportedManualActions")
  void unsupportedMaintenanceKeepsLegacyCancelResponse(TableFormat format, Action action)
      throws Exception {
    startCleanup(format, action);
    Assertions.assertFalse(client.cancelProcess(processId));
    controller.cancelOptimizingProcess(ctx);
    Mockito.verify(ctx).json(Mockito.any());
    Assertions.assertEquals(ProcessStatus.RUNNING, status());
    Assertions.assertTrue(processes.getTableProcessInstances(identifier).containsKey(processId));
    release.countDown();
    await(() -> status() == ProcessStatus.SUCCESS);
    close();
    Assertions.assertEquals(0, engine.cancelRequests.get());
  }

  static Stream<Arguments> unsupportedManualActions() {
    return Stream.of(
        Arguments.of(TableFormat.ICEBERG, PaimonActions.EXPIRE_SNAPSHOTS),
        Arguments.of(TableFormat.MIXED_ICEBERG, PaimonActions.EXPIRE_SNAPSHOTS),
        Arguments.of(TableFormat.PAIMON, IcebergActions.CLEAN_ORPHAN));
  }

  @Test
  void tableRemovalCancelsPublishedExecutionOnce() throws Exception {
    startCleanup(TableFormat.ICEBERG);
    processes.getTableHandlerChain().fireTableRemoved(tables.getRuntime(identifier.getId()));
    Assertions.assertEquals(ProcessStatus.CANCELED, status());
    close();
    Assertions.assertEquals(1, engine.cancelRequests.get());
  }

  static Stream<TableFormat> formats() {
    return Stream.of(TableFormat.ICEBERG, TableFormat.PAIMON);
  }

  @Test
  void dashboardRejectsCancellationForAnotherTable() throws Exception {
    startCleanup(TableFormat.PAIMON);
    Mockito.when(tableManager.getServerTableIdentifier(Mockito.any()))
        .thenReturn(
            ServerTableIdentifier.of(
                identifier.getId() + 1, "catalog", "db", "other", TableFormat.PAIMON));
    Assertions.assertThrows(
        IllegalStateException.class, () -> controller.cancelOptimizingProcess(ctx));
    Assertions.assertEquals(ProcessStatus.RUNNING, status());
    Mockito.verify(ctx, Mockito.never()).json(Mockito.any());
    Assertions.assertTrue(client.cancelProcess(processId));
  }

  @Test
  void cleanupPreservesOtherCatalogs() throws Exception {
    String catalogName = "cancel-test-unrelated-catalog";
    CatalogMeta catalog = new CatalogMeta(catalogName, "ams", Map.of(), Map.of(), Map.of());
    doAs(CatalogMetaMapper.class, mapper -> mapper.insertCatalog(catalog));
    try {
      startCleanup(TableFormat.ICEBERG);
      close();
      Assertions.assertNotNull(
          getAs(CatalogMetaMapper.class, mapper -> mapper.getCatalog(catalogName)));
      Assertions.assertNull(
          getAs(TableProcessMapper.class, mapper -> mapper.getProcessMeta(processId)));
    } finally {
      doAs(CatalogMetaMapper.class, mapper -> mapper.deleteCatalog(catalogName));
    }
  }

  @Test
  void completedCleanupIsNotReportedAsCanceled() throws Exception {
    startCleanup(TableFormat.PAIMON);
    release.countDown();
    await(() -> status() == ProcessStatus.SUCCESS);
    Assertions.assertThrows(
        IllegalStateException.class, () -> controller.cancelOptimizingProcess(ctx));
    Assertions.assertEquals(ProcessStatus.SUCCESS, status());
    Mockito.verify(ctx, Mockito.never()).json(Mockito.any());
  }

  @ParameterizedTest
  @MethodSource("cleanupPhases")
  void cleanupTimeoutPreservesPersistenceUntilCompletionExits(
      TableFormat format, boolean beforePersistence) throws Exception {
    startCleanup(format);
    pauseBeforePersistence = beforePersistence;
    pauseCallback = !beforePersistence;
    try {
      // Model a failure before the test reaches its explicit cancellation. Hold the real tracer
      // either before its terminal write or in its completion callback, after it was untracked.
      release.countDown();
      Assertions.assertTrue(completionEntered.await(10, TimeUnit.SECONDS));
      AssertionError timeout =
          Assertions.assertThrows(AssertionError.class, () -> close(0, TimeUnit.NANOSECONDS));
      Assertions.assertTrue(timeout.getMessage().contains("Process callbacks did not terminate"));
      Assertions.assertFalse(processes.awaitTermination(0, TimeUnit.NANOSECONDS));
      Assertions.assertEquals(
          beforePersistence ? ProcessStatus.RUNNING : ProcessStatus.SUCCESS, status());
    } finally {
      releaseCompletion.countDown();
      // Retry the same teardown only after the held worker can finish; @AfterEach is idempotent.
      close();
    }
    Assertions.assertTrue(processes.awaitTermination(0, TimeUnit.NANOSECONDS));
    Assertions.assertNull(
        getAs(TableProcessMapper.class, mapper -> mapper.getProcessMeta(processId)));
  }

  static Stream<Arguments> cleanupPhases() {
    return formats()
        .flatMap(format -> Stream.of(Arguments.of(format, true), Arguments.of(format, false)));
  }

  private void awaitCompletionRelease() {
    completionEntered.countDown();
    try {
      Assertions.assertTrue(
          releaseCompletion.await(40, TimeUnit.SECONDS), "Completion gate timed out");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    }
  }

  private void startCleanup(TableFormat format) throws Exception {
    startCleanup(format, PaimonActions.EXPIRE_SNAPSHOTS);
  }

  private void startCleanup(TableFormat format, Action action) throws Exception {
    identifier = ServerTableIdentifier.of("catalog", "db", "tbl", format);
    doAs(TableMetaMapper.class, mapper -> mapper.insertTable(identifier));
    TableRuntimeMeta meta = new TableRuntimeMeta();
    meta.setTableId(identifier.getId());
    meta.setGroupName("default");
    meta.setStatusCode(0);
    DefaultTableRuntimeStore store =
        new DefaultTableRuntimeStore(
            identifier, meta, DefaultTableRuntime.REQUIRED_STATES, Collections.emptyList());
    DefaultTableRuntime runtime = new DefaultTableRuntime(store, () -> null);
    store.setTableRuntime(runtime);
    Mockito.when(tables.getRuntime(identifier.getId())).thenReturn(runtime);
    processes.register(runtime, new BlockingCleanup(runtime, action));
    Assertions.assertTrue(entered.await(10, TimeUnit.SECONDS));
    processId = processes.getTableProcessInstances(identifier).keySet().iterator().next();
    await(() -> status() == ProcessStatus.RUNNING);
    Mockito.when(ctx.pathParam("processId")).thenReturn(String.valueOf(processId));
  }

  private ProcessStatus status() {
    return getAs(TableProcessMapper.class, mapper -> mapper.getProcessMeta(processId)).getStatus();
  }

  private static void await(BooleanSupplier condition) throws Exception {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
    while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
      Thread.sleep(20);
    }
    Assertions.assertTrue(condition.getAsBoolean(), "Timed out waiting for cancellation lifecycle");
  }

  private class BlockingCleanup extends TableProcess implements LocalProcess {
    private final Action action;

    BlockingCleanup(DefaultTableRuntime runtime, Action action) {
      super(runtime, engine);
      this.action = action;
    }

    @Override
    public Action getAction() {
      return action;
    }

    @Override
    public Map<String, String> getProcessParameters() {
      return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getSummary() {
      if (pauseBeforePersistence && finished.getCount() == 0) {
        awaitCompletionRelease();
      }
      return Collections.emptyMap();
    }

    @Override
    public void run() {
      entered.countDown();
      try {
        if (!release.await(20, TimeUnit.SECONDS)) {
          throw new IllegalStateException("Test release timed out");
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      } finally {
        finished.countDown();
      }
    }
  }

  private static class AwaitableEngine extends LocalExecutionEngine {
    private final List<ThreadPoolExecutor> pools = new ArrayList<>();
    private final AtomicInteger cancelRequests = new AtomicInteger();

    @Override
    public ProcessStatus tryCancelTableProcess(TableProcess process, String identifier) {
      cancelRequests.incrementAndGet();
      return super.tryCancelTableProcess(process, identifier);
    }

    @Override
    protected ThreadPoolExecutor newFixedPool(String tag, int size) {
      ThreadPoolExecutor pool = super.newFixedPool(tag, size);
      pools.add(pool);
      return pool;
    }

    void awaitWorkers() throws InterruptedException {
      long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
      for (ThreadPoolExecutor pool : pools) {
        Assertions.assertTrue(
            pool.awaitTermination(Math.max(0, deadline - System.nanoTime()), TimeUnit.NANOSECONDS),
            "Local workers did not terminate; Derby must remain available");
      }
    }
  }
}
