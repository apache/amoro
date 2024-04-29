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

package org.apache.amoro.properties;

import java.util.HashSet;
import java.util.Set;

public class HiveTableProperties {

  public static final Set<String> EXPOSED = new HashSet<>();

  static {
    EXPOSED.add(HiveTableProperties.AUTO_SYNC_HIVE_DATA_WRITE);
    EXPOSED.add(HiveTableProperties.AUTO_SYNC_HIVE_SCHEMA_CHANGE);
  }

  public static final String MIXED_TABLE_FLAG = "arctic.enabled";
  @Deprecated public static final String AMORO_TABLE_FLAG_LEGACY = "arctic.enable";

  public static final String MIXED_TABLE_PRIMARY_KEYS = "arctic.table.primary-keys";
  // save the root location of mixed-format table.
  public static final String MIXED_TABLE_ROOT_LOCATION = "arctic.table.root-location";

  public static final String PARTITION_PROPERTIES_KEY_HIVE_LOCATION = "hive-location";

  public static final String PARTITION_PROPERTIES_KEY_TRANSIENT_TIME = "transient-time";

  public static final String BASE_HIVE_LOCATION_ROOT = "base.hive.location-root";

  public static final String AUTO_SYNC_HIVE_SCHEMA_CHANGE = "base.hive.auto-sync-schema-change";
  public static final boolean AUTO_SYNC_HIVE_SCHEMA_CHANGE_DEFAULT = true;

  public static final String AUTO_SYNC_HIVE_DATA_WRITE = "base.hive.auto-sync-data-write";
  public static final boolean AUTO_SYNC_HIVE_DATA_WRITE_DEFAULT = false;

  public static final String REFRESH_HIVE_INTERVAL = "base.hive.refresh-interval";
  public static final long REFRESH_HIVE_INTERVAL_DEFAULT = -1L;

  public static final String HIVE_CONSISTENT_WRITE_ENABLED = "base.hive.consistent-write.enabled";
  public static final boolean HIVE_CONSISTENT_WRITE_ENABLED_DEFAULT = true;

  public static final String ALLOW_HIVE_TABLE_EXISTED = "allow-hive-table-existed";

  public static final String WATERMARK_HIVE = "watermark.hive";

  public static final String PARQUET_INPUT_FORMAT =
      "org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat";
  public static final String PARQUET_OUTPUT_FORMAT =
      "org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat";
  public static final String PARQUET_ROW_FORMAT_SERDE =
      "org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe";

  public static final String ORC_INPUT_FORMAT = "org.apache.hadoop.hive.ql.io.orc.OrcInputFormat";
  public static final String ORC_OUTPUT_FORMAT = "org.apache.hadoop.hive.ql.io.orc.OrcOutputFormat";
  public static final String ORC_ROW_FORMAT_SERDE = "org.apache.hadoop.hive.ql.io.orc.OrcSerde";
}
