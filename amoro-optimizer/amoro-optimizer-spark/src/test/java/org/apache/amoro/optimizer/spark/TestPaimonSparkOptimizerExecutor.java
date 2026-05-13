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

package org.apache.amoro.optimizer.spark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.api.OptimizingTask;
import org.apache.amoro.api.OptimizingTaskId;
import org.apache.amoro.api.OptimizingTaskResult;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionExecutorFactory;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionInput;
import org.apache.amoro.optimizer.common.OptimizerConfig;
import org.apache.amoro.optimizing.TaskProperties;
import org.apache.amoro.utils.SerializationUtil;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kohsuke.args4j.CmdLineException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DisplayName("Paimon Spark optimizer executor classpath")
public class TestPaimonSparkOptimizerExecutor {

  private JavaSparkContext jsc;

  @BeforeEach
  void startSpark() {
    SparkConf conf =
        new SparkConf()
            .setMaster("local[1]")
            .setAppName("paimon-spark-optimizer-executor-test")
            .set("spark.ui.enabled", "false")
            .set("spark.driver.bindAddress", "127.0.0.1")
            .set("spark.driver.host", "127.0.0.1");
    jsc = new JavaSparkContext(conf);
  }

  @AfterEach
  void stopSpark() {
    if (jsc != null) {
      jsc.close();
      jsc = null;
    }
  }

  @Test
  @DisplayName("local Spark executor returns errorMessage for invalid Paimon input")
  void testLocalSparkExecutorReturnsErrorMessageForInvalidPaimonInput() throws CmdLineException {
    OptimizingTask task = new OptimizingTask(new OptimizingTaskId(88L, 1));
    task.setTaskInput(SerializationUtil.simpleSerialize(new PaimonCompactionInput()));
    Map<String, String> properties = new HashMap<>();
    properties.put(
        TaskProperties.TASK_EXECUTOR_FACTORY_IMPL, PaimonCompactionExecutorFactory.class.getName());
    task.setProperties(properties);

    SparkOptimizingTaskFunction function = new SparkOptimizingTaskFunction(optimizerConfig(), 3);
    List<OptimizingTaskResult> results =
        jsc.parallelize(Collections.singletonList(task), 1).map(function).collect();

    assertEquals(1, results.size());
    OptimizingTaskResult result = results.get(0);
    assertEquals(task.getTaskId(), result.getTaskId());
    assertNotNull(result.getErrorMessage());
    assertFalse(result.getErrorMessage().isEmpty());
    assertTrue(
        result.getErrorMessage().contains("PaimonCompactionInput is missing required fields"),
        "Spark executor should return the Paimon executor failure, was: "
            + result.getErrorMessage());
  }

  private static OptimizerConfig optimizerConfig() throws CmdLineException {
    return new OptimizerConfig(
        new String[] {
          "-a", "thrift://localhost:1261", "-p", "1", "-g", "g1", "-id", "spark-test", "-hb", "1000"
        });
  }
}
