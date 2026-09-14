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

package org.apache.amoro.server.optimizing;

import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.GROUP_TAG;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_COMMITTING_TABLES;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_EXECUTING_TABLES;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_IDLE_TABLES;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_PENDING_TABLES;
import static org.apache.amoro.server.optimizing.OptimizerGroupMetrics.OPTIMIZER_GROUP_PLANING_TABLES;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.amoro.metrics.Gauge;
import org.apache.amoro.metrics.MetricDefine;
import org.apache.amoro.metrics.MetricKey;
import org.apache.amoro.metrics.MetricRegistry;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;

public class TestOptimizerGroupMetrics {

  private static final String GROUP_NAME = "test-group";

  @Test
  public void testTableGaugesUseRuntimeSnapshot() {
    List<DefaultTableRuntime> tableRuntimes =
        Arrays.asList(
            tableRuntime(OptimizingStatus.PLANNING),
            tableRuntime(OptimizingStatus.PENDING),
            tableRuntime(OptimizingStatus.MINOR_OPTIMIZING),
            tableRuntime(OptimizingStatus.IDLE),
            tableRuntime(OptimizingStatus.COMMITTING));

    SchedulingPolicy schedulingPolicy = mock(SchedulingPolicy.class);
    when(schedulingPolicy.getTableRuntimeMap())
        .thenThrow(new ConcurrentModificationException("live map must not be iterated"));
    when(schedulingPolicy.snapshotTableRuntimes()).thenReturn(tableRuntimes);

    OptimizingQueue optimizingQueue = mock(OptimizingQueue.class);
    when(optimizingQueue.getSchedulingPolicy()).thenReturn(schedulingPolicy);

    MetricRegistry registry = new MetricRegistry();
    OptimizerGroupMetrics metrics =
        new OptimizerGroupMetrics(GROUP_NAME, registry, optimizingQueue);
    metrics.register();
    try {
      Assert.assertEquals(1L, gaugeValue(registry, OPTIMIZER_GROUP_PLANING_TABLES));
      Assert.assertEquals(1L, gaugeValue(registry, OPTIMIZER_GROUP_PENDING_TABLES));
      Assert.assertEquals(2L, gaugeValue(registry, OPTIMIZER_GROUP_EXECUTING_TABLES));
      Assert.assertEquals(1L, gaugeValue(registry, OPTIMIZER_GROUP_IDLE_TABLES));
      Assert.assertEquals(1L, gaugeValue(registry, OPTIMIZER_GROUP_COMMITTING_TABLES));
    } finally {
      metrics.unregister();
    }
  }

  private DefaultTableRuntime tableRuntime(OptimizingStatus status) {
    DefaultTableRuntime tableRuntime = mock(DefaultTableRuntime.class);
    when(tableRuntime.getOptimizingStatus()).thenReturn(status);
    return tableRuntime;
  }

  @SuppressWarnings("unchecked")
  private long gaugeValue(MetricRegistry registry, MetricDefine define) {
    Map<String, String> tagValues = Collections.singletonMap(GROUP_TAG, GROUP_NAME);
    Gauge<Long> gauge = (Gauge<Long>) registry.getMetrics().get(new MetricKey(define, tagValues));
    Assert.assertNotNull(gauge);
    return gauge.getValue();
  }
}
