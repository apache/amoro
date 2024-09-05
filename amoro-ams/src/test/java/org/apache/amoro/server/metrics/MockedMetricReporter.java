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

package org.apache.amoro.server.metrics;

import org.apache.amoro.metrics.MetricReporter;
import org.apache.amoro.metrics.MetricSet;

import java.util.Map;

public class MockedMetricReporter implements MetricReporter {

  MetricSet metrics;

  @Override
  public void open(Map<String, String> properties) {}

  @Override
  public void close() {}

  @Override
  public String name() {
    return "mocked-reporter";
  }

  @Override
  public void setGlobalMetricSet(MetricSet globalMetricSet) {
    this.metrics = globalMetricSet;
  }
}
