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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manage metrics and metrics reporter
 * Register reporter by adding the following configuration in the system configuration:
 * reporters:
 *   - name: report1
 *     impl: {reporter class}
 *     properties:
 *       host: 127.0.0.1
 *       port: 8080
 */
public class MetricsManager {
  private final Map<String, MetricsReporter> amoroReporters = new ConcurrentHashMap<>();
  private final Map<String, org.apache.iceberg.metrics.MetricsReporter> icebergReporters = new ConcurrentHashMap<>();

  public MetricsManager() {
  }

  public MetricsManager(List<ReporterMeta> metas) {
    metas.forEach(this::register);
  }

  public void register(ReporterMeta meta) {
    try {
      Class<?> clazz = Class.forName(meta.getImpl());
      if (MetricsReporter.class.isAssignableFrom(clazz)) {
        MetricsReporter reporter = (MetricsReporter) clazz.newInstance();
        reporter.open(meta.getProperties());
        register(meta.getName(), reporter);
      } else if (org.apache.iceberg.metrics.MetricsReporter.class.isAssignableFrom(clazz)) {
        org.apache.iceberg.metrics.MetricsReporter reporter =
            (org.apache.iceberg.metrics.MetricsReporter) clazz.newInstance();
        reporter.initialize(meta.getProperties());
        register(meta.getName(), reporter);
      } else {
        throw new UnsupportedOperationException("not support reporter implements " + meta.getImpl());
      }
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      throw new RuntimeException("can not find reporter " + meta.getImpl(), e);
    }
  }

  public void register(String name, MetricsReporter reporter) {
    this.amoroReporters.put(name, reporter);
  }

  public void register(String name, org.apache.iceberg.metrics.MetricsReporter reporter) {
    this.icebergReporters.put(name, reporter);
  }

  public void unregister(String name) {
    if (this.amoroReporters.containsKey(name)) {
      this.amoroReporters.get(name).close();
      this.amoroReporters.remove(name);
    } else {
      this.icebergReporters.remove(name);
    }
  }

  public void report(MetricsContent metrics) {
    for (MetricsReporter reporter : amoroReporters.values()) {
      reporter.report(metrics);
    }
  }

  public void report(MetricsReport metrics) {
    for (org.apache.iceberg.metrics.MetricsReporter reporter : icebergReporters.values()) {
      reporter.report(metrics);
    }
  }

  public void shutdown() {
    this.amoroReporters.keySet().forEach(this::unregister);
    this.icebergReporters.keySet().forEach(this::unregister);
  }
}
