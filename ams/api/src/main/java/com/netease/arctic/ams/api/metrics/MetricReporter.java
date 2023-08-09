package com.netease.arctic.ams.api.metrics;

import java.util.Map;

public interface MetricReporter {

  /**
   * A custom MetricsReporter implementation must have a no-arg constructor, which will be called
   * first. {@link MetricReporter#open(Map properties)} is called to complete the
   * initialization.
   *
   * @param properties properties
   */
  default void open(Map<String, String> properties) {

  }

  /**
   * Indicates that an operation is done by reporting a {@link MetricReport}. A {@link
   * MetricReport} is usually directly derived from a {@link MetricReport} instance.
   *
   * @param metricReport {@link MetricReport} to report.
   */
  void report(MetricReport metricReport);

  /**
   * Indicates that an operation is done by reporting a {@link MetricReport}. A {@link
   * MetricReport} is usually directly derived from a {@link MetricReport} instance.
   */
  void close();
}
