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

package com.netease.arctic.ams.api.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Map;

public interface IcebergScanReport extends MetricReport {

  String TABLE_NAME = "table-name";
  String SNAPSHOT_ID = "snapshot-id";
  String FILTER = "filter";
  String SCHEMA_ID = "schema-id";
  String PROJECTED_FIELD_IDS = "projected-field-ids";
  String PROJECTED_FIELD_NAMES = "projected-field-names";
  String METADATA = "metadata";

  String TOTAL_PLANNING_DURATION = "total-planning-duration";
  String RESULT_DATA_FILES = "result-data-files";
  String RESULT_DELETE_FILES = "result-delete-files";
  String SCANNED_DATA_MANIFESTS = "scanned-data-manifests";
  String SCANNED_DELETE_MANIFESTS = "scanned-delete-manifests";
  String TOTAL_DATA_MANIFESTS = "total-data-manifests";
  String TOTAL_DELETE_MANIFESTS = "total-delete-manifests";
  String TOTAL_FILE_SIZE_IN_BYTES = "total-file-size-in-bytes";
  String TOTAL_DELETE_FILE_SIZE_IN_BYTES = "total-delete-file-size-in-bytes";
  String SKIPPED_DATA_MANIFESTS = "skipped-data-manifests";
  String SKIPPED_DELETE_MANIFESTS = "skipped-delete-manifests";
  String SKIPPED_DATA_FILES = "skipped-data-files";
  String SKIPPED_DELETE_FILES = "skipped-delete-files";
  String INDEXED_DELETE_FILES = "indexed-delete-files";
  String EQUALITY_DELETE_FILES = "equality-delete-files";
  String POSITIONAL_DELETE_FILES = "positional-delete-files";

  @Override
  default String name() {
    return "iceberg_scan_report";
  }

  @TaggedMetrics.Tag(name = TABLE_NAME)
  String tableName();

  @TaggedMetrics.Tag(name = SNAPSHOT_ID)
  long snapshotId();

  @TaggedMetrics.Tag(name = FILTER)
  String filter();

  @TaggedMetrics.Tag(name = SCHEMA_ID)
  int schemaId();

  @TaggedMetrics.Tag(name = PROJECTED_FIELD_IDS)
  List<Integer> projectedFieldIds();

  @TaggedMetrics.Tag(name = PROJECTED_FIELD_NAMES)
  List<String> projectedFieldNames();

  @TaggedMetrics.Tag(name = METADATA)
  Map<String, String> metadata();

  @Nullable
  @TaggedMetrics.Metric(name = TOTAL_PLANNING_DURATION)
  Timer totalPlanningDuration();

  @Nullable
  @TaggedMetrics.Metric(name = RESULT_DATA_FILES)
  Counter resultDataFiles();

  @Nullable
  @TaggedMetrics.Metric(name = RESULT_DELETE_FILES)
  Counter resultDeleteFiles();

  @Nullable
  @TaggedMetrics.Metric(name = TOTAL_DATA_MANIFESTS)
  Counter totalDataManifests();

  @Nullable
  @TaggedMetrics.Metric(name = TOTAL_DELETE_MANIFESTS)
  Counter totalDeleteManifests();

  @Nullable
  @TaggedMetrics.Metric(name = SCANNED_DATA_MANIFESTS)
  Counter scannedDataManifests();

  @Nullable
  @TaggedMetrics.Metric(name = SKIPPED_DATA_MANIFESTS)
  Counter skippedDataManifests();

  @Nullable
  @TaggedMetrics.Metric(name = TOTAL_FILE_SIZE_IN_BYTES)
  Counter totalFileSizeInBytes();

  @Nullable
  @TaggedMetrics.Metric(name = TOTAL_DELETE_FILE_SIZE_IN_BYTES)
  Counter totalDeleteFileSizeInBytes();

  @Nullable
  @TaggedMetrics.Metric(name = SKIPPED_DATA_FILES)
  Counter skippedDataFiles();

  @Nullable
  @TaggedMetrics.Metric(name = SKIPPED_DELETE_FILES)
  Counter skippedDeleteFiles();

  @Nullable
  @TaggedMetrics.Metric(name = SCANNED_DELETE_MANIFESTS)
  Counter scannedDeleteManifests();

  @Nullable
  @TaggedMetrics.Metric(name = SKIPPED_DELETE_MANIFESTS)
  Counter skippedDeleteManifests();

  @Nullable
  @TaggedMetrics.Metric(name = INDEXED_DELETE_FILES)
  Counter indexedDeleteFiles();

  @Nullable
  @TaggedMetrics.Metric(name = EQUALITY_DELETE_FILES)
  Counter equalityDeleteFiles();

  @Nullable
  @TaggedMetrics.Metric(name = POSITIONAL_DELETE_FILES)
  Counter positionalDeleteFiles();
}
