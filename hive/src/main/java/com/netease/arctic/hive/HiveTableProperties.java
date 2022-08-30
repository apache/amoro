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

package com.netease.arctic.hive;


public class HiveTableProperties {

  public static final String ARCTIC_TABLE_FLAG = "arctic.enable";

  public static final String ARCTIC_TABLE_PRIMARY_KEYS = "arctic.table.primary-keys";

  public static final String PARTITION_PROPERTIES_KEY_HIVE_LOCATION = "hive-location";

  public static final String PARTITION_PROPERTIES_KEY_TRANSIENT_TIME = "transient-time";

  public static final String BASE_HIVE_LOCATION_ROOT = "base.hive.location-root";

  public static final String AUTO_SYNC_HIVE_SCHEMA_CHANGE = "base.hive.auto-sync-schema-change";
  public static final boolean AUTO_SYNC_HIVE_SCHEMA_CHANGE_DEFAULT = true;

  public static final String AUTO_SYNC_HIVE_DATA_WRITE = "base.hive.auto-sync-data-write";
  public static final boolean AUTO_SYNC_HIVE_DATA_WRITE_DEFAULT = false;
}

