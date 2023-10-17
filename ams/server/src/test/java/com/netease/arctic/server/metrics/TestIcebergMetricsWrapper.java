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
import org.apache.iceberg.metrics.CommitMetricsResult;
import org.apache.iceberg.metrics.CommitReport;
import org.apache.iceberg.metrics.MetricsReport;
import org.apache.iceberg.rest.requests.ReportMetricsRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestIcebergMetricsWrapper {

  @Test
  public void testWrap() {
    CommitReport report = new CommitReport() {
      @Override
      public String tableName() {
        return null;
      }

      @Override
      public long snapshotId() {
        return 0;
      }

      @Override
      public long sequenceNumber() {
        return 0;
      }

      @Override
      public String operation() {
        return null;
      }

      @Override
      public CommitMetricsResult commitMetrics() {
        return null;
      }

      @Override
      public Map<String, String> metadata() {
        return null;
      }
    };
    MetricsContent<MetricsReport> metricsContent = IcebergMetricsWrapper.wrap(report);
    assertEquals(metricsContent.name(), ReportMetricsRequest.ReportType.COMMIT_REPORT.name());
    assertEquals(metricsContent.type(), MetricType.FORMAT_ICEBERG);
    assertEquals(metricsContent.data(), report);
  }
}
