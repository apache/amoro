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

package com.netease.arctic.table;

import java.util.HashSet;
import java.util.Set;

import static org.apache.iceberg.TableProperties.DEFAULT_NAME_MAPPING;

/**
 * Reserved Arctic table properties list.
 */
public class TableProperties {

  private TableProperties() {
  }

  /**
   * Protected properties which should not be exposed to user.
   */
  public static final Set<String> PROTECTED_PROPERTIES = new HashSet<>();

  static {
    PROTECTED_PROPERTIES.add(TableProperties.BASE_TABLE_MAX_TRANSACTION_ID);
    PROTECTED_PROPERTIES.add(TableProperties.LOCATION);
    PROTECTED_PROPERTIES.add(TableProperties.TABLE_PARTITION_PROPERTIES);
    PROTECTED_PROPERTIES.add(DEFAULT_NAME_MAPPING);
  }

  public static final String TABLE_PARTITION_PROPERTIES = "table.partition-properties";

  public static final String BASE_TABLE_MAX_TRANSACTION_ID = "base.table.max-transaction-id";

  public static final String LOCATION = "location";

  public static final String TABLE_CREATE_TIME = "table.create-timestamp";
  public static final long TABLE_CREATE_TIME_DEFAULT = 0L;

  public static final String TABLE_EVENT_TIME_FIELD = "table.event-time-field";
  /**
   * table optimize related properties
   */
  public static final String ENABLE_OPTIMIZE = "optimize.enable";
  public static final String ENABLE_OPTIMIZE_DEFAULT = "true";

  @Deprecated
  public static final String OPTIMIZE_COMMIT_INTERVAL = "optimize.commit.interval";
  @Deprecated
  public static final long OPTIMIZE_COMMIT_INTERVAL_DEFAULT = 60_000; // 1 min

  public static final String OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD = "optimize.small-file-size-bytes-threshold";
  public static final long OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD_DEFAULT = 16777216; // 16 MB

  public static final String OPTIMIZE_GROUP = "optimize.group";
  public static final String OPTIMIZE_GROUP_DEFAULT = "default";

  public static final String OPTIMIZE_RETRY_NUMBER = "optimize.num-retries";
  public static final int OPTIMIZE_RETRY_NUMBER_DEFAULT = 5;

  public static final String OPTIMIZE_EXECUTE_TIMEOUT = "optimize.execute.timeout";
  public static final int OPTIMIZE_EXECUTE_TIMEOUT_DEFAULT = 1800000; // 30 min
  
  public static final String OPTIMIZE_MAX_FILE_COUNT = "optimize.max-file-count";
  public static final int OPTIMIZE_MAX_FILE_COUNT_DEFAULT = 100000;

  public static final String MAJOR_OPTIMIZE_TRIGGER_MAX_INTERVAL = "optimize.major.trigger.max-interval";
  public static final long MAJOR_OPTIMIZE_TRIGGER_MAX_INTERVAL_DEFAULT = 86_400_000; // 1 day

  public static final String FULL_OPTIMIZE_TRIGGER_MAX_INTERVAL = "optimize.full.trigger.max-interval";
  public static final long FULL_OPTIMIZE_TRIGGER_MAX_INTERVAL_DEFAULT = -1; // default not trigger

  public static final String MINOR_OPTIMIZE_TRIGGER_MAX_INTERVAL = "optimize.minor.trigger.max-interval";
  public static final long MINOR_OPTIMIZE_TRIGGER_MAX_INTERVAL_DEFAULT = 3600_000; // 1h

  public static final String FULL_OPTIMIZE_TRIGGER_DELETE_FILE_SIZE_BYTES =
      "optimize.full.trigger.delete-file-size-bytes";
  public static final long FULL_OPTIMIZE_TRIGGER_DELETE_FILE_SIZE_BYTES_DEFAULT = 67108864; // 64 MB

  public static final String MINOR_OPTIMIZE_TRIGGER_DELETE_FILE_COUNT = "optimize.minor.trigger.delete-file-count";
  public static final int MINOR_OPTIMIZE_TRIGGER_DELETE_FILE_COUNT_DEFAULT = 12; // 12

  public static final String MAJOR_OPTIMIZE_TRIGGER_SMALL_FILE_COUNT = "optimize.major.trigger.small-file-count";
  public static final int MAJOR_OPTIMIZE_TRIGGER_SMALL_FILE_COUNT_DEFAULT = 12; // 12

  public static final String OPTIMIZE_QUOTA = "optimize.quota";
  public static final double OPTIMIZE_QUOTA_DEFAULT = 0.1;

  public static final String MAJOR_OPTIMIZE_MAX_TASK_FILE_SIZE = "optimize.major.max-task-file-size-bytes";
  public static final long MAJOR_OPTIMIZE_MAX_TASK_FILE_SIZE_DEFAULT = 1073741824L; // 1 GB

  /**
   * table clean related properties
   */
  public static final String ENABLE_TABLE_EXPIRE = "table-expire.enable";
  public static final String ENABLE_TABLE_EXPIRE_DEFAULT = "true";

  public static final String CHANGE_DATA_TTL = "change.data.ttl.minutes";
  public static final String CHANGE_DATA_TTL_DEFAULT = "10080"; // 7 Days

  public static final String CHANGE_SNAPSHOT_KEEP_MINUTES = "snapshot.change.keep.minutes";
  public static final String CHANGE_SNAPSHOT_KEEP_MINUTES_DEFAULT = "10080"; // 7 Days

  public static final String BASE_SNAPSHOT_KEEP_MINUTES = "snapshot.base.keep.minutes";
  public static final String BASE_SNAPSHOT_KEEP_MINUTES_DEFAULT = "720"; // 12 Hours

  public static final String ENABLE_ORPHAN_CLEAN = "clean-orphan-file.enable";
  public static final String ENABLE_ORPHAN_CLEAN_DEFAULT = "false";

  public static final String MIN_ORPHAN_FILE_EXISTING_TIME = "clean-orphan-file.min-existing-time-minutes";
  public static final String MIN_ORPHAN_FILE_EXISTING_TIME_DEFAULT = "2880"; // 2 Days

  /**
   * table write related properties
   */
  public static final String BASE_FILE_FORMAT = "base.write.format";
  public static final String BASE_FILE_FORMAT_DEFAULT = "parquet";

  public static final String CHANGE_FILE_FORMAT = "change.write.format";
  public static final String CHANGE_FILE_FORMAT_DEFAULT = "parquet";

  public static final String DEFAULT_FILE_FORMAT = org.apache.iceberg.TableProperties.DEFAULT_FILE_FORMAT;
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

  public static final String WRITE_DISTRIBUTION_MODE = org.apache.iceberg.TableProperties.WRITE_DISTRIBUTION_MODE;
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

  /**
   * table read related properties
   */
  public static final String READ_DISTRIBUTION_MODE = "read.distribution-mode";
  public static final String READ_DISTRIBUTION_MODE_NONE = "none";
  public static final String READ_DISTRIBUTION_MODE_HASH = "hash";
  public static final String READ_DISTRIBUTION_MODE_DEFAULT = READ_DISTRIBUTION_MODE_HASH;
  public static final String READ_DISTRIBUTION_HASH_MODE = "read.distribution.hash-mode";
  public static final String READ_DISTRIBUTION_HASH_PARTITION = "partition-key";
  public static final String READ_DISTRIBUTION_HASH_PRIMARY = "primary-key";
  public static final String READ_DISTRIBUTION_HASH_PRIMARY_PARTITION = "primary-partition-key";
  public static final String READ_DISTRIBUTION_HASH_AUTO = "auto";
  public static final String READ_DISTRIBUTION_HASH_MODE_DEFAULT = READ_DISTRIBUTION_HASH_AUTO;

  public static final String SPLIT_SIZE = org.apache.iceberg.TableProperties.SPLIT_SIZE;
  public static final long SPLIT_SIZE_DEFAULT = 134217728; // 128 MB

  public static final String SPLIT_LOOKBACK = org.apache.iceberg.TableProperties.SPLIT_LOOKBACK;
  public static final int SPLIT_LOOKBACK_DEFAULT = 10;

  public static final String SPLIT_OPEN_FILE_COST = org.apache.iceberg.TableProperties.SPLIT_OPEN_FILE_COST;
  public static final long SPLIT_OPEN_FILE_COST_DEFAULT = 4 * 1024 * 1024; // 4MB
  /**
   * log store related properties
   */
  public static final String ENABLE_LOG_STORE = "log-store.enable";
  public static final boolean ENABLE_LOG_STORE_DEFAULT = false;

  public static final String LOG_STORE_TYPE = "log-store.type";
  public static final String LOG_STORE_STORAGE_TYPE_DEFAULT = "kafka";

  public static final String LOG_STORE_ADDRESS = "log-store.address";

  public static final String LOG_STORE_MESSAGE_TOPIC = "log-store.topic";

  public static final String LOG_STORE_DATA_FORMAT = "log-store.data-format";
  public static final String LOG_STORE_DATA_FORMAT_DEFAULT = "json";

  public static final String LOG_STORE_DATA_VERSION = "log-store.data-version";
  public static final String LOG_STORE_DATA_VERSION_DEFAULT = "v1";

  public static final String OWNER = "owner";
}
