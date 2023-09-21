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

import com.netease.arctic.ams.api.metrics.MetricsDomain;
import com.netease.arctic.ams.api.metrics.MetricsEmitter;
import com.netease.arctic.ams.api.metrics.PayloadMetrics;
import com.netease.arctic.server.metrics.reporters.ReporterFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manage metrics and metrics reporter
 * Register reporter by adding the following configuration in the system configuration:
 * reporters:
 *   - name: report1
 *     domain: AMORO
 *     impl: {reporter class}
 *     properties:
 *       host: 127.0.0.1
 *       port: 8080
 */
public class MetricsManager {

  private final ReporterFactory factory = new ReporterFactory();
  @SuppressWarnings("rawtypes")
  private final Map<MetricsDomain, Map<String, MetricsEmitter>> reporters = new ConcurrentHashMap<>();

  public MetricsManager() {
  }

  public MetricsManager(List<ReporterMeta> metas) {
    metas.forEach(this::register);
  }

  public void register(ReporterMeta meta) {
    MetricsDomain domain = MetricsDomain.valueOf(meta.getDomain());
    register(domain, meta.getName(), this.factory.create(meta));
  }

  @SuppressWarnings("rawtypes")
  public void register(MetricsDomain domain, String name, MetricsEmitter reporter) {
    reporters.computeIfAbsent(domain, k -> new HashMap<>()).put(name, reporter);
  }

  public void unregister(String name) {
    reporters.values().forEach(namedReporters -> {
      if (namedReporters.containsKey(name)) {
        namedReporters.remove(name).close();
      }
    });
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public void report(PayloadMetrics metrics) {
    Map<String, MetricsEmitter> domainReports = this.reporters.get(metrics.domain());
    if (domainReports == null) {
      return;
    }
    for (MetricsEmitter reporter : domainReports.values()) {
      reporter.report(metrics);
    }
  }

  public void shutdown() {
    this.reporters.values().forEach(namedReporters -> namedReporters.keySet().forEach(this::unregister));
  }
}
