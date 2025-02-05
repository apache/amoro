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

package org.apache.amoro.server.dashboard;

import org.apache.amoro.config.Configurations;
import org.apache.amoro.metrics.Counter;
import org.apache.amoro.metrics.Gauge;
import org.apache.amoro.metrics.Metric;
import org.apache.amoro.metrics.MetricDefine;
import org.apache.amoro.metrics.MetricKey;
import org.apache.amoro.metrics.MetricSet;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.dashboard.model.OverviewDataSizeItem;
import org.apache.amoro.server.dashboard.model.OverviewResourceUsageItem;
import org.apache.amoro.server.dashboard.model.OverviewTopTableItem;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.server.persistence.mapper.CatalogMetaMapper;
import org.apache.amoro.server.persistence.mapper.OptimizerMapper;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.amoro.server.resource.OptimizerManager;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableList;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.shade.guava32.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_COMMITTING_TABLES;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_EXECUTING_TABLES;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_IDLE_TABLES;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_MEMORY_BYTES_ALLOCATED;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_PENDING_TABLES;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_PLANING_TABLES;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_THREADS;
import static org.apache.amoro.server.table.TableSummaryMetrics.TABLE_SUMMARY_HEALTH_SCORE;
import static org.apache.amoro.server.table.TableSummaryMetrics.TABLE_SUMMARY_TOTAL_FILES;
import static org.apache.amoro.server.table.TableSummaryMetrics.TABLE_SUMMARY_TOTAL_FILES_SIZE;

public class OverviewManager extends PersistentBase {

  public static final String STATUS_PENDING = "Pending";
  public static final String STATUS_PLANING = "Planing";
  public static final String STATUS_EXECUTING = "Executing";
  public static final String STATUS_IDLE = "Idle";
  public static final String STATUS_COMMITTING = "Committing";

  private static final Logger LOG = LoggerFactory.getLogger(OverviewManager.class);
  private final List<OverviewTopTableItem> allTopTableItem = new ArrayList<>();
  private final Map<String, Long> optimizingStatusCountMap = new ConcurrentHashMap<>();
  private final ConcurrentLinkedDeque<OverviewResourceUsageItem> resourceUsageHistory =
      new ConcurrentLinkedDeque<>();
  private final ConcurrentLinkedDeque<OverviewDataSizeItem> dataSizeHistory =
      new ConcurrentLinkedDeque<>();
  private final AtomicInteger totalCatalog = new AtomicInteger();
  private final AtomicLong totalDataSize = new AtomicLong();
  private final AtomicInteger totalTableCount = new AtomicInteger();
  private final AtomicInteger totalCpu = new AtomicInteger();
  private final AtomicLong totalMemory = new AtomicLong();

  private final int maxRecordCount;

  public OverviewManager(Configurations serverConfigs) {
    this(
        serverConfigs.getInteger(AmoroManagementConf.OVERVIEW_CACHE_MAX_SIZE),
        serverConfigs.get(AmoroManagementConf.OVERVIEW_CACHE_REFRESH_INTERVAL));
  }

  @VisibleForTesting
  public OverviewManager(int maxRecordCount, Duration refreshInterval) {
    this.maxRecordCount = maxRecordCount;
    ScheduledExecutorService overviewUpdaterScheduler = Executors.newSingleThreadScheduledExecutor(
        new ThreadFactoryBuilder()
           .setNameFormat("overview-updater-scheduler-%d")
           .setDaemon(true)
           .build());
    overviewUpdaterScheduler.scheduleAtFixedRate(
        this::refresh, 1000L, refreshInterval.toMillis(), TimeUnit.MILLISECONDS);
  }

  public List<OverviewTopTableItem> getAllTopTableItem() {
    return ImmutableList.copyOf(allTopTableItem);
  }

  public int getTotalCatalog() {
    return totalCatalog.get();
  }

  public int getTotalTableCount() {
    return totalTableCount.get();
  }

  public long getTotalDataSize() {
    return totalDataSize.get();
  }

  public int getTotalCpu() {
    return totalCpu.get();
  }

  public long getTotalMemory() {
    return totalMemory.get();
  }

  public List<OverviewResourceUsageItem> getResourceUsageHistory(long startTime) {
    return resourceUsageHistory.stream()
        .filter(item -> item.getTs() >= startTime)
        .collect(Collectors.toList());
  }

  public List<OverviewDataSizeItem> getDataSizeHistory(long startTime) {
    return dataSizeHistory.stream()
        .filter(item -> item.getTs() >= startTime)
        .collect(Collectors.toList());
  }

  public Map<String, Long> getOptimizingStatus() {
    return optimizingStatusCountMap;
  }

  @VisibleForTesting
  public void refresh() {
    long start = System.currentTimeMillis();
    LOG.info("Updating overview cache");
    try {
      refreshTableCache(start);
      refreshResourceUsage(start);

    } catch (Exception e) {
      LOG.error("OverviewRefresher error", e);
    } finally {
      long end = System.currentTimeMillis();
      LOG.info("Refresher overview cache took {} ms.", end - start);
    }
  }

  private void refreshTableCache(long ts) {
    int totalCatalogs = getAs(CatalogMetaMapper.class, CatalogMetaMapper::selectCatalogCount);

    List<TableRuntimeMeta> metas = getAs(TableMetaMapper.class, TableMetaMapper::selectTableRuntimeMetas);
    AtomicLong totalDataSize = new AtomicLong();
    AtomicInteger totalFileCounts = new AtomicInteger();
    Map<String, OverviewTopTableItem> topTableItemMap = Maps.newHashMap();
    Map<String, Long> optimizingStatusMap = Maps.newHashMap();
    for (TableRuntimeMeta meta : metas) {
      String tableName = fullTableName(meta);
          OverviewTopTableItem tableItem =
          topTableItemMap.computeIfAbsent(tableName, ignore -> new OverviewTopTableItem(tableName));
      tableItem.setTableSize(meta.getPendingInput().getTotalFileSize());
      tableItem.setFileCount(meta.getPendingInput().getTotalFileCount());
      tableItem.setAverageFileSize(
          tableItem.getFileCount() == 0? 0 : tableItem.getTableSize() / tableItem.getFileCount());
      tableItem.setHealthScore(meta.getPendingInput().getHealthScore());
      totalDataSize.addAndGet(tableItem.getTableSize());
      totalFileCounts.addAndGet(tableItem.getFileCount());

      String status = statusToMetricString(meta.getTableStatus());
      if (StringUtils.isNotEmpty(status)) {
        optimizingStatusMap.computeIfAbsent(status, ignore -> 0L);
        optimizingStatusMap.computeIfPresent(status, (k, v) -> v + 1);
      }
    }

    this.totalCatalog.set(totalCatalogs);
    this.totalTableCount.set(totalFileCounts.get());
    this.totalDataSize.set(totalDataSize.get());
    this.allTopTableItem.clear();
    this.allTopTableItem.addAll(topTableItemMap.values());
    addAndCheck(new OverviewDataSizeItem(ts, this.totalDataSize.get()));
    this.optimizingStatusCountMap.clear();
    this.optimizingStatusCountMap.putAll(optimizingStatusMap);
  }


  private String statusToMetricString(OptimizingStatus status) {
    if (status == null) {
      return null;
    }
    switch (status) {
      case PENDING:
        return STATUS_PENDING;
      case PLANNING:
        return STATUS_PLANING;
      case MINOR_OPTIMIZING:
      case MAJOR_OPTIMIZING:
      case FULL_OPTIMIZING:
        return STATUS_EXECUTING;
      case IDLE:
        return STATUS_IDLE;
      case COMMITTING:
        return STATUS_COMMITTING;
      default:
        return null;
    }
  }

  private void refreshResourceUsage(long ts) {
      List<OptimizerInstance> instances = getAs(OptimizerMapper.class, OptimizerMapper::selectAll);
      AtomicInteger cpuCount = new AtomicInteger();
      AtomicLong memoryBytes = new AtomicLong();
      for (OptimizerInstance instance : instances) {
        cpuCount.addAndGet(instance.getThreadCount());
        memoryBytes.addAndGet(instance.getMemoryMb() * 1024L * 1024L);
      }
      this.totalCpu.set(cpuCount.get());
      this.totalMemory.set(memoryBytes.get());
      addAndCheck(
          new OverviewResourceUsageItem(ts, cpuCount.get(), memoryBytes.get()));
  }


  private void addAndCheck(OverviewDataSizeItem dataSizeItem) {
    dataSizeHistory.add(dataSizeItem);
    checkSize(dataSizeHistory);
  }

  private void addAndCheck(OverviewResourceUsageItem resourceUsageItem) {
    resourceUsageHistory.add(resourceUsageItem);
    checkSize(resourceUsageHistory);
  }

  private <T> void checkSize(Deque<T> deque) {
    if (deque.size() > maxRecordCount) {
      deque.poll();
    }
  }

    private String fullTableName(TableRuntimeMeta meta) {
      return meta.getCatalogName()
         .concat(".")
         .concat(meta.getDbName())
         .concat(".")
         .concat(meta.getTableName());
    }
}
