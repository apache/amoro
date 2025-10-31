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
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.TableCleanupMapper;
import org.apache.amoro.server.persistence.mapper.TableRuntimeMapper;
import org.apache.amoro.server.scheduler.PeriodicTableScheduler;
import org.apache.amoro.server.table.DerbyPersistence;
import org.apache.amoro.server.table.TableRuntimeHandler;
import org.apache.amoro.server.table.cleanup.CleanupOperation;
import org.apache.amoro.server.table.cleanup.TableCleanupProcessMeta;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class tests all aspects of cleanup operation handling in {@link
 * org.apache.amoro.server.scheduler.PeriodicTableScheduler}, including initialization, record
 * persistence and task execution.
 */
public class TestPeriodicTableSchedulerCleanup extends PersistentBase {

  private static DerbyPersistence persistence;
  private static final String TEST_CATALOG = "test_catalog";
  private static final String TEST_DB = "test_db";
  private static final String TEST_TABLE = "test_table";

  private static final TableRuntimeHandler TEST_HANDLER =
      new TableRuntimeHandler() {
        @Override
        public void handleTableChanged(
            TableRuntime tableRuntime, OptimizingStatus originalStatus) {}

        @Override
        public void handleTableChanged(
            TableRuntime tableRuntime, TableConfiguration originalConfig) {}
      };

  @BeforeClass
  public static void setUp() {
    persistence = new DerbyPersistence();
  }

  @AfterClass
  public static void tearDown() {
    persistence = null;
  }

  /**
   * Create a test server table identifier with the given ID
   *
   * @param tableId the table ID
   * @return a ServerTableIdentifier instance
   */
  private ServerTableIdentifier createTableIdentifier(long tableId) {
    return ServerTableIdentifier.of(
        tableId, TEST_CATALOG, TEST_DB, TEST_TABLE + "_" + tableId, null);
  }

  /**
   * Create a test table runtime with the given identifier
   *
   * @param identifier the table identifier
   * @return a TableRuntime instance
   */
  private TableRuntime createTableRuntime(ServerTableIdentifier identifier) {
    return new TableRuntime() {
      @Override
      public List<? extends org.apache.amoro.process.TableProcessState> getProcessStates() {
        return Collections.emptyList();
      }

      @Override
      public List<? extends org.apache.amoro.process.TableProcessState> getProcessStates(
          org.apache.amoro.Action action) {
        return Collections.emptyList();
      }

      @Override
      public String getGroupName() {
        return "test_group";
      }

      @Override
      public ServerTableIdentifier getTableIdentifier() {
        return identifier;
      }

      @Override
      public TableConfiguration getTableConfiguration() {
        return new TableConfiguration();
      }

      @Override
      public void registerMetric(org.apache.amoro.metrics.MetricRegistry metricRegistry) {}

      @Override
      public void unregisterMetric() {}
    };
  }

  private void cleanUpTestData(List<Long> tableIds) {
    doAs(
        TableCleanupMapper.class,
        mapper -> {
          for (Long tableId : tableIds) {
            try {
              mapper.deleteTableCleanupProcesses(tableId);
            } catch (Exception e) {
              // Ignore if tables don't exist
            }
          }
        });
  }

  private void cleanUpTableRuntimeData(List<Long> tableIds) {
    doAs(
        TableRuntimeMapper.class,
        mapper -> {
          for (Long tableId : tableIds) {
            try {
              mapper.deleteRuntime(tableId);
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
    cleanUpTestData(testTableIds);
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
   * Create test data with specified table IDs
   *
   * @param tableIds table IDs to create data for
   * @return list of TableRuntime instances
   */
  private List<TableRuntime> createTestTableRuntimes(List<Long> tableIds) {
    List<TableRuntime> runtimes = new ArrayList<>();
    for (Long tableId : tableIds) {
      ServerTableIdentifier identifier = createTableIdentifier(tableId);
      runtimes.add(createTableRuntime(identifier));
    }
    return runtimes;
  }

  /**
   * Helper method to set up a basic test environment with a single table
   *
   * @param cleanupOperation the cleanup operation to use
   * @return PeriodicTableSchedulerTestBase and TableRuntime for testing
   */
  private TestSetup setupSingleTableTest(CleanupOperation cleanupOperation) {
    List<Long> testTableIds = Collections.singletonList(1L);
    prepareTestEnvironment(testTableIds);

    PeriodicTableSchedulerTestBase executor = createTestExecutor(cleanupOperation);
    ServerTableIdentifier identifier = createTableIdentifier(1L);
    TableRuntime tableRuntime = createTableRuntime(identifier);

    return new TestSetup(executor, identifier, tableRuntime, testTableIds);
  }

  /** Helper class to hold test setup data */
  private static class TestSetup {
    final PeriodicTableSchedulerTestBase executor;
    final ServerTableIdentifier identifier;
    final TableRuntime tableRuntime;
    final List<Long> tableIds;

    TestSetup(
        PeriodicTableSchedulerTestBase executor,
        ServerTableIdentifier identifier,
        TableRuntime tableRuntime,
        List<Long> tableIds) {
      this.executor = executor;
      this.identifier = identifier;
      this.tableRuntime = tableRuntime;
      this.tableIds = tableIds;
    }
  }

  private void insertCleanupRecord(long tableId, CleanupOperation operation, long cleanupTime) {
    long currentTime = System.currentTimeMillis();
    doAs(
        TableCleanupMapper.class,
        mapper -> {
          TableCleanupProcessMeta meta =
              new TableCleanupProcessMeta(
                  currentTime,
                  tableId,
                  TEST_CATALOG,
                  TEST_DB,
                  TEST_TABLE + "_" + tableId,
                  operation,
                  cleanupTime);
          mapper.insertTableCleanupProcess(meta);
        });
  }

  @Test
  public void testBatchQueryExistingCleanupInfoWithNoExistingRecords() {
    // Setup test environment
    List<Long> testTableIds = Arrays.asList(1L, 2L, 3L);
    prepareTestEnvironment(testTableIds);

    // Create test table runtimes
    List<TableRuntime> tableRuntimeList = createTestTableRuntimes(testTableIds);

    // Create test executor with DANGLING_DELETE_FILES_CLEANING cleanup operation
    PeriodicTableSchedulerTestBase executor =
        createTestExecutor(CleanupOperation.DANGLING_DELETE_FILES_CLEANING);

    // Directly invoke batchQueryExistingCleanupInfo method using reflection
    invokeBatchQueryExistingCleanupInfo(
        executor, tableRuntimeList, CleanupOperation.DANGLING_DELETE_FILES_CLEANING);

    // Cache should be empty as no existing records
    assertCacheEmpty(executor, "Cache should be empty when no existing cleanup records found");
  }

  @Test
  public void testBatchQueryExistingCleanupInfoWithAllExistingRecords() {
    // Setup test environment
    List<Long> testTableIds = Arrays.asList(1L, 2L, 3L);
    prepareTestEnvironment(testTableIds);

    // Create test table runtimes first to get actual table IDs
    List<TableRuntime> tableRuntimeList = createTestTableRuntimes(testTableIds);

    // Get actual table IDs from the runtime list
    long tableId1 = tableRuntimeList.get(0).getTableIdentifier().getId();
    long tableId2 = tableRuntimeList.get(1).getTableIdentifier().getId();
    long tableId3 = tableRuntimeList.get(2).getTableIdentifier().getId();

    // Insert existing cleanup records for all tables using actual table IDs
    long currentTime = System.currentTimeMillis();
    insertCleanupRecord(
        tableId1, CleanupOperation.DATA_EXPIRING, currentTime - 3600000L); // 1 hour ago
    insertCleanupRecord(
        tableId2, CleanupOperation.DATA_EXPIRING, currentTime - 1800000L); // 30 minutes ago
    insertCleanupRecord(
        tableId3, CleanupOperation.DATA_EXPIRING, currentTime - 900000L); // 15 minutes ago

    // Create test executor with DATA_EXPIRING cleanup operation
    PeriodicTableSchedulerTestBase executor = createTestExecutor(CleanupOperation.DATA_EXPIRING);

    // Directly invoke batchQueryExistingCleanupInfo method using reflection
    invokeBatchQueryExistingCleanupInfo(executor, tableRuntimeList, CleanupOperation.DATA_EXPIRING);

    // Verify that all existing cleanup records are loaded into cache
    Map<Long, Long> cache = executor.getCacheForTest();

    Assert.assertNotNull("Cache should not be null", cache);
    Assert.assertEquals("Cache should contain 3 entries", 3, cache.size());
    Assert.assertTrue("Cache should contain first table", cache.containsKey(tableId1));
    Assert.assertTrue("Cache should contain second table", cache.containsKey(tableId2));
    Assert.assertTrue("Cache should contain third table", cache.containsKey(tableId3));

    // Verify the cached times are correct
    Assert.assertEquals(
        "Table 1 cleanup time should match", currentTime - 3600000L, (long) cache.get(tableId1));
    Assert.assertEquals(
        "Table 2 cleanup time should match", currentTime - 1800000L, (long) cache.get(tableId2));
    Assert.assertEquals(
        "Table 3 cleanup time should match", currentTime - 900000L, (long) cache.get(tableId3));
  }

  @Test
  public void testBatchQueryExistingCleanupInfoWithDifferentCleanupOperations() {
    // Setup test environment
    List<Long> testTableIds = Arrays.asList(1L, 2L);
    prepareTestEnvironment(testTableIds);

    // Create test table runtimes first to get actual table IDs
    List<TableRuntime> tableRuntimeList = createTestTableRuntimes(testTableIds);

    // Get actual table IDs from the runtime list
    long tableId1 = tableRuntimeList.get(0).getTableIdentifier().getId();
    long tableId2 = tableRuntimeList.get(1).getTableIdentifier().getId();

    // Insert records with different cleanup operations using actual table IDs
    long currentTime = System.currentTimeMillis();
    insertCleanupRecord(
        tableId1,
        CleanupOperation.DANGLING_DELETE_FILES_CLEANING,
        currentTime - 3600000L); // 1 hour ago
    insertCleanupRecord(
        tableId1, CleanupOperation.DATA_EXPIRING, currentTime - 1800000L); // 30 minutes ago
    insertCleanupRecord(
        tableId2, CleanupOperation.DATA_EXPIRING, currentTime - 900000L); // 15 minutes ago

    // Create test executor with DATA_EXPIRING cleanup operation
    PeriodicTableSchedulerTestBase executor = createTestExecutor(CleanupOperation.DATA_EXPIRING);

    // Directly invoke batchQueryExistingCleanupInfo method using reflection
    invokeBatchQueryExistingCleanupInfo(executor, tableRuntimeList, CleanupOperation.DATA_EXPIRING);

    // Verify only DATA_EXPIRING records are loaded
    Map<Long, Long> cache = executor.getCacheForTest();

    Assert.assertNotNull("Cache should not be null", cache);
    Assert.assertEquals("Cache should contain 2 entries", 2, cache.size());
    Assert.assertTrue("Cache should contain first table", cache.containsKey(tableId1));
    Assert.assertTrue("Cache should contain second table", cache.containsKey(tableId2));

    // Verify the cached time matches DATA_EXPIRING operation
    Assert.assertEquals(
        "Table 1 cleanup time should match DATA_EXPIRING record",
        currentTime - 1800000L,
        (long) cache.get(tableId1));
    Assert.assertEquals(
        "Table 2 cleanup time should match DATA_EXPIRING record",
        currentTime - 900000L,
        (long) cache.get(tableId2));
  }

  private void invokeBatchQueryExistingCleanupInfo(
      PeriodicTableSchedulerTestBase executor,
      List<TableRuntime> tableRuntimes,
      CleanupOperation cleanupOperation) {
    try {
      Method method =
          PeriodicTableScheduler.class.getDeclaredMethod(
              "batchQueryExistingCleanupInfo", List.class, CleanupOperation.class);
      method.setAccessible(true);
      method.invoke(executor, tableRuntimes, cleanupOperation);
    } catch (Exception e) {
      throw new RuntimeException("Failed to invoke batchQueryExistingCleanupInfo method", e);
    }
  }

  private void assertCacheEmpty(PeriodicTableSchedulerTestBase executor, String message) {
    Map<Long, Long> cache = executor.getCacheForTest();
    Assert.assertTrue(message, cache != null && cache.isEmpty());
  }

  @Test
  public void testUpsertWithInsertOperation() {
    TestSetup setup = setupSingleTableTest(CleanupOperation.DATA_EXPIRING);

    // Execute a task which should insert the cleanup time record
    setup.executor.executeTaskForTest(setup.tableRuntime);

    // Check that the cleanup record was inserted
    List<TableCleanupProcessMeta> records =
        getAs(
            TableCleanupMapper.class,
            mapper ->
                mapper.selectTableIdAndLastCleanupEndTime(
                    setup.tableIds, CleanupOperation.DATA_EXPIRING));

    Assert.assertEquals(1, records.size());
    TableCleanupProcessMeta record = records.get(0);
    Assert.assertNotNull(
        "Last cleanup end time should not be null", record.getLastCleanupEndTime());
  }

  @Test
  public void testUpsertWithUpdateOperation() {
    TestSetup setup = setupSingleTableTest(CleanupOperation.DATA_EXPIRING);

    // Insert initial cleanup record
    long initialTime = System.currentTimeMillis() - 10000; // 10 seconds ago
    doAs(
        TableCleanupMapper.class,
        mapper -> {
          TableCleanupProcessMeta meta =
              new TableCleanupProcessMeta(
                  System.currentTimeMillis(),
                  1L,
                  TEST_CATALOG,
                  TEST_DB,
                  TEST_TABLE + "_1",
                  CleanupOperation.DATA_EXPIRING,
                  initialTime);
          mapper.insertTableCleanupProcess(meta);
        });

    // Execute task which should update the existing record
    setup.executor.executeTaskForTest(setup.tableRuntime);

    // Check that the cleanup record was updated (time should be more recent)
    List<TableCleanupProcessMeta> records =
        getAs(
            TableCleanupMapper.class,
            mapper ->
                mapper.selectTableIdAndLastCleanupEndTime(
                    setup.tableIds, CleanupOperation.DATA_EXPIRING));

    Assert.assertEquals(1, records.size());
    TableCleanupProcessMeta record = records.get(0);
    Assert.assertTrue(
        "Last cleanup end time should be updated", record.getLastCleanupEndTime() > initialTime);
  }

  @Test
  public void testShouldExecuteTaskWithNoPreviousCleanup() {
    TestSetup setup = setupSingleTableTest(CleanupOperation.SNAPSHOTS_EXPIRING);

    // Should execute when there's no previous cleanup time
    boolean shouldExecute =
        setup.executor.shouldExecuteTaskForTest(
            setup.tableRuntime, CleanupOperation.SNAPSHOTS_EXPIRING);
    Assert.assertTrue("Should execute when there's no previous cleanup time", shouldExecute);
  }

  @Test
  public void testShouldExecuteTaskWithPreviousCleanup() {
    // Setup test environment - only clean up table runtime data for this test
    List<Long> testTableIds = Collections.singletonList(1L);
    cleanUpTableRuntimeData(testTableIds);

    // Create test executor with SNAPSHOTS_EXPIRING cleanup operation
    PeriodicTableSchedulerTestBase executor =
        createTestExecutor(CleanupOperation.SNAPSHOTS_EXPIRING);

    // Add a previous cleanup time to the cache (more than 1 hour ago)
    long oneHourAgo = System.currentTimeMillis() - 60 * 60 * 1000L;
    executor.addToCacheForTest(1L, oneHourAgo);

    ServerTableIdentifier identifier = createTableIdentifier(1L);
    TableRuntime tableRuntime = createTableRuntime(identifier);

    // Should execute because enough time has passed since last cleanup
    boolean shouldExecute =
        executor.shouldExecuteTaskForTest(tableRuntime, CleanupOperation.SNAPSHOTS_EXPIRING);
    Assert.assertTrue(
        "Should execute when enough time has passed since last cleanup", shouldExecute);
  }

  @Test
  public void testShouldNotExecuteTaskWithRecentCleanup() {
    // Setup test environment - only clean up table runtime data for this test
    List<Long> testTableIds = Collections.singletonList(1L);
    cleanUpTableRuntimeData(testTableIds);

    // Create test executor with SNAPSHOTS_EXPIRING cleanup operation
    PeriodicTableSchedulerTestBase executor =
        createTestExecutor(CleanupOperation.SNAPSHOTS_EXPIRING);

    // Add a recent cleanup time to the cache (1 second ago)
    long recentCleanupTime = System.currentTimeMillis() - 1000L;
    executor.addToCacheForTest(1L, recentCleanupTime);

    ServerTableIdentifier identifier = createTableIdentifier(1L);
    TableRuntime tableRuntime = createTableRuntime(identifier);

    // Should not execute because not enough time has passed since last cleanup
    boolean shouldExecute =
        executor.shouldExecuteTaskForTest(tableRuntime, CleanupOperation.SNAPSHOTS_EXPIRING);
    Assert.assertFalse(
        "Should not execute when not enough time has passed since last cleanup", shouldExecute);
  }

  @Test
  public void testShouldExecuteTaskWithNoneOperation() {
    TestSetup setup = setupSingleTableTest(CleanupOperation.NONE);

    // Should always execute with NONE operation
    boolean shouldExecute =
        setup.executor.shouldExecuteTaskForTest(setup.tableRuntime, CleanupOperation.NONE);
    Assert.assertTrue("Should always execute with NONE operation", shouldExecute);
  }
}
