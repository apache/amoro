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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.api.OptimizingTaskResult;
import org.apache.amoro.exception.PersistenceException;
import org.apache.amoro.formats.iceberg.IcebergTable;
import org.apache.amoro.formats.iceberg.IcebergV3TestTables;
import org.apache.amoro.io.IcebergDataTestHelpers;
import org.apache.amoro.optimizing.RewriteFilesOutput;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.persistence.NestedSqlSession;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.SqlSessionFactoryProvider;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.server.persistence.mapper.OptimizingProcessMapper;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.persistence.mapper.TableRuntimeMapper;
import org.apache.amoro.server.resource.OptimizerThread;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.DefaultTableRuntimeStore;
import org.apache.amoro.server.table.DerbyPersistence;
import org.apache.amoro.server.utils.CompressUtil;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.SerializationUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.iceberg.DeleteFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InvalidClassException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/** Verifies recovery after a persisted task output becomes incompatible with the current class. */
class TestOptimizingTaskRecovery extends PersistentBase {
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
  void testRecoveryClosesProcessWithIncompatibleTaskOutput() throws Exception {
    DefaultTableRuntime runtime = createRuntime("task_recovery");
    UnkeyedTable table = (UnkeyedTable) runtime.loadTable().originalTable();
    queue = createQueue(runtime);
    OptimizerThread worker = thread(1);
    TaskRuntime<?> task = queue.pollTask(worker, 0);
    assertNotNull(task);
    long processId = runtime.getProcessId();
    queue.ackTask(task.getTaskId(), worker);
    RewriteFilesOutput output =
        new RewriteFilesOutput(
            IcebergDataTestHelpers.insert(table, IcebergV3TestTables.records(table, 1, 2))
                .dataFiles(),
            new DeleteFile[0],
            null);
    OptimizingTaskResult result = new OptimizingTaskResult(task.getTaskId(), worker.getThreadId());
    result.setTaskOutput(SerializationUtil.simpleSerialize(output));
    queue.completeTask(worker, result);
    // Restart after persisting the result but before persisting the COMMITTING transition.
    runtime
        .store()
        .begin()
        .updateStatusCode(ignored -> OptimizingStatus.MINOR_OPTIMIZING.getCode())
        .commit();
    queue.dispose();
    queue = null;

    assertEquals(
        ProcessStatus.RUNNING,
        getAs(TableProcessMapper.class, mapper -> mapper.getProcessMeta(processId)).getStatus());
    assertEquals(
        OptimizingStatus.MINOR_OPTIMIZING.getCode(),
        getAs(
                TableRuntimeMapper.class,
                mapper -> mapper.selectRuntime(runtime.getTableIdentifier().getId()))
            .getStatusCode());
    changePersistedOutputSerialVersionUid(processId);
    PersistenceException failure =
        assertThrows(
            PersistenceException.class,
            () ->
                getAs(
                    OptimizingProcessMapper.class,
                    mapper ->
                        mapper.selectTaskRuntimes(
                            runtime.getTableIdentifier().getId(), processId)));
    Throwable rootCause = failure;
    while (rootCause.getCause() != null) {
      rootCause = rootCause.getCause();
    }
    assertEquals(InvalidClassException.class, rootCause.getClass());
    assertTrue(rootCause.getMessage().contains("org.apache.iceberg.BaseFile"));

    // Reconstruct the runtime from the DB as well, rather than reusing the in-memory process.
    ServerTableIdentifier identifier = runtime.getTableIdentifier();
    DefaultTableRuntime restored = restoreRuntime(runtime);
    queue = createQueue(restored);
    assertAll(
        () ->
            assertEquals(
                ProcessStatus.CLOSED,
                getAs(TableProcessMapper.class, mapper -> mapper.getProcessMeta(processId))
                    .getStatus(),
                "An unreadable process must be closed persistently"),
        () ->
            assertEquals(
                OptimizingStatus.IDLE,
                restored.getOptimizingStatus(),
                "An unreadable task must not leave the table optimizing"),
        () ->
            assertTrue(
                queue.getSchedulingPolicy().getTableRuntimeMap().containsKey(identifier),
                "The table must be eligible for planning again"));
  }

  @Test
  void testRecoveryDoesNotCloseProcessOnDatabaseFailure() throws Exception {
    DefaultTableRuntime runtime = createRuntime("database_failure");
    queue = createQueue(runtime);
    assertNotNull(queue.pollTask(thread(1), 0));
    long processId = runtime.getProcessId();
    OptimizingStatus status = runtime.getOptimizingStatus();
    queue.dispose();
    queue = null;
    DefaultTableRuntime restored = restoreRuntime(runtime);
    long tableId = runtime.getTableIdentifier().getId();

    // Fail only the task query, after the process metadata has been loaded successfully.
    try (SqlSession sql = SqlSessionFactoryProvider.getInstance().get().openSession()) {
      SqlSession session = spy(sql);
      OptimizingProcessMapper mapper = spy(sql.getMapper(OptimizingProcessMapper.class));
      doThrow(
              new org.apache.ibatis.exceptions.PersistenceException(
                  "Task query failed", new SQLException("Database unavailable")))
          .when(mapper)
          .selectTaskRuntimes(tableId, processId);
      doReturn(mapper).when(session).getMapper(OptimizingProcessMapper.class);
      try (NestedSqlSession ignored = NestedSqlSession.openSession(() -> session)) {
        queue = createQueue(restored);
        verify(mapper).selectTaskRuntimes(tableId, processId);
      }
    }

    assertAll(
        () ->
            assertEquals(
                ProcessStatus.RUNNING,
                getAs(TableProcessMapper.class, mapper -> mapper.getProcessMeta(processId))
                    .getStatus()),
        () -> assertEquals(status, restored.getOptimizingStatus()),
        () -> assertEquals(processId, restored.getProcessId()));
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

  private void changePersistedOutputSerialVersionUid(long processId) throws SQLException {
    try (SqlSession session = SqlSessionFactoryProvider.getInstance().get().openSession(true)) {
      byte[] serialized;
      try (PreparedStatement select =
          session
              .getConnection()
              .prepareStatement("SELECT rewrite_output FROM task_runtime WHERE process_id = ?")) {
        select.setLong(1, processId);
        try (ResultSet rows = select.executeQuery()) {
          assertTrue(rows.next());
          serialized = CompressUtil.unGzip(rows.getBytes(1));
          assertFalse(rows.next(), "The fixture must contain exactly one task");
        }
      }
      // Keep a valid object stream and GZIP envelope; change only the class descriptor UID.
      // This models a binary-incompatible upgrade without tying the test to an Iceberg release.
      assertNotNull(SerializationUtil.simpleDeserialize(serialized));
      byte[] className = "org.apache.iceberg.BaseFile".getBytes(StandardCharsets.UTF_8);
      int changed = 0;
      for (int i = 3; i + className.length + Long.BYTES <= serialized.length; i++) {
        if (serialized[i - 3] == 0x72
            && serialized[i - 2] == 0
            && serialized[i - 1] == className.length
            && Arrays.equals(serialized, i, i + className.length, className, 0, className.length)) {
          serialized[i + className.length] ^= 1;
          changed++;
        }
      }
      assertEquals(1, changed, "Expected one BaseFile class descriptor");
      try (PreparedStatement update =
          session
              .getConnection()
              .prepareStatement(
                  "UPDATE task_runtime SET rewrite_output = ? WHERE process_id = ?")) {
        update.setBytes(1, CompressUtil.gzip(serialized));
        update.setLong(2, processId);
        assertEquals(1, update.executeUpdate());
      }
    }
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
