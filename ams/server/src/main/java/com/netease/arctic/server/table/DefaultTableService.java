package com.netease.arctic.server.table;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.netease.arctic.AmoroTable;
import com.netease.arctic.TableIDWithFormat;
import com.netease.arctic.ams.api.BlockableOperation;
import com.netease.arctic.ams.api.Blocker;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.server.ArcticManagementConf;
import com.netease.arctic.server.catalog.CatalogBuilder;
import com.netease.arctic.server.catalog.ExternalCatalog;
import com.netease.arctic.server.catalog.InternalCatalog;
import com.netease.arctic.server.catalog.ServerCatalog;
import com.netease.arctic.server.exception.AlreadyExistsException;
import com.netease.arctic.server.exception.IllegalMetadataException;
import com.netease.arctic.server.exception.ObjectNotExistsException;
import com.netease.arctic.server.optimizing.OptimizingStatus;
import com.netease.arctic.server.persistence.StatedPersistentBase;
import com.netease.arctic.server.persistence.mapper.CatalogMetaMapper;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;
import com.netease.arctic.server.table.blocker.TableBlocker;
import com.netease.arctic.server.utils.Configurations;
import org.apache.commons.lang.StringUtils;
import org.apache.iceberg.relocated.com.google.common.annotations.VisibleForTesting;
import org.apache.iceberg.relocated.com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.stream.Collectors;

public class DefaultTableService extends StatedPersistentBase implements TableService {

  public static final Logger LOG = LoggerFactory.getLogger(DefaultTableService.class);
  private final long externalCatalogRefreshingInterval;
  private final long blockerTimeout;
  private final Map<String, InternalCatalog> internalCatalogMap = new ConcurrentHashMap<>();
  private final Map<String, ExternalCatalog> externalCatalogMap = new ConcurrentHashMap<>();
  @StateField
  private final Map<ServerTableIdentifier, TableRuntime> tableRuntimeMap = new ConcurrentHashMap<>();
  private RuntimeHandlerChain headHandler;

  private final ScheduledExecutorService tableExplorerScheduler = Executors.newSingleThreadScheduledExecutor(
      new ThreadFactoryBuilder()
          .setNameFormat("table-explorer-scheduler")
          .setDaemon(true)
          .build()
  );

  private ExecutorService tableExplorerExecutors;

  private final CompletableFuture<Boolean> initialized = new CompletableFuture<>();
  private final Configurations serverConfiguration;

  public DefaultTableService(Configurations configuration) {
    this.externalCatalogRefreshingInterval =
        configuration.getLong(ArcticManagementConf.REFRESH_EXTERNAL_CATALOGS_INTERVAL);
    this.blockerTimeout = configuration.getLong(ArcticManagementConf.BLOCKER_TIMEOUT);
    this.serverConfiguration = configuration;
  }

  @Override
  public List<CatalogMeta> listCatalogMetas() {
    checkStarted();
    List<CatalogMeta> catalogs = internalCatalogMap.values().stream()
        .map(ServerCatalog::getMetadata).collect(Collectors.toList());
    catalogs.addAll(externalCatalogMap.values().stream()
        .map(ServerCatalog::getMetadata).collect(Collectors.toList()));
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
    return internalCatalogMap.containsKey(catalogName) || externalCatalogMap.containsKey(catalogName);
  }

  @Override
  public ServerCatalog getServerCatalog(String catalogName) {
    ServerCatalog catalog = Optional.ofNullable((ServerCatalog) internalCatalogMap.get(catalogName))
        .orElse(externalCatalogMap.get(catalogName));
    return Optional.ofNullable(catalog).orElseThrow(() -> new ObjectNotExistsException("Catalog " + catalogName));
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
    doAsExisted(
        CatalogMetaMapper.class,
        mapper -> mapper.deleteCatalog(catalogName),
        () -> new IllegalMetadataException("Catalog " + catalogName + " has more than one database or table"));
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
  public TableMetadata loadTableMetadata(TableIdentifier tableIdentifier) {
    validateTableExists(tableIdentifier);
    InternalCatalog internalCatalog = getInternalCatalog(tableIdentifier.getCatalog());
    return internalCatalog.loadTableMetadata(tableIdentifier.getDatabase(), tableIdentifier.getTableName());
  }

  @Override
  public void dropTableMetadata(TableIdentifier tableIdentifier, boolean deleteData) {
    checkStarted();
    validateTableExists(tableIdentifier);
    ServerTableIdentifier serverTableIdentifier = getInternalCatalog(tableIdentifier.getCatalog())
        .dropTable(tableIdentifier.getDatabase(), tableIdentifier.getTableName());
    Optional.ofNullable(tableRuntimeMap.remove(serverTableIdentifier))
        .ifPresent(tableRuntime -> {
          if (headHandler != null) {
            headHandler.fireTableRemoved(tableRuntime);
          }
          tableRuntime.dispose();
        });
  }

  @Override
  public void createTable(String catalogName, TableMetadata tableMetadata) {
    checkStarted();
    validateTableNotExists(tableMetadata.getTableIdentifier().getIdentifier());

    InternalCatalog catalog = getInternalCatalog(catalogName);
    ServerTableIdentifier tableIdentifier = catalog.createTable(tableMetadata);
    AmoroTable<?> table = catalog.loadTable(tableIdentifier.getDatabase(), tableIdentifier.getTableName());
    TableRuntime tableRuntime = new TableRuntime(tableIdentifier, this, table.properties());
    tableRuntimeMap.put(tableIdentifier, tableRuntime);
    if (headHandler != null) {
      headHandler.fireTableAdded(table, tableRuntime);
    }
  }

  @Override
  public List<String> listDatabases(String catalogName) {
    checkStarted();
    return getServerCatalog(catalogName).listDatabases();
  }

  @Override
  public List<ServerTableIdentifier> listManagedTables() {
    checkStarted();
    return getAs(TableMetaMapper.class, TableMetaMapper::selectAllTableIdentifiers);
  }

  @Override
  public List<ServerTableIdentifier> listManagedTables(String catalogName) {
    checkStarted();
    return getAs(TableMetaMapper.class, mapper -> mapper.selectTableIdentifiersByCatalog(catalogName));
  }

  @Override
  public List<TableIDWithFormat> listTables(String catalogName, String dbName) {
    checkStarted();
    return getServerCatalog(catalogName).listTables(dbName);
  }

  @Override
  public void createDatabase(String catalogName, String dbName) {
    checkStarted();
    getInternalCatalog(catalogName).createDatabase(dbName);
  }

  @Override
  public void dropDatabase(String catalogName, String dbName) {
    checkStarted();
    getInternalCatalog(catalogName).dropDatabase(dbName);
  }

  @Override
  public AmoroTable<?> loadTable(ServerTableIdentifier tableIdentifier) {
    checkStarted();
    return getServerCatalog(tableIdentifier.getCatalog())
        .loadTable(tableIdentifier.getDatabase(), tableIdentifier.getTableName());
  }

  @Override
  public List<TableMetadata> listTableMetas() {
    checkStarted();
    return getAs(TableMetaMapper.class, TableMetaMapper::selectTableMetas);
  }

  @Override
  public List<TableMetadata> listTableMetas(String catalogName, String database) {
    checkStarted();
    return getAs(TableMetaMapper.class, mapper -> mapper.selectTableMetasByDb(catalogName, database));
  }

  @Override
  public boolean tableExist(TableIdentifier tableIdentifier) {
    checkStarted();
    return getServerCatalog(tableIdentifier.getCatalog()).exist(
        tableIdentifier.getDatabase(),
        tableIdentifier.getTableName());
  }

  @Override
  public Blocker block(
      TableIdentifier tableIdentifier, List<BlockableOperation> operations,
      Map<String, String> properties) {
    checkStarted();
    return getAndCheckExist(getServerTableIdentifier(tableIdentifier))
        .block(operations, properties, blockerTimeout)
        .buildBlocker();
  }

  @Override
  public void releaseBlocker(TableIdentifier tableIdentifier, String blockerId) {
    checkStarted();
    TableRuntime tableRuntime = getRuntime(getServerTableIdentifier(tableIdentifier));
    if (tableRuntime != null) {
      tableRuntime.release(blockerId);
    }
  }

  @Override
  public long renewBlocker(TableIdentifier tableIdentifier, String blockerId) {
    checkStarted();
    TableRuntime tableRuntime = getAndCheckExist(getServerTableIdentifier(tableIdentifier));
    return tableRuntime.renew(blockerId, blockerTimeout);
  }

  @Override
  public List<Blocker> getBlockers(TableIdentifier tableIdentifier) {
    checkStarted();
    return getAndCheckExist(getServerTableIdentifier(tableIdentifier))
        .getBlockers().stream().map(TableBlocker::buildBlocker).collect(Collectors.toList());
  }

  private InternalCatalog getInternalCatalog(String catalogName) {
    return Optional.ofNullable(internalCatalogMap.get(catalogName)).orElseThrow(() ->
        new ObjectNotExistsException("Catalog " + catalogName));
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
    List<CatalogMeta> catalogMetas =
        getAs(CatalogMetaMapper.class, CatalogMetaMapper::getCatalogs);
    catalogMetas.forEach(this::initServerCatalog);

    List<TableRuntimeMeta> tableRuntimeMetaList =
        getAs(TableMetaMapper.class, TableMetaMapper::selectTableRuntimeMetas);
    tableRuntimeMetaList.forEach(tableRuntimeMeta -> {
      TableRuntime tableRuntime = tableRuntimeMeta.constructTableRuntime(this);
      tableRuntimeMap.put(tableRuntime.getTableIdentifier(), tableRuntime);
    });

    if (headHandler != null) {
      headHandler.initialize(tableRuntimeMetaList);
    }
    if (tableExplorerExecutors == null) {
      int threadCount = serverConfiguration.getInteger(ArcticManagementConf.REFRESH_EXTERNAL_CATALOGS_THREAD_COUNT);
      int queueSize = serverConfiguration.getInteger(ArcticManagementConf.REFRESH_EXTERNAL_CATALOGS_QUEUE_SIZE);
      tableExplorerExecutors = new ThreadPoolExecutor(
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
        this::exploreExternalCatalog,
        0,
        externalCatalogRefreshingInterval,
        TimeUnit.MILLISECONDS);
    initialized.complete(true);
  }

  public TableRuntime getAndCheckExist(ServerTableIdentifier tableIdentifier) {
    Preconditions.checkArgument(tableIdentifier != null, "tableIdentifier cannot be null");
    TableRuntime tableRuntime = getRuntime(tableIdentifier);
    if (tableRuntime == null) {
      throw new ObjectNotExistsException(tableIdentifier);
    }
    return tableRuntime;
  }

  private ServerTableIdentifier getServerTableIdentifier(TableIdentifier id) {
    return getAs(
        TableMetaMapper.class,
        mapper -> mapper.selectTableIdentifier(id.getCatalog(), id.getDatabase(), id.getTableName()));
  }

  @Override
  public TableRuntime getRuntime(ServerTableIdentifier tableIdentifier) {
    checkStarted();
    return tableRuntimeMap.get(tableIdentifier);
  }

  @Override
  public boolean contains(ServerTableIdentifier tableIdentifier) {
    checkStarted();
    return tableRuntimeMap.containsKey(tableIdentifier);
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
    long start = System.currentTimeMillis();
    LOG.info("Syncing external catalogs: {}", String.join(",", externalCatalogMap.keySet()));
    for (ExternalCatalog externalCatalog : externalCatalogMap.values()) {
      try {
        final List<CompletableFuture<Set<TableIdentity>>> tableIdentifiersFutures = Lists.newArrayList();
        externalCatalog.listDatabases().forEach(
            database -> {
              try {
                tableIdentifiersFutures.add(
                    CompletableFuture.supplyAsync(
                        () -> externalCatalog.listTables(database).stream()
                            .map(TableIdentity::new)
                            .collect(Collectors.toSet()), tableExplorerExecutors));
              } catch (RejectedExecutionException e) {
                LOG.error("The queue of table explorer is full, please increase the queue size or thread count.");
              }
            }
        );
        Set<TableIdentity> tableIdentifiers =
            tableIdentifiersFutures.stream()
                .map(CompletableFuture::join)
                .reduce(
                    (a, b) -> {
                      a.addAll(b);
                      return a;
                    })
                .orElse(Sets.newHashSet());
        LOG.info("Loaded {} tables from external catalog {}.", tableIdentifiers.size(), externalCatalog.name());
        Map<TableIdentity, ServerTableIdentifier> serverTableIdentifiers =
            getAs(
                TableMetaMapper.class,
                mapper -> mapper.selectTableIdentifiersByCatalog(externalCatalog.name())).stream()
                .collect(Collectors.toMap(TableIdentity::new, tableIdentifier -> tableIdentifier));
        LOG.info("Loaded {} tables from Amoro server catalog {}.",
            serverTableIdentifiers.size(), externalCatalog.name());
        final List<CompletableFuture<Void>> taskFutures = Lists.newArrayList();
        Sets.difference(tableIdentifiers, serverTableIdentifiers.keySet())
            .forEach(tableIdentity -> {
                  try {
                    taskFutures.add(
                        CompletableFuture.runAsync(
                            () -> {
                              try {
                                syncTable(externalCatalog, tableIdentity);
                              } catch (Exception e) {
                                LOG.error("TableExplorer sync table {} error", tableIdentity.toString(), e);
                              }
                            }, tableExplorerExecutors));
                  } catch (RejectedExecutionException e) {
                    LOG.error("The queue of table explorer is full, please increase the queue size or thread count.");
                  }
                }
            );
        Sets.difference(serverTableIdentifiers.keySet(), tableIdentifiers)
            .forEach(tableIdentity -> {
              try {
                taskFutures.add(
                    CompletableFuture.runAsync(
                        () -> {
                          try {
                            disposeTable(externalCatalog, serverTableIdentifiers.get(tableIdentity));
                          } catch (Exception e) {
                            LOG.error("TableExplorer dispose table {} error", tableIdentity.toString(), e);
                          }
                        }, tableExplorerExecutors));
              } catch (RejectedExecutionException e) {
                LOG.error("The queue of table explorer is full, please increase the queue size or thread count.");
              }
            });
        taskFutures.forEach(CompletableFuture::join);
      } catch (Throwable e) {
        LOG.error("TableExplorer error", e);
      }
    }
    long end = System.currentTimeMillis();
    LOG.info("Syncing external catalogs took {} ms.", end - start);
  }

  private void validateTableIdentifier(TableIdentifier tableIdentifier) {
    if (StringUtils.isBlank(tableIdentifier.getTableName())) {
      throw new IllegalMetadataException("table name is blank");
    }
    if (StringUtils.isBlank(tableIdentifier.getCatalog())) {
      throw new IllegalMetadataException("catalog is blank");
    }
    if (StringUtils.isBlank(tableIdentifier.getDatabase())) {
      throw new IllegalMetadataException("database is blank");
    }
  }

  private void validateTableNotExists(TableIdentifier tableIdentifier) {
    validateTableIdentifier(tableIdentifier);
    if (tableExist(tableIdentifier)) {
      throw new AlreadyExistsException(tableIdentifier);
    }
  }

  private void validateTableExists(TableIdentifier tableIdentifier) {
    validateTableIdentifier(tableIdentifier);
    if (!tableExist(tableIdentifier)) {
      throw new ObjectNotExistsException(tableIdentifier);
    }
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
    try {
      doAsTransaction(
          () -> externalCatalog.syncTable(
              tableIdentity.getDatabase(), tableIdentity.getTableName(), tableIdentity.getFormat()),
          () -> handleTableRuntimeAdded(externalCatalog, tableIdentity)
      );
    } catch (Throwable t) {
      revertTableRuntimeAdded(externalCatalog, tableIdentity);
      throw t;
    }
  }

  private void handleTableRuntimeAdded(ExternalCatalog externalCatalog, TableIdentity tableIdentity) {
    ServerTableIdentifier tableIdentifier =
        externalCatalog.getServerTableIdentifier(tableIdentity.getDatabase(), tableIdentity.getTableName());
    AmoroTable<?> table = externalCatalog.loadTable(tableIdentity.getDatabase(), tableIdentity.getTableName());
    TableRuntime tableRuntime = new TableRuntime(tableIdentifier, this, table.properties());
    tableRuntimeMap.put(tableIdentifier, tableRuntime);
    if (headHandler != null) {
      headHandler.fireTableAdded(table, tableRuntime);
    }
  }

  private void revertTableRuntimeAdded(ExternalCatalog externalCatalog, TableIdentity tableIdentity) {
    ServerTableIdentifier tableIdentifier =
        externalCatalog.getServerTableIdentifier(tableIdentity.getDatabase(), tableIdentity.getTableName());
    if (tableIdentifier != null) {
      tableRuntimeMap.remove(tableIdentifier);
    }
  }

  private void disposeTable(ExternalCatalog externalCatalog, ServerTableIdentifier tableIdentifier) {
    externalCatalog.disposeTable(tableIdentifier.getDatabase(), tableIdentifier.getTableName());
    Optional.ofNullable(tableRuntimeMap.remove(tableIdentifier))
        .ifPresent(tableRuntime -> {
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
