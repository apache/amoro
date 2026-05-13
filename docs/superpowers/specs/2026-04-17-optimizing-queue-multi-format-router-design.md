# OptimizingQueue Multi-Format Router — Design

- **Date:** 2026-04-17
- **Author:** ConradJam (brainstormed with Claude)
- **Issue:** AMORO-4200
- **Supersedes:** commit `77a06a978` (`[AMORO-4200] ams: CompositeOptimizingProcessFactory for multi-format routing`)

## Background

Commit `77a06a978` introduced `CompositeOptimizingProcessFactory` so that Iceberg
and Paimon `ProcessFactory` SPIs can coexist inside a single AMS instance. The
composite implements `ProcessFactory` itself and routes each call
(`createPlanner` / `createCommitter` / `trigger` / …) to a delegate keyed by
`TableFormat`.

`OptimizingQueue` treats the composite as a single opaque `ProcessFactory`. All
routing happens **inside** the composite, invisible to the queue.

## Problems with the current shape

Three concerns motivate this redesign (labels match the brainstorming session):

- **A — Opaque routing.** `OptimizingQueue` cannot tell whether a given
  `planInternal` / `buildCommit` call ended up at the Iceberg factory or the
  Paimon one. Logs, exceptions, and future metrics conflate all formats.
- **E — `buildCommit()` relies on `AmoroTable.format()` to route.**
  `TableOptimizingProcess` has no record of its own format, so the commit path
  must reload the `AmoroTable` from the catalog just to decide which
  `ProcessFactory` to invoke. This couples catalog state to routing.
- **F — Responsibility mislocated.** The composite pretends to be a
  `ProcessFactory` but its real job is "look up a factory by format". Because
  `OptimizingQueue` uses it through the `ProcessFactory` interface, adding a
  new format (Hudi, Delta) forces internal edits to the composite while the
  queue stays oblivious.

Out-of-scope concerns that were considered and explicitly deferred:

- **B** — heterogeneous task types across formats (`TableOptimizingProcess`
  hardcodes `RewriteStageTask`). No format currently needs a different
  `StagedTaskDescriptor`; revisit when one does.
- **C** — format-aware recovery bookkeeping. Current recovery works because
  `DefaultTableRuntime` already knows its format.
- **D** — per-format sub-queue / scheduling / metrics isolation. Not a
  reported pain point yet.

## Design

### Core idea

Split "which factory handles which format" out of the `ProcessFactory` facade
into a dedicated `ProcessFactoryRouter` that is **not** a `ProcessFactory`. The
router is a lookup table, nothing more. `OptimizingQueue` holds the router
directly and reads routing decisions at the call sites where they matter.

`TableOptimizingProcess` captures its format at construction time, sourced from
`tableRuntime.getFormat()`. Commit routing becomes
`router.forFormat(this.format)` — independent of the catalog reload.

### Data flow

```
Startup:
  DefaultOptimizingService(processFactories)
    → new ProcessFactoryRouter(processFactories)   // conflict detection here
    → new OptimizingQueue(..., router)             // one router shared across all queues

Plan:
  OptimizingQueue.planInternal(tableRuntime)
    → format  = tableRuntime.getFormat()
    → factory = router.forFormat(format)
    → planner = factory.createPlanner(...)
    → new TableOptimizingProcess(planResult, tableRuntime)
        // stores this.format = tableRuntime.getFormat()

Commit:
  TableOptimizingProcess.buildCommit()
    → factory = router.forFormat(this.format)      // no table.format() lookup
    → factory.createCommitter(table, ...)          // table only used as a parameter

Recovery:
  OptimizingQueue.loadProcess(tableRuntime)
    → new TableOptimizingProcess(tableRuntime, meta, state)
        // this.format = tableRuntime.getFormat()
    // no schema change: format is always derivable from tableRuntime
```

### Invariants

1. The `(format → factory)` mapping is a global singleton tied to the
   `DefaultOptimizingService` lifetime. All `OptimizingQueue` instances share
   the same router.
2. A `TableOptimizingProcess.format` field is immutable from construction
   onward. (Table format itself is immutable; no runtime change can invalidate
   the cache.)
3. An empty `processFactories` list is legal: router is empty, every
   `isFormatSupported()` returns `false`, queues idle without crashing.

### Lifecycle

- `ProcessFactoryRouter` owns no resources and has no `close()`.
- Sub-factories' `close()` is driven by `DefaultOptimizingService.dispose()`,
  which iterates `router.delegates()`. This responsibility used to live inside
  the composite; it moves up one level.

## Class-level changes

> Line numbers below refer to the repository state at the time of writing
> (commit `77a06a978`). Use them as locators, not exact addresses — nearby
> code may shift by a few lines during implementation.

### New

**`amoro-ams/src/main/java/org/apache/amoro/server/process/ProcessFactoryRouter.java`**

```java
public final class ProcessFactoryRouter {
  private final List<ProcessFactory> delegates;              // read-only snapshot
  private final Map<TableFormat, ProcessFactory> byFormat;   // read-only map

  public ProcessFactoryRouter(List<ProcessFactory> factories) {
    this.delegates = List.copyOf(factories);
    this.byFormat = buildRoutingTable(factories);            // conflict detection
  }

  public ProcessFactory forFormat(TableFormat format) {
    ProcessFactory f = byFormat.get(format);
    if (f == null) {
      throw new UnsupportedOperationException(
          "No ProcessFactory registered for table format " + format);
    }
    return f;
  }

  public Set<TableFormat> supportedFormats() { return byFormat.keySet(); }
  public List<ProcessFactory> delegates()    { return delegates; }

  private static Map<TableFormat, ProcessFactory> buildRoutingTable(
      List<ProcessFactory> factories) {
    // Copied verbatim from CompositeOptimizingProcessFactory.buildRoutingTable:
    // two factories claiming the same format → IllegalArgumentException
    // with a message naming both factory.name()s.
  }
}
```

No `open` / `close` / `trigger` / `recover` / `supportedActions` /
`triggerStrategy` — none of those are routing concerns.

**`amoro-ams/src/test/java/org/apache/amoro/server/process/TestProcessFactoryRouter.java`**

| Case | Assertion |
|------|-----------|
| `forFormat_hit` | With Iceberg + Paimon fake factories, `forFormat(ICEBERG)` and `forFormat(PAIMON)` return the matching instance. |
| `forFormat_miss` | Querying an unregistered format throws `UnsupportedOperationException`. |
| `supportedFormats_union` | Returns the union of each factory's declared formats; the returned `Set` is immutable (mutation throws). |
| `conflict_detection` | Two factories claiming `ICEBERG` → constructor throws `IllegalArgumentException`, message contains both factory names. |
| `empty_factories` | `new ProcessFactoryRouter(List.of())` succeeds; `supportedFormats()` is empty; `forFormat(any)` throws. |
| `delegates_exposure` | `delegates()` preserves insertion order, is read-only, and has size equal to the input. |

### Modified

**`amoro-ams/src/main/java/org/apache/amoro/server/optimizing/OptimizingQueue.java`**

| Change | Location |
|--------|----------|
| Field: `ProcessFactory optimizingFactory` → `ProcessFactoryRouter router` | L104 |
| Constructor param: `ProcessFactory optimizingFactory` → `ProcessFactoryRouter router` | L108-115 |
| `isFormatSupported`: `router.supportedFormats().contains(format)` | L243-254 |
| `planInternal`: `router.forFormat(tableRuntime.getFormat()).createPlanner(...)` | L409-411 |
| **New** field inside inner class `TableOptimizingProcess`: `private final TableFormat format;` | near L519 |
| Constructor 1 `TableOptimizingProcess(OptimizingPlanResult, DefaultTableRuntime)`: set `this.format = tableRuntime.getFormat();` | L567-579 |
| Constructor 2 `TableOptimizingProcess(DefaultTableRuntime, TableProcessMeta, OptimizingProcessState)`: set `this.format = tableRuntime.getFormat();` | L581-600 |
| `buildCommit()`: `router.forFormat(this.format).createCommitter(...)` | L889-904 |

**`amoro-ams/src/main/java/org/apache/amoro/server/DefaultOptimizingService.java`**

| Change | Location |
|--------|----------|
| Field: `ProcessFactory optimizingFactory` → `ProcessFactoryRouter router` | L134 |
| Constructor: `this.router = new ProcessFactoryRouter(processFactories != null ? processFactories : List.of())` | L171 |
| Startup log records `router.delegates().stream().map(ProcessFactory::name).toList()` and `router.supportedFormats()` | L181 |
| **Delete** `resolveOptimizingFactory(...)` | L429-446 |
| **Delete** inner class `NoopOptimizingProcessFactory` | L1000- |
| **Delete** `getOptimizingFactory()` (already dead code, zero callers) | L188-190 |
| `new OptimizingQueue(..., optimizingFactory)` → `new OptimizingQueue(..., router)` (two sites) | L202-210, L414-422 |
| `dispose()`: iterate `router.delegates()` and call `close()` on each, catching + logging any `Exception` (same tolerance as the composite today) | new code |
| Drop unused imports (`CompositeOptimizingProcessFactory`, possibly `TableFormat`) | imports |

**`amoro-ams/src/test/java/org/apache/amoro/server/optimizing/TestOptimizingQueue.java`**

- Swap the `ProcessFactory` constructor argument for a `ProcessFactoryRouter`;
  single-format existing cases wrap one factory in a router.
- **New** cross-format routing case: plan an Iceberg table and a Paimon table in
  the same queue; assert each hits the matching factory's planner + committer
  (spy on both).
- **New** recovery assertion: reconstruct a `TableOptimizingProcess` via
  constructor 2 and verify the cached `format` equals
  `tableRuntime.getFormat()`. Expose a package-private accessor
  `TableFormat getFormat()` on `TableOptimizingProcess` solely for this
  assertion (the field is already `final`; a getter adds no mutability).

### Deleted

- `amoro-ams/src/main/java/org/apache/amoro/server/process/CompositeOptimizingProcessFactory.java`
- `amoro-ams/src/test/java/org/apache/amoro/server/process/TestCompositeOptimizingProcessFactory.java`

## Verification

1. `mvn -pl amoro-ams -am test -Dtest='TestProcessFactoryRouter,TestOptimizingQueue'` green.
2. `mvn -pl amoro-ams -am compile` clean (no unused imports, no dangling refs).
3. Repo-wide `grep -rn "CompositeOptimizingProcessFactory\|resolveOptimizingFactory\|NoopOptimizingProcessFactory\|getOptimizingFactory" --include="*.java"` returns zero hits.
4. *(Optional, if a Paimon IT environment is available)* End-to-end smoke: AMS
   with one Iceberg table and one Paimon table bound to the same optimizer
   group; both should plan → execute → commit independently.

## Out of scope (YAGNI)

- No persistence-schema change — `format` stays derivable from
  `tableRuntime.getFormat()`.
- No per-format sub-queue / sub-scheduler / sub-metrics.
- No change to `TableOptimizingProcess`'s `RewriteStageTask` generic binding.
  Defer until a format actually needs a heterogeneous task type.
- No backward-compatibility shim / `@Deprecated` composite. The composite was
  added one day before this redesign and has no external users.

## Risks

- **Router exposed to plugins.** The router isn't a `ProcessFactory`, so any
  external caller that held `DefaultOptimizingService.getOptimizingFactory()`
  would break. Mitigation: that method has zero callers in the repo today;
  deleting it is safe.
- **Factory close() error handling moves from composite to service.** Keep the
  same tolerant behavior (swallow + log) so a buggy factory doesn't abort
  shutdown.
- **Cross-format test fakes.** `TestOptimizingQueue` needs two fake factories
  that declare distinct formats. If the existing test infrastructure only
  knows Iceberg, we add minimal Paimon-format-declaring fakes locally in the
  test file.
