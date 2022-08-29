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

| Connector Version | Flink Version | Dependent Iceberg Version | 下载                                                                                                                         |
| ----------------- |---------------|  ----------------- |----------------------------------------------------------------------------------------------------------------------------|
| 0.3.0             | 1.12.x        | 0.12.0            | [flink-1.12-0.3.0-rc1](https://github.com/NetEase/arctic/releases/download/v0.3.0-rc1/arctic-flink-runtime-1.12-0.3.0.jar) |
| 0.3.0             | 1.14.x        | 0.12.0            | [flink-1.14-0.3.0-rc1](https://github.com/NetEase/arctic/releases/download/v0.3.0-rc1/arctic-flink-runtime-1.14-0.3.0.jar) |
| 0.3.0             | 1.15.x        | 0.12.0            | [flink-1.15-0.3.0-rc1](https://github.com/NetEase/arctic/releases/download/v0.3.0-rc1/arctic-flink-runtime-1.15-0.3.0.jar) |

对 Arctic 工程自行编译也可以获取该 runtime jar

`mvn clean package -Pflink-1.14 -DskipTests`

Flink Runtime Jar 存放在 `flink/v1.14/flink-runtime/target` 目录。

## 环境准备
下载flink和相关依赖，按需下载 Flink 1.12/1.14/1.15。以 1.12 为例：

```shell
FLINK_VERSION=1.12.7
SCALA_VERSION=2.12
APACHE_FLINK_URL=archive.apache.org/dist/flink
HADOOP_VERSION=2.7.5

## 下载 Flink 1.12.x 包，目前 Arctic-flink-runtime jar 包使用 scala 2.12
wget ${APACHE_FLINK_URL}/flink-${FLINK_VERSION}/flink-${FLINK_VERSION}-bin-scala_${SCALA_VERSION}.tgz
## 解压文件
tar -zxvf flink-1.12.7-bin-scala_2.12.tgz

# 下载 hadoop 依赖
wget https://repo1.maven.org/maven2/org/apache/flink/flink-shaded-hadoop-2-uber/${HADOOP_VERSION}-10.0/flink-shaded-hadoop-2-uber-${HADOOP_VERSION}-10.0.jar
# 下载 arctic flink connector
wget https://github.com/NetEase/arctic/releases/download/v0.3.0-rc1/arctic-flink-runtime-1.12-0.3.0.jar
```

修改 Flink 相关配置文件：

```shell
cd flink-1.12.7
vim conf/flink-conf.yaml
```
修改下面的配置：

```yaml
# 需要同时运行两个流任务，增加 slot
taskmanager.numberOfTaskSlots: 4
# 开启 Checkpoint。只有开启 Checkpoint，写入 file 的数据才可见
execution.checkpointing.interval: 10s
```

将依赖移到 Flink 的 lib 目录中：

```shell
# 用于创建 socket connector，以便通过 socket 输入 CDC 数据。非 quickstart 案例流程可以不添加
cp examples/table/ChangelogSocketExample.jar lib

cp ../arctic-flink-runtime-1.12-0.3.0.jar lib
cp ../flink-shaded-hadoop-2-uber-${HADOOP_VERSION}-10.0.jar lib
```

## Roadmap
- [Arctic 表支持实时维表 JOIN](https://github.com/NetEase/arctic/issues/94)
- [批模式下 MOR](https://github.com/NetEase/arctic/issues/5)
- [Arctic 主键表支持 insert overwrite](https://github.com/NetEase/arctic/issues/4)
- [Arctic 完善 DDL 语法，例如 Alter Table](https://github.com/NetEase/arctic/issues/2)

## 常见问题
### 写 Arctic 表 File 数据不可见
需要打开 Flink checkpoint，修改 Flink conf 中的 [Flink checkpoint 配置](https://nightlies.apache.org/flink/flink-docs-release-1.12/deployment/config.html#execution-checkpointing-interval)，数据只有在 checkpoint 时才会提交。
