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

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.OptimizerProperties;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.api.OptimizerRegisterInfo;
import org.apache.amoro.api.OptimizingTask;
import org.apache.amoro.api.OptimizingTaskId;
import org.apache.amoro.api.OptimizingTaskResult;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.exception.IllegalTaskStateException;
import org.apache.amoro.exception.PluginRetryAuthException;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.optimizing.RewriteFilesOutput;
import org.apache.amoro.optimizing.TableOptimizing;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.optimizing.TaskRuntime;
import org.apache.amoro.server.persistence.SqlSessionFactoryProvider;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.persistence.mapper.TableRuntimeMapper;
import org.apache.amoro.server.process.TableProcessMeta;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.amoro.server.scheduler.inline.TableRuntimeRefreshExecutor;
import org.apache.amoro.server.table.AMSTableTestBase;
import org.apache.amoro.server.table.DefaultTableRuntime;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(Parameterized.class)
public class TestDefaultOptimizingService extends AMSTableTestBase {

  private final int THREAD_ID = 0;
  private String token;
  private Toucher toucher;

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
  public void testTouchTimeout() throws InterruptedException {
    OptimizingTask task = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task);
    toucher.stop();
    toucher = null;
    Thread.sleep(1000);
    Assertions.assertThrows(PluginRetryAuthException.class, () -> optimizingService().touch(token));
    Assertions.assertThrows(
        PluginRetryAuthException.class, () -> optimizingService().pollTask(token, THREAD_ID));
    // After optimizer expires, its tasks are immediately reset to PLANNED
    // because unregister happens before task scan in OptimizerKeeper
    assertTaskStatus(TaskRuntime.Status.PLANNED);
    token = optimizingService().authenticate(buildRegisterInfo());
    toucher = new Toucher();
    Thread.sleep(1000);
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
    reboot();

    // wait for last optimizer expiring
    Thread.sleep(1000);
    assertTaskStatus(TaskRuntime.Status.PLANNED);
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
    Assertions.assertThrows(
        IllegalTaskStateException.class,
        () -> optimizingService().completeTask(token, buildOptimizingTaskResult(task.getTaskId())));

    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());

    TaskRuntime taskRuntime =
        optimizingService().listTasks(defaultResourceGroup().getName()).get(0);
    optimizingService().completeTask(token, buildOptimizingTaskResult(task.getTaskId()));
    assertTaskCompleted(taskRuntime);
  }

  @Test
  public void testExecuteTaskTimeOutAndRetry() throws InterruptedException {
    OptimizingTask task = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task);

    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());

    TaskRuntime taskRuntime =
        optimizingService().listTasks(defaultResourceGroup().getName()).get(0);
    assertTaskStatus(TaskRuntime.Status.ACKED);

    // In this test, OPTIMIZER_TASK_EXECUTE_TIMEOUT is set to 30 seconds, so after waiting 45
    // seconds the task will be considered suspended and retried
    Thread.sleep(45000);

    assertTaskStatus(TaskRuntime.Status.PLANNED);
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

  private OptimizerRegisterInfo buildRegisterInfo() {
    OptimizerRegisterInfo registerInfo = new OptimizerRegisterInfo();
    Map<String, String> registerProperties = Maps.newHashMap();
    registerProperties.put(OptimizerProperties.OPTIMIZER_HEART_BEAT_INTERVAL, "100");
    registerInfo.setProperties(registerProperties);
    registerInfo.setThreadCount(1);
    registerInfo.setMemoryMb(1024);
    registerInfo.setGroupName(defaultResourceGroup().getName());
    registerInfo.setResourceId("1");
    registerInfo.setStartTime(System.currentTimeMillis());
    return registerInfo;
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
    disposeTableService();
    toucher.suspend();
    initTableService();
    toucher.goOn();
  }

  protected void reboot() throws InterruptedException {
    disposeTableService();
    toucher.stop();
    toucher = null;
    initTableService();
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
          Thread.sleep(300);
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
