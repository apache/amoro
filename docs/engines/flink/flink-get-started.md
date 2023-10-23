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

## Paimon format

The Paimon Format can be accessed using the Connector provided by Paimon.
Refer to the documentation at [Paimon Flink user manual](https://paimon.apache.org/docs/master/engines/flink/)
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
| 0.6.0             | 1.14.x        | 1.3.0            |
| 0.6.0             | 1.15.x        | 1.3.0            |

The Amoro project can be self-compiled to obtain the runtime jar.

`mvn clean package -pl ':amoro-flink-runtime-1.14' -am -DskipTests`

The Flink Runtime Jar is located in the `flink/v1.14/flink-runtime/target` directory.

## Environment preparation
Download Flink and related dependencies, and download Flink 1.14 and 1.15 as needed. Taking Flink 1.15 as an example:
```shell
# Replace version value with the latest Amoro version if needed
AMORO_VERSION=0.6.0
FLINK_VERSION=1.15.3
FLINK_MAJOR_VERSION=1.15
FLINK_HADOOP_SHADE_VERSION=2.7.5
APACHE_FLINK_URL=archive.apache.org/dist/flink
MAVEN_URL=https://repo1.maven.org/maven2
FLINK_CONNECTOR_URL=${MAVEN_URL}/org/apache/flink
AMORO_CONNECTOR_URL=${MAVEN_URL}/com/netease/amoro

# Download FLink binary package
wget ${APACHE_FLINK_URL}/flink-${FLINK_VERSION}/flink-${FLINK_VERSION}-bin-scala_2.12.tgz
# Unzip Flink binary package
tar -zxvf flink-${FLINK_VERSION}-bin-scala_2.12.tgz

cd flink-${FLINK_VERSION}
# Download Flink Hadoop dependency
wget ${FLINK_CONNECTOR_URL}/flink-shaded-hadoop-2-uber/${FLINK_HADOOP_SHADE_VERSION}-10.0/flink-shaded-hadoop-2-uber-${FLINK_HADOOP_SHADE_VERSION}-10.0.jar
# Download Flink Amoro Connector
wget ${AMORO_CONNECTOR_URL}/amoro-flink-runtime-${FLINK_MAJOR_VERSION}/${AMORO_VERSION}/amoro-flink-runtime-${FLINK_MAJOR_VERSION}-${AMORO_VERSION}.jar

# Copy the necessary JAR files to the lib directory
mv flink-shaded-hadoop-2-uber-${FLINK_HADOOP_SHADE_VERSION}-10.0.jar lib
mv amoro-flink-runtime-${FLINK_MAJOR_VERSION}-${AMORO_VERSION}.jar lib
```

Modify Flink related configuration files:

```shell
cd flink-1.15.3
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

cp ../amoro-flink-runtime-${FLINK_MAJOR_VERSION}-${AMORO_VERSION}.jar lib
cp ../flink-shaded-hadoop-2-uber-${FLINK_HADOOP_SHADE_VERSION}-10.0.jar lib
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