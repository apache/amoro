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
import org.apache.amoro.table.TableRuntimeStore;
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
  private static final long STEP = 30000L; // 30s
  private static final int MAX_PENDING_PARTITIONS = 1;
  private static final int MINOR_LEAST_INTERVAL = 3600000; // 1h

  /**
   * A test helper class that allows configuration updates. Reuses the same TableRuntime instance
   * across different test scenarios.
   */
  private static class TestTableRuntime extends DefaultTableRuntime {
    private OptimizingConfig testOptimizingConfig;

    TestTableRuntime(TableRuntimeStore store, OptimizingConfig optimizingConfig) {
      super(store);
      this.testOptimizingConfig = optimizingConfig;
    }

    /** Update the optimizing config without creating a new instance */
    void updateOptimizingConfig(OptimizingConfig newConfig) {
      this.testOptimizingConfig = newConfig;
    }

    @Override
    public OptimizingConfig getOptimizingConfig() {
      return this.testOptimizingConfig;
    }
  }

  /** Create OptimizingConfig with specified parameters */
  private OptimizingConfig createOptimizingConfig(long maxInterval, long step) {
    OptimizingConfig config = new OptimizingConfig();
    config.setRefreshTableAdaptiveMaxIntervalMs(maxInterval);
    config.setMinorLeastInterval(MINOR_LEAST_INTERVAL);
    config.setRefreshTableAdaptiveIncreaseStepMs(step);
    return config;
  }

  @Test
  public void testAdaptiveRefreshIntervalScenarios() {
    createDatabase();
    createTable();

    // Get the original table runtime
    DefaultTableRuntime baseRuntime =
        (DefaultTableRuntime) tableService().getRuntime(serverTableIdentifier().getId());
    TableRuntimeRefreshExecutor executor =
        new TableRuntimeRefreshExecutor(tableService(), 1, INTERVAL, MAX_PENDING_PARTITIONS);

    // Create a tableRuntime instance with initial config
    OptimizingConfig initialConfig = createOptimizingConfig(MAX_INTERVAL, STEP);
    TestTableRuntime tableRuntime = new TestTableRuntime(baseRuntime.store(), initialConfig);

    // Test 1: Healthy table (not need optimizing) - interval should increase by STEP
    // Initial state: needOptimizing=false, latestRefreshInterval=0 (will use default INTERVAL)
    tableRuntime.setLatestEvaluatedNeedOptimizing(false);
    tableRuntime.setLatestRefreshInterval(0);
    long adaptiveExecutingInterval = executor.getAdaptiveExecutingInterval(tableRuntime);
    long expectedInterval = INTERVAL + STEP;
    Assert.assertEquals(expectedInterval, adaptiveExecutingInterval);

    // Test 2: Test minimum boundary - interval should not below INTERVAL
    // Unhealthy table (need optimizing) - interval should decrease to half
    // current interval is INTERVAL + STEP, the latest interval is INTERVAL
    // not (INTERVAL + STEP) / 2
    tableRuntime.setLatestEvaluatedNeedOptimizing(true);
    tableRuntime.setLatestRefreshInterval(adaptiveExecutingInterval);
    adaptiveExecutingInterval = executor.getAdaptiveExecutingInterval(tableRuntime);
    expectedInterval = INTERVAL;
    Assert.assertEquals(expectedInterval, adaptiveExecutingInterval);

    // Test 3: Healthy table with larger step value - interval should increase by 8 * STEP
    tableRuntime.setLatestEvaluatedNeedOptimizing(false);
    tableRuntime.setLatestRefreshInterval(adaptiveExecutingInterval);
    tableRuntime.updateOptimizingConfig(createOptimizingConfig(MAX_INTERVAL, 8 * STEP));
    adaptiveExecutingInterval = executor.getAdaptiveExecutingInterval(tableRuntime);
    expectedInterval = expectedInterval + 8 * STEP;
    Assert.assertEquals(expectedInterval, adaptiveExecutingInterval);

    // Test 4: Maximum boundary - interval should not exceed MAX_INTERVAL
    tableRuntime.setLatestRefreshInterval(adaptiveExecutingInterval);
    tableRuntime.updateOptimizingConfig(createOptimizingConfig(MAX_INTERVAL, STEP));
    // current interval is INTERVAL + 8 * STEP, the latest interval is MAX_INTERVAL
    // rather than INTERVAL + 9 * STEP
    adaptiveExecutingInterval = executor.getAdaptiveExecutingInterval(tableRuntime);
    Assert.assertEquals(MAX_INTERVAL, adaptiveExecutingInterval);

    dropTable();
    dropDatabase();
  }

  @Test
  public void testGetNextExecutingTime() {
    createDatabase();
    createTable();

    // Get the original table runtime
    DefaultTableRuntime baseRuntime =
        (DefaultTableRuntime) tableService().getRuntime(serverTableIdentifier().getId());
    TableRuntimeRefreshExecutor executor =
        new TableRuntimeRefreshExecutor(tableService(), 1, INTERVAL, MAX_PENDING_PARTITIONS);

    // Create a tableRuntime instance with adaptive interval enabled
    OptimizingConfig configWithAdaptiveEnabled = createOptimizingConfig(MAX_INTERVAL, STEP);
    TestTableRuntime tableRuntime =
        new TestTableRuntime(baseRuntime.store(), configWithAdaptiveEnabled);

    // Test 1: getNextExecutingTime with adaptive interval enabled and positive
    // latestRefreshInterval
    // Set a positive latestRefreshInterval to enable adaptive behavior
    tableRuntime.setLatestRefreshInterval(MAX_INTERVAL);
    long nextExecutingTime = executor.getNextExecutingTime(tableRuntime);
    Assert.assertEquals(MAX_INTERVAL, nextExecutingTime);

    // Test 2: getNextExecutingTime with adaptive interval enabled but latestRefreshInterval is 0
    // Should fall back to min(minorLeastInterval * 4/5, INTERVAL)
    tableRuntime.setLatestRefreshInterval(0);
    nextExecutingTime = executor.getNextExecutingTime(tableRuntime);
    long expectedFallbackInterval = Math.min(MINOR_LEAST_INTERVAL * 4L / 5, INTERVAL);
    Assert.assertEquals(expectedFallbackInterval, nextExecutingTime);

    // Test 3: getNextExecutingTime with adaptive interval disabled (maxInterval <= INTERVAL)
    tableRuntime.updateOptimizingConfig(createOptimizingConfig(INTERVAL, STEP));
    nextExecutingTime = executor.getNextExecutingTime(tableRuntime);
    Assert.assertEquals(expectedFallbackInterval, nextExecutingTime);

    dropTable();
    dropDatabase();
  }
}
