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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

@DisplayName("Test TaskProperties after getStructLikeCollections extraction")
public class TestTaskPropertiesSplit {

  @Test
  @DisplayName("getProcessId should return correct process id from properties")
  void testGetProcessId() {
    Map<String, String> properties = new HashMap<>();
    properties.put(TaskProperties.PROCESS_ID, "12345");
    assertEquals("12345", TaskProperties.getProcessId(properties));
  }

  @Test
  @DisplayName("getProcessId should return UNKNOWN when property is null")
  void testGetProcessIdWithNull() {
    Map<String, String> properties = new HashMap<>();
    assertEquals(TaskProperties.UNKNOWN_PROCESS_ID, TaskProperties.getProcessId(properties));
  }

  @Test
  @DisplayName("getProcessId should return UNKNOWN when property is empty")
  void testGetProcessIdWithEmpty() {
    Map<String, String> properties = new HashMap<>();
    properties.put(TaskProperties.PROCESS_ID, "  ");
    assertEquals(TaskProperties.UNKNOWN_PROCESS_ID, TaskProperties.getProcessId(properties));
  }

  @Test
  @DisplayName("Task property constants should have expected values")
  void testTaskPropertyConstants() {
    assertEquals("task-executor-factory-impl", TaskProperties.TASK_EXECUTOR_FACTORY_IMPL);
    assertEquals("process-id", TaskProperties.PROCESS_ID);
    assertEquals("unknown", TaskProperties.UNKNOWN_PROCESS_ID);
    assertEquals("output_location", TaskProperties.OUTPUT_DIR);
    assertEquals("move-files-to-hive-location", TaskProperties.MOVE_FILE_TO_HIVE_LOCATION);
  }
}
