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
import org.apache.amoro.client.AmsServerInfo;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.BucketAssignStore;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.catalog.ExternalCatalog;
import org.apache.amoro.server.catalog.InternalCatalog;
import org.apache.amoro.server.catalog.ServerCatalog;
import org.apache.amoro.server.ha.HighAvailabilityContainer;
import org.apache.amoro.server.manager.MetricManager;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.persistence.BucketIdCount;
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
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

  private final Map<Long, TableRuntime> tableRuntimeMap = new ConcurrentHashMap<>();

  private final ScheduledExecutorService tableExplorerScheduler =
      Executors.newSingleThreadScheduledExecutor(
          new ThreadFactoryBuilder()
              .setNameFormat("table-explorer-scheduler-%d")
              .setDaemon(true)
              .build());
  private final CompletableFuture<Boolean> initialized = new CompletableFuture<>();
  private final Configurations serverConfiguration;
  private final CatalogManager catalogManager;
  private final TableRuntimeFactoryManager tableRuntimeFactoryManager;
  private final HighAvailabilityContainer haContainer;
  private final BucketAssignStore bucketAssignStore;
  private final boolean isMasterSlaveMode;
  private RuntimeHandlerChain headHandler;
  private ExecutorService tableExplorerExecutors;

  // Master-slave mode related fields
  private ScheduledExecutorService bucketTableSyncScheduler;
  private volatile List<String> assignedBucketIds = new ArrayList<>();
  private final long bucketTableSyncInterval;
  // Lock for bucketId assignment to prevent concurrent assignment conflicts
  private final Object bucketIdAssignmentLock = new Object();
  // Local cache of bucketId assignments to track pending assignments before they're saved to DB
  // This ensures concurrent table creation gets different bucketIds even before DB is updated
  private final Map<String, Integer> pendingBucketIdCounts = new ConcurrentHashMap<>();

  public DefaultTableService(
      Configurations configuration,
      CatalogManager catalogManager,
      TableRuntimeFactoryManager tableRuntimeFactoryManager) {
    this(configuration, catalogManager, tableRuntimeFactoryManager, null, null);
  }

  public DefaultTableService(
      Configurations configuration,
      CatalogManager catalogManager,
      TableRuntimeFactoryManager tableRuntimeFactoryManager,
      HighAvailabilityContainer haContainer,
      BucketAssignStore bucketAssignStore) {
    this.catalogManager = catalogManager;
    this.externalCatalogRefreshingInterval =
        configuration.get(AmoroManagementConf.REFRESH_EXTERNAL_CATALOGS_INTERVAL).toMillis();
    this.serverConfiguration = configuration;
    this.tableRuntimeFactoryManager = tableRuntimeFactoryManager;
    this.haContainer = haContainer;
    this.bucketAssignStore = bucketAssignStore;
    this.isMasterSlaveMode = configuration.getBoolean(AmoroManagementConf.USE_MASTER_SLAVE_MODE);
    this.bucketTableSyncInterval =
        configuration.get(AmoroManagementConf.BUCKET_TABLE_SYNC_INTERVAL).toMillis();
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

    List<TableRuntimeMeta> tableRuntimeMetaList;
    if (isMasterSlaveMode && haContainer != null && bucketAssignStore != null) {
      // In master-slave mode, load only tables assigned to this node
      try {
        updateAssignedBucketIds();
        if (!assignedBucketIds.isEmpty()) {
          tableRuntimeMetaList =
              getAs(
                  TableRuntimeMapper.class,
                  mapper -> mapper.selectRuntimesByBucketIds(assignedBucketIds));
          LOG.info(
              "Master-slave mode: Loaded {} tables for assigned bucketIds: {}",
              tableRuntimeMetaList.size(),
              assignedBucketIds);
        } else {
          tableRuntimeMetaList = new ArrayList<>();
          LOG.info("Master-slave mode: No bucketIds assigned to this node yet");
        }
      } catch (Exception e) {
        LOG.error("Failed to load tables for assigned bucketIds in master-slave mode", e);
        tableRuntimeMetaList = new ArrayList<>();
      }
    } else {
      // Non-master-slave mode: load all tables
      tableRuntimeMetaList = getAs(TableRuntimeMapper.class, TableRuntimeMapper::selectAllRuntimes);
    }

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

    List<TableRuntime> tableRuntimes = new ArrayList<>(tableRuntimeMetaList.size());

    for (TableRuntimeMeta tableRuntimeMeta : tableRuntimeMetaList) {
      ServerTableIdentifier identifier = identifierMap.get(tableRuntimeMeta.getTableId());
      if (identifier == null) {
        LOG.warn(
            "No available table identifier found for table runtime meta id={}",
            tableRuntimeMeta.getTableId());
        continue;
      }
      List<TableRuntimeState> states = statesMap.get(tableRuntimeMeta.getTableId());
      // Use empty list if states is null to avoid NullPointerException
      if (states == null) {
        states = Collections.emptyList();
      }
      Optional<TableRuntime> tableRuntime =
          createTableRuntime(identifier, tableRuntimeMeta, states);
      if (!tableRuntime.isPresent()) {
        LOG.warn("No available table runtime factory found for table {}", identifier);
        continue;
      }
      tableRuntime.ifPresent(
          t -> {
            t.registerMetric(MetricManager.getInstance().getGlobalRegistry());
            tableRuntimeMap.put(t.getTableIdentifier().getId(), t);
            tableRuntimes.add(t);
          });
    }

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
    initialized.complete(true);
    tableExplorerScheduler.scheduleAtFixedRate(
        this::exploreTableRuntimes, 0, externalCatalogRefreshingInterval, TimeUnit.MILLISECONDS);

    if (isMasterSlaveMode && haContainer != null && bucketAssignStore != null) {
      // In master-slave mode, start periodic sync for assigned bucket tables
      // Delay the first sync to allow AmsAssignService to assign bucketIds first
      bucketTableSyncScheduler =
          Executors.newSingleThreadScheduledExecutor(
              new ThreadFactoryBuilder()
                  .setNameFormat("bucket-table-sync-scheduler-%d")
                  .setDaemon(true)
                  .build());
      bucketTableSyncScheduler.scheduleAtFixedRate(
          this::syncBucketTables,
          bucketTableSyncInterval,
          bucketTableSyncInterval,
          TimeUnit.MILLISECONDS);
      LOG.info(
          "Master-slave mode: Started bucket table sync scheduler with interval {} ms (first sync delayed by {} ms)",
          bucketTableSyncInterval,
          bucketTableSyncInterval);
    }
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
    if (bucketTableSyncScheduler != null) {
      bucketTableSyncScheduler.shutdown();
    }
    if (tableExplorerExecutors != null) {
      tableExplorerExecutors.shutdown();
    }
    if (headHandler != null) {
      headHandler.dispose();
    }
    tableRuntimeMap.values().forEach(TableRuntime::unregisterMetric);
  }

  /**
   * Update assigned bucket IDs from ZK. This should be called periodically to refresh the bucket
   * assignments.
   */
  private void updateAssignedBucketIds() {
    if (haContainer == null || bucketAssignStore == null) {
      LOG.warn(
          "No assigned bucket ids found. check if haContainer == null or bucketAssignStore == null");
      return;
    }
    try {
      AmsServerInfo currentServerInfo = haContainer.getOptimizingServiceServerInfo();
      if (currentServerInfo == null) {
        LOG.warn("Cannot get current server info, skip updating assigned bucketIds");
        return;
      }
      List<String> newBucketIds = bucketAssignStore.getAssignments(currentServerInfo);
      if (!newBucketIds.equals(assignedBucketIds)) {
        LOG.info("Assigned bucketIds changed from {} to {}", assignedBucketIds, newBucketIds);
        assignedBucketIds = new ArrayList<>(newBucketIds);
      }
    } catch (Exception e) {
      LOG.error("Failed to update assigned bucketIds", e);
    }
  }

  /**
   * Sync tables for assigned bucket IDs. This method is called periodically in master-slave mode.
   */
  private void syncBucketTables() {
    try {
      updateAssignedBucketIds();
      if (assignedBucketIds.isEmpty()) {
        // In master-slave mode, if no bucketIds are assigned yet, it's normal during startup
        // The AmsAssignService will assign bucketIds later
        LOG.debug("No bucketIds assigned to this node yet, skip syncing tables (will retry later)");
        return;
      }

      LOG.info("syncBucketTables assignedBucketIds:{}", assignedBucketIds);
      // Load tables from database for assigned bucketIds
      List<TableRuntimeMeta> tableRuntimeMetaList =
          getAs(
              TableRuntimeMapper.class,
              mapper -> mapper.selectRuntimesByBucketIds(assignedBucketIds));

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

      // Find tables that should be added (in DB but not in memory)
      Set<Long> currentTableIds = new HashSet<>(tableRuntimeMap.keySet());
      Set<Long> dbTableIds =
          tableRuntimeMetaList.stream()
              .map(TableRuntimeMeta::getTableId)
              .collect(Collectors.toSet());

      // Add new tables
      for (TableRuntimeMeta tableRuntimeMeta : tableRuntimeMetaList) {
        Long tableId = tableRuntimeMeta.getTableId();
        // Double-check to avoid race condition: check again after creating TableRuntime
        if (!currentTableIds.contains(tableId) && !tableRuntimeMap.containsKey(tableId)) {
          ServerTableIdentifier identifier = identifierMap.get(tableId);
          if (identifier == null) {
            LOG.warn("No available table identifier found for table runtime meta id={}", tableId);
            continue;
          }
          List<TableRuntimeState> states = statesMap.get(tableId);
          // Use empty list if states is null to avoid NullPointerException
          if (states == null) {
            states = Collections.emptyList();
          }
          Optional<TableRuntime> tableRuntime =
              createTableRuntime(identifier, tableRuntimeMeta, states);
          if (tableRuntime.isPresent()) {
            TableRuntime runtime = tableRuntime.get();
            // Final check before registering metrics to avoid duplicate registration
            TableRuntime existing = tableRuntimeMap.putIfAbsent(tableId, runtime);
            if (existing != null) {
              // Another thread already added this table, skip registration
              LOG.debug("Table {} already exists in tableRuntimeMap, skip adding", tableId);
              continue;
            }
            // Register metrics only after successfully adding to map
            runtime.registerMetric(MetricManager.getInstance().getGlobalRegistry());
            if (headHandler != null) {
              AmoroTable<?> table = loadTable(identifier);
              if (table != null) {
                headHandler.fireTableAdded(table, runtime);
              }
            }
            LOG.info("Added table {} for bucketId {}", tableId, tableRuntimeMeta.getBucketId());
          }
        }
      }

      // Remove tables that are no longer assigned to this node
      List<Long> tablesToRemove = new ArrayList<>();
      for (Long tableId : currentTableIds) {
        if (!dbTableIds.contains(tableId)) {
          // Check if this table's bucketId is still assigned to this node
          TableRuntime tableRuntime = tableRuntimeMap.get(tableId);
          if (tableRuntime != null) {
            // Get bucketId from database
            TableRuntimeMeta meta =
                getAs(TableRuntimeMapper.class, mapper -> mapper.selectRuntime(tableId));
            if (meta != null && meta.getBucketId() != null) {
              if (!assignedBucketIds.contains(meta.getBucketId())) {
                tablesToRemove.add(tableId);
              }
            } else if (meta == null || meta.getBucketId() == null) {
              // Table removed from database or bucketId is null
              tablesToRemove.add(tableId);
            }
          }
        }
      }

      for (Long tableId : tablesToRemove) {
        TableRuntime tableRuntime = tableRuntimeMap.get(tableId);
        if (tableRuntime != null) {
          try {
            if (headHandler != null) {
              headHandler.fireTableRemoved(tableRuntime);
            }
            tableRuntime.dispose();
            tableRuntimeMap.remove(tableId);
            LOG.info("Removed table {} as it's no longer assigned to this node", tableId);
          } catch (Exception e) {
            LOG.error("Error occurred while removing tableRuntime of table {}", tableId, e);
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Error during bucket table sync", e);
    }
  }

  /**
   * Assign bucketId to a table using min-heap strategy. This ensures tables are evenly distributed
   * across bucketIds.
   *
   * @return The assigned bucketId, or null if assignment fails
   */
  private String assignBucketIdForTable() {
    // Synchronize to prevent concurrent assignments from selecting the same bucketId
    synchronized (bucketIdAssignmentLock) {
      try {
        // Get bucketId distribution statistics from database (optimized query)
        List<BucketIdCount> bucketIdCounts =
            getAs(
                TableRuntimeMapper.class,
                (TableRuntimeMapper mapper) -> mapper.countTablesByBucketId());

        // Initialize bucketId count map with all possible bucketIds
        Map<String, Integer> bucketTableCount = new ConcurrentHashMap<>();
        int bucketIdTotalCount =
            serverConfiguration.getInteger(AmoroManagementConf.BUCKET_ID_TOTAL_COUNT);
        for (int i = 1; i <= bucketIdTotalCount; i++) {
          bucketTableCount.put(String.valueOf(i), 0);
        }

        // Fill in counts from database query results
        for (BucketIdCount bucketIdCount : bucketIdCounts) {
          String bucketId = bucketIdCount.getBucketId();
          if (bucketId != null && !bucketId.trim().isEmpty()) {
            bucketTableCount.put(bucketId, bucketIdCount.getCount());
          }
        }

        // Add pending assignments (assigned but not yet saved to DB) to the count
        // This ensures concurrent table creation gets different bucketIds
        // Note: Pending counts are decremented when tables are successfully saved to DB
        for (Map.Entry<String, Integer> pendingEntry : pendingBucketIdCounts.entrySet()) {
          String bucketId = pendingEntry.getKey();
          int pendingCount = pendingEntry.getValue();
          bucketTableCount.put(bucketId, bucketTableCount.getOrDefault(bucketId, 0) + pendingCount);
        }

        // Use min-heap to find bucketId with minimum table count
        // Create new entries instead of using Map.Entry directly to avoid reference issues
        PriorityQueue<Map.Entry<String, Integer>> minHeap =
            new PriorityQueue<>(
                Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue)
                    .thenComparing(Map.Entry::getKey)); // Use bucketId as tie-breaker

        // Create independent entries for the heap to avoid reference issues
        for (Map.Entry<String, Integer> entry : bucketTableCount.entrySet()) {
          minHeap.offer(new java.util.AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
        }

        if (!minHeap.isEmpty()) {
          Map.Entry<String, Integer> selected = minHeap.poll();
          String assignedBucketId = selected.getKey();
          int tableCount = selected.getValue();

          // Update pending count immediately to prevent next concurrent call from selecting same
          // bucketId
          pendingBucketIdCounts.put(
              assignedBucketId, pendingBucketIdCounts.getOrDefault(assignedBucketId, 0) + 1);

          LOG.debug(
              "Assigned bucketId {} to new table (min table count: {}, pending: {})",
              assignedBucketId,
              tableCount,
              pendingBucketIdCounts.get(assignedBucketId));
          return assignedBucketId;
        }
      } catch (Exception e) {
        LOG.error("Failed to assign bucketId for table", e);
      }
      return null;
    }
  }

  @VisibleForTesting
  void exploreTableRuntimes() {
    // In master-slave mode, only leader node should explore table runtimes
    if (isMasterSlaveMode && haContainer != null && !haContainer.hasLeadership()) {
      LOG.debug("Not the leader node in master-slave mode, skip exploring table runtimes");
      return;
    }
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
    // In master-slave mode, only clean up tables assigned to this node
    Set<String> catalogNames =
        catalogManager.listCatalogMetas().stream()
            .map(CatalogMeta::getCatalogName)
            .collect(Collectors.toSet());
    for (TableRuntime tableRuntime : tableRuntimeMap.values()) {
      if (!catalogNames.contains(tableRuntime.getTableIdentifier().getCatalog())) {
        // In master-slave mode, only dispose tables assigned to this node
        if (isMasterSlaveMode && haContainer != null && bucketAssignStore != null) {
          try {
            TableRuntimeMeta meta =
                getAs(
                    TableRuntimeMapper.class,
                    mapper -> mapper.selectRuntime(tableRuntime.getTableIdentifier().getId()));
            if (meta != null && meta.getBucketId() != null) {
              if (!assignedBucketIds.contains(meta.getBucketId())) {
                // Not assigned to this node, skip
                continue;
              }
            }
          } catch (Exception e) {
            LOG.warn("Failed to check bucketId for table {}", tableRuntime.getTableIdentifier(), e);
          }
        }
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

    Map<String, String> properties = table.properties();
    TableRuntimeMeta meta = new TableRuntimeMeta();
    meta.setTableId(serverTableIdentifier.getId());
    meta.setTableConfig(properties);
    TableConfiguration configuration = TableConfigurations.parseTableConfig(properties);
    meta.setStatusCode(OptimizingStatus.IDLE.getCode());
    meta.setGroupName(configuration.getOptimizingConfig().getOptimizerGroup());
    meta.setTableSummary(new TableSummary());

    // In master-slave mode, assign bucketId to the table if it's not assigned yet
    // Only leader node should assign bucketIds
    String assignedBucketId = null;
    if (isMasterSlaveMode) {
      assignedBucketId = assignBucketIdForTable();
      if (assignedBucketId != null) {
        meta.setBucketId(assignedBucketId);
        LOG.info("Assigned bucketId {} to table {}", assignedBucketId, serverTableIdentifier);
      } else {
        LOG.warn(
            "Failed to assign bucketId to table {}, will be assigned later", serverTableIdentifier);
      }
    }

    try {
      doAs(TableRuntimeMapper.class, mapper -> mapper.insertRuntime(meta));
      // After successful save, decrease pending count since the assignment is now persisted in DB
      if (assignedBucketId != null) {
        synchronized (bucketIdAssignmentLock) {
          int pendingCount = pendingBucketIdCounts.getOrDefault(assignedBucketId, 0);
          if (pendingCount > 0) {
            pendingBucketIdCounts.put(assignedBucketId, pendingCount - 1);
            if (pendingBucketIdCounts.get(assignedBucketId) == 0) {
              pendingBucketIdCounts.remove(assignedBucketId);
            }
          }
        }
      }
    } catch (Exception e) {
      // If save fails, keep the pending count so the bucketId assignment is still tracked
      // This ensures the count remains accurate even if the save operation fails
      throw e;
    }

    if (isMasterSlaveMode) {
      return true;
    }

    Optional<TableRuntime> tableRuntimeOpt =
        createTableRuntime(serverTableIdentifier, meta, Collections.emptyList());
    if (!tableRuntimeOpt.isPresent()) {
      LOG.warn("No available table runtime factory found for table {}", serverTableIdentifier);
      return false;
    }

    TableRuntime tableRuntime = tableRuntimeOpt.get();
    tableRuntimeMap.put(serverTableIdentifier.getId(), tableRuntime);
    tableRuntime.registerMetric(MetricManager.getInstance().getGlobalRegistry());
    if (headHandler != null) {
      headHandler.fireTableAdded(table, tableRuntime);
    }
    return true;
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
              store.setRuntimeHandler(this);
              TableRuntime tableRuntime = creator.create(store);
              store.setTableRuntime(tableRuntime);
              return tableRuntime;
            });
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
                          if (headHandler != null) {
                            headHandler.fireTableRemoved(tableRuntime);
                          }
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
