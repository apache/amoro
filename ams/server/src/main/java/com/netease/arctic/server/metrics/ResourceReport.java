package com.netease.arctic.server.metrics;

import org.apache.iceberg.metrics.MetricsReport;
import org.immutables.value.Value;

@Value.Immutable
public interface ResourceReport extends MetricsReport {

  String resourceGroup();

  ResourceMetricsResult metrics();
}
