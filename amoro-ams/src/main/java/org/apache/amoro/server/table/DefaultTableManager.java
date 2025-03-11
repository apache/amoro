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

package org.apache.amoro.server.table;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.api.BlockableOperation;
import org.apache.amoro.api.Blocker;
import org.apache.amoro.api.TableIdentifier;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.exception.AlreadyExistsException;
import org.apache.amoro.exception.BlockerConflictException;
import org.apache.amoro.exception.IllegalMetadataException;
import org.apache.amoro.exception.ObjectNotExistsException;
import org.apache.amoro.exception.PersistenceException;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.AmoroServiceConstants;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.catalog.InternalCatalog;
import org.apache.amoro.server.dashboard.model.TableOptimizingInfo;
import org.apache.amoro.server.dashboard.utils.OptimizingUtil;
import org.apache.amoro.server.optimizing.OptimizingTaskMeta;
import org.apache.amoro.server.optimizing.TaskRuntime;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.server.persistence.mapper.OptimizingMapper;
import org.apache.amoro.server.persistence.mapper.TableBlockerMapper;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.server.table.blocker.TableBlocker;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultTableManager extends PersistentBase implements TableManager {

  public static final Logger LOG = LoggerFactory.getLogger(DefaultTableManager.class);
  private static final int TABLE_BLOCKER_RETRY = 3;
  private final long blockerTimeout;

  private final CatalogManager catalogManager;

  private @Nullable TableService tableService;

  public DefaultTableManager(Configurations configuration, CatalogManager catalogManager) {
    this.catalogManager = catalogManager;
    this.blockerTimeout = configuration.get(AmoroManagementConf.BLOCKER_TIMEOUT).toMillis();
  }

  @Override
  public void setTableService(@Nullable TableService tableService) {
    this.tableService = tableService;
  }

  private Optional<TableService> tableService() {
    return Optional.ofNullable(tableService);
  }

  @Override
  public void dropTableMetadata(TableIdentifier tableIdentifier, boolean deleteData) {
    if (StringUtils.isBlank(tableIdentifier.getTableName())) {
      throw new IllegalMetadataException("table name is blank");
    }
    if (StringUtils.isBlank(tableIdentifier.getCatalog())) {
      throw new IllegalMetadataException("catalog is blank");
    }
    if (StringUtils.isBlank(tableIdentifier.getDatabase())) {
      throw new IllegalMetadataException("database is blank");
    }

    InternalCatalog internalCatalog =
        catalogManager.getInternalCatalog(tableIdentifier.getCatalog());
    String database = tableIdentifier.getDatabase();
    String table = tableIdentifier.getTableName();
    if (!internalCatalog.tableExists(database, table)) {
      throw new ObjectNotExistsException(tableIdentifier);
    }

    ServerTableIdentifier serverTableIdentifier = internalCatalog.dropTable(database, table);
    tableService().ifPresent(s -> s.onTableDropped(internalCatalog, serverTableIdentifier));
  }

  @Override
  public void createTable(String catalogName, TableMetadata tableMetadata) {
    InternalCatalog catalog = catalogManager.getInternalCatalog(catalogName);
    String database = tableMetadata.getTableIdentifier().getDatabase();
    String table = tableMetadata.getTableIdentifier().getTableName();
    if (catalog.tableExists(database, table)) {
      throw new AlreadyExistsException(
          tableMetadata.getTableIdentifier().getIdentifier().buildTableIdentifier());
    }

    TableMetadata metadata = catalog.createTable(tableMetadata);
    tableService().ifPresent(s -> s.onTableCreated(catalog, metadata.getTableIdentifier()));
  }

  @Override
  public List<ServerTableIdentifier> listManagedTables() {
    return getAs(TableMetaMapper.class, TableMetaMapper::selectAllTableIdentifiers);
  }

  @Override
  public Blocker block(
      TableIdentifier tableIdentifier,
      List<BlockableOperation> operations,
      Map<String, String> properties) {
    Preconditions.checkNotNull(operations, "operations should not be null");
    Preconditions.checkArgument(!operations.isEmpty(), "operations should not be empty");
    Preconditions.checkArgument(blockerTimeout > 0, "blocker timeout must > 0");
    String catalog = tableIdentifier.getCatalog();
    String database = tableIdentifier.getDatabase();
    String table = tableIdentifier.getTableName();
    int tryCount = 0;
    while (tryCount++ < TABLE_BLOCKER_RETRY) {
      long now = System.currentTimeMillis();
      doAs(
          TableBlockerMapper.class,
          mapper -> mapper.deleteExpiredBlockers(catalog, database, table, now));
      List<TableBlocker> tableBlockers =
          getAs(
              TableBlockerMapper.class,
              mapper ->
                  mapper.selectBlockers(
                      tableIdentifier.getCatalog(),
                      tableIdentifier.getDatabase(),
                      tableIdentifier.getTableName(),
                      now));
      if (TableBlocker.conflict(operations, tableBlockers)) {
        throw new BlockerConflictException(operations + " is conflict with " + tableBlockers);
      }
      Optional<Long> maxBlockerOpt =
          tableBlockers.stream()
              .map(TableBlocker::getBlockerId)
              .max(Comparator.comparingLong(l -> l));
      long prevBlockerId = maxBlockerOpt.orElse(-1L);

      TableBlocker tableBlocker =
          TableBlocker.buildTableBlocker(
              tableIdentifier, operations, properties, now, blockerTimeout, prevBlockerId);
      try {
        doAs(TableBlockerMapper.class, mapper -> mapper.insert(tableBlocker));
        if (tableBlocker.getBlockerId() > 0) {
          return tableBlocker.buildBlocker();
        }
      } catch (PersistenceException e) {
        LOG.warn("An exception occurs when creating a blocker:{}", tableBlocker, e);
      }
    }
    throw new BlockerConflictException("Failed to create a blocker: conflict meet max retry");
  }

  @Override
  public void releaseBlocker(TableIdentifier tableIdentifier, String blockerId) {
    doAs(TableBlockerMapper.class, mapper -> mapper.deleteBlocker(Long.parseLong(blockerId)));
  }

  @Override
  public long renewBlocker(TableIdentifier tableIdentifier, String blockerId) {
    int retry = 0;
    while (retry++ < TABLE_BLOCKER_RETRY) {
      long now = System.currentTimeMillis();
      long id = Long.parseLong(blockerId);
      TableBlocker tableBlocker =
          getAs(TableBlockerMapper.class, mapper -> mapper.selectBlocker(id, now));
      if (tableBlocker == null) {
        throw new ObjectNotExistsException("Blocker " + blockerId + " of " + tableIdentifier);
      }
      long current = System.currentTimeMillis();
      long expirationTime = now + blockerTimeout;
      long effectRow =
          updateAs(
              TableBlockerMapper.class, mapper -> mapper.renewBlocker(id, current, expirationTime));
      if (effectRow > 0) {
        return expirationTime;
      }
    }
    throw new BlockerConflictException("Failed to renew a blocker: conflict meet max retry");
  }

  @Override
  public List<Blocker> getBlockers(TableIdentifier tableIdentifier) {
    return getAs(
            TableBlockerMapper.class,
            mapper ->
                mapper.selectBlockers(
                    tableIdentifier.getCatalog(),
                    tableIdentifier.getDatabase(),
                    tableIdentifier.getTableName(),
                    System.currentTimeMillis()))
        .stream()
        .map(TableBlocker::buildBlocker)
        .collect(Collectors.toList());
  }

  @Override
  public ServerTableIdentifier getServerTableIdentifier(TableIdentifier id) {
    return getAs(
        TableMetaMapper.class,
        mapper ->
            mapper.selectTableIdentifier(id.getCatalog(), id.getDatabase(), id.getTableName()));
  }

  @Override
  public TableRuntimeMeta getTableRuntimeMata(ServerTableIdentifier id) {
    return getAs(TableMetaMapper.class, mapper -> mapper.getTableRuntimeMeta(id.getId()));
  }

  @Override
  public Pair<List<TableOptimizingInfo>, Integer> queryTableOptimizingInfo(
      String optimizerGroup,
      @Nullable String fuzzyDbName,
      @Nullable String fuzzyTableName,
      @Nullable List<Integer> statusCodeFilters,
      int limit,
      int offset) {

    // page helper is 1-based
    int pageNumber = (offset / limit) + 1;
    int total = 0;
    List<TableRuntimeMeta> ret;
    try (Page<?> ignore = PageHelper.startPage(pageNumber, limit, true)) {
      ret =
          getAs(
              TableMetaMapper.class,
              mapper ->
                  mapper.selectTableRuntimesForOptimizerGroup(
                      optimizerGroup, fuzzyDbName, fuzzyTableName, statusCodeFilters));
      PageInfo<TableRuntimeMeta> pageInfo = new PageInfo<>(ret);
      total = (int) pageInfo.getTotal();
    }
    List<Long> processIds =
        ret.stream()
            .map(TableRuntimeMeta::getOptimizingProcessId)
            .filter(i -> i != -1)
            .collect(Collectors.toList());
    List<Long> tableIds =
        ret.stream().map(TableRuntimeMeta::getTableId).collect(Collectors.toList());

    List<OptimizingTaskMeta> taskMetas =
        getAs(
            OptimizingMapper.class,
            m -> {
              if (processIds.isEmpty()) {
                return Lists.newArrayList();
              } else {
                return m.selectOptimizeTaskMetas(processIds);
              }
            });
    Map<Long, List<OptimizingTaskMeta>> tableTaskMetaMap =
        taskMetas.stream()
            .collect(Collectors.groupingBy(OptimizingTaskMeta::getTableId, Collectors.toList()));

    // load quota info
    Map<Long, List<TaskRuntime.TaskQuota>> tableQuotaMap = getQuotaTime(tableIds);

    List<TableOptimizingInfo> infos =
        ret.stream()
            .map(
                meta -> {
                  List<OptimizingTaskMeta> tasks = tableTaskMetaMap.get(meta.getTableId());
                  List<TaskRuntime.TaskQuota> quotas = tableQuotaMap.get(meta.getTableId());
                  return OptimizingUtil.buildTableOptimizeInfo(meta, tasks, quotas);
                })
            .collect(Collectors.toList());
    return Pair.of(infos, total);
  }

  private Map<Long, List<TaskRuntime.TaskQuota>> getQuotaTime(List<Long> tableIds) {
    if (tableIds == null || tableIds.isEmpty()) {
      return Maps.newHashMap();
    }
    long calculatingEndTime = System.currentTimeMillis();
    long calculatingStartTime = calculatingEndTime - AmoroServiceConstants.QUOTA_LOOK_BACK_TIME;

    List<TaskRuntime.TaskQuota> quotas =
        getAs(
            OptimizingMapper.class,
            mapper -> mapper.selectTableQuotas(tableIds, calculatingStartTime));

    return quotas.stream()
        .collect(Collectors.groupingBy(TaskRuntime.TaskQuota::getTableId, Collectors.toList()));
  }
}
