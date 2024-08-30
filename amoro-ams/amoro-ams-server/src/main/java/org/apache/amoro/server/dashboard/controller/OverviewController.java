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

package org.apache.amoro.server.dashboard.controller;

import io.javalin.http.Context;
import org.apache.amoro.server.dashboard.OverviewCache;
import org.apache.amoro.server.dashboard.model.OverviewBaseData;
import org.apache.amoro.server.dashboard.model.OverviewDataSizeItem;
import org.apache.amoro.server.dashboard.model.OverviewResourceUsageItem;
import org.apache.amoro.server.dashboard.model.OverviewSummary;
import org.apache.amoro.server.dashboard.model.OverviewTopTableItem;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

/** The controller that handles overview page requests. */
public class OverviewController {

  private OverviewCache overviewCache;

  public OverviewController() {
    this.overviewCache = OverviewCache.getInstance();
  }

  public void getResourceUsageHistory(Context ctx) {
    String startTime = ctx.queryParam("startTime");
    Preconditions.checkArgument(StringUtils.isNumeric(startTime), "invalid startTime!");
    List<OverviewResourceUsageItem> resourceUsageHistory =
        overviewCache.getResourceUsageHistory(Long.parseLong(startTime));
    ctx.json(OkResponse.of(resourceUsageHistory));
  }

  public void getDataSizeHistory(Context ctx) {
    String startTime = ctx.queryParam("startTime");
    Preconditions.checkArgument(StringUtils.isNumeric(startTime), "invalid startTime!");
    List<OverviewDataSizeItem> dataSizeHistory =
        overviewCache.getDataSizeHistory(Long.parseLong(startTime));
    ctx.json(OkResponse.of(dataSizeHistory));
  }

  public void getTopTables(Context ctx) {
    String order = ctx.queryParam("order");
    String orderBy = ctx.queryParam("orderBy");
    Integer limit = ctx.queryParamAsClass("limit", Integer.class).getOrDefault(10);
    Preconditions.checkArgument(StringUtils.isNotBlank(order), "order can not be empty");
    Preconditions.checkArgument(StringUtils.isNotBlank(orderBy), "orderBy can not be empty");
    List<OverviewTopTableItem> top10Tables;
    boolean isAsc = "asc".equals(order);

    switch (orderBy) {
      case "tableSize":
        top10Tables = getTopTables(isAsc, OverviewTopTableItem::getTableSize, limit);
        break;
      case "fileCount":
        top10Tables = getTopTables(isAsc, OverviewTopTableItem::getFileCount, limit);
        break;
      case "healthScore":
        top10Tables = getTopTables(isAsc, OverviewTopTableItem::getHealthScore, limit);
        break;
      default:
        throw new IllegalArgumentException("Invalid orderBy: " + orderBy);
    }
    ctx.json(OkResponse.of(top10Tables));
  }

  private List<OverviewTopTableItem> getTopTables(
      boolean asc, ToLongFunction<OverviewTopTableItem> orderby, int limit) {
    return overviewCache.getAllTopTableItem().stream()
        .sorted(
            asc
                ? Comparator.comparingLong(orderby)
                    .thenComparing(OverviewTopTableItem::getTableName)
                : Comparator.comparingLong(orderby)
                    .reversed()
                    .thenComparing(OverviewTopTableItem::getTableName))
        .limit(limit)
        .collect(Collectors.toList());
  }

  public void getSummary(Context ctx) {
    int totalCatalog = overviewCache.getTotalCatalog();
    int totalTableCount = overviewCache.getTotalTableCount();
    long totalDataSize = overviewCache.getTotalDataSize();
    int totalCpu = overviewCache.getTotalCpu();
    long totalMemory = overviewCache.getTotalMemory();

    OverviewSummary overviewSummary =
        new OverviewSummary(totalCatalog, totalTableCount, totalDataSize, totalCpu, totalMemory);
    ctx.json(OkResponse.of(overviewSummary));
  }

  public void getOptimizingStatus(Context ctx) {
    Map<String, Long> optimizingStatus = overviewCache.getOptimizingStatus();
    List<OverviewBaseData> optimizingStatusList =
        optimizingStatus.entrySet().stream()
            .map(status -> new OverviewBaseData(status.getKey(), status.getValue()))
            .collect(Collectors.toList());
    ctx.json(OkResponse.of(optimizingStatusList));
  }
}
