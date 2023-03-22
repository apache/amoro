/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.ams.server.optimize;

import com.netease.arctic.TableTestBase;
import com.netease.arctic.ams.api.OptimizeStatus;
import com.netease.arctic.ams.api.OptimizeTaskId;
import com.netease.arctic.ams.api.OptimizeType;
import com.netease.arctic.ams.server.model.BaseOptimizeTask;
import com.netease.arctic.ams.server.model.BaseOptimizeTaskRuntime;
import com.netease.arctic.ams.server.model.TableMetadata;
import com.netease.arctic.ams.server.utils.JDBCSqlSessionFactoryProvider;
import com.netease.arctic.table.TableProperties;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.iceberg.util.PropertyUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.platform.commons.util.Preconditions;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.netease.arctic.ams.server.util.DerbyTestUtil.get;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest({
    JDBCSqlSessionFactoryProvider.class
})
@PowerMockIgnore({"org.apache.logging.log4j.*", "javax.management.*", "org.apache.http.conn.ssl.*",
    "com.amazonaws.http.conn.ssl.*",
    "javax.net.ssl.*", "org.apache.hadoop.*", "javax.*", "com.sun.org.apache.*", "org.apache.xerces.*"})
public class TableOptimizeItemTest extends TableTestBase {

  @Before
  public void mock() {
    mockStatic(JDBCSqlSessionFactoryProvider.class);
    when(JDBCSqlSessionFactoryProvider.get()).thenAnswer((Answer<SqlSessionFactory>) invocation ->
        get());
  }

  @Test
  public void testNoNeedClearFailedTasksWithFailedTask() {
    TableMetadata metadata = new TableMetadata();
    metadata.setTableIdentifier(testKeyedTable.id());
    metadata.setProperties(testKeyedTable.properties());
    TableOptimizeItem tableOptimizeItem =
        new TableOptimizeItem(testKeyedTable, metadata, System.currentTimeMillis() + 6 * 60 * 60 * 1000);

    int maxRetry = PropertyUtil
        .propertyAsInt(testKeyedTable.properties(), TableProperties.OPTIMIZE_RETRY_NUMBER,
            TableProperties.OPTIMIZE_RETRY_NUMBER_DEFAULT);
    Preconditions.condition(maxRetry > 0, "max retry can't less than 1");
    List<OptimizeTaskItem> mockTasksItem = new ArrayList<>(mockCommonTaskItems(maxRetry, 10,
        Arrays.asList(OptimizeStatus.Init, OptimizeStatus.Pending, OptimizeStatus.Executing, OptimizeStatus.Failed,
            OptimizeStatus.Prepared, OptimizeStatus.Committed)));
    tableOptimizeItem.initOptimizeTasks(mockTasksItem);

    Assert.assertEquals(10, tableOptimizeItem.getOptimizeTasks().size());
    tableOptimizeItem.clearFailedTasks();
    Assert.assertEquals(10, tableOptimizeItem.getOptimizeTasks().size());
  }

  @Test
  public void testNoNeedClearFailedTasks() {
    TableMetadata metadata = new TableMetadata();
    metadata.setTableIdentifier(testKeyedTable.id());
    metadata.setProperties(testKeyedTable.properties());
    TableOptimizeItem tableOptimizeItem =
        new TableOptimizeItem(testKeyedTable, metadata, System.currentTimeMillis() + 6 * 60 * 60 * 1000);

    int maxRetry = PropertyUtil
        .propertyAsInt(testKeyedTable.properties(), TableProperties.OPTIMIZE_RETRY_NUMBER,
            TableProperties.OPTIMIZE_RETRY_NUMBER_DEFAULT);
    Preconditions.condition(maxRetry > 0, "max retry can't less than 1");
    List<OptimizeTaskItem> mockTasksItem = new ArrayList<>(mockCommonTaskItems(maxRetry + 1, 10,
        Arrays.asList(OptimizeStatus.Init, OptimizeStatus.Pending, OptimizeStatus.Executing, OptimizeStatus.Prepared, OptimizeStatus.Committed)));
    tableOptimizeItem.initOptimizeTasks(mockTasksItem);

    Assert.assertEquals(10, tableOptimizeItem.getOptimizeTasks().size());
    tableOptimizeItem.clearFailedTasks();
    Assert.assertEquals(10, tableOptimizeItem.getOptimizeTasks().size());
  }

  @Test
  public void testClearFailedTasks() {
    TableMetadata metadata = new TableMetadata();
    metadata.setTableIdentifier(testKeyedTable.id());
    metadata.setProperties(testKeyedTable.properties());
    TableOptimizeItem tableOptimizeItem =
        new TableOptimizeItem(testKeyedTable, metadata, System.currentTimeMillis() + 6 * 60 * 60 * 1000);

    int maxRetry = PropertyUtil
        .propertyAsInt(testKeyedTable.properties(), TableProperties.OPTIMIZE_RETRY_NUMBER,
            TableProperties.OPTIMIZE_RETRY_NUMBER_DEFAULT);
    Preconditions.condition(maxRetry > 0, "max retry can't less than 1");
    List<OptimizeTaskItem> mockTasksItem = new ArrayList<>(mockCommonTaskItems(maxRetry, 10,
        Arrays.asList(OptimizeStatus.Init, OptimizeStatus.Pending, OptimizeStatus.Executing, OptimizeStatus.Failed,
            OptimizeStatus.Prepared, OptimizeStatus.Committed)));

    // construct need clear failed task
    BaseOptimizeTaskRuntime optimizeTaskRuntime = new BaseOptimizeTaskRuntime();
    optimizeTaskRuntime.setRetry(maxRetry + 1);
    optimizeTaskRuntime.setStatus(OptimizeStatus.Failed);
    BaseOptimizeTask basicOptimizeTask = new BaseOptimizeTask();
    basicOptimizeTask.setTaskId(new OptimizeTaskId(OptimizeType.Minor, "failed task id"));
    OptimizeTaskItem taskItem = new OptimizeTaskItem(basicOptimizeTask, optimizeTaskRuntime);
    mockTasksItem.add(taskItem);
    tableOptimizeItem.initOptimizeTasks(mockTasksItem);

    Assert.assertEquals(11, tableOptimizeItem.getOptimizeTasks().size());
    tableOptimizeItem.clearFailedTasks();
    Assert.assertEquals(0, tableOptimizeItem.getOptimizeTasks().size());
  }

  private List<OptimizeTaskItem> mockCommonTaskItems(int maxRetry, int count, List<OptimizeStatus> status) {
    Preconditions.condition(CollectionUtils.isNotEmpty(status), "status list can't be empty");

    List<OptimizeTaskItem> result = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      int index = i % status.size();
      BaseOptimizeTaskRuntime optimizeTaskRuntime = new BaseOptimizeTaskRuntime();
      optimizeTaskRuntime.setRetry(Math.max(maxRetry - 1, 0));
      optimizeTaskRuntime.setStatus(status.get(index));
      BaseOptimizeTask basicOptimizeTask = new BaseOptimizeTask();
      basicOptimizeTask.setTaskId(new OptimizeTaskId(OptimizeType.Minor, String.valueOf(i)));
      OptimizeTaskItem taskItem = new OptimizeTaskItem(basicOptimizeTask, optimizeTaskRuntime);
      result.add(taskItem);
    }

    return result;
  }
}