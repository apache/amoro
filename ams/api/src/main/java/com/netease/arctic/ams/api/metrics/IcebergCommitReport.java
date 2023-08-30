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

public class IcebergCommitReport implements MetricReport {

  public static final String TABLE_NAME = "table-name";
  public static final String SNAPSHOT_ID = "snapshot-id";
  public static final String SEQUENCE_NUMBER = "sequence-number";
  public static final String OPERATION = "operation";
  public static final String METADATA = "metadata";

  public static final String TOTAL_DURATION = "total-duration";
  public static final String ATTEMPTS = "attempts";
  public static final String ADDED_DATA_FILES = "added-data-files";
  public static final String REMOVED_DATA_FILES = "removed-data-files";
  public static final String TOTAL_DATA_FILES = "total-data-files";
  public static final String ADDED_DELETE_FILES = "added-delete-files";
  public static final String ADDED_EQ_DELETE_FILES = "added-equality-delete-files";
  public static final String ADDED_POS_DELETE_FILES = "added-positional-delete-files";
  public static final String REMOVED_POS_DELETE_FILES = "removed-positional-delete-files";
  public static final String REMOVED_EQ_DELETE_FILES = "removed-equality-delete-files";
  public static final String REMOVED_DELETE_FILES = "removed-delete-files";
  public static final String TOTAL_DELETE_FILES = "total-delete-files";
  public static final String ADDED_RECORDS = "added-records";
  public static final String REMOVED_RECORDS = "removed-records";
  public static final String TOTAL_RECORDS = "total-records";
  public static final String ADDED_FILE_SIZE_BYTES = "added-files-size-bytes";
  public static final String REMOVED_FILE_SIZE_BYTES = "removed-files-size-bytes";
  public static final String TOTAL_FILE_SIZE_BYTES = "total-files-size-bytes";
  public static final String ADDED_POS_DELETES = "added-positional-deletes";
  public static final String REMOVED_POS_DELETES = "removed-positional-deletes";
  public static final String TOTAL_POS_DELETES = "total-positional-deletes";
  public static final String ADDED_EQ_DELETES = "added-equality-deletes";
  public static final String REMOVED_EQ_DELETES = "removed-equality-deletes";
  public static final String TOTAL_EQ_DELETES = "total-equality-deletes";

  private final String tableName;
  private final Long snapshotId;
  private final Long sequenceNumber;
  private final String operation;
  private final Map<String, String> metadata;

  private final Timer totalDuration = new Timer();
  private final Counter attempts = new Counter();
  private final Counter addedDataFiles = new Counter();
  private final Counter removedDataFiles = new Counter();
  private final Counter totalDataFiles = new Counter();
  private final Counter addedDeleteFiles = new Counter();
  private final Counter addedEqualityDeleteFiles = new Counter();
  private final Counter addedPositionalDeleteFiles = new Counter();
  private final Counter removedDeleteFiles = new Counter();
  private final Counter removedEqualityDeleteFiles = new Counter();
  private final Counter removedPositionalDeleteFiles = new Counter();
  private final Counter totalDeleteFiles = new Counter();
  private final Counter addedRecords = new Counter();
  private final Counter removedRecords = new Counter();
  private final Counter totalRecords = new Counter();
  private final Counter addedFilesSizeInBytes = new Counter();
  private final Counter removedFilesSizeInBytes = new Counter();
  private final Counter totalFilesSizeInBytes = new Counter();
  private final Counter addedPositionalDeletes = new Counter();
  private final Counter removedPositionalDeletes = new Counter();
  private final Counter totalPositionalDeletes = new Counter();
  private final Counter addedEqualityDeletes = new Counter();
  private final Counter removedEqualityDeletes = new Counter();
  private final Counter totalEqualityDeletes = new Counter();

  public IcebergCommitReport(
      String tableName, Long snapshotId, Long sequenceNumber, String operation, Map<String,
      String> metadata) {
    this.tableName = tableName;
    this.snapshotId = snapshotId;
    this.sequenceNumber = sequenceNumber;
    this.operation = operation;
    this.metadata = metadata;
  }

  @Override
  public String name() {
    return "iceberg_commit_report";
  }

  @TaggedMetrics.Tag(name = TABLE_NAME)
  public String tableName() {
    return this.tableName;
  }

  @TaggedMetrics.Tag(name = SNAPSHOT_ID)
  public long snapshotId() {
    return this.snapshotId;
  }

  @TaggedMetrics.Tag(name = SEQUENCE_NUMBER)
  public long sequenceNumber() {
    return this.sequenceNumber;
  }

  @TaggedMetrics.Tag(name = OPERATION)
  public String operation() {
    return this.operation;
  }

  @TaggedMetrics.Tag(name = METADATA)
  public Map<String, String> metadata() {
    return this.metadata;
  }

  @TaggedMetrics.Metric(name = TOTAL_DURATION)
  public Timer totalDuration() {
    return this.totalDuration;
  }

  @TaggedMetrics.Metric(name = ATTEMPTS)
  public Counter attempts() {
    return this.attempts;
  }

  @TaggedMetrics.Metric(name = ADDED_DATA_FILES)
  public Counter addedDataFiles() {
    return this.addedDataFiles;
  }

  @TaggedMetrics.Metric(name = REMOVED_DATA_FILES)
  public Counter removedDataFiles() {
    return this.removedDataFiles;
  }

  @TaggedMetrics.Metric(name = TOTAL_DATA_FILES)
  public Counter totalDataFiles() {
    return this.totalDataFiles;
  }

  @TaggedMetrics.Metric(name = ADDED_DELETE_FILES)
  public Counter addedDeleteFiles() {
    return this.addedDeleteFiles;
  }

  @TaggedMetrics.Metric(name = ADDED_EQ_DELETE_FILES)
  public Counter addedEqualityDeleteFiles() {
    return this.addedEqualityDeleteFiles;
  }

  @TaggedMetrics.Metric(name = ADDED_POS_DELETE_FILES)
  public Counter addedPositionalDeleteFiles() {
    return this.addedPositionalDeleteFiles;
  }

  @TaggedMetrics.Metric(name = REMOVED_DELETE_FILES)
  public Counter removedDeleteFiles() {
    return this.removedDeleteFiles;
  }

  @TaggedMetrics.Metric(name = REMOVED_EQ_DELETE_FILES)
  public Counter removedEqualityDeleteFiles() {
    return this.removedEqualityDeleteFiles;
  }

  @TaggedMetrics.Metric(name = REMOVED_POS_DELETE_FILES)
  public Counter removedPositionalDeleteFiles() {
    return this.removedPositionalDeleteFiles;
  }

  @TaggedMetrics.Metric(name = TOTAL_DELETE_FILES)
  public Counter totalDeleteFiles() {
    return this.totalDeleteFiles;
  }

  @TaggedMetrics.Metric(name = ADDED_RECORDS)
  public Counter addedRecords() {
    return this.addedRecords;
  }

  @TaggedMetrics.Metric(name = REMOVED_RECORDS)
  public Counter removedRecords() {
    return this.removedRecords;
  }

  @TaggedMetrics.Metric(name = TOTAL_RECORDS)
  public Counter totalRecords() {
    return this.totalRecords;
  }

  @TaggedMetrics.Metric(name = ADDED_FILE_SIZE_BYTES)
  public Counter addedFilesSizeInBytes() {
    return this.addedFilesSizeInBytes;
  }

  @TaggedMetrics.Metric(name = REMOVED_FILE_SIZE_BYTES)
  public Counter removedFilesSizeInBytes() {
    return this.removedFilesSizeInBytes;
  }

  @TaggedMetrics.Metric(name = TOTAL_FILE_SIZE_BYTES)
  public Counter totalFilesSizeInBytes() {
    return this.totalFilesSizeInBytes;
  }

  @TaggedMetrics.Metric(name = ADDED_POS_DELETES)
  public Counter addedPositionalDeletes() {
    return this.addedPositionalDeletes;
  }

  @TaggedMetrics.Metric(name = REMOVED_POS_DELETES)
  public Counter removedPositionalDeletes() {
    return this.removedPositionalDeletes;
  }

  @TaggedMetrics.Metric(name = TOTAL_POS_DELETES)
  public Counter totalPositionalDeletes() {
    return this.totalPositionalDeletes;
  }

  @TaggedMetrics.Metric(name = ADDED_EQ_DELETES)
  public Counter addedEqualityDeletes() {
    return this.addedEqualityDeletes;
  }

  @TaggedMetrics.Metric(name = REMOVED_EQ_DELETES)
  public Counter removedEqualityDeletes() {
    return this.removedEqualityDeletes;
  }

  @TaggedMetrics.Metric(name = TOTAL_EQ_DELETES)
  public Counter totalEqualityDeletes() {
    return this.totalEqualityDeletes;
  }
}
