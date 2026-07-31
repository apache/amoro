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

import static org.apache.flink.configuration.HighAvailabilityOptions.HA_CLUSTER_ID;
import static org.apache.flink.configuration.TaskManagerOptions.TASK_CANCELLATION_TIMEOUT;
import static org.apache.flink.configuration.TaskManagerOptions.TASK_MANAGER_RESOURCE_ID;

import org.apache.amoro.optimizer.common.OptimizerConfig;
import org.apache.amoro.optimizer.common.OptimizerExecutor;
import org.apache.flink.streaming.api.operators.AbstractStreamOperator;
import org.apache.flink.streaming.api.operators.OneInputStreamOperator;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;

public class FlinkExecutor extends AbstractStreamOperator<Void>
    implements OneInputStreamOperator<String, Void> {

  /**
   * Kept below Flink's cancellation watchdog deadline (task.cancellation.timeout) so the
   * force-interrupt and thread exit complete before Flink fails the whole TaskManager.
   */
  static final long CANCELLATION_SAFETY_MARGIN_MS = 10_000L;

  private final OptimizerExecutor[] allExecutors;
  private final long shutdownTimeoutMs;
  private final long heartbeatIntervalMs;
  private FlinkOptimizerExecutor executor;
  private String optimizeGroupName;
  private Thread optimizerThread;
  private long drainTimeoutMs;

  public FlinkExecutor(
      OptimizerExecutor[] allExecutors, String optimizeGroupName, OptimizerConfig config) {
    this.allExecutors = allExecutors;
    this.optimizeGroupName = optimizeGroupName;
    this.shutdownTimeoutMs = config.getShutdownTimeoutMs();
    this.heartbeatIntervalMs = config.getHeartBeat();
  }

  @Override
  public void open() throws Exception {
    super.open();
    int subTaskIndex = getRuntimeContext().getIndexOfThisSubtask();
    String taskManagerId =
        getRuntimeContext()
            .getTaskManagerRuntimeInfo()
            .getConfiguration()
            .get(TASK_MANAGER_RESOURCE_ID);
    String applicationId =
        getRuntimeContext().getTaskManagerRuntimeInfo().getConfiguration().getString(HA_CLUSTER_ID);
    executor = (FlinkOptimizerExecutor) allExecutors[subTaskIndex];
    // set optimizer flink runtime info, including application_id, tm_id, host
    if (applicationId != null) {
      executor.addRuntimeContext("application_id", applicationId);
    }
    if (taskManagerId != null) {
      executor.addRuntimeContext("tm_id", taskManagerId);
    }
    executor.addRuntimeContext("subtask_index", String.valueOf(subTaskIndex));
    // add label optimize_group;
    getMetricGroup().getAllVariables().put("<optimizer_group>", optimizeGroupName);
    executor.initOperatorMetric(getMetricGroup());
    long taskCancellationTimeoutMs =
        getRuntimeContext()
            .getTaskManagerRuntimeInfo()
            .getConfiguration()
            .get(TASK_CANCELLATION_TIMEOUT);
    drainTimeoutMs = effectiveDrainTimeoutMs(shutdownTimeoutMs, taskCancellationTimeoutMs);
    optimizerThread =
        new Thread(() -> executor.start(), "flink-optimizer-executor-" + subTaskIndex);
    optimizerThread.setDaemon(true);
    optimizerThread.start();
  }

  @Override
  public void close() throws Exception {
    if (executor != null) {
      executor.stop();
    }
    // The FlinkToucher source is cancelled before this operator, so heartbeats have already
    // stopped when the drain begins. Keep the registration alive by touching AMS with the
    // existing token from here — otherwise AMS expires the optimizer after its heartbeat
    // timeout (default 1 min) and resets the in-flight task, dropping the drained result.
    Runnable drainHeartbeat = executor != null ? executor::bestEffortTouch : null;
    drainThenForceStop(optimizerThread, drainTimeoutMs, heartbeatIntervalMs, drainHeartbeat);
  }

  /**
   * Bounds the graceful drain by Flink's cancellation watchdog: when a cancelled task has not
   * terminated within task.cancellation.timeout (default 180s), Flink kills the entire TaskManager,
   * so waiting any longer than that would turn a graceful drain into a TM failure. A value of 0
   * disables the watchdog, in which case the full shutdown timeout applies.
   */
  static long effectiveDrainTimeoutMs(long shutdownTimeoutMs, long taskCancellationTimeoutMs) {
    if (taskCancellationTimeoutMs <= 0) {
      return shutdownTimeoutMs;
    }
    long cap = taskCancellationTimeoutMs - CANCELLATION_SAFETY_MARGIN_MS;
    return Math.max(0, Math.min(shutdownTimeoutMs, cap));
  }

  static void drainThenForceStop(
      Thread optimizerThread,
      long drainTimeoutMs,
      long heartbeatIntervalMs,
      Runnable drainHeartbeat) {
    if (optimizerThread == null || !optimizerThread.isAlive()) {
      return;
    }
    // Flink's cancellation machinery (TaskCanceler/TaskInterrupter) interrupts the task thread
    // running close() to unblock I/O; absorb those interrupts and keep waiting until the drain
    // deadline — the budget is capped below the cancellation watchdog, so close() always
    // returns before Flink escalates to failing the TaskManager.
    boolean selfInterrupted =
        joinUntil(optimizerThread, drainTimeoutMs, heartbeatIntervalMs, drainHeartbeat);
    if (optimizerThread.isAlive()) {
      LOG.warn(
          "Optimizer executor thread {} did not finish in-progress work within {}ms, "
              + "force-interrupting",
          optimizerThread.getName(),
          drainTimeoutMs);
      optimizerThread.interrupt();
      selfInterrupted |= joinUntil(optimizerThread, 1_000, 1_000, null);
    }
    if (selfInterrupted) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Joins the thread until the deadline, absorbing interrupts of the calling thread and running the
   * heartbeat action between join slices. Returns whether an interrupt was absorbed so the caller
   * can restore the flag afterwards.
   */
  private static boolean joinUntil(
      Thread thread, long timeoutMs, long heartbeatIntervalMs, Runnable heartbeat) {
    boolean interrupted = false;
    long deadline = System.currentTimeMillis() + timeoutMs;
    while (thread.isAlive()) {
      long remaining = deadline - System.currentTimeMillis();
      if (remaining <= 0) {
        break;
      }
      try {
        thread.join(Math.min(remaining, heartbeatIntervalMs));
      } catch (InterruptedException e) {
        interrupted = true;
      }
      if (heartbeat != null && thread.isAlive()) {
        heartbeat.run();
      }
    }
    return interrupted;
  }

  @Override
  public void processElement(StreamRecord<String> element) {
    executor.setToken(element.getValue());
  }
}
