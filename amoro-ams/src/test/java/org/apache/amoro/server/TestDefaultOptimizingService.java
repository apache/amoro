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

package org.apache.amoro.server;

import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_OPTIMIZER_INSTANCES;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_THREADS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.OptimizerProperties;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.api.OptimizerRegisterInfo;
import org.apache.amoro.api.OptimizingTask;
import org.apache.amoro.api.OptimizingTaskId;
import org.apache.amoro.api.OptimizingTaskResult;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.exception.ObjectNotExistsException;
import org.apache.amoro.exception.PluginRetryAuthException;
import org.apache.amoro.exception.TaskRuntimeException;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.metrics.Gauge;
import org.apache.amoro.metrics.MetricKey;
import org.apache.amoro.metrics.MetricRegistry;
import org.apache.amoro.optimizing.RewriteFilesOutput;
import org.apache.amoro.optimizing.TableOptimizing;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.server.manager.MetricManager;
import org.apache.amoro.server.optimizing.OptimizingQueue;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.optimizing.TaskRuntime;
import org.apache.amoro.server.persistence.SqlSessionFactoryProvider;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.server.persistence.mapper.OptimizerMapper;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.persistence.mapper.TableRuntimeMapper;
import org.apache.amoro.server.process.TableProcessMeta;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.amoro.server.scheduler.inline.TableRuntimeRefreshExecutor;
import org.apache.amoro.server.table.AMSTableTestBase;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.RuntimeHandlerChain;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.SerializationUtil;
import org.apache.ibatis.session.SqlSession;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.data.Record;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(Parameterized.class)
public class TestDefaultOptimizingService extends AMSTableTestBase {

  private static final Duration EXPIRATION_TEST_HEARTBEAT_TIMEOUT = Duration.ofMillis(800);
  private static final long OPTIMIZER_HEARTBEAT_INTERVAL_MS = 100;
  private static final long ASYNC_WAIT_TIMEOUT_MS = 10000;

  private final int THREAD_ID = 0;
  private String token;
  private Toucher toucher;
  private boolean customHeartbeatTimeout;

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(false, true)}
    };
  }

  public TestDefaultOptimizingService(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper, false);
  }

  @Before
  public void prepare() {
    toucher = new Toucher();
    createDatabase();
    createTable();
    initTableWithFiles();
    TableRuntimeRefresher refresher = new TableRuntimeRefresher();
    refresher.refreshPending();
    refresher.dispose();
  }

  @After
  public void clear() {
    try {
      if (toucher != null) {
        toucher.stop();
        toucher = null;
      }
      optimizerManager()
          .listOptimizers()
          .forEach(
              optimizer ->
                  optimizingService()
                      .deleteOptimizer(optimizer.getGroupName(), optimizer.getResourceId()));
      dropTable();
      dropDatabase();
    } catch (Exception e) {
      // ignore
    } finally {
      if (customHeartbeatTimeout) {
        disposeTableService();
        initTableService();
        customHeartbeatTimeout = false;
      }
    }
  }

  private void initTableWithFiles() {
    MixedTable mixedTable =
        (MixedTable) tableService().loadTable(serverTableIdentifier()).originalTable();
    appendData(mixedTable.asUnkeyedTable(), 1);
    appendData(mixedTable.asUnkeyedTable(), 2);
    DefaultTableRuntime runtime = getDefaultTableRuntime(serverTableIdentifier().getId());

    runtime.refresh(tableService().loadTable(serverTableIdentifier()));
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

  @Test
  public void testPollWithoutAuth() {
    // 1.poll task
    clear();
    Assertions.assertThrows(
        PluginRetryAuthException.class, () -> optimizingService().pollTask("whatever", THREAD_ID));
  }

  @Test
  public void testOrphanedOptimizerRecordMustNotBreakInitialization() {
    // An optimizer row whose resource group no longer exists (e.g. the group was dropped while
    // AMS was down) must be cleaned up instead of failing initialization with an NPE.
    OptimizerRegisterInfo registerInfo = buildRegisterInfo();
    registerInfo.setGroupName("group-dropped-while-ams-down");
    OptimizerInstance orphan = new OptimizerInstance(registerInfo, "local");
    OptimizerRegisterInfo emptyGroupRegisterInfo = buildRegisterInfo();
    emptyGroupRegisterInfo.setGroupName("");
    emptyGroupRegisterInfo.setResourceId("resource-with-empty-group");
    OptimizerInstance emptyGroupOrphan = new OptimizerInstance(emptyGroupRegisterInfo, "local");
    optimizerManager().createResourceGroup(new ResourceGroup.Builder("", "local").build());
    try (SqlSession session = SqlSessionFactoryProvider.getInstance().get().openSession(true)) {
      session.getMapper(OptimizerMapper.class).insertOptimizer(orphan);
      session.getMapper(OptimizerMapper.class).insertOptimizer(emptyGroupOrphan);
    }

    try {
      // Exercise the production startup path rather than calling the recovery helper directly.
      Assertions.assertDoesNotThrow(this::reload);

      List<OptimizerInstance> remaining;
      try (SqlSession session = SqlSessionFactoryProvider.getInstance().get().openSession(true)) {
        remaining = session.getMapper(OptimizerMapper.class).selectAll();
      }
      Assertions.assertFalse(
          remaining.stream().anyMatch(o -> o.getToken().equals(orphan.getToken())),
          "orphaned optimizer record should be removed during initialization");
      Assertions.assertFalse(
          remaining.stream().anyMatch(o -> o.getToken().equals(emptyGroupOrphan.getToken())),
          "an optimizer record with an empty group should always be removed");
    } finally {
      optimizingService().deleteOptimizer("", emptyGroupOrphan.getResourceId());
      optimizingService().deleteResourceGroup("");
      optimizerManager().deleteResourceGroup("");
    }
  }

  @Test
  public void testValidOptimizerRecordMustSurviveStaleQueueSnapshot() {
    String groupName = "group-created-by-another-ams";
    ResourceGroup group = new ResourceGroup.Builder(groupName, "local").build();
    optimizerManager().createResourceGroup(group);
    optimizingService().createResourceGroup(group);
    // Simulate a startup snapshot taken before another AMS created the persisted group.
    optimizingService().deleteResourceGroup(groupName);

    OptimizerRegisterInfo registerInfo = buildRegisterInfo();
    registerInfo.setGroupName(groupName);
    registerInfo.setResourceId("resource-created-by-another-ams");
    OptimizerInstance optimizer = new OptimizerInstance(registerInfo, "local");
    insertOptimizer(optimizer);

    try {
      optimizingService().registerOptimizers(Lists.newArrayList(optimizer));

      Assertions.assertTrue(
          optimizerExists(optimizer.getToken()),
          "a stale local queue snapshot must not delete a valid shared optimizer record");
    } finally {
      deleteOptimizerRecord(optimizer.getToken());
      optimizerManager().deleteResourceGroup(groupName);
    }
  }

  @Test
  public void testAuthenticateMustRejectStaleLocalQueueAfterGroupDeletion() {
    String groupName = "group-deleted-by-another-ams";
    ResourceGroup group = new ResourceGroup.Builder(groupName, "local").build();
    optimizerManager().createResourceGroup(group);
    optimizingService().createResourceGroup(group);
    // Keep the local queue but remove the shared database row, as can happen before watcher sync.
    optimizerManager().deleteResourceGroup(groupName);

    OptimizerRegisterInfo registerInfo = buildRegisterInfo();
    registerInfo.setGroupName(groupName);
    registerInfo.setResourceId("resource-for-deleted-group");

    try {
      Assertions.assertThrows(
          ObjectNotExistsException.class, () -> optimizingService().authenticate(registerInfo));
      Assertions.assertFalse(
          optimizerManager().listOptimizers().stream()
              .anyMatch(optimizer -> groupName.equals(optimizer.getGroupName())),
          "authentication must not persist an optimizer for a deleted resource group");
    } finally {
      optimizerManager().listOptimizers().stream()
          .filter(optimizer -> groupName.equals(optimizer.getGroupName()))
          .map(OptimizerInstance::getToken)
          .forEach(this::deleteOptimizerRecord);
      optimizingService().deleteResourceGroup(groupName);
    }
  }

  @Test
  public void testPollOnce() {
    // 1.poll task
    OptimizingTask task = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task);
    assertTaskStatus(TaskRuntime.Status.SCHEDULED);
    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());
    assertTaskStatus(TaskRuntime.Status.ACKED);

    TaskRuntime taskRuntime =
        optimizingService().listTasks(defaultResourceGroup().getName()).get(0);
    optimizingService().completeTask(token, buildOptimizingTaskResult(task.getTaskId()));
    assertTaskCompleted(taskRuntime);
  }

  @Test
  public void testPollTaskBlockedWhileDraining() {
    // A draining optimizer receives no new assignments even though a task is available; in-flight
    // completion paths (touch/ack/complete) are deliberately not blocked.
    optimizingService().beginGracefulDrain(token, Long.MAX_VALUE);
    Assertions.assertNull(optimizingService().pollTask(token, THREAD_ID));

    optimizingService().cancelDrain(token);
    Assertions.assertNotNull(optimizingService().pollTask(token, THREAD_ID));
  }

  @Test
  public void testDrainStartedDuringPollHandsTaskBack() {
    OptimizingTask polled = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(polled);
    TaskRuntime<?> taskRuntime =
        optimizingService().listTasks(defaultResourceGroup().getName()).stream()
            .filter(t -> t.getStatus() == TaskRuntime.Status.SCHEDULED)
            .findFirst()
            .orElse(null);
    Assertions.assertNotNull(taskRuntime);

    // The drain begins while a long-poll is parked inside the queue: the entry check has already
    // passed, so the post-poll guard must hand the fetched task back instead of assigning it.
    optimizingService().beginGracefulDrain(token, Long.MAX_VALUE);
    Assertions.assertNull(optimizingService().guardDrainedPoll(token, taskRuntime));
    Assertions.assertEquals(TaskRuntime.Status.PLANNED, taskRuntime.getStatus());
  }

  @Test
  public void testPollTaskTwice() {
    // 1.poll task
    OptimizingTask task = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task);

    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());
    // 3.fail task
    optimizingService()
        .completeTask(token, buildOptimizingTaskFailResult(task.getTaskId(), "unknown error"));
    assertTaskStatus(TaskRuntime.Status.PLANNED);

    // 4.retry poll task
    OptimizingTask task2 = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertEquals(task2.getTaskId(), task.getTaskId());
    Assertions.assertNotEquals(task2.getTaskInput(), task.getTaskInput());
    TableOptimizing.OptimizingInput input =
        SerializationUtil.simpleDeserialize(task.getTaskInput());
    TableOptimizing.OptimizingInput input2 =
        SerializationUtil.simpleDeserialize(task2.getTaskInput());
    Assertions.assertEquals(input2.toString(), input.toString());
    assertTaskStatus(TaskRuntime.Status.SCHEDULED);
    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());
    assertTaskStatus(TaskRuntime.Status.ACKED);

    TaskRuntime<?> taskRuntime =
        optimizingService().listTasks(defaultResourceGroup().getName()).get(0);
    optimizingService().completeTask(token, buildOptimizingTaskResult(task.getTaskId()));
    assertTaskCompleted(taskRuntime);
  }

  @Test
  public void testPollTaskThreeTimes() {
    // 1.poll task
    OptimizingTask task = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task);
    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());

    // 3.fail task
    optimizingService()
        .completeTask(token, buildOptimizingTaskFailResult(task.getTaskId(), "unknown error"));

    // 4.retry poll task
    OptimizingTask task2 = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertEquals(task2.getTaskId(), task.getTaskId());
    Assertions.assertNotEquals(task2.getTaskInput(), task.getTaskInput());
    TableOptimizing.OptimizingInput input =
        SerializationUtil.simpleDeserialize(task.getTaskInput());
    TableOptimizing.OptimizingInput input2 =
        SerializationUtil.simpleDeserialize(task2.getTaskInput());
    Assertions.assertEquals(input2.toString(), input.toString());

    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());
    optimizingService()
        .completeTask(token, buildOptimizingTaskFailResult(task.getTaskId(), "unknown error"));

    // retry again
    OptimizingTask task3 = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertEquals(task3.getTaskId(), task.getTaskId());
    Assertions.assertNotEquals(task2.getTaskInput(), task.getTaskInput());
    TableOptimizing.OptimizingInput input3 =
        SerializationUtil.simpleDeserialize(task2.getTaskInput());
    Assertions.assertEquals(input3.toString(), input.toString());
    assertTaskStatus(TaskRuntime.Status.SCHEDULED);
    // third time would be null
    Assertions.assertNull(optimizingService().pollTask(token, THREAD_ID));
    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());
    assertTaskStatus(TaskRuntime.Status.ACKED);

    TaskRuntime taskRuntime =
        optimizingService().listTasks(defaultResourceGroup().getName()).get(0);
    optimizingService().completeTask(token, buildOptimizingTaskResult(task.getTaskId()));
    assertTaskCompleted(taskRuntime);
  }

  @Test
  public void testTouch() throws InterruptedException {
    OptimizerInstance optimizer = optimizerManager().listOptimizers().get(0);
    long oldTouchTime = optimizer.getTouchTime();
    Thread.sleep(1);
    optimizingService().touch(token);
    OptimizerInstance optimizerAfterTouched = optimizerManager().listOptimizers().get(0);
    Assertions.assertTrue(optimizerAfterTouched.getTouchTime() > oldTouchTime);
  }

  @Test
  public void testHeartbeatExpiryClearsDrainState() throws InterruptedException {
    // An optimizer that dies mid-drain is unregistered by heartbeat expiry, a path that must
    // clear the drain state too: the token can never be matched again, so a leftover entry would
    // sit in the pending-removal set forever.
    rebootWithHeartbeatTimeout(EXPIRATION_TEST_HEARTBEAT_TIMEOUT);
    String drainingToken = token;
    optimizingService().beginGracefulDrain(drainingToken, Long.MAX_VALUE);
    toucher.stop();
    toucher = null;
    waitForOptimizerExpiration(drainingToken, ASYNC_WAIT_TIMEOUT_MS);
    Assertions.assertThrows(
        PluginRetryAuthException.class, () -> optimizingService().touch(drainingToken));
    Assertions.assertFalse(
        optimizingService().isDraining(drainingToken),
        "unregistration must clear the drain state of a dead optimizer");
  }

  @Test
  public void testUnregisterDoesNotFailWhenAuthenticationAlreadyRemoved() throws Exception {
    toucher.stop();
    toucher = null;
    OptimizerInstance optimizer = optimizerManager().listOptimizers().get(0);
    OptimizingQueue queue = (OptimizingQueue) optimizerState("optimizingQueueByToken").get(token);
    // Simulate another unregister call having already claimed the authentication entry.
    optimizerState("authOptimizers").remove(token);

    try {
      Assertions.assertDoesNotThrow(
          () ->
              optimizingService()
                  .deleteOptimizer(optimizer.getGroupName(), optimizer.getResourceId()));
    } finally {
      queue.removeOptimizer(optimizer);
    }
  }

  @Test
  public void testUnregisterCleansMetricsWhenTokenQueueAlreadyRemoved() throws Exception {
    toucher.stop();
    toucher = null;
    OptimizerInstance optimizer = optimizerManager().listOptimizers().get(0);
    // Simulate another unregister call having already claimed the token-to-queue entry.
    OptimizingQueue queue =
        (OptimizingQueue) optimizerState("optimizingQueueByToken").remove(token);
    Map<String, String> tagValues = Maps.newHashMap();
    tagValues.put("group", optimizer.getGroupName());
    MetricRegistry registry = MetricManager.getInstance().getGlobalRegistry();
    Gauge<Integer> optimizerCountGauge =
        (Gauge<Integer>)
            registry
                .getMetrics()
                .get(new MetricKey(OPTIMIZER_GROUP_OPTIMIZER_INSTANCES, tagValues));
    Gauge<Long> optimizerThreadsGauge =
        (Gauge<Long>) registry.getMetrics().get(new MetricKey(OPTIMIZER_GROUP_THREADS, tagValues));

    Assertions.assertEquals(1, optimizerCountGauge.getValue());
    Assertions.assertEquals(1L, optimizerThreadsGauge.getValue());
    try {
      optimizingService().deleteOptimizer(optimizer.getGroupName(), optimizer.getResourceId());
      Assertions.assertEquals(0, optimizerCountGauge.getValue());
      Assertions.assertEquals(0L, optimizerThreadsGauge.getValue());
    } finally {
      queue.removeOptimizer(optimizer);
    }
  }

  @Test
  public void testTouchTimeout() throws InterruptedException {
    rebootWithHeartbeatTimeout(EXPIRATION_TEST_HEARTBEAT_TIMEOUT);
    OptimizingTask task = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task);
    String expiredToken = token;
    toucher.stop();
    toucher = null;
    waitForOptimizerExpiration(expiredToken, ASYNC_WAIT_TIMEOUT_MS);
    Assertions.assertThrows(
        PluginRetryAuthException.class, () -> optimizingService().touch(expiredToken));
    Assertions.assertThrows(
        PluginRetryAuthException.class,
        () -> optimizingService().pollTask(expiredToken, THREAD_ID));
    // After optimizer expires, its tasks are immediately reset to PLANNED
    // because unregister happens before task scan in OptimizerKeeper
    waitForTaskStatus(TaskRuntime.Status.PLANNED, ASYNC_WAIT_TIMEOUT_MS);
    toucher = new Toucher();
    assertTaskStatus(TaskRuntime.Status.PLANNED);
    OptimizingTask task2 = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertEquals(task2.getTaskId(), task.getTaskId());
    TableOptimizing.OptimizingInput input =
        SerializationUtil.simpleDeserialize(task.getTaskInput());
    TableOptimizing.OptimizingInput input2 =
        SerializationUtil.simpleDeserialize(task2.getTaskInput());
    Assertions.assertEquals(input2.toString(), input.toString());
  }

  @Test
  public void testRebootAndPoll() throws InterruptedException {
    OptimizingTask task = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task);
    rebootWithHeartbeatTimeout(EXPIRATION_TEST_HEARTBEAT_TIMEOUT);

    // wait for last optimizer expiring
    waitForTaskStatus(TaskRuntime.Status.PLANNED, ASYNC_WAIT_TIMEOUT_MS);
    OptimizingTask task2 = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task2);
    Assertions.assertEquals(task2.getTaskId(), task.getTaskId());
    TableOptimizing.OptimizingInput input =
        SerializationUtil.simpleDeserialize(task.getTaskInput());
    TableOptimizing.OptimizingInput input2 =
        SerializationUtil.simpleDeserialize(task2.getTaskInput());
    Assertions.assertEquals(input2.toString(), input.toString());
  }

  @Test
  public void testAckAndCompleteTask() {
    OptimizingTask task = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task);
    // Completing before ack is now treated as a stale response and absorbed silently (see
    // TaskRuntime#complete): the result cannot be told apart from a stale completion for a task
    // that
    // was reset and re-scheduled to the same thread, so the task simply stays SCHEDULED.
    optimizingService().completeTask(token, buildOptimizingTaskResult(task.getTaskId()));
    assertTaskStatus(TaskRuntime.Status.SCHEDULED);

    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());

    TaskRuntime taskRuntime =
        optimizingService().listTasks(defaultResourceGroup().getName()).get(0);
    optimizingService().completeTask(token, buildOptimizingTaskResult(task.getTaskId()));
    assertTaskCompleted(taskRuntime);
  }

  // Reproduces the EXACT path of issue #4235 end-to-end with the real OptimizerKeeper: a live
  // optimizer (the Toucher keeps heartbeating) polls a task but its ack is delayed past
  // OPTIMIZER_TASK_ACK_TIMEOUT (5s in tests). The keeper, via the SCHEDULED + ackTimeout branch of
  // buildSuspendingPredication, resets the still-owned task to PLANNED. The late ack then arrives
  // and is rejected -- this is the "Task has been reset or not yet scheduled" from the issue log,
  // produced without any artificial retryTask() call.
  @Test
  public void testAckTimeoutResetThenLateAckRejected() throws InterruptedException {
    OptimizingTask task = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task);
    assertTaskStatus(TaskRuntime.Status.SCHEDULED); // polled but NOT acked

    // the optimizer stays alive, so waiting past the ack timeout hits
    // the SCHEDULED + ackTimeout branch rather than the optimizer-expired branch: the keeper resets
    // the task out from under the live optimizer
    waitForTaskStatus(TaskRuntime.Status.PLANNED, 20000);

    // the delayed ack arrives for the now-reset task -> rejected, exactly like the issue
    Assertions.assertThrows(
        TaskRuntimeException.class,
        () -> optimizingService().ackTask(token, THREAD_ID, task.getTaskId()));
  }

  @Test
  public void testExecuteTaskTimeOutAndRetry() throws InterruptedException {
    OptimizingTask task = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task);

    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());

    TaskRuntime taskRuntime =
        optimizingService().listTasks(defaultResourceGroup().getName()).get(0);
    assertTaskStatus(TaskRuntime.Status.ACKED);

    // In this test, OPTIMIZER_TASK_EXECUTE_TIMEOUT is set to 30 seconds.
    waitForTaskStatus(TaskRuntime.Status.PLANNED, 60000);
    OptimizingTask task2 = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task2);
    Assertions.assertEquals(task2.getTaskId(), task.getTaskId());
    TableOptimizing.OptimizingInput input =
        SerializationUtil.simpleDeserialize(task.getTaskInput());
    TableOptimizing.OptimizingInput input2 =
        SerializationUtil.simpleDeserialize(task2.getTaskInput());
    Assertions.assertEquals(input2.toString(), input.toString());

    optimizingService().ackTask(token, THREAD_ID, task2.getTaskId());
    optimizingService().completeTask(token, buildOptimizingTaskResult(task2.getTaskId()));
    assertTaskCompleted(taskRuntime);
  }

  @Test
  public void testReloadScheduledTask() {
    // 1.poll task
    OptimizingTask task = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task);

    // After reload, SCHEDULED tasks are kept as-is (not reset to PLANNED).
    // The optimizer is still alive, so it can complete the task directly.
    reload();
    assertTaskStatus(TaskRuntime.Status.SCHEDULED);

    // Complete the task with the same token (optimizer still alive)
    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());

    TaskRuntime taskRuntime =
        optimizingService().listTasks(defaultResourceGroup().getName()).get(0);
    optimizingService().completeTask(token, buildOptimizingTaskResult(task.getTaskId()));
    assertTaskCompleted(taskRuntime);
  }

  @Test
  public void testReloadAckTask() {
    // 1.poll task and ack
    OptimizingTask task = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task);
    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());

    // After reload, ACKED tasks are kept as-is (not reset to PLANNED).
    // The optimizer is still alive, so it can complete the task directly.
    reload();
    assertTaskStatus(TaskRuntime.Status.ACKED);

    // Complete the task with the same token (optimizer still alive)
    TaskRuntime<?> taskRuntime =
        optimizingService().listTasks(defaultResourceGroup().getName()).get(0);
    optimizingService().completeTask(token, buildOptimizingTaskResult(task.getTaskId()));
    assertTaskCompleted(taskRuntime);
  }

  @Test
  public void testPollResetsStaleAckedTask() {
    // 1. Poll and ack a task
    OptimizingTask task = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task);
    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());
    assertTaskStatus(TaskRuntime.Status.ACKED);

    // 2. Reload (simulate AMS restart) — ACKED task is kept as-is
    reload();
    assertTaskStatus(TaskRuntime.Status.ACKED);

    // 3. The same optimizer thread polls again — this means the executor finished
    //    the old task but completeTask was lost during AMS downtime.
    //    The stale ACKED task should be automatically reset to PLANNED,
    //    then immediately re-polled by this same poll call.
    OptimizingTask task2 = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task2);
    Assertions.assertEquals(task.getTaskId(), task2.getTaskId());

    // 4. Complete the re-polled task normally
    optimizingService().ackTask(token, THREAD_ID, task2.getTaskId());
    TaskRuntime<?> taskRuntime =
        optimizingService().listTasks(defaultResourceGroup().getName()).get(0);
    optimizingService().completeTask(token, buildOptimizingTaskResult(task2.getTaskId()));
    assertTaskCompleted(taskRuntime);
  }

  @Test
  public void testReloadCompletedTask() {
    // THREAD_ID.poll task
    OptimizingTask task = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task);
    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());
    optimizingService().completeTask(token, buildOptimizingTaskResult(task.getTaskId()));

    reload();
    // Committing process will be closed when reloading
    Assertions.assertNull(
        getDefaultTableRuntime(serverTableIdentifier().getId()).getOptimizingProcess());
    Assertions.assertEquals(
        OptimizingStatus.IDLE,
        getDefaultTableRuntime(serverTableIdentifier().getId()).getOptimizingStatus());
  }

  @Test
  public void testReloadAllTasksCompletedNotYetCommitting() {
    // Simulate: AMS crashes after persisting the last task as SUCCESS
    // but before beginCommitting() updates the table status to COMMITTING.
    // DB state: process=RUNNING, all tasks=SUCCESS, table=*_OPTIMIZING

    // 1. Complete all tasks normally — table transitions to COMMITTING
    OptimizingTask task = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task);
    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());
    optimizingService().completeTask(token, buildOptimizingTaskResult(task.getTaskId()));

    DefaultTableRuntime runtime = getDefaultTableRuntime(serverTableIdentifier().getId());
    Assertions.assertEquals(OptimizingStatus.COMMITTING, runtime.getOptimizingStatus());

    // 2. Revert table status in DB to *_OPTIMIZING (simulate crash before beginCommitting)
    long tableId = serverTableIdentifier().getId();
    updateTableStatusInDb(tableId, OptimizingStatus.MINOR_OPTIMIZING);

    // 3. Reload (simulate AMS restart)
    reload();

    // 4. During recovery, all tasks are SUCCESS so beginCommitting() should be triggered
    Assertions.assertEquals(
        OptimizingStatus.COMMITTING,
        getDefaultTableRuntime(serverTableIdentifier().getId()).getOptimizingStatus());
  }

  @Test
  public void testReloadPlanningWithOrphanedProcess() {
    // 1. Poll and ack a task - table is now in optimizing state with an active process
    OptimizingTask task = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task);
    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());
    assertTaskStatus(TaskRuntime.Status.ACKED);

    // 2. Simulate table status being PLANNING while process is still active
    // This can happen when AMS crashes during a planning transition
    getDefaultTableRuntime(serverTableIdentifier().getId()).beginPlanning();
    Assertions.assertEquals(
        OptimizingStatus.PLANNING,
        getDefaultTableRuntime(serverTableIdentifier().getId()).getOptimizingStatus());

    // 3. Reload (simulate AMS restart)
    reload();

    // 4. Orphaned process should be closed, table should transition to IDLE
    Assertions.assertNull(
        getDefaultTableRuntime(serverTableIdentifier().getId()).getOptimizingProcess());
    Assertions.assertEquals(
        OptimizingStatus.IDLE,
        getDefaultTableRuntime(serverTableIdentifier().getId()).getOptimizingStatus());
  }

  @Test
  public void testReloadOptimizingWithFailedProcess() {
    // Simulate: table is *_OPTIMIZING but process is FAILED in DB
    // Before fix: table stuck in tableQueue (poll blocked for FAILED process)
    OptimizingTask task = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task);
    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());

    // Table should be in *_OPTIMIZING with a RUNNING process
    DefaultTableRuntime runtime = getDefaultTableRuntime(serverTableIdentifier().getId());
    Assertions.assertTrue(runtime.getOptimizingStatus().isProcessing());
    Assertions.assertNotNull(runtime.getOptimizingProcess());

    // Directly update process status to FAILED in DB to simulate crash after process failure
    long processId = runtime.getProcessId();
    long tableId = serverTableIdentifier().getId();
    updateProcessStatusInDb(tableId, processId, ProcessStatus.FAILED);

    // Reload (simulate AMS restart)
    reload();

    // Table should be reset to IDLE and added to scheduler
    Assertions.assertNull(
        getDefaultTableRuntime(serverTableIdentifier().getId()).getOptimizingProcess());
    Assertions.assertEquals(
        OptimizingStatus.IDLE,
        getDefaultTableRuntime(serverTableIdentifier().getId()).getOptimizingStatus());
  }

  @Test
  public void testReloadCommittingWithFailedProcess() {
    // Simulate: table is COMMITTING but process is FAILED in DB
    // Before fix: table became a ghost (not in scheduler or tableQueue)
    OptimizingTask task = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task);
    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());
    optimizingService().completeTask(token, buildOptimizingTaskResult(task.getTaskId()));

    // Table should be in COMMITTING state
    DefaultTableRuntime runtime = getDefaultTableRuntime(serverTableIdentifier().getId());
    Assertions.assertEquals(OptimizingStatus.COMMITTING, runtime.getOptimizingStatus());

    // Directly update process status to FAILED in DB
    long processId = runtime.getProcessId();
    long tableId = serverTableIdentifier().getId();
    updateProcessStatusInDb(tableId, processId, ProcessStatus.FAILED);

    // Reload (simulate AMS restart)
    reload();

    // Table should be reset to IDLE
    Assertions.assertNull(
        getDefaultTableRuntime(serverTableIdentifier().getId()).getOptimizingProcess());
    Assertions.assertEquals(
        OptimizingStatus.IDLE,
        getDefaultTableRuntime(serverTableIdentifier().getId()).getOptimizingStatus());
  }

  @Test
  public void testReloadOptimizingWithNoProcessRecord() {
    // Simulate: table is *_OPTIMIZING but process record is missing from DB
    // Before fix: table became a ghost (not in scheduler or tableQueue)
    OptimizingTask task = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task);
    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());

    DefaultTableRuntime runtime = getDefaultTableRuntime(serverTableIdentifier().getId());
    Assertions.assertTrue(runtime.getOptimizingStatus().isProcessing());

    // Delete process record from DB to simulate missing process
    long processId = runtime.getProcessId();
    long tableId = serverTableIdentifier().getId();
    deleteProcessFromDb(tableId, processId);

    // Reload (simulate AMS restart)
    reload();

    // Table should be reset to IDLE
    Assertions.assertNull(
        getDefaultTableRuntime(serverTableIdentifier().getId()).getOptimizingProcess());
    Assertions.assertEquals(
        OptimizingStatus.IDLE,
        getDefaultTableRuntime(serverTableIdentifier().getId()).getOptimizingStatus());
  }

  @Test
  public void testReloadFailedTask() {
    // 1.poll task
    OptimizingTask task = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task);
    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());
    optimizingService()
        .completeTask(token, buildOptimizingTaskFailResult(task.getTaskId(), "error"));

    reload();
    assertTaskStatus(TaskRuntime.Status.PLANNED);

    OptimizingTask task2 = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertEquals(task2.getTaskId(), task.getTaskId());
    TableOptimizing.OptimizingInput input =
        SerializationUtil.simpleDeserialize(task.getTaskInput());
    TableOptimizing.OptimizingInput input2 =
        SerializationUtil.simpleDeserialize(task2.getTaskInput());
    Assertions.assertEquals(input2.toString(), input.toString());
    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());
    optimizingService()
        .completeTask(token, buildOptimizingTaskFailResult(task.getTaskId(), "error"));

    reload();
    assertTaskStatus(TaskRuntime.Status.PLANNED);

    OptimizingTask task3 = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertEquals(task3.getTaskId(), task.getTaskId());
    TableOptimizing.OptimizingInput input3 =
        SerializationUtil.simpleDeserialize(task2.getTaskInput());
    Assertions.assertEquals(input3.toString(), input.toString());
    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());
    optimizingService()
        .completeTask(token, buildOptimizingTaskFailResult(task.getTaskId(), "error"));
    assertTaskStatus(TaskRuntime.Status.PLANNED);
  }

  /**
   * Test handleConfigChanged when the optimizer group changes to a different existing group. The
   * table should be released from the old group's queue and from the new group's queue.
   */
  @Test
  public void testHandleConfigChangedGroupChanged() {
    // Create a new resource group
    ResourceGroup newGroup = new ResourceGroup.Builder("test-new-group", "local").build();
    try {
      optimizerManager().createResourceGroup(newGroup);
    } catch (Throwable ignored) {
    }
    optimizingService().createResourceGroup(newGroup);

    try {
      TableRuntime tableRuntime = tableService().getRuntime(serverTableIdentifier().getId());
      String originalGroup = tableRuntime.getGroupName();

      // Build original config with the old group name
      OptimizingConfig originalOptConfig = new OptimizingConfig();
      originalOptConfig.setOptimizerGroup(originalGroup);
      TableConfiguration originalConfig = new TableConfiguration();
      originalConfig.setOptimizingConfig(originalOptConfig);

      // Simulate that the table now belongs to the new group
      TableRuntime spyRuntime = spy(tableRuntime);
      doReturn("test-new-group").when(spyRuntime).getGroupName();
      doReturn(TableFormat.ICEBERG).when(spyRuntime).getFormat();

      // Fire config changed (group changed from "default" to "test-new-group")
      RuntimeHandlerChain handler = optimizingService().getTableRuntimeHandler();
      handler.fireConfigChanged(spyRuntime, originalConfig);

      // No exception should be thrown; table should be released from both old and new queue
    } finally {
      optimizingService().deleteResourceGroup("test-new-group");
      try {
        optimizerManager().deleteResourceGroup("test-new-group");
      } catch (Throwable ignored) {
      }
    }
  }

  /**
   * Test handleConfigChanged when the new optimizer group does not exist. The table runtime's
   * completeEmptyProcess() should be called.
   */
  @Test
  public void testHandleConfigChangedGroupNotExist() {
    DefaultTableRuntime tableRuntime =
        (DefaultTableRuntime) tableService().getRuntime(serverTableIdentifier().getId());
    String originalGroup = tableRuntime.getGroupName();

    // Build original config with the original group
    OptimizingConfig originalOptConfig = new OptimizingConfig();
    originalOptConfig.setOptimizerGroup(originalGroup);
    TableConfiguration originalConfig = new TableConfiguration();
    originalConfig.setOptimizingConfig(originalOptConfig);

    // Simulate that the table now belongs to a non-existing group
    DefaultTableRuntime spyRuntime = spy(tableRuntime);
    doReturn("non-existing-group").when(spyRuntime).getGroupName();
    doReturn(TableFormat.ICEBERG).when(spyRuntime).getFormat();
    doReturn(serverTableIdentifier()).when(spyRuntime).getTableIdentifier();

    // Fire config changed (group changed from "default" to "non-existing-group")
    RuntimeHandlerChain handler = optimizingService().getTableRuntimeHandler();
    handler.fireConfigChanged(spyRuntime, originalConfig);

    // Verify that completeEmptyProcess was called on the spy
    verify(spyRuntime).completeEmptyProcess();
  }

  private OptimizerRegisterInfo buildRegisterInfo() {
    OptimizerRegisterInfo registerInfo = new OptimizerRegisterInfo();
    Map<String, String> registerProperties = Maps.newHashMap();
    registerProperties.put(
        OptimizerProperties.OPTIMIZER_HEART_BEAT_INTERVAL,
        String.valueOf(OPTIMIZER_HEARTBEAT_INTERVAL_MS));
    registerInfo.setProperties(registerProperties);
    registerInfo.setThreadCount(1);
    registerInfo.setMemoryMb(1024);
    registerInfo.setGroupName(defaultResourceGroup().getName());
    registerInfo.setResourceId("1");
    registerInfo.setStartTime(System.currentTimeMillis());
    return registerInfo;
  }

  private void insertOptimizer(OptimizerInstance optimizer) {
    try (SqlSession session = SqlSessionFactoryProvider.getInstance().get().openSession(true)) {
      session.getMapper(OptimizerMapper.class).insertOptimizer(optimizer);
    }
  }

  private boolean optimizerExists(String optimizerToken) {
    return optimizerManager().listOptimizers().stream()
        .anyMatch(optimizer -> optimizerToken.equals(optimizer.getToken()));
  }

  private void deleteOptimizerRecord(String optimizerToken) {
    try (SqlSession session = SqlSessionFactoryProvider.getInstance().get().openSession(true)) {
      session.getMapper(OptimizerMapper.class).deleteOptimizer(optimizerToken);
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, ?> optimizerState(String fieldName) throws Exception {
    Field field = DefaultOptimizingService.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    return (Map<String, ?>) field.get(optimizingService());
  }

  private OptimizingTaskResult buildOptimizingTaskResult(OptimizingTaskId taskId) {
    TableOptimizing.OptimizingOutput output = new RewriteFilesOutput(null, null, null);
    OptimizingTaskResult optimizingTaskResult = new OptimizingTaskResult(taskId, THREAD_ID);
    optimizingTaskResult.setTaskOutput(SerializationUtil.simpleSerialize(output));
    return optimizingTaskResult;
  }

  private OptimizingTaskResult buildOptimizingTaskFailResult(
      OptimizingTaskId taskId, String errorMessage) {
    TableOptimizing.OptimizingOutput output = new RewriteFilesOutput(null, null, null);
    OptimizingTaskResult optimizingTaskResult = new OptimizingTaskResult(taskId, THREAD_ID);
    optimizingTaskResult.setTaskOutput(SerializationUtil.simpleSerialize(output));
    optimizingTaskResult.setErrorMessage(errorMessage);
    return optimizingTaskResult;
  }

  private void updateProcessStatusInDb(long tableId, long processId, ProcessStatus status) {
    try (SqlSession session = SqlSessionFactoryProvider.getInstance().get().openSession(true)) {
      TableProcessMapper mapper = session.getMapper(TableProcessMapper.class);
      TableProcessMeta meta = mapper.getProcessMeta(processId);
      mapper.updateProcess(
          tableId,
          processId,
          meta.getExternalProcessIdentifier(),
          status,
          meta.getProcessStage(),
          meta.getRetryNumber(),
          System.currentTimeMillis(),
          "simulated failure",
          meta.getProcessParameters(),
          meta.getSummary());
    }
  }

  private void updateTableStatusInDb(long tableId, OptimizingStatus status) {
    try (SqlSession session = SqlSessionFactoryProvider.getInstance().get().openSession(true)) {
      TableRuntimeMapper mapper = session.getMapper(TableRuntimeMapper.class);
      TableRuntimeMeta meta = mapper.selectRuntime(tableId);
      meta.setStatusCode(status.getCode());
      mapper.updateRuntime(meta);
    }
  }

  private void deleteProcessFromDb(long tableId, long processId) {
    try (SqlSession session = SqlSessionFactoryProvider.getInstance().get().openSession(true)) {
      TableProcessMapper mapper = session.getMapper(TableProcessMapper.class);
      mapper.deleteBefore(tableId, processId);
    }
  }

  private void assertTaskStatus(TaskRuntime.Status expectedStatus) {
    Assertions.assertEquals(
        expectedStatus,
        optimizingService().listTasks(defaultResourceGroup().getName()).get(0).getStatus());
  }

  private void waitForTaskStatus(TaskRuntime.Status expectedStatus, long timeoutMs)
      throws InterruptedException {
    long deadline = System.currentTimeMillis() + timeoutMs;
    while (System.currentTimeMillis() < deadline) {
      if (expectedStatus
          == optimizingService().listTasks(defaultResourceGroup().getName()).get(0).getStatus()) {
        return;
      }
      Thread.sleep(100);
    }
    assertTaskStatus(expectedStatus);
  }

  private void waitForOptimizerExpiration(String optimizerToken, long timeoutMs)
      throws InterruptedException {
    long deadline = System.currentTimeMillis() + timeoutMs;
    while (System.currentTimeMillis() < deadline) {
      boolean optimizerExists =
          optimizerManager().listOptimizers().stream()
              .anyMatch(optimizer -> optimizerToken.equals(optimizer.getToken()));
      boolean optimizerAuthenticated =
          optimizingService().getTotalQuota(defaultResourceGroup().getName()) > 0;
      if (!optimizerExists && !optimizerAuthenticated) {
        return;
      }
      Thread.sleep(100);
    }
    Assertions.fail("Optimizer did not expire within " + timeoutMs + " ms");
  }

  private void assertTaskCompleted(TaskRuntime<?> taskRuntime) {
    if (taskRuntime != null) {
      Assertions.assertEquals(TaskRuntime.Status.SUCCESS, taskRuntime.getStatus());
    }
    Assertions.assertEquals(
        ProcessStatus.RUNNING,
        getDefaultTableRuntime(serverTableIdentifier().getId()).getOptimizingProcess().getStatus());
    Assertions.assertEquals(
        OptimizingStatus.COMMITTING,
        getDefaultTableRuntime(serverTableIdentifier().getId()).getOptimizingStatus());
  }

  protected void reload() {
    toucher.suspend();
    disposeTableService();
    initTableService();
    toucher.goOn();
  }

  protected void rebootWithHeartbeatTimeout(Duration heartbeatTimeout) throws InterruptedException {
    toucher.stop();
    toucher = null;
    disposeTableService();
    customHeartbeatTimeout = true;
    initTableService(heartbeatTimeout);
    toucher = new Toucher();
  }

  private class TableRuntimeRefresher extends TableRuntimeRefreshExecutor {

    public TableRuntimeRefresher() {
      super(tableService(), 1, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    void refreshPending() {
      execute(getDefaultTableRuntime(serverTableIdentifier().getId()));
    }
  }

  private class Toucher implements Runnable {

    private volatile boolean stop = false;
    private volatile boolean suspend = false;
    private final Thread thread = new Thread(this);

    public Toucher() {
      token = optimizingService().authenticate(buildRegisterInfo());
      thread.setDaemon(true);
      thread.start();
    }

    public synchronized void stop() throws InterruptedException {
      stop = true;
      thread.interrupt();
      thread.join();
    }

    public synchronized void suspend() {
      suspend = true;
      thread.interrupt();
    }

    public synchronized void goOn() {
      suspend = false;
      thread.interrupt();
    }

    @Override
    public void run() {
      while (!stop) {
        try {
          Thread.sleep(OPTIMIZER_HEARTBEAT_INTERVAL_MS);
          synchronized (this) {
            if (!suspend) {
              optimizingService().touch(token);
            }
          }
        } catch (PluginRetryAuthException e) {
          e.printStackTrace();
        } catch (Exception ignore) {
          // ignore
        }
      }
    }
  }
}
