因为传统离线数仓架构对实时业务不能很好支撑，所以这几年实时数仓演进迅速。在实时数仓架构中往往使用 Apache Kafka 作为实时数据的存储系统。但也带来与离线数仓数据割裂的问题。

开发者往往既需要关注存放在 HDFS 的数据，也需要关注 Kafka 中的数据，这给业务开发提高了复杂性。所以 Arctic 提出在表参数新增一个可选项开启 Logstore，`log-store.enabled`，使得操作一张表便可获得秒级别和分钟级别延迟的数据，同时也能保证两者数据最终一致性。

##概要
###使用 Logstore 前提

新建 Arctic 表时，需要开启 Logstore。

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

###双写 Logstore 和 Filestore

![Introduce](../images/double-write.png){:height="70%" width="70%"}

Arctic Connector 通过双写写入 Logstore 和 Changestore，不会开启 Kafka transaction 保证两者数据一致性，因为这会给下游任务带来数分钟延迟（与上游任务 Checkpoint 时间间隔有关）。

当上游任务重启或是发生 failover 时，会有冗余数据发送到 Logstore，下游任务会辨识出冗余部分数据，将这部分冗余数据进行回撤来保证数据最终一致性。
Logstore 的配置请参考[这里](../meta-service/table-properties.md#logstore)，消费 Kafka 的配置请参考[这里](flink-dml.md#logstore)。
> **TIPS**
>
> 目前只有 Apache Flink 引擎实现双写 Logstore 和 Filestore 功能。
> 

###开启一致性读取
```sql
select * from arctic.db.arctic
/*+ OPTIONS('arctic.read.mode'='log','log-store.consistency-guarantee.enable'='true') */;

--或者是创建表时开启一致性读取
create table catalog.db.arctic (
    ...
) WITH (
    'log-store.enabled' = 'true',
    'log-store.topic'='topic_log_test',
    'log-store.address'='localhost:9092',
    'log-store.consistency-guarantee.enable'='true'
);
```

###限制

- 下游任务开启一致性保证时，不能包含 Cumulate Window Agg Operator，因为这个算子不能处理 Update_before/Delete 数据。
- 目标端不能处理 Delete 数据，例如下游任务是 ETL 任务，只 Append 数据到目标端。
