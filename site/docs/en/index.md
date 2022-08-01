# Overview
Welcome to arctic, arctic is a streaming lake warehouse system open sourced by NetEase.
Arctic adds more real-time capabilities on top of iceberg and hive, and provides stream-batch unified, out-of-the-box metadata services for dataops,
allowing Data lakes much more usable and practical.

### Introduction

Arctic is a streaming lakehouse service built on top of apache iceberg table format.
Through arctic, users could benefit optimized CDC、streaming update、fresh olap etc. on engines like flink, spark, and trino.
Combined with efficient offline processing capabilities of data lakes, arctic can serve more scenarios where streaming and batch are fused.
At the same time, the function of self-optimization、concurrent conflict resolution and standard management tools could effectively reduce the burden on users in data lake management and optimization.
![Introduce](images/arctic_introduce.png)
Arctic service is presented by deploying AMS, which can be considered as a replacement for HMS (Hive Metastore), or HMS for iceberg.
Arctic uses iceberg as the base table format, but instead of hacking the iceberg implementation, it uses iceberg as a lib.
Arctic's open overlay architecture can help large-scale offline data lakes quickly upgraded to real-time data lakes, without worrying about compatibility issues with the original data lakes,
allowing data lakes to meet more real-time analysis, real-time risk control, Real-time training, feature engineering and other scenarios.

### Arctic features

* Efficient streaming updates based on primary keys
* Data auto bucketing and self-optimized for performance and efficiency
* Encapsulating data lake and message queue into a unified table to achieve lower-latency computing
* Provide standardized metrics, dashboard and related management tools for streaming lakehouse
* Support spark and flink to read and write data, support trino to query data
* 100% compatible with iceberg / hive table format and syntax
* Provide transactional guarantee for streaming and batch concurrent writing


### Architecture and concepts
Arctic 的组件包括 AMS，optimizer 以及 dashboard，如下所示：
![Architecture](images/arctic_architecture.png)

**AMS**

Arctic Meta Service，在 arctic 架构中，AMS 定义为新一代 HMS，AMS 管理 arctic 所有 schema，向计算引擎提供元数据服务和事务 API，以及负责触发后台结构优化任务。

**Transaction**

Arctic 将一次数据提交定义为事务，并且保障流和批并发写入下的事务一致性语义，与 iceberg 提供的 ACID 不同，arctic 因为支持 CDC 摄取和流式更新，需要保障基于主键的数据一致性。

**TableStore**

TableStore 是 arctic 在数据湖上存储的表格式实体，TableStore 类似于数据库中的 cluster index，代表独立的存储结构，实现中一个 TableStore 是一张 iceberg 表，数据流式写入和批式写入会分别进入 arctic 的 ChangeStore 和 BaseStore，在查询时 arctic 会在多个 TableStore 上提供整合的视图，后续在 arctic 上扩展 sort key 或 aggregate key 也将通过扩展 TableStore 来实现。

**Optimizing**

Arctic 作为流式湖仓服务，会在后台持续进行文件结构优化操作，并致力于这些优化任务的可视化和可测量，优化操作包括但不限于小文件合并，数据分区，数据在 TableStore 之间的合并转化。

- `Optimizing planner` 决定了优化任务的调度策略，Arctic 支持在表属性中设置 quota，以此影响 Optimizing planner 在单表结构优化占用的资源。
- `Optimizer container` 是 optimizing 任务调度的容器，目前支持两种调度：standalone 和 yarn，standalone 在 AMS 本地调度，适合测试，arctic 支持用户扩展 optimizer container 实现。
- `Optimizer group` 用于资源隔离，optimizing container 下可以设置一个或多个 optimizer group，也可以通过 optimizer group 保障优先级，在 yarn 上 optimizer container 对应队列。


