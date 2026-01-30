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

import static org.apache.amoro.metrics.MetricDefine.defineCounter;
import static org.apache.amoro.metrics.MetricDefine.defineGauge;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.maintainer.MaintainerMetrics;
import org.apache.amoro.metrics.MetricDefine;

/**
 * Abstract base class for table maintenance operation metrics
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Define all MetricDefine constants
 *   <li>Provide template methods for metrics registration
 *   <li>Handle tags (catalog, database, table, table_format, operation_type)
 * </ul>
 *
 * <p>Note: All metrics include the table_format tag to distinguish Iceberg and Paimon tables
 */
public abstract class AbstractTableMaintainerMetrics extends AbstractTableMetrics
    implements MaintainerMetrics {

  // ========== Orphan Files Related MetricDefine ==========

  /** Count of orphan data files cleaned */
  public static final MetricDefine TABLE_ORPHAN_CONTENT_FILE_CLEANING_COUNT =
      defineCounter("table_orphan_content_file_cleaning_count")
          .withDescription("Count of orphan data files cleaned")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  /** Expected count of orphan data files to clean */
  public static final MetricDefine TABLE_EXPECTED_ORPHAN_CONTENT_FILE_CLEANING_COUNT =
      defineCounter("table_expected_orphan_content_file_cleaning_count")
          .withDescription("Expected count of orphan data files to clean")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  /** Count of orphan metadata files cleaned */
  public static final MetricDefine TABLE_ORPHAN_METADATA_FILES_CLEANED_COUNT =
      defineCounter("table_orphan_metadata_files_cleaned_count")
          .withDescription("Count of orphan metadata files cleaned")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  /** Expected count of orphan metadata files to clean */
  public static final MetricDefine TABLE_ORPHAN_METADATA_FILES_CLEANED_EXPECTED_COUNT =
      defineCounter("table_orphan_metadata_files_cleaned_expected_count")
          .withDescription("Expected count of orphan metadata files to clean")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  /** Duration of orphan files cleaning operation (milliseconds) */
  public static final MetricDefine TABLE_ORPHAN_FILES_CLEANING_DURATION =
      defineGauge("table_orphan_files_cleaning_duration_millis")
          .withDescription("Duration of orphan files cleaning operation in milliseconds")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  // ========== Dangling Delete Files Related MetricDefine (Iceberg) ==========

  /** Count of dangling delete files cleaned */
  public static final MetricDefine TABLE_DANGLING_DELETE_FILES_CLEANED_COUNT =
      defineCounter("table_dangling_delete_files_cleaned_count")
          .withDescription("Count of dangling delete files cleaned")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  /** Duration of dangling delete files cleaning operation (milliseconds) */
  public static final MetricDefine TABLE_DANGLING_DELETE_FILES_CLEANING_DURATION =
      defineGauge("table_dangling_delete_files_cleaning_duration_millis")
          .withDescription("Duration of dangling delete files cleaning operation in milliseconds")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  // ========== Snapshot Expiration Related MetricDefine ==========

  /** Count of snapshots expired */
  public static final MetricDefine TABLE_SNAPSHOTS_EXPIRED_COUNT =
      defineCounter("table_snapshots_expired_count")
          .withDescription("Count of snapshots expired")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  /** Count of data files deleted during snapshot expiration */
  public static final MetricDefine TABLE_SNAPSHOTS_EXPIRED_DATA_FILES_DELETED =
      defineCounter("table_snapshots_expired_data_files_deleted")
          .withDescription("Count of data files deleted during snapshot expiration")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  /** Duration of snapshot expiration operation (milliseconds) */
  public static final MetricDefine TABLE_SNAPSHOTS_EXPIRATION_DURATION =
      defineGauge("table_snapshots_expiration_duration_millis")
          .withDescription("Duration of snapshot expiration operation in milliseconds")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  // ========== Data Expiration Related MetricDefine (Iceberg) ==========

  /** Count of data files expired */
  public static final MetricDefine TABLE_DATA_EXPIRED_DATA_FILES_COUNT =
      defineCounter("table_data_expired_data_files_count")
          .withDescription("Count of data files expired")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  /** Count of delete files expired */
  public static final MetricDefine TABLE_DATA_EXPIRED_DELETE_FILES_COUNT =
      defineCounter("table_data_expired_delete_files_count")
          .withDescription("Count of delete files expired")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  /** Duration of data expiration operation (milliseconds) */
  public static final MetricDefine TABLE_DATA_EXPIRATION_DURATION =
      defineGauge("table_data_expiration_duration_millis")
          .withDescription("Duration of data expiration operation in milliseconds")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  // ========== Tag Creation Related MetricDefine (Iceberg) ==========

  /** Count of tags created */
  public static final MetricDefine TABLE_TAGS_CREATED_COUNT =
      defineCounter("table_tags_created_count")
          .withDescription("Count of tags created")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  /** Duration of tag creation operation (milliseconds) */
  public static final MetricDefine TABLE_TAG_CREATION_DURATION =
      defineGauge("table_tag_creation_duration_millis")
          .withDescription("Duration of tag creation operation in milliseconds")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  // ========== Partition Expiration Related MetricDefine (Paimon) ==========

  /** Count of partitions expired */
  public static final MetricDefine TABLE_PARTITIONS_EXPIRED_COUNT =
      defineCounter("table_partitions_expired_count")
          .withDescription("Count of partitions expired")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  /** Count of files expired during partition expiration */
  public static final MetricDefine TABLE_PARTITIONS_EXPIRED_FILES_COUNT =
      defineCounter("table_partitions_expired_files_count")
          .withDescription("Count of files expired during partition expiration")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  /** Duration of partition expiration operation (milliseconds) */
  public static final MetricDefine TABLE_PARTITION_EXPIRATION_DURATION =
      defineGauge("table_partition_expiration_duration_millis")
          .withDescription("Duration of partition expiration operation in milliseconds")
          .withTags("catalog", "database", "table", "table_format")
          .build();

  // ========== General Operation Status Related MetricDefine ==========

  /** Count of successful maintainer operations */
  public static final MetricDefine TABLE_MAINTAINER_OPERATION_SUCCESS_COUNT =
      defineCounter("table_maintainer_operation_success_count")
          .withDescription("Count of successful maintainer operations")
          .withTags("catalog", "database", "table", "table_format", "operation_type")
          .build();

  /** Count of failed maintainer operations */
  public static final MetricDefine TABLE_MAINTAINER_OPERATION_FAILURE_COUNT =
      defineCounter("table_maintainer_operation_failure_count")
          .withDescription("Count of failed maintainer operations")
          .withTags("catalog", "database", "table", "table_format", "operation_type")
          .build();

  /** Duration of maintainer operation (milliseconds) */
  public static final MetricDefine TABLE_MAINTAINER_OPERATION_DURATION =
      defineGauge("table_maintainer_operation_duration_millis")
          .withDescription("Duration of maintainer operation in milliseconds")
          .withTags("catalog", "database", "table", "table_format", "operation_type")
          .build();

  /** Table format type */
  protected final String tableFormat;

  /**
   * Constructor
   *
   * @param identifier Table identifier (contains format information via getFormat())
   */
  protected AbstractTableMaintainerMetrics(ServerTableIdentifier identifier) {
    super(identifier);
    this.tableFormat = identifier.getFormat().name().toLowerCase();
  }
}
