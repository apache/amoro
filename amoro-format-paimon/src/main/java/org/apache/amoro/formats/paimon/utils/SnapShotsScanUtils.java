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

package org.apache.amoro.formats.paimon.utils;

import org.apache.amoro.table.descriptor.OperationType;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.paimon.Snapshot;
import org.apache.paimon.utils.SnapshotManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class SnapShotsScanUtils {

  private static final Logger LOG = LoggerFactory.getLogger(SnapShotsScanUtils.class);

  private static final int DEFAULT_BOUNDED_SCAN_MULTIPLIER = 1;

  /**
   * Get snapshots with cursor-based pagination to avoid full scan. Uses smart scanning to only scan
   * the necessary ranges for the requested page. Supports cursor-based pagination with
   * lastSnapshotId.
   *
   * @param snapshotManager the snapshot manager
   * @param commitKind the commit kind to filter (null for all kinds)
   * @param limit the maximum number of snapshots to return
   * @param offset the number of snapshots to skip
   * @param lastSnapshotId the last snapshot ID from previous page (null for first page)
   * @return Pair of (snapshot list, total count matching the filter)
   */
  public static Pair<List<Snapshot>, Integer> getSnapshotsWithPagination(
      SnapshotManager snapshotManager,
      Snapshot.CommitKind commitKind,
      int limit,
      int offset,
      Long lastSnapshotId) {

    Predicate<Snapshot> filter = commitKind == null ? s -> true : s -> s.commitKind() == commitKind;

    return scanWithPagination(snapshotManager, filter, limit, offset, lastSnapshotId, "page");
  }

  /**
   * Get snapshots with cursor-based pagination for general snapshot queries. This method handles
   * different operation types and uses smart scanning to avoid full table scans.
   *
   * @param snapshotManager the snapshot manager
   * @param commitKind the commit kind to filter (null for all kinds)
   * @param operationType the operation type to filter
   * @param limit the maximum number of snapshots to return
   * @param offset the number of snapshots to skip
   * @param lastSnapshotId the last snapshot ID from previous page (null for first page)
   * @return Pair of (snapshot list, total count matching the filter)
   */
  public static Pair<List<Snapshot>, Integer> getSnapshotsWithPaginationForGeneral(
      SnapshotManager snapshotManager,
      Snapshot.CommitKind commitKind,
      OperationType operationType,
      int limit,
      int offset,
      Long lastSnapshotId) {

    Predicate<Snapshot> filter = createOperationTypeFilter(operationType);

    return scanWithPagination(
        snapshotManager, filter, limit, offset, lastSnapshotId, "general page");
  }

  /**
   * Returns a bounded newest-first page without scanning the whole snapshot history.
   *
   * <p>The returned total is intentionally an upper-bound estimate. Exact filtered totals require
   * scanning every snapshot, which this helper is designed to avoid. When older snapshots exist
   * outside the scanned window, the total includes one sentinel row beyond the current page so the
   * UI can keep next-page navigation available.
   */
  public static Pair<List<Snapshot>, Integer> getSnapshotsWithBoundedPagination(
      SnapshotManager snapshotManager,
      Predicate<Snapshot> filter,
      int limit,
      int offset,
      Long lastSnapshotId) {
    if (limit <= 0) {
      return Pair.of(Collections.emptyList(), 0);
    }

    Long earliestSnapshotId = snapshotManager.earliestSnapshotId();
    Long latestSnapshotId = snapshotManager.latestSnapshotId();
    if (earliestSnapshotId == null || latestSnapshotId == null) {
      return Pair.of(Collections.emptyList(), 0);
    }

    long requestedStart =
        lastSnapshotId == null ? latestSnapshotId - Math.max(offset, 0) : lastSnapshotId - 1;
    if (requestedStart < earliestSnapshotId) {
      return Pair.of(Collections.emptyList(), Math.max(offset, 0));
    }
    long currentStart = Math.min(latestSnapshotId, requestedStart);

    int scanLimit = Math.max(limit, limit * DEFAULT_BOUNDED_SCAN_MULTIPLIER);
    long minSnapshotId = Math.max(earliestSnapshotId, currentStart - scanLimit + 1L);
    Iterator<Snapshot> rangeSnapshots =
        snapshotManager.snapshotsWithinRange(Optional.of(currentStart), Optional.of(minSnapshotId));

    List<Snapshot> matched = new ArrayList<>();
    while (rangeSnapshots.hasNext()) {
      Snapshot snapshot = rangeSnapshots.next();
      if (filter.test(snapshot)) {
        matched.add(snapshot);
      }
    }
    matched.sort((s1, s2) -> Long.compare(s2.id(), s1.id()));

    List<Snapshot> page =
        matched.size() <= limit ? matched : new ArrayList<>(matched.subList(0, limit));
    boolean mayHaveOlderSnapshots = minSnapshotId > earliestSnapshotId;
    int estimatedTotal = Math.max(offset, 0) + page.size() + (mayHaveOlderSnapshots ? 1 : 0);

    LOG.debug(
        "Bounded snapshot pagination scanned [{}, {}], matched {}, returned {}, estimatedTotal {}",
        minSnapshotId,
        currentStart,
        matched.size(),
        page.size(),
        estimatedTotal);
    return Pair.of(page, estimatedTotal);
  }

  /**
   * Creates a predicate filter based on the operation type.
   *
   * @param operationType the operation type to filter
   * @return a predicate that filters snapshots by operation type
   */
  private static Predicate<Snapshot> createOperationTypeFilter(OperationType operationType) {
    if (operationType == OperationType.ALL) {
      return s -> true;
    } else if (operationType == OperationType.OPTIMIZING) {
      return s -> s.commitKind() == Snapshot.CommitKind.COMPACT;
    } else {
      return s -> s.commitKind() != Snapshot.CommitKind.COMPACT;
    }
  }

  /**
   * Unified pagination method that scans snapshots with the given filter predicate.
   *
   * @param snapshotManager the snapshot manager
   * @param filter the predicate to filter snapshots
   * @param limit the maximum number of snapshots to return
   * @param offset the number of snapshots to skip
   * @param lastSnapshotId the last snapshot ID from previous page (null for first page)
   * @param logPrefix prefix for log messages
   * @return Pair of (snapshot list, total count matching the filter)
   */
  private static Pair<List<Snapshot>, Integer> scanWithPagination(
      SnapshotManager snapshotManager,
      Predicate<Snapshot> filter,
      int limit,
      int offset,
      Long lastSnapshotId,
      String logPrefix) {

    Long earliestSnapshotId = snapshotManager.earliestSnapshot().id();
    Long latestSnapshotId = snapshotManager.latestSnapshotId();

    if (earliestSnapshotId == null || latestSnapshotId == null) {
      return Pair.of(Collections.emptyList(), 0);
    }

    // Determine the starting point based on cursor or offset
    long currentStart =
        lastSnapshotId != null
            ? lastSnapshotId // Cursor-based: start from lastSnapshotId
            : latestSnapshotId; // Offset-based: start from the latest

    // For cursor-based pagination, we can be more precise about the range to scan
    int targetRange =
        lastSnapshotId != null
            ? limit + Math.min(offset, 5) // Small buffer for cursor-based
            : limit + Math.min(offset, limit); // Conservative for first page

    LOG.info(
        "Starting {} pagination - lastSnapshotId:{}, currentStart:{}, targetRange:{}, limit:{}, offset:{}",
        logPrefix,
        lastSnapshotId,
        currentStart,
        targetRange,
        limit,
        offset);

    // Create pagination context and scan
    PaginationContext context =
        new PaginationContext(
            limit,
            earliestSnapshotId,
            lastSnapshotId != null,
            offset,
            currentStart,
            targetRange,
            latestSnapshotId);

    return scanSnapshots(snapshotManager, filter, context, logPrefix);
  }

  /**
   * Iterative scanning method that replaces the recursive implementation. Scans from newest to
   * oldest, stops when enough results are found.
   *
   * @param snapshotManager the snapshot manager
   * @param filter the predicate to filter snapshots
   * @param context the pagination context containing mutable state
   * @param logPrefix prefix for log messages
   * @return Pair of (snapshot list, total count matching the filter)
   */
  private static Pair<List<Snapshot>, Integer> scanSnapshots(
      SnapshotManager snapshotManager,
      Predicate<Snapshot> filter,
      PaginationContext context,
      String logPrefix) {

    while (context.currentStart >= context.earliestSnapshotId) {
      // Check termination: have enough results and don't need counting
      if (context.accumulatedSnapshots.size() >= context.limit && !context.needCounting) {
        break;
      }

      // Calculate smart scan range
      long rangeEnd =
          Math.max(context.earliestSnapshotId, context.currentStart - context.scanRange + 1);

      // Ensure we don't exceed the valid snapshot ID range
      long maxSnapshotId = Math.min(context.currentStart, context.latestSnapshotId);
      long minSnapshotId = Math.max(rangeEnd, context.earliestSnapshotId);

      // Safety check: ensure maxSnapshotId >= minSnapshotId
      if (maxSnapshotId < minSnapshotId) {
        LOG.info(
            "Safety check failed - maxSnapshotId:{} < minSnapshotId:{}",
            maxSnapshotId,
            minSnapshotId);
        break;
      }

      LOG.info(
          "Smart scanning for {} - mode:{}, minSnapshotId:{}, maxSnapshotId:{}, needCounting:{}",
          logPrefix,
          context.isCursorBased ? "CURSOR" : "OFFSET",
          minSnapshotId,
          maxSnapshotId,
          context.needCounting);

      // Get snapshots in current range
      Iterator<Snapshot> rangeSnapshots =
          snapshotManager.snapshotsWithinRange(
              Optional.of(maxSnapshotId), Optional.of(minSnapshotId));

      LOG.info(
          "Querying snapshotsWithinRange - maxSnapshotId:{}, minSnapshotId:{}",
          maxSnapshotId,
          minSnapshotId);

      // Collect and sort snapshots in this range
      List<Snapshot> snapshotsInRange = new ArrayList<>();
      while (rangeSnapshots.hasNext()) {
        snapshotsInRange.add(rangeSnapshots.next());
      }

      // Sort in descending order (newest first)
      if (!snapshotsInRange.isEmpty()) {
        snapshotsInRange.sort((s1, s2) -> Long.compare(s2.id(), s1.id()));
      }

      // Process snapshots in this range
      int rangeMatchingCount = 0;
      List<Snapshot> rangeResultSnapshots = new ArrayList<>();
      int rangeSkippedCount = 0;

      for (Snapshot snapshot : snapshotsInRange) {
        if (filter.test(snapshot)) {
          rangeMatchingCount++;

          if (context.remainingOffset > 0) {
            rangeSkippedCount++;
          } else if (context.accumulatedSnapshots.size() < context.limit) {
            rangeResultSnapshots.add(snapshot);
          }
        }
      }

      LOG.info(
          "Processed range - snapshotsInRange:{}, rangeMatchingCount:{}, rangeResultSnapshots:{}, rangeSkippedCount:{}",
          snapshotsInRange.size(),
          rangeMatchingCount,
          rangeResultSnapshots.size(),
          rangeSkippedCount);

      // Update accumulated results
      context.accumulatedSnapshots.addAll(rangeResultSnapshots);
      context.accumulatedCount += rangeMatchingCount;
      context.remainingOffset = Math.max(0, context.remainingOffset - rangeSkippedCount);

      // Check if we should stop
      boolean hasEnoughResults = context.accumulatedSnapshots.size() >= context.limit;

      // Determine if we should continue counting
      boolean shouldStopCounting =
          hasEnoughResults
              && (context.isCursorBased
                  || rangeEnd <= context.earliestSnapshotId
                  || context.remainingOffset == 0);

      if (shouldStopCounting) {
        context.needCounting = false;
      }

      // Early termination if we have enough results and don't need counting
      if (hasEnoughResults && !context.needCounting) {
        LOG.info(
            "Early termination - found enough results: {}, stopped at rangeEnd: {}",
            context.accumulatedSnapshots.size(),
            rangeEnd);
        break;
      }

      // Update scan range for next iteration
      if (!hasEnoughResults && !rangeResultSnapshots.isEmpty()) {
        // Found some results but not enough
        context.scanRange =
            context.isCursorBased
                ? Math.min(context.scanRange * 2, context.limit + 10)
                : Math.min(context.scanRange * 2, context.limit * 2);
      } else if (rangeResultSnapshots.isEmpty()) {
        // No results in this range, skip ahead more aggressively
        context.scanRange = Math.min(context.scanRange * 2, context.limit * 3);
      }

      // Move to the next range
      context.currentStart = rangeEnd - 1;
    }

    return Pair.of(context.accumulatedSnapshots, context.accumulatedCount);
  }

  /** Encapsulates pagination state to reduce parameter count and improve readability. */
  private static class PaginationContext {
    final int limit;
    final long earliestSnapshotId;
    final boolean isCursorBased;
    final long latestSnapshotId;

    // Mutable state
    int remainingOffset;
    long currentStart;
    int scanRange;
    List<Snapshot> accumulatedSnapshots;
    int accumulatedCount;
    boolean needCounting;

    PaginationContext(
        int limit,
        long earliestSnapshotId,
        boolean isCursorBased,
        int remainingOffset,
        long currentStart,
        int scanRange,
        long latestSnapshotId) {
      this.limit = limit;
      this.earliestSnapshotId = earliestSnapshotId;
      this.isCursorBased = isCursorBased;
      this.latestSnapshotId = latestSnapshotId;
      this.remainingOffset = remainingOffset;
      this.currentStart = currentStart;
      this.scanRange = scanRange;
      this.accumulatedSnapshots = new ArrayList<>();
      this.accumulatedCount = 0;
      this.needCounting = true;
    }
  }
}
