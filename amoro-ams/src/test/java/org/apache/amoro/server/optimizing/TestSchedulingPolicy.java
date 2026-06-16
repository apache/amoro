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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.table.TableIdentifier;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class TestSchedulingPolicy {

  @Test
  public void testScheduleTablesRespectsLimitAndDoesNotMutateSkipSet() {
    SchedulingPolicy policy =
        new SchedulingPolicy(new ResourceGroup.Builder("test", "local").build());
    DefaultTableRuntime first = tableRuntime("first", 1L, OptimizingStatus.PENDING, 0L, 2L, 1L, 0L);
    DefaultTableRuntime second =
        tableRuntime("second", 2L, OptimizingStatus.PENDING, 0L, 2L, 1L, 0L);
    DefaultTableRuntime skipped =
        tableRuntime("skipped", 3L, OptimizingStatus.PENDING, 0L, 2L, 1L, 0L);
    policy.addTable(first);
    policy.addTable(second);
    policy.addTable(skipped);

    Set<ServerTableIdentifier> skipSet = new HashSet<>();
    skipSet.add(skipped.getTableIdentifier());
    Set<ServerTableIdentifier> original = new HashSet<>(skipSet);

    List<DefaultTableRuntime> result = policy.scheduleTables(skipSet, 1);

    Assert.assertEquals(1, result.size());
    Assert.assertFalse(result.contains(skipped));
    Assert.assertEquals(original, skipSet);
  }

  @Test
  public void testScheduleTablesFiltersNonPendingAndMinPlanIntervalTables() {
    SchedulingPolicy policy =
        new SchedulingPolicy(new ResourceGroup.Builder("test", "local").build());
    DefaultTableRuntime eligible =
        tableRuntime("eligible", 4L, OptimizingStatus.PENDING, 0L, 2L, 1L, 0L);
    DefaultTableRuntime idle = tableRuntime("idle", 5L, OptimizingStatus.IDLE, 0L, 2L, 1L, 0L);
    DefaultTableRuntime insideInterval =
        tableRuntime(
            "inside_interval",
            6L,
            OptimizingStatus.PENDING,
            System.currentTimeMillis(),
            2L,
            1L,
            60_000L);
    policy.addTable(eligible);
    policy.addTable(idle);
    policy.addTable(insideInterval);

    List<DefaultTableRuntime> result = policy.scheduleTables(new HashSet<>(), 10);

    Assert.assertEquals(1, result.size());
    Assert.assertEquals(eligible.getTableIdentifier(), result.get(0).getTableIdentifier());
  }

  @Test
  public void testScheduleTablesWithNonPositiveLimitReturnsEmpty() {
    SchedulingPolicy policy =
        new SchedulingPolicy(new ResourceGroup.Builder("test", "local").build());
    DefaultTableRuntime table =
        tableRuntime("eligible", 7L, OptimizingStatus.PENDING, 0L, 2L, 1L, 0L);
    policy.addTable(table);

    Assert.assertTrue(policy.scheduleTables(new HashSet<>(), 0).isEmpty());
    Assert.assertTrue(policy.scheduleTables(new HashSet<>(), -1).isEmpty());
  }

  @Test
  public void testTableIdentifiersSnapshotIsACopy() {
    SchedulingPolicy policy =
        new SchedulingPolicy(new ResourceGroup.Builder("test", "local").build());
    DefaultTableRuntime table =
        tableRuntime("snapshot", 8L, OptimizingStatus.PENDING, 0L, 2L, 1L, 0L);
    policy.addTable(table);

    Set<ServerTableIdentifier> snapshot = policy.tableIdentifiersSnapshot();
    snapshot.clear();

    Assert.assertEquals(1, policy.tableIdentifiersSnapshot().size());
  }

  @Test
  public void testScheduleTablesDoesNotHoldTableLockWhileSorting() throws Exception {
    SchedulingPolicy policy =
        new SchedulingPolicy(new ResourceGroup.Builder("test", "local").build());
    DefaultTableRuntime blocking =
        tableRuntime("blocking", 9L, OptimizingStatus.PENDING, 0L, 2L, 1L, 0L);
    DefaultTableRuntime another =
        tableRuntime("another", 10L, OptimizingStatus.PENDING, 0L, 2L, 1L, 0L);
    DefaultTableRuntime added =
        tableRuntime("added", 11L, OptimizingStatus.PENDING, 0L, 2L, 1L, 0L);

    CountDownLatch sortingStarted = new CountDownLatch(1);
    CountDownLatch releaseSorting = new CountDownLatch(1);
    when(blocking.calculateQuotaOccupy())
        .thenAnswer(
            invocation -> {
              sortingStarted.countDown();
              Assert.assertTrue(releaseSorting.await(5, TimeUnit.SECONDS));
              return 0.1D;
            });
    when(another.calculateQuotaOccupy()).thenReturn(0.2D);
    when(added.calculateQuotaOccupy()).thenReturn(0.3D);

    policy.addTable(blocking);
    policy.addTable(another);

    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      Future<List<DefaultTableRuntime>> scheduled =
          executor.submit(() -> policy.scheduleTables(Collections.emptySet(), 2));

      Assert.assertTrue(sortingStarted.await(5, TimeUnit.SECONDS));

      ExecutorService addExecutor = Executors.newSingleThreadExecutor();
      try {
        Future<?> addFuture = addExecutor.submit(() -> policy.addTable(added));
        addFuture.get(1, TimeUnit.SECONDS);
      } finally {
        addExecutor.shutdownNow();
      }

      releaseSorting.countDown();
      Assert.assertEquals(2, scheduled.get(5, TimeUnit.SECONDS).size());
      Assert.assertTrue(policy.tableIdentifiersSnapshot().contains(added.getTableIdentifier()));
    } finally {
      releaseSorting.countDown();
      executor.shutdownNow();
    }
  }

  private DefaultTableRuntime tableRuntime(
      String name,
      long id,
      OptimizingStatus status,
      long lastPlanTime,
      long currentSnapshotId,
      long lastOptimizedSnapshotId,
      long minPlanInterval) {
    ServerTableIdentifier identifier =
        ServerTableIdentifier.of(TableIdentifier.of("catalog", "db", name), TableFormat.ICEBERG);
    identifier.setId(id);
    OptimizingConfig config = mock(OptimizingConfig.class);
    when(config.getMinPlanInterval()).thenReturn(minPlanInterval);

    DefaultTableRuntime runtime = mock(DefaultTableRuntime.class);
    when(runtime.getTableIdentifier()).thenReturn(identifier);
    when(runtime.getOptimizingStatus()).thenReturn(status);
    when(runtime.getLastPlanTime()).thenReturn(lastPlanTime);
    when(runtime.getCurrentSnapshotId()).thenReturn(currentSnapshotId);
    when(runtime.getLastOptimizedSnapshotId()).thenReturn(lastOptimizedSnapshotId);
    when(runtime.getCurrentChangeSnapshotId()).thenReturn(0L);
    when(runtime.getLastOptimizedChangeSnapshotId()).thenReturn(0L);
    when(runtime.getOptimizingConfig()).thenReturn(config);
    return runtime;
  }
}
