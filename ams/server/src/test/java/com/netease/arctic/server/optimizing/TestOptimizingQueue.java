package com.netease.arctic.server.optimizing;

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.OptimizingTaskId;
import com.netease.arctic.ams.api.OptimizingTaskResult;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.resource.ResourceGroup;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.io.MixedDataTestHelpers;
import com.netease.arctic.optimizing.RewriteFilesOutput;
import com.netease.arctic.optimizing.TableOptimizing;
import com.netease.arctic.server.resource.OptimizerThread;
import com.netease.arctic.server.resource.QuotaProvider;
import com.netease.arctic.server.table.AMSTableTestBase;
import com.netease.arctic.server.table.TableConfiguration;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.table.TableRuntimeMeta;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
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

  private OptimizingQueue buildOptimizingGroupService(TableRuntimeMeta tableRuntimeMeta) {
    return new OptimizingQueue(
        tableService(),
        defaultResourceGroup(),
        quotaProvider,
        planExecutor,
        Collections.singletonList(tableRuntimeMeta),
        1,
        1);
  }

  private OptimizingQueue buildOptimizingGroupService() {
    return new OptimizingQueue(
        tableService(),
        defaultResourceGroup(),
        quotaProvider,
        planExecutor,
        Collections.emptyList(),
        1,
        1);
  }

  @Test
  public void testPollNoTask() {
    TableRuntimeMeta tableRuntimeMeta =
        buildTableRuntimeMeta(OptimizingStatus.PENDING, defaultResourceGroup());
    OptimizingQueue queue = buildOptimizingGroupService(tableRuntimeMeta);
    Assert.assertNull(queue.pollTask(0));
  }

  @Test
  public void testRefreshAndReleaseTable() {
    OptimizingQueue queue = buildOptimizingGroupService();
    Assert.assertEquals(0, queue.getSchedulingPolicy().getTableRuntimeMap().size());
    TableRuntimeMeta tableRuntimeMeta =
        buildTableRuntimeMeta(OptimizingStatus.IDLE, defaultResourceGroup());
    queue.refreshTable(tableRuntimeMeta.getTableRuntime());
    Assert.assertEquals(1, queue.getSchedulingPolicy().getTableRuntimeMap().size());
    Assert.assertTrue(
        queue.getSchedulingPolicy().getTableRuntimeMap().containsKey(serverTableIdentifier()));

    queue.releaseTable(tableRuntimeMeta.getTableRuntime());
    Assert.assertEquals(0, queue.getSchedulingPolicy().getTableRuntimeMap().size());

    queue.refreshTable(tableRuntimeMeta.getTableRuntime());
    Assert.assertEquals(1, queue.getSchedulingPolicy().getTableRuntimeMap().size());
  }

  @Test
  public void testPollTask() {
    TableRuntimeMeta tableRuntimeMeta = initTableWithFiles();
    OptimizingQueue queue = buildOptimizingGroupService(tableRuntimeMeta);

    // 1.poll task
    TaskRuntime task = queue.pollTask(MAX_POLLING_TIME);

    Assert.assertNotNull(task);
    Assert.assertEquals(TaskRuntime.Status.PLANNED, task.getStatus());
    Assert.assertNull(queue.pollTask(0));
  }

  @Test
  public void testRetryTask() {
    TableRuntimeMeta tableRuntimeMeta = initTableWithFiles();
    OptimizingQueue queue = buildOptimizingGroupService(tableRuntimeMeta);

    // 1.poll task
    TaskRuntime task = queue.pollTask(MAX_POLLING_TIME);
    Assert.assertNotNull(task);

    for (int i = 0; i < TableProperties.SELF_OPTIMIZING_EXECUTE_RETRY_NUMBER_DEFAULT; i++) {
      queue.retryTask(task);
      TaskRuntime retryTask = queue.pollTask(MAX_POLLING_TIME);
      Assert.assertEquals(retryTask.getTaskId(), task.getTaskId());
      retryTask.schedule(optimizerThread);
      retryTask.ack(optimizerThread);
      retryTask.complete(optimizerThread, buildOptimizingTaskFailed(task.getTaskId(),
          optimizerThread.getThreadId()));
      Assert.assertEquals(TaskRuntime.Status.PLANNED, task.getStatus());
    }

    queue.retryTask(task);
    TaskRuntime retryTask = queue.pollTask(MAX_POLLING_TIME);
    Assert.assertEquals(retryTask.getTaskId(), task.getTaskId());
    retryTask.schedule(optimizerThread);
    retryTask.ack(optimizerThread);
    retryTask.complete(optimizerThread, buildOptimizingTaskFailed(task.getTaskId(),
        optimizerThread.getThreadId()));
    Assert.assertEquals(TaskRuntime.Status.FAILED, task.getStatus());
  }

  @Test
  public void testCommitTask() {
    TableRuntimeMeta tableRuntimeMeta = initTableWithFiles();
    OptimizingQueue queue = buildOptimizingGroupService(tableRuntimeMeta);
    Assert.assertEquals(0, queue.collectTasks().size());

    TaskRuntime task = queue.pollTask(MAX_POLLING_TIME);
    task.schedule(optimizerThread);
    task.ack(optimizerThread);
    Assert.assertEquals(
        1, queue.collectTasks(t -> t.getStatus() == TaskRuntime.Status.ACKED).size());
    Assert.assertNotNull(task);
    task.complete(
        optimizerThread,
        buildOptimizingTaskResult(task.getTaskId(), optimizerThread.getThreadId()));
    Assert.assertEquals(TaskRuntime.Status.SUCCESS, task.getStatus());

    // 7.commit
    OptimizingProcess optimizingProcess = tableRuntimeMeta.getTableRuntime().getOptimizingProcess();
    Assert.assertEquals(OptimizingProcess.Status.RUNNING, optimizingProcess.getStatus());
    optimizingProcess.commit();
    Assert.assertEquals(OptimizingProcess.Status.SUCCESS, optimizingProcess.getStatus());
    Assert.assertNull(tableRuntimeMeta.getTableRuntime().getOptimizingProcess());

    // 8.commit again
    optimizingProcess.commit();
    Assert.assertEquals(OptimizingProcess.Status.FAILED, optimizingProcess.getStatus());

    // 9.close
    optimizingProcess.close();
    Assert.assertEquals(OptimizingProcess.Status.CLOSED, optimizingProcess.getStatus());

    Assert.assertEquals(0, queue.collectTasks().size());
  }

  @Test
  public void testCollectingTasks() {
    TableRuntimeMeta tableRuntimeMeta = initTableWithFiles();
    OptimizingQueue queue = buildOptimizingGroupService(tableRuntimeMeta);
    Assert.assertEquals(0, queue.collectTasks().size());

    TaskRuntime task = queue.pollTask(MAX_POLLING_TIME);
    Assert.assertNotNull(task);
    task.schedule(optimizerThread);
    Assert.assertEquals(1, queue.collectTasks().size());
    Assert.assertEquals(
        1, queue.collectTasks(t -> t.getStatus() == TaskRuntime.Status.SCHEDULED).size());
  }

  private TableRuntimeMeta initTableWithFiles() {
    ArcticTable arcticTable =
        (ArcticTable) tableService().loadTable(serverTableIdentifier()).originalTable();
    appendData(arcticTable.asUnkeyedTable(), 1);
    appendData(arcticTable.asUnkeyedTable(), 2);
    TableRuntimeMeta tableRuntimeMeta =
        buildTableRuntimeMeta(OptimizingStatus.PENDING, defaultResourceGroup());
    TableRuntime runtime = tableRuntimeMeta.getTableRuntime();

    runtime.refresh(tableService().loadTable(serverTableIdentifier()));
    return tableRuntimeMeta;
  }

  private TableRuntimeMeta buildTableRuntimeMeta(
      OptimizingStatus status, ResourceGroup resourceGroup) {
    ArcticTable arcticTable =
        (ArcticTable) tableService().loadTable(serverTableIdentifier()).originalTable();
    TableRuntimeMeta tableRuntimeMeta = new TableRuntimeMeta();
    tableRuntimeMeta.setCatalogName(serverTableIdentifier().getCatalog());
    tableRuntimeMeta.setDbName(serverTableIdentifier().getDatabase());
    tableRuntimeMeta.setTableName(serverTableIdentifier().getTableName());
    tableRuntimeMeta.setTableId(serverTableIdentifier().getId());
    tableRuntimeMeta.setFormat(TableFormat.ICEBERG);
    tableRuntimeMeta.setTableStatus(status);
    tableRuntimeMeta.setTableConfig(TableConfiguration.parseConfig(arcticTable.properties()));
    tableRuntimeMeta.setOptimizerGroup(resourceGroup.getName());
    tableRuntimeMeta.constructTableRuntime(tableService());
    return tableRuntimeMeta;
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

  private OptimizingTaskResult buildOptimizingTaskResult(OptimizingTaskId taskId, int threadId) {
    TableOptimizing.OptimizingOutput output = new RewriteFilesOutput(null, null, null);
    OptimizingTaskResult optimizingTaskResult = new OptimizingTaskResult(taskId, threadId);
    optimizingTaskResult.setTaskOutput(SerializationUtil.simpleSerialize(output));
    return optimizingTaskResult;
  }

  private OptimizingTaskResult buildOptimizingTaskFailed(OptimizingTaskId taskId, int threadId) {
    TableOptimizing.OptimizingOutput output = new RewriteFilesOutput(null, null, null);
    OptimizingTaskResult optimizingTaskResult = new OptimizingTaskResult(taskId, threadId);
    optimizingTaskResult.setErrorMessage("error");
    return optimizingTaskResult;
  }
}
