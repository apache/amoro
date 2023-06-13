由于传统离线数据仓库架构的局限性，无法有效支持实时业务需求，近年来实时数据仓库得到了快速发展。在实时数据仓库架构中，通常使用 Apache Kafka 作为实时数据存储系统，但这也导致了离线数据仓库与实时数据的分离问题。

开发人员通常需要同时关注存储在 HDFS 中的数据和 Kafka 中的数据，这增加了业务开发的复杂性。为解决这一问题，Arctic 提出在表参数中新增了一个可选项"开启 LogStore"（`log-store.enabled`），通过这个选项，可以在操作单张表时同时获取秒级和分钟级的数据延迟，并确保这两个数据源的最终一致性。
## 概要
### 使用 LogStore 前提

|  Flink   |  Kafka   |  Pulsar   |
|-----|-----|-----|
|  Flink 1.12   |  &#x2714   |  &#x2714   |
|  Flink 1.14   |  &#x2714   |  &#x2716   |
|  Flink 1.15   |  &#x2714   |  &#x2716   |


新建 Arctic 表时，需要开启 LogStore。

- 可以在 Arctic [Dashboard](http://localhost:1630) - Terminal 页面选择具体的 Catalog 后创建表

```sql
create table db.log_table (
    id int,
    name string,
    ts timestamp,
    primary key (id)
) using arctic
tblproperties (
"log-store.enabled" = "true",
"log-store.topic"="topic_log_test",
"log-store.address"="localhost:9092"
);
```

- 也可以使用 Flink SQL 在 Flink-SQL-Client 创建表

```sql
-- 首先使用 use catalog 命令切换到 arctic catalog。
create table db.log_table (
    id int,
    name string,
    ts timestamp,
    primary key (id) not enforced
) with (
    'log-store.enabled' = 'true',
    'log-store.topic'='topic_log_test',
    'log-store.address'='localhost:9092');
```

### 双写 LogStore 和 FileStore

![Introduce](../images/double-write.png){:height="70%" width="70%"}

Arctic Connector通过双写操作将数据同时写入 LogStore 和 ChangeStore，而不开启 Kafka 事务以确保两者数据的一致性，因为开启事务会给下游任务带来数分钟的延迟（具体延迟时间取决于上游任务的检查点间隔）。

当上游任务重新启动或发生故障切换时，会导致冗余数据发送到 LogStore。下游任务会识别并回滚这部分冗余数据，以保证数据的最终一致性。

有关 LogStore 的配置，请参考[这里](../configurations.md#logstore)，消费Kafka的配置请参考[这里](flink-dml.md#logstore)。

> 目前只有 Apache Flink 引擎实现双写 LogStore 和 FileStore 功能。 

### 开启一致性读取
```sql
select * from arctic.db.arctic
/*+ OPTIONS('arctic.read.mode'='log','log-store.consistency-guarantee.enabled'='true') */;

--或者是创建表时开启一致性读取
create table catalog.db.arctic (
    ...
) WITH (
    'log-store.enabled' = 'true',
    'log-store.topic'='topic_log_test',
    'log-store.address'='localhost:9092',
    'log-store.consistency-guarantee.enabled'='true'
);
```

### Hint Options
|Key|默认值|类型|是否必填|描述|
|--- |--- |--- |--- |--- |
|log.consumer.changelog.modes| all-kinds   | String   |否| 读log数据时会产生的RowKind类型，支持：all-kinds, append-only。<br>all-kinds：会读出cdc的数据，包括+I/-D/-U/+U；<br>append-only：只会产生Insert数据，读无主键时建议使用此配置。|

### 限制

- 下游任务开启一致性保证时，不能包含 Cumulate Window Agg Operator，因为这个算子不能处理 Update_before/Delete 数据。
- 目标端不能处理 Delete 数据，例如下游任务是 ETL 任务，只 Append 数据到目标端。
