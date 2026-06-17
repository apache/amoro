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

import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.shade.guava32.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** Asynchronously warms table runtimes into optimizing queues during AMS startup. */
public class OptimizingQueueWarmupService implements AutoCloseable {

  private static final Logger LOG = LoggerFactory.getLogger(OptimizingQueueWarmupService.class);
  private static final int MAX_ATTEMPTS = 3;

  private final Function<String, Optional<OptimizingQueue>> queueSupplier;
  private final Predicate<DefaultTableRuntime> runtimeStillActive;
  private final OptimizingQueueWarmupMetrics metrics;
  private final ExecutorService executor;
  private final AtomicInteger pendingTasks = new AtomicInteger();
  private final Object completionMonitor = new Object();
  private volatile boolean closed;

  public OptimizingQueueWarmupService(
      int threadCount,
      Function<String, Optional<OptimizingQueue>> queueSupplier,
      Predicate<DefaultTableRuntime> runtimeStillActive,
      OptimizingQueueWarmupMetrics metrics) {
    this.queueSupplier = queueSupplier;
    this.runtimeStillActive = runtimeStillActive;
    this.metrics = metrics;
    this.executor =
        Executors.newFixedThreadPool(
            Math.max(1, threadCount),
            new ThreadFactoryBuilder()
                .setNameFormat("optimizing-queue-warmup-%d")
                .setDaemon(true)
                .build());
  }

  public void warmupTables(List<DefaultTableRuntime> tableRuntimes) {
    List<DefaultTableRuntime> sortedRuntimes =
        Optional.ofNullable(tableRuntimes).orElseGet(Collections::emptyList).stream()
            .sorted(Comparator.comparingInt(this::priority))
            .collect(Collectors.toList());
    if (sortedRuntimes.isEmpty() || closed) {
      return;
    }
    metrics.recordSubmitted(sortedRuntimes.size());
    pendingTasks.addAndGet(sortedRuntimes.size());
    LOG.info("Start optimizing queue warmup: tables={}", sortedRuntimes.size());
    sortedRuntimes.forEach(runtime -> executor.submit(() -> warmupWithRetry(runtime)));
  }

  public boolean awaitCompletionForTest(long timeout, TimeUnit unit) throws InterruptedException {
    long deadline = System.nanoTime() + unit.toNanos(timeout);
    synchronized (completionMonitor) {
      while (pendingTasks.get() > 0) {
        long remaining = deadline - System.nanoTime();
        if (remaining <= 0) {
          return false;
        }
        TimeUnit.NANOSECONDS.timedWait(completionMonitor, remaining);
      }
      return true;
    }
  }

  @Override
  public void close() {
    closed = true;
    executor.shutdownNow();
    metrics.unregister();
    synchronized (completionMonitor) {
      completionMonitor.notifyAll();
    }
  }

  private void warmupWithRetry(DefaultTableRuntime runtime) {
    OptimizingQueue.WarmupResult result = OptimizingQueue.WarmupResult.FAILED;
    metrics.recordStart();
    try {
      for (int attempt = 1; attempt <= MAX_ATTEMPTS && !closed; attempt++) {
        result = warmup(runtime);
        if (result != OptimizingQueue.WarmupResult.FAILED) {
          return;
        }
        if (attempt < MAX_ATTEMPTS) {
          metrics.recordRetry();
          LOG.warn(
              "Optimizing queue warmup failed for table {}, retrying attempt {}/{}",
              runtime.getTableIdentifier(),
              attempt + 1,
              MAX_ATTEMPTS);
        }
      }
    } finally {
      metrics.recordResult(result);
      if (pendingTasks.decrementAndGet() == 0) {
        synchronized (completionMonitor) {
          completionMonitor.notifyAll();
        }
      }
    }
  }

  private OptimizingQueue.WarmupResult warmup(DefaultTableRuntime runtime) {
    if (!runtimeStillActive.test(runtime)) {
      LOG.info("Skip optimizing queue warmup for inactive table {}", runtime.getTableIdentifier());
      return OptimizingQueue.WarmupResult.SKIPPED;
    }
    Optional<OptimizingQueue> queue = queueSupplier.apply(runtime.getGroupName());
    if (!queue.isPresent()) {
      LOG.info(
          "Skip optimizing queue warmup for table {}, optimizer group {} does not exist",
          runtime.getTableIdentifier(),
          runtime.getGroupName());
      return OptimizingQueue.WarmupResult.SKIPPED;
    }
    return queue.get().warmupTable(runtime);
  }

  private int priority(DefaultTableRuntime runtime) {
    if (runtime.getProcessId() > 0) {
      return 0;
    }
    if (runtime.getOptimizingStatus().isProcessing()) {
      return 1;
    }
    return 2;
  }
}
