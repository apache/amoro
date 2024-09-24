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

import static org.apache.amoro.metrics.MetricDefine.defineGauge;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.metrics.Gauge;
import org.apache.amoro.metrics.Metric;
import org.apache.amoro.metrics.MetricDefine;
import org.apache.amoro.metrics.MetricKey;
import org.apache.amoro.server.metrics.MetricRegistry;
import org.apache.amoro.server.optimizing.plan.OptimizingEvaluator;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.UnkeyedTable;

import java.util.List;

/** Table Summary metrics. */
public class TableSummaryMetrics {

  // table summary files number metrics
  public static final MetricDefine TABLE_SUMMARY_TOTAL_FILES =
      defineGauge("table_summary_total_files")
          .withDescription("Total number of files in the table")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_SUMMARY_DATA_FILES =
      defineGauge("table_summary_data_files")
          .withDescription("Number of data files in the table")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_SUMMARY_EQUALITY_DELETE_FILES =
      defineGauge("table_summary_equality_delete_files")
          .withDescription("Number of equality delete files in the table")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_SUMMARY_POSITION_DELETE_FILES =
      defineGauge("table_summary_position_delete_files")
          .withDescription("Number of position delete files in the table")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_SUMMARY_DANGLING_DELETE_FILES =
      defineGauge("table_summary_dangling_delete_files")
          .withDescription("Number of dangling delete files in the table")
          .withTags("catalog", "database", "table")
          .build();

  // table summary files size metrics
  public static final MetricDefine TABLE_SUMMARY_TOTAL_FILES_SIZE =
      defineGauge("table_summary_total_files_size")
          .withDescription("Total size of files in the table")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_SUMMARY_DATA_FILES_SIZE =
      defineGauge("table_summary_data_files_size")
          .withDescription("Size of data files in the table")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_SUMMARY_EQUALITY_DELETE_FILES_SIZE =
      defineGauge("table_summary_equality_delete_files_size")
          .withDescription("Size of equality delete files in the table")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_SUMMARY_POSITION_DELETE_FILES_SIZE =
      defineGauge("table_summary_position_delete_files_size")
          .withDescription("Size of position delete files in the table")
          .withTags("catalog", "database", "table")
          .build();

  // table summary files records metrics
  public static final MetricDefine TABLE_SUMMARY_TOTAL_RECORDS =
      defineGauge("table_summary_total_records")
          .withDescription("Total records in the table")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_SUMMARY_DATA_FILES_RECORDS =
      defineGauge("table_summary_data_files_records")
          .withDescription("Records of data files in the table")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_SUMMARY_EQUALITY_DELETE_FILES_RECORDS =
      defineGauge("table_summary_equality_delete_files_records")
          .withDescription("Records of equality delete files in the table")
          .withTags("catalog", "database", "table")
          .build();

  public static final MetricDefine TABLE_SUMMARY_POSITION_DELETE_FILES_RECORDS =
      defineGauge("table_summary_position_delete_files_records")
          .withDescription("Records of position delete files in the table")
          .withTags("catalog", "database", "table")
          .build();

  // table summary snapshots number metric
  public static final MetricDefine TABLE_SUMMARY_SNAPSHOTS =
      defineGauge("table_summary_snapshots")
          .withDescription("Number of snapshots in the table")
          .withTags("catalog", "database", "table")
          .build();

  // table summary health score metric
  public static final MetricDefine TABLE_SUMMARY_HEALTH_SCORE =
      defineGauge("table_summary_health_score")
          .withDescription("Health score of the table")
          .withTags("catalog", "database", "table")
          .build();

  private final ServerTableIdentifier identifier;
  private final List<MetricKey> registeredMetricKeys = Lists.newArrayList();
  private OptimizingEvaluator.PendingInput tableSummary = new OptimizingEvaluator.PendingInput();
  private MetricRegistry globalRegistry;

  private long snapshots = 0L;

  public TableSummaryMetrics(ServerTableIdentifier identifier) {
    this.identifier = identifier;
  }

  private void registerMetric(MetricRegistry registry, MetricDefine define, Metric metric) {
    MetricKey key =
        registry.register(
            define,
            ImmutableMap.of(
                "catalog",
                identifier.getCatalog(),
                "database",
                identifier.getDatabase(),
                "table",
                identifier.getTableName()),
            metric);
    registeredMetricKeys.add(key);
  }

  public void register(MetricRegistry registry) {
    if (globalRegistry == null) {
      // register files number metrics
      registerMetric(
          registry,
          TABLE_SUMMARY_TOTAL_FILES,
          (Gauge<Long>) () -> (long) tableSummary.getTotalFileCount());
      registerMetric(
          registry,
          TABLE_SUMMARY_DATA_FILES,
          (Gauge<Long>) () -> (long) tableSummary.getDataFileCount());
      registerMetric(
          registry,
          TABLE_SUMMARY_POSITION_DELETE_FILES,
          (Gauge<Long>) () -> (long) tableSummary.getPositionalDeleteFileCount());
      registerMetric(
          registry,
          TABLE_SUMMARY_EQUALITY_DELETE_FILES,
          (Gauge<Long>) () -> (long) tableSummary.getEqualityDeleteFileCount());
      registerMetric(
          registry,
          TABLE_SUMMARY_DANGLING_DELETE_FILES,
          (Gauge<Long>) () -> (long) tableSummary.getDanglingDeleteFileCount());

      // register files size metrics
      registerMetric(
          registry,
          TABLE_SUMMARY_TOTAL_FILES_SIZE,
          (Gauge<Long>) () -> tableSummary.getTotalFileSize());
      registerMetric(
          registry,
          TABLE_SUMMARY_DATA_FILES_SIZE,
          (Gauge<Long>) () -> tableSummary.getDataFileSize());
      registerMetric(
          registry,
          TABLE_SUMMARY_POSITION_DELETE_FILES_SIZE,
          (Gauge<Long>) () -> tableSummary.getPositionalDeleteBytes());
      registerMetric(
          registry,
          TABLE_SUMMARY_EQUALITY_DELETE_FILES_SIZE,
          (Gauge<Long>) () -> tableSummary.getEqualityDeleteBytes());

      // register files records metrics
      registerMetric(
          registry,
          TABLE_SUMMARY_TOTAL_RECORDS,
          (Gauge<Long>) () -> tableSummary.getTotalFileRecords());
      registerMetric(
          registry,
          TABLE_SUMMARY_DATA_FILES_RECORDS,
          (Gauge<Long>) () -> tableSummary.getDataFileRecords());
      registerMetric(
          registry,
          TABLE_SUMMARY_POSITION_DELETE_FILES_RECORDS,
          (Gauge<Long>) () -> tableSummary.getPositionalDeleteFileRecords());
      registerMetric(
          registry,
          TABLE_SUMMARY_EQUALITY_DELETE_FILES_RECORDS,
          (Gauge<Long>) () -> tableSummary.getEqualityDeleteFileRecords());

      // register health score metric
      registerMetric(
          registry,
          TABLE_SUMMARY_HEALTH_SCORE,
          (Gauge<Long>) () -> (long) tableSummary.getHealthScore());

      // register snapshots number metric
      registerMetric(registry, TABLE_SUMMARY_SNAPSHOTS, (Gauge<Long>) () -> snapshots);

      globalRegistry = registry;
    }
  }

  public void unregister() {
    registeredMetricKeys.forEach(globalRegistry::unregister);
    registeredMetricKeys.clear();
    globalRegistry = null;
  }

  public void refresh(OptimizingEvaluator.PendingInput tableSummary) {
    if (tableSummary == null) {
      return;
    }
    this.tableSummary = tableSummary;
  }

  public void refreshSnapshots(MixedTable table) {
    UnkeyedTable unkeyedTable =
        table.isKeyedTable() ? table.asKeyedTable().baseTable() : table.asUnkeyedTable();
    snapshots = Lists.newArrayList(unkeyedTable.snapshots().iterator()).size();
  }
}
