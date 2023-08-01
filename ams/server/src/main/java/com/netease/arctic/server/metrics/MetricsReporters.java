package com.netease.arctic.server.metrics;

import com.netease.arctic.ams.api.metrics.MetricReporter;
import org.apache.iceberg.metrics.MetricsReport;

import java.util.Set;

public class MetricsReporters {
  private Set<MetricReporter> metricReporters;

  public void report(MetricsReport report) {
    for (MetricReporter reporter : metricReporters) {
      reporter.report(report);
    }
  }
}
