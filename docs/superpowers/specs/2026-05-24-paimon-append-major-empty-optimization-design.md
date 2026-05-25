# Paimon Append 表 MAJOR 空优化拦截设计 Spec

## 目标

避免 Paimon append-only `BUCKET_UNAWARE` 表在没有新增写入时，反复触发“纯尺寸型 MAJOR 合并”，但实际只是把文件重写成几乎相同的文件数和大小分布。

用户提供的运行时样例是稳定 Append 表：一次 MAJOR 将 6 个文件重写成 6 个文件，其中 `253 MB + 96 MB`、`253 MB + 115 MB`、`253 MB + 64 MB` 这类组合重写后仍近似保持 `target + tail` 结构。由于 tail 仍小于 target，后续 planning tick 又可能继续把它识别为 undersized，从而形成周期性空优化。

## 源码级根因

当前 Amoro 对 Paimon Append 表的优化规划分为三段：

1. `PaimonAppendFileScanner` 使用 `ScanMode.ALL` 扫描最新快照中的活跃 ADD 文件，并只保留 `PaimonFileCandidate.isProblemFile()` 为 true 的文件。
2. `PaimonPartitionEvaluator` 在分区存在 `undersizedFiles` 或 `highDeleteFiles` 时返回 MAJOR；存在 small files 时返回 MINOR；FULL 由 full interval 触发。
3. `PaimonAppendTaskPacker` 将候选文件打包成 `AppendCompactTask`，随后 `PaimonCompactionExecutor` 调用 Paimon 原生 `AppendCompactTask#doCompact`。

执行器复用 Paimon 原生 rewrite 没有问题。Paimon append rewrite 的路径是 `BaseAppendFileStoreWrite.compactRewrite`，写出时经过 target-size rolling writer。对于 `target + tail` 输入，rolling writer 很自然会再次写出 `target + tail`，这不是执行失败，而是 planner 规划了低收益任务。

## 策略边界

本 Spec 只处理 MAJOR 中“没有 delete cleanup 价值的纯尺寸型 undersized 任务”。不改变 MINOR、FULL，也不改变 deletion vector 或 high-delete 的 MAJOR。

需要保留的行为：

- `MINOR`：保持现有小文件合并语义。
- `FULL`：保持 rewrite-all-files 的显式运维语义，即使文件数不下降也不被新规则过滤。
- `MAJOR + deletion vector`：保留。即使单文件重写也可能清理 DV 状态。
- `MAJOR + high delete ratio`：保留。文件数不下降时仍可能减少 delete 元数据和不可见数据负担。

需要拦截的行为：

- `MAJOR`。
- 候选任务中没有 DV、没有 high-delete 信号。
- 仅基于文件大小触发 undersized。
- 按 target-size 模型估算，重写后文件数不会少于输入文件数。

## 新策略

对每个即将发出的 MAJOR task，在 `PaimonAppendTaskPacker` 内部做收益判断：

```text
estimatedOutputFiles = ceil(sum(inputFileSize) / targetFileSize)
worthCompacting = estimatedOutputFiles < inputFileCount
```

当 `worthCompacting == false` 且任务没有 DV/high-delete 时，不发出该 `AppendCompactTask`。如果一个分区的所有候选 task 都被过滤，planner 返回空 task 列表，AMS 不创建 optimizer process。

该判断可覆盖用户样例的形状：

```text
target ~= 256 MB
253 MB + 96 MB -> ceil(349 / 256) = 2
inputFileCount = 2
skip
```

同时保留真正能降低文件数的 MAJOR：

```text
target = 256 MB
96 MB + 115 MB -> ceil(211 / 256) = 1
inputFileCount = 2
keep
```

## 规划拆分

### 1. 收益判断工具类

新增 package-private 工具类：

```text
PaimonAppendCompactBenefit
  -> shouldKeep(OptimizingType type, List<PaimonFileCandidate> candidates, PaimonPlanContext context)
```

职责：

- 只使用 scanner 已经加载的 `PaimonFileCandidate` 与 `PaimonPlanContext`。
- 不访问文件系统。
- 不执行 Paimon rewrite。
- 不预测 Parquet 编码压缩后的精确字节数。
- 对不可判断或异常边界采用 fail-open：保留任务，避免误杀有效优化。

建议规则：

1. `optimizingType != MAJOR`：返回 true。
2. `candidates == null || candidates.isEmpty()`：返回 false。
3. 任一候选文件有 `hasDeletionVector()` 或 `isHighDeleteRatio()`：返回 true。
4. 纯尺寸型 MAJOR 且 `candidates.size() <= 1`：返回 false。
5. `targetSize <= 0`：返回 true，配置异常时不由该规则误杀。
6. 汇总 `fileSize` 时如果 long 溢出风险出现：返回 true。
7. 使用 `totalSize / targetSize + (totalSize % targetSize == 0 ? 0 : 1)` 计算 ceil，避免 `totalSize + targetSize - 1` 溢出。
8. 返回 `estimatedOutputFiles < candidates.size()`。

### 2. Packer 发包点拦截

将收益判断放在 `PaimonAppendTaskPacker` 即将创建 `AppendCompactTask` 的位置，而不是放在 evaluator：

- evaluator 只有分区级文件集合，不知道最终打包组合。
- packer 知道最终 task group，能判断 `target + tail` 是否会同数重写。
- 过滤必须发生在把候选转成 `AppendCompactTask` 之前，否则会丢失 high-delete/DV 判断所需信息。

实现上可以让内部 `task(...)` 或 `taskFromUnits(...)` 返回 `null` 表示不发 task，所有调用点统一跳过 null。MINOR/FULL 调用同一入口也安全，因为 helper 对非 MAJOR 返回 true。

### 3. 单元测试迁移

现有 `TestPaimonAppendTaskPacker` 中有若干 MAJOR 测试使用“健康的大 regular file”作为输入。源码实际路径中，MAJOR evaluator 不会选择健康 oversized regular file，因为 `problemFiles(files)` 只保留 small、undersized、high-delete 或 DV 相关候选。因此这些测试不能继续作为 MAJOR 行为约束。

迁移原则：

- 如果测试目标是 packer 对 oversized regular file 的普通打包能力，将 `OptimizingType.MAJOR` 改为 `OptimizingType.FULL`。
- 如果测试目标必须覆盖 MAJOR 原子打包，保留 MAJOR，但把 oversized regular file 转成 high-delete 或 DV 文件。
- 新增纯尺寸型 MAJOR 的 regression：
  - `target + tail` 同数估算必须被过滤。
  - 多个 undersized 文件能减少文件数时必须保留。
- 不新增依赖真实 Parquet 输出大小的 planner 集成测试。空优化拦截的确定性证明应放在 `DataFileMeta.fileSize()` 可控的 helper/packer 单测。

### 4. Planner 测试调整

`TestPaimonOptimizingPlanner` 只验证稳定契约：

- MAJOR 对“真正能降低文件数的 undersized 组合”仍能规划任务。
- high-delete 促发 MAJOR 的确定性测试保留在 evaluator 或 helper 层。
- FULL planner 测试证明新 guard 不影响 rewrite-all-files。

如果现有 planner 的 `testMajorForUndersizedFiles` 依赖真实写出文件大小，需要把输入形状调整为明确可减少文件数，避免因为新 guard 正确过滤同数重写而让测试表达错误契约。

## 二次 Review：漏洞与修复措施

| 漏洞 | 风险 | 修复措施 |
|---|---|---|
| rewrite 后实际 Parquet 大小可能与输入 `DataFileMeta.fileSize()` 不一致 | 估算为可减少文件数的任务仍可能同数写出 | guard 只拦截“按 planner target-size 模型必然没有文件数收益”的任务；不声称精确预测 rewrite 结果 |
| high-delete 同数 rewrite 仍可能有价值 | 错误跳过会保留 delete 元数据或不可见数据负担 | 任一文件 high-delete 直接保留 |
| DV 单文件 rewrite 可能有价值 | 错误跳过会保留 deletion vector 状态 | 任一文件有 DV 直接保留，包括单文件 |
| FULL 同数 rewrite 可能是显式运维意图 | 过滤 FULL 会改变操作语义 | guard 只对 MAJOR 生效 |
| MINOR 也可能出现同数小文件重写 | 本次修复没有覆盖所有空优化类型 | 本次故障证据是 MAJOR；MINOR 需要单独按 small-file age/merge 语义评估，不混入本修复 |
| 只加 MAJOR interval | 降低频率但到期仍会空优化 | interval 只能作为调度降噪，不作为根因修复 |
| evaluator 级过滤 | 可能误杀同分区内部分有收益 task | 在 packer 发 task 前过滤最终 task group |
| serialization 后过滤 | 丢失 high-delete/DV 元数据 | 在 `AppendCompactTask` 创建前过滤 |
| `targetSize <= 0` | 除零或误杀 | helper fail-open，保留 task，并加单测 |
| `totalSize + targetSize - 1` 溢出 | 估算错误导致误杀或误保留 | 使用除法加余数计算 ceil，并对累加溢出 fail-open |
| 空候选/单 regular 候选 | 可能生成无意义 MAJOR | helper 返回 false；pack 调用点跳过 |
| 旧测试把健康 oversized regular file 当作 MAJOR 正常输入 | 测试会锁死错误行为 | 迁移到 FULL 或改成 high-delete/DV MAJOR |
| 过滤全部 task 后 planner/process 状态 | 可能产生空 process 或异常 | 单测确认 pack 返回空列表，planner 不创建实际 task；必要时补 planner 层空任务断言 |

## 事实信心结论

对“避免用户样例这类 MAJOR 纯尺寸型 `target + tail` 同数重写空优化”，本策略有事实上的 100% 信心，依据是：

- 触发条件、候选文件大小、target-size 在 AMS planner 阶段已经可见。
- 用户样例的 253 MB + 96 MB 这类输入按 target-size 模型不会降低文件数。
- guard 位于最终 task group 发出前，正好覆盖同数重写的决策点。
- high-delete、DV、FULL、MINOR 都显式排除，不会被该规则误伤。
- 单测可用构造的 `DataFileMeta.fileSize()` 精确锁定该决策，不依赖 Parquet 压缩和编码。

不能声称 100% 预测 Paimon rewrite 后的精确字节大小；这不是 planner 能可靠知道的事实。本方案的设计特意绕开这个不可知点，只拦截在 planner 自身 target-size 模型下没有文件数收益的 MAJOR 任务。对于“目标故障类是否会被拦截”，信心为 100%；对于“所有未来空优化形态都被覆盖”，不声称 100%，需要后续以 MINOR、FULL、delete cleanup 等独立证据继续扩展。
