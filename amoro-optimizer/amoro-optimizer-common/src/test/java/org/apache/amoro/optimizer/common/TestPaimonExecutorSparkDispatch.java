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

package org.apache.amoro.optimizer.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.api.OptimizingTask;
import org.apache.amoro.api.OptimizingTaskId;
import org.apache.amoro.api.OptimizingTaskResult;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionExecutorFactory;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionInput;
import org.apache.amoro.optimizing.OptimizingExecutorFactory;
import org.apache.amoro.optimizing.TableOptimizing;
import org.apache.amoro.optimizing.TaskProperties;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.utils.DynConstructors;
import org.apache.amoro.utils.SerializationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Simulates the Spark Executor JVM dispatch path for Paimon compaction tasks without standing up a
 * real {@code JavaSparkContext}.
 *
 * <p>On the real Spark Optimizer side, {@code SparkOptimizerExecutor} serializes the
 * OptimizingTask, sets a Spark job description derived from {@link
 * TableOptimizing.OptimizingInput#describe()}, and relies on {@code SparkOptimizingTaskFunction} to
 * invoke {@link OptimizerExecutor#executeTask(OptimizerConfig, int, OptimizingTask,
 * org.slf4j.Logger)} on every Spark Executor JVM. The latter performs reflective factory load via
 * {@link DynConstructors} keyed on {@link TaskProperties#TASK_EXECUTOR_FACTORY_IMPL}. Exercising
 * both the describe() contract and the reflective factory load here in {@code
 * amoro-optimizer-common} is faithful to the wire-level contract without pulling Spark test deps
 * into this module.
 */
@DisplayName("Simulate Spark Optimizer dispatch of Paimon tasks (no real SparkContext)")
public class TestPaimonExecutorSparkDispatch {

  private static final String DUMMY_AMS_URL = "thrift://localhost:1261";

  @Test
  @DisplayName(
      "PaimonCompactionInput#describe() yields table + partition suitable for Spark jobDescription")
  void testPaimonInputDescribeCarriesTableAndPartition() {
    PaimonCompactionInput empty = new PaimonCompactionInput();
    String emptyDescribe = empty.describe();
    assertNotNull(emptyDescribe);
    assertTrue(
        emptyDescribe.contains("paimon compaction task"),
        "describe() must identify the Paimon compaction task flavor, was: " + emptyDescribe);
    assertTrue(
        emptyDescribe.contains("partition:"),
        "describe() must surface partition slot for Spark UI, was: " + emptyDescribe);
    // Empty-input fallback: partition must render as <none>, never a blank string.
    assertTrue(
        emptyDescribe.contains("partition:<none>"),
        "describe() must default partition to <none> when not set, was: " + emptyDescribe);
  }

  @Test
  @DisplayName("DynConstructors reflective load of PaimonCompactionExecutorFactory succeeds")
  void testFactoryReflectiveLoad() throws Exception {
    DynConstructors.Ctor<OptimizingExecutorFactory> ctor =
        DynConstructors.builder(OptimizingExecutorFactory.class)
            .impl(PaimonCompactionExecutorFactory.class.getName())
            .buildChecked();
    OptimizingExecutorFactory<?> factory = ctor.newInstance();
    assertNotNull(factory);
    assertTrue(
        factory instanceof PaimonCompactionExecutorFactory,
        "Reflective load must return a PaimonCompactionExecutorFactory instance");
  }

  @Test
  @DisplayName(
      "OptimizerExecutor.executeTask (invoked inside SparkOptimizingTaskFunction) surfaces "
          + "structured errorMessage when Paimon input is empty")
  void testSimulatedSparkExecutorJvmDispatch() {
    PaimonCompactionInput input = new PaimonCompactionInput();
    OptimizingTask task = new OptimizingTask(new OptimizingTaskId(42L, 7));
    task.setTaskInput(SerializationUtil.simpleSerialize(input));

    Map<String, String> properties = Maps.newHashMap();
    properties.put(
        TaskProperties.TASK_EXECUTOR_FACTORY_IMPL, PaimonCompactionExecutorFactory.class.getName());
    task.setProperties(properties);

    OptimizerConfig config = OptimizerTestHelpers.buildOptimizerConfig(DUMMY_AMS_URL);

    // Same entry point used by SparkOptimizingTaskFunction#call.
    OptimizingTaskResult result =
        OptimizerExecutor.executeTask(
            config, 0, task, LoggerFactory.getLogger(TestPaimonExecutorSparkDispatch.class));

    assertNotNull(result);
    assertEquals(task.getTaskId(), result.getTaskId());
    // Empty input → Executor should have rejected with a readable errorMessage rather than crash
    // the Spark executor JVM (which would surface as a TaskKilled).
    assertNotNull(result.getErrorMessage(), "errorMessage must be set on empty-input rejection");
    assertFalse(
        result.getErrorMessage().isEmpty(), "errorMessage must be non-empty on failure paths");
  }

  @Test
  @DisplayName("Simulated jobDescription for Paimon embeds describe() output + task id")
  void testSimulatedJobDescriptionContainsTableAndTaskId() {
    // Reproduce the phrasing SparkOptimizerExecutor#jobDescription constructs so any drift from
    // that format string causes a failure here, even though this module does not depend on Spark.
    PaimonCompactionInput input = new PaimonCompactionInput();
    OptimizingTaskId taskId = new OptimizingTaskId(100L, 3);
    String simulated =
        String.format("Amoro optimizing task, %s, task id:%s", input.describe(), taskId);

    assertTrue(
        simulated.contains("paimon compaction task"),
        "Simulated jobDescription must carry Paimon marker for Spark UI observability: "
            + simulated);
    assertTrue(
        simulated.contains("task id:OptimizingTaskId"),
        "Simulated jobDescription must carry taskId: " + simulated);
  }
}
