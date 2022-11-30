/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.ams.server.service.impl;

import com.netease.arctic.ams.api.OptimizerMetric;
import com.netease.arctic.ams.api.TableMetric;
import com.netease.arctic.ams.api.properties.OptimizerProperties;
import com.netease.arctic.ams.server.mapper.MetricsSummaryMapper;
import com.netease.arctic.ams.server.mapper.OptimizerMetricsStatisticMapper;
import com.netease.arctic.ams.server.mapper.TableMetricsStatisticMapper;
import com.netease.arctic.ams.server.model.MetricsSummary;
import com.netease.arctic.ams.server.model.OptimizerMetricsStatistic;
import com.netease.arctic.ams.server.model.TableMetricsStatistic;
import com.netease.arctic.ams.server.optimize.TableOptimizeItem;
import com.netease.arctic.ams.server.service.IJDBCService;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import org.apache.ibatis.session.SqlSession;
import org.apache.iceberg.SnapshotSummary;
import org.apache.iceberg.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static com.netease.arctic.ams.api.Constants.INNER_TABLE_BASE;
import static com.netease.arctic.ams.api.Constants.INNER_TABLE_CHANGE;

public class MetricsStatisticService extends IJDBCService {

  private static final Logger LOG = LoggerFactory.getLogger(MetricsStatisticService.class);

  public void commitTableMetrics(TableIdentifier tableIdentifier, List<TableMetric> metrics) {
    try (SqlSession sqlSession = getSqlSession(false)) {
      metrics.forEach(metric -> {
        TableMetricsStatisticMapper mapper = sqlSession.getMapper(TableMetricsStatisticMapper.class);
        TableMetricsStatistic statistic = new TableMetricsStatistic();
        statistic.setTableIdentifier(tableIdentifier.buildTableIdentifier());
        statistic.setInnerTable(metric.getInnerTable());
        statistic.setMetricName(metric.getMetricName());
        statistic.setMetricValue(BigDecimal.valueOf(metric.getMetricValue()));
        mapper.insertMetricsStatistic(statistic);
      });
      sqlSession.commit();
    }
  }

  public TableMetricsStatistic getTableMetrics(
      com.netease.arctic.ams.api.TableIdentifier tableIdentifier,
      String metricName) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      TableMetricsStatisticMapper mapper = sqlSession.getMapper(TableMetricsStatisticMapper.class);
      return mapper.getMetricsStatistic(tableIdentifier, metricName);
    }
  }

  public List<TableMetricsStatistic> getTableMetrics(
      TableIdentifier tableIdentifier, String innerTable,
      String metricName) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      TableMetricsStatisticMapper mapper = sqlSession.getMapper(TableMetricsStatisticMapper.class);
      TableMetricsStatistic statistic = new TableMetricsStatistic();
      statistic.setTableIdentifier(tableIdentifier.buildTableIdentifier());
      statistic.setInnerTable(innerTable);
      statistic.setMetricName(metricName);
      return mapper.getInnerTableMetricsStatistic(statistic);
    }
  }

  public List<TableMetricsStatistic> getTableMetricsOrdered(String orderExpression, String metricName, Integer limit) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      TableMetricsStatisticMapper mapper = sqlSession.getMapper(TableMetricsStatisticMapper.class);
      return mapper.getTableMetricsOrdered(orderExpression, metricName, limit);
    }
  }

  public Long getMaxCommitTime(com.netease.arctic.ams.api.TableIdentifier identifier) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      TableMetricsStatisticMapper mapper = sqlSession.getMapper(TableMetricsStatisticMapper.class);
      Timestamp timestamp = mapper.getMaxCommitTime(identifier);
      return timestamp == null ? 0 : timestamp.getTime();
    }
  }

  public void deleteTableMetrics(com.netease.arctic.ams.api.TableIdentifier tableIdentifier) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      TableMetricsStatisticMapper mapper = sqlSession.getMapper(TableMetricsStatisticMapper.class);
      mapper.deleteTableMetrics(tableIdentifier);
    }
  }

  public List<MetricsSummary> getMetricsSummary(String metricName) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      MetricsSummaryMapper mapper = sqlSession.getMapper(MetricsSummaryMapper.class);
      return mapper.getMetricsSummary(metricName);
    }
  }

  public MetricsSummary getCurrentSummary(String metricName) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      MetricsSummaryMapper mapper = sqlSession.getMapper(MetricsSummaryMapper.class);
      return mapper.getCurrentSummary(metricName);
    }
  }

  public void metricSummaryExpire(long expireTime) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      MetricsSummaryMapper mapper = sqlSession.getMapper(MetricsSummaryMapper.class);
      mapper.expire(expireTime);
    }
  }

  public void commitOptimizerMetrics(List<OptimizerMetric> metrics) {
    try (SqlSession sqlSession = getSqlSession(false)) {
      metrics.forEach(metric -> {
        OptimizerMetricsStatisticMapper mapper = sqlSession.getMapper(OptimizerMetricsStatisticMapper.class);
        OptimizerMetricsStatistic statistic = new OptimizerMetricsStatistic();
        statistic.setOptimizerId(metric.getOptimizerId());
        statistic.setSubtaskId(metric.getSubtaskId());
        statistic.setMetricName(metric.getMetricName());
        statistic.setMetricValue(new BigDecimal(metric.getMetricValue()));
        if (mapper.getMetricsStatistic(statistic).isEmpty()) {
          mapper.insertMetricsStatistic(statistic);
        } else {
          mapper.updateMetricsStatistic(statistic);
        }
      });
      sqlSession.commit();
    }
  }

  public List<OptimizerMetricsStatistic> getOptimizerMetrics(
      long optimizerId, String subtaskId,
      String metricName) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      OptimizerMetricsStatisticMapper mapper = sqlSession.getMapper(OptimizerMetricsStatisticMapper.class);
      OptimizerMetricsStatistic statistic = new OptimizerMetricsStatistic();
      statistic.setOptimizerId(optimizerId);
      statistic.setSubtaskId(subtaskId);
      statistic.setMetricName(metricName);
      return mapper.getMetricsStatistic(statistic);
    }
  }

  public void deleteOptimizerMetrics(long optimizerId) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      OptimizerMetricsStatisticMapper mapper = sqlSession.getMapper(OptimizerMetricsStatisticMapper.class);
      mapper.deleteOptimizerMetrics(optimizerId);
    }
  }

  public void statisticTableFileMetrics(ArcticTable arcticTable) {
    if (arcticTable == null) {
      return;
    }
    List<TableMetricsStatistic> tableMetricsStatistics = new ArrayList<>();
    if (getMaxCommitTime(arcticTable.id().buildTableIdentifier()) > System.currentTimeMillis() - 60000) {
      return;
    }

    if (arcticTable.isKeyedTable()) {
      TableMetricsStatistic changeFileSizeStatistic = statisticFileSize(arcticTable.asKeyedTable().changeTable(),
          INNER_TABLE_CHANGE, arcticTable.id().buildTableIdentifier());
      TableMetricsStatistic baseFileSizeStatistic = statisticFileSize(arcticTable.asKeyedTable().baseTable(),
          INNER_TABLE_BASE, arcticTable.id().buildTableIdentifier());
      tableMetricsStatistics.add(changeFileSizeStatistic);
      tableMetricsStatistics.add(baseFileSizeStatistic);

      TableMetricsStatistic changeFileCountStatistic = statisticFileCount(arcticTable.asKeyedTable().changeTable(),
          INNER_TABLE_CHANGE, arcticTable.id().buildTableIdentifier());
      TableMetricsStatistic baseFileCountStatistic = statisticFileCount(arcticTable.asKeyedTable().baseTable(),
          INNER_TABLE_BASE, arcticTable.id().buildTableIdentifier());
      tableMetricsStatistics.add(changeFileCountStatistic);
      tableMetricsStatistics.add(baseFileCountStatistic);
    } else {
      TableMetricsStatistic fileSizeStatistic =
          statisticFileSize(arcticTable.asUnkeyedTable(), INNER_TABLE_BASE, arcticTable.id().buildTableIdentifier());
      TableMetricsStatistic fileCountStatistic =
          statisticFileCount(arcticTable.asUnkeyedTable(), INNER_TABLE_BASE, arcticTable.id().buildTableIdentifier());
      tableMetricsStatistics.add(fileSizeStatistic);
      tableMetricsStatistics.add(fileCountStatistic);
    }
    tableMetricsStatistics.forEach(this::insertStatistic);
  }

  public void insertStatistic(TableMetricsStatistic fileSizeStatistic) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      TableMetricsStatisticMapper metricsStatisticMapper = getMapper(sqlSession, TableMetricsStatisticMapper.class);
      if (metricsStatisticMapper.getInnerTableMetricsStatistic(fileSizeStatistic).size() > 0) {
        metricsStatisticMapper.updateMetricsStatistic(fileSizeStatistic);
      } else {
        metricsStatisticMapper.insertMetricsStatistic(fileSizeStatistic);
      }
    } catch (Exception e) {
      LOG.error("insert statistic error", e);
    }
  }

  public void summaryMetrics() {
    long statisticTime = System.currentTimeMillis() / 60000 * 60000;
    try (SqlSession sqlSession = getSqlSession(true)) {
      TableMetricsStatisticMapper metricsStatisticMapper = getMapper(sqlSession, TableMetricsStatisticMapper.class);
      metricsStatisticMapper.summaryMetrics(SnapshotSummary.TOTAL_FILE_SIZE_PROP, statisticTime);
      OptimizerMetricsStatisticMapper optimizerMetricsStatisticMapper = getMapper(
          sqlSession,
          OptimizerMetricsStatisticMapper.class);
      optimizerMetricsStatisticMapper.summaryMetrics(OptimizerProperties.QUOTA_USAGE, statisticTime);
    } catch (Exception e) {
      LOG.error("summaryMetrics error", e);
    }
  }

  public TableMetricsStatistic statisticFileSize(
      Table table, String innerTable,
      com.netease.arctic.ams.api.TableIdentifier identifier) {
    TableMetricsStatistic tableMetricsStatistic = new TableMetricsStatistic();
    tableMetricsStatistic.setInnerTable(innerTable);
    tableMetricsStatistic.setTableIdentifier(identifier);
    tableMetricsStatistic.setMetricName(SnapshotSummary.TOTAL_FILE_SIZE_PROP);
    if (table.currentSnapshot() == null || table.currentSnapshot().summary() == null) {
      tableMetricsStatistic.setMetricValue(BigDecimal.valueOf(0));
    } else {
      tableMetricsStatistic.setMetricValue(new BigDecimal(table.currentSnapshot().summary()
          .getOrDefault(SnapshotSummary.TOTAL_FILE_SIZE_PROP, "0")));
    }
    return tableMetricsStatistic;
  }

  public TableMetricsStatistic statisticFileCount(
      Table table, String innerTable,
      com.netease.arctic.ams.api.TableIdentifier identifier) {
    TableMetricsStatistic tableMetricsStatistic = new TableMetricsStatistic();
    tableMetricsStatistic.setInnerTable(innerTable);
    tableMetricsStatistic.setTableIdentifier(identifier);
    tableMetricsStatistic.setMetricName(SnapshotSummary.TOTAL_DATA_FILES_PROP);
    if (table.currentSnapshot() == null || table.currentSnapshot().summary() == null) {
      tableMetricsStatistic.setMetricValue(BigDecimal.valueOf(0));
    } else {
      String dataFileCount = table.currentSnapshot()
          .summary()
          .getOrDefault(SnapshotSummary.TOTAL_DATA_FILES_PROP, "0");
      String deleteFileCount = table.currentSnapshot()
          .summary()
          .getOrDefault(SnapshotSummary.TOTAL_DELETE_FILES_PROP, "0");
      tableMetricsStatistic.setMetricValue(BigDecimal.valueOf(
          Long.parseLong(dataFileCount) + Long.parseLong(deleteFileCount)));
    }
    return tableMetricsStatistic;
  }

  public static class TableMetricsStatisticTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(TableMetricsStatisticTask.class);

    private final MetricsStatisticService metricsStatisticService;

    public TableMetricsStatisticTask() {
      this.metricsStatisticService = ServiceContainer.getMetricsStatisticService();
    }

    public Thread doTask() {
      LOG.info("TableMetricsStatisticTask start");
      Thread thread = new Thread(this);
      thread.start();
      return thread;
    }

    @Override
    public void run() {
      while (true) {
        try {
          Thread.sleep(30000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        List<TableIdentifier> tableIdentifiers = ServiceContainer.getOptimizeService().listCachedTables();
        tableIdentifiers.forEach(tableIdentifier -> {
          try {
            if (tableIdentifier == null) {
              return;
            }
            TableOptimizeItem arcticTableItem =
                ServiceContainer.getOptimizeService().getTableOptimizeItem(tableIdentifier);
            ArcticTable arcticTable = arcticTableItem.getArcticTable();
            metricsStatisticService.statisticTableFileMetrics(arcticTable);
          } catch (Exception e) {
            LOG.error("statistic table file metrics error", e);
          }
        });
      }
    }
  }
}
