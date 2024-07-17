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
import org.apache.amoro.server.dashboard.model.OverviewBaseData;
import org.apache.amoro.server.dashboard.model.OverviewSummary;
import org.apache.amoro.server.dashboard.model.TableStatistics;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.server.dashboard.utils.TableStatCollector;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.amoro.server.table.TableMetadata;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.table.MixedTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The controller that handles overview page requests. */
public class OverviewController {

  private static final Logger log = LoggerFactory.getLogger(OverviewController.class);
  private final TableService tableService;
  private final DefaultOptimizingService optimizerManager;

  public OverviewController(TableService tableService, DefaultOptimizingService optimizerManager) {
    this.tableService = tableService;
    this.optimizerManager = optimizerManager;
  }

  public void getSummary(Context ctx) {
    long sumTableSizeInBytes = 0;
    long sumMemorySizeInBytes = 0;
    int totalCpu = 0;

    // table info
    List<TableMetadata> tableMetadata = tableService.listTableMetas();
    Map<String, ServerCatalog> serverCatalogMap = new HashMap<>();
    for (TableMetadata tableMeta : tableMetadata) {
      String catalog = tableMeta.getTableIdentifier().getCatalog();
      if (!serverCatalogMap.containsKey(catalog)) {
        serverCatalogMap.put(catalog, tableService.getServerCatalog(catalog));
      }
      ServerCatalog serverCatalog = serverCatalogMap.get(catalog);
      sumTableSizeInBytes += getTotalTableSize(tableMeta.getTableIdentifier(), serverCatalog);
    }

    // resource info
    List<OptimizerInstance> optimizers = optimizerManager.listOptimizers();
    for (OptimizerInstance optimizer : optimizers) {
      sumMemorySizeInBytes += mb2Bytes(optimizer.getMemoryMb());
      totalCpu += optimizer.getThreadCount();
    }

    OverviewSummary overviewSummary =
        new OverviewSummary(
            serverCatalogMap.size(),
            tableMetadata.size(),
            sumTableSizeInBytes,
            totalCpu,
            sumMemorySizeInBytes);
    ctx.json(OkResponse.of(overviewSummary));
  }

  private long mb2Bytes(long memoryMb) {
    return memoryMb * 1024 * 1024;
  }

  private static long getTotalTableSize(
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
    return tableBaseInfo.getTotalFilesStat().getTotalSize();
  }

  public void getTableFormat(Context ctx) {
    List<OverviewBaseData> tableFormats = new ArrayList<>();
    tableFormats.add(new OverviewBaseData(70, "Iceberg format"));
    tableFormats.add(new OverviewBaseData(20, "Mixed-Iceberg"));
    tableFormats.add(new OverviewBaseData(10, "Mixed-Hive format"));
    ctx.json(OkResponse.of(tableFormats));
  }

  public void getOptimizingStatus(Context ctx) {
    List<OverviewBaseData> optimizingStatusList = new ArrayList<>();
    optimizingStatusList.add(new OverviewBaseData(40, OptimizingStatus.FULL_OPTIMIZING.name()));
    optimizingStatusList.add(new OverviewBaseData(20, OptimizingStatus.MAJOR_OPTIMIZING.name()));
    optimizingStatusList.add(new OverviewBaseData(30, OptimizingStatus.MINOR_OPTIMIZING.name()));
    optimizingStatusList.add(new OverviewBaseData(10, OptimizingStatus.COMMITTING.name()));
    optimizingStatusList.add(new OverviewBaseData(2, OptimizingStatus.PLANNING.name()));
    optimizingStatusList.add(new OverviewBaseData(3, OptimizingStatus.PENDING.name()));
    optimizingStatusList.add(new OverviewBaseData(50, OptimizingStatus.IDLE.name()));
    ctx.json(OkResponse.of(optimizingStatusList));
  }
}
