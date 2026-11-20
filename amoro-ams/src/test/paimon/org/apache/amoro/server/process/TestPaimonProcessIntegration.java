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
import org.apache.amoro.formats.paimon.PaimonCatalog;
import org.apache.amoro.formats.paimon.PaimonCatalogFactory;
import org.apache.amoro.formats.paimon.process.PaimonProcessFactory;
import org.apache.amoro.formats.paimon.process.PaimonSnapshotsExpiringProcess;
import org.apache.amoro.process.ActionCoordinator;
import org.apache.amoro.process.LocalExecutionEngine;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.server.DefaultOptimizingService;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.catalog.ServerCatalog;
import org.apache.amoro.server.dashboard.ServerTableDescriptor;
import org.apache.amoro.server.manager.AbstractPluginManager;
import org.apache.amoro.server.manager.PluginConfiguration;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.table.DefaultActionCoordinator;
import org.apache.amoro.server.table.DefaultTableRuntimeFactory;
import org.apache.amoro.server.table.DefaultTableRuntimeStore;
import org.apache.amoro.server.table.DerbyPersistence;
import org.apache.amoro.server.table.TableManager;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.shade.guava32.com.google.common.util.concurrent.Uninterruptibles;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.amoro.table.TableRuntimeFactory;
import org.apache.amoro.table.descriptor.OptimizingProcessInfo;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.data.GenericRow;
import org.apache.paimon.data.InternalRow;
import org.apache.paimon.schema.Schema;
import org.apache.paimon.schema.SchemaChange;
import org.apache.paimon.table.FileStoreTable;
import org.apache.paimon.table.sink.BatchTableCommit;
import org.apache.paimon.table.sink.BatchTableWrite;
import org.apache.paimon.table.sink.BatchWriteBuilder;
import org.apache.paimon.table.source.ReadBuilder;
import org.apache.paimon.types.DataTypes;
import org.apache.paimon.utils.CloseableIterator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class TestPaimonProcessIntegration extends PersistentBase {
  private Path warehouse;
  private final DerbyPersistence persistence = new DerbyPersistence();
  private final ServerTableIdentifier identifier =
      ServerTableIdentifier.of("catalog", "db", "tbl", TableFormat.PAIMON);
  private final Clock clock = Mockito.mock(Clock.class);
  private final ObservedFactory factory = new ObservedFactory(clock);
  private final AwaitableEngine engine = new AwaitableEngine();
  private Catalog nativeCatalog;
  private PaimonCatalog catalog;
  private TableRuntime runtime;
  private AwaitableService service;
  private ServerTableDescriptor descriptor;
  private CountDownLatch loadEntered;
  private CountDownLatch releaseLoad;
  private boolean gateLocalLoad;
  private TableService scheduledTables;
  private CountDownLatch nativeReadEntered;
  private CountDownLatch releaseNativeRead;
  private boolean closed;
  private Duration schedulerWaitTimeout = Duration.ofSeconds(40);
  private ScheduledExecutorService continuousWriter;
  private ScheduledFuture<?> continuousWrites;
  private final CompletableFuture<Void> resourcesCleared = new CompletableFuture<>();

  @BeforeEach
  void setup() throws Exception {
    // JUnit TempDir cleanup runs even after a failed teardown, while workers may still be alive.
    warehouse = Files.createTempDirectory("amoro-paimon-");
    Mockito.when(clock.millis()).thenAnswer(call -> System.currentTimeMillis());
    nativeCatalog =
        PaimonCatalogFactory.paimonCatalog(
            Map.of("warehouse", warehouse.toString()), new Configuration());
    nativeCatalog.createDatabase("db", false);
    nativeCatalog.createTable(
        Identifier.create("db", "tbl"),
        Schema.newBuilder().column("id", DataTypes.INT()).option("write-only", "true").build(),
        false);
    catalog = new PaimonCatalog(nativeCatalog, "catalog");
    doAs(TableMetaMapper.class, mapper -> mapper.insertTable(identifier));
    engine.open(Map.of("pool.paimon-expire-snapshots.thread-count", "1"));
    factory.open(Map.of("expire-snapshots.interval", "1h"));
    factory.availableExecuteEngines(List.of(engine));
    DefaultTableRuntimeFactory runtimeFactory = new DefaultTableRuntimeFactory();
    runtimeFactory.initialize(List.of(factory));
    runtimeFactory.withTableLoader(
        id -> {
          if (releaseLoad != null
              && (!gateLocalLoad || Thread.currentThread().getName().startsWith("local-"))) {
            loadEntered.countDown();
            try {
              if (!releaseLoad.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for test load gate");
              }
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              throw new IllegalStateException(e);
            }
          }
          // The regression owns this gate and releases it in finally; it models slow native IO.
          if (nativeReadEntered != null && Thread.currentThread().getName().startsWith("local-")) {
            nativeReadEntered.countDown();
            awaitGate(releaseNativeRead);
          }
          return catalog.loadTable(id.getDatabase(), id.getTableName());
        });
    TableRuntimeFactory.TableRuntimeCreator creator =
        runtimeFactory.accept(identifier, Map.of()).orElseThrow();
    TableRuntimeMeta meta = new TableRuntimeMeta();
    meta.setTableId(identifier.getId());
    meta.setGroupName("default");
    meta.setStatusCode(0);
    DefaultTableRuntimeStore store =
        new DefaultTableRuntimeStore(
            identifier, meta, creator.requiredStateKeys(), Collections.emptyList());
    runtime = creator.create(store);
    store.setTableRuntime(runtime);
    ProcessService.ExecuteEngineManager engines =
        Mockito.mock(ProcessService.ExecuteEngineManager.class);
    Mockito.when(engines.installedPlugins()).thenReturn(List.of(engine));
    service =
        new AwaitableService(
            Mockito.mock(TableService.class), runtimeFactory.supportedCoordinators(), engines);
    // Initialize lifecycle without scheduling automatic checks over test data.
    service.getTableHandlerChain().initialize(Collections.emptyList());
    CatalogManager manager = Mockito.mock(CatalogManager.class);
    ServerCatalog serverCatalog = Mockito.mock(ServerCatalog.class);
    Mockito.when(manager.getServerCatalog("catalog")).thenReturn(serverCatalog);
    Mockito.when(serverCatalog.loadTable("db", "tbl"))
        .thenAnswer(call -> catalog.loadTable("db", "tbl"));
    descriptor =
        new ServerTableDescriptor(manager, Mockito.mock(TableManager.class), new Configurations());
  }

  @AfterEach
  synchronized void close() throws Exception {
    if (closed) {
      return;
    }
    if (scheduledTables != null) {
      Mockito.when(scheduledTables.contains(identifier.getId())).thenReturn(false);
    }
    factory.stopObserving();
    if (service != null) {
      service
          .getActionCoordinators()
          .values()
          .forEach(schedulers -> schedulers.forEach(ActionCoordinatorScheduler::dispose));
    }
    stopContinuousWriter();
    if (releaseLoad != null) {
      releaseLoad.countDown();
    }
    factory.awaitSchedulerWorkers(schedulerWaitTimeout);
    if (service != null) {
      // Keep the executor accepting bounded failure retries until its completion callback untracks.
      await(() -> service.getTableProcessInstances(identifier).isEmpty());
      service.dispose();
      Assertions.assertTrue(
          service.awaitTermination(40, TimeUnit.SECONDS),
          "Process callbacks did not terminate; catalog and Derby must remain available");
    }
    engine.close();
    // Cancellation completes the engine Future before an already-running body necessarily exits.
    engine.awaitWorkers();
    if (nativeCatalog != null) {
      nativeCatalog.close();
    }
    persistence.clearTable(identifier.getId());
    if (warehouse != null) {
      FileUtils.deleteDirectory(warehouse.toFile());
    }
    closed = true;
    resourcesCleared.complete(null);
  }

  @Test
  void realFrameworkPersistsPaimonResultsAndServesCleanupQueries() throws Exception {
    service.register(
        runtime, factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).orElseThrow());
    await(() -> rows().size() == 1 && rows().get(0).getStatus() == ProcessStatus.SUCCESS);
    OptimizingProcessInfo info = rows().get(0);
    Assertions.assertTrue(info.isAmsCancellationSupported());
    Assertions.assertTrue(
        new ObjectMapper().valueToTree(info).get("amsCancellationSupported").asBoolean());
    Assertions.assertEquals("noop", info.getSummary().get("outcome"));
    Assertions.assertEquals("0", info.getSummary().get("reported-expired-snapshots"));
    Assertions.assertTrue(runtime.getState(PaimonProcessFactory.LAST_SUCCESS_TIME) > 0);
    Assertions.assertEquals(
        Map.of(PaimonActions.EXPIRE_SNAPSHOTS.getName(), "Expire Snapshots"),
        descriptor.getTableProcessTypes(
            identifier.getIdentifier().buildTableIdentifier(), "CLEANUP"));
    Assertions.assertEquals(0, info.getTotalTasks());
    Assertions.assertNotNull(info.getInputFiles());
    await(() -> service.getTableProcessInstances(identifier).isEmpty());
  }

  @Test
  void independentChangelogTableExpiresThroughProcessService() throws Exception {
    nativeCatalog.alterTable(
        Identifier.create("db", "tbl"),
        List.of(
            SchemaChange.setOption("snapshot.num-retained.min", "1"),
            SchemaChange.setOption("snapshot.num-retained.max", "2"),
            SchemaChange.setOption("snapshot.time-retained", "100 d"),
            SchemaChange.setOption("changelog.num-retained.max", "5")),
        false);
    FileStoreTable table = (FileStoreTable) nativeCatalog.getTable(Identifier.create("db", "tbl"));
    for (int id = 1; id <= 8; id++) {
      append(table, id);
    }
    service.register(
        runtime, factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).orElseThrow());
    await(() -> rows().size() == 1 && rows().get(0).getStatus() == ProcessStatus.SUCCESS);
    OptimizingProcessInfo info = rows().get(0);
    Assertions.assertEquals("executed", info.getSummary().get("outcome"));
    Assertions.assertEquals("6", info.getSummary().get("reported-expired-snapshots"));
    Assertions.assertEquals("5", info.getSummary().get("changelog.num-retained.max"));
    Assertions.assertEquals(7L, table.snapshotManager().earliestSnapshotId());
    Assertions.assertEquals(6, table.changelogManager().safelyGetAllChangelogs().size());
    Assertions.assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8), readIds(table));
    Assertions.assertTrue(runtime.getState(PaimonProcessFactory.LAST_SUCCESS_TIME) > 0);
    await(() -> service.getTableProcessInstances(identifier).isEmpty());
  }

  @Test
  void failureSummaryIsPersistedAndRetriesAreBounded() throws Exception {
    nativeCatalog.alterTable(
        Identifier.create("db", "tbl"),
        SchemaChange.setOption("snapshot.expire.limit", "0"),
        false);
    service.register(
        runtime, factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).orElseThrow());
    await(() -> !rows().isEmpty() && service.getTableProcessInstances(identifier).isEmpty());
    Assertions.assertEquals(1, rows().size());
    OptimizingProcessInfo info = rows().get(0);
    Assertions.assertEquals(ProcessStatus.FAILED, info.getStatus());
    Assertions.assertEquals("config", info.getSummary().get("error-stage"));
    Assertions.assertNotNull(info.getSummary().get("error-message"));
    Assertions.assertFalse(info.getSummary().containsKey("outcome"));
    Assertions.assertEquals(
        3,
        getAs(
                TableProcessMapper.class,
                mapper -> mapper.getProcessMeta(Long.parseLong(info.getProcessId())))
            .getRetryNumber());
    Assertions.assertEquals(0L, runtime.getState(PaimonProcessFactory.LAST_SUCCESS_TIME));
    Assertions.assertTrue(factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).isPresent());
  }

  @Test
  void recoveredLostLocalTaskRunsWithCurrentSettings() throws Exception {
    doAs(
        TableProcessMapper.class,
        mapper ->
            mapper.insertProcess(
                identifier.getId(),
                42L,
                "lost-local-process",
                ProcessStatus.RUNNING,
                PaimonActions.EXPIRE_SNAPSHOTS.getName(),
                "default",
                "local",
                0,
                System.currentTimeMillis(),
                Map.of(),
                Map.of()));
    nativeCatalog.alterTable(
        Identifier.create("db", "tbl"),
        SchemaChange.setOption("amoro.expire-snapshots.enabled", "false"),
        false);
    service.recoverProcesses(List.of(runtime));
    await(() -> rows().get(0).getStatus() == ProcessStatus.SUCCESS);
    Assertions.assertEquals("42", rows().get(0).getProcessId());
    Assertions.assertEquals("skipped", rows().get(0).getSummary().get("outcome"));
    Assertions.assertEquals(0L, runtime.getState(PaimonProcessFactory.LAST_SUCCESS_TIME));
    await(() -> service.getTableProcessInstances(identifier).isEmpty());
  }

  @Test
  void pluginDefaultsDoNotEnableMaintenanceAndExplicitConfigAdmitsRuntime() throws Exception {
    for (List<PluginConfiguration> configs :
        List.of(
            Collections.<PluginConfiguration>emptyList(),
            List.of(
                new PluginConfiguration(
                    "paimon", false, 100, Map.of("expire-snapshots.interval", "1h"))),
            List.of(PluginConfiguration.emptyConfig("paimon")),
            List.of(
                new PluginConfiguration(
                    "paimon",
                    true,
                    100,
                    Map.of(
                        "expire-snapshots.enabled",
                        "false",
                        "expire-snapshots.interval",
                        "1h"))))) {
      try (Plugins manager = new Plugins(configs)) {
        manager.initialize();
        DefaultTableRuntimeFactory runtimes = new DefaultTableRuntimeFactory();
        runtimes.initialize(manager.installedPlugins());
        Assertions.assertTrue(runtimes.accept(identifier, Map.of()).isEmpty());
      }
    }
    PluginConfiguration enabled =
        PluginConfiguration.fromJSONObject(
            new ObjectMapper()
                .readTree(
                    "{\"name\":\"paimon\",\"properties\":{\"expire-snapshots.interval\":\"1h\"}}"));
    try (Plugins manager = new Plugins(List.of(enabled))) {
      manager.initialize();
      DefaultTableRuntimeFactory runtimes = new DefaultTableRuntimeFactory();
      runtimes.initialize(manager.installedPlugins());
      Assertions.assertTrue(runtimes.accept(identifier, Map.of()).isPresent());
      Assertions.assertEquals(1, runtimes.supportedCoordinators().size());
    }
  }

  @Test
  void cancelServiceEntryCancelsPaimonCleanup() throws Exception {
    TableProcess process = factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).orElseThrow();
    loadEntered = new CountDownLatch(1);
    releaseLoad = new CountDownLatch(1);
    TableService tables = Mockito.mock(TableService.class);
    Mockito.when(tables.getRuntime(identifier.getId())).thenReturn(runtime);
    DefaultOptimizingService endpoint =
        new DefaultOptimizingService(new Configurations(), null, null, tables, service, null, null);
    try {
      service.register(runtime, process);
      Assertions.assertTrue(loadEntered.await(10, TimeUnit.SECONDS));
      await(() -> rows().size() == 1 && rows().get(0).getStatus() == ProcessStatus.RUNNING);
      long processId = Long.parseLong(rows().get(0).getProcessId());
      Assertions.assertTrue(endpoint.cancelProcess(processId));
      Assertions.assertEquals(ProcessStatus.CANCELED, rows().get(0).getStatus());
      Assertions.assertTrue(service.getTableProcessInstances(identifier).isEmpty());
      Assertions.assertFalse(endpoint.cancelProcess(processId));
      Assertions.assertFalse(endpoint.cancelProcess(Long.MAX_VALUE));
    } finally {
      releaseLoad.countDown();
      await(() -> process.getSummary().containsKey("outcome"));
      endpoint.dispose();
    }
  }

  @Test
  void sameActionIsNotSubmittedTwiceAndTableRemovalCancelsTrackedProcess() throws Exception {
    TableProcess process = factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).orElseThrow();
    loadEntered = new CountDownLatch(1);
    releaseLoad = new CountDownLatch(1);
    service.register(runtime, process);
    Assertions.assertTrue(loadEntered.await(10, TimeUnit.SECONDS));
    service.register(runtime, new PaimonSnapshotsExpiringProcess(runtime, engine));
    await(() -> rows().size() == 1 && rows().get(0).getStatus() == ProcessStatus.RUNNING);
    service.getTableHandlerChain().fireTableRemoved(runtime);
    Assertions.assertEquals(ProcessStatus.CANCELED, rows().get(0).getStatus());
    Assertions.assertTrue(service.getTableProcessInstances(identifier).isEmpty());
    releaseLoad.countDown();
    // Cancellation is not a rollback or proof that the underlying invocation stopped.
    await(() -> process.getSummary().containsKey("outcome"));
    Assertions.assertEquals(ProcessStatus.CANCELED, rows().get(0).getStatus());
  }

  @Test
  void periodicSchedulerWaitsForStartupAndSuccessInterval() throws Exception {
    loadEntered = new CountDownLatch(1);
    releaseLoad = new CountDownLatch(1);
    gateLocalLoad = true;
    factory.controlChecks = true;
    long started = startPeriodicScheduling(1_000);

    Check first = factory.nextCheck();
    Assertions.assertTrue(first.started - started >= Duration.ofSeconds(11).toNanos());
    Assertions.assertTrue(first.proceed().isPresent());
    Assertions.assertTrue(loadEntered.await(10, TimeUnit.SECONDS));

    // The real scheduler continues checking while the first local invocation is blocked.
    Check second = factory.nextCheck();
    Assertions.assertTrue(second.started - first.finished >= Duration.ofSeconds(1).toNanos());
    Assertions.assertTrue(second.proceed().isPresent());
    Check third = factory.nextCheck();
    Assertions.assertEquals(1, rows().size(), "An in-flight action must not be registered twice");
    Assertions.assertEquals(0L, runtime.getState(PaimonProcessFactory.LAST_SUCCESS_TIME));
    releaseLoad.countDown();
    await(() -> runtime.getState(PaimonProcessFactory.LAST_SUCCESS_TIME) > 0);
    long successTime = runtime.getState(PaimonProcessFactory.LAST_SUCCESS_TIME);
    Mockito.when(clock.millis()).thenReturn(successTime + 999);
    Assertions.assertTrue(third.proceed().isEmpty(), "A check just after success must be skipped");

    // Another real scheduler interval can pass while the injected time remains below the boundary.
    Check fourth = factory.nextCheck();
    Assertions.assertTrue(fourth.started - third.finished >= Duration.ofSeconds(1).toNanos());
    Assertions.assertTrue(fourth.proceed().isEmpty());

    // Completion tracing has its own polling interval. Let it finish before the next check.
    await(() -> rows().get(0).getStatus() == ProcessStatus.SUCCESS);
    await(() -> service.getTableProcessInstances(identifier).isEmpty());
    Mockito.when(clock.millis()).thenReturn(successTime + 1_000);
    Check fifth = factory.nextCheck();
    Assertions.assertTrue(fifth.proceed().isPresent());
    await(
        () ->
            rows().size() == 2
                && rows().stream().allMatch(r -> r.getStatus() == ProcessStatus.SUCCESS));
  }

  @ParameterizedTest
  @CsvSource({"100,8", "300,12"})
  void periodicExpirationCatchesUpWhileWritesContinue(long intervalMillis, int expireLimit)
      throws Exception {
    nativeCatalog.alterTable(
        Identifier.create("db", "tbl"),
        List.of(
            SchemaChange.setOption("snapshot.num-retained.min", "2"),
            SchemaChange.setOption("snapshot.num-retained.max", "2"),
            SchemaChange.setOption("snapshot.time-retained", "100 d"),
            SchemaChange.setOption("snapshot.expire.limit", String.valueOf(expireLimit))),
        false);
    FileStoreTable table = (FileStoreTable) nativeCatalog.getTable(Identifier.create("db", "tbl"));
    append(table, 1);
    table.createTag("first", 1);
    for (int id = 2; id <= 24; id++) {
      append(table, id);
    }
    Assertions.assertEquals(22, eligibleSnapshots(table));
    startPeriodicScheduling(intervalMillis);
    factory.nextCheck();

    AtomicInteger committed = new AtomicInteger(24);
    AtomicReference<Throwable> writeFailure = new AtomicReference<>();
    continuousWriter = Executors.newSingleThreadScheduledExecutor();
    continuousWrites =
        continuousWriter.scheduleWithFixedDelay(
            () -> {
              try {
                append(table, committed.get() + 1);
                committed.incrementAndGet();
              } catch (Throwable e) {
                writeFailure.set(e);
                throw new IllegalStateException("Continuous test writer failed", e);
              }
            },
            0,
            1,
            TimeUnit.SECONDS);
    try {
      // At most one new snapshot per second. Allow for the framework's 5-second status poll,
      // not just the much shorter coordinator interval, when choosing expiration capacity.
      await(
          () -> {
            Assertions.assertNull(writeFailure.get(), "Writer must remain healthy");
            return committed.get() >= 27 && eligibleSnapshots(table) == 0;
          },
          Duration.ofSeconds(90));
      int firstCatchUp = committed.get();
      // Catch up again after further writes: draining only the initial static backlog is not
      // enough.
      await(
          () -> {
            Assertions.assertNull(writeFailure.get(), "Writer must remain healthy");
            return committed.get() >= firstCatchUp + 3 && eligibleSnapshots(table) == 0;
          },
          Duration.ofSeconds(90));
      Assertions.assertFalse(
          continuousWrites.isDone(), "Convergence must happen before the writer stops");
    } finally {
      stopContinuousWriter();
    }
    Assertions.assertNull(writeFailure.get());
    Mockito.when(scheduledTables.contains(identifier.getId())).thenReturn(false);
    await(() -> service.getTableProcessInstances(identifier).isEmpty());
    Assertions.assertTrue(
        table.snapshotManager().snapshotExists(table.snapshotManager().latestSnapshotId() - 1));
    List<OptimizingProcessInfo> completed = rows();
    Assertions.assertTrue(
        completed.size() >= 3, "Convergence must span multiple periodic processes");
    for (OptimizingProcessInfo process : completed) {
      Assertions.assertEquals(ProcessStatus.SUCCESS, process.getStatus());
      int expired = Integer.parseInt(process.getSummary().get("reported-expired-snapshots"));
      Assertions.assertTrue(expired >= 0 && expired <= expireLimit);
      Assertions.assertEquals(
          String.valueOf(expireLimit), process.getSummary().get("snapshot.expire.limit"));
    }
    Assertions.assertEquals(
        IntStream.rangeClosed(1, committed.get()).boxed().collect(Collectors.toList()),
        readIds(table));
    Assertions.assertEquals(List.of(1), readIds(table.copy(Map.of("scan.tag-name", "first"))));
  }

  private void stopContinuousWriter() throws InterruptedException {
    if (continuousWriter == null) {
      return;
    }
    if (continuousWrites != null) {
      continuousWrites.cancel(false);
    }
    continuousWriter.shutdown();
    try {
      if (!continuousWriter.awaitTermination(15, TimeUnit.SECONDS)) {
        continuousWriter.shutdownNow();
        Assertions.assertTrue(
            continuousWriter.awaitTermination(40, TimeUnit.SECONDS),
            "Continuous writer did not terminate; its table must remain available");
      }
    } catch (InterruptedException e) {
      continuousWriter.shutdownNow();
      Thread.currentThread().interrupt();
      throw e;
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void cleanupAfterFailureWaitsForNativeWorkAndCallbacks(boolean nativeStillRunning)
      throws Exception {
    ExecutorService closer = Executors.newSingleThreadExecutor();
    Future<Void> cleanup = null;
    try {
      if (nativeStillRunning) {
        nativeReadEntered = new CountDownLatch(1);
        releaseNativeRead = new CountDownLatch(1);
      } else {
        service.pauseCallback = true;
      }
      service.register(
          runtime, factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).orElseThrow());
      if (nativeStillRunning) {
        Assertions.assertTrue(nativeReadEntered.await(10, TimeUnit.SECONDS));
        await(() -> rows().size() == 1 && rows().get(0).getStatus() == ProcessStatus.RUNNING);
        Assertions.assertTrue(
            service.cancelProcess(identifier, Long.parseLong(rows().get(0).getProcessId())));
      } else {
        Assertions.assertTrue(service.callbackEntered.await(10, TimeUnit.SECONDS));
      }
      // Both paths are already untracked; neither proves the native worker/callback has returned.
      Assertions.assertTrue(service.getTableProcessInstances(identifier).isEmpty());
      cleanup =
          closer.submit(
              (Callable<Void>)
                  () -> {
                    try {
                      throw new AssertionError("simulated test abort");
                    } finally {
                      close();
                    }
                  });
      CompletableFuture<Void> draining = nativeStillRunning ? engine.draining : service.draining;
      CompletableFuture.anyOf(draining, resourcesCleared).get(30, TimeUnit.SECONDS);
      Assertions.assertFalse(resourcesCleared.isDone(), "Resources cleared before workers exited");
      Assertions.assertTrue(draining.isDone());
      Assertions.assertFalse(cleanup.isDone());
      Assertions.assertEquals(1, rows().size(), "Derby must remain usable while work is pending");
    } finally {
      if (releaseNativeRead != null) {
        releaseNativeRead.countDown();
      }
      service.releaseCallback.countDown();
      try {
        if (cleanup != null) {
          Future<Void> result = cleanup;
          ExecutionException failure =
              Assertions.assertThrows(
                  ExecutionException.class, () -> result.get(40, TimeUnit.SECONDS));
          Assertions.assertInstanceOf(AssertionError.class, failure.getCause());
          Assertions.assertEquals("simulated test abort", failure.getCause().getMessage());
          // Keep worker cleanup independent of the assertions above.
          Assertions.assertTrue(service.awaitTermination(40, TimeUnit.SECONDS));
          engine.awaitWorkers();
        }
      } finally {
        closer.shutdownNow();
        Assertions.assertTrue(closer.awaitTermination(40, TimeUnit.SECONDS));
      }
    }
    Assertions.assertTrue(resourcesCleared.isDone());
  }

  @Test
  void cleanupTimeoutPreservesResourcesUntilSchedulerExits() throws Exception {
    CountDownLatch entered = new CountDownLatch(1);
    CountDownLatch release = new CountDownLatch(1);
    AtomicReference<Thread> schedulerThread = new AtomicReference<>();
    catalog = Mockito.spy(catalog);
    Mockito.doAnswer(
            call -> {
              if (Thread.currentThread().getName().startsWith("async-")) {
                schedulerThread.set(Thread.currentThread());
                entered.countDown();
                // Model catalog IO that cannot respond to cancellation until the read completes.
                boolean released = false;
                while (!released) {
                  try {
                    release.await();
                    released = true;
                  } catch (InterruptedException ignored) {
                    // The regression releases the external operation in finally.
                  }
                }
                throw new IllegalStateException("Test catalog read released");
              }
              return call.callRealMethod();
            })
        .when(catalog)
        .loadTable("db", "tbl");
    try {
      startPeriodicScheduling(100);
      Assertions.assertTrue(entered.await(20, TimeUnit.SECONDS));
      // Keep the production scheduler's real 30s + 30s stop path. Only the fixture's
      // additional wait is zero, so the still-blocked worker deterministically times out.
      schedulerWaitTimeout = Duration.ZERO;
      AssertionError failure = Assertions.assertThrows(AssertionError.class, this::close);
      Assertions.assertTrue(failure.getMessage().contains("Scheduler worker did not terminate"));
      Assertions.assertFalse(resourcesCleared.isDone());
      Assertions.assertTrue(schedulerThread.get().isAlive());
      Assertions.assertNotNull(nativeCatalog.getTable(Identifier.create("db", "tbl")));
      Assertions.assertEquals(
          identifier,
          getAs(
              TableMetaMapper.class,
              mapper ->
                  mapper.selectTableIdentifier(
                      identifier.getCatalog(),
                      identifier.getDatabase(),
                      identifier.getTableName())));
    } finally {
      release.countDown();
      if (schedulerThread.get() != null) {
        schedulerThread.get().join(40_000);
        Assertions.assertFalse(schedulerThread.get().isAlive());
      }
      schedulerWaitTimeout = Duration.ofSeconds(40);
      close();
    }
    Assertions.assertTrue(resourcesCleared.isDone());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void junitTeardownOwnsWarehouseLifetime(boolean schedulerBlocked) throws Exception {
    // Release this fixture's table name before the nested fixture registers its own.
    close();
    PaimonWarehouseLifecycle.instance = null;
    SummaryGeneratingListener listener = new SummaryGeneratingListener();
    try {
      // Run a real Jupiter lifecycle. Expected inner failures are inspected here, not swallowed
      // by a test extension before JUnit gets a chance to clean its method-scoped resources.
      LauncherFactory.create()
          .execute(
              LauncherDiscoveryRequestBuilder.request()
                  .selectors(
                      DiscoverySelectors.selectMethod(
                          PaimonWarehouseLifecycle.class,
                          schedulerBlocked ? "schedulerTimeout" : "bodyFailure"))
                  .configurationParameter("junit.jupiter.execution.parallel.enabled", "false")
                  .build(),
              listener);
      Assertions.assertEquals(1, listener.getSummary().getTestsFoundCount());
      Assertions.assertEquals(1, listener.getSummary().getTestsFailedCount());
      Assertions.assertEquals(1, listener.getSummary().getFailures().size());
      String message = listener.getSummary().getFailures().get(0).getException().getMessage();
      Assertions.assertTrue(
          message.contains(
              schedulerBlocked ? "Scheduler worker did not terminate" : "simulated test failure"),
          message);
      PaimonWarehouseLifecycle inner = PaimonWarehouseLifecycle.instance;
      Assertions.assertNotNull(inner);
      TestPaimonProcessIntegration fixture = inner;
      if (schedulerBlocked) {
        Assertions.assertTrue(inner.worker.get().isAlive());
        Assertions.assertFalse(fixture.resourcesCleared.isDone());
        Assertions.assertTrue(
            Files.exists(fixture.warehouse),
            "JUnit removed warehouse while work was still running");
        Assertions.assertNotNull(fixture.nativeCatalog.getTable(Identifier.create("db", "tbl")));
      } else {
        Assertions.assertTrue(fixture.resourcesCleared.isDone());
        Assertions.assertFalse(Files.exists(fixture.warehouse));
      }
    } finally {
      PaimonWarehouseLifecycle inner = PaimonWarehouseLifecycle.instance;
      if (inner != null) {
        inner.release.countDown();
        if (inner.worker.get() != null) {
          inner.worker.get().join(40_000);
          Assertions.assertFalse(inner.worker.get().isAlive());
        }
        TestPaimonProcessIntegration fixture = inner;
        fixture.schedulerWaitTimeout = Duration.ofSeconds(40);
        fixture.close();
        Assertions.assertFalse(Files.exists(fixture.warehouse));
      }
      PaimonWarehouseLifecycle.instance = null;
    }
  }

  void startSchedulerWithBlockedRead(CountDownLatch release, AtomicReference<Thread> worker)
      throws Exception {
    CountDownLatch entered = new CountDownLatch(1);
    catalog = Mockito.spy(catalog);
    Mockito.doAnswer(
            call -> {
              if (Thread.currentThread().getName().startsWith("async-")) {
                worker.set(Thread.currentThread());
                entered.countDown();
                Assertions.assertTrue(
                    Uninterruptibles.awaitUninterruptibly(release, 3, TimeUnit.MINUTES),
                    "External read gate timed out");
                throw new IllegalStateException("Test catalog read released");
              }
              return call.callRealMethod();
            })
        .when(catalog)
        .loadTable("db", "tbl");
    startPeriodicScheduling(100);
    Assertions.assertTrue(entered.await(20, TimeUnit.SECONDS));
    schedulerWaitTimeout = Duration.ZERO;
  }

  private static void awaitGate(CountDownLatch gate) {
    try {
      Assertions.assertTrue(gate.await(40, TimeUnit.SECONDS), "Test gate timed out");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    }
  }

  private static class AwaitableEngine extends LocalExecutionEngine {
    private final List<ThreadPoolExecutor> workers = new ArrayList<>();
    private final CompletableFuture<Void> draining = new CompletableFuture<>();

    @Override
    protected ThreadPoolExecutor newFixedPool(String tag, int size) {
      ThreadPoolExecutor pool = super.newFixedPool(tag, size);
      workers.add(pool);
      return pool;
    }

    void awaitWorkers() throws InterruptedException {
      draining.complete(null);
      long deadline = System.nanoTime() + Duration.ofSeconds(40).toNanos();
      for (ThreadPoolExecutor worker : workers) {
        Assertions.assertTrue(
            worker.awaitTermination(
                Math.max(0, deadline - System.nanoTime()), TimeUnit.NANOSECONDS),
            "Local worker did not terminate; catalog and Derby must remain available");
      }
    }
  }

  private static class AwaitableService extends ProcessService {
    private final CompletableFuture<Void> draining = new CompletableFuture<>();
    private final CountDownLatch callbackEntered = new CountDownLatch(1);
    private final CountDownLatch releaseCallback = new CountDownLatch(1);
    private volatile boolean pauseCallback;

    AwaitableService(
        TableService tables, List<ActionCoordinator> coordinators, ExecuteEngineManager engines) {
      super(tables, coordinators, engines);
    }

    @Override
    public TableProcess untrackTableProcessInstance(ServerTableIdentifier table, long processId) {
      TableProcess process = super.untrackTableProcessInstance(table, processId);
      if (pauseCallback && process != null) {
        callbackEntered.countDown();
        awaitGate(releaseCallback);
      }
      return process;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
      draining.complete(null);
      return super.awaitTermination(timeout, unit);
    }
  }

  private static long eligibleSnapshots(FileStoreTable table) {
    // This fixture has no consumer and retains exactly the latest two snapshots. A tag protects
    // its referenced data separately; it does not turn those two retained snapshots into backlog.
    // Read the oldest boundary first so concurrent commits/expiration can only overestimate lag.
    long earliest = table.snapshotManager().earliestSnapshotId();
    return Math.max(0, table.snapshotManager().latestSnapshotId() - earliest - 1);
  }

  private static void append(FileStoreTable table, int id) throws Exception {
    BatchWriteBuilder builder = table.newBatchWriteBuilder();
    try (BatchTableWrite write = builder.newWrite();
        BatchTableCommit commit = builder.newCommit()) {
      write.write(GenericRow.of(id));
      commit.commit(write.prepareCommit());
    }
  }

  private static List<Integer> readIds(org.apache.paimon.table.Table table) throws Exception {
    ReadBuilder builder = table.newReadBuilder();
    List<Integer> ids = new ArrayList<>();
    try (CloseableIterator<InternalRow> rows =
        builder.newRead().createReader(builder.newScan().plan()).toCloseableIterator()) {
      while (rows.hasNext()) {
        ids.add(rows.next().getInt(0));
      }
    }
    Collections.sort(ids);
    return ids;
  }

  private long startPeriodicScheduling(long intervalMillis) throws InterruptedException {
    service
        .getActionCoordinators()
        .values()
        .forEach(schedulers -> schedulers.forEach(ActionCoordinatorScheduler::dispose));
    service.dispose();
    Assertions.assertTrue(service.awaitTermination(40, TimeUnit.SECONDS));
    factory.open(Map.of("expire-snapshots.interval", intervalMillis + "ms"));
    scheduledTables = Mockito.mock(TableService.class);
    Mockito.when(scheduledTables.contains(identifier.getId())).thenReturn(true);
    ProcessService.ExecuteEngineManager engines =
        Mockito.mock(ProcessService.ExecuteEngineManager.class);
    Mockito.when(engines.installedPlugins()).thenReturn(List.of(engine));
    service =
        new AwaitableService(
            scheduledTables,
            List.of(
                new DefaultActionCoordinator(
                    TableFormat.PAIMON, PaimonActions.EXPIRE_SNAPSHOTS, factory)),
            engines);
    factory.observing = true;
    // Use the production startup delay and recurring scheduler, not direct trigger/register calls.
    long started = System.nanoTime();
    service.getTableHandlerChain().initialize(List.of(runtime));
    return started;
  }

  private static class Check {
    private final long started = System.nanoTime();
    private long finished;
    private final CountDownLatch permit = new CountDownLatch(1);
    private final CompletableFuture<Optional<TableProcess>> result = new CompletableFuture<>();

    Optional<TableProcess> proceed() throws Exception {
      permit.countDown();
      return result.get(10, TimeUnit.SECONDS);
    }
  }

  /** Observe the factory SPI without replacing scheduling, admission or expiration behavior. */
  private static class ObservedFactory extends PaimonProcessFactory {
    private final BlockingQueue<Check> checks = new LinkedBlockingQueue<>();
    private volatile boolean observing;
    private volatile boolean controlChecks;
    private volatile boolean stopped;
    private volatile Check current;
    private final Set<Thread> schedulerWorkers = new LinkedHashSet<>();

    ObservedFactory(Clock clock) {
      super(clock);
    }

    @Override
    public Optional<TableProcess> trigger(TableRuntime runtime, Action action) {
      if (!observing) {
        return super.trigger(runtime, action);
      }
      synchronized (this) {
        // Close admission atomically with recording workers: a late check cannot access Catalog.
        if (stopped) {
          return Optional.empty();
        }
        schedulerWorkers.add(Thread.currentThread());
      }
      Check check = new Check();
      current = check;
      checks.add(check);
      try {
        if (controlChecks && !check.permit.await(30, TimeUnit.SECONDS)) {
          throw new IllegalStateException("Timed out waiting for scheduled check gate");
        }
        Optional<TableProcess> process =
            stopped ? Optional.empty() : super.trigger(runtime, action);
        check.finished = System.nanoTime();
        check.result.complete(process);
        return process;
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        check.result.completeExceptionally(e);
        throw new IllegalStateException(e);
      } catch (RuntimeException e) {
        check.result.completeExceptionally(e);
        throw e;
      }
    }

    Check nextCheck() throws Exception {
      Check check = checks.poll(30, TimeUnit.SECONDS);
      Assertions.assertNotNull(check, "The production scheduler did not run a check");
      return check;
    }

    void awaitSchedulerWorkers(Duration timeout) throws InterruptedException {
      List<Thread> workers;
      synchronized (this) {
        workers = new ArrayList<>(schedulerWorkers);
      }
      long deadline = System.nanoTime() + timeout.toNanos();
      for (Thread worker : workers) {
        long remaining = deadline - System.nanoTime();
        if (remaining > 0) {
          TimeUnit.NANOSECONDS.timedJoin(worker, remaining);
        }
        // dispose() can return after logging a timeout. Only actual thread exit is sufficient;
        // it also covers process registration and scheduler cleanup after trigger() returns.
        Assertions.assertFalse(
            worker.isAlive(),
            "Scheduler worker did not terminate; catalog and Derby must remain available");
      }
    }

    synchronized void stopObserving() {
      // Releasing a pending check during teardown must not register another native invocation.
      stopped = true;
      controlChecks = false;
      if (current != null) {
        current.permit.countDown();
      }
    }
  }

  private List<OptimizingProcessInfo> rows() {
    return descriptor
        .getOptimizingProcessesInfo(
            identifier.getIdentifier().buildTableIdentifier(), null, "CLEANUP", null, 10, 0)
        .getLeft();
  }

  private static void await(BooleanSupplier condition) throws Exception {
    await(condition, Duration.ofSeconds(40));
  }

  private static void await(BooleanSupplier condition, Duration timeout) throws Exception {
    long end = System.nanoTime() + timeout.toNanos();
    while (System.nanoTime() < end) {
      if (condition.getAsBoolean()) {
        return;
      }
      Thread.sleep(20);
    }
    Assertions.fail("Timed out waiting for process lifecycle");
  }

  private static class Plugins extends AbstractPluginManager<ProcessFactory>
      implements AutoCloseable {
    private final List<PluginConfiguration> configs;

    Plugins(List<PluginConfiguration> configs) {
      super("process-factories");
      this.configs = configs;
    }

    @Override
    protected List<PluginConfiguration> loadPluginConfigurations() {
      return configs;
    }
  }
}
