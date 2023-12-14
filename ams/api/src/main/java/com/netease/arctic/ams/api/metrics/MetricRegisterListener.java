package com.netease.arctic.ams.api.metrics;

import com.codahale.metrics.Metric;

public interface MetricRegisterListener {

  void onMetricAdded(MetricName name, Metric metric);

  void onMetricRemoved(MetricName name);
}
