# Paimon Append 表 MAJOR 空优化修复总结

## 背景

用户提供的 Snapshot 记录显示，Append 表在没有新增数据写入时仍不定期触发 MAJOR 优化。一次典型优化将 6 个 BASE_FILE 删除并新增 6 个 BASE_FILE，新旧文件大小几乎一致，例如：

```text
253 MB + 96 MB  -> 253 MB + 96 MB
253 MB + 115 MB -> 253 MB + 115 MB
253 MB + 64 MB  -> 253 MB + 64 MB
```

这类优化没有降低文件数，也没有证据表明存在 delete cleanup 收益，因此属于 MAJOR 纯尺寸型空优化。

## 改进前

改进前的规划链路是：

```text
PaimonAppendFileScanner
  -> PaimonFileCandidate.isProblemFile()
  -> PaimonPartitionEvaluator
  -> PaimonAppendTaskPacker
  -> AppendCompactTask
  -> PaimonCompactionExecutor
  -> Paimon AppendCompactTask#doCompact
```

其中 `PaimonPartitionEvaluator` 只要看到 `undersizedFiles` 或 `highDeleteFiles` 就会返回 MAJOR。`PaimonAppendTaskPacker` 只负责把候选文件组成合法 task，不判断该 task 对文件数是否有收益。

因此当 task 输入形状是 `target + tail` 时，Paimon 原生 rolling writer 很自然会再次写出 `target + tail`。这不是 Paimon 执行失败，而是 Amoro planner 发出了低收益 MAJOR task。

## 问题点

1. **MAJOR 判定粒度太粗**

   分区级别存在 undersized 文件并不等价于最终 task group 有收益。收益必须在 packer 形成最终 task group 后判断。

2. **纯尺寸型 MAJOR 缺少文件数收益判断**

   对没有 DV、没有 high-delete 的 MAJOR task，如果满足：

   ```text
   ceil(sum(inputFileSize) / targetFileSize) >= inputFileCount
   ```

   则按 planner 自身 target-size 模型无法降低文件数，继续发 task 会导致同数重写。

3. **旧单测包含不真实 MAJOR 输入**

   部分 `TestPaimonAppendTaskPacker` 用健康 oversized regular file 构造 MAJOR 测试。但真实 evaluator 不会选择健康 oversized regular file 作为 MAJOR problem file，这些测试会把错误假设锁死。

4. **不能依赖真实 Parquet 输出字节数**

   rewrite 后字节数受编码、压缩和 writer rolling 影响，不能作为稳定单测断言。空优化防护应使用 `DataFileMeta.fileSize()` 和 target-size 模型在 planner 层做确定性判断。

## 改进后

新增 `PaimonAppendCompactBenefit`，在 `PaimonAppendTaskPacker` 创建 `AppendCompactTask` 前判断是否保留 task。

保留路径：

- `MINOR`：不改变。
- `FULL`：不改变。
- `MAJOR + deletion vector`：保留，即使单文件。
- `MAJOR + high-delete`：保留，即使文件数不下降。
- `MAJOR` 纯尺寸型且预计输出文件数小于输入文件数：保留。

过滤路径：

- `MAJOR`。
- 无 DV。
- 无 high-delete。
- 纯尺寸型 undersized group。
- 按 target-size 估算不会减少文件数。

核心判断：

```text
estimatedOutputFiles = ceil(sum(inputFileSize) / targetFileSize)
keep = estimatedOutputFiles < inputFileCount
```

边界处理采用 fail-open：

- `targetSize <= 0`：保留。
- `fileSize < 0`：保留。
- `totalSize` 累加溢出风险：保留。
- 非 MAJOR：保留。
- DV/high-delete：保留。

## 测试调整

新增测试：

- `TestPaimonAppendCompactBenefit`
  - MAJOR 纯尺寸同数估算跳过。
  - MAJOR 纯尺寸可减少文件数保留。
  - high-delete MAJOR 同数估算保留。
  - DV MAJOR 单文件保留。
  - MINOR/FULL 不被过滤。
  - MAJOR 空候选跳过。
  - `targetSize <= 0` fail-open 保留。

- `TestPaimonAppendTaskPacker`
  - `majorPackingSkipsPureUndersizedNoBenefitBin`
  - `majorPackingKeepsPureSizeBasedReducingBin`
  - `majorPackingKeepsHighDeleteNoBenefitBin`

迁移测试：

- 原先依赖健康 oversized regular file 的 MAJOR packer 测试迁移为 FULL 测试。
- 保留 packer 对 oversized regular file 配对算法的覆盖，但不再表达“健康 oversized regular file 是 MAJOR 正常输入”的错误契约。

## 验证结果

已运行：

```bash
./mvnw -pl amoro-format-paimon -Dtest=TestPaimonAppendCompactBenefit test
```

结果：

```text
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
```

已运行：

```bash
./mvnw -pl amoro-format-paimon -Dtest=TestPaimonAppendTaskPacker test
```

结果：

```text
Tests run: 19, Failures: 0, Errors: 0, Skipped: 0
```

已运行：

```bash
./mvnw -pl amoro-format-paimon -Dtest=TestPaimonOptimizingPlanner,TestPaimonPartitionEvaluator test
```

结果：

```text
Tests run: 26, Failures: 0, Errors: 0, Skipped: 0
```

已运行：

```bash
./mvnw -pl amoro-format-paimon -Dtest='TestPaimonAppendCompactBenefit,TestPaimonAppendTaskPacker,TestPaimonPartitionEvaluator,TestPaimonOptimizingPlanner' test
```

结果：

```text
Tests run: 52, Failures: 0, Errors: 0, Skipped: 0
```

已运行：

```bash
./mvnw -pl amoro-format-paimon -Dtest='TestPaimonCompactionExecutor,TestPaimonTableCommit' test
```

结果：

```text
Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
```

已运行：

```bash
git diff --check -- \
  amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/plan/PaimonAppendTaskPacker.java \
  amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan/TestPaimonAppendTaskPacker.java
```

结果：无输出。该命令只覆盖已跟踪的本次修改文件。

已运行：

```bash
rg -n "[ \t]+$" \
  amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/plan/PaimonAppendCompactBenefit.java \
  amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/plan/PaimonAppendTaskPacker.java \
  amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan/TestPaimonAppendCompactBenefit.java \
  amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan/TestPaimonAppendTaskPacker.java \
  docs/superpowers/specs/2026-05-24-paimon-append-major-empty-optimization-design.md \
  docs/superpowers/plans/2026-05-24-paimon-append-major-empty-optimization-plan.md \
  docs/analysis/2026-05-24-paimon-append-major-empty-optimization-summary.md
```

结果：无输出，覆盖新增未跟踪文件。

## 二次 Review 结论

| 检查项 | 结论 |
|---|---|
| MINOR 是否被改变 | 未改变，helper 对非 MAJOR 直接保留 |
| FULL 是否被改变 | 未改变，helper 对非 MAJOR 直接保留 |
| MAJOR 纯尺寸同数估算 | 已跳过，helper 和 packer 均有测试 |
| MAJOR 纯尺寸文件数下降估算 | 已保留，helper 和 packer 均有测试 |
| MAJOR high-delete | 已保留，helper、packer、evaluator 均有覆盖 |
| MAJOR DV | 已保留，helper 和既有 packer 测试覆盖 |
| 旧 oversized regular MAJOR 假设 | 已迁移为 FULL 测试 |
| 是否依赖新增 MAJOR interval | 否 |
| 是否在 serialization 后过滤 | 否，过滤发生在 `AppendCompactTask` 创建前 |
| 是否依赖真实 Parquet 输出大小 | 否 |

## 信心边界

对“MAJOR、纯尺寸型、无 DV、无 high-delete、`target + tail`、预计输出文件数不下降”的空优化故障类，本修复有事实上的 100% 信心。该故障类的所有判断条件都在 AMS planner 发 task 前可见，并且测试已锁定 helper 与 packer 两层行为。

不声明对所有未来空优化形态有 100% 覆盖。例如 MINOR interval fallback、FULL 显式 rewrite、真实 Parquet rewrite 后大小波动，都属于不同问题边界，应单独基于证据设计策略。
