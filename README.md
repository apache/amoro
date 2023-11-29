<!--
r
 - Licensed to the Apache Software Foundation (ASF) under one
 - or more contributor license agreements.  See the NOTICE file
 - distributed with this work for additional information
 - regarding copyright ownership.  The ASF licenses this file
 - to you under the Apache License, Version 2.0 (the
 - "License"); you may not use this file except in compliance
 - with the License.  You may obtain a copy of the License at
 - 
 -     http://www.apache.org/licenses/LICENSE-2.0
 - 
 - Unless required by applicable law or agreed to in writing, software
 - distributed under the License is distributed on an "AS IS" BASIS,
 - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 - See the License for the specific language governing permissions and 
 - limitations under the License.
-->
<p align="center">
  <img src="https://amoro.netease.com/img/amoro-logo.svg" alt="Amoro logo" height="120px"/>
</p>

<p align="center">
  <a href="https://www.apache.org/licenses/LICENSE-2.0.html">
    <img src="https://img.shields.io/badge/license-Apache%202-4EB1BA.svg" />
  </a>
  <a href="https://github.com/NetEase/amoro/actions/workflows/core-hadoop3-ci.yml">
    <img src="https://github.com/NetEase/amoro/actions/workflows/core-hadoop3-ci.yml/badge.svg" />
  </a>
  <a href="https://github.com/NetEase/amoro/actions/workflows/core-hadoop2-ci.yml">
    <img src="https://github.com/NetEase/amoro/actions/workflows/core-hadoop2-ci.yml/badge.svg" />
  </a>
  <a href="https://github.com/NetEase/amoro/actions/workflows/trino-ci.yml">
    <img src="https://github.com/NetEase/amoro/actions/workflows/trino-ci.yml/badge.svg" />
  </a>
</p>

Amoro(former name was Arctic) is a Lakehouse management system built on open data lake formats.
Working with compute engines including Flink, Spark, and Trino, Amoro brings pluggable and self-managed features for Lakehouse to provide out-of-the-box data warehouse experience,
and helps data platforms or products easily build infra-decoupled, stream-and-batch-fused and lake-native architecture.

## Architecture

Here is the architecture diagram of Amoro:

<p align="center">
  <img src="https://amoro.netease.com//img/home-content.png" alt="Amoro architecture" height="360px"/>
</p>

* AMS: Amoro Management Service provides Lakehouse management features, like self-optimizing, data expiration, etc.
  It also provides a unified catalog service for all compute engines, which can also be combined with existing metadata services.
* Plugins: Amoro provides a wide selection of external plugins to meet different scenarios.
  * Optimizers: The self-optimizing execution engine plugin asynchronously performs merging, sorting, deduplication,
    layout optimization, and other operations on all type table format tables.
  * Terminal: SQL command-line tools, provide various implementations like local Spark and Kyuubi.
  * LogStore: Provide millisecond to second level SLAs for real-time data processing based on message queues like Kafka and Pulsar.

## Supported table formats

Amoro can manage tables of different table formats, similar to how MySQL/ClickHouse can choose different storage engines.
Amoro meets diverse user needs by using different table formats. Currently, Amoro supports four table formats:

* Iceberg format: Users can directly entrust their Iceberg tables to Amoro for maintenance, so that users can not only use all the functions of Iceberg tables, but also enjoy the performance and stability improvements brought by Amoro.
* Mixed-Iceberg format: Amoro provides a set of more optimized formats for streaming update scenarios on top of the Iceberg format. If users have high performance requirements for streaming updates or have demands for CDC incremental data reading functions, they can choose to use the Mixed-Iceberg format.
* Mixed-Hive format: Many users do not want to affect the business originally built on Hive while using data lakes. Therefore, Amoro provides the Mixed-Hive format, which can upgrade Hive tables to Mixed-Hive format only through metadata migration, and the original Hive tables can still be used normally. This ensures business stability and benefits from the advantages of data lake computing.
* Paimon format: Amoro supports displaying metadata information in the Paimon format, including Schema, Options, Files, Snapshots, DDLs, and Compaction information.

## Supported engines

### Iceberg format

Iceberg format tables use the engine integration method provided by the Iceberg community.
For details, please refer to: [Iceberg Docs](https://iceberg.apache.org/docs/latest/).

### Mixed format

Amoro support multiple processing engines for Mixed format as below:

| Processing Engine | Version                | Batch Read  | Batch Write | Batch Overwrite | Streaming Read | Streaming Write | Create Table | Alter Table |
|-------------------|------------------------|-------------|-------------|-----------------|----------------|-----------------|--------------|-------------|
| Flink             | 1.15.x, 1.16.x, 1.17.x |  &#x2714;   |   &#x2714;   |       &#x2716;   |      &#x2714;   |       &#x2714;   |    &#x2714;   |   &#x2716;   |
| Spark             | 3.1, 3.2, 3.3          |  &#x2714;   |   &#x2714;   |       &#x2714;   |      &#x2716;   |       &#x2716;   |    &#x2714;   |   &#x2714;   |
| Hive              | 2.x, 3.x               |  &#x2714;  |   &#x2716;  |       &#x2714;  |      &#x2716;  |       &#x2716;  |    &#x2716;  |   &#x2714;  |
| Trino             | 406                    |  &#x2714;  |   &#x2716;  |       &#x2714;  |      &#x2716;  |       &#x2716;  |    &#x2716;  |   &#x2714;  |

## Features

- Self-optimizing - Continuously optimizing tables, including compacting small files, change files, regularly delete expired files to keep high query performance and reducing storage costs.
- Multiple Formats - Support different table formats such as Iceberg, Mixed-Iceberg and Mixed-Hive to meet different scenario requirements and provide them with unified management capabilities.
- Catalog Service - Provide a unified catalog service for all compute engines, which can also used with existing metadata store service such as Hive Metastore and AWS Glue.
- Rich Plugins - Provide various plugins to integrate with other systems, like continuously optimizing with Flink and data analysis with Spark and Kyuubi.
- Management Tools - Provide a variety of management tools, including WEB UI and standard SQL command line, to help you get started faster and integrate with other systems more easily.
- Infrastructure Independent - Can be easily deployed and used in private environments, cloud environments, hybrid cloud environments, and multi-cloud environments.

## Modules

Amoro contains modules as below:

- `amoro-core` contains core abstractions and common implementation for other modules
- `amoro-ams` is amoro management service module
    - `ams-api` contains ams thrift api and common interfaces
    - `ams-dashboard` is the dashboard frontend for ams
    - `ams-server` is the backend server for ams
    - `ams-optimizer` provides default optimizer implementation
- `amoro-mixed` provides Mixed format implementation
    - `amoro-hive` integrates with Apache Hive and implements Mixed Hive format
    - `amoro-flink` provides Flink connectors for Mixed format tables (use amoro-flink-runtime for a shaded version)
    - `amoro-spark` provides Spark connectors for Mixed format tables (use amoro-spark-runtime for a shaded version)
    - `amoro-trino` provides Trino connectors for Mixed format tables


## Building

Amoro is built using Maven with Java 1.8 and Java 17(only for `mixed/trino` module).

* To build Trino module need config `toolchains.xml` in `${user.home}/.m2/` dir, the content is

```
<?xml version="1.0" encoding="UTF-8"?>
<toolchains>
    <toolchain>
        <type>jdk</type>
        <provides>
            <version>17</version>
            <vendor>sun</vendor>
        </provides>
        <configuration>
            <jdkHome>${YourJDK17Home}</jdkHome>
        </configuration>
    </toolchain>
</toolchains>
```

* To invoke a build and run tests: `mvn package -P toolchain`
* To skip tests: `mvn -DskipTests package -P toolchain`
* To package without trino module and JAVA 17 dependency: `mvn clean package -DskipTests -pl '!mixed/trino'`
* To build with hadoop 2.x(the default is 3.x) `mvn clean package -DskipTests -Dhadoop=v2`
* To indicate flink version for optimizer(the default is 1.14, 1.15 and 1.16 are available)
  `mvn clean package -DskipTests -Doptimizer.flink=1.15`

>Spotless is skipped by default in `trino` module. So if you want to perform checkstyle when building `trino` module, you must be in a Java 17 environment.

* To invoke a build include `mixed/trino` module in Java 17 environment: `mvn clean package -DskipTests -P trino-spotless`
* To only build `mixed/trino` and its dependent modules in Java 17 environment: `mvn clean package -DskipTests -P trino-spotless -pl 'trino' -am`
## Quickstart

Visit [https://amoro.netease.com/quick-demo/](https://amoro.netease.com/quick-demo/) to quickly
explore what amoro can do.

## Join Community

If you are interested in Lakehouse, Data Lake Format, welcome to join our community, we welcome any organizations, teams
and individuals to grow together, and sincerely hope to help users better use Data Lake Format through open source.

Join the Amoro WeChat Group: Add " `kllnn999` " as a friend on WeChat and specify "Amoro lover".
