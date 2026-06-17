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

package org.apache.amoro.formats.paimon.optimizing.primary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.api.OptimizingTask;
import org.apache.amoro.api.OptimizingTaskId;
import org.apache.amoro.formats.paimon.PaimonTable;
import org.apache.amoro.formats.paimon.optimizing.PaimonMetricsSummary;
import org.apache.amoro.optimizing.OptimizingExecutor;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.amoro.optimizing.TaskProperties;
import org.apache.amoro.utils.SerializationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@DisplayName("PaimonPrimaryKeyCompactionTask")
class TestPaimonPrimaryKeyCompactionTask {

  @Test
  @DisplayName("input stores fields and describes null table safely")
  void inputStoresFieldsAndDescribesUnknownTable() {
    PaimonBucketCompactionUnit unit =
        new PaimonBucketCompactionUnit(new byte[] {1}, 0, 5L, 500L, 10L, 1L);
    PaimonPrimaryKeyCompactionInput input =
        new PaimonPrimaryKeyCompactionInput(
            (PaimonTable) null,
            Collections.singletonList(unit),
            OptimizingType.MAJOR,
            true,
            7L,
            "user",
            11L);

    assertEquals(Collections.singletonList(unit), input.getUnits());
    assertEquals(OptimizingType.MAJOR, input.getOptimizingType());
    assertEquals(true, input.isFullCompaction());
    assertEquals(7L, input.getTargetSnapshotId());
    assertEquals("user", input.getCommitUser());
    assertEquals(11L, input.getCommitIdentifier());
    assertEquals(
        "Amoro paimon primary-key compaction task, table:<unknown>, type:MAJOR, buckets:1",
        input.describe());
  }

  @Test
  @DisplayName("descriptor deserializes output and builds Paimon metrics summary")
  void descriptorDeserializesOutputAndBuildsSummary() {
    PaimonPrimaryKeyCompactionInput input = newInput();
    Map<String, String> props = new HashMap<>();
    props.put(
        TaskProperties.TASK_EXECUTOR_FACTORY_IMPL,
        PaimonPrimaryKeyCompactionExecutorFactory.class.getName());
    PaimonPrimaryKeyCompactionTask task =
        new PaimonPrimaryKeyCompactionTask(1L, "primary-key-buckets", input, props);
    PaimonPrimaryKeyCompactionOutput output =
        new PaimonPrimaryKeyCompactionOutput(Collections.emptyList(), 1, 5L, 500L, 10L, 2L, 200L);

    ByteBuffer outputBuffer = SerializationUtil.simpleSerialize(output);
    byte[] outputBytes = new byte[outputBuffer.remaining()];
    outputBuffer.get(outputBytes);
    task.setOutputBytes(outputBytes);

    assertInstanceOf(PaimonPrimaryKeyCompactionOutput.class, task.getOutput());
    assertEquals(5L, task.getOutput().getCompactedFileCount());
    PaimonMetricsSummary summary = task.getSummary();
    assertEquals(5L, summary.getCompactedFileCount());
    assertEquals(500L, summary.getCompactedFileSize());
    assertEquals(2L, summary.getProducedFileCount());
    assertEquals(200L, summary.getProducedFileSize());
    assertEquals("5", task.toMetricsSummary().summaryAsMap(false).get("input-data-files"));
    assertEquals("200", task.toMetricsSummary().summaryAsMap(false).get("output-data-size"));
  }

  @Test
  @DisplayName("buildTask creates descriptor with typed summary")
  void buildTaskCreatesDescriptor() {
    PaimonPrimaryKeyCompactionInput input = newInput();
    Map<String, String> props = new HashMap<>();
    props.put("k", "v");

    PaimonPrimaryKeyCompactionTask task =
        PaimonPrimaryKeyCompactionTask.buildTask(9L, "primary-key-buckets", input, props);

    assertEquals(9L, task.getTableId());
    assertEquals("primary-key-buckets", task.getPartition());
    assertSame(input, task.getInput());
    assertEquals("v", task.getProperties().get("k"));
    assertNotNull(task.getSummary());
    assertEquals(0L, task.getSummary().getCompactedFileCount());
  }

  @Test
  @DisplayName("extractProtocolTask serializes primary-key input")
  void extractProtocolTaskSerializesInput() {
    PaimonPrimaryKeyCompactionInput input = newInput();
    Map<String, String> props = new HashMap<>();
    props.put(
        TaskProperties.TASK_EXECUTOR_FACTORY_IMPL,
        PaimonPrimaryKeyCompactionExecutorFactory.class.getName());
    PaimonPrimaryKeyCompactionTask task =
        new PaimonPrimaryKeyCompactionTask(1L, "primary-key-buckets", input, props);

    OptimizingTask protocolTask = task.extractProtocolTask(new OptimizingTaskId(1L, 1));
    PaimonPrimaryKeyCompactionInput deserialized =
        SerializationUtil.simpleDeserialize(protocolTask.getTaskInput());

    assertEquals(OptimizingType.MAJOR, deserialized.getOptimizingType());
    assertEquals(1, deserialized.getUnits().size());
    assertEquals(5L, deserialized.getUnits().get(0).getFileCount());
    assertEquals(
        PaimonPrimaryKeyCompactionExecutorFactory.class.getName(),
        protocolTask.getProperties().get(TaskProperties.TASK_EXECUTOR_FACTORY_IMPL));
  }

  @Test
  @DisplayName("factory creates primary-key executor")
  void factoryCreatesPrimaryKeyExecutor() {
    PaimonPrimaryKeyCompactionExecutorFactory factory =
        new PaimonPrimaryKeyCompactionExecutorFactory();
    factory.initialize(Collections.singletonMap("k", "v"));

    OptimizingExecutor<?> executor = factory.createExecutor(newInput());

    assertInstanceOf(PaimonPrimaryKeyCompactionExecutor.class, executor);
    IllegalStateException error = assertThrows(IllegalStateException.class, executor::execute);
    assertTrue(error.getMessage().contains("missing required fields"));
  }

  private static PaimonPrimaryKeyCompactionInput newInput() {
    return new PaimonPrimaryKeyCompactionInput(
        (PaimonTable) null,
        Collections.singletonList(
            new PaimonBucketCompactionUnit(new byte[0], 0, 5L, 500L, 10L, 1L)),
        OptimizingType.MAJOR,
        true,
        7L,
        "user",
        11L);
  }
}
