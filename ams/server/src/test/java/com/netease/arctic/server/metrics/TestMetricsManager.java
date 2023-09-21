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
import com.netease.arctic.ams.api.metrics.MetricsDomain;
import com.netease.arctic.ams.api.metrics.MetricsEmitter;
import com.netease.arctic.ams.api.metrics.PayloadMetrics;
import com.netease.arctic.server.metrics.reporters.IcebergReporterWrapper;
import org.apache.iceberg.metrics.MetricsReport;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestMetricsManager {

  @Test
  public void report() {
    MetricsManager manager = new MetricsManager();
    TestAmoroMetricsEmitter amoroReporter = new TestAmoroMetricsEmitter();
    TestIcebergReporter icebergReporter = new TestIcebergReporter();
    manager.register(MetricsDomain.AMORO, "testAmoro", amoroReporter);
    manager.register(MetricsDomain.ICEBERG, "testIceberg", new IcebergReporterWrapper(icebergReporter));
    MetricsContent metricsContent = () -> "test";
    PayloadMetrics<MetricsContent> amoroMetric = AmoroPayloadMetrics.wrap(metricsContent);
    manager.report(amoroMetric);
    Assert.assertEquals(1, amoroReporter.getTestMetrics().size());
    Assert.assertEquals(metricsContent, amoroReporter.getTestMetrics().get(0));

    MetricsReport metricsReport = new MetricsReport() {

    };
    PayloadMetrics<MetricsReport> icebergMetric = IcebergPayloadMetrics.wrap(metricsReport);
    manager.report(icebergMetric);
    Assert.assertEquals(1, icebergReporter.getTestMetrics().size());
    Assert.assertEquals(metricsReport, icebergReporter.getTestMetrics().get(0));
  }

  private static class TestAmoroMetricsEmitter implements MetricsEmitter<MetricsContent> {

    private final List<MetricsContent> testMetrics = new ArrayList<>();

    @Override
    public void open(Map<String, String> properties) {

    }

    @Override
    public void report(PayloadMetrics<MetricsContent> payloadMetrics) {
      this.testMetrics.add(payloadMetrics.metrics());
    }

    @Override
    public void close() {

    }

    public List<MetricsContent> getTestMetrics() {
      return this.testMetrics;
    }
  }

  private static class TestIcebergReporter implements org.apache.iceberg.metrics.MetricsReporter {

    private final List<MetricsReport> testMetrics = new ArrayList<>();

    public List<MetricsReport> getTestMetrics() {
      return this.testMetrics;
    }

    @Override
    public void report(MetricsReport report) {
      this.testMetrics.add(report);
    }
  }
}
