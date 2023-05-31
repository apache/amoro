package com.netease.arctic.server.optimizing;

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.OptimizerRegisterInfo;
import com.netease.arctic.ams.api.OptimizingTask;
import com.netease.arctic.ams.api.OptimizingTaskId;
import com.netease.arctic.ams.api.OptimizingTaskResult;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.resource.ResourceGroup;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.io.DataTestHelpers;
import com.netease.arctic.optimizing.RewriteFilesOutput;
import com.netease.arctic.optimizing.TableOptimizing;
import com.netease.arctic.server.ArcticServiceConstants;
import com.netease.arctic.server.persistence.PersistentBase;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;
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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RunWith(Parameterized.class)
public class TestOptimizingQueue extends AMSTableTestBase {

  private final Persistency persistency = new Persistency();

  public TestOptimizingQueue(CatalogTestHelper catalogTestHelper,
                             TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper, true);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
        {new BasicCatalogTestHelper(TableFormat.ICEBERG),
            new BasicTableTestHelper(false, true)}};
  }

  @Test
  public void testPollNoTask() {
    TableRuntimeMeta tableRuntimeMeta = buildTableRuntimeMeta(OptimizingStatus.PENDING, defaultResourceGroup());
    OptimizingQueue queue = new OptimizingQueue(tableService(), defaultResourceGroup(),
        Collections.singletonList(tableRuntimeMeta));
    OptimizingTask optimizingTask = queue.pollTask("", 1);
    Assert.assertNull(optimizingTask);
  }

  @Test
  public void testRefreshTable() {
    OptimizingQueue queue = new OptimizingQueue(tableService(), defaultResourceGroup(),
        Collections.emptyList());
    Assert.assertEquals(0, queue.getSchedulingPolicy().getTableRuntimeMap().size());
    TableRuntimeMeta tableRuntimeMeta = buildTableRuntimeMeta(OptimizingStatus.IDLE, defaultResourceGroup());
    queue.refreshTable(tableRuntimeMeta.getTableRuntime());
    Assert.assertEquals(1, queue.getSchedulingPolicy().getTableRuntimeMap().size());
    Assert.assertTrue(queue.getSchedulingPolicy().getTableRuntimeMap().containsKey(serverTableIdentifier()));

    queue.releaseTable(tableRuntimeMeta.getTableRuntime());
    Assert.assertEquals(0, queue.getSchedulingPolicy().getTableRuntimeMap().size());
  }

  @Test
  public void testHandleTask() {
    ArcticTable arcticTable = tableService().loadTable(serverTableIdentifier());
    appendData(arcticTable.asUnkeyedTable(), 1);
    appendData(arcticTable.asUnkeyedTable(), 2);
    String authToken = "token";
    int threadId = 1;
    OptimizingQueue.OptimizingThread thread = new OptimizingQueue.OptimizingThread(authToken, threadId);
    TableRuntimeMeta tableRuntimeMeta = buildTableRuntimeMeta(OptimizingStatus.PENDING, defaultResourceGroup());
    TableRuntime runtime = tableRuntimeMeta.getTableRuntime();

    runtime.refresh(tableService().loadTable(serverTableIdentifier()));

    OptimizingQueue queue = new OptimizingQueue(tableService(), defaultResourceGroup(),
        Collections.singletonList(tableRuntimeMeta));
    // 1.poll task
    OptimizingTask task = queue.pollTask(authToken, threadId);
    Assert.assertNotNull(task);
    Assert.assertEquals(1, queue.getExecutingTaskMap().size());
    TaskRuntime taskRuntime = queue.getExecutingTaskMap().get(task.getTaskId());
    assertTaskRuntime(taskRuntime, TaskRuntime.Status.SCHEDULED, thread);

    // 2.ack task
    queue.ackTask(authToken, threadId, task.getTaskId());
    Assert.assertEquals(1, queue.getExecutingTaskMap().size());
    taskRuntime = queue.getExecutingTaskMap().get(task.getTaskId());
    assertTaskRuntime(taskRuntime, TaskRuntime.Status.ACKED, thread);

    // 3.fail task
    String errorMessage = "unknown error";
    queue.completeTask(authToken, buildOptimizingTaskFailResult(task.getTaskId(), threadId, errorMessage));
    Assert.assertEquals(0, queue.getExecutingTaskMap().size());
    assertTaskRuntime(taskRuntime, TaskRuntime.Status.PLANNED, null); // retry and change to PLANNED
    Assert.assertEquals(1, taskRuntime.getRetry());
    Assert.assertEquals(errorMessage, taskRuntime.getFailReason());

    // 4.retry poll task
    threadId = 2;
    thread = new OptimizingQueue.OptimizingThread(authToken, threadId);
    task = queue.pollTask(authToken, threadId);
    Assert.assertNotNull(task);
    Assert.assertEquals(1, queue.getExecutingTaskMap().size());
    taskRuntime = queue.getExecutingTaskMap().get(task.getTaskId());
    assertTaskRuntime(taskRuntime, TaskRuntime.Status.SCHEDULED, thread);

    // 5.ackTask
    queue.ackTask(authToken, threadId, task.getTaskId());
    Assert.assertEquals(1, queue.getExecutingTaskMap().size());
    taskRuntime = queue.getExecutingTaskMap().get(task.getTaskId());
    assertTaskRuntime(taskRuntime, TaskRuntime.Status.ACKED, thread);

    // 6.complete task
    queue.completeTask(authToken, buildOptimizingTaskResult(task.getTaskId(), threadId));
    Assert.assertEquals(0, queue.getExecutingTaskMap().size());
    assertTaskRuntime(taskRuntime, TaskRuntime.Status.SUCCESS, null);
  }
  
  @Test
  public void testInitTableRuntime() {
    ArcticTable arcticTable = tableService().loadTable(serverTableIdentifier());
    appendData(arcticTable.asUnkeyedTable(), 1);
    appendData(arcticTable.asUnkeyedTable(), 2);
    String authToken = "token";
    int threadId = 1;
    OptimizingQueue.OptimizingThread thread = new OptimizingQueue.OptimizingThread(authToken, threadId);
    TableRuntimeMeta tableRuntimeMeta = buildTableRuntimeMeta(OptimizingStatus.PENDING, defaultResourceGroup());
    TableRuntime runtime = tableRuntimeMeta.getTableRuntime();

    runtime.refresh(tableService().loadTable(serverTableIdentifier()));

    OptimizingQueue queue = new OptimizingQueue(tableService(), defaultResourceGroup(),
        Collections.singletonList(tableRuntimeMeta));
    // 1.poll task
    OptimizingTask task = queue.pollTask(authToken, threadId);
    Assert.assertNotNull(task);
    Assert.assertEquals(1, queue.getExecutingTaskMap().size());
    TaskRuntime taskRuntime = queue.getExecutingTaskMap().get(task.getTaskId());
    assertTaskRuntime(taskRuntime, TaskRuntime.Status.SCHEDULED, thread);

    // 2.ack task
    queue.ackTask(authToken, threadId, task.getTaskId());
    Assert.assertEquals(1, queue.getExecutingTaskMap().size());
    taskRuntime = queue.getExecutingTaskMap().get(task.getTaskId());
    assertTaskRuntime(taskRuntime, TaskRuntime.Status.ACKED, thread);

    // 3.fail task
    String errorMessage = "unknown error";
    queue.completeTask(authToken, buildOptimizingTaskFailResult(task.getTaskId(), threadId, errorMessage));
    Assert.assertEquals(0, queue.getExecutingTaskMap().size());
    assertTaskRuntime(taskRuntime, TaskRuntime.Status.PLANNED, null); // retry and change to PLANNED
    Assert.assertEquals(1, taskRuntime.getRetry());
    Assert.assertEquals(errorMessage, taskRuntime.getFailReason());
    
    // 4.reload from sysdb
    List<TableRuntimeMeta> tableRuntimeMetas = persistency.selectTableRuntimeMetas();
    Assert.assertEquals(1, tableRuntimeMetas.size());
    tableRuntimeMetas.get(0).constructTableRuntime(tableService());
    queue = new OptimizingQueue(tableService(), defaultResourceGroup(), tableRuntimeMetas);

    Map<OptimizingTaskId, TaskRuntime> taskMap = queue.getExecutingTaskMap();

    // TODO fix bug and check

  }

  @Test
  public void testOptimizer() throws InterruptedException {
    OptimizingQueue queue = new OptimizingQueue(tableService(), defaultResourceGroup(),
        Collections.emptyList());
    OptimizerRegisterInfo registerInfo = new OptimizerRegisterInfo();
    registerInfo.setThreadCount(1);
    registerInfo.setMemoryMb(1024);
    registerInfo.setGroupName(defaultResourceGroup().getName());
    registerInfo.setResourceId("1");
    registerInfo.setStartTime(System.currentTimeMillis());

    // authenticate
    String authToken = queue.authenticate(registerInfo);
    List<OptimizerInstance> optimizers = queue.getOptimizers();
    Assert.assertEquals(1, optimizers.size());
    OptimizerInstance optimizerInstance = optimizers.get(0);
    Assert.assertEquals(authToken, optimizerInstance.getToken());

    // touch
    long oldTouchTime = optimizerInstance.getTouchTime();
    Thread.sleep(1);
    queue.touch(authToken);
    Assert.assertTrue(optimizerInstance.getTouchTime() > oldTouchTime);

    // remove
    queue.removeOptimizer(registerInfo.getResourceId());
    Assert.assertEquals(0, queue.getOptimizers().size());
  }

  private void assertTaskRuntime(TaskRuntime taskRuntime, TaskRuntime.Status status,
                                 OptimizingQueue.OptimizingThread thread) {
    Assert.assertEquals(status, taskRuntime.getStatus());
    Assert.assertEquals(thread, taskRuntime.getOptimizingThread());
  }

  private ResourceGroup defaultResourceGroup() {
    return new ResourceGroup.Builder("test", "local").build();
  }

  private TableRuntimeMeta buildTableRuntimeMeta(OptimizingStatus status, ResourceGroup resourceGroup) {
    ArcticTable arcticTable = tableService().loadTable(serverTableIdentifier());
    TableRuntimeMeta tableRuntimeMeta = new TableRuntimeMeta();
    tableRuntimeMeta.setCatalogName(serverTableIdentifier().getCatalog());
    tableRuntimeMeta.setDbName(serverTableIdentifier().getDatabase());
    tableRuntimeMeta.setTableName(serverTableIdentifier().getTableName());
    tableRuntimeMeta.setTableId(serverTableIdentifier().getId());
    tableRuntimeMeta.setTableStatus(status);
    tableRuntimeMeta.setTableConfig(TableConfiguration.parseConfig(arcticTable.properties()));
    tableRuntimeMeta.setCurrentChangeSnapshotId(ArcticServiceConstants.INVALID_SNAPSHOT_ID);
    tableRuntimeMeta.setCurrentSnapshotId(ArcticServiceConstants.INVALID_SNAPSHOT_ID);
    tableRuntimeMeta.setLastOptimizedChangeSnapshotId(ArcticServiceConstants.INVALID_SNAPSHOT_ID);
    tableRuntimeMeta.setLastOptimizedSnapshotId(ArcticServiceConstants.INVALID_SNAPSHOT_ID);
    tableRuntimeMeta.setOptimizerGroup(resourceGroup.getName());
    tableRuntimeMeta.constructTableRuntime(tableService());
    return tableRuntimeMeta;
  }

  private List<DataFile> appendData(UnkeyedTable table, int id) {
    ArrayList<Record> newRecords = Lists.newArrayList(
        DataTestHelpers.createRecord(table.schema(), id, "111", 0L, "2022-01-01T12:00:00"));
    List<DataFile> dataFiles = DataTestHelpers.writeBaseStore(table, 0L, newRecords, false);
    AppendFiles appendFiles = table.newAppend();
    dataFiles.forEach(appendFiles::appendFile);
    appendFiles.commit();
    return dataFiles;
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

  private static class Persistency extends PersistentBase {
    public List<TableRuntimeMeta> selectTableRuntimeMetas() {
      return getAs(TableMetaMapper.class, TableMetaMapper::selectTableRuntimeMetas);
    }
  }
}
