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

package com.netease.arctic.server.dashboard.controller;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.server.dashboard.ServerTableDescriptor;
import com.netease.arctic.server.dashboard.ServerTableProperties;
import com.netease.arctic.server.dashboard.model.AMSColumnInfo;
import com.netease.arctic.server.dashboard.model.AMSDataFileInfo;
import com.netease.arctic.server.dashboard.model.AMSPartitionField;
import com.netease.arctic.server.dashboard.model.AMSTransactionsOfTable;
import com.netease.arctic.server.dashboard.model.BaseMajorCompactRecord;
import com.netease.arctic.server.dashboard.model.DDLInfo;
import com.netease.arctic.server.dashboard.model.FilesStatistics;
import com.netease.arctic.server.dashboard.model.ServerTableMeta;
import com.netease.arctic.server.dashboard.model.TableBasicInfo;
import com.netease.arctic.server.dashboard.model.TableMeta;
import com.netease.arctic.server.dashboard.model.TableOperation;
import com.netease.arctic.server.dashboard.model.TableStatistics;
import com.netease.arctic.server.dashboard.model.TransactionsOfTable;
import com.netease.arctic.server.dashboard.response.ErrorResponse;
import com.netease.arctic.server.dashboard.response.OkResponse;
import com.netease.arctic.server.dashboard.response.PageResult;
import com.netease.arctic.server.dashboard.utils.AmsUtils;
import com.netease.arctic.server.dashboard.utils.CommonUtils;
import com.netease.arctic.server.dashboard.utils.TableStatCollector;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableService;
import com.netease.arctic.hive.HiveTableProperties;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Table moudle controller.
 */
public class TableController extends RestBaseController {
  private static final Logger LOG = LoggerFactory.getLogger(TableController.class);

  private TableService tableService;
  private ServerTableDescriptor tableDescriptor;

  public TableController(TableService tableService, ServerTableDescriptor tableDescriptor) {
    this.tableService = tableService;
    this.tableDescriptor = tableDescriptor;
  }

  /**
   * get table detail.
   */
  public void getTableDetail(Context ctx) {

    String catalog = ctx.pathParam("catalog");
    String database = ctx.pathParam("db");
    String tableMame = ctx.pathParam("table");

    Preconditions.checkArgument(catalog != null && database != null && tableMame != null,
        "catalog.dabatase.tableName can not be null in any element");
    // get table from catalog
    if (!tableService.catalogExist(catalog)) {
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST, "invalid catalog!", null));
      return;
    }

    try {
      ArcticTable table = tableService.loadTable(ServerTableIdentifier.of(catalog, database, tableMame));
      // set basic info
      TableBasicInfo tableBasicInfo = getTableBasicInfo(table);
      ServerTableMeta serverTableMeta = getServerTableMeta(table);
      long tableSize = 0;
      long tableFileCnt = 0;
      Map<String, Object> baseMetrics = Maps.newHashMap();
      FilesStatistics baseFilesStatistics = tableBasicInfo.getBaseStatistics().getTotalFilesStat();
      Map<String, String> baseSummary = tableBasicInfo.getBaseStatistics().getSummary();
      baseMetrics.put("lastCommitTime", AmsUtils.longOrNull(baseSummary.get("visibleTime")));
      baseMetrics.put("totalSize", AmsUtils.byteToXB(baseFilesStatistics.getTotalSize()));
      baseMetrics.put("fileCount", baseFilesStatistics.getFileCnt());
      baseMetrics.put("averageFileSize", AmsUtils.byteToXB(baseFilesStatistics.getAverageSize()));
      baseMetrics.put("baseWatermark", AmsUtils.longOrNull(serverTableMeta.getBaseWatermark()));
      tableSize += baseFilesStatistics.getTotalSize();
      tableFileCnt += baseFilesStatistics.getFileCnt();
      serverTableMeta.setBaseMetrics(baseMetrics);

      Map<String, Object> changeMetrics = Maps.newHashMap();
      if (tableBasicInfo.getChangeStatistics() != null) {
        FilesStatistics changeFilesStatistics = tableBasicInfo.getChangeStatistics().getTotalFilesStat();
        Map<String, String> changeSummary = tableBasicInfo.getChangeStatistics().getSummary();
        changeMetrics.put("lastCommitTime", AmsUtils.longOrNull(changeSummary.get("visibleTime")));
        changeMetrics.put("totalSize", AmsUtils.byteToXB(changeFilesStatistics.getTotalSize()));
        changeMetrics.put("fileCount", changeFilesStatistics.getFileCnt());
        changeMetrics.put("averageFileSize", AmsUtils.byteToXB(changeFilesStatistics.getAverageSize()));
        changeMetrics.put("tableWatermark", AmsUtils.longOrNull(serverTableMeta.getTableWatermark()));
        tableSize += changeFilesStatistics.getTotalSize();
        tableFileCnt += changeFilesStatistics.getFileCnt();
      } else {
        changeMetrics.put("lastCommitTime", null);
        changeMetrics.put("totalSize", null);
        changeMetrics.put("fileCount", null);
        changeMetrics.put("averageFileSize", null);
        changeMetrics.put("tableWatermark", null);
      }
      serverTableMeta.setChangeMetrics(changeMetrics);
      Set<TableFormat> tableFormats =
          com.netease.arctic.utils.CatalogUtil.tableFormats(tableService.getCatalogMeta(catalog));
      Preconditions.checkArgument(tableFormats.size() == 1, "Catalog support only one table format now.");
      TableFormat tableFormat = tableFormats.iterator().next();
      Map<String, Object> tableSummary = new HashMap<>();
      tableSummary.put("size", AmsUtils.byteToXB(tableSize));
      tableSummary.put("file", tableFileCnt);
      tableSummary.put("averageFile", AmsUtils.byteToXB(tableFileCnt == 0 ? 0 : tableSize / tableFileCnt));
      tableSummary.put("tableFormat", AmsUtils.formatString(tableFormat.name()));
      serverTableMeta.setTableSummary(tableSummary);
      ctx.json(OkResponse.of(serverTableMeta));
    } catch (Throwable t) {
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST, t.getMessage(), ""));
    }
  }

  /**
   * get hive table detail.
   */
  public void getHiveTableDetail(Context ctx) {
    // String catalog = ctx.pathParam("catalog");
    // String db = ctx.pathParam("db");
    // String table = ctx.pathParam("table");
    //
    // // get table from catalog
    // String thriftHost = serviceConfig.getString(ArcticManagementConf.THRIFT_BIND_HOST);
    // Integer thriftPort = serviceConfig.getInteger(ArcticManagementConf.THRIFT_BIND_PORT);
    // ArcticHiveCatalog arcticHiveCatalog
    //     = (ArcticHiveCatalog) CatalogUtil.getArcticCatalog(thriftHost, thriftPort, catalog);
    //
    // TableIdentifier tableIdentifier = TableIdentifier.of(catalog, db, table);
    // HiveTableInfo hiveTableInfo;
    // try {
    //   Table hiveTable = HiveTableUtil.loadHmsTable(arcticHiveCatalog.getHMSClient(), tableIdentifier);
    //   List<AMSColumnInfo> schema =
    //       AmsUtils.transforHiveSchemaToAMSColumnInfos(hiveTable.getSd().getCols());
    //   List<AMSColumnInfo> partitionColumnInfos =
    //       AmsUtils.transforHiveSchemaToAMSColumnInfos(hiveTable.getPartitionKeys());
    //   hiveTableInfo = new HiveTableInfo(tableIdentifier, TableMeta.TableType.HIVE, schema, partitionColumnInfos,
    //       new HashMap<>(), hiveTable.getCreateTime());
    // } catch (Exception e) {
    //   LOG.error("Failed to get hive table info", e);
    //   ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST, "Failed to get hive table info", ""));
    //   return;
    // }
    // ctx.json(OkResponse.of(hiveTableInfo));
    ctx.json(OkResponse.ok());
  }

  /**
   * upgrade hive table to arctic.
   */
  public void upgradeHiveTable(Context ctx) {
    // String catalog = ctx.pathParam("catalog");
    // String db = ctx.pathParam("db");
    // String table = ctx.pathParam("table");
    // UpgradeHiveMeta upgradeHiveMeta = ctx.bodyAsClass(UpgradeHiveMeta.class);
    //
    // ServerCatalog serverCatalog = CatalogBuilder.buildServerCatalog(tableService.getCatalogMeta(catalog));
    //
    // String thriftHost = serviceConfig.getString(ArcticManagementConf.THRIFT_BIND_HOST);
    // Integer thriftPort = serviceConfig.getInteger(ArcticManagementConf.THRIFT_BIND_PORT);
    // ArcticHiveCatalog arcticHiveCatalog
    //     = (ArcticHiveCatalog) CatalogUtil.getArcticCatalog(thriftHost, thriftPort, catalog);
    // try {
    //   UpgradeHiveTableUtil.upgradeHiveTable(arcticHiveCatalog, TableIdentifier.of(catalog, db, table),
    //       upgradeHiveMeta.getPkList()
    //           .stream()
    //           .map(UpgradeHiveMeta.PrimaryKeyField::getFieldName)
    //           .collect(Collectors.toList()), upgradeHiveMeta.getProperties());
    //   ctx.json(OkResponse.ok());
    // } catch (Exception e) {
    //   LOG.error("upgrade hive table error:", e);
    //   ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST, "Failed to upgrade hive table", ""));
    // }
    ctx.json(OkResponse.ok());
  }

  public void getUpgradeStatus(Context ctx) {
    // String catalog = ctx.pathParam("catalog");
    // String db = ctx.pathParam("db");
    // String table = ctx.pathParam("table");
    // ctx.json(OkResponse.of(adaptHiveService.getUpgradeRunningInfo(TableIdentifier.of(catalog, db, table))));
    ctx.json(OkResponse.ok());
  }

  /**
   * upgrade hive table to arctic.
   */
  public void getUpgradeHiveTableProperties(Context ctx) throws IllegalAccessException {
    Map<String, String> keyValues = new TreeMap<>();
    Map<String, String> tableProperties =
        AmsUtils.getNotDeprecatedAndNotInternalStaticFields(TableProperties.class);
    tableProperties.keySet().stream()
        .filter(key -> !key.endsWith("_DEFAULT"))
        .forEach(
            key -> keyValues
                .put(tableProperties.get(key), tableProperties.get(key + "_DEFAULT")));
    ServerTableProperties.HIDDEN_EXPOSED.forEach(keyValues::remove);
    Map<String, String> hiveProperties =
        AmsUtils.getNotDeprecatedAndNotInternalStaticFields(HiveTableProperties.class);

    hiveProperties.keySet().stream()
        .filter(key -> HiveTableProperties.EXPOSED.contains(hiveProperties.get(key)))
        .filter(key -> !key.endsWith("_DEFAULT"))
        .forEach(
            key -> keyValues
                .put(hiveProperties.get(key), hiveProperties.get(key + "_DEFAULT")));
    ctx.json(OkResponse.of(keyValues));
  }

  /**
   * get optimize info.
   */
  public void getOptimizeInfo(Context ctx) {

    String catalog = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String table = ctx.pathParam("table");
    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);

    int offset = (page - 1) * pageSize;
    int limit = pageSize;
    checkOffsetAndLimit(offset, limit);

    if (!tableService.tableExist(new com.netease.arctic.ams.api.TableIdentifier(catalog, db, table))) {
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST, "no such table", ""));
    }
    List<BaseMajorCompactRecord> all = tableDescriptor.getOptimizeInfo(catalog, db, table);
    List<BaseMajorCompactRecord> result = all.stream()
        .skip(offset)
        .limit(limit)
        .collect(Collectors.toList());
    ctx.json(OkResponse.of(PageResult.of(result, all.size())));
  }

  /**
   * get list of transactions.
   */
  public void getTableTransactions(Context ctx) {
    String catalogName = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String tableName = ctx.pathParam("table");
    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);

    try {
      List<TransactionsOfTable> transactionsOfTables =
          tableDescriptor.getTransactions(ServerTableIdentifier.of(catalogName, db, tableName));
      int offset = (page - 1) * pageSize;
      PageResult<TransactionsOfTable, AMSTransactionsOfTable> pageResult = PageResult.of(transactionsOfTables,
          offset, pageSize, AmsUtils::toTransactionsOfTable);
      ctx.json(OkResponse.of(pageResult));
    } catch (Exception e) {
      LOG.error("Failed to list transactions ", e);
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST, "Failed to list transactions", ""));
    }
  }

  /**
   * get detail of transaction.
   */
  public void getTransactionDetail(Context ctx) {
    String catalogName = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String tableName = ctx.pathParam("table");
    String transactionId = ctx.pathParam("transactionId");
    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);

    try {
      List<AMSDataFileInfo> result = tableDescriptor.getTransactionDetail(ServerTableIdentifier.of(catalogName, db,
          tableName), Long.parseLong(transactionId));
      int offset = (page - 1) * pageSize;
      PageResult<AMSDataFileInfo, AMSDataFileInfo> amsPageResult = PageResult.of(result,
          offset, pageSize);
      ctx.json(OkResponse.of(amsPageResult));
    } catch (Exception e) {
      LOG.error("Failed to get transactions detail", e);
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST, "Failed to get transactions detail", ""));
    }
  }

  /**
   * get partition list.
   */
  public void getTablePartitions(Context ctx) {
  }

  /**
   * get file list of some partition.
   */
  public void getPartitionFileListInfo(Context ctx) {
  }

  /* get  operations of some table*/
  public void getTableOperations(Context ctx) {
    String catalogName = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String tableName = ctx.pathParam("table");

    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);
    int offset = (page - 1) * pageSize;

    List<DDLInfo> ddlInfos = tableDescriptor.getTableOperations(ServerTableIdentifier.of(catalogName, db,
        tableName));
    PageResult<DDLInfo, TableOperation> amsPageResult = PageResult.of(ddlInfos,
        offset, pageSize, TableOperation::buildFromDDLInfo);
    ctx.json(OkResponse.of(amsPageResult));
  }

  /**
   * get table list of catalog.db.
   */
  public void getTableList(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String keywords = ctx.queryParam("keywords");
    List<ServerTableIdentifier> tableIdentifiers = tableService.listTables(catalog, db);
    LinkedHashSet<TableMeta> tempTables = new LinkedHashSet<>();
    List<TableMeta> tables = new ArrayList<>();
    for (ServerTableIdentifier tableIdentifier : tableIdentifiers) {
      tables.add(new TableMeta(tableIdentifier.getTableName(), TableMeta.TableType.ICEBERG.toString()));
    }
    // else if (CatalogUtil.isHiveCatalog(catalog)) {
    //   ArcticHiveCatalog arcticHiveCatalog = (ArcticHiveCatalog) ac;
    //   List<String> hiveTables = HiveTableUtil.getAllHiveTables(arcticHiveCatalog.getHMSClient(), db);
    //   for (String hiveTable : hiveTables) {
    //     tempTables.add(new TableMeta(hiveTable, TableMeta.TableType.HIVE.toString()));
    //   }
    //   for (com.netease.arctic.ams.api.TableIdentifier tableIdentifier : tableIdentifiers) {
    //     TableMeta tableMeta = new TableMeta(tableIdentifier.getTableName(), TableMeta.TableType.ARCTIC.toString());
    //     if (tempTables.contains(tableMeta)) {
    //       tables.add(tableMeta);
    //       tempTables.remove(tableMeta);
    //     }
    //   }
    //   tables.addAll(tempTables);
    // }
    ctx.json(OkResponse.of(tables.stream().filter(t -> StringUtils.isEmpty(keywords) ||
        t.getName().contains(keywords)).collect(Collectors.toList())));
  }

  /**
   * get databases of some catalog.
   */
  public void getDatabaseList(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String keywords = ctx.queryParam("keywords");

    List<String> dbList = tableService.listDatabases(catalog).stream()
        .filter(item -> StringUtils.isEmpty(keywords) || item.contains(keywords))
        .collect(Collectors.toList());
    ctx.json(OkResponse.of(dbList));
  }

  /**
   * list catalogs.
   */
  public void getCatalogs(Context ctx) {
    List<CatalogMeta> catalogs = tableService.listCatalogMetas();
    ctx.json(OkResponse.of(catalogs));
  }

  /**
   * get single page query token
   */
  public void getTableDetailTabToken(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String table = ctx.pathParam("table");

    String signCal = CommonUtils.generateTablePageToken(catalog, db, table);
    ctx.json(OkResponse.of(signCal));
  }

  private TableBasicInfo getTableBasicInfo(ArcticTable table) {
    try {
      TableBasicInfo tableBasicInfo = new TableBasicInfo();
      tableBasicInfo.setTableIdentifier(table.id());
      TableStatistics changeInfo = null;
      TableStatistics baseInfo;

      if (table.isUnkeyedTable()) {
        UnkeyedTable unkeyedTable = table.asUnkeyedTable();
        baseInfo = new TableStatistics();
        TableStatCollector.fillTableStatistics(baseInfo, unkeyedTable, table, "TABLE");
      } else if (table.isKeyedTable()) {
        KeyedTable keyedTable = table.asKeyedTable();
        if (!PrimaryKeySpec.noPrimaryKey().equals(keyedTable.primaryKeySpec())) {
          changeInfo = TableStatCollector.collectChangeTableInfo(keyedTable);
        }
        baseInfo = TableStatCollector.collectBaseTableInfo(keyedTable);
      } else {
        throw new IllegalStateException("unknown type of table");
      }

      tableBasicInfo.setChangeStatistics(changeInfo);
      tableBasicInfo.setBaseStatistics(baseInfo);
      tableBasicInfo.setTableStatistics(TableStatCollector.union(changeInfo, baseInfo));

      long createTime
          = PropertyUtil.propertyAsLong(table.properties(), TableProperties.TABLE_CREATE_TIME,
          TableProperties.TABLE_CREATE_TIME_DEFAULT);
      if (createTime != TableProperties.TABLE_CREATE_TIME_DEFAULT) {
        if (tableBasicInfo.getTableStatistics() != null) {
          if (tableBasicInfo.getTableStatistics().getSummary() == null) {
            tableBasicInfo.getTableStatistics().setSummary(new HashMap<>());
          } else {
            LOG.warn("{} summary is null", table.id());
          }
          tableBasicInfo.getTableStatistics().getSummary()
              .put("createTime", String.valueOf(createTime));
        } else {
          LOG.warn("{} table statistics is null {}", table.id(), tableBasicInfo);
        }
      }
      return tableBasicInfo;
    } catch (Throwable t) {
      LOG.error("{} failed to build table basic info", table.id(), t);
      throw t;
    }
  }

  public ServerTableMeta getServerTableMeta(ArcticTable table) {
    ServerTableMeta serverTableMeta = new ServerTableMeta();
    serverTableMeta.setTableType(table.format().toString());
    serverTableMeta.setTableIdentifier(table.id());
    serverTableMeta.setBaseLocation(table.location());
    fillTableProperties(serverTableMeta, table.properties());
    serverTableMeta.setPartitionColumnList(table
        .spec()
        .fields()
        .stream()
        .map(item -> AMSPartitionField.buildFromPartitionSpec(table.spec().schema(), item))
        .collect(Collectors.toList()));
    serverTableMeta.setSchema(table
        .schema()
        .columns()
        .stream()
        .map(AMSColumnInfo::buildFromNestedField)
        .collect(Collectors.toList()));

    serverTableMeta.setFilter(null);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Table " + table.name() + " is keyedTable: {}", table instanceof KeyedTable);
    }
    if (table.isKeyedTable()) {
      KeyedTable kt = table.asKeyedTable();
      if (kt.primaryKeySpec() != null) {
        serverTableMeta.setPkList(kt
            .primaryKeySpec()
            .fields()
            .stream()
            .map(item -> AMSColumnInfo.buildFromPartitionSpec(table.spec().schema(), item))
            .collect(Collectors.toList()));
      }
    }
    if (serverTableMeta.getPkList() == null) {
      serverTableMeta.setPkList(new ArrayList<>());
    }
    return serverTableMeta;
  }

  private void fillTableProperties(ServerTableMeta serverTableMeta,
                                          Map<String, String> tableProperties) {
    Map<String, String> properties = com.google.common.collect.Maps.newHashMap(tableProperties);
    serverTableMeta.setTableWatermark(properties.remove(TableProperties.WATERMARK_TABLE));
    serverTableMeta.setBaseWatermark(properties.remove(TableProperties.WATERMARK_BASE_STORE));
    serverTableMeta.setCreateTime(PropertyUtil.propertyAsLong(properties, TableProperties.TABLE_CREATE_TIME,
        TableProperties.TABLE_CREATE_TIME_DEFAULT));
    properties.remove(TableProperties.TABLE_CREATE_TIME);

    TableProperties.READ_PROTECTED_PROPERTIES.forEach(properties::remove);
    serverTableMeta.setProperties(properties);
  }
}
