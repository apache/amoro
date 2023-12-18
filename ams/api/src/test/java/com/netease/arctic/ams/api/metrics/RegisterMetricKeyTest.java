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

package com.netease.arctic.ams.api.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class RegisterMetricKeyTest {

  @ParameterizedTest
  @MethodSource("provideRegisteredMetricKeysForEquality")
  void testEqualsAndHashCode(
      RegisteredMetricKey key1, RegisteredMetricKey key2, boolean expectedEquality) {
    if (expectedEquality) {
      assertEquals(key1, key2, "RegisteredMetricKeys should be equal");
      assertEquals(
          key1.hashCode(), key2.hashCode(), "RegisteredMetricKeys' hash codes should be equal");
    } else {
      assertNotEquals(key1, key2, "RegisteredMetricKeys should not be equal");
    }
  }

  static Stream<Arguments> provideRegisteredMetricKeysForEquality() {
    MetricDefine define1 =
        new MetricDefine("metric1", Arrays.asList("host", "region"), MetricType.Counter, "");
    MetricDefine define2 =
        new MetricDefine("metric1", Arrays.asList("region", "host"), MetricType.Counter, "");
    MetricDefine define3 =
        new MetricDefine("metric2", Arrays.asList("host", "region"), MetricType.Counter, "");

    Map<String, String> tags1 = new HashMap<>();
    tags1.put("host", "server1");
    tags1.put("region", "us-west");

    Map<String, String> tags2 = new HashMap<>();
    tags2.put("region", "us-west");
    tags2.put("host", "server1");

    Map<String, String> tags3 = new HashMap<>();
    tags3.put("host", "server2");
    tags3.put("region", "us-east");

    return Stream.of(
        // Same MetricDefine and tag values in different order
        Arguments.of(
            new RegisteredMetricKey(define1, tags1), new RegisteredMetricKey(define2, tags2), true),
        // Different MetricDefine
        Arguments.of(
            new RegisteredMetricKey(define1, tags1),
            new RegisteredMetricKey(define3, tags1),
            false),
        // Same MetricDefine, different tag values
        Arguments.of(
            new RegisteredMetricKey(define1, tags1),
            new RegisteredMetricKey(define2, tags3),
            false));
  }
}
