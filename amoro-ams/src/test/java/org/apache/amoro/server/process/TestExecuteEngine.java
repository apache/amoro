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
import org.apache.amoro.server.process.executor.ExecuteOption;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.parquet.Strings;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestExecuteEngine implements ExecuteEngine {

  public static final String CHARACTERS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  private final ThreadPoolExecutor executionPool =
      new ThreadPoolExecutor(10, 100, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

  private final Map<String, Future<?>> activeInstances = new ConcurrentHashMap<>();

  private final Map<String, Future<?>> cancelingInstances = new ConcurrentHashMap<>();

  @Override
  public EngineType engineType() {
    return EngineType.DEFAULT;
  }

  @Override
  public ProcessStatus getStatus(String processIdentifier) {
    if (Strings.isNullOrEmpty(processIdentifier)) {
      return ProcessStatus.UNKNOWN;
    }

    Map<String, Future<?>> instances =
        cancelingInstances.containsKey(processIdentifier) ? cancelingInstances : activeInstances;

    Future<?> future = instances.get(processIdentifier);
    if (future == null) {
      return ProcessStatus.UNKNOWN;
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

  @Override
  public String submitTableProcess(TableProcess tableProcess) {
    String identifier = generateUnique128String();
    Future<?> future = executionPool.submit(new TableProcessExecutor(tableProcess, this));
    activeInstances.put(identifier, future);
    return identifier;
  }

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

  @Override
  public void open(Map<String, String> properties) {}

  @Override
  public void close() {}

  @Override
  public String name() {
    return "default_execute_engine";
  }

  /**
   * Generate a unique 128-length string
   *
   * @return Unique 128-length string
   * @throws IllegalStateException If failed to generate unique string after max retries
   */
  private String generateUnique128String() {
    int maxRetryCount = 100;
    for (int retry = 0; retry < maxRetryCount; retry++) {
      // Generate a 128-length random string
      String randomStr = generate128LengthRandomString();

      // Check uniqueness: return directly if not exists in the collection (containsKey returns
      // false)
      if (!activeInstances.containsKey(randomStr)) {
        System.out.printf("Attempt %d: Successfully generated unique string%n", retry + 1);
        return randomStr;
      }

      // Print log and retry if duplicate (can remove log in production to avoid redundancy)
      System.out.printf("Attempt %d: String already exists, regenerating...%n", retry + 1);
    }

    // Throw exception if failed to generate after max retries (theoretically, 62^128 combinations
    // result in extremely low duplication probability)
    throw new IllegalStateException(
        "Failed to generate unique 128-length string after " + 100 + " max retries");
  }

  /**
   * Generate a single 128-length random string (based on secure random number)
   *
   * @return 128-length random string (letters + numbers)
   */
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

  @VisibleForTesting
  public Map<String, Future<?>> getActiveInstances() {
    return activeInstances;
  }

  @VisibleForTesting
  public Map<String, Future<?>> getCancelingInstances() {
    return cancelingInstances;
  }

  private class TableProcessExecutor extends PersistentBase implements Runnable {
    public ExecuteEngine executeEngine;
    public ExecuteOption
        executeOption; // Note: This field is declared but not used in the current code
    protected TableProcess tableProcess;

    public TableProcessExecutor(TableProcess tableProcess, ExecuteEngine executeEngine) {
      this.tableProcess = tableProcess;
      this.executeEngine = executeEngine;
    }

    @Override
    public void run() {
      // 1. Generate a random integer between 1 and 100 (recommend ThreadLocalRandom for
      // thread-safety and high performance)
      int randomNum = ThreadLocalRandom.current().nextInt(1, 101);
      System.out.println("Generated random number: " + randomNum);
      System.out.println("Start loop: i from 0 to " + randomNum + ", sleep 5 seconds each time");
      System.out.println("----------------------------------------");

      // 2. Loop: i increments from 0 to randomNum (inclusive of 0 and randomNum)
      for (int i = 0; i <= randomNum; i++) { // Note: Use i <= randomNum to include the end value
        try {
          System.out.printf(
              "TableProcess: %s, ExecuteType: %s.",
              tableProcess.toString(), executeEngine.engineType());
          System.out.printf(
              "Current loop: i = %d / %d, start sleeping for 5 seconds...%n", i, randomNum);
          // Sleep for 5 seconds (1 second = 1000 milliseconds, 5 seconds = 5000 milliseconds)
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          // Handle interrupt exception (avoid program abnormality caused by interrupted sleep)
          System.out.println("Loop interrupted, exiting program!");
          // Reset interrupt flag (for upper-layer perception, optional)
          Thread.currentThread().interrupt();
          // Terminate loop
          break;
        }
      }

      // 3. Loop completion prompt
      System.out.println("----------------------------------------");
      System.out.println("Loop execution completed!");
    }
  }
}
