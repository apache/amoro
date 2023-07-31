<!--
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
  <img src="https://amoro.netease.com//img/amoro-logo-icon.png" alt="Amoro logo" height="120px"/>
</p>

<p align="center">
  <a href="https://www.apache.org/licenses/LICENSE-2.0.html">
    <img src="https://img.shields.io/badge/license-Apache%202-4EB1BA.svg" />
  </a>
  <a href="https://github.com/NetEase/arctic/actions/workflows/core-hadoop3-ci.yml">
    <img src="https://github.com/NetEase/arctic/actions/workflows/core-hadoop3-ci.yml/badge.svg" />
  </a>
  <a href="https://github.com/NetEase/arctic/actions/workflows/core-hadoop2-ci.yml">
    <img src="https://github.com/NetEase/arctic/actions/workflows/core-hadoop2-ci.yml/badge.svg" />
  </a>
  <a href="https://github.com/NetEase/arctic/actions/workflows/trino-ci.yml">
    <img src="https://github.com/NetEase/arctic/actions/workflows/trino-ci.yml/badge.svg" />
  </a>
</p>

Amoro(former name was Arctic) is a Lakehouse management system built on open data lake formats.
Working with compute engines including Flink, Spark, and Trino, Amoro brings pluggable and self-managed features for Lakehouse to provide out-of-the-box data warehouse experience,
and helps data platforms or products easily build infra-decoupled, stream-and-batch-fused and lake-native architecture.

## Architecture

<p align="center">
  <img src="https://amoro.netease.com//img/home-content.png" alt="Amoro architecture" height="360px"/>
</p>

* AMS: Amoro Management Service provides Lakehouse management features, like self-optimizing, data expiration, etc.
  It also provides a unified catalog service for all computing engines, which can also be combined with existing metadata services.
* Plugins: Amoro provides a wide selection of external plugins to meet different scenarios.
    * Optimizers: The self-optimizing execution engine plugin asynchronously performs merging, sorting, deduplication,
      layout optimization, and other operations on all type table format tables.
    * Terminal: SQL command-line tools, provide various implementations like local Spark and Kyuubi.
    * LogStore: Provide millisecond to second level SLAs for real-time data processing based on message queues like Kafka and Pulsar.

## Supported table formats 

Amoro can manage tables of different table formats, similar to how MySQL/ClickHouse can choose different storage engines.
Amoro meets diverse user needs by using different table formats. Currently, Amoro supports three table formats:

* Iceberg format: means using the native table format of the Apache Iceberg, which has all the features and characteristics of Iceberg.
* Mixed Iceberg format: built on top of Iceberg format, which can accelerate data processing using LogStore 
  and provides more efficient query performance and streaming read capability in CDC scenarios.
* Mixed Hive format: has the same features as the Mixed Iceberg tables but is compatible with a Hive table.
  Support upgrading Hive tables to Mixed Hive tables, and allow Hive's native read and write methods after upgrading.

## Supported engines

### Iceberg format

Iceberg format tables use the engine integration method provided by the Iceberg community.
For details, please refer to: [Iceberg Docs](https://iceberg.apache.org/docs/latest/).

### Mixed format

Amoro support multiple processing engines for Mixed format as below:

| Processing Engine | Version                   | Batch Read  | Batch Write | Batch Overwrite | Streaming Read | Streaming Write | Create Table | Alter Table |
|-------------------|---------------------------|-------------|-------------|-----------------|----------------|-----------------|--------------|-------------|
| Flink             | 1.12.x, 1.14.x and 1.15.x |  &#x2714;   |   &#x2714;   |       &#x2716;   |      &#x2714;   |       &#x2714;   |    &#x2714;   |   &#x2716;   |
| Spark             | 3.1, 3.2, 3.3             |  &#x2714;   |   &#x2714;   |       &#x2714;   |      &#x2716;   |       &#x2716;   |    &#x2714;   |   &#x2714;   |
| Hive              | 2.x, 3.x                  |  &#x2714;  |   &#x2716;  |       &#x2714;  |      &#x2716;  |       &#x2716;  |    &#x2716;  |   &#x2714;  |
| Trino             | 406                       |  &#x2714;  |   &#x2716;  |       &#x2714;  |      &#x2716;  |       &#x2716;  |    &#x2716;  |   &#x2714;  |

## Features

- Self-managed - Automatically compact small files and change files, regularly delete expired data to ensure the quality of table queries, and reduce system costs.
- Multiple Formats - Support different table formats to meet different scenario requirements and provide them with unified management capabilities.
- Catalog Service - Provide a unified metadata management service for all computing engines, which can also be combined with existing metadata services.
- Rich Plugins - Offers a wide selection of external plugins to meet different scenarios such as automatic data optimizing, data analysis.
- Management Tools - Visual management tools provide rich monitoring and management capabilities to help users get started easily.
- Infrastructure Independent - Can be easily deployed and used in private environments, cloud environments, hybrid cloud environments, and multi-cloud environments.

## Modules

Amoro contains modules as below:

- `amoro-core` contains core abstractions and common implementation for other modules
- `amoro-ams` is amoro management service module
    - `ams-api` contains ams thrift api and common interfaces
    - `ams-dashboard` is the dashboard frontend for ams
    - `ams-server` is the backend server for ams
    - `ams-optimizer` provides default optimizer implementation
- `amoro-hive` integrates with Apache Hive and implements Mixed Hive format
- `amoro-flink` provides Flink connectors for Mixed format tables (use amoro-flink-runtime for a shaded version)
- `amoro-spark` provides Spark connectors for Mixed format tables (use amoro-spark-runtime for a shaded version)
- `amoro-trino` provides Trino connectors for Mixed format tables


## Building

Amoro is built using Maven with Java 1.8 and Java 17(only for `trino` module).

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
* To package without trino module and JAVA 17 dependency: `mvn clean package -DskipTests -pl '!trino'`
* To build with hadoop 2.x(the default is 3.x) `mvn clean package -DskipTests -Dhadoop=v2`
* To indicate flink version for optimizer(the default is 1.14, 1.15 and 1.16 are available)
`mvn clean package -DskipTests -Doptimizer.flink=1.15`

## Quickstart

Visit [https://amoro.netease.com/quick-demo/](https://amoro.netease.com/quick-demo/) to quickly
explore what amoro can do.

## Join Community

If you are interested in Lakehouse, Data Lake Format, welcome to join our community, we welcome any organizations, teams
and individuals to grow together, and sincerely hope to help users better use Data Lake Format through open source.

Join the Amoro WeChat Group: Add " `kllnn999` " as a friend on WeChat and specify "Amoro lover".
