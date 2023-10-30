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
    SelfOptimizingStatusDurationMsContent selfOptimizingStatusDurationMsContent =
        new SelfOptimizingStatusDurationMsContent("tableName", "idle");
    selfOptimizingStatusDurationMsContent.setOptimizingProcessId(1L);
    selfOptimizingStatusDurationMsContent.setTargetSnapshotId(1L);
    selfOptimizingStatusDurationMsContent.setOptimizingType("major");
    Counter counter = selfOptimizingStatusDurationMsContent.tableOptimizingStatusDurationMs();
    counter.inc(100);
    TaggedMetrics taggedMetrics = TaggedMetrics.from(selfOptimizingStatusDurationMsContent);
    Assert.assertNotNull(
        taggedMetrics.tags().get(SelfOptimizingStatusDurationMsContent.TABLE_NAME));
    Assert.assertEquals(
        ((Counter)
                taggedMetrics
                    .metrics()
                    .get(SelfOptimizingStatusDurationMsContent.TABLE_OPTIMIZING_STATUS_DURATION_MS))
            .getCount(),
        100);
  }

  @Test
  public void selfOptimizingTotalCostContentTest() {
    SelfOptimizingTotalCostMsContent selfOptimizingTotalCostMsContent =
        new SelfOptimizingTotalCostMsContent("tableName", 1L, "major");
    Counter counter = selfOptimizingTotalCostMsContent.tableOptimizingTotalCostMs();
    counter.inc(100);
    TaggedMetrics taggedMetrics = TaggedMetrics.from(selfOptimizingTotalCostMsContent);
    Assert.assertNotNull(taggedMetrics.tags().get(SelfOptimizingTotalCostMsContent.TABLE_NAME));
    Assert.assertEquals(
        ((Counter)
                taggedMetrics
                    .metrics()
                    .get(SelfOptimizingTotalCostMsContent.TABLE_OPTIMIZING_TOTAL_COST_MS))
            .getCount(),
        100);
  }
}
