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
import org.apache.amoro.server.dashboard.model.OverviewDataSizeItem;
import org.apache.amoro.server.dashboard.model.OverviewResourceUsageItem;
import org.apache.amoro.server.dashboard.model.OverviewSummary;
import org.apache.amoro.server.dashboard.model.OverviewTopTableItem;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
        Comparator<OverviewTopTableItem> tableSizeComparator =
            Comparator.comparingLong(OverviewTopTableItem::getTableSize);
        top10Tables = getTopTables(isAsc, tableSizeComparator, limit);
        break;
      case "fileCount":
        Comparator<OverviewTopTableItem> fileCountComparator =
            Comparator.comparingLong(OverviewTopTableItem::getFileCount);
        top10Tables = getTopTables(isAsc, fileCountComparator, limit);
        break;
      case "healthScore":
        // If the table health score of the table has not been calculated,
        // it should be sorted at the end of the list
        Comparator<OverviewTopTableItem> healthScoreComparator =
            Comparator.comparingLong(
                item ->
                    item.getHealthScore() < 0
                        ? (isAsc ? Long.MAX_VALUE : Long.MIN_VALUE)
                        : item.getTableSize());
        top10Tables = getTopTables(isAsc, healthScoreComparator, limit);
        break;
      default:
        throw new IllegalArgumentException("Invalid orderBy: " + orderBy);
    }
    ctx.json(OkResponse.of(top10Tables));
  }

  private List<OverviewTopTableItem> getTopTables(
      boolean asc, Comparator<OverviewTopTableItem> comparator, int limit) {
    return overviewCache.getAllTopTableItem().stream()
        .sorted(
            asc
                ? comparator.thenComparing(OverviewTopTableItem::getTableName)
                : comparator.reversed().thenComparing(OverviewTopTableItem::getTableName))
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
    List<ImmutableMap<String, ? extends Serializable>> optimizingStatusList =
        optimizingStatus.entrySet().stream()
            .map(status -> ImmutableMap.of("name", status.getKey(), "value", status.getValue()))
            .collect(Collectors.toList());
    ctx.json(OkResponse.of(optimizingStatusList));
  }
}
