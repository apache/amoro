# Paimon Compact MINOR / MAJOR / FULL Review Repair Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` to implement this plan task-by-task.
> **For Codex:** 执行时必须继续使用 Spec/TDD：先补可失败的回归测试，再做最小实现，再跑聚焦测试和相关全量 Paimon optimizing 测试。

**Goal:** 对 `dev-paimon-compact` 分支中自 `88bdac80461963021f9d635c711de80d4098494f` 起引入的 Paimon 小文件合并拆分逻辑做修复闭环：保持 `MINOR / MAJOR / FULL` 规划语义不变，修复 Paimon commit 幂等语义缺口，并用测试证明 compact 前后逻辑行数与数据内容一致。

**Architecture:** 不重写 Paimon compact 内核。Amoro 继续只负责 `scan -> evaluate -> pack -> execute -> commit` 调度闭环；executor 继续调用 Paimon `AppendCompactTask#doCompact`，committer 继续走 Paimon `StreamTableCommit`，但 retry/idempotent 场景必须使用 Paimon 提供的过滤提交 API。`PaimonAppendTaskPacker.packMajorOrFull` 的性能优化只允许复用 Paimon `BinPacking` 覆盖“每个 atomic unit 本身已合法”的等价子集，普通 regular 文件仍保留 Amoro 自定义 partner-reservation 规则。

**Tech Stack:** Java 11, Maven, JUnit 5, Apache Paimon local source at `/Users/SL/javaProject/paimon`, Amoro module `amoro-format-paimon`.

---

## 0. Scope / Branch / Non-goals

### 0.1 分支与代码范围

- 工作分支：`dev-paimon-compact`
- 代码审查范围：`88bdac80461963021f9d635c711de80d4098494f^..dev-paimon-compact`（包含起始 commit 本身）
- 当前修复落点：只修改 Paimon compact 相关实现与单测；排除当前工作区无关未提交改动、纯文档措辞问题。
- 参考内核源码：`/Users/SL/javaProject/paimon`

### 0.2 明确不做

- 不改变 `MINOR / MAJOR / FULL` 的触发分类、quota 后 type 聚合、FULL rewrite-all-files、DV atomic group、regular oversize partner-reservation 等调度语义。
- 不将 Paimon native coordinator 的 `COMPACT_AGE / REMOVE_AGE` 引入 Amoro。
- 不引入新依赖。
- 不把 primary-key、fixed bucket、clustering compact 混入本次 append-only bucket-unaware 修复。
- 不把“物理文件 bytes 前后一致”当作数据一致性证明；compact 后文件大小可能因重写、编码、统计信息变化而不同。

---

## 1. Review Evidence Summary

| Finding | Severity | Evidence | Required repair |
|---|---:|---|---|
| `PaimonTableCommit` 文档声称 `commit(commitIdentifier, messages)` 内建 replay dedupe，但 Paimon 内核明确该 API 不检查 identifier 是否已提交。 | High | Amoro `PaimonTableCommit.java:40-48` 声称幂等；实现 `PaimonTableCommit.java:128-130` 调用 `StreamTableCommit.commit`。Paimon `StreamTableCommit.java:52-76` 明确 `commit` 不检查，retry 应用 `filterAndCommit`。`TableCommitImpl.java:217-290` 与 `FileStoreCommitImpl.java:246-285` 执行过滤。 | 改为 `filterAndCommit(Collections.singletonMap(commitIdentifier, messages))`；补 replay 回归测试，证明第二次相同 `(commitUser, commitIdentifier)` 不重复 commit、不重复/丢失数据。 |
| 现有测试名声称“preserving row count”，实际只断言 data file 数量减少。 | Medium | `TestPaimonTableCommit.java:137-155` 只比较 `countDataFiles`；`countDataFiles` 只读 split dataFiles 数量。 | 添加读取 Paimon 表实际 rows 的 helper，断言 compact 前后 row count、row set/checksum 一致。 |
| executor summary 是物理文件指标，不是逻辑数据量证明。 | Medium | `PaimonCompactionExecutor.java:111-131` 输出 compacted/produced file count 与 bytes。 | 测试层用 Paimon `ReadBuilder` 读取真实 rows；不要把 summary bytes 当数据一致性验收。 |
| `packMajorOrFull` 原实现存在 O(n^2) suffix scan/bin copy 风险。 | Medium | 原逻辑 repeated `countRegularPartners`/`hasPackableAfter`/`totalSizeOfUnits` 与大 bin copy；RED test 30k small + 30k large 在 2s timeout 暴露。 | 已完成性能优化：使用 Paimon `BinPacking` 仅覆盖 DV-only 等价子集；regular path 改为 suffix lookahead + in-place bin 缩减。保留语义回归测试。 |

---

## 2. Paimon Kernel Semantics Anchors

### 2.1 Commit retry / idempotency

- `StreamTableCommit.commit(identifier, messages)`：更快，但“不检查 identifier 是否已经提交”。
- `StreamTableCommit.filterAndCommit(map)`：先过滤已提交 identifier，Paimon 注释给出的典型用途就是 retry commit。
- `TableCommitImpl.filterAndCommit` 会把 map 转成 committables，再按 identifier 排序后调用 `commit.filterCommitted`。
- `FileStoreCommitImpl.filterCommitted` 对同一 `commitUser` 找 latest snapshot，并过滤 `identifier <= latestSnapshot.commitIdentifier()` 的 committable。

**Amoro implication:** `PaimonTableCommit` 若要在 AMS retry / close 后 replay 场景中保持 Paimon commit 语义，必须使用 `filterAndCommit`；单个 identifier 的 map 足以保持现有“一次计划一个 snapshot”的意图。

### 2.2 Data preservation through compact rewrite

- Amoro executor 调用 `AppendCompactTask#doCompact(table, write)`。
- Paimon `AppendCompactTask#doCompact` 对非 DV 要求 `compactBefore.size() > 1`，DV 可以单文件；随后调用 `write.compactRewrite(...)`，返回 `compactAfter` 并封装 `CompactIncrement(compactBefore, compactAfter, ...)`。
- Paimon `BaseAppendFileStoreWrite#compactRewrite` 用 `createFilesIterator(partition, bucket, toCompact, dvFactories)` 读取 compactBefore 全部输入行，再写入 rolling writer。

**Amoro implication:** 只要 Amoro packer 不遗漏 candidate、executor 正确提交 Paimon commit message，逻辑数据应由 Paimon 内核 rewrite 保证。但本分支仍必须用 Amoro 集成测试证明 compact 前后 row count 与 row content 一致。

### 2.3 Packer performance utility boundary

- 可复用内核工具：`org.apache.paimon.utils.BinPacking.packForOrdered`。
- 不可直接替代的内核对象：`AppendCompactCoordinator.FileBin` 是 private inner class，且更偏 MINOR small-file bin，不表达 Amoro MAJOR/FULL 对 oversized regular 文件必须搭配合法 partner 的保留规则。

**Amoro implication:** `BinPacking` 只能用于“每个 atomic unit 本身可独立形成合法 task”的语义等价子集（当前 DV atomic units）。普通 regular single-file 单元不能直接交给 `BinPacking`，否则会改变 MAJOR/FULL 的合法 task 规则。

---

## 3. Implementation Tasks

### Task 0 — Freeze already-approved `packMajorOrFull` performance optimization

**Status:** 已实现，后续执行只允许在测试失败时做语义等价的小修，不允许再改变 MINOR/MAJOR/FULL 规划语义。

**Files already touched:**

- `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/plan/PaimonAppendTaskPacker.java`
- `amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan/TestPaimonAppendTaskPacker.java`

**What is allowed in this task:**

1. Keep `MINOR` path unchanged.
2. Keep MAJOR/FULL regular path semantics unchanged:
   - regular single-file atomic unit alone is illegal;
   - oversized regular input can only be emitted with at least one non-oversized partner;
   - future oversized regular files may reserve partners;
   - DV group remains atomic and may be single-file legal.
3. Keep Paimon `BinPacking.packForOrdered` only in DV-only/all-units-legal path.
4. Keep suffix lookahead arrays as performance optimization only.

**Acceptance:**

```bash
./mvnw -pl amoro-format-paimon -Dtest=TestPaimonAppendTaskPacker test
```

Expected: `Tests run: 16, Failures: 0, Errors: 0, Skipped: 0` or higher if more tests are added.

---

### Task 1 — Fix Paimon commit replay semantics with `filterAndCommit`

**Goal:** 让 `PaimonTableCommit` 的实现匹配其 retry/idempotent 设计意图与 Paimon 内核 commit 语义。

**Files:**

- `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/commit/PaimonTableCommit.java`
- `amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/commit/TestPaimonTableCommit.java`

**RED test first:**

Add a test similar to:

```java
@Test
@DisplayName("Replay with same commit user and identifier is filtered without duplicating data")
void testReplaySameCommitIdentifierIsIdempotent(@TempDir Path warehouse) throws Exception
```

Test shape:

1. Create append-only bucket-unaware table with deterministic rows, e.g. ids `0..4`.
2. Read and store logical rows before compact.
3. Plan and execute compaction tasks once.
4. Commit with `commitUser = "user-retry"`, `commitIdentifier = <planner target snapshot id or fixed monotonic id>`.
5. Read rows after first commit and assert same row count/content as before.
6. Store latest snapshot id after first commit.
7. Construct a new `PaimonTableCommit` with the **same** tasks, same user, same identifier; call `commit()` again.
8. Assert:
   - latest snapshot id did not change, or `filterAndCommit` committed count is logged as 0 if implementation exposes it only internally;
   - row count/content remains exactly the same;
   - no exception from replay.

This test should fail or expose duplicate/conflict behavior with the current direct `commit(...)` implementation.

**Implementation:**

Replace direct commit:

```java
commit.commit(commitIdentifier, messages);
```

with filtered commit:

```java
int committed = commit.filterAndCommit(Collections.singletonMap(commitIdentifier, messages));
```

Then log `committed` count. Update class Javadoc/comments so they no longer claim direct `commit(...)` dedupes.

**Notes:**

- Add `java.util.Collections` import if needed.
- Keep `commitIdentifier < 0` guard.
- Keep empty task / empty message no-op behavior.
- Do not switch to batch commit or direct FileStore APIs.

**Acceptance:**

```bash
./mvnw -pl amoro-format-paimon \
  -Dtest='TestPaimonTableCommit#testReplaySameCommitIdentifierIsIdempotent' test
```

Then:

```bash
./mvnw -pl amoro-format-paimon -Dtest=TestPaimonTableCommit test
```

---

### Task 2 — Add logical row-count and row-content consistency proof

**Goal:** 用测试证明 compact 前后行数和数据总量一致，避免只验证“文件数量减少”。

**Files:**

- `amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/commit/TestPaimonTableCommit.java`

**Evidence from Paimon kernel / tests:**

- Paimon `ReadBuilder` 官方接口说明中明确提醒：`InternalRow` 可能被 reader 内部复用，不能直接长期保存；
  测试应复制 row 或立即转成自有结构。
- Paimon `TableTestBase` 的读取模式是：
  `table.newReadBuilder()` -> `readBuilder.newRead().createReader(readBuilder.newScan().plan())` ->
  `RecordReader.forEachRemaining(...)`。
- Paimon `AppendCompactTask#doCompact` 返回的 `CommitMessageImpl` 内含
  `CompactIncrement(compactBefore, compactAfter, ...)`；其中每个 `DataFileMeta` 都有
  `rowCount()`，可作为 compact message 层的行数守恒证据。
- Amoro 当前 `PaimonCompactionExecutor` summary 只有 physical file count / bytes，不足以证明逻辑数据量；
  因此本任务必须读取真实 table rows，而不能只断言 summary。

**Preferred local helper:**

不要依赖 `/Users/SL/javaProject/paimon` 的 test helper（例如 `DataFormatTestUtil`），避免给
`amoro-format-paimon` 新增 test-jar 依赖。直接在 `TestPaimonTableCommit` 中增加本地 helper，
并在读取时立即转为稳定字符串：

```java
private static List<String> readRowStrings(Table table) throws Exception {
  ReadBuilder readBuilder = table.newReadBuilder();
  List<String> rows = new ArrayList<>();
  RecordReader<InternalRow> reader =
      readBuilder.newRead().createReader(readBuilder.newScan().plan());
  reader.forEachRemaining(
      row -> rows.add(row.getInt(0) + "|" + row.getString(1).toString()));
  Collections.sort(rows);
  return rows;
}
```

Required imports:

```java
import org.apache.paimon.data.InternalRow;
import org.apache.paimon.reader.RecordReader;
import org.apache.paimon.table.source.ReadBuilder;
```

Why this helper is preferred:

- It follows Paimon `ReadBuilder` / `TableRead` public API instead of reading manifest metadata only.
- It avoids retaining reusable `InternalRow` objects.
- It sorts rows because compact may change split/file order; ordering is not the semantic contract.
- It is enough for the deterministic schema created by `createTinyAppendTable`: `id INT, name STRING`.

**Optional row-copy helper if later schema grows:**

Add a local helper based on Paimon test patterns:

```java
private static List<InternalRow> readRows(Table table) throws Exception {
  ReadBuilder readBuilder = table.newReadBuilder();
  RecordReader<InternalRow> reader =
      readBuilder.newRead().createReader(readBuilder.newScan().plan());
  InternalRowSerializer serializer = new InternalRowSerializer(table.rowType());
  List<InternalRow> rows = new ArrayList<>();
  reader.forEachRemaining(row -> rows.add(serializer.copy(row)));
  return rows;
}
```

Then normalize rows to stable comparable strings, for example `id + "|" + name`, or sort copied `InternalRow` values by `id` and compare expected fields.

**Required visible-table assertions:**

In `testCommitMergesFiles` or a new dedicated test:

1. Read `rowsBefore` before planning/execute/commit.
2. Read `rowsAfter` after `PaimonTableCommit.commit()`.
3. Keep `afterFiles < beforeFiles` as compact effectiveness assertion.
4. Add `assertEquals(rowsBefore.size(), rowsAfter.size())`.
5. Add `assertEquals(rowsBefore, rowsAfter)`; this proves no row loss and no duplicate row for the deterministic row set.
6. If replay test from Task 1 reuses the same helper, also assert row content after replay is unchanged.

Suggested shape:

```java
List<String> rowsBefore = readRowStrings(catalog.getTable(id));
long beforeFiles = countDataFiles((AppendOnlyFileStoreTable) catalog.getTable(id));

List<PaimonCompactionTask> tasks = planAndExecute(catalog, id, 1L, 42L);
assertTrue(tasks.size() >= 1);
assertCompactMessageRowCountPreserved(tasks);

new PaimonTableCommit(
        (AppendOnlyFileStoreTable) catalog.getTable(id), tasks, "user-1", 42L)
    .commit();

long afterFiles = countDataFiles((AppendOnlyFileStoreTable) catalog.getTable(id));
List<String> rowsAfter = readRowStrings(catalog.getTable(id));

assertTrue(afterFiles < beforeFiles, "Compaction must drop file count");
assertEquals(rowsBefore.size(), rowsAfter.size(), "Compaction must preserve row count");
assertEquals(rowsBefore, rowsAfter, "Compaction must preserve row content");
```

**Required compact-message rowCount assertion:**

Add a second helper that deserializes the `CommitMessageImpl` carried by every `PaimonCompactionTask`
output and checks `CompactIncrement` row counts:

```java
private static void assertCompactMessageRowCountPreserved(List<PaimonCompactionTask> tasks)
    throws Exception {
  CommitMessageSerializer serializer = new CommitMessageSerializer();
  for (PaimonCompactionTask task : tasks) {
    PaimonCompactionOutput output = task.getOutput();
    CommitMessageImpl message =
        (CommitMessageImpl)
            serializer.deserialize(
                output.getCommitMessageVersion(), output.getCommitMessageBytes());
    long beforeRows =
        message.compactIncrement().compactBefore().stream()
            .mapToLong(DataFileMeta::rowCount)
            .sum();
    long afterRows =
        message.compactIncrement().compactAfter().stream()
            .mapToLong(DataFileMeta::rowCount)
            .sum();
    assertEquals(beforeRows, afterRows, "Compact message must preserve row count");
  }
}
```

Required imports:

```java
import org.apache.paimon.io.DataFileMeta;
import org.apache.paimon.table.sink.CommitMessageImpl;
import org.apache.paimon.table.sink.CommitMessageSerializer;
```

This assertion is not a substitute for reading table rows; it is a supporting invariant proving the
Paimon compact message itself describes a row-count-preserving rewrite.

**Acceptance:**

```bash
./mvnw -pl amoro-format-paimon \
  -Dtest='TestPaimonTableCommit#testCommitMergesFiles,TestPaimonTableCommit#testReplaySameCommitIdentifierIsIdempotent' test
```

**Review checklist for Task 2:**

- [ ] Test reads rows from `Table.newReadBuilder()` before and after commit.
- [ ] Test compares row content, not only row count.
- [ ] Test sorts normalized rows before comparison.
- [ ] Test does not retain reusable `InternalRow` without copy/normalization.
- [ ] Test includes `CompactIncrement` rowCount equality as secondary evidence.
- [ ] Test does not assert physical byte equality before/after compact.
- [ ] Test keeps the existing file-count reduction assertion.

---

### Task 3 — Verify planner semantics after performance optimization

**Goal:** 证明 `packMajorOrFull` 性能优化没有改变 MINOR/MAJOR/FULL 语义。

**Files:**

- `amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan/TestPaimonAppendTaskPacker.java`
- Existing planner/evaluator/scanner tests remain authoritative.

**Required evidence:**

Run the focused packer tests plus the Paimon planner/evaluator/scanner suite:

```bash
./mvnw -pl amoro-format-paimon \
  -Dtest='TestPaimonAppendTaskPacker,TestPaimonAppendFileScanner,TestPaimonPartitionEvaluator,TestPaimonOptimizingPlanner' test
```

Expected properties:

- MINOR FileBin tests still pass.
- DV atomic group tests still pass.
- MAJOR/FULL quota tests still pass.
- New linear performance regression test passes without timeout.

---

### Task 4 — Run final relevant verification set

**Goal:** 对本次修复相关链路做可复现验证。

**Commands:**

```bash
./mvnw -pl amoro-format-paimon \
  -Dtest='TestPaimonAppendFileScanner,TestPaimonAppendTaskPacker,TestPaimonPartitionEvaluator,TestPaimonOptimizingPlanner,TestPaimonTableCommit,TestPaimonProcessFactory,TestPaimonCompactionExecutor' test
```

```bash
git diff --check
```

If time allows before final commit:

```bash
./mvnw validate -pl amoro-format-paimon -am
```

**Expected:**

- Maven test suite passes.
- Spotless/checkstyle phases in Maven output have no violations.
- `git diff --check` has no whitespace errors.

---

## 4. Acceptance Criteria

The repair is complete only when all criteria are met:

1. `PaimonTableCommit` uses Paimon retry-safe API (`filterAndCommit`) for non-empty commit messages.
2. Replaying the same `(commitUser, commitIdentifier)` with the same task outputs is no-op from user-visible table state: no duplicate rows, no lost rows, and no additional successful snapshot if Paimon filters it before commit.
3. Compact前后真实读取出的行数一致。
4. Compact前后真实读取出的数据集合一致（至少覆盖 deterministic row set/checksum，不只看 file count）。
5. `PaimonAppendTaskPacker.packMajorOrFull` 性能优化不改变 `MINOR / MAJOR / FULL` 语义；普通 regular file path 不被 Paimon `BinPacking` 替代。
6. Final relevant verification commands pass and are recorded in the final report.

---

## 5. Risk Register

| Risk | Mitigation |
|---|---|
| `filterAndCommit` 对多个 identifier 要求排序。 | 本次只提交 singleton map；Paimon 内部仍会排序，满足约束。 |
| `commitIdentifier` 需要按 commitUser 单调。 | 继续使用 planner target snapshot id；test 覆盖 replay 同 id；不要改 commitUser 生成/持久化策略。 |
| Paimon read helper 比 file count 测试更重。 | 只放在 `TestPaimonTableCommit` 集成测试中；不影响 packer/evaluator unit tests。 |
| 性能测试超时时间在慢机器上可能抖动。 | 当前 60k units / 5s 是回归保护；若 CI 过慢，只能先降规模并保留旧实现会失败的复杂度特征，不可移除语义断言。 |
| 把 `BinPacking` 扩展到 regular units 会改变语义。 | 代码注释和测试都必须明确：只允许 DV-only/all-legal-units path 使用 Paimon utility。 |

---

## 6. Completion Report Template

最终修复汇报必须包含：

- Changed files
- 修复点与证据
- compact 前后 row count / row content 一致性的测试证据
- replay/idempotent 测试证据
- `MINOR / MAJOR / FULL` 语义未变的 packer/planner 测试证据
- 未覆盖风险（如未跑全仓库 Maven）
