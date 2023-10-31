package com.netease.arctic.server;

import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.OptimizerRegisterInfo;
import com.netease.arctic.ams.api.OptimizingTask;
import com.netease.arctic.ams.api.OptimizingTaskId;
import com.netease.arctic.ams.api.OptimizingTaskResult;
import com.netease.arctic.ams.api.resource.ResourceGroup;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.io.MixedDataTestHelpers;
import com.netease.arctic.optimizing.RewriteFilesOutput;
import com.netease.arctic.optimizing.TableOptimizing;
import com.netease.arctic.server.exception.IllegalTaskStateException;
import com.netease.arctic.server.exception.PluginRetryAuthException;
import com.netease.arctic.server.optimizing.OptimizingStatus;
import com.netease.arctic.server.optimizing.TaskRuntime;
import com.netease.arctic.server.resource.OptimizerInstance;
import com.netease.arctic.server.table.AMSTableTestBase;
import com.netease.arctic.server.table.TableConfiguration;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.table.TableRuntimeMeta;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.SerializationUtil;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class TestDefaultOptimizingService extends AMSTableTestBase {

  private String token;

  public TestDefaultOptimizingService(CatalogTestHelper catalogTestHelper,
                             TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper, false);
  }

  @BeforeEach
  public void prepare() {
    token = optimizingService().authenticate(buildRegisterInfo());
    createDatabase();
    createTable();
    initTableWithFiles();
  }

  @AfterEach
  public void clear() {
    try {
      optimizingService().listOptimizers()
          .forEach(optimizer -> optimizingService().deleteOptimizer(optimizer.getGroupName(),
              optimizer.getResourceId()));
      dropTable();
      dropDatabase();
    } catch (Exception e) {
      // ignore
    }
  }

  private void initTableWithFiles() {
    ArcticTable arcticTable = (ArcticTable) tableService().loadTable(serverTableIdentifier()).originalTable();
    appendData(arcticTable.asUnkeyedTable(), 1);
    appendData(arcticTable.asUnkeyedTable(), 2);
    TableRuntime runtime = tableService().getRuntime(serverTableIdentifier());

    runtime.refresh(tableService().loadTable(serverTableIdentifier()));
  }

  private List<DataFile> appendData(UnkeyedTable table, int id) {
    ArrayList<Record> newRecords = Lists.newArrayList(
        MixedDataTestHelpers.createRecord(table.schema(), id, "111", 0L, "2022-01-01T12:00:00"));
    List<DataFile> dataFiles = MixedDataTestHelpers.writeBaseStore(table, 0L, newRecords, false);
    AppendFiles appendFiles = table.newAppend();
    dataFiles.forEach(appendFiles::appendFile);
    appendFiles.commit();
    return dataFiles;
  }

  @Test
  public void testPollWithoutAuth() {
    // 1.poll task
    clear();
    Assertions.assertThrows(PluginRetryAuthException.class, () ->
        optimizingService().pollTask("whatever", 1));
  }

  @Test
  public void testPollOnce() {
    // 1.poll task
    OptimizingTask task = optimizingService().pollTask(token, 1);
    Assertions.assertNotNull(task);
  }

  @Test
  public void testPollTaskTwice() {
    // 1.poll task
    OptimizingTask task = optimizingService().pollTask(token, 1);
    Assertions.assertNotNull(task);
    optimizingService().touch(token);

    // 3.fail task
    optimizingService().completeTask(token,
        buildOptimizingTaskFailResult(task.getTaskId(), 1, "unknown error"));

    // 4.retry poll task
    OptimizingTask task2 = optimizingService().pollTask(token, 1);
    Assertions.assertEquals(task2, task);
  }

  @Test
  public void testPollTaskThreeTimes() {
    // 1.poll task
    OptimizingTask task = optimizingService().pollTask(token, 1);
    Assertions.assertNotNull(task);

    // 3.fail task
    optimizingService().completeTask(token,
        buildOptimizingTaskFailResult(task.getTaskId(), 1, "unknown error"));

    // 4.retry poll task
    OptimizingTask task2 = optimizingService().pollTask(token, 1);
    Assertions.assertEquals(task2, task);

    optimizingService().completeTask(token,
        buildOptimizingTaskFailResult(task.getTaskId(), 1, "unknown error"));

    // retry again
    OptimizingTask task3 = optimizingService().pollTask(token, 1);
    Assertions.assertEquals(task3, task);

    //third time would be null
    Assertions.assertNull(optimizingService().pollTask(token, 1));
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
    OptimizingTask task = optimizingService().pollTask(token, 1);
    Assertions.assertNotNull(task);
    Thread.sleep(600);
    Assertions.assertThrows(PluginRetryAuthException.class, () ->
        optimizingService().touch(token));
    Assertions.assertThrows(PluginRetryAuthException.class, () ->
        optimizingService().pollTask(token, 1));
    token = optimizingService().authenticate(buildRegisterInfo());
    OptimizingTask task2 = optimizingService().pollTask(token, 1);
    Assertions.assertNotEquals(task2, task);
  }

  @Test
  public void testAckAndCompleteTask() {
    OptimizingTask task = optimizingService().pollTask(token, 1);
    Assertions.assertNotNull(task);
    Assertions.assertThrows(IllegalTaskStateException.class, () ->
        optimizingService().completeTask(token, buildOptimizingTaskResult(task.getTaskId(), 1)));

    optimizingService().ackTask(token, 1, task.getTaskId());
    optimizingService().completeTask(token, buildOptimizingTaskResult(task.getTaskId(), 1));
  }

  @Test
  public void testReloadScheduledTask() {
    // 1.poll task
    OptimizingTask task = optimizingService().pollTask(token, 1);
    Assertions.assertNotNull(task);

    reload();
    optimizingService().ackTask(token, 1, task.getTaskId());
    optimizingService().completeTask(token, buildOptimizingTaskResult(task.getTaskId(), 1));
  }

  @Test
  public void testReloadAckTask() {
    // 1.poll task
    OptimizingTask task = optimizingService().pollTask(token, 1);
    Assertions.assertNotNull(task);
    optimizingService().ackTask(token, 1, task.getTaskId());

    reload();
    optimizingService().completeTask(token, buildOptimizingTaskResult(task.getTaskId(), 1));
    Assertions.assertEquals(optimizingService().listTasks(defaultResourceGroup().getName()).get(0).getStatus(),
        TaskRuntime.Status.SUCCESS);
  }

  @Test
  public void testReloadCompletedTask() {
    // 1.poll task
    OptimizingTask task = optimizingService().pollTask(token, 1);
    Assertions.assertNotNull(task);
    optimizingService().ackTask(token, 1, task.getTaskId());
    optimizingService().completeTask(token, buildOptimizingTaskResult(task.getTaskId(), 1));

    reload();
    Assertions.assertEquals(optimizingService().listTasks(defaultResourceGroup().getName()).get(0).getStatus(),
        TaskRuntime.Status.SUCCESS);
  }

  @Test
  public void testReloadFailedTask() {
    // 1.poll task
    OptimizingTask task = optimizingService().pollTask(token, 1);
    Assertions.assertNotNull(task);
    optimizingService().ackTask(token, 1, task.getTaskId());
    optimizingService().completeTask(token,
        buildOptimizingTaskFailResult(task.getTaskId(), 1, "error"));

    reload();
    Assertions.assertEquals(optimizingService().listTasks(defaultResourceGroup().getName()).get(0).getStatus(),
        TaskRuntime.Status.FAILED);
    Assertions.assertEquals(optimizingService().listTasks(defaultResourceGroup().getName()).get(0).getFailReason(),
        "error");
  }

  private OptimizerRegisterInfo buildRegisterInfo() {
    OptimizerRegisterInfo registerInfo = new OptimizerRegisterInfo();
    registerInfo.setThreadCount(1);
    registerInfo.setMemoryMb(1024);
    registerInfo.setGroupName(defaultResourceGroup().getName());
    registerInfo.setResourceId("1");
    registerInfo.setStartTime(System.currentTimeMillis());
    return registerInfo;
  }

  private ResourceGroup defaultResourceGroup() {
    return new ResourceGroup.Builder("test", "local").build();
  }

  private TableRuntimeMeta buildTableRuntimeMeta(OptimizingStatus status, ResourceGroup resourceGroup) {
    ArcticTable arcticTable = (ArcticTable) tableService().loadTable(serverTableIdentifier()).originalTable();
    TableRuntimeMeta tableRuntimeMeta = new TableRuntimeMeta();
    tableRuntimeMeta.setCatalogName(serverTableIdentifier().getCatalog());
    tableRuntimeMeta.setDbName(serverTableIdentifier().getDatabase());
    tableRuntimeMeta.setTableName(serverTableIdentifier().getTableName());
    tableRuntimeMeta.setTableId(serverTableIdentifier().getId());
    tableRuntimeMeta.setTableStatus(status);
    tableRuntimeMeta.setTableConfig(TableConfiguration.parseConfig(arcticTable.properties()));
    tableRuntimeMeta.setOptimizerGroup(resourceGroup.getName());
    tableRuntimeMeta.constructTableRuntime(tableService());
    return tableRuntimeMeta;
  }

  private OptimizingTaskResult buildOptimizingTaskResult(OptimizingTaskId taskId, int threadId) {
    TableOptimizing.OptimizingOutput output = new RewriteFilesOutput(null, null, null);
    OptimizingTaskResult optimizingTaskResult = new OptimizingTaskResult(taskId, threadId);
    optimizingTaskResult.setTaskOutput(SerializationUtil.simpleSerialize(output));
    return optimizingTaskResult;
  }

  private OptimizingTaskResult buildOptimizingTaskFailResult(OptimizingTaskId taskId, int threadId,
                                                             String errorMessage) {
    TableOptimizing.OptimizingOutput output = new RewriteFilesOutput(null, null, null);
    OptimizingTaskResult optimizingTaskResult = new OptimizingTaskResult(taskId, threadId);
    optimizingTaskResult.setTaskOutput(SerializationUtil.simpleSerialize(output));
    optimizingTaskResult.setErrorMessage(errorMessage);
    return optimizingTaskResult;
  }
}
