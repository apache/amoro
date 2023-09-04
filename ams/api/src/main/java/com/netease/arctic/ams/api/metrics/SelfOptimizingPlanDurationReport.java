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

import com.codahale.metrics.Timer;

public class SelfOptimizingPlanDurationReport implements MetricsContent<SelfOptimizingPlanDurationReport> {
  public static final String SELF_OPTIMIZING_PLAN_DURATION_REPORT_NAME = "self_optimizing_plan_duration_report";

  public static final String TABLE_NAME = "table-name";

  private static final String TABLE_OPTIMIZING_PLAN_DURATION = "table-optimizing-plan-duration";

  private final String tableName;

  private final Timer tableOptimizingPlanDuration = new Timer();

  public SelfOptimizingPlanDurationReport(String tableName) {
    this.tableName = tableName;
  }

  @TaggedMetrics.Tag(name = TABLE_NAME)
  public String tableName() {
    return tableName;
  }

  @TaggedMetrics.Metric(name = TABLE_OPTIMIZING_PLAN_DURATION)
  public Timer tableOptimizingPlanDuration() {
    return this.tableOptimizingPlanDuration;
  }

  @Override
  public String name() {
    return SELF_OPTIMIZING_PLAN_DURATION_REPORT_NAME;
  }

  @Override
  public MetricType type() {
    return MetricType.SERVICE;
  }

  @Override
  public SelfOptimizingPlanDurationReport data() {
    return this;
  }
}
