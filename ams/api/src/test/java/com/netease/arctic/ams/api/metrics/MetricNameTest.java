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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;

public class MetricNameTest {

  static Stream<Arguments> testFromExplicitName() {
    return Stream.of(
        Arguments.of("metricNameWithoutTags", "metricNameWithoutTags", "", ""),
        Arguments.of("metricName<tag1=value1,tag2=value2>", "metricName", "tag1", "value1"),
        Arguments.of("anotherMetric<tag3=value3>", "anotherMetric", "tag3", "value3"));
  }

  @ParameterizedTest
  @MethodSource()
  public void testFromExplicitName(
      String explicitMetricName, String expectedName, String expectedTag, String expectedValue) {
    MetricName metricName = MetricName.fromExplicitName(explicitMetricName);
    assertEquals(expectedName, metricName.getName());
    if (StringUtils.isNotBlank(expectedTag)) {
      assertTrue(metricName.getTags().contains(expectedTag));
      assertEquals(expectedValue, metricName.getValues().get(expectedTag));
    } else {
      assertTrue(metricName.getTags().isEmpty());
      assertTrue(metricName.getValues().isEmpty());
    }
  }

  @ParameterizedTest
  @MethodSource("provideMetricNamesForEquality")
  void testEquals(MetricName a, Object b, boolean expectedEquality) {
    if (expectedEquality) {
      assertEquals(a, b, "MetricNames should be equal");
      assertEquals(a.hashCode(), b.hashCode(), "MetricNames hash code should be equal");
    } else {
      assertNotEquals(a, b, "MetricNames should not be equal");
      assertNotEquals(
          Objects.hash(a), Objects.hash(b), "MetricNames hash code should not be equal");
    }
  }

  private static Stream<Arguments> provideMetricNamesForEquality() {
    return Stream.of(
        // Equal MetricNames
        Arguments.of(
            new MetricName(
                "metric", Arrays.asList("tag1", "tag2"), Arrays.asList("value1", "value2")),
            new MetricName(
                "metric", Arrays.asList("tag2", "tag1"), Arrays.asList("value2", "value1")),
            true),
        // Equal MetricNames, tag order different
        Arguments.of(
            new MetricName(
                "metric", Arrays.asList("tag2", "tag1"), Arrays.asList("value2", "value1")),
            new MetricName(
                "metric", Arrays.asList("tag2", "tag1"), Arrays.asList("value2", "value1")),
            true),
        // MetricNames with different names should not be equal
        Arguments.of(
            new MetricName(
                "metric1", Collections.singletonList("tag1"), Collections.singletonList("value1")),
            new MetricName(
                "metric2", Collections.singletonList("tag1"), Collections.singletonList("value1")),
            false),
        // MetricNames with different values should not be equal
        Arguments.of(
            new MetricName(
                "metric", Collections.singletonList("tag1"), Collections.singletonList("value1")),
            new MetricName(
                "metric", Collections.singletonList("tag1"), Collections.singletonList("value2")),
            false),
        // MetricNames with different tags/values should not be equal
        Arguments.of(
            new MetricName(
                "metric", Collections.singletonList("tag1"), Collections.singletonList("value1")),
            new MetricName(
                "metric", Collections.singletonList("tag2"), Collections.singletonList("value2")),
            false),
        // MetricName compared to null should not be equal
        Arguments.of(
            new MetricName(
                "metric", Collections.singletonList("tag1"), Collections.singletonList("value1")),
            null,
            false),
        // MetricName compared to a different class object should not be equal
        Arguments.of(
            new MetricName(
                "metric", Collections.singletonList("tag1"), Collections.singletonList("value1")),
            "SomeString",
            false));
  }
}
