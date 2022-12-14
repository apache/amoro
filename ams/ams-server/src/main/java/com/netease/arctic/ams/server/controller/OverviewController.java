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

import com.netease.arctic.ams.api.properties.OptimizerProperties;
import com.netease.arctic.ams.server.controller.response.ErrorResponse;
import com.netease.arctic.ams.server.controller.response.OkResponse;
import com.netease.arctic.ams.server.model.OptimizerResourceInfo;
import com.netease.arctic.ams.server.model.OverviewFileSizeVO;
import com.netease.arctic.ams.server.model.OverviewQuotaVO;
import com.netease.arctic.ams.server.model.OverviewSummary;
import com.netease.arctic.ams.server.model.OverviewTableInfo;
import com.netease.arctic.ams.server.model.TableMetricsStatistic;
import com.netease.arctic.ams.server.optimize.IOptimizeService;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.ams.server.service.impl.CatalogMetadataService;
import com.netease.arctic.ams.server.service.impl.MetricsStatisticService;
import com.netease.arctic.ams.server.service.impl.OptimizerService;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import org.apache.iceberg.SnapshotSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class OverviewController extends RestBaseController {

  private static final Logger LOG = LoggerFactory.getLogger(OverviewController.class);
  private static final MetricsStatisticService metricsStatisticService = ServiceContainer.getMetricsStatisticService();
  private static final OptimizerService optimizerService = ServiceContainer.getOptimizerService();
  private static final CatalogMetadataService catalogMetadataService = ServiceContainer.getCatalogMetadataService();
  private static final IOptimizeService optimizeService = ServiceContainer.getOptimizeService();

  public static void summary(Context ctx) {
    try {
      int catalogCnt = catalogMetadataService.getCatalogs().size();
      int tableCnt = optimizeService.listCachedTables().size();
      long tableTotalSize =
          metricsStatisticService
              .getCurrentSummary(SnapshotSummary.TOTAL_FILE_SIZE_PROP)
              .getMetricValue()
              .longValue();
      OptimizerResourceInfo optimizerResourceInfo =
          optimizerService.getOptimizerGroupsResourceInfo();

      OverviewSummary summary = new OverviewSummary(catalogCnt, tableCnt, tableTotalSize,
          optimizerResourceInfo.getOccupationCore(), optimizerResourceInfo.getOccupationMemory());
      ctx.json(OkResponse.of(summary));
    } catch (Exception e) {
      LOG.error("Failed to get overview summary", e);
      ctx.json(new ErrorResponse(HttpCode.INTERNAL_SERVER_ERROR, "Failed to get overview summary", ""));
    }
  }

  public static void quota(Context ctx) {
    try {
      ctx.json(OkResponse.of(OverviewQuotaVO.convert(metricsStatisticService
          .getMetricsSummary(OptimizerProperties.QUOTA_USAGE), optimizerService.getOptimizerGroupsResourceInfo())));
    } catch (Exception e) {
      LOG.error("Failed to get overview quota", e);
      ctx.json(new ErrorResponse(HttpCode.INTERNAL_SERVER_ERROR, "Failed to get overview quota", ""));
    }
  }

  public static void dataSize(Context ctx) {
    try {
      ctx.json(OkResponse.of(OverviewFileSizeVO.convert(metricsStatisticService
          .getMetricsSummary(SnapshotSummary.TOTAL_FILE_SIZE_PROP))));
    } catch (Exception e) {
      LOG.error("Failed to get overview dataSize", e);
      ctx.json(new ErrorResponse(HttpCode.INTERNAL_SERVER_ERROR, "Failed to get overview dataSize", ""));
    }
  }

  public static void tableInfo(Context ctx) {
    try {
      String order = ctx.queryParamAsClass("order", String.class).getOrDefault("desc");
      String orderBy = ctx.queryParamAsClass("orderBy", String.class).getOrDefault("Size");
      Integer count = ctx.queryParamAsClass("count", Integer.class).getOrDefault(10);
      List<OverviewTableInfo> result = new ArrayList<>();
      List<TableMetricsStatistic> ordered;
      switch (orderBy) {
        case "Size":
          ordered = metricsStatisticService
              .getTableMetricsOrdered(order, SnapshotSummary.TOTAL_FILE_SIZE_PROP, count);
          ordered.forEach(e -> {
            OverviewTableInfo info = new OverviewTableInfo();
            info.setTableName(String.format("%s.%s.%s", e.getTableIdentifier().catalog,
                e.getTableIdentifier().database, e.getTableIdentifier().tableName));
            info.setSize(e.getMetricValue().longValue());
            TableMetricsStatistic countMetric = metricsStatisticService
                .getTableMetrics(e.getTableIdentifier(), SnapshotSummary.TOTAL_DATA_FILES_PROP);
            info.setFileCnt(countMetric == null ? 0 : countMetric.getMetricValue().longValue());
            result.add(info);
          });
          break;
        case "Files":
          ordered = metricsStatisticService
              .getTableMetricsOrdered(order, SnapshotSummary.TOTAL_DATA_FILES_PROP, count);
          ordered.forEach(e -> {
            OverviewTableInfo info = new OverviewTableInfo();
            info.setTableName(String.format("%s.%s.%s", e.getTableIdentifier().catalog,
                e.getTableIdentifier().database, e.getTableIdentifier().tableName));
            info.setFileCnt(e.getMetricValue().longValue());
            TableMetricsStatistic countMetric = metricsStatisticService
                .getTableMetrics(e.getTableIdentifier(), SnapshotSummary.TOTAL_FILE_SIZE_PROP);
            info.setSize(countMetric == null ? 0 : countMetric.getMetricValue().longValue());
            result.add(info);
          });
          break;
        default:
          throw new RuntimeException(String.format("do not support order by %s", orderBy));
      }
      ctx.json(OkResponse.of(result));
    } catch (Exception e) {
      LOG.error("Failed to get overview tableInfo", e);
      ctx.json(new ErrorResponse(HttpCode.INTERNAL_SERVER_ERROR, "Failed to get overview tableInfo", ""));
    }
  }
}
