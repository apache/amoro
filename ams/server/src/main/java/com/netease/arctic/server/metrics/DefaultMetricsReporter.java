package com.netease.arctic.server.metrics;

import com.netease.arctic.ams.api.metrics.MetricReporter;
import org.apache.iceberg.metrics.MetricsReport;

import java.util.Map;

public class DefaultMetricsReporter implements MetricReporter {
  public void initialize(Map<String, String> properties) {

  }

  @Override
  public void report(MetricsReport report) {

  }
}
