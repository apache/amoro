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
import java.util.Map;

public interface IcebergCommitReport extends MetricReport {

  String TABLE_NAME = "table-name";
  String SNAPSHOT_ID = "snapshot-id";
  String SEQUENCE_NUMBER = "sequence-number";
  String OPERATION = "operation";
  String METADATA = "metadata";

  String TOTAL_DURATION = "total-duration";
  String ATTEMPTS = "attempts";
  String ADDED_DATA_FILES = "added-data-files";
  String REMOVED_DATA_FILES = "removed-data-files";
  String TOTAL_DATA_FILES = "total-data-files";
  String ADDED_DELETE_FILES = "added-delete-files";
  String ADDED_EQ_DELETE_FILES = "added-equality-delete-files";
  String ADDED_POS_DELETE_FILES = "added-positional-delete-files";
  String REMOVED_POS_DELETE_FILES = "removed-positional-delete-files";
  String REMOVED_EQ_DELETE_FILES = "removed-equality-delete-files";
  String REMOVED_DELETE_FILES = "removed-delete-files";
  String TOTAL_DELETE_FILES = "total-delete-files";
  String ADDED_RECORDS = "added-records";
  String REMOVED_RECORDS = "removed-records";
  String TOTAL_RECORDS = "total-records";
  String ADDED_FILE_SIZE_BYTES = "added-files-size-bytes";
  String REMOVED_FILE_SIZE_BYTES = "removed-files-size-bytes";
  String TOTAL_FILE_SIZE_BYTES = "total-files-size-bytes";
  String ADDED_POS_DELETES = "added-positional-deletes";
  String REMOVED_POS_DELETES = "removed-positional-deletes";
  String TOTAL_POS_DELETES = "total-positional-deletes";
  String ADDED_EQ_DELETES = "added-equality-deletes";
  String REMOVED_EQ_DELETES = "removed-equality-deletes";
  String TOTAL_EQ_DELETES = "total-equality-deletes";

  @Override
  default String name() {
    return "iceberg_commit_report";
  }

  @TaggedMetrics.Tag(name = TABLE_NAME)
  String tableName();

  @TaggedMetrics.Tag(name = SNAPSHOT_ID)
  long snapshotId();

  @TaggedMetrics.Tag(name = SEQUENCE_NUMBER)
  long sequenceNumber();

  @TaggedMetrics.Tag(name = OPERATION)
  String operation();

  @TaggedMetrics.Tag(name = METADATA)
  Map<String, String> metadata();

  @TaggedMetrics.Metric(name = TOTAL_DURATION)
  Timer totalDuration();

  @TaggedMetrics.Metric(name = ATTEMPTS)
  Counter attempts();

  @Nullable
  @TaggedMetrics.Metric(name = ADDED_DATA_FILES)
  Counter addedDataFiles();

  @Nullable
  @TaggedMetrics.Metric(name = REMOVED_DATA_FILES)
  Counter removedDataFiles();

  @Nullable
  @TaggedMetrics.Metric(name = TOTAL_DATA_FILES)
  Counter totalDataFiles();

  @Nullable
  @TaggedMetrics.Metric(name = ADDED_DELETE_FILES)
  Counter addedDeleteFiles();

  @Nullable
  @TaggedMetrics.Metric(name = ADDED_EQ_DELETE_FILES)
  Counter addedEqualityDeleteFiles();

  @Nullable
  @TaggedMetrics.Metric(name = ADDED_POS_DELETE_FILES)
  Counter addedPositionalDeleteFiles();

  @Nullable
  @TaggedMetrics.Metric(name = REMOVED_DELETE_FILES)
  Counter removedDeleteFiles();

  @Nullable
  @TaggedMetrics.Metric(name = REMOVED_EQ_DELETE_FILES)
  Counter removedEqualityDeleteFiles();

  @Nullable
  @TaggedMetrics.Metric(name = REMOVED_POS_DELETE_FILES)
  Counter removedPositionalDeleteFiles();

  @Nullable
  @TaggedMetrics.Metric(name = TOTAL_DELETE_FILES)
  Counter totalDeleteFiles();

  @Nullable
  @TaggedMetrics.Metric(name = ADDED_RECORDS)
  Counter addedRecords();

  @Nullable
  @TaggedMetrics.Metric(name = REMOVED_RECORDS)
  Counter removedRecords();

  @Nullable
  @TaggedMetrics.Metric(name = TOTAL_RECORDS)
  Counter totalRecords();

  @Nullable
  @TaggedMetrics.Metric(name = ADDED_FILE_SIZE_BYTES)
  Counter addedFilesSizeInBytes();

  @Nullable
  @TaggedMetrics.Metric(name = REMOVED_FILE_SIZE_BYTES)
  Counter removedFilesSizeInBytes();

  @Nullable
  @TaggedMetrics.Metric(name = TOTAL_FILE_SIZE_BYTES)
  Counter totalFilesSizeInBytes();

  @Nullable
  @TaggedMetrics.Metric(name = ADDED_POS_DELETES)
  Counter addedPositionalDeletes();

  @Nullable
  @TaggedMetrics.Metric(name = REMOVED_POS_DELETES)
  Counter removedPositionalDeletes();

  @Nullable
  @TaggedMetrics.Metric(name = TOTAL_POS_DELETES)
  Counter totalPositionalDeletes();

  @Nullable
  @TaggedMetrics.Metric(name = ADDED_EQ_DELETES)
  Counter addedEqualityDeletes();

  @Nullable
  @TaggedMetrics.Metric(name = REMOVED_EQ_DELETES)
  Counter removedEqualityDeletes();

  @Nullable
  @TaggedMetrics.Metric(name = TOTAL_EQ_DELETES)
  Counter totalEqualityDeletes();
}
