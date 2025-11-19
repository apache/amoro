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

import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.GROUP_TAG;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_COMMITTING_TABLES;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_EXECUTING_TABLES;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_EXECUTING_TASKS;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_IDLE_TABLES;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_MEMORY_BYTES_ALLOCATED;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_OPTIMIZER_INSTANCES;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_PENDING_TABLES;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_PENDING_TASKS;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_PLANING_TABLES;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_THREADS;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.api.OptimizerRegisterInfo;
import org.apache.amoro.api.OptimizingTaskId;
import org.apache.amoro.api.OptimizingTaskResult;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.metrics.Gauge;
import org.apache.amoro.metrics.MetricKey;
import org.apache.amoro.metrics.MetricRegistry;
import org.apache.amoro.optimizing.RewriteFilesOutput;
import org.apache.amoro.optimizing.TableOptimizing;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.server.manager.MetricManager;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.amoro.server.resource.OptimizerThread;
import org.apache.amoro.server.resource.QuotaProvider;
import org.apache.amoro.server.table.AMSTableTestBase;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.SerializationUtil;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.data.Record;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@RunWith(Parameterized.class)
public class TestOptimizingQueue extends AMSTableTestBase {

  private final Executor planExecutor = Executors.newSingleThreadExecutor();
  private final QuotaProvider quotaProvider = resourceGroup -> 1;
  private final long MAX_POLLING_TIME = 5000;

  private final OptimizerThread optimizerThread =
      new OptimizerThread(1, null) {

        @Override
        public String getToken() {
          return "aah";
        }
      };

  public TestOptimizingQueue(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper, true);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(false, true)}
    };
  }

  protected static ResourceGroup testResourceGroup() {
    return new ResourceGroup.Builder("test", "local").build();
  }

  @Before
  public void setUp() {
    // Clean up any existing metrics for the test resource group before each test
    // to avoid "Metric is already been registered" errors
    MetricRegistry registry = MetricManager.getInstance().getGlobalRegistry();
    String testGroupName = testResourceGroup().getName();

    // Unregister all metrics for the test resource group
    List<MetricKey> keysToRemove = new ArrayList<>();
    for (MetricKey key : registry.getMetrics().keySet()) {
      if (key.getDefine().getName().startsWith("optimizer_group_")
          && testGroupName.equals(key.valueOfTag(GROUP_TAG))) {
        keysToRemove.add(key);
      }
    }
    keysToRemove.forEach(registry::unregister);
  }

  protected OptimizingQueue buildOptimizingGroupService(DefaultTableRuntime tableRuntime) {
    return new OptimizingQueue(
        CATALOG_MANAGER,
        testResourceGroup(),
        quotaProvider,
        planExecutor,
        Collections.singletonList(tableRuntime),
        1);
  }

  private OptimizingQueue buildOptimizingGroupService() {
    return new OptimizingQueue(
        CATALOG_MANAGER,
        testResourceGroup(),
        quotaProvider,
        planExecutor,
        Collections.emptyList(),
        1);
  }

  @Test
  public void testPollNoTask() {
    DefaultTableRuntime tableRuntimeMeta =
        buildTableRuntimeMeta(OptimizingStatus.PENDING, defaultResourceGroup());
    OptimizingQueue queue = buildOptimizingGroupService(tableRuntimeMeta);
    Assert.assertNull(queue.pollTask(optimizerThread, 0));
    queue.dispose();
  }

  @Test
  public void testRefreshAndReleaseTable() {
    OptimizingQueue queue = buildOptimizingGroupService();
    Assert.assertEquals(0, queue.getSchedulingPolicy().getTableRuntimeMap().size());
    DefaultTableRuntime tableRuntime =
        buildTableRuntimeMeta(OptimizingStatus.IDLE, defaultResourceGroup());
    queue.refreshTable(tableRuntime);
    Assert.assertEquals(1, queue.getSchedulingPolicy().getTableRuntimeMap().size());
    Assert.assertTrue(
        queue.getSchedulingPolicy().getTableRuntimeMap().containsKey(serverTableIdentifier()));

    queue.releaseTable(tableRuntime);
    Assert.assertEquals(0, queue.getSchedulingPolicy().getTableRuntimeMap().size());

    queue.refreshTable(tableRuntime);
    Assert.assertEquals(1, queue.getSchedulingPolicy().getTableRuntimeMap().size());
    queue.dispose();
  }

  @Test
  public void testPollTask() {
    DefaultTableRuntime tableRuntime = initTableWithFiles();
    OptimizingQueue queue = buildOptimizingGroupService(tableRuntime);

    // 1.poll task
    TaskRuntime<?> task = queue.pollTask(optimizerThread, MAX_POLLING_TIME);

    Assert.assertNotNull(task);
    Assert.assertEquals(TaskRuntime.Status.SCHEDULED, task.getStatus());
    Assert.assertNull(queue.pollTask(optimizerThread, 0));
    queue.dispose();
  }

  @Test
  public void testPollTaskWithOverQuotaDisabled() {
    DefaultTableRuntime tableRuntime = initTableWithPartitionedFiles();
    OptimizingQueue queue =
        new OptimizingQueue(
            CATALOG_MANAGER,
            testResourceGroup(),
            resourceGroup -> 2,
            planExecutor,
            Collections.singletonList(tableRuntime),
            1);

    TaskRuntime<?> task = queue.pollTask(optimizerThread, MAX_POLLING_TIME, false);
    Assert.assertNotNull(task);
    Assert.assertEquals(TaskRuntime.Status.SCHEDULED, task.getStatus());
    queue.ackTask(task.getTaskId(), optimizerThread);
    Assert.assertEquals(
        1, queue.collectTasks(t -> t.getStatus() == TaskRuntime.Status.ACKED).size());
    Assert.assertNotNull(task);

    TaskRuntime<?> task2 = queue.pollTask(optimizerThread, MAX_POLLING_TIME, false);
    Assert.assertNull(task2);

    queue.completeTask(
        optimizerThread,
        buildOptimizingTaskResult(task.getTaskId(), optimizerThread.getThreadId()));
    Assert.assertEquals(TaskRuntime.Status.SUCCESS, task.getStatus());

    TaskRuntime<?> retryTask = queue.pollTask(optimizerThread, MAX_POLLING_TIME);
    Assert.assertNotNull(retryTask);

    queue.dispose();
  }

  @Test
  public void testPollTaskWithOverQuotaEnabled() {
    DefaultTableRuntime tableRuntime = initTableWithPartitionedFiles();
    OptimizingQueue queue =
        new OptimizingQueue(
            CATALOG_MANAGER,
            testResourceGroup(),
            resourceGroup -> 2,
            planExecutor,
            Collections.singletonList(tableRuntime),
            1);

    TaskRuntime<?> task = queue.pollTask(optimizerThread, MAX_POLLING_TIME);
    Assert.assertNotNull(task);
    Assert.assertEquals(TaskRuntime.Status.SCHEDULED, task.getStatus());
    queue.ackTask(task.getTaskId(), optimizerThread);
    Assert.assertEquals(
        1, queue.collectTasks(t -> t.getStatus() == TaskRuntime.Status.ACKED).size());
    Assert.assertNotNull(task);

    TaskRuntime<?> task2 = queue.pollTask(optimizerThread, MAX_POLLING_TIME, true);
    Assert.assertNotNull(task2);

    queue.completeTask(
        optimizerThread,
        buildOptimizingTaskResult(task.getTaskId(), optimizerThread.getThreadId()));
    Assert.assertEquals(TaskRuntime.Status.SUCCESS, task.getStatus());
    TaskRuntime<?> task4 = queue.pollTask(optimizerThread, MAX_POLLING_TIME, false);
    Assert.assertNull(task4);
    TaskRuntime<?> retryTask = queue.pollTask(optimizerThread, MAX_POLLING_TIME, true);
    Assert.assertNotNull(retryTask);
    queue.dispose();
  }

  @Test
  public void testQuotaSchedulePolicy() throws InterruptedException {
    DefaultTableRuntime tableRuntime = initTableWithFiles();

    OptimizingQueue queue =
        new OptimizingQueue(
            CATALOG_MANAGER,
            testResourceGroup(),
            resourceGroup -> 2,
            planExecutor,
            Collections.singletonList(tableRuntime),
            1);
    TaskRuntime<?> task = queue.pollTask(optimizerThread, MAX_POLLING_TIME);
    queue.ackTask(task.getTaskId(), optimizerThread);
    Assert.assertEquals(
        1, queue.collectTasks(t -> t.getStatus() == TaskRuntime.Status.ACKED).size());
    Assert.assertNotNull(task);
    Assert.assertTrue(tableRuntime.getTableIdentifier().getId() == task.getTableId());
    queue.completeTask(
        optimizerThread,
        buildOptimizingTaskResult(task.getTaskId(), optimizerThread.getThreadId()));
    Assert.assertEquals(TaskRuntime.Status.SUCCESS, task.getStatus());
    OptimizingProcess optimizingProcess = tableRuntime.getOptimizingProcess();
    Assert.assertEquals(ProcessStatus.RUNNING, optimizingProcess.getStatus());
    optimizingProcess.commit();
    Assert.assertEquals(ProcessStatus.SUCCESS, optimizingProcess.getStatus());
    Assert.assertNull(tableRuntime.getOptimizingProcess());

    // waiting for min-plan-interval and minor-trigger-interval
    Thread.sleep(500);
    tableRuntime = initTableWithPartitionedFiles();
    ServerTableIdentifier serverTableIdentifier =
        ServerTableIdentifier.of(
            org.apache.amoro.table.TableIdentifier.of(
                serverTableIdentifier().getCatalog(), "db", "new_table"),
            TableFormat.ICEBERG);
    serverTableIdentifier.setId(100L);
    DefaultTableRuntime tableRuntime2 = createTable(serverTableIdentifier);
    queue.refreshTable(tableRuntime2);
    queue.refreshTable(tableRuntime);

    // Poll two tasks and verify they come from different tables
    // The order may vary due to async planning, so we check both possibilities
    TaskRuntime<?> task2 = queue.pollTask(optimizerThread, MAX_POLLING_TIME);
    Assert.assertNotNull(task2);
    TaskRuntime<?> task3 = queue.pollTask(optimizerThread, MAX_POLLING_TIME);
    Assert.assertNotNull(task3);

    // Verify that the two tasks come from the two different tables
    long tableId1 = tableRuntime.getTableIdentifier().getId();
    long tableId2 = tableRuntime2.getTableIdentifier().getId();
    long task2TableId = task2.getTableId();
    long task3TableId = task3.getTableId();

    Assert.assertTrue(
        "Task2 should come from one of the two tables",
        task2TableId == tableId1 || task2TableId == tableId2);
    Assert.assertTrue(
        "Task3 should come from one of the two tables",
        task3TableId == tableId1 || task3TableId == tableId2);
    Assert.assertNotEquals(
        "Task2 and Task3 should come from different tables", task2TableId, task3TableId);
    queue.dispose();
    tableRuntime2.dispose();
  }

  @Test
  public void testRetryTask() {
    DefaultTableRuntime tableRuntimeMeta = initTableWithFiles();
    OptimizingQueue queue = buildOptimizingGroupService(tableRuntimeMeta);

    // 1.poll task
    TaskRuntime<?> task = queue.pollTask(optimizerThread, MAX_POLLING_TIME);
    Assert.assertNotNull(task);

    for (int i = 0; i < TableProperties.SELF_OPTIMIZING_EXECUTE_RETRY_NUMBER_DEFAULT; i++) {
      queue.retryTask(task);
      TaskRuntime<?> retryTask = queue.pollTask(optimizerThread, MAX_POLLING_TIME);
      Assert.assertEquals(retryTask.getTaskId(), task.getTaskId());
      queue.ackTask(task.getTaskId(), optimizerThread);
      queue.completeTask(
          optimizerThread,
          buildOptimizingTaskFailed(task.getTaskId(), optimizerThread.getThreadId()));
      Assert.assertEquals(TaskRuntime.Status.PLANNED, task.getStatus());
    }

    queue.retryTask(task);
    TaskRuntime<?> retryTask = queue.pollTask(optimizerThread, MAX_POLLING_TIME);
    Assert.assertEquals(retryTask.getTaskId(), task.getTaskId());
    queue.ackTask(task.getTaskId(), optimizerThread);
    queue.completeTask(
        optimizerThread,
        buildOptimizingTaskFailed(task.getTaskId(), optimizerThread.getThreadId()));
    Assert.assertEquals(TaskRuntime.Status.FAILED, task.getStatus());
    queue.dispose();
  }

  @Test
  public void testCommitTask() {
    DefaultTableRuntime tableRuntime = initTableWithFiles();
    OptimizingQueue queue = buildOptimizingGroupService(tableRuntime);
    Assert.assertEquals(0, queue.collectTasks().size());

    TaskRuntime<?> task = queue.pollTask(optimizerThread, MAX_POLLING_TIME);
    queue.ackTask(task.getTaskId(), optimizerThread);
    Assert.assertEquals(
        1, queue.collectTasks(t -> t.getStatus() == TaskRuntime.Status.ACKED).size());
    Assert.assertNotNull(task);
    queue.completeTask(
        optimizerThread,
        buildOptimizingTaskResult(task.getTaskId(), optimizerThread.getThreadId()));
    Assert.assertEquals(TaskRuntime.Status.SUCCESS, task.getStatus());

    // 7.commit
    OptimizingProcess optimizingProcess = tableRuntime.getOptimizingProcess();
    Assert.assertEquals(ProcessStatus.RUNNING, optimizingProcess.getStatus());
    optimizingProcess.commit();
    Assert.assertEquals(ProcessStatus.SUCCESS, optimizingProcess.getStatus());
    Assert.assertNull(tableRuntime.getOptimizingProcess());

    // 8.commit again, throw exceptions, and status not changed.
    Assert.assertThrows(IllegalStateException.class, optimizingProcess::commit);
    Assert.assertEquals(ProcessStatus.SUCCESS, optimizingProcess.getStatus());

    Assert.assertEquals(0, queue.collectTasks().size());
    queue.dispose();
  }

  @Test
  public void testCommitTaskWithFailed() {
    DefaultTableRuntime tableRuntime = initTableWithPartitionedFiles();
    OptimizingQueue queue = buildOptimizingGroupService(tableRuntime);
    Assert.assertEquals(0, queue.collectTasks().size());

    TaskRuntime<?> firstTask = queue.pollTask(optimizerThread, MAX_POLLING_TIME);
    queue.ackTask(firstTask.getTaskId(), optimizerThread);
    Assert.assertEquals(
        1, queue.collectTasks(t -> t.getStatus() == TaskRuntime.Status.ACKED).size());
    Assert.assertNotNull(firstTask);
    queue.completeTask(
        optimizerThread,
        buildOptimizingTaskResult(firstTask.getTaskId(), optimizerThread.getThreadId()));
    Assert.assertEquals(TaskRuntime.Status.SUCCESS, firstTask.getStatus());

    queue.pollTask(optimizerThread, MAX_POLLING_TIME);
    TaskRuntime<?> task = queue.pollTask(optimizerThread, MAX_POLLING_TIME);
    Assert.assertNotNull(task);

    for (int i = 0; i < TableProperties.SELF_OPTIMIZING_EXECUTE_RETRY_NUMBER_DEFAULT; i++) {
      queue.retryTask(task);
      TaskRuntime<?> retryTask = queue.pollTask(optimizerThread, MAX_POLLING_TIME);
      Assert.assertEquals(retryTask.getTaskId(), task.getTaskId());
      queue.ackTask(task.getTaskId(), optimizerThread);
      queue.completeTask(
          optimizerThread,
          buildOptimizingTaskFailed(task.getTaskId(), optimizerThread.getThreadId()));
      Assert.assertEquals(TaskRuntime.Status.PLANNED, task.getStatus());
    }

    queue.retryTask(task);
    TaskRuntime<?> retryTask = queue.pollTask(optimizerThread, MAX_POLLING_TIME);
    Assert.assertEquals(retryTask.getTaskId(), task.getTaskId());
    queue.ackTask(retryTask.getTaskId(), optimizerThread);

    OptimizingProcess optimizingProcess = tableRuntime.getOptimizingProcess();
    queue.completeTask(
        optimizerThread,
        buildOptimizingTaskFailed(task.getTaskId(), optimizerThread.getThreadId()));
    Assert.assertEquals(TaskRuntime.Status.FAILED, task.getStatus());
    optimizingProcess.commit();
    Assert.assertEquals(ProcessStatus.FAILED, optimizingProcess.getStatus());
    Assert.assertNull(tableRuntime.getOptimizingProcess());
    Assert.assertEquals(0, queue.collectTasks().size());
    queue.dispose();
  }

  @Test
  public void testCollectingTasks() {
    DefaultTableRuntime tableRuntime = initTableWithFiles();
    OptimizingQueue queue = buildOptimizingGroupService(tableRuntime);
    Assert.assertEquals(0, queue.collectTasks().size());

    TaskRuntime<?> task = queue.pollTask(optimizerThread, MAX_POLLING_TIME);
    Assert.assertNotNull(task);
    Assert.assertEquals(1, queue.collectTasks().size());
    Assert.assertEquals(
        1, queue.collectTasks(t -> t.getStatus() == TaskRuntime.Status.SCHEDULED).size());
    queue.dispose();
  }

  @Test
  public void testTaskAndTableMetrics() {
    DefaultTableRuntime tableRuntime = initTableWithFiles();
    OptimizingQueue queue = buildOptimizingGroupService(tableRuntime);
    MetricRegistry registry = MetricManager.getInstance().getGlobalRegistry();
    Map<String, String> tagValues = ImmutableMap.of(GROUP_TAG, testResourceGroup().getName());

    Gauge<Integer> queueTasksGauge =
        (Gauge<Integer>)
            registry.getMetrics().get(new MetricKey(OPTIMIZER_GROUP_PENDING_TASKS, tagValues));
    Gauge<Integer> executingTasksGauge =
        (Gauge<Integer>)
            registry.getMetrics().get(new MetricKey(OPTIMIZER_GROUP_EXECUTING_TASKS, tagValues));
    Gauge<Long> planingTablesGauge =
        (Gauge<Long>)
            registry.getMetrics().get(new MetricKey(OPTIMIZER_GROUP_PLANING_TABLES, tagValues));
    Gauge<Long> pendingTablesGauge =
        (Gauge<Long>)
            registry.getMetrics().get(new MetricKey(OPTIMIZER_GROUP_PENDING_TABLES, tagValues));
    Gauge<Long> executingTablesGauge =
        (Gauge<Long>)
            registry.getMetrics().get(new MetricKey(OPTIMIZER_GROUP_EXECUTING_TABLES, tagValues));
    Gauge<Long> idleTablesGauge =
        (Gauge<Long>)
            registry.getMetrics().get(new MetricKey(OPTIMIZER_GROUP_IDLE_TABLES, tagValues));
    Gauge<Long> committingTablesGauge =
        (Gauge<Long>)
            registry.getMetrics().get(new MetricKey(OPTIMIZER_GROUP_COMMITTING_TABLES, tagValues));

    Assert.assertEquals(0, queueTasksGauge.getValue().longValue());
    Assert.assertEquals(0, executingTasksGauge.getValue().longValue());
    Assert.assertEquals(0, planingTablesGauge.getValue().longValue());
    Assert.assertEquals(1, pendingTablesGauge.getValue().longValue());
    Assert.assertEquals(0, executingTablesGauge.getValue().longValue());
    Assert.assertEquals(0, idleTablesGauge.getValue().longValue());
    Assert.assertEquals(0, committingTablesGauge.getValue().longValue());

    TaskRuntime<?> task = queue.pollTask(optimizerThread, MAX_POLLING_TIME);
    Assert.assertNotNull(task);
    Assert.assertEquals(1, queueTasksGauge.getValue().longValue());
    Assert.assertEquals(0, executingTasksGauge.getValue().longValue());
    Assert.assertEquals(0, planingTablesGauge.getValue().longValue());
    Assert.assertEquals(0, pendingTablesGauge.getValue().longValue());
    Assert.assertEquals(1, executingTablesGauge.getValue().longValue());
    Assert.assertEquals(0, idleTablesGauge.getValue().longValue());
    Assert.assertEquals(0, committingTablesGauge.getValue().longValue());

    queue.ackTask(task.getTaskId(), optimizerThread);
    Assert.assertEquals(0, queueTasksGauge.getValue().longValue());
    Assert.assertEquals(1, executingTasksGauge.getValue().longValue());
    Assert.assertEquals(0, planingTablesGauge.getValue().longValue());
    Assert.assertEquals(0, pendingTablesGauge.getValue().longValue());
    Assert.assertEquals(1, executingTablesGauge.getValue().longValue());
    Assert.assertEquals(0, idleTablesGauge.getValue().longValue());
    Assert.assertEquals(0, committingTablesGauge.getValue().longValue());

    queue.completeTask(
        optimizerThread,
        buildOptimizingTaskResult(task.getTaskId(), optimizerThread.getThreadId()));
    Assert.assertEquals(0, queueTasksGauge.getValue().longValue());
    Assert.assertEquals(0, executingTasksGauge.getValue().longValue());
    Assert.assertEquals(0, planingTablesGauge.getValue().longValue());
    Assert.assertEquals(0, pendingTablesGauge.getValue().longValue());
    Assert.assertEquals(1, executingTablesGauge.getValue().longValue());
    Assert.assertEquals(0, idleTablesGauge.getValue().longValue());
    Assert.assertEquals(1, committingTablesGauge.getValue().longValue());

    OptimizingProcess optimizingProcess = tableRuntime.getOptimizingProcess();
    optimizingProcess.commit();
    Assert.assertEquals(0, queueTasksGauge.getValue().longValue());
    Assert.assertEquals(0, executingTasksGauge.getValue().longValue());
    Assert.assertEquals(0, planingTablesGauge.getValue().longValue());
    Assert.assertEquals(0, pendingTablesGauge.getValue().longValue());
    Assert.assertEquals(0, executingTablesGauge.getValue().longValue());
    Assert.assertEquals(1, idleTablesGauge.getValue().longValue());
    Assert.assertEquals(0, committingTablesGauge.getValue().longValue());
    queue.dispose();
  }

  @Test
  public void testAddAndRemoveOptimizers() {

    OptimizingQueue queue = buildOptimizingGroupService();
    MetricRegistry registry = MetricManager.getInstance().getGlobalRegistry();
    Map<String, String> tagValues = ImmutableMap.of(GROUP_TAG, testResourceGroup().getName());
    OptimizerRegisterInfo optimizerRegisterInfo =
        new OptimizerRegisterInfo(
            2, 2048, System.currentTimeMillis(), testResourceGroup().getName());
    OptimizerInstance optimizer = new OptimizerInstance(optimizerRegisterInfo, "test_container");

    Gauge<Integer> optimizerCountGauge =
        (Gauge<Integer>)
            registry
                .getMetrics()
                .get(new MetricKey(OPTIMIZER_GROUP_OPTIMIZER_INSTANCES, tagValues));
    Gauge<Long> optimizerMemoryGauge =
        (Gauge<Long>)
            registry
                .getMetrics()
                .get(new MetricKey(OPTIMIZER_GROUP_MEMORY_BYTES_ALLOCATED, tagValues));
    Gauge<Long> optimizerThreadsGauge =
        (Gauge<Long>) registry.getMetrics().get(new MetricKey(OPTIMIZER_GROUP_THREADS, tagValues));

    queue.addOptimizer(optimizer);
    Assert.assertEquals(1, optimizerCountGauge.getValue().longValue());
    Assert.assertEquals((long) 2048 * 1024 * 1024, optimizerMemoryGauge.getValue().longValue());
    Assert.assertEquals(2, optimizerThreadsGauge.getValue().longValue());

    queue.removeOptimizer(optimizer);
    Assert.assertEquals(0, optimizerCountGauge.getValue().longValue());
    Assert.assertEquals(0, optimizerMemoryGauge.getValue().longValue());
    Assert.assertEquals(0, optimizerThreadsGauge.getValue().longValue());
    queue.dispose();
  }

  protected DefaultTableRuntime initTableWithFiles() {
    MixedTable mixedTable =
        (MixedTable) tableService().loadTable(serverTableIdentifier()).originalTable();
    mixedTable
        .updateProperties()
        .set(TableProperties.SELF_OPTIMIZING_MIN_PLAN_INTERVAL, "10")
        .set(TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_INTERVAL, "10")
        .commit();
    appendData(mixedTable.asUnkeyedTable(), 1);
    appendData(mixedTable.asUnkeyedTable(), 2);
    DefaultTableRuntime tableRuntime =
        buildTableRuntimeMeta(OptimizingStatus.PENDING, defaultResourceGroup());

    tableRuntime.refresh(tableService().loadTable(serverTableIdentifier()));
    return tableRuntime;
  }

  protected DefaultTableRuntime initTableWithPartitionedFiles() {
    MixedTable mixedTable =
        (MixedTable) tableService().loadTable(serverTableIdentifier()).originalTable();
    appendPartitionedData(mixedTable.asUnkeyedTable(), 1);
    appendPartitionedData(mixedTable.asUnkeyedTable(), 2);
    DefaultTableRuntime tableRuntime =
        buildTableRuntimeMeta(OptimizingStatus.PENDING, defaultResourceGroup());

    tableRuntime.refresh(tableService().loadTable(serverTableIdentifier()));
    return tableRuntime;
  }

  private DefaultTableRuntime buildTableRuntimeMeta(
      OptimizingStatus status, ResourceGroup resourceGroup) {
    DefaultTableRuntime tableRuntime =
        (DefaultTableRuntime) tableService().getRuntime(serverTableIdentifier().getId());
    tableRuntime
        .store()
        .begin()
        .updateStatusCode(code -> status.getCode())
        .updateGroup(any -> resourceGroup.getName())
        .commit();
    return tableRuntime;
  }

  private void appendPartitionedData(UnkeyedTable table, int id) {
    ArrayList<Record> newRecords =
        Lists.newArrayList(
            MixedDataTestHelpers.createRecord(
                table.schema(), id, "111", 0L, "2022-01-01T12:00:00"));
    newRecords.add(
        MixedDataTestHelpers.createRecord(table.schema(), id, "222", 0L, "2022-01-02T12:00:00"));
    newRecords.add(
        MixedDataTestHelpers.createRecord(table.schema(), id, "333", 0L, "2022-01-03T12:00:00"));
    List<DataFile> dataFiles = MixedDataTestHelpers.writeBaseStore(table, 0L, newRecords, false);
    AppendFiles appendFiles = table.newAppend();
    dataFiles.forEach(appendFiles::appendFile);
    appendFiles.commit();
  }

  private void appendData(UnkeyedTable table, int id) {
    ArrayList<Record> newRecords =
        Lists.newArrayList(
            MixedDataTestHelpers.createRecord(
                table.schema(), id, "111", 0L, "2022-01-01T12:00:00"));
    List<DataFile> dataFiles = MixedDataTestHelpers.writeBaseStore(table, 0L, newRecords, false);
    AppendFiles appendFiles = table.newAppend();
    dataFiles.forEach(appendFiles::appendFile);
    appendFiles.commit();
  }

  private DefaultTableRuntime createTable(ServerTableIdentifier serverTableIdentifier) {
    org.apache.iceberg.catalog.Catalog catalog =
        catalogTestHelper().buildIcebergCatalog(catalogMeta());
    catalog.createTable(
        TableIdentifier.of(
            serverTableIdentifier.getDatabase(), serverTableIdentifier.getTableName()),
        tableTestHelper().tableSchema(),
        tableTestHelper().partitionSpec(),
        tableTestHelper().tableProperties());
    exploreTableRuntimes();
    serverTableIdentifier =
        tableManager()
            .getServerTableIdentifier(serverTableIdentifier.getIdentifier().buildTableIdentifier());

    MixedTable mixedTable =
        (MixedTable) tableService().loadTable(serverTableIdentifier).originalTable();
    appendPartitionedData(mixedTable.asUnkeyedTable(), 1);
    appendPartitionedData(mixedTable.asUnkeyedTable(), 2);

    DefaultTableRuntime tableRuntime =
        (DefaultTableRuntime) tableService().getRuntime(serverTableIdentifier.getId());

    tableRuntime
        .store()
        .begin()
        .updateGroup(any -> defaultResourceGroup().getName())
        .updateStatusCode(any -> OptimizingStatus.PENDING.getCode())
        .commit();

    tableRuntime.refresh(tableService().loadTable(serverTableIdentifier));

    return tableRuntime;
  }

  private OptimizingTaskResult buildOptimizingTaskResult(OptimizingTaskId taskId, int threadId) {
    TableOptimizing.OptimizingOutput output = new RewriteFilesOutput(null, null, null);
    OptimizingTaskResult optimizingTaskResult = new OptimizingTaskResult(taskId, threadId);
    optimizingTaskResult.setTaskOutput(SerializationUtil.simpleSerialize(output));
    return optimizingTaskResult;
  }

  private OptimizingTaskResult buildOptimizingTaskFailed(OptimizingTaskId taskId, int threadId) {
    OptimizingTaskResult optimizingTaskResult = new OptimizingTaskResult(taskId, threadId);
    optimizingTaskResult.setErrorMessage("error");
    return optimizingTaskResult;
  }
}
