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

import org.apache.amoro.api.OptimizerRegisterInfo;
import org.apache.amoro.api.OptimizingTask;
import org.apache.amoro.api.OptimizingTaskId;
import org.apache.amoro.api.OptimizingTaskResult;
import org.apache.amoro.optimizing.BaseOptimizingInput;
import org.apache.amoro.optimizing.OptimizingExecutor;
import org.apache.amoro.optimizing.OptimizingExecutorFactory;
import org.apache.amoro.optimizing.TableOptimizing;
import org.apache.amoro.optimizing.TaskProperties;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.thrift.org.apache.thrift.TException;
import org.apache.amoro.utils.SerializationUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TestOptimizerExecutor extends OptimizerTestBase {

  private static final String FAILED_TASK_MESSAGE = "Execute Task failed";

  /**
   * When set, slow test tasks count this down as soon as execute() begins, letting tests wait
   * deterministically for "the task is now in progress" instead of sleeping fixed intervals.
   */
  static volatile CountDownLatch slowExecutionStartedLatch;

  private OptimizerExecutor optimizerExecutor;

  @BeforeEach
  public void startOptimizer() {
    OptimizerConfig optimizerConfig =
        OptimizerTestHelpers.buildOptimizerConfig(TEST_AMS.getServerUrl());
    optimizerExecutor = new OptimizerExecutor(optimizerConfig, 0);
    new Thread(optimizerExecutor::start).start();
  }

  @AfterEach
  public void stopOptimizer() {
    optimizerExecutor.stop();
  }

  @Test
  public void testWaitForToken() throws InterruptedException {
    TEST_AMS.getOptimizerHandler().offerTask(TestOptimizingInput.successInput(1).toTask(0, 0));
    TimeUnit.MILLISECONDS.sleep(OptimizerTestHelpers.CALL_AMS_INTERVAL * 2);
    Assertions.assertEquals(1, TEST_AMS.getOptimizerHandler().getPendingTasks().size());
  }

  @Test
  public void testExecuteTaskSuccess() throws InterruptedException, TException {
    TEST_AMS.getOptimizerHandler().authenticate(new OptimizerRegisterInfo());
    String token =
        TEST_AMS.getOptimizerHandler().getRegisteredOptimizers().keySet().iterator().next();
    TEST_AMS.getOptimizerHandler().offerTask(TestOptimizingInput.successInput(1).toTask(0, 0));
    Assertions.assertEquals(1, TEST_AMS.getOptimizerHandler().getPendingTasks().size());
    optimizerExecutor.setToken(token);
    TimeUnit.MILLISECONDS.sleep(OptimizerTestHelpers.CALL_AMS_INTERVAL * 2);
    Assertions.assertEquals(0, TEST_AMS.getOptimizerHandler().getPendingTasks().size());
    Assertions.assertTrue(TEST_AMS.getOptimizerHandler().getCompletedTasks().containsKey(token));
    Assertions.assertEquals(
        1, TEST_AMS.getOptimizerHandler().getCompletedTasks().get(token).size());
    OptimizingTaskResult taskResult =
        TEST_AMS.getOptimizerHandler().getCompletedTasks().get(token).get(0);
    Assertions.assertEquals(new OptimizingTaskId(0, 0), taskResult.getTaskId());
    Assertions.assertNull(taskResult.getErrorMessage());
    TestOptimizingOutput output = SerializationUtil.simpleDeserialize(taskResult.getTaskOutput());
    Assertions.assertEquals(1, output.inputId());
  }

  @Test
  public void testCompleteTaskRetriesTransientErrorDuringShutdownDrain() throws TException {
    TEST_AMS.getOptimizerHandler().authenticate(new OptimizerRegisterInfo());
    String token =
        TEST_AMS.getOptimizerHandler().getRegisteredOptimizers().keySet().iterator().next();
    optimizerExecutor.setToken(token);
    TEST_AMS.getOptimizerHandler().ackTask(token, 0, new OptimizingTaskId(0, 0));

    // Shutdown already requested when the in-flight task finishes and reports its result.
    optimizerExecutor.stop();
    TEST_AMS.getOptimizerHandler().failNextCompleteTasks(2);

    OptimizingTaskResult result = new OptimizingTaskResult(new OptimizingTaskId(0, 0), 0);
    optimizerExecutor.completeTask(TEST_AMS.getServerUrl(), result);

    Assertions.assertNotNull(
        TEST_AMS.getOptimizerHandler().getCompletedTasks().get(token),
        "Task result must be reported despite transient AMS errors during the shutdown drain");
    Assertions.assertEquals(
        1, TEST_AMS.getOptimizerHandler().getCompletedTasks().get(token).size());
  }

  @Test
  public void testDrainWindowAnchorsAtStopTime() throws InterruptedException, TException {
    TEST_AMS.getOptimizerHandler().authenticate(new OptimizerRegisterInfo());
    String token =
        TEST_AMS.getOptimizerHandler().getRegisteredOptimizers().keySet().iterator().next();
    optimizerExecutor.setToken(token);
    TEST_AMS.getOptimizerHandler().ackTask(token, 0, new OptimizingTaskId(0, 0));

    // AMS keeps failing while the optimizer is still running, for longer than the drain budget.
    TEST_AMS.getOptimizerHandler().failNextCompleteTasks(Integer.MAX_VALUE);
    OptimizingTaskResult result = new OptimizingTaskResult(new OptimizingTaskId(0, 0), 0);
    AtomicReference<Exception> error = new AtomicReference<>();
    Thread caller =
        new Thread(
            () -> {
              try {
                optimizerExecutor.callAuthenticatedAmsWithDrain(
                    TEST_AMS.getServerUrl(),
                    (client, callToken) -> {
                      client.completeTask(callToken, result);
                      return null;
                    },
                    2_000);
              } catch (Exception e) {
                error.set(e);
              }
            });
    caller.start();
    TimeUnit.MILLISECONDS.sleep(2_500);

    // Shutdown begins and AMS recovers: the drain window must start counting from now,
    // not from call entry (which is already past the 2s budget).
    optimizerExecutor.stop();
    TEST_AMS.getOptimizerHandler().failNextCompleteTasks(0);
    caller.join(5_000);

    Assertions.assertFalse(caller.isAlive(), "Drain call should have finished");
    Assertions.assertNull(
        error.get(), "Result must be reported within a full drain window anchored at stop time");
    Assertions.assertEquals(
        1, TEST_AMS.getOptimizerHandler().getCompletedTasks().get(token).size());
  }

  @Test
  public void testEchoTaskAttemptIdOnSuccess() throws InterruptedException, TException {
    TEST_AMS.getOptimizerHandler().authenticate(new OptimizerRegisterInfo());
    String token =
        TEST_AMS.getOptimizerHandler().getRegisteredOptimizers().keySet().iterator().next();
    OptimizingTask task = TestOptimizingInput.successInput(1).toTask(0, 0);
    task.getProperties().put(TaskProperties.TASK_ATTEMPT_ID, "7");
    TEST_AMS.getOptimizerHandler().offerTask(task);
    optimizerExecutor.setToken(token);
    TimeUnit.MILLISECONDS.sleep(OptimizerTestHelpers.CALL_AMS_INTERVAL * 2);
    OptimizingTaskResult taskResult =
        TEST_AMS.getOptimizerHandler().getCompletedTasks().get(token).get(0);
    Assertions.assertNull(taskResult.getErrorMessage());
    // TestOptimizingOutput.summary() is null, so the echo must create the summary map itself
    Assertions.assertNotNull(taskResult.getSummary());
    Assertions.assertEquals("7", taskResult.getSummary().get(TaskProperties.TASK_ATTEMPT_ID));
  }

  @Test
  public void testEchoTaskAttemptIdOnFailure() throws InterruptedException, TException {
    TEST_AMS.getOptimizerHandler().authenticate(new OptimizerRegisterInfo());
    String token =
        TEST_AMS.getOptimizerHandler().getRegisteredOptimizers().keySet().iterator().next();
    OptimizingTask task = TestOptimizingInput.failedInput(1).toTask(0, 0);
    task.getProperties().put(TaskProperties.TASK_ATTEMPT_ID, "7");
    TEST_AMS.getOptimizerHandler().offerTask(task);
    optimizerExecutor.setToken(token);
    TimeUnit.MILLISECONDS.sleep(OptimizerTestHelpers.CALL_AMS_INTERVAL * 2);
    OptimizingTaskResult taskResult =
        TEST_AMS.getOptimizerHandler().getCompletedTasks().get(token).get(0);
    // an error result reports the attempt id too, so a stale FAILED completion cannot poison the
    // retry counting of the current attempt
    Assertions.assertTrue(taskResult.getErrorMessage().contains(FAILED_TASK_MESSAGE));
    Assertions.assertEquals("7", taskResult.getSummary().get(TaskProperties.TASK_ATTEMPT_ID));
  }

  @Test
  public void testExecuteTaskFailed() throws InterruptedException, TException {
    TEST_AMS.getOptimizerHandler().authenticate(new OptimizerRegisterInfo());
    String token =
        TEST_AMS.getOptimizerHandler().getRegisteredOptimizers().keySet().iterator().next();
    TEST_AMS.getOptimizerHandler().offerTask(TestOptimizingInput.failedInput(1).toTask(0, 0));
    Assertions.assertEquals(1, TEST_AMS.getOptimizerHandler().getPendingTasks().size());
    optimizerExecutor.setToken(token);
    TimeUnit.MILLISECONDS.sleep(OptimizerTestHelpers.CALL_AMS_INTERVAL * 2);
    Assertions.assertEquals(0, TEST_AMS.getOptimizerHandler().getPendingTasks().size());
    Assertions.assertTrue(TEST_AMS.getOptimizerHandler().getCompletedTasks().containsKey(token));
    Assertions.assertEquals(
        1, TEST_AMS.getOptimizerHandler().getCompletedTasks().get(token).size());
    OptimizingTaskResult taskResult =
        TEST_AMS.getOptimizerHandler().getCompletedTasks().get(token).get(0);
    Assertions.assertEquals(new OptimizingTaskId(0, 0), taskResult.getTaskId());
    Assertions.assertNull(taskResult.getTaskOutput());
    Assertions.assertTrue(taskResult.getErrorMessage().contains(FAILED_TASK_MESSAGE));
  }

  public static class TestOptimizingInput extends BaseOptimizingInput {
    private final int inputId;
    private final boolean executeSuccess;
    private final long executionTimeMs;

    private TestOptimizingInput(int inputId, boolean executeSuccess) {
      this(inputId, executeSuccess, 0L);
    }

    private TestOptimizingInput(int inputId, boolean executeSuccess, long executionTimeMs) {
      this.inputId = inputId;
      this.executeSuccess = executeSuccess;
      this.executionTimeMs = executionTimeMs;
    }

    public static TestOptimizingInput successInput(int inputId) {
      return new TestOptimizingInput(inputId, true);
    }

    public static TestOptimizingInput failedInput(int inputId) {
      return new TestOptimizingInput(inputId, false);
    }

    public static TestOptimizingInput slowSuccessInput(int inputId, long executionTimeMs) {
      return new TestOptimizingInput(inputId, true, executionTimeMs);
    }

    private int inputId() {
      return inputId;
    }

    private long executionTimeMs() {
      return executionTimeMs;
    }

    public OptimizingTask toTask(long processId, int taskId) {
      OptimizingTask optimizingTask = new OptimizingTask(new OptimizingTaskId(processId, taskId));
      optimizingTask.setTaskInput(SerializationUtil.simpleSerialize(this));
      Map<String, String> inputProperties = Maps.newHashMap();
      inputProperties.put(
          TaskProperties.TASK_EXECUTOR_FACTORY_IMPL, TestOptimizingExecutorFactory.class.getName());
      optimizingTask.setProperties(inputProperties);
      return optimizingTask;
    }
  }

  public static class TestOptimizingExecutorFactory
      implements OptimizingExecutorFactory<TestOptimizingInput> {

    @Override
    public void initialize(Map<String, String> properties) {}

    @Override
    public OptimizingExecutor createExecutor(TestOptimizingInput input) {
      return new TestOptimizingExecutor(input);
    }
  }

  public static class TestOptimizingExecutor implements OptimizingExecutor<TestOptimizingOutput> {

    private final TestOptimizingInput input;

    private TestOptimizingExecutor(TestOptimizingInput input) {
      this.input = input;
    }

    @Override
    public TestOptimizingOutput execute() {
      if (input.executionTimeMs() > 0) {
        CountDownLatch latch = slowExecutionStartedLatch;
        if (latch != null) {
          latch.countDown();
        }
        try {
          Thread.sleep(input.executionTimeMs());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new IllegalStateException("Task was interrupted before completion", e);
        }
      }
      if (input.executeSuccess) {
        return new TestOptimizingOutput(input.inputId());
      } else {
        throw new IllegalStateException(FAILED_TASK_MESSAGE);
      }
    }
  }

  public static class TestOptimizingOutput implements TableOptimizing.OptimizingOutput {

    private final int inputId;

    private TestOptimizingOutput(int inputId) {
      this.inputId = inputId;
    }

    @Override
    public Map<String, String> summary() {
      return null;
    }

    private int inputId() {
      return inputId;
    }
  }
}
