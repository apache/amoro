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

package org.apache.amoro.formats.paimon.process;

import org.apache.amoro.Action;
import org.apache.amoro.AmoroTable;
import org.apache.amoro.PaimonActions;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.formats.paimon.PaimonCatalog;
import org.apache.amoro.formats.paimon.PaimonCatalogFactory;
import org.apache.amoro.metrics.MetricRegistry;
import org.apache.amoro.process.LocalExecutionEngine;
import org.apache.amoro.process.LocalProcess;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.process.RecoverProcessFailedException;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.table.StateKey;
import org.apache.amoro.table.TableMetaStore;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.RawLocalFileSystem;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.paimon.catalog.CachingCatalog;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.consumer.Consumer;
import org.apache.paimon.data.GenericRow;
import org.apache.paimon.data.InternalRow;
import org.apache.paimon.options.Options;
import org.apache.paimon.schema.Schema;
import org.apache.paimon.schema.SchemaChange;
import org.apache.paimon.table.ExpireSnapshots;
import org.apache.paimon.table.FileStoreTable;
import org.apache.paimon.table.PrimaryKeyFileStoreTable;
import org.apache.paimon.table.sink.BatchTableCommit;
import org.apache.paimon.table.sink.BatchTableWrite;
import org.apache.paimon.table.sink.BatchWriteBuilder;
import org.apache.paimon.table.source.ReadBuilder;
import org.apache.paimon.table.source.StreamTableScan;
import org.apache.paimon.types.DataTypes;
import org.apache.paimon.utils.CloseableIterator;
import org.apache.paimon.utils.FileDeletionThreadPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

class TestPaimonSnapshotExpiration {
  @TempDir Path warehouse;
  Catalog original;
  Catalog cached;
  PaimonCatalog catalog;
  final Identifier id = Identifier.create("db", "tbl");
  final LocalExecutionEngine engine = new LocalExecutionEngine();

  @BeforeEach
  void setup() throws Exception {
    original =
        PaimonCatalogFactory.paimonCatalog(
            Collections.singletonMap("warehouse", warehouse.toString()), new Configuration());
    original.createDatabase("db", false);
    cached = new CachingCatalog(original, new Options());
    catalog = new PaimonCatalog(cached, "catalog");
  }

  @AfterEach
  void close() throws Exception {
    cached.close();
    engine.close();
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void expiresUsingTableRetentionAndLimitAndPreservesCurrentData(boolean primaryKey)
      throws Exception {
    createTable(
        primaryKey,
        Map.of(
            "snapshot.num-retained.min",
            "1",
            "snapshot.num-retained.max",
            "2",
            "snapshot.time-retained",
            "100 d",
            "snapshot.expire.limit",
            "1"));
    append(1, 4);
    Runtime runtime = new Runtime();
    PaimonSnapshotsExpiringProcess process = new PaimonSnapshotsExpiringProcess(runtime, engine);
    process.run();
    Assertions.assertEquals(2L, table().snapshotManager().earliestSnapshotId());
    Assertions.assertEquals("executed", process.getSummary().get("outcome"));
    Assertions.assertEquals("1", process.getSummary().get("reported-expired-snapshots"));
    Assertions.assertEquals("2", process.getSummary().get("snapshot.num-retained.max"));
    Assertions.assertTrue(runtime.getState(PaimonProcessFactory.LAST_SUCCESS_TIME) > 0);
    Assertions.assertEquals(List.of(1, 2, 3, 4), readIds(table()));
    process.run();
    Assertions.assertEquals(3L, table().snapshotManager().earliestSnapshotId());
    process.run();
    Assertions.assertEquals("noop", process.getSummary().get("outcome"));
    Assertions.assertEquals("0", process.getSummary().get("reported-expired-snapshots"));
  }

  @Test
  void nativeDefaultsAndEmptyTableAreReportedWithoutOverrides() throws Exception {
    createTable(false, Collections.emptyMap());
    PaimonSnapshotsExpiringProcess process =
        new PaimonSnapshotsExpiringProcess(new Runtime(), engine);
    process.run();
    Assertions.assertEquals("noop", process.getSummary().get("outcome"));
    Assertions.assertEquals("10", process.getSummary().get("snapshot.num-retained.min"));
    Assertions.assertEquals("2147483647", process.getSummary().get("snapshot.num-retained.max"));
    Assertions.assertEquals("PT1H", process.getSummary().get("snapshot.time-retained"));
    Assertions.assertEquals("50", process.getSummary().get("snapshot.expire.limit"));
    Assertions.assertEquals("10", process.getSummary().get("changelog.num-retained.min"));
  }

  @ParameterizedTest
  @CsvSource({
    "false,changelog.num-retained.min,2",
    "true,changelog.num-retained.min,2",
    "false,changelog.num-retained.max,5",
    "true,changelog.num-retained.max,5",
    "false,changelog.time-retained,200 d",
    "true,changelog.time-retained,200 d"
  })
  void independentChangelogRetentionPreservesHistory(boolean primaryKey, String key, String value)
      throws Exception {
    Map<String, String> options =
        new HashMap<>(
            Map.of(
                "snapshot.num-retained.min", "1",
                "snapshot.num-retained.max", "2",
                "snapshot.time-retained", "100 d"));
    if (primaryKey) {
      options.put("changelog-producer", "input");
    }
    options.put(key, value);
    createTable(primaryKey, options);
    append(1, 8);
    Assertions.assertTrue(table().store().options().changelogLifecycleDecoupled());
    Runtime runtime = new Runtime();
    TableProcess process = factory().trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).orElseThrow();
    ((LocalProcess) process).run();
    Assertions.assertEquals("executed", process.getSummary().get("outcome"));
    Assertions.assertEquals("6", process.getSummary().get("reported-expired-snapshots"));
    Assertions.assertEquals(7L, table().snapshotManager().earliestSnapshotId());
    Assertions.assertTrue(runtime.getState(PaimonProcessFactory.LAST_SUCCESS_TIME) > 0);
    Assertions.assertEquals(List.of(1L, 2L, 3L, 4L, 5L, 6L), archivedChangelogIds());
    Assertions.assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8), readIds(table()));

    // Resume a consumer from an expired snapshot, reading actual retained changelog files.
    ReadBuilder reader = table().newReadBuilder();
    StreamTableScan scan = reader.newStreamScan();
    scan.restore(1L);
    List<Integer> history = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      try (CloseableIterator<InternalRow> rows =
          reader.newRead().createReader(scan.plan()).toCloseableIterator()) {
        while (rows.hasNext()) {
          history.add(rows.next().getInt(0));
        }
      }
    }
    Collections.sort(history);
    Assertions.assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8), history);
    ((LocalProcess) process).run();
    Assertions.assertEquals("noop", process.getSummary().get("outcome"));
    // A separate changelog expiry would delete some of these archives; snapshot expiry must not.
    Assertions.assertEquals(List.of(1L, 2L, 3L, 4L, 5L, 6L), archivedChangelogIds());
  }

  @Test
  void queuedAndRecoveredProcessesAcceptIndependentChangelogRetention() throws Exception {
    createTable(false, Map.of("snapshot.num-retained.min", "1", "snapshot.num-retained.max", "2"));
    append(1, 4);
    Runtime runtime = new Runtime();
    PaimonProcessFactory factory = factory();
    TableProcess queued = factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).orElseThrow();
    original.alterTable(id, SchemaChange.setOption("changelog.num-retained.max", "5"), false);
    ((LocalProcess) queued).run();
    Assertions.assertEquals("executed", queued.getSummary().get("outcome"));
    Assertions.assertEquals("5", queued.getSummary().get("changelog.num-retained.max"));
    Assertions.assertEquals(List.of(1L, 2L), archivedChangelogIds());
    append(5, 6);
    TableProcessStore store = Mockito.mock(TableProcessStore.class);
    Mockito.when(store.getAction()).thenReturn(PaimonActions.EXPIRE_SNAPSHOTS);
    TableProcess recovered = factory.recover(runtime, store);
    original.alterTable(id, SchemaChange.setOption("changelog.num-retained.max", "6"), false);
    ((LocalProcess) recovered).run();
    Assertions.assertEquals("executed", recovered.getSummary().get("outcome"));
    Assertions.assertEquals("6", recovered.getSummary().get("changelog.num-retained.max"));
    Assertions.assertEquals(5L, table().snapshotManager().earliestSnapshotId());
    Assertions.assertEquals(List.of(1L, 2L, 3L, 4L), archivedChangelogIds());
    Assertions.assertEquals(List.of(1, 2, 3, 4, 5, 6), readIds(table()));
  }

  @ParameterizedTest
  @CsvSource({
    "amoro.expire-snapshots.enabled,false,governance-disabled",
    "write-only,false,write-only-required"
  })
  void queuedProcessRechecksEligibility(String key, String value, String reason) throws Exception {
    createTable(false, Collections.emptyMap());
    Runtime runtime = new Runtime();
    PaimonProcessFactory factory = factory();
    TableProcess process = factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).orElseThrow();
    original.alterTable(id, SchemaChange.setOption(key, value), false);
    ((LocalProcess) process).run();
    Assertions.assertEquals("skipped", process.getSummary().get("outcome"));
    Assertions.assertEquals(reason, process.getSummary().get("skip-reason"));
    Assertions.assertFalse(process.getSummary().containsKey("reported-expired-snapshots"));
    Assertions.assertEquals(0L, runtime.getState(PaimonProcessFactory.LAST_SUCCESS_TIME));
    Assertions.assertTrue(factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).isEmpty());
  }

  @Test
  void queuedAndRecoveredProcessesReadLatestPolicy() throws Exception {
    createTable(
        true,
        Map.of(
            "snapshot.num-retained.min",
            "1",
            "snapshot.num-retained.max",
            "3",
            "snapshot.time-retained",
            "100 d",
            "snapshot.expire.limit",
            "1"));
    append(1, 6);
    Runtime runtime = new Runtime();
    PaimonProcessFactory factory = factory();
    TableProcess queued = factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).orElseThrow();
    original.alterTable(
        id,
        List.of(
            SchemaChange.setOption("snapshot.num-retained.max", "2"),
            SchemaChange.setOption("snapshot.expire.limit", "10")),
        false);
    ((LocalProcess) queued).run();
    Assertions.assertEquals(5L, table().snapshotManager().earliestSnapshotId());
    Assertions.assertEquals("10", queued.getSummary().get("snapshot.expire.limit"));
    Assertions.assertTrue(factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).isEmpty());
    Assertions.assertTrue(queued.getProcessParameters().isEmpty());
    TableProcessStore store = Mockito.mock(TableProcessStore.class);
    Mockito.when(store.getAction()).thenReturn(PaimonActions.EXPIRE_SNAPSHOTS);
    original.alterTable(id, SchemaChange.setOption("snapshot.num-retained.max", "1"), false);
    TableProcess recovered = factory.recover(runtime, store);
    ((LocalProcess) recovered).run();
    Assertions.assertEquals(6L, table().snapshotManager().earliestSnapshotId());
    Assertions.assertEquals(List.of(1, 2, 3, 4, 5, 6), readIds(table()));
  }

  @Test
  void nativeResultSurvivesStateFailureAndRetryReplacesSummary() throws Exception {
    createTable(false, Map.of("snapshot.num-retained.min", "1", "snapshot.num-retained.max", "1"));
    append(1, 3);
    Runtime runtime = new Runtime();
    runtime.failStateUpdate = true;
    PaimonSnapshotsExpiringProcess process = new PaimonSnapshotsExpiringProcess(runtime, engine);
    Assertions.assertThrows(RuntimeException.class, process::run);
    Map<String, String> failed = process.getSummary();
    Assertions.assertEquals("executed", failed.get("outcome"));
    Assertions.assertEquals("2", failed.get("reported-expired-snapshots"));
    Assertions.assertEquals("state-update", failed.get("error-stage"));
    Assertions.assertEquals(3L, table().snapshotManager().earliestSnapshotId());
    original.alterTable(
        id, SchemaChange.setOption("amoro.expire-snapshots.enabled", "false"), false);
    process.run();
    Assertions.assertEquals("skipped", process.getSummary().get("outcome"));
    Assertions.assertFalse(process.getSummary().containsKey("reported-expired-snapshots"));
    Assertions.assertFalse(process.getSummary().containsKey("error-message"));
    Assertions.assertEquals("executed", failed.get("outcome"));
    Assertions.assertThrows(
        UnsupportedOperationException.class, () -> failed.put("outcome", "bad"));
  }

  @Test
  void loadingFailureIsNotSkippedAndSummaryDoesNotLeakCredentials() throws Exception {
    createTable(false, Collections.emptyMap());
    Runtime runtime = new Runtime();
    runtime.loader =
        () -> {
          throw new IllegalStateException("password=secret https://user:secret@host");
        };
    Assertions.assertThrows(
        IllegalStateException.class,
        () -> factory().trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS));
    PaimonSnapshotsExpiringProcess process = new PaimonSnapshotsExpiringProcess(runtime, engine);
    Assertions.assertThrows(RuntimeException.class, process::run);
    Assertions.assertEquals("load", process.getSummary().get("error-stage"));
    Assertions.assertFalse(process.getSummary().containsKey("outcome"));
    Assertions.assertFalse(process.getSummary().get("error-message").contains("secret"));
    runtime.loader = () -> catalog.loadTable("db", "tbl");
    process.run();
    Assertions.assertEquals("noop", process.getSummary().get("outcome"));
    Assertions.assertFalse(process.getSummary().containsKey("error-message"));
  }

  @Test
  void invalidLimitFailsWithoutDeletingSnapshots() throws Exception {
    createTable(
        false,
        Map.of(
            "snapshot.num-retained.min",
            "1",
            "snapshot.num-retained.max",
            "1",
            "snapshot.expire.limit",
            "0"));
    append(1, 3);
    PaimonSnapshotsExpiringProcess process =
        new PaimonSnapshotsExpiringProcess(new Runtime(), engine);
    Assertions.assertThrows(RuntimeException.class, process::run);
    Assertions.assertEquals("config", process.getSummary().get("error-stage"));
    Assertions.assertFalse(process.getSummary().containsKey("outcome"));
    Assertions.assertEquals(1L, table().snapshotManager().earliestSnapshotId());
  }

  @Test
  void nativeTagAndConsumerProtectionRemainEffective() throws Exception {
    createTable(true, Map.of("snapshot.num-retained.min", "1", "snapshot.num-retained.max", "1"));
    append(1, 1);
    table().createTag("first", 1);
    append(2, 5);
    table().consumerManager().resetConsumer("reader", new Consumer(3));
    PaimonSnapshotsExpiringProcess process =
        new PaimonSnapshotsExpiringProcess(new Runtime(), engine);
    process.run();
    Assertions.assertTrue(table().snapshotManager().snapshotExists(3));
    Assertions.assertEquals(List.of(1), readIds(table().copy(Map.of("scan.tag-name", "first"))));
    Assertions.assertEquals(List.of(1, 2, 3, 4, 5), readIds(table()));
    table().consumerManager().deleteConsumer("reader");
    process.run();
    Assertions.assertEquals(5L, table().snapshotManager().earliestSnapshotId());
    Assertions.assertEquals(List.of(1), readIds(table().copy(Map.of("scan.tag-name", "first"))));
  }

  @ParameterizedTest
  @ValueSource(strings = {"omitted", "true", "false"})
  void tableExpirationSwitchDefaultsToEnabled(String enabled) throws Exception {
    createTable(
        false,
        "omitted".equals(enabled)
            ? Map.of("snapshot.num-retained.min", "1", "snapshot.num-retained.max", "1")
            : Map.of(
                "snapshot.num-retained.min", "1",
                "snapshot.num-retained.max", "1",
                "amoro.expire-snapshots.enabled", enabled));
    append(1, 3);
    Runtime runtime = new Runtime();
    boolean expected = !"false".equals(enabled);
    Assertions.assertEquals(
        expected, factory().trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).isPresent());
    PaimonSnapshotsExpiringProcess process = new PaimonSnapshotsExpiringProcess(runtime, engine);
    process.run();
    Assertions.assertEquals(expected ? 3L : 1L, table().snapshotManager().earliestSnapshotId());
    Assertions.assertEquals(expected ? "executed" : "skipped", process.getSummary().get("outcome"));
    if (!expected) {
      Assertions.assertEquals("governance-disabled", process.getSummary().get("skip-reason"));
      Assertions.assertEquals(0L, runtime.getState(PaimonProcessFactory.LAST_SUCCESS_TIME));
      original.alterTable(id, SchemaChange.removeOption("amoro.expire-snapshots.enabled"), false);
      Assertions.assertTrue(factory().trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).isPresent());
      process.run();
      Assertions.assertEquals(3L, table().snapshotManager().earliestSnapshotId());
    }
  }

  @Test
  void pluginExpirationSwitchControlsActionRegistration() throws Exception {
    createTable(false, Collections.emptyMap());
    Runtime runtime = new Runtime();
    PaimonProcessFactory factory = factory();
    Assertions.assertTrue(factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).isPresent());
    factory.open(Map.of("expire-snapshots.enabled", "false", "expire-snapshots.interval", "1h"));
    Assertions.assertTrue(factory.supportedActions().isEmpty());
    Assertions.assertTrue(factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).isEmpty());
    // A disabled action does not require or parse its interval, as in the Iceberg factory.
    factory.open(
        Map.of("expire-snapshots.enabled", "false", "expire-snapshots.interval", "invalid"));
    Assertions.assertTrue(factory.supportedActions().isEmpty());
    factory.open(Map.of("expire-snapshots.enabled", "false"));
    Assertions.assertTrue(factory.supportedActions().isEmpty());
    factory.open(Map.of("expire-snapshots.enabled", "true"));
    Assertions.assertTrue(factory.supportedActions().isEmpty());
    factory.open(Map.of("expire-snapshots.enabled", "true", "expire-snapshots.interval", "1h"));
    Assertions.assertTrue(factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).isPresent());
  }

  @Test
  void factoryDispatchOnlyAcceptsRegisteredPaimonActions() {
    PaimonProcessFactory factory = factory();
    TableRuntime runtime = Mockito.mock(TableRuntime.class);
    Action unsupported = Action.register("test-paimon-unsupported");
    Mockito.when(runtime.getFormat()).thenReturn(TableFormat.PAIMON);
    Assertions.assertTrue(factory.trigger(runtime, unsupported).isEmpty());
    Mockito.when(runtime.getFormat()).thenReturn(TableFormat.ICEBERG);
    Assertions.assertTrue(factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).isEmpty());
    factory.close();
    Assertions.assertTrue(factory.supportedActions().isEmpty());
    Mockito.when(runtime.getFormat()).thenReturn(TableFormat.PAIMON);
    Assertions.assertTrue(factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).isEmpty());
    // Rejected dispatches must not read table state or load and refresh the table.
    Mockito.verify(runtime, Mockito.atLeastOnce()).getFormat();
    Mockito.verifyNoMoreInteractions(runtime);
  }

  @Test
  void factoryRequiresExplicitIntervalAndLocalEngine() throws Exception {
    PaimonProcessFactory factory = new PaimonProcessFactory();
    factory.open(Collections.emptyMap());
    Assertions.assertTrue(factory.supportedActions().isEmpty());
    factory.open(Map.of("expire-snapshots.interval", "1h"));
    Assertions.assertEquals(
        java.time.Duration.ofHours(1),
        factory
            .triggerStrategy(TableFormat.PAIMON, PaimonActions.EXPIRE_SNAPSHOTS)
            .getTriggerInterval());
    createTable(false, Collections.emptyMap());
    Runtime runtime = new Runtime();
    Assertions.assertTrue(factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).isEmpty());
    TableProcessStore store = Mockito.mock(TableProcessStore.class);
    Mockito.when(store.getAction()).thenReturn(PaimonActions.EXPIRE_SNAPSHOTS);
    Assertions.assertThrows(
        RecoverProcessFailedException.class, () -> factory.recover(runtime, store));
    factory.availableExecuteEngines(List.of(engine));
    Assertions.assertTrue(factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).isPresent());
    factory.open(Collections.emptyMap());
    Assertions.assertTrue(factory.supportedActions().isEmpty());
    Assertions.assertTrue(
        StreamSupport.stream(ServiceLoader.load(ProcessFactory.class).spliterator(), false)
            .anyMatch(f -> f instanceof PaimonProcessFactory));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "0ms", "-1s", "1ns", "invalid", "9223372036854775807d"})
  void invalidIntervalsDoNotRegisterActions(String interval) {
    PaimonProcessFactory factory = new PaimonProcessFactory();
    Assertions.assertThrows(
        RuntimeException.class, () -> factory.open(Map.of("expire-snapshots.interval", interval)));
    Assertions.assertTrue(factory.supportedActions().isEmpty());
  }

  @Test
  void defaultBranchAndOrdinaryTableScopeAreCheckedBeforeExecution() throws Exception {
    createTable(true, Collections.emptyMap());
    append(1, 1);
    table().createBranch("development");
    Runtime runtime = new Runtime();
    runtime.loader = () -> catalog.loadTable("db", "tbl$branch_development");
    Assertions.assertTrue(factory().trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).isEmpty());
    PaimonSnapshotsExpiringProcess process = new PaimonSnapshotsExpiringProcess(runtime, engine);
    process.run();
    Assertions.assertEquals("non-default-branch", process.getSummary().get("skip-reason"));
    runtime.loader = () -> catalog.loadTable("db", "tbl$snapshots");
    process.run();
    Assertions.assertEquals("skipped", process.getSummary().get("outcome"));
    Assertions.assertEquals(0L, runtime.getState(PaimonProcessFactory.LAST_SUCCESS_TIME));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void localEngineUsesDedicatedPoolOrExistingDefaultFallback(boolean configured) throws Exception {
    createTable(false, Collections.emptyMap());
    engine.open(configured ? Map.of("pool.paimon-expire-snapshots.thread-count", "1") : Map.of());
    Runtime runtime = new Runtime();
    java.util.concurrent.atomic.AtomicReference<String> thread =
        new java.util.concurrent.atomic.AtomicReference<>();
    runtime.loader =
        () -> {
          thread.set(Thread.currentThread().getName());
          return catalog.loadTable("db", "tbl");
        };
    PaimonSnapshotsExpiringProcess process = new PaimonSnapshotsExpiringProcess(runtime, engine);
    String executionId = engine.submitTableProcess(process);
    long deadline = System.nanoTime() + java.time.Duration.ofSeconds(10).toNanos();
    while (engine.getStatus(executionId) == org.apache.amoro.process.ProcessStatus.RUNNING
        && System.nanoTime() < deadline) {
      Thread.sleep(10);
    }
    Assertions.assertEquals(
        org.apache.amoro.process.ProcessStatus.SUCCESS, engine.getStatus(executionId));
    Assertions.assertTrue(
        thread.get().startsWith(configured ? "local-paimon-expire-snapshots-" : "local-default-"));
  }

  @Test
  void usesCatalogIdentityForRefreshEligibilityAndExpiration() throws Exception {
    createTable(true, Map.of("snapshot.num-retained.min", "1", "snapshot.num-retained.max", "1"));
    append(1, 3);
    String caller = UserGroupInformation.getCurrentUser().getShortUserName();
    String catalogUser = caller + "-catalog";
    byte[] site = "<configuration/>".getBytes(StandardCharsets.UTF_8);
    TableMetaStore store =
        TableMetaStore.builder()
            .withHdfsSite(site)
            .withCoreSite(site)
            .withSimpleAuth(catalogUser)
            .build();
    PrimaryKeyFileStoreTable nativeTable = Mockito.spy((PrimaryKeyFileStoreTable) table());
    ExpireSnapshots expiration = Mockito.spy(nativeTable.newExpireSnapshots());
    Mockito.doReturn(expiration).when(nativeTable).newExpireSnapshots();
    Answer<Object> callAsCatalogUser =
        call -> {
          Assertions.assertEquals(
              catalogUser, UserGroupInformation.getCurrentUser().getShortUserName());
          return call.callRealMethod();
        };
    Mockito.doAnswer(callAsCatalogUser).when(expiration).expire();
    Mockito.doAnswer(callAsCatalogUser).when(nativeTable).options();
    Mockito.doAnswer(callAsCatalogUser).when(nativeTable).store();
    Catalog nativeCatalog = Mockito.mock(Catalog.class);
    Mockito.when(nativeCatalog.getTable(id))
        .thenAnswer(
            call -> {
              Assertions.assertEquals(
                  catalogUser, UserGroupInformation.getCurrentUser().getShortUserName());
              return nativeTable;
            });
    PaimonCatalog wrapper = new PaimonCatalog(nativeCatalog, "catalog", store);
    Runtime runtime = new Runtime();
    // The AMS catalog authenticates the initial lookup before returning the table to the scheduler.
    runtime.loader = () -> store.doAs(() -> wrapper.loadTable("db", "tbl"));
    TableProcess process = factory().trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).orElseThrow();
    Assertions.assertEquals(caller, UserGroupInformation.getCurrentUser().getShortUserName());
    ((LocalProcess) process).run();
    Assertions.assertEquals(caller, UserGroupInformation.getCurrentUser().getShortUserName());
    Assertions.assertEquals("2", process.getSummary().get("reported-expired-snapshots"));
    Assertions.assertEquals(3L, table().snapshotManager().earliestSnapshotId());
    Mockito.verify(expiration).expire();
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void expiresExternalFilesAsCatalogUserOnReusedThreads(boolean primaryKey) throws Exception {
    Path external = Files.createDirectory(warehouse.resolve("external"));
    createTable(
        primaryKey,
        Map.of(
            "snapshot.num-retained.min", "1",
            "snapshot.num-retained.max", "1",
            "data-file.external-paths", "testdata://external" + external.toUri().getRawPath(),
            "data-file.external-paths.strategy", "round-robin"));
    Map<String, String> properties =
        Map.of("warehouse", warehouse.toUri().toString(), "resolving-file-io.enabled", "true");
    Configuration conf = new Configuration(false);
    conf.setClass(
        "fs.testdata.impl", ExternalDataFileSystem.class, org.apache.hadoop.fs.FileSystem.class);
    conf.setBoolean("fs.testdata.impl.disable.cache", true);
    try (Catalog writer = PaimonCatalogFactory.paimonCatalog(properties, conf)) {
      for (int i = 1; i <= 3; i++) {
        BatchWriteBuilder builder =
            ((FileStoreTable) writer.getTable(id)).newBatchWriteBuilder().withOverwrite();
        try (BatchTableWrite write = builder.newWrite();
            BatchTableCommit commit = builder.newCommit()) {
          write.write(GenericRow.of(i));
          commit.commit(write.prepareCommit());
        }
      }
    }
    Assertions.assertEquals(3L, countDataFiles(external));

    // All native deletion workers already exist before the catalog user starts this attempt.
    FileDeletionThreadPool.getExecutorService(1).prestartAllCoreThreads();
    String catalogUser = "cleanup-" + warehouse.getFileName();
    conf.set(ExternalDataFileSystem.REQUIRED_USER, catalogUser);
    ByteArrayOutputStream coreSite = new ByteArrayOutputStream();
    conf.writeXml(coreSite);
    TableMetaStore store =
        TableMetaStore.builder()
            .withCoreSite(coreSite.toByteArray())
            .withHdfsSite("<configuration/>".getBytes(StandardCharsets.UTF_8))
            .withSimpleAuth(catalogUser)
            .build();
    PaimonCatalog reader =
        store.doAs(() -> new PaimonCatalogFactory().create("catalog", "hadoop", properties, store));
    Runtime runtime = new Runtime();
    runtime.loader = () -> store.doAs(() -> reader.loadTable("db", "tbl"));
    TableProcess process = factory().trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).orElseThrow();
    ((LocalProcess) process).run();
    Assertions.assertEquals("2", process.getSummary().get("reported-expired-snapshots"));
    Assertions.assertEquals(3L, table().snapshotManager().earliestSnapshotId());
    Assertions.assertEquals(1L, countDataFiles(external));
  }

  private long countDataFiles(Path directory) throws IOException {
    try (Stream<Path> paths = Files.walk(directory)) {
      return paths.filter(Files::isRegularFile).count();
    }
  }

  /** Local storage exposed as a separate Hadoop authority with a per-catalog identity check. */
  public static class ExternalDataFileSystem extends RawLocalFileSystem {
    static final String REQUIRED_USER = "fs.testdata.required-user";

    @Override
    public URI getUri() {
      return URI.create("testdata://external");
    }

    @Override
    public File pathToFile(org.apache.hadoop.fs.Path path) {
      return new File(path.toUri().getPath());
    }

    @Override
    public void initialize(URI uri, Configuration conf) throws IOException {
      checkUser(conf);
      super.initialize(uri, conf);
    }

    @Override
    public boolean delete(org.apache.hadoop.fs.Path path, boolean recursive) throws IOException {
      checkUser(getConf());
      return super.delete(path, recursive);
    }

    private static void checkUser(Configuration conf) throws IOException {
      String required = conf.get(REQUIRED_USER);
      if (required != null
          && !required.equals(UserGroupInformation.getCurrentUser().getShortUserName())) {
        throw new IOException("External test filesystem requires " + required);
      }
    }
  }

  @Test
  void nativeFailureProducesFailureSummaryWithoutCallingChangelogExpiration() throws Exception {
    createTable(true, Collections.emptyMap());
    org.apache.paimon.table.PrimaryKeyFileStoreTable nativeTable =
        Mockito.spy((org.apache.paimon.table.PrimaryKeyFileStoreTable) table());
    org.apache.paimon.table.ExpireSnapshots expiration =
        Mockito.mock(org.apache.paimon.table.ExpireSnapshots.class);
    Mockito.doReturn(expiration).when(nativeTable).newExpireSnapshots();
    Mockito.when(expiration.config(Mockito.any())).thenReturn(expiration);
    Mockito.when(expiration.expire()).thenThrow(new IllegalStateException("secret-token=hidden"));
    Catalog failingCatalog = Mockito.mock(Catalog.class);
    Mockito.when(failingCatalog.getTable(id)).thenReturn(nativeTable);
    PaimonCatalog wrapper = new PaimonCatalog(failingCatalog, "catalog");
    Runtime runtime = new Runtime();
    runtime.loader = () -> wrapper.loadTable("db", "tbl");
    PaimonSnapshotsExpiringProcess process = new PaimonSnapshotsExpiringProcess(runtime, engine);
    Assertions.assertThrows(RuntimeException.class, process::run);
    Assertions.assertEquals("expire", process.getSummary().get("error-stage"));
    Assertions.assertFalse(process.getSummary().containsKey("reported-expired-snapshots"));
    Assertions.assertFalse(process.getSummary().get("error-message").contains("hidden"));
    Mockito.verify(nativeTable, Mockito.never()).newExpireChangelog();
    Assertions.assertEquals(0L, runtime.getState(PaimonProcessFactory.LAST_SUCCESS_TIME));
  }

  @Test
  void nativeTimeAndMinimumRetentionAreRespectedAndChangelogSettingsPreserved() throws Exception {
    createTable(
        false,
        Map.of(
            "snapshot.num-retained.min",
            "2",
            "snapshot.time-retained",
            "100 d",
            "changelog.num-retained.min",
            "1",
            "changelog.time-retained",
            "1 d",
            "snapshot.expire.limit",
            "7"));
    append(1, 5);
    PaimonSnapshotsExpiringProcess process =
        new PaimonSnapshotsExpiringProcess(new Runtime(), engine);
    process.run();
    Assertions.assertEquals(1L, table().snapshotManager().earliestSnapshotId());
    Assertions.assertEquals("noop", process.getSummary().get("outcome"));
    Assertions.assertEquals("1", process.getSummary().get("changelog.num-retained.min"));
    Assertions.assertEquals("PT24H", process.getSummary().get("changelog.time-retained"));
    Assertions.assertEquals("7", process.getSummary().get("effective-changelog-max-deletes"));
    original.alterTable(
        id,
        List.of(
            SchemaChange.setOption("snapshot.time-retained", "0 ms"),
            SchemaChange.setOption("changelog.time-retained", "0 ms")),
        false);
    process.run();
    Assertions.assertEquals(4L, table().snapshotManager().earliestSnapshotId());
    Assertions.assertEquals(List.of(1, 2, 3, 4, 5), readIds(table()));
  }

  @Test
  void factoryUsesControlledTimeAtSuccessIntervalBoundary() throws Exception {
    createTable(false, Collections.emptyMap());
    Runtime runtime = new Runtime();
    runtime.updateState(PaimonProcessFactory.LAST_SUCCESS_TIME, ignored -> 10_000L);
    java.time.Clock clock = Mockito.mock(java.time.Clock.class);
    PaimonProcessFactory factory = new PaimonProcessFactory(clock);
    factory.open(Map.of("expire-snapshots.interval", "1s"));
    factory.availableExecuteEngines(List.of(engine));

    Mockito.when(clock.millis()).thenReturn(10_999L);
    Assertions.assertTrue(factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).isEmpty());
    Mockito.when(clock.millis()).thenReturn(11_000L);
    Assertions.assertTrue(factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).isPresent());
    Mockito.when(clock.millis()).thenReturn(11_001L);
    Assertions.assertTrue(factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS).isPresent());
    Assertions.assertEquals(10_000L, runtime.getState(PaimonProcessFactory.LAST_SUCCESS_TIME));
  }

  private PaimonProcessFactory factory() {
    PaimonProcessFactory factory = new PaimonProcessFactory();
    factory.open(Map.of("expire-snapshots.interval", "1h"));
    factory.availableExecuteEngines(List.of(engine));
    return factory;
  }

  void createTable(boolean primaryKey, Map<String, String> options) throws Exception {
    Schema.Builder builder =
        Schema.newBuilder()
            .column("id", DataTypes.INT())
            .option("write-only", "true")
            .option("bucket", primaryKey ? "1" : "-1");
    if (primaryKey) {
      builder.primaryKey("id");
    }
    options.forEach(builder::option);
    original.createTable(id, builder.build(), false);
  }

  FileStoreTable table() throws Exception {
    return (FileStoreTable) original.getTable(id);
  }

  private List<Long> archivedChangelogIds() throws Exception {
    List<Long> ids = new ArrayList<>();
    table().changelogManager().safelyGetAllChangelogs().forEach(log -> ids.add(log.id()));
    Collections.sort(ids);
    return ids;
  }

  void append(int start, int end) throws Exception {
    for (int i = start; i <= end; i++) {
      BatchWriteBuilder builder = table().newBatchWriteBuilder();
      try (BatchTableWrite write = builder.newWrite();
          BatchTableCommit commit = builder.newCommit()) {
        write.write(GenericRow.of(i));
        commit.commit(write.prepareCommit());
      }
    }
  }

  static List<Integer> readIds(org.apache.paimon.table.Table table) throws Exception {
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

  class Runtime implements TableRuntime {
    private final Map<StateKey<?>, Object> states = new HashMap<>();
    java.util.function.Supplier<AmoroTable<?>> loader = () -> catalog.loadTable("db", "tbl");
    boolean failStateUpdate;

    @Override
    public AmoroTable<?> loadTable() {
      return loader.get();
    }

    @Override
    public ServerTableIdentifier getTableIdentifier() {
      return ServerTableIdentifier.of(1L, "catalog", "db", "tbl", TableFormat.PAIMON);
    }

    @Override
    public <T> T getState(StateKey<T> key) {
      return (T) states.getOrDefault(key, key.getDefaultValue());
    }

    @Override
    public <T> void updateState(StateKey<T> key, Function<T, T> updater) {
      if (failStateUpdate) {
        throw new IllegalStateException("injected state failure");
      }
      states.put(key, updater.apply(getState(key)));
    }

    @Override
    public List<? extends TableProcessStore> getProcessStates() {
      return Collections.emptyList();
    }

    @Override
    public List<? extends TableProcessStore> getProcessStates(Action action) {
      return Collections.emptyList();
    }

    @Override
    public String getGroupName() {
      return "default";
    }

    @Override
    public TableConfiguration getTableConfiguration() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getTableConfig() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void registerMetric(MetricRegistry registry) {}

    @Override
    public void unregisterMetric() {}
  }
}
