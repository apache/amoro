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

package com.netease.arctic.ams.server.optimize;

import com.google.common.collect.ArrayListMultimap;
import com.netease.arctic.AmsClient;
import com.netease.arctic.ams.api.ErrorMessage;
import com.netease.arctic.ams.api.InvalidObjectException;
import com.netease.arctic.ams.api.NoSuchObjectException;
import com.netease.arctic.ams.api.OptimizeStatus;
import com.netease.arctic.ams.api.OptimizeTaskId;
import com.netease.arctic.ams.api.OptimizeTaskStat;
import com.netease.arctic.ams.server.mapper.OptimizeHistoryMapper;
import com.netease.arctic.ams.server.mapper.OptimizeTaskRuntimesMapper;
import com.netease.arctic.ams.server.mapper.OptimizeTasksMapper;
import com.netease.arctic.ams.server.mapper.TableOptimizeRuntimeMapper;
import com.netease.arctic.ams.server.model.BaseOptimizeTask;
import com.netease.arctic.ams.server.model.BaseOptimizeTaskRuntime;
import com.netease.arctic.ams.server.model.OptimizeHistory;
import com.netease.arctic.ams.server.model.TableMetadata;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import com.netease.arctic.ams.server.service.IJDBCService;
import com.netease.arctic.ams.server.service.IMetaService;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.ams.server.service.impl.OptimizeQueueService;
import com.netease.arctic.ams.server.utils.ArcticMetaValidator;
import com.netease.arctic.ams.server.utils.OptimizeStatusUtil;
import com.netease.arctic.ams.server.utils.ScheduledTasks;
import com.netease.arctic.ams.server.utils.ThreadPool;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableProperties;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.iceberg.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class OptimizeService extends IJDBCService implements IOptimizeService {
  private static final Logger LOG = LoggerFactory.getLogger(OptimizeService.class);
  private static final long DEFAULT_CHECK_INTERVAL = 60000;

  private ScheduledTasks<TableIdentifier, OptimizeCommitTask> commitTasks;
  private ScheduledTasks<TableIdentifier, OptimizeCheckTask> checkTasks;

  private static final long DEFAULT_CACHE_REFRESH_TIME = 60_000; // 1min
  private final Map<TableIdentifier, TableOptimizeItem> cachedTables = new HashMap<>();
  private final ReadWriteLock tablesLock = new ReentrantReadWriteLock();

  private long refreshTime = 0;

  private final OptimizeQueueService optimizeQueueService;
  private final IMetaService metaService;
  private final AmsClient metastoreClient;
  private Long optimizeStatusCheckInterval = null;

  public OptimizeService() {
    super();
    optimizeQueueService = ServiceContainer.getOptimizeQueueService();
    metaService = ServiceContainer.getMetaService();
    metastoreClient = ServiceContainer.getTableMetastoreHandler();
    init();
  }

  private void init() {
    tablesLock.writeLock().lock();
    try {
      LOG.info("OptimizeService init...");
      loadTables();
      initOptimizeTasksIntoOptimizeQueue();
      LOG.info("OptimizeService init completed");
    } finally {
      tablesLock.writeLock().unlock();
    }
  }

  @Override
  public synchronized void checkOptimizeCheckTasks(long checkInterval) {
    this.optimizeStatusCheckInterval = checkInterval;
    if (checkTasks == null) {
      checkTasks = new ScheduledTasks<>(ThreadPool.Type.OPTIMIZE_CHECK);
    }
    internalCheckOptimizeChecker(checkInterval);
  }

  private void internalCheckOptimizeChecker(long checkInterval) {
    LOG.info("Schedule Optimize Checker");
    if (checkTasks == null) {
      return;
    }
    List<TableIdentifier> validTables = listCachedTables(true);
    checkTasks.checkRunningTask(
        new HashSet<>(validTables),
        identifier -> checkInterval,
        OptimizeCheckTask::new,
        false);
    LOG.info("Schedule Optimize Checker finished with {} valid tables", validTables.size());
  }

  @Override
  public synchronized void checkOptimizeCommitTasks() {
    if (commitTasks == null) {
      commitTasks = new ScheduledTasks<>(ThreadPool.Type.COMMIT);
    }
    internalCheckOptimizeCommitter();
  }

  private void internalCheckOptimizeCommitter() {
    LOG.info("Schedule Optimize Committer");
    if (commitTasks == null) {
      return;
    }
    List<TableIdentifier> validTables = listCachedTables(true);
    commitTasks.checkRunningTask(
        new HashSet<>(validTables),
        this::getCommitInterval,
        OptimizeCommitTask::new,
        false);
    LOG.info("Schedule Optimize Committer finished with {} valid tables", validTables.size());
  }

  private Long getCommitInterval(TableIdentifier identifier) {
    try {
      return getTableOptimizeItem(identifier).getCommitInterval();
    } catch (NoSuchObjectException e) {
      return null;
    }
  }

  @Override
  public List<TableIdentifier> listCachedTables(boolean forceRefresh) {
    tablesLock.readLock().lock();
    try {
      if (!forceRefresh && !cacheExpired()) {
        return new ArrayList<>(cachedTables.keySet());
      }
    } finally {
      tablesLock.readLock().unlock();
    }
    return refreshAndListTables(forceRefresh);
  }

  private void clearTableCache(TableIdentifier tableIdentifier) {
    TableOptimizeItem tableItem = cachedTables.remove(tableIdentifier);
    optimizeQueueService.release(tableIdentifier);
    try {
      deleteTableOptimizeRuntime(tableIdentifier);
    } catch (Throwable t) {
      LOG.error("failed to delete  " + tableIdentifier + " runtime, ignore", t);
    }
    try {
      tableItem.clearOptimizeTasks();
    } catch (Throwable t) {
      LOG.error("failed to delete " + tableIdentifier + " optimize task, ignore", t);
    }
    try {
      deleteOptimizeRecord(tableIdentifier);
      deleteOptimizeTaskHistory(tableIdentifier);
    } catch (Throwable t) {
      LOG.error("failed to delete " + tableIdentifier + " optimize(task) history, ignore", t);
    }
  }

  private void addTableIntoCache(TableOptimizeItem arcticTableItem, Map<String, String> properties,
                                 boolean persistRuntime) {
    cachedTables.put(arcticTableItem.getTableIdentifier(), arcticTableItem);
    try {
      int queueId = optimizeQueueService.getQueueId(properties);
      optimizeQueueService.bind(arcticTableItem.getTableIdentifier(), queueId);
    } catch (InvalidObjectException e) {
      LOG.error("failed to bind " + arcticTableItem.getTableIdentifier() + " and queue ", e);
    }
    if (persistRuntime) {
      try {
        insertTableOptimizeRuntime(arcticTableItem.getTableOptimizeRuntime());
      } catch (Throwable t) {
        LOG.error("failed to insert " + arcticTableItem.getTableIdentifier() + " runtime, ignore", t);
      }
    }
  }

  @Override
  public TableOptimizeItem getTableOptimizeItem(TableIdentifier tableIdentifier) throws NoSuchObjectException {
    TableOptimizeItem tableOptimizeItem = cachedTables.get(tableIdentifier);
    if (tableOptimizeItem == null) {
      listCachedTables(true);
      TableOptimizeItem reloadTableOptimizeItem = cachedTables.get(tableIdentifier);
      if (reloadTableOptimizeItem == null) {
        throw new NoSuchObjectException("can't find table " + tableIdentifier);
      }
      return reloadTableOptimizeItem;
    }
    return tableOptimizeItem;
  }

  @Override
  public void handleOptimizeResult(OptimizeTaskStat optimizeTaskStat) throws NoSuchObjectException {
    getTableOptimizeItem(new TableIdentifier(optimizeTaskStat.getTableIdentifier()))
        .updateOptimizeTaskStat(optimizeTaskStat);
  }

  private void loadTables() {
    LOG.info("init load tables");
    // load table when server start, only load table metadata
    Map<TableIdentifier, List<OptimizeTaskItem>> optimizeTasks = loadOptimizeTasks();
    Map<TableIdentifier, TableOptimizeRuntime> tableOptimizeRuntimes = loadTableOptimizeRuntimes();

    List<TableIdentifier> tableIdentifiers = metaService.listTables().stream()
        .map(TableMetadata::getTableIdentifier).collect(Collectors.toList());
    for (TableIdentifier tableIdentifier : tableIdentifiers) {
      TableMetadata tableMetadata = metaService.loadTableMetadata(tableIdentifier);
      TableOptimizeItem arcticTableItem = new TableOptimizeItem(null, tableMetadata);
      TableOptimizeRuntime oldTableOptimizeRuntime = tableOptimizeRuntimes.remove(tableIdentifier);
      arcticTableItem.initTableOptimizeRuntime(oldTableOptimizeRuntime)
          .initOptimizeTasks(optimizeTasks.remove(tableIdentifier));
      try {
        addTableIntoCache(arcticTableItem, tableMetadata.getProperties(), oldTableOptimizeRuntime == null);
      } catch (Throwable t) {
        LOG.error("cannot add table to server " + tableIdentifier.toString(), t);
      }
    }

    if (!optimizeTasks.isEmpty()) {
      LOG.warn("clear optimize tasks {}", optimizeTasks.keySet());
      for (Map.Entry<TableIdentifier, List<OptimizeTaskItem>> entry : optimizeTasks.entrySet()) {
        for (OptimizeTaskItem task : entry.getValue()) {
          task.clearOptimizeTask();
        }
      }
    }

    if (!tableOptimizeRuntimes.isEmpty()) {
      LOG.warn("clear table runtime {}", tableOptimizeRuntimes.keySet());
      for (TableIdentifier tableIdentifier : tableOptimizeRuntimes.keySet()) {
        deleteTableOptimizeRuntime(tableIdentifier);
      }
    }
  }

  private void initOptimizeTasksIntoOptimizeQueue() {
    ArrayListMultimap<Integer, OptimizeTaskItem> multiMap = ArrayListMultimap.create();
    cachedTables.values().stream().flatMap(t -> t.getOptimizeTasks().stream())
        .filter(t -> OptimizeStatusUtil.in(t.getOptimizeStatus(), OptimizeStatus.Pending))
        .forEach(t -> multiMap.put(t.getOptimizeTask().getQueueId(), t));
    for (Integer queueId : multiMap.keySet()) {
      List<OptimizeTaskItem> optimizeTaskItems = multiMap.get(queueId);
      optimizeTaskItems
          .sort(Comparator.comparingLong(o -> o.getOptimizeRuntime().getPendingTime()));
      for (OptimizeTaskItem task : optimizeTaskItems) {
        try {
          optimizeQueueService.submitTask(task);
        } catch (NoSuchObjectException | InvalidObjectException e) {
          LOG.error("failed to load task {} into optimizeQueue {}", task.getOptimizeTask(), queueId);
          task.onFailed(new ErrorMessage(System.currentTimeMillis(), e.getMessage()), 0L);
        }
      }
    }
  }

  private List<TableIdentifier> refreshAndListTables(boolean forceRefresh) {
    tablesLock.writeLock().lock();
    try {
      if (forceRefresh || cacheExpired()) {
        LOG.info("refresh tables");
        Set<TableIdentifier> tableIdentifiers = metaService.listTables().stream()
            .map(TableMetadata::getTableIdentifier).collect(Collectors.toSet());
        final long now = System.currentTimeMillis();
        List<TableIdentifier> toAddTables = tableIdentifiers.stream()
            .filter(t -> !cachedTables.containsKey(t))
            .collect(Collectors.toList());
        List<TableIdentifier> toRemoveTables = cachedTables.keySet().stream()
            .filter(t -> !tableIdentifiers.contains(t))
            .collect(Collectors.toList());

        addNewTables(toAddTables);
        clearRemovedTables(toRemoveTables);
        this.refreshTime = now;
      }
      return new ArrayList<>(cachedTables.keySet());
    } finally {
      tablesLock.writeLock().unlock();
    }
  }

  private void addNewTables(List<TableIdentifier> toAddTables) {
    if (CollectionUtils.isEmpty(toAddTables)) {
      return;
    }
    for (TableIdentifier toAddTable : toAddTables) {
      try {
        ArcticMetaValidator.nuSuchObjectValidator(metaService, toAddTable);
      } catch (Exception e) {
        LOG.error("no such table " + toAddTable, e);
        continue;
      }
      TableMetadata tableMetadata = metaService.loadTableMetadata(toAddTable);
      ArcticCatalog catalog = CatalogLoader.load(metastoreClient, toAddTable.getCatalog());
      ArcticTable arcticTable = catalog.loadTable(toAddTable);
      TableOptimizeItem newTableItem = new TableOptimizeItem(arcticTable, tableMetadata);
      long createTime = PropertyUtil.propertyAsLong(tableMetadata.getProperties(), TableProperties.TABLE_CREATE_TIME,
          TableProperties.TABLE_CREATE_TIME_DEFAULT);
      newTableItem.getTableOptimizeRuntime().setOptimizeStatusStartTime(createTime);
      addTableIntoCache(newTableItem, arcticTable.properties(), true);
    }
    LOG.info("add new tables[{}] {}", toAddTables.size(), toAddTables);
  }

  private void clearRemovedTables(List<TableIdentifier> toRemoveTables) {
    if (CollectionUtils.isEmpty(toRemoveTables)) {
      return;
    }
    toRemoveTables.forEach(this::clearTableCache);
    internalCheckOptimizeChecker(
        this.optimizeStatusCheckInterval == null ? DEFAULT_CHECK_INTERVAL : this.optimizeStatusCheckInterval);
    internalCheckOptimizeCommitter();
    LOG.info("clear tables[{}] {}", toRemoveTables.size(), toRemoveTables);
  }

  private boolean cacheExpired() {
    return refreshTime == 0 || System.currentTimeMillis() > refreshTime + DEFAULT_CACHE_REFRESH_TIME;
  }

  private Map<TableIdentifier, List<OptimizeTaskItem>> loadOptimizeTasks() {
    Map<TableIdentifier, List<OptimizeTaskItem>> results = new HashMap<>();

    List<BaseOptimizeTask> optimizeTasks = selectAllOptimizeTasks();

    for (BaseOptimizeTask optimizeTask : optimizeTasks) {
      initOptimizeTask(optimizeTask);
    }
    Map<OptimizeTaskId, BaseOptimizeTaskRuntime> optimizeTaskRuntimes =
        selectAllOptimizeTaskRuntimes().stream()
            .collect(Collectors.toMap(BaseOptimizeTaskRuntime::getOptimizeTaskId, r -> r));
    AtomicBoolean lostTaskRuntime = new AtomicBoolean(false);
    List<OptimizeTaskItem> optimizeTaskItems = optimizeTasks.stream()
        .map(t -> {
          BaseOptimizeTaskRuntime optimizeTaskRuntime = optimizeTaskRuntimes.get(t.getTaskId());
          if (optimizeTaskRuntime == null) {
            lostTaskRuntime.set(true);
            LOG.error("can't find optimize task runtime in sysdb, tableIdentifier = {}, taskId = {}",
                t.getTableIdentifier(), t.getTaskId());
          }
          return new OptimizeTaskItem(t,
              optimizeTaskRuntimes.getOrDefault(t.getTaskId(), new BaseOptimizeTaskRuntime(t.getTaskId())));
        })
        .collect(Collectors.toList());

    if (lostTaskRuntime.get()) {
      throw new IllegalStateException("sysdb error, lost some task runtimes, fix sysdb first");
    }

    for (OptimizeTaskItem optimizeTaskItem : optimizeTaskItems) {
      TableIdentifier tableIdentifier = optimizeTaskItem.getTableIdentifier();
      List<OptimizeTaskItem> optimizeTaskItemList = results.computeIfAbsent(tableIdentifier, e -> new ArrayList<>());
      optimizeTaskItemList.add(optimizeTaskItem);
    }
    return results;
  }

  private void initOptimizeTask(BaseOptimizeTask optimizeTask) {
    if (optimizeTask.getInsertFiles() == null) {
      optimizeTask.setInsertFiles(Collections.emptyList());
    }
    if (optimizeTask.getDeleteFiles() == null) {
      optimizeTask.setDeleteFiles(Collections.emptyList());
    }
    if (optimizeTask.getBaseFiles() == null) {
      optimizeTask.setBaseFiles(Collections.emptyList());
    }
    if (optimizeTask.getPosDeleteFiles() == null) {
      optimizeTask.setPosDeleteFiles(Collections.emptyList());
    }
  }

  private Map<TableIdentifier, TableOptimizeRuntime> loadTableOptimizeRuntimes() {
    Map<TableIdentifier, TableOptimizeRuntime> collector = new HashMap<>();
    List<TableOptimizeRuntime> tableOptimizeRuntimes = selectTableOptimizeRuntimes();
    for (TableOptimizeRuntime runtime : tableOptimizeRuntimes) {
      // TODO TEMP: force plan when arctic server restart
      runtime.setCurrentSnapshotId(TableOptimizeRuntime.INVALID_SNAPSHOT_ID);
      runtime.setCurrentChangeSnapshotId(TableOptimizeRuntime.INVALID_SNAPSHOT_ID);
      collector.put(runtime.getTableIdentifier(), runtime);
    }
    return collector;
  }

  private void deleteOptimizeRecord(TableIdentifier tableIdentifier) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      OptimizeHistoryMapper optimizeHistoryMapper =
          getMapper(sqlSession, OptimizeHistoryMapper.class);
      optimizeHistoryMapper.deleteOptimizeRecord(tableIdentifier);
    }
  }

  private void deleteOptimizeTaskHistory(TableIdentifier tableIdentifier) {
    ServiceContainer.getTableTaskHistoryService().deleteTaskHistory(tableIdentifier);
  }

  @Override
  public List<OptimizeHistory> getOptimizeHistory(TableIdentifier identifier) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      OptimizeHistoryMapper optimizeHistoryMapper =
          getMapper(sqlSession, OptimizeHistoryMapper.class);

      return optimizeHistoryMapper.selectOptimizeHistory(identifier);
    }
  }

  private List<BaseOptimizeTaskRuntime> selectAllOptimizeTaskRuntimes() {
    try (SqlSession sqlSession = getSqlSession(true)) {
      OptimizeTaskRuntimesMapper optimizeTaskRuntimesMapper =
          getMapper(sqlSession, OptimizeTaskRuntimesMapper.class);
      return optimizeTaskRuntimesMapper.selectAllOptimizeTaskRuntimes();
    }
  }

  private List<BaseOptimizeTask> selectAllOptimizeTasks() {
    try (SqlSession sqlSession = getSqlSession(true)) {
      OptimizeTasksMapper optimizeTasksMapper =
          getMapper(sqlSession, OptimizeTasksMapper.class);
      return optimizeTasksMapper.selectAllOptimizeTasks();
    }
  }

  // table runtime
  private void insertTableOptimizeRuntime(TableOptimizeRuntime tableOptimizeRuntime) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      TableOptimizeRuntimeMapper tableOptimizeRuntimeMapper =
          getMapper(sqlSession, TableOptimizeRuntimeMapper.class);
      tableOptimizeRuntimeMapper.insertTableOptimizeRuntime(tableOptimizeRuntime);
    }
  }

  private void deleteTableOptimizeRuntime(TableIdentifier tableIdentifier) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      TableOptimizeRuntimeMapper tableOptimizeRuntimeMapper =
          getMapper(sqlSession, TableOptimizeRuntimeMapper.class);
      tableOptimizeRuntimeMapper.deleteTableOptimizeRuntime(tableIdentifier);
    }
  }

  private List<TableOptimizeRuntime> selectTableOptimizeRuntimes() {
    try (SqlSession sqlSession = getSqlSession(true)) {
      TableOptimizeRuntimeMapper tableOptimizeRuntimeMapper =
          getMapper(sqlSession, TableOptimizeRuntimeMapper.class);
      return tableOptimizeRuntimeMapper.selectTableOptimizeRuntimes();
    }
  }
}
