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
import com.netease.arctic.ams.api.metrics.MetricsPayload;
import com.netease.arctic.server.metrics.emitters.IcebergEmitterWrapper;
import org.apache.iceberg.metrics.MetricsReport;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestMetricsManager {

  @Test
  public void report() {
    EmitterConfig emitterConfig =
        EmitterConfig.of("test", "AMORO", TestAmoroMetricsEmitter.class.getName(), new HashMap<>());
    EmitterConfig emitterConfig2 =
        EmitterConfig.of("test2", "ICEBERG", TestIcebergReporter.class.getName(), new HashMap<>());
    List<EmitterConfig> emitterConfigs = new ArrayList<>();
    emitterConfigs.add(emitterConfig);
    emitterConfigs.add(emitterConfig2);
    Map<String, String> domainToClass = new HashMap<>();
    domainToClass.put("AMORO", "com.netease.arctic.ams.api.metrics.MetricsContent");
    domainToClass.put("ICEBERG", "org.apache.iceberg.metrics.MetricsReport");
    MetricsManager manager = new MetricsManager(emitterConfigs, domainToClass);
    TestAmoroMetricsEmitter amoroReporter = new TestAmoroMetricsEmitter();
    TestIcebergReporter icebergReporter = new TestIcebergReporter();
    manager.register(MetricsDomain.AMORO, "testAmoro", amoroReporter);
    manager.register(MetricsDomain.ICEBERG, "testIceberg", new IcebergEmitterWrapper(icebergReporter));
    MetricsContent metricsContent = () -> "test";
    manager.emit(metricsContent);
    Assert.assertEquals(1, amoroReporter.getTestMetrics().size());
    Assert.assertEquals(metricsContent, amoroReporter.getTestMetrics().get(0));

    MetricsReport metricsReport = new MetricsReport() {

    };
    manager.emit(metricsReport);
    Assert.assertEquals(1, icebergReporter.getTestMetrics().size());
    Assert.assertEquals(metricsReport, icebergReporter.getTestMetrics().get(0));
  }

  public static class TestAmoroMetricsEmitter implements MetricsEmitter<MetricsContent> {

    private final List<MetricsContent> testMetrics = new ArrayList<>();

    @Override
    public void open(Map<String, String> properties) {

    }

    @Override
    public void emit(MetricsPayload<MetricsContent> metricsPayload) {
      this.testMetrics.add(metricsPayload.payload());
    }

    @Override
    public void close() {

    }

    public List<MetricsContent> getTestMetrics() {
      return this.testMetrics;
    }
  }

  public static class TestIcebergReporter implements org.apache.iceberg.metrics.MetricsReporter {

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
