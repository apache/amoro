package com.netease.arctic.server.metrics.promethues;

import com.netease.arctic.ams.api.metrics.MetricReporter;
import com.netease.arctic.ams.api.metrics.MetricSet;
import io.prometheus.client.exporter.HTTPServer;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class PrometheusMetricReporter implements MetricReporter {

  public static final String PORT = "port";

  private int port;
  private HTTPServer server;

  @Override
  public void open(Map<String, String> properties) {
    this.port =
        Optional.ofNullable(properties.get(PORT))
            .map(Integer::valueOf)
            .orElseThrow(() -> new IllegalArgumentException("Lack required property: " + PORT));

    try {
      this.server = new HTTPServer(this.port);
    } catch (IOException e) {
      throw new RuntimeException("Start prometheus exporter server failed.", e);
    }
  }

  @Override
  public void close() {
    this.server.close();
  }

  @Override
  public String name() {
    return "prometheus-exporter";
  }

  @Override
  public void setGlobalMetricSet(MetricSet globalMetricSet) {
    MetricCollector collector = new MetricCollector(globalMetricSet);
    collector.register();
  }
}
