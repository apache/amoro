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
import com.netease.arctic.ams.server.model.MetricsSummary;
import com.netease.arctic.ams.server.model.TableMetricsStatistic;
import com.netease.arctic.ams.server.service.impl.TableMetricsStatisticService;
import com.netease.arctic.table.KeyedTable;
import org.apache.iceberg.SnapshotSummary;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static com.netease.arctic.ams.server.AmsTestBase.AMS_TEST_CATALOG_NAME;
import static com.netease.arctic.ams.server.AmsTestBase.AMS_TEST_DB_NAME;
import static com.netease.arctic.ams.server.AmsTestBase.catalog;

public class TestTableMetricsStatisticService extends TableTestBase {

  private static TableMetricsStatisticService service = ServiceContainer.getTableMetricsStatisticService();

  @Test
  public void testCommitMetrics() {
    com.netease.arctic.table.TableIdentifier tableId =
        com.netease.arctic.table.TableIdentifier.of(AMS_TEST_CATALOG_NAME, AMS_TEST_DB_NAME,
            "file_sync_test_keyed_table");
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
        service.getMetrics(tableId, "base", SnapshotSummary.TOTAL_FILE_SIZE_PROP);
    Assert.assertEquals(1, baseSizeMetrics.size());
    Assert.assertEquals(
        FILE_A.fileSizeInBytes() + FILE_B.fileSizeInBytes(),
        Long.parseLong(baseSizeMetrics.get(0).getMetricValue()));
    List<TableMetricsStatistic> baseCountMetrics =
        service.getMetrics(tableId, "base", SnapshotSummary.TOTAL_DATA_FILES_PROP);
    Assert.assertEquals(1, baseCountMetrics.size());
    Assert.assertEquals(2, Long.parseLong(baseCountMetrics.get(0).getMetricValue()));
    List<TableMetricsStatistic> changeSizeMetrics =
        service.getMetrics(tableId, "change", SnapshotSummary.TOTAL_FILE_SIZE_PROP);
    Assert.assertEquals(1, changeSizeMetrics.size());
    Assert.assertEquals(
        FILE_A.fileSizeInBytes() + FILE_B.fileSizeInBytes(),
        Long.parseLong(changeSizeMetrics.get(0).getMetricValue()));
    List<TableMetricsStatistic> changeCountMetrics =
        service.getMetrics(tableId, "change", SnapshotSummary.TOTAL_DATA_FILES_PROP);
    Assert.assertEquals(1, changeCountMetrics.size());
    Assert.assertEquals(2, Long.parseLong(changeCountMetrics.get(0).getMetricValue()));
    service.summaryMetrics();
    List<MetricsSummary> summaries = service.getMetricsSummary("total-files-size");
    Assert.assertEquals(1, summaries.size());
    Assert.assertEquals(
        2 * (FILE_A.fileSizeInBytes() + FILE_B.fileSizeInBytes()),
        Long.parseLong(summaries.get(0).getMetricValue().trim()));
  }
}
