# OptimizingQueue 异步 Warmup 启动优化 Spec

**Date:** 2026-06-02
**Scope:** 优化 AMS leader 启动阶段 `OptimizingQueue` 同步逐表恢复导致 Thrift 服务长时间不可用的问题
**Affected path:** `AmoroServiceContainer#startOptimizingService` -> `DefaultTableService#initialize` -> `DefaultOptimizingService#loadOptimizingQueues` -> `OptimizingQueue#initTableRuntime`

---

## 背景

当前 AMS leader 启动优化服务时，`AmoroServiceContainer.startOptimizingService()` 会先执行
`tableService.initialize()`，然后才执行 `initThriftService()` 和 `startThriftService()`：

```java
tableService.initialize();
LOG.info("AMS table service have been initialized");
tableManager.setTableService(tableService);

initThriftService();
startThriftService();
```

`DefaultTableService.initialize()` 会从 AMS DB 恢复全部 `TableRuntime`，然后同步初始化
`RuntimeHandlerChain`。其中 `DefaultOptimizingService.TableRuntimeHandlerImpl` 会调用
`loadOptimizingQueues(...)`，为 resource group 创建 `OptimizingQueue`。

`OptimizingQueue` 当前在构造函数中同步逐表执行：

```java
tableRuntimeList.forEach(this::initTableRuntime);
```

`initTableRuntime(...)` 会恢复历史 optimizing process、读取 process state、恢复 task runtime 和
task input，并根据表状态把表重新加入 scheduler 或 table queue。表数量达到 700+ 时，这段同步恢复会把
Thrift 服务启动挡在后面，导致 optimizer service 端口 20-30 分钟不可用。

本设计目标是把“Thrift 服务可连接”和“全部表优化队列恢复完成”解耦：Thrift 不等待全量表恢复；每张表
warmup 成功后立即进入对应 `OptimizingQueue`，开始具备被 optimizer poll 到并优化的能力。

---

## 目标

1. Thrift 服务不等待全部表完成 `OptimizingQueue` warmup。
2. `DefaultTableService.initialize()` 仍同步完成，保证基础 `TableRuntime` map 可用。
3. 启动阶段同步创建所有 resource group 的空 `OptimizingQueue`，保证 optimizer 可以认证到已有 group。
4. 表级 warmup 在后台异步执行；一张表恢复成功，就立即进入对应 queue 并可被 optimizer poll 到 task。
5. warmup 未完成的表暂时不参与优化调度，不阻塞已完成表。
6. 单表 warmup 失败不影响其他表，失败表不进入 scheduler，并按轻量重试策略处理。
7. 历史正在处理中的表优先恢复，降低重启后 RUNNING/COMMITTING/PENDING 表长时间无人接管的风险。
8. 增加日志和指标，让运维能看清 warmup 进度、失败数和耗时。

## 非目标

- 不异步化 `DefaultTableService.initialize()` 的基础 runtime map 恢复。
- 不异步化 `ProcessService` 的 active process 恢复，也不异步化 inline table scheduler 的初始化。本次只切走
  `DefaultOptimizingService` 中 `OptimizingQueue` 的表级恢复等待。
- 不新增 dashboard 页面或 REST API。
- 不修改 optimizer thrift 协议。
- 不改变 `pollTask`、`ackTask`、`completeTask` 的协议语义。
- 不改变 planner、task commit、task retry、quota 计算的业务语义。
- 不要求 resource group 整组 ready 后才开始出 task。
- 不引入新的长期后台调度系统；本次只覆盖启动期 warmup 和有限重试。

---

## 当前源码问题

### 1. Thrift 启动被优化队列 warmup 硬阻塞

`AmoroServiceContainer.startOptimizingService()` 的启动顺序是：

1. 创建 `DefaultTableService`、`ProcessService`、`DefaultOptimizingService`。
2. 注册 `RuntimeHandlerChain`。
3. 调用 `tableService.initialize()`。
4. 调用 `initThriftService()`。
5. 调用 `startThriftService()`。

只要 `tableService.initialize()` 内部 handler 链没返回，Thrift 端口就不会开始监听。

### 2. `OptimizingQueue` 构造函数承担了重恢复逻辑

`DefaultOptimizingService.loadOptimizingQueues(...)` 当前按 resource group 创建 queue：

```java
OptimizingQueue optimizingQueue =
    new OptimizingQueue(
        catalogManager,
        group,
        this,
        planExecutor,
        Optional.ofNullable(tableRuntimes).orElseGet(ArrayList::new),
        maxPlanningParallelism,
        router);
optimizingQueueByGroup.put(groupName, optimizingQueue);
```

`OptimizingQueue` 构造函数立即执行 `tableRuntimeList.forEach(this::initTableRuntime)`。这导致
“创建 queue 对象”和“逐表恢复全部 queue 内容”无法分离。

### 3. 单表恢复可能包含多次 DB 查询和反序列化

有历史 `processId` 的表会走：

- `TableProcessMapper#getProcessMeta`
- `OptimizingProcessMapper#getProcessState`
- `OptimizingProcessMapper#selectTaskRuntimes`
- `OptimizingProcessMapper#selectProcessInputFiles`
- gzip 解压和 Java 反序列化 task input

这些操作逐表串行执行时，会随表数量和历史 process/task 数线性放大。

### 4. 外部 catalog 探索不是本次主要阻塞点

`DefaultTableService.initialize()` 尾部会以 0 delay 调度 `exploreTableRuntimes()`，该任务可能和
Thrift 初始化竞争 DB/catalog 资源。但它是 scheduler 线程异步执行，不是 `initialize()` 的直接 join
阻塞。本次主要修复同步 handler 链中的 `OptimizingQueue` warmup。

### 5. 后续 handler 仍然同步初始化

`RuntimeHandlerChain.initialize(...)` 会先执行当前 handler，再继续执行 `next.initialize(...)`。因此
`DefaultOptimizingService.TableRuntimeHandlerImpl` 返回后，`ProcessService` 和各类 inline scheduler 仍会
同步初始化。源码中 `ProcessService.initialize(...)` 会查询 `selectAllActiveProcesses` 并恢复普通
`TableProcess`；`PeriodicTableScheduler.initHandler(...)` 会为每张表注册后续调度任务。

本 Spec 的真实性边界是：**Thrift 不再等待 `OptimizingQueue` 对全部表做 optimizing process/task
恢复，但仍会等待基础 `TableRuntime`、`ProcessService` 和 inline scheduler 的既有同步初始化。** 如果后续
实测发现这些同步步骤也成为主要瓶颈，需要另起 Spec。

---

## 方案比较

### 方案 A：同步空队列 + 异步逐表 warmup（推荐）

做法：

- 启动时同步创建每个 resource group 的空 `OptimizingQueue`。
- 同步注册历史 optimizer，并启动 keeper/watcher。
- Thrift 服务不等待表级 warmup 完成。
- 后台按优先级逐表恢复；表成功恢复后立即进入 queue。

优点：

- 满足“表加载后即可进入优化器队列开始优化”的核心目标。
- 改动边界聚焦在 `DefaultOptimizingService` 和 `OptimizingQueue`。
- 不需要改动 thrift 协议。
- `DefaultTableService` 基础语义保持稳定。

缺点：

- 需要新增 warmup 状态、幂等保护、失败重试和并发控制。
- 运维需要理解启动后存在“queue 逐步 ready”的过渡状态。

结论：采用。

### 方案 B：按 resource group 分批 warmup

做法：

- Thrift 提前启动。
- 每个 resource group 后台恢复。
- 一个 group 内所有表都恢复完成后，才允许该 group 出 task。

优点：

- group 状态更简单。

缺点：

- 不满足“已恢复表优先处理”；一个 group 内仍要等待全部表恢复。
- 700+ 表集中在同一个 group 时，效果接近当前同步等待。

结论：不采用。

### 方案 C：`DefaultTableService.initialize()` 和 queue 全部异步化

做法：

- Thrift 更早启动。
- 基础 runtime map、queue 创建和表恢复都异步执行。
- 所有依赖 `tableService.getRuntime/contains/listRuntime` 的路径加 readiness gate。

优点：

- 端口可连接时间最短。

缺点：

- 改动面大，影响 table management、dashboard controller、optimizer service 多条路径。
- 需要大量初始化中状态处理，本次风险过高。

结论：不采用；如后续证明基础 runtime map 恢复也成为瓶颈，再单独设计。

---

## 详细设计

### 1. 拆分 `OptimizingQueue` 构造和表级 warmup

调整 `OptimizingQueue` 构造函数语义：构造函数只创建 queue 基础结构、scheduler、metrics 和 plan
slot，不再强制同步恢复传入的全部表。

新增显式表级 warmup 入口，并让调用方能区分成功、跳过、重复和失败：

```java
public WarmupResult warmupTable(DefaultTableRuntime tableRuntime) {
  return initTableRuntimeIfAbsent(tableRuntime);
}

public enum WarmupResult {
  WARMED,
  SKIPPED,
  DUPLICATE,
  FAILED
}
```

`initTableRuntimeIfAbsent(...)` 复用当前 `initTableRuntime(...)` 的核心逻辑，但必须增加表级幂等保护。
当前 `initTableRuntime(...)` 会 catch 住所有异常并只打印日志；为了让 warmup service 能统计失败并执行
重试，需要把异常边界改造为以下结构：

- 内层 `doInitTableRuntime(...)` 执行原恢复逻辑，失败时抛出异常。
- 外层 `warmupTable(...)` 负责 catch 异常，返回 `FAILED`，并清理 `warmingTables`。
- disabled self-optimizing 或 unsupported format 这类“按业务规则不入队”的情况返回 `SKIPPED`，不计入失败。
- 正常恢复并进入 scheduler/tableQueue 的情况返回 `WARMED`。

建议新增字段：

```java
private final Set<ServerTableIdentifier> warmedTables = ConcurrentHashMap.newKeySet();
private final Set<ServerTableIdentifier> warmingTables = ConcurrentHashMap.newKeySet();
```

约束：

- `warmingTables.add(identifier)` 失败时，表示已有线程正在恢复该表，直接跳过。
- `warmedTables.contains(identifier)` 为 true 时，表示该表已经恢复过，直接跳过。
- `doInitTableRuntime(...)` 返回 `WARMED` 或 `SKIPPED` 后加入 `warmedTables`，表示启动恢复已经处理过该表。
- 失败时从 `warmingTables` 移除，但不加入 `warmedTables`。
- 表被 release/remove 时，需要从 `warmingTables` 和 `warmedTables` 清理。

这样可以避免启动 warmup 对同一张表重复恢复历史 process。注意：`warmedTables` 只约束启动恢复入口，
不能阻止后续 `refreshTable(...)` 根据状态/config 变化把表重新加入 scheduler；`SchedulingPolicy.addTable`
本身是按 table identifier 覆盖 put，重复 refresh 是幂等的。

### 2. 新增启动期 warmup 编排组件

新增 `OptimizingQueueWarmupService`，职责只包括：

- 接收启动时的 `DefaultTableRuntime` 列表。
- 按 resource group 找到对应 `OptimizingQueue`。
- 按优先级排序。
- 使用 bounded executor 并发恢复。
- 执行单表失败重试。
- 维护 warmup 进度统计。
- 在 dispose 时停止接收新任务并中断未执行任务。

建议字段：

```java
private final ExecutorService executor;
private final ScheduledExecutorService retryExecutor;
private final int maxAttempts = 3;
private final AtomicInteger totalCount = new AtomicInteger();
private final AtomicInteger loadedCount = new AtomicInteger();
private final AtomicInteger skippedCount = new AtomicInteger();
private final AtomicInteger failedCount = new AtomicInteger();
private final AtomicInteger runningCount = new AtomicInteger();
private final AtomicInteger retryingCount = new AtomicInteger();
private final ConcurrentMap<ServerTableIdentifier, Integer> attempts = new ConcurrentHashMap<>();
```

线程命名：

- `optimizing-queue-warmup-%d`
- `optimizing-queue-warmup-retry-%d`

### 3. 启动流程调整

`DefaultOptimizingService.loadOptimizingQueues(...)` 改为：

1. 查询 resource groups。
2. 查询历史 optimizer instances。
3. 按 group 创建空 `OptimizingQueue` 并放入 `optimizingQueueByGroup`。
4. 注册历史 optimizer。
5. 对不存在 resource group 的历史 table runtime 维持现有保护：如果表状态是 `PLANNING/PENDING`，仍执行
   `completeEmptyProcess()`，避免表卡死。
6. 将存在 resource group 的 table runtime 提交给 `OptimizingQueueWarmupService`。

`DefaultOptimizingService.TableRuntimeHandlerImpl.initHandler(...)` 保持同步返回，不等待 warmup 全量完成。随后
`RuntimeHandlerChain` 仍会同步初始化后续 handler；本次优化不改变这些 handler 的生命周期。

`AmoroServiceContainer.startOptimizingService()` 的顺序保持表面结构不变：

```java
tableService.initialize();
tableManager.setTableService(tableService);
initThriftService();
startThriftService();
```

区别是 `tableService.initialize()` 中的 optimizing handler 不再等待全部表级 queue warmup。

### 4. Thrift 行为

`authenticate()`：

- resource group 的空 queue 已经同步创建，因此 `getQueueByGroup(group)` 可以正常返回。
- optimizer 可以正常认证并绑定到 queue。

`pollTask()`：

- 不等待 warmup 全量完成。
- 如果当前 queue 暂时没有可用 task，保持现有行为返回 `null`。
- 已 warmup 的表可以正常 planning、入队、poll。
- 未 warmup 的表不会被 scheduler 选中，也不会被 poll 到。

`ackTask()` / `completeTask()`：

- 不需要新增协议逻辑。
- 只有已经 poll 出去的 task 才会进入这些路径。

### 5. Warmup 恢复优先级

启动 warmup 的表按以下顺序恢复：

1. 有历史 `processId` 的表。
2. `OptimizingStatus.isProcessing()` 为 true 的表，即 `FULL_OPTIMIZING`、`MAJOR_OPTIMIZING`、
   `MINOR_OPTIMIZING`、`COMMITTING`。
3. `PLANNING`、`PENDING` 表。
4. 普通 `IDLE` 表。

排序目标：

- 优先接管重启前已经在处理中的 process。
- 优先恢复可能影响任务 ack/complete/commit 的表。
- 普通空闲表可以稍后进入 scheduler。

### 6. 并发配置

新增配置项：

```java
public static final ConfigOption<Integer> OPTIMIZING_QUEUE_WARMUP_THREAD_COUNT =
    ConfigOptions.key("self-optimizing.queue-warmup.thread-count")
        .intType()
        .defaultValue(10)
        .withDescription("The number of threads used to warm up optimizing queues during AMS startup.");
```

配置语义：

- 默认并发为 `10`。
- 小于等于 0 的配置视为非法，启动时报配置错误。
- 该并发控制全局 warmup 线程数，不是每个 resource group 各 10 个线程。

采用全局线程池的原因：

- warmup 主要压力在 DB 查询、blob 反序列化和状态修正。
- 全局并发更容易控制启动期 DB 压力。
- resource group 数较多时，避免并发数按 group 膨胀。

### 7. 失败重试

单表 warmup 失败时：

1. 本次尝试不把表加入 scheduler。
2. 记录 table identifier、group、processId、optimizing status、attempt 和异常。
3. 从 `warmingTables` 移除。
4. 如果 attempt 小于 3，提交下一次 retry。
5. 如果 3 次后仍失败，计入 `failedCount`，不阻塞其他表和 resource group ready。

固定退避：

| Attempt | 下一次重试延迟 |
---|---:|
| 1 | 30s |
| 2 | 60s |
| 3 | 120s |

第 3 次仍失败后不再由启动 warmup service 自动重试。后续如果表配置变更、状态变更或 table added 路径触发，可以继续复用 queue 的显式 warmup/refresh 入口。

`SKIPPED` 不触发重试。典型 `SKIPPED` 包括：

- 表的 self-optimizing 已关闭，且历史 running process 已按当前逻辑关闭。
- 表格式不被当前 `ProcessFactoryRouter` 支持，且历史 running process 已按当前逻辑关闭。
- 表在执行前已经不属于当前 queue 或当前 table service。

### 8. 可观测性

不新增 dashboard/API。本次只增加日志和指标。

启动日志：

```text
Start optimizing queue warmup: tables=742, groups=3, threadCount=10
```

周期进度日志：

```text
Optimizing queue warmup progress: loaded=120/742, skipped=8, failed=2, running=10, retrying=1, elapsedMs=45000
```

单表失败日志：

```text
Failed to warm up optimizing queue table catalog.db.table, group=default, processId=12345, status=PENDING, attempt=2
```

完成日志：

```text
Optimizing queue warmup completed: loaded=731, skipped=8, failed=3, total=742, elapsedMs=185000
```

建议新增指标：

- `optimizing_queue_warmup_total`
- `optimizing_queue_warmup_loaded`
- `optimizing_queue_warmup_skipped`
- `optimizing_queue_warmup_failed`
- `optimizing_queue_warmup_running`
- `optimizing_queue_warmup_retrying`
- `optimizing_queue_warmup_initializing`

指标建议新增独立的 `OptimizingQueueWarmupMetrics`，通过 `MetricManager.getInstance().getGlobalRegistry()`
注册全局 gauge。`OptimizerGroupMetrics` 当前绑定单个 `OptimizingQueue` 和 `group` tag，适合 queue 内运行态指标；
warmup service 是跨 group 的启动编排组件，先做全局指标更贴合当前代码结构。group 维度可以作为后续增强。

### 9. Dispose 和资源组变更

`DefaultOptimizingService.dispose()` 需要先停止 warmup service，再 dispose queue：

1. warmup service 停止接收新任务。
2. 取消 retry executor 中未执行任务。
3. 中断 executor 中等待执行的任务。
4. 已经进入单表恢复临界区的任务允许自然失败或完成，但完成前需要检查 service/queue 是否仍 active。
5. dispose 所有 `OptimizingQueue`。

`deleteResourceGroup(groupName)`：

- 从 `optimizingQueueByGroup` 移除 queue 后，后续 warmup 任务如果发现 queue 不存在，直接跳过。
- queue dispose 时清理 `warmingTables` 和 `warmedTables`。

`updateResourceGroup(resourceGroup)`：

- 继续更新已存在 queue 的 group 配置。
- 不影响 warmup 线程池。

### 10. 与现有 handler 事件的关系

`handleTableAdded(table, runtime)`：

- 如果 queue 已存在，继续调用 `refreshTable(...)` 或等价的轻量绑定入口。
- 不建议让普通 table-added 事件走启动恢复入口；新表没有历史 process 需要恢复，直接按现有
  `refreshTable(...)` 语义加入 scheduler 即可。

`handleStatusChanged(runtime, originalStatus)`：

- 保持现有语义：`PENDING` 或 `IDLE` 时 refresh queue。
- `refreshTable(...)` 不应被 `warmedTables` 拦截；状态变化后的轻量 refresh 需要继续生效。

`handleConfigChanged(runtime, originalConfig)`：

- group 变更时仍从旧 queue release，再向新 queue refresh/warmup。
- 如果新 group 的 queue 正在初始化或已存在空 queue，允许表进入新 queue。
- group 变更时应清理旧 queue 中的启动 warmup 状态，避免旧状态影响新 group 的恢复或 refresh。

---

## 数据一致性与并发约束

1. 表级 warmup 返回 `WARMED` 前，不允许该表通过启动恢复路径进入 scheduler。
2. 表级 warmup 成功后，该表可以立即被 scheduler 选择，不等待其他表。
3. 同一张表同一时刻最多一个 warmup 任务。
4. 同一张表成功 warmup 后，重复启动 warmup 不应重复恢复 process。
5. 表被 release/remove 后，warmup 状态必须清理，避免后续 group 切换失败。
6. 启动 warmup 失败不改变其他表状态。
7. 历史 process 恢复仍沿用当前 `initTableRuntime(...)` 的语义，不因异步化改变 RUNNING/COMMITTING/PENDING 的恢复判断。
8. `pollTask()` 返回 null 只表示当前没有可 poll task，不表示 group warmup 完成或失败。

---

## 风险与缓解

| 风险 | 影响 | 缓解 |
|---|---|---|
| warmup 异步化后 optimizer 过早 poll 到 null | optimizer 短时间空轮询 | 保持现有 poll null 语义；通过日志/指标展示 warmup 进度 |
| 单表重复 warmup | 重复入队、重复恢复 process | `warmingTables` + `warmedTables` 幂等保护 |
| `initTableRuntime` 继续吞异常 | warmup service 无法统计失败或重试 | 拆出会抛异常的内层恢复方法，外层转换为 `WarmupResult` |
| `warmedTables` 误挡运行期 refresh | 表状态/config 变化后无法重新入队 | 幂等集合只作用于启动恢复入口，不拦截 `refreshTable(...)` |
| 并发 warmup 打爆 DB | 启动期 DB 压力升高 | 全局配置并发，默认 10；日志暴露耗时和失败 |
| 历史 RUNNING/COMMITTING process 恢复延迟 | 重启后任务接管慢 | 优先恢复有 processId 和非 IDLE 表 |
| warmup 失败表永久不优化 | 个别表停摆 | 3 次启动重试；后续事件路径可再次触发；日志/指标显式暴露失败 |
| resource group 删除时仍有 warmup 任务 | 空指针或向已删除 queue 入队 | 单表执行前检查 queue 仍存在；dispose 停止 warmup service |
| 指标实现侵入过大 | 扩大改造范围 | 优先全局指标；group 维度作为实现计划中的可选低风险增强 |

---

## 测试计划

### 单元测试

新增或扩展 `TestDefaultOptimizingService`：

1. `testLoadOptimizingQueuesCreatesQueuesBeforeWarmupCompletes`
   - 构造多个 table runtime。
   - mock/stub warmup executor 不立即执行。
   - 验证 `optimizingQueueByGroup` 已有空 queue。
   - 验证 optimizer `authenticate()` 可以成功。

2. `testWarmupTableMakesTableAvailableIncrementally`
   - 创建两个 runtime。
   - 只执行第一张表 warmup。
   - 验证第一张表进入 scheduler 或 queue。
   - 验证第二张表未 warmup 时不参与调度。

3. `testWarmupFailureDoesNotBlockOtherTables`
   - 第一张表 warmup 抛异常。
   - 第二张表 warmup 成功。
   - 验证第二张表可入队。
   - 验证失败计数为 1。

4. `testWarmupPrioritizesProcessingTables`
   - 构造有 `processId`、`COMMITTING`、`PENDING`、`IDLE` 四类 runtime。
   - 验证提交顺序为 processId 优先，其次 processing 状态，再 `PLANNING/PENDING`，最后普通 `IDLE`。

5. `testWarmupTableIsIdempotent`
   - 对同一 runtime 并发调用 warmup。
   - 验证核心恢复逻辑只执行一次。

6. `testWarmupSkippedTableDoesNotRetry`
   - 构造 self-optimizing disabled 或 unsupported format runtime。
   - 验证 warmup 返回 skipped，失败计数不增加，retry 不提交。

扩展 `TestOptimizingQueue`：

1. `testWarmupTableSkipsDuplicateRuntime`
2. `testReleaseTableClearsWarmupState`
3. `testWarmupFailureDoesNotMarkTableWarmed`
4. `testWarmupDoesNotBlockRefreshTableAfterWarmed`

扩展 `TestAmoroManagementConfValidator`：

1. `testValidateThreadCount` 中加入 `OPTIMIZING_QUEUE_WARMUP_THREAD_COUNT <= 0` 的校验。

### 集成或模块级测试

运行：

```bash
./mvnw -pl amoro-ams -Dtest='TestDefaultOptimizingService,TestOptimizingQueue' test
```

如果实现触及 config doc 或默认配置：

```bash
UPDATE=1 ./mvnw test -pl amoro-ams -am -Dtest=ConfigurationsTest
```

### 手工验证

在有大量 runtime 的 AMS 环境中验证：

1. 启动 AMS。
2. 观察 Thrift optimizing service 端口在 queue 全量 warmup 完成前可连接。
3. 启动 optimizer。
4. 观察 warmup 进度日志持续推进。
5. 观察已 warmup 表可以开始 planning/poll task。
6. 确认未 warmup 表不会被提前 poll。
7. 确认 warmup 失败表不会阻塞其他表。

---

## 验收标准

1. AMS 启动时，Thrift 服务不等待全部 `OptimizingQueue` 表级 warmup 完成。
2. optimizer 可以在 warmup 未完成期间认证成功。
3. 表 warmup 成功后立即进入对应 `OptimizingQueue`，可以开始被 optimizer poll 和优化。
4. 未 warmup 表不会提前出 task。
5. 单表 warmup 失败不阻塞其他表继续 warmup。
6. 有历史 process 或非 IDLE 状态的表优先恢复。
7. `self-optimizing.queue-warmup.thread-count` 默认值为 10，且能控制全局 warmup 并发。
8. 日志或指标可以看出 warmup 的 total、loaded、skipped、failed、running、retrying 状态。
9. 现有 `pollTask`、`ackTask`、`completeTask` 语义不改变。
10. `TestDefaultOptimizingService` 和 `TestOptimizingQueue` 相关测试通过。

---

## 实施边界

建议实施计划按以下任务拆分：

1. 为 `OptimizingQueue` 增加显式 warmup 入口和幂等保护。
2. 改造 `initTableRuntime` 的异常边界，让启动恢复能返回 `WarmupResult` 并支持失败重试。
3. 新增 `OptimizingQueueWarmupService`，实现优先级排序、并发执行、失败重试和进度统计。
4. 改造 `DefaultOptimizingService.loadOptimizingQueues(...)`，同步创建空 queue，异步提交 table warmup。
5. 增加配置项 `self-optimizing.queue-warmup.thread-count`，默认 10，并加入配置校验。
6. 增加日志和指标。
7. 增加/调整单元测试。
8. 运行模块测试和配置文档测试。

---

## 已确认决策

以下决策已在设计采访中确认：

- Thrift 提前启动后，optimizer 可以认证和 poll；已 warmup 表优先处理，未 warmup 表暂时返回 null。
- 本次只异步化 `OptimizingQueue` warmup，不异步化 `DefaultTableService.initialize()`。
- warmup 优先恢复有历史 process、processing 状态表、`PLANNING/PENDING` 表，再恢复 `IDLE` 表。
- 单表失败隔离，不阻塞其他表。
- warmup 并发做成配置项，默认 10。
- 暂不新增 dashboard/API。
- 单表失败最多重试 3 次，退避为 30s、60s、120s。
