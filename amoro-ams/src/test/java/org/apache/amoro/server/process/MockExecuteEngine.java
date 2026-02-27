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

package org.apache.amoro.server.process;

import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.process.executor.EngineType;
import org.apache.amoro.server.process.executor.ExecuteEngine;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.parquet.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Mock execute engine used for tests with a background thread pool to simulate process execution.
 */
public class MockExecuteEngine implements ExecuteEngine {
  private static final Logger LOG = LoggerFactory.getLogger(MockExecuteEngine.class);

  public static final String CHARACTERS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  private final ThreadPoolExecutor executionPool =
      new ThreadPoolExecutor(10, 100, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

  private final Map<String, Future<?>> activeInstances = new ConcurrentHashMap<>();

  private final Map<String, Future<?>> cancelingInstances = new ConcurrentHashMap<>();

  /** Engine type of this mock engine. */
  @Override
  public EngineType engineType() {
    return EngineType.of("default");
  }

  /**
   * Get process status by identifier.
   *
   * @param processIdentifier identifier
   * @return status
   */
  @Override
  public ProcessStatus getStatus(String processIdentifier) {
    if (Strings.isNullOrEmpty(processIdentifier)) {
      return ProcessStatus.UNKNOWN;
    }

    Map<String, Future<?>> instances =
        cancelingInstances.containsKey(processIdentifier) ? cancelingInstances : activeInstances;

    Future<?> future = instances.get(processIdentifier);
    if (future == null) {
      return ProcessStatus.KILLED;
    }
    if (future.isCancelled()) {
      instances.remove(processIdentifier);
      return ProcessStatus.CANCELED;
    } else if (future.isDone()) {
      instances.remove(processIdentifier);
      try {
        future.get();
        return ProcessStatus.SUCCESS;
      } catch (Exception e) {
        return ProcessStatus.FAILED;
      }
    } else {
      if (cancelingInstances.containsKey(processIdentifier)) {
        return ProcessStatus.CANCELING;
      } else {
        return ProcessStatus.RUNNING;
      }
    }
  }

  /**
   * Submit a table process and return identifier.
   *
   * @param tableProcess process
   * @return identifier
   */
  @Override
  public String submitTableProcess(TableProcess tableProcess) {
    String identifier = generateUnique128String();
    Future<?> future = executionPool.submit(new TableProcessExecutor(tableProcess, this));
    activeInstances.put(identifier, future);
    return identifier;
  }

  /**
   * Try cancel a table process.
   *
   * @param tableProcess process
   * @param processIdentifier identifier
   * @return status after cancel attempt
   */
  @Override
  public ProcessStatus tryCancelTableProcess(TableProcess tableProcess, String processIdentifier) {
    Future<?> future = activeInstances.get(processIdentifier);
    if (future == null) {
      return ProcessStatus.CANCELED;
    }

    activeInstances.remove(processIdentifier);
    cancelingInstances.put(processIdentifier, future);

    if (future.isDone()) {
      try {
        future.get();
        return ProcessStatus.SUCCESS;
      } catch (Exception e) {
        return ProcessStatus.FAILED;
      }
    } else if (future.isCancelled()) {
      return ProcessStatus.CANCELED;
    } else {
      future.cancel(true);
      return ProcessStatus.CANCELING;
    }
  }

  /** Open plugin. */
  @Override
  public void open(Map<String, String> properties) {}

  /** Close plugin. */
  @Override
  public void close() {}

  /** Plugin name. */
  @Override
  public String name() {
    return "mock_execute_engine";
  }

  /** Generate a unique 128-length string identifier. */
  private String generateUnique128String() {
    int maxRetryCount = 100;
    for (int retry = 0; retry < maxRetryCount; retry++) {
      // Generate a 128-length random string
      String randomStr = generate128LengthRandomString();

      if (!activeInstances.containsKey(randomStr)) {
        LOG.info("Attempt {}: Successfully generated unique string.", retry + 1);
        return randomStr;
      }

      LOG.info("Attempt {}: String already exists, regenerating...", retry + 1);
    }

    throw new IllegalStateException(
        "Failed to generate unique 128-length string after " + 100 + " max retries");
  }

  /** Generate a random 128-length string. */
  private String generate128LengthRandomString() {
    StringBuilder sb = new StringBuilder(128);
    SecureRandom secureRandom = new SecureRandom();
    for (int i = 0; i < 128; i++) {
      // Randomly select a character from the character set (index range: 0 ~ 61)
      int randomIndex = secureRandom.nextInt(CHARACTERS.length());
      sb.append(CHARACTERS.charAt(randomIndex));
    }
    return sb.toString();
  }

  /** Active instances in engine (testing only). */
  @VisibleForTesting
  public Map<String, Future<?>> getActiveInstances() {
    return activeInstances;
  }

  /** Canceling instances in engine (testing only). */
  @VisibleForTesting
  public Map<String, Future<?>> getCancelingInstances() {
    return cancelingInstances;
  }

  /** Executor that simulates running tasks. */
  private class TableProcessExecutor extends PersistentBase implements Runnable {
    public ExecuteEngine executeEngine;
    protected TableProcess tableProcess;

    /**
     * Construct executor.
     *
     * @param tableProcess process
     * @param executeEngine engine
     */
    public TableProcessExecutor(TableProcess tableProcess, ExecuteEngine executeEngine) {
      this.tableProcess = tableProcess;
      this.executeEngine = executeEngine;
    }

    /** Simulate execution loop and log messages. */
    @Override
    public void run() {
      int randomNum = ThreadLocalRandom.current().nextInt(20, 101);
      LOG.info("Generated random number: " + randomNum);
      LOG.info("Start loop: i from 0 to " + randomNum + ", sleep 5 seconds each time");
      LOG.info("----------------------------------------");

      for (int i = 0; i <= randomNum; i++) { // Note: Use i <= randomNum to include the end value
        try {
          LOG.info(
              "TableProcess: {}, ExecuteType: {}.",
              tableProcess.toString(),
              executeEngine.engineType());
          LOG.info("Current loop: i = {} / {}, start sleeping for 5 seconds...", i, randomNum);
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          LOG.info("Loop interrupted, exiting program!");
          Thread.currentThread().interrupt();
          break;
        }
      }

      LOG.info("----------------------------------------");
      LOG.info("Loop execution completed!");
    }
  }
}
