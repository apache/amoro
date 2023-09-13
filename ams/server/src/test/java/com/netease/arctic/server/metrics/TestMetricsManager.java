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

package com.netease.arctic.server.metrics;

import com.netease.arctic.ams.api.metrics.MetricsContent;
import com.netease.arctic.ams.api.metrics.MetricsReporter;
import org.apache.iceberg.metrics.MetricsReport;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestMetricsManager {

  private MetricsManager manager;
  private TestAmoroReporter amoroReporter;
  private TestIcebergReporter icebergReporter;

  @Before
  public void before() {
    this.manager = new MetricsManager();
    this.amoroReporter = new TestAmoroReporter();
    this.icebergReporter = new TestIcebergReporter();
    manager.register("testAmoro", amoroReporter);
    manager.register("testIceberg", icebergReporter);
  }

  @After
  public void after() {
    this.manager.shutdown();
  }

  @Test
  public void report() {
    MetricsContent amoroMetric = () -> "test-metrics";
    manager.report(amoroMetric);
    Assert.assertEquals(1, amoroReporter.getTestMetrics().size());
    Assert.assertEquals(amoroMetric, amoroReporter.getTestMetrics().get(0));

    MetricsReport icebergMetric = new MetricsReport() {};
    manager.report(icebergMetric);
    Assert.assertEquals(1, icebergReporter.getTestMetrics().size());
    Assert.assertEquals(icebergMetric, icebergReporter.getTestMetrics().get(0));
  }

  private static class TestAmoroReporter implements MetricsReporter {

    private final List<MetricsContent> testMetrics = new ArrayList<>();

    @Override
    public void report(MetricsContent metricsContent) {
      this.testMetrics.add(metricsContent);
    }

    public List<MetricsContent> getTestMetrics() {
      return this.testMetrics;
    }
  }

  private static class TestIcebergReporter implements org.apache.iceberg.metrics.MetricsReporter {

    private final List<MetricsReport> testMetrics = new ArrayList<>();

    @Override
    public void report(MetricsReport report) {
      this.testMetrics.add(report);
    }

    public List<MetricsReport> getTestMetrics() {
      return this.testMetrics;
    }
  }
}
