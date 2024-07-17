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
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.amoro.server.table.TableMetadata;
import org.apache.amoro.server.table.TableRuntime;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.table.MixedTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    long sumMemorySizeInMb = 0;
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
      sumMemorySizeInMb += optimizer.getMemoryMb();
      totalCpu += optimizer.getThreadCount();
    }

    OverviewSummary overviewSummary =
        new OverviewSummary(
            serverCatalogMap.size(),
            tableMetadata.size(),
            sumTableSizeInBytes,
            totalCpu,
            sumMemorySizeInMb);
    ctx.json(OkResponse.of(overviewSummary));
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
    List<TableMetadata> tableMetadata = tableService.listTableMetas();
    List<OverviewBaseData> tableFormats =
        tableMetadata.stream()
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
    // optimizing status info
    List<TableRuntime> tableRuntimes = new ArrayList<>();
    List<ServerTableIdentifier> tables = tableService.listManagedTables();
    for (ServerTableIdentifier identifier : tables) {
      TableRuntime tableRuntime = tableService.getRuntime(identifier);
      if (tableRuntime == null) {
        continue;
      }
      tableRuntimes.add(tableRuntime);
    }

    // group by
    List<OverviewBaseData> optimizingStatusList =
        tableRuntimes.stream()
            .map(TableRuntime::getOptimizingStatus)
            .collect(Collectors.groupingBy(status -> status.name(), Collectors.counting()))
            .entrySet()
            .stream()
            .map(status -> new OverviewBaseData(status.getKey(), status.getValue()))
            .collect(Collectors.toList());
    ctx.json(OkResponse.of(optimizingStatusList));
  }
}
