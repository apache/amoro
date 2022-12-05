## Introduce multi-catalog

Catalog 是一个包含了数据库，表，视图， 索引，用户和 UDF 等一系列信息的元数据空间，catalog 可以被简单理解为 table 和 database 的上一级 namespace。在实践中，一般将 catalog 指向特定类型的数据源实例或集群，在 Flink、Spark 和 Trino 中，可以通过 multi-catalog 功能来支持跨数据源的 SQL，如：

```SQL
SELECT c.ID, c.NAME, c.AGE, o.AMOUNT
FROM MYSQL.ONLINE.CUSTOMERS c JOIN HIVE.OFFLINE.ORDERS o
ON (c.ID = o.CUSTOMER_ID)
```

过去数据湖围绕 HMS 来管理元数据，遗憾的是 HMS 不支持 multi-catalog，导致引擎在数据湖上的功能存在一定限制，比如有些用户希望用 spark 通过指定 catalog 名称在不同 Hive 集群之间做联邦计算，需要用户在上层重构一套 Hive catalog plugin。其次，数据湖格式正在从 Hive 单极走向 Iceberg，Delta 以及 Hudi 多家争鸣的格局，新型的数据湖 format 对公有云更加友好，也会促进数据湖上云的进程，在这些背景下，需要一套面向 multi-catalog 的管理系统帮助用户治理不同环境，不同 format 的数据湖。

用户可以在 Arctic 中为不同环境，不同集群以及不同的 table format 创建 catalog，再利用 Flink、Spark、Trino 的 multi-catalog 功能实现多集群、多格式的联邦计算。同时，配置在 catalog 中的属性可以被所有表和用户共享，避免了重复设置。Arctic 通过 multi-catalog 的设计，为数据平台提供元数据中心的支持。

AMS 和 HMS 协同使用时，相当于使用 HMS 作为 AMS 的存储底座，结合 native Iceberg format，用户可以在不引入任何 Arctic 依赖的情况下，使用 AMS 的 multi-catalog 管理功能。

## How to use

在 Arctic v0.4 之后，引入了 catalog 管理功能，所有表的管理都要在 catalog 下完成，在 dashboard 的 catalogs 模块中创建，编辑和删除 catalog，创建 catalog 时需要配置 metastore，table format 以及环境配置信息，了解更多请参阅：Admin guide 

> 用户可以在 catalog properties 中配置给所有表配置参数，也可以在建表时指定 table properteis，在各个引擎执行时配置执行参数，这些参数的覆盖规则为：引擎优先于表，优先于 catalog。

在实践中，推荐按照下面的方式创建 catalog：

- 如果希望和 HMS 协同使用，Metastore 选择 Hive，format 根据需求选择 Mixed Hive 或 Iceberg
- 如果希望使用 Arctic 提供的 Mixed Iceberg format，Metastore 选择 Arctic

目前 Arctic catalog 创建时只能选择一种 table format，这主要考虑到引擎在使用 catalog 时会解析成特定的数据源，一对一的形式是符合直觉的，另一方面，直接使用 HMS 时可以突破这个限制，比如 Iceberg 社区提供的 SessionCatalog 实现，未来 Arctic 会考虑为用户提供更加灵活的管理方式。

## Future work

AMS 未来会围绕两个目标提升元数据中心的价值：

- 扩展数据源 —  除数据湖之外，消息队列，数据库，数据仓库都可以作为 catalog 托管的对象，通过元数据中心和计算引擎基于 SQL 的联邦计算，为 DataOps、DataFabric 等数据平台提供基础设施方案
- catalog 自动感知 — 在 spark、flink 等计算引擎里能够自动感知到 catalog 的创建和变更，做到一次配置，永久扩展
