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
  private static final long DEFAULT_MAX_INTERVAL = -1L;
  private static final long STEP = 30000L; // 30s
  private static final int MAX_PENDING_PARTITIONS = 1;
  private static final int MINOR_LEAST_INTERVAL = 3600000; // 1h

  // Create DefaultTableRuntime for adaptive interval tests
  private DefaultTableRuntime buildTableRuntimeWithAdaptiveRefresh(
      DefaultTableRuntime tableRuntime, boolean needOptimizing, long interval, long step) {
    return buildTableRuntimeWithConfig(tableRuntime, needOptimizing, interval, MAX_INTERVAL, step);
  }

  private DefaultTableRuntime buildTableRuntimeWithAdaptiveMaxInterval(
      DefaultTableRuntime tableRuntime, long interval, long maxInterval) {
    return buildTableRuntimeWithConfig(tableRuntime, true, interval, maxInterval, STEP);
  }

  private DefaultTableRuntime buildTableRuntimeWithConfig(
      DefaultTableRuntime tableRuntime,
      boolean needOptimizing,
      long interval,
      long maxInterval,
      long step) {
    OptimizingConfig optimizingConfig = new OptimizingConfig();
    optimizingConfig.setRefreshTableAdaptiveMaxIntervalMs(maxInterval);
    optimizingConfig.setMinorLeastInterval(MINOR_LEAST_INTERVAL);
    optimizingConfig.setRefreshTableAdaptiveIncreaseStepMs(step);

    DefaultTableRuntime newRuntime =
        new DefaultTableRuntime(tableRuntime.store()) {
          @Override
          public OptimizingConfig getOptimizingConfig() {
            return optimizingConfig;
          }
        };
    newRuntime.setLatestEvaluatedNeedOptimizing(needOptimizing);
    newRuntime.setLatestRefreshInterval(interval);

    return newRuntime;
  }

  @Test
  public void testAdaptiveRefreshIntervalScenarios() {
    createDatabase();
    createTable();

    DefaultTableRuntime tableRuntime =
        (DefaultTableRuntime) tableService().getRuntime(serverTableIdentifier().getId());
    TableRuntimeRefreshExecutor executor =
        new TableRuntimeRefreshExecutor(tableService(), 1, INTERVAL, MAX_PENDING_PARTITIONS);

    // Test healthy table (not need optimizing) - interval should increase
    DefaultTableRuntime newTableRuntime =
        buildTableRuntimeWithAdaptiveRefresh(tableRuntime, false, 0, STEP);
    long adaptiveExecutingInterval = executor.getAdaptiveExecutingInterval(newTableRuntime);
    long expectedInterval = INTERVAL + STEP;
    Assert.assertEquals(expectedInterval, adaptiveExecutingInterval);

    // Test minimum boundary - interval should not below INTERVAL
    // The unhealthy table (need optimizing) - interval should decrease half
    // The currentInterval is INTERVAL + STEP, the latest interval is INTERVAL not (INTERVAL + STEP)
    // / 2
    newTableRuntime =
        buildTableRuntimeWithAdaptiveRefresh(
            newTableRuntime, true, adaptiveExecutingInterval, STEP);
    adaptiveExecutingInterval = executor.getAdaptiveExecutingInterval(newTableRuntime);
    expectedInterval = INTERVAL;
    Assert.assertEquals(expectedInterval, adaptiveExecutingInterval);

    // Test healthy table (not need optimizing) with different step values
    newTableRuntime =
        buildTableRuntimeWithAdaptiveRefresh(
            newTableRuntime, false, adaptiveExecutingInterval, 8 * STEP);
    adaptiveExecutingInterval = executor.getAdaptiveExecutingInterval(newTableRuntime);
    expectedInterval = expectedInterval + 8 * STEP;
    Assert.assertEquals(expectedInterval, adaptiveExecutingInterval);

    // Test maximum boundary - interval should not exceed MAX_INTERVAL
    newTableRuntime =
        buildTableRuntimeWithAdaptiveRefresh(
            newTableRuntime, false, adaptiveExecutingInterval, STEP);
    adaptiveExecutingInterval = executor.getAdaptiveExecutingInterval(newTableRuntime);
    Assert.assertEquals(MAX_INTERVAL, adaptiveExecutingInterval);

    dropTable();
    dropDatabase();
  }

  @Test
  public void testGetNextExecutingTime() {
    createDatabase();
    createTable();

    DefaultTableRuntime tableRuntime =
        (DefaultTableRuntime) tableService().getRuntime(serverTableIdentifier().getId());
    TableRuntimeRefreshExecutor executor =
        new TableRuntimeRefreshExecutor(tableService(), 1, INTERVAL, MAX_PENDING_PARTITIONS);

    // Test getNextExecutingTime with adaptive interval enabled
    DefaultTableRuntime newTableRuntime =
        buildTableRuntimeWithAdaptiveMaxInterval(tableRuntime, MAX_INTERVAL, MAX_INTERVAL);
    // Should use adaptive interval rather than default interval
    long nextExecutingTime = executor.getNextExecutingTime(newTableRuntime);
    Assert.assertEquals(MAX_INTERVAL, nextExecutingTime);

    // Test getNextExecutingTime with adaptive interval disabled
    newTableRuntime =
        buildTableRuntimeWithAdaptiveMaxInterval(
            newTableRuntime, MAX_INTERVAL, DEFAULT_MAX_INTERVAL);
    // Should use default interval rather than adaptive interval
    nextExecutingTime = executor.getNextExecutingTime(newTableRuntime);
    Assert.assertEquals(INTERVAL, nextExecutingTime);

    // Test getNextExecutingTime with zero adaptive interval
    newTableRuntime = buildTableRuntimeWithAdaptiveMaxInterval(newTableRuntime, 0, MAX_INTERVAL);
    // Should fallback to default interval when adaptive interval is 0
    nextExecutingTime = executor.getNextExecutingTime(newTableRuntime);
    Assert.assertEquals(INTERVAL, nextExecutingTime);

    dropTable();
    dropDatabase();
  }
}
