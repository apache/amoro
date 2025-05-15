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
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.exception.ObjectNotExistsException;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.catalog.ExternalCatalog;
import org.apache.amoro.server.catalog.InternalCatalog;
import org.apache.amoro.server.catalog.ServerCatalog;
import org.apache.amoro.server.manager.MetricManager;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;
import org.apache.amoro.shade.guava32.com.google.common.base.Objects;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.shade.guava32.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.amoro.utils.TablePropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
import java.util.stream.Collectors;

public class DefaultTableService extends PersistentBase implements TableService {

  public static final Logger LOG = LoggerFactory.getLogger(DefaultTableService.class);
  private final long externalCatalogRefreshingInterval;

  private final Map<Long, DefaultTableRuntime> tableRuntimeMap = new ConcurrentHashMap<>();

  private final ScheduledExecutorService tableExplorerScheduler =
      Executors.newSingleThreadScheduledExecutor(
          new ThreadFactoryBuilder()
              .setNameFormat("table-explorer-scheduler-%d")
              .setDaemon(true)
              .build());
  private final CompletableFuture<Boolean> initialized = new CompletableFuture<>();
  private final Configurations serverConfiguration;
  private final CatalogManager catalogManager;
  private RuntimeHandlerChain headHandler;
  private ExecutorService tableExplorerExecutors;

  public DefaultTableService(Configurations configuration, CatalogManager catalogManager) {
    this.catalogManager = catalogManager;
    this.externalCatalogRefreshingInterval =
        configuration.get(AmoroManagementConf.REFRESH_EXTERNAL_CATALOGS_INTERVAL).toMillis();
    this.serverConfiguration = configuration;
  }

  @Override
  public void onTableCreated(InternalCatalog catalog, ServerTableIdentifier identifier) {
    triggerTableAdded(catalog, identifier);
  }

  @Override
  public void onTableDropped(InternalCatalog catalog, ServerTableIdentifier identifier) {
    Optional.ofNullable(tableRuntimeMap.get(identifier.getId()))
        .ifPresent(
            tableRuntime -> {
              try {
                if (headHandler != null) {
                  headHandler.fireTableRemoved(tableRuntime);
                }
                tableRuntime.dispose();
                tableRuntimeMap.remove(
                    identifier.getId()); // remove only after successful operation
              } catch (Exception e) {
                LOG.error(
                    "Error occurred while removing tableRuntime of table {}",
                    identifier.getId(),
                    e);
              }
            });
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
  public void handleTableChanged(
      DefaultTableRuntime tableRuntime, OptimizingStatus originalStatus) {
    if (headHandler != null) {
      headHandler.fireStatusChanged(tableRuntime, originalStatus);
    }
  }

  @Override
  public void handleTableChanged(
      DefaultTableRuntime tableRuntime, TableConfiguration originalConfig) {
    if (headHandler != null) {
      headHandler.fireConfigChanged(tableRuntime, originalConfig);
    }
  }

  @Override
  public void initialize() {
    checkNotStarted();

    List<TableRuntimeMeta> tableRuntimeMetaList =
        getAs(TableMetaMapper.class, TableMetaMapper::selectTableRuntimeMetas);
    List<DefaultTableRuntime> tableRuntimes = new ArrayList<>(tableRuntimeMetaList.size());
    tableRuntimeMetaList.forEach(
        tableRuntimeMeta -> {
          DefaultTableRuntime tableRuntime = new DefaultTableRuntime(tableRuntimeMeta, this);
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
        this::exploreTableRuntimes, 0, externalCatalogRefreshingInterval, TimeUnit.MILLISECONDS);
    initialized.complete(true);
  }

  private DefaultTableRuntime getAndCheckExist(ServerTableIdentifier tableIdentifier) {
    Preconditions.checkArgument(tableIdentifier != null, "tableIdentifier cannot be null");
    DefaultTableRuntime tableRuntime = getRuntime(tableIdentifier.getId());
    if (tableRuntime == null) {
      throw new ObjectNotExistsException(tableIdentifier);
    }
    return tableRuntime;
  }

  @Override
  public DefaultTableRuntime getRuntime(Long tableId) {
    checkStarted();
    return tableRuntimeMap.get(tableId);
  }

  @Override
  public boolean contains(Long tableId) {
    checkStarted();
    return tableRuntimeMap.containsKey(tableId);
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
    if (headHandler != null) {
      headHandler.dispose();
    }
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
    for (DefaultTableRuntime tableRuntime : tableRuntimeMap.values()) {
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
        .forEach(i -> triggerTableAdded(internalCatalog, i));

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
    DefaultTableRuntime tableRuntime =
        new DefaultTableRuntime(serverTableIdentifier, this, table.properties());
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
    DefaultTableRuntime existedTableRuntime = tableRuntimeMap.get(tableIdentifier.getId());
    try {
      doAsTransaction(
          () ->
              Optional.ofNullable(existedTableRuntime)
                  .ifPresent(
                      tableRuntime -> {
                        try {
                          if (headHandler != null) {
                            headHandler.fireTableRemoved(tableRuntime);
                          }
                          tableRuntime.dispose();
                          tableRuntimeMap.remove(
                              tableIdentifier.getId()); // remove only after successful operation
                        } catch (Exception e) {
                          LOG.error("Error occurred while disposing table {}", tableIdentifier, e);
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
