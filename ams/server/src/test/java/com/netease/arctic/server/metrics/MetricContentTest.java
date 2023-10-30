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
import com.codahale.metrics.Timer;
import com.netease.arctic.ams.api.metrics.TaggedMetrics;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class MetricContentTest {
  @Test
  public void selfOptimizingPlanDurationContentTest() {
    SelfOptimizingPlanDurationContent selfOptimizingPlanDurationContent =
        new SelfOptimizingPlanDurationContent("tableName");
    Timer timer = selfOptimizingPlanDurationContent.tableOptimizingPlanDuration();
    timer.time(
        () -> {
          try {
            TimeUnit.MILLISECONDS.sleep(10);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        });
    TaggedMetrics taggedMetrics = TaggedMetrics.from(selfOptimizingPlanDurationContent);
    Assert.assertNotNull(
        taggedMetrics
            .metrics()
            .get(SelfOptimizingPlanDurationContent.TABLE_OPTIMIZING_PLAN_DURATION));
    Assert.assertEquals(
        taggedMetrics.tags().get(SelfOptimizingPlanDurationContent.TABLE_NAME), "tableName");
  }

  @Test
  public void selfOptimizingStatusDurationContentTest() {
    SelfOptimizingStatusDurationContent selfOptimizingStatusDurationContent =
        new SelfOptimizingStatusDurationContent("tableName", "idle");
    selfOptimizingStatusDurationContent.setOptimizingProcessId(1L);
    selfOptimizingStatusDurationContent.setTargetSnapshotId(1L);
    selfOptimizingStatusDurationContent.setOptimizingType("major");
    Counter counter = selfOptimizingStatusDurationContent.tableOptimizingStatusDurationMs();
    counter.inc(100);
    TaggedMetrics taggedMetrics = TaggedMetrics.from(selfOptimizingStatusDurationContent);
    Assert.assertNotNull(taggedMetrics.tags().get(SelfOptimizingStatusDurationContent.TABLE_NAME));
    Assert.assertEquals(
        ((Counter)
                taggedMetrics
                    .metrics()
                    .get(SelfOptimizingStatusDurationContent.TABLE_OPTIMIZING_STATUS_DURATION))
            .getCount(),
        100);
  }

  @Test
  public void selfOptimizingTotalCostContentTest() {
    SelfOptimizingTotalCostContent selfOptimizingTotalCostContent =
        new SelfOptimizingTotalCostContent("tableName", 1L, "major");
    Counter counter = selfOptimizingTotalCostContent.tableOptimizingTotalCostDurationMs();
    counter.inc(100);
    TaggedMetrics taggedMetrics = TaggedMetrics.from(selfOptimizingTotalCostContent);
    Assert.assertNotNull(taggedMetrics.tags().get(SelfOptimizingTotalCostContent.TABLE_NAME));
    Assert.assertEquals(
        ((Counter)
                taggedMetrics
                    .metrics()
                    .get(SelfOptimizingTotalCostContent.TABLE_OPTIMIZING_TOTAL_COST_DURATION))
            .getCount(),
        100);
  }
}
