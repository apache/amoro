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

import java.util.concurrent.ExecutorService;

public class IcebergThreadPools {

  private static final Logger LOG = LoggerFactory.getLogger(IcebergThreadPools.class);
  private static volatile ExecutorService planningExecutor;
  private static volatile ExecutorService commitExecutor;

  public static void init(int planningThreadPoolSize, int commitThreadPoolSize) {
    if (planningExecutor == null) {
      synchronized (IcebergThreadPools.class) {
        if (planningExecutor == null) {
          planningExecutor =
              ThreadPools.newWorkerPool("iceberg-planning-pool", planningThreadPoolSize);
        }
      }
    }
    if (commitExecutor == null) {
      synchronized (IcebergThreadPools.class) {
        if (commitExecutor == null) {
          commitExecutor = ThreadPools.newWorkerPool("iceberg-commit-pool", commitThreadPoolSize);
        }
      }
    }

    LOG.info(
        "init iceberg thread pool success, planningExecutor size:{},commitExecutor size:{}",
        planningThreadPoolSize,
        commitThreadPoolSize);
  }

  public static ExecutorService getPlanningExecutor() {
    if (planningExecutor == null) {
      synchronized (IcebergThreadPools.class) {
        if (planningExecutor == null) {
          planningExecutor =
              ThreadPools.newWorkerPool(
                  "iceberg-planning-pool", Runtime.getRuntime().availableProcessors());
        }
      }
    }
    return planningExecutor;
  }

  public static ExecutorService getCommitExecutor() {
    if (commitExecutor == null) {
      synchronized (IcebergThreadPools.class) {
        if (commitExecutor == null) {
          commitExecutor =
              ThreadPools.newWorkerPool(
                  "iceberg-commit-pool", Runtime.getRuntime().availableProcessors());
        }
      }
    }
    return commitExecutor;
  }
}
