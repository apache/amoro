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

import com.netease.arctic.AmsClient;
import com.netease.arctic.ams.api.AlreadyExistsException;
import com.netease.arctic.ams.api.Constants;
import com.netease.arctic.ams.api.DataFileInfo;
import com.netease.arctic.ams.api.ErrorMessage;
import com.netease.arctic.ams.api.OptimizeRangeType;
import com.netease.arctic.ams.api.OptimizeStatus;
import com.netease.arctic.ams.api.OptimizeTaskId;
import com.netease.arctic.ams.api.OptimizeTaskStat;
import com.netease.arctic.ams.api.OptimizeType;
import com.netease.arctic.ams.server.mapper.InternalTableFilesMapper;
import com.netease.arctic.ams.server.mapper.OptimizeHistoryMapper;
import com.netease.arctic.ams.server.mapper.OptimizeTaskRuntimesMapper;
import com.netease.arctic.ams.server.mapper.OptimizeTasksMapper;
import com.netease.arctic.ams.server.mapper.TableOptimizeRuntimeMapper;
import com.netease.arctic.ams.server.model.BaseOptimizeTask;
import com.netease.arctic.ams.server.model.BaseOptimizeTaskRuntime;
import com.netease.arctic.ams.server.model.CoreInfo;
import com.netease.arctic.ams.server.model.FilesStatistics;
import com.netease.arctic.ams.server.model.OptimizeHistory;
import com.netease.arctic.ams.server.model.TableMetadata;
import com.netease.arctic.ams.server.model.TableOptimizeInfo;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import com.netease.arctic.ams.server.service.IJDBCService;
import com.netease.arctic.ams.server.service.IQuotaService;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.ams.server.service.impl.FileInfoCacheService;
import com.netease.arctic.ams.server.utils.FilesStatisticsBuilder;
import com.netease.arctic.ams.server.utils.TableStatCollector;
import com.netease.arctic.ams.server.utils.UnKeyedTableUtil;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.data.DataFileType;
import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.hive.utils.TableTypeUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.CompatiblePropertyUtil;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.base.Predicate;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.util.PropertyUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class TableOptimizeItem extends IJDBCService {
  public static final Long META_EXPIRE_TIME = 60_000L;// 1min
  private static final Logger LOG = LoggerFactory.getLogger(TableOptimizeItem.class);

  private final TableIdentifier tableIdentifier;
  private volatile ArcticTable arcticTable;
  private TableOptimizeRuntime tableOptimizeRuntime;
  private FilesStatistics optimizeFileInfo;

  private final ReentrantLock tasksLock = new ReentrantLock();
  private final ReentrantLock tableLock = new ReentrantLock();
  private final ReentrantLock tasksCommitLock = new ReentrantLock();
  private final AtomicBoolean waitCommit = new AtomicBoolean(false);

  private final Map<OptimizeTaskId, OptimizeTaskItem> optimizeTasks = new LinkedHashMap<>();

  private volatile long metaRefreshTime;

  private final FileInfoCacheService fileInfoCacheService;
  private final IQuotaService quotaService;
  private final AmsClient metastoreClient;
  private volatile double quotaCache;
  private volatile String groupNameCache;
  private final Predicate<Long> snapshotIsCached = new Predicate<Long>() {
    @Override
    public boolean apply(@Nullable Long snapshotId) {
      return fileInfoCacheService.snapshotIsCached(tableIdentifier.buildTableIdentifier(),
          Constants.INNER_TABLE_BASE, snapshotId);
    }
  };

  /**
   * -1: not initialized
   * 0: not committed
   */
  private volatile long latestCommitTime = -1L;

  public TableOptimizeItem(ArcticTable arcticTable, TableMetadata tableMetadata) {
    this.arcticTable = arcticTable;
    this.metaRefreshTime = -1;
    this.tableOptimizeRuntime = new TableOptimizeRuntime(tableMetadata.getTableIdentifier());
    this.quotaCache = CompatiblePropertyUtil.propertyAsDouble(tableMetadata.getProperties(),
        TableProperties.SELF_OPTIMIZING_QUOTA,
        TableProperties.SELF_OPTIMIZING_QUOTA_DEFAULT);
    this.groupNameCache = CompatiblePropertyUtil.propertyAsString(tableMetadata.getProperties(),
        TableProperties.SELF_OPTIMIZING_GROUP,
        TableProperties.SELF_OPTIMIZING_GROUP_DEFAULT);
    this.tableIdentifier = tableMetadata.getTableIdentifier();
    this.fileInfoCacheService = ServiceContainer.getFileInfoCacheService();
    this.metastoreClient = ServiceContainer.getTableMetastoreHandler();
    this.quotaService = ServiceContainer.getQuotaService();
  }

  /**
   * Initial optimize tasks.
   *
   * @param optimizeTasks -
   */
  public void initOptimizeTasks(List<OptimizeTaskItem> optimizeTasks) {
    if (CollectionUtils.isNotEmpty(optimizeTasks)) {
      optimizeTasks
          .forEach(task -> this.optimizeTasks.put(task.getOptimizeTask().getTaskId(), task));
    }
  }

  /**
   * Initial TableOptimizeRuntime.
   *
   * @param runtime -
   * @return this for chain
   */
  public TableOptimizeItem initTableOptimizeRuntime(TableOptimizeRuntime runtime) {
    if (runtime != null) {
      this.tableOptimizeRuntime = runtime;
    } else if (this.tableOptimizeRuntime == null) {
      this.tableOptimizeRuntime = new TableOptimizeRuntime(tableIdentifier);
    }
    return this;
  }

  /**
   * if all tasks are Prepared
   *
   * @return true if tasks is not empty and all Prepared
   */
  public boolean allTasksPrepared() {
    if (!optimizeTasks.isEmpty()) {
      return optimizeTasks.values().stream().allMatch(t -> t.getOptimizeStatus() == OptimizeStatus.Prepared);
    } else {
      return false;
    }
  }

  /**
   * try trigger commit if all tasks are Prepared.
   */
  public void tryTriggerCommit() {
    tasksLock.lock();
    try {
      if (waitCommit.get()) {
        return;
      }
      if (!allTasksPrepared()) {
        return;
      }
      boolean success = ServiceContainer.getOptimizeService().triggerOptimizeCommit(this);
      if (success) {
        waitCommit.set(true);
      }
    } finally {
      tasksLock.unlock();
    }
  }

  /**
   * Get table identifier.
   *
   * @return TableIdentifier
   */
  public TableIdentifier getTableIdentifier() {
    return tableIdentifier;
  }

  /**
   * Get Arctic Table, refresh if expired.
   *
   * @return ArcticTable
   */
  public ArcticTable getArcticTable() {
    if (arcticTable == null) {
      tryRefresh(false);
    }
    return arcticTable;
  }

  /**
   * Get arcticTable, refresh immediately or not.
   *
   * @param forceRefresh - refresh immediately
   * @return ArcticTable
   */
  public ArcticTable getArcticTable(boolean forceRefresh) {
    tryRefresh(forceRefresh);
    return arcticTable;
  }

  /**
   * If arctic table is KeyedTable.
   *
   * @return true/false
   */
  public boolean isKeyedTable() {
    if (arcticTable == null) {
      tryRefresh(false);
    }
    return arcticTable.isKeyedTable();
  }

  /**
   * Get cached quota, cache will be updated when arctic table refresh.
   *
   * @return quota
   */
  public double getQuotaCache() {
    return quotaCache;
  }

  public long getLatestCommitTime() {
    if (latestCommitTime == -1L) {
      latestCommitTime = ServiceContainer.getOptimizeService().getLatestCommitTime(tableIdentifier);
    }
    return latestCommitTime;
  }

  public String getGroupNameCache() {
    return groupNameCache;
  }

  private void tryRefresh(boolean force) {
    if (force || isMetaExpired() || arcticTable == null) {
      tableLock.lock();
      try {
        if (force || isMetaExpired() || arcticTable == null) {
          refresh();
        }
      } finally {
        tableLock.unlock();
      }
    }
  }

  private void refresh() {
    ArcticCatalog catalog = CatalogLoader.load(metastoreClient, tableIdentifier.getCatalog());
    this.arcticTable = catalog.loadTable(tableIdentifier);
    this.metaRefreshTime = System.currentTimeMillis();
    this.quotaCache = CompatiblePropertyUtil.propertyAsDouble(arcticTable.properties(),
        TableProperties.SELF_OPTIMIZING_QUOTA,
        TableProperties.SELF_OPTIMIZING_QUOTA_DEFAULT);
    this.groupNameCache = CompatiblePropertyUtil.propertyAsString(arcticTable.properties(),
        TableProperties.SELF_OPTIMIZING_GROUP,
        TableProperties.SELF_OPTIMIZING_GROUP_DEFAULT);
  }

  private int optimizeMaxRetry() {
    return CompatiblePropertyUtil
        .propertyAsInt(getArcticTable(false).properties(), TableProperties.SELF_OPTIMIZING_RETRY_NUMBER,
            TableProperties.SELF_OPTIMIZING_RETRY_NUMBER_DEFAULT);
  }

  private boolean isMetaExpired() {
    return System.currentTimeMillis() > metaRefreshTime + META_EXPIRE_TIME;
  }

  /**
   * Update optimize task result, Failed or Prepared.
   *
   * @param optimizeTaskStat - optimizeTaskStat
   */
  public void updateOptimizeTaskStat(OptimizeTaskStat optimizeTaskStat) {
    Objects.requireNonNull(optimizeTaskStat, "optimizeTaskStat can't be null");
    Objects.requireNonNull(optimizeTaskStat.getTaskId(), "optimizeTaskId can't be null");

    OptimizeTaskItem optimizeTaskItem = optimizeTasks.get(optimizeTaskStat.getTaskId());
    Preconditions.checkNotNull(optimizeTaskItem, "can't find optimize task " + optimizeTaskStat.getTaskId());
    LOG.info("{} task {} ==== updateMajorOptimizeTaskStat, commitGroup = {}, status = {}, attemptId={}",
        optimizeTaskItem.getTableIdentifier(), optimizeTaskItem.getOptimizeTask().getTaskId(),
        optimizeTaskItem.getOptimizeTask().getTaskCommitGroup(), optimizeTaskStat.getStatus(),
        optimizeTaskStat.getAttemptId());
    Preconditions.checkArgument(
        Objects.equals(optimizeTaskStat.getAttemptId(), optimizeTaskItem.getOptimizeRuntime().getAttemptId()),
        "wrong attemptId " + optimizeTaskStat.getAttemptId() + " valid attemptId " +
            optimizeTaskItem.getOptimizeRuntime().getAttemptId());
    switch (optimizeTaskStat.getStatus()) {
      case Failed:
        optimizeTaskItem.onFailed(optimizeTaskStat.getErrorMessage(), optimizeTaskStat.getCostTime());
        break;
      case Prepared:
        List<ByteBuffer> targetFiles = optimizeTaskStat.getFiles();
        long targetFileSize = optimizeTaskStat.getNewFileSize();
        // if minor optimize, insert files as base new files
        if (optimizeTaskItem.getOptimizeTask().getTaskId().getType() == OptimizeType.Minor &&
            !com.netease.arctic.utils.TableTypeUtil.isIcebergTableFormat(getArcticTable())) {
          if (optimizeTaskItem.getOptimizeTask().getInsertFiles() == null ||
              optimizeTaskItem.getOptimizeTask().getInsertFileCnt() !=
                  optimizeTaskItem.getOptimizeTask().getInsertFiles().size()) {
            optimizeTaskItem.setFiles();
          }
          // check whether insert files don't change, confirm data consistency
          if (optimizeTaskItem.getOptimizeTask().getInsertFiles() != null &&
              optimizeTaskItem.getOptimizeTask().getInsertFileCnt() !=
              optimizeTaskItem.getOptimizeTask().getInsertFiles().size()) {
            String errorMessage =
                String.format("table %s insert files changed in minor optimize task %s, can't prepared.",
                    optimizeTaskItem.getTableIdentifier(), optimizeTaskItem.getOptimizeTask().getTaskId().getTraceId());
            throw new IllegalStateException(errorMessage);
          }
          targetFiles.addAll(optimizeTaskItem.getOptimizeTask().getInsertFiles());
          targetFileSize = targetFileSize + optimizeTaskItem.getOptimizeTask().getInsertFileSize();
        }
        optimizeTaskItem.onPrepared(optimizeTaskStat.getReportTime(),
            targetFiles, targetFileSize, optimizeTaskStat.getCostTime());
        tryTriggerCommit();
        break;
      default:
        throw new IllegalArgumentException("unsupported status: " + optimizeTaskStat.getStatus());
    }
  }

  /**
   * Build current table optimize info.
   *
   * @return TableOptimizeInfo
   */
  public TableOptimizeInfo buildTableOptimizeInfo() {
    CoreInfo tableResourceInfo = quotaService.getTableResourceInfo(tableIdentifier, 3600 * 1000);
    double needCoreCount = tableResourceInfo.getNeedCoreCount();
    double realCoreCount = tableResourceInfo.getRealCoreCount();
    TableOptimizeInfo tableOptimizeInfo = new TableOptimizeInfo(tableIdentifier);
    TableOptimizeRuntime tableOptimizeRuntime = getTableOptimizeRuntime();
    tableOptimizeInfo.setOptimizeStatus(tableOptimizeRuntime.getOptimizeStatus().displayValue());
    tableOptimizeInfo.setDuration(System.currentTimeMillis() - tableOptimizeRuntime.getOptimizeStatusStartTime());
    tableOptimizeInfo.setQuota(needCoreCount);
    double value = realCoreCount / needCoreCount;
    tableOptimizeInfo.setQuotaOccupation(new BigDecimal(value).setScale(4, RoundingMode.HALF_UP).doubleValue());
    if (tableOptimizeRuntime.getOptimizeStatus() == TableOptimizeRuntime.OptimizeStatus.FullOptimizing) {
      List<BaseOptimizeTask> optimizeTasks =
          this.optimizeTasks.values().stream().map(OptimizeTaskItem::getOptimizeTask).collect(
              Collectors.toList());
      this.optimizeFileInfo = collectOptimizeFileInfo(optimizeTasks, OptimizeType.FullMajor);
    } else if (tableOptimizeRuntime.getOptimizeStatus() == TableOptimizeRuntime.OptimizeStatus.MajorOptimizing) {
      List<BaseOptimizeTask> optimizeTasks =
          this.optimizeTasks.values().stream().map(OptimizeTaskItem::getOptimizeTask).collect(
              Collectors.toList());
      this.optimizeFileInfo = collectOptimizeFileInfo(optimizeTasks, OptimizeType.Major);
    } else if (tableOptimizeRuntime.getOptimizeStatus() == TableOptimizeRuntime.OptimizeStatus.MinorOptimizing) {
      List<BaseOptimizeTask> optimizeTasks =
          this.optimizeTasks.values().stream().map(OptimizeTaskItem::getOptimizeTask).collect(
              Collectors.toList());
      this.optimizeFileInfo = collectOptimizeFileInfo(optimizeTasks, OptimizeType.Minor);
    }
    if (this.optimizeFileInfo != null) {
      tableOptimizeInfo.setFileCount(this.optimizeFileInfo.getFileCnt());
      tableOptimizeInfo.setFileSize(this.optimizeFileInfo.getTotalSize());
    }
    tableOptimizeInfo.setGroupName(groupNameCache);
    return tableOptimizeInfo;
  }

  /**
   * Refresh and update table optimize status.
   */
  public void updateTableOptimizeStatus() {
    if (!this.optimizeTasks.isEmpty()) {
      tasksLock.lock();
      try {
        if (!this.optimizeTasks.isEmpty()) {
          List<BaseOptimizeTask> optimizeTasks =
              this.optimizeTasks.values().stream().map(OptimizeTaskItem::getOptimizeTask).collect(
                  Collectors.toList());
          if (hasFullOptimizeTask()) {
            tryUpdateOptimizeInfo(
                TableOptimizeRuntime.OptimizeStatus.FullOptimizing, optimizeTasks, OptimizeType.FullMajor);
          } else if (hasMajorOptimizeTask()) {
            tryUpdateOptimizeInfo(
                TableOptimizeRuntime.OptimizeStatus.MajorOptimizing, optimizeTasks, OptimizeType.Major);
          } else {
            tryUpdateOptimizeInfo(
                TableOptimizeRuntime.OptimizeStatus.MinorOptimizing, optimizeTasks, OptimizeType.Minor);
          }
          return;
        }
      } finally {
        tasksLock.unlock();
      }
    }
    // if optimizeTasks is empty
    if (!CompatiblePropertyUtil
        .propertyAsBoolean(getArcticTable(false).properties(), TableProperties.ENABLE_SELF_OPTIMIZING,
            TableProperties.ENABLE_SELF_OPTIMIZING_DEFAULT)) {
      tryUpdateOptimizeInfo(TableOptimizeRuntime.OptimizeStatus.Idle, Collections.emptyList(), null);
    } else {
      Map<String, Boolean> partitionIsRunning = generatePartitionRunning();
      if (com.netease.arctic.utils.TableTypeUtil.isIcebergTableFormat(getArcticTable())) {
        List<FileScanTask> fileScanTasks;
        try (CloseableIterable<FileScanTask> filesIterable = arcticTable.asUnkeyedTable().newScan().planFiles()) {
          fileScanTasks = Lists.newArrayList(filesIterable);
        } catch (IOException e) {
          throw new UncheckedIOException("Failed to close table scan of " + tableIdentifier, e);
        }
        IcebergFullOptimizePlan fullPlan =
            getIcebergFullPlan(fileScanTasks, -1, System.currentTimeMillis(), partitionIsRunning);
        List<BaseOptimizeTask> fullTasks = fullPlan.plan();
        // pending for full optimize
        if (CollectionUtils.isNotEmpty(fullTasks)) {
          tryUpdateOptimizeInfo(
              TableOptimizeRuntime.OptimizeStatus.Pending, fullTasks, OptimizeType.FullMajor);
        } else {
          IcebergMinorOptimizePlan minorPlan =
              getIcebergMinorPlan(fileScanTasks, -1, System.currentTimeMillis(), partitionIsRunning);
          List<BaseOptimizeTask> minorTasks = minorPlan.plan();
          // pending for minor optimize
          if (CollectionUtils.isNotEmpty(minorTasks)) {
            tryUpdateOptimizeInfo(
                TableOptimizeRuntime.OptimizeStatus.Pending, minorTasks, OptimizeType.Minor);
          } else {
            // idle state
            tryUpdateOptimizeInfo(TableOptimizeRuntime.OptimizeStatus.Idle, Collections.emptyList(), null);
          }
        }
      } else {
        FullOptimizePlan fullPlan = getFullPlan(-1, System.currentTimeMillis(), partitionIsRunning);
        List<BaseOptimizeTask> fullTasks = fullPlan.plan();
        // pending for full optimize
        if (CollectionUtils.isNotEmpty(fullTasks)) {
          tryUpdateOptimizeInfo(
              TableOptimizeRuntime.OptimizeStatus.Pending, fullTasks, OptimizeType.FullMajor);
        } else {
          MajorOptimizePlan majorPlan = getMajorPlan(-1, System.currentTimeMillis(), partitionIsRunning);
          List<BaseOptimizeTask> majorTasks = majorPlan.plan();
          // pending for major optimize
          if (CollectionUtils.isNotEmpty(majorTasks)) {
            tryUpdateOptimizeInfo(
                TableOptimizeRuntime.OptimizeStatus.Pending, majorTasks, OptimizeType.Major);
          } else {
            if (isKeyedTable()) {
              MinorOptimizePlan minorPlan = getMinorPlan(-1, System.currentTimeMillis(), partitionIsRunning);
              List<BaseOptimizeTask> minorTasks = minorPlan.plan();
              if (CollectionUtils.isNotEmpty(minorTasks)) {
                tryUpdateOptimizeInfo(
                    TableOptimizeRuntime.OptimizeStatus.Pending, minorTasks, OptimizeType.Minor);
                return;
              }
            }
            // idle state
            tryUpdateOptimizeInfo(TableOptimizeRuntime.OptimizeStatus.Idle, Collections.emptyList(), null);
          }
        }
      }
    }
  }

  public void checkOptimizeGroup() {
    try {
      Set<TableIdentifier> tablesOfQueue =
          ServiceContainer.getOptimizeQueueService().getTablesOfQueue(this.groupNameCache);
      if (!tablesOfQueue.contains(this.tableIdentifier)) {
        ServiceContainer.getOptimizeQueueService().release(this.tableIdentifier);
        ServiceContainer.getOptimizeQueueService().bind(tableIdentifier, this.groupNameCache);
      }
    } catch (Exception e) {
      LOG.error("checkOptimizeGroup error", e);
    }
  }

  private boolean hasMajorOptimizeTask() {
    for (Map.Entry<OptimizeTaskId, OptimizeTaskItem> entry : optimizeTasks.entrySet()) {
      OptimizeTaskId key = entry.getKey();
      if (key.getType() == OptimizeType.Major) {
        return true;
      }
    }
    return false;
  }

  private boolean hasFullOptimizeTask() {
    for (Map.Entry<OptimizeTaskId, OptimizeTaskItem> entry : optimizeTasks.entrySet()) {
      OptimizeTaskId key = entry.getKey();
      if (key.getType() == OptimizeType.FullMajor) {
        return true;
      }
    }
    return false;
  }

  private FilesStatistics collectOptimizeFileInfo(Collection<BaseOptimizeTask> tasks, OptimizeType optimizeType) {
    FilesStatisticsBuilder builder = new FilesStatisticsBuilder();
    for (BaseOptimizeTask task : tasks) {
      if (task.getTaskId().getType().equals(optimizeType)) {
        builder.addFiles(task.getBaseFileSize(), task.getBaseFileCnt());
        builder.addFiles(task.getInsertFileSize(), task.getInsertFileCnt());
        builder.addFiles(task.getDeleteFileSize(), task.getDeleteFileCnt());
        builder.addFiles(task.getPosDeleteFileSize(), task.getPosDeleteFileCnt());
      }
    }
    return builder.build();
  }

  private void tryUpdateOptimizeInfo(TableOptimizeRuntime.OptimizeStatus optimizeStatus,
      Collection<BaseOptimizeTask> optimizeTasks, OptimizeType optimizeType) {
    if (tableOptimizeRuntime.getOptimizeStatus() != optimizeStatus) {
      tableOptimizeRuntime.setOptimizeStatus(optimizeStatus);
      tableOptimizeRuntime.setOptimizeStatusStartTime(System.currentTimeMillis());
      try {
        persistTableOptimizeRuntime();
      } catch (Throwable t) {
        LOG.warn("failed to persist tableOptimizeRuntime when update OptimizeStatus, ignore", t);
      }
      optimizeFileInfo = collectOptimizeFileInfo(optimizeTasks, optimizeType);
    }
    if (tableOptimizeRuntime.getOptimizeStatusStartTime() <= 0) {
      long createTime = PropertyUtil.propertyAsLong(getArcticTable().properties(), TableProperties.TABLE_CREATE_TIME,
          TableProperties.TABLE_CREATE_TIME_DEFAULT);
      if (createTime != tableOptimizeRuntime.getOptimizeStatusStartTime()) {
        tableOptimizeRuntime.setOptimizeStatusStartTime(createTime);
        persistTableOptimizeRuntime();
      }
    }
  }

  /**
   * Add new optimize tasks.
   *
   * @param newOptimizeTasks new optimize tasks
   * @throws AlreadyExistsException when task already exists
   */
  public void addNewOptimizeTasks(List<BaseOptimizeTask> newOptimizeTasks)
      throws AlreadyExistsException {
    // for rollback
    Set<OptimizeTaskId> addedOptimizeTaskIds = new HashSet<>();
    tasksLock.lock();
    try {
      for (BaseOptimizeTask optimizeTask : newOptimizeTasks) {
        BaseOptimizeTaskRuntime optimizeRuntime = new BaseOptimizeTaskRuntime(optimizeTask.getTaskId());
        OptimizeTaskItem optimizeTaskItem = new OptimizeTaskItem(optimizeTask, optimizeRuntime);
        if (optimizeTasks.putIfAbsent(optimizeTask.getTaskId(), optimizeTaskItem) != null) {
          throw new AlreadyExistsException(optimizeTask.getTaskId() + " already exists");
        }
        optimizeTaskItem.persistOptimizeTask();
        addedOptimizeTaskIds.add(optimizeTask.getTaskId());
        LOG.info("{} add new task {}", tableIdentifier, optimizeTask);
        // when minor optimize, there is no need to execute task not contains deleteFiles or not contains any dataFiles,
        // for no deleteFiles the inertFiles need to commit to base table
        // for no dataFiles the txId in base properties need to update
        boolean minorNotNeedExecute = optimizeTask.getDeleteFiles().isEmpty() ||
            (optimizeTask.getBaseFiles().isEmpty() && optimizeTask.getInsertFiles().isEmpty());
        if (minorNotNeedExecute && optimizeTask.getTaskId().getType().equals(OptimizeType.Minor) &&
            !com.netease.arctic.utils.TableTypeUtil.isIcebergTableFormat(arcticTable)) {
          optimizeTaskItem.onPrepared(System.currentTimeMillis(),
              optimizeTask.getInsertFiles(), optimizeTask.getInsertFileSize(), 0L);
        }
        optimizeTaskItem.clearFiles();
      }
      updateTableOptimizeStatus();
      tryTriggerCommit();
    } catch (Throwable t) {
      // rollback
      for (OptimizeTaskId addedOptimizeTaskId : addedOptimizeTaskIds) {
        OptimizeTaskItem removed = optimizeTasks.remove(addedOptimizeTaskId);
        if (removed != null) {
          removed.clearOptimizeTask();
        }
      }
      throw t;
    } finally {
      tasksLock.unlock();
    }
  }

  private void optimizeTasksClear(BaseOptimizeCommit optimizeCommit) {
    try (SqlSession sqlSession = getSqlSession(false)) {
      Map<String, List<OptimizeTaskItem>> tasks = optimizeCommit.getCommittedTasks();

      OptimizeTasksMapper optimizeTasksMapper =
          getMapper(sqlSession, OptimizeTasksMapper.class);
      InternalTableFilesMapper internalTableFilesMapper =
          getMapper(sqlSession, InternalTableFilesMapper.class);
      TableOptimizeRuntimeMapper tableOptimizeRuntimeMapper =
          getMapper(sqlSession, TableOptimizeRuntimeMapper.class);

      try {
        // persist partition optimize time
        tableOptimizeRuntimeMapper.updateTableOptimizeRuntime(tableOptimizeRuntime);
      } catch (Throwable t) {
        LOG.warn("failed to persist tableOptimizeRuntime after commit, ignore. " + getTableIdentifier(), t);
        sqlSession.rollback(true);
      }

      tasksLock.lock();
      List<OptimizeTaskItem> removedList = new ArrayList<>();
      try {
        tasks.values().stream().flatMap(Collection::stream).map(OptimizeTaskItem::getTaskId)
            .forEach(optimizeTaskId -> {
              OptimizeTaskItem removed = optimizeTasks.remove(optimizeTaskId);
              if (removed != null) {
                removedList.add(removed);
                optimizeTasksMapper.deleteOptimizeTask(optimizeTaskId.getTraceId());
                internalTableFilesMapper.deleteOptimizeTaskFile(optimizeTaskId);
              }
              LOG.info("{} removed", optimizeTaskId);
            });
      } catch (Throwable t) {
        for (OptimizeTaskItem optimizeTaskItem : removedList) {
          optimizeTasks.put(optimizeTaskItem.getTaskId(), optimizeTaskItem);
        }
        LOG.warn("failed to remove optimize task after commit, ignore. " + getTableIdentifier(),
            t);
        sqlSession.rollback(true);
      } finally {
        tasksLock.unlock();
      }

      sqlSession.commit(true);
    }
  }

  private void optimizeTasksCommitted(BaseOptimizeCommit optimizeCommit,
                                      long commitTime) {
    try (SqlSession sqlSession = getSqlSession(false)) {
      Map<String, List<OptimizeTaskItem>> tasks = optimizeCommit.getCommittedTasks();
      Map<String, OptimizeType> optimizeTypMap = optimizeCommit.getPartitionOptimizeType();

      // commit
      OptimizeTasksMapper optimizeTasksMapper =
          getMapper(sqlSession, OptimizeTasksMapper.class);
      OptimizeTaskRuntimesMapper optimizeTaskRuntimesMapper =
          getMapper(sqlSession, OptimizeTaskRuntimesMapper.class);
      InternalTableFilesMapper internalTableFilesMapper =
          getMapper(sqlSession, InternalTableFilesMapper.class);
      TableOptimizeRuntimeMapper tableOptimizeRuntimeMapper =
          getMapper(sqlSession, TableOptimizeRuntimeMapper.class);
      OptimizeHistoryMapper optimizeHistoryMapper =
          getMapper(sqlSession, OptimizeHistoryMapper.class);

      try {
        tasks.values().stream().flatMap(Collection::stream)
            .forEach(taskItem -> {
              BaseOptimizeTaskRuntime newRuntime = taskItem.getOptimizeRuntime().clone();
              newRuntime.setCommitTime(commitTime);
              newRuntime.setStatus(OptimizeStatus.Committed);
              // after commit, task will be deleted, there is no need to update
              optimizeTaskRuntimesMapper.updateOptimizeTaskRuntime(newRuntime);
              taskItem.setOptimizeRuntime(newRuntime);
            });
      } catch (Exception e) {
        LOG.warn("failed to persist taskOptimizeRuntime after commit, ignore. " + getTableIdentifier(), e);
        sqlSession.rollback(true);
      }

      tasks.keySet().forEach(
          partition -> {
            OptimizeType optimizeType = optimizeTypMap.get(partition);
            switch (optimizeType) {
              case Minor:
                tableOptimizeRuntime.putLatestMinorOptimizeTime(partition, commitTime);
                break;
              case Major:
                tableOptimizeRuntime.putLatestMajorOptimizeTime(partition, commitTime);
                break;
              case FullMajor:
                tableOptimizeRuntime.putLatestFullOptimizeTime(partition, commitTime);
                break;
            }
          });

      try {
        // persist optimize task history
        OptimizeHistory record = buildOptimizeRecord(tasks, commitTime);
        optimizeHistoryMapper.insertOptimizeHistory(record);

        // update the latest commit time in memory
        latestCommitTime = Math.max(latestCommitTime, commitTime);
      } catch (Throwable t) {
        LOG.warn("failed to persist optimize history after commit, ignore. " + getTableIdentifier(), t);
        sqlSession.rollback(true);
      }

      try {
        // persist partition optimize time
        tableOptimizeRuntimeMapper.updateTableOptimizeRuntime(tableOptimizeRuntime);
      } catch (Throwable t) {
        LOG.warn("failed to persist tableOptimizeRuntime after commit, ignore. " + getTableIdentifier(), t);
        sqlSession.rollback(true);
      }

      tasksLock.lock();
      List<OptimizeTaskItem> removedList = new ArrayList<>();
      try {
        tasks.values().stream().flatMap(Collection::stream).map(OptimizeTaskItem::getTaskId)
            .forEach(optimizeTaskId -> {
              OptimizeTaskItem removed = optimizeTasks.remove(optimizeTaskId);
              if (removed != null) {
                removedList.add(removed);
                optimizeTasksMapper.deleteOptimizeTask(optimizeTaskId.getTraceId());
                internalTableFilesMapper.deleteOptimizeTaskFile(optimizeTaskId);
              }
              LOG.info("{} removed", optimizeTaskId);
            });
      } catch (Throwable t) {
        for (OptimizeTaskItem optimizeTaskItem : removedList) {
          optimizeTasks.put(optimizeTaskItem.getTaskId(), optimizeTaskItem);
        }
        LOG.warn("failed to remove optimize task after commit, ignore. " + getTableIdentifier(),
            t);
        sqlSession.rollback(true);
      } finally {
        tasksLock.unlock();
      }

      sqlSession.commit(true);
    }

    updateTableOptimizeStatus();
  }

  private OptimizeHistory buildOptimizeRecord(Map<String, List<OptimizeTaskItem>> tasks, long commitTime) {
    OptimizeHistory record = new OptimizeHistory();
    record.setTableIdentifier(getTableIdentifier());
    record.setOptimizeRange(OptimizeRangeType.Partition);
    record.setCommitTime(commitTime);
    Long minPlanTime = tasks.entrySet().stream().flatMap(entry -> entry.getValue().stream())
        .map(OptimizeTaskItem::getOptimizeTask).map(BaseOptimizeTask::getCreateTime)
        .min(Long::compare).orElse(0L);
    record.setOptimizeType(tasks.entrySet().iterator().next().getValue().get(0)
        .getOptimizeTask().getTaskId().getType());
    record.setPlanTime(minPlanTime);
    record.setVisibleTime(commitTime);
    record.setDuration(record.getCommitTime() - record.getPlanTime());
    FilesStatisticsBuilder insertFb = new FilesStatisticsBuilder();
    FilesStatisticsBuilder deleteFb = new FilesStatisticsBuilder();
    FilesStatisticsBuilder baseFb = new FilesStatisticsBuilder();
    FilesStatisticsBuilder targetFb = new FilesStatisticsBuilder();
    FilesStatisticsBuilder posDeleteFb = new FilesStatisticsBuilder();
    tasks.values()
        .forEach(list -> list
            .forEach(t -> {
              BaseOptimizeTask task = t.getOptimizeTask();
              insertFb.addFiles(task.getInsertFileSize(), task.getInsertFileCnt());
              deleteFb.addFiles(task.getDeleteFileSize(), task.getDeleteFileCnt());
              baseFb.addFiles(task.getBaseFileSize(), task.getBaseFileCnt());
              posDeleteFb.addFiles(task.getPosDeleteFileSize(), task.getPosDeleteFileCnt());
              BaseOptimizeTaskRuntime runtime = t.getOptimizeRuntime();
              targetFb.addFiles(runtime.getNewFileSize(), runtime.getNewFileCnt());
            }));
    record.setInsertFilesStatBeforeOptimize(insertFb.build());
    record.setDeleteFilesStatBeforeOptimize(deleteFb.build());
    record.setBaseFilesStatBeforeOptimize(baseFb.build());
    record.setPosDeleteFilesStatBeforeOptimize(posDeleteFb.build());

    FilesStatistics totalFs = new FilesStatisticsBuilder()
        .addFilesStatistics(record.getInsertFilesStatBeforeOptimize())
        .addFilesStatistics(record.getDeleteFilesStatBeforeOptimize())
        .addFilesStatistics(record.getBaseFilesStatBeforeOptimize())
        .addFilesStatistics(record.getPosDeleteFilesStatBeforeOptimize())
        .build();
    record.setTotalFilesStatBeforeOptimize(totalFs);
    record.setTotalFilesStatAfterOptimize(targetFb.build());

    record.setPartitionCnt(tasks.keySet().size());
    record.setPartitions(String.join(",", tasks.keySet()));
    if (isKeyedTable()) {
      KeyedTable keyedHiveTable = getArcticTable(true).asKeyedTable();
      record.setSnapshotInfo(TableStatCollector.buildBaseTableSnapshotInfo(keyedHiveTable.baseTable()));
      record.setBaseTableMaxTransactionId(TablePropertyUtil.getPartitionMaxTransactionId(keyedHiveTable).toString());
    } else {
      getArcticTable(true);
      record.setSnapshotInfo(TableStatCollector.buildBaseTableSnapshotInfo(getArcticTable(true).asUnkeyedTable()));
    }
    return record;
  }

  /**
   * Clear all optimize tasks.
   */
  public void clearOptimizeTasks() {
    tasksLock.lock();
    try {
      HashSet<OptimizeTaskItem> toRemoved = new HashSet<>(optimizeTasks.values());
      optimizeTasks.clear();
      Set<String> removedTaskHistory = new HashSet<>();
      for (OptimizeTaskItem task : toRemoved) {
        task.clearOptimizeTask();
        removedTaskHistory.add(task.getOptimizeTask().getTaskPlanGroup());
      }
      for (String taskPlanGroup : removedTaskHistory) {
        ServiceContainer.getTableTaskHistoryService().deleteTaskHistoryWithPlanGroup(tableIdentifier, taskPlanGroup);
      }
      LOG.info("{} clear all optimize tasks", getTableIdentifier());
      updateTableOptimizeStatus();
    } finally {
      tasksLock.unlock();
    }
  }

  /**
   * GetOptimizeTasksToExecute
   * include Init, Failed.
   *
   * @return List of OptimizeTaskItem
   */
  public List<OptimizeTaskItem> getOptimizeTasksToExecute() {
    // lock for conflict with add new tasks, because files with be removed from OptimizeTask after tasks added
    tasksLock.lock();
    try {
      return optimizeTasks.values().stream()
          .filter(taskItem -> taskItem.canExecute(this::optimizeMaxRetry))
          .sorted(Comparator.comparingLong(o -> o.getOptimizeTask().getCreateTime()))
          .collect(Collectors.toList());
    } finally {
      tasksLock.unlock();
    }
  }

  /**
   * If task execute timeout, set it to be Failed.
   */
  public void checkTaskExecuteTimeout() {
    tasksLock.lock();
    try {
      optimizeTasks.values().stream().filter(OptimizeTaskItem::executeTimeout)
          .forEach(task -> {
            task.onFailed(new ErrorMessage(System.currentTimeMillis(), "execute expired"),
                System.currentTimeMillis() - task.getOptimizeRuntime().getExecuteTime());
            LOG.error("{} execute timeout, change to Failed", task.getTaskId());
          });
    } finally {
      tasksLock.unlock();
    }
  }

  /**
   * Get tasks which is ready to commit (only if all tasks in a table is ready).
   *
   * @return map partition -> tasks of partition
   */
  public Map<String, List<OptimizeTaskItem>> getOptimizeTasksToCommit() {
    tasksLock.lock();
    try {
      Map<String, List<OptimizeTaskItem>> collector = new HashMap<>();
      for (OptimizeTaskItem optimizeTaskItem : optimizeTasks.values()) {
        String partition = optimizeTaskItem.getOptimizeTask().getPartition();
        if (!optimizeTaskItem.canCommit()) {
          collector.clear();
          break;
        }
        optimizeTaskItem.setFiles();
        collector.computeIfAbsent(partition, p -> new ArrayList<>()).add(optimizeTaskItem);
      }
      return collector;
    } finally {
      tasksLock.unlock();
    }
  }

  public void setTableCanCommit() {
    waitCommit.set(false);
  }

  /**
   * Commit optimize tasks.
   *
   * @throws Exception -
   */
  public void commitOptimizeTasks() throws Exception {
    tasksCommitLock.lock();

    // check current base table snapshot whether changed when minor optimize
    if (isMinorOptimizing() && !com.netease.arctic.utils.TableTypeUtil.isIcebergTableFormat(getArcticTable())) {
      if (tableOptimizeRuntime.getCurrentSnapshotId() !=
          UnKeyedTableUtil.getSnapshotId(getArcticTable().asKeyedTable().baseTable())) {
        LOG.info("the latest snapshot has changed in base table {}, give up commit.", tableIdentifier);
        clearOptimizeTasks();
      }
    }

    try {
      Map<String, List<OptimizeTaskItem>> tasksToCommit = getOptimizeTasksToCommit();
      long taskCount = tasksToCommit.values().stream().mapToLong(Collection::size).sum();
      if (MapUtils.isNotEmpty(tasksToCommit)) {
        LOG.info("{} get {} tasks of {} partitions to commit", tableIdentifier, taskCount, tasksToCommit.size());
        BaseOptimizeCommit optimizeCommit;
        if (com.netease.arctic.utils.TableTypeUtil.isIcebergTableFormat(getArcticTable())) {
          optimizeCommit = new IcebergOptimizeCommit(getArcticTable(true), tasksToCommit);
        } else if (TableTypeUtil.isHive(getArcticTable())) {
          optimizeCommit = new SupportHiveCommit(getArcticTable(true),
              tasksToCommit, OptimizeTaskItem::persistTargetFiles);
        } else {
          optimizeCommit = new BaseOptimizeCommit(getArcticTable(true), tasksToCommit);
        }

        boolean committed = optimizeCommit.commit(tableOptimizeRuntime.getCurrentSnapshotId());
        if (committed) {
          long commitTime = System.currentTimeMillis();
          optimizeTasksCommitted(optimizeCommit, commitTime);
        } else {
          optimizeTasksClear(optimizeCommit);
        }
      } else {
        LOG.info("{} get no tasks to commit", tableIdentifier);
      }
    } finally {
      tasksCommitLock.unlock();
    }
  }

  /**
   * Get all optimize tasks.
   *
   * @return list of all optimize tasks
   */
  public List<OptimizeTaskItem> getOptimizeTasks() {
    return new ArrayList<>(optimizeTasks.values());
  }

  /**
   * Get full optimize plan for arctic tables.
   *
   * @param queueId     -
   * @param currentTime -
   * @return -
   */
  public FullOptimizePlan getFullPlan(int queueId, long currentTime, Map<String, Boolean> partitionIsRunning) {
    List<DataFileInfo> baseTableFiles =
        fileInfoCacheService.getOptimizeDatafiles(tableIdentifier.buildTableIdentifier(), Constants.INNER_TABLE_BASE);
    List<DataFileInfo> baseFiles = filterFile(baseTableFiles, DataFileType.BASE_FILE);
    baseFiles.addAll(filterFile(baseTableFiles, DataFileType.INSERT_FILE));
    List<DataFileInfo> posDeleteFiles = filterFile(baseTableFiles, DataFileType.POS_DELETE_FILE);

    if (getArcticTable() instanceof SupportHive) {
      return new SupportHiveFullOptimizePlan(getArcticTable(), tableOptimizeRuntime,
          baseFiles, posDeleteFiles, partitionIsRunning, queueId, currentTime, snapshotIsCached);
    } else {
      return new FullOptimizePlan(getArcticTable(), tableOptimizeRuntime,
          baseFiles, posDeleteFiles, partitionIsRunning, queueId, currentTime, snapshotIsCached);
    }
  }

  /**
   * Get major optimize plan for arctic tables.
   *
   * @param queueId     -
   * @param currentTime -
   * @return -
   */
  public MajorOptimizePlan getMajorPlan(int queueId, long currentTime, Map<String, Boolean> partitionIsRunning) {
    List<DataFileInfo> baseTableFiles =
        fileInfoCacheService.getOptimizeDatafiles(tableIdentifier.buildTableIdentifier(), Constants.INNER_TABLE_BASE);
    List<DataFileInfo> baseFiles = filterFile(baseTableFiles, DataFileType.BASE_FILE);
    baseFiles.addAll(filterFile(baseTableFiles, DataFileType.INSERT_FILE));
    List<DataFileInfo> posDeleteFiles = filterFile(baseTableFiles, DataFileType.POS_DELETE_FILE);

    if (getArcticTable() instanceof SupportHive) {
      return new SupportHiveMajorOptimizePlan(getArcticTable(), tableOptimizeRuntime,
          baseFiles, posDeleteFiles, partitionIsRunning, queueId, currentTime, snapshotIsCached);
    } else {
      return new MajorOptimizePlan(getArcticTable(), tableOptimizeRuntime,
          baseFiles, posDeleteFiles, partitionIsRunning, queueId, currentTime, snapshotIsCached);
    }
  }

  /**
   * Get minor optimize plan for arctic tables.
   *
   * @param queueId     -
   * @param currentTime -
   * @return -
   */
  public MinorOptimizePlan getMinorPlan(int queueId, long currentTime, Map<String, Boolean> partitionIsRunning) {
    List<DataFileInfo> baseTableFiles =
        fileInfoCacheService.getOptimizeDatafiles(tableIdentifier.buildTableIdentifier(), Constants.INNER_TABLE_BASE);
    List<DataFileInfo> baseFiles = filterFile(baseTableFiles, DataFileType.BASE_FILE);
    baseFiles.addAll(filterFile(baseTableFiles, DataFileType.INSERT_FILE));
    List<DataFileInfo> posDeleteFiles = filterFile(baseTableFiles, DataFileType.POS_DELETE_FILE);

    List<DataFileInfo> changeTableFiles =
        fileInfoCacheService.getOptimizeDatafiles(tableIdentifier.buildTableIdentifier(), Constants.INNER_TABLE_CHANGE);

    return new MinorOptimizePlan(getArcticTable(), tableOptimizeRuntime, baseFiles, changeTableFiles, posDeleteFiles,
        partitionIsRunning, queueId, currentTime, snapshotIsCached);
  }

  /**
   * Get full optimize plan for iceberg tables.
   *
   * @param queueId     -
   * @param currentTime -
   * @return -
   */
  public IcebergFullOptimizePlan getIcebergFullPlan(List<FileScanTask> fileScanTasks,
                                                    int queueId,
                                                    long currentTime,
                                                    Map<String, Boolean> partitionIsRunning) {
    return new IcebergFullOptimizePlan(arcticTable, tableOptimizeRuntime, fileScanTasks,
        partitionIsRunning, queueId, currentTime);
  }

  /**
   * Get minor optimize plan for iceberg tables.
   *
   * @param queueId     -
   * @param currentTime -
   * @return -
   */
  public IcebergMinorOptimizePlan getIcebergMinorPlan(List<FileScanTask> fileScanTasks,
                                                      int queueId,
                                                      long currentTime,
                                                      Map<String, Boolean> partitionIsRunning) {
    return new IcebergMinorOptimizePlan(arcticTable, tableOptimizeRuntime, fileScanTasks,
        partitionIsRunning, queueId, currentTime);
  }

  /**
   * Get optimizeRuntime.
   *
   * @return -
   */
  public TableOptimizeRuntime getTableOptimizeRuntime() {
    return tableOptimizeRuntime;
  }

  /**
   * check whether table has optimize task
   * @return -
   */
  public boolean optimizeRunning() {
    return CollectionUtils.isNotEmpty(getOptimizeTasks());
  }

  /**
   * Persist after update table optimizeRuntime.
   */
  public void persistTableOptimizeRuntime() {
    try (SqlSession sqlSession = getSqlSession(true)) {
      TableOptimizeRuntimeMapper tableOptimizeRuntimeMapper =
          getMapper(sqlSession, TableOptimizeRuntimeMapper.class);
      tableOptimizeRuntimeMapper.updateTableOptimizeRuntime(tableOptimizeRuntime);
    }
  }

  private List<DataFileInfo> filterFile(List<DataFileInfo> dataFileInfoList, DataFileType fileType) {
    return dataFileInfoList.stream()
        .filter(dataFileInfo -> fileType == DataFileType.valueOf(dataFileInfo.getType()))
        .collect(Collectors.toList());
  }

  public Map<String, Boolean> generatePartitionRunning() {
    Map<String, Boolean> result = new HashMap<>();
    for (OptimizeTaskItem optimizeTask : getOptimizeTasks()) {
      String partition = optimizeTask.getOptimizeTask().getPartition();
      result.put(partition, true);
    }

    return result;
  }

  private boolean isMinorOptimizing() {
    if (MapUtils.isEmpty(optimizeTasks)) {
      return false;
    }
    OptimizeTaskItem optimizeTaskItem = new ArrayList<>(optimizeTasks.values()).get(0);
    return optimizeTaskItem.getTaskId().getType() == OptimizeType.Minor;
  }
}