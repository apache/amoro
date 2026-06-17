# Paimon 主键表 HASH_FIXED / HASH_DYNAMIC 自优化设计

## 背景

当前 Amoro 的 Paimon 自优化链路只覆盖 APPEND 表中的 `AppendOnlyFileStoreTable + BUCKET_UNAWARE` 场景。主键表在 Paimon 内部基于 LSM / MergeTree 组织文件，`HASH_FIXED` 和 `HASH_DYNAMIC` 的 compact 粒度是 `(partition,bucket)`。本设计扩展 Amoro 对 Paimon 主键表的自优化能力，支持 `HASH_FIXED` 和 `HASH_DYNAMIC`，并在 Amoro 侧明确 `MINOR / MAJOR / FULL` 三类优化语义。

设计目标：

1. 支持 Paimon 主键表 `HASH_FIXED` 和 `HASH_DYNAMIC` 的自优化。
2. 在 Amoro 层定义 `MINOR / MAJOR / FULL`，其中 `MAJOR` 是 Amoro 扩展语义。
3. 最大限度复用 Paimon public compact API，不直接依赖 Paimon MergeTree 内部实现。
4. 保持现有 APPEND 表优化链路行为不变。
5. 对危险误配置拒绝规划并输出明确 `LOG.warn`，避免用户误以为配置生效。

## 已核实的源码事实

### Amoro 当前 Paimon 链路

当前 Paimon APPEND 自优化由以下组件构成：

1. `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/PaimonTable.java`
2. `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/process/PaimonProcessFactory.java`
3. `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/plan/PaimonOptimizingPlanner.java`
4. `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/PaimonCompactionExecutor.java`
5. `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/PaimonTableCommit.java`

现有链路对 APPEND 表有强约束：

1. 只接受 `AppendOnlyFileStoreTable`。
2. 只接受 `BucketMode.BUCKET_UNAWARE`。
3. task payload 使用 Paimon `AppendCompactTask`。
4. executor / committer 中存在 APPEND 表强转。

因此主键表不能直接复用现有 APPEND task、executor、committer。

### Paimon Spark Procedure

Paimon Spark `CALL sys.compact` 对 `HASH_FIXED` 和 `HASH_DYNAMIC` 共用 `compactAwareBucketTable`：

1. 使用 `SnapshotReader.bucketEntries()` 枚举当前 snapshot 中真实存在的 `(partition,bucket)`。
2. 使用 partition filter 和 `partition_idle_time` 过滤 partition。
3. 在 Spark task 内创建 `BatchTableWrite`。
4. 循环调用 `write.compact(partition,bucket,fullCompact)`。
5. `prepareCommit()` 后由 driver 汇总 commit messages。
6. 使用 `BatchTableCommit.commit(messages)` 提交。

这一模型证明主键表优化可以采用“一个 task 内处理多个 bucket，一次 prepareCommit”的执行方式。

### Paimon Spark Producer

Spark Producer 写入路由会区分 bucket mode：

1. `HASH_DYNAMIC`：首次写入走 `SimpleHashBucketAssigner`，已有 snapshot 后走 `DynamicBucketProcessor`。
2. `HASH_FIXED`：优先用 fixed bucket UDF 计算 bucket 并 shuffle，否则走 `CommonBucketProcessor`。

但 full compaction producer 不按 `HASH_FIXED` / `HASH_DYNAMIC` 拆两套 compact 逻辑：

1. `DataWrite` 通过 `full-compaction.delta-commits` 判断是否触发 full compaction。
2. 写入过程中记录本次写入触达的 `writtenBuckets`。
3. `preCommit()` 中对这些 bucket 循环调用 `write.compact(partition,bucket,true)`。
4. 提交仍使用 `BatchTableCommit.commit(messages)`。

对 Amoro 的启发：

1. `HASH_FIXED` / `HASH_DYNAMIC` 的 compact 执行层可以共用同一套主键表实现。
2. Producer 的候选来源是本次写入触达 bucket；Amoro 自优化没有该上下文，因此需要从 `SnapshotReader.bucketEntries()` 枚举候选。
3. `MAJOR` 可以借鉴 Producer 的“局部 full compaction”模型：只对局部高压力 bucket 执行 `fullCompaction=true`。

### Paimon LSM Compaction

Paimon 主键表 normal compaction 由 `UniversalCompaction` / `ForceUpLevel0Compaction` 等策略决定。关键参数包括：

1. `num-sorted-run.compaction-trigger`
2. `num-sorted-run.stop-trigger`
3. `compaction.max-size-amplification-percent`
4. `compaction.size-ratio`
5. `compaction.force-up-level-0`
6. lookup changelog producer 对 L0 的强制 compact 策略

`BucketEntry` 只包含 bucket 级轻量元数据：

1. `partition`
2. `bucket`
3. `recordCount`
4. `fileSizeInBytes`
5. `fileCount`
6. `lastFileCreationTime`

第一版不精确模拟 Paimon 内部 LSM level/run 选择，只使用 `BucketEntry` 做轻量候选判断；真正 compact 的文件选择交给 Paimon `write.compact(...)`。

## 范围

### 支持范围

第一版只支持：

1. Paimon 主键表。
2. `BucketMode.HASH_FIXED`。
3. `BucketMode.HASH_DYNAMIC`。
4. Amoro metadata trigger 驱动的自优化。

`HASH_FIXED` 和 `HASH_DYNAMIC` 必须显式支持，并在主键表内部共用同一套 planner / executor / committer。这里的“共用”只指主键表内部共用，不表示和 APPEND 表共用现有 APPEND 链路。

### 不支持范围

第一版不支持：

1. `KEY_DYNAMIC`
2. `POSTPONE_MODE`
3. 非主键表的 HASH bucket 自优化
4. 主键表 `self-optimizing.filter`
5. 主键表 partial commit
6. 在 commit 冲突后当前 process 内重新 plan / re-execute
7. 精确读取 Paimon MergeTree level / sorted run 细节做 Amoro 侧决策

## 总体方案

采用独立主键表优化链路，复用 Paimon public compact API。

新增组件：

1. `PaimonPrimaryKeyOptimizingPlanner`
2. `PaimonPrimaryKeyCompactionTask`
3. `PaimonBucketCompactionUnit`
4. `PaimonPrimaryKeyCompactionExecutor`
5. `PaimonPrimaryKeyCompactionOutput`
6. `PaimonPrimaryKeyTableCommit` 或等价的主键表专用 committer
7. 主键表 executor factory 和 task descriptor recovery 映射

保留 APPEND 表现有组件和行为：

1. `PaimonOptimizingPlanner`
2. `PaimonCompactionTask`
3. `PaimonCompactionExecutor`
4. `PaimonCompactionOutput`
5. `PaimonTableCommit`

可复用但不能改变语义的能力：

1. `ProcessFactory` / `OptimizingPlanResult` / `StagedTaskDescriptor` 契约
2. `CommitMessageSerializer`
3. metrics summary 聚合思路
4. `PaimonPlanContext` 中通用配置读取，必要时抽取公共 context
5. Amoro 现有 process / task runtime / retry 机制

不能复用或不能直接扩展的能力：

1. 不复用 `AppendCompactTask`。
2. 不扩展 `PaimonCompactionTask` 承载主键 payload。
3. 不让 `PaimonCompactionExecutor` 处理主键表。
4. 不让 `PaimonTableCommit` 强转或提交主键表。
5. 不改变 APPEND 表 eligibility、planner、executor、committer。

## 启用条件

主键表自优化必须同时满足：

1. `self-optimizing.enabled=true`
2. `paimon-optimizer.enabled=true`
3. `paimon-optimizer.primary-key.enabled=true`
4. 表是 Paimon 主键表
5. bucket mode 是 `HASH_FIXED` 或 `HASH_DYNAMIC`
6. `self-optimizing.filter` 为空

普通不匹配场景使用 `LOG.info` 后跳过：

1. 主键表开关未启用。
2. 表不是主键表。
3. bucket mode 不支持。
4. APPEND 表继续走现有 APPEND 链路。

危险误配置使用 `LOG.warn` 并拒绝主键表规划：

1. `self-optimizing.filter` 非空。
2. `paimon-optimizer.primary-key.max-buckets-per-task <= 0`。
3. `paimon-optimizer.primary-key.major.file-count-threshold < effectiveMinorTriggerFileCount`。
4. FULL 达到触发但未配置 `paimon-optimizer.primary-key.partition-idle-time`。

## 配置

新增 Paimon format 私有表属性：

| 配置 | 默认值 | 说明 |
|---|---:|---|
| `paimon-optimizer.primary-key.enabled` | `false` | 是否启用 Paimon 主键表自优化 |
| `paimon-optimizer.primary-key.max-buckets-per-task` | `16` | 单个 Amoro task 最多处理的 bucket 数 |
| `paimon-optimizer.primary-key.partition-idle-time` | 未配置 | partition 冷却时间，按表配置 |
| `paimon-optimizer.primary-key.major.file-count-threshold` | 未配置 | MAJOR 文件数阈值覆盖项 |

复用通用 Amoro 表属性：

1. `self-optimizing.enabled`
2. `self-optimizing.minor.trigger.file-count`
3. `self-optimizing.minor.trigger.interval`
4. `self-optimizing.min-plan-interval`
5. `self-optimizing.full.trigger.interval`
6. `self-optimizing.filter`

复用 Paimon 表属性：

1. `num-sorted-run.compaction-trigger`
2. `num-sorted-run.stop-trigger`
3. `commit.max-retries`
4. `commit.timeout`

## MINOR / MAJOR / FULL 语义

主键表优化优先级为：

```text
MAJOR > MINOR > FULL
```

同一轮 process 只包含一种 `OptimizingType`：

1. 如果存在 MAJOR bucket，本轮只规划 MAJOR bucket。
2. 如果没有 MAJOR，但存在 MINOR bucket，本轮只规划 MINOR bucket。
3. 如果没有 MAJOR/MINOR，且 FULL 达到触发条件，才规划 FULL。
4. 不在同一个 process 中混合 `fullCompaction=true` 和 `fullCompaction=false`。

### MINOR

`MINOR` 是日常优化，执行：

```java
write.compact(partition, bucket, false)
```

MINOR 文件数阈值：

```text
effectiveMinorTriggerFileCount =
  explicit Paimon num-sorted-run.compaction-trigger
  ?: Amoro self-optimizing.minor.trigger.file-count
```

当 `bucket.fileCount >= effectiveMinorTriggerFileCount` 时，bucket 进入 MINOR 候选。

`self-optimizing.minor.trigger.interval` 和 `self-optimizing.min-plan-interval` 只作为 Amoro 表级规划节流，不作为 bucket 压力指标。

### MAJOR

`MAJOR` 是 Amoro 对 Paimon 主键表扩展出来的优化指标，不是 Paimon procedure 原生 `major` 参数。

MAJOR 是从满足 MINOR 资格的 bucket 中升级出来的局部 full compact，执行：

```java
write.compact(partition, bucket, true)
```

MAJOR 阈值：

1. bucket 必须先满足 `bucket.fileCount >= effectiveMinorTriggerFileCount`。
2. 如果配置了 `paimon-optimizer.primary-key.major.file-count-threshold`，使用该值。
3. 否则如果 Paimon 显式配置了 `num-sorted-run.stop-trigger`，使用 Paimon stop-trigger。
4. 否则使用 `effectiveMinorTriggerFileCount + 3`，对齐 Paimon 默认 stop-trigger 语义。

覆盖配置必须满足：

```text
paimon-optimizer.primary-key.major.file-count-threshold >= effectiveMinorTriggerFileCount
```

否则 `LOG.warn` 并拒绝规划。

### FULL

`FULL` 是配置驱动、低优先级、冷却 partition 范围内的 full compact，执行：

```java
write.compact(partition, bucket, true)
```

FULL 触发条件：

1. `self-optimizing.full.trigger.interval > 0`。
2. 距离上次 FULL 达到 interval。
3. 本轮没有 MAJOR/MINOR 候选。
4. 必须配置 `paimon-optimizer.primary-key.partition-idle-time`。
5. 存在满足冷却条件的 bucket。

FULL 未配置 `partition-idle-time` 时，即使达到 full trigger interval，也拒绝规划 FULL，并输出 `LOG.warn`。推荐日志语义：

```text
Paimon primary-key FULL optimizing requires paimon-optimizer.primary-key.partition-idle-time;
skip FULL to avoid compacting active partitions.
```

FULL 不处理活跃 partition。找不到冷却 bucket 时不创建 process。

## Planner 设计

新增 `PaimonPrimaryKeyOptimizingPlanner`。

Planner 使用：

```java
table.newSnapshotReader().bucketEntries()
```

枚举当前 snapshot 真实存在的 `(partition,bucket)`。不枚举理论 bucket 范围，不读取每个 data file 的 level/run 明细。

候选判断使用 `BucketEntry`：

1. `partition`
2. `bucket`
3. `recordCount`
4. `fileSizeInBytes`
5. `fileCount`
6. `lastFileCreationTime`

规划流程：

1. 校验启用条件。
2. 校验危险误配置。
3. 获取 target snapshot id。
4. 读取 `bucketEntries()`。
5. 如果配置了 `partition-idle-time`，按 partition 冷却时间过滤候选。
6. 计算 `effectiveMinorTriggerFileCount`。
7. 计算 MAJOR 阈值。
8. 先筛选 MAJOR bucket。
9. 无 MAJOR 时筛选 MINOR bucket。
10. 无 MAJOR/MINOR 时判断 FULL 条件并筛选 FULL bucket。
11. 按 `max-buckets-per-task` 打包 task。
12. 生成 `OptimizingPlanResult<PaimonPrimaryKeyCompactionTask>`。

`HASH_FIXED` 与 `HASH_DYNAMIC` 使用完全相同的候选判断和 task packing。

## partition-idle-time

配置名：

```text
paimon-optimizer.primary-key.partition-idle-time
```

语义：

1. 默认不配置。
2. 未配置时，`MINOR/MAJOR` 可处理所有 bucket。
3. 配置后，`MINOR/MAJOR/FULL` 都只处理冷却 partition。
4. `FULL` 强制要求配置。

冷却判断参考 Paimon Spark procedure 的 `partition_idle_time` 思路，以 partition 维度 `lastFileCreationTime` 判断。实现阶段优先使用 `SnapshotReader.partitionEntries()` 获取 partition 级 `lastFileCreationTime`；如果同一版本 Paimon API 行为存在差异，需要继续探索 Paimon 内核并选择 public API 可稳定表达的方案。

## self-optimizing.filter

第一版主键表不支持任何非空 `self-optimizing.filter`。

规则：

1. filter 为空：允许规划。
2. filter 非空：拒绝主键表规划并 `LOG.warn`。
3. 不解析 partition filter。
4. 不静默忽略 filter。
5. APPEND 表现有 filter 行为不改变。

推荐日志语义：

```text
Paimon primary-key self-optimizing does not support self-optimizing.filter in this version;
skip planning to avoid silently ignoring filter.
```

## Task 与 Output

### PaimonPrimaryKeyCompactionTask

字段：

1. `List<PaimonBucketCompactionUnit> units`
2. `OptimizingType optimizingType`
3. `boolean fullCompaction`
4. `long snapshotId`

### PaimonBucketCompactionUnit

字段：

1. serialized `BinaryRow partition`
2. `int bucket`
3. `long fileCount`
4. `long fileSizeInBytes`
5. `long recordCount`
6. `long lastFileCreationTime`

### PaimonPrimaryKeyCompactionOutput

字段：

1. serialized Paimon `CommitMessage` bytes
2. compacted bucket count
3. input bucket count
4. input file count
5. input file size bytes
6. input record count
7. output file count，如果能从 commit message 稳定统计
8. output file size bytes，如果能从 commit message 稳定统计

输出指标应接入现有 `StagedTaskDescriptor.toMetricsSummary()` 聚合契约。

## Executor 设计

新增 `PaimonPrimaryKeyCompactionExecutor`。

执行流程：

1. 从 task 反序列化 bucket units。
2. 重新加载 Paimon 表。
3. 校验表仍是主键表，bucket mode 仍是 `HASH_FIXED` 或 `HASH_DYNAMIC`。
4. 创建 `BatchTableWrite`。
5. 创建并设置 `IOManager`。
6. 对 task 内每个 unit 循环调用：
   - `MINOR`: `write.compact(partition,bucket,false)`
   - `MAJOR/FULL`: `write.compact(partition,bucket,true)`
7. 调用 `write.prepareCommit()`。
8. 使用 `CommitMessageSerializer` 序列化 commit messages。
9. 生成 `PaimonPrimaryKeyCompactionOutput`。
10. 关闭 write 和 IOManager。

失败语义：

1. 任意 bucket compact 失败，整个 Amoro task 失败。
2. 第一版不做 task 内 bucket 级 partial success。
3. Amoro 现有 task retry 负责重试。

## Committer 设计

新增主键表专用 committer。

提交流程：

1. 收集所有成功 task 的 `PaimonPrimaryKeyCompactionOutput`。
2. 反序列化所有 Paimon `CommitMessage`。
3. 创建 `BatchTableCommit`。
4. 调用 `BatchTableCommit.commit(messages)`。
5. 关闭 commit。

第一版不实现 partial commit：

1. 不对 `self-optimizing.allow-partial-commit` 做主键表特殊支持。
2. 只在 process 成功路径提交 commit messages。
3. process 存在失败 task 时走 Amoro 现有失败 / 重试机制。

## Commit 冲突处理

采用“Paimon 内部重试 + Amoro 下一轮重新 plan”。

Spark Producer 的事实：

1. Spark task 生成 commit messages。
2. Driver 聚合 commit messages。
3. Spark Producer 调用 `BatchTableCommit.commit(messages)`。
4. Spark Producer 自身不在 commit 冲突后重新选择 bucket 或重写 compact result。

Paimon core 的事实：

1. compact 提交走 `CommitKind.COMPACT`。
2. `FileStoreCommitImpl` 按 `commit.max-retries` / `commit.timeout` 重试。
3. Paimon 会做 LSM conflict detection。
4. 检测到不可合并冲突或超过重试后抛异常。

Amoro 设计：

1. Committer 不复用旧 commit messages。
2. Committer 不在当前 process 内重规划。
3. Paimon commit 最终失败时，当前 Amoro process 失败。
4. 后续由 Amoro 现有重试或下一轮 planning 基于最新 snapshot 重新生成任务。

## ProcessFactory 集成

`PaimonProcessFactory.createPlanner()` 保持唯一入口。

路由规则：

1. APPEND `BUCKET_UNAWARE` 表继续返回现有 `PaimonOptimizingPlanner`。
2. 主键 `HASH_FIXED/HASH_DYNAMIC` 且主键表开关开启时返回 `PaimonPrimaryKeyOptimizingPlanner`。
3. 其他表类型返回空计划或跳过。

新增主键表 executor factory，并注册到 task descriptor recovery 映射，保证 AMS 恢复 in-flight task 时能恢复主键表 task。

## Descriptor / UI

`PaimonTableDescriptor.getTableOptimizingTypes(...)` 需要返回：

1. `FULL`
2. `MAJOR`
3. `MINOR`

原因：

1. 主键表新增 `MAJOR` process。
2. 当前 APPEND planner 已经可能产生 `MAJOR`，descriptor 只暴露 `FULL/MINOR` 是不完整的。

优化历史和筛选需要支持 `MAJOR`。如果历史 Paimon snapshot 不能可靠区分主键 MAJOR，应优先依赖 Amoro process runtime/history 记录的 `OptimizingType.MAJOR`；snapshot 推断只能作为兜底。

## 测试计划

### Planner 测试

必须覆盖：

1. `paimon-optimizer.primary-key.enabled` 默认关闭，不规划主键表。
2. 只支持 `HASH_FIXED` 和 `HASH_DYNAMIC`。
3. `HASH_FIXED` 和 `HASH_DYNAMIC` 共用行为。
4. APPEND `BUCKET_UNAWARE` 表继续走现有 planner。
5. 非空 `self-optimizing.filter` 拒绝规划并 `LOG.warn`。
6. `max-buckets-per-task <= 0` 拒绝规划并 `LOG.warn`。
7. MINOR 阈值：Paimon 显式 `num-sorted-run.compaction-trigger` 优先，否则使用 Amoro `self-optimizing.minor.trigger.file-count`。
8. MAJOR 阈值：私有覆盖参数优先，其次 Paimon `num-sorted-run.stop-trigger`，否则 `effectiveMinor + 3`。
9. `major.file-count-threshold < effectiveMinorTriggerFileCount` 拒绝规划并 `LOG.warn`。
10. `MAJOR > MINOR > FULL` 优先级。
11. 同时存在 MAJOR 和 MINOR 候选时，只规划 MAJOR。
12. FULL 达到 interval 但存在 MAJOR/MINOR 候选时，不规划 FULL。
13. FULL 未配置 `partition-idle-time` 时拒绝 FULL 并 `LOG.warn`。
14. FULL 只处理冷却 partition。
15. 找不到冷却 bucket 时不创建 process。
16. task packing 遵守 `max-buckets-per-task=16` 默认值。

### Executor / Committer 测试

必须覆盖：

1. 一个 task 内多个 bucket 循环 compact。
2. `MINOR` 调用 `fullCompaction=false`。
3. `MAJOR/FULL` 调用 `fullCompaction=true`。
4. commit messages 可序列化和反序列化。
5. 多 task output 能汇总提交。
6. 任意 bucket 执行失败导致 task 失败。
7. Paimon commit 冲突抛出后不复用旧 messages，不在当前 process 内重规划。
8. 第一版不执行 partial commit。

### 回归测试

必须覆盖：

1. 现有 APPEND bucket-unaware planner 行为不变。
2. 现有 APPEND executor 行为不变。
3. 现有 APPEND committer 行为不变。
4. Paimon descriptor 支持 `MAJOR`。

实现阶段如果发现新的边界风险，可以自动补充测试，不需要再次逐项确认。

## 确定性审查

### 已有事实级信心的结论

1. Paimon Spark procedure 对 `HASH_FIXED/HASH_DYNAMIC` 共用 aware bucket compact 路径。
2. Paimon Spark Producer 的 full compaction producer 只对 touched buckets 做局部 full compact，不按 bucket mode 拆 compact 逻辑。
3. Paimon public API `TableWrite.compact(partition,bucket,fullCompaction)` 能表达 `MINOR` 和 `MAJOR/FULL` 的执行差异。
4. Paimon `BatchTableCommit.commit(messages)` 内部负责 commit retry 和 LSM conflict detection。
5. 当前 Amoro Paimon APPEND 链路不能直接承载主键表 task。

### 仍可能出错的漏洞与修复措施

1. **Paimon 版本 API 差异**
   - 漏洞：Amoro 当前依赖的 Paimon 版本可能与本地 `/Users/SL/javaProject/paimon` 源码存在细节差异。
   - 修复：实现前必须在 Amoro 依赖版本源码或本仓库依赖中核对 `SnapshotReader.bucketEntries()`、`PartitionEntry`、`BucketEntry`、`BatchTableWrite`、`BatchTableCommit` API 是否一致。若 API 不一致，以 Amoro 实际依赖版本为准调整实现。

2. **`BucketEntry.fileCount` 与 sorted run count 不完全等价**
   - 漏洞：Paimon LSM 压力核心是 sorted run，`BucketEntry` 暴露的是 file count。
   - 修复：第一版明确使用 file count 作为轻量近似，并让真正 compact 选择交给 Paimon `write.compact`。如果测试发现误判过多，再探索 public API 是否能稳定读取 run/level 信息；不能为了精确而直接依赖内部 `MergeTreeCompactManager`。

3. **FULL 冷却判断的时间来源**
   - 漏洞：`BucketEntry.lastFileCreationTime` 和 `PartitionEntry.lastFileCreationTime` 的聚合语义需要以实际 Paimon 版本确认。
   - 修复：实现阶段优先参考 Spark procedure 的 `getHistoryPartition` 方式，使用 `SnapshotReader.partitionEntries()` 做 partition 级冷却过滤；如果 API 不可用，继续探索 Paimon 内核 public scan 能力，直到能确定一个不会把活跃 partition 纳入 FULL 的方案。

4. **Amoro full trigger last time 语义**
   - 漏洞：主键表 `FULL` 低优先级可能被 `MAJOR/MINOR` 长期延后。
   - 修复：这是已确认的产品语义。实现时必须确保 FULL 只在没有 MAJOR/MINOR 候选时触发，并通过日志或 metrics 保持可观测。

5. **Task recovery 映射遗漏**
   - 漏洞：新增 executor factory 后如果未注册 recovery 类型，AMS 恢复 in-flight task 会失败。
   - 修复：实现计划必须包含 task descriptor recovery 映射和恢复测试。

6. **APPEND 行为被误改**
   - 漏洞：如果为了复用改动现有 planner/executor/committer，可能改变 APPEND 表行为。
   - 修复：主键表新增独立类；公共代码抽取只允许无语义变化；必须跑 APPEND 回归测试。

7. **`self-optimizing.filter` 被静默忽略**
   - 漏洞：用户可能以为 filter 生效。
   - 修复：主键表第一版任何非空 filter 都拒绝规划并 `LOG.warn`。

8. **commit 冲突后旧 messages 误提交**
   - 漏洞：compact result 基于旧文件状态，冲突后复用旧 messages 可能破坏 LSM 约束。
   - 修复：只调用 Paimon commit；Paimon 最终失败后 Amoro process 失败，下一轮重新 plan。

### 事实上的 100% 信心标准

进入编码前，本设计对目标方向有事实级信心；但实现层面的 100% 信心必须通过以下循环获得：

1. 对照 Amoro 实际依赖的 Paimon API 核对所有调用点。
2. 写最小但覆盖关键语义的单元测试。
3. 实现主键表独立链路。
4. 运行主键表 planner / executor / committer 测试。
5. 运行 APPEND 表回归测试。
6. 对代码做一次 review，重点检查 APPEND 隔离、commit 语义、危险配置拒绝、task recovery。
7. 如果发现无法识别的问题，继续探索 Paimon 内核代码，更新计划或实现，再重复测试与 review。

只有上述循环通过，才能声明实现对目标没有严重偏离。

## 后续执行顺序

1. 用户审阅并确认本 Spec。
2. 编写实现计划。
3. 实现阶段可使用多 Agent 并行：
   - Agent A：Planner / 配置 / eligibility。
   - Agent B：Executor / task / output / committer。
   - Agent C：Descriptor / recovery / 测试。
4. 主 Agent 负责合并、冲突处理、统一 review 和最终验证。
