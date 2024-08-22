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
import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableIDWithFormat;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.server.DefaultOptimizingService;
import org.apache.amoro.server.catalog.ServerCatalog;
import org.apache.amoro.server.dashboard.OverviewCache;
import org.apache.amoro.server.dashboard.ServerTableDescriptor;
import org.apache.amoro.server.dashboard.model.FilesStatistics;
import org.apache.amoro.server.dashboard.model.OverviewBaseData;
import org.apache.amoro.server.dashboard.model.OverviewResourceUsage;
import org.apache.amoro.server.dashboard.model.OverviewSummary;
import org.apache.amoro.server.dashboard.model.TableStatistics;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.server.dashboard.utils.TableStatCollector;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** The controller that handles overview page requests. */
public class OverviewController {

  private static final Logger log = LoggerFactory.getLogger(OverviewController.class);
  private final TableService tableService;
  private final DefaultOptimizingService optimizerManager;
  private final ServerTableDescriptor tableDescriptor;
  private OverviewCache overviewCache;

  public OverviewController(
      TableService tableService,
      DefaultOptimizingService optimizerManager,
      ServerTableDescriptor tableDescriptor) {
    this.tableService = tableService;
    this.optimizerManager = optimizerManager;
    this.tableDescriptor = tableDescriptor;
    this.overviewCache = OverviewCache.getInstance();
  }

  public void getResourceUsageHistory(Context ctx) {
    String startTime = ctx.queryParam("startTime");
    List<OverviewResourceUsage> resourceUsageHistory =
        overviewCache.getResourceUsageHistory(getStartTime(startTime));
    ctx.json(OkResponse.of(resourceUsageHistory));
  }

  public void getDataSizeHistory(Context ctx) {
    String startTime = ctx.queryParam("startTime");
    // TODO
    getStartTime(startTime);
    ctx.json(OkResponse.of(Lists.newArrayList()));
  }

  public void getTop10Tables(Context ctx) {
    // TODO
    ctx.json(OkResponse.of(Lists.newArrayList()));
  }

  private static long getStartTime(String startTime) {
    return StringUtils.isNumeric(startTime)
        ? Long.parseLong(startTime)
        : System.currentTimeMillis() - Duration.ofDays(1).toMillis();
  }

  public void getSummary(Context ctx) {
    long sumTableSizeInBytes = 0;
    List<CatalogMeta> catalogMetas = tableService.listCatalogMetas();
    int catalogCount = catalogMetas.size();
    int tableCount = 0;

    // table info
    for (CatalogMeta catalogMeta : catalogMetas) {
      ServerCatalog serverCatalog = tableService.getServerCatalog(catalogMeta.getCatalogName());
      List<TableIDWithFormat> tableIDWithFormats = serverCatalog.listTables();
      tableCount += tableIDWithFormats.size();
      for (TableIDWithFormat tableIDWithFormat : tableIDWithFormats) {
        sumTableSizeInBytes += getFilesStatistics(tableIDWithFormat, serverCatalog).getTotalSize();
      }
    }

    // resource info
    int totalCpu = overviewCache.getTotalCpu();
    long totalMemory = overviewCache.getTotalMemory();

    OverviewSummary overviewSummary =
        new OverviewSummary(catalogCount, tableCount, sumTableSizeInBytes, totalCpu, totalMemory);
    ctx.json(OkResponse.of(overviewSummary));
  }

  private static FilesStatistics getFilesStatistics(
      TableIDWithFormat tableIDWithFormat, ServerCatalog catalog) {
    AmoroTable<?> amoroTable =
        catalog.loadTable(tableIDWithFormat.database(), tableIDWithFormat.table());
    MixedTable table = (MixedTable) amoroTable.originalTable();
    UnkeyedTable unkeyedTable =
        table.isKeyedTable() ? table.asKeyedTable().baseTable() : table.asUnkeyedTable();
    TableStatistics tableBaseInfo = new TableStatistics();
    TableStatCollector.fillTableStatistics(tableBaseInfo, unkeyedTable, table);
    return tableBaseInfo.getTotalFilesStat();
  }

  public void getTableFormat(Context ctx) {
    List<CatalogMeta> catalogMetas = tableService.listCatalogMetas();
    List<TableIDWithFormat> allTables = Lists.newArrayList();
    for (CatalogMeta catalogMeta : catalogMetas) {
      ServerCatalog serverCatalog = tableService.getServerCatalog(catalogMeta.getCatalogName());
      List<TableIDWithFormat> tableIDWithFormats = serverCatalog.listTables();
      allTables.addAll(tableIDWithFormats);
    }

    List<OverviewBaseData> tableFormats =
        allTables.stream()
            .map(TableIDWithFormat::getTableFormat)
            .collect(
                Collectors.groupingBy(tableFormat -> tableFormat.name(), Collectors.counting()))
            .entrySet()
            .stream()
            .map(format -> new OverviewBaseData(format.getKey(), format.getValue()))
            .collect(Collectors.toList());

    ctx.json(OkResponse.of(tableFormats));
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
