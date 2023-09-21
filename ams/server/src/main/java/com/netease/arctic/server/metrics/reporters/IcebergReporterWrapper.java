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

package com.netease.arctic.server.metrics.reporters;

import com.netease.arctic.ams.api.metrics.MetricEmitter;
import com.netease.arctic.ams.api.metrics.PayloadMetrics;
import org.apache.iceberg.metrics.MetricsReport;
import org.apache.iceberg.metrics.MetricsReporter;

import java.util.Map;

public class IcebergReporterWrapper implements MetricEmitter<MetricsReport> {

  private final MetricsReporter reporter;

  public IcebergReporterWrapper(MetricsReporter reporter) {
    this.reporter = reporter;
  }

  @Override
  public void open(Map<String, String> properties) {
    this.reporter.initialize(properties);
  }

  @Override
  public void report(PayloadMetrics<MetricsReport> metrics) {
    this.reporter.report(metrics.metrics());
  }

  @Override
  public void close() {

  }
}
