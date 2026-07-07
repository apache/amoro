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

import org.apache.amoro.OptimizerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class Optimizer {
  private static final Logger LOG = LoggerFactory.getLogger(Optimizer.class);

  private final OptimizerConfig config;
  private final OptimizerToucher toucher;
  private final OptimizerExecutor[] executors;
  private volatile Thread[] executorThreads;

  public Optimizer(OptimizerConfig config) {
    this(config, () -> new OptimizerToucher(config), (i) -> new OptimizerExecutor(config, i));
  }

  protected Optimizer(
      OptimizerConfig config,
      Supplier<OptimizerToucher> toucherFactory,
      IntFunction<OptimizerExecutor> executorFactory) {
    this.config = config;
    this.toucher = toucherFactory.get();
    this.executors = new OptimizerExecutor[config.getExecutionParallel()];
    IntStream.range(0, config.getExecutionParallel())
        .forEach(i -> executors[i] = executorFactory.apply(i));
    if (config.getResourceId() != null) {
      toucher.withRegisterProperty(OptimizerProperties.RESOURCE_ID, config.getResourceId());
    }
  }

  public void startOptimizing() {
    LOG.info("Starting optimizer with configuration:{}", config);
    Thread[] threads = new Thread[executors.length];
    for (int i = 0; i < executors.length; i++) {
      threads[i] =
          new Thread(
              executors[i]::start,
              String.format("Optimizer-executor-%d", executors[i].getThreadId()));
    }
    // Publish the fully-built array before starting any thread: a concurrent shutdown hook
    // reads executorThreads, and volatile orders only the array reference, not element
    // writes made after publication.
    executorThreads = threads;
    for (Thread thread : threads) {
      thread.start();
    }
    toucher.withTokenChangeListener(new SetTokenToExecutors()).start();
  }

  public void stopOptimizing() {
    LOG.info("Stopping optimizer, waiting for in-progress tasks to complete...");
    // Stop executors first so they don't poll new tasks, but keep the toucher alive
    // so it keeps sending heartbeats. Otherwise AMS hits its heartbeat-timeout during
    // long-running in-flight tasks, unregisters this optimizer, and the subsequent
    // best-effort completeTask fails with "Optimizer has not been authenticated".
    // Drain mode stops the toucher from re-registering if AMS has already unregistered
    // this optimizer (scale-down releases the resource before the pod gets SIGTERM).
    toucher.enterDrainMode();
    Arrays.stream(executors).forEach(OptimizerExecutor::stop);

    Thread[] threads = executorThreads;
    if (threads == null) {
      toucher.stop();
      LOG.info("Optimizer stopped (no executor threads to wait for)");
      return;
    }

    long shutdownTimeoutMs = config.getShutdownTimeoutMs();
    boolean selfInterrupted = !joinAll(threads, shutdownTimeoutMs);

    // Force-interrupt pass: interrupt ALL survivors first, then wait for them against one
    // shared deadline. Interleaving interrupt with per-thread joins would both serialize
    // the waits (N extra seconds past the deadline) and, when this thread was itself
    // interrupted above, skip interrupting the remaining threads because join() throws
    // instantly while the interrupt flag is set.
    boolean anyAlive = false;
    for (Thread t : threads) {
      if (t.isAlive()) {
        anyAlive = true;
        LOG.warn(
            "Executor thread {} did not terminate within {}ms timeout, force-interrupting",
            t.getName(),
            shutdownTimeoutMs);
        t.interrupt();
      }
    }
    if (anyAlive) {
      selfInterrupted |= !joinAll(threads, 1_000);
    }
    toucher.stop();
    if (selfInterrupted) {
      Thread.currentThread().interrupt();
    }
    LOG.info("Optimizer stopped");
  }

  /**
   * Joins all threads against one shared deadline. Returns false if the calling thread was
   * interrupted while waiting; the interrupt flag is left cleared so subsequent joins work.
   */
  private static boolean joinAll(Thread[] threads, long timeoutMs) {
    long deadline = System.currentTimeMillis() + timeoutMs;
    for (Thread t : threads) {
      long remaining = deadline - System.currentTimeMillis();
      if (remaining <= 0) {
        return true;
      }
      try {
        t.join(remaining);
      } catch (InterruptedException e) {
        LOG.warn("Interrupted while waiting for executor thread {} to finish", t.getName());
        return false;
      }
    }
    return true;
  }

  public OptimizerToucher getToucher() {
    return toucher;
  }

  public OptimizerExecutor[] getExecutors() {
    return executors;
  }

  class SetTokenToExecutors implements OptimizerToucher.TokenChangeListener {

    @Override
    public void tokenChange(String newToken) {
      Arrays.stream(executors).forEach(optimizerExecutor -> optimizerExecutor.setToken(newToken));
    }
  }
}
