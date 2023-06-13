## Iceberg format

Arctic v0.4 之后开始支持管理原生 [Iceberg](https://iceberg.apache.org) 表， 用户只需要在 Arctic 上将原生 Iceberg 的 catalog 登记进来即可将原生的 Iceberg 表托管给 Arctic 维护其表的性能及可用性,
且 Arctic 对原生 Iceberg 的原生功能没有任何侵入性。
Iceberg format 具有充分的向上和向下兼容特性，一般情况下，用户不用担心引擎客户端所用的 Iceberg 版本与 Arctic 依赖的 Iceberg 版本的兼容性。

Arctic 同时支持 Iceberg format v1 和 v2。关于 v1 和 v2 的区别见 [v1 v2 区别](https://iceberg.apache.org/spec/)
Arctic 支持原生 Iceberg 支持的所有 catalog 类型，包括但不限于: Hadoop, Hive, Glue, JDBC, Nessie, Snowflake，DynamoDb ..等类型
Arctic 支持原生 Iceberg 支持的所有存储类型，包括但不限于: Hadoop, S3, AliyunOSS, GCS, ECS ..等类型
