---
title: "Configurations"
url: configurations
aliases:
    - "user-guides/configurations"
menu:
    main:
        parent: User Guides
        weight: 300
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
# Table Configurations

## Multi-level configuration management

Amoro provides configurations that can be configured at the `Catalog`, `Table`, and `Engine` levels. The configuration 
priority is given first to the `Engine`, followed by the `Table`, and finally by the `Catalog`. 

- Catalog: Generally, we recommend 
users to set default values for tables through the [Catalog properties configuration](../managing-catalogs/#configure-properties), such as Self-optimizing related configurations.
- Table: We also recommend users to 
specify customized configurations when [Create Table](../using-tables/#create-table), which can also be 
modified through [Alter Table](../using-tables/#modify-table) operations. 
- Engine: If tuning is required in the engines, then consider configuring it at the engine level, refer to 
[Spark](../spark-configuration/) and [Flink](../flink-dml/).

## Self-optimizing configurations

Self-optimizing configurations are applicable to both Iceberg Format and Mixed streaming Format.

| Key                                           | Default          | Description                                                                                                                              |
|-----------------------------------------------|------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| self-optimizing.enabled                       | true             | Enables Self-optimizing                                                                                                                  |
| self-optimizing.group                         | default          | Optimizer group for Self-optimizing                                                                                                      |
| self-optimizing.quota                         | 0.1              | Quota for Self-optimizing, indicating the CPU resource the table can take up                                                             |
| self-optimizing.execute.num-retries           | 5                | Number of retries after failure of Self-optimizing                                                                                       |
| self-optimizing.target-size                   | 134217728(128MB) | Target size for Self-optimizing                                                                                                          |
| self-optimizing.max-file-count                | 10000            | Maximum number of files processed by a Self-optimizing process                                                                           |
| self-optimizing.max-task-size-bytes           | 134217728(128MB) | Maximum file size bytes in a single task for splitting tasks                                                                             |
| self-optimizing.fragment-ratio                | 8                | The fragment file size threshold. We could divide self-optimizing.target-size by this ratio to get the actual fragment file size         |
| self-optimizing.min-target-size-ratio         | 0.75             | The undersized segment file size threshold. Segment files under this threshold will be considered for rewriting                          |
| self-optimizing.minor.trigger.file-count      | 12               | The minimum number of files to trigger minor optimizing is determined by the sum of fragment file count and equality delete file count   |
| self-optimizing.minor.trigger.interval        | 3600000(1 hour)  | The time interval in milliseconds to trigger minor optimizing                                                                            |
| self-optimizing.major.trigger.duplicate-ratio | 0.1              | The ratio of duplicate data of segment files to trigger major optimizing                                                                 |
| self-optimizing.full.trigger.interval         | -1(closed)       | The time interval in milliseconds to trigger full optimizing                                                                             |
| self-optimizing.full.rewrite-all-files        | true             | Whether full optimizing rewrites all files or skips files that do not need to be optimized                                               |
| self-optimizing.min-plan-interval             | 60000            | The minimum time interval between two self-optimizing planning action                                                                    |

## Data-cleaning configurations

Data-cleaning configurations are applicable to both Iceberg Format and Mixed streaming Format.

| Key                                         | Default          | Description                                                                                                                                                                                                                                                           |
|---------------------------------------------|------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| table-expire.enabled                        | true             | Enables periodically expire table                                                                                                                                                                                                                                     |
| change.data.ttl.minutes                     | 10080(7 days)    | Time to live in minutes for data of ChangeStore                                                                                                                                                                                                                       |
| snapshot.keep.duration                      | 720min(12 hours) | Table-Expiration keeps the latest snapshots within a specified duration                                                                                                                                                                                               |
| snapshot.keep.min-count                     | 1                | Minimum number of snapshots retained for table expiration                                                                                                                                                                                                             |
| clean-orphan-file.enabled                   | false            | Enables periodically clean orphan files                                                                                                                                                                                                                               |
| clean-orphan-file.min-existing-time-minutes | 2880(2 days)     | Cleaning orphan files keeps the files modified within a specified time in minutes                                                                                                                                                                                     |
| clean-dangling-delete-files.enabled         | true             | Whether to enable cleaning of dangling delete files                                                                                                                                                                                                                   |
| data-expire.enabled                         | false            | Whether to enable data expiration                                                                                                                                                                                                                                     |
| data-expire.level                           | partition        | Level of data expiration. Including partition and file                                                                                                                                                                                                                |
| data-expire.field                           | NULL             | Field used to determine data expiration, supporting timestamp/timestampz/long type and string type field in date format                                                                                                                                               |
| data-expire.datetime-string-pattern         | yyyy-MM-dd       | Pattern used for matching string datetime                                                                                                                                                                                                                             |
| data-expire.datetime-number-format          | TIMESTAMP_MS     | Timestamp unit for long field. Including TIMESTAMP_MS and TIMESTAMP_S                                                                                                                                                                                                 |
| data-expire.retention-time                  | NULL             | Retention period for data expiration. For example, 1d means retaining data for 1 day. Other supported units include h (hour), min (minute), s (second), ms (millisecond), etc.                                                                                        |
| data-expire.base-on-rule                    | LAST_COMMIT_TIME | A rule to indicate how to start expire data. Including LAST_COMMIT_TIME and CURRENT_TIME. LAST_COMMIT_TIME uses the timestamp of latest commit snapshot which is not optimized as the start of the expiration, which ensures that the table has `retention-time` data |

## Tags configurations

Tags configurations are applicable to Iceberg Format only now, and will be supported in Mixed Format
soon.

| Key                                       | Default                                                          | Description                                                                                                                          |
|-------------------------------------------|------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| tag.auto-create.enabled                   | false                                                            | Enables automatically creating tags                                                                                                  |
| tag.auto-create.trigger.period            | daily                                                            | Period of creating tags, support `daily`,`hourly` now                                                                                |
| tag.auto-create.trigger.offset.minutes    | 0                                                                | The minutes by which the tag is created after midnight (00:00)                                                                       |
| tag.auto-create.trigger.max-delay.minutes | 60                                                               | The maximum delay time for creating a tag                                                                                            |
| tag.auto-create.tag-format                | 'tag-'yyyyMMdd for daily and 'tag-'yyyyMMddHH for hourly periods | The format of the name for tag. Modifying this configuration will not take effect on old tags                                        |
| tag.auto-create.max-age-ms                | -1                                                               | Time of automatically created Tag to retain, -1 means keep it forever. Modifying this configuration will not take effect on old tags |

## Mixed Format configurations

If using Iceberg Format，please refer to [Iceberg configurations](https://iceberg.apache.org/docs/latest/configuration/)，the following configurations are only applicable to Mixed Format.

### Reading configurations

| Key                                                 | Default          | Description                                              |
| ---------------------------------- | ---------------- | ----------------------------------       |
| read.split.open-file-cost          | 4194304(4MB)    | The estimated cost to open a file                        |
| read.split.planning-lookback       | 10               | Number of bins to consider when combining input splits               |
| read.split.target-size              | 134217728(128MB)| Target size when combining data input splits                     |
| read.split.delete-ratio            | 0.05             | When the ratio of delete files is below this threshold, the read task will be split into more tasks to improve query speed |

### Writing configurations

| Key                           | Default          | Description                                                                                                     |
|-------------------------------|------------------|-----------------------------------------------------------------------------------------------------------------|
| base.write.format             | parquet          | File format for the table for BaseStore, applicable to KeyedTable                                               |
| change.write.format           | parquet          | File format for the table for ChangeStore, applicable to KeyedTable                                             |
| write.format.default          | parquet          | Default file format for the table, applicable to UnkeyedTable                                                   |
| base.file-index.hash-bucket   | 4                | Initial number of buckets for BaseStore auto-bucket                                                             |
| change.file-index.hash-bucket | 4                | Initial number of buckets for ChangeStore auto-bucket                                                           |
| write.target-file-size-bytes  | 134217728(128MB) | Target size when writing                                                                                        |
| write.upsert.enabled          | false            | Enable upsert mode, multiple insert data with the same primary key will be merged if enabled                    |
| write.distribution-mode       | hash             | Shuffle rules for writing. UnkeyedTable can choose between none and hash, while KeyedTable can only choose hash |
| write.distribution.hash-mode  | auto             | Auto-bucket mode, which supports primary-key, partition-key, primary-partition-key, and auto                    |
| base.refresh-interval         | -1 (Closed)      | The interval for refreshing the BaseStore                                                                       |

### LogStore configurations

| Key                                     | Default | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
|-----------------------------------------|---------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| log-store.enabled                       | false   | Enables LogStore                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| log-store.type                          | kafka   | Type of LogStore, which supports 'kafka' and 'pulsar'                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| log-store.address                       | NULL    | Address of LogStore, required when LogStore enabled. For Kafka, this is the Kafka bootstrap servers. For Pulsar, this is the Pulsar Service URL, such as 'pulsar://localhost:6650'                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| log-store.topic                         | NULL    | Topic of LogStore, required when LogStore enabled                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| properties.pulsar.admin.adminUrl        | NULL    | HTTP URL of Pulsar admin, such as 'http://my-broker.example.com:8080'. Only required when log-store.type=pulsar                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| properties.XXX                          | NULL    | Other configurations of LogStore. <br><br>For Kafka, all the configurations supported by Kafka Consumer/Producer can be set by prefixing them with `properties.`，<br>such as `'properties.batch.size'='16384'`，<br>refer to [Kafka Consumer Configurations](https://kafka.apache.org/documentation/#consumerconfigs), [Kafka Producer Configurations](https://kafka.apache.org/documentation/#producerconfigs) for more details.<br><br> For Pulsar，all the configurations supported by Pulsar can be set by prefixing them with `properties.`, <br>such as `'properties.pulsar.client.requestTimeoutMs'='60000'`，<br>refer to [Flink-Pulsar-Connector](https://nightlies.apache.org/flink/flink-docs-release-1.16/docs/connectors/datastream/pulsar) for more details |

### Watermark configurations

| Key                                           | Default          | Description                                              |
| ----------------------------------------------| ------------------- | ----------------------------------       |
| table.event-time-field                        | _ingest_time        | The event time field for calculating the watermark. The default `_ingest_time` indicates calculating with the time when the data was written |
| table.watermark-allowed-lateness-second       | 0                   | The allowed lateness time in seconds when calculating watermark          |
| table.event-time-field.datetime-string-format | `yyyy-MM-dd HH:mm:ss` | The format of event time when it is in string format           |
| table.event-time-field.datetime-number-format | TIMESTAMP_MS | The format of event time when it is in numeric format, which supports TIMESTAMP_MS (timestamp in milliseconds) and TIMESTAMP_S (timestamp in seconds)|

### Mixed-Hive format configurations

| Key                               | Default          | Description                                                                                            |
|-----------------------------------|------------------|--------------------------------------------------------------------------------------------------------|
| base.hive.auto-sync-schema-change | true             | Whether synchronize schema changes of Hive Table from HMS                                              |
| base.hive.auto-sync-data-write    | false            | Whether synchronize data changes of Hive Table from HMS, this should be true when writing to Hive      |
| base.hive.consistent-write.enabled | true            | To avoid writing dirty data, the files written to the Hive directory will be hidden files and renamed to visible files upon commit. |
