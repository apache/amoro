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

package org.apache.amoro.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class MetricDefineTest {
  private MetricDefine source =
      new MetricDefine(
          "test-define", Arrays.asList("tag1", "tag2"), MetricType.Counter, "description");

  @ParameterizedTest
  @MethodSource("provideMetricNamesForEquality")
  public void testEquals(MetricDefine target, boolean expectedEquality) {
    // MetricDefine is equally if(name, tag set, type) are qually
    if (expectedEquality) {
      assertEquals(source, target, "MetricNames should be equal");
      assertEquals(source.hashCode(), target.hashCode(), "MetricNames hash code should be equal");
    } else {
      assertNotEquals(source, target, "MetricNames should not be equal");
      assertNotEquals(
          Objects.hash(source), Objects.hash(target), "MetricNames hash code should not be equal");
    }
  }

  public static Stream<Arguments> provideMetricNamesForEquality() {

    return Stream.of(
        // same <name, tags, type> should be true
        Arguments.of(
            new MetricDefine(
                "test-define", Arrays.asList("tag1", "tag2"), MetricType.Counter, "description"),
            true),
        // different name should be false
        Arguments.of(
            new MetricDefine(
                "different-name", Arrays.asList("tag1", "tag2"), MetricType.Counter, "description"),
            false),
        // different order of tags should be true
        Arguments.of(
            new MetricDefine(
                "test-define", Arrays.asList("tag2", "tag1"), MetricType.Counter, "description"),
            true),
        // different tags should be false
        Arguments.of(
            new MetricDefine(
                "test-define", Arrays.asList("tag3", "tag4"), MetricType.Counter, "description"),
            false),
        // different MetricType should be false
        Arguments.of(
            new MetricDefine(
                "test-define", Arrays.asList("tag1", "tag2"), MetricType.Gauge, "description"),
            false),
        // even description is different，should be true
        Arguments.of(
            new MetricDefine(
                "test-define",
                Arrays.asList("tag1", "tag2"),
                MetricType.Counter,
                "different description"),
            true));
  }
}
