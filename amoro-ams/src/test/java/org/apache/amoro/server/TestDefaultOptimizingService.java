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
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.optimizing.RewriteFilesOutput;
import org.apache.amoro.optimizing.TableOptimizing;
import org.apache.amoro.server.exception.IllegalTaskStateException;
import org.apache.amoro.server.exception.PluginRetryAuthException;
import org.apache.amoro.server.optimizing.OptimizingProcess;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.optimizing.TaskRuntime;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.amoro.server.table.AMSTableTestBase;
import org.apache.amoro.server.table.TableRuntime;
import org.apache.amoro.server.table.executor.TableRuntimeRefreshExecutor;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.SerializationUtil;
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
      optimizingService()
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

    TaskRuntime taskRuntime =
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
    OptimizerInstance optimizer = optimizingService().listOptimizers().get(0);
    long oldTouchTime = optimizer.getTouchTime();
    Thread.sleep(1);
    optimizingService().touch(token);
    Assertions.assertTrue(optimizer.getTouchTime() > oldTouchTime);
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

    TaskRuntime taskRuntime =
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
    assertTaskCompleted(null);
    Assertions.assertNull(optimizingService().pollTask(token, THREAD_ID));
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

  private void assertTaskCompleted(TaskRuntime taskRuntime) {
    if (taskRuntime != null) {
      Assertions.assertEquals(TaskRuntime.Status.SUCCESS, taskRuntime.getStatus());
    }
    Assertions.assertEquals(
        0, optimizingService().listTasks(defaultResourceGroup().getName()).size());
    Assertions.assertEquals(
        OptimizingProcess.Status.RUNNING,
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
      super(tableService(), 1, Integer.MAX_VALUE);
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
