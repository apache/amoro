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

package com.netease.arctic.ams.api.metrics;

/** Metric type defines. */
public enum MetricType {
  Counter,
  Gauge;
  // More metric type is not defined.

  public boolean isType(Metric metric) {
    switch (this) {
      case Counter:
        return metric instanceof Counter;
      case Gauge:
        return metric instanceof Gauge;
    }
    return false;
  }

  public static MetricType ofType(Metric metric) {
    if (metric instanceof Counter) {
      return Counter;
    } else if (metric instanceof Gauge) {
      return Gauge;
    }
    throw new IllegalStateException("Unknown type of metric: " + metric.getClass().getName());
  }
}
