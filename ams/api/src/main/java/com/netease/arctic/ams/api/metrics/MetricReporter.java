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

import com.netease.arctic.ams.api.ActivePlugin;

/**
 * This interface define a collector, which users can fetch metrics and report to metrics monitor
 * system.
 *
 * <p>If a metric reporter implement the {@link MetricRegisterListener} interface, when new metric
 * added or removed, the listener method will be called.
 */
public interface MetricReporter extends ActivePlugin {

  /**
   * This method will be called after metric reporter is opened. And a metric set will be set to the
   * metric reporter which could fetch all metric at time.
   *
   * @param globalMetricSet a metric set contains all registered metrics
   */
  void setGlobalMetricSet(MetricSet globalMetricSet);
}
