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

package org.apache.amoro.server.optimizing;

public interface OptimizingProcess {

  long getProcessId();

  void close();

  boolean isClosed();

  long getTargetSnapshotId();

  long getTargetChangeSnapshotId();

  long getPlanTime();

  long getDuration();

  OptimizingType getOptimizingType();

  Status getStatus();

  long getRunningQuotaTime(long calculatingStartTime, long calculatingEndTime);

  void commit();

  MetricsSummary getSummary();

  /**
   * Whether all tasks in this process have reached {@code SUCCESS} status and are ready to be
   * committed.
   *
   * <p>This flag is primarily used by {@link
   * org.apache.amoro.server.table.executor.TableRuntimeRefreshExecutor} to detect a stuck {@code
   * RUNNING} process whose tasks all succeeded but never transitioned to {@code COMMITTING} (see
   * issue #4172: transient failures of {@code beginCommitting()}, such as DB lock wait timeout,
   * leave the table permanently stuck in {@code *_OPTIMIZING} until AMS is restarted).
   *
   * @return {@code true} if the process has at least one task and all of them are in {@code
   *     SUCCESS} status
   */
  boolean allTasksPrepared();

  enum Status {
    RUNNING,
    CLOSED,
    SUCCESS,
    FAILED
  }
}
