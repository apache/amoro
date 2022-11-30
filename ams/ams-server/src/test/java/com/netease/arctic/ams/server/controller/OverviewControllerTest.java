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

package com.netease.arctic.ams.server.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.ams.api.properties.OptimizerProperties;
import com.netease.arctic.ams.server.ArcticMetaStore;
import com.netease.arctic.ams.server.controller.response.OkResponse;
import com.netease.arctic.ams.server.model.MetricsSummary;
import com.netease.arctic.ams.server.model.OptimizerResourceInfo;
import com.netease.arctic.ams.server.model.OverviewQuotaVO;
import com.netease.arctic.ams.server.model.TableMetricsStatistic;
import com.netease.arctic.ams.server.optimize.OptimizeService;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.ams.server.service.impl.CatalogMetadataService;
import com.netease.arctic.ams.server.service.impl.MetricsStatisticService;
import com.netease.arctic.ams.server.service.impl.OptimizerService;
import com.netease.arctic.ams.server.utils.JDBCSqlSessionFactoryProvider;
import io.javalin.testtools.JavalinTest;
import org.apache.iceberg.SnapshotSummary;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @Description: OverviewController Test
 */
@PrepareForTest({
    JDBCSqlSessionFactoryProvider.class,
    ArcticMetaStore.class,
    ServiceContainer.class,
    MetricsStatisticService.class
})
@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*"})
public class OverviewControllerTest {

  private static MetricsStatisticService metricsStatisticService;
  private static OptimizerService optimizerService;
  private static OptimizeService optimizeService;
  private static CatalogMetadataService catalogMetadataService;

  @BeforeClass
  public static void before() {
    metricsStatisticService = mock(MetricsStatisticService.class);
    when(ServiceContainer.getMetricsStatisticService()).thenReturn(metricsStatisticService);
    optimizerService = mock(OptimizerService.class);
    when(ServiceContainer.getOptimizerService()).thenReturn(optimizerService);
    optimizeService = mock(OptimizeService.class);
    when(ServiceContainer.getOptimizeService()).thenReturn(optimizeService);
    catalogMetadataService = mock(CatalogMetadataService.class);
    when(ServiceContainer.getCatalogMetadataService()).thenReturn(catalogMetadataService);
  }

  @Test
  public void summary() {
    MetricsSummary summary = new MetricsSummary();
    summary.setCommitTime(1L);
    summary.setMetricName(SnapshotSummary.TOTAL_FILE_SIZE_PROP);
    summary.setMetricValue(BigDecimal.valueOf(1000));
    List<CatalogMeta> catalogMetaList = new ArrayList<>();
    catalogMetaList.add(new CatalogMeta());
    when(catalogMetadataService.getCatalogs()).thenReturn(catalogMetaList);
    List<com.netease.arctic.table.TableIdentifier> tables = new ArrayList<>();
    tables.add(new com.netease.arctic.table.TableIdentifier());
    when(optimizeService.listCachedTables()).thenReturn(tables);
    when(metricsStatisticService.getCurrentSummary(SnapshotSummary.TOTAL_FILE_SIZE_PROP)).thenReturn(summary);
    OptimizerResourceInfo optimizerResourceInfo = new OptimizerResourceInfo();
    optimizerResourceInfo.setOccupationCore(20);
    optimizerResourceInfo.setOccupationMemory(1024);
    when(optimizerService.getOptimizerGroupsResourceInfo()).thenReturn(optimizerResourceInfo);
    JavalinTest.test((app, client) -> {
      app.get("/", OverviewController::summary);
      final okhttp3.Response resp = client.get("/", x -> {});
      OkResponse<JSONObject> result = JSONObject.parseObject(resp.body().string(), OkResponse.class);
      assert result.getResult().getInteger("catalogCnt") == 1;
      assert result.getResult().getInteger("tableTotalSize") == 1000;
      assert result.getResult().getInteger("totalCpu") == 20;
      assert result.getResult().getInteger("totalMemory") == 1024;
      assert result.getResult().getInteger("tableCnt") == 1;
      assert result.getCode() == 200;
    });
  }

  @Test
  public void quota() {
    List<MetricsSummary> metricsSummaries = new ArrayList<>();
    MetricsSummary metricsSummary = new MetricsSummary();
    metricsSummaries.add(metricsSummary);
    metricsSummary.setMetricValue(BigDecimal.valueOf(83.24));
    metricsSummary.setCommitTime(1669776182286L);
    when(metricsStatisticService.getMetricsSummary(OptimizerProperties.QUOTA_USAGE)).thenReturn(metricsSummaries);
    OptimizerResourceInfo optimizerResourceInfo = new OptimizerResourceInfo();
    optimizerResourceInfo.setOccupationCore(2196);
    when(optimizerService.getOptimizerGroupsResourceInfo()).thenReturn(optimizerResourceInfo);
    JavalinTest.test((app, client) -> {
      app.get("/", OverviewController::quota);
      final okhttp3.Response resp = client.get("/", x -> {});
      OkResponse<JSONObject> result = JSONObject.parseObject(resp.body().string(), OkResponse.class);
      OverviewQuotaVO overviewQuotaVO = result.getResult().toJavaObject(OverviewQuotaVO.class);
      assert overviewQuotaVO.getTimeLine().get(0).equals("11-30 10:43");
      assert overviewQuotaVO.getUsedCpu().get(0).equals("83.24");
      assert overviewQuotaVO.getUsedCpuDivision().get(0).equals("1828核/2196核");
      assert overviewQuotaVO.getUsedCpuPercent().get(0).equals("83.24%");
      assert result.getCode() == 200;
    });
  }

  @Test
  public void dataSize() {
    List<MetricsSummary> metricsSummaries = new ArrayList<>();
    MetricsSummary metricsSummary = new MetricsSummary();
    metricsSummaries.add(metricsSummary);
    metricsSummary.setMetricValue(BigDecimal.valueOf(1024 * 100));
    metricsSummary.setCommitTime(1669776182286L);
    when(metricsStatisticService.getMetricsSummary(SnapshotSummary.TOTAL_FILE_SIZE_PROP)).thenReturn(metricsSummaries);
    JavalinTest.test((app, client) -> {
      app.get("/", OverviewController::dataSize);
      final okhttp3.Response resp = client.get("/", x -> {});
      OkResponse<JSONObject> result = JSONObject.parseObject(resp.body().string(), OkResponse.class);
      assert result.getResult().getJSONArray("timeLine").get(0).equals("11-30 10:43");
      assert result.getResult().getJSONArray("size").get(0).equals("0.10MB");
      assert result.getCode() == 200;
    });
  }

  @Test
  public void tableInfo() {
    TableIdentifier identifier = new TableIdentifier("test", "test", "test");
    List<TableMetricsStatistic> tableMetricsStatistics = new ArrayList<>();
    TableMetricsStatistic tableMetricsStatistic = new TableMetricsStatistic();
    tableMetricsStatistic.setTableIdentifier(identifier);
    tableMetricsStatistic.setMetricName(SnapshotSummary.TOTAL_FILE_SIZE_PROP);
    tableMetricsStatistic.setMetricValue(BigDecimal.valueOf(1000));
    tableMetricsStatistics.add(tableMetricsStatistic);
    when(metricsStatisticService.getTableMetricsOrdered("desc", SnapshotSummary.TOTAL_FILE_SIZE_PROP, 10)).thenReturn(
        tableMetricsStatistics);

    TableMetricsStatistic size = new TableMetricsStatistic();
    size.setTableIdentifier(identifier);
    size.setMetricName(SnapshotSummary.TOTAL_DATA_FILES_PROP);
    size.setMetricValue(BigDecimal.valueOf(50));
    when(metricsStatisticService.getTableMetrics(identifier, SnapshotSummary.TOTAL_DATA_FILES_PROP)).thenReturn(size);
    JavalinTest.test((app, client) -> {
      app.get("/", OverviewController::tableInfo);
      final okhttp3.Response resp = client.get("/", x -> {});
      OkResponse<JSONArray> result = JSONObject.parseObject(resp.body().string(), OkResponse.class);
      JSONObject re = result.getResult().getJSONObject(0);
      assert re.getInteger("size") == 1000;
      assert re.getInteger("fileCnt") == 50;
      assert result.getCode() == 200;
    });
  }
}
