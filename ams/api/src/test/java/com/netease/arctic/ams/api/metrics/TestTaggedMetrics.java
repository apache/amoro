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

import com.codahale.metrics.Counter;
import org.junit.Assert;
import org.junit.Test;

public class TestTaggedMetrics {

  @Test
  public void from() {
    MetricsContent metricsContent = new MetricsContent() {
      @Override
      public String name() {
        return "test";
      }

      @TaggedMetrics.Tag(name = "test-tag")
      public String testTag() {
        return "testTag";
      }

      @TaggedMetrics.Metric(name = "test-metric")
      public Counter testMetric() {
        Counter test = new Counter();
        test.inc(5);
        return test;
      }
    };
    TaggedMetrics taggedMetrics = TaggedMetrics.from(metricsContent);
    Assert.assertEquals(taggedMetrics.tags().get("test-tag"), "testTag");
    Assert.assertEquals(((Counter) taggedMetrics.metrics().get("test-metric")).getCount(), 5);
  }
}
