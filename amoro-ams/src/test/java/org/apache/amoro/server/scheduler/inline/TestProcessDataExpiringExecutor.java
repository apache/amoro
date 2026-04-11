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

package org.apache.amoro.server.scheduler.inline;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.server.AMSServiceTestBase;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.process.TableProcessMeta;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.server.utils.SnowflakeIdGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class TestProcessDataExpiringExecutor extends AMSServiceTestBase {

  private static final long TABLE_ID = 1L;
  private static final ServerTableIdentifier TABLE_IDENTIFIER =
      ServerTableIdentifier.of(
          TABLE_ID, "test_catalog", "test_db", "test_table_expiring", TableFormat.MIXED_ICEBERG);

  private final Persistency persistency = new Persistency();
  private DefaultTableRuntime tableRuntime;
  private TableService tableService;

  @Before
  public void mock() {
    tableRuntime = Mockito.mock(DefaultTableRuntime.class);
    tableService = Mockito.mock(TableService.class);
    Mockito.when(tableRuntime.getTableIdentifier()).thenReturn(TABLE_IDENTIFIER);
    // Clean up any leftover data
    persistency.cleanAll(TABLE_ID);
  }

  @Test
  public void testProcessHistoryExpiringWhenShorterThanKeepTime() {
    // optimizingKeepTime=30d, processKeepTime=7d
    Duration optimizingKeepTime = Duration.ofDays(30);
    Duration expireInterval = Duration.ofHours(1);
    Duration processKeepTime = Duration.ofDays(7);

    long now = System.currentTimeMillis();

    // Insert a terminal (SUCCESS) process at 10 days ago - should be deleted by processKeepTime
    long tenDaysAgo = now - Duration.ofDays(10).toMillis();
    long processId10d = SnowflakeIdGenerator.getMinSnowflakeId(tenDaysAgo) + 1;
    persistency.insertProcess(TABLE_ID, processId10d, ProcessStatus.SUCCESS, tenDaysAgo);

    // Insert a terminal (SUCCESS) process at 3 days ago - should be kept (within processKeepTime)
    long threeDaysAgo = now - Duration.ofDays(3).toMillis();
    long processId3d = SnowflakeIdGenerator.getMinSnowflakeId(threeDaysAgo) + 1;
    persistency.insertProcess(TABLE_ID, processId3d, ProcessStatus.SUCCESS, threeDaysAgo);

    Assert.assertEquals(2, persistency.listProcesses(TABLE_ID).size());

    ProcessDataExpiringExecutor executor =
        new ProcessDataExpiringExecutor(
            tableService, optimizingKeepTime, expireInterval, processKeepTime);
    executor.execute(tableRuntime);

    List<TableProcessMeta> remaining = persistency.listProcesses(TABLE_ID);
    Assert.assertEquals(1, remaining.size());
    Assert.assertEquals(processId3d, remaining.get(0).getProcessId());
  }

  @Test
  public void testProcessHistoryNotExpiringWhenEqualToKeepTime() {
    // When processKeepTime >= optimizingKeepTime, the extra process cleanup should not trigger
    Duration optimizingKeepTime = Duration.ofDays(7);
    Duration expireInterval = Duration.ofHours(1);
    Duration processKeepTime = Duration.ofDays(7);

    long now = System.currentTimeMillis();

    // Insert a terminal process at 5 days ago - within both optimizingKeepTime and processKeepTime
    long fiveDaysAgo = now - Duration.ofDays(5).toMillis();
    long processId5d = SnowflakeIdGenerator.getMinSnowflakeId(fiveDaysAgo) + 1;
    persistency.insertProcess(TABLE_ID, processId5d, ProcessStatus.SUCCESS, fiveDaysAgo);

    Assert.assertEquals(1, persistency.listProcesses(TABLE_ID).size());

    ProcessDataExpiringExecutor executor =
        new ProcessDataExpiringExecutor(
            tableService, optimizingKeepTime, expireInterval, processKeepTime);
    executor.execute(tableRuntime);

    // The process should still exist - not expired by either mechanism
    Assert.assertEquals(1, persistency.listProcesses(TABLE_ID).size());
  }

  @Test
  public void testDeleteExpiredProcessesSkipsActiveStatuses() {
    // optimizingKeepTime=30d, processKeepTime=7d
    Duration optimizingKeepTime = Duration.ofDays(30);
    Duration expireInterval = Duration.ofHours(1);
    Duration processKeepTime = Duration.ofDays(7);

    long now = System.currentTimeMillis();
    long tenDaysAgo = now - Duration.ofDays(10).toMillis();

    // Insert active-status processes at 10 days ago - should NOT be deleted
    long pidRunning = SnowflakeIdGenerator.getMinSnowflakeId(tenDaysAgo) + 1;
    persistency.insertProcess(TABLE_ID, pidRunning, ProcessStatus.RUNNING, tenDaysAgo);

    long pidSubmitted = SnowflakeIdGenerator.getMinSnowflakeId(tenDaysAgo) + 2;
    persistency.insertProcess(TABLE_ID, pidSubmitted, ProcessStatus.SUBMITTED, tenDaysAgo);

    long pidPending = SnowflakeIdGenerator.getMinSnowflakeId(tenDaysAgo) + 3;
    persistency.insertProcess(TABLE_ID, pidPending, ProcessStatus.PENDING, tenDaysAgo);

    long pidCanceling = SnowflakeIdGenerator.getMinSnowflakeId(tenDaysAgo) + 4;
    persistency.insertProcess(TABLE_ID, pidCanceling, ProcessStatus.CANCELING, tenDaysAgo);

    // Insert a terminal process at 10 days ago - SHOULD be deleted
    long pidSuccess = SnowflakeIdGenerator.getMinSnowflakeId(tenDaysAgo) + 5;
    persistency.insertProcess(TABLE_ID, pidSuccess, ProcessStatus.SUCCESS, tenDaysAgo);

    long pidFailed = SnowflakeIdGenerator.getMinSnowflakeId(tenDaysAgo) + 6;
    persistency.insertProcess(TABLE_ID, pidFailed, ProcessStatus.FAILED, tenDaysAgo);

    Assert.assertEquals(6, persistency.listProcesses(TABLE_ID).size());

    ProcessDataExpiringExecutor executor =
        new ProcessDataExpiringExecutor(
            tableService, optimizingKeepTime, expireInterval, processKeepTime);
    executor.execute(tableRuntime);

    List<TableProcessMeta> remaining = persistency.listProcesses(TABLE_ID);
    // Active statuses (RUNNING, SUBMITTED, PENDING, CANCELING) should survive
    Assert.assertEquals(4, remaining.size());
    for (TableProcessMeta meta : remaining) {
      Assert.assertTrue(
          "Expected active status but got: " + meta.getStatus(),
          meta.getStatus() == ProcessStatus.RUNNING
              || meta.getStatus() == ProcessStatus.SUBMITTED
              || meta.getStatus() == ProcessStatus.PENDING
              || meta.getStatus() == ProcessStatus.CANCELING);
    }
  }

  private static class Persistency extends PersistentBase {

    public void insertProcess(long tableId, long processId, ProcessStatus status, long createTime) {
      doAs(
          TableProcessMapper.class,
          mapper ->
              mapper.insertProcess(
                  tableId,
                  processId,
                  "",
                  status,
                  "TEST",
                  status.name(),
                  "LOCAL",
                  0,
                  createTime,
                  Collections.emptyMap(),
                  Collections.emptyMap()));
    }

    public List<TableProcessMeta> listProcesses(long tableId) {
      return getAs(TableProcessMapper.class, mapper -> mapper.listProcessMeta(tableId, null, null));
    }

    public void cleanAll(long tableId) {
      doAs(TableProcessMapper.class, mapper -> mapper.deleteBefore(tableId, Long.MAX_VALUE));
    }
  }
}
