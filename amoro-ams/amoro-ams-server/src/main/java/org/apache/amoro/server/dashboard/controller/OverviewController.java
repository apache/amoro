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
import org.apache.amoro.server.catalog.ServerCatalog;
import org.apache.amoro.server.dashboard.ServerTableDescriptor;
import org.apache.amoro.server.dashboard.model.ServerStatistics;
import org.apache.amoro.server.dashboard.model.TableStatistics;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.server.dashboard.utils.AmsUtil;
import org.apache.amoro.server.dashboard.utils.TableStatCollector;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.table.MixedTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/** The controller that handles overview page requests. */
public class OverviewController {

  private static final Logger log = LoggerFactory.getLogger(OverviewController.class);
  private final TableService tableService;
  private final ServerTableDescriptor tableDescriptor;

  public OverviewController(TableService tableService, ServerTableDescriptor tableDescriptor) {
    this.tableService = tableService;
    this.tableDescriptor = tableDescriptor;
  }

  public void getServerStat(Context ctx) {
    List<ServerTableIdentifier> tables = tableService.listManagedTables();

    List<CatalogMeta> catalogMetaList = tableService.listCatalogMetas();
    int totalTableCnt = 0;
    long sumTableSizeInBytes = 0;
    for (CatalogMeta catalogMeta : catalogMetaList) {
      ServerCatalog catalog = tableService.getServerCatalog(catalogMeta.getCatalogName());
      catalog.listDatabases();
      List<TableIDWithFormat> listTables = catalog.listTables();
      // table count
      totalTableCnt += listTables.size();

      // get sum of tables sizes
      for (TableIDWithFormat tableIDWithFormat : listTables) {
        sumTableSizeInBytes += getTotalTableSize(tableIDWithFormat, catalog);
      }
    }

    ServerStatistics serverStatistics =
        new ServerStatistics(
            catalogMetaList.size(), totalTableCnt, AmsUtil.byteToXB(sumTableSizeInBytes), 96, 256);
    ctx.json(OkResponse.of(serverStatistics));
  }

  private static long getTotalTableSize(
      TableIDWithFormat tableIDWithFormat, ServerCatalog catalog) {
    AmoroTable<?> amoroTable =
        catalog.loadTable(tableIDWithFormat.database(), tableIDWithFormat.table());
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
}
