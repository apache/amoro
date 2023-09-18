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

package com.netease.arctic.server.metrics;

import com.netease.arctic.ams.api.metrics.MetricParser;
import com.netease.arctic.ams.api.metrics.MetricsContent;
import com.netease.arctic.ams.api.metrics.TaggedMetrics;
import org.apache.iceberg.metrics.MetricsReport;

public class TaggedMetricParser implements MetricParser<TaggedMetrics> {

  @Override
  public TaggedMetrics parse(Object metrics) {
    if (metrics instanceof MetricsContent) {
      return TaggedMetrics.from((MetricsContent) metrics);
    }
    throw new UnsupportedOperationException(String.format("%s %s %s", "not support parse",
        metrics.getClass().getName(), "to TaggedMetrics"));
  }

}
