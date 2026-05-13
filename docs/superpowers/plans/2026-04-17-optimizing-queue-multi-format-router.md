# OptimizingQueue Multi-Format Router — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor `OptimizingQueue` so multiple lake-format `ProcessFactory` SPIs coexist via an explicit `ProcessFactoryRouter` instead of the opaque `CompositeOptimizingProcessFactory` facade. Commit routing becomes independent of catalog re-reads.

**Architecture:** Introduce `ProcessFactoryRouter` (not a `ProcessFactory`) as a thin format → factory lookup. `OptimizingQueue` holds the router directly; `TableOptimizingProcess` caches its format at construction so `buildCommit()` routes off the cached field. Delete the composite and its supporting dead code.

**Tech Stack:** Java 11 (project target per root `pom.xml:73`), JUnit 4 (existing `TestOptimizingQueue`) + JUnit 5 + Mockito (new router tests), Maven.

**Spec:** `docs/superpowers/specs/2026-04-17-optimizing-queue-multi-format-router-design.md`.

**Branch:** Work continues on `czy006/paimon-unaware-compact` (no new worktree — this is a direct follow-up to commit `77a06a978` on the same branch).

**Module scope:** All changes live under `amoro-ams`. Build with `mvn -pl amoro-ams -am compile` and test with `mvn -pl amoro-ams -am test -Dtest=...`.

---

## Task 1: Create `ProcessFactoryRouter` (new, unwired)

Build the router as a standalone, fully-tested class. Nothing in the main code calls it yet.

**Files:**
- Create: `amoro-ams/src/main/java/org/apache/amoro/server/process/ProcessFactoryRouter.java`
- Create: `amoro-ams/src/test/java/org/apache/amoro/server/process/TestProcessFactoryRouter.java`

- [ ] **Step 1: Write the failing test file skeleton**

Create `amoro-ams/src/test/java/org/apache/amoro/server/process/TestProcessFactoryRouter.java`:

```java
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

package org.apache.amoro.server.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.amoro.TableFormat;
import org.apache.amoro.process.ProcessFactory;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestProcessFactoryRouter {

  private ProcessFactory mockFactory(String name, Set<TableFormat> formats) {
    ProcessFactory factory = mock(ProcessFactory.class);
    when(factory.name()).thenReturn(name);
    when(factory.supportedFormats()).thenReturn(formats);
    return factory;
  }

  @Test
  public void forFormat_hit() {
    ProcessFactory iceberg =
        mockFactory("iceberg", Set.of(TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG));
    ProcessFactory paimon = mockFactory("paimon", Set.of(TableFormat.PAIMON));

    ProcessFactoryRouter router = new ProcessFactoryRouter(List.of(iceberg, paimon));

    assertSame(iceberg, router.forFormat(TableFormat.ICEBERG));
    assertSame(iceberg, router.forFormat(TableFormat.MIXED_ICEBERG));
    assertSame(paimon, router.forFormat(TableFormat.PAIMON));
  }
}
```

- [ ] **Step 2: Run the test — expect a compile failure**

Run:
```
mvn -pl amoro-ams -am test-compile -Dtest=TestProcessFactoryRouter
```

Expected: compile error — `cannot find symbol: class ProcessFactoryRouter`. This confirms the test sees the missing target.

- [ ] **Step 3: Create `ProcessFactoryRouter` skeleton that satisfies `forFormat_hit`**

Create `amoro-ams/src/main/java/org/apache/amoro/server/process/ProcessFactoryRouter.java`:

```java
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

package org.apache.amoro.server.process;

import org.apache.amoro.TableFormat;
import org.apache.amoro.process.ProcessFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Format → {@link ProcessFactory} lookup built from the SPI-discovered factory list.
 *
 * <p>Responsibility is intentionally narrow: find a factory for a format. Lifecycle, trigger,
 * recover, and other {@link ProcessFactory} concerns stay on the delegate instances; the router
 * never implements {@code ProcessFactory} itself.
 */
public final class ProcessFactoryRouter {

  private final List<ProcessFactory> delegates;
  private final Map<TableFormat, ProcessFactory> byFormat;

  public ProcessFactoryRouter(List<ProcessFactory> factories) {
    this.delegates = List.copyOf(factories);
    this.byFormat = Collections.unmodifiableMap(buildRoutingTable(this.delegates));
  }

  public ProcessFactory forFormat(TableFormat format) {
    ProcessFactory factory = byFormat.get(format);
    if (factory == null) {
      throw new UnsupportedOperationException(
          "No ProcessFactory registered for table format " + format);
    }
    return factory;
  }

  public Set<TableFormat> supportedFormats() {
    return byFormat.keySet();
  }

  public List<ProcessFactory> delegates() {
    return delegates;
  }

  private static Map<TableFormat, ProcessFactory> buildRoutingTable(
      List<ProcessFactory> factories) {
    Map<TableFormat, ProcessFactory> table = new HashMap<>();
    for (ProcessFactory factory : factories) {
      for (TableFormat format : factory.supportedFormats()) {
        ProcessFactory existing = table.get(format);
        if (existing != null && existing != factory) {
          throw new IllegalArgumentException(
              String.format(
                  "ProcessFactory conflict for format %s: '%s' and '%s' both claim it",
                  format, existing.name(), factory.name()));
        }
        table.put(format, factory);
      }
    }
    return table;
  }
}
```

- [ ] **Step 4: Run the single test — expect it to pass**

Run:
```
mvn -pl amoro-ams -am test -Dtest='TestProcessFactoryRouter#forFormat_hit'
```

Expected: `BUILD SUCCESS`, one test passed.

- [ ] **Step 5: Add `forFormat_miss` test case**

Append to `TestProcessFactoryRouter`:

```java
  @Test
  public void forFormat_miss() {
    ProcessFactory iceberg = mockFactory("iceberg", Set.of(TableFormat.ICEBERG));
    ProcessFactoryRouter router = new ProcessFactoryRouter(List.of(iceberg));

    UnsupportedOperationException ex =
        assertThrows(
            UnsupportedOperationException.class, () -> router.forFormat(TableFormat.HUDI));
    assertTrue(ex.getMessage().contains("HUDI"));
  }
```

- [ ] **Step 6: Run — expect pass (router already throws in this path)**

Run:
```
mvn -pl amoro-ams -am test -Dtest='TestProcessFactoryRouter#forFormat_miss'
```

Expected: PASS.

- [ ] **Step 7: Add `conflict_detection` test case**

Append:

```java
  @Test
  public void conflict_detection() {
    ProcessFactory factoryA =
        mockFactory("a", new HashSet<>(Set.of(TableFormat.ICEBERG, TableFormat.PAIMON)));
    ProcessFactory factoryB = mockFactory("b", Set.of(TableFormat.PAIMON));

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> new ProcessFactoryRouter(List.of(factoryA, factoryB)));
    assertTrue(ex.getMessage().contains("PAIMON"));
    assertTrue(ex.getMessage().contains("'a'"));
    assertTrue(ex.getMessage().contains("'b'"));
  }
```

- [ ] **Step 8: Run — expect pass**

Run:
```
mvn -pl amoro-ams -am test -Dtest='TestProcessFactoryRouter#conflict_detection'
```

Expected: PASS.

- [ ] **Step 9: Add `empty_factories` test case**

Append:

```java
  @Test
  public void empty_factories() {
    ProcessFactoryRouter router = new ProcessFactoryRouter(Collections.emptyList());

    assertTrue(router.supportedFormats().isEmpty());
    assertTrue(router.delegates().isEmpty());
    assertThrows(
        UnsupportedOperationException.class, () -> router.forFormat(TableFormat.ICEBERG));
  }
```

- [ ] **Step 10: Run — expect pass**

Run:
```
mvn -pl amoro-ams -am test -Dtest='TestProcessFactoryRouter#empty_factories'
```

Expected: PASS.

- [ ] **Step 11: Add `supportedFormats_union` test case (asserts immutability too)**

Append:

```java
  @Test
  public void supportedFormats_union() {
    ProcessFactory iceberg =
        mockFactory(
            "iceberg",
            Set.of(TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG, TableFormat.MIXED_HIVE));
    ProcessFactory paimon = mockFactory("paimon", Set.of(TableFormat.PAIMON));

    ProcessFactoryRouter router = new ProcessFactoryRouter(List.of(iceberg, paimon));

    Set<TableFormat> formats = router.supportedFormats();
    assertEquals(4, formats.size());
    assertTrue(formats.contains(TableFormat.ICEBERG));
    assertTrue(formats.contains(TableFormat.MIXED_ICEBERG));
    assertTrue(formats.contains(TableFormat.MIXED_HIVE));
    assertTrue(formats.contains(TableFormat.PAIMON));

    assertThrows(UnsupportedOperationException.class, () -> formats.add(TableFormat.HUDI));
  }
```

- [ ] **Step 12: Run — expect pass**

Run:
```
mvn -pl amoro-ams -am test -Dtest='TestProcessFactoryRouter#supportedFormats_union'
```

Expected: PASS.

- [ ] **Step 13: Add `delegates_exposure` test case**

Append:

```java
  @Test
  public void delegates_exposure() {
    ProcessFactory a = mockFactory("a", Set.of(TableFormat.ICEBERG));
    ProcessFactory b = mockFactory("b", Set.of(TableFormat.PAIMON));

    ProcessFactoryRouter router = new ProcessFactoryRouter(List.of(a, b));

    List<ProcessFactory> delegates = router.delegates();
    assertEquals(2, delegates.size());
    assertSame(a, delegates.get(0));
    assertSame(b, delegates.get(1));
    assertThrows(UnsupportedOperationException.class, () -> delegates.add(a));
  }
```

- [ ] **Step 14: Run full router test class — expect all green**

Run:
```
mvn -pl amoro-ams -am test -Dtest='TestProcessFactoryRouter'
```

Expected: 6 tests, all PASS.

- [ ] **Step 15: Commit**

```bash
git add amoro-ams/src/main/java/org/apache/amoro/server/process/ProcessFactoryRouter.java \
        amoro-ams/src/test/java/org/apache/amoro/server/process/TestProcessFactoryRouter.java
git commit -m "$(cat <<'EOF'
[AMORO-4200] ams: introduce ProcessFactoryRouter for explicit format routing

Adds a dedicated (format → ProcessFactory) lookup that does NOT implement
ProcessFactory itself. Meant to replace CompositeOptimizingProcessFactory in
a follow-up commit; this one only introduces the class + unit tests.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Task 2: Wire Router into `OptimizingQueue` + delete Composite

Coordinated change across 4 files. At the end of this task the old composite is gone and every call site uses the router directly. Existing `TestOptimizingQueue` keeps passing as the safety net.

**Files:**
- Modify: `amoro-ams/src/main/java/org/apache/amoro/server/optimizing/OptimizingQueue.java`
- Modify: `amoro-ams/src/main/java/org/apache/amoro/server/DefaultOptimizingService.java`
- Modify: `amoro-ams/src/test/java/org/apache/amoro/server/optimizing/TestOptimizingQueue.java`
- Delete: `amoro-ams/src/main/java/org/apache/amoro/server/process/CompositeOptimizingProcessFactory.java`
- Delete: `amoro-ams/src/test/java/org/apache/amoro/server/process/TestCompositeOptimizingProcessFactory.java`

- [ ] **Step 1: Update `OptimizingQueue` imports**

In `OptimizingQueue.java`, replace the existing `import org.apache.amoro.process.ProcessFactory;` line with:

```java
import org.apache.amoro.server.process.ProcessFactoryRouter;
```

(The `TableFormat` import at line 24 is already present and stays.)

- [ ] **Step 2: Swap the `optimizingFactory` field for `router`**

In `OptimizingQueue.java`, locate line 104:

```java
  private final ProcessFactory optimizingFactory;
```

Replace with:

```java
  private final ProcessFactoryRouter router;
```

- [ ] **Step 3: Update constructor signature and body**

In `OptimizingQueue.java`, change the constructor (lines 108-129). The last parameter and its assignment change:

Before:
```java
      int maxPlanningParallelism,
      ProcessFactory optimizingFactory) {
    ...
    this.optimizingFactory = optimizingFactory;
```

After:
```java
      int maxPlanningParallelism,
      ProcessFactoryRouter router) {
    ...
    this.router = router;
```

- [ ] **Step 4: Update `isFormatSupported`**

In `OptimizingQueue.java`, lines 243-254, change:

```java
    if (!optimizingFactory.supportedFormats().contains(format)) {
```

to:

```java
    if (!router.supportedFormats().contains(format)) {
```

- [ ] **Step 5: Update `planInternal`**

In `OptimizingQueue.java`, lines 409-411, change:

```java
      TableOptimizingPlanner planner =
          optimizingFactory.createPlanner(
              tableRuntime, table, getAvailableCore(), maxInputSizePerThread());
```

to:

```java
      TableOptimizingPlanner planner =
          router
              .forFormat(tableRuntime.getFormat())
              .createPlanner(tableRuntime, table, getAvailableCore(), maxInputSizePerThread());
```

- [ ] **Step 6: Add `format` field + getter to inner `TableOptimizingProcess`**

In `OptimizingQueue.java`, inside inner class `TableOptimizingProcess` (starts line 519), just below `private final long processId;` (line 522), add:

```java
    private final TableFormat format;
```

Then add a package-private getter immediately after the other simple getters (after `getProcessId()` at line 615-617 is a good spot):

```java
    TableFormat getFormat() {
      return format;
    }
```

- [ ] **Step 7: Initialise `format` in both `TableOptimizingProcess` constructors**

Constructor 1 (line 567 `TableOptimizingProcess(OptimizingPlanResult, DefaultTableRuntime)`): right after `this.tableRuntime = tableRuntime;` (line 570), add:

```java
      this.format = tableRuntime.getFormat();
```

Constructor 2 (line 581 `TableOptimizingProcess(DefaultTableRuntime, TableProcessMeta, OptimizingProcessState)`): right after `this.tableRuntime = tableRuntime;` (line 585), add:

```java
      this.format = tableRuntime.getFormat();
```

- [ ] **Step 8: Route `buildCommit()` via the cached format**

In `OptimizingQueue.java`, lines 889-904, change:

```java
    private TableOptimizingCommitter buildCommit() {
      AmoroTable<?> table =
          catalogManager.loadTable(tableRuntime.getTableIdentifier().getIdentifier());
      List<RewriteStageTask> taskDescriptors =
          taskMap.values().stream()
              .filter(task -> task.getStatus() == Status.SUCCESS)
              .map(TaskRuntime::getTaskDescriptor)
              .collect(Collectors.toList());
      return optimizingFactory.createCommitter(
          table,
          targetSnapshotId,
          targetChangeSnapshotId,
          taskDescriptors,
          fromSequence,
          toSequence);
    }
```

to:

```java
    private TableOptimizingCommitter buildCommit() {
      AmoroTable<?> table =
          catalogManager.loadTable(tableRuntime.getTableIdentifier().getIdentifier());
      List<RewriteStageTask> taskDescriptors =
          taskMap.values().stream()
              .filter(task -> task.getStatus() == Status.SUCCESS)
              .map(TaskRuntime::getTaskDescriptor)
              .collect(Collectors.toList());
      return router
          .forFormat(format)
          .createCommitter(
              table,
              targetSnapshotId,
              targetChangeSnapshotId,
              taskDescriptors,
              fromSequence,
              toSequence);
    }
```

- [ ] **Step 9: Update `DefaultOptimizingService` field + constructor**

In `DefaultOptimizingService.java`, replace the existing line 134:

```java
  private final ProcessFactory optimizingFactory;
```

with:

```java
  private final ProcessFactoryRouter router;
```

Replace line 171:

```java
    this.optimizingFactory = resolveOptimizingFactory(processFactories);
```

with:

```java
    this.router =
        new ProcessFactoryRouter(
            Optional.ofNullable(processFactories).orElseGet(Collections::emptyList));
```

Replace the startup log at lines 179-181:

```java
    LOG.info(
        "Use process factory {} for table optimizing planner/committer.",
        this.optimizingFactory.name());
```

with:

```java
    LOG.info(
        "Optimizing router initialised: delegates={} formats={}",
        router.delegates().stream().map(ProcessFactory::name).collect(Collectors.toList()),
        router.supportedFormats());
```

- [ ] **Step 10: Delete dead `getOptimizingFactory()` accessor**

In `DefaultOptimizingService.java`, delete lines 188-190:

```java
  ProcessFactory getOptimizingFactory() {
    return optimizingFactory;
  }
```

(`grep` earlier confirmed zero callers.)

- [ ] **Step 11: Delete `resolveOptimizingFactory(...)` method**

In `DefaultOptimizingService.java`, delete the whole `resolveOptimizingFactory` method (currently lines 429-446 — the exact lines will have shifted after step 10; locate by method name).

- [ ] **Step 12: Delete inner class `NoopOptimizingProcessFactory`**

In `DefaultOptimizingService.java`, delete the entire `private static class NoopOptimizingProcessFactory implements ProcessFactory { ... }` block (currently starts line 1000). Remove any imports that become unused (`org.apache.amoro.server.process.CompositeOptimizingProcessFactory`, plus anything only referenced by the deleted noop: `org.apache.amoro.Action`, `TableFormat`, `RecoverProcessFailedException`, `TableProcess`, `TableProcessStore`, `Sets` — **verify each by checking if any other code in the file still uses it** before removing).

- [ ] **Step 13: Extend `dispose()` to close the delegate factories**

In `DefaultOptimizingService.java`, modify `dispose()` (around line 459-471). After `optimizingQueueByGroup.values().forEach(OptimizingQueue::dispose);`, add:

```java
    router
        .delegates()
        .forEach(
            factory -> {
              try {
                factory.close();
              } catch (Exception e) {
                LOG.warn("Error closing ProcessFactory '{}': {}", factory.name(), e.getMessage());
              }
            });
```

(Mirrors the tolerance the old composite had; keeps shutdown resilient to a buggy factory.)

- [ ] **Step 14: Pass `router` into both `OptimizingQueue` construction sites**

In `DefaultOptimizingService.java`:

Line 210 — change `optimizingFactory` to `router`:
```java
              new OptimizingQueue(
                  catalogManager,
                  group,
                  this,
                  planExecutor,
                  Optional.ofNullable(tableRuntimes).orElseGet(ArrayList::new),
                  maxPlanningParallelism,
                  router);
```

Line 422 (the other `new OptimizingQueue(...)` inside `loadOptimizingQueues`/equivalent call, around line 414-422) — same swap. (Locate by `new OptimizingQueue` and change the trailing argument from `optimizingFactory` to `router`.)

- [ ] **Step 15: Update `TestOptimizingQueue` to construct a router**

In `TestOptimizingQueue.java`:

Replace line 91:
```java
  private final ProcessFactory optimizingFactory = new IcebergProcessFactory();
```

with:
```java
  private final ProcessFactoryRouter router =
      new ProcessFactoryRouter(java.util.List.of(new IcebergProcessFactory()));
```

Every place that currently passes `optimizingFactory` as the final `OptimizingQueue` constructor argument (lines 153, 164, 220, 256, 293, 727) must pass `router` instead. Also remove the now-unused `import org.apache.amoro.process.ProcessFactory;` if it becomes orphaned (check other references first) and add `import org.apache.amoro.server.process.ProcessFactoryRouter;`.

- [ ] **Step 16: Delete `CompositeOptimizingProcessFactory` and its test**

```bash
rm amoro-ams/src/main/java/org/apache/amoro/server/process/CompositeOptimizingProcessFactory.java
rm amoro-ams/src/test/java/org/apache/amoro/server/process/TestCompositeOptimizingProcessFactory.java
```

- [ ] **Step 17: Compile**

Run:
```
mvn -pl amoro-ams -am compile
```

Expected: `BUILD SUCCESS`, no unresolved symbols, no unused-import warnings. Fix any lingering references to `CompositeOptimizingProcessFactory` / `NoopOptimizingProcessFactory` / `getOptimizingFactory` / `resolveOptimizingFactory`.

- [ ] **Step 18: Hunt any lingering references**

Run:
```
grep -rn "CompositeOptimizingProcessFactory\|resolveOptimizingFactory\|NoopOptimizingProcessFactory\|getOptimizingFactory" amoro-ams --include="*.java"
```

Expected: zero hits.

- [ ] **Step 19: Run `TestOptimizingQueue` — expect all existing cases still pass**

Run:
```
mvn -pl amoro-ams -am test -Dtest='TestOptimizingQueue'
```

Expected: all pre-existing tests PASS — the router-plumbing change is behavior-preserving for single-format cases.

- [ ] **Step 20: Run `TestProcessFactoryRouter` — still all green**

Run:
```
mvn -pl amoro-ams -am test -Dtest='TestProcessFactoryRouter'
```

Expected: 6 tests PASS.

- [ ] **Step 21: Commit**

```bash
git add -A amoro-ams/src/main/java/org/apache/amoro/server/optimizing/OptimizingQueue.java \
         amoro-ams/src/main/java/org/apache/amoro/server/DefaultOptimizingService.java \
         amoro-ams/src/test/java/org/apache/amoro/server/optimizing/TestOptimizingQueue.java
git add -A amoro-ams/src/main/java/org/apache/amoro/server/process/CompositeOptimizingProcessFactory.java \
         amoro-ams/src/test/java/org/apache/amoro/server/process/TestCompositeOptimizingProcessFactory.java
git commit -m "$(cat <<'EOF'
[AMORO-4200] ams: wire OptimizingQueue through ProcessFactoryRouter; drop Composite

- OptimizingQueue now holds ProcessFactoryRouter and routes per call.
- TableOptimizingProcess caches format at construction so buildCommit() no
  longer relies on AmoroTable.format() for routing.
- DefaultOptimizingService builds the router directly from the SPI list and
  drives delegate close() at dispose().
- CompositeOptimizingProcessFactory, its test, NoopOptimizingProcessFactory,
  resolveOptimizingFactory(), and the dead getOptimizingFactory() accessor
  are removed.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Task 3: Cross-format routing + recovery-format coverage

Strengthen `TestOptimizingQueue` with two focused assertions that cover the new invariants: (a) the router dispatches to the right factory for each format, (b) recovery-path constructor sets `format` from the table runtime.

**Files:**
- Modify: `amoro-ams/src/test/java/org/apache/amoro/server/optimizing/TestOptimizingQueue.java`

- [ ] **Step 1: Add cross-format routing test**

Append to `TestOptimizingQueue` (below `testSkipPlanningForUnsupportedFormat` at line 741, keeping JUnit 4 style to match the file):

```java
  @Test
  public void testRouterSelectsFactoryByFormat() {
    ProcessFactory icebergFactory = Mockito.mock(ProcessFactory.class);
    Mockito.when(icebergFactory.name()).thenReturn("iceberg-mock");
    Mockito.when(icebergFactory.supportedFormats())
        .thenReturn(java.util.Set.of(TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG,
            TableFormat.MIXED_HIVE));

    ProcessFactory paimonFactory = Mockito.mock(ProcessFactory.class);
    Mockito.when(paimonFactory.name()).thenReturn("paimon-mock");
    Mockito.when(paimonFactory.supportedFormats()).thenReturn(java.util.Set.of(TableFormat.PAIMON));

    ProcessFactoryRouter router =
        new ProcessFactoryRouter(java.util.List.of(icebergFactory, paimonFactory));

    Assert.assertSame(icebergFactory, router.forFormat(TableFormat.ICEBERG));
    Assert.assertSame(icebergFactory, router.forFormat(TableFormat.MIXED_ICEBERG));
    Assert.assertSame(paimonFactory, router.forFormat(TableFormat.PAIMON));
    Assert.assertThrows(
        UnsupportedOperationException.class, () -> router.forFormat(TableFormat.HUDI));
  }
```

Add the import near the other `org.apache.amoro.server.process.*` imports:
```java
import org.apache.amoro.server.process.ProcessFactoryRouter;
```

- [ ] **Step 2: Run the new test — expect pass**

Run:
```
mvn -pl amoro-ams -am test -Dtest='TestOptimizingQueue#testRouterSelectsFactoryByFormat'
```

Expected: PASS.

- [ ] **Step 3: Add recovery-path format caching test**

Append to `TestOptimizingQueue`:

```java
  @Test
  public void testRecoveredProcessCachesFormatFromTableRuntime() throws Exception {
    DefaultTableRuntime tableRuntime = initTableWithFiles();
    OptimizingQueue queue = buildOptimizingGroupService(tableRuntime);

    // Drive a task through planning to create a TableOptimizingProcess.
    TaskRuntime<?> task = queue.pollTask(optimizerThread, MAX_POLLING_TIME);
    Assert.assertNotNull(task);

    OptimizingProcess process = tableRuntime.getOptimizingProcess();
    Assert.assertNotNull(process);

    // TableOptimizingProcess is an inner class; reach in via reflection to
    // verify the cached format matches the table runtime's format.
    Method getFormat = process.getClass().getDeclaredMethod("getFormat");
    getFormat.setAccessible(true);
    Assert.assertEquals(tableRuntime.getFormat(), getFormat.invoke(process));

    queue.dispose();
  }
```

(`java.lang.reflect.Method` is already imported in the test file.)

- [ ] **Step 4: Run the new test — expect pass**

Run:
```
mvn -pl amoro-ams -am test -Dtest='TestOptimizingQueue#testRecoveredProcessCachesFormatFromTableRuntime'
```

Expected: PASS. (This exercises constructor 1 — construction via `planInternal`. Constructor 2's recovery path is covered indirectly because both constructors share the same assignment source, `tableRuntime.getFormat()`; the invariant is one line, mirrored.)

- [ ] **Step 5: Run the full `TestOptimizingQueue` class**

Run:
```
mvn -pl amoro-ams -am test -Dtest='TestOptimizingQueue'
```

Expected: every test PASS, including the two new ones.

- [ ] **Step 6: Run the broader optimizing + process test slice to catch regressions**

Run:
```
mvn -pl amoro-ams -am test -Dtest='TestOptimizingQueue,TestProcessFactoryRouter'
```

Expected: full green.

- [ ] **Step 7: Commit**

```bash
git add amoro-ams/src/test/java/org/apache/amoro/server/optimizing/TestOptimizingQueue.java
git commit -m "$(cat <<'EOF'
[AMORO-4200] ams: cover Router cross-format dispatch + format caching on recovery

Two focused assertions on TestOptimizingQueue:
- ProcessFactoryRouter returns the matching factory for each declared format
  and throws on unregistered formats.
- TableOptimizingProcess caches format == tableRuntime.getFormat() at
  construction.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Verification (post-implementation)

After all three tasks are committed, run these checks end-to-end:

- [ ] **Full amoro-ams test run**

```
mvn -pl amoro-ams -am test
```

Expected: BUILD SUCCESS. Any pre-existing failing tests unrelated to this change should be investigated but are not part of this scope.

- [ ] **Compile clean**

```
mvn -pl amoro-ams -am compile
```

Expected: zero warnings related to this change.

- [ ] **No lingering references**

```
grep -rn "CompositeOptimizingProcessFactory\|resolveOptimizingFactory\|NoopOptimizingProcessFactory\|getOptimizingFactory" . --include="*.java"
```

Expected: zero hits.

- [ ] **Commit-log summary**

```
git log --oneline origin/master..HEAD | head
```

Expected: three new commits on top of `77a06a978`, one per task.
