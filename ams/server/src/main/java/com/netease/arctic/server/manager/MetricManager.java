package com.netease.arctic.server.manager;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricRegistryListener;
import com.codahale.metrics.Timer;
import com.google.common.collect.Maps;
import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.ams.api.events.Event;
import com.netease.arctic.ams.api.events.EventEmitter;
import com.netease.arctic.ams.api.events.EventType;
import com.netease.arctic.ams.api.events.TableOptimizingStateChangedEventContent;
import com.netease.arctic.ams.api.events.TableRuntimeEventContent;
import com.netease.arctic.ams.api.metrics.MetricName;
import com.netease.arctic.ams.api.metrics.MetricRegister;
import com.netease.arctic.ams.api.metrics.MetricRegisterListener;
import com.netease.arctic.ams.api.metrics.MetricReporter;
import com.netease.arctic.server.metrics.TableMetrics;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MetricManager extends ActivePluginManager<MetricReporter>
    implements EventEmitter, MetricRegister {

  private static final String pluginName = "metric-manager";

  private final MetricRegistry metricRegistry = new MetricRegistry();
  private final Map<TableIdentifier, TableMetrics> tableMetrics = Maps.newConcurrentMap();

  public MetricManager() {
    this.metricRegistry.addListener(new MetricListener());
  }

  public void initialize() {
    forEach(
        reporter -> {
          reporter.setRegistry(this);
        });
  }

  @Override
  protected Map<String, String> loadProperties(String pluginName) {
    return null;
  }

  @Override
  public void open(Map<String, String> properties) {}

  @Override
  public String name() {
    return pluginName;
  }

  @Override
  public void emit(Event<?> event) {
    if (EventType.TableRuntimeAdded.equals(event.type())) {
      TableRuntimeEventContent content = (TableRuntimeEventContent) event.content();
      TableMetrics tableMetric =
          tableMetrics.computeIfAbsent(content.getIdentifier(), TableMetrics::new);
      tableMetric.register(metricRegistry);
    } else if (EventType.TableRuntimeRemoved.equals(event.type())) {
      TableRuntimeEventContent content = (TableRuntimeEventContent) event.content();
      TableMetrics tableMetric = tableMetrics.get(content.getIdentifier());
      if (tableMetric != null) {
        tableMetric.unregister(metricRegistry);
        tableMetrics.remove(content.getIdentifier());
      }
    } else if (EventType.TableOptimizingStateChanged.equals(event.type())) {
      TableOptimizingStateChangedEventContent content =
          (TableOptimizingStateChangedEventContent) event.content();
      tableMetrics.computeIfPresent(
          content.getIdentifier(),
          (id, metric) -> {
            metric.stateChanged(content.getCurrentState(), event.eventTime());
            return metric;
          });
    }
  }

  @Override
  public Set<EventType<?>> accepts() {
    return EventType.allTypes();
  }

  @Override
  public Map<MetricName, Metric> allMetrics() {
    return metricRegistry.getMetrics().entrySet().stream()
        .collect(
            Collectors.toMap(
                entry -> MetricName.fromExplicitName(entry.getKey()), Map.Entry::getValue));
  }

  private void onMetricAdded(MetricName metricName, Metric metric) {
    forEach(
        reporter -> {
          if (reporter instanceof MetricRegisterListener) {
            ((MetricRegisterListener) reporter).onMetricAdded(metricName, metric);
          }
        });
  }

  private void onMetricRemoved(MetricName metricName) {
    forEach(
        reporter -> {
          if (reporter instanceof MetricRegisterListener) {
            ((MetricRegisterListener) reporter).onMetricRemoved(metricName);
          }
        });
  }

  class MetricListener implements MetricRegistryListener {

    @Override
    public void onGaugeAdded(String name, Gauge<?> gauge) {
      onMetricAdded(MetricName.fromExplicitName(name), gauge);
    }

    @Override
    public void onGaugeRemoved(String name) {
      onMetricRemoved(MetricName.fromExplicitName(name));
    }

    @Override
    public void onCounterAdded(String name, Counter counter) {
      onMetricAdded(MetricName.fromExplicitName(name), counter);
    }

    @Override
    public void onCounterRemoved(String name) {
      onMetricRemoved(MetricName.fromExplicitName(name));
    }

    @Override
    public void onHistogramAdded(String name, Histogram histogram) {
      onMetricAdded(MetricName.fromExplicitName(name), histogram);
    }

    @Override
    public void onHistogramRemoved(String name) {
      onMetricRemoved(MetricName.fromExplicitName(name));
    }

    @Override
    public void onMeterAdded(String name, Meter meter) {
      onMetricAdded(MetricName.fromExplicitName(name), meter);
    }

    @Override
    public void onMeterRemoved(String name) {
      onMetricRemoved(MetricName.fromExplicitName(name));
    }

    @Override
    public void onTimerAdded(String name, Timer timer) {
      onMetricAdded(MetricName.fromExplicitName(name), timer);
    }

    @Override
    public void onTimerRemoved(String name) {
      onMetricRemoved(MetricName.fromExplicitName(name));
    }
  }
}
