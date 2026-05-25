# Paimon Append MAJOR 空优化拦截实施 Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.
>
> 中文说明：执行本计划时按任务逐项推进。每完成一个 checkbox 就运行对应验证，不要一次性改完再补测。

**目标：** 阻止 Paimon append-only `BUCKET_UNAWARE` 表发出“纯尺寸型、预计不会减少文件数”的 MAJOR compact task，从而避免用户样例中的周期性空优化。

**架构：** 在 `PaimonAppendTaskPacker` 发出 `AppendCompactTask` 前引入 package-private 收益判断工具类。工具类只过滤没有 DV、没有 high-delete 信号、且按 target-size 估算不会减少文件数的 MAJOR group；MINOR、FULL、DV、high-delete 路径保持原行为。

**技术栈：** Java 11、Maven、JUnit 5、Apache Paimon append `AppendCompactTask`、Amoro `amoro-format-paimon` planner package。

---

## 修改文件

- 新增：`amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/plan/PaimonAppendCompactBenefit.java`
- 修改：`amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/plan/PaimonAppendTaskPacker.java`
- 新增测试：`amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan/TestPaimonAppendCompactBenefit.java`
- 修改测试：`amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan/TestPaimonAppendTaskPacker.java`
- 修改测试：`amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan/TestPaimonOptimizingPlanner.java`
- 视情况检查：`amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan/TestPaimonPartitionEvaluator.java`

## Task 1：新增 MAJOR 收益判断工具类

**文件：**

- 新增：`amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/plan/PaimonAppendCompactBenefit.java`
- 新增测试：`amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan/TestPaimonAppendCompactBenefit.java`

- [ ] **Step 1：先写失败单测，锁定收益判断契约**

创建 `TestPaimonAppendCompactBenefit.java`，至少覆盖以下用例：

```text
MAJOR 纯尺寸，预计输出文件数不下降：skip
MAJOR 纯尺寸，预计输出文件数下降：keep
MAJOR high-delete，即使文件数不下降：keep
MAJOR deletion vector，即使单文件：keep
MINOR 纯尺寸同数估算：keep
FULL 纯尺寸同数估算：keep
MAJOR 空候选：skip
MAJOR targetSize <= 0：keep
```

建议测试辅助构造：

```java
private static PaimonPlanContext context() {
  Map<String, String> options = new HashMap<>();
  options.put("target-file-size", "1000 b");
  options.put("compaction.small-file-ratio", "0.7");
  options.put("compaction.delete-ratio-threshold", "0.2");
  return PaimonPlanContext.forOptions(
      CoreOptions.fromMap(options),
      new OptimizingConfig().setMaxTaskSize(4096L),
      0L,
      0L,
      0L,
      1.0,
      4096L,
      10_000L);
}

private static PaimonFileCandidate candidate(
    PaimonPlanContext context, DataFileMeta file, DeletionFile deletionFile) {
  return PaimonFileCandidate.from(
      BinaryRow.EMPTY_ROW,
      file,
      context,
      deletionFile,
      deletionFile == null ? null : deletionFile.path());
}

private static DataFileMeta file(String name, long size, long rows, Long deleteRows) {
  return DataFileMeta.create(
      name,
      size,
      rows,
      DataFileMeta.EMPTY_MIN_KEY,
      DataFileMeta.EMPTY_MAX_KEY,
      SimpleStats.EMPTY_STATS,
      SimpleStats.EMPTY_STATS,
      0L,
      0L,
      0L,
      DataFileMeta.DUMMY_LEVEL,
      deleteRows,
      null,
      null,
      null,
      null,
      null);
}
```

关键断言示例：

```java
assertFalse(
    PaimonAppendCompactBenefit.shouldKeep(
        OptimizingType.MAJOR,
        Arrays.asList(
            candidate(context, file("near-target", 990L, 100L, null), null),
            candidate(context, file("tail", 300L, 100L, null), null)),
        context));

assertTrue(
    PaimonAppendCompactBenefit.shouldKeep(
        OptimizingType.MAJOR,
        Arrays.asList(
            candidate(context, file("left", 400L, 100L, null), null),
            candidate(context, file("right", 450L, 100L, null), null)),
        context));
```

- [ ] **Step 2：运行新测试，确认先失败**

运行：

```bash
./mvnw -pl amoro-format-paimon -Dtest=TestPaimonAppendCompactBenefit test
```

预期：失败，错误应为 `cannot find symbol: variable PaimonAppendCompactBenefit` 或等价编译失败。

- [ ] **Step 3：实现最小 helper**

新增 `PaimonAppendCompactBenefit.java`：

```java
package org.apache.amoro.formats.paimon.optimizing.plan;

import org.apache.amoro.optimizing.OptimizingType;

import java.util.List;

final class PaimonAppendCompactBenefit {

  private PaimonAppendCompactBenefit() {}

  static boolean shouldKeep(
      OptimizingType optimizingType,
      List<PaimonFileCandidate> candidates,
      PaimonPlanContext context) {
    if (optimizingType != OptimizingType.MAJOR) {
      return true;
    }
    if (candidates == null || candidates.isEmpty()) {
      return false;
    }
    if (candidates.stream()
        .anyMatch(file -> file.hasDeletionVector() || file.isHighDeleteRatio())) {
      return true;
    }
    int inputFileCount = candidates.size();
    if (inputFileCount <= 1) {
      return false;
    }

    long totalSize = 0L;
    for (PaimonFileCandidate candidate : candidates) {
      long fileSize = candidate.fileSize();
      if (fileSize < 0 || Long.MAX_VALUE - totalSize < fileSize) {
        return true;
      }
      totalSize += fileSize;
    }

    long targetSize = context.targetSize();
    if (targetSize <= 0L) {
      return true;
    }

    long estimatedOutputFiles =
        totalSize / targetSize + (totalSize % targetSize == 0L ? 0L : 1L);
    return Math.max(1L, estimatedOutputFiles) < inputFileCount;
  }
}
```

注意点：

- 不使用 `(totalSize + targetSize - 1) / targetSize`，避免 long 溢出。
- `fileSize < 0`、累加溢出、`targetSize <= 0` 都 fail-open，保留任务。
- 该 helper 不依赖真实文件系统，不读取 Parquet，不执行 Paimon compact。

- [ ] **Step 4：运行 helper 单测**

运行：

```bash
./mvnw -pl amoro-format-paimon -Dtest=TestPaimonAppendCompactBenefit test
```

预期：通过。

## Task 2：在 Packer 发包点接入 guard，并迁移 Packer 测试

**文件：**

- 修改：`amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/plan/PaimonAppendTaskPacker.java`
- 修改测试：`amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan/TestPaimonAppendTaskPacker.java`

- [ ] **Step 1：新增用户样例形状的失败回归测试**

在 `TestPaimonAppendTaskPacker` 中新增：

```java
@Test
void majorPackingSkipsPureUndersizedNoBenefitBin() {
  PaimonPlanContext context = context(4096L);
  PaimonPartitionEvaluation evaluation =
      evaluation(
          OptimizingType.MAJOR,
          candidates(context, file("near-target", 990, 100), file("tail", 300, 100)));

  List<AppendCompactTask> tasks = new PaimonAppendTaskPacker(context).pack(evaluation);

  assertTrue(tasks.isEmpty());
}
```

该测试对应运行时 `253 MB + 96 MB -> 253 MB + 96 MB` 的同数重写形状，只是用更小的字节数让断言稳定。

- [ ] **Step 2：新增可减少文件数的 MAJOR 保留测试**

新增：

```java
@Test
void majorPackingKeepsPureSizeBasedReducingBin() {
  PaimonPlanContext context = context(4096L);
  PaimonPartitionEvaluation evaluation =
      evaluation(
          OptimizingType.MAJOR,
          candidates(context, file("left", 400, 100), file("right", 450, 100)));

  List<AppendCompactTask> tasks = new PaimonAppendTaskPacker(context).pack(evaluation);

  assertEquals(1, tasks.size());
  assertEquals(2, tasks.get(0).compactBefore().size());
}
```

- [ ] **Step 3：运行 packer 测试，确认新增 skip 测试先失败**

运行：

```bash
./mvnw -pl amoro-format-paimon -Dtest=TestPaimonAppendTaskPacker test
```

预期：`majorPackingSkipsPureUndersizedNoBenefitBin` 失败，因为当前代码仍会发出同数 MAJOR task。

- [ ] **Step 4：在 task 创建入口接入 helper**

将 `PaimonAppendTaskPacker` 内部创建 task 的方法改为可返回 null：

```java
private AppendCompactTask task(
    PaimonPartitionEvaluation evaluation, List<PaimonFileCandidate> candidates) {
  if (!PaimonAppendCompactBenefit.shouldKeep(
      evaluation.optimizingType(), candidates, context)) {
    return null;
  }
  return new AppendCompactTask(
      evaluation.partition(),
      candidates.stream().map(PaimonFileCandidate::file).collect(Collectors.toList()));
}
```

所有调用点都用相同模式跳过 null：

```java
AppendCompactTask task = task(evaluation, bin);
if (task != null) {
  tasks.add(task);
}
```

`packDeletionVectorGroupsForMinor`、`packMinor`、`taskFromUnits` 等路径可以复用同一入口。非 MAJOR 不会被过滤。

- [ ] **Step 5：让 `taskFromUnits(...)` 也保持 null-safe**

建议实现：

```java
private AppendCompactTask taskFromUnits(
    PaimonPartitionEvaluation evaluation, List<AtomicUnit> units) {
  List<PaimonFileCandidate> candidates =
      units.stream().flatMap(unit -> unit.files().stream()).collect(Collectors.toList());
  return task(evaluation, candidates);
}
```

调用方只在返回值非 null 时 add。

- [ ] **Step 6：迁移不真实的 MAJOR oversized regular 测试**

检查 `TestPaimonAppendTaskPacker` 中类似下列测试：

```text
majorPackingKeepsEarlierLegalBinWhenLastRegularNeedsPartner
majorPackingPairsMultipleOverLimitRegularFilesWithReservedPartners
majorPackingSkipsUnpairedOverLimitRegularFiles
majorPackingDoesNotPairOverLimitRegularFilesTogetherWhenPartnersAreInsufficient
```

迁移规则：

- 如果测试需要健康 oversized regular file，改成 `OptimizingType.FULL`。
- 如果测试必须保持 MAJOR，则把 oversized regular file 标记为 high-delete 或带 DV。
- 不再让“健康 oversized regular file 是 MAJOR 正常输入”成为测试契约。

- [ ] **Step 7：运行 packer 测试**

运行：

```bash
./mvnw -pl amoro-format-paimon -Dtest=TestPaimonAppendTaskPacker test
```

预期：通过。

## Task 3：迁移 Planner 测试到新 MAJOR 契约

**文件：**

- 修改测试：`amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan/TestPaimonOptimizingPlanner.java`
- 视情况检查：`amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan/TestPaimonPartitionEvaluator.java`

- [ ] **Step 1：调整 `testMajorForUndersizedFiles` 的输入形状**

保留该测试对“MAJOR 仍能规划 undersized 文件”的断言，但确保测试数据对应可减少文件数的组合。目标是让它表达新契约：

```java
assertEquals(OptimizingType.MAJOR, result.getOptimizingType());
assertFalse(result.getTasks().isEmpty());
```

如果当前测试依赖真实 append 写出后的 Parquet 文件大小，需要调整写入次数、payload 大小或 target 配置，使最终 packer 估算能减少文件数。不要让测试偶然落入 `target + tail` 同数形状。

- [ ] **Step 2：不要新增依赖 Parquet 输出大小的 no-benefit planner 集成测试**

在 packer 回归测试附近加简短注释：

```java
// Keep the no-benefit MAJOR regression at DataFileMeta level because exact Parquet
// output sizes vary with encoding and compression. The planner delegates final task
// emission to PaimonAppendTaskPacker, so this test is the deterministic proof for
// the observed target-plus-tail shape.
```

原因：planner 集成测试若依赖真实 Parquet 压缩后大小，会不稳定。空优化拦截的确定性边界是 `DataFileMeta.fileSize()` 与 target-size 模型。

- [ ] **Step 3：保留 high-delete MAJOR 证据**

确认已有或新增单测覆盖：

```text
PaimonPartitionEvaluator：high-delete 能触发 MAJOR
PaimonAppendCompactBenefit：high-delete MAJOR 不因同数估算被过滤
PaimonAppendTaskPacker：high-delete MAJOR 可以发 task
```

如果没有稳定方式通过 append-table 写入构造 high-delete 元数据，不要新增脆弱的文件系统级测试。

- [ ] **Step 4：运行 planner/evaluator 测试**

运行：

```bash
./mvnw -pl amoro-format-paimon -Dtest=TestPaimonOptimizingPlanner,TestPaimonPartitionEvaluator test
```

预期：通过。

## Task 4：集中验证

- [ ] **Step 1：运行 Paimon planner 相关焦点测试**

运行：

```bash
./mvnw -pl amoro-format-paimon -Dtest='TestPaimonAppendCompactBenefit,TestPaimonAppendTaskPacker,TestPaimonPartitionEvaluator,TestPaimonOptimizingPlanner' test
```

预期：通过。

- [ ] **Step 2：运行 executor/commit 冒烟测试，证明执行路径未被破坏**

运行：

```bash
./mvnw -pl amoro-format-paimon -Dtest='TestPaimonCompactionExecutor,TestPaimonTableCommit' test
```

预期：通过。

- [ ] **Step 3：运行格式与 diff 检查**

运行：

```bash
git diff --check
```

如果失败来自本任务以外的既有 dirty 文件，改用只检查本次修改文件：

```bash
git diff --check -- \
  amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/plan/PaimonAppendCompactBenefit.java \
  amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/plan/PaimonAppendTaskPacker.java \
  amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan/TestPaimonAppendCompactBenefit.java \
  amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan/TestPaimonAppendTaskPacker.java \
  amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan/TestPaimonOptimizingPlanner.java
```

预期：本任务文件无 whitespace error。

## Task 5：二次 Review 与 100% 信心循环

执行完 Task 1-4 后，不直接提交，先做二次 Review。

- [ ] **Review 1：行为矩阵核对**

逐项确认测试覆盖：

```text
MINOR 纯尺寸小文件：不变
FULL rewrite-all：不变
MAJOR 纯尺寸，同数估算：跳过
MAJOR 纯尺寸，文件数下降估算：保留
MAJOR high-delete：保留
MAJOR deletion vector：保留
MAJOR 空候选：跳过
MAJOR targetSize <= 0：fail-open 保留
```

任一项没有测试或没有明确源码路径，就回到对应 Task 补测。

- [ ] **Review 2：旧错误假设清理**

搜索测试中是否仍把健康 oversized regular file 当作 MAJOR 正常输入：

```bash
rg -n "MAJOR.*large|large.*MAJOR|healthy oversized|oversized.*MAJOR|MAJOR.*oversized" \
  amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan
```

预期：不存在会锁定错误 MAJOR 假设的断言。若命中的是注释，确认注释是在解释迁移原因，而不是保留旧契约。

- [ ] **Review 3：确认没有把 interval 当成根因修复**

搜索：

```bash
rg -n "lastMajor|major.*interval|interval.*major" \
  amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/plan \
  amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan
```

预期：本修复不依赖新增 MAJOR interval。interval 可以后续单独作为调度降噪，但不能替代 task benefit guard。

- [ ] **Review 4：确认 guard 不晚于 task serialization**

检查 `PaimonAppendTaskPacker`：

```text
PaimonFileCandidate -> PaimonAppendCompactBenefit.shouldKeep -> AppendCompactTask
```

不得出现：

```text
AppendCompactTask -> 反查 DataFileMeta/DV/high-delete -> filter
```

否则 high-delete/DV 元数据可能已经丢失，需回滚到发 task 前过滤。

- [ ] **Review 5：确认 fail-open 边界**

逐项检查 helper：

```text
targetSize <= 0：return true
fileSize < 0：return true
totalSize 累加溢出：return true
非 MAJOR：return true
DV/high-delete：return true
```

这些边界的目标是“不确定时保留任务”，避免新规则误杀真实优化。

- [ ] **Review 6：确认同数重写故障类已被事实锁死**

以用户样例映射：

```text
input files = [253 MB, 96 MB]
target ~= 256 MB
estimatedOutputFiles = 2
inputFileCount = 2
helper -> false
packer -> 不发 AppendCompactTask
planner -> 若无其他 task，则无 optimizer process
```

如果任一环节没有测试或源码证据，补充对应测试后重新运行 Task 4。

- [ ] **Review 7：外部不可知点声明**

确认代码和测试没有假设“rewrite 后 Parquet 字节数一定等于输入估算”。本策略只使用 target-size 模型拦截确定无文件数收益的 task，不预测精确输出大小。

二次 Review 通过标准：

```text
上述 7 个 Review 全部通过
Task 4 两组 Maven 测试通过
本任务 diff 无 whitespace error
没有发现未测试的行为矩阵项
没有发现对 Parquet 精确输出大小的依赖
```

只有满足这些条件，才能声明对“目标故障类”有事实上的 100% 信心。

## Task 6：提交准备

- [ ] **Step 1：只 stage 本任务文件**

运行：

```bash
git add \
  amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/plan/PaimonAppendCompactBenefit.java \
  amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/plan/PaimonAppendTaskPacker.java \
  amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan/TestPaimonAppendCompactBenefit.java \
  amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan/TestPaimonAppendTaskPacker.java \
  amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan/TestPaimonOptimizingPlanner.java
```

不要 stage 既有 unrelated dirty 文件。

- [ ] **Step 2：使用 Lore commit message**

建议提交信息：

```text
Prevent no-benefit MAJOR rewrites for append tables

Paimon append compaction rewrites through a target-size rolling writer.
For pure undersized MAJOR groups shaped as target plus tail, rewrite can
produce the same number of files and leave the tail eligible for another
MAJOR process. The planner now keeps those size-only tasks only when the
target-size estimate predicts a file-count reduction, while preserving
delete-vector, high-delete, MINOR, and FULL behavior.

Constraint: Paimon rewrite output size cannot be known without executing the task
Rejected: Add only a MAJOR interval | reduces frequency but does not remove empty optimization
Rejected: Disable undersized MAJOR globally | skips valid file-count-reducing rewrites
Confidence: high
Scope-risk: narrow
Directive: Do not apply this guard to FULL or delete-cleanup MAJOR paths without re-checking Paimon rewrite semantics
Tested: amoro-format-paimon focused planner, executor, and commit tests
Not-tested: production HDFS table replay
```

## 当前信心结论

在编写本 Plan 时，我不会声称对“所有 Paimon compact 输出字节数”有 100% 信心，因为真实 Parquet rewrite 会受编码、压缩、统计信息和 writer rolling 细节影响。

但对本策略覆盖用户报告的故障类，即“MAJOR、纯尺寸型、`target + tail`、预计输出文件数不下降、无新增写入后仍反复触发”，有事实上的 100% 信心。原因是该故障类的全部判定条件都位于 AMS planner 的可见数据内，并且新 guard 正好在最终 task group 发出前执行。

若执行过程中二次 Review 发现以下任一漏洞，必须回到相应 Task 修复并重新验证：

```text
有 MAJOR 同数纯尺寸 task 仍能发出
high-delete 或 DV MAJOR 被误过滤
FULL 或 MINOR 被误过滤
helper 依赖真实 Parquet 输出大小
旧测试仍把健康 oversized regular file 当作 MAJOR 正常输入
guard 在 task serialization 之后才执行
targetSize/overflow 边界没有 fail-open
```

以上漏洞清零后，才能对“避免 MAJOR 空优化”这个新策略给出事实上的 100% 信心。
