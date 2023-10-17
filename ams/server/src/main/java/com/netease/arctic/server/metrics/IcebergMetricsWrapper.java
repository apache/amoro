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

import com.netease.arctic.ams.api.metrics.MetricType;
import com.netease.arctic.ams.api.metrics.MetricsContent;
import org.apache.iceberg.metrics.MetricsReport;
import org.apache.iceberg.rest.requests.ReportMetricsRequest;

public class IcebergMetricsWrapper {
  public static MetricsContent<MetricsReport> wrap(MetricsReport report) {
    ReportMetricsRequest typedMetric = ReportMetricsRequest.of(report);
    return new MetricsContent<MetricsReport>() {
      @Override
      public String name() {
        return typedMetric.reportType().name();
      }

      @Override
      public MetricType type() {
        return MetricType.FORMAT_ICEBERG;
      }

      @Override
      public MetricsReport data() {
        return report;
      }
    };
  }
}
