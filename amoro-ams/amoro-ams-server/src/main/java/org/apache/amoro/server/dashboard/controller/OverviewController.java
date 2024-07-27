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
import org.apache.amoro.api.ServerTableIdentifier;
import org.apache.amoro.server.DefaultOptimizingService;
import org.apache.amoro.server.catalog.ServerCatalog;
import org.apache.amoro.server.dashboard.ServerTableDescriptor;
import org.apache.amoro.server.dashboard.model.FilesStatistics;
import org.apache.amoro.server.dashboard.model.OverviewBaseData;
import org.apache.amoro.server.dashboard.model.OverviewSummary;
import org.apache.amoro.server.dashboard.model.OverviewTableOperation;
import org.apache.amoro.server.dashboard.model.OverviewUnhealthTable;
import org.apache.amoro.server.dashboard.model.TableStatistics;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.server.dashboard.response.PageResult;
import org.apache.amoro.server.dashboard.utils.TableStatCollector;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.amoro.server.table.TableMetadata;
import org.apache.amoro.server.table.TableRuntime;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.shade.guava32.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.amoro.table.MixedTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/** The controller that handles overview page requests. */
public class OverviewController {

  private static final Logger log = LoggerFactory.getLogger(OverviewController.class);
  private final TableService tableService;
  private final DefaultOptimizingService optimizerManager;
  private final ServerTableDescriptor tableDescriptor;

  private final ScheduledExecutorService overviewUpdaterScheduler =
      Executors.newSingleThreadScheduledExecutor(
          new ThreadFactoryBuilder()
              .setNameFormat("overview-updater-scheduler-%d")
              .setDaemon(true)
              .build());

  public OverviewController(
      TableService tableService,
      DefaultOptimizingService optimizerManager,
      ServerTableDescriptor tableDescriptor) {
    this.tableService = tableService;
    this.optimizerManager = optimizerManager;
    this.tableDescriptor = tableDescriptor;

    overviewUpdaterScheduler.scheduleAtFixedRate(
        this::overviewUpdate, 0, 3 * 60 * 1000L, TimeUnit.MILLISECONDS);
  }

  private void overviewUpdate() {
    // TODO cache overview page data
  }

  public void getSummary(Context ctx) {
    long sumTableSizeInBytes = 0;
    long sumMemorySizeInMb = 0;
    int totalCpu = 0;
    int catalogCount = tableService.listCatalogMetas().size();

    // table info
    List<TableMetadata> tableMetadataList = tableService.listTableMetas();
    for (TableMetadata tableMetadata : tableMetadataList) {
      ServerCatalog serverCatalog =
          tableService.getServerCatalog(tableMetadata.getTableIdentifier().getCatalog());
      sumTableSizeInBytes +=
          getFilesStatistics(tableMetadata.getTableIdentifier(), serverCatalog).getTotalSize();
    }

    // resource info
    List<OptimizerInstance> optimizers = optimizerManager.listOptimizers();
    for (OptimizerInstance optimizer : optimizers) {
      sumMemorySizeInMb += optimizer.getMemoryMb();
      totalCpu += optimizer.getThreadCount();
    }

    OverviewSummary overviewSummary =
        new OverviewSummary(
            catalogCount,
            tableMetadataList.size(),
            sumTableSizeInBytes,
            totalCpu,
            sumMemorySizeInMb);
    ctx.json(OkResponse.of(overviewSummary));
  }

  private static FilesStatistics getFilesStatistics(
      ServerTableIdentifier tableIdentifier, ServerCatalog catalog) {
    AmoroTable<?> amoroTable =
        catalog.loadTable(tableIdentifier.getDatabase(), tableIdentifier.getTableName());
    MixedTable table = (MixedTable) amoroTable.originalTable();
    TableStatistics tableBaseInfo;
    if (table.isUnkeyedTable()) {
      tableBaseInfo = new TableStatistics();
      TableStatCollector.fillTableStatistics(tableBaseInfo, table.asUnkeyedTable(), table);
    } else {
      tableBaseInfo = TableStatCollector.collectBaseTableInfo(table.asKeyedTable());
    }
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
    List<OverviewBaseData> optimizingStatusList =
        tableService.listManagedTables().stream()
            .map(
                tableIdentifier -> {
                  TableRuntime runtime = tableService.getRuntime(tableIdentifier);
                  if (runtime == null) {
                    return null;
                  }
                  return runtime.getOptimizingStatus();
                })
            .filter(status -> status != null)
            .collect(Collectors.groupingBy(status -> status.name(), Collectors.counting()))
            .entrySet()
            .stream()
            .map(status -> new OverviewBaseData(status.getKey(), status.getValue()))
            .collect(Collectors.toList());
    ctx.json(OkResponse.of(optimizingStatusList));
  }

  public void getUnhealthTables(Context ctx) {
    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);
    int offset = (page - 1) * pageSize;

    List<OverviewUnhealthTable> unhealthTableList = new ArrayList<>();
    List<TableMetadata> tableMetadataList = tableService.listTableMetas();
    for (TableMetadata tableMetadata : tableMetadataList) {
      String catalog = tableMetadata.getTableIdentifier().getCatalog();
      FilesStatistics filesStatistics =
          getFilesStatistics(
              tableMetadata.getTableIdentifier(), tableService.getServerCatalog(catalog));
      unhealthTableList.add(
          // TODO compute health score
          new OverviewUnhealthTable(tableMetadata.getTableIdentifier(), 90, filesStatistics));
    }
    unhealthTableList.sort(Comparator.comparing(OverviewUnhealthTable::getHealthScore));

    PageResult<OverviewUnhealthTable> amsPageResult =
        PageResult.of(unhealthTableList, offset, pageSize);
    ctx.json(OkResponse.of(amsPageResult));
  }

  public void getLatestOperations(Context ctx) {
    List<OverviewTableOperation> latest10Operations =
        tableService.listTableMetas().stream()
            .flatMap(
                tableMetadata ->
                    tableDescriptor
                        .getTableOperations(tableMetadata.getTableIdentifier().getIdentifier())
                        .stream()
                        .map(
                            ddl ->
                                new OverviewTableOperation(
                                    tableMetadata.getTableIdentifier(), ddl)))
            .sorted(Comparator.comparing(OverviewTableOperation::getTs).reversed())
            .limit(10)
            .collect(Collectors.toList());
    ctx.json(OkResponse.of(latest10Operations));
  }
}
