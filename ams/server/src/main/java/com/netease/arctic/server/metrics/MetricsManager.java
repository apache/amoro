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

import com.netease.arctic.ams.api.metrics.MetricsReporter;

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
  @SuppressWarnings("rawtypes")
  private final Map<String, MetricsReporter> reporters = new ConcurrentHashMap<>();

  public MetricsManager() {
  }

  public MetricsManager(List<ReporterMeta> metas) {
    metas.forEach(this::register);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public void register(ReporterMeta meta) {
    try {
      Class<?> clazz = Class.forName(meta.getImpl());
        MetricsReporter reporter = (MetricsReporter) clazz.newInstance();
        reporter.open(meta.getProperties());
        register(meta.getName(), reporter);
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      throw new RuntimeException("can not find reporter " + meta.getImpl(), e);
    }
  }

  @SuppressWarnings("rawtypes")
  public void register(String name, MetricsReporter reporter) {
    this.reporters.put(name, reporter);
  }

  public void unregister(String name) {
    if (this.reporters.containsKey(name)) {
      this.reporters.remove(name).close();
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public void report(Object metrics) {
    for (MetricsReporter reporter : reporters.values()) {
      reporter.report(reporter.parser().parse(metrics));
    }
  }

  public void shutdown() {
    this.reporters.keySet().forEach(this::unregister);
  }
}
