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

package org.apache.amoro;

public class OptimizerProperties {

  // Resource properties
  public static final String RESOURCE_ID = "resource-id";
  public static final String AMS_OPTIMIZER_URI = "ams-optimizing-uri";
  public static final String AMS_HOME = "ams-home";

  // Resource container properties
  public static final String EXPORT_PROPERTY_PREFIX = "export.";

  // Resource group properties
  public static final String OPTIMIZER_EXECUTION_PARALLEL = "execution-parallel";
  public static final String OPTIMIZER_MEMORY_SIZE = "memory-size";
  public static final String OPTIMIZER_GROUP_NAME = "group-name";

  /**
   * @deprecated since 0.9.0, use {@link #DYNAMIC_ALLOCATION_MIN_PARALLELISM} instead. Still honored
   *     as a fallback when the namespaced property is absent.
   */
  @Deprecated public static final String OPTIMIZER_GROUP_MIN_PARALLELISM = "min-parallelism";

  public static final String OPTIMIZER_HEART_BEAT_INTERVAL = "heart-beat-interval";
  public static final String OPTIMIZER_EXTEND_DISK_STORAGE = "extend-disk-storage";
  public static final boolean OPTIMIZER_EXTEND_DISK_STORAGE_DEFAULT = false;
  public static final String OPTIMIZER_DISK_STORAGE_PATH = "disk-storage-path";
  public static final String OPTIMIZER_MEMORY_STORAGE_SIZE = "memory-storage-size";
  public static final String MAX_INPUT_FILE_SIZE_PER_THREAD = "max-input-file-size-per-thread";
  public static final Long MAX_INPUT_FILE_SIZE_PER_THREAD_DEFAULT = 512 * 1024 * 1024L; // 512MB
  public static final String OPTIMIZER_CACHE_ENABLED = "cache-enabled";
  public static final boolean OPTIMIZER_CACHE_ENABLED_DEFAULT = false;
  public static final String OPTIMIZER_CACHE_MAX_ENTRY_SIZE = "cache-max-entry-size";
  public static final String OPTIMIZER_CACHE_MAX_ENTRY_SIZE_DEFAULT = "64mb";
  public static final String OPTIMIZER_CACHE_MAX_TOTAL_SIZE = "cache-max-total-size";
  public static final String OPTIMIZER_CACHE_MAX_TOTAL_SIZE_DEFAULT = "128mb";
  public static final String OPTIMIZER_CACHE_TIMEOUT = "cache-timeout";
  public static final String OPTIMIZER_CACHE_TIMEOUT_DEFAULT = "10min";
  public static final String OPTIMIZER_MASTER_SLAVE_MODE_ENABLED = "master-slave-mode-enabled";

  // Dynamic resource allocation (DRA) properties (AIP-5), configured at the resource group level.
  // Semantics and validation rules are documented in DynamicAllocationConfig (amoro-ams).

  /** @since 0.9.0 */
  public static final String DYNAMIC_ALLOCATION_ENABLED = "dynamic-allocation.enabled";

  public static final boolean DYNAMIC_ALLOCATION_ENABLED_DEFAULT = false;

  /** @since 0.9.0 */
  public static final String DYNAMIC_ALLOCATION_MIN_PARALLELISM =
      "dynamic-allocation.min-parallelism";

  public static final int DYNAMIC_ALLOCATION_MIN_PARALLELISM_DEFAULT = 0;

  /** @since 0.9.0 */
  public static final String DYNAMIC_ALLOCATION_MAX_PARALLELISM =
      "dynamic-allocation.max-parallelism";

  public static final int DYNAMIC_ALLOCATION_MAX_PARALLELISM_LIMIT = 1024;

  /** @since 0.9.0 */
  public static final String DYNAMIC_ALLOCATION_SCHEDULER_BACKLOG_TIMEOUT =
      "dynamic-allocation.scheduler-backlog-timeout";

  public static final String DYNAMIC_ALLOCATION_SCHEDULER_BACKLOG_TIMEOUT_DEFAULT = "1min";

  /** @since 0.9.0 */
  public static final String DYNAMIC_ALLOCATION_SUSTAINED_BACKLOG_TIMEOUT =
      "dynamic-allocation.sustained-backlog-timeout";

  public static final String DYNAMIC_ALLOCATION_SUSTAINED_BACKLOG_TIMEOUT_DEFAULT = "30s";

  /** @since 0.9.0 */
  public static final String DYNAMIC_ALLOCATION_EXECUTOR_IDLE_TIMEOUT =
      "dynamic-allocation.executor-idle-timeout";

  public static final String DYNAMIC_ALLOCATION_EXECUTOR_IDLE_TIMEOUT_DEFAULT = "5min";
  public static final String DYNAMIC_ALLOCATION_EXECUTOR_IDLE_TIMEOUT_MIN = "30s";

  /** @since 0.9.0 */
  public static final String DYNAMIC_ALLOCATION_SCALE_DOWN_COOLDOWN =
      "dynamic-allocation.scale-down-cooldown";

  public static final String DYNAMIC_ALLOCATION_SCALE_DOWN_COOLDOWN_DEFAULT = "1min";

  /** @since 0.9.0 */
  public static final String DYNAMIC_ALLOCATION_DRAIN_TIMEOUT = "dynamic-allocation.drain-timeout";

  public static final String DYNAMIC_ALLOCATION_DRAIN_TIMEOUT_DEFAULT = "15min";
}
