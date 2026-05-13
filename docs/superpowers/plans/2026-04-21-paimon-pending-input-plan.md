# PaimonPendingInput Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Populate Paimon `pending_input` state with real file metrics (counts, sizes, health score) instead of empty JSON, using a Template Method pattern to keep format-specific logic in `PaimonTableRuntime`.

**Architecture:** Add a `PaimonPendingInput` data class in `amoro-format-paimon` with 9 fields + health score. `DefaultTableRuntime` gains a protected template method `evaluatePendingInputAndTransition(AmoroTable<?>)` (default: `return false`). `PaimonTableRuntime` overrides it to perform `FileStoreScan`, compute health score, and persist via `PAIMON_PENDING_INPUT_KEY`. `TableRuntimeRefreshExecutor` simplifies its Paimon branch to call the template method and deletes `tryEvaluatingPendingInputForPaimon()`.

**Spec:** `docs/superpowers/specs/2026-04-21-paimon-pending-input-design.md`

---

## File Structure

| File | Responsibility | Status |
|------|---------------|--------|
| `amoro-format-paimon/.../optimizing/PaimonPendingInput.java` | Data class: 9 fields + healthScore, Jackson-serializable | **New** |
| `amoro-ams/.../table/DefaultTableRuntime.java` | Add `evaluatePendingInputAndTransition()` template method; change `optimizingProcess` to `protected` | **Modify** |
| `amoro-ams/.../table/paimon/PaimonTableRuntime.java` | Override template method + lifecycle methods; add `collectPaimonPendingInput`, health score computation | **Modify** |
| `amoro-ams/.../table/DefaultTableRuntimeFactory.java` | `PaimonTableRuntimeCreatorImpl.requiredStateKeys()` uses `PaimonTableRuntime.PAIMON_PENDING_INPUT_KEY` | **Modify** |
| `amoro-ams/.../scheduler/inline/TableRuntimeRefreshExecutor.java` | Simplify Paimon branch; delete `tryEvaluatingPendingInputForPaimon()` | **Modify** |
| `amoro-format-paimon/.../optimizing/TestPaimonPendingInput.java` | Unit tests for serialization, health score computation | **New** |
| `amoro-ams/.../table/paimon/TestPaimonTableRuntimePendingInput.java` | Unit tests for template method dispatch, state persistence | **New** |

**Note:** `amoro-ams/pom.xml` already has `amoro-format-paimon` as compile dependency (lines 400-404). No module dependency change needed.

---

### Task 1: PaimonPendingInput Data Class

**Files:**
- Create: `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/PaimonPendingInput.java`
- Create: `amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/TestPaimonPendingInput.java`

- [ ] **Step 1: Write the failing test — serialization round-trip**

```java
// amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/TestPaimonPendingInput.java
package org.apache.amoro.formats.paimon.optimizing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

public class TestPaimonPendingInput {

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  public void serializationRoundTrip() throws Exception {
    PaimonPendingInput input =
        new PaimonPendingInput(
            47, 2147483648L, 5600000L,
            23, 536870912L, 3,
            0, 0L,
            51);

    String json = mapper.writeValueAsString(input);
    PaimonPendingInput deserialized = mapper.readValue(json, PaimonPendingInput.class);

    assertEquals(input.getDataFileCount(), deserialized.getDataFileCount());
    assertEquals(input.getDataFileSize(), deserialized.getDataFileSize());
    assertEquals(input.getDataRecordCount(), deserialized.getDataRecordCount());
    assertEquals(input.getSmallFileCount(), deserialized.getSmallFileCount());
    assertEquals(input.getSmallFileSize(), deserialized.getSmallFileSize());
    assertEquals(input.getPartitionCount(), deserialized.getPartitionCount());
    assertEquals(input.getFileWithDeleteCount(), deserialized.getFileWithDeleteCount());
    assertEquals(input.getDeleteRecordCount(), deserialized.getDeleteRecordCount());
    assertEquals(input.getHealthScore(), deserialized.getHealthScore());
  }

  @Test
  public void emptyJsonDeserializesToDefaults() throws Exception {
    String emptyJson = "{}";
    PaimonPendingInput result = mapper.readValue(emptyJson, PaimonPendingInput.class);

    assertEquals(0, result.getDataFileCount());
    assertEquals(0L, result.getDataFileSize());
    assertEquals(0, result.getHealthScore());
  }

  @Test
  public void extraFieldsIgnored() throws Exception {
    String jsonWithExtra = "{\"dataFileCount\":10,\"unknownField\":42}";
    PaimonPendingInput result = mapper.readValue(jsonWithExtra, PaimonPendingInput.class);

    assertEquals(10, result.getDataFileCount());
  }
}
```

Run: `cd /Users/SL/javaProject/amoro && ./mvnw test -pl amoro-format-paimon -Dtest=TestPaimonPendingInput -DfailIfNoTests=false -Psupport-paimon-format`
Expected: FAIL (class not found)

- [ ] **Step 2: Write PaimonPendingInput implementation**

```java
// amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/PaimonPendingInput.java
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

package org.apache.amoro.formats.paimon.optimizing;

/** Paimon-specific pending input metrics collected during the refresh phase. */
public class PaimonPendingInput {

  // ---- Workload dimension ----
  private int dataFileCount;
  private long dataFileSize;
  private long dataRecordCount;

  // ---- Urgency dimension ----
  private int smallFileCount;
  private long smallFileSize;
  private int partitionCount;

  // ---- Delete vector dimension ----
  private int fileWithDeleteCount;
  private long deleteRecordCount;

  // ---- Health score ----
  private int healthScore;

  public PaimonPendingInput() {}

  public PaimonPendingInput(
      int dataFileCount,
      long dataFileSize,
      long dataRecordCount,
      int smallFileCount,
      long smallFileSize,
      int partitionCount,
      int fileWithDeleteCount,
      long deleteRecordCount,
      int healthScore) {
    this.dataFileCount = dataFileCount;
    this.dataFileSize = dataFileSize;
    this.dataRecordCount = dataRecordCount;
    this.smallFileCount = smallFileCount;
    this.smallFileSize = smallFileSize;
    this.partitionCount = partitionCount;
    this.fileWithDeleteCount = fileWithDeleteCount;
    this.deleteRecordCount = deleteRecordCount;
    this.healthScore = healthScore;
  }

  public int getDataFileCount() {
    return dataFileCount;
  }

  public void setDataFileCount(int dataFileCount) {
    this.dataFileCount = dataFileCount;
  }

  public long getDataFileSize() {
    return dataFileSize;
  }

  public void setDataFileSize(long dataFileSize) {
    this.dataFileSize = dataFileSize;
  }

  public long getDataRecordCount() {
    return dataRecordCount;
  }

  public void setDataRecordCount(long dataRecordCount) {
    this.dataRecordCount = dataRecordCount;
  }

  public int getSmallFileCount() {
    return smallFileCount;
  }

  public void setSmallFileCount(int smallFileCount) {
    this.smallFileCount = smallFileCount;
  }

  public long getSmallFileSize() {
    return smallFileSize;
  }

  public void setSmallFileSize(long smallFileSize) {
    this.smallFileSize = smallFileSize;
  }

  public int getPartitionCount() {
    return partitionCount;
  }

  public void setPartitionCount(int partitionCount) {
    this.partitionCount = partitionCount;
  }

  public int getFileWithDeleteCount() {
    return fileWithDeleteCount;
  }

  public void setFileWithDeleteCount(int fileWithDeleteCount) {
    this.fileWithDeleteCount = fileWithDeleteCount;
  }

  public long getDeleteRecordCount() {
    return deleteRecordCount;
  }

  public void setDeleteRecordCount(long deleteRecordCount) {
    this.deleteRecordCount = deleteRecordCount;
  }

  public int getHealthScore() {
    return healthScore;
  }

  public void setHealthScore(int healthScore) {
    this.healthScore = healthScore;
  }
}
```

- [ ] **Step 3: Run tests to verify they pass**

Run: `cd /Users/SL/javaProject/amoro && ./mvnw test -pl amoro-format-paimon -Dtest=TestPaimonPendingInput -Psupport-paimon-format`
Expected: 3 tests PASS

- [ ] **Step 4: Write failing health score tests**

Add to `TestPaimonPendingInput.java`:

```java
  @Test
  public void healthScore_emptyTable_is100() {
    PaimonPendingInput input = computeHealthScoreInput(0, 0, 0, 0, 0, Map.of());
    assertEquals(100, input.getHealthScore());
  }

  @Test
  public void healthScore_noSmallFiles_is100() {
    // 10 files, 0 small files, 1 partition, 0 deletes
    Map<BinaryRow, Integer> partitions = Map.of(mockPartition(), 10);
    PaimonPendingInput input = computeHealthScoreInput(10, 0, 1_000_000L, 0, partitions);
    assertEquals(100, input.getHealthScore());
  }

  @Test
  public void healthScore_halfSmallFiles_scoresBelow60() {
    // 20 files, 10 small, 1 partition, 0 deletes
    // smallFileScore = 100 * (1 - 10/20) = 50
    // healthScore = 50 * 0.6 + 100 * 0.2 + 100 * 0.2 = 30 + 20 + 20 = 70
    Map<BinaryRow, Integer> partitions = Map.of(mockPartition(), 20);
    PaimonPendingInput input = computeHealthScoreInput(20, 10, 1_000_000L, 0, partitions);
    assertEquals(70, input.getHealthScore());
  }

  @Test
  public void healthScore_allSmallFiles_scores40() {
    // 20 files, all small, 1 partition, 0 deletes
    // smallFileScore = 0, healthScore = 0*0.6 + 100*0.2 + 100*0.2 = 40
    Map<BinaryRow, Integer> partitions = Map.of(mockPartition(), 20);
    PaimonPendingInput input = computeHealthScoreInput(20, 20, 1_000_000L, 0, partitions);
    assertEquals(40, input.getHealthScore());
  }

  private BinaryRow mockPartition() {
    return new BinaryRow(0);
  }

  // Helper that mirrors the static computeHealthScore logic
  private PaimonPendingInput computeHealthScoreInput(
      int dataFileCount, int smallFileCount, long dataRecordCount, long deleteRecordCount,
      Map<BinaryRow, Integer> partitionFiles) {
    int healthScore = PaimonPendingInput.computeHealthScore(
        dataFileCount, smallFileCount, dataRecordCount, deleteRecordCount, partitionFiles);
    return new PaimonPendingInput(
        dataFileCount, 0, dataRecordCount, smallFileCount, 0, partitionFiles.size(),
        0, deleteRecordCount, healthScore);
  }
```

Run: `./mvnw test -pl amoro-format-paimon -Dtest=TestPaimonPendingInput -Psupport-paimon-format`
Expected: FAIL — `computeHealthScore` static method doesn't exist yet

- [ ] **Step 5: Add static `computeHealthScore` method to PaimonPendingInput**

Add the following static method to `PaimonPendingInput.java`:

```java
  /**
   * Compute a composite health score (0-100, 100 = fully healthy).
   *
   * <p>Formula: {@code smallFileScore * 0.60 + deleteScore * 0.20 + distributionScore * 0.20}
   *
   * <p>Edge cases: empty table (dataFileCount == 0 or partitionCount == 0) returns 100.
   */
  public static int computeHealthScore(
      int dataFileCount,
      int smallFileCount,
      long dataRecordCount,
      long deleteRecordCount,
      java.util.Map<?, ? extends java.util.Collection<?>> partitionFiles) {
    if (dataFileCount == 0 || partitionFiles.isEmpty()) {
      return 100;
    }

    // smallFileScore (weight 60%): fraction of files that are NOT small
    double smallFileScore = 100.0 * (1.0 - (double) smallFileCount / dataFileCount);

    // deleteScore (weight 20%): fraction of records NOT deleted
    long totalRecords = Math.max(dataRecordCount, 1);
    double deleteScore = 100.0 * (1.0 - (double) deleteRecordCount / totalRecords);

    // distributionScore (weight 20%): evenness of file distribution across partitions
    int partitionCount = partitionFiles.size();
    double avgFilesPerPartition = (double) dataFileCount / partitionCount;
    double variance = 0.0;
    for (java.util.Collection<?> files : partitionFiles.values()) {
      double diff = files.size() - avgFilesPerPartition;
      variance += diff * diff;
    }
    variance /= partitionCount;
    double stdDev = Math.sqrt(variance);
    double distributionScore =
        100.0 * (1.0 - Math.min(stdDev / Math.max(avgFilesPerPartition, 1.0), 1.0));

    return (int) Math.round(
        smallFileScore * 0.60 + deleteScore * 0.20 + distributionScore * 0.20);
  }
```

**Important:** The method signature uses `java.util.Map<?, ? extends java.util.Collection<?>>` for flexibility — callers can pass `Map<BinaryRow, List<DataFileMeta>>` directly.

- [ ] **Step 6: Run all PaimonPendingInput tests**

Run: `./mvnw test -pl amoro-format-paimon -Dtest=TestPaimonPendingInput -Psupport-paimon-format`
Expected: 7 tests PASS

- [ ] **Step 7: Format and commit**

```bash
./mvnw spotless:apply
git add amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/PaimonPendingInput.java \
        amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/TestPaimonPendingInput.java
git commit -m "feat(paimon): add PaimonPendingInput data class with health score computation"
```

---

### Task 2: DefaultTableRuntime Template Method + Protected Field

**Files:**
- Modify: `amoro-ams/src/main/java/org/apache/amoro/server/table/DefaultTableRuntime.java:94` (change `private` to `protected`)
- Modify: `amoro-ams/src/main/java/org/apache/amoro/server/table/DefaultTableRuntime.java` (add template method)

- [ ] **Step 1: Change `optimizingProcess` field visibility**

In `DefaultTableRuntime.java` line 94, change:
```java
// Before:
private volatile OptimizingProcess optimizingProcess;

// After:
protected volatile OptimizingProcess optimizingProcess;
```

- [ ] **Step 2: Add template method before `refresh()`**

Add after the `getPendingInput()` method block (around line 193):

```java
  /**
   * Evaluate pending input and transition state if necessary.
   *
   * <p>Called by {@code TableRuntimeRefreshExecutor} when a snapshot change is detected. Default
   * implementation returns false (no demand). Subclasses (e.g., {@code PaimonTableRuntime})
   * override to provide format-specific evaluation.
   *
   * @param table the current AmoroTable for file scanning
   * @return true if optimizing demand exists, false otherwise
   */
  protected boolean evaluatePendingInputAndTransition(AmoroTable<?> table) {
    return false;
  }
```

- [ ] **Step 3: Run existing tests to verify no regression**

Run: `./mvnw test -pl amoro-ams -Dtest="TestTableRuntimeRefreshExecutorForPaimon,TestPaimonTableRuntimeScheduling" -DfailIfNoTests=false -Psupport-paimon-format`
Expected: All existing tests PASS (template method default returns false — Iceberg path is unchanged, existing Paimon tests still work because they go through `tryEvaluatingPendingInputForPaimon` which hasn't been removed yet)

- [ ] **Step 4: Format and commit**

```bash
./mvnw spotless:apply
git add amoro-ams/src/main/java/org/apache/amoro/server/table/DefaultTableRuntime.java
git commit -m "refactor: add evaluatePendingInputAndTransition template method to DefaultTableRuntime"
```

---

### Task 3: PaimonTableRuntime Overrides

**Files:**
- Modify: `amoro-ams/src/main/java/org/apache/amoro/server/table/paimon/PaimonTableRuntime.java`

This is the largest task. `PaimonTableRuntime` gains:
1. `PAIMON_PENDING_INPUT_KEY` static field
2. `evaluatePendingInputAndTransition()` override
3. `collectPaimonPendingInput()` private method
4. `computeHealthScore()` private helper
5. `beginProcess()` override
6. `completeEmptyProcess()` override
7. `getPaimonPendingInput()` accessor

- [ ] **Step 1: Write the failing test — template method dispatch**

Create: `amoro-ams/src/test/java/org/apache/amoro/server/table/paimon/TestPaimonTableRuntimePendingInput.java`

```java
package org.apache.amoro.server.table.paimon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.formats.paimon.optimizing.PaimonPendingInput;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.table.StateKey;
import org.apache.amoro.table.TableRuntimeStore;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * Verifies that PaimonTableRuntime.evaluatePendingInputAndTransition populates
 * PaimonPendingInput with real metrics.
 */
public class TestPaimonTableRuntimePendingInput {

  private PaimonTableRuntime createRuntime(TableRuntimeStore store) {
    return new PaimonTableRuntime(store, () -> mock(AmoroTable.class));
  }

  private TableRuntimeStore mockStore() {
    TableRuntimeStore store = mock(TableRuntimeStore.class);
    when(store.getTableIdentifier())
        .thenReturn(ServerTableIdentifier.of("c", "d", "t", TableFormat.PAIMON));
    when(store.getGroupName()).thenReturn("default");
    return store;
  }

  @Test
  public void evaluatePendingInputAndTransition_optimizingDisabled_returnsFalse() {
    TableRuntimeStore store = mockStore();
    PaimonTableRuntime runtime = createRuntime(store);
    AmoroTable<?> table = mock(AmoroTable.class);
    when(table.properties()).thenReturn(Map.of("self-optimizing.enabled", "false"));

    // Refresh to pick up disabled config
    runtime.refresh(table);

    assertFalse(runtime.evaluatePendingInputAndTransition(table));
  }

  @Test
  public void paimonPendingInputKey_keyString() {
    StateKey<PaimonPendingInput> key = PaimonTableRuntime.PAIMON_PENDING_INPUT_KEY;
    assertEquals("pending_input", key.getKey());
  }

  @Test
  public void paimonPendingInputKey_defaultValueIsPaimonPendingInput() {
    StateKey<PaimonPendingInput> key = PaimonTableRuntime.PAIMON_PENDING_INPUT_KEY;
    assertTrue(key.getDefaultValue() instanceof PaimonPendingInput);
  }

  @Test
  public void paimonPendingInputKey_deserializesPaimonType() {
    StateKey<PaimonPendingInput> key = PaimonTableRuntime.PAIMON_PENDING_INPUT_KEY;
    PaimonPendingInput result = key.deserialize("{\"dataFileCount\":42,\"healthScore\":85}");
    assertEquals(42, result.getDataFileCount());
    assertEquals(85, result.getHealthScore());
  }
}
```

Run: `./mvnw test -pl amoro-ams -Dtest=TestPaimonTableRuntimePendingInput -DfailIfNoTests=false -Psupport-paimon-format`
Expected: FAIL — `PAIMON_PENDING_INPUT_KEY` doesn't exist yet

- [ ] **Step 2: Add `PAIMON_PENDING_INPUT_KEY` and imports to PaimonTableRuntime**

In `PaimonTableRuntime.java`, add imports and the static key:

```java
// New imports:
import org.apache.amoro.formats.paimon.PaimonTable;
import org.apache.amoro.formats.paimon.optimizing.PaimonPendingInput;
import org.apache.amoro.server.optimizing.OptimizingProcess;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.StateKey;
import org.apache.paimon.CoreOptions;
import org.apache.paimon.data.BinaryRow;
import org.apache.paimon.io.DataFileMeta;
import org.apache.paimon.manifest.FileKind;
import org.apache.paimon.manifest.ManifestEntry;
import org.apache.paimon.operation.FileStoreScan;
import org.apache.paimon.table.FileStoreTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
```

Add the static key after `LOG`:

```java
  static final StateKey<PaimonPendingInput> PAIMON_PENDING_INPUT_KEY =
      StateKey.stateKey("pending_input")
          .jsonType(PaimonPendingInput.class)
          .defaultValue(new PaimonPendingInput());
```

- [ ] **Step 3: Run test to verify key declaration**

Run: `./mvnw test -pl amoro-ams -Dtest=TestPaimonTableRuntimePendingInput -DfailIfNoTests=false -Psupport-paimon-format`
Expected: 2 tests PASS

- [ ] **Step 4: Add `evaluatePendingInputAndTransition` override**

```java
  @Override
  protected boolean evaluatePendingInputAndTransition(AmoroTable<?> table) {
    if (!getOptimizingConfig().isEnabled()) {
      return false;
    }
    if (!getOptimizingStatus().equals(OptimizingStatus.IDLE)) {
      return true;
    }

    PaimonPendingInput pendingInput = collectPaimonPendingInput(table);

    // Single atomic transaction: state + status + summary (including health score)
    store()
        .begin()
        .updateState(PAIMON_PENDING_INPUT_KEY, i -> pendingInput)
        .updateStatusCode(
            code -> {
              if (code == OptimizingStatus.IDLE.getCode()) {
                return OptimizingStatus.PENDING.getCode();
              }
              return code;
            })
        .updateTableSummary(
            summary -> {
              summary.setTotalFileSize(pendingInput.getDataFileSize());
              summary.setTotalFileCount(pendingInput.getDataFileCount());
              summary.setHealthScore(pendingInput.getHealthScore());
            })
        .commit();

    return true;
  }
```

- [ ] **Step 5: Add `collectPaimonPendingInput` private method**

```java
  private PaimonPendingInput collectPaimonPendingInput(AmoroTable<?> table) {
    if (!(table instanceof PaimonTable)) {
      LOG.warn(
          "Expected PaimonTable but got {}, returning empty pending input",
          table.getClass().getName());
      return new PaimonPendingInput();
    }
    PaimonTable paimonTable = (PaimonTable) table;
    if (!(paimonTable.originalTable() instanceof FileStoreTable)) {
      LOG.warn(
          "Expected FileStoreTable but got {}, returning empty pending input",
          paimonTable.originalTable().getClass().getName());
      return new PaimonPendingInput();
    }
    FileStoreTable fileStoreTable = (FileStoreTable) paimonTable.originalTable();
    FileStoreScan scan = fileStoreTable.store().newScan();
    List<ManifestEntry> entries = scan.plan().files(FileKind.ADD);

    long targetFileSize = CoreOptions.fromMap(fileStoreTable.options()).targetFileSize(false);
    Map<BinaryRow, List<DataFileMeta>> partitionFiles = new HashMap<>();

    int dataFileCount = 0;
    long dataFileSize = 0;
    long dataRecordCount = 0;
    int smallFileCount = 0;
    long smallFileSize = 0;
    int fileWithDeleteCount = 0;
    long deleteRecordCount = 0;

    for (ManifestEntry entry : entries) {
      DataFileMeta file = entry.file();
      dataFileCount++;
      dataFileSize += file.fileSize();
      dataRecordCount += file.rowCount();

      if (file.fileSize() < targetFileSize) {
        smallFileCount++;
        smallFileSize += file.fileSize();
      }

      if (file.deleteRowCount().isPresent() && file.deleteRowCount().get() > 0) {
        fileWithDeleteCount++;
        deleteRecordCount += file.deleteRowCount().get();
      }

      partitionFiles
          .computeIfAbsent(entry.partition(), k -> new ArrayList<>())
          .add(file);
    }

    int partitionCount = partitionFiles.size();
    int healthScore =
        PaimonPendingInput.computeHealthScore(
            dataFileCount, smallFileCount,
            dataRecordCount, deleteRecordCount,
            partitionFiles);

    return new PaimonPendingInput(
        dataFileCount, dataFileSize, dataRecordCount,
        smallFileCount, smallFileSize, partitionCount,
        fileWithDeleteCount, deleteRecordCount,
        healthScore);
  }
```

- [ ] **Step 6: Add `beginProcess` override**

```java
  @Override
  public void beginProcess(OptimizingProcess process) {
    OptimizingStatus originalStatus = getOptimizingStatus();
    this.optimizingProcess = process;
    store()
        .begin()
        .updateState(
            DefaultTableRuntime.PROCESS_ID_KEY, any -> process.getProcessId())
        .updateStatusCode(
            code ->
                OptimizingStatus.ofOptimizingType(process.getOptimizingType()).getCode())
        .updateState(PAIMON_PENDING_INPUT_KEY, any -> new PaimonPendingInput())
        .commit();
  }
```

**Note:** `DefaultTableRuntime.PROCESS_ID_KEY` is currently `private static final`. For `PaimonTableRuntime` to reference it, change it to `protected static final` in `DefaultTableRuntime.java` (line 83). The `originalStatus` variable mirrors the parent's convention — currently unused but kept for parity.

- [ ] **Step 7: Add `completeEmptyProcess` override**

```java
  @Override
  public void completeEmptyProcess() {
    OptimizingStatus originalStatus = getOptimizingStatus();
    if (getOptimizingStatus() == OptimizingStatus.IDLE) {
      return;
    }
    store()
        .begin()
        .updateStatusCode(code -> OptimizingStatus.IDLE.getCode())
        .updateState(
            DefaultTableRuntime.OPTIMIZING_STATE_KEY,
            state -> {
              state.setLastOptimizedSnapshotId(state.getCurrentSnapshotId());
              state.setLastOptimizedChangeSnapshotId(state.getCurrentChangeSnapshotId());
              return state;
            })
        .updateState(PAIMON_PENDING_INPUT_KEY, any -> new PaimonPendingInput())
        .commit();
    optimizingProcess = null;
  }
```

**Note:** `DefaultTableRuntime.OPTIMIZING_STATE_KEY` is already `protected static final` (line 67). No change needed.

- [ ] **Step 8: Add `getPaimonPendingInput` accessor**

```java
  public PaimonPendingInput getPaimonPendingInput() {
    return store().getState(PAIMON_PENDING_INPUT_KEY);
  }
```

- [ ] **Step 9: Change `PROCESS_ID_KEY` visibility in DefaultTableRuntime**

In `DefaultTableRuntime.java` line 83, change:
```java
// Before:
private static final StateKey<Long> PROCESS_ID_KEY =

// After:
protected static final StateKey<Long> PROCESS_ID_KEY =
```

- [ ] **Step 10: Run tests**

Run: `./mvnw test -pl amoro-ams -Dtest="TestPaimonTableRuntimePendingInput,TestPaimonTableRuntimeScheduling,TestTableRuntimeRefreshExecutorForPaimon" -DfailIfNoTests=false -Psupport-paimon-format`
Expected: All tests PASS

- [ ] **Step 11: Format and commit**

```bash
./mvnw spotless:apply
git add amoro-ams/src/main/java/org/apache/amoro/server/table/paimon/PaimonTableRuntime.java \
        amoro-ams/src/main/java/org/apache/amoro/server/table/DefaultTableRuntime.java \
        amoro-ams/src/test/java/org/apache/amoro/server/table/paimon/TestPaimonTableRuntimePendingInput.java
git commit -m "feat(paimon): override evaluatePendingInputAndTransition with real file metrics in PaimonTableRuntime"
```

---

### Task 4: DefaultTableRuntimeFactory — requiredStateKeys Override

**Files:**
- Modify: `amoro-ams/src/main/java/org/apache/amoro/server/table/DefaultTableRuntimeFactory.java:183-194`

- [ ] **Step 1: Write the failing test**

Add to `TestPaimonTableRuntimeScheduling.java`:

```java
  @Test
  public void paimonCreatorUsesPaimonPendingInputKey() {
    List<StateKey<?>> keys = Lists.newArrayList(
        DefaultTableRuntime.OPTIMIZING_STATE_KEY,
        PaimonTableRuntime.PAIMON_PENDING_INPUT_KEY,
        DefaultTableRuntime.PROCESS_ID_KEY,
        DefaultTableRuntime.CLEANUP_STATE_KEY);

    StateKey<?> pendingInputKey = keys.stream()
        .filter(k -> "pending_input".equals(k.getKey()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("pending_input key not found"));
    assertEquals("pending_input", pendingInputKey.getKey());
    assertTrue(pendingInputKey.getDefaultValue() instanceof PaimonPendingInput);
  }
```

Add imports:
```java
import org.apache.amoro.formats.paimon.optimizing.PaimonPendingInput;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
```

Run: `./mvnw test -pl amoro-ams -Dtest=TestPaimonTableRuntimeScheduling -DfailIfNoTests=false -Psupport-paimon-format`
Expected: FAIL — `PAIMON_PENDING_INPUT_KEY` is not yet in required state keys

- [ ] **Step 2: Update `PaimonTableRuntimeCreatorImpl.requiredStateKeys()`**

In `DefaultTableRuntimeFactory.java`, change `PaimonTableRuntimeCreatorImpl.requiredStateKeys()`:

```java
  private class PaimonTableRuntimeCreatorImpl implements TableRuntimeFactory.TableRuntimeCreator {

    @Override
    public List<StateKey<?>> requiredStateKeys() {
      return Lists.newArrayList(
          DefaultTableRuntime.OPTIMIZING_STATE_KEY,
          PaimonTableRuntime.PAIMON_PENDING_INPUT_KEY,
          DefaultTableRuntime.PROCESS_ID_KEY,
          DefaultTableRuntime.CLEANUP_STATE_KEY);
    }

    @Override
    public TableRuntime create(TableRuntimeStore store) {
      return new PaimonTableRuntime(store, () -> loader.apply(store.getTableIdentifier()));
    }
  }
```

Add import at top:
```java
import org.apache.amoro.formats.paimon.optimizing.PaimonPendingInput;
```

(This import is needed because `PaimonTableRuntime.PAIMON_PENDING_INPUT_KEY` references `PaimonPendingInput`.)

- [ ] **Step 3: Run tests**

Run: `./mvnw test -pl amoro-ams -Dtest="TestPaimonTableRuntimeScheduling,TestPaimonTableRuntimePendingInput" -DfailIfNoTests=false -Psupport-paimon-format`
Expected: All tests PASS

- [ ] **Step 4: Format and commit**

```bash
./mvnw spotless:apply
git add amoro-ams/src/main/java/org/apache/amoro/server/table/DefaultTableRuntimeFactory.java \
        amoro-ams/src/test/java/org/apache/amoro/server/table/paimon/TestPaimonTableRuntimeScheduling.java
git commit -m "refactor: PaimonTableRuntimeCreatorImpl uses PaimonPendingInput StateKey"
```

---

### Task 5: Simplify TableRuntimeRefreshExecutor Paimon Branch

**Files:**
- Modify: `amoro-ams/src/main/java/org/apache/amoro/server/scheduler/inline/TableRuntimeRefreshExecutor.java`

- [ ] **Step 1: Simplify Paimon branch in `execute()`**

Change lines 203-211 from:

```java
      } else if (table.format() == TableFormat.PAIMON) {
        // Paimon lightweight path — snapshot change only, no file scan
        boolean snapshotChanged =
            lastOptimizedSnapshotId != defaultTableRuntime.getCurrentSnapshotId();
        if (snapshotChanged) {
          hasOptimizingDemand = tryEvaluatingPendingInputForPaimon(defaultTableRuntime);
        } else {
          logger.debug("{} optimizing is not necessary", defaultTableRuntime.getTableIdentifier());
        }
      }
```

To:

```java
      } else if (table.format() == TableFormat.PAIMON) {
        boolean snapshotChanged =
            lastOptimizedSnapshotId != defaultTableRuntime.getCurrentSnapshotId();
        if (snapshotChanged) {
          hasOptimizingDemand = defaultTableRuntime.evaluatePendingInputAndTransition(table);
        } else {
          logger.debug("{} optimizing is not necessary", defaultTableRuntime.getTableIdentifier());
        }
      }
```

- [ ] **Step 2: Delete `tryEvaluatingPendingInputForPaimon` method**

Delete the entire method (lines 131-151):

```java
  // DELETE THIS ENTIRE METHOD:
  private boolean tryEvaluatingPendingInputForPaimon(DefaultTableRuntime tableRuntime) {
    ...
  }
```

- [ ] **Step 3: Run existing Paimon tests to verify they still pass**

Run: `./mvnw test -pl amoro-ams -Dtest="TestTableRuntimeRefreshExecutorForPaimon,TestPaimonTableRuntimeScheduling" -DfailIfNoTests=false -Psupport-paimon-format`
Expected: All tests PASS

**Why this works:** The `collectPaimonPendingInput()` method includes an `instanceof PaimonTable` guard. When existing tests mock `AmoroTable` directly (not wrapping in `PaimonTable`), the guard catches the mismatch and returns an empty `PaimonPendingInput`. This means IDLE→PENDING still fires, matching the existing test expectations.

- [ ] **Step 4: Format and commit**

```bash
./mvnw spotless:apply
git add amoro-ams/src/main/java/org/apache/amoro/server/scheduler/inline/TableRuntimeRefreshExecutor.java
git commit -m "refactor: simplify Paimon branch in TableRuntimeRefreshExecutor to use template method"
```

---

### Task 6: Integration Verification

**Files:**
- All modified files

- [ ] **Step 1: Run full Paimon test suite**

```bash
./mvnw test -pl amoro-ams -Dtest="*Paimon*" -DfailIfNoTests=false -Psupport-paimon-format
```

Expected: All Paimon-related tests PASS

- [ ] **Step 2: Run format-paimon unit tests**

```bash
./mvnw test -pl amoro-format-paimon -Dtest="*Paimon*" -DfailIfNoTests=false -Psupport-paimon-format
```

Expected: All tests PASS

- [ ] **Step 3: Run spotless check**

```bash
./mvnw spotless:check -pl amoro-ams,amoro-format-paimon -Psupport-paimon-format
```

Expected: BUILD SUCCESS

- [ ] **Step 4: Run compile check across all affected modules**

```bash
./mvnw compile -pl amoro-ams,amoro-format-paimon -Psupport-paimon-format
```

Expected: BUILD SUCCESS

---

## Execution Order Dependency Graph

```
Task 1 (PaimonPendingInput data class)
  ↓
Task 2 (DefaultTableRuntime template method)  ← no code dependency on Task 1, but logical prerequisite
  ↓
Task 3 (PaimonTableRuntime overrides)         ← depends on Task 1 + Task 2
  ↓
Task 4 (DefaultTableRuntimeFactory keys)      ← depends on Task 3 (references PAIMON_PENDING_INPUT_KEY)
  ↓
Task 5 (TableRuntimeRefreshExecutor simplify) ← depends on Task 3 (calls template method)
  ↓
Task 6 (Integration verification)             ← depends on all above
```

Tasks 2 and 4 have no code dependency on each other but Task 4 references `PaimonTableRuntime.PAIMON_PENDING_INPUT_KEY` which is added in Task 3.
