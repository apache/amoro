# Paimon 主键表 completeTask 卡在 ACKED 的 Thrift 消息大小排查记录

## 背景

这份记录整理 2026-06-25 对 Paimon 主键表优化任务的排查过程，避免后续忘记当时的判断链路。

现象发生在 Paimon 主键表 MAJOR compaction 上：Spark 任务已经执行完成，Spark UI 显示 stage/task 成功，但 Amoro 页面和数据库中的 process/task 仍停留在 `RUNNING` / `ACKED`，没有进入后续 committing / committed 状态。

最终验证结果：调整 Thrift 消息大小后，optimizer 可以正常把任务完成结果回传给 AMS。根因方向确认是 `completeTask` 回传的 `OptimizingTaskResult` 过大，触发 Thrift 传输层限制或连接重置，而不是 Paimon 元数据提交逻辑本身没有执行。

## 关键结论

1. Spark UI 的 `1/1` 成功只说明当前 Amoro task 对应的 Spark partition 执行完成，不代表 AMS 已经收到 `completeTask`。
2. AMS 看到任务停留在 `ACKED` 并每小时重试，说明 AMS 收到了 poll/ACK，但没有收到成功的 `completeTask` 调用。
3. Optimizer 侧日志中 `client.completeTask(...)` 抛出 `TTransportException: SocketException: Connection reset`，是这次排查最关键的证据。
4. Paimon 主键表 compaction output 中携带的是序列化后的 Paimon `CommitMessage` 列表；大表、小文件多、一个任务 compact 多个 bucket 时，`CommitMessage` 会明显膨胀。
5. 调大 Thrift message size 后可以正常 ACK/完成，说明问题主要在 optimizer 到 AMS 的 Thrift 完成回传链路，而不是 AMS 队列状态机、Paimon commit、或者 Spark 执行未结束。

术语备注：这里的 "ACK" 容易混淆。AMS 里的 `ACKED` 是 optimizer poll 到 task 后的确认状态；任务真正执行完成后还需要 optimizer 调用 `completeTask`，AMS 才会进入后续完成和 commit 流程。

## 现场证据

### AMS 视角

指定 `processId=1460107216928769` 在 `ams-info.log` 中只有 plan、poll、ACK、timeout retry 相关日志，没有 `completeTask` 成功记录。

关键时间线：

| 时间 | AMS 观察到的事件 |
|---|---|
| 11:17:22 | plan new task，tableId=154831 |
| 11:17:24 | optimizer threadId=24 poll task 并 ACK |
| 12:18:00 | keeper 判定 ACK 状态挂起 PT1H，放回 retry queue |
| 12:18:01 | threadId=20 再次 poll/ACK |
| 13:18:45 | 再次 ACK 挂起 PT1H，放回 retry queue |
| 13:18:46 | threadId=17 再次 poll/ACK |
| 14:19:32 | 再次 ACK 挂起 PT1H，放回 retry queue |
| 14:19:33 | 再次 poll/ACK |

数据库中对应 task runtime 也符合这个状态：

```text
process_id: 1460107216928769
task_id: 1
status: ACKED
retry_num: 0
end_time: NULL
cost_time: 0
fail_reason: NULL
output_bytes: NULL
```

AMS 日志中没有该 processId 的 `completeTask` 记录，也没有 AMS handler 侧的 commit 失败日志。这说明 AMS 应用层没有成功处理到这个 COMPLETE 请求。

### Optimizer 视角

Optimizer 侧在执行完成后调用 `completeTask` 时出现传输异常：

```text
ERROR AbstractOptimizerOperator: Call ams thrift://10.89.56.103:1261 got an error and will try again later
org.apache.amoro.shade.thrift.org.apache.thrift.transport.TTransportException: java.net.SocketException: Connection reset
  at org.apache.amoro.shade.thrift.org.apache.thrift.transport.TIOStreamTransport.write(...)
  at org.apache.amoro.shade.thrift.org.apache.thrift.transport.layered.TFramedTransport.flush(...)
  at org.apache.amoro.api.OptimizingService$Client.send_completeTask(...)
  at org.apache.amoro.api.OptimizingService$Client.completeTask(...)
  at org.apache.amoro.optimizer.common.OptimizerExecutor.lambda$completeTask$2(...)
```

这个异常发生在 client 端发送 `completeTask` 的 Thrift frame flush 阶段。AMS 没有记录 `completeTask` 是合理的，因为请求可能在进入 AMS service handler 之前已经被传输层拒绝或连接被重置。

## 调用链路

### Optimizer 执行和完成回传

相关代码：

- `amoro-optimizer/amoro-optimizer-common/src/main/java/org/apache/amoro/optimizer/common/OptimizerExecutor.java`
- `amoro-optimizer/amoro-optimizer-spark/src/main/java/org/apache/amoro/optimizer/spark/SparkOptimizerExecutor.java`

链路如下：

```text
OptimizerExecutor.start
  -> poll task from AMS
  -> ackTask(...)
  -> executeTask(...)
     -> SparkOptimizerExecutor.executeTask(...)
        -> jsc.parallelize(ImmutableList.of(task), 1)
        -> SparkOptimizingTaskFunction.map(...)
        -> collect()
     -> return OptimizingTaskResult
  -> finally completeTask(amsUrl, result)
     -> client.completeTask(token, optimizingTaskResult)
```

注意：`SparkOptimizerExecutor` 当前对一个 Amoro task 使用 `parallelize(of, 1)`。所以 Spark UI 中一个 Amoro task 对应一个 Spark task。如果 Planner 把多个 bucket 合并进一个 Amoro task，Spark 侧仍然只会显示一个 task。

### Paimon 主键表 compaction output

相关代码：

- `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyCompactionExecutor.java`
- `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyCompactionOutput.java`
- `amoro-format-paimon/src/main/java/org/apache/amoro/formats/paimon/optimizing/primary/PaimonPrimaryKeyTableCommit.java`

Executor 执行逻辑：

```text
PaimonPrimaryKeyCompactionExecutor.execute
  -> for each PaimonBucketCompactionUnit
       write.compact(partition, bucket, fullCompaction)
  -> write.prepareCommit()
  -> CommitMessageSerializer.serializeAll(messages)
  -> new PaimonPrimaryKeyCompactionOutput(commitMessageBytesList, ...)
```

Committer 再从 success task output 中反序列化：

```text
PaimonPrimaryKeyTableCommit.collectCommitMessages
  -> output.getCommitMessageBytesList()
  -> CommitMessageSerializer.deserializeAll(bytesList)
  -> StreamTableCommit.filterAndCommit(...)
```

因此，optimizer 回传 AMS 的 `OptimizingTaskResult.taskOutput` 中会包含 Paimon `CommitMessage` 的序列化字节。这个输出不是简单计数，而是包含 Paimon 本次 compact 产生和删除的文件元数据。

## 为什么 Paimon CommitMessage 容易变大

Paimon 主键表基于 LSM / sorted run 组织文件。一次 compact 的 `CommitMessage` 可能包含：

1. 新生成的数据文件元数据。
2. 被 compact 掉的旧数据文件元数据。
3. changelog 文件元数据。
4. compact before / after 相关信息。
5. index 文件信息。
6. 每个 `DataFileMeta` 自身携带 row count、file size、min/max key、统计信息、level/run 信息等。

所以对小文件很多的主键表，尤其是 MAJOR / FULL 这类 full compaction 语义，`CommitMessage` 大小可能随参与 compact 的文件数快速增长。

如果一个 Amoro task 内又合并了多个 bucket / partition-bucket unit，多个 Paimon `CommitMessage` 会被一起放进同一个 `OptimizingTaskResult`，进一步放大单次 `completeTask` 的 Thrift payload。

## Thrift 配置锚点

AMS 服务端配置：

- 配置文件：`dist/src/main/amoro-bin/conf/config.yaml`
- key：`thrift-server.max-message-size`
- 当前默认：`100MB`

```yaml
thrift-server:
  max-message-size: 100MB
```

代码锚点：

- `amoro-ams/src/main/java/org/apache/amoro/server/AmoroManagementConf.java`
  - `THRIFT_MAX_MESSAGE_SIZE`
  - 默认 `MemorySize.ofMebiBytes(100)`
- `amoro-ams/src/main/java/org/apache/amoro/server/AmoroServiceContainer.java`
  - `initThriftService()`
  - `createThriftServer(...)`
  - `new TBinaryProtocol.Factory(true, true, maxMessageSize, maxMessageSize)`

客户端配置锚点：

- `amoro-common/src/main/java/org/apache/amoro/client/PoolConfig.java`
  - `maxMessageSize = 100 * 1024 * 1024`
- `amoro-common/src/main/java/org/apache/amoro/client/ThriftClientPool.java`
  - `configuration.setMaxMessageSize(poolConfig.getMaxMessageSize())`
  - `new TFramedTransport(..., poolConfig.getMaxMessageSize())`

连接 URL 也支持 query 参数形式：

```text
thrift://host:1261?maxMessageSize=...
```

排查和调整时要注意客户端和 AMS 服务端两侧上限需要一致或至少都足够大。只调一侧可能仍然在另一侧失败。

## 新增日志观察点

为方便后续直接确认 payload 大小，optimizer 增加了两层 INFO 日志。

### Paimon 主键 executor 层

日志关键字：

```text
Paimon primary-key compaction produced commit messages
```

日志格式：

```text
Paimon primary-key compaction produced commit messages: table={}, type={}, identifier={}, buckets={}, commitMessageCount={}, commitMessageBytes={}, compactedFiles={}, compactedBytes={}, producedFiles={}, producedBytes={}
```

这个日志在 `PaimonPrimaryKeyCompactionExecutor` 中输出，统计的是 Paimon `CommitMessageSerializer.serializeAll(...)` 后的原始 commit message 数量和总字节数。它能回答：“Paimon 本次 compact 产生了多大的 commit message？”

### optimizer completeTask 层

日志关键字：

```text
taskOutputBytes=
Optimizer executor
completeTask
```

发送前和成功后日志会记录：

```text
Optimizer executor[{}] completing task[{}](status: {}) to AMS {}, taskOutputBytes={}
Optimizer executor[{}] completed task[{}](status: {}) to AMS {}, taskOutputBytes={}
```

发送失败时日志会记录：

```text
Optimizer executor[{}] completed task[{}](status: {}) to AMS {} failed, taskOutputBytes={}
```

这个日志在 `OptimizerExecutor.completeTask(...)` 中输出，统计的是最终 `OptimizingTaskResult.taskOutput` 序列化后的字节数。它更接近 Thrift COMPLETE 请求里的大对象大小，适合和 `thrift-server.max-message-size` 对照。

## 排查判断过程

### 1. 先区分 Spark 完成和 AMS 完成

Spark UI 显示 job/stage/task `1/1` 成功，只能证明 Spark task 执行完成。Amoro 任务完成还依赖 optimizer 在 driver 侧把 `OptimizingTaskResult` 通过 Thrift 回传给 AMS。

所以看到 Spark 成功但 AMS 仍 `ACKED`，下一步应优先检查 optimizer driver 日志里的 `completeTask`。

### 2. AMS 无 completeTask 记录，说明请求没进应用层

AMS 日志中没有该 processId 的 `completeTask`、commit failure、TaskNotFound、IllegalTaskState 等记录，只看到 keeper 每小时把 ACKED task 放回 retry queue。

这说明 AMS 的任务状态机本身没有机会处理 completion，问题更靠近网络 / Thrift / client flush。

### 3. Optimizer completeTask 发生 Connection reset

Optimizer 侧堆栈显示异常发生在：

```text
OptimizingService$Client.send_completeTask
TServiceClient.sendBase
TFramedTransport.flush
TIOStreamTransport.write
SocketException: Connection reset
```

这与 payload 过大、超过 frame/message 限制或连接被服务端关闭高度吻合。

### 4. Paimon output 类型支持 payload 过大的解释

主键表 output 不是普通指标，而是 `List<byte[]> commitMessageBytesList`。这些 bytes 来自 Paimon `CommitMessageSerializer.serializeAll(messages)`。

结合主键表小文件多、MAJOR compact 文件元数据多，可以解释为什么 task 执行成功但回传失败。

### 5. 调大 Thrift size 后现象消失

调大 Thrift 大小后任务可以正常完成回传，验证了上述判断：根因不是 Paimon committer 没执行，也不是 AMS 没调度，而是 `completeTask` payload 超过原 Thrift message/frame 上限。

## 后续建议

1. 保留 Thrift message size 调整，并确保 optimizer 客户端和 AMS 服务端配置一致。
2. 对 Paimon 主键表 compaction 继续保持一个 partition-bucket unit 一个 Amoro task，避免多个 bucket 的 commit message 合并成更大的单次 COMPLETE payload。
3. 如果后续仍遇到类似问题，先查 optimizer driver 日志中的 `completeTask`，不要只看 Spark executor/stage 成功。
4. 可以考虑在主键表 compaction output summary 或日志中增加 commit message bytes 长度，便于后续提前发现接近 Thrift 上限的任务。
5. 长期更稳的方向是减少 COMPLETE 携带的大对象，例如把 Paimon commit message 存到外部临时位置后回传引用，或实现更细粒度的 partial commit / 分批 complete，但这属于后续版本设计。

## 快速排查清单

遇到 “Spark 成功但 Amoro task 仍 RUNNING/ACKED” 时：

1. 查 AMS 日志是否有该 `processId` 的 `completeTask`。
2. 查 AMS 是否只有 `poll` / `ACK` / `ACK timeout put to retry queue`。
3. 查 DB `task_runtime`：
   - `status=ACKED`
   - `end_time IS NULL`
   - `output_bytes IS NULL`
4. 查 optimizer driver 日志：
   - `Paimon primary-key compaction produced commit messages`
   - `taskOutputBytes=`
   - `OptimizerExecutor.completeTask`
   - `OptimizingService$Client.completeTask`
   - `TTransportException`
   - `TFramedTransport.flush`
   - `Connection reset`
   - `Frame size` / `message size`
5. 对大表、小文件多、MAJOR/FULL compaction，优先怀疑 `OptimizingTaskResult.taskOutput` 过大。
6. 核对并调大：
   - AMS：`thrift-server.max-message-size`
   - optimizer client URL 或 `PoolConfig.maxMessageSize`
