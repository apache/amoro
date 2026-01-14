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

import static org.mockito.Mockito.verify;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.TestedCatalogs;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.hive.catalog.HiveCatalogTestHelper;
import org.apache.amoro.hive.catalog.HiveTableTestHelper;
import org.apache.amoro.server.table.AMSTableTestBase;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

@RunWith(Parameterized.class)
public class TestTableRuntimeRefreshExecutor extends AMSTableTestBase {
  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {
        TestedCatalogs.hadoopCatalog(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, true)
      },
      {
        TestedCatalogs.hadoopCatalog(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(false, true)
      },
      {
        TestedCatalogs.hadoopCatalog(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(false, false)
      },
      {
        TestedCatalogs.hadoopCatalog(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, false)
      },
      {
        new HiveCatalogTestHelper(TableFormat.ICEBERG, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(true, true)
      },
      {
        new HiveCatalogTestHelper(TableFormat.ICEBERG, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(false, true)
      },
      {
        new HiveCatalogTestHelper(TableFormat.ICEBERG, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(false, false)
      },
      {
        new HiveCatalogTestHelper(TableFormat.ICEBERG, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(true, false)
      },
    };
  }

  public TestTableRuntimeRefreshExecutor(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  private static final long INTERVAL = 60000L; // 1 minute
  private static final long MAX_INTERVAL = 300000L; // 5 minutes
  private static final long STEP = 30000L; // 30s
  private static final int MAX_PENDING_PARTITIONS = 1;
  private static final int MINOR_LEAST_INTERVAL = 3600000; // 1h
  private static final long INITIAL_SNAPSHOTID = 0L;

  // Create mock DefaultTableRuntime for adaptive interval tests
  private DefaultTableRuntime getMockTableRuntimeWithAdaptiveInterval(
      DefaultTableRuntime tableRuntime,
      boolean needOptimizing,
      boolean adaptiveEnabled,
      long currentInterval,
      long step) {
    DefaultTableRuntime mockTableRuntime = Mockito.mock(DefaultTableRuntime.class);
    Mockito.when(mockTableRuntime.getTableIdentifier())
        .thenReturn(tableRuntime.getTableIdentifier());
    Mockito.when(mockTableRuntime.getLastOptimizedSnapshotId())
        .thenReturn(tableRuntime.getLastOptimizedSnapshotId());
    Mockito.when(mockTableRuntime.getLastOptimizedChangeSnapshotId())
        .thenReturn(tableRuntime.getLastOptimizedChangeSnapshotId());
    Mockito.when(mockTableRuntime.getCurrentSnapshotId()).thenReturn(INITIAL_SNAPSHOTID);
    Mockito.when(mockTableRuntime.getCurrentChangeSnapshotId()).thenReturn(INITIAL_SNAPSHOTID);
    Mockito.when(mockTableRuntime.getLatestRefreshInterval()).thenReturn(currentInterval);
    Mockito.when(mockTableRuntime.getLatestEvaluatedNeedOptimizing()).thenReturn(needOptimizing);

    OptimizingConfig mockOptimizingConfig = Mockito.mock(OptimizingConfig.class);
    Mockito.when(mockOptimizingConfig.getRefreshTableAdaptiveEnabled()).thenReturn(adaptiveEnabled);
    Mockito.when(mockOptimizingConfig.getRefreshTableAdaptiveMaxInterval())
        .thenReturn(MAX_INTERVAL);
    Mockito.when(mockOptimizingConfig.getMinorLeastInterval()).thenReturn(MINOR_LEAST_INTERVAL);
    Mockito.when(mockOptimizingConfig.getRefreshTableAdaptiveIncreaseStep()).thenReturn(step);
    Mockito.when(mockTableRuntime.getOptimizingConfig()).thenReturn(mockOptimizingConfig);

    return mockTableRuntime;
  }

  // Overloaded method with default step factor
  private DefaultTableRuntime getMockTableRuntimeWithAdaptiveInterval(
      DefaultTableRuntime tableRuntime,
      boolean needOptimizing,
      boolean adaptiveEnabled,
      long currentInterval) {
    return getMockTableRuntimeWithAdaptiveInterval(
        tableRuntime, needOptimizing, adaptiveEnabled, currentInterval, STEP);
  }

  @Test
  public void testAdaptiveIntervalScenarios() {
    createDatabase();
    createTable();

    DefaultTableRuntime tableRuntime =
        (DefaultTableRuntime) tableService().getRuntime(serverTableIdentifier().getId());
    TableRuntimeRefreshExecutor executor =
        new TableRuntimeRefreshExecutor(tableService(), 1, INTERVAL, MAX_PENDING_PARTITIONS);

    // Test healthy table (not need optimizing) - interval should increase
    DefaultTableRuntime mockTableRuntime =
        getMockTableRuntimeWithAdaptiveInterval(tableRuntime, false, true, INTERVAL);

    // Initial interval is INTERVAL, should increase by 30000
    long expectedInterval = INTERVAL + STEP;
    executor.execute(mockTableRuntime);

    // Verify that setLatestRefreshInterval was called with the expected value
    verify(mockTableRuntime, Mockito.times(1)).setLatestRefreshInterval(expectedInterval);

    // Test unhealthy table (need optimizing) - interval should decrease
    mockTableRuntime =
        getMockTableRuntimeWithAdaptiveInterval(tableRuntime, true, true, INTERVAL * 2);

    // Current interval is INTERVAL * 2, should be halved
    expectedInterval = (INTERVAL * 2) / 2;
    executor.execute(mockTableRuntime);

    // Verify that setLatestRefreshInterval was called with the expected value
    verify(mockTableRuntime, Mockito.times(1)).setLatestRefreshInterval(expectedInterval);

    // Test when adaptive interval is disabled
    mockTableRuntime = getMockTableRuntimeWithAdaptiveInterval(tableRuntime, false, false, 0);

    executor.execute(mockTableRuntime);

    // Verify that setLatestRefreshInterval was never called
    verify(mockTableRuntime, Mockito.never()).setLatestRefreshInterval(Mockito.anyLong());

    // Test maximum boundary - interval should not exceed MAX_INTERVAL
    mockTableRuntime =
        getMockTableRuntimeWithAdaptiveInterval(tableRuntime, false, true, MAX_INTERVAL - 1000);

    executor.execute(mockTableRuntime);

    // Verify interval is set to MAX_INTERVAL
    verify(mockTableRuntime, Mockito.times(1)).setLatestRefreshInterval(MAX_INTERVAL);

    // Test minimum boundary - interval should not go below INTERVAL
    mockTableRuntime = getMockTableRuntimeWithAdaptiveInterval(tableRuntime, true, true, INTERVAL);

    executor.execute(mockTableRuntime);

    // Verify interval remains at INTERVAL (minimum)
    verify(mockTableRuntime, Mockito.times(1)).setLatestRefreshInterval(INTERVAL);

    // Test initialization case (currentInterval is 0)
    mockTableRuntime = getMockTableRuntimeWithAdaptiveInterval(tableRuntime, false, true, 0);

    // Initial interval is 0, should be initialized to INTERVAL, then increased by 30000
    expectedInterval = INTERVAL + STEP;
    executor.execute(mockTableRuntime);

    // Verify interval was correctly initialized and increased
    verify(mockTableRuntime, Mockito.times(1)).setLatestRefreshInterval(expectedInterval);

    // Test with different step values
    long newStep = 60000L;
    mockTableRuntime =
        getMockTableRuntimeWithAdaptiveInterval(tableRuntime, false, true, INTERVAL, newStep);

    // Initial interval is INTERVAL, should increase by newStep
    expectedInterval = INTERVAL + newStep;
    executor.execute(mockTableRuntime);

    // Verify interval increased with correct step factor
    verify(mockTableRuntime, Mockito.times(1)).setLatestRefreshInterval(expectedInterval);

    dropTable();
    dropDatabase();
  }

  @Test
  public void testGetNextExecutingTime() {
    createDatabase();
    createTable();

    // Test getNextExecutingTime with adaptive interval enabled
    DefaultTableRuntime tableRuntime =
        (DefaultTableRuntime) tableService().getRuntime(serverTableIdentifier().getId());
    DefaultTableRuntime mockTableRuntime =
        getMockTableRuntimeWithAdaptiveInterval(tableRuntime, false, true, MAX_INTERVAL);

    TableRuntimeRefreshExecutor executor =
        new TableRuntimeRefreshExecutor(tableService(), 1, INTERVAL, MAX_PENDING_PARTITIONS);

    // Should use adaptive interval rather than default interval
    long nextExecutingTime = executor.getNextExecutingTime(mockTableRuntime);

    // Verify returned time equals adaptive interval
    Assert.assertEquals(MAX_INTERVAL, nextExecutingTime);

    // Test getNextExecutingTime with adaptive interval disabled
    Mockito.when(mockTableRuntime.getOptimizingConfig().getRefreshTableAdaptiveEnabled())
        .thenReturn(false);
    nextExecutingTime = executor.getNextExecutingTime(mockTableRuntime);

    // Should use default interval rather than adaptive interval
    Assert.assertEquals(INTERVAL, nextExecutingTime);

    // Test getNextExecutingTime with zero adaptive interval (fallback to default)
    Mockito.when(mockTableRuntime.getOptimizingConfig().getRefreshTableAdaptiveEnabled())
        .thenReturn(true);
    Mockito.when(mockTableRuntime.getLatestRefreshInterval()).thenReturn(0L);
    nextExecutingTime = executor.getNextExecutingTime(mockTableRuntime);

    // Should fallback to default interval when adaptive interval is 0
    Assert.assertEquals(INTERVAL, nextExecutingTime);

    dropTable();
    dropDatabase();
  }
}
