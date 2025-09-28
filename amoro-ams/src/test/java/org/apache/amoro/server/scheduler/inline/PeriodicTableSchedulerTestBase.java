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

package org.apache.amoro.server.scheduler.inline;

import org.apache.amoro.TableRuntime;
import org.apache.amoro.server.scheduler.PeriodicTableScheduler;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.server.table.cleanup.CleanupOperation;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Test table executor implementation for testing PeriodicTableScheduler functionality. This class
 * allows configuration of cleanup operations and enabled state for testing purposes. It provides
 * public methods to access protected/private functionality of PeriodicTableScheduler for
 * comprehensive test coverage.
 */
class PeriodicTableSchedulerTestBase extends PeriodicTableScheduler {
  private final CleanupOperation cleanupOperation;
  private final boolean enabled;
  private final TableService tableService;
  private static final long SNAPSHOTS_EXPIRING_INTERVAL = 60 * 60 * 1000L; // 1 hour
  private static final long ORPHAN_FILES_CLEANING_INTERVAL = 24 * 60 * 60 * 1000L; // 1 day
  private static final long DANGLING_DELETE_FILES_CLEANING_INTERVAL = 24 * 60 * 60 * 1000L;
  private static final long DATA_EXPIRING_INTERVAL = 60 * 60 * 1000L; // 1 hour

  public PeriodicTableSchedulerTestBase(
      TableService tableService, CleanupOperation cleanupOperation, boolean enabled) {
    super(tableService, 1);
    this.tableService = tableService;
    this.cleanupOperation = cleanupOperation;
    this.enabled = enabled;
  }

  @Override
  protected CleanupOperation getCleanupOperation() {
    return cleanupOperation;
  }

  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return 1000;
  }

  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return enabled;
  }

  @Override
  protected void execute(TableRuntime tableRuntime) {
    // Do nothing in test
  }

  @Override
  protected long getExecutorDelay() {
    return 0;
  }

  @Override
  protected boolean shouldExecute(Long lastCleanupEndTime) {
    long currentTime = System.currentTimeMillis();
    switch (cleanupOperation) {
      case SNAPSHOTS_EXPIRING:
        return currentTime - lastCleanupEndTime >= SNAPSHOTS_EXPIRING_INTERVAL;
      case ORPHAN_FILES_CLEANING:
        return currentTime - lastCleanupEndTime >= ORPHAN_FILES_CLEANING_INTERVAL;
      case DANGLING_DELETE_FILES_CLEANING:
        return currentTime - lastCleanupEndTime >= DANGLING_DELETE_FILES_CLEANING_INTERVAL;
      case DATA_EXPIRING:
        return currentTime - lastCleanupEndTime >= DATA_EXPIRING_INTERVAL;
      default:
        return true;
    }
  }

  // Public methods for testing private methods
  public void initHandlerForTest(List<TableRuntime> tableRuntimeList) {
    initHandler(tableRuntimeList);
  }

  public boolean shouldExecuteTaskForTest(
      TableRuntime tableRuntime, CleanupOperation cleanupOperation) {
    return shouldExecuteTask(tableRuntime, cleanupOperation);
  }

  public void executeTaskForTest(TableRuntime tableRuntime) {
    // Access private method via reflection
    invokePrivateMethod("executeTask", TableRuntime.class, tableRuntime);
  }

  public void addToCacheForTest(long tableId, long cleanupTime) {
    try {
      java.lang.reflect.Field field =
          PeriodicTableScheduler.class.getDeclaredField("tableIdWithLastCleanTimeCache");
      field.setAccessible(true);
      Map<Long, Long> cache = (Map<Long, Long>) field.get(this);
      cache.put(tableId, cleanupTime);
    } catch (Exception e) {
      throw new RuntimeException("Failed to add to cache for test", e);
    }
  }

  // Override isExecutable to avoid NPE when tableService is null
  @Override
  protected boolean isExecutable(TableRuntime tableRuntime) {
    if (tableService == null) {
      // When tableService is null (in tests), just check if the table is enabled
      return enabled(tableRuntime);
    }
    return super.isExecutable(tableRuntime);
  }

  /**
   * Helper method to invoke private methods in PeriodicTableScheduler using reflection
   *
   * @param methodName The name of the method to invoke
   * @param parameterType The parameter type of the method
   * @param parameterValue The parameter value to pass to the method
   */
  private void invokePrivateMethod(
      String methodName, Class<?> parameterType, Object parameterValue) {
    try {
      Method method = PeriodicTableScheduler.class.getDeclaredMethod(methodName, parameterType);
      method.setAccessible(true);
      method.invoke(this, parameterValue);
    } catch (Exception e) {
      throw new RuntimeException("Failed to invoke private method " + methodName + " for test", e);
    }
  }

  public Map<Long, Long> getCacheForTest() {
    try {
      java.lang.reflect.Field field =
          PeriodicTableScheduler.class.getDeclaredField("tableIdWithLastCleanTimeCache");
      field.setAccessible(true);
      return (Map<Long, Long>) field.get(this);
    } catch (Exception e) {
      throw new RuntimeException("Failed to get cache for test", e);
    }
  }
}
