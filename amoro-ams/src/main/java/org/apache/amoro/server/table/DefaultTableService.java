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
import org.apache.amoro.AmoroTable;
import org.apache.amoro.NoSuchTableException;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableIDWithFormat;
import org.apache.amoro.api.BlockableOperation;
import org.apache.amoro.api.Blocker;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.api.TableIdentifier;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.exception.AlreadyExistsException;
import org.apache.amoro.exception.BlockerConflictException;
import org.apache.amoro.exception.IllegalMetadataException;
import org.apache.amoro.exception.ObjectNotExistsException;
import org.apache.amoro.exception.PersistenceException;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.catalog.CatalogBuilder;
import org.apache.amoro.server.catalog.ExternalCatalog;
import org.apache.amoro.server.catalog.InternalCatalog;
import org.apache.amoro.server.catalog.ServerCatalog;
import org.apache.amoro.server.manager.MetricManager;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.persistence.StatedPersistentBase;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.server.persistence.mapper.CatalogMetaMapper;
import org.apache.amoro.server.persistence.mapper.TableBlockerMapper;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.server.table.blocker.TableBlocker;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;
import org.apache.amoro.shade.guava32.com.google.common.base.Objects;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.shade.guava32.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.amoro.utils.TablePropertyUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class DefaultTableService extends StatedPersistentBase implements TableService {

  public static final Logger LOG = LoggerFactory.getLogger(DefaultTableService.class);
  private static final int TABLE_BLOCKER_RETRY = 3;
  private final long externalCatalogRefreshingInterval;
  private final long blockerTimeout;
  private final Map<String, InternalCatalog> internalCatalogMap = new ConcurrentHashMap<>();
  private final Map<String, ExternalCatalog> externalCatalogMap = new ConcurrentHashMap<>();

  private final Map<Long, TableRuntime> tableRuntimeMap = new ConcurrentHashMap<>();

  private final ScheduledExecutorService tableExplorerScheduler =
      Executors.newSingleThreadScheduledExecutor(
          new ThreadFactoryBuilder()
              .setNameFormat("table-explorer-scheduler-%d")
              .setDaemon(true)
              .build());
  private final CompletableFuture<Boolean> initialized = new CompletableFuture<>();
  private final Configurations serverConfiguration;
  private RuntimeHandlerChain headHandler;
  private ExecutorService tableExplorerExecutors;

  public DefaultTableService(Configurations configuration) {
    this.externalCatalogRefreshingInterval =
        configuration.getLong(AmoroManagementConf.REFRESH_EXTERNAL_CATALOGS_INTERVAL);
    this.blockerTimeout = configuration.getLong(AmoroManagementConf.BLOCKER_TIMEOUT);
    this.serverConfiguration = configuration;
  }

  @Override
  public List<CatalogMeta> listCatalogMetas() {
    checkStarted();
    List<CatalogMeta> catalogs =
        internalCatalogMap.values().stream()
            .map(ServerCatalog::getMetadata)
            .collect(Collectors.toList());
    catalogs.addAll(
        externalCatalogMap.values().stream()
            .map(ServerCatalog::getMetadata)
            .collect(Collectors.toList()));
    return catalogs;
  }

  @Override
  public CatalogMeta getCatalogMeta(String catalogName) {
    checkStarted();
    ServerCatalog catalog = getServerCatalog(catalogName);
    return catalog.getMetadata();
  }

  @Override
  public boolean catalogExist(String catalogName) {
    checkStarted();
    return internalCatalogMap.containsKey(catalogName)
        || externalCatalogMap.containsKey(catalogName);
  }

  @Override
  public ServerCatalog getServerCatalog(String catalogName) {
    ServerCatalog catalog =
        Optional.ofNullable((ServerCatalog) internalCatalogMap.get(catalogName))
            .orElse(externalCatalogMap.get(catalogName));
    return Optional.ofNullable(catalog)
        .orElseThrow(() -> new ObjectNotExistsException("Catalog " + catalogName));
  }

  @Override
  public void createCatalog(CatalogMeta catalogMeta) {
    checkStarted();
    if (catalogExist(catalogMeta.getCatalogName())) {
      throw new AlreadyExistsException("Catalog " + catalogMeta.getCatalogName());
    }
    doAsTransaction(
        () -> doAs(CatalogMetaMapper.class, mapper -> mapper.insertCatalog(catalogMeta)),
        () -> initServerCatalog(catalogMeta));
  }

  private void initServerCatalog(CatalogMeta catalogMeta) {
    ServerCatalog catalog = CatalogBuilder.buildServerCatalog(catalogMeta, serverConfiguration);
    if (catalog instanceof InternalCatalog) {
      internalCatalogMap.put(catalogMeta.getCatalogName(), (InternalCatalog) catalog);
    } else {
      externalCatalogMap.put(catalogMeta.getCatalogName(), (ExternalCatalog) catalog);
    }
  }

  @Override
  public void dropCatalog(String catalogName) {
    checkStarted();
    ServerCatalog serverCatalog = getServerCatalog(catalogName);
    if (serverCatalog == null) {
      throw new ObjectNotExistsException("Catalog " + catalogName);
    }

    // TableRuntime cleanup is responsibility by exploreExternalCatalog method
    serverCatalog.dispose();
    internalCatalogMap.remove(catalogName);
    externalCatalogMap.remove(catalogName);
  }

  @Override
  public void updateCatalog(CatalogMeta catalogMeta) {
    checkStarted();
    ServerCatalog catalog = getServerCatalog(catalogMeta.getCatalogName());
    validateCatalogUpdate(catalog.getMetadata(), catalogMeta);
    doAs(CatalogMetaMapper.class, mapper -> mapper.updateCatalog(catalogMeta));
    catalog.updateMetadata(catalogMeta);
  }

  @Override
  public void dropTableMetadata(TableIdentifier tableIdentifier, boolean deleteData) {
    checkStarted();
    if (StringUtils.isBlank(tableIdentifier.getTableName())) {
      throw new IllegalMetadataException("table name is blank");
    }
    if (StringUtils.isBlank(tableIdentifier.getCatalog())) {
      throw new IllegalMetadataException("catalog is blank");
    }
    if (StringUtils.isBlank(tableIdentifier.getDatabase())) {
      throw new IllegalMetadataException("database is blank");
    }

    InternalCatalog internalCatalog = getInternalCatalog(tableIdentifier.getCatalog());
    String database = tableIdentifier.getDatabase();
    String table = tableIdentifier.getTableName();
    if (!internalCatalog.tableExists(database, table)) {
      throw new ObjectNotExistsException(tableIdentifier);
    }

    ServerTableIdentifier serverTableIdentifier = internalCatalog.dropTable(database, table);
    Optional.ofNullable(tableRuntimeMap.remove(serverTableIdentifier.getId()))
        .ifPresent(
            tableRuntime -> {
              if (headHandler != null) {
                headHandler.fireTableRemoved(tableRuntime);
              }
              tableRuntime.dispose();
            });
  }

  @Override
  public void createTable(String catalogName, TableMetadata tableMetadata) {
    checkStarted();
    InternalCatalog catalog = getInternalCatalog(catalogName);
    String database = tableMetadata.getTableIdentifier().getDatabase();
    String table = tableMetadata.getTableIdentifier().getTableName();
    if (catalog.tableExists(database, table)) {
      throw new AlreadyExistsException(tableMetadata.getTableIdentifier().getIdentifier());
    }

    TableMetadata metadata = catalog.createTable(tableMetadata);

    triggerTableAdded(catalog, metadata.getTableIdentifier());
  }

  @Override
  public List<ServerTableIdentifier> listManagedTables() {
    checkStarted();
    return getAs(TableMetaMapper.class, TableMetaMapper::selectAllTableIdentifiers);
  }

  @Override
  public AmoroTable<?> loadTable(ServerTableIdentifier tableIdentifier) {
    checkStarted();
    return getServerCatalog(tableIdentifier.getCatalog())
        .loadTable(tableIdentifier.getDatabase(), tableIdentifier.getTableName());
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
  public Pair<List<TableRuntimeMeta>, Integer> getTableRuntimes(
      String optimizerGroup,
      @Nullable String fuzzyDbName,
      @Nullable String fuzzyTableName,
      @Nullable List<Integer> statusCodeFilters,
      int limit,
      int offset) {
    checkStarted();

    // page helper is 1-based
    int pageNumber = (offset / limit) + 1;

    try (Page<?> ignore = PageHelper.startPage(pageNumber, limit, true)) {
      int total = 0;
      List<TableRuntimeMeta> ret =
          getAs(
              TableMetaMapper.class,
              mapper ->
                  mapper.selectTableRuntimesForOptimizerGroup(
                      optimizerGroup, fuzzyDbName, fuzzyTableName, statusCodeFilters));
      PageInfo<TableRuntimeMeta> pageInfo = new PageInfo<>(ret);
      total = (int) pageInfo.getTotal();
      return Pair.of(ret, total);
    }
  }

  public InternalCatalog getInternalCatalog(String catalogName) {
    return Optional.ofNullable(internalCatalogMap.get(catalogName))
        .orElseThrow(() -> new ObjectNotExistsException("Catalog " + catalogName));
  }

  @Override
  public void addHandlerChain(RuntimeHandlerChain handler) {
    checkNotStarted();
    if (headHandler == null) {
      headHandler = handler;
    } else {
      headHandler.appendNext(handler);
    }
  }

  @Override
  public void handleTableChanged(TableRuntime tableRuntime, OptimizingStatus originalStatus) {
    if (headHandler != null) {
      headHandler.fireStatusChanged(tableRuntime, originalStatus);
    }
  }

  @Override
  public void handleTableChanged(TableRuntime tableRuntime, TableConfiguration originalConfig) {
    if (headHandler != null) {
      headHandler.fireConfigChanged(tableRuntime, originalConfig);
    }
  }

  @Override
  public void initialize() {
    checkNotStarted();
    List<CatalogMeta> catalogMetas = getAs(CatalogMetaMapper.class, CatalogMetaMapper::getCatalogs);
    catalogMetas.forEach(this::initServerCatalog);

    List<TableRuntimeMeta> tableRuntimeMetaList =
        getAs(TableMetaMapper.class, TableMetaMapper::selectTableRuntimeMetas);
    List<TableRuntime> tableRuntimes = new ArrayList<>(tableRuntimeMetaList.size());
    tableRuntimeMetaList.forEach(
        tableRuntimeMeta -> {
          TableRuntime tableRuntime = new TableRuntime(tableRuntimeMeta, this);
          tableRuntimeMap.put(tableRuntimeMeta.getTableId(), tableRuntime);
          tableRuntime.registerMetric(MetricManager.getInstance().getGlobalRegistry());
          tableRuntimes.add(tableRuntime);
        });

    if (headHandler != null) {
      headHandler.initialize(tableRuntimes);
    }
    if (tableExplorerExecutors == null) {
      int threadCount =
          serverConfiguration.getInteger(
              AmoroManagementConf.REFRESH_EXTERNAL_CATALOGS_THREAD_COUNT);
      int queueSize =
          serverConfiguration.getInteger(AmoroManagementConf.REFRESH_EXTERNAL_CATALOGS_QUEUE_SIZE);
      tableExplorerExecutors =
          new ThreadPoolExecutor(
              threadCount,
              threadCount,
              0,
              TimeUnit.SECONDS,
              new LinkedBlockingQueue<>(queueSize),
              new ThreadFactoryBuilder()
                  .setNameFormat("table-explorer-executor-%d")
                  .setDaemon(true)
                  .build());
    }
    tableExplorerScheduler.scheduleAtFixedRate(
        this::exploreExternalCatalog, 0, externalCatalogRefreshingInterval, TimeUnit.MILLISECONDS);
    initialized.complete(true);
  }

  private TableRuntime getAndCheckExist(ServerTableIdentifier tableIdentifier) {
    Preconditions.checkArgument(tableIdentifier != null, "tableIdentifier cannot be null");
    TableRuntime tableRuntime = getRuntime(tableIdentifier.getId());
    if (tableRuntime == null) {
      throw new ObjectNotExistsException(tableIdentifier);
    }
    return tableRuntime;
  }

  @Override
  public ServerTableIdentifier getServerTableIdentifier(TableIdentifier id) {
    return getAs(
        TableMetaMapper.class,
        mapper ->
            mapper.selectTableIdentifier(id.getCatalog(), id.getDatabase(), id.getTableName()));
  }

  private ServerTableIdentifier getOrSyncServerTableIdentifier(TableIdentifier id) {
    ServerTableIdentifier serverTableIdentifier = getServerTableIdentifier(id);
    if (serverTableIdentifier != null) {
      return serverTableIdentifier;
    }
    ServerCatalog serverCatalog = getServerCatalog(id.getCatalog());
    if (serverCatalog instanceof InternalCatalog) {
      return null;
    }
    try {
      AmoroTable<?> table = serverCatalog.loadTable(id.database, id.getTableName());
      TableIdentity identity =
          new TableIdentity(id.getDatabase(), id.getTableName(), table.format());
      syncTable((ExternalCatalog) serverCatalog, identity);
      return getServerTableIdentifier(id);
    } catch (NoSuchTableException e) {
      return null;
    }
  }

  @Override
  public TableRuntime getRuntime(Long tableId) {
    checkStarted();
    return tableRuntimeMap.get(tableId);
  }

  @Override
  public boolean contains(Long tableId) {
    checkStarted();
    return tableRuntimeMap.containsKey(tableId);
  }

  public void dispose() {
    tableExplorerScheduler.shutdown();
    if (tableExplorerExecutors != null) {
      tableExplorerExecutors.shutdown();
    }
    if (headHandler != null) {
      headHandler.dispose();
    }
  }

  @VisibleForTesting
  void exploreExternalCatalog() {
    if (!initialized.isDone()) {
      throw new IllegalStateException("TableService is not initialized");
    }
    long start = System.currentTimeMillis();
    LOG.info("Syncing external catalogs: {}", String.join(",", externalCatalogMap.keySet()));
    for (ExternalCatalog externalCatalog : externalCatalogMap.values()) {
      try {
        final List<CompletableFuture<Set<TableIdentity>>> tableIdentifiersFutures =
            Lists.newArrayList();
        externalCatalog
            .listDatabases()
            .forEach(
                database -> {
                  try {
                    tableIdentifiersFutures.add(
                        CompletableFuture.supplyAsync(
                            () -> {
                              try {
                                return externalCatalog.listTables(database).stream()
                                    .map(TableIdentity::new)
                                    .collect(Collectors.toSet());
                              } catch (Exception e) {
                                LOG.error(
                                    "TableExplorer list tables in database {} error", database, e);
                                return new HashSet<>();
                              }
                            },
                            tableExplorerExecutors));
                  } catch (RejectedExecutionException e) {
                    LOG.error(
                        "The queue of table explorer is full, please increase the queue size or thread count.");
                  }
                });
        Set<TableIdentity> tableIdentifiers =
            tableIdentifiersFutures.stream()
                .map(CompletableFuture::join)
                .reduce(
                    (a, b) -> {
                      a.addAll(b);
                      return a;
                    })
                .orElse(Sets.newHashSet());
        LOG.info(
            "Loaded {} tables from external catalog {}.",
            tableIdentifiers.size(),
            externalCatalog.name());
        Map<TableIdentity, ServerTableIdentifier> serverTableIdentifiers =
            getAs(
                    TableMetaMapper.class,
                    mapper -> mapper.selectTableIdentifiersByCatalog(externalCatalog.name()))
                .stream()
                .collect(Collectors.toMap(TableIdentity::new, tableIdentifier -> tableIdentifier));
        LOG.info(
            "Loaded {} tables from Amoro server catalog {}.",
            serverTableIdentifiers.size(),
            externalCatalog.name());
        final List<CompletableFuture<Void>> taskFutures = Lists.newArrayList();
        Sets.difference(tableIdentifiers, serverTableIdentifiers.keySet())
            .forEach(
                tableIdentity -> {
                  try {
                    taskFutures.add(
                        CompletableFuture.runAsync(
                            () -> {
                              try {
                                syncTable(externalCatalog, tableIdentity);
                              } catch (Exception e) {
                                LOG.error(
                                    "TableExplorer sync table {} error",
                                    tableIdentity.toString(),
                                    e);
                              }
                            },
                            tableExplorerExecutors));
                  } catch (RejectedExecutionException e) {
                    LOG.error(
                        "The queue of table explorer is full, please increase the queue size or thread count.");
                  }
                });
        Sets.difference(serverTableIdentifiers.keySet(), tableIdentifiers)
            .forEach(
                tableIdentity -> {
                  try {
                    taskFutures.add(
                        CompletableFuture.runAsync(
                            () -> {
                              try {
                                disposeTable(serverTableIdentifiers.get(tableIdentity));
                              } catch (Exception e) {
                                LOG.error(
                                    "TableExplorer dispose table {} error",
                                    tableIdentity.toString(),
                                    e);
                              }
                            },
                            tableExplorerExecutors));
                  } catch (RejectedExecutionException e) {
                    LOG.error(
                        "The queue of table explorer is full, please increase the queue size or thread count.");
                  }
                });
        taskFutures.forEach(CompletableFuture::join);
      } catch (Throwable e) {
        LOG.error("TableExplorer error", e);
      }
    }

    // Clear TableRuntime objects that do not correspond to a catalog.
    // This scenario is mainly due to the fact that TableRuntime objects were not cleaned up in a
    // timely manner during the process of dropping the catalog due to concurrency considerations.
    // It is permissible to have some erroneous states in the middle, as long as the final data is
    // consistent.
    Set<String> catalogNames =
        listCatalogMetas().stream().map(CatalogMeta::getCatalogName).collect(Collectors.toSet());
    for (TableRuntime tableRuntime : tableRuntimeMap.values()) {
      if (!catalogNames.contains(tableRuntime.getTableIdentifier().getCatalog())) {
        disposeTable(tableRuntime.getTableIdentifier());
      }
    }

    long end = System.currentTimeMillis();
    LOG.info("Syncing external catalogs took {} ms.", end - start);
  }

  private void validateCatalogUpdate(CatalogMeta oldMeta, CatalogMeta newMeta) {
    if (!oldMeta.getCatalogType().equals(newMeta.getCatalogType())) {
      throw new IllegalMetadataException("Cannot update catalog type");
    }
  }

  private void checkStarted() {
    try {
      initialized.get();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void checkNotStarted() {
    if (initialized.isDone()) {
      throw new IllegalStateException("Table service has started.");
    }
  }

  private void syncTable(ExternalCatalog externalCatalog, TableIdentity tableIdentity) {
    AtomicBoolean tableRuntimeAdded = new AtomicBoolean(false);
    try {
      doAsTransaction(
          () ->
              externalCatalog.syncTable(
                  tableIdentity.getDatabase(),
                  tableIdentity.getTableName(),
                  tableIdentity.getFormat()),
          () -> {
            ServerTableIdentifier tableIdentifier =
                externalCatalog.getServerTableIdentifier(
                    tableIdentity.getDatabase(), tableIdentity.getTableName());
            tableRuntimeAdded.set(triggerTableAdded(externalCatalog, tableIdentifier));
          });
    } catch (Throwable t) {
      if (tableRuntimeAdded.get()) {
        revertTableRuntimeAdded(externalCatalog, tableIdentity);
      }
      throw t;
    }
  }

  private boolean triggerTableAdded(
      ServerCatalog catalog, ServerTableIdentifier serverTableIdentifier) {
    AmoroTable<?> table =
        catalog.loadTable(
            serverTableIdentifier.getDatabase(), serverTableIdentifier.getTableName());
    if (TableFormat.ICEBERG.equals(table.format())) {
      if (TablePropertyUtil.isMixedTableStore(table.properties())) {
        return false;
      }
    }
    TableRuntime tableRuntime = new TableRuntime(serverTableIdentifier, this, table.properties());
    tableRuntimeMap.put(serverTableIdentifier.getId(), tableRuntime);
    tableRuntime.registerMetric(MetricManager.getInstance().getGlobalRegistry());
    if (headHandler != null) {
      headHandler.fireTableAdded(table, tableRuntime);
    }
    return true;
  }

  private void revertTableRuntimeAdded(
      ExternalCatalog externalCatalog, TableIdentity tableIdentity) {
    ServerTableIdentifier tableIdentifier =
        externalCatalog.getServerTableIdentifier(
            tableIdentity.getDatabase(), tableIdentity.getTableName());
    if (tableIdentifier != null) {
      tableRuntimeMap.remove(tableIdentifier.getId());
    }
  }

  private void disposeTable(ServerTableIdentifier tableIdentifier) {
    doAs(
        TableMetaMapper.class,
        mapper ->
            mapper.deleteTableIdByName(
                tableIdentifier.getCatalog(),
                tableIdentifier.getDatabase(),
                tableIdentifier.getTableName()));
    Optional.ofNullable(tableRuntimeMap.remove(tableIdentifier.getId()))
        .ifPresent(
            tableRuntime -> {
              if (headHandler != null) {
                headHandler.fireTableRemoved(tableRuntime);
              }
              tableRuntime.dispose();
            });
  }

  private static class TableIdentity {

    private final String database;
    private final String tableName;

    private final TableFormat format;

    protected TableIdentity(TableIDWithFormat idWithFormat) {
      this.database = idWithFormat.getIdentifier().getDatabase();
      this.tableName = idWithFormat.getIdentifier().getTableName();
      this.format = idWithFormat.getTableFormat();
    }

    protected TableIdentity(ServerTableIdentifier serverTableIdentifier) {
      this.database = serverTableIdentifier.getDatabase();
      this.tableName = serverTableIdentifier.getTableName();
      this.format = serverTableIdentifier.getFormat();
    }

    protected TableIdentity(String database, String tableName, TableFormat format) {
      this.database = database;
      this.tableName = tableName;
      this.format = format;
    }

    public String getDatabase() {
      return database;
    }

    public String getTableName() {
      return tableName;
    }

    public TableFormat getFormat() {
      return format;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      TableIdentity that = (TableIdentity) o;
      return Objects.equal(database, that.database) && Objects.equal(tableName, that.tableName);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(database, tableName);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("database", database)
          .add("tableName", tableName)
          .toString();
    }
  }
}
