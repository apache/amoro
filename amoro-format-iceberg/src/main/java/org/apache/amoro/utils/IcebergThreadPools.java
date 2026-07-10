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

package org.apache.amoro.utils;

import org.apache.amoro.config.ConfigOption;
import org.apache.amoro.config.Configurations;
import org.apache.iceberg.util.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/** Long-lived Iceberg I/O pools that isolate Amoro maintenance workloads from one another. */
public class IcebergThreadPools {

  private static final Logger LOG = LoggerFactory.getLogger(IcebergThreadPools.class);

  private static ExecutorService planningPool;
  private static ExecutorService commitPool;
  private static volatile boolean optimizingPoolsInited;

  private IcebergThreadPools() {}

  /**
   * Initializes the self-optimizing Iceberg I/O pools from the supplied configuration options.
   *
   * <p>Thread pools are process-wide and can only be initialized once. Repeated initialization is
   * ignored because existing pools cannot be resized.
   */
  public static synchronized void initSelfOptimizingPools(
      Configurations configurations,
      ConfigOption<Integer> planningPoolSizeOption,
      ConfigOption<Integer> commitPoolSizeOption) {
    if (optimizingPoolsInited) {
      LOG.warn("Self-optimizing Iceberg thread pools are already initialized");
      return;
    }

    int planningPoolSize =
        Math.max(
            Runtime.getRuntime().availableProcessors() / 2,
            configurations.getInteger(planningPoolSizeOption));
    int commitPoolSize =
        Math.max(
            Runtime.getRuntime().availableProcessors() / 2,
            configurations.getInteger(commitPoolSizeOption));

    planningPool = ThreadPools.newExitingWorkerPool("iceberg-planning-pool", planningPoolSize);
    commitPool = ThreadPools.newExitingWorkerPool("iceberg-commit-pool", commitPoolSize);
    optimizingPoolsInited = true;

    LOG.info(
        "Initialized Iceberg thread pools, self-optimizing planning: {}, self-optimizing commit: {}",
        planningPoolSize,
        commitPoolSize);
  }

  /**
   * Return an {@link ExecutorService} that uses the self-optimizing planning pool.
   *
   * <p>The size of this pool limits the number of tasks concurrently reading manifests across all
   * self-optimizing planning operations.
   *
   * <p>The size of this pool is controlled by the AMS configuration {@code
   * self-optimizing.plan-manifest-io-thread-count}.
   *
   * @return an {@link ExecutorService} that uses the self-optimizing planning pool
   * @throws IllegalStateException if the pools have not been initialized
   */
  public static ExecutorService getPlanningPool() {
    if (!optimizingPoolsInited) {
      throw new IllegalStateException("Iceberg planning pool has not been initialized");
    }
    return planningPool;
  }

  /**
   * Return an {@link ExecutorService} that uses the self-optimizing commit pool.
   *
   * <p>The size of this pool limits the number of tasks concurrently filtering manifests across all
   * self-optimizing commit operations. It does not replace Iceberg's worker pool used internally by
   * {@code SnapshotProducer.writeManifests}.
   *
   * <p>The size of this pool is controlled by the AMS configuration {@code
   * self-optimizing.commit-manifest-io-thread-count}.
   *
   * @return an {@link ExecutorService} that uses the self-optimizing commit pool
   * @throws IllegalStateException if the pools have not been initialized
   */
  public static ExecutorService getCommitPool() {
    if (!optimizingPoolsInited) {
      throw new IllegalStateException("Iceberg commit pool has not been initialized");
    }
    return commitPool;
  }
}
