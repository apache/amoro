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

package org.apache.amoro.formats.paimon.optimizing.primary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

class TestPaimonPrimaryKeyOptions {

  @Test
  void defaultsKeepPrimaryKeyOptimizingDisabled() {
    PaimonPrimaryKeyOptions options = PaimonPrimaryKeyOptions.from(new HashMap<>());

    assertFalse(options.enabled());
    assertEquals(16, options.maxBucketsPerTask());
    assertFalse(options.partitionIdleTime().isPresent());
    assertFalse(options.majorFileCountThreshold().isPresent());
  }

  @Test
  void parsesPrimaryKeySpecificOptions() {
    Map<String, String> props = new HashMap<>();
    props.put(PaimonPrimaryKeyOptions.ENABLED, "true");
    props.put(PaimonPrimaryKeyOptions.MAX_BUCKETS_PER_TASK, "8");
    props.put(PaimonPrimaryKeyOptions.PARTITION_IDLE_TIME, "PT30M");
    props.put(PaimonPrimaryKeyOptions.MAJOR_FILE_COUNT_THRESHOLD, "12");

    PaimonPrimaryKeyOptions options = PaimonPrimaryKeyOptions.from(props);

    assertTrue(options.enabled());
    assertEquals(8, options.maxBucketsPerTask());
    assertEquals(
        Duration.ofMinutes(30), options.partitionIdleTime().orElseThrow(AssertionError::new));
    assertEquals(12L, options.majorFileCountThreshold().orElseThrow(AssertionError::new));
  }

  @Test
  void parsesPaimonStylePartitionIdleTime() {
    Map<String, String> props = new HashMap<>();
    props.put(PaimonPrimaryKeyOptions.PARTITION_IDLE_TIME, "10s");

    PaimonPrimaryKeyOptions options = PaimonPrimaryKeyOptions.from(props);

    assertEquals(
        Duration.ofSeconds(10), options.partitionIdleTime().orElseThrow(AssertionError::new));
  }

  @Test
  void rejectsInvalidMaxBucketsPerTask() {
    Map<String, String> props = new HashMap<>();
    props.put(PaimonPrimaryKeyOptions.MAX_BUCKETS_PER_TASK, "0");

    assertThrows(IllegalArgumentException.class, () -> PaimonPrimaryKeyOptions.from(props));
  }
}
