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

import com.codahale.metrics.Counter;
import com.netease.arctic.ams.api.metrics.MetricType;
import com.netease.arctic.ams.api.metrics.MetricsContent;
import com.netease.arctic.ams.api.metrics.TaggedMetrics;
import org.apache.iceberg.relocated.com.google.common.annotations.VisibleForTesting;

public class SelfOptimizingTotalCostMsContent
    implements MetricsContent<SelfOptimizingTotalCostMsContent> {
  public static final String SELF_OPTIMIZING_TOTAL_COST_MS_CONTENT =
      "self_optimizing_total_cost_ms_content";

  public static final String TABLE_NAME = "table-name";
  public static final String OPTIMIZING_PROCESS_ID = "optimizing-process-id";
  public static final String OPTIMIZING_TYPE = "optimizing-type";

  @VisibleForTesting
  public static final String TABLE_OPTIMIZING_TOTAL_COST_MS = "table-optimizing-total-cost-ms";

  private final String tableName;
  private final Long optimizingProcessId;
  private final String optimizingType;

  private final Counter tableOptimizingTotalCostMs = new Counter();

  public SelfOptimizingTotalCostMsContent(
      String tableName, Long optimizingProcessId, String optimizingType) {
    this.tableName = tableName;
    this.optimizingProcessId = optimizingProcessId;
    this.optimizingType = optimizingType;
  }

  @TaggedMetrics.Tag(name = TABLE_NAME)
  public String tableName() {
    return tableName;
  }

  @TaggedMetrics.Tag(name = OPTIMIZING_PROCESS_ID)
  public Long optimizingProcessId() {
    return optimizingProcessId;
  }

  @TaggedMetrics.Tag(name = OPTIMIZING_TYPE)
  public String optimizingType() {
    return optimizingType;
  }

  @TaggedMetrics.Metric(name = TABLE_OPTIMIZING_TOTAL_COST_MS)
  public Counter tableOptimizingTotalCostMs() {
    return this.tableOptimizingTotalCostMs;
  }

  @Override
  public String name() {
    return SELF_OPTIMIZING_TOTAL_COST_MS_CONTENT;
  }

  @Override
  public MetricType type() {
    return MetricType.SERVICE;
  }

  @Override
  public SelfOptimizingTotalCostMsContent data() {
    return this;
  }
}
