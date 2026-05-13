# TableRuntimeRefreshExecutor Multi-Format Scheduling Design

**Date:** 2026-04-20  
**Branch:** dev-paimon-compact  
**Scope:** Fix Paimon PENDING-state scheduling in `TableRuntimeRefreshExecutor`

---

## Background

### What already works

`PaimonTableRuntime` (added in this branch) overrides `DefaultTableRuntime.refresh(AmoroTable<?>)` and routes snapshot resolution through `AmoroTable.currentSnapshot()` + `Long.parseLong(snapshot.id())` instead of the Iceberg-specific `MixedTable` cast. `DefaultTableRuntimeFactory` already routes `TableFormat.PAIMON` to `PaimonTableRuntimeCreatorImpl`, which instantiates `PaimonTableRuntime`.

As a result, `defaultTableRuntime.refresh(table)` succeeds for Paimon — `currentSnapshotId` is correctly updated after each new Paimon snapshot.

### The live bug

`TableRuntimeRefreshExecutor.execute()` has a single hard-coded `MixedTable` cast that fires **after** the successful `refresh()` call:

```java
// line 157 — succeeds for Paimon (PaimonTableRuntime.refresh() override)
defaultTableRuntime.refresh(table);

// line 158 — ClassCastException for Paimon (AppendOnlyFileStoreTable ≠ MixedTable)
MixedTable mixedTable = (MixedTable) table.originalTable();
```

The `ClassCastException` is caught by the outer `catch (Throwable)` (line 184) and silently swallowed. Consequence: `tryEvaluatingPendingInput()` and `setPendingInput()` are never called for Paimon, so the table never transitions from IDLE to PENDING. `SchedulingPolicy.fillSkipSet()` always adds the Paimon table to the skip set, and planning never fires.

---

## Design Goals

1. Fix the single live cast in `TableRuntimeRefreshExecutor.execute()` with minimal blast radius
2. Mirror the structure of the existing `tryEvaluatingPendingInput()` for consistency
3. No new cross-module dependencies
4. Iceberg/MixedTable/MixedHive paths: zero changes

---

## Two-Level Evaluation Contract

```
TableRuntimeRefreshExecutor (lightweight, ~1 min period)
  └─ Paimon: snapshot ID comparison — O(1), no file I/O
       └─ new snapshot? → setPendingInput(empty) → IDLE → PENDING

OptimizingQueue.planInternal (fires when optimizer available)
  └─ PaimonOptimizingPlanner.isNecessary()
       └─ AppendCompactCoordinator.run() — scans files
       └─ false → completeEmptyProcess() → back to IDLE
       └─ true  → plan() → PaimonCompactionTask
```

`AppendCompactCoordinator.run()` is called **exactly once** per plan cycle in `planInternal`. It is never called in the refresh executor.

---

## Changes

### 1. `TableRuntimeRefreshExecutor.execute()` — add Paimon dispatch

**File:** `amoro-ams/src/main/java/org/apache/amoro/server/scheduler/inline/TableRuntimeRefreshExecutor.java`

Wrap the existing `MixedTable` cast in an `instanceof` guard; add a Paimon branch alongside it. The `snapshotChanged` semantics for Paimon match Iceberg: "has a new snapshot appeared since the last successful compaction commit?" (`lastOptimizedSnapshotId != getCurrentSnapshotId()` — both captured consistently at their post-`refresh()` values).

```java
@Override
public void execute(TableRuntime tableRuntime) {
    try {
        Preconditions.checkArgument(tableRuntime instanceof DefaultTableRuntime);
        DefaultTableRuntime defaultTableRuntime = (DefaultTableRuntime) tableRuntime;

        long lastOptimizedSnapshotId = defaultTableRuntime.getLastOptimizedSnapshotId();
        long lastOptimizedChangeSnapshotId = defaultTableRuntime.getLastOptimizedChangeSnapshotId();
        AmoroTable<?> table = loadTable(tableRuntime);
        defaultTableRuntime.refresh(table);   // PaimonTableRuntime.refresh() handles Paimon

        OptimizingConfig optimizingConfig = defaultTableRuntime.getOptimizingConfig();
        boolean tableSummaryOnly =
            !optimizingConfig.isEnabled() && optimizingConfig.isTableSummaryEnabled();
        boolean hasOptimizingDemand = false;

        if (table.originalTable() instanceof MixedTable) {
            // ── Iceberg / MixedIceberg / MixedHive path — unchanged ──
            MixedTable mixedTable = (MixedTable) table.originalTable();
            boolean snapshotChanged =
                (mixedTable.isKeyedTable()
                        && (lastOptimizedSnapshotId != defaultTableRuntime.getCurrentSnapshotId()
                            || lastOptimizedChangeSnapshotId
                                != defaultTableRuntime.getCurrentChangeSnapshotId()))
                    || (mixedTable.isUnkeyedTable()
                        && lastOptimizedSnapshotId != defaultTableRuntime.getCurrentSnapshotId());
            if (snapshotChanged) {
                hasOptimizingDemand = tryEvaluatingPendingInput(defaultTableRuntime, mixedTable);
            } else {
                logger.debug("{} optimizing is not necessary", defaultTableRuntime.getTableIdentifier());
            }
        } else if (table.format() == TableFormat.PAIMON) {
            // ── Paimon lightweight path ──
            boolean snapshotChanged =
                lastOptimizedSnapshotId != defaultTableRuntime.getCurrentSnapshotId();
            if (snapshotChanged) {
                hasOptimizingDemand = tryEvaluatingPendingInputForPaimon(defaultTableRuntime);
            } else {
                logger.debug("{} optimizing is not necessary", defaultTableRuntime.getTableIdentifier());
            }
        }

        // Adaptive interval update — existing logic, unchanged
        if (!tableSummaryOnly && optimizingConfig.isRefreshTableAdaptiveEnabled(interval)) {
            defaultTableRuntime.setLatestEvaluatedNeedOptimizing(hasOptimizingDemand);
            long newInterval = getAdaptiveExecutingInterval(defaultTableRuntime);
            defaultTableRuntime.setLatestRefreshInterval(newInterval);
        }
    } catch (Throwable throwable) {
        logger.error("Refreshing table {} failed.", tableRuntime.getTableIdentifier(), throwable);
    }
}
```

### 2. New `tryEvaluatingPendingInputForPaimon()`

Mirrors the structure of `tryEvaluatingPendingInput()` but without the `MixedTable` evaluator. `PendingInput` is constructed in the AMS layer so `amoro-format-paimon` has no dependency on `amoro-format-iceberg`.

**Note on table-summary-only mode:** When optimizing is disabled but `tableSummaryEnabled` is true, the Iceberg path collects file-level metrics via `IcebergTableUtil.createOptimizingEvaluator`. Paimon does not have an equivalent evaluator and intentionally skips this — no summary metrics are collected for Paimon in this mode. This is acceptable for the current scope and should be revisited when Paimon-native table summary metrics are implemented.

```java
private boolean tryEvaluatingPendingInputForPaimon(DefaultTableRuntime tableRuntime) {
    OptimizingConfig optimizingConfig = tableRuntime.getOptimizingConfig();
    if (!optimizingConfig.isEnabled()) {
        // Paimon does not collect table-summary metrics when optimizing is disabled
        return false;
    }
    if (!tableRuntime.getOptimizingStatus().equals(OptimizingStatus.IDLE)) {
        // Already in PENDING, PLANNING, or an optimizing/committing state — do not re-trigger
        return true;
    }
    // Snapshot change was already confirmed by the caller; no file scan needed here.
    // AppendCompactCoordinator.run() fires later in planInternal.
    tableRuntime.setPendingInput(new AbstractOptimizingEvaluator.PendingInput());
    logger.debug(
        "{} Paimon snapshot changed — marking PENDING",
        tableRuntime.getTableIdentifier());
    return true;
}
```

---

## File Change Summary

| File | Change |
|------|--------|
| `amoro-ams/.../scheduler/inline/TableRuntimeRefreshExecutor.java` | Guard `MixedTable` cast with `instanceof`; add Paimon branch in `execute()`; add `tryEvaluatingPendingInputForPaimon()`; add `import org.apache.amoro.TableFormat` |

`DefaultTableRuntime`, `PaimonTableRuntime`, and `DefaultTableRuntimeFactory` require **no changes** — snapshot refresh is already correctly handled by the existing `PaimonTableRuntime.refresh()` override.

---

## Invariants

- Iceberg/MixedTable/MixedHive paths: **zero changes**, original `tryEvaluatingPendingInput()` untouched
- `AppendCompactCoordinator.run()` called exactly once per plan cycle (inside `planInternal`)
- `amoro-format-paimon` gains no new dependency on `amoro-format-iceberg`
- `Long.parseLong(snapshot.id())` is safe for Paimon: `PaimonSnapshot.id()` always returns `String.valueOf(paimonSnapshot.id())` where the underlying type is `long`. This is an invariant of `PaimonSnapshot` and is already relied upon by `PaimonTableRuntime.resolveSnapshotId()`.
- `tableSummaryMetrics.refreshSnapshots()` is not called for Paimon (it is MixedTable-specific); Paimon summary metrics remain at initial values — acknowledged as a known gap for this scope.
- **No oscillation loop**: when `planInternal` finds nothing to compact (`PaimonOptimizingPlanner.isNecessary() == false`), it calls `tableRuntime.completeEmptyProcess()`, which advances `lastOptimizedSnapshotId` to `currentSnapshotId` (line 429 of `DefaultTableRuntime`). The next refresh cycle will therefore correctly find `lastOptimizedSnapshotId == currentSnapshotId` and skip PENDING.
- **Adaptive interval**: Paimon tables participate in the adaptive interval update block exactly like MixedTable tables — the outer `if (!tableSummaryOnly && ...)` block in `execute()` is not format-gated. `hasOptimizingDemand` is set correctly for both formats before that block runs.

---

## Required Tests

A new parameterized test case must be added to `TestTableRuntimeRefreshExecutor` covering the Paimon `AppendOnly` BUCKET_UNAWARE format:

- Assert: after a new Paimon snapshot appears, table transitions from IDLE to PENDING
- Assert: without a new snapshot, table remains IDLE
- Assert: with optimizing disabled, table remains IDLE regardless of snapshot change
- Assert: when already in PENDING status, a second refresh cycle returns `hasOptimizingDemand = true` without calling `setPendingInput()` again
- Assert: when in PLANNING or an active-optimizing status, the method returns `hasOptimizingDemand = true` without re-triggering a new pending transition
