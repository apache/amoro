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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.api.OptimizingTaskResult;
import org.apache.amoro.formats.iceberg.IcebergTable;
import org.apache.amoro.formats.iceberg.IcebergV3TestTables;
import org.apache.amoro.io.IcebergDataTestHelpers;
import org.apache.amoro.optimizing.RewriteFilesOutput;
import org.apache.amoro.optimizing.RewriteStageTask;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.persistence.mapper.TableRuntimeMapper;
import org.apache.amoro.server.process.TableProcessMeta;
import org.apache.amoro.server.resource.OptimizerThread;
import org.apache.amoro.server.scheduler.inline.TableRuntimeRefreshExecutor;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.DefaultTableRuntimeStore;
import org.apache.amoro.server.table.DerbyPersistence;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.SerializationUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.DeleteFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

class TestIcebergV3OptimizingPlanning extends PersistentBase {
  @TempDir private Path temp;

  private final TestPersistence persistence = new TestPersistence();
  private final CatalogManager catalogManager = mock(CatalogManager.class);
  private OptimizingQueue queue;

  @AfterEach
  void cleanUp() {
    if (queue != null) {
      queue.dispose();
    }
    persistence.cleanUp();
  }

  @Test
  void testV3IsNotScheduledAndV2StillGetsTask() throws Exception {
    DefaultTableRuntime v3 = createRuntime("v3", 3);
    UnkeyedTable table = (UnkeyedTable) v3.loadTable().originalTable();
    String metadataLocation = IcebergV3TestTables.metadataLocation(table);
    byte[] metadataBytes = IcebergV3TestTables.metadataBytes(table);
    queue = createQueue(v3);

    TaskRuntime<?> rejectedTask = queue.pollTask(thread(1), 0);

    // Add the supported table after the V3 planning attempt, without removing V3.
    DefaultTableRuntime v2 = createRuntime("v2", 2);
    queue.refreshTable(v2);
    TaskRuntime<?> supportedTask = queue.pollTask(thread(2), 0);

    assertAll(
        () -> assertNull(rejectedTask, "An initially V3 table received an optimizing task"),
        () -> assertEquals(0L, v3.getProcessId(), "V3 planning created a process"),
        () ->
            assertTrue(
                queue
                    .collectTasks(task -> task.getTableId() == v3.getTableIdentifier().getId())
                    .isEmpty()),
        () -> assertFalse(v3.getOptimizingStatus().isProcessing()),
        () ->
            assertTrue(
                v3.getOptimizingConfig().isEnabled(), "Do not rewrite the user's enabled setting"),
        () -> assertTrue(v3.getLastPlanTime() > 0, "The V3 planning attempt did not run"),
        () -> verify(catalogManager).loadTable(v3.getTableIdentifier().getIdentifier()),
        () -> assertScheduledTask(v2, supportedTask),
        () -> IcebergV3TestTables.assertV3MetadataUnchanged(table, metadataLocation, metadataBytes),
        () -> assertEquals(Arrays.asList(1, 2), IcebergV3TestTables.readIds(table)));
  }

  @Test
  void testV2PlanningSchedulesRealInputFiles() throws Exception {
    DefaultTableRuntime runtime = createRuntime("v2", 2);
    UnkeyedTable table = (UnkeyedTable) runtime.loadTable().originalTable();
    String metadataLocation = IcebergV3TestTables.metadataLocation(table);
    byte[] metadataBytes = IcebergV3TestTables.metadataBytes(table);
    queue = createQueue(runtime);

    TaskRuntime<?> task = queue.pollTask(thread(1), 0);

    assertScheduledTask(runtime, task);
    table.refresh();
    assertAll(
        () -> assertEquals(metadataLocation, IcebergV3TestTables.metadataLocation(table)),
        () -> assertArrayEquals(metadataBytes, IcebergV3TestTables.metadataBytes(table)),
        () -> assertEquals(Arrays.asList(1, 2), IcebergV3TestTables.readIds(table)));
  }

  @ParameterizedTest(name = "refresh initially V{0} from IDLE")
  @ValueSource(ints = {3, 2})
  void testRefreshOnlyAdmitsSupportedTables(int formatVersion) throws Exception {
    DefaultTableRuntime runtime = createRuntime("refresh", formatVersion, OptimizingStatus.IDLE);
    AmoroTable<?> amoroTable = runtime.loadTable();
    UnkeyedTable table = (UnkeyedTable) amoroTable.originalTable();
    String metadataLocation = IcebergV3TestTables.metadataLocation(table);
    byte[] metadataBytes = IcebergV3TestTables.metadataBytes(table);
    long lastOptimizedSnapshotId = runtime.getLastOptimizedSnapshotId();
    assertEquals(OptimizingStatus.IDLE, runtime.getOptimizingStatus());
    assertEquals(0, runtime.getPendingInput().getDataFileCount());
    assertNotEquals(runtime.getCurrentSnapshotId(), lastOptimizedSnapshotId);

    TableService tableService = mock(TableService.class);
    doReturn(amoroTable).when(tableService).loadTable(runtime.getTableIdentifier());
    TableRuntimeRefreshExecutor executor =
        new TableRuntimeRefreshExecutor(tableService, 1, 60000L, 1);
    try {
      // Call the real refresh/evaluation path without starting periodic scheduling.
      executor.execute(runtime);
    } finally {
      executor.gracefulShutdown();
    }

    verify(tableService).loadTable(runtime.getTableIdentifier());
    table.refresh();
    assertAll(
        () -> {
          if (formatVersion == 3) {
            assertNotEquals(
                OptimizingStatus.PENDING,
                runtime.getOptimizingStatus(),
                "Refresh admitted a V3 table as an optimizing candidate");
          } else {
            assertEquals(OptimizingStatus.PENDING, runtime.getOptimizingStatus());
          }
        },
        () ->
            assertEquals(formatVersion == 3 ? 0 : 2, runtime.getPendingInput().getDataFileCount()),
        () -> assertFalse(runtime.getOptimizingStatus().isProcessing()),
        () -> assertEquals(0L, runtime.getProcessId()),
        () -> assertTrue(runtime.getOptimizingConfig().isEnabled()),
        () -> assertEquals(lastOptimizedSnapshotId, runtime.getLastOptimizedSnapshotId()),
        () -> assertEquals(metadataLocation, IcebergV3TestTables.metadataLocation(table)),
        () -> assertArrayEquals(metadataBytes, IcebergV3TestTables.metadataBytes(table)),
        () -> assertEquals(Arrays.asList(1, 2), IcebergV3TestTables.readIds(table)));
  }

  private DefaultTableRuntime createRuntime(String name, int formatVersion) throws Exception {
    return createRuntime(name, formatVersion, OptimizingStatus.PENDING);
  }

  @Test
  void testV3ReadOnlySummaryStillCollected() throws Exception {
    DefaultTableRuntime runtime = createRuntime("summary", 2, OptimizingStatus.IDLE);
    UnkeyedTable table = (UnkeyedTable) runtime.loadTable().originalTable();
    table
        .updateProperties()
        .set("self-optimizing.enabled", "false")
        .set("table-summary.enabled", "true")
        .commit();
    AmoroTable<?> loaded = upgradedTable(runtime, table);
    UnkeyedTable current = (UnkeyedTable) loaded.originalTable();
    String location = IcebergV3TestTables.metadataLocation(current);
    byte[] bytes = IcebergV3TestTables.metadataBytes(current);
    long lastOptimized = runtime.getLastOptimizedSnapshotId();

    executeRefresh(runtime, loaded);

    TableRuntimeMeta meta =
        getAs(
            TableRuntimeMapper.class,
            mapper -> mapper.selectRuntime(runtime.getTableIdentifier().getId()));
    assertAll(
        () -> assertEquals(2, meta.getTableSummary().getTotalFileCount()),
        () -> assertFalse(runtime.getOptimizingConfig().isEnabled()),
        () -> assertEquals(OptimizingStatus.IDLE, runtime.getOptimizingStatus()),
        () -> assertEquals(0, runtime.getPendingInput().getDataFileCount()),
        () -> assertEquals(lastOptimized, runtime.getLastOptimizedSnapshotId()),
        () -> IcebergV3TestTables.assertV3MetadataUnchanged(current, location, bytes));
  }

  @Test
  void testRefreshPersistsUnsupportedProcessReason() throws Exception {
    DefaultTableRuntime runtime = createRuntime("running", 2);
    UnkeyedTable table = (UnkeyedTable) runtime.loadTable().originalTable();
    queue = createQueue(runtime);
    assertNotNull(queue.pollTask(thread(1), 0));
    long processId = runtime.getProcessId();
    long lastOptimized = runtime.getLastOptimizedSnapshotId();
    AmoroTable<?> loaded = upgradedTable(runtime, table);
    UnkeyedTable current = (UnkeyedTable) loaded.originalTable();
    String location = IcebergV3TestTables.metadataLocation(current);
    byte[] bytes = IcebergV3TestTables.metadataBytes(current);

    executeRefresh(runtime, loaded);

    TableProcessMeta meta =
        getAs(TableProcessMapper.class, mapper -> mapper.getProcessMeta(processId));
    assertAll(
        () -> assertEquals(ProcessStatus.CLOSED, meta.getStatus()),
        () ->
            assertTrue(
                meta.getFailMessage() != null
                    && meta.getFailMessage().contains("Unsupported Iceberg format version: 3")),
        () -> assertNull(runtime.getOptimizingProcess()),
        () -> assertEquals(OptimizingStatus.IDLE, runtime.getOptimizingStatus()),
        () -> assertEquals(lastOptimized, runtime.getLastOptimizedSnapshotId()),
        () -> IcebergV3TestTables.assertV3MetadataUnchanged(current, location, bytes));
  }

  private AmoroTable<?> upgradedTable(DefaultTableRuntime runtime, UnkeyedTable table)
      throws Exception {
    return IcebergTable.newIcebergTable(
        runtime.getTableIdentifier().getIdentifier(),
        IcebergV3TestTables.upgradeToV3(table),
        TableMetaStore.builder().withConfiguration(new Configuration()).build(),
        Collections.emptyMap());
  }

  private void executeRefresh(DefaultTableRuntime runtime, AmoroTable<?> loaded) {
    TableService service = mock(TableService.class);
    doReturn(loaded).when(service).loadTable(runtime.getTableIdentifier());
    TableRuntimeRefreshExecutor executor = new TableRuntimeRefreshExecutor(service, 1, 60000L, 1);
    try {
      executor.execute(runtime);
    } finally {
      executor.gracefulShutdown();
    }
  }

  @ParameterizedTest(name = "recover {0} on V{1}, catalog unavailable: {2}")
  @CsvSource({
    "tasks,3,false",
    "commit,3,false",
    "tasks,2,false",
    "commit,2,false",
    "tasks,2,true",
    "commit,2,true"
  })
  void testOptimizingRecoveryChecksCurrentVersion(
      String stage, int version, boolean catalogUnavailable) throws Exception {
    DefaultTableRuntime runtime = createRuntime("recovery", 2);
    UnkeyedTable table = (UnkeyedTable) runtime.loadTable().originalTable();
    queue = createQueue(runtime);
    OptimizerThread worker = thread(1);
    TaskRuntime<?> task = queue.pollTask(worker, 0);
    assertNotNull(task);
    if (stage.equals("commit")) {
      queue.ackTask(task.getTaskId(), worker);
      RewriteFilesOutput output =
          new RewriteFilesOutput(
              IcebergDataTestHelpers.insert(table, IcebergV3TestTables.records(table, 1, 2))
                  .dataFiles(),
              new DeleteFile[0],
              null);
      OptimizingTaskResult result =
          new OptimizingTaskResult(task.getTaskId(), worker.getThreadId());
      result.setTaskOutput(SerializationUtil.simpleSerialize(output));
      queue.completeTask(worker, result);
      // Reproduce a restart after persisting the result but before persisting COMMITTING.
      runtime
          .store()
          .begin()
          .updateStatusCode(ignored -> OptimizingStatus.MINOR_OPTIMIZING.getCode())
          .commit();
    } else {
      queue.retryTask(task);
    }
    queue.dispose();
    UnkeyedTable current = version == 3 ? IcebergV3TestTables.upgradeToV3(table) : table;
    IcebergTable loaded =
        IcebergTable.newIcebergTable(
            runtime.getTableIdentifier().getIdentifier(),
            current,
            TableMetaStore.builder().withConfiguration(new Configuration()).build(),
            Collections.emptyMap());
    doReturn(loaded).when(catalogManager).loadTable(runtime.getTableIdentifier().getIdentifier());
    if (catalogUnavailable) {
      // The recovery lookup fails once; the catalog is available again at commit time.
      doThrow(new IllegalStateException("Catalog temporarily unavailable"))
          .doReturn(loaded)
          .when(catalogManager)
          .loadTable(runtime.getTableIdentifier().getIdentifier());
    }
    String location = IcebergV3TestTables.metadataLocation(current);
    byte[] bytes = IcebergV3TestTables.metadataBytes(current);
    ServerTableIdentifier identifier = runtime.getTableIdentifier();
    DefaultTableRuntime restored =
        new DefaultTableRuntime(
            new DefaultTableRuntimeStore(
                identifier,
                getAs(TableRuntimeMapper.class, mapper -> mapper.selectRuntime(identifier.getId())),
                DefaultTableRuntime.REQUIRED_STATES,
                getAs(TableRuntimeMapper.class, TableRuntimeMapper::selectAllStates).stream()
                    .filter(state -> state.getTableId() == identifier.getId())
                    .collect(Collectors.toList())),
            () -> loaded);
    queue = createQueue(restored);
    if (stage.equals("commit")) {
      if (version != 3) {
        assertEquals(OptimizingStatus.COMMITTING, restored.getOptimizingStatus());
      }
      if (restored.getOptimizingStatus() == OptimizingStatus.COMMITTING) {
        restored.getOptimizingProcess().commit();
      }
      if (version != 3) {
        current.refresh();
        assertEquals(1, IcebergV3TestTables.dataFiles(current).size());
      }
    } else {
      TaskRuntime<?> resumed = queue.pollTask(thread(2), 0);
      if (version == 3) {
        assertNull(resumed, "Recovery rescheduled work on V3");
      } else {
        assertNotNull(resumed);
        assertEquals(task.getTaskId(), resumed.getTaskId());
      }
    }
    if (version == 3) {
      assertAll(
          () -> assertFalse(restored.getOptimizingStatus().isProcessing()),
          () -> assertTrue(queue.collectTasks().isEmpty()),
          () -> IcebergV3TestTables.assertV3MetadataUnchanged(current, location, bytes));
    }
    assertEquals(Arrays.asList(1, 2), IcebergV3TestTables.readIds(current));
  }

  private DefaultTableRuntime createRuntime(
      String name, int formatVersion, OptimizingStatus initialStatus) throws Exception {
    UnkeyedTable table = IcebergV3TestTables.create(temp.resolve(name), 2);
    // Configure while still V2 so setup does not corrupt the V3 fixture.
    table
        .updateProperties()
        .set("self-optimizing.enabled", "true")
        .set(TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT, "2")
        .commit();
    if (formatVersion == 3) {
      table = IcebergV3TestTables.upgradeToV3(table);
    }
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
    meta.setStatusCode(initialStatus.getCode());
    doAs(TableMetaMapper.class, mapper -> mapper.insertTable(identifier));
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

  private void assertScheduledTask(DefaultTableRuntime runtime, TaskRuntime<?> task) {
    verify(catalogManager).loadTable(runtime.getTableIdentifier().getIdentifier());
    assertNotNull(task, "A supported V2 table must still receive work");
    assertEquals(runtime.getTableIdentifier().getId(), task.getTableId());
    assertEquals(TaskRuntime.Status.SCHEDULED, task.getStatus());
    assertTrue(runtime.getProcessId() > 0);
    RewriteStageTask rewrite = (RewriteStageTask) task.getTaskDescriptor();
    assertEquals(2, rewrite.getInput().rewrittenDataFiles().length);
  }

  private OptimizerThread thread(int id) {
    return new OptimizerThread(id, null) {
      @Override
      public String getToken() {
        return "planning-test";
      }
    };
  }

  private static class TestPersistence extends DerbyPersistence {
    void cleanUp() {
      super.after();
    }
  }
}
