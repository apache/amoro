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

package org.apache.amoro.server.optimizing;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.formats.iceberg.IcebergTable;
import org.apache.amoro.formats.iceberg.IcebergV3TestTables;
import org.apache.amoro.optimizing.plan.AbstractOptimizingEvaluator;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.SqlSessionFactoryProvider;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.persistence.mapper.TableRuntimeMapper;
import org.apache.amoro.server.resource.OptimizerThread;
import org.apache.amoro.server.scheduler.inline.TableRuntimeRefreshExecutor;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.DefaultTableRuntimeStore;
import org.apache.amoro.server.table.DerbyPersistence;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.server.utils.IcebergTableUtil;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.hadoop.conf.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/** Verifies that disabling self-optimizing clears leftover pending state of a table. */
class TestDisabledOptimizingCleanup extends PersistentBase {
  @TempDir private Path temp;

  @SuppressWarnings("unused")
  private final DerbyPersistence standalonePersistence = initializePersistence();

  private final TestPersistence persistence = new TestPersistence();
  private final CatalogManager catalogManager = mock(CatalogManager.class);
  private final Set<Long> tableIds = new HashSet<>();
  private OptimizingQueue queue;

  private static DerbyPersistence initializePersistence() {
    try {
      SqlSessionFactoryProvider.getInstance().get();
      return null;
    } catch (IllegalStateException e) {
      return new DerbyPersistence();
    }
  }

  @AfterEach
  void cleanUp() {
    if (queue != null) {
      queue.dispose();
    }
    persistence.cleanUp(tableIds);
    tableIds.clear();
  }

  @Test
  void testRecoveryClearsDisabledPendingWithClosedProcessHistory() throws Exception {
    DefaultTableRuntime runtime = createRuntime("disabled_closed_history");
    queue = createQueue(runtime);
    assertNotNull(queue.pollTask(thread(1), 0));
    runtime.getOptimizingProcess().close(false);
    assertNull(runtime.getOptimizingProcess());
    assertTrue(runtime.getProcessId() != 0);
    AbstractOptimizingEvaluator evaluator =
        IcebergTableUtil.createOptimizingEvaluator(
            runtime, (MixedTable) runtime.loadTable().originalTable(), 1);
    assertTrue(evaluator.isNecessary());
    runtime.setPendingInput(evaluator.getOptimizingPendingInput());
    long lastOptimizedSnapshotId = runtime.getLastOptimizedSnapshotId();
    disableOptimizing(runtime);
    DefaultTableRuntime restored = restoreRuntime(runtime);
    assertEquals(OptimizingStatus.PENDING, restored.getOptimizingStatus());
    queue.dispose();

    queue = createQueue(restored);

    assertEquals(OptimizingStatus.IDLE, restored.getOptimizingStatus());
    assertEquals(OptimizingStatus.IDLE, restoreRuntime(restored).getOptimizingStatus());
    assertNull(restored.getOptimizingProcess());
    assertEquals(0, restored.getPendingInput().getDataFileCount());
    assertEquals(0, restoreRuntime(restored).getPendingInput().getDataFileCount());
    assertEquals(lastOptimizedSnapshotId, restored.getLastOptimizedSnapshotId());
    assertEquals(lastOptimizedSnapshotId, restoreRuntime(restored).getLastOptimizedSnapshotId());
  }

  @Test
  void testPlanningFailureAfterDisableClearsPending() throws Exception {
    DefaultTableRuntime runtime = createRuntime("disabled_planning_failure");
    long lastOptimizedSnapshotId = runtime.getLastOptimizedSnapshotId();
    queue = createQueue(runtime);
    doAnswer(
            invocation -> {
              assertEquals(OptimizingStatus.PLANNING, runtime.getOptimizingStatus());
              TableConfiguration originalConfig = runtime.getTableConfiguration();
              disableOptimizing(runtime);
              TableRuntimeRefreshExecutor executor =
                  new TableRuntimeRefreshExecutor(mock(TableService.class), 1, 60000, 1);
              try {
                executor.handleConfigChanged(runtime, originalConfig);
              } finally {
                executor.gracefulShutdown();
              }
              queue.refreshTable(runtime);
              throw new IllegalStateException("Injected catalog load failure");
            })
        .when(catalogManager)
        .loadTable(runtime.getTableIdentifier().getIdentifier());

    assertNull(queue.pollTask(thread(1), 0));

    assertFalse(runtime.getOptimizingConfig().isEnabled());
    assertEquals(OptimizingStatus.IDLE, runtime.getOptimizingStatus());
    assertEquals(OptimizingStatus.IDLE, restoreRuntime(runtime).getOptimizingStatus());
    assertNull(runtime.getOptimizingProcess());
    assertEquals(lastOptimizedSnapshotId, runtime.getLastOptimizedSnapshotId());
    assertEquals(lastOptimizedSnapshotId, restoreRuntime(runtime).getLastOptimizedSnapshotId());
  }

  @Test
  void testDisablingPendingTableResetsStatusWithoutAdvancingSnapshot() throws Exception {
    DefaultTableRuntime runtime = createRuntime("disable_pending");
    AbstractOptimizingEvaluator evaluator =
        IcebergTableUtil.createOptimizingEvaluator(
            runtime, (MixedTable) runtime.loadTable().originalTable(), 1);
    assertTrue(evaluator.isNecessary());
    runtime.setPendingInput(evaluator.getOptimizingPendingInput());
    assertTrue(runtime.getPendingInput().getDataFileCount() > 0);
    TableConfiguration originalConfig = runtime.getTableConfiguration();
    long lastOptimizedSnapshotId = runtime.getLastOptimizedSnapshotId();
    disableOptimizing(runtime);

    TableRuntimeRefreshExecutor executor =
        new TableRuntimeRefreshExecutor(mock(TableService.class), 1, 60000, 1);
    try {
      executor.handleConfigChanged(runtime, originalConfig);
    } finally {
      executor.gracefulShutdown();
    }

    assertAll(
        () -> assertEquals(OptimizingStatus.IDLE, runtime.getOptimizingStatus()),
        () -> assertEquals(OptimizingStatus.IDLE, restoreRuntime(runtime).getOptimizingStatus()),
        () -> assertEquals(0, runtime.getPendingInput().getDataFileCount()),
        () -> assertEquals(0, restoreRuntime(runtime).getPendingInput().getDataFileCount()),
        () -> assertEquals(lastOptimizedSnapshotId, runtime.getLastOptimizedSnapshotId()));
  }

  @Test
  void testDisabledPendingTableIsRemovedFromScheduler() throws Exception {
    DefaultTableRuntime runtime = createRuntime("disable_scheduled_pending");
    queue = createQueue(runtime);
    assertTrue(
        queue.getSchedulingPolicy().getTableRuntimeMap().containsKey(runtime.getTableIdentifier()));
    disableOptimizing(runtime);

    queue.refreshTable(runtime);

    assertFalse(
        queue.getSchedulingPolicy().getTableRuntimeMap().containsKey(runtime.getTableIdentifier()));
  }

  @Test
  void testRecoveryResetsDisabledPendingTableWithoutAdvancingSnapshot() throws Exception {
    DefaultTableRuntime runtime = createRuntime("recover_disabled_pending");
    disableOptimizing(runtime);
    DefaultTableRuntime restored = restoreRuntime(runtime);
    long lastOptimizedSnapshotId = restored.getLastOptimizedSnapshotId();
    assertEquals(OptimizingStatus.PENDING, restored.getOptimizingStatus());
    assertFalse(restored.getOptimizingConfig().isEnabled());

    queue = createQueue(restored);

    assertAll(
        () -> assertEquals(OptimizingStatus.IDLE, restored.getOptimizingStatus()),
        () -> assertEquals(OptimizingStatus.IDLE, restoreRuntime(restored).getOptimizingStatus()),
        () -> assertEquals(lastOptimizedSnapshotId, restored.getLastOptimizedSnapshotId()));
  }

  @Test
  void testDisabledTableIsNotPlannedAfterBeingSelected() throws Exception {
    DefaultTableRuntime runtime = createRuntime("disable_before_planning");
    long lastOptimizedSnapshotId = runtime.getLastOptimizedSnapshotId();
    AtomicReference<Runnable> planning = new AtomicReference<>();
    queue =
        new OptimizingQueue(
            catalogManager,
            new ResourceGroup.Builder("default", "local").build(),
            group -> 1,
            planning::set,
            Collections.singletonList(runtime),
            1);
    assertNull(queue.pollTask(thread(1), 0));
    assertNotNull(planning.get());
    disableOptimizing(runtime);
    queue.refreshTable(runtime);

    planning.get().run();

    assertAll(
        () -> assertEquals(OptimizingStatus.IDLE, runtime.getOptimizingStatus()),
        () -> assertNull(runtime.getOptimizingProcess()),
        () -> assertEquals(lastOptimizedSnapshotId, runtime.getLastOptimizedSnapshotId()));
  }

  @Test
  void testDisablingDuringPlanningRefreshDoesNotAdvanceSnapshot() throws Exception {
    DefaultTableRuntime runtime = createRuntime("disable_during_refresh");
    long lastOptimizedSnapshotId = runtime.getLastOptimizedSnapshotId();
    queue = createQueue(runtime);
    doAnswer(
            invocation -> {
              disableOptimizing(runtime);
              return runtime.loadTable();
            })
        .when(catalogManager)
        .loadTable(runtime.getTableIdentifier().getIdentifier());

    assertNull(queue.pollTask(thread(1), 0));

    assertAll(
        () -> assertEquals(OptimizingStatus.IDLE, runtime.getOptimizingStatus()),
        () -> assertNull(runtime.getOptimizingProcess()),
        () -> assertEquals(lastOptimizedSnapshotId, runtime.getLastOptimizedSnapshotId()));
  }

  @Test
  void testReenabledTableCanBeEvaluatedAndScheduledAgain() throws Exception {
    DefaultTableRuntime runtime = createRuntime("reenable_pending");
    disableOptimizing(runtime);
    queue = createQueue(runtime);
    assertEquals(OptimizingStatus.IDLE, runtime.getOptimizingStatus());
    UnkeyedTable table = (UnkeyedTable) runtime.loadTable().originalTable();
    table.updateProperties().set("self-optimizing.enabled", "true").commit();
    TableService service = mock(TableService.class);
    doReturn(runtime.loadTable()).when(service).loadTable(runtime.getTableIdentifier());
    TableRuntimeRefreshExecutor executor = new TableRuntimeRefreshExecutor(service, 1, 60000, 1);
    try {
      executor.execute(runtime);
    } finally {
      executor.gracefulShutdown();
    }

    assertEquals(OptimizingStatus.PENDING, runtime.getOptimizingStatus());
    queue.refreshTable(runtime);
    assertNotNull(queue.pollTask(thread(1), 0));
  }

  @Test
  void testDisablingRunningProcessStillClosesIt() throws Exception {
    DefaultTableRuntime runtime = createRuntime("disable_running");
    queue = createQueue(runtime);
    assertNotNull(queue.pollTask(thread(1), 0));
    long processId = runtime.getProcessId();
    long lastOptimizedSnapshotId = runtime.getLastOptimizedSnapshotId();
    TableConfiguration originalConfig = runtime.getTableConfiguration();
    disableOptimizing(runtime);
    TableRuntimeRefreshExecutor executor =
        new TableRuntimeRefreshExecutor(mock(TableService.class), 1, 60000, 1);
    try {
      executor.handleConfigChanged(runtime, originalConfig);
    } finally {
      executor.gracefulShutdown();
    }

    assertAll(
        () -> assertEquals(OptimizingStatus.IDLE, runtime.getOptimizingStatus()),
        () -> assertNull(runtime.getOptimizingProcess()),
        () ->
            assertEquals(
                ProcessStatus.CLOSED,
                getAs(TableProcessMapper.class, mapper -> mapper.getProcessMeta(processId))
                    .getStatus()),
        () -> assertEquals(lastOptimizedSnapshotId, runtime.getLastOptimizedSnapshotId()));
  }

  private void disableOptimizing(DefaultTableRuntime runtime) {
    UnkeyedTable table = (UnkeyedTable) runtime.loadTable().originalTable();
    table.updateProperties().set("self-optimizing.enabled", "false").commit();
    runtime.refresh(runtime.loadTable());
    assertFalse(runtime.getOptimizingConfig().isEnabled());
  }

  private DefaultTableRuntime restoreRuntime(DefaultTableRuntime runtime) {
    ServerTableIdentifier identifier = runtime.getTableIdentifier();
    return new DefaultTableRuntime(
        new DefaultTableRuntimeStore(
            identifier,
            getAs(TableRuntimeMapper.class, mapper -> mapper.selectRuntime(identifier.getId())),
            DefaultTableRuntime.REQUIRED_STATES,
            getAs(TableRuntimeMapper.class, TableRuntimeMapper::selectAllStates).stream()
                .filter(state -> state.getTableId() == identifier.getId())
                .collect(Collectors.toList())),
        runtime::loadTable);
  }

  private DefaultTableRuntime createRuntime(String name) throws Exception {
    UnkeyedTable table = IcebergV3TestTables.create(temp.resolve(name), 2);
    table
        .updateProperties()
        .set("self-optimizing.enabled", "true")
        .set(TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT, "2")
        .commit();
    ServerTableIdentifier identifier =
        ServerTableIdentifier.of("test_catalog", "test_database", name, TableFormat.ICEBERG);
    IcebergTable amoroTable =
        IcebergTable.newIcebergTable(
            identifier.getIdentifier(),
            table,
            TableMetaStore.builder().withConfiguration(new Configuration()).build(),
            Collections.emptyMap());
    doReturn(amoroTable).when(catalogManager).loadTable(identifier.getIdentifier());

    TableRuntimeMeta meta = new TableRuntimeMeta();
    meta.setGroupName("default");
    meta.setStatusCode(OptimizingStatus.PENDING.getCode());
    doAs(TableMetaMapper.class, mapper -> mapper.insertTable(identifier));
    tableIds.add(identifier.getId());
    meta.setTableId(identifier.getId());
    doAs(TableRuntimeMapper.class, mapper -> mapper.insertRuntime(meta));
    DefaultTableRuntimeStore store =
        new DefaultTableRuntimeStore(
            identifier, meta, DefaultTableRuntime.REQUIRED_STATES, Collections.emptyList());
    DefaultTableRuntime runtime = new DefaultTableRuntime(store, () -> amoroTable);
    runtime.refresh(amoroTable);
    assertTrue(runtime.getOptimizingConfig().isEnabled());
    assertEquals(2, IcebergV3TestTables.dataFiles(amoroTable.originalTable()).size());
    return runtime;
  }

  private OptimizingQueue createQueue(DefaultTableRuntime runtime) {
    // Run planning synchronously. pollTask(..., 0) still drives planning and then
    // fetches the produced task, without sleeps or a background executor.
    return new OptimizingQueue(
        catalogManager,
        new ResourceGroup.Builder("default", "local").build(),
        group -> 1,
        Runnable::run,
        Collections.singletonList(runtime),
        1);
  }

  private OptimizerThread thread(int id) {
    return new OptimizerThread(id, null) {
      @Override
      public String getToken() {
        return "recovery-test";
      }
    };
  }

  private static class TestPersistence {
    private static final String[] TABLES_WITH_TABLE_ID = {
      "task_runtime",
      "optimizing_task_quota",
      "optimizing_process_state",
      "table_process_state",
      "table_process",
      "table_runtime_state",
      "table_runtime",
      "table_metadata",
      "table_identifier"
    };

    void cleanUp(Set<Long> tableIds) {
      if (tableIds.isEmpty()) {
        return;
      }

      try (SqlSession session = SqlSessionFactoryProvider.getInstance().get().openSession(true)) {
        for (String table : TABLES_WITH_TABLE_ID) {
          try (PreparedStatement statement =
              session
                  .getConnection()
                  .prepareStatement("DELETE FROM " + table + " WHERE table_id = ?")) {
            for (Long tableId : tableIds) {
              statement.setLong(1, tableId);
              statement.addBatch();
            }
            statement.executeBatch();
          }
        }
      } catch (SQLException e) {
        throw new RuntimeException("Failed to clean up optimizing recovery test data", e);
      }
    }
  }
}
