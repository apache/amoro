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

/** Tests for configurable interval in ProcessDataExpiringExecutor. */
public class TestConfigurableIntervalExecutors {

  @Test
  public void testProcessDataExpiringDefaultInterval() {
    Duration optimizingKeepTime = Duration.ofDays(30);
    Duration expireInterval = Duration.ofHours(1);
    Duration processKeepTime = Duration.ofDays(7);
    ProcessDataExpiringExecutor executor =
        new ProcessDataExpiringExecutor(null, optimizingKeepTime, expireInterval, processKeepTime);

    TableRuntime tableRuntime = Mockito.mock(TableRuntime.class);
    Assert.assertEquals(
        Duration.ofHours(1).toMillis(), executor.getNextExecutingTime(tableRuntime));
  }

  @Test
  public void testProcessDataExpiringCustomInterval() {
    Duration optimizingKeepTime = Duration.ofDays(15);
    Duration expireInterval = Duration.ofMinutes(30);
    Duration processKeepTime = Duration.ofDays(3);
    ProcessDataExpiringExecutor executor =
        new ProcessDataExpiringExecutor(null, optimizingKeepTime, expireInterval, processKeepTime);

    TableRuntime tableRuntime = Mockito.mock(TableRuntime.class);
    Assert.assertEquals(
        Duration.ofMinutes(30).toMillis(), executor.getNextExecutingTime(tableRuntime));
  }
}
