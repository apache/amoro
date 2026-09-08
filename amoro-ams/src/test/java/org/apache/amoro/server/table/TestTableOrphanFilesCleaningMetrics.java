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
import org.apache.amoro.metrics.MetricRegistry;
import org.apache.amoro.maintainer.MaintainerMetrics.CleanFailureReason;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link TableOrphanFilesCleaningMetrics} — the 2 last-value gauges introduced for
 * orphan-file-cleaning monitoring. These tests exercise the {@code recordSuccess()} and {@code
 * recordFailure(reason)} methods directly against the metric objects registered in a {@link
 * MetricRegistry}, verifying the Gauge <em>last-value</em> semantics.
 */
public class TestTableOrphanFilesCleaningMetrics {

  private MetricRegistry registry;
  private TableOrphanFilesCleaningMetrics metrics;

  @Before
  public void setUp() {
    registry = new MetricRegistry();
    metrics =
        new TableOrphanFilesCleaningMetrics(
            ServerTableIdentifier.of("test_catalog", "test_db", "test_table", TableFormat.ICEBERG));
    metrics.register(registry);
  }

  @After
  public void tearDown() {
    if (metrics != null) {
      metrics.unregister();
    }
  }

  // ---- baseline initialization ----

  @Test
  public void testInitialStateIsSuccess() {
    assertEquals(TableOrphanFilesCleaningMetrics.STATUS_SUCCESS, metrics.getLastStatus());
    assertEquals(0L, metrics.getLastFailureTimestampMs());
  }

  // ---- recordSuccess resets last_status but preserves lastFailureTimestampMs ----

  @Test
  public void testRecordSuccessAfterFailureResetsStatus() {
    metrics.recordFailure(CleanFailureReason.LOCATION_CONFLICT);
    assertEquals(CleanFailureReason.LOCATION_CONFLICT.statusCode(), metrics.getLastStatus());
    long failureTsBefore = metrics.getLastFailureTimestampMs();
    assertTrue("lastFailureTimestampMs should be > 0 after a real failure", failureTsBefore > 0);

    metrics.recordSuccess();
    assertEquals(TableOrphanFilesCleaningMetrics.STATUS_SUCCESS, metrics.getLastStatus());
    assertEquals(
        "recordSuccess must NOT reset lastFailureTimestampMs — timestamp persists so monitoring"
            + " can alert on stale-failure windows",
        failureTsBefore,
        metrics.getLastFailureTimestampMs());
  }

  // ---- recordFailure for each "real failure" reason updates last_status / ts ----

  @Test
  public void testLocationConflictUpdatesLastStatusAndTs() {
    metrics.recordFailure(CleanFailureReason.LOCATION_CONFLICT);
    assertEquals(CleanFailureReason.LOCATION_CONFLICT.statusCode(), metrics.getLastStatus());
    assertTrue(
        "lastFailureTimestampMs should be > 0 on a real failure",
        metrics.getLastFailureTimestampMs() > 0);
  }

  @Test
  public void testLocationCheckUnavailableUpdatesLastStatusAndTs() {
    metrics.recordFailure(CleanFailureReason.LOCATION_CONFLICT_CHECK_FAILED);
    assertEquals(
        CleanFailureReason.LOCATION_CONFLICT_CHECK_FAILED.statusCode(), metrics.getLastStatus());
    assertTrue(metrics.getLastFailureTimestampMs() > 0);
  }

  @Test
  public void testExecutionFailedUpdatesLastStatusAndTs() {
    metrics.recordFailure(CleanFailureReason.EXECUTION_FAILED);
    assertEquals(CleanFailureReason.EXECUTION_FAILED.statusCode(), metrics.getLastStatus());
    assertTrue(metrics.getLastFailureTimestampMs() > 0);
  }
}
