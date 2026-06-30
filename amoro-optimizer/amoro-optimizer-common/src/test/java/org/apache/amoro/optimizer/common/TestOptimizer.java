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

import org.apache.amoro.api.OptimizingTaskResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestOptimizer extends OptimizerTestBase {

  @Test
  public void testStartOptimizer() throws InterruptedException {
    OptimizerConfig optimizerConfig =
        OptimizerTestHelpers.buildOptimizerConfig(TEST_AMS.getServerUrl());
    Optimizer optimizer = new Optimizer(optimizerConfig);
    new Thread(optimizer::startOptimizing).start();
    TimeUnit.SECONDS.sleep(1);
    Assertions.assertEquals(1, TEST_AMS.getOptimizerHandler().getRegisteredOptimizers().size());
    TEST_AMS
        .getOptimizerHandler()
        .offerTask(TestOptimizerExecutor.TestOptimizingInput.successInput(1).toTask(0, 0));
    TEST_AMS
        .getOptimizerHandler()
        .offerTask(TestOptimizerExecutor.TestOptimizingInput.successInput(2).toTask(0, 1));
    TimeUnit.MILLISECONDS.sleep(OptimizerTestHelpers.CALL_AMS_INTERVAL * 10);
    String token = optimizer.getToucher().getToken();
    List<OptimizingTaskResult> taskResults =
        TEST_AMS.getOptimizerHandler().getCompletedTasks().get(token);
    Assertions.assertEquals(2, taskResults.size());
    optimizer.stopOptimizing();
  }

  @Test
  public void testGracefulShutdown() throws InterruptedException {
    OptimizerConfig optimizerConfig =
        OptimizerTestHelpers.buildOptimizerConfig(TEST_AMS.getServerUrl());
    Optimizer optimizer = new Optimizer(optimizerConfig);
    Thread optimizerThread = new Thread(optimizer::startOptimizing);
    optimizerThread.start();
    TimeUnit.SECONDS.sleep(1);

    TEST_AMS
        .getOptimizerHandler()
        .offerTask(TestOptimizerExecutor.TestOptimizingInput.successInput(1).toTask(0, 0));
    TimeUnit.MILLISECONDS.sleep(OptimizerTestHelpers.CALL_AMS_INTERVAL * 5);

    optimizer.stopOptimizing();
    optimizerThread.join(5000);

    Assertions.assertFalse(optimizerThread.isAlive(), "Optimizer thread should have terminated");

    String token = optimizer.getToucher().getToken();
    List<OptimizingTaskResult> taskResults =
        TEST_AMS.getOptimizerHandler().getCompletedTasks().get(token);
    Assertions.assertNotNull(taskResults, "Task results should be reported before shutdown");
    Assertions.assertEquals(1, taskResults.size());
  }

  @Test
  public void testGracefulShutdownWaitsForInProgressTask() throws InterruptedException {
    OptimizerConfig optimizerConfig =
        OptimizerTestHelpers.buildOptimizerConfig(TEST_AMS.getServerUrl());
    Optimizer optimizer = new Optimizer(optimizerConfig);
    Thread optimizerThread = new Thread(optimizer::startOptimizing);
    optimizerThread.start();
    TimeUnit.SECONDS.sleep(1);

    long taskExecutionMs = 3_000;
    TEST_AMS
        .getOptimizerHandler()
        .offerTask(
            TestOptimizerExecutor.TestOptimizingInput.slowSuccessInput(1, taskExecutionMs)
                .toTask(0, 0));

    // Wait long enough for the executor to poll, ack and start executing the task,
    // but short enough that it is still in the middle of execute() when we call stop.
    TimeUnit.MILLISECONDS.sleep(OptimizerTestHelpers.CALL_AMS_INTERVAL * 3);

    long startStop = System.currentTimeMillis();
    optimizer.stopOptimizing();
    long elapsed = System.currentTimeMillis() - startStop;
    optimizerThread.join(5_000);

    Assertions.assertFalse(optimizerThread.isAlive(), "Optimizer thread should have terminated");
    Assertions.assertTrue(
        elapsed >= 1_000,
        "stopOptimizing should block until in-progress task completes (elapsed=" + elapsed + "ms)");

    String token = optimizer.getToucher().getToken();
    List<OptimizingTaskResult> taskResults =
        TEST_AMS.getOptimizerHandler().getCompletedTasks().get(token);
    Assertions.assertNotNull(taskResults, "Task results should be reported before shutdown");
    Assertions.assertEquals(1, taskResults.size());
    Assertions.assertNull(
        taskResults.get(0).getErrorMessage(),
        "In-progress task must complete successfully, not be interrupted");
  }
}
