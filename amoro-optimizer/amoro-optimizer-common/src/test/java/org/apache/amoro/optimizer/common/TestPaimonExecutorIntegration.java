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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.api.OptimizingTask;
import org.apache.amoro.api.OptimizingTaskId;
import org.apache.amoro.api.OptimizingTaskResult;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionExecutorFactory;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionInput;
import org.apache.amoro.optimizing.OptimizingExecutorFactory;
import org.apache.amoro.optimizing.TaskProperties;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.utils.DynConstructors;
import org.apache.amoro.utils.SerializationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.Map;

@DisplayName("Test Paimon executor integration with amoro-optimizer-common")
public class TestPaimonExecutorIntegration {

  @Test
  @DisplayName("DynConstructors should load PaimonCompactionExecutorFactory from classpath")
  void testPaimonFactoryLoadable() throws Exception {
    String className = PaimonCompactionExecutorFactory.class.getName();
    DynConstructors.Ctor<OptimizingExecutorFactory> ctor =
        DynConstructors.builder(OptimizingExecutorFactory.class).impl(className).buildChecked();
    OptimizingExecutorFactory<?> factory = ctor.newInstance();
    assertNotNull(factory);
    assertTrue(factory instanceof PaimonCompactionExecutorFactory);
  }

  @Test
  @DisplayName(
      "executeTask with empty PaimonCompactionInput should return an error result (missing required fields)")
  void testPaimonExecuteTaskReturnsError() {
    PaimonCompactionInput input = new PaimonCompactionInput();
    OptimizingTask task = new OptimizingTask(new OptimizingTaskId(1L, 1));
    task.setTaskInput(SerializationUtil.simpleSerialize(input));

    Map<String, String> properties = Maps.newHashMap();
    properties.put(
        TaskProperties.TASK_EXECUTOR_FACTORY_IMPL, PaimonCompactionExecutorFactory.class.getName());
    task.setProperties(properties);

    OptimizerConfig config = OptimizerTestHelpers.buildOptimizerConfig("thrift://localhost:1261");
    OptimizingTaskResult result =
        OptimizerExecutor.executeTask(
            config, 0, task, LoggerFactory.getLogger(TestPaimonExecutorIntegration.class));

    assertNotNull(result);
    assertNotNull(result.getErrorMessage());
    // Empty Input has no PaimonTable / taskBytes, Executor rejects with IllegalStateException.
    assertTrue(
        result.getErrorMessage().contains("missing required fields")
            || result.getErrorMessage().contains("IllegalStateException"));
  }

  @Test
  @DisplayName(
      "executeTask with missing/malformed TASK_EXECUTOR_FACTORY_IMPL should return error (not throw)")
  void testPaimonExecuteTaskFactoryMissingReturnsError() {
    PaimonCompactionInput input = new PaimonCompactionInput();
    OptimizingTask task = new OptimizingTask(new OptimizingTaskId(2L, 1));
    task.setTaskInput(SerializationUtil.simpleSerialize(input));

    // Case 1: missing TASK_EXECUTOR_FACTORY_IMPL
    task.setProperties(Maps.newHashMap());
    OptimizerConfig config = OptimizerTestHelpers.buildOptimizerConfig("thrift://localhost:1261");
    OptimizingTaskResult missingResult =
        OptimizerExecutor.executeTask(
            config, 0, task, LoggerFactory.getLogger(TestPaimonExecutorIntegration.class));
    assertNotNull(missingResult);
    assertNotNull(
        missingResult.getErrorMessage(),
        "Missing TASK_EXECUTOR_FACTORY_IMPL should surface as errorMessage, not throw");

    // Case 2: malformed / non-existent class
    Map<String, String> malformedProps = Maps.newHashMap();
    malformedProps.put(
        TaskProperties.TASK_EXECUTOR_FACTORY_IMPL,
        "org.apache.amoro.formats.paimon.optimizing.NoSuchFactory");
    task.setProperties(malformedProps);
    OptimizingTaskResult malformedResult =
        OptimizerExecutor.executeTask(
            config, 0, task, LoggerFactory.getLogger(TestPaimonExecutorIntegration.class));
    assertNotNull(malformedResult);
    assertNotNull(
        malformedResult.getErrorMessage(),
        "Malformed TASK_EXECUTOR_FACTORY_IMPL should surface as errorMessage, not throw");
  }
}
