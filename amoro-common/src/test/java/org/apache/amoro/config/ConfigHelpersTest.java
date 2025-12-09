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

package org.apache.amoro.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

public class ConfigHelpersTest {
  @Test
  public void testTimeUtils() {
    assert ConfigHelpers.TimeUtils.formatWithHighestUnit(Duration.ofNanos(1)).equals("1 ns");
    assert ConfigHelpers.TimeUtils.formatWithHighestUnit(Duration.ofNanos(1000)).equals("1 µs");
    assert ConfigHelpers.TimeUtils.formatWithHighestUnit(Duration.ofMillis(1)).equals("1 ms");
    assert ConfigHelpers.TimeUtils.formatWithHighestUnit(Duration.ofSeconds(1)).equals("1 s");
    assert ConfigHelpers.TimeUtils.formatWithHighestUnit(Duration.ofMinutes(1)).equals("1 min");
    assert ConfigHelpers.TimeUtils.formatWithHighestUnit(Duration.ofHours(1)).equals("1 h");
    assert ConfigHelpers.TimeUtils.formatWithHighestUnit(Duration.ofDays(1)).equals("1 d");
    assert ConfigHelpers.TimeUtils.formatWithHighestUnit(Duration.ofHours(25)).equals("25 h");
  }
}
