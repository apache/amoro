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

import com.netease.arctic.ams.api.TableMetric;
import com.netease.arctic.ams.server.mapper.TableMetricsStatisticMapper;
import com.netease.arctic.ams.server.mapper.TableRuntimeMetricsStatisticMapper;
import com.netease.arctic.ams.server.model.TableMetadata;
import com.netease.arctic.ams.server.model.TableMetricsStatistic;
import com.netease.arctic.ams.server.model.TableRuntimeMetricsStatistic;
import com.netease.arctic.ams.server.service.IJDBCService;
import com.netease.arctic.ams.server.service.IMetaService;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import org.apache.ibatis.session.SqlSession;
import org.apache.iceberg.SnapshotSummary;
import org.apache.iceberg.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.netease.arctic.ams.api.Constants.INNER_TABLE_BASE;

public class TableMetricsStatisticService extends IJDBCService {

  private static final Logger LOG = LoggerFactory.getLogger(TableMetricsStatisticService.class);

  public void commitMetrics(TableIdentifier tableIdentifier, List<TableMetric> metrics) {
    try (SqlSession sqlSession = getSqlSession(false)) {
      metrics.forEach(metric -> {
        TableMetricsStatisticMapper mapper = sqlSession.getMapper(TableMetricsStatisticMapper.class);
        TableMetricsStatistic statistic = new TableMetricsStatistic();
        statistic.setTableIdentifier(tableIdentifier.buildTableIdentifier());
        statistic.setInnerTable(metric.getInnerTable());
        statistic.setMetricName(metric.getMetricName());
        statistic.setMetricValue(metric.getMetricValue());
        mapper.insertMetricsStatistic(statistic);
      });
      sqlSession.commit();
    }
  }

  //TODO: just should statistic the table which is native iceberg table
  public void statisticTableFileMetrics(ArcticTable arcticTable) {
    if (arcticTable == null) {
      return;
    }
    TableMetricsStatistic fileSizeStatistic;
    TableMetricsStatistic fileCountStatistic;
    if (arcticTable.isKeyedTable()) {
      fileSizeStatistic = mergeLongStatistic(
          statisticFileSize(arcticTable.asKeyedTable().changeTable()),
          statisticFileSize(arcticTable.asKeyedTable().baseTable()));
      fileCountStatistic = mergeLongStatistic(
          statisticFileCount(arcticTable.asKeyedTable().changeTable()),
          statisticFileCount(arcticTable.asKeyedTable().baseTable()));
    } else {
      fileSizeStatistic = statisticFileSize(arcticTable.asUnkeyedTable());
      fileCountStatistic = statisticFileCount(arcticTable.asUnkeyedTable());
    }
    fileSizeStatistic.setInnerTable(INNER_TABLE_BASE);
    fileCountStatistic.setInnerTable(INNER_TABLE_BASE);
    fileSizeStatistic.setTableIdentifier(arcticTable.id().buildTableIdentifier());
    fileCountStatistic.setTableIdentifier(arcticTable.id().buildTableIdentifier());
    try (SqlSession sqlSession = getSqlSession(true)) {
      TableMetricsStatisticMapper metricsStatisticMapper = getMapper(sqlSession, TableMetricsStatisticMapper.class);
      if (metricsStatisticMapper.getMetricsStatistic(fileSizeStatistic).size() > 0) {
        metricsStatisticMapper.updateMetricsStatistic(fileSizeStatistic);
      } else {
        metricsStatisticMapper.insertMetricsStatistic(fileSizeStatistic);
      }
      if (metricsStatisticMapper.getMetricsStatistic(fileCountStatistic).size() > 0) {
        metricsStatisticMapper.updateMetricsStatistic(fileCountStatistic);
      } else {
        metricsStatisticMapper.insertMetricsStatistic(fileCountStatistic);
      }
    }
  }

  public void summaryMetrics() {
    long statisticTime = System.currentTimeMillis() / 60000 * 60000;
    try (SqlSession sqlSession = getSqlSession(true)) {
      TableMetricsStatisticMapper metricsStatisticMapper = getMapper(sqlSession, TableMetricsStatisticMapper.class);
      metricsStatisticMapper.summaryMetrics(SnapshotSummary.TOTAL_FILE_SIZE_PROP, statisticTime);
    }
  }

  public void statisticRuntimeTableFileMetrics(ArcticTable arcticTable, Long statisticTime) {
    if (arcticTable == null) {
      return;
    }
    TableMetricsStatistic fileSizeStatistic;
    if (arcticTable.isKeyedTable()) {
      fileSizeStatistic = mergeLongStatistic(
          statisticFileSize(arcticTable.asKeyedTable().changeTable()),
          statisticFileSize(arcticTable.asKeyedTable().baseTable()));
    } else {
      fileSizeStatistic = statisticFileSize(arcticTable.asUnkeyedTable());
    }
    TableRuntimeMetricsStatistic runtimeMetricsStatistic = new TableRuntimeMetricsStatistic();
    runtimeMetricsStatistic.setTableIdentifier(arcticTable.id().buildTableIdentifier());
    runtimeMetricsStatistic.setDataSize(Long.parseLong(fileSizeStatistic.getMetricValue()));
    runtimeMetricsStatistic.setCommitTime(statisticTime);
    try (SqlSession sqlSession = getSqlSession(true)) {
      TableRuntimeMetricsStatisticMapper
          metricsStatisticMapper = getMapper(sqlSession, TableRuntimeMetricsStatisticMapper.class);
      metricsStatisticMapper.insertMetricsStatistic(runtimeMetricsStatistic);
    }
  }

  public TableMetricsStatistic statisticFileSize(Table table) {
    TableMetricsStatistic tableMetricsStatistic = new TableMetricsStatistic();
    tableMetricsStatistic.setMetricName(SnapshotSummary.TOTAL_FILE_SIZE_PROP);
    if (table.currentSnapshot() == null || table.currentSnapshot().summary() == null) {
      tableMetricsStatistic.setMetricValue("0");
    } else {
      tableMetricsStatistic.setMetricValue(table.currentSnapshot().summary()
          .getOrDefault(SnapshotSummary.TOTAL_FILE_SIZE_PROP, "0"));
    }
    return tableMetricsStatistic;
  }

  public TableMetricsStatistic statisticFileCount(Table table) {
    TableMetricsStatistic tableMetricsStatistic = new TableMetricsStatistic();
    tableMetricsStatistic.setMetricName(SnapshotSummary.TOTAL_DATA_FILES_PROP);
    if (table.currentSnapshot() == null || table.currentSnapshot().summary() == null) {
      tableMetricsStatistic.setMetricValue("0");
    } else {
      String dataFileCount = table.currentSnapshot()
          .summary()
          .getOrDefault(SnapshotSummary.TOTAL_DATA_FILES_PROP, "0");
      String deleteFileCount = table.currentSnapshot()
          .summary()
          .getOrDefault(SnapshotSummary.TOTAL_DELETE_FILES_PROP, "0");
      tableMetricsStatistic.setMetricValue(Long.parseLong(dataFileCount) + Long.parseLong(deleteFileCount) + "");
    }
    return tableMetricsStatistic;
  }

  private TableMetricsStatistic mergeLongStatistic(TableMetricsStatistic base, TableMetricsStatistic merge) {
    base.setMetricValue(Long.parseLong(base.getMetricValue()) + Long.parseLong(merge.getMetricValue()) + "");
    return base;
  }

  public static class TableMetricsStatisticTask implements Runnable {

    private final TableMetricsStatisticService metricsStatisticService;
    private final IMetaService metaService;

    public TableMetricsStatisticTask() {
      this.metricsStatisticService = ServiceContainer.getTableMetricsStatisticService();
      this.metaService = ServiceContainer.getMetaService();
    }

    private static final Logger LOG = LoggerFactory.getLogger(TableMetricsStatisticTask.class);

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
          Thread.sleep(60000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        //TODO: just should statistic the table which is native iceberg table
        List<TableMetadata> tableMetadata = metaService.listTables();
        tableMetadata.forEach(meta -> {
          try {
            if (meta.getTableIdentifier() == null) {
              return;
            }
            TableIdentifier tableIdentifier = meta.getTableIdentifier();
            ArcticCatalog catalog =
                CatalogLoader.load(ServiceContainer.getTableMetastoreHandler(), tableIdentifier.getCatalog());
            ArcticTable arcticTable = catalog.loadTable(tableIdentifier);
            metricsStatisticService.statisticTableFileMetrics(arcticTable);
          } catch (Exception e) {
            LOG.error("statistic table file metrics error", e);
          }
        });
      }
    }
  }
}
