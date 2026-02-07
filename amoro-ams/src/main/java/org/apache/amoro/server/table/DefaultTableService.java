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

import org.apache.amoro.AmoroTable;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableIDWithFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.catalog.ExternalCatalog;
import org.apache.amoro.server.catalog.InternalCatalog;
import org.apache.amoro.server.catalog.ServerCatalog;
import org.apache.amoro.server.manager.MetricManager;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.server.persistence.TableRuntimeState;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.server.persistence.mapper.TableRuntimeMapper;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;
import org.apache.amoro.shade.guava32.com.google.common.base.Objects;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.shade.guava32.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.amoro.table.TableSummary;
import org.apache.amoro.utils.TablePropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultTableService extends PersistentBase implements TableService {

  public static final Logger LOG = LoggerFactory.getLogger(DefaultTableService.class);
  private final long externalCatalogRefreshingInterval;

  protected final Map<Long, TableRuntime> tableRuntimeMap = new ConcurrentHashMap<>();
  protected final TableRuntimeFactoryManager tableRuntimeFactoryManager;
  private final ScheduledExecutorService tableExplorerScheduler =
      Executors.newSingleThreadScheduledExecutor(
          new ThreadFactoryBuilder()
              .setNameFormat("table-explorer-scheduler-%d")
              .setDaemon(true)
              .build());
  private final CompletableFuture<Boolean> initialized = new CompletableFuture<>();
  private final Configurations serverConfiguration;
  private final CatalogManager catalogManager;
  private List<TableRuntimePlugin> tableRuntimePlugins;
  private ExecutorService tableExplorerExecutors;

  public DefaultTableService(
      Configurations configuration,
      CatalogManager catalogManager,
      TableRuntimeFactoryManager tableRuntimeFactoryManager) {
    this.catalogManager = catalogManager;
    this.externalCatalogRefreshingInterval =
        configuration.get(AmoroManagementConf.REFRESH_EXTERNAL_CATALOGS_INTERVAL).toMillis();
    this.serverConfiguration = configuration;
    this.tableRuntimeFactoryManager = tableRuntimeFactoryManager;
  }

  @Override
  public void initialize(List<TableRuntimePlugin> tableRuntimePlugins) {
    this.tableRuntimePlugins =
        tableRuntimePlugins == null ? new ArrayList<>() : tableRuntimePlugins;
    checkNotStarted();
    initTableRuntimes();
    initTableRuntimePlugins();
    initTableExplorer();
  }

  private void initTableRuntimes() {
    List<TableRuntimeMeta> tableRuntimeMetaList =
        getAs(TableRuntimeMapper.class, TableRuntimeMapper::selectAllRuntimes);
    Map<Long, ServerTableIdentifier> identifierMap =
        getAs(TableMetaMapper.class, TableMetaMapper::selectAllTableIdentifiers).stream()
            .collect(Collectors.toMap(ServerTableIdentifier::getId, Function.identity()));

    Map<Long, List<TableRuntimeState>> statesMap =
        getAs(TableRuntimeMapper.class, TableRuntimeMapper::selectAllStates).stream()
            .collect(
                Collectors.toMap(
                    TableRuntimeState::getTableId,
                    Lists::newArrayList,
                    (a, b) -> {
                      a.addAll(b);
                      return a;
                    }));

    for (TableRuntimeMeta tableRuntimeMeta : tableRuntimeMetaList) {
      ServerTableIdentifier identifier = identifierMap.get(tableRuntimeMeta.getTableId());
      if (identifier == null) {
        LOG.warn(
            "No available table identifier found for table runtime meta id={}",
            tableRuntimeMeta.getTableId());
        continue;
      }
      List<TableRuntimeState> states = statesMap.get(tableRuntimeMeta.getTableId());
      createTableRuntime(identifier, tableRuntimeMeta, states)
          .ifPresentOrElse(
              tableRuntime ->
                  tableRuntimeMap.put(tableRuntime.getTableIdentifier().getId(), tableRuntime),
              () -> LOG.warn("No available table runtime factory found for table {}", identifier));
    }
  }

  private void initTableRuntimePlugins() {
    List<TableRuntime> tableRuntimes = new ArrayList<>(tableRuntimeMap.values());
    tableRuntimePlugins.forEach(plugin -> plugin.initialize(tableRuntimes));
  }

  private Optional<TableRuntime> createTableRuntime(
      ServerTableIdentifier identifier,
      TableRuntimeMeta runtimeMeta,
      List<TableRuntimeState> restoredStates) {
    return tableRuntimeFactoryManager.installedPlugins().stream()
        .map(f -> f.accept(identifier, runtimeMeta.getTableConfig()))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst()
        .map(
            creator -> {
              DefaultTableRuntimeStore store =
                  new DefaultTableRuntimeStore(
                      identifier, runtimeMeta, creator.requiredStateKeys(), restoredStates);
              TableRuntime tableRuntime =
                  tableRuntimePlugins.stream()
                      .filter(plugin -> plugin.accept(identifier))
                      .findFirst()
                      .map(plugin -> plugin.createTableRuntime(creator, store))
                      .orElse(creator.create(store));
              tableRuntime.registerMetric(MetricManager.getInstance().getGlobalRegistry());
              store.setTableRuntime(tableRuntime);
              return tableRuntime;
            });
  }

  private void initTableExplorer() {
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
    initialized.complete(true);
    tableExplorerScheduler.scheduleAtFixedRate(
        this::exploreTableRuntimes, 0, externalCatalogRefreshingInterval, TimeUnit.MILLISECONDS);
  }

  @Override
  public TableRuntime getRuntime(Long tableId) {
    checkStarted();
    return tableRuntimeMap.get(tableId);
  }

  @VisibleForTesting
  public void setRuntime(TableRuntime tableRuntime) {
    checkStarted();
    tableRuntimeMap.put(tableRuntime.getTableIdentifier().getId(), tableRuntime);
  }

  @Override
  public AmoroTable<?> loadTable(ServerTableIdentifier identifier) {
    return catalogManager.loadTable(identifier.getIdentifier());
  }

  @Override
  public void dispose() {
    tableExplorerScheduler.shutdown();
    if (tableExplorerExecutors != null) {
      tableExplorerExecutors.shutdown();
    }
    tableRuntimePlugins.forEach(TableRuntimePlugin::dispose);
    tableRuntimeMap.values().forEach(TableRuntime::unregisterMetric);
  }

  @Override
  public void addTable(TableMetadata tableMetadata) {
    triggerTableAdded(tableMetadata.getTableIdentifier());
  }

  @Override
  public void removeTable(ServerTableIdentifier tableIdentifier) {
    disposeTable(tableIdentifier);
  }

  @VisibleForTesting
  void exploreTableRuntimes() {
    if (!initialized.isDone()) {
      throw new IllegalStateException("TableService is not initialized");
    }
    long start = System.currentTimeMillis();
    List<ServerCatalog> externalCatalogs = catalogManager.getServerCatalogs();
    List<String> externalCatalogNames =
        externalCatalogs.stream().map(ServerCatalog::name).collect(Collectors.toList());
    LOG.info("Syncing server catalogs: {}", String.join(",", externalCatalogNames));
    for (ServerCatalog serverCatalog : externalCatalogs) {
      try {
        if (serverCatalog.isInternal()) {
          exploreInternalCatalog((InternalCatalog) serverCatalog);
        } else {
          exploreExternalCatalog((ExternalCatalog) serverCatalog);
        }
      } catch (Throwable e) {
        LOG.error(
            "TableExplorer error when explore table runtimes for catalog:{}",
            serverCatalog.name(),
            e);
      }
    }

    // Clear TableRuntime objects that do not correspond to a catalog.
    // This scenario is mainly due to the fact that TableRuntime objects were not cleaned up in a
    // timely manner during the process of dropping the catalog due to concurrency considerations.
    // It is permissible to have some erroneous states in the middle, as long as the final data is
    // consistent.
    Set<String> catalogNames =
        catalogManager.listCatalogMetas().stream()
            .map(CatalogMeta::getCatalogName)
            .collect(Collectors.toSet());
    for (TableRuntime tableRuntime : tableRuntimeMap.values()) {
      if (!catalogNames.contains(tableRuntime.getTableIdentifier().getCatalog())) {
        disposeTable(tableRuntime.getTableIdentifier());
      }
    }

    long end = System.currentTimeMillis();
    LOG.info("Syncing external catalogs took {} ms.", end - start);
  }

  @VisibleForTesting
  public void exploreExternalCatalog(ExternalCatalog externalCatalog) {
    final List<CompletableFuture<Set<TableIdentity>>> tableIdentifiersFutures =
        Lists.newArrayList();
    externalCatalog
        .listDatabases()
        .forEach(
            database -> {
              try {
                tableIdentifiersFutures.add(
                    CompletableFuture.supplyAsync(
                            () ->
                                externalCatalog.listTables(database).stream()
                                    .map(TableIdentity::new)
                                    .collect(Collectors.toSet()),
                            tableExplorerExecutors)
                        .exceptionally(
                            ex -> {
                              LOG.error(
                                  "TableExplorer list tables in database {} error", database, ex);
                              throw new RuntimeException(ex);
                            }));
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
                                "TableExplorer sync table {} error", tableIdentity.toString(), e);
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
  }

  private void exploreInternalCatalog(InternalCatalog internalCatalog) {
    LOG.info("Start explore internal catalog {}", internalCatalog.name());
    List<ServerTableIdentifier> identifiers =
        getAs(
            TableMetaMapper.class, m -> m.selectTableIdentifiersByCatalog(internalCatalog.name()));
    AtomicInteger addedCount = new AtomicInteger();
    identifiers.stream()
        .filter(i -> !tableRuntimeMap.containsKey(i.getId()))
        .peek(
            i ->
                LOG.info(
                    "Found new table {} in internal catalog {}, create table runtime for it.",
                    i,
                    internalCatalog.name()))
        .peek(i -> addedCount.incrementAndGet())
        .forEach(this::triggerTableAdded);

    Set<Long> tableIds =
        identifiers.stream().map(ServerTableIdentifier::getId).collect(Collectors.toSet());
    Set<ServerTableIdentifier> tablesToBeDisposed = Sets.newHashSet();
    tableRuntimeMap.forEach(
        (id, tableRuntime) -> {
          if (tableRuntime.getTableIdentifier().getCatalog().equals(internalCatalog.name())) {
            if (!tableIds.contains(id)) {
              LOG.info(
                  "Found table {} in internal catalog {} is removed, dispose table runtime for it.",
                  id,
                  internalCatalog.name());
              tablesToBeDisposed.add(tableRuntime.getTableIdentifier());
            }
          }
        });
    tablesToBeDisposed.forEach(this::disposeTable);
    LOG.info(
        "Explore internal catalog {} finished, {} tables are added, {} tables are disposed.",
        internalCatalog.name(),
        addedCount.get(),
        tablesToBeDisposed.size());
  }

  protected void checkStarted() {
    try {
      initialized.get();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected void checkNotStarted() {
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
            tableRuntimeAdded.set(triggerTableAdded(tableIdentifier));
          });
    } catch (Throwable t) {
      if (tableRuntimeAdded.get()) {
        ServerTableIdentifier tableIdentifier =
            externalCatalog.getServerTableIdentifier(
                tableIdentity.getDatabase(), tableIdentity.getTableName());
        if (tableIdentifier != null) {
          tableRuntimeMap.remove(tableIdentifier.getId());
        }
      }
      throw t;
    }
  }

  protected boolean triggerTableAdded(ServerTableIdentifier serverTableIdentifier) {
    AmoroTable<?> table = loadTable(serverTableIdentifier);
    if (TableFormat.ICEBERG.equals(table.format())) {
      if (TablePropertyUtil.isMixedTableStore(table.properties())) {
        return false;
      }
    }

    Map<String, String> properties = table.properties();
    TableRuntimeMeta meta = new TableRuntimeMeta();
    meta.setTableId(serverTableIdentifier.getId());
    meta.setTableConfig(properties);
    TableConfiguration configuration = TableConfigurations.parseTableConfig(properties);
    meta.setStatusCode(OptimizingStatus.IDLE.getCode());
    meta.setGroupName(configuration.getOptimizingConfig().getOptimizerGroup());
    meta.setTableSummary(new TableSummary());
    doAs(TableRuntimeMapper.class, mapper -> mapper.insertRuntime(meta));

    Optional<TableRuntime> tableRuntimeOpt =
        createTableRuntime(serverTableIdentifier, meta, Collections.emptyList());
    if (tableRuntimeOpt.isPresent()) {
      TableRuntime tableRuntime = tableRuntimeOpt.get();
      tableRuntimeMap.put(serverTableIdentifier.getId(), tableRuntime);
      tableRuntime.registerMetric(MetricManager.getInstance().getGlobalRegistry());
      tableRuntimePlugins.forEach(plugin -> plugin.onTableCreated(table, tableRuntime));
      return true;
    } else {
      LOG.warn("No available table runtime factory found for table {}", serverTableIdentifier);
      return false;
    }
  }

  @VisibleForTesting
  public void disposeTable(ServerTableIdentifier tableIdentifier) {
    TableRuntime existedTableRuntime = tableRuntimeMap.get(tableIdentifier.getId());
    try {
      doAsTransaction(
          () ->
              Optional.ofNullable(existedTableRuntime)
                  .ifPresent(
                      tableRuntime -> {
                        try {
                          tableRuntimePlugins.forEach(
                              plugin -> plugin.onTableDropped(tableRuntime));
                          tableRuntime.dispose();
                          tableRuntimeMap.remove(
                              tableIdentifier.getId()); // remove only after successful operation
                        } catch (Exception e) {
                          LOG.error("Error occurred while disposing table {}", tableIdentifier, e);
                          throw e;
                        }
                      }),
          () ->
              doAs(
                  TableMetaMapper.class,
                  mapper ->
                      mapper.deleteTableIdByName(
                          tableIdentifier.getCatalog(),
                          tableIdentifier.getDatabase(),
                          tableIdentifier.getTableName())));
    } catch (Throwable t) {
      tableRuntimeMap.putIfAbsent(tableIdentifier.getId(), existedTableRuntime);
      throw t;
    }
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
