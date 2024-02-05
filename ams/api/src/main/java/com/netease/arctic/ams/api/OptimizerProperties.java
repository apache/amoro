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

package com.netease.arctic.ams.api;

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
  public static final String OPTIMIZER_HEART_BEAT_INTERVAL = "heart-beat-interval";
  public static final String OPTIMIZER_EXTEND_DISK_STORAGE = "extend-disk-storage";
  public static final boolean OPTIMIZER_EXTEND_DISK_STORAGE_DEFAULT = false;
  public static final String OPTIMIZER_DISK_STORAGE_PATH = "disk-storage-path";
  public static final String OPTIMIZER_MEMORY_STORAGE_SIZE = "memory-storage-size";
  public static final String MAX_INPUT_FILE_SIZE_PER_THREAD = "max-input-file-size-per-thread";
  public static final Long MAX_INPUT_FILE_SIZE_PER_THREAD_DEFAULT = 512 * 1024 * 1024L; // 512MB
}
