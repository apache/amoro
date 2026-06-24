# Paimon 主键表 HASH Compaction 深度 Review

## 审查范围

审查基线：`a8fc6e560`。

审查目标：当前分支 `codex/hash-fxied` 到 `HEAD` 的 Paimon 主键表
`HASH_FIXED` / `HASH_DYNAMIC` 自优化实现。

对照来源：

1. Amoro 本分支实现与测试。
2. Paimon 内核源码 `/Users/SL/javaProject/paimon`。
3. Paimon Spark `CompactProcedure`。
4. 设计文档 `2026-06-18-paimon-primary-key-hash-compaction-design.md`。

本次审查使用三个并行 Agent 分别覆盖：

1. Paimon 内核 compact / commit 语义。
2. Amoro 进程路由、恢复、APPEND 隔离。
3. 测试与 Spec 一致性。

## Paimon 内核对照结论

Paimon Spark `CompactProcedure` 对 `HASH_FIXED` 和 `HASH_DYNAMIC` 使用同一套
`compactAwareBucketTable` 逻辑：通过 `SnapshotReader.bucketEntries()` 枚举
`(partition,bucket)`，在 task 内调用
`BatchTableWrite.compact(partition,bucket,fullCompact)`，driver 端汇总
`CommitMessage` 后提交。

Paimon 内核关键事实：

1. `CoreOptions.WRITE_ONLY=true` 会让主键表写入侧进入 `NoopCompactManager`；
   `NoopCompactManager.triggerCompaction(true)` 会拒绝用户触发 full compact。
   Spark Procedure 也会强制 table copy 覆盖 `write-only=false`。
2. `BatchTableWrite.compact(partition,bucket,false)` 表达 Paimon 原生 MINOR compact。
3. `BatchTableWrite.compact(partition,bucket,true)` 表达强制 full compact，可被 Amoro
   用于 MAJOR 和 FULL。
4. `StreamTableCommit.filterAndCommit` 会按 `commitUser + commitIdentifier`
   过滤 replay，是 AMS 恢复重放时更合适的提交方式。
5. `BucketEntry.fileCount` 是 public API 暴露的 bucket 文件压力指标，不等价于 Paimon
   内部 sorted run count；最终是否真正 compact 仍由 Paimon writer 判断。
6. Paimon public commit API 没有 target snapshot 条件提交能力，因此 FULL 的
   “计划后表变活跃”保护只能通过 executor 前与 committer 前双重 latest snapshot
   检查缩小竞态窗口。

## 合并等级定义

| 等级 | 触发定位 | Paimon 调用 | 候选范围 | 优先级 | 说明 |
|---|---|---|---|---:|---|
| MINOR | 日常小文件 / sorted-run 压力 | `write.compact(..., false)` | 所有满足 MINOR 压力的 bucket | 2 | 使用 Paimon 原生 non-full compact；`num-sorted-run.compaction-trigger` 显式配置优先于 Amoro `self-optimizing.minor.trigger.file-count`。 |
| MAJOR | Amoro 扩展的高压力局部 full compact | `write.compact(..., true)` | 同时满足 MINOR 资格和 MAJOR 阈值的 bucket | 1 | MAJOR 不是 Paimon Procedure 原生参数；阈值顺序为 `major.file-count-threshold > num-sorted-run.stop-trigger > effectiveMinor + 3`。 |
| FULL | 低优先级、配置驱动的冷却分区 full compact | `write.compact(..., true)` | 无 MINOR/MAJOR 候选且满足 `partition-idle-time` 的冷却 bucket | 3 | 默认不启用；必须配置表级 `paimon-optimizer.primary-key.partition-idle-time`；活跃表或计划后有新 snapshot 时 no-op。 |

优先级固定为：

```text
MAJOR > MINOR > FULL
```

## 本次修正

### 1. `partition-idle-time` 不再过滤 MINOR / MAJOR

发现：原实现先按 `partition-idle-time` 过滤所有 bucket，再判断 MINOR / MAJOR。
这会导致为了启用 FULL 冷却而配置该参数后，活跃分区的日常 MINOR 和高压力 MAJOR
也被跳过。

修正：`partition-idle-time` 只在 FULL 路径筛选冷却 bucket；MINOR / MAJOR 继续按
bucket 压力处理全部候选。

新增测试：

1. `partitionIdleTimeDoesNotFilterMinorCandidates`
2. `partitionIdleTimeDoesNotFilterMajorCandidates`

### 2. FULL commit 前增加 stale snapshot 防线

发现：executor 执行 FULL 前已检查 latest snapshot，但 executor 输出 commit messages
之后、AMS committer 提交之前，如果表产生新 snapshot，旧 FULL messages 仍可能被提交。

修正：committer 从 task input 校验一致的 `optimizingType` 与 `targetSnapshotId`；
如果是 FULL，则在 `filterAndCommit` 前再次校验 latest snapshot。不一致时直接 no-op。
同时 committer 会先校验并反序列化 success task output，再判断 stale FULL no-op，
避免“成功任务没有输出”这类框架不变量被 stale FULL 分支吞掉。
如果 success task 非空但 committer 缺少 `FileStoreTable`，也会显式拒绝，避免进入
未包装的空指针路径。

新增测试：

1. `committerSkipsStaleFullOutputWhenSnapshotChangedAfterExecute`
2. `committerRejectsStaleFullMissingOutput`
3. `committerRejectsMissingTable`

### 3. 补齐 MAJOR 默认阈值与 FULL 冷却空集测试

新增测试：

1. `majorDefaultThresholdUsesEffectiveMinorPlusThree`
2. `fullWithNoColdBucketsReturnsEmptyPlan`

## 逐文件审核

| 文件 | 审核结论 |
|---|---|
| `amoro-ams/src/main/java/org/apache/amoro/server/optimizing/OptimizingQueueWarmupMetrics.java` | 新增 warmup 指标类；与 Paimon 主键链路无直接耦合。审查发现 close/submit 并发下指标可能存在轻微污染风险，属于关闭路径 minor 风险，不影响主键 compact 语义。 |
| `amoro-ams/src/main/java/org/apache/amoro/server/optimizing/OptimizingQueueWarmupService.java` | 新增 warmup 服务；未改变 Paimon APPEND planner/executor/committer。保留 minor 风险：executor close 与 submit 并发时 pending 计数可能不回落。 |
| `amoro-ams/src/main/java/org/apache/amoro/server/persistence/converter/TaskDescriptorRecoveryTypes.java` | 已注册 Paimon APPEND 与 Paimon primary-key task/input/output/summary 恢复类型；满足 AMS recovery 要求。 |
| `amoro-ams/src/test/java/org/apache/amoro/server/optimizing/TestPaimonOptimizingE2E.java` | 覆盖 Paimon 优化 E2E、restart/replay 场景；主键链路的 process factory 和 commit replay 已被纳入 AMS 层验证。 |
| `amoro-ams/src/test/java/org/apache/amoro/server/process/paimon/TestPaimonExpireSnapshotProcess.java` | 仅适配 warmup / Paimon 测试上下文；未改变主键 compact 语义。 |
| `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/PaimonTable.java` | APPEND pending 统计仍限定 `AppendOnlyFileStoreTable + BUCKET_UNAWARE`；主键表只通过 enabled 条件进入 optimizing 队列，不复用 APPEND pending 估算。 |
| `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/PaimonTableDescriptor.java` | 暴露 `MAJOR` 类型用于优化历史筛选；现有 snapshot 推断仍只能兜底 FULL/MINOR，MAJOR 应优先依赖 Amoro process runtime/history。 |
| `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/plan/PaimonPrimaryKeyOptimizingPlanner.java` | 主键 HASH planner 独立于 APPEND planner；支持 HASH_FIXED/HASH_DYNAMIC 同一路径；已修正 `partition-idle-time` 只作用于 FULL；优先级和阈值符合 Spec。 |
| `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonBucketCompactionUnit.java` | 只保存 public `BucketEntry` 指标与 serialized partition；没有依赖 Paimon 内部 level/run 类。 |
| `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyCompactionExecutor.java` | 使用 `write-only=false` table copy；MINOR 映射 `fullCompaction=false`，MAJOR/FULL 映射 `true`；FULL 执行前 stale snapshot no-op。 |
| `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyCompactionExecutorFactory.java` | 独立 executor factory；不影响 APPEND `PaimonCompactionExecutorFactory`。 |
| `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyCompactionInput.java` | 包含 table、units、optimizingType、fullCompaction、targetSnapshotId、commitUser、commitIdentifier；满足 executor 与 committer 校验需要。 |
| `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyCompactionOutput.java` | 输出 commit messages 与 compact 指标；空 commit message list 视为 no-op。保留 minor 风险：如果 Paimon 返回非空但内部 empty 的 `CommitMessageImpl`，计划输入指标可能大于实际产出。 |
| `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyCompactionTask.java` | 独立 task 类型，executor factory 属性指向主键 executor；summary 与 output 聚合接入 Amoro task 契约。 |
| `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyOptions.java` | enabled 默认 false；`max-buckets-per-task` 默认 16；`partition-idle-time` 支持 Paimon duration 和 ISO-8601；major 阈值有合法性校验。 |
| `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyTableCommit.java` | 使用 `write-only=false` copy 与 `StreamTableCommit.filterAndCommit`；校验 table、commit identity、optimizingType、targetSnapshotId 和 success task output；FULL 提交前 stale snapshot no-op。 |
| `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/process/PaimonProcessFactory.java` | planner 按 `PaimonPrimaryKeyOptimizingPlanner.supports` 路由；committer 按 task 类型分组，混合 APPEND/primary-key task 拒绝；APPEND 继续走原 committer。 |
| `amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/TestPaimonTableDescriptorOptimizingProcesses.java` | 覆盖 Paimon descriptor 暴露 FULL/MAJOR/MINOR；保留说明：snapshot 兜底推断不制造 MAJOR。 |
| `amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/TestPaimonTablePendingInputEligibility.java` | 覆盖 primary-key 默认不进入队列、enabled 后 HASH_FIXED/HASH_DYNAMIC 可进入队列、APPEND 行为不变。 |
| `amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/plan/TestPaimonPrimaryKeyOptimizingPlanner.java` | 覆盖 MINOR、MAJOR、FULL、HASH_DYNAMIC、filter 拒绝、阈值优先级、任务打包、冷却语义；本次新增四个 planner 回归测试。 |
| `amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/primary/TestPaimonPrimaryKeyCompactionExecutor.java` | 覆盖 executor 校验、MINOR/MAJOR 执行、write-only=true、FULL stale executor no-op。 |
| `amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/primary/TestPaimonPrimaryKeyCompactionOutput.java` | 覆盖 output summary 与序列化语义。 |
| `amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/primary/TestPaimonPrimaryKeyCompactionTask.java` | 覆盖 task 序列化、executor factory、summary 文案与 output 还原。 |
| `amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/primary/TestPaimonPrimaryKeyOptions.java` | 覆盖默认配置、显式配置、Paimon duration 解析和非法值拒绝。 |
| `amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/optimizing/primary/TestPaimonPrimaryKeyTableCommit.java` | 覆盖 commit、replay filter、missing table、missing output、stale FULL missing output、empty messages no-op、FULL stale commit no-op。 |
| `amoro-format-paimon/src/test/java/org/apache/amoro/formats/paimon/process/TestPaimonProcessFactory.java` | 覆盖 primary-key planner/committer 路由、非法 task 混合拒绝、APPEND 路由不变。 |
| `docs/superpowers/plans/2026-06-18-paimon-primary-key-hash-compaction.md` | 实施计划已同步修正 FULL 双重 stale 检查和 `partition-idle-time` 只作用于 FULL 的语义。 |
| `docs/superpowers/specs/2026-06-17-paimon-hash-compaction-exploration.md` | 早期探索文档，记录 Paimon compact public API 与 Spark Procedure 参考，不作为最终实现契约。 |
| `docs/superpowers/specs/2026-06-18-paimon-primary-key-hash-compaction-design.md` | 最终 Spec 已同步本次审查结论：MINOR/MAJOR 不受 FULL 冷却参数过滤，FULL executor/committer 双重 stale no-op。 |

## 保留权衡

### MAJOR stale snapshot 不 no-op

审查 Agent 建议 MAJOR 也按 target snapshot stale no-op。最终没有采纳，原因：

1. 用户历史确认的“活跃表 no-op”约束明确针对 FULL。
2. MAJOR 是高压力局部 full compact，应尽量推进优化；持续写入表如果按 stale snapshot
   直接 no-op，可能长期无法处理高压力 bucket。
3. Paimon writer 会基于当前 bucket 状态执行 compact；MAJOR 的候选粒度是
   `(partition,bucket)`，不是固定文件列表。

该选择的代价是：MAJOR process 的 planned metrics 可能与实际 compact 的最新 bucket
状态存在偏差。此风险已在 Spec 中限定为 Amoro 侧 file-count 压力近似，真实 compact
选择交给 Paimon。

### MAJOR no-op backoff

`lastMajorOptimizingTime` 当前不参与 planner 节流。若 fileCount 近似误触发但 Paimon
返回 no-op，可能产生重复 MAJOR process。第一版没有新增 MAJOR 专属 interval，因为当前
没有明确表级配置，且高压力场景应优先优化。后续如观察到重复 no-op，可新增主键表私有
MAJOR backoff 参数。

### `LOG.warn` 断言

实现已在危险误配置路径输出 `LOG.warn`，但测试主要断言行为结果。由于当前测试运行时
SLF4J 绑定为 no-op，日志断言收益较低；如后续引入稳定 test logger，可补充日志断言。

## 验证结果

本次审查已执行：

```bash
./mvnw test -pl amoro-format-paimon -Dtest=TestPaimonPrimaryKeyOptimizingPlanner,TestPaimonPrimaryKeyCompactionExecutor,TestPaimonPrimaryKeyTableCommit
./mvnw test -pl amoro-format-paimon
./mvnw test -pl amoro-format-paimon,amoro-ams -Dtest=TestPaimonProcessFactory,TestPaimonOptimizingE2E,TestDefaultOptimizingService,TestOptimizingQueue
./mvnw -pl amoro-format-paimon -DskipTests validate
```

其中 `amoro-format-paimon` 全量测试覆盖 226 个用例，包含 APPEND 表优化既有
Planner / Executor / Committer 测试；跨模块 AMS 验证覆盖 71 个用例。
