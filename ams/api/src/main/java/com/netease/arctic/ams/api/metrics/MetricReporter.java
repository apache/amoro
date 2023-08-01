package com.netease.arctic.ams.api.metrics;

import org.apache.iceberg.metrics.MetricsReport;

import java.util.Map;

public interface MetricReporter {

  /**
   * A custom MetricsReporter implementation must have a no-arg constructor, which will be called
   * first. {@link MetricReporter#initialize(Map properties)} is called to complete the
   * initialization.
   *
   * @param properties properties
   */
  default void initialize(Map<String, String> properties) {

  }

  /**
   * Indicates that an operation is done by reporting a {@link MetricsReport}. A {@link
   * MetricsReport} is usually directly derived from a {@link MetricsReport} instance.
   *
   * @param report {@link MetricsReport} to report.
   */
  void report(MetricsReport report);
}
