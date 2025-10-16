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

package org.apache.amoro.optimizing;

import org.apache.amoro.OptimizerProperties;
import org.apache.amoro.utils.PropertyUtil;
import org.apache.amoro.utils.map.StructLikeCollections;

import java.util.Map;

public class TaskProperties {

  public static final String TASK_EXECUTOR_FACTORY_IMPL = "task-executor-factory-impl";

  public static final String PROCESS_ID = "process-id";
  public static final String UNKNOWN_PROCESS_ID = "unknown";

  public static final String EXTEND_DISK_STORAGE =
      OptimizerProperties.OPTIMIZER_EXTEND_DISK_STORAGE;
  public static final boolean EXTEND_DISK_STORAGE_DEFAULT = false;

  public static final String MEMORY_STORAGE_SIZE =
      OptimizerProperties.OPTIMIZER_MEMORY_STORAGE_SIZE;
  public static final long MEMORY_STORAGE_SIZE_DEFAULT = 512 * 1024 * 1024;

  public static final String DISK_STORAGE_PATH = OptimizerProperties.OPTIMIZER_DISK_STORAGE_PATH;

  public static final String OUTPUT_DIR = "output_location";

  public static final String MOVE_FILE_TO_HIVE_LOCATION = "move-files-to-hive-location";

  public static StructLikeCollections getStructLikeCollections(Map<String, String> properties) {
    boolean enableSpillMap =
        PropertyUtil.propertyAsBoolean(
            properties, EXTEND_DISK_STORAGE, EXTEND_DISK_STORAGE_DEFAULT);
    long maxInMemory =
        PropertyUtil.propertyAsLong(properties, MEMORY_STORAGE_SIZE, MEMORY_STORAGE_SIZE_DEFAULT);
    String spillMapPath = properties.get(DISK_STORAGE_PATH);

    return new StructLikeCollections(enableSpillMap, maxInMemory, spillMapPath);
  }

  public static String getProcessId(Map<String, String> properties) {
    String processId = properties.get(PROCESS_ID);
    if (processId == null || processId.trim().isEmpty()) {
      processId = UNKNOWN_PROCESS_ID;
    }
    return processId;
  }
}
