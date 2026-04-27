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

package org.apache.amoro.formats.paimon;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.table.descriptor.OperationType;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.paimon.Snapshot;
import org.apache.paimon.table.FileStoreTable;
import org.apache.paimon.utils.BranchManager;
import org.apache.paimon.utils.SnapshotManager;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Pins down the total-count computation inside {@link PaimonTableDescriptor#getSnapshots}.
 *
 * <p>Before JSTP-2356 the method computed {@code total = latestSnapshotId() - earliestSnapshotId()}
 * which had three distinct bugs:
 *
 * <ul>
 *   <li>NPE on a freshly-created table because {@code latestSnapshotId()} returns {@code null}
 *       there — the unboxed subtraction crashed before any empty-table early-return could fire.
 *   <li>Off-by-one: the subtraction is a range width, not a count, so it reported {@code count - 1}
 *       even on non-empty tables.
 *   <li>Ignored {@code operationType}: the subtraction never looked at commit kinds, so OPTIMIZING
 *       / NON_OPTIMIZING pagination totals were identical to the unfiltered total.
 * </ul>
 *
 * <p>The fix short-circuits on {@code latestSnapshotId() == null}, uses Paimon's O(1) {@code
 * SnapshotManager#snapshotCount()} for the unfiltered {@code ALL} path (directory listing only, no
 * JSON reads), and iterates {@code SnapshotManager#snapshots()} to apply the same predicate the
 * paginator uses for {@code OPTIMIZING} / {@code NON_OPTIMIZING}. These tests lock each of those
 * three behaviours independently and use Mockito {@code verify} to prove the cheap path is actually
 * taken for the common {@code ALL} query.
 */
class TestPaimonTableDescriptorSnapshotPagination {

  private static final String MAIN_BRANCH = "main";

  /**
   * Wires an {@link AmoroTable} whose {@code originalTable()} is a {@link FileStoreTable} whose
   * {@code snapshotManager().copyWithBranch(main)} returns the supplied mock. The branch-exists
   * check is stubbed to {@code false} so {@link BranchManager#isMainBranch} decides the path.
   */
  private static AmoroTable<?> mockAmoroTable(SnapshotManager snapshotManager) {
    FileStoreTable table = mock(FileStoreTable.class);
    SnapshotManager base = mock(SnapshotManager.class);
    when(base.copyWithBranch(MAIN_BRANCH)).thenReturn(snapshotManager);
    when(table.snapshotManager()).thenReturn(base);

    BranchManager branchManager = mock(BranchManager.class);
    when(branchManager.branchExists(any())).thenReturn(false);
    when(table.branchManager()).thenReturn(branchManager);

    AmoroTable<?> amoroTable = mock(AmoroTable.class);
    when(((AmoroTable) amoroTable).originalTable()).thenReturn(table);
    return amoroTable;
  }

  /**
   * {@link PaimonTableDescriptor#getSnapshots} shells every per-snapshot detail load onto an {@link
   * java.util.concurrent.ExecutorService} that is injected via {@link
   * PaimonTableDescriptor#withIoExecutor}. Without wiring one up, {@code CompletableFuture
   * .supplyAsync(task, null)} NPEs before we ever reach the pagination return. We only exercise the
   * non-empty happy paths here, so a same-thread direct executor is sufficient.
   */
  private static PaimonTableDescriptor newDescriptorWithExecutor() {
    PaimonTableDescriptor descriptor = new PaimonTableDescriptor();
    descriptor.withIoExecutor(Executors.newSingleThreadExecutor());
    return descriptor;
  }

  private static Snapshot snapshotWithKind(long id, Snapshot.CommitKind kind) {
    Snapshot s = mock(Snapshot.class);
    when(s.id()).thenReturn(id);
    when(s.commitKind()).thenReturn(kind);
    return s;
  }

  /**
   * These tests only care about the {@code total} computation. Arming {@code snapshotsWithinRange}
   * with an empty iterator short-circuits the downstream pagination loop so we never reach {@code
   * table.store()} (which would need its own deep mock stack) or the per-snapshot {@link
   * java.util.concurrent.CompletableFuture} fan-out. The pagination still terminates cleanly and
   * returns {@code (emptyList, total)} — exactly what we want to assert on.
   */
  private static void armEmptyPagination(SnapshotManager manager) {
    when(manager.snapshotsWithinRange(any(), any()))
        .thenAnswer(inv -> new ArrayList<Snapshot>().iterator());
  }

  @Test
  void emptyTableReturnsZeroWithoutNpe() throws Exception {
    // latestSnapshotId() == null simulates a brand-new table. The old code NPE'd here
    // because it unboxed the null. The fix must return (empty, 0L) before touching
    // any of the counting APIs, so snapshotCount() / snapshots() must NOT be invoked.
    SnapshotManager manager = mock(SnapshotManager.class);
    when(manager.latestSnapshotId()).thenReturn(null);
    AmoroTable<?> amoroTable = mockAmoroTable(manager);

    PaimonTableDescriptor descriptor = new PaimonTableDescriptor();
    Pair<? extends List<?>, Long> result =
        assertDoesNotThrow(
            () -> descriptor.getSnapshots(amoroTable, MAIN_BRANCH, OperationType.ALL, 10, 0, null));

    assertTrue(result.getLeft().isEmpty(), "empty table must yield empty list");
    assertEquals(0L, result.getRight(), "empty table must yield zero total");
    verify(manager, never()).snapshotCount();
    verify(manager, never()).snapshots();
  }

  @Test
  void allOperationTypeUsesSnapshotCount() throws Exception {
    // operationType == ALL must hit the cheap kernel path. We prove this by verifying
    // snapshotCount() is called exactly once and snapshots() is never called — the
    // latter would indicate a full iteration which the ALL path must not do.
    // Build snapshot mocks BEFORE any when(manager.xxx()) stubbing so the inner when(s.id())
    // stubs don't interleave with the outer stubbing on manager (Mockito UnfinishedStubbing).
    List<Snapshot> all = new ArrayList<>();
    for (long id = 1; id <= 10; id++) {
      all.add(snapshotWithKind(id, Snapshot.CommitKind.APPEND));
    }
    Snapshot earliest = snapshotWithKind(1L, Snapshot.CommitKind.APPEND);

    SnapshotManager manager = mock(SnapshotManager.class);
    when(manager.latestSnapshotId()).thenReturn(10L);
    when(manager.earliestSnapshot()).thenReturn(earliest);
    when(manager.snapshotCount()).thenReturn(10L);
    armEmptyPagination(manager);

    AmoroTable<?> amoroTable = mockAmoroTable(manager);
    PaimonTableDescriptor descriptor = newDescriptorWithExecutor();

    Pair<?, Long> result =
        descriptor.getSnapshots(amoroTable, MAIN_BRANCH, OperationType.ALL, 5, 0, null);

    assertEquals(10L, result.getRight(), "ALL path must report snapshotCount() verbatim");
    verify(manager, times(1)).snapshotCount();
    verify(manager, never()).snapshots();
  }

  @Test
  void filteredOperationTypeIteratesSnapshots() throws Exception {
    // operationType != ALL has no kernel shortcut; the fix must iterate snapshots()
    // and apply the same predicate the paginator uses. We assemble 5 APPEND + 5
    // COMPACT snapshots and expect OPTIMIZING (COMPACT) to surface exactly 5.
    List<Snapshot> all = new ArrayList<>();
    for (long id = 1; id <= 5; id++) {
      all.add(snapshotWithKind(id, Snapshot.CommitKind.APPEND));
    }
    for (long id = 6; id <= 10; id++) {
      all.add(snapshotWithKind(id, Snapshot.CommitKind.COMPACT));
    }
    Snapshot earliest = snapshotWithKind(1L, Snapshot.CommitKind.APPEND);

    SnapshotManager manager = mock(SnapshotManager.class);
    when(manager.latestSnapshotId()).thenReturn(10L);
    when(manager.earliestSnapshot()).thenReturn(earliest);
    when(manager.snapshots()).thenReturn(all.iterator());
    armEmptyPagination(manager);

    AmoroTable<?> amoroTable = mockAmoroTable(manager);
    PaimonTableDescriptor descriptor = newDescriptorWithExecutor();

    Pair<?, Long> result =
        descriptor.getSnapshots(amoroTable, MAIN_BRANCH, OperationType.OPTIMIZING, 5, 0, null);

    assertEquals(
        5L,
        result.getRight(),
        "OPTIMIZING must count only COMPACT commits (5 of 10), not the full set");
    verify(manager, times(1)).snapshots();
    verify(manager, never()).snapshotCount();
  }

  @Test
  void snapshotCountIsOffByOneFromRange() throws Exception {
    // Regression guard for the off-by-one: latest=10, earliest=1 yielded total=9
    // under the old arithmetic (range width, not element count). snapshotCount()=10
    // is the correct answer because both endpoints are inclusive in the count.
    List<Snapshot> all = new ArrayList<>();
    for (long id = 1; id <= 10; id++) {
      all.add(snapshotWithKind(id, Snapshot.CommitKind.APPEND));
    }
    Snapshot earliest = snapshotWithKind(1L, Snapshot.CommitKind.APPEND);

    SnapshotManager manager = mock(SnapshotManager.class);
    when(manager.latestSnapshotId()).thenReturn(10L);
    when(manager.earliestSnapshotId()).thenReturn(1L);
    when(manager.earliestSnapshot()).thenReturn(earliest);
    when(manager.snapshotCount()).thenReturn(10L);
    armEmptyPagination(manager);

    AmoroTable<?> amoroTable = mockAmoroTable(manager);
    PaimonTableDescriptor descriptor = newDescriptorWithExecutor();

    Pair<?, Long> result =
        descriptor.getSnapshots(amoroTable, MAIN_BRANCH, OperationType.ALL, 5, 0, null);

    assertEquals(
        10L,
        result.getRight(),
        "fix must report 10 (snapshotCount), not 9 (latest - earliest range width)");
  }
}
