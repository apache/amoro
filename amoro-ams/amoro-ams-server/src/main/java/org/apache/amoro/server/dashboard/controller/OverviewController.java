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
import org.apache.amoro.api.ServerTableIdentifier;
import org.apache.amoro.server.DefaultOptimizingService;
import org.apache.amoro.server.catalog.ServerCatalog;
import org.apache.amoro.server.dashboard.OverviewCache;
import org.apache.amoro.server.dashboard.ServerTableDescriptor;
import org.apache.amoro.server.dashboard.model.DDLInfo;
import org.apache.amoro.server.dashboard.model.FilesStatistics;
import org.apache.amoro.server.dashboard.model.OverviewBaseData;
import org.apache.amoro.server.dashboard.model.OverviewSummary;
import org.apache.amoro.server.dashboard.model.OverviewTableOperation;
import org.apache.amoro.server.dashboard.model.OverviewUnhealthTable;
import org.apache.amoro.server.dashboard.model.TableStatistics;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.server.dashboard.response.PageResult;
import org.apache.amoro.server.dashboard.utils.TableStatCollector;
import org.apache.amoro.server.table.TableMetadata;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.UnkeyedTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
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
    int threadCount = overviewCache.getThreadCount();
    long totalMemory = overviewCache.getTotalMemory();

    OverviewSummary overviewSummary =
        new OverviewSummary(
            catalogCount, tableCount, sumTableSizeInBytes, threadCount, totalMemory);
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
    List<OverviewBaseData> tableFormats =
        tableService.listTableMetas().stream()
            .map(TableMetadata::getFormat)
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

  public void getUnhealthTables(Context ctx) {
    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);
    int offset = (page - 1) * pageSize;

    List<OverviewUnhealthTable> unhealthTableList = new ArrayList<>();
    List<CatalogMeta> catalogMetas = tableService.listCatalogMetas();
    for (CatalogMeta catalogMeta : catalogMetas) {
      ServerCatalog serverCatalog = tableService.getServerCatalog(catalogMeta.getCatalogName());
      List<TableIDWithFormat> tableIDWithFormats = serverCatalog.listTables();
      for (TableIDWithFormat tableIDWithFormat : tableIDWithFormats) {
        FilesStatistics filesStatistics = getFilesStatistics(tableIDWithFormat, serverCatalog);
        unhealthTableList.add(
            // TODO compute health score
            new OverviewUnhealthTable(
                ServerTableIdentifier.of(
                    tableIDWithFormat.getIdentifier().buildTableIdentifier(),
                    tableIDWithFormat.getTableFormat()),
                90,
                filesStatistics));
      }
    }
    unhealthTableList.sort(Comparator.comparing(OverviewUnhealthTable::getHealthScore));

    PageResult<OverviewUnhealthTable> amsPageResult =
        PageResult.of(unhealthTableList, offset, pageSize);
    ctx.json(OkResponse.of(amsPageResult));
  }

  public void getLatestOperations(Context ctx) {
    List<CatalogMeta> catalogMetas = tableService.listCatalogMetas();
    List<OverviewTableOperation> allTableOperationList = Lists.newArrayList();

    for (CatalogMeta catalogMeta : catalogMetas) {
      ServerCatalog serverCatalog = tableService.getServerCatalog(catalogMeta.getCatalogName());
      List<TableIDWithFormat> tableIDWithFormats = serverCatalog.listTables();
      for (TableIDWithFormat tableIDWithFormat : tableIDWithFormats) {
        ServerTableIdentifier serverTableIdentifier =
            ServerTableIdentifier.of(
                tableIDWithFormat.getIdentifier().buildTableIdentifier(),
                tableIDWithFormat.getTableFormat());
        List<DDLInfo> tableOperations =
            tableDescriptor.getTableOperations(serverTableIdentifier.getIdentifier());
        List<OverviewTableOperation> overviewTableOperationList =
            tableOperations.stream()
                .map(ddl -> new OverviewTableOperation(serverTableIdentifier, ddl))
                .collect(Collectors.toList());
        allTableOperationList.addAll(overviewTableOperationList);
      }
    }

    List<OverviewTableOperation> latest10Operations =
        allTableOperationList.stream()
            .sorted(Comparator.comparing(OverviewTableOperation::getTs).reversed())
            .limit(10)
            .collect(Collectors.toList());
    ctx.json(OkResponse.of(latest10Operations));
  }
}
