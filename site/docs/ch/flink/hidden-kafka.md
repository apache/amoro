因为传统离线数仓架构对实时业务不能很好支撑，所以这几年实时数仓演进迅速。在实时数仓架构中往往使用 Apache Kafka 作为实时数据的存储系统。但也带来与离线数仓数据割裂的问题。

开发者往往既需要关注存放在 HDFS 的数据，也需要关注 Kafka 中的数据，这给业务开发提高了复杂性。所以 Arctic 提出在表参数新增一个可选项开启 Logstore，`log-store.enable`，使得操作一张表便可获得秒级别和分钟级别延迟的数据，同时也能保证两者数据最终一致性。

##概要
**使用 Hidden Kafka 前提**

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
"log-store.enable" = "true",
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
    'log-store.enable' = 'true',
    'log-store.topic'='topic_log_test',
    'log-store.address'='localhost:9092');
```

**双写 Logstore 和 Filestore**

目前只有 Apache Flink 引擎实现双写 Logstore 和 Filestore 功能。