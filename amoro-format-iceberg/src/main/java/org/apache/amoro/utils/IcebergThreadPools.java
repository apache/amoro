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

import org.apache.iceberg.util.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/** Long-lived Iceberg I/O pools that isolate Amoro maintenance workloads from one another. */
public class IcebergThreadPools {

  private static final Logger LOG = LoggerFactory.getLogger(IcebergThreadPools.class);

  private static final String PLANNING_POOL_NAME_PREFIX = "iceberg-planning-pool";
  private static final String COMMIT_POOL_NAME_PREFIX = "iceberg-commit-pool";

  private static final Map<String, Integer> POOL_SIZES = new ConcurrentHashMap<>();
  private static final Map<String, ExecutorService> POOLS = new ConcurrentHashMap<>();

  /**
   * Initializes the self-optimizing Iceberg I/O pools.
   *
   * <p>Thread pools are process-wide and can only be initialized once. Repeated initialization is
   * ignored because existing pools cannot be resized.
   */
  public static synchronized void init(int planningThreadPoolSize, int commitThreadPoolSize) {
    newThreadPool(PLANNING_POOL_NAME_PREFIX, planningThreadPoolSize);
    newThreadPool(COMMIT_POOL_NAME_PREFIX, commitThreadPoolSize);

    LOG.info(
        "Initialized Iceberg thread pools, self-optimizing planning: {}, self-optimizing commit: {}",
        POOL_SIZES.get(PLANNING_POOL_NAME_PREFIX),
        POOL_SIZES.get(COMMIT_POOL_NAME_PREFIX));
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
   * <p>Before the dedicated pool is initialized, this returns Iceberg's global worker pool.
   *
   * @return an {@link ExecutorService} that uses the self-optimizing planning pool, or Iceberg's
   *     global worker pool if the dedicated pool has not been initialized
   */
  public static ExecutorService getPlanningExecutor() {
    return getThreadPool(PLANNING_POOL_NAME_PREFIX);
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
   * <p>Before the dedicated pool is initialized, this returns Iceberg's global worker pool.
   *
   * @return an {@link ExecutorService} that uses the self-optimizing commit pool, or Iceberg's
   *     global worker pool if the dedicated pool has not been initialized
   */
  public static ExecutorService getCommitExecutor() {
    return getThreadPool(COMMIT_POOL_NAME_PREFIX);
  }

  public static ExecutorService getThreadPool(String namePrefix) {
    ExecutorService executorService = POOLS.get(namePrefix);
    if (executorService == null) {
      return ThreadPools.getWorkerPool();
    }
    return executorService;
  }

  public static synchronized void newThreadPool(String namePrefix, int poolSize) {
    if (namePrefix == null || namePrefix.isEmpty()) {
      throw new IllegalArgumentException("Thread pool name prefix must not be empty");
    }
    if (poolSize <= 0) {
      throw new IllegalArgumentException("Thread pool size must be greater than 0");
    }

    ExecutorService existingPool = POOLS.get(namePrefix);
    if (existingPool != null) {
      int existingPoolSize = POOL_SIZES.get(namePrefix);
      if (existingPoolSize != poolSize) {
        LOG.warn(
            "Iceberg thread pool {} is already initialized with size {} and cannot be resized to {}; keeping the existing pool",
            namePrefix,
            existingPoolSize,
            poolSize);
      }
      return;
    }

    ExecutorService executorService = ThreadPools.newExitingWorkerPool(namePrefix, poolSize);
    POOL_SIZES.put(namePrefix, poolSize);
    POOLS.put(namePrefix, executorService);
  }
}
