package com.netease.arctic.ams.api.metrics;

import java.util.Map;

/**
 * This is an interface defining a reporter, which users can implement to notify metrics to a monitoring system.
 * The system calls the open method to initialize the reporter and the close method to shut it down when needed.
 * The report method is called to notify the reporter when a metric is generated.
 */
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
