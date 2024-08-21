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
import org.apache.amoro.api.ServerTableIdentifier;
import org.apache.amoro.api.metrics.Gauge;
import org.apache.amoro.api.metrics.Metric;
import org.apache.amoro.api.metrics.MetricDefine;
import org.apache.amoro.api.metrics.MetricKey;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.server.manager.MetricManager;
import org.apache.amoro.server.optimizing.OptimizingTestHelpers;
import org.apache.amoro.server.table.executor.TableRuntimeRefreshExecutor;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
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

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(true, false)}
    };
  }

  public TestTableSummaryMetrics(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper, false);
  }

  @Before
  public void prepare() {
    createDatabase();
    createTable();
    initTableWithFiles();
    refreshPending();
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

  private void initTableWithFiles() {
    UnkeyedTable table =
        ((MixedTable) tableService().loadTable(serverTableIdentifier()).originalTable())
            .asUnkeyedTable();
    appendData(table);
    appendPosDelete(table);
    TableRuntime runtime = tableService().getRuntime(serverTableIdentifier());
    runtime.refresh(tableService().loadTable(serverTableIdentifier()));
  }

  private void appendData(UnkeyedTable table) {
    ArrayList<Record> newRecords =
        Lists.newArrayList(
            tableTestHelper().generateTestRecord(1, "111", 0, "2022-01-01T12:00:00"),
            tableTestHelper().generateTestRecord(2, "222", 0, "2022-01-01T12:00:00"));
    List<DataFile> dataFiles =
        OptimizingTestHelpers.appendBase(
            table, tableTestHelper().writeBaseStore(table, 0L, newRecords, false));

    AppendFiles appendFiles = table.newAppend();
    dataFiles.forEach(appendFiles::appendFile);
    appendFiles.commit();
  }

  private void appendPosDelete(UnkeyedTable table) {
    ArrayList<Record> newRecords =
        Lists.newArrayList(
            tableTestHelper().generateTestRecord(3, "333", 0, "2022-01-01T12:00:00"),
            tableTestHelper().generateTestRecord(4, "444", 0, "2022-01-01T12:00:00"));
    List<DataFile> dataFiles =
        OptimizingTestHelpers.appendBase(
            table, tableTestHelper().writeBaseStore(table, 0L, newRecords, false));
    List<DeleteFile> posDeleteFiles = Lists.newArrayList();
    for (DataFile dataFile : dataFiles) {
      posDeleteFiles.addAll(
          MixedDataTestHelpers.writeBaseStorePosDelete(
              table, 0L, dataFile, Collections.singletonList(0L)));
    }
    OptimizingTestHelpers.appendBasePosDelete(table, posDeleteFiles);
  }

  void refreshPending() {
    TableRuntimeRefreshExecutor refresher =
        new TableRuntimeRefreshExecutor(tableService(), 1, Integer.MAX_VALUE);
    refresher.execute(tableService().getRuntime(serverTableIdentifier()));
    refresher.dispose();
  }

  @Test
  public void testTableSummaryMetrics() {
    ServerTableIdentifier identifier = serverTableIdentifier();
    Map<MetricKey, Metric> metrics = MetricManager.getInstance().getGlobalRegistry().getMetrics();
    assertTableSummaryMetric(metrics, identifier, TABLE_SUMMARY_TOTAL_FILES);
    assertTableSummaryMetric(metrics, identifier, TABLE_SUMMARY_DATA_FILES);
    assertTableSummaryMetric(metrics, identifier, TABLE_SUMMARY_POSITION_DELETE_FILES);

    assertTableSummaryMetric(metrics, identifier, TABLE_SUMMARY_TOTAL_FILES_SIZE);
    assertTableSummaryMetric(metrics, identifier, TABLE_SUMMARY_DATA_FILES_SIZE);
    assertTableSummaryMetric(metrics, identifier, TABLE_SUMMARY_POSITION_DELETE_FILES_SIZE);

    assertTableSummaryMetric(metrics, identifier, TABLE_SUMMARY_TOTAL_RECORDS);
    assertTableSummaryMetric(metrics, identifier, TABLE_SUMMARY_DATA_FILES_RECORDS);
    assertTableSummaryMetric(metrics, identifier, TABLE_SUMMARY_POSITION_DELETE_FILES_RECORDS);

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
}
