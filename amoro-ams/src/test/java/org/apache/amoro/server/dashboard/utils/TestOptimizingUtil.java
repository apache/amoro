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

package org.apache.amoro.server.dashboard.utils;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.api.OptimizingTaskId;
import org.apache.amoro.api.OptimizingTaskResult;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.optimizing.RewriteFilesOutput;
import org.apache.amoro.optimizing.TableOptimizing;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.server.AmoroServiceConstants;
import org.apache.amoro.server.optimizing.OptimizingQueue;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.optimizing.OptimizingTaskMeta;
import org.apache.amoro.server.optimizing.TaskRuntime;
import org.apache.amoro.server.resource.OptimizerThread;
import org.apache.amoro.server.resource.QuotaProvider;
import org.apache.amoro.server.table.AMSTableTestBase;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.SerializationUtil;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.data.Record;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@RunWith(Parameterized.class)
public class TestOptimizingUtil extends AMSTableTestBase {
  private final long MAX_POLLING_TIME = 5000;
  private final Executor planExecutor = Executors.newSingleThreadExecutor();
  private final QuotaProvider quotaProvider = resourceGroup -> 1;

  private final OptimizerThread optimizerThread =
      new OptimizerThread(1, null) {

        @Override
        public String getToken() {
          return "aah";
        }
      };

  public TestOptimizingUtil(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper, true);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(false, true)}
    };
  }

  @Test
  public void testCalculateQuotaOccupy() {
    long endTime = System.currentTimeMillis();
    long startTime = endTime - AmoroServiceConstants.QUOTA_LOOK_BACK_TIME;

    Assertions.assertEquals(0, OptimizingUtil.calculateQuotaOccupy(null, null, startTime, endTime));

    DefaultTableRuntime tableRuntime = initTableWithFiles();
    OptimizingQueue queue = buildOptimizingGroupService(tableRuntime);
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

    List<TaskRuntime.TaskQuota> quotas = new ArrayList<>();
    TaskRuntime.TaskQuota quota = task.getCurrentQuota();
    quotas.add(quota);
    Assertions.assertTrue(
        OptimizingUtil.calculateQuotaOccupy(null, quotas, startTime, endTime) > 0);

    List<OptimizingTaskMeta> tasks = new ArrayList<>();
    OptimizingTaskMeta taskMeta = new OptimizingTaskMeta();
    taskMeta.setStatus(TaskRuntime.Status.ACKED);
    taskMeta.setStartTime(endTime - 2000);
    taskMeta.setCostTime(1000);
    tasks.add(taskMeta);
    Assertions.assertEquals(
        1000, OptimizingUtil.calculateQuotaOccupy(tasks, null, startTime, endTime));

    Assertions.assertTrue(
        OptimizingUtil.calculateQuotaOccupy(tasks, quotas, startTime, endTime)
            > OptimizingUtil.calculateQuotaOccupy(null, quotas, startTime, endTime));
    Assertions.assertTrue(
        OptimizingUtil.calculateQuotaOccupy(tasks, quotas, startTime, endTime)
            > OptimizingUtil.calculateQuotaOccupy(tasks, null, startTime, endTime));
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

  protected static ResourceGroup testResourceGroup() {
    return new ResourceGroup.Builder("test", "local").build();
  }

  protected DefaultTableRuntime initTableWithFiles() {
    MixedTable mixedTable =
        (MixedTable) tableService().loadTable(serverTableIdentifier()).originalTable();
    appendData(mixedTable.asUnkeyedTable(), 1);
    appendData(mixedTable.asUnkeyedTable(), 2);
    DefaultTableRuntime tableRuntime =
        buildTableRuntimeMeta(OptimizingStatus.PENDING, defaultResourceGroup());

    tableRuntime.refresh(tableService().loadTable(serverTableIdentifier()));
    return tableRuntime;
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

  private DefaultTableRuntime buildTableRuntimeMeta(
      OptimizingStatus status, ResourceGroup resourceGroup) {
    MixedTable mixedTable =
        (MixedTable) tableService().loadTable(serverTableIdentifier()).originalTable();
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

  private OptimizingTaskResult buildOptimizingTaskResult(OptimizingTaskId taskId, int threadId) {
    TableOptimizing.OptimizingOutput output = new RewriteFilesOutput(null, null, null);
    OptimizingTaskResult optimizingTaskResult = new OptimizingTaskResult(taskId, threadId);
    optimizingTaskResult.setTaskOutput(SerializationUtil.simpleSerialize(output));
    return optimizingTaskResult;
  }
}
