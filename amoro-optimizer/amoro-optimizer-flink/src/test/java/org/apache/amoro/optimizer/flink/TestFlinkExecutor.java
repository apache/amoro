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

package org.apache.amoro.optimizer.flink;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestFlinkExecutor {

  @Test
  public void testDrainWaitsForInProgressTask() throws InterruptedException {
    AtomicBoolean taskCompleted = new AtomicBoolean(false);
    AtomicBoolean interrupted = new AtomicBoolean(false);
    Thread worker =
        new Thread(
            () -> {
              try {
                TimeUnit.MILLISECONDS.sleep(500);
                taskCompleted.set(true);
              } catch (InterruptedException e) {
                interrupted.set(true);
              }
            });
    worker.start();

    FlinkExecutor.drainThenForceStop(worker, 10_000);

    Assertions.assertFalse(worker.isAlive(), "Worker thread should have terminated");
    Assertions.assertTrue(taskCompleted.get(), "In-progress task should complete within budget");
    Assertions.assertFalse(interrupted.get(), "Task within budget must not be interrupted");
  }

  @Test
  public void testDrainForceInterruptsAfterTimeout() throws InterruptedException {
    AtomicBoolean interrupted = new AtomicBoolean(false);
    Thread worker =
        new Thread(
            () -> {
              try {
                TimeUnit.MINUTES.sleep(10);
              } catch (InterruptedException e) {
                interrupted.set(true);
              }
            });
    worker.start();

    long start = System.currentTimeMillis();
    FlinkExecutor.drainThenForceStop(worker, 300);
    long elapsed = System.currentTimeMillis() - start;

    Assertions.assertFalse(worker.isAlive(), "Worker thread should have been force-stopped");
    Assertions.assertTrue(interrupted.get(), "Task exceeding budget should be interrupted");
    Assertions.assertTrue(
        elapsed < 5_000, "Drain should return promptly after budget, elapsed=" + elapsed + "ms");
  }

  @Test
  public void testDrainSurvivesCallerInterrupts() throws InterruptedException {
    AtomicBoolean taskCompleted = new AtomicBoolean(false);
    AtomicBoolean workerInterrupted = new AtomicBoolean(false);
    Thread worker =
        new Thread(
            () -> {
              try {
                TimeUnit.MILLISECONDS.sleep(800);
                taskCompleted.set(true);
              } catch (InterruptedException e) {
                workerInterrupted.set(true);
              }
            });
    worker.start();

    // Simulate Flink's TaskCanceler/TaskInterrupter interrupting the task thread that is
    // running close() during job cancellation: the drain must absorb it and keep waiting.
    Thread drainThread = Thread.currentThread();
    Thread canceler =
        new Thread(
            () -> {
              try {
                TimeUnit.MILLISECONDS.sleep(200);
              } catch (InterruptedException e) {
                return;
              }
              drainThread.interrupt();
            });
    canceler.start();
    try {
      FlinkExecutor.drainThenForceStop(worker, 10_000);

      Assertions.assertTrue(
          Thread.interrupted(), "Self-interrupt must be restored after the drain completes");
      Assertions.assertFalse(worker.isAlive(), "Worker thread should have terminated");
      Assertions.assertTrue(
          taskCompleted.get(), "Drain must keep waiting across caller interrupts");
      Assertions.assertFalse(
          workerInterrupted.get(), "Task within budget must not be force-interrupted");
    } finally {
      Thread.interrupted();
      canceler.interrupt();
    }
  }

  @Test
  public void testDrainToleratesDeadOrNullThread() {
    FlinkExecutor.drainThenForceStop(null, 1_000);

    Thread finished = new Thread(() -> {});
    finished.start();
    FlinkExecutor.drainThenForceStop(finished, 1_000);
  }

  @Test
  public void testEffectiveDrainTimeoutCappedByCancellationTimeout() {
    // Flink kills the TaskManager when a cancelled task does not terminate within
    // task.cancellation.timeout (default 180s), so the drain budget must stay below it.
    Assertions.assertEquals(
        180_000 - FlinkExecutor.CANCELLATION_SAFETY_MARGIN_MS,
        FlinkExecutor.effectiveDrainTimeoutMs(600_000, 180_000));
  }

  @Test
  public void testEffectiveDrainTimeoutUsesShutdownTimeoutWhenSmaller() {
    Assertions.assertEquals(60_000, FlinkExecutor.effectiveDrainTimeoutMs(60_000, 180_000));
  }

  @Test
  public void testEffectiveDrainTimeoutUnlimitedCancellationWatchdog() {
    // task.cancellation.timeout = 0 disables Flink's cancellation watchdog entirely.
    Assertions.assertEquals(600_000, FlinkExecutor.effectiveDrainTimeoutMs(600_000, 0));
  }

  @Test
  public void testEffectiveDrainTimeoutNeverNegative() {
    Assertions.assertEquals(0, FlinkExecutor.effectiveDrainTimeoutMs(600_000, 1_000));
  }
}
