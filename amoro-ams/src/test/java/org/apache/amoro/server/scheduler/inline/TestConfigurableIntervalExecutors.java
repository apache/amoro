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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Duration;

/**
 * Tests for configurable interval in DanglingDeleteFilesCleaningExecutor and
 * SnapshotsExpiringExecutor.
 */
public class TestConfigurableIntervalExecutors {

  @Test
  public void testDanglingDeleteFilesDefaultInterval() {
    Duration interval = Duration.ofDays(1);
    DanglingDeleteFilesCleaningExecutor executor =
        new DanglingDeleteFilesCleaningExecutor(null, 1, interval);

    TableRuntime tableRuntime = Mockito.mock(TableRuntime.class);
    Assert.assertEquals(Duration.ofDays(1).toMillis(), executor.getNextExecutingTime(tableRuntime));
  }

  @Test
  public void testDanglingDeleteFilesCustomInterval() {
    Duration interval = Duration.ofHours(12);
    DanglingDeleteFilesCleaningExecutor executor =
        new DanglingDeleteFilesCleaningExecutor(null, 1, interval);

    TableRuntime tableRuntime = Mockito.mock(TableRuntime.class);
    Assert.assertEquals(
        Duration.ofHours(12).toMillis(), executor.getNextExecutingTime(tableRuntime));
  }

  @Test
  public void testDanglingDeleteFilesShouldExecuteAfterInterval() {
    Duration interval = Duration.ofHours(6);
    DanglingDeleteFilesCleaningExecutor executor =
        new DanglingDeleteFilesCleaningExecutor(null, 1, interval);

    long now = System.currentTimeMillis();
    // 7 hours ago - should execute
    Assert.assertTrue(executor.shouldExecute(now - Duration.ofHours(7).toMillis()));
    // 5 hours ago - should not execute
    Assert.assertFalse(executor.shouldExecute(now - Duration.ofHours(5).toMillis()));
  }

  @Test
  public void testSnapshotsExpiringDefaultInterval() {
    Duration interval = Duration.ofHours(1);
    SnapshotsExpiringExecutor executor = new SnapshotsExpiringExecutor(null, 1, interval);

    TableRuntime tableRuntime = Mockito.mock(TableRuntime.class);
    Assert.assertEquals(
        Duration.ofHours(1).toMillis(), executor.getNextExecutingTime(tableRuntime));
  }

  @Test
  public void testSnapshotsExpiringCustomInterval() {
    Duration interval = Duration.ofMinutes(30);
    SnapshotsExpiringExecutor executor = new SnapshotsExpiringExecutor(null, 1, interval);

    TableRuntime tableRuntime = Mockito.mock(TableRuntime.class);
    Assert.assertEquals(
        Duration.ofMinutes(30).toMillis(), executor.getNextExecutingTime(tableRuntime));
  }

  @Test
  public void testSnapshotsExpiringShouldExecuteAfterInterval() {
    Duration interval = Duration.ofHours(2);
    SnapshotsExpiringExecutor executor = new SnapshotsExpiringExecutor(null, 1, interval);

    long now = System.currentTimeMillis();
    // 3 hours ago - should execute
    Assert.assertTrue(executor.shouldExecute(now - Duration.ofHours(3).toMillis()));
    // 1 hour ago - should not execute
    Assert.assertFalse(executor.shouldExecute(now - Duration.ofHours(1).toMillis()));
  }
}
