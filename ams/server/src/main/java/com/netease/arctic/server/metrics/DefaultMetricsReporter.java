package com.netease.arctic.server.metrics;

import com.netease.arctic.ams.api.metrics.MetricReport;
import com.netease.arctic.ams.api.metrics.MetricReporter;
import org.apache.iceberg.metrics.MetricsReport;

import java.util.Map;

public class DefaultMetricsReporter implements MetricReporter {

  @Override
  public void open(Map<String, String> properties) {
    MetricReporter.super.open(properties);
  }

  @Override
  public void report(MetricReport metricReport) {

  }

  @Override
  public void close() {

  }
}
