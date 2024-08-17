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

import static org.apache.amoro.api.metrics.MetricDefine.defineGauge;

import org.apache.amoro.api.ServerTableIdentifier;
import org.apache.amoro.api.metrics.Gauge;
import org.apache.amoro.api.metrics.Metric;
import org.apache.amoro.api.metrics.MetricDefine;
import org.apache.amoro.api.metrics.MetricKey;
import org.apache.amoro.server.metrics.MetricRegistry;
import org.apache.amoro.server.optimizing.plan.OptimizingEvaluator;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.UnkeyedTable;

import java.util.List;
import java.util.stream.StreamSupport;

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

  // table summary snapshots number metrics
  public static final MetricDefine TABLE_SUMMARY_SNAPSHOTS =
      defineGauge("table_summary_snapshots")
          .withDescription("Number of snapshots in the table")
          .withTags("catalog", "database", "table")
          .build();

  private final ServerTableIdentifier identifier;
  private final List<MetricKey> registeredMetricKeys = Lists.newArrayList();
  private MetricRegistry globalRegistry;

  private long totalFiles = 0L;
  private long dataFiles = 0L;
  private long positionDeleteFiles = 0L;
  private long equalityDeleteFiles = 0L;
  private long totalFilesSize = 0L;
  private long positionDeleteFilesSize = 0L;
  private long dataFilesSize = 0L;
  private long equalityDeleteFilesSize = 0L;
  private long positionDeleteFilesRecords = 0L;
  private long totalRecords = 0L;
  private long dataFilesRecords = 0L;
  private long equalityDeleteFilesRecords = 0L;
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
      registerMetric(registry, TABLE_SUMMARY_TOTAL_FILES, (Gauge<Long>) () -> totalFiles);
      registerMetric(registry, TABLE_SUMMARY_DATA_FILES, (Gauge<Long>) () -> dataFiles);
      registerMetric(
          registry, TABLE_SUMMARY_POSITION_DELETE_FILES, (Gauge<Long>) () -> positionDeleteFiles);
      registerMetric(
          registry, TABLE_SUMMARY_EQUALITY_DELETE_FILES, (Gauge<Long>) () -> equalityDeleteFiles);

      // register files size metrics
      registerMetric(registry, TABLE_SUMMARY_TOTAL_FILES_SIZE, (Gauge<Long>) () -> totalFilesSize);
      registerMetric(registry, TABLE_SUMMARY_DATA_FILES_SIZE, (Gauge<Long>) () -> dataFilesSize);
      registerMetric(
          registry,
          TABLE_SUMMARY_POSITION_DELETE_FILES_SIZE,
          (Gauge<Long>) () -> positionDeleteFilesSize);
      registerMetric(
          registry,
          TABLE_SUMMARY_EQUALITY_DELETE_FILES_SIZE,
          (Gauge<Long>) () -> equalityDeleteFilesSize);

      // register files records metrics
      registerMetric(registry, TABLE_SUMMARY_TOTAL_RECORDS, (Gauge<Long>) () -> totalRecords);
      registerMetric(
          registry, TABLE_SUMMARY_DATA_FILES_RECORDS, (Gauge<Long>) () -> dataFilesRecords);
      registerMetric(
          registry,
          TABLE_SUMMARY_POSITION_DELETE_FILES_RECORDS,
          (Gauge<Long>) () -> positionDeleteFilesRecords);
      registerMetric(
          registry,
          TABLE_SUMMARY_EQUALITY_DELETE_FILES_RECORDS,
          (Gauge<Long>) () -> equalityDeleteFilesRecords);

      // register snapshots number metrics
      registerMetric(registry, TABLE_SUMMARY_SNAPSHOTS, (Gauge<Long>) () -> snapshots);

      globalRegistry = registry;
    }
  }

  public void unregister() {
    registeredMetricKeys.forEach(globalRegistry::unregister);
    registeredMetricKeys.clear();
    globalRegistry = null;
  }

  public void refresh(OptimizingEvaluator.PendingInput pendingInput) {
    if (pendingInput == null) {
      return;
    }
    totalFiles =
        pendingInput.getDataFileCount()
            + pendingInput.getEqualityDeleteFileCount()
            + pendingInput.getPositionalDeleteFileCount();
    dataFiles = pendingInput.getDataFileCount();
    positionDeleteFiles = pendingInput.getPositionalDeleteFileCount();
    equalityDeleteFiles = pendingInput.getEqualityDeleteFileCount();

    totalFilesSize =
        pendingInput.getDataFileSize()
            + pendingInput.getEqualityDeleteBytes()
            + pendingInput.getPositionalDeleteBytes();
    positionDeleteFilesSize = pendingInput.getPositionalDeleteBytes();
    dataFilesSize = pendingInput.getDataFileSize();
    equalityDeleteFilesSize = pendingInput.getEqualityDeleteBytes();

    totalRecords =
        pendingInput.getDataFileRecords()
            + pendingInput.getEqualityDeleteFileRecords()
            + pendingInput.getPositionalDeleteFileRecords();
    positionDeleteFilesRecords = pendingInput.getPositionalDeleteFileRecords();
    dataFilesRecords = pendingInput.getDataFileRecords();
    equalityDeleteFilesRecords = pendingInput.getEqualityDeleteFileRecords();
  }

  public void refreshSnapshots(MixedTable table) {
    UnkeyedTable unkeyedTable =
        table.isKeyedTable() ? table.asKeyedTable().baseTable() : table.asUnkeyedTable();
    snapshots = StreamSupport.stream(unkeyedTable.snapshots().spliterator(), false).count();
  }
}
