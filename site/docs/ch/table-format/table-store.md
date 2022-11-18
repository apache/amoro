# 表结构
Arctic 能够兼容已有的存储介质(如 HDFS、OSS)和表结构(如 Hive、Iceberg)，并在之上提供透明的流批一体表服务。
同时 Arctic 提供自动的结构优化，以帮组用户解决数据湖常见的小文件、读放大、写放大等问题。

## 存储结构
对于一张定义了主键的 Arctic 表，存储结构上最多可以拆分为三部分：Changestore、Basestore、Logstore。

![TableStructure](../images/format/table-structure.png){:height="70%" width="70%"}

### Changestore
Changestore 中存储了表上最近的变更数据。
它通常由 Apache Flink 任务实时写入，并用于下游 Flink 任务进行近实时的流式消费。
同时也可以对它直接进行批量计算或联合 Basestore 里的数据一起通过 Merge-On-Read 的查询方式提供分钟级延迟的批量查询能力。

Arctic 内 Changestore 一般是一张单独的 Iceberg 表，它与 Arctic 表拥有相同的表结构和分区配置。
Changestore 内包含了存储插入数据的 insert file 和存储删除数据的 equality delete file，更新数据会被拆分为更新前项和更新后项分别存储在 delete file 与 insert file中。

### Basestore
Basestore 中存储了表的存量数据。
它通常由 Apache Spark 等引擎完成第一次写入，再之后则通过自动的结构优化过程将 Changestore 中的数据转化之后写入。

Arctic 内 Basestore 现阶段支持 Hive Table 与 Iceberg Table 并可以支持更多的扩展，它与 Arctic 表拥有相同的表结构和分区规则。
Basestore 内包含了存储数据文件的 base file 和存储已经被删除数据的 positional delete file，相较于 Changestore 中的 equality delete file，positional delete file 拥有更好的 merge-on-read 性能。

### Logstore
尽管 Changestore 已经能够为表提供近实时的 CDC 能力，但在对延迟有更高要求的场景仍然需要诸如 Apache Kafka 这样的消息队列提供毫秒级的 CDC 数据分发能力。
而消息队列在 Arctic 表中被封装为 Logstore。它由 Flink 任务实时写入，并用于下游 Flink 任务进行实时消费。


