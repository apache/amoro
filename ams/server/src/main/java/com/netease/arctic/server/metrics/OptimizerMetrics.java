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
import com.codahale.metrics.Gauge;
import com.codahale.metrics.RatioGauge;
import com.netease.arctic.ams.api.metrics.AmoroMetrics;
import com.netease.arctic.ams.api.metrics.TaggedMetrics;
import org.apache.iceberg.relocated.com.google.common.base.MoreObjects;

public class OptimizerMetrics implements AmoroMetrics {
  public static final String OPTIMIZER_REPORT_NAME = "optimizer_metrics";
  public static final String OPTIMIZER_GROUP = "optimizer-group";
  public static final String OPTIMIZER_THREAD_COUNT = "optimizer-thread-count";
  public static final String OPTIMIZER_OCCUPY_RATE = "optimizer-occupy-rate";

  private final String optimizerGroup;
  private final Counter optimizerThreadCount = new Counter();
  private final Counter optimizerOccupyTime = new Counter();
  private final Counter optimizerOccupyInterval = new Counter();
  private final Gauge<?> optimizerOccupyRate =
      new RatioGauge() {
        @Override
        protected Ratio getRatio() {
          return Ratio.of(optimizerOccupyTime.getCount(), optimizerOccupyInterval.getCount());
        }
      };

  public OptimizerMetrics(String optimizerGroup) {
    this.optimizerGroup = optimizerGroup;
  }

  public String name() {
    return OPTIMIZER_REPORT_NAME;
  }

  @TaggedMetrics.Tag(name = OPTIMIZER_GROUP)
  public String optimizerGroup() {
    return optimizerGroup;
  }

  @TaggedMetrics.Metric(name = OPTIMIZER_THREAD_COUNT)
  public Counter optimizerThreadCount() {
    return optimizerThreadCount;
  }

  public Counter optimizerOccupyTime() {
    return optimizerOccupyTime;
  }

  public Counter optimizerOccupyInterval() {
    return optimizerOccupyInterval;
  }

  @TaggedMetrics.Metric(name = OPTIMIZER_OCCUPY_RATE)
  public Gauge<?> optimizerOccupyRate() {
    return optimizerOccupyRate;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("optimizerGroup", optimizerGroup)
        .add("optimizerThreadCount", optimizerThreadCount.getCount())
        .add("optimizerOccupyTime", optimizerOccupyTime.getCount())
        .add("optimizerOccupyInterval", optimizerOccupyInterval.getCount())
        .add("optimizerOccupyRate", optimizerOccupyRate.getValue())
        .toString();
  }
}
