---
title: "CDC Ingestion"
url: cdc-ingestion
aliases:
    - "user-guides/cdc-ingestion"
menu:
    main:
        parent: User Guides
        weight: 400
---
<!--
 - Licensed to the Apache Software Foundation (ASF) under one or more
 - contributor license agreements.  See the NOTICE file distributed with
 - this work for additional information regarding copyright ownership.
 - The ASF licenses this file to You under the Apache License, Version 2.0
 - (the "License"); you may not use this file except in compliance with
 - the License.  You may obtain a copy of the License at
 -
 -   http://www.apache.org/licenses/LICENSE-2.0
 -
 - Unless required by applicable law or agreed to in writing, software
 - distributed under the License is distributed on an "AS IS" BASIS,
 - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 - See the License for the specific language governing permissions and
 - limitations under the License.
 -->
# CDC Ingestion
CDC stands for Change Data Capture, which is a broad concept, as long as it can capture the change data, it can be called CDC.
[Flink CDC](https://github.com/apache/flink-cdc) is a Log message-based data capture tool, all the inventory 
and incremental data can be captured. Taking MySQL as an example, it can easily capture Binlog data through 
[Debezium](https://debezium.io/)、[Flink CDC](https://github.com/apache/flink-cdc) and process the calculations in real time to send them to the data lake. The data lake can then be 
queried by other engines.

This section will show how to ingest one table or multiple tables into the data lake for both [Iceberg](../iceberg-format/) format and [Mixed-Iceberg](../mixed-iceberg-format/) format.
## Apache Flink CDC

[**Apache Flink CDC**](https://nightlies.apache.org/flink/flink-cdc-docs-stable/) is a distributed data integration 
tool for real time data and batch data. Flink CDC brings the 
simplicity and elegance of data integration via YAML to describe the data movement and transformation.

Amoro provides the relevant code case reference how to complete cdc data to different lakehouse table format, see 
[**flink-cdc-ingestion**](../flink-cdc-ingestion) doc

At the same time, we provide [**Mixed-Iceberg**](../iceberg-format)  format, which you can understand as 
**STREAMING** For iceberg, which will enhance your real-time processing scene for you

## Debezium

Debezium is an open source distributed platform for change data capture. Start it up, point it at your databases, and your apps can start responding to all of the inserts, updates, and deletes that other apps commit to your databases. Debezium is durable and fast, so your apps can respond quickly and never miss an event, even when things go wrong.

### Demo

Coming Soon

## Airbyte

Airbyte is Data integration platform for ELT pipelines from APIs, databases & files to databases, warehouses & lakes

### Demo
Coming Soon
