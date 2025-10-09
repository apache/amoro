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

package org.apache.amoro.table;

import static org.apache.iceberg.TableProperties.DEFAULT_NAME_MAPPING;
import static org.apache.iceberg.TableProperties.FORMAT_VERSION;

import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.properties.HiveTableProperties;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.utils.MemorySize;

import java.util.HashSet;
import java.util.Set;

/** Reserved mixed-format table properties list. */
public class TableProperties {

  private TableProperties() {}

  public static final String TABLE_PARTITION_PROPERTIES = "table.partition-properties";

  public static final String BASE_TABLE_MAX_TRANSACTION_ID = "base.table.max-transaction-id";

  public static final String PARTITION_OPTIMIZED_SEQUENCE = "max-txId";

  public static final String PARTITION_BASE_OPTIMIZED_TIME = "base-op-time";

  public static final String LOCATION = "location";

  public static final String TABLE_CREATE_TIME = "table.create-timestamp";
  public static final long TABLE_CREATE_TIME_DEFAULT = 0L;

  /** table comment related properties */
  public static final String COMMENT = "comment";

  /** table watermark related properties */
  public static final String TABLE_EVENT_TIME_FIELD = "table.event-time-field";

  public static final String TABLE_EVENT_TIME_FIELD_DEFAULT = WatermarkGenerator.INGEST_TIME;

  public static final String TABLE_WATERMARK_ALLOWED_LATENESS =
      "table.watermark-allowed-lateness-second";
  public static final long TABLE_WATERMARK_ALLOWED_LATENESS_DEFAULT = 0L;

  public static final String TABLE_EVENT_TIME_STRING_FORMAT =
      "table.event-time-field.datetime-string-format";
  public static final String TABLE_EVENT_TIME_STRING_FORMAT_DEFAULT = "yyyy-MM-dd HH:mm:ss";

  public static final String TABLE_EVENT_TIME_NUMBER_FORMAT =
      "table.event-time-field.datetime-number-format";
  public static final String TABLE_EVENT_TIME_NUMBER_FORMAT_DEFAULT =
      WatermarkGenerator.EVENT_TIME_TIMESTAMP_MS;

  public static final String WATERMARK_TABLE = "watermark.table";

  public static final String WATERMARK_BASE_STORE = "watermark.base";

  /** table optimize related properties */
  public static final String ENABLE_SELF_OPTIMIZING = "self-optimizing.enabled";

  public static final boolean ENABLE_SELF_OPTIMIZING_DEFAULT = true;

  public static final String SELF_OPTIMIZING_ALLOW_PARTIAL_COMMIT =
      "self-optimizing.allow-partial-commit";
  public static final boolean SELF_OPTIMIZING_ALLOW_PARTIAL_COMMIT_DEFAULT = false;

  public static final String SELF_OPTIMIZING_GROUP = "self-optimizing.group";
  public static final String SELF_OPTIMIZING_GROUP_DEFAULT = "default";

  public static final String SELF_OPTIMIZING_QUOTA = "self-optimizing.quota";
  public static final double SELF_OPTIMIZING_QUOTA_DEFAULT = 0.5;

  public static final String SELF_OPTIMIZING_EXECUTE_RETRY_NUMBER =
      "self-optimizing.execute.num-retries";
  public static final int SELF_OPTIMIZING_EXECUTE_RETRY_NUMBER_DEFAULT = 5;

  public static final String SELF_OPTIMIZING_TARGET_SIZE = "self-optimizing.target-size";
  public static final long SELF_OPTIMIZING_TARGET_SIZE_DEFAULT = 134217728; // 128 MB

  public static final String SELF_OPTIMIZING_MAX_FILE_CNT = "self-optimizing.max-file-count";
  public static final int SELF_OPTIMIZING_MAX_FILE_CNT_DEFAULT = 10000;

  public static final String SELF_OPTIMIZING_MAX_TASK_SIZE = "self-optimizing.max-task-size-bytes";
  public static final long SELF_OPTIMIZING_MAX_TASK_SIZE_DEFAULT = 134217728; // 128 MB

  public static final String SELF_OPTIMIZING_FRAGMENT_RATIO = "self-optimizing.fragment-ratio";
  public static final int SELF_OPTIMIZING_FRAGMENT_RATIO_DEFAULT = 8;

  public static final String SELF_OPTIMIZING_MIN_TARGET_SIZE_RATIO =
      "self-optimizing.min-target-size-ratio";
  public static final double SELF_OPTIMIZING_MIN_TARGET_SIZE_RATIO_DEFAULT = 0.75;

  public static final String SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT =
      "self-optimizing.minor.trigger.file-count";
  public static final int SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT_DEFAULT = 12;

  public static final String SELF_OPTIMIZING_MINOR_TRIGGER_INTERVAL =
      "self-optimizing.minor.trigger.interval";
  public static final int SELF_OPTIMIZING_MINOR_TRIGGER_INTERVAL_DEFAULT = 3600000; // 1 h

  public static final String SELF_OPTIMIZING_MAJOR_TRIGGER_DUPLICATE_RATIO =
      "self-optimizing.major.trigger.duplicate-ratio";
  public static final double SELF_OPTIMIZING_MAJOR_TRIGGER_DUPLICATE_RATIO_DEFAULT = 0.1;

  public static final String SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL =
      "self-optimizing.full.trigger.interval";
  public static final int SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL_DEFAULT = -1; // not trigger

  public static final String SELF_OPTIMIZING_FULL_REWRITE_ALL_FILES =
      "self-optimizing.full.rewrite-all-files";
  public static final boolean SELF_OPTIMIZING_FULL_REWRITE_ALL_FILES_DEFAULT = true;

  public static final String SELF_OPTIMIZING_FILTER = "self-optimizing.filter";
  public static final String SELF_OPTIMIZING_FILTER_DEFAULT = null;

  public static final String SELF_OPTIMIZING_MIN_PLAN_INTERVAL =
      "self-optimizing.min-plan-interval";
  public static final long SELF_OPTIMIZING_MIN_PLAN_INTERVAL_DEFAULT = 60000;

  public static final String SELF_OPTIMIZING_AVERAGE_FILE_SIZE_TOLERANCE =
      "self-optimizing.average-file-size.tolerance"; // the minimum tolerance value for the average
  // partition file size (between 0 and (self-optimizing.target-size))
  public static final MemorySize SELF_OPTIMIZING_AVERAGE_FILE_SIZE_TOLERANCE_DEFAULT =
      MemorySize.MAX_VALUE;

  /** table clean related properties */
  public static final String ENABLE_TABLE_EXPIRE = "table-expire.enabled";

  public static final boolean ENABLE_TABLE_EXPIRE_DEFAULT = true;

  public static final String CHANGE_DATA_TTL = "change.data.ttl.minutes";
  public static final long CHANGE_DATA_TTL_DEFAULT = 10080; // 7 Days

  public static final String SNAPSHOT_KEEP_DURATION = "snapshot.keep.duration";
  public static final String SNAPSHOT_KEEP_DURATION_DEFAULT = "720min"; // 12 Hours

  public static final String SNAPSHOT_MIN_COUNT = "snapshot.keep.min-count";
  public static final int SNAPSHOT_MIN_COUNT_DEFAULT = 1;

  public static final String ENABLE_ORPHAN_CLEAN = "clean-orphan-file.enabled";
  public static final boolean ENABLE_ORPHAN_CLEAN_DEFAULT = false;

  public static final String MIN_ORPHAN_FILE_EXISTING_TIME =
      "clean-orphan-file.min-existing-time-minutes";
  public static final long MIN_ORPHAN_FILE_EXISTING_TIME_DEFAULT = 2880; // 2 Days

  public static final String ENABLE_DANGLING_DELETE_FILES_CLEAN =
      "clean-dangling-delete-files.enabled";
  public static final boolean ENABLE_DANGLING_DELETE_FILES_CLEAN_DEFAULT = true;

  public static final String ENABLE_DATA_EXPIRATION = "data-expire.enabled";
  public static final boolean ENABLE_DATA_EXPIRATION_DEFAULT = false;

  public static final String DATA_EXPIRATION_LEVEL = "data-expire.level";
  public static final String DATA_EXPIRATION_LEVEL_DEFAULT = "partition";

  public static final String DATA_EXPIRATION_FIELD = "data-expire.field";

  public static final String DATA_EXPIRATION_DATE_STRING_PATTERN =
      "data-expire.datetime-string-pattern";
  public static final String DATA_EXPIRATION_DATE_STRING_PATTERN_DEFAULT = "yyyy-MM-dd";

  public static final String DATA_EXPIRATION_DATE_NUMBER_FORMAT =
      "data-expire.datetime-number-format";
  public static final String DATA_EXPIRATION_DATE_NUMBER_FORMAT_DEFAULT = "TIMESTAMP_MS";

  public static final String DATA_EXPIRATION_RETENTION_TIME = "data-expire.retention-time";

  public static final String DATA_EXPIRATION_BASE_ON_RULE = "data-expire.base-on-rule";
  public static final String DATA_EXPIRATION_BASE_ON_RULE_DEFAULT = "LAST_COMMIT_TIME";

  public static final String ENABLE_TABLE_TRASH = "table-trash.enabled";
  public static final boolean ENABLE_TABLE_TRASH_DEFAULT = false;

  public static final String TABLE_TRASH_CUSTOM_ROOT_LOCATION = "table-trash.custom-root-location";

  public static final String TABLE_TRASH_FILE_PATTERN = "table-trash.file-pattern";
  public static final String TABLE_TRASH_FILE_PATTERN_DEFAULT =
      ".+\\.parquet"
          + "|.*snap-[0-9]+-[0-9]+-.+\\.avro"
          + // snap-1515213806302741636-1-UUID.avro
          "|.*version-hint.text"
          + // version-hint.text
          "|.*v[0-9]+\\.metadata\\.json"
          + // v123.metadata.json
          "|.*[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}-m[0-9]+\\.avro"; // UUID-m0.avro

  /** table tag management related properties */
  public static final String ENABLE_AUTO_CREATE_TAG = "tag.auto-create.enabled";

  public static final boolean ENABLE_AUTO_CREATE_TAG_DEFAULT = false;

  public static final String AUTO_CREATE_TAG_TRIGGER_PERIOD = "tag.auto-create.trigger.period";
  public static final String AUTO_CREATE_TAG_TRIGGER_PERIOD_DEFAULT = "daily";

  public static final String AUTO_CREATE_TAG_TRIGGER_OFFSET_MINUTES =
      "tag.auto-create.trigger.offset.minutes";
  public static final int AUTO_CREATE_TAG_TRIGGER_OFFSET_MINUTES_DEFAULT = 0;

  public static final String AUTO_CREATE_TAG_MAX_DELAY_MINUTES =
      "tag.auto-create.trigger.max-delay.minutes";
  public static final int AUTO_CREATE_TAG_MAX_DELAY_MINUTES_DEFAULT = 60;

  public static final String AUTO_CREATE_TAG_MAX_AGE_MS = "tag.auto-create.max-age-ms";
  public static final int AUTO_CREATE_TAG_MAX_AGE_MS_DEFAULT = -1;

  public static final String AUTO_CREATE_TAG_FORMAT = "tag.auto-create.tag-format";
  public static final String AUTO_CREATE_TAG_FORMAT_DAILY_DEFAULT = "'tag-'yyyyMMdd";
  public static final String AUTO_CREATE_TAG_FORMAT_HOURLY_DEFAULT = "'tag-'yyyyMMddHH";

  /** table write related properties */
  public static final String FILE_FORMAT_PARQUET = "parquet";

  public static final String FILE_FORMAT_ORC = "orc";

  public static final String BASE_FILE_FORMAT = "base.write.format";
  public static final String BASE_FILE_FORMAT_DEFAULT = FILE_FORMAT_PARQUET;

  public static final String DELTA_FILE_FORMAT = "delta.write.format";

  public static final String CHANGE_FILE_FORMAT = "change.write.format";
  public static final String CHANGE_FILE_FORMAT_DEFAULT = FILE_FORMAT_PARQUET;

  public static final String DEFAULT_FILE_FORMAT =
      org.apache.iceberg.TableProperties.DEFAULT_FILE_FORMAT;

  public static final String DEFAULT_FILE_FORMAT_DEFAULT =
      org.apache.iceberg.TableProperties.DEFAULT_FILE_FORMAT_DEFAULT;

  public static final String BASE_FILE_INDEX_HASH_BUCKET = "base.file-index.hash-bucket";
  public static final int BASE_FILE_INDEX_HASH_BUCKET_DEFAULT = 4;

  public static final String CHANGE_FILE_INDEX_HASH_BUCKET = "change.file-index.hash-bucket";
  public static final int CHANGE_FILE_INDEX_HASH_BUCKET_DEFAULT = 4;

  public static final String WRITE_TARGET_FILE_SIZE_BYTES =
      org.apache.iceberg.TableProperties.WRITE_TARGET_FILE_SIZE_BYTES;
  public static final long WRITE_TARGET_FILE_SIZE_BYTES_DEFAULT = 134217728; // 128 MB

  public static final String UPSERT_ENABLED = "write.upsert.enabled";
  public static final boolean UPSERT_ENABLED_DEFAULT = false;

  public static final String WRITE_DISTRIBUTION_MODE =
      org.apache.iceberg.TableProperties.WRITE_DISTRIBUTION_MODE;
  public static final String WRITE_DISTRIBUTION_MODE_NONE =
      org.apache.iceberg.TableProperties.WRITE_DISTRIBUTION_MODE_NONE;
  public static final String WRITE_DISTRIBUTION_MODE_HASH =
      org.apache.iceberg.TableProperties.WRITE_DISTRIBUTION_MODE_HASH;
  public static final String WRITE_DISTRIBUTION_MODE_RANGE =
      org.apache.iceberg.TableProperties.WRITE_DISTRIBUTION_MODE_RANGE;
  public static final String WRITE_DISTRIBUTION_MODE_DEFAULT = WRITE_DISTRIBUTION_MODE_HASH;

  public static final String WRITE_DISTRIBUTION_HASH_MODE = "write.distribution.hash-mode";
  public static final String WRITE_DISTRIBUTION_HASH_PARTITION = "partition-key";
  public static final String WRITE_DISTRIBUTION_HASH_PRIMARY = "primary-key";
  public static final String WRITE_DISTRIBUTION_HASH_PRIMARY_PARTITION = "primary-partition-key";
  public static final String WRITE_DISTRIBUTION_HASH_AUTO = "auto";
  public static final String WRITE_DISTRIBUTION_HASH_MODE_DEFAULT = WRITE_DISTRIBUTION_HASH_AUTO;

  public static final String BASE_REFRESH_INTERVAL = "base.refresh-interval";
  public static final long BASE_REFRESH_INTERVAL_DEFAULT = -1L;

  public static final String SPLIT_SIZE = org.apache.iceberg.TableProperties.SPLIT_SIZE;
  public static final long SPLIT_SIZE_DEFAULT = 134217728; // 128 MB

  public static final String SPLIT_LOOKBACK = org.apache.iceberg.TableProperties.SPLIT_LOOKBACK;
  public static final int SPLIT_LOOKBACK_DEFAULT = 10;

  public static final String SPLIT_OPEN_FILE_COST =
      org.apache.iceberg.TableProperties.SPLIT_OPEN_FILE_COST;
  public static final long SPLIT_OPEN_FILE_COST_DEFAULT = 4 * 1024 * 1024; // 4MB

  /** log store related properties */
  public static final String ENABLE_LOG_STORE = "log-store.enabled";

  public static final boolean ENABLE_LOG_STORE_DEFAULT = false;

  public static final String LOG_STORE_TYPE = "log-store.type";
  public static final String LOG_STORE_STORAGE_TYPE_KAFKA = "kafka";
  public static final String LOG_STORE_STORAGE_TYPE_PULSAR = "pulsar";
  public static final String LOG_STORE_STORAGE_TYPE_DEFAULT = LOG_STORE_STORAGE_TYPE_KAFKA;

  public static final String LOG_STORE_ADDRESS = "log-store.address";

  public static final String LOG_STORE_MESSAGE_TOPIC = "log-store.topic";

  public static final String LOG_STORE_DATA_FORMAT = "log-store.data-format";
  public static final String LOG_STORE_DATA_FORMAT_DEFAULT = "json";

  public static final String LOG_STORE_DATA_VERSION = "log-store.data-version";
  public static final String LOG_STORE_DATA_VERSION_DEFAULT = "v1";

  public static final String LOG_STORE_PROPERTIES_PREFIX = "properties.";

  public static final String OWNER = "owner";

  /** table format related properties */
  public static final String TABLE_FORMAT = "table-format";

  public static final String MIXED_FORMAT_PRIMARY_KEY_FIELDS = "mixed-format.primary-key-fields";

  public static final String MIXED_FORMAT_TABLE_STORE = "mixed-format.table-store";

  public static final String MIXED_FORMAT_TABLE_STORE_BASE = "base";

  public static final String MIXED_FORMAT_TABLE_STORE_CHANGE = "change";

  public static final String MIXED_FORMAT_CHANGE_STORE_IDENTIFIER =
      "mixed-format.change.identifier";

  /** Protected properties which should not be read by user. */
  public static final Set<String> READ_PROTECTED_PROPERTIES = new HashSet<>();

  /** Protected properties which should not be written by user. */
  public static final Set<String> WRITE_PROTECTED_PROPERTIES = new HashSet<>();

  static {
    READ_PROTECTED_PROPERTIES.add(TableProperties.BASE_TABLE_MAX_TRANSACTION_ID);
    READ_PROTECTED_PROPERTIES.add(TableProperties.PARTITION_OPTIMIZED_SEQUENCE);
    READ_PROTECTED_PROPERTIES.add(TableProperties.LOCATION);
    READ_PROTECTED_PROPERTIES.add(TableProperties.TABLE_PARTITION_PROPERTIES);
    READ_PROTECTED_PROPERTIES.add(DEFAULT_NAME_MAPPING);
    READ_PROTECTED_PROPERTIES.add(FORMAT_VERSION);
    READ_PROTECTED_PROPERTIES.add("flink.max-continuous-empty-commits");

    WRITE_PROTECTED_PROPERTIES.add(TableProperties.BASE_TABLE_MAX_TRANSACTION_ID);
    WRITE_PROTECTED_PROPERTIES.add(TableProperties.PARTITION_OPTIMIZED_SEQUENCE);
    WRITE_PROTECTED_PROPERTIES.add(TableProperties.LOCATION);
    WRITE_PROTECTED_PROPERTIES.add(TableProperties.TABLE_PARTITION_PROPERTIES);
    WRITE_PROTECTED_PROPERTIES.add(DEFAULT_NAME_MAPPING);
    WRITE_PROTECTED_PROPERTIES.add(FORMAT_VERSION);
    WRITE_PROTECTED_PROPERTIES.add(WATERMARK_TABLE);
    WRITE_PROTECTED_PROPERTIES.add(WATERMARK_BASE_STORE);
    WRITE_PROTECTED_PROPERTIES.add("flink.max-continuous-empty-commits");
  }

  public static final HashSet<String> DEFAULT_NON_PERSISTED_TABLE_PROPERTIES =
      Sets.newHashSet(
          CatalogMetaProperties.OPTIMIZE_PROPERTIES_PREFIX,
          CatalogMetaProperties.DEPRECATED_OPTIMIZE_PROPERTIES_PREFIX,
          CatalogMetaProperties.TABLE_EXPIRE_PREFIX,
          CatalogMetaProperties.ORPHAN_CLEAN_PREFIX,
          CatalogMetaProperties.DANGLING_DELETE_FILES_CLEAN_PREFIX,
          CatalogMetaProperties.DATA_EXPIRATION_PREFIX,
          CatalogMetaProperties.TABLE_TRASH_PREFIX,
          CatalogMetaProperties.AUTO_CREATE_TAG_PREFIX,
          // mixed format reading config keys
          TableProperties.SPLIT_OPEN_FILE_COST,
          TableProperties.SPLIT_LOOKBACK,
          TableProperties.SPLIT_SIZE,
          // mixed format writing config keys
          TableProperties.UPSERT_ENABLED,
          // mixed-hive config keys
          HiveTableProperties.AUTO_SYNC_HIVE_SCHEMA_CHANGE,
          HiveTableProperties.AUTO_SYNC_HIVE_DATA_WRITE,
          HiveTableProperties.HIVE_CONSISTENT_WRITE_ENABLED);
}
