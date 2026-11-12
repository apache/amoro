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

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.server.AmoroServiceConstants;
import org.apache.amoro.server.dashboard.model.TableOptimizingInfo;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.optimizing.OptimizingTaskMeta;
import org.apache.amoro.server.optimizing.TaskRuntime;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.table.TableProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Tests for {@link OptimizingUtil#buildTableOptimizeInfo}. */
public class TestTableOptimizingInfo {

  private static final int THREAD_COUNT = 4;

  @Test
  public void testZeroQuota() {
    TableOptimizingInfo tableOptimizingInfo =
        buildOptimizingInfo("0", OptimizingStatus.PENDING, null);
    Assertions.assertEquals(1.0, tableOptimizingInfo.getQuota());
    Assertions.assertEquals(0.0, tableOptimizingInfo.getQuotaOccupation());
  }

  @Test
  public void testNegativeQuota() {
    TableOptimizingInfo tableOptimizingInfo =
        buildOptimizingInfo("-0.5", OptimizingStatus.IDLE, null);
    Assertions.assertEquals(1.0, tableOptimizingInfo.getQuota());
    Assertions.assertTrue(Double.isFinite(tableOptimizingInfo.getQuotaOccupation()));
  }

  @Test
  public void testFractionalQuota() {
    TableOptimizingInfo tableOptimizingInfo =
        buildOptimizingInfo("0.5", OptimizingStatus.PENDING, null);
    Assertions.assertEquals(2.0, tableOptimizingInfo.getQuota());
  }

  @Test
  public void testQuotaGreaterThanOne() {
    TableOptimizingInfo tableOptimizingInfo =
        buildOptimizingInfo("3", OptimizingStatus.PENDING, null);
    Assertions.assertEquals(3.0, tableOptimizingInfo.getQuota());
  }

  @Test
  public void testQuotaOccupationWithRunningTask() {
    long taskStartTime =
        System.currentTimeMillis() - AmoroServiceConstants.QUOTA_LOOK_BACK_TIME / 2;
    OptimizingTaskMeta taskMeta = new OptimizingTaskMeta();
    taskMeta.setStatus(TaskRuntime.Status.ACKED);
    taskMeta.setStartTime(taskStartTime);
    taskMeta.setCostTime(AmoroServiceConstants.QUOTA_LOOK_BACK_TIME / 2);

    TableOptimizingInfo tableOptimizingInfo =
        buildOptimizingInfo("0.25", OptimizingStatus.PENDING, Collections.singletonList(taskMeta));
    Assertions.assertEquals(1.0, tableOptimizingInfo.getQuota());
    Assertions.assertEquals(0.5, tableOptimizingInfo.getQuotaOccupation());
  }

  private TableOptimizingInfo buildOptimizingInfo(
      String quota, OptimizingStatus status, List<OptimizingTaskMeta> processTasks) {
    Map<String, String> tableConfig = new HashMap<>();
    tableConfig.put(TableProperties.SELF_OPTIMIZING_QUOTA, quota);
    TableRuntimeMeta tableRuntimeMeta = new TableRuntimeMeta();
    tableRuntimeMeta.setTableId(1L);
    tableRuntimeMeta.setGroupName("test");
    tableRuntimeMeta.setStatusCode(status.getCode());
    tableRuntimeMeta.setTableConfig(tableConfig);
    return OptimizingUtil.buildTableOptimizeInfo(
        ServerTableIdentifier.of(1L, "catalog", "database", "table", TableFormat.ICEBERG),
        tableRuntimeMeta,
        processTasks,
        null,
        THREAD_COUNT);
  }
}
