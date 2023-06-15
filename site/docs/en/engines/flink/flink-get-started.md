## Overview
The Apache Flink engine can process Arctic table data in batch and streaming mode. The Flink on Arctic connector provides the ability to read and write to the Arctic data lake while ensuring data consistency. To meet the high real-time data requirements of businesses, the Arctic data lake's underlying storage structure is designed with LogStore, which stores the latest changelog or append-only real-time data.

Arctic integrates the DataStream API and Table API of [Apache Flink](https://flink.apache.org/) to facilitate the use of Flink to read data from Arctic tables or write data to Arctic tables.

The documents under Arctic Flink directory only apply to Mixed-Format. If you are using Iceberg format tables, please refer to the official usage of Iceberg.

[Iceberg Flink user manual](https://iceberg.apache.org/docs/latest/flink-connector/)

Flink Connector includes:

- `Flink SQL Select` reads Arctic table data through Apache Flink SQL. 
- `Flink SQL Insert` writes data to Arctic tables through Apache Flink SQL. 
- `Flink SQL DDL` creates/modifies/deletes libraries and tables through Apache Flink DDL statements. 
- `FlinkSource` reads Arctic table data through Apache Flink DS API. 
- `FlinkSink` writes data to Arctic tables through Apache Flink DS API. 
- `Flink Lookup Join` performs real-time read of Arctic table data for association calculation through Apache Flink Temporal Join grammar.

Version Description:

| Connector Version | Flink Version | Dependent Iceberg Version | Download                                                                                                                         |
| ----------------- |---------------|  ----------------- |--------------------------------------------------------------------------------------------------------------------------|
| 0.4.0             | 1.12.x        | 0.13.2            | [flink-1.12-0.4.0](https://github.com/NetEase/arctic/releases/download/v0.4.0/arctic-flink-runtime-1.12-0.4.0.jar) |
| 0.4.0             | 1.14.x        | 0.13.2            | [flink-1.14-0.4.0](https://github.com/NetEase/arctic/releases/download/v0.4.0/arctic-flink-runtime-1.14-0.4.0.jar) |
| 0.4.0             | 1.15.x        | 0.13.2            | [flink-1.15-0.4.0](https://github.com/NetEase/arctic/releases/download/v0.4.0/arctic-flink-runtime-1.15-0.4.0.jar) |

Kafka as LogStore Version Description:

| Connector Version | Flink Version | Kafka Versions |
| ----------------- |---------------|  ----------------- |
| 0.4.0             | 1.12.x        | 0.10.2.\*<br> 0.11.\*<br> 1.\*<br> 2.\*<br> 3.\*            | 
| 0.4.0             | 1.14.x        | 0.10.2.\*<br> 0.11.\*<br> 1.\*<br> 2.\*<br> 3.\*            | 
| 0.4.0             | 1.15.x        | 0.10.2.\*<br> 0.11.\*<br> 1.\*<br> 2.\*<br> 3.\*            | 


The Arctic project can be self-compiled to obtain the runtime jar.

`mvn clean package -pl ':arctic-flink-runtime-1.14' -am -DskipTests`

The Flink Runtime Jar is located in the `flink/v1.14/flink-runtime/target` directory.

## Environment Preparation
Download Flink and related dependencies, and download Flink 1.12/1.14/1.15 as needed. Taking Flink 1.12 as an example:

```shell
FLINK_VERSION=1.12.7
SCALA_VERSION=2.12
APACHE_FLINK_URL=archive.apache.org/dist/flink
HADOOP_VERSION=2.7.5

## Download Flink 1.12.x package, currently arctic-flink-runtime jar package uses scala 2.12
wget ${APACHE_FLINK_URL}/flink-${FLINK_VERSION}/flink-${FLINK_VERSION}-bin-scala_${SCALA_VERSION}.tgz
## Unpack the file
tar -zxvf flink-1.12.7-bin-scala_2.12.tgz

# Download hadoop dependency
wget https://repo1.maven.org/maven2/org/apache/flink/flink-shaded-hadoop-2-uber/${HADOOP_VERSION}-10.0/flink-shaded-hadoop-2-uber-${HADOOP_VERSION}-10.0.jar
# Download arctic flink connector
wget https://github.com/NetEase/arctic/releases/download/v0.4.0-rc2/arctic-flink-runtime-1.12-0.4.0.jar
```

Modify Flink related configuration files:

```shell
cd flink-1.12.7
vim conf/flink-conf.yaml
```
Modify the following settings:

```yaml
# Increase the number of slots to run two streaming tasks simultaneously
taskmanager.numberOfTaskSlots: 4
# Enable Checkpoint. Only when Checkpoint is enabled, the data written to the file is visible
execution.checkpointing.interval: 10s
```

Move the dependencies to the lib directory of Flink:

```shell
# Used to create a socket connector for inputting CDC data via sockets. Not necessary for non-quickstart examples.
cp examples/table/ChangelogSocketExample.jar lib

cp ../arctic-flink-runtime-1.12-0.3.0.jar lib
cp ../flink-shaded-hadoop-2-uber-${HADOOP_VERSION}-10.0.jar lib
```

## Hive compatibility
Starting from Arctic version 0.3.1, Hive compatibility is supported, and data in Arctic Hive compatible tables can be read/written through Flink. When operating on Hive-compatible tables through Flink, the following points should be noted:

1. Flink Runtime Jar does not include the content of the Jar packages that Hive depends on. You need to manually put the [Hive-dependent Jar package](https://repo1.maven.org/maven2/org/apache/hive/hive-exec/2.1.1/hive-exec-2.1.1.jar) in the flink/lib directory; 
2. When creating partitioned tables, the partition field needs to be placed in the last column; when there are multiple partition fields, they need to be placed at the end; 
3. For Hive-compatible tables, the [table creation method](flink-ddl.md) and [reading/writing method](flink-dml.md) are consistent with non-Hive-compatible Arctic tables; 
4. Hive version 2.1.1 is supported.


## Frequently Asked Questions

**Data written to Arctic table is not visible**

You need to enable Flink checkpoint and modify the [Flink checkpoint configuration](https://nightlies.apache.org/flink/flink-docs-release-1.12/deployment/config.html#execution-checkpointing-interval) in Flink conf. The data will only be committed during checkpoint.

**When using Flink SQL-Client to read Arctic tables with write.upsert feature enabled, there are still duplicate primary key data**

The query results obtained through Flink SQL-Client cannot provide MOR semantics based on primary keys. If you need to obtain merged results through Flink engine queries, you can write the content of Arctic tables to a MySQL table through JDBC connector for viewing.

**When writing to Arctic tables with write.upsert feature enabled through SQL-Client under Flink 1.15, there are still duplicate primary key data**

You need to execute the command `set table.exec.sink.upsert-materialize = none` in SQL-Client to turn off the upsert materialize operator generated upsert view. This operator will affect the ArcticWriter's generation of delete data when the write.upsert feature is enabled, causing duplicate primary key data to not be merged.

## Version compatibility and migration

- Starting from version 0.4.1, the API of LogStore has been refactored and migrated to the new interface of Flink FLIP-27. For tasks that use Arctic Flink version <= 0.4.0 to read Arctic LogStore (Kafka), when upgrading the old checkpoint state to the new version, please note:
  1. Set the SQL Hint parameter to use the deprecated API: `log-store.kafka.compatible.enabled = true`, see [Read LogStore](flink-dml.md#Real-Time Data in LogStore). Otherwise, there may be duplicate data. 
  2. If conditions permit, upgrade to the new API. When running tasks with old versions of Arctic Flink, interrupt the data flow of Kafka for a short time, that is, do not write data to LogStore Kafka for a period of time to ensure that the task has successfully checkpointed and completed Kafka Offset submission. Then stop the task, upgrade to the new version of Arctic Flink, and restore the task from the previous state. Then restore the normal writing of upstream LogStore Kafka.