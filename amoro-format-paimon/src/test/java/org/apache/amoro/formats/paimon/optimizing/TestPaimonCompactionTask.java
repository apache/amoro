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

package org.apache.amoro.formats.paimon.optimizing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.apache.amoro.api.OptimizingTask;
import org.apache.amoro.api.OptimizingTaskId;
import org.apache.amoro.utils.SerializationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

@DisplayName("PaimonCompactionTask descriptor")
public class TestPaimonCompactionTask {

  @Test
  @DisplayName("Full-args ctor builds a non-null summary even before output arrives")
  void testConstructorInitializesSummary() {
    PaimonCompactionInput input = new PaimonCompactionInput(null, new byte[] {1}, 2, "u", "p", 9L);
    Map<String, String> props = new HashMap<>();
    props.put("k", "v");
    PaimonCompactionTask task = new PaimonCompactionTask(42L, "age=10", input, props);

    assertEquals(42L, task.getTableId());
    assertEquals("age=10", task.getPartition());
    assertSame(input, task.getInput());
    assertEquals("v", task.getProperties().get("k"));
    assertNotNull(task.getSummary());
    assertEquals(0L, task.getSummary().getCompactedFileCount());
  }

  @Test
  @DisplayName("setOutputBytes deserialises output and recomputes summary")
  void testSetOutputBytesRecomputesSummary() {
    PaimonCompactionInput input = new PaimonCompactionInput(null, new byte[] {1}, 2, "u", "p", 0L);
    PaimonCompactionTask task = new PaimonCompactionTask(1L, "p", input, new HashMap<>());

    PaimonCompactionOutput output =
        new PaimonCompactionOutput(new byte[] {9}, 3, 11L, 2_000L, 1L, 1_900L);
    ByteBuffer buffer = SerializationUtil.simpleSerialize(output);
    byte[] outBytes = new byte[buffer.remaining()];
    buffer.get(outBytes);

    task.setOutputBytes(outBytes);

    assertNotNull(task.getOutput());
    assertEquals(11L, task.getOutput().getCompactedFileCount());
    PaimonMetricsSummary summary = task.getSummary();
    assertEquals(11L, summary.getCompactedFileCount());
    assertEquals(2_000L, summary.getCompactedFileSize());
    assertEquals(1L, summary.getProducedFileCount());
    assertEquals(1_900L, summary.getProducedFileSize());
  }

  @Test
  @DisplayName(
      "extractProtocolTask serialises input into OptimizingTask.taskInput and preserves properties")
  void testExtractProtocolTask() {
    PaimonCompactionInput input =
        new PaimonCompactionInput(null, new byte[] {1, 2, 3}, 2, "u", "p", 0L);
    Map<String, String> props = new HashMap<>();
    props.put("task-executor-factory-impl", "x.y.Z");
    PaimonCompactionTask task = new PaimonCompactionTask(1L, "p", input, props);

    OptimizingTaskId id = new OptimizingTaskId(1L, 1);
    OptimizingTask protocolTask = task.extractProtocolTask(id);
    assertNotNull(protocolTask);
    assertNotNull(protocolTask.getTaskInput());
    assertEquals("x.y.Z", protocolTask.getProperties().get("task-executor-factory-impl"));
  }
}
