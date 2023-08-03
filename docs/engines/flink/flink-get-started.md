---
title: "Flink Getting Started"
url: flink-getting-started
aliases:
    - "flink/getting-started"
menu:
    main:
        parent: Flink
        weight: 100
---
# Flink Getting Started

## Iceberg format

The Iceberg Format can be accessed using the Connector provided by Iceberg.
Refer to the documentation at [Iceberg Flink user manual](https://iceberg.apache.org/docs/latest/flink-connector/)
for more information.

## Mixed format
The Apache Flink engine can process Amoro table data in batch and streaming mode. The Flink on Amoro connector provides the ability to read and write to the Amoro data lake while ensuring data consistency. To meet the high real-time data requirements of businesses, the Amoro data lake's underlying storage structure is designed with LogStore, which stores the latest changelog or append-only real-time data.

Amoro integrates the DataStream API and Table API of [Apache Flink](https://flink.apache.org/) to facilitate the use of Flink to read data from Amoro tables or write data to Amoro tables.

Flink Connector includes:

- `Flink SQL Select` reads Amoro table data through Apache Flink SQL.
- `Flink SQL Insert` writes data to Amoro tables through Apache Flink SQL.
- `Flink SQL DDL` creates/modifies/deletes libraries and tables through Apache Flink DDL statements.
- `FlinkSource` reads Amoro table data through Apache Flink DS API.
- `FlinkSink` writes data to Amoro tables through Apache Flink DS API.
- `Flink Lookup Join` performs real-time read of Amoro table data for association calculation through Apache Flink Temporal Join grammar.

Version Description:

| Connector Version | Flink Version | Dependent Iceberg Version                                                                                                                |
| ----------------- |---------------|  ----------------- |
| 0.5.0             | 1.12.x        | 1.1.0            |
| 0.5.0             | 1.14.x        | 1.1.0            |
| 0.5.0             | 1.15.x        | 1.1.0            |

The Amoro project can be self-compiled to obtain the runtime jar.

`mvn clean package -pl ':arctic-flink-runtime-1.14' -am -DskipTests`

The Flink Runtime Jar is located in the `flink/v1.14/flink-runtime/target` directory.

## Environment preparation
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

### Mixed-Hive format
Starting from Amoro version 0.3.1, Mixed-Hive format is supported, and data in Amoro  Mixed-Hive format tables can be read/written through Flink. When operating on Mixed-Hive format tables through Flink, the following points should be noted:

1. Flink Runtime Jar does not include the content of the Jar packages that Hive depends on. You need to manually put the [Hive-dependent Jar package](https://repo1.maven.org/maven2/org/apache/hive/hive-exec/2.1.1/hive-exec-2.1.1.jar) in the flink/lib directory;
2. When creating partitioned tables, the partition field needs to be placed in the last column; when there are multiple partition fields, they need to be placed at the end;

## Frequently Asked Questions

**1. Data written to Amoro table is not visible**

You need to enable Flink checkpoint and modify the [Flink checkpoint configuration](https://nightlies.apache.org/flink/flink-docs-release-1.12/deployment/config.html#execution-checkpointing-interval) in Flink conf. The data will only be committed during checkpoint.

**2. When using Flink SQL-Client to read Amoro tables with write.upsert feature enabled, there are still duplicate primary key data**

The query results obtained through Flink SQL-Client cannot provide MOR semantics based on primary keys. If you need to obtain merged results through Flink engine queries, you can write the content of Amoro tables to a MySQL table through JDBC connector for viewing.

**3. When writing to Amoro tables with write.upsert feature enabled through SQL-Client under Flink 1.15, there are still duplicate primary key data**

You need to execute the command `set table.exec.sink.upsert-materialize = none` in SQL-Client to turn off the upsert materialize operator generated upsert view. This operator will affect the AmoroWriter's generation of delete data when the write.upsert feature is enabled, causing duplicate primary key data to not be merged.