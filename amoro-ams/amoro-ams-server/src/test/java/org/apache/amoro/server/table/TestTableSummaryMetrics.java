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

import static org.apache.amoro.server.table.TableSummaryMetrics.TABLE_SUMMARY_DATA_FILES;
import static org.apache.amoro.server.table.TableSummaryMetrics.TABLE_SUMMARY_DATA_FILES_RECORDS;
import static org.apache.amoro.server.table.TableSummaryMetrics.TABLE_SUMMARY_DATA_FILES_SIZE;
import static org.apache.amoro.server.table.TableSummaryMetrics.TABLE_SUMMARY_EQUALITY_DELETE_FILES;
import static org.apache.amoro.server.table.TableSummaryMetrics.TABLE_SUMMARY_EQUALITY_DELETE_FILES_RECORDS;
import static org.apache.amoro.server.table.TableSummaryMetrics.TABLE_SUMMARY_EQUALITY_DELETE_FILES_SIZE;
import static org.apache.amoro.server.table.TableSummaryMetrics.TABLE_SUMMARY_POSITION_DELETE_FILES;
import static org.apache.amoro.server.table.TableSummaryMetrics.TABLE_SUMMARY_POSITION_DELETE_FILES_RECORDS;
import static org.apache.amoro.server.table.TableSummaryMetrics.TABLE_SUMMARY_POSITION_DELETE_FILES_SIZE;
import static org.apache.amoro.server.table.TableSummaryMetrics.TABLE_SUMMARY_SNAPSHOTS;
import static org.apache.amoro.server.table.TableSummaryMetrics.TABLE_SUMMARY_TOTAL_FILES;
import static org.apache.amoro.server.table.TableSummaryMetrics.TABLE_SUMMARY_TOTAL_FILES_SIZE;
import static org.apache.amoro.server.table.TableSummaryMetrics.TABLE_SUMMARY_TOTAL_RECORDS;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.api.OptimizerProperties;
import org.apache.amoro.api.OptimizerRegisterInfo;
import org.apache.amoro.api.ServerTableIdentifier;
import org.apache.amoro.api.metrics.Gauge;
import org.apache.amoro.api.metrics.Metric;
import org.apache.amoro.api.metrics.MetricDefine;
import org.apache.amoro.api.metrics.MetricKey;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.server.exception.PluginRetryAuthException;
import org.apache.amoro.server.manager.MetricManager;
import org.apache.amoro.server.optimizing.OptimizingTestHelpers;
import org.apache.amoro.server.table.executor.TableRuntimeRefreshExecutor;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.iceberg.AppendFiles;
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
import java.util.List;
import java.util.Map;

@RunWith(Parameterized.class)
public class TestTableSummaryMetrics extends AMSTableTestBase {

  private String token;
  private Toucher toucher;

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(false, true)}
    };
  }

  public TestTableSummaryMetrics(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper, false);
  }

  @Before
  public void prepare() {
    toucher = new Toucher();
    createDatabase();
    createTable();
    initTableWithFiles();
    TableRuntimeRefresher refresher = new TableRuntimeRefresher();
    refresher.refreshPending();
    refresher.dispose();
  }

  @After
  public void clear() {
    try {
      if (toucher != null) {
        toucher.stop();
        toucher = null;
      }
      optimizingService()
          .listOptimizers()
          .forEach(
              optimizer ->
                  optimizingService()
                      .deleteOptimizer(optimizer.getGroupName(), optimizer.getResourceId()));
      dropTable();
      dropDatabase();
    } catch (Exception e) {
      // ignore
    }
  }

  private void initTableWithFiles() {
    MixedTable mixedTable =
        (MixedTable) tableService().loadTable(serverTableIdentifier()).originalTable();
    appendData(mixedTable.asUnkeyedTable(), 1);
    appendData(mixedTable.asUnkeyedTable(), 2);
    appendPosDelete(mixedTable.asUnkeyedTable(), 3);
    appendPosDelete(mixedTable.asUnkeyedTable(), 4);
    TableRuntime runtime = tableService().getRuntime(serverTableIdentifier());

    runtime.refresh(tableService().loadTable(serverTableIdentifier()));
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

  private void appendPosDelete(UnkeyedTable table, int id) {
    ArrayList<Record> newRecords =
        Lists.newArrayList(
            tableTestHelper().generateTestRecord(id, "222", 0, "2022-01-01T12:00:00"));
    List<DataFile> dataFiles = MixedDataTestHelpers.writeBaseStore(table, 0L, newRecords, false);
    List<DeleteFile> posDeleteFiles = Lists.newArrayList();
    for (DataFile dataFile : dataFiles) {
      posDeleteFiles.addAll(
          MixedDataTestHelpers.writeBaseStorePosDelete(
              table, 0L, dataFile, Collections.singletonList(0L)));
    }
    OptimizingTestHelpers.appendBasePosDelete(table, posDeleteFiles);
  }

  @Test
  public void testTableSummaryMetrics() {
    ServerTableIdentifier identifier = serverTableIdentifier();
    Map<MetricKey, Metric> metrics = MetricManager.getInstance().getGlobalRegistry().getMetrics();
    assertTableSummaryMetric(metrics, identifier, TABLE_SUMMARY_TOTAL_FILES);
    assertTableSummaryMetric(metrics, identifier, TABLE_SUMMARY_DATA_FILES);
    assertTableSummaryMetric(metrics, identifier, TABLE_SUMMARY_POSITION_DELETE_FILES);
    assertTableSummaryMetric(metrics, identifier, TABLE_SUMMARY_EQUALITY_DELETE_FILES);

    assertTableSummaryMetric(metrics, identifier, TABLE_SUMMARY_TOTAL_FILES_SIZE);
    assertTableSummaryMetric(metrics, identifier, TABLE_SUMMARY_DATA_FILES_SIZE);
    assertTableSummaryMetric(metrics, identifier, TABLE_SUMMARY_POSITION_DELETE_FILES_SIZE);
    assertTableSummaryMetric(metrics, identifier, TABLE_SUMMARY_EQUALITY_DELETE_FILES_SIZE);

    assertTableSummaryMetric(metrics, identifier, TABLE_SUMMARY_TOTAL_RECORDS);
    assertTableSummaryMetric(metrics, identifier, TABLE_SUMMARY_DATA_FILES_RECORDS);
    assertTableSummaryMetric(metrics, identifier, TABLE_SUMMARY_POSITION_DELETE_FILES_RECORDS);
    assertTableSummaryMetric(metrics, identifier, TABLE_SUMMARY_EQUALITY_DELETE_FILES_RECORDS);

    assertTableSummaryMetric(metrics, identifier, TABLE_SUMMARY_SNAPSHOTS);
  }

  private void assertTableSummaryMetric(
      Map<MetricKey, Metric> metrics, ServerTableIdentifier identifier, MetricDefine metricDefine) {
    Gauge<Long> metric =
        (Gauge<Long>)
            metrics.get(
                new MetricKey(
                    metricDefine,
                    ImmutableMap.of(
                        "catalog",
                        identifier.getCatalog(),
                        "database",
                        identifier.getDatabase(),
                        "table",
                        identifier.getTableName())));
    Assertions.assertTrue(metric.getValue() > 0);
  }

  private OptimizerRegisterInfo buildRegisterInfo() {
    OptimizerRegisterInfo registerInfo = new OptimizerRegisterInfo();
    Map<String, String> registerProperties = Maps.newHashMap();
    registerProperties.put(OptimizerProperties.OPTIMIZER_HEART_BEAT_INTERVAL, "100");
    registerInfo.setProperties(registerProperties);
    registerInfo.setThreadCount(1);
    registerInfo.setMemoryMb(1024);
    registerInfo.setGroupName(defaultResourceGroup().getName());
    registerInfo.setResourceId("1");
    registerInfo.setStartTime(System.currentTimeMillis());
    return registerInfo;
  }

  private class TableRuntimeRefresher extends TableRuntimeRefreshExecutor {

    public TableRuntimeRefresher() {
      super(tableService(), 1, Integer.MAX_VALUE);
    }

    void refreshPending() {
      execute(tableService().getRuntime(serverTableIdentifier()));
    }
  }

  private class Toucher implements Runnable {

    private volatile boolean stop = false;
    private volatile boolean suspend = false;
    private final Thread thread = new Thread(this);

    public Toucher() {
      token = optimizingService().authenticate(buildRegisterInfo());
      thread.setDaemon(true);
      thread.start();
    }

    public synchronized void stop() throws InterruptedException {
      stop = true;
      thread.interrupt();
      thread.join();
    }

    public synchronized void suspend() {
      suspend = true;
      thread.interrupt();
    }

    public synchronized void goOn() {
      suspend = false;
      thread.interrupt();
    }

    @Override
    public void run() {
      while (!stop) {
        try {
          Thread.sleep(300);
          synchronized (this) {
            if (!suspend) {
              optimizingService().touch(token);
            }
          }
        } catch (PluginRetryAuthException e) {
          e.printStackTrace();
        } catch (Exception ignore) {
          // ignore
        }
      }
    }
  }
}
