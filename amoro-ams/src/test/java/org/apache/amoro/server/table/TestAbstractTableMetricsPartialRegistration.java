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

package org.apache.amoro.server.table;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.metrics.Counter;
import org.apache.amoro.metrics.MetricDefine;
import org.apache.amoro.metrics.MetricRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test that AbstractTableMetrics.register properly rolls back partially registered metrics when
 * registerMetrics throws an exception partway through.
 */
public class TestAbstractTableMetricsPartialRegistration {

  private static final MetricDefine METRIC_A =
      MetricDefine.defineCounter("test_metric_a").withTags("catalog", "database", "table").build();

  private static final MetricDefine METRIC_B =
      MetricDefine.defineCounter("test_metric_b").withTags("catalog", "database", "table").build();

  private static final MetricDefine METRIC_C =
      MetricDefine.defineCounter("test_metric_c").withTags("catalog", "database", "table").build();

  /**
   * A test subclass of AbstractTableMetrics that registers 2 metrics successfully, then throws an
   * exception on the 3rd metric.
   */
  private static class PartialFailureMetrics extends AbstractTableMetrics {

    PartialFailureMetrics(ServerTableIdentifier identifier) {
      super(identifier);
    }

    @Override
    protected void registerMetrics(MetricRegistry registry) {
      registerMetric(registry, METRIC_A, new Counter());
      registerMetric(registry, METRIC_B, new Counter());
      // Simulate failure on the 3rd metric
      throw new RuntimeException("Simulated registration failure");
    }
  }

  /** A normal test subclass that registers all 3 metrics successfully. */
  private static class NormalMetrics extends AbstractTableMetrics {

    NormalMetrics(ServerTableIdentifier identifier) {
      super(identifier);
    }

    @Override
    protected void registerMetrics(MetricRegistry registry) {
      registerMetric(registry, METRIC_A, new Counter());
      registerMetric(registry, METRIC_B, new Counter());
      registerMetric(registry, METRIC_C, new Counter());
    }
  }

  @Test
  public void testPartialRegistrationRollback() {
    MetricRegistry registry = new MetricRegistry();
    ServerTableIdentifier identifier =
        ServerTableIdentifier.of("test_catalog", "test_db", "test_table", TableFormat.ICEBERG);

    PartialFailureMetrics metrics = new PartialFailureMetrics(identifier);

    // register should throw, and the 2 partially registered metrics should be rolled back
    Assertions.assertThrows(RuntimeException.class, () -> metrics.register(registry));

    // Verify: no metrics remain in the registry (all rolled back)
    Assertions.assertEquals(0, registry.getMetrics().size());

    // Verify: globalRegistry is still null (register didn't complete)
    // This means unregister() would be a no-op, which is fine since we already cleaned up
    metrics.unregister(); // should be safe no-op

    // Verify: a new metrics instance can register the same metrics without conflict
    NormalMetrics newMetrics = new NormalMetrics(identifier);
    newMetrics.register(registry);
    Assertions.assertEquals(3, registry.getMetrics().size());

    // Clean up
    newMetrics.unregister();
    Assertions.assertEquals(0, registry.getMetrics().size());
  }

  @Test
  public void testSuccessfulRegistrationAndUnregistration() {
    MetricRegistry registry = new MetricRegistry();
    ServerTableIdentifier identifier =
        ServerTableIdentifier.of("test_catalog", "test_db", "test_table", TableFormat.ICEBERG);

    NormalMetrics metrics = new NormalMetrics(identifier);

    // Normal registration should succeed
    metrics.register(registry);
    Assertions.assertEquals(3, registry.getMetrics().size());

    // Unregister should clean up all metrics
    metrics.unregister();
    Assertions.assertEquals(0, registry.getMetrics().size());

    // Re-registration with a new instance should succeed
    NormalMetrics newMetrics = new NormalMetrics(identifier);
    newMetrics.register(registry);
    Assertions.assertEquals(3, registry.getMetrics().size());

    newMetrics.unregister();
  }
}
