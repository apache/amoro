package com.netease.arctic.server.metrics.promethues;

import com.clearspring.analytics.util.Lists;
import com.netease.arctic.ams.api.metrics.Counter;
import com.netease.arctic.ams.api.metrics.Gauge;
import com.netease.arctic.ams.api.metrics.Metric;
import com.netease.arctic.ams.api.metrics.MetricDefine;
import com.netease.arctic.ams.api.metrics.MetricSet;
import com.netease.arctic.ams.api.metrics.MetricType;
import com.netease.arctic.ams.api.metrics.RegisteredMetricKey;
import io.prometheus.client.Collector;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MetricCollector extends Collector {
  MetricSet metrics;

  public MetricCollector(MetricSet metrics) {
    this.metrics = metrics;
  }

  @Override
  public List<MetricFamilySamples> collect() {
    Map<RegisteredMetricKey, Metric> registeredMetrics = metrics.getMetrics();

    Map<MetricDefine, List<RegisteredMetricKey>> metricDefineMap =
        registeredMetrics.keySet().stream()
            .collect(
                Collectors.groupingBy(
                    RegisteredMetricKey::getDefine,
                    Collectors.mapping(Function.identity(), Collectors.toList())));
    return metricDefineMap.entrySet().stream()
        .map(entry -> createFamilySample(entry.getKey(), entry.getValue(), registeredMetrics))
        .collect(Collectors.toList());
  }

  private MetricFamilySamples createFamilySample(
      MetricDefine define,
      List<RegisteredMetricKey> keys,
      Map<RegisteredMetricKey, Metric> registeredMetrics) {

    List<MetricFamilySamples.Sample> samples = Lists.newArrayList();
    for (RegisteredMetricKey key : keys) {
      Metric metric = registeredMetrics.get(key);

      MetricFamilySamples.Sample sample =
          new MetricFamilySamples.Sample(
              define.getName(), define.getTags(), key.valueOfTags(), covertValue(metric));
      samples.add(sample);
    }

    return new MetricFamilySamples(
        define.getName(), covertType(define.getType()), define.getDescription(), samples);
  }

  private Type covertType(MetricType metricType) {
    switch (metricType) {
      case Counter:
        return Type.COUNTER;
      case Gauge:
        return Type.GAUGE;
      default:
        throw new IllegalStateException("unknown type:" + metricType);
    }
  }

  private double covertValue(Metric metric) {
    if (metric instanceof Counter) {
      return ((Counter) metric).getCount();
    } else if (metric instanceof Gauge) {
      return ((Gauge<?>) metric).getValue().doubleValue();
    } else {
      throw new IllegalStateException(
          "unknown metric implement class:" + metric.getClass().getName());
    }
  }
}
