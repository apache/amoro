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

import static org.apache.amoro.properties.CatalogMetaProperties.CATALOG_TYPE_HIVE;

import io.javalin.http.Context;
import org.apache.amoro.Constants;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.hive.CachedHiveClientPool;
import org.apache.amoro.hive.HMSClientPool;
import org.apache.amoro.hive.catalog.MixedHiveCatalog;
import org.apache.amoro.hive.utils.HiveTableUtil;
import org.apache.amoro.hive.utils.UpgradeHiveTableUtil;
import org.apache.amoro.mixed.CatalogLoader;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.properties.HiveTableProperties;
import org.apache.amoro.server.catalog.ServerCatalog;
import org.apache.amoro.server.dashboard.ServerTableDescriptor;
import org.apache.amoro.server.dashboard.ServerTableProperties;
import org.apache.amoro.server.dashboard.model.HiveTableInfo;
import org.apache.amoro.server.dashboard.model.TableMeta;
import org.apache.amoro.server.dashboard.model.TableOperation;
import org.apache.amoro.server.dashboard.model.UpgradeHiveMeta;
import org.apache.amoro.server.dashboard.model.UpgradeRunningInfo;
import org.apache.amoro.server.dashboard.model.UpgradeStatus;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.server.dashboard.response.PageResult;
import org.apache.amoro.server.dashboard.utils.AmsUtil;
import org.apache.amoro.server.dashboard.utils.CommonUtil;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.optimizing.plan.OptimizingEvaluator;
import org.apache.amoro.server.table.TableRuntime;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.shade.guava32.com.google.common.base.Function;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.descriptor.AMSColumnInfo;
import org.apache.amoro.table.descriptor.AmoroSnapshotsOfTable;
import org.apache.amoro.table.descriptor.ConsumerInfo;
import org.apache.amoro.table.descriptor.DDLInfo;
import org.apache.amoro.table.descriptor.OperationType;
import org.apache.amoro.table.descriptor.OptimizingProcessInfo;
import org.apache.amoro.table.descriptor.OptimizingTaskInfo;
import org.apache.amoro.table.descriptor.PartitionBaseInfo;
import org.apache.amoro.table.descriptor.PartitionFileBaseInfo;
import org.apache.amoro.table.descriptor.ServerTableMeta;
import org.apache.amoro.table.descriptor.TableSummary;
import org.apache.amoro.table.descriptor.TagOrBranchInfo;
import org.apache.amoro.utils.CatalogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.SnapshotRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/** The controller that handles table requests. */
public class TableController {
  private static final Logger LOG = LoggerFactory.getLogger(TableController.class);
  private static final long UPGRADE_INFO_EXPIRE_INTERVAL = 60 * 60 * 1000;

  private final TableService tableService;
  private final ServerTableDescriptor tableDescriptor;
  private final Configurations serviceConfig;
  private final ConcurrentHashMap<TableIdentifier, UpgradeRunningInfo> upgradeRunningInfo =
      new ConcurrentHashMap<>();
  private final ScheduledExecutorService tableUpgradeExecutor;

  public TableController(
      TableService tableService,
      ServerTableDescriptor tableDescriptor,
      Configurations serviceConfig) {
    this.tableService = tableService;
    this.tableDescriptor = tableDescriptor;
    this.serviceConfig = serviceConfig;
    this.tableUpgradeExecutor =
        Executors.newScheduledThreadPool(
            0,
            new ThreadFactoryBuilder()
                .setDaemon(false)
                .setNameFormat("async-hive-table-upgrade-%d")
                .build());
  }

  /**
   * get table detail.
   *
   * @param ctx - context for handling the request and response
   */
  public void getTableDetail(Context ctx) {

    String catalog = ctx.pathParam("catalog");
    String database = ctx.pathParam("db");
    String tableName = ctx.pathParam("table");

    Preconditions.checkArgument(
        StringUtils.isNotBlank(catalog)
            && StringUtils.isNotBlank(database)
            && StringUtils.isNotBlank(tableName),
        "catalog.database.tableName can not be empty in any element");
    Preconditions.checkState(tableService.catalogExist(catalog), "invalid catalog!");

    ServerTableMeta serverTableMeta =
        tableDescriptor.getTableDetail(
            TableIdentifier.of(catalog, database, tableName).buildTableIdentifier());
    TableSummary tableSummary = serverTableMeta.getTableSummary();
    Optional<ServerTableIdentifier> serverTableIdentifier =
        Optional.ofNullable(
            tableService.getServerTableIdentifier(
                TableIdentifier.of(catalog, database, tableName).buildTableIdentifier()));
    if (serverTableIdentifier.isPresent()) {
      TableRuntime tableRuntime = tableService.getRuntime(serverTableIdentifier.get());
      tableSummary.setOptimizingStatus(tableRuntime.getOptimizingStatus().name());
      OptimizingEvaluator.PendingInput tableRuntimeSummary = tableRuntime.getTableSummary();
      if (tableRuntimeSummary != null) {
        tableSummary.setHealthScore(tableRuntimeSummary.getHealthScore());
      }
    } else {
      tableSummary.setOptimizingStatus(OptimizingStatus.IDLE.name());
    }
    ctx.json(OkResponse.of(serverTableMeta));
  }

  /**
   * get hive table detail.
   *
   * @param ctx - context for handling the request and response
   */
  public void getHiveTableDetail(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String table = ctx.pathParam("table");
    Preconditions.checkArgument(
        StringUtils.isNotBlank(catalog)
            && StringUtils.isNotBlank(db)
            && StringUtils.isNotBlank(table),
        "catalog.database.tableName can not be empty in any element");
    ServerCatalog serverCatalog = tableService.getServerCatalog(catalog);
    CatalogMeta catalogMeta = serverCatalog.getMetadata();
    TableMetaStore tableMetaStore = CatalogUtil.buildMetaStore(catalogMeta);
    HMSClientPool hmsClientPool =
        new CachedHiveClientPool(tableMetaStore, catalogMeta.getCatalogProperties());

    TableIdentifier tableIdentifier = TableIdentifier.of(catalog, db, table);
    HiveTableInfo hiveTableInfo;
    Table hiveTable = HiveTableUtil.loadHmsTable(hmsClientPool, tableIdentifier);
    List<AMSColumnInfo> schema = transformHiveSchemaToAMSColumnInfo(hiveTable.getSd().getCols());
    List<AMSColumnInfo> partitionColumnInfos =
        transformHiveSchemaToAMSColumnInfo(hiveTable.getPartitionKeys());
    hiveTableInfo =
        new HiveTableInfo(
            tableIdentifier,
            TableMeta.TableType.HIVE,
            schema,
            partitionColumnInfos,
            new HashMap<>(),
            hiveTable.getCreateTime());
    ctx.json(OkResponse.of(hiveTableInfo));
  }

  /**
   * upgrade a hive table to mixed-hive table.
   *
   * @param ctx - context for handling the request and response
   */
  public void upgradeHiveTable(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String table = ctx.pathParam("table");
    Preconditions.checkArgument(
        StringUtils.isNotBlank(catalog)
            && StringUtils.isNotBlank(db)
            && StringUtils.isNotBlank(table),
        "catalog.database.tableName can not be empty in any element");
    UpgradeHiveMeta upgradeHiveMeta = ctx.bodyAsClass(UpgradeHiveMeta.class);

    ServerCatalog serverCatalog = tableService.getServerCatalog(catalog);
    CatalogMeta catalogMeta = serverCatalog.getMetadata();
    String amsUri = AmsUtil.getAMSThriftAddress(serviceConfig, Constants.THRIFT_TABLE_SERVICE_NAME);
    catalogMeta.putToCatalogProperties(CatalogMetaProperties.AMS_URI, amsUri);
    TableMetaStore tableMetaStore = CatalogUtil.buildMetaStore(catalogMeta);
    // check whether catalog support MIXED_HIVE format.
    Set<TableFormat> tableFormats = CatalogUtil.tableFormats(catalogMeta);
    Preconditions.checkState(
        tableFormats.contains(TableFormat.MIXED_HIVE),
        "Catalog %s does not support MIXED_HIVE format",
        catalog);
    // we should only keep MIXED_HIVE format，
    // so `CatalogLoader.createCatalog` can get right CatalogImpl through calling catalogImpl.
    Map<String, String> originCatalogProperties = catalogMeta.getCatalogProperties();
    Map<String, String> catalogProperties = new HashMap<>(originCatalogProperties);
    catalogProperties.put(CatalogMetaProperties.TABLE_FORMATS, TableFormat.MIXED_HIVE.name());

    MixedHiveCatalog mixedHiveCatalog =
        (MixedHiveCatalog)
            CatalogLoader.createCatalog(
                catalog, catalogMeta.getCatalogType(), catalogProperties, tableMetaStore);

    tableUpgradeExecutor.execute(
        () -> {
          TableIdentifier tableIdentifier = TableIdentifier.of(catalog, db, table);
          upgradeRunningInfo.put(tableIdentifier, new UpgradeRunningInfo());
          try {
            UpgradeHiveTableUtil.upgradeHiveTable(
                mixedHiveCatalog,
                TableIdentifier.of(catalog, db, table),
                upgradeHiveMeta.getPkList().stream()
                    .map(UpgradeHiveMeta.PrimaryKeyField::getFieldName)
                    .collect(Collectors.toList()),
                upgradeHiveMeta.getProperties());
            upgradeRunningInfo.get(tableIdentifier).setStatus(UpgradeStatus.SUCCESS.toString());
          } catch (Throwable t) {
            LOG.error("Failed to upgrade hive table to mixed-hive table ", t);
            upgradeRunningInfo.get(tableIdentifier).setErrorMessage(AmsUtil.getStackTrace(t));
            upgradeRunningInfo.get(tableIdentifier).setStatus(UpgradeStatus.FAILED.toString());
          } finally {
            tableUpgradeExecutor.schedule(
                () -> upgradeRunningInfo.remove(tableIdentifier),
                UPGRADE_INFO_EXPIRE_INTERVAL,
                TimeUnit.MILLISECONDS);
          }
        });
    ctx.json(OkResponse.ok());
  }

  public void getUpgradeStatus(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String table = ctx.pathParam("table");
    UpgradeRunningInfo info =
        upgradeRunningInfo.containsKey(TableIdentifier.of(catalog, db, table))
            ? upgradeRunningInfo.get(TableIdentifier.of(catalog, db, table))
            : new UpgradeRunningInfo(UpgradeStatus.NONE.toString());
    ctx.json(OkResponse.of(info));
  }

  /**
   * get table properties for upgrading hive table to mixed-hive table.
   *
   * @param ctx - context for handling the request and response
   */
  public void getUpgradeHiveTableProperties(Context ctx) throws IllegalAccessException {
    Map<String, String> keyValues = new TreeMap<>();
    Map<String, String> tableProperties =
        AmsUtil.getNotDeprecatedAndNotInternalStaticFields(TableProperties.class);
    tableProperties.keySet().stream()
        .filter(key -> !key.endsWith("_DEFAULT"))
        .forEach(
            key -> keyValues.put(tableProperties.get(key), tableProperties.get(key + "_DEFAULT")));
    ServerTableProperties.HIDDEN_EXPOSED.forEach(keyValues::remove);
    Map<String, String> hiveProperties =
        AmsUtil.getNotDeprecatedAndNotInternalStaticFields(HiveTableProperties.class);

    hiveProperties.keySet().stream()
        .filter(key -> HiveTableProperties.EXPOSED.contains(hiveProperties.get(key)))
        .filter(key -> !key.endsWith("_DEFAULT"))
        .forEach(
            key -> keyValues.put(hiveProperties.get(key), hiveProperties.get(key + "_DEFAULT")));
    ctx.json(OkResponse.of(keyValues));
  }

  /**
   * get list of optimizing processes.
   *
   * @param ctx - context for handling the request and response
   */
  public void getOptimizingProcesses(Context ctx) {

    String catalog = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String table = ctx.pathParam("table");
    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);

    int offset = (page - 1) * pageSize;
    int limit = pageSize;
    ServerCatalog serverCatalog = tableService.getServerCatalog(catalog);
    Preconditions.checkArgument(offset >= 0, "offset[%s] must >= 0", offset);
    Preconditions.checkArgument(limit >= 0, "limit[%s] must >= 0", limit);
    Preconditions.checkState(serverCatalog.tableExists(db, table), "no such table");

    TableIdentifier tableIdentifier = TableIdentifier.of(catalog, db, table);
    Pair<List<OptimizingProcessInfo>, Integer> optimizingProcessesInfo =
        tableDescriptor.getOptimizingProcessesInfo(
            tableIdentifier.buildTableIdentifier(), limit, offset);
    List<OptimizingProcessInfo> result = optimizingProcessesInfo.getLeft();
    int total = optimizingProcessesInfo.getRight();

    ctx.json(OkResponse.of(PageResult.of(result, total)));
  }

  /**
   * Get tasks of optimizing process.
   *
   * @param ctx - context for handling the request and response
   */
  public void getOptimizingProcessTasks(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String table = ctx.pathParam("table");
    String processId = ctx.pathParam("processId");
    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);

    int offset = (page - 1) * pageSize;
    int limit = pageSize;
    ServerCatalog serverCatalog = tableService.getServerCatalog(catalog);
    Preconditions.checkArgument(offset >= 0, "offset[%s] must >= 0", offset);
    Preconditions.checkArgument(limit >= 0, "limit[%s] must >= 0", limit);
    Preconditions.checkState(serverCatalog.tableExists(db, table), "no such table");

    TableIdentifier tableIdentifier = TableIdentifier.of(catalog, db, table);
    List<OptimizingTaskInfo> optimizingTaskInfos =
        tableDescriptor.getOptimizingProcessTaskInfos(
            tableIdentifier.buildTableIdentifier(), processId);

    PageResult<OptimizingTaskInfo> pageResult = PageResult.of(optimizingTaskInfos, offset, limit);
    ctx.json(OkResponse.of(pageResult));
  }

  /**
   * get list of snapshots.
   *
   * @param ctx - context for handling the request and response
   */
  public void getTableSnapshots(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String database = ctx.pathParam("db");
    String tableName = ctx.pathParam("table");
    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);
    // ref means tag/branch
    String ref = ctx.queryParamAsClass("ref", String.class).getOrDefault(null);
    String operation =
        ctx.queryParamAsClass("operation", String.class)
            .getOrDefault(OperationType.ALL.displayName());
    OperationType operationType = OperationType.of(operation);

    List<AmoroSnapshotsOfTable> snapshotsOfTables =
        tableDescriptor.getSnapshots(
            TableIdentifier.of(catalog, database, tableName).buildTableIdentifier(),
            ref,
            operationType);
    int offset = (page - 1) * pageSize;
    PageResult<AmoroSnapshotsOfTable> pageResult =
        PageResult.of(snapshotsOfTables, offset, pageSize);
    ctx.json(OkResponse.of(pageResult));
  }

  /**
   * get detail of snapshot.
   *
   * @param ctx - context for handling the request and response
   */
  public void getSnapshotDetail(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String database = ctx.pathParam("db");
    String tableName = ctx.pathParam("table");
    String snapshotId = ctx.pathParam("snapshotId");
    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);

    List<PartitionFileBaseInfo> result =
        tableDescriptor.getSnapshotDetail(
            TableIdentifier.of(catalog, database, tableName).buildTableIdentifier(), snapshotId);
    int offset = (page - 1) * pageSize;
    PageResult<PartitionFileBaseInfo> amsPageResult = PageResult.of(result, offset, pageSize);
    ctx.json(OkResponse.of(amsPageResult));
  }

  /**
   * get partition list.
   *
   * @param ctx - context for handling the request and response
   */
  public void getTablePartitions(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String database = ctx.pathParam("db");
    String table = ctx.pathParam("table");
    String filter = ctx.queryParamAsClass("filter", String.class).getOrDefault("");
    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);

    List<PartitionBaseInfo> partitionBaseInfos =
        tableDescriptor.getTablePartition(
            TableIdentifier.of(catalog, database, table).buildTableIdentifier());
    partitionBaseInfos =
        partitionBaseInfos.stream()
            .filter(e -> e.getPartition().contains(filter))
            .sorted(Comparator.comparing(PartitionBaseInfo::getPartition).reversed())
            .collect(Collectors.toList());
    int offset = (page - 1) * pageSize;
    PageResult<PartitionBaseInfo> amsPageResult =
        PageResult.of(partitionBaseInfos, offset, pageSize);
    ctx.json(OkResponse.of(amsPageResult));
  }

  /**
   * get file list of some partition.
   *
   * @param ctx - context for handling the request and response
   */
  public void getPartitionFileListInfo(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String table = ctx.pathParam("table");
    String partition = ctx.pathParam("partition");

    Integer specId = ctx.queryParamAsClass("specId", Integer.class).getOrDefault(0);
    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);

    List<PartitionFileBaseInfo> partitionFileBaseInfos =
        tableDescriptor.getTableFile(
            TableIdentifier.of(catalog, db, table).buildTableIdentifier(), partition, specId);
    int offset = (page - 1) * pageSize;
    PageResult<PartitionFileBaseInfo> amsPageResult =
        PageResult.of(partitionFileBaseInfos, offset, pageSize);
    ctx.json(OkResponse.of(amsPageResult));
  }

  /**
   * get table operations.
   *
   * @param ctx - context for handling the request and response
   */
  public void getTableOperations(Context ctx) throws Exception {
    String catalogName = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String tableName = ctx.pathParam("table");

    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);
    int offset = (page - 1) * pageSize;

    List<DDLInfo> ddlInfoList =
        tableDescriptor.getTableOperations(
            TableIdentifier.of(catalogName, db, tableName).buildTableIdentifier());
    Collections.reverse(ddlInfoList);
    PageResult<TableOperation> amsPageResult =
        PageResult.of(ddlInfoList, offset, pageSize, TableOperation::buildFromDDLInfo);
    ctx.json(OkResponse.of(amsPageResult));
  }

  /**
   * get table list of catalog.db.
   *
   * @param ctx - context for handling the request and response
   */
  public void getTableList(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String keywords = ctx.queryParam("keywords");
    Preconditions.checkArgument(
        StringUtils.isNotBlank(catalog) && StringUtils.isNotBlank(db),
        "catalog.database can not be empty in any element");

    ServerCatalog serverCatalog = tableService.getServerCatalog(catalog);
    Function<TableFormat, String> formatToType =
        format -> {
          switch (format) {
            case MIXED_HIVE:
            case MIXED_ICEBERG:
              return TableMeta.TableType.ARCTIC.toString();
            case PAIMON:
              return TableMeta.TableType.PAIMON.toString();
            case ICEBERG:
              return TableMeta.TableType.ICEBERG.toString();
            case HUDI:
              return TableMeta.TableType.HUDI.toString();
            default:
              throw new IllegalStateException("Unknown format");
          }
        };

    List<TableMeta> tables =
        serverCatalog.listTables(db).stream()
            .map(
                idWithFormat ->
                    new TableMeta(
                        idWithFormat.getIdentifier().getTableName(),
                        formatToType.apply(idWithFormat.getTableFormat())))
            // Sort by table format and table name
            .sorted(
                (table1, table2) -> {
                  if (Objects.equals(table1.getType(), table2.getType())) {
                    return table1.getName().compareTo(table2.getName());
                  } else {
                    return table1.getType().compareTo(table2.getType());
                  }
                })
            .collect(Collectors.toList());
    String catalogType = serverCatalog.getMetadata().getCatalogType();
    if (catalogType.equals(CATALOG_TYPE_HIVE)) {
      CatalogMeta catalogMeta = serverCatalog.getMetadata();
      TableMetaStore tableMetaStore = CatalogUtil.buildMetaStore(catalogMeta);
      HMSClientPool hmsClientPool =
          new CachedHiveClientPool(tableMetaStore, catalogMeta.getCatalogProperties());

      List<String> hiveTables = HiveTableUtil.getAllHiveTables(hmsClientPool, db);
      Set<String> mixedHiveTables =
          tables.stream().map(TableMeta::getName).collect(Collectors.toSet());
      hiveTables.stream()
          .filter(e -> !mixedHiveTables.contains(e))
          .sorted(String::compareTo)
          .forEach(e -> tables.add(new TableMeta(e, TableMeta.TableType.HIVE.toString())));
    }

    ctx.json(
        OkResponse.of(
            tables.stream()
                .filter(t -> StringUtils.isBlank(keywords) || t.getName().contains(keywords))
                .collect(Collectors.toList())));
  }

  /**
   * get databases of some catalog.
   *
   * @param ctx - context for handling the request and response
   */
  public void getDatabaseList(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String keywords = ctx.queryParam("keywords");

    List<String> dbList =
        tableService.getServerCatalog(catalog).listDatabases().stream()
            .filter(item -> StringUtils.isBlank(keywords) || item.contains(keywords))
            .collect(Collectors.toList());
    ctx.json(OkResponse.of(dbList));
  }

  /**
   * get list of catalogs.
   *
   * @param ctx - context for handling the request and response
   */
  public void getCatalogs(Context ctx) {
    List<CatalogMeta> catalogs = tableService.listCatalogMetas();
    ctx.json(OkResponse.of(catalogs));
  }

  /**
   * get single page query token.
   *
   * @param ctx - context for handling the request and response
   */
  public void getTableDetailTabToken(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String table = ctx.pathParam("table");

    String signCal = CommonUtil.generateTablePageToken(catalog, db, table);
    ctx.json(OkResponse.of(signCal));
  }

  public void getTableTags(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String database = ctx.pathParam("db");
    String table = ctx.pathParam("table");
    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);
    List<TagOrBranchInfo> partitionBaseInfos =
        tableDescriptor.getTableTags(
            TableIdentifier.of(catalog, database, table).buildTableIdentifier());
    int offset = (page - 1) * pageSize;
    PageResult<TagOrBranchInfo> amsPageResult = PageResult.of(partitionBaseInfos, offset, pageSize);
    ctx.json(OkResponse.of(amsPageResult));
  }

  public void getTableBranches(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String database = ctx.pathParam("db");
    String table = ctx.pathParam("table");
    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);
    List<TagOrBranchInfo> partitionBaseInfos =
        tableDescriptor.getTableBranches(
            TableIdentifier.of(catalog, database, table).buildTableIdentifier());
    putMainBranchFirst(partitionBaseInfos);
    int offset = (page - 1) * pageSize;
    PageResult<TagOrBranchInfo> amsPageResult = PageResult.of(partitionBaseInfos, offset, pageSize);
    ctx.json(OkResponse.of(amsPageResult));
  }

  public void getTableConsumerInfos(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String database = ctx.pathParam("db");
    String table = ctx.pathParam("table");
    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);
    List<ConsumerInfo> consumerInfos =
        tableDescriptor.getTableConsumersInfos(
            TableIdentifier.of(catalog, database, table).buildTableIdentifier());
    int offset = (page - 1) * pageSize;
    PageResult<ConsumerInfo> amsPageResult = PageResult.of(consumerInfos, offset, pageSize);
    ctx.json(OkResponse.of(amsPageResult));
  }

  /**
   * cancel the running optimizing process of one certain table.
   *
   * @param ctx - context for handling the request and response
   */
  public void cancelOptimizingProcess(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String table = ctx.pathParam("table");
    String processId = ctx.pathParam("processId");
    Preconditions.checkArgument(
        StringUtils.isNotBlank(catalog)
            && StringUtils.isNotBlank(db)
            && StringUtils.isNotBlank(table),
        "catalog.database.tableName can not be empty in any element");
    Preconditions.checkState(tableService.catalogExist(catalog), "invalid catalog!");

    ServerTableIdentifier serverTableIdentifier =
        tableService.getServerTableIdentifier(
            TableIdentifier.of(catalog, db, table).buildTableIdentifier());
    TableRuntime tableRuntime =
        serverTableIdentifier != null ? tableService.getRuntime(serverTableIdentifier) : null;

    Preconditions.checkArgument(
        tableRuntime != null
            && tableRuntime.getOptimizingProcess() != null
            && Objects.equals(
                tableRuntime.getOptimizingProcess().getProcessId(), Long.parseLong(processId)),
        "Can't cancel optimizing process %s",
        processId);

    tableRuntime.getOptimizingProcess().close();
    ctx.json(OkResponse.ok());
  }

  private void putMainBranchFirst(List<TagOrBranchInfo> branchInfos) {
    if (branchInfos.size() <= 1) {
      return;
    }
    branchInfos.stream()
        .filter(branch -> SnapshotRef.MAIN_BRANCH.equals(branch.getName()))
        .findFirst()
        .ifPresent(
            mainBranch -> {
              branchInfos.remove(mainBranch);
              branchInfos.add(0, mainBranch);
            });
  }

  private List<AMSColumnInfo> transformHiveSchemaToAMSColumnInfo(List<FieldSchema> fields) {
    return fields.stream()
        .map(
            f -> {
              AMSColumnInfo columnInfo = new AMSColumnInfo();
              columnInfo.setField(f.getName());
              columnInfo.setType(f.getType());
              columnInfo.setComment(f.getComment());
              return columnInfo;
            })
        .collect(Collectors.toList());
  }
}
