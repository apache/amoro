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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.amoro.utils.SerializationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@DisplayName("Test optimizing SPI interfaces after moving to amoro-common")
public class TestOptimizingSpiInterfaces {

  @Test
  @DisplayName("BaseOptimizingInput.option() should store and retrieve a single option")
  void testBaseOptimizingInputOption() {
    BaseOptimizingInput input = createTestInput();
    input.option("key1", "value1");
    assertEquals("value1", input.getOptions().get("key1"));
  }

  @Test
  @DisplayName("BaseOptimizingInput.options() should merge multiple options")
  void testBaseOptimizingInputOptions() {
    BaseOptimizingInput input = createTestInput();
    input.option("existing", "v0");

    Map<String, String> batch = new HashMap<>();
    batch.put("key1", "value1");
    batch.put("key2", "value2");
    input.options(batch);

    assertEquals(3, input.getOptions().size());
    assertEquals("v0", input.getOptions().get("existing"));
    assertEquals("value1", input.getOptions().get("key1"));
    assertEquals("value2", input.getOptions().get("key2"));
  }

  @Test
  @DisplayName("OptimizingOutput.summary() should return a map")
  void testOptimizingOutputSummary() {
    TableOptimizing.OptimizingOutput output =
        new TableOptimizing.OptimizingOutput() {
          @Override
          public Map<String, String> summary() {
            return Collections.singletonMap("files", "10");
          }
        };
    assertNotNull(output.summary());
    assertEquals("10", output.summary().get("files"));
  }

  @Test
  @DisplayName("TaskProperties constants should have expected values")
  void testTaskPropertyConstants() {
    assertEquals("task-executor-factory-impl", TaskProperties.TASK_EXECUTOR_FACTORY_IMPL);
    assertEquals("process-id", TaskProperties.PROCESS_ID);
    assertEquals("unknown", TaskProperties.UNKNOWN_PROCESS_ID);
  }

  @Test
  @DisplayName("BaseOptimizingInput subclass should be serializable via SerializationUtil")
  void testBaseOptimizingInputSerialization() {
    TestSerializableInput input = new TestSerializableInput();
    input.option("key1", "value1");
    input.option("key2", "value2");

    ByteBuffer buffer = SerializationUtil.simpleSerialize(input);
    assertNotNull(buffer);

    byte[] bytes = new byte[buffer.remaining()];
    buffer.get(bytes);
    TestSerializableInput deserialized = SerializationUtil.simpleDeserialize(bytes);
    assertNotNull(deserialized);
    assertEquals("value1", deserialized.getOptions().get("key1"));
    assertEquals("value2", deserialized.getOptions().get("key2"));
  }

  private BaseOptimizingInput createTestInput() {
    return new BaseOptimizingInput() {};
  }

  /** A concrete subclass for serialization testing. */
  public static class TestSerializableInput extends BaseOptimizingInput {
    private static final long serialVersionUID = 1L;
  }
}
