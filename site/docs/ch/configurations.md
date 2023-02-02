## 多级配置管理

Arctic 提供的参数都可以在 catalog, table, 引擎端配置，配置的优先级为引擎优先表，优先 catalog，一般情况下，我们推荐用户在 catalog 中设置默认值，比如 self-optimizing 相关参数，推荐用户在 [创建表](guides/managing-tables.md##_1) 时指定表的个性化配置，也可以通过 [修改表](guides/managing-tables.md##_2) 修改配置，如果要在引擎中针对性调优，再考虑在引擎中配置

## Self-optimizing 配置

Self-optimizing 配置对 Iceberg format, Mixed streaming format 都会生效。


| 配置名称                                             | 默认值            | 描述                                               |
|-----------------------------------------------------|------------------|---------------------------------------------------|
| self-optimizing.enabled                             | true             | 是否开启 self-optimizing                                    |
| self-optimizing.group                               | default          | self-optimizing 所属的 optimizer 组                                     |
| self-optimizing.quota                               | 0.1              | 表所能占用的 self-optimizing 资源量                             |
| self-optimizing.num-retries                         | 5                | self-optimizing 失败时的重试次数                               |
| self-optimizing.execute.timeout                     | 1800000（30分钟） | self-optimizing 执行超时时间，超时会失败重试                                  |
| self-optimizing.target-size                         | 134217728（128MB）| self-optimizing 的目标文件大小                                |
| self-optimizing.max-file-count                      | 100000           | 一次 self-optimizing 最多处理的文件个数                           |               |
| self-optimizing.fragment-ratio                      | 8                | fragment 文件大小阈值，实际计算时取倒数与  self-optimizing.target-size 的值相乘                         |
| self-optimizing.minor.trigger.file-count            | 12               | 触发 minor optimizing 的 fragment 最少文件数量             |
| self-optimizing.minor.trigger.interval              | 3600000（1小时）  | 触发 minor optimizing 的最长时间间隔                        |
| self-optimizing.major.trigger.file-count            | 12               | 触发 major optimizing 的最少文件数量                      |
| self-optimizing.major.trigger.duplicate-ratio       | 0.2              | 在一个 target-size 空间内，在主键上重复的数据量占比到达 duplicate-ratio 阈值后出发 major optimizing  |
| self-optimizing.major.trigger.interval              | 86400000（1天）   | 触发 major optimizing 的最长时间间隔                        |
| self-optimizing.full.trigger.interval               | -1（关闭）         | 触发 full optimizing 的最长时间间隔       

## 数据清理配置

数据清理配置对 Iceberg format, Mixed streaming format 都会生效。

| 配置名称                                        | 默认值       | 描述                                 |
|---------------------------------------------|-----------|------------------------------------|
| table-expire.enabled                        | true      | 是否开启的表过期数据自动清理                     |
| change.data.ttl.minutes                     | 10080（7天） | ChangeStore 数据的过期时间                |
| snapshot.change.keep.minutes                | 10080（7天） | ChangeStore 历史快照的保留时间              |
| snapshot.base.keep.minutes                  | 720（12小时） | BaseStore 历史快照的保留时间                |
| clean-orphan-file.enabled                   | false     | 是否开启孤儿文件自动清理                       |
| clean-orphan-file.min-existing-time-minutes | 2880（2天）  | 存在时间超过 min-existing-time-minutes 未被引用的孤儿文件会被自动清理 |

## Mixed streaming format

如果使用 native Iceberg format 表，请参阅 [Iceberg configurations](https://iceberg.apache.org/docs/latest/configuration/)，以下参数对 Mixed streaming format 有效。

### 表读取配置

| 配置名称                            | 默认值             | 描述                                     |
| ---------------------------------- | ---------------- | ----------------------------------       |
| read.split.open-file-cost          | 4194304（4MB）    | 预估与读取开销等价的打开一个文件开销，一般不改                        |
| read.split.planning-lookback       | 10               | 拆分读取任务所使用分桶算法里桶的个数               |
| read.split.target-size              | 134217728（128MB）| 查询引擎中的读取任务会尽量拆分成这个大小                     |

### 表写入配置

| 配置名称                            | 默认值             | 描述                                     |
| ---------------------------------- | ---------------- | ----------------------------------       |
| base.write.format                  | parquet          | BaseStore 的文件格式，只对有主键表有效        |
| change.write.format                | parquet          | ChangeStore 的文件格式，只对有主键表有效      |
| write.format.default               | parquet          | 表的默认写入文件格式，只对无主建表有效          |
| base.file-index.hash-bucket        | 4                | BaseStore auto-bucket 的初始 bucket 个数         |
| change.file-index.hash-bucket      | 4                | ChangeStore auto-bucket 文件索引的初始 bucket 个数       |
| write.target-file-size-bytes       | 134217728（128MB）| 文件写入时的目标文件大小                     |
| write.upsert.enabled               | false            | 是否开启 upsert 写入模式，开启后相同主键的多条 insert 数据会被合并   |
| write.distribution-mode            | hash             | 数据写入时的 shuffle 规则，无主键表可以选择 none、hash，有主键表目前只能选择 hash                  |
| write.distribution.hash-mode       | auto             | 使用 auto-bucket hash，支持 primary-key、partition-key、primary-partition-key 和 auto  |

### Logstore 相关配置

| 配置名称                            | 默认值             | 描述                                     |
| ---------------------------------- | ---------------- | ----------------------------------       |
| log-store.enabled                  | false            | 是否开启 Logstore                        |
| log-store.type                     | kafka            | Logstore 的类型，当前支持 'kafka'、'pulsar'            |
| log-store.address                  | NULL             | 当 log-store.enabled=true 时必填，Logstore 的地址。对于 Kafka，为 kafka bootstrap servers地址；对于 Pulsar，为 Pulsar Service url，如: 'pulsar://localhost:6650'|
| log-store.topic                    | NULL             | 当 log-store.enabled=true 时必填，Logstore 使用的 topic                      
| log-store.consistency-guarantee.enabled   | false     | 标记是否开启一致性保证，目前 log-store.type=plusar 时不支持开启一致性保证 |
| properties.pulsar.admin.adminUrl   | NULL             | Logstore 是 pulsar 时必填，否则可不填。Pulsar admin 的 HTTP URL，如：http://my-broker.example.com:8080|
| properties.XXX                     | NULL             | Logstore的参数。<br><br>对于 Logstore 为 Kafka ('log-store.type'='kafka' 默认值)时，Kafka Consumer/Producer 支持的其他所有参数都可以通过在前面拼接 `properties.` 的前缀来设置，<br>如：`'properties.batch.size'='16384'`，<br>完整的参数信息可以参考 [Kafka consumer 配置](https://kafka.apache.org/documentation/#consumerconfigs)、[kafka producer 配置](https://kafka.apache.org/documentation/#producerconfigs)；<br><br>对于 Logstore 为 Pulsar ('log-store.type'='pulsar')时，Pulsar 支持的相关配置都可以通过在前面拼接 `properties.` 的前缀来设置，<br>如：`'properties.pulsar.client.requestTimeoutMs'='60000'`，<br>完整的参数信息可以参考 [Flink-Pulsar-Connector文档](https://nightlies.apache.org/flink/flink-docs-release-1.16/docs/connectors/datastream/pulsar)


### Watermark 相关配置

| 配置名称                                       | 默认值               | 描述                                     |
| ----------------------------------------------| ------------------- | ----------------------------------       |
| table.event-time-field                        | _ingest_time        | 计算 watermark 的事件时间字段，默认的 _ingest_time 表示使用数据写入时间来计算 |
| table.watermark-allowed-lateness-second       | 0                   | 计算 watermark 时允许的数据乱序时间           |
| table.event-time-field.datetime-string-format | `yyyy-MM-dd HH:mm:ss` | 当事件时间为字符串时，事件时间的格式           |
| table.event-time-field.datetime-number-format | TIMESTAMP_MS | 当事件时间为数字时，事件时间的格式，支持 TIMESTAMP_MS(毫秒级时间戳)与TIMESTAMP_S(秒级时间戳) |

### Mixed Hive format 相关配置

| 配置名称                            | 默认值             | 描述                                     |
| ---------------------------------- | ---------------- | ----------------------------------       |
| base.hive.auto-sync-schema-change  | true             | 是否从 HMS 自动同步 Hive 的 schema 变更             |
| base.hive.auto-sync-data-write     | false            | 是否自动同步 Hive 的原生的数据写入，有 Hive 原生数据写入时需要打开             |
