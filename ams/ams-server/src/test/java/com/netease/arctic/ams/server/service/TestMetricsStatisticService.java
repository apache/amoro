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

package com.netease.arctic.ams.server.service;

import com.netease.arctic.TableTestBase;
import com.netease.arctic.ams.api.OptimizerMetric;
import com.netease.arctic.ams.api.properties.OptimizerProperties;
import com.netease.arctic.ams.server.model.MetricsSummary;
import com.netease.arctic.ams.server.model.TableMetricsStatistic;
import com.netease.arctic.ams.server.service.impl.MetricsStatisticService;
import com.netease.arctic.ams.server.util.TableUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import java.math.BigDecimal;
import java.util.ArrayList;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.SnapshotSummary;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static com.netease.arctic.ams.server.AmsTestBase.AMS_TEST_CATALOG_NAME;
import static com.netease.arctic.ams.server.AmsTestBase.AMS_TEST_DB_NAME;
import static com.netease.arctic.ams.server.AmsTestBase.AMS_TEST_ICEBERG_CATALOG_NAME;
import static com.netease.arctic.ams.server.AmsTestBase.AMS_TEST_ICEBERG_DB_NAME;
import static com.netease.arctic.ams.server.AmsTestBase.catalog;
import static com.netease.arctic.ams.server.AmsTestBase.icebergCatalog;

public class TestMetricsStatisticService extends TableTestBase {

  private static MetricsStatisticService service = ServiceContainer.getMetricsStatisticService();

  @Test
  public void testCommitTableMetrics() {
    com.netease.arctic.table.TableIdentifier tableId =
        com.netease.arctic.table.TableIdentifier.of(AMS_TEST_CATALOG_NAME, AMS_TEST_DB_NAME,
            "metric_commit_test_keyed_table");
    KeyedTable fileSyncKeyedTable = catalog
        .newTableBuilder(
            tableId,
            TABLE_SCHEMA).withPrimaryKeySpec(PRIMARY_KEY_SPEC).withPartitionSpec(SPEC).create().asKeyedTable();
    fileSyncKeyedTable.baseTable().newFastAppend()
        .appendFile(FILE_A)
        .appendFile(FILE_B)
        .commit();
    fileSyncKeyedTable.changeTable().newFastAppend()
        .appendFile(FILE_A)
        .appendFile(FILE_B)
        .commit();
    List<TableMetricsStatistic> baseSizeMetrics =
        service.getTableMetrics(tableId, "base", SnapshotSummary.TOTAL_FILE_SIZE_PROP);
    Assert.assertEquals(1, baseSizeMetrics.size());
    Assert.assertEquals(
        FILE_A.fileSizeInBytes() + FILE_B.fileSizeInBytes(),
        baseSizeMetrics.get(0).getMetricValue().longValue());
    List<TableMetricsStatistic> baseCountMetrics =
        service.getTableMetrics(tableId, "base", SnapshotSummary.TOTAL_DATA_FILES_PROP);
    Assert.assertEquals(1, baseCountMetrics.size());
    Assert.assertEquals(2, baseCountMetrics.get(0).getMetricValue().longValue());
    List<TableMetricsStatistic> changeSizeMetrics =
        service.getTableMetrics(tableId, "change", SnapshotSummary.TOTAL_FILE_SIZE_PROP);
    Assert.assertEquals(1, changeSizeMetrics.size());
    Assert.assertEquals(
        FILE_A.fileSizeInBytes() + FILE_B.fileSizeInBytes(),
        changeSizeMetrics.get(0).getMetricValue().longValue());
    List<TableMetricsStatistic> changeCountMetrics =
        service.getTableMetrics(tableId, "change", SnapshotSummary.TOTAL_DATA_FILES_PROP);
    Assert.assertEquals(1, changeCountMetrics.size());
    Assert.assertEquals(2, changeCountMetrics.get(0).getMetricValue().longValue());
    service.summaryMetrics();
    List<MetricsSummary> summaries = service.getMetricsSummary("total-files-size");
    Assert.assertNotEquals(
        0,
        summaries.get(summaries.size() - 1).getMetricValue().longValue());
    service.deleteTableMetrics(tableId.buildTableIdentifier());
    service.metricSummaryExpire(System.currentTimeMillis());
    List<MetricsSummary> summaries1 = service.getMetricsSummary("total-files-size");
    Assert.assertEquals(0, summaries1.size());
  }

  @Test
  public void statisticTableFileMetrics() {
    com.netease.arctic.table.TableIdentifier tableId =
        com.netease.arctic.table.TableIdentifier.of(AMS_TEST_CATALOG_NAME, AMS_TEST_DB_NAME,
            "metric_sync_test_keyed_table");
    KeyedTable arcticTable = catalog
        .newTableBuilder(
            tableId,
            TABLE_SCHEMA).withPrimaryKeySpec(PRIMARY_KEY_SPEC).withPartitionSpec(SPEC).create().asKeyedTable();
    arcticTable.baseTable().newFastAppend()
        .appendFile(FILE_A)
        .appendFile(FILE_B)
        .commit();
    arcticTable.changeTable().newFastAppend()
        .appendFile(FILE_A)
        .commit();
    service.statisticTableFileMetrics(arcticTable);
    List<TableMetricsStatistic> baseStatistics = service.getTableMetrics(tableId, "base",
        SnapshotSummary.TOTAL_FILE_SIZE_PROP);
    List<TableMetricsStatistic> changeStatistics = service.getTableMetrics(tableId, "change",
        SnapshotSummary.TOTAL_FILE_SIZE_PROP);
    Assert.assertEquals(
        FILE_A.fileSizeInBytes() + FILE_B.fileSizeInBytes(),
        baseStatistics.get(0).getMetricValue().longValue());
    Assert.assertEquals(
        FILE_A.fileSizeInBytes(),
        changeStatistics.get(0).getMetricValue().longValue());
    service.summaryMetrics();
    List<MetricsSummary> summaries = service.getMetricsSummary(SnapshotSummary.TOTAL_FILE_SIZE_PROP);
    Assert.assertEquals(
        summaries.get(summaries.size() - 1).getMetricValue().longValue(),
        baseStatistics.get(0).getMetricValue().add(changeStatistics.get(0).getMetricValue()).longValue());
    service.deleteTableMetrics(tableId.buildTableIdentifier());

    com.netease.arctic.table.TableIdentifier icebergTableId =
        com.netease.arctic.table.TableIdentifier.of(AMS_TEST_ICEBERG_CATALOG_NAME, AMS_TEST_ICEBERG_DB_NAME,
            "metric_sync_test_iceberg_table");
    ArcticTable icebergTable =
        TableUtil.createIcebergTable(
            "metric_sync_test_iceberg_table",
            TABLE_SCHEMA,
            null,
            PartitionSpec.unpartitioned());
    icebergTable.asUnkeyedTable().newFastAppend()
        .appendFile(FILE_A)
        .appendFile(FILE_B)
        .commit();
    service.statisticTableFileMetrics(icebergTable);
    List<TableMetricsStatistic> icebergMetricsStatistics = service.getTableMetrics(icebergTableId, "base",
        SnapshotSummary.TOTAL_FILE_SIZE_PROP);
    Assert.assertEquals(
        FILE_A.fileSizeInBytes() + FILE_B.fileSizeInBytes(),
        icebergMetricsStatistics.get(0).getMetricValue().longValue());
    service.metricSummaryExpire(System.currentTimeMillis());
    service.summaryMetrics();
    List<MetricsSummary> icebergSummaries = service.getMetricsSummary(SnapshotSummary.TOTAL_FILE_SIZE_PROP);
    Assert.assertEquals(
        icebergMetricsStatistics.get(0).getMetricValue().longValue(),
        icebergSummaries.get(icebergSummaries.size() - 1).getMetricValue().longValue());
    service.deleteTableMetrics(icebergTable.id().buildTableIdentifier());
  }

  @Test
  public void testCommitOptimizerMetrics() {
    List<OptimizerMetric> optimizerMetricList = new ArrayList<>();
    OptimizerMetric optimizerMetric = new OptimizerMetric();
    optimizerMetric.setOptimizerId(1);
    optimizerMetric.setSubtaskId("1");
    optimizerMetric.setMetricName(OptimizerProperties.QUOTA_USAGE);
    optimizerMetric.setMetricValue("56.1");
    optimizerMetricList.add(optimizerMetric);
    service.commitOptimizerMetrics(optimizerMetricList);
    Assert.assertEquals(
        0,
        service.getOptimizerMetrics(1, "1", OptimizerProperties.QUOTA_USAGE)
            .get(0)
            .getMetricValue()
            .compareTo(new BigDecimal("56.1")));
    List<OptimizerMetric> optimizerMetricList2 = new ArrayList<>();
    OptimizerMetric optimizerMetric2 = new OptimizerMetric();
    optimizerMetric2.setOptimizerId(1);
    optimizerMetric2.setSubtaskId("1");
    optimizerMetric2.setMetricName(OptimizerProperties.QUOTA_USAGE);
    optimizerMetric2.setMetricValue("78.1");
    optimizerMetricList2.add(optimizerMetric2);
    service.commitOptimizerMetrics(optimizerMetricList2);
    Assert.assertEquals(
        0,
        service.getOptimizerMetrics(1, "1", OptimizerProperties.QUOTA_USAGE)
            .get(0)
            .getMetricValue()
            .compareTo(new BigDecimal("78.1")));

    List<OptimizerMetric> optimizerMetricList3 = new ArrayList<>();
    OptimizerMetric optimizerMetric3 = new OptimizerMetric();
    optimizerMetric3.setOptimizerId(2);
    optimizerMetric3.setSubtaskId("1");
    optimizerMetric3.setMetricName(OptimizerProperties.QUOTA_USAGE);
    optimizerMetric3.setMetricValue("66.1");
    optimizerMetricList3.add(optimizerMetric3);
    service.commitOptimizerMetrics(optimizerMetricList3);
    service.summaryMetrics();
    List<MetricsSummary> summaries = service.getMetricsSummary(OptimizerProperties.QUOTA_USAGE);
    Assert.assertEquals(
        0,
        summaries.get(summaries.size() - 1)
            .getMetricValue()
            .compareTo(new BigDecimal(String.valueOf((78.1 + 66.1) / 2))));
  }
}
