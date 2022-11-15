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

package com.netease.arctic.ams.server.controller;

import com.netease.arctic.ams.api.Constants;
import com.netease.arctic.ams.api.DataFileInfo;
import com.netease.arctic.ams.api.MetaException;
import com.netease.arctic.ams.api.NoSuchObjectException;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.ams.server.ArcticMetaStore;
import com.netease.arctic.ams.server.config.ArcticMetaStoreConf;
import com.netease.arctic.ams.server.config.ServerTableProperties;
import com.netease.arctic.ams.server.controller.response.ErrorResponse;
import com.netease.arctic.ams.server.controller.response.OkResponse;
import com.netease.arctic.ams.server.controller.response.PageResult;
import com.netease.arctic.ams.server.exception.SignatureCheckException;
import com.netease.arctic.ams.server.model.AMSColumnInfo;
import com.netease.arctic.ams.server.model.AMSDataFileInfo;
import com.netease.arctic.ams.server.model.AMSTransactionsOfTable;
import com.netease.arctic.ams.server.model.BaseMajorCompactRecord;
import com.netease.arctic.ams.server.model.CatalogMeta;
import com.netease.arctic.ams.server.model.DDLInfo;
import com.netease.arctic.ams.server.model.FilesStatistics;
import com.netease.arctic.ams.server.model.HiveTableInfo;
import com.netease.arctic.ams.server.model.OptimizeHistory;
import com.netease.arctic.ams.server.model.PartitionBaseInfo;
import com.netease.arctic.ams.server.model.PartitionFileBaseInfo;
import com.netease.arctic.ams.server.model.ServerTableMeta;
import com.netease.arctic.ams.server.model.TableBasicInfo;
import com.netease.arctic.ams.server.model.TableMeta;
import com.netease.arctic.ams.server.model.TableOperation;
import com.netease.arctic.ams.server.model.TransactionsOfTable;
import com.netease.arctic.ams.server.model.UpgradeHiveMeta;
import com.netease.arctic.ams.server.optimize.IOptimizeService;
import com.netease.arctic.ams.server.service.ITableInfoService;
import com.netease.arctic.ams.server.service.MetaService;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.ams.server.service.impl.AdaptHiveService;
import com.netease.arctic.ams.server.service.impl.CatalogMetadataService;
import com.netease.arctic.ams.server.service.impl.DDLTracerService;
import com.netease.arctic.ams.server.service.impl.FileInfoCacheService;
import com.netease.arctic.ams.server.utils.AmsUtils;
import com.netease.arctic.ams.server.utils.CatalogUtil;
import com.netease.arctic.ams.server.utils.ParamSignatureCalculator;
import com.netease.arctic.ams.server.utils.Utils;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.hive.HiveTableProperties;
import com.netease.arctic.hive.catalog.ArcticHiveCatalog;
import com.netease.arctic.hive.utils.HiveTableUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableProperties;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import org.apache.arrow.util.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hive.metastore.api.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Table moudle controller.
 *
 */
public class TableController extends RestBaseController {
  private static final Logger LOG = LoggerFactory.getLogger(TableController.class);

  private static ITableInfoService tableInfoService = ServiceContainer.getTableInfoService();
  private static IOptimizeService optimizeService = ServiceContainer.getOptimizeService();
  private static FileInfoCacheService fileInfoCacheService = ServiceContainer.getFileInfoCacheService();
  private static CatalogMetadataService catalogMetadataService = ServiceContainer.getCatalogMetadataService();
  private static AdaptHiveService adaptHiveService = ServiceContainer.getAdaptHiveService();
  private static DDLTracerService ddlTracerService = ServiceContainer.getDdlTracerService();

  /**
   * get table detail.
   */
  public static void getTableDetail(Context ctx) {

    String catalog = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String table = ctx.pathParam("table");

    // get table from catalog
    String thriftHost = ArcticMetaStore.conf.getString(ArcticMetaStoreConf.THRIFT_BIND_HOST);
    Integer thriftPort = ArcticMetaStore.conf.getInteger(ArcticMetaStoreConf.THRIFT_BIND_PORT);
    ArcticCatalog ac = CatalogUtil.getArcticCatalog(thriftHost, thriftPort, catalog);
    if (ac == null) {
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST, "invalid catalog!", null));
      return;
    }

    TableBasicInfo tableBasicInfo = null;

    try {
      // set basic info
      tableBasicInfo = tableInfoService.getTableBasicInfo(
              TableIdentifier.of(catalog, db, table));
    } catch (MetaException | NoSuchObjectException e) {
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST, "", ""));
      return;
    }
    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    ServerTableMeta serverTableMeta = MetaService.getServerTableMeta(ac, TableIdentifier.of(catalog, db, table));
    Map baseMetrics = new HashMap();
    FilesStatistics baseFilesStatistics = tableBasicInfo.getBaseStatistics().getTotalFilesStat();
    Map<String, String> baseSummary = tableBasicInfo.getBaseStatistics().getSummary();
    baseMetrics.put("lastCommitTime", AmsUtils.longOrNull(baseSummary.get("visibleTime")));
    baseMetrics.put("size", AmsUtils.byteToXB(baseFilesStatistics.getTotalSize()));
    baseMetrics.put("file", baseFilesStatistics.getFileCnt());
    baseMetrics.put("averageFile", AmsUtils.byteToXB(baseFilesStatistics.getAverageSize()));
    Long baseMaxET = fileInfoCacheService
        .getWatermark(AmsUtils.toTableIdentifier(TableIdentifier.of(catalog, db, table)), Constants.INNER_TABLE_BASE);
    if (baseMaxET != null && baseMaxET != 0L) {
      baseMetrics.put("maxEventTime", sd.format(new Date(baseMaxET)));
    } else {
      baseMetrics.put("maxEventTime", null);
    }
    serverTableMeta.setBaseMetrics(baseMetrics);

    Map changeMetrics = new HashMap();
    if (tableBasicInfo.getChangeStatistics() != null) {
      FilesStatistics changeFilesStatistics = tableBasicInfo.getChangeStatistics().getTotalFilesStat();
      Map<String, String> changeSummary = tableBasicInfo.getChangeStatistics().getSummary();
      changeMetrics.put("lastCommitTime", AmsUtils.longOrNull(changeSummary.get("visibleTime")));
      changeMetrics.put("size", AmsUtils.byteToXB(changeFilesStatistics.getTotalSize()));
      changeMetrics.put("file", changeFilesStatistics.getFileCnt());
      changeMetrics.put("averageFile", AmsUtils.byteToXB(changeFilesStatistics.getAverageSize()));
      Long changeMaxET = fileInfoCacheService
          .getWatermark(AmsUtils.toTableIdentifier(TableIdentifier.of(catalog, db, table)),
              Constants.INNER_TABLE_CHANGE);
      if (changeMaxET != null && changeMaxET != 0L) {
        changeMetrics.put("maxEventTime", sd.format(new Date(changeMaxET)));
      } else {
        changeMetrics.put("maxEventTime", null);
      }
    } else {
      changeMetrics.put("lastCommitTime", null);
      changeMetrics.put("size", null);
      changeMetrics.put("file", null);
      changeMetrics.put("averageFile", null);
      changeMetrics.put("maxEventTime", null);
    }
    serverTableMeta.setChangeMetrics(changeMetrics);
    ctx.json(OkResponse.of(serverTableMeta));
  }

  /**
   * get hive table detail.
   */
  public static void getHiveTableDetail(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String table = ctx.pathParam("table");

    // get table from catalog
    String thriftHost = ArcticMetaStore.conf.getString(ArcticMetaStoreConf.THRIFT_BIND_HOST);
    Integer thriftPort = ArcticMetaStore.conf.getInteger(ArcticMetaStoreConf.THRIFT_BIND_PORT);
    ArcticHiveCatalog arcticHiveCatalog
        = (ArcticHiveCatalog)CatalogUtil.getArcticCatalog(thriftHost, thriftPort, catalog);

    TableIdentifier tableIdentifier = TableIdentifier.of(catalog, db, table);
    HiveTableInfo hiveTableInfo = null;
    try {
      Table hiveTable = HiveTableUtil.loadHmsTable(arcticHiveCatalog.getHMSClient(), tableIdentifier);
      List<AMSColumnInfo> schema =
          AmsUtils.transforHiveSchemaToAMSColumnInfos(hiveTable.getSd().getCols());
      List<AMSColumnInfo> partitionColumnInfos =
          AmsUtils.transforHiveSchemaToAMSColumnInfos(hiveTable.getPartitionKeys());
      hiveTableInfo = new HiveTableInfo(tableIdentifier, TableMeta.TableType.HIVE, schema, partitionColumnInfos,
          new HashMap<>(), hiveTable.getCreateTime());
    } catch (Exception e) {
      LOG.error("Failed to get hive table info", e);
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST, "Failed to get hive table info", ""));
      return;
    }
    ctx.json(OkResponse.of(hiveTableInfo));
  }

  /**
   * upgrade hive table to arctic.
   */
  public static void upgradeHiveTable(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String table = ctx.pathParam("table");
    UpgradeHiveMeta upgradeHiveMeta = ctx.bodyAsClass(UpgradeHiveMeta.class);

    String thriftHost = ArcticMetaStore.conf.getString(ArcticMetaStoreConf.THRIFT_BIND_HOST);
    Integer thriftPort = ArcticMetaStore.conf.getInteger(ArcticMetaStoreConf.THRIFT_BIND_PORT);
    ArcticHiveCatalog arcticHiveCatalog
        = (ArcticHiveCatalog)CatalogUtil.getArcticCatalog(thriftHost, thriftPort, catalog);
    adaptHiveService.upgradeHiveTable(arcticHiveCatalog, TableIdentifier.of(catalog, db, table), upgradeHiveMeta);
    ctx.json(OkResponse.ok());
  }

  public static void getUpgradeStatus(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String table = ctx.pathParam("table");
    ctx.json(OkResponse.of(adaptHiveService.getUpgradeRunningInfo(TableIdentifier.of(catalog, db, table))));
  }


  /**
   * upgrade hive table to arctic.
   */
  public static void getUpgradeHiveTableProperties(Context ctx) throws IllegalAccessException {
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
  public static void getOptimizeInfo(Context ctx) {

    String catalog = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String table = ctx.pathParam("table");
    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);


    int offset = (page - 1) * pageSize;
    int limit = pageSize;
    checkOffsetAndLimit(offset, limit);

    TableIdentifier tableIdentifier = TableIdentifier.of(catalog, db, table);
    List<BaseMajorCompactRecord> baseMajorCompactRecords = null;
    try {
      List<OptimizeHistory> tmpRecords = optimizeService.getOptimizeHistory(
              tableIdentifier);
      if (tmpRecords == null) {
        ctx.json(OkResponse.of(PageResult.of(new ArrayList<>(), 0)));
        return;
      }
      baseMajorCompactRecords = tmpRecords.stream()
              .map(AmsUtils::transferToBaseMajorCompactRecord)
              .collect(Collectors.toList());
    } catch (Exception e) {
      LOG.error("Failed to get optimize info", e);
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST,
              "Failed to get optimize info", ""));
      return;
    }
    int total = baseMajorCompactRecords.size();
    Collections.reverse(baseMajorCompactRecords);
    List<BaseMajorCompactRecord> result = baseMajorCompactRecords.stream()
            .skip(offset)
            .limit(limit)
            .collect(Collectors.toList());
    ctx.json(OkResponse.of(PageResult.of(result, total)));
    return;
  }

  /**
   * get list of transactions.
   */
  public static void getTableTransactions(Context ctx) {

    String catalog = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String table = ctx.pathParam("table");
    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);

    try {
      List<TransactionsOfTable> transactionsOfTables = fileInfoCacheService.getTxExcludeOptimize(
              AmsUtils.toTableIdentifier(TableIdentifier.of(catalog, db, table)));
      Integer offset = (page - 1) * pageSize;
      PageResult<TransactionsOfTable, AMSTransactionsOfTable> pageResult = PageResult.of(transactionsOfTables,
              offset, pageSize, AmsUtils::toTransactionsOfTable);
      ctx.json(OkResponse.of(pageResult));
      return;
    } catch (Exception e) {
      LOG.error("Failed to list transactions ", e);
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST, "Failed to list transactions", ""));
      return;
    }
  }

  /**
   * get detail of transaction.
   */
  public static void getTransactionDetail(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String table = ctx.pathParam("table");
    String transactionId = ctx.pathParam("transactionId");
    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);

    try {
      List<AMSDataFileInfo> dataFileInfo = fileInfoCacheService.getDatafilesInfo(
              AmsUtils.toTableIdentifier(TableIdentifier.of(catalog, db, table)), Long.valueOf(transactionId));
      Integer offset = (page - 1) * pageSize;
      PageResult<DataFileInfo, AMSDataFileInfo> amsPageResult = PageResult.of(dataFileInfo,
              offset, pageSize);
      ctx.json(OkResponse.of(amsPageResult));
      return;
    } catch (Exception e) {
      LOG.error("Failed to get transactions detail", e);
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST, "Failed to get transactions detail", ""));
      return;
    }
  }

  /**
   * get partition list.
   */
  public static void getTablePartitions(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String table = ctx.pathParam("table");
    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);

    try {
      // First determine whether there is a partitioned table, and then get different information
      List<PartitionBaseInfo> partitionBaseInfos = fileInfoCacheService.getPartitionBaseInfoList(
              AmsUtils.toTableIdentifier(TableIdentifier.of(catalog, db, table)));
      Integer offset = (page - 1) * pageSize;
      PageResult<PartitionBaseInfo, PartitionBaseInfo> amsPageResult = PageResult.of(partitionBaseInfos,
              offset, pageSize);
      ctx.json(OkResponse.of(amsPageResult));
      return;
    } catch (Exception e) {
      LOG.error("Failed to get transactions detail", e);
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST, "Failed to get transactions detail", ""));
    }
  }

  /**
   * get file list of some partition.
   */
  public static void getPartitionFileListInfo(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String table = ctx.pathParam("table");
    String partition = ctx.pathParam("partition");

    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);


    // Determine whether there is a partitioned table
    try {
      // The partition passed by the no-partition table is null
      if ("null".equals(partition)) {
        // get table from catalog
        String thriftHost = ArcticMetaStore.conf.getString(ArcticMetaStoreConf.THRIFT_BIND_HOST);
        Integer thriftPort = ArcticMetaStore.conf.getInteger(ArcticMetaStoreConf.THRIFT_BIND_PORT);
        ArcticCatalog ac = CatalogUtil.getArcticCatalog(thriftHost, thriftPort, catalog);
        ArcticTable at = ac.loadTable(TableIdentifier.of(catalog, db, table));
        // The partition is passed as null, and it is a table with no partition,
        // then it is confirmed to be a table with no partition.
        if (at.spec().isUnpartitioned()) {
          partition = null;
        }
      }
      if (partition != null) {
        partition = java.net.URLDecoder.decode(partition, StandardCharsets.UTF_8.name());
      }
      List<PartitionFileBaseInfo> partitionFileBaseInfos = fileInfoCacheService.getPartitionFileList(
              AmsUtils.toTableIdentifier(TableIdentifier.of(catalog, db, table)), partition);
      Integer offset = (page - 1) * pageSize;
      PageResult<PartitionFileBaseInfo, PartitionFileBaseInfo> amsPageResult = PageResult.of(partitionFileBaseInfos,
              offset, pageSize);
      ctx.json(OkResponse.of(amsPageResult));
      return;
    } catch (Exception e) {
      LOG.error("Failed to get partition file list", e);
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST, "Failed to get partition file list", ""));
      return;
    }
  }

  /* get  operations of some table*/
  public static void getTableOperations(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String table = ctx.pathParam("table");

    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);
    Integer offset = (page - 1) * pageSize;

    List<DDLInfo> ddlInfos = ddlTracerService.getDDL(TableIdentifier.of(catalog, db, table).buildTableIdentifier());
    PageResult<DDLInfo, TableOperation> amsPageResult = PageResult.of(ddlInfos,
            offset, pageSize, TableOperation::buildFromDDLInfo);
    ctx.json(OkResponse.of(amsPageResult));
  }

  /**
   * get table list of catalog.db.
   */
  public static void getTableList(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String keywords = ctx.queryParam("keywords");

    String thriftHost = ArcticMetaStore.conf.getString(ArcticMetaStoreConf.THRIFT_BIND_HOST);
    Integer thriftPort = ArcticMetaStore.conf.getInteger(ArcticMetaStoreConf.THRIFT_BIND_PORT);
    ArcticCatalog ac = CatalogUtil.getArcticCatalog(thriftHost, thriftPort, catalog);
    List<TableIdentifier> tableIdentifiers = ac.listTables(db);
    LinkedHashSet<TableMeta> tempTables = new LinkedHashSet<>();
    List<TableMeta> tables = new ArrayList<>();
    if (CatalogUtil.isIcebergCatalog(catalog)) {
      for (TableIdentifier tableIdentifier : tableIdentifiers) {
        tables.add(new TableMeta(tableIdentifier.getTableName(), TableMeta.TableType.ICEBERG.toString()));
      }
    } else if (catalogMetadataService.getCatalog(catalog)
        .getCatalogType().equals(CatalogMetaProperties.CATALOG_TYPE_HIVE)) {
      ArcticHiveCatalog arcticHiveCatalog = (ArcticHiveCatalog)ac;
      List<String> hiveTables = HiveTableUtil.getAllHiveTables(arcticHiveCatalog.getHMSClient(), db);
      for (String hiveTable : hiveTables) {
        tempTables.add(new TableMeta(hiveTable, TableMeta.TableType.HIVE.toString()));
      }
      for (TableIdentifier tableIdentifier : tableIdentifiers) {
        TableMeta tableMeta = new TableMeta(tableIdentifier.getTableName(), TableMeta.TableType.ARCTIC.toString());
        if (tempTables.contains(tableMeta)) {
          tables.add(tableMeta);
          tempTables.remove(tableMeta);
        }
      }
      tables.addAll(tempTables);
    } else {
      for (TableIdentifier tableIdentifier : tableIdentifiers) {
        tables.add(new TableMeta(tableIdentifier.getTableName(), TableMeta.TableType.ARCTIC.toString()));
      }
    }
    ctx.json(OkResponse.of(tables.stream().filter(t -> StringUtils.isEmpty(keywords) ||
        t.getName().contains(keywords)).collect(Collectors.toList())));
  }

  /**
   * get databases of some catalog.
   */
  public static void getDatabaseList(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String keywords = ctx.queryParam("keywords");

    String thriftHost = ArcticMetaStore.conf.getString(ArcticMetaStoreConf.THRIFT_BIND_HOST);
    Integer thriftPort = ArcticMetaStore.conf.getInteger(ArcticMetaStoreConf.THRIFT_BIND_PORT);
    ArcticCatalog ac = CatalogUtil.getArcticCatalog(thriftHost, thriftPort, catalog);
    List<String> dbList = ac.listDatabases().stream()
            .filter(item -> StringUtils.isEmpty(keywords) || item.contains(keywords))
            .collect(Collectors.toList());
    ctx.json(OkResponse.of(dbList));
  }

  /**
   * list catalogs.
   */
  public static void getCatalogs(Context ctx) {
    List<CatalogMeta> catalogs = catalogMetadataService.getCatalogs().stream().map(t ->
            new CatalogMeta(t.getCatalogName(), t.getCatalogType())).collect(Collectors.toList());
    ctx.json(OkResponse.of(catalogs));
  }

  /**
   * get single page query token
   * @param ctx
   */
  public static void getTableDetailTabToken(Context ctx) {
    String catalog =  ctx.pathParam("catalog");
    String db =  ctx.pathParam("db");
    String table =  ctx.pathParam("table");

    String signCal = Utils.generateTablePageToken(catalog, db, table);
    ctx.json(OkResponse.of(signCal));
  }

}
