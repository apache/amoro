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
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class TestIcebergThreadPools {

  @Test
  public void testDefaultPoolsAndInitialization() throws Exception {
    ExecutorService workerPool = ThreadPools.getWorkerPool();
    Assert.assertSame(workerPool, IcebergThreadPools.getPlanningExecutor());
    Assert.assertSame(workerPool, IcebergThreadPools.getCommitExecutor());
    Assert.assertSame(workerPool, IcebergThreadPools.getThreadPool("unregistered-test-pool"));

    IcebergThreadPools.newThreadPool("registered-test-pool", 1);
    ExecutorService registeredPool = IcebergThreadPools.getThreadPool("registered-test-pool");
    Assert.assertNotSame(workerPool, registeredPool);
    IcebergThreadPools.newThreadPool("registered-test-pool", 2);
    Assert.assertSame(registeredPool, IcebergThreadPools.getThreadPool("registered-test-pool"));

    IcebergThreadPools.init(1, 1);

    ExecutorService planningPool = IcebergThreadPools.getPlanningExecutor();
    ExecutorService commitPool = IcebergThreadPools.getCommitExecutor();
    Assert.assertNotSame(workerPool, planningPool);
    Assert.assertNotSame(workerPool, commitPool);
    Assert.assertNotSame(planningPool, commitPool);
    Assert.assertTrue(
        planningPool
            .submit(() -> Thread.currentThread().getName())
            .get(10, TimeUnit.SECONDS)
            .startsWith("iceberg-planning-pool-"));
    Assert.assertTrue(
        commitPool
            .submit(() -> Thread.currentThread().getName())
            .get(10, TimeUnit.SECONDS)
            .startsWith("iceberg-commit-pool-"));

    IcebergThreadPools.init(2, 2);
    Assert.assertSame(planningPool, IcebergThreadPools.getPlanningExecutor());
    Assert.assertSame(commitPool, IcebergThreadPools.getCommitExecutor());
  }
}
