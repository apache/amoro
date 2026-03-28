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

package org.apache.amoro.maintainer;

import java.util.function.Supplier;

/**
 * Executor for running maintainer operations with consistent metrics recording.
 *
 * <p>This executor ensures that all maintainer operations record metrics in a consistent way:
 *
 * <ul>
 *   <li>Record operation start
 *   <li>Execute the operation
 *   <li>Record operation success/failure with duration
 * </ul>
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * MaintainerOperationExecutor executor = new MaintainerOperationExecutor(metrics);
 * executor.execute(
 *     MaintainerOperationType.ORPHAN_FILES_CLEANING,
 *     () -> {
 *       // Operation logic here
 *       cleanOrphanFiles();
 *     }
 * );
 * }</pre>
 */
public class MaintainerOperationExecutor {

  private final MaintainerMetrics metrics;

  /**
   * Creates a new operation executor with the given metrics collector.
   *
   * @param metrics the metrics collector (can be null, will use NOOP in that case)
   */
  public MaintainerOperationExecutor(MaintainerMetrics metrics) {
    this.metrics = metrics != null ? metrics : MaintainerMetrics.NOOP;
  }

  /**
   * Executes a maintainer operation with metrics recording.
   *
   * <p>This method will:
   *
   * <ol>
   *   <li>Record the operation start via {@link MaintainerMetrics#recordOperationStart}
   *   <li>Execute the provided operation
   *   <li>On success: record operation success via {@link MaintainerMetrics#recordOperationSuccess}
   *   <li>On failure: record operation failure via {@link MaintainerMetrics#recordOperationFailure}
   *       and rethrow the exception
   * </ol>
   *
   * @param operationType the type of operation being executed
   * @param operation the operation to execute
   */
  public void execute(MaintainerOperationType operationType, Runnable operation) {
    long startTime = System.currentTimeMillis();
    metrics.recordOperationStart(operationType);

    try {
      operation.run();
      long duration = System.currentTimeMillis() - startTime;
      metrics.recordOperationSuccess(operationType, duration);
    } catch (Throwable t) {
      long duration = System.currentTimeMillis() - startTime;
      metrics.recordOperationFailure(operationType, duration, t);
      throw t;
    }
  }

  /**
   * Executes a maintainer operation with metrics recording and return result.
   *
   * <p>This method will:
   *
   * <ol>
   *   <li>Record the operation start via {@link MaintainerMetrics#recordOperationStart}
   *   <li>Execute the provided operation
   *   <li>On success: record operation success via {@link MaintainerMetrics#recordOperationSuccess}
   *   <li>On failure: record operation failure via {@link MaintainerMetrics#recordOperationFailure}
   *       and rethrow the exception
   * </ol>
   *
   * @param operationType the type of operation being executed
   * @param operation the operation to execute
   * @param <T> the result type
   * @return the operation result
   */
  public <T> T executeAndReturn(MaintainerOperationType operationType, Supplier<T> operation) {
    long startTime = System.currentTimeMillis();
    metrics.recordOperationStart(operationType);

    try {
      T result = operation.get();
      long duration = System.currentTimeMillis() - startTime;
      metrics.recordOperationSuccess(operationType, duration);
      return result;
    } catch (Throwable t) {
      long duration = System.currentTimeMillis() - startTime;
      metrics.recordOperationFailure(operationType, duration, t);
      throw t;
    }
  }
}
