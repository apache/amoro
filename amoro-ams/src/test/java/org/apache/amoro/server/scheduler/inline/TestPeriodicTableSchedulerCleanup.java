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

package org.apache.amoro.server.scheduler.inline;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.persistence.mapper.TableRuntimeMapper;
import org.apache.amoro.server.process.TableProcessMeta;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.DefaultTableRuntimeStore;
import org.apache.amoro.server.table.TableRuntimeHandler;
import org.apache.amoro.server.table.cleanup.CleanupOperation;
import org.apache.amoro.table.TableRuntimeStore;
import org.apache.amoro.table.TableSummary;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class tests all aspects of cleanup operation handling in {@link
 * org.apache.amoro.server.scheduler.PeriodicTableScheduler}.
 */
public class TestPeriodicTableSchedulerCleanup extends PersistentBase {

  private static final String TEST_CATALOG = "test_catalog";
  private static final String TEST_DB = "test_db";
  private static final String TEST_TABLE = "test_table";

  static {
    try {
      Class.forName("org.apache.amoro.server.table.DerbyPersistence");
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize Derby persistence", e);
    }
  }

  private static final TableRuntimeHandler TEST_HANDLER =
      new TableRuntimeHandler() {
        @Override
        public void handleTableChanged(
            TableRuntime tableRuntime,
            org.apache.amoro.server.optimizing.OptimizingStatus originalStatus) {}

        @Override
        public void handleTableChanged(
            TableRuntime tableRuntime, TableConfiguration originalConfig) {}
      };

  /**
   * Create a test server table identifier with the given ID
   *
   * @param tableId the table ID
   * @return a ServerTableIdentifier instance
   */
  private ServerTableIdentifier createTableIdentifier(long tableId) {
    return ServerTableIdentifier.of(
        tableId, TEST_CATALOG, TEST_DB, TEST_TABLE + "_" + tableId, TableFormat.ICEBERG);
  }

  /**
   * Create a test DefaultTableRuntime with the given identifier
   *
   * @param identifier the table identifier
   * @return a DefaultTableRuntime instance
   */
  private DefaultTableRuntime createDefaultTableRuntime(ServerTableIdentifier identifier) {
    // Create table runtime meta
    TableRuntimeMeta meta = new TableRuntimeMeta();
    meta.setTableId(identifier.getId());
    meta.setGroupName("test_group");
    meta.setStatusCode(0);
    meta.setTableConfig(Collections.emptyMap());
    meta.setTableSummary(new TableSummary());

    // Create table runtime store
    TableRuntimeStore store =
        new DefaultTableRuntimeStore(
            identifier, meta, DefaultTableRuntime.REQUIRED_STATES, Collections.emptyList());

    return new DefaultTableRuntime(store);
  }

  private void cleanUpTableRuntimeData(List<Long> tableIds) {
    doAs(
        TableRuntimeMapper.class,
        mapper -> {
          for (Long tableId : tableIds) {
            try {
              mapper.deleteRuntime(tableId);
              mapper.removeAllTableStates(tableId);
            } catch (Exception e) {
              // Ignore if tables don't exist
            }
          }
        });
    doAs(
        TableMetaMapper.class,
        mapper -> {
          for (Long tableId : tableIds) {
            try {
              mapper.deleteTableIdById(tableId);
            } catch (Exception e) {
              // Ignore if tables don't exist
            }
          }
        });
  }

  /**
   * Prepare test environment by cleaning up test data and table runtime data
   *
   * @param testTableIds list of table IDs to clean up
   */
  private void prepareTestEnvironment(List<Long> testTableIds) {
    cleanUpTableRuntimeData(testTableIds);
  }

  /**
   * Create a test table executor
   *
   * @param cleanupOperation the cleanup operation to use
   * @param enabled whether the executor should be enabled
   * @return a new PeriodicTableSchedulerTestBase instance
   */
  private PeriodicTableSchedulerTestBase createTestExecutor(
      CleanupOperation cleanupOperation, boolean enabled) {
    return new PeriodicTableSchedulerTestBase(null, cleanupOperation, enabled);
  }

  /**
   * Create a test table executor with default enabled state (true)
   *
   * @param cleanupOperation the cleanup operation to use
   * @return a new PeriodicTableSchedulerTestBase instance
   */
  private PeriodicTableSchedulerTestBase createTestExecutor(CleanupOperation cleanupOperation) {
    return createTestExecutor(cleanupOperation, true);
  }

  /**
   * Test whether the executor should execute a task for a given table runtime and cleanup operation
   */
  @Test
  public void testShouldExecuteTaskWithNoPreviousCleanup() {
    List<CleanupOperation> operations =
        Arrays.asList(
            CleanupOperation.ORPHAN_FILES_CLEANING,
            CleanupOperation.DANGLING_DELETE_FILES_CLEANING,
            CleanupOperation.DATA_EXPIRING,
            CleanupOperation.SNAPSHOTS_EXPIRING);

    for (CleanupOperation operation : operations) {
      List<Long> testTableIds = Collections.singletonList(1L);
      prepareTestEnvironment(testTableIds);

      PeriodicTableSchedulerTestBase executor = createTestExecutor(operation);
      ServerTableIdentifier identifier = createTableIdentifier(1L);
      DefaultTableRuntime tableRuntime = createDefaultTableRuntime(identifier);

      boolean shouldExecute = executor.shouldExecuteTaskForTest(tableRuntime, operation);
      Assert.assertTrue(
          "Should execute when there's no previous cleanup time for operation " + operation,
          shouldExecute);
    }
  }

  /** Test should not execute task with recent cleanup */
  @Test
  public void testShouldNotExecuteTaskWithRecentCleanup() {
    List<CleanupOperation> operations =
        Arrays.asList(
            CleanupOperation.ORPHAN_FILES_CLEANING,
            CleanupOperation.DANGLING_DELETE_FILES_CLEANING,
            CleanupOperation.DATA_EXPIRING,
            CleanupOperation.SNAPSHOTS_EXPIRING);

    for (CleanupOperation operation : operations) {
      List<Long> testTableIds = Collections.singletonList(1L);
      cleanUpTableRuntimeData(testTableIds);

      PeriodicTableSchedulerTestBase executor = createTestExecutor(operation);

      // Create DefaultTableRuntime and set recent cleanup time
      ServerTableIdentifier identifier = createTableIdentifier(1L);
      DefaultTableRuntime tableRuntime = createDefaultTableRuntime(identifier);

      // Simulate recent cleanup
      long recentTime = System.currentTimeMillis() - 10000L;
      tableRuntime.updateLastCleanTime(operation, recentTime);

      boolean shouldExecute = executor.shouldExecuteTaskForTest(tableRuntime, operation);
      Assert.assertFalse(
          "Should not execute when recently cleaned up for operation " + operation, shouldExecute);
    }
  }

  /** Test should execute task with old cleanup */
  @Test
  public void testShouldExecuteTaskWithOldCleanup() {
    List<CleanupOperation> operations =
        Arrays.asList(
            CleanupOperation.ORPHAN_FILES_CLEANING,
            CleanupOperation.DANGLING_DELETE_FILES_CLEANING,
            CleanupOperation.DATA_EXPIRING,
            CleanupOperation.SNAPSHOTS_EXPIRING);

    for (CleanupOperation operation : operations) {
      List<Long> testTableIds = Collections.singletonList(1L);
      cleanUpTableRuntimeData(testTableIds);

      PeriodicTableSchedulerTestBase executor = createTestExecutor(operation);

      // Create DefaultTableRuntime and set old cleanup time
      ServerTableIdentifier identifier = createTableIdentifier(1L);
      DefaultTableRuntime tableRuntime = createDefaultTableRuntime(identifier);

      // Simulate old cleanup time (30 hours ago)
      long oldTime = System.currentTimeMillis() - 30 * 60 * 60 * 1000L;
      tableRuntime.updateLastCleanTime(operation, oldTime);

      boolean shouldExecute = executor.shouldExecuteTaskForTest(tableRuntime, operation);
      Assert.assertTrue(
          "Should execute when enough time has passed since last cleanup for operation "
              + operation,
          shouldExecute);
    }
  }

  @Test
  public void testShouldExecuteTaskWithNoneOperation() {
    List<Long> testTableIds = Collections.singletonList(1L);
    prepareTestEnvironment(testTableIds);

    PeriodicTableSchedulerTestBase executor = createTestExecutor(CleanupOperation.NONE);
    ServerTableIdentifier identifier = createTableIdentifier(1L);
    DefaultTableRuntime tableRuntime = createDefaultTableRuntime(identifier);

    // Should always execute with NONE operation
    boolean shouldExecute = executor.shouldExecuteTaskForTest(tableRuntime, CleanupOperation.NONE);
    Assert.assertTrue("Should always execute with NONE operation", shouldExecute);
  }

  /**
   * Validates that process info is correctly saved to table_process with proper status transitions.
   */
  @Test
  public void testCleanupProcessPersistence() {
    long baseTableId = 200L;
    List<CleanupOperation> operations =
        Arrays.asList(
            CleanupOperation.ORPHAN_FILES_CLEANING,
            CleanupOperation.DANGLING_DELETE_FILES_CLEANING,
            CleanupOperation.DATA_EXPIRING,
            CleanupOperation.SNAPSHOTS_EXPIRING);

    for (int i = 0; i < operations.size(); i++) {
      long tableId = baseTableId + i;
      CleanupOperation operation = operations.get(i);
      prepareTestEnvironment(Collections.singletonList(tableId));

      PeriodicTableSchedulerTestBase executor = createTestExecutor(operation);
      ServerTableIdentifier identifier = createTableIdentifier(tableId);
      DefaultTableRuntime tableRuntime = createDefaultTableRuntime(identifier);

      // Scenario 1: Create process - verify initial persisted state
      TableProcessMeta processMeta =
          executor.createCleanupProcessInfoForTest(tableRuntime, operation);
      TableProcessMeta persistedRunning = getProcessMeta(processMeta.getProcessId());

      Assert.assertEquals(ProcessStatus.RUNNING, persistedRunning.getStatus());

      // Scenario 2: Success completion - verify status update and timestamps
      executor.persistCleanupResultForTest(tableRuntime, operation, processMeta.copy(), null);
      TableProcessMeta persistedSuccess = getProcessMeta(processMeta.getProcessId());
      Assert.assertEquals(ProcessStatus.SUCCESS, persistedSuccess.getStatus());
      Assert.assertTrue(persistedSuccess.getCreateTime() < persistedSuccess.getFinishTime());

      // Scenario 3: Failure handling - verify error persistence
      processMeta = executor.createCleanupProcessInfoForTest(tableRuntime, operation);
      RuntimeException testError = new RuntimeException("Cleanup failed for " + operation);
      executor.persistCleanupResultForTest(tableRuntime, operation, processMeta.copy(), testError);

      TableProcessMeta persistedFailure = getProcessMeta(processMeta.getProcessId());
      Assert.assertEquals(ProcessStatus.FAILED, persistedFailure.getStatus());
      Assert.assertEquals(testError.getMessage(), persistedFailure.getFailMessage());

      cleanUpTableProcess(tableId);
    }
  }

  private TableProcessMeta getProcessMeta(long processId) {
    return getAs(TableProcessMapper.class, m -> m.getProcessMeta(processId));
  }

  /** Clean up table_process records for a specific table */
  private void cleanUpTableProcess(long tableId) {
    doAs(
        TableProcessMapper.class,
        mapper -> {
          mapper
              .listProcessMeta(tableId, null, null)
              .forEach(p -> mapper.deleteBefore(tableId, p.getProcessId()));
        });
  }
}
