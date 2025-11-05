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

package org.apache.amoro.server.process.metric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SchedulerMetrics {

  private static final Logger LOG = LoggerFactory.getLogger(SchedulerMetrics.class);
  private static final String METRIC_SCHEDULER_SUCCESS = "scheduler_success";
  private static final String METRIC_SCHEDULER_FAILED = "scheduler_failed";
  private static final String METRIC_SCHEDULER_COST = "scheduler_cost";
  private static final String METRIC_SCHEDULER_LATENCY = "scheduler_latency";

  public static Metric newMetric(String schedulerName) {
    return new Metric(schedulerName);
  }

  public static class Metric {
    private final String schedulerName;
    private long start = System.currentTimeMillis();
    private long finish = 0;

    public Metric(String schedulerName) {
      this.schedulerName = schedulerName;
    }

    public void start() {}

    public void success() {}

    public void failed(String message) {}

    private Map<String, String> tags() {
      return null;
    }
  }
}
