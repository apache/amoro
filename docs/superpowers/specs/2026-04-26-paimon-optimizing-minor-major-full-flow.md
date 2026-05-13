# Paimon Optimizing MINOR / MAJOR / FULL 全流程说明

## Review 范围

本文件记录 Paimon append-only `BUCKET_UNAWARE` 表在 Amoro 中对齐
`MINOR / MAJOR / FULL` 优化语义后的完整调度流程、实现方案和全量 commit review 结论。

本次 review 覆盖 commit range：

```text
Base: ce0d71a489645353664d10f778f893311c512246
Head: 78513b4dd683498123763ff692499852f40ecd78
```

包含的 feature commits（功能实现完成后的 9 个提交）：

```text
88bdac804 feat(paimon): model optimizing plan context
382ad7f81 feat(paimon): evaluate optimizing partition type
80219de2e feat(paimon): pack append optimizing tasks
0a3fa50d1 feat(paimon): scan append files for optimizing
564735448 feat(paimon): plan minor major full optimizing
24d5e33a8 feat(paimon): pass optimizing runtime config
54577b682 test(paimon): verify planner task compatibility
59172596a chore(paimon): align optimizing planner docs
78513b4dd fix(paimon): align file scan limit semantics
```

本轮全量 review 后，又补充了一个 quota/type 修正：`OptimizingPlanResult.optimizingType`
现在按 quota 后实际释放任务重新聚合，避免未释放任务提前提升本轮 process type。该修正与本文档会作为后续提交保存。

对照资料：

- 设计 Spec：`/Users/SL/javaProject/amoro/docs/superpowers/specs/2026-04-25-paimon-optimizing-minor-major-full-design.md`
- 执行 Plan：`/Users/SL/javaProject/amoro/docs/superpowers/plans/2026-04-25-paimon-optimizing-minor-major-full.md`
- Paimon 内核：`/Users/SL/javaProject/paimon/paimon-core/src/main/java/org/apache/paimon/append/AppendCompactCoordinator.java`
- Paimon 任务：`/Users/SL/javaProject/paimon/paimon-core/src/main/java/org/apache/paimon/append/AppendCompactTask.java`
- Iceberg 语义：`amoro-format-iceberg/src/main/java/org/apache/amoro/optimizing/plan/CommonPartitionEvaluator.java`

## 结论

当前实现满足 Spec 的核心功能要求：

- Paimon planner 不再直接调用 `AppendCompactCoordinator.run()` 作为最终任务来源。
- Amoro 侧扫描 Paimon active ADD files，并按 Paimon 表配置复刻关键文件治理语义。
- `MINOR / MAJOR / FULL` 三类 optimizing type 已落到 Paimon append-only planner。
- `FULL` 支持问题分区内全量 ADD files 覆盖，也支持只覆盖问题文件。
- executor 和 committer 路径保持原有 Paimon `AppendCompactTask`、`CommitMessage`、
  `StreamTableCommit` 执行提交链路。
- quota 截断后，`OptimizingPlanResult.optimizingType` 反映本 tick 实际释放任务的最高类型，
  避免 metrics 和 last optimizing time 记录被未释放任务提前提升。
- planner 包内已形成 context、candidate、scanner、evaluator、packer、orchestrator 的边界，
  后续可以自然提升为不同 Paimon 表类型的接口实现。

保留的刻意差异：

- Amoro planner 不保留 Paimon coordinator 的跨 tick `SubCoordinator.age` 内存状态。
  每轮基于 latest snapshot 重新扫描和重新规划。
- Paimon 原生 append coordinator 没有 Amoro `FULL` 概念。当前 `FULL` 是 Amoro 类型体系上的增强：
  只在问题分区触发，必要时覆盖分区内全部 ADD files。
- `MAJOR` 的 `DataFileMeta.deleteRowCount()` 兜底是 Amoro 兼容增强。Paimon 原生
  `tooHighDeleteRatio` 只使用 deletion vector maintainer。

## Paimon 原生 Append Compact 流程

### 1. Coordinator 构造

`AppendCompactCoordinator` 只支持无主键 append 表：

```text
checkArgument(table.primaryKeys().isEmpty())
```

构造时读取的关键配置：

| Paimon 配置概念 | CoreOptions 方法 | 原生用途 |
|---|---|---|
| target file size | `targetFileSize(false)` | FileBin 内容阈值和写出目标 |
| small file boundary | `compactionFileSize(false)` | `target-file-size * compaction.small-file-ratio` |
| delete ratio threshold | `compactionDeleteRatioThreshold()` | DV 删除比例触发 compact |
| open file cost | `splitOpenFileCost()` | FileBin 估算输入成本 |
| min file num | `compactionMinFileNum()` | trailing bin 最小文件数 |
| file num limit | `compactionFileNumLimit()` | 单轮扫描 ADD 文件数上限 |
| deletion vectors | `deletionVectorsEnabled()` | 是否加载 append DV maintainer |

### 2. run 调度

原生 `run()` 是两段式：

```text
run()
  -> scan()
  -> compactPlan()
```

`scan()` 从 latest snapshot 读 manifest entries。`FilesIterator.next()` 会跳过
`FileKind.DELETE`，只返回 ADD 文件；每轮最多读取 `compaction.file-num-limit`
个 ADD 文件。读取结果按 partition 聚合后进入 `notifyNewFiles(partition, files)`。

`notifyNewFiles` 会过滤出真正需要 compact 的文件：

```text
file.fileSize() < compactionFileSize
  || tooHighDeleteRatio(partition, file)
```

其中 `tooHighDeleteRatio` 在 DV 开启时判断：

```text
deletionFile != null
  && (cardinality == null || cardinality > file.rowCount() * deleteThreshold)
```

### 3. SubCoordinator 组包

每个 partition 一个 `SubCoordinator`，内部维护 `toCompact` set。

非 DV 模式：

```text
sort by fileSize asc
for each file:
  total += fileSize + openFileCost
  if bin.size > 1 && total >= targetFileSize * 2:
    emit task
after loop:
  if bin.size >= minFileNum:
    emit task
```

DV 模式：

- 先按关联 index file path 分组。
- 同一个 DV index group 不能拆到多个 compact task。
- 带 DV 的 group 即使不足普通 `minFileNum` 也要尽量 compact。
- 无 DV 的 rest 文件仍走普通 FileBin。

### 4. AppendCompactTask 执行

`AppendCompactTask.doCompact(table, write)` 是本次实现继续复用的核心执行单元：

- 非 DV 表要求 `compactBefore.size() > 1`。
- DV 表允许单文件 compact。
- 执行 `write.compactRewrite(...)` 生成 `compactAfter`。
- DV 模式会维护新增和删除的 index files。
- 返回 `CommitMessageImpl`，后续由 Paimon commit path 提交。

## Amoro 当前实现流程

### 1. ProcessFactory 创建 planner

入口在 `PaimonProcessFactory.createPlanner(...)`。

创建 planner 时传入：

- `PaimonTable`
- Amoro table id
- 新生成的 process id
- `availableCore`
- optimizer group 的 `maxInputSizePerThread`
- `OptimizingConfig`
- last minor / major / full optimizing time
- partition filter 扩展位，当前为 `null`

`OptimizingConfig` 优先来自 `TableRuntime.getTableConfiguration().getOptimizingConfig()`；
当 table configuration 不可用且 runtime 是 `OptimizationContext` 时，fallback 到
`OptimizationContext.getOptimizingConfig()`。

`paimon-optimizer.enabled` 当前默认 `false`，作为 Paimon optimizing 的灰度开关。

### 2. Planner 表类型保护

`PaimonOptimizingPlanner.isNecessary()` 先校验：

- 原始表必须是 `AppendOnlyFileStoreTable`
- bucket mode 必须是 `BUCKET_UNAWARE`

不满足时直接返回 false。primary-key 表、fixed bucket 表和无 snapshot 表不会进入计划。

### 3. PaimonPlanContext 配置映射

`PaimonPlanContext` 将 Paimon 表配置映射为 planner 文件语义，将 Amoro 配置映射为调度语义。

Paimon 主导文件语义：

| Paimon 配置 | 当前实现变量 | 用途 |
|---|---|---|
| `target-file-size` | `targetSize` | undersized 上界、FileBin 阈值、pack 目标来源 |
| `compaction.small-file-ratio` | `smallFileBoundary` | 小文件边界 |
| `compaction.min.file-num` | `minorMinFileNum` | MINOR trailing bin 触发 |
| `compaction.file-num-limit` | `fileNumLimit` | 单轮扫描 ADD 文件数上限 |
| `compaction.delete-ratio-threshold` | `deleteRatioThreshold` | MAJOR 删除比例触发 |
| `source.split.open-file-cost` | `openFileCost` | MINOR FileBin 输入估算 |
| `deletion-vectors.enabled` | `coreOptions.deletionVectorsEnabled()` | DV maintainer 与单文件 compact 合法性 |

Amoro 主导调度语义：

| Amoro 配置/运行态 | 当前用途 |
|---|---|
| `self-optimizing.full.trigger.interval` | 判断 FULL 周期 |
| `self-optimizing.full.rewrite-all-files` | FULL 是否覆盖问题分区全部 ADD files |
| `self-optimizing.max-task-size-bytes` | MAJOR/FULL pack 输入上限 |
| optimizer group `maxInputSizePerThread` | MAJOR/FULL pack 输入上限 |
| `self-optimizing.minor.trigger.interval` | small files 数量不足时的 MINOR 时间兜底 |
| `availableCore` | 单 tick task quota |
| last minor/major/full time | interval 判断 |

刻意不使用 Iceberg 的 `fragment-ratio`、`min-target-size-ratio`、
`minor.trigger.file-count`、`major.trigger.duplicate-ratio` 来覆盖 Paimon 文件语义。
Paimon append 表以自身 options 表达文件治理，Amoro 只将这些语义映射到 process type。

### 4. PaimonAppendFileScanner 扫描 ADD files

`PaimonAppendFileScanner.scan()` 当前流程：

```text
check bucket unaware
latestSnapshot == null -> empty
scanCandidateFiles(snapshot)
if full interval reached && fullRewriteAllFiles:
  scanAllFilesInPartitions(snapshot, problemPartitions)
return ScanResult(snapshotId, filesByPartition)
```

候选扫描只读取 ADD files：

```text
table.store().newScan()
  .withSnapshot(snapshot)
  .withKind(ScanMode.ALL)
  .withManifestEntryFilter(entry -> entry.kind() == FileKind.ADD)
```

`compaction.file-num-limit` 按 Paimon 原生语义限制“可 compact 的 ADD 文件数”。
Paimon 内核 `AppendCompactCoordinator.FilesIterator` 会先通过
`shouldCompact(partition, file)` 过滤 small file 和高删除率文件，再进入
`fileNumLimit` 循环计数。Amoro 侧对应为先构造 `PaimonFileCandidate`，只在
`candidate.isProblemFile()` 为 true 时消耗额度；健康 ADD 文件不会占用扫描配额。

对于每个 ADD file，scanner 构造 `PaimonFileCandidate`：

- partition copy
- `DataFileMeta`
- 是否 small
- 是否 undersized
- 是否 high-delete-ratio
- DV deletion file
- DV group key

DV 开启时，通过：

```text
BaseAppendDeleteFileMaintainer.forUnawareAppend(indexFileHandler, snapshot, partition)
```

读取 deletion file 和 cardinality。

### 5. 文件画像和类型判断

`PaimonFileCandidate` 的分类：

```text
small:
  fileSize < smallFileBoundary

undersized:
  smallFileBoundary <= fileSize < targetSize

high-delete-ratio:
  DV deletionFile exists &&
    (cardinality == null || cardinality > rowCount * deleteRatioThreshold)
  OR DataFileMeta.deleteRowCount exists &&
    deleteRowCount > rowCount * deleteRatioThreshold
```

`PaimonPartitionEvaluator` 以 partition 为单位决策：

```text
if full interval reached && partition has problem files:
  type = FULL
else if has undersized files or high-delete-ratio files:
  type = MAJOR
else if small files can produce legal compact:
  type = MINOR
else:
  no optimizing
```

同一 partition 只使用最高优先级类型：

```text
FULL > MAJOR > MINOR
```

这与 Iceberg `CommonPartitionEvaluator` 的聚合方向对齐：存在更重类型时，分区和 plan result
都提升到更重 process type。

### 6. MINOR 语义

MINOR 表示小文件收敛，候选文件是：

```text
fileSize < smallFileBoundary
```

触发来源：

- small files 数量满足 `compaction.min.file-num`
- 或者 FileBin 满足 `bin.size > 1 && sum(fileSize + openFileCost) >= targetSize * 2`
- 或者 small files 数量大于 1 且达到 Amoro minor interval 兜底
- 或者存在 DV small file，交给 DV packer 尽量形成合法任务

MINOR packer 复刻 Paimon FileBin 的核心计算：

```text
sort by fileSize asc
sum += fileSize + openFileCost
if bin.size > 1 && sum >= targetSize * 2:
  emit AppendCompactTask
if trailing bin.size >= compaction.min.file-num:
  emit AppendCompactTask
```

### 7. MAJOR 语义

MAJOR 表示比 MINOR 更重的文件质量修复。

候选文件包括：

- undersized files：`smallFileBoundary <= fileSize < targetSize`
- high-delete-ratio files：DV cardinality 或 `deleteRowCount` 超过阈值
- 同分区内 small files 可并入 MAJOR，避免本轮同一文件或同一分区重复规划

MAJOR 使用 Amoro 执行约束 pack：

```text
inputLimit = min(valid self-optimizing.max-task-size-bytes,
                 valid maxInputSizePerThread)
```

非 DV task 仍必须至少 2 个文件；DV task 可以单文件。

### 8. FULL 语义

FULL 是当前 Paimon planner 中最重的重合并类型，优先级最高。

触发条件：

```text
full.trigger.interval >= 0
  && planTime - lastFullOptimizingTime > full.trigger.interval
  && partition has small / undersized / high-delete-ratio files
```

FULL 不默认全表重写，只处理问题分区。

文件选择：

- `full.rewrite-all-files=false`：只选择问题文件。
- `full.rewrite-all-files=true`：对命中的问题分区二次扫描，选择该分区全部 ADD files。

这满足“FULL 一般是较重合并，能够触达覆盖分区内全部或大范围文件”的要求，同时避免无问题分区被无差别重写。

### 9. DV 原子组和 task packing

`PaimonAppendTaskPacker` 在 pack 前先拆分：

```text
dvGroupKey != null -> DV atomic group
dvGroupKey == null -> regular file
```

DV atomic group 不拆分，避免同一 deletion/index file 被多个 compact task 重写。

MAJOR/FULL 组包按 size 升序处理 atomic units：

- 当前 bin 加入下一 unit 超过 input limit 且当前 bin 合法时，先输出当前 bin。
- 单个 DV atomic group 超过 limit 时跳过并 WARN，保留 DV 原子性。
- regular 单文件不能单独输出，因为 Paimon 非 DV `AppendCompactTask.doCompact()` 要求输入文件数大于 1。
- 如果 oversized regular file 找不到合法 partner，则跳过并 WARN。

### 10. Quota 和 OptimizingPlanResult

`PaimonOptimizingPlanner.plan()` 将 packer 输出的 `AppendCompactTask` 连同 partition evaluation
type 一起经过 quota：

```text
cap = max(1, ceil(availableCore))
release first cap tasks
defer others to next tick by not persisting them
```

quota 不拆分、不合并、不修改 `AppendCompactTask.compactBefore()`，避免破坏 Paimon compact 原子任务。

最终每个任务包装为 `PaimonCompactionTask`：

- 使用 `AppendCompactTaskSerializer` 序列化。
- `PaimonCompactionInput` 携带 table、serialized task、serializer version、commit user、partition path、target snapshot id。
- task properties 设置 `PaimonCompactionExecutorFactory`，供 optimizer 侧加载执行器。

plan result 的 `optimizingType` 按本 tick 实际释放任务的最高优先级聚合：

```text
存在 FULL -> FULL
否则存在 MAJOR -> MAJOR
否则存在 MINOR -> MINOR
否则 empty
```

这个规则避免在 quota 只释放 MINOR task 时，因为后续未释放的 MAJOR/FULL task 导致本轮
plan result 被提前提升。

### 11. Executor 和 Committer

执行提交路径保持不变：

```text
PaimonCompactionTask
  -> PaimonCompactionInput
  -> PaimonCompactionExecutor
  -> deserialize AppendCompactTask
  -> AppendCompactTask.doCompact(table, write)
  -> serialized CommitMessage
  -> PaimonTableCommit
  -> StreamTableCommit.commit(identifier, messages)
```

`commitUser` 每次 plan 生成一次，并写入每个 task input。AMS 重启后任务通过
`TaskFilesPersistence` 恢复时能复用同一个 `commitUser`。

`targetSnapshotId` 作为 Paimon commit identifier 传给 committer，保持当前 Paimon commit
幂等语义。

## 需求覆盖矩阵

| 需求 | 当前状态 | 依据 |
|---|---|---|
| 不再直接调用 `AppendCompactCoordinator.run()` 产出最终任务 | 已满足 | planner 使用 scanner/evaluator/packer 自建 `AppendCompactTask` |
| 扫描 ADD files 并按 Paimon 配置复刻关键语义 | 已满足 | `PaimonAppendFileScanner`、`PaimonPlanContext`、`PaimonFileCandidate` |
| 支持 small-file-size、min-file-num、open-file-cost | 已满足 | context 映射 + MINOR FileBin pack tests |
| 支持 `compaction.file-num-limit` | 已满足 | 按可 compact/problem ADD 文件计数，健康 ADD 文件不消耗 limit |
| 支持 DV 删除比例 | 已满足 | DV cardinality 优先，`deleteRowCount` 兜底 |
| 拆分 MINOR / MAJOR / FULL | 已满足 | `PaimonPartitionEvaluator` 和 planner type 聚合 |
| FULL 能覆盖分区内全部或大范围文件 | 已满足 | `full.rewrite-all-files=true` 二次扫描问题分区全部 ADD files |
| 保留 evaluator 边界的小类结构 | 已满足 | context/candidate/evaluation/evaluator/scanner/packer 分离 |
| 后续可兼容不同 Paimon 表类型 | 部分满足 | 当前用 package-private 小类沉淀边界；后续可提升接口 |
| task packing 与 Amoro quota/task-size 衔接 | 已满足 | packer 处理 inputLimit，planner applyQuota 控制单 tick task 数，result type 按 quota 后任务聚合 |
| executor/committer 不改路径 | 已满足 | 仍使用 Paimon 原生 `AppendCompactTask` 和 `CommitMessage` |
| 测试覆盖核心行为 | 已满足 | 新增 plan context/evaluator/scanner/packer/planner/factory tests，并扩展执行提交兼容测试 |

## 和 Paimon 内核的一致与替代

一致点：

- append-only 无主键语义。
- bucket unaware compact task 使用 `AppendCompactTask`。
- small file boundary 来自 Paimon `compactionFileSize(false)` 或等价
  `target-file-size * compaction.small-file-ratio`。
- `compaction.file-num-limit` 限制可 compact/problem ADD 文件数，健康 ADD 文件不消耗额度。
- `source.split.open-file-cost` 参与 FileBin 内容估算。
- `compaction.min.file-num` 参与 trailing bin 判断。
- DV 删除比例按 deletion file cardinality 判断，`cardinality == null` 视为需要 compact。
- DV index/deletion file 相关文件保持原子组。
- 非 DV task 至少 2 个文件，DV task 可以单文件。

刻意替代：

- 原生 coordinator 的 `COMPACT_AGE`、`REMOVE_AGE` 不映射。Amoro 不保留 coordinator 内存状态，下一 tick 重新扫描。
- 原生 coordinator 只表达 compact 候选，不表达 Amoro process type。当前实现将候选文件映射到 `MINOR / MAJOR / FULL`。
- 原生 coordinator 不提供 FULL interval 和 rewrite-all-files。当前实现由 Amoro `OptimizingConfig` 控制。
- 原生 `tooHighDeleteRatio` 只看 DV；当前实现增加 `DataFileMeta.deleteRowCount()` 兜底。
- 原生 coordinator 的 batch remain files 状态不持久化；当前通过 Amoro 下一 tick 重新规划替代。

## Review 结论

全量 commit 对照 Spec 后，当前实现没有发现 Critical 或 Important 缺口。subagent review
发现的 quota 后 `planResult.optimizingType` 可能代表未释放任务的问题已修正，并补充
`testPlanTypeFollowsQuotaReleasedTasks` 回归测试。

生产就绪判断：Yes, with documented constraints。

原因：

- 核心语义已按 Spec 落地，且关键 Paimon 内核语义已有对应测试。
- 执行提交路径保持原 Paimon task/commit API，降低 executor 和 committer 风险。
- 当前范围明确限制在 append-only + bucket unaware，未把 primary-key、fixed bucket、
  clustering compact 等未来需求混入第一版。

剩余约束：

- 后续升级 Paimon 版本时，需要重点 diff `AppendCompactCoordinator` 的 FileBin、DV grouping、
  scan limit 和 delete-ratio 逻辑。
- 当前 planner 边界还是 package-private 小类，不是公共接口。接入 primary-key 或 fixed bucket 时，
  应再提炼 `PaimonFileScanner`、`PaimonPartitionEvaluator`、`PaimonTaskPacker` 接口。
- FULL rewrite-all-files 可能带来较大 IO，生产上需要结合 quota、task input limit 和 optimizer group
  并发进行灰度。

## 验证记录

本 feature 最近一次完成的验证命令：

```bash
./mvnw -pl amoro-format-paimon -Dtest='TestPaimonOptimizingPlanner#testPlanTypeFollowsQuotaReleasedTasks' test
./mvnw -pl amoro-format-paimon -Dtest='TestPaimonAppendFileScanner#healthyAddFilesDoNotConsumeFileNumLimit,TestPaimonAppendFileScanner#fullRewriteAllFilesScansAllFilesInProblemPartition' test
./mvnw -pl amoro-format-paimon -Dtest=TestPaimonAppendFileScanner test
./mvnw -pl amoro-format-paimon -Dtest=TestPaimonOptimizingPlanner test
./mvnw -pl amoro-format-paimon test
./mvnw -pl amoro-ams -am -Dtest='TestPaimonOptimizingE2E,TestOptimizingQueueMultiFormat,TestPaimonDashboardSummary' test
./mvnw validate -pl amoro-format-paimon -am
```

验证结果：

- quota/type 回归测试：1 test，0 failures，0 errors。
- scan limit/FULL 覆盖回归测试：2 tests，0 failures，0 errors。
- `TestPaimonAppendFileScanner`：7 tests，0 failures，0 errors。
- `TestPaimonOptimizingPlanner`：15 tests，0 failures，0 errors。
- `amoro-format-paimon`：125 tests，0 failures，0 errors。
- AMS Paimon 相关测试：13 tests，0 failures，0 errors。
- `validate -pl amoro-format-paimon -am`：BUILD SUCCESS，checkstyle 0 violations。
