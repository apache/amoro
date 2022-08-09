## 概要
Apache Flink 引擎可以在批流模式处理 Arctic 表数据。Flink on Arctic connector 提供读写 Arctic 数据湖的能力且满足数据一致性保证，也提供实时维表关联 Arctic 数据湖的能力。为了满足对数据实时性要求很高的业务，Arctic 数据湖底层存储结构设计了 Logstore，其存放着最新的 changelog 或 append-only 实时数据。

Arctic 集成了 [Apache Flink](https://flink.apache.org/) 的 DataStream API 与 Table API，以方便的使用 Flink 从 Arctic 表中读取数据，或将数据写入
Arctic 表中。

Flink Connector 包括：

- `Flink SQL Select` 通过 Apache Flink SQL 读取 Arctic 表数据。
- `Flink SQL Insert` 通过 Apache Flink SQL 将数据写入到 Arctic 表。
- `Flink SQL DDL` 通过 Apache Flink DDL 语句创建/修改/删除库和表。
- `FlinkSource` 通过 Apache Flink DS API 读取 Arctic 表数据。
- `FlinkSink` 通过 Apache Flink DS API 将数据写入到 Arctic 表。
- `Flink Lookup Join` 通过 Apache Flink Temporal Join 语法实时读取 Arctic 表数据进行关联计算。

版本说明：

| Connector Version | Flink Version     | Dependent Iceberg Version |  下载 |
| ----------------- | ----------------- |  ----------------- |  ----------------- |
| 0.3.0             | 1.12.x            | 0.12.0            | [flink-1.12-0.3.0-rc1](https://github.com/NetEase/arctic/releases/download/v0.3.0-rc1/arctic-flink-runtime-1.12-0.3.0.jar)|
| 0.3.0             | 1.14.x            | 0.12.0            | [flink-1.14-0.3.0-rc1](https://github.com/NetEase/arctic/releases/download/v0.3.0-rc1/arctic-flink-runtime-1.14-0.3.0.jar)|

对 Arctic 工程自行编译也可以获取该 runtime jar

`mvn clean package -Pflink-1.14 -DskipTests`

Flink Runtime Jar 存放在 `flink/v1.14/flink-runtime/target` 目录。

## Roadmap
- [Arctic 表支持实时维表 JOIN](https://github.com/NetEase/arctic/issues/94)
- [批模式下 MOR](https://github.com/NetEase/arctic/issues/5)
- [Arctic 主键表支持 insert overwrite](https://github.com/NetEase/arctic/issues/4)
- [Arctic 完善 DDL 语法，例如 Alter Table](https://github.com/NetEase/arctic/issues/2)

## 常见问题
### 写 Arctic 表 File 数据不可见
需要打开 Flink checkpoint，修改 Flink conf 中的 [Flink checkpoint 配置](https://nightlies.apache.org/flink/flink-docs-release-1.12/deployment/config.html#execution-checkpointing-interval)，数据只有在 checkpoint 时才会提交。
