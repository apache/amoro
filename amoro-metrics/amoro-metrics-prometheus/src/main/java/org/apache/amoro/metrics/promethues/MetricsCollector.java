/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.amoro.metrics.promethues;

import io.prometheus.client.Collector;
import org.apache.amoro.metrics.Counter;
import org.apache.amoro.metrics.Gauge;
import org.apache.amoro.metrics.Metric;
import org.apache.amoro.metrics.MetricDefine;
import org.apache.amoro.metrics.MetricKey;
import org.apache.amoro.metrics.MetricSet;
import org.apache.amoro.metrics.MetricType;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Metric type converter for prometheus api */
public class MetricsCollector extends Collector {
  private static final Logger LOGGER = LoggerFactory.getLogger(MetricsCollector.class);
  private static final String PREFIX = "amoro_";
  private static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z_:][a-zA-Z0-9_:]*");
  private static final Pattern LABEL_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");
  MetricSet metrics;

  public MetricsCollector(MetricSet metrics) {
    this.metrics = metrics;
  }

  @Override
  public List<MetricFamilySamples> collect() {
    Map<MetricKey, Metric> registeredMetrics = metrics.getMetrics();

    Map<MetricDefine, List<MetricKey>> metricDefineMap =
        registeredMetrics.keySet().stream()
            .collect(
                Collectors.groupingBy(
                    MetricKey::getDefine,
                    Collectors.mapping(Function.identity(), Collectors.toList())));
    return metricDefineMap.entrySet().stream()
        .filter(entry -> isValidMetric(entry.getKey()))
        .map(entry -> createFamilySample(entry.getKey(), entry.getValue(), registeredMetrics))
        .collect(Collectors.toList());
  }

  private boolean isValidMetric(MetricDefine define) {
    boolean nameIsValid = NAME_PATTERN.matcher(define.getName()).matches();
    boolean labelIsValid = true;
    for (String tag : define.getTags()) {
      if (!LABEL_PATTERN.matcher(tag).matches()) {
        labelIsValid = false;
        break;
      }
    }
    boolean valid = nameIsValid && labelIsValid;
    if (!valid) {
      LOGGER.warn("Metric {} is not a valid prometheus metric.", define);
    }
    return valid;
  }

  private MetricFamilySamples createFamilySample(
      MetricDefine define, List<MetricKey> keys, Map<MetricKey, Metric> registeredMetrics) {

    List<MetricFamilySamples.Sample> samples = Lists.newArrayList();
    for (MetricKey key : keys) {
      Metric metric = registeredMetrics.get(key);

      MetricFamilySamples.Sample sample =
          new MetricFamilySamples.Sample(
              PREFIX + define.getName(), define.getTags(), key.valueOfTags(), covertValue(metric));
      samples.add(sample);
    }

    return new MetricFamilySamples(
        PREFIX + define.getName(), covertType(define.getType()), define.getDescription(), samples);
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
