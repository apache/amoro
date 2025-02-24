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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.OptimizerProperties;
import org.apache.amoro.ServerTableIdentifier;
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
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.amoro.optimizing.RewriteFilesOutput;
import org.apache.amoro.optimizing.TableOptimizing;
import org.apache.amoro.optimizing.plan.AbstractOptimizingEvaluator;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.server.dashboard.model.TableOptimizingInfo;
import org.apache.amoro.server.optimizing.OptimizingProcess;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.optimizing.TaskRuntime;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.amoro.server.table.AMSTableTestBase;
import org.apache.amoro.server.table.TableManager;
import org.apache.amoro.server.table.TableRuntime;
import org.apache.amoro.server.table.executor.TableRuntimeRefreshExecutor;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.SerializationUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.data.Record;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
    TableRuntime runtime = tableService().getRuntime(serverTableIdentifier().getId());

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
    Assertions.assertEquals(task2, task);
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
    Assertions.assertEquals(task2, task);

    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());
    optimizingService()
        .completeTask(token, buildOptimizingTaskFailResult(task.getTaskId(), "unknown error"));

    // retry again
    OptimizingTask task3 = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertEquals(task3, task);
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
    assertTaskStatus(TaskRuntime.Status.SCHEDULED);
    token = optimizingService().authenticate(buildRegisterInfo());
    toucher = new Toucher();
    Thread.sleep(1000);
    assertTaskStatus(TaskRuntime.Status.PLANNED);
    OptimizingTask task2 = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertEquals(task2, task);
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
  public void testReloadScheduledTask() {
    // 1.poll task
    OptimizingTask task = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task);

    reload();
    assertTaskStatus(TaskRuntime.Status.SCHEDULED);
    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());

    TaskRuntime taskRuntime =
        optimizingService().listTasks(defaultResourceGroup().getName()).get(0);
    optimizingService().completeTask(token, buildOptimizingTaskResult(task.getTaskId()));
    assertTaskCompleted(taskRuntime);
  }

  @Test
  public void testReloadAckTask() {
    // 1.poll task
    OptimizingTask task = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertNotNull(task);
    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());

    reload();
    assertTaskStatus(TaskRuntime.Status.ACKED);

    TaskRuntime<?> taskRuntime =
        optimizingService().listTasks(defaultResourceGroup().getName()).get(0);
    optimizingService().completeTask(token, buildOptimizingTaskResult(task.getTaskId()));
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
        tableService().getRuntime(serverTableIdentifier().getId()).getOptimizingProcess());
    Assertions.assertEquals(
        OptimizingStatus.IDLE,
        tableService().getRuntime(serverTableIdentifier().getId()).getOptimizingStatus());
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
    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());
    optimizingService()
        .completeTask(token, buildOptimizingTaskFailResult(task.getTaskId(), "error"));

    reload();
    assertTaskStatus(TaskRuntime.Status.PLANNED);

    OptimizingTask task3 = optimizingService().pollTask(token, THREAD_ID);
    Assertions.assertEquals(task3.getTaskId(), task.getTaskId());
    optimizingService().ackTask(token, THREAD_ID, task.getTaskId());
    optimizingService()
        .completeTask(token, buildOptimizingTaskFailResult(task.getTaskId(), "error"));
    assertTaskStatus(TaskRuntime.Status.PLANNED);
  }

  /**
   * Test the logic for {@link TableManager#queryTableOptimizingInfo(String, String, String, List,
   * int, int)}.
   */
  @Test
  public void testGetRuntimes() {
    String catalog = "catalog";
    String db1 = "db1";

    String optimizerGroup1 = "opGroup1";

    // 1 add some tables

    // table in opGroup1
    Map<String, String> properties = new HashMap<>();
    properties.put(TableProperties.SELF_OPTIMIZING_GROUP, optimizerGroup1);

    // 1.1 add tables with IDLE status
    // the status will be OptimizingStatus.IDLE default
    String idle1InGroup1 = "idle1InGroup1";
    TableRuntime idle1 =
        new TableRuntime(
            ServerTableIdentifier.of(10001L, catalog, db1, idle1InGroup1, TableFormat.ICEBERG),
            tableService(),
            properties);

    // the status will be OptimizingStatus.IDLE default
    String idle2InGroup1 = "idle2InGroup1";
    TableRuntime idle2 =
        new TableRuntime(
            ServerTableIdentifier.of(10002L, catalog, db1, idle2InGroup1, TableFormat.ICEBERG),
            tableService(),
            properties);

    // 1.2 add tables with PENDING status
    String pending1InGroup1 = "pending1InGroup1";
    TableRuntime pending1 =
        new TableRuntime(
            ServerTableIdentifier.of(10003L, catalog, db1, pending1InGroup1, TableFormat.ICEBERG),
            tableService(),
            properties);
    // update status
    pending1.setPendingInput(new AbstractOptimizingEvaluator.PendingInput());

    String pending2InGroup1 = "pending2InGroup1";
    TableRuntime pending2 =
        new TableRuntime(
            ServerTableIdentifier.of(10004L, catalog, db1, pending2InGroup1, TableFormat.ICEBERG),
            tableService(),
            properties);
    // update status
    pending2.setPendingInput(new AbstractOptimizingEvaluator.PendingInput());

    // 1.3 add tables with PLANNING status
    String db2 = "db2";
    String plan1InGroup1 = "plan1InGroup1";
    TableRuntime plan1 =
        new TableRuntime(
            ServerTableIdentifier.of(10005L, catalog, db2, plan1InGroup1, TableFormat.ICEBERG),
            tableService(),
            properties);
    plan1.beginPlanning();

    String plan2InGroup1 = "plan2InGroup1";
    TableRuntime plan2 =
        new TableRuntime(
            ServerTableIdentifier.of(10006L, catalog, db2, plan2InGroup1, TableFormat.ICEBERG),
            tableService(),
            properties);
    plan2.beginPlanning();

    // 1.4 add tables with COMMITTING status
    String committing1InGroup1 = "committing1InGroup1";
    TableRuntime committing1 =
        new TableRuntime(
            ServerTableIdentifier.of(
                10007L, catalog, db2, committing1InGroup1, TableFormat.ICEBERG),
            tableService(),
            properties);
    committing1.beginCommitting();

    String commiting2InGroup1 = "committing2InGroup1";
    TableRuntime committing2 =
        new TableRuntime(
            ServerTableIdentifier.of(10008L, catalog, db2, commiting2InGroup1, TableFormat.ICEBERG),
            tableService(),
            properties);
    committing2.beginCommitting();

    // 1.5 add tables with MINOR_OPTIMIZING status
    String minor1InGroup1 = "minor1InGroup1";
    TableRuntime minor1 =
        new TableRuntime(
            ServerTableIdentifier.of(10009L, catalog, db2, minor1InGroup1, TableFormat.ICEBERG),
            tableService(),
            properties);
    OptimizingProcess process = mock(OptimizingProcess.class);
    doReturn(1L).when(process).getProcessId();
    doReturn(OptimizingType.MINOR).when(process).getOptimizingType();
    minor1.beginProcess(process);

    String minor2InGroup1 = "minor2InGroup1";
    TableRuntime minor2 =
        new TableRuntime(
            ServerTableIdentifier.of(10010L, catalog, db2, minor2InGroup1, TableFormat.ICEBERG),
            tableService(),
            properties);
    OptimizingProcess process2 = mock(OptimizingProcess.class);
    doReturn(2L).when(process2).getProcessId();
    doReturn(OptimizingType.MINOR).when(process2).getOptimizingType();
    minor2.beginProcess(process2);

    // 1.6 add tables with MAJOR_OPTIMIZING status
    String major1InGroup1 = "major1InGroup1";
    TableRuntime major1 =
        new TableRuntime(
            ServerTableIdentifier.of(10011L, catalog, db1, major1InGroup1, TableFormat.ICEBERG),
            tableService(),
            properties);
    OptimizingProcess process3 = mock(OptimizingProcess.class);
    doReturn(3L).when(process3).getProcessId();
    doReturn(OptimizingType.MAJOR).when(process3).getOptimizingType();
    major1.beginProcess(process3);

    String major2InGroup1 = "major2InGroup1";
    TableRuntime major2 =
        new TableRuntime(
            ServerTableIdentifier.of(10012L, catalog, db1, major2InGroup1, TableFormat.ICEBERG),
            tableService(),
            properties);
    OptimizingProcess process4 = mock(OptimizingProcess.class);
    doReturn(4L).when(process4).getProcessId();
    doReturn(OptimizingType.MAJOR).when(process4).getOptimizingType();
    major2.beginProcess(process4);

    // 1.7 add tables with FULL_OPTIMIZING status
    String full1InGroup1 = "full1InGroup1";
    TableRuntime full1 =
        new TableRuntime(
            ServerTableIdentifier.of(10013L, catalog, db1, full1InGroup1, TableFormat.ICEBERG),
            tableService(),
            properties);
    OptimizingProcess process5 = mock(OptimizingProcess.class);
    doReturn(5L).when(process5).getProcessId();
    doReturn(OptimizingType.FULL).when(process5).getOptimizingType();
    full1.beginProcess(process5);

    String full2InGroup1 = "full2InGroup1";
    TableRuntime full2 =
        new TableRuntime(
            ServerTableIdentifier.of(10014L, catalog, db1, full2InGroup1, TableFormat.ICEBERG),
            tableService(),
            properties);
    OptimizingProcess process6 = mock(OptimizingProcess.class);
    doReturn(6L).when(process6).getProcessId();
    doReturn(OptimizingType.FULL).when(process6).getOptimizingType();
    full2.beginProcess(process6);

    // 1.8 add tables in other group with MINOR_OPTIMIZING status
    // table in other group.
    String opGroup2 = "opGroup2-other";
    properties.put(TableProperties.SELF_OPTIMIZING_GROUP, opGroup2);
    String minor1InOtherGroup1 = "minor1-InOtherGroup";
    TableRuntime minor1Other =
        new TableRuntime(
            ServerTableIdentifier.of(
                10015L, catalog, db1, minor1InOtherGroup1, TableFormat.ICEBERG),
            tableService(),
            properties);
    OptimizingProcess process7 = mock(OptimizingProcess.class);
    doReturn(7L).when(process7).getProcessId();
    doReturn(OptimizingType.MINOR).when(process7).getOptimizingType();
    minor1Other.beginProcess(process7);

    String minor2InOtherGroup1 = "minor2-InOtherGroup";
    TableRuntime minor2Other =
        new TableRuntime(
            ServerTableIdentifier.of(
                10016L, catalog, db1, minor2InOtherGroup1, TableFormat.ICEBERG),
            tableService(),
            properties);
    OptimizingProcess process8 = mock(OptimizingProcess.class);
    doReturn(8L).when(process8).getProcessId();
    doReturn(OptimizingType.MINOR).when(process8).getOptimizingType();
    minor2Other.beginProcess(process8);

    String minor3InOtherGroup1 = "minor3-InOtherGroup";
    TableRuntime minor3Other =
        new TableRuntime(
            ServerTableIdentifier.of(
                10017L, catalog, db1, minor3InOtherGroup1, TableFormat.ICEBERG),
            tableService(),
            properties);
    OptimizingProcess process9 = mock(OptimizingProcess.class);
    doReturn(9L).when(process9).getProcessId();
    doReturn(OptimizingType.MINOR).when(process9).getOptimizingType();
    minor3Other.beginProcess(process9);

    // 2 test and assert the result
    // 2.1 only optimize group filter set
    Pair<List<TableOptimizingInfo>, Integer> res =
        tableManager()
            .queryTableOptimizingInfo(optimizerGroup1, null, null, Collections.emptyList(), 10, 0);
    Integer expectedTotalinGroup1 = 14;
    Assert.assertEquals(expectedTotalinGroup1, res.getRight());
    Assert.assertEquals(10, res.getLeft().size());

    // 2.2 set optimize group and db filter
    res =
        tableManager()
            .queryTableOptimizingInfo(optimizerGroup1, db1, null, Collections.emptyList(), 5, 0);
    // there are 8 tables in db1 in optimizerGroup1
    Integer expectedTotalGroup1Db1 = 8;
    Assert.assertEquals(expectedTotalGroup1Db1, res.getRight());
    Assert.assertEquals(5, res.getLeft().size());

    // 2.3 set optimize group and table filter
    // there are 3 tables with suffix "-InOtherGroup" in opGroup2
    String fuzzyDbName = "InOtherGroup";
    res =
        tableManager()
            .queryTableOptimizingInfo(opGroup2, null, fuzzyDbName, Collections.emptyList(), 2, 0);
    Integer expectedTotalWithFuzzyDbName = 3;
    Assert.assertEquals(expectedTotalWithFuzzyDbName, res.getRight());
    Assert.assertEquals(2, res.getLeft().size());

    res =
        tableManager()
            .queryTableOptimizingInfo(opGroup2, null, fuzzyDbName, Collections.emptyList(), 5, 0);
    Assert.assertEquals(expectedTotalWithFuzzyDbName, res.getRight());
    // there are only 3 tables with the suffix in opGroup2
    Assert.assertEquals(3, res.getLeft().size());

    // 2.4 set optimize group and status filter, with only one status
    List<Integer> statusCode = new ArrayList<>();
    statusCode.add(OptimizingStatus.MAJOR_OPTIMIZING.getCode());
    res = tableManager().queryTableOptimizingInfo(optimizerGroup1, null, null, statusCode, 10, 0);
    Integer expectedTotalInGroup1WithMajorStatus = 2;
    Assert.assertEquals(expectedTotalInGroup1WithMajorStatus, res.getRight());
    Assert.assertEquals(2, res.getLeft().size());

    // 2.5 set optimize group and status filter with two statuses
    statusCode.clear();
    statusCode.add(OptimizingStatus.MINOR_OPTIMIZING.getCode());
    statusCode.add(OptimizingStatus.MAJOR_OPTIMIZING.getCode());
    res = tableManager().queryTableOptimizingInfo(optimizerGroup1, null, null, statusCode, 3, 0);
    Integer expectedTotalInGroup1WithMinorMajorStatus = 4;
    Assert.assertEquals(expectedTotalInGroup1WithMinorMajorStatus, res.getRight());
    Assert.assertEquals(3, res.getLeft().size());

    // 2.6 all filter set which contains result
    statusCode.clear();
    statusCode.add(OptimizingStatus.PENDING.getCode());
    statusCode.add(OptimizingStatus.FULL_OPTIMIZING.getCode());
    String tableFilter = "pending";
    res =
        tableManager()
            .queryTableOptimizingInfo(optimizerGroup1, db1, tableFilter, statusCode, 10, 0);
    Integer expectedTotalInGroup1InDb1WithTableFilterAndStatus = 2;
    Assert.assertEquals(expectedTotalInGroup1InDb1WithTableFilterAndStatus, res.getRight());
    Assert.assertEquals(2, res.getLeft().size());

    // 2.7 all filters with no result
    statusCode.clear();
    statusCode.add(OptimizingStatus.PENDING.getCode());
    statusCode.add(OptimizingStatus.FULL_OPTIMIZING.getCode());
    String wrongTableFilter2 = "noTableWithName";
    res =
        tableManager()
            .queryTableOptimizingInfo(optimizerGroup1, db1, wrongTableFilter2, statusCode, 10, 0);
    Assert.assertEquals(0, (int) res.getRight());
    Assert.assertTrue(res.getLeft().isEmpty());
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
        tableService()
            .getRuntime(serverTableIdentifier().getId())
            .getOptimizingProcess()
            .getStatus());
    Assertions.assertEquals(
        OptimizingStatus.COMMITTING,
        tableService().getRuntime(serverTableIdentifier().getId()).getOptimizingStatus());
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
      execute(tableService().getRuntime(serverTableIdentifier().getId()));
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
