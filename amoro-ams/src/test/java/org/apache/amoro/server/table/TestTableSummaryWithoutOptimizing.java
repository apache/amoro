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

package org.apache.amoro.server.table;

import static org.apache.amoro.server.table.TableSummaryMetrics.TABLE_SUMMARY_HEALTH_SCORE;
import static org.apache.amoro.server.table.TableSummaryMetrics.TABLE_SUMMARY_TOTAL_FILES;
import static org.apache.amoro.server.table.TableSummaryMetrics.TABLE_SUMMARY_TOTAL_FILES_SIZE;
import static org.apache.amoro.server.table.TableSummaryMetrics.TABLE_SUMMARY_TOTAL_RECORDS;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.metrics.Gauge;
import org.apache.amoro.metrics.Metric;
import org.apache.amoro.metrics.MetricDefine;
import org.apache.amoro.metrics.MetricKey;
import org.apache.amoro.server.manager.MetricManager;
import org.apache.amoro.server.optimizing.OptimizingTestHelpers;
import org.apache.amoro.server.scheduler.inline.TableRuntimeRefreshExecutor;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.data.Record;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Tests that table_summary metrics are collected when self-optimizing is disabled. */
@RunWith(Parameterized.class)
public class TestTableSummaryWithoutOptimizing extends AMSTableTestBase {

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(true, false)}
    };
  }

  public TestTableSummaryWithoutOptimizing(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper, false);
  }

  @Before
  public void prepare() {
    createDatabase();
    createTable();
  }

  @After
  public void clear() {
    try {
      dropTable();
      dropDatabase();
    } catch (Exception e) {
      // ignore
    }
  }

  private UnkeyedTable loadUnkeyedTable() {
    return ((MixedTable) tableService().loadTable(serverTableIdentifier()).originalTable())
        .asUnkeyedTable();
  }

  private void initTableWithFiles() {
    UnkeyedTable table = loadUnkeyedTable();
    appendData(table);
    appendPosDelete(table);
    DefaultTableRuntime runtime = getDefaultTableRuntime(serverTableIdentifier().getId());
    runtime.refresh(tableService().loadTable(serverTableIdentifier()));
  }

  private void appendData(UnkeyedTable table) {
    ArrayList<Record> records =
        Lists.newArrayList(
            tableTestHelper().generateTestRecord(1, "111", 0, "2022-01-01T12:00:00"),
            tableTestHelper().generateTestRecord(2, "222", 0, "2022-01-01T12:00:00"));
    OptimizingTestHelpers.appendBase(
        table, tableTestHelper().writeBaseStore(table, 0L, records, false));
  }

  private void appendPosDelete(UnkeyedTable table) {
    ArrayList<Record> records =
        Lists.newArrayList(
            tableTestHelper().generateTestRecord(3, "333", 0, "2022-01-01T12:00:00"),
            tableTestHelper().generateTestRecord(4, "444", 0, "2022-01-01T12:00:00"));
    List<DataFile> dataFiles =
        OptimizingTestHelpers.appendBase(
            table, tableTestHelper().writeBaseStore(table, 0L, records, false));
    List<DeleteFile> posDeleteFiles = Lists.newArrayList();
    for (DataFile dataFile : dataFiles) {
      posDeleteFiles.addAll(
          MixedDataTestHelpers.writeBaseStorePosDelete(
              table, 0L, dataFile, Collections.singletonList(0L)));
    }
    OptimizingTestHelpers.appendBasePosDelete(table, posDeleteFiles);
  }

  private void refreshPending() {
    TableRuntimeRefreshExecutor refresher =
        new TableRuntimeRefreshExecutor(tableService(), 1, Integer.MAX_VALUE, Integer.MAX_VALUE);
    refresher.execute(getDefaultTableRuntime(serverTableIdentifier().getId()));
    refresher.dispose();
  }

  @SuppressWarnings("unchecked")
  private Gauge<Long> getMetric(
      Map<MetricKey, Metric> metrics, ServerTableIdentifier identifier, MetricDefine define) {
    return (Gauge<Long>)
        metrics.get(
            new MetricKey(
                define,
                ImmutableMap.of(
                    "catalog",
                    identifier.getCatalog(),
                    "database",
                    identifier.getDatabase(),
                    "table",
                    identifier.getTableName())));
  }

  @Test
  public void testSummaryCollectedWhenOptimizingDisabledAndSummaryEnabled() {
    // First, add files with optimizing enabled (default), then verify metrics via refresh
    initTableWithFiles();
    refreshPending();

    Map<MetricKey, Metric> metrics = MetricManager.getInstance().getGlobalRegistry().getMetrics();
    ServerTableIdentifier identifier = serverTableIdentifier();

    Gauge<Long> totalFiles = getMetric(metrics, identifier, TABLE_SUMMARY_TOTAL_FILES);
    Gauge<Long> totalSize = getMetric(metrics, identifier, TABLE_SUMMARY_TOTAL_FILES_SIZE);
    Gauge<Long> totalRecords = getMetric(metrics, identifier, TABLE_SUMMARY_TOTAL_RECORDS);
    Gauge<Long> healthScore = getMetric(metrics, identifier, TABLE_SUMMARY_HEALTH_SCORE);

    // Baseline: metrics should be populated with optimizing enabled
    long baselineTotalFiles = totalFiles.getValue();
    long baselineTotalSize = totalSize.getValue();
    Assertions.assertTrue(baselineTotalFiles > 0, "baseline totalFiles should be > 0");
    Assertions.assertTrue(baselineTotalSize > 0, "baseline totalSize should be > 0");

    // Now disable optimizing and explicitly enable table-summary
    UnkeyedTable table = loadUnkeyedTable();
    table
        .updateProperties()
        .set(TableProperties.ENABLE_SELF_OPTIMIZING, "false")
        .set(TableProperties.TABLE_SUMMARY_ENABLED, "true")
        .commit();
    DefaultTableRuntime runtime = getDefaultTableRuntime(serverTableIdentifier().getId());
    runtime.refresh(tableService().loadTable(serverTableIdentifier()));

    // Append more data to create a new snapshot
    appendData(loadUnkeyedTable());
    runtime.refresh(tableService().loadTable(serverTableIdentifier()));

    // Execute refresh - should collect summary via summary-only branch
    refreshPending();

    // Verify: metrics should be updated (totalFiles/totalSize should increase from new data)
    Assertions.assertTrue(
        totalFiles.getValue() >= baselineTotalFiles,
        "totalFiles should be >= baseline after summary-only refresh");
    Assertions.assertTrue(
        totalSize.getValue() >= baselineTotalSize,
        "totalSize should be >= baseline after summary-only refresh");
    Assertions.assertTrue(totalRecords.getValue() > 0, "totalRecords should be > 0");
    Assertions.assertTrue(healthScore.getValue() >= 0, "healthScore should be >= 0");
  }

  @Test
  public void testSummaryNotCollectedWhenOptimizingDisabledWithDefault() {
    // Disable optimizing only — table-summary.enabled defaults to false
    UnkeyedTable table = loadUnkeyedTable();
    table.updateProperties().set(TableProperties.ENABLE_SELF_OPTIMIZING, "false").commit();
    DefaultTableRuntime runtime = getDefaultTableRuntime(serverTableIdentifier().getId());
    runtime.refresh(tableService().loadTable(serverTableIdentifier()));

    // Add files and refresh
    initTableWithFiles();
    refreshPending();

    Map<MetricKey, Metric> metrics = MetricManager.getInstance().getGlobalRegistry().getMetrics();
    ServerTableIdentifier identifier = serverTableIdentifier();

    Gauge<Long> totalFiles = getMetric(metrics, identifier, TABLE_SUMMARY_TOTAL_FILES);
    Gauge<Long> healthScore = getMetric(metrics, identifier, TABLE_SUMMARY_HEALTH_SCORE);

    // Metrics should remain at initial values (not collected)
    Assertions.assertEquals(0, totalFiles.getValue(), "totalFiles should remain 0");
    Assertions.assertEquals(-1, healthScore.getValue(), "healthScore should remain at initial -1");
  }

  @Test
  public void testSummaryCollectedRepeatedlyWithoutNewSnapshots() {
    // Setup: add files, then switch to summary-only mode
    initTableWithFiles();
    UnkeyedTable table = loadUnkeyedTable();
    table
        .updateProperties()
        .set(TableProperties.ENABLE_SELF_OPTIMIZING, "false")
        .set(TableProperties.TABLE_SUMMARY_ENABLED, "true")
        .commit();
    DefaultTableRuntime runtime = getDefaultTableRuntime(serverTableIdentifier().getId());
    runtime.refresh(tableService().loadTable(serverTableIdentifier()));

    // First refresh — should collect summary
    refreshPending();

    Map<MetricKey, Metric> metrics = MetricManager.getInstance().getGlobalRegistry().getMetrics();
    ServerTableIdentifier identifier = serverTableIdentifier();
    Gauge<Long> totalFiles = getMetric(metrics, identifier, TABLE_SUMMARY_TOTAL_FILES);
    Gauge<Long> healthScore = getMetric(metrics, identifier, TABLE_SUMMARY_HEALTH_SCORE);

    long firstTotalFiles = totalFiles.getValue();
    long firstHealthScore = healthScore.getValue();
    Assertions.assertTrue(firstTotalFiles > 0, "first refresh should collect totalFiles > 0");
    Assertions.assertTrue(firstHealthScore >= 0, "first refresh should collect healthScore >= 0");

    // Second refresh without any new snapshot — still collected because data was never optimized
    refreshPending();

    Assertions.assertEquals(
        firstTotalFiles,
        totalFiles.getValue(),
        "totalFiles should remain the same after second refresh without new snapshot");
    Assertions.assertEquals(
        firstHealthScore,
        healthScore.getValue(),
        "healthScore should remain the same after second refresh without new snapshot");

    // Third refresh — still works
    refreshPending();

    Assertions.assertEquals(
        firstTotalFiles,
        totalFiles.getValue(),
        "totalFiles should remain the same after third refresh without new snapshot");
  }

  @Test
  public void testSummaryUpdatedAfterNewSnapshotInSummaryOnlyMode() {
    // Setup: add initial files
    initTableWithFiles();

    // Switch to summary-only mode
    UnkeyedTable table = loadUnkeyedTable();
    table
        .updateProperties()
        .set(TableProperties.ENABLE_SELF_OPTIMIZING, "false")
        .set(TableProperties.TABLE_SUMMARY_ENABLED, "true")
        .commit();
    DefaultTableRuntime runtime = getDefaultTableRuntime(serverTableIdentifier().getId());
    runtime.refresh(tableService().loadTable(serverTableIdentifier()));

    // First refresh — record baseline
    refreshPending();

    Map<MetricKey, Metric> metrics = MetricManager.getInstance().getGlobalRegistry().getMetrics();
    ServerTableIdentifier identifier = serverTableIdentifier();
    Gauge<Long> totalFiles = getMetric(metrics, identifier, TABLE_SUMMARY_TOTAL_FILES);
    Gauge<Long> totalSize = getMetric(metrics, identifier, TABLE_SUMMARY_TOTAL_FILES_SIZE);

    long baselineTotalFiles = totalFiles.getValue();
    long baselineTotalSize = totalSize.getValue();
    Assertions.assertTrue(baselineTotalFiles > 0, "baseline totalFiles should be > 0");

    // Append more data — creates a new snapshot
    appendData(loadUnkeyedTable());
    runtime.refresh(tableService().loadTable(serverTableIdentifier()));

    // Refresh again — metrics should reflect the additional files
    refreshPending();

    Assertions.assertTrue(
        totalFiles.getValue() > baselineTotalFiles,
        "totalFiles should increase after appending more data");
    Assertions.assertTrue(
        totalSize.getValue() > baselineTotalSize,
        "totalSize should increase after appending more data");
  }

  @Test
  public void testPropertyKeyNotFilteredWhenOptimizingDisabled() {
    // Verify that table-summary.enabled is parsed independently from self-optimizing.enabled
    Map<String, String> properties = new HashMap<>();
    properties.put(TableProperties.ENABLE_SELF_OPTIMIZING, "false");
    properties.put(TableProperties.TABLE_SUMMARY_ENABLED, "true");

    OptimizingConfig config = TableConfigurations.parseOptimizingConfig(properties);
    Assertions.assertFalse(config.isEnabled(), "optimizing should be disabled");
    Assertions.assertTrue(
        config.isTableSummaryEnabled(),
        "tableSummaryEnabled should be true independently of optimizing.enabled");

    // Also verify default: when table-summary.enabled is not set, it defaults to false
    Map<String, String> defaultProperties = new HashMap<>();
    defaultProperties.put(TableProperties.ENABLE_SELF_OPTIMIZING, "false");

    OptimizingConfig defaultConfig = TableConfigurations.parseOptimizingConfig(defaultProperties);
    Assertions.assertFalse(defaultConfig.isEnabled(), "optimizing should be disabled");
    Assertions.assertFalse(
        defaultConfig.isTableSummaryEnabled(), "tableSummaryEnabled should default to false");
  }

  @Test
  public void testReEnableOptimizingAfterSummaryOnlyMode() {
    // Setup: add files, switch to summary-only, collect summary
    initTableWithFiles();
    UnkeyedTable table = loadUnkeyedTable();
    table
        .updateProperties()
        .set(TableProperties.ENABLE_SELF_OPTIMIZING, "false")
        .set(TableProperties.TABLE_SUMMARY_ENABLED, "true")
        .commit();
    DefaultTableRuntime runtime = getDefaultTableRuntime(serverTableIdentifier().getId());
    runtime.refresh(tableService().loadTable(serverTableIdentifier()));
    refreshPending();

    Map<MetricKey, Metric> metrics = MetricManager.getInstance().getGlobalRegistry().getMetrics();
    ServerTableIdentifier identifier = serverTableIdentifier();
    Gauge<Long> totalFiles = getMetric(metrics, identifier, TABLE_SUMMARY_TOTAL_FILES);
    Gauge<Long> healthScore = getMetric(metrics, identifier, TABLE_SUMMARY_HEALTH_SCORE);

    long summaryOnlyTotalFiles = totalFiles.getValue();
    Assertions.assertTrue(summaryOnlyTotalFiles > 0, "summary-only mode should collect metrics");

    // Re-enable optimizing
    table = loadUnkeyedTable();
    table.updateProperties().set(TableProperties.ENABLE_SELF_OPTIMIZING, "true").commit();
    runtime.refresh(tableService().loadTable(serverTableIdentifier()));

    // Append data and refresh — normal optimizing path should work
    appendData(loadUnkeyedTable());
    runtime.refresh(tableService().loadTable(serverTableIdentifier()));
    refreshPending();

    // Metrics should still be collected via the optimizing-enabled branch
    Assertions.assertTrue(
        totalFiles.getValue() > 0, "totalFiles should be collected after re-enabling optimizing");
    Assertions.assertTrue(
        healthScore.getValue() >= 0,
        "healthScore should be collected after re-enabling optimizing");
  }

  @Test
  public void testSummaryCollectedWithOptimizingEnabled() {
    // Verify that table summary is always collected in the optimizing-enabled path,
    // regardless of table-summary.enabled setting
    initTableWithFiles();
    refreshPending();

    Map<MetricKey, Metric> metrics = MetricManager.getInstance().getGlobalRegistry().getMetrics();
    ServerTableIdentifier identifier = serverTableIdentifier();
    Gauge<Long> totalFiles = getMetric(metrics, identifier, TABLE_SUMMARY_TOTAL_FILES);
    Gauge<Long> totalSize = getMetric(metrics, identifier, TABLE_SUMMARY_TOTAL_FILES_SIZE);
    Gauge<Long> healthScore = getMetric(metrics, identifier, TABLE_SUMMARY_HEALTH_SCORE);

    // With optimizing enabled (default), summary should always be collected
    Assertions.assertTrue(totalFiles.getValue() > 0, "totalFiles should be > 0");
    Assertions.assertTrue(totalSize.getValue() > 0, "totalSize should be > 0");
    Assertions.assertTrue(healthScore.getValue() >= 0, "healthScore should be >= 0");

    // Append more data and refresh — summary should still be collected
    long previousTotalFiles = totalFiles.getValue();
    long previousHealthScore = healthScore.getValue();
    appendData(loadUnkeyedTable());
    DefaultTableRuntime runtime = getDefaultTableRuntime(serverTableIdentifier().getId());
    runtime.refresh(tableService().loadTable(serverTableIdentifier()));
    refreshPending();

    Assertions.assertTrue(
        totalFiles.getValue() >= previousTotalFiles,
        "totalFiles should be >= previous after refresh with optimizing enabled");
    Assertions.assertTrue(
        healthScore.getValue() >= 0,
        "healthScore should remain valid after refresh with optimizing enabled");
  }
}
