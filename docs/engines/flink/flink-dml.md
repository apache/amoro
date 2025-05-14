---
title: "Flink DML"
url: flink-dml
aliases:
    - "flink/dml"
menu:
    main:
        parent: Flink
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
# Flink DML

## Querying with SQL
Amoro tables support reading data in stream or batch mode through Flink SQL. You can switch modes using the following methods:
```sql
-- Run Flink tasks in streaming mode in the current session
SET execution.runtime-mode = streaming;

-- Run Flink tasks in batch mode in the current session
SET execution.runtime-mode = batch;
```

The following Hint Options are supported:

| Key                | Default Value | Type    | Required                                                                                                                                       | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
|--------------------|---------------|---------|------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| source.parallelism | (none)        | Integer | No                                                                                                                                             | Defines a custom parallelism for the source. By default, if this option is not defined, the planner will derive the parallelism for each statement individually by also considering the global configuration.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |

### Batch mode
Use batch mode to read full and incremental data from FileStore.

> **TIPS**
>
> LogStore does not support bounded reading.

```sql
-- Run Flink tasks in batch mode in the current session
SET execution.runtime-mode = batch;

-- Enable dynamic table parameter configuration to make hint options configured in Flink SQL effective
SET table.dynamic-table-options.enabled=true;
```

### Batch mode (non-primary key table)

Non-primary key tables support reading full data in batch mode, specifying snapshot data with snapshot-id or timestamp, and specifying the incremental data of the snapshot interval.

```sql
-- Read full data
SELECT * FROM unkeyed /*+ OPTIONS('streaming'='false')*/;

-- Read specified snapshot data
SELECT * FROM unkeyed /*+ OPTIONS('snapshot-id'='4411985347497777546')*/;
```
The supported parameters for bounded reads of non-primary-key tables in BaseStore include:

| Key                         | Default Value | Type   | Required | Description                                                                                                                                                                                                                                                                                                                                                                                        |
|-----------------------------|---------------|--------|----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| snapshot-id<img width=100/> | (none)        | Long   | No       | Reading the full data of a specific snapshot only works when streaming is set to false.                                                                                                                                                                                                                                                                                                            |
| as-of-timestamp             | (none)        | Long   | No       | Reading the full data of the latest snapshot taken before the specified timestamp only works when streaming is set to false.                                                                                                                                                                                                                                                                       |
| start-snapshot-id           | (none)        | Long   | No       | When streaming is set to false, you need to specify the end-snapshot-id to read the incremental data within two intervals (snapshot1, snapshot2]. When streaming is set to true, you can read the incremental data after the specified snapshot. If not specified, it will read the incremental data after the current snapshot (excluding the current one).                                       |
| end-snapshot-id             | (none)        | Long   | No       | When streaming is set to false, you need to specify the start-snapshot-id to read the incremental data within two intervals (snapshot1, snapshot2].                                                                                                                                                                                                                                                |
| other table parameters      | (none)        | String | No       | All parameters of an Amoro table can be dynamically modified through SQL Hint, but only for the current task. For a list of specific parameters, please refer to [Table Configurations](../configurations/). For permissions-related configurations on the catalog, they can also be configured in Hint using parameters such as [properties.auth.XXX in catalog DDL](../flink-ddl/#flink-sql) |

### Batch mode (primary key table)
```sql
-- Merge on Read the current mixed-format table and return append-only data.
SELECT * FROM keyed /*+ OPTIONS('streaming'='false', 'scan.startup.mode'='earliest')*/;
```

### Streaming mode
Amoro supports reading incremental data from FileStore or LogStore in streaming mode.

### Streaming mode (LogStore)

```sql
-- Run Flink tasks in streaming mode in the current session
SET execution.runtime-mode = streaming;

-- Enable dynamic table parameter configuration to make hint options configured in Flink SQL effective
SET table.dynamic-table-options.enabled=true;

SELECT * FROM test_table /*+ OPTIONS('mixed-format.read.mode'='log') */;
```
The following Hint Options are supported:

| Key                                | Default Value | Type    | Required                                                                                                                                       | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
|------------------------------------|---------------|---------|------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| mixed-format.read.mode                   | file          | String  | No                                                                                                                                             | To specify the type of data to read from an Amoro table, either File or Log, use the mixed-format.read.mode parameter. If the value is set to log, the Log configuration must be enabled.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| scan.startup.mode<img width=90/>   | latest        | String  | No                                                                                                                                             | The valid values are 'earliest', 'latest', 'timestamp', 'group-offsets' and 'specific-offsets'. <br> 'earliest' reads the data from the earliest offset possible.<br> 'latest' reads the data from the latest offset. <br>'timestamp' reads from a specified time position, which requires configuring the 'scan.startup.timestamp-millis' parameter. <br>'group-offsets' reads the data from committed offsets in ZK / Kafka brokers of a specific consumer group. <br>'specific-offsets' read the data from user-supplied specific offsets for each partition, which requires configuring the 'scan.startup.specific-offsets' parameter.                                                                                                                                                                                                  |
| scan.startup.timestamp-millis      | (none)        | Long    | No                                                                                                                                             | Valid when 'scan.startup.mode' = 'timestamp', reads data from the specified Kafka time with a millisecond timestamp starting at 00:00:00.000 GMT on 1 Jan 1970                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| scan.startup.specific-offsets      | (none)        | String  | No                                                                                                                                             | specify offsets for each partition in case of 'specific-offsets' startup mode, e.g. 'partition:0,offset:42;partition:1,offset:300'.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| properties.group.id                | (none)        | String  | If the LogStore for an Amoro table is Kafka, it is mandatory to provide its details while querying the table. Otherwise, it can be left empty. | The group id used to read the Kafka Topic                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| properties.pulsar.admin.adminUrl   | (none)        | String  | Required if LogStore is pulsar, otherwise not required                                                                                         | Pulsar admin's HTTP URL， e.g. http://my-broker.example.com:8080                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| properties.*                       | (none)        | String  | No                                                                                                                                             | Parameters for Logstore: <br>For Logstore with Kafka ('log-store.type'='kafka' default value), all other parameters supported by the Kafka Consumer can be set by prefixing properties. to the parameter name, for example, 'properties.batch.size'='16384'. The complete parameter information can be found in the [Kafka official documentation](https://kafka.apache.org/documentation/#consumerconfigs); <br>For LogStore set to Pulsar ('log-store.type'='pulsar'), all relevant configurations supported by Pulsar can be set by prefixing properties. to the parameter name, for example: 'properties.pulsar.client.requestTimeoutMs'='60000'. For complete parameter information, refer to the [Flink-Pulsar-Connector documentation](https://nightlies.apache.org/flink/flink-docs-release-1.16/docs/connectors/datastream/pulsar) |
| log.consumer.changelog.modes       | all-kinds     | String  | No                                                                                                                                             | The type of RowKind that will be generated when reading log data, supports: all-kinds, append-only.<br>all-kinds: will read cdc data, including +I/-D/-U/+U;<br>append-only: will only generate Insert data, recommended to use this configuration when reading without primary key.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |

> **Notes**
>
> - When log-store.type = pulsar, the parallelism of the Flink task cannot be less than the number of partitions in the Pulsar topic, otherwise some partition data cannot be read.
> - When the number of topic partitions in log-store is less than the parallelism of the Flink task, some Flink subtasks will be idle. At this time, if the task has a watermark, the parameter table.exec.source.idle-timeout must be configured, otherwise the watermark will not advance. See [official documentation](https://nightlies.apache.org/flink/flink-docs-release-1.16/docs/dev/table/config/#table-exec-source-idle-timeout) for details.


### Streaming mode (FileStore non-primary key table)

```sql
-- Run Flink tasks in streaming mode in the current session
SET execution.runtime-mode = streaming;

-- Enable dynamic table parameter configuration to make hint options configured in Flink SQL effective
SET table.dynamic-table-options.enabled = true;

-- Read incremental data after the current snapshot.
SELECT * FROM unkeyed /*+ OPTIONS('monitor-interval'='1s')*/ ;
```
Hint Options

| Key                              | Default Value | Type     | Required | Description                                                                                                                                                                                                                                                                                                                                                                                                        |
|----------------------------------|---------------|----------|----------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| streaming                        | true          | Boolean  | No       | Reads bounded data or unbounded data in a streaming mode, false: reads bounded data, true: reads unbounded data                                                                                                                                                                                                                                                                                                    |
| mixed-format.read.mode                 | file          | String   | No       | To specify the type of data to read from an Amoro table, either File or Log, use the mixed-format.read.mode parameter. If the value is set to log, the Log configuration must be enabled.                                                                                                                                                                                                                          |
| monitor-interval<img width=120/> | 10s           | Duration | No       | The mixed-format.read.mode = file parameter needs to be set for this to take effect. The time interval for monitoring newly added data files                                                                                                                                                                                                                                                                       |
| start-snapshot-id                | (none)        | Long     | No       | To read incremental data starting from a specified snapshot (excluding the data in the start-snapshot-id snapshot), specify the snapshot ID using the start-snapshot-id parameter. If not specified, the reader will start reading from the snapshot after the current one (excluding the data in the current snapshot).                                                                                           |
| other table parameters           | (none)        | String   | No       | All parameters of an Amoro table can be dynamically modified through SQL Hints, but they only take effect for this specific task. For the specific parameter list, please refer to the [Table Configuration](../configurations/). For permissions-related configurations on the catalog, they can also be configured in Hint using parameters such as [properties.auth.XXX in catalog DDL](../flink-ddl/#flink-sql) |

### Streaming Mode (FileStore primary key table)


After using CDC (Change Data Capture) to ingest data into the lake, you can use the Flink engine to read both stock data and incremental data in the same task without restarting the task, and ensure consistent data reading. Amoro Source will save the file offset information in the Flink state.

In this way, the task can continue to read data from the last read offset position, ensuring data consistency and being able to process newly arrived incremental data.
```sql
-- Run Flink tasks in streaming mode in the current session
SET execution.runtime-mode = streaming;

-- Enable dynamic table parameter configuration to make hint options configured in Flink SQL effective
SET table.dynamic-table-options.enabled = true;

-- Incremental unified reading of BaseStore and ChangeStore
SELECT * FROM keyed /*+ OPTIONS('streaming'='true', 'scan.startup.mode'='earliest')*/;
```

Hint Options

| Key                    | Default Value | Type   | Required | Description                                                                                                                                                                                                                                                                                                                                                                                                         |
|------------------------|---------------|--------|----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| streaming              | true          | String | No       | Reads bounded data or unbounded data in a streaming mode, false: reads bounded data, true: reads unbounded data                                                                                                                                                                                                                                                                                                     |
| mixed-format.read.mode       | file          | String | No       | Specifies the data to read from an Amoro table, either file or log. If the value is "log", Log configuration must be enabled                                                                                                                                                                                                                                                                                        |
| monitor-interval       | 10s           | String | No       | This parameter only takes effect when mixed-format.read.mode = file. It sets the time interval for monitoring newly added data files                                                                                                                                                                                                                                                                                      |
| scan.startup.mode      | latest        | String | No       | The valid values are 'earliest', 'latest'. 'earliest' reads the full table data and will continue to read incremental data when streaming=true. 'latest' reads only the data after the current snapshot, not including the data in the current snapshot.                                                                                                                                                            |
| other table parameters | (none)        | String | No       | All parameters of an Amoro table can be dynamically modified through SQL Hints, but they only take effect for this specific task. For the specific parameter list, please refer to the [Table Configuration](../configurations/). For permissions-related configurations on the catalog, they can also be configured in Hint using parameters such as [properties.auth.XXX in catalog DDL](../flink-ddl/#flink-sql) |

## Writing With SQL
Amoro tables support writing data to LogStore or FileStore using Flink SQL.
### INSERT OVERWRITE
Currently, INSERT OVERWRITE is only supported for non-primary key tables. It replaces the data in the table, and the overwrite operation is atomic. Partitions are dynamically generated from the query statement, and the data in these partitions will be fully replaced.

INSERT OVERWRITE only allows running in Flink Batch mode.

```sql
INSERT OVERWRITE unkeyed VALUES (1, 'a', '2022-07-01');
```

```sql
-- It is also possible to overwrite data for a specific partition:

INSERT OVERWRITE `mixed_catalog`.`mixed_db`.`unkeyed` PARTITION(data='2022-07-01') SELECT 5, 'b';
```
For non-partitioned tables, INSERT OVERWRITE will overwrite the entire data in the table.
### INSERT INTO
For Amoro tables, it is possible to specify whether to write data to FileStore or LogStore.

For Amoro primary key tables, writing to FileStore will also write CDC data to the ChangeStore.
```sql
INSERT INTO `mixed_catalog`.`mixed_db`.`test_table` 
    /*+ OPTIONS('mixed-format.emit.mode'='log,file') */
SELECT id, name from `source`;
```

Hint Options

| Key                                              | Default Value | Type     | Required                                                                                                                         | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
|--------------------------------------------------|---------------|----------|----------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| mixed-format.emit.mode                                 | auto          | String   | No                                                                                                                               | Data writing modes currently supported are: file, log, and auto. For example: 'file' means data is only written to the Filestore. 'log' means data is only written to the Logstore. 'file,log' means data is written to both the Filestore and the Logstore. 'auto' means data is written only to the Filestore if the Logstore for the Amoro table is disabled. If the Logstore for the Amoro table is enabled, it means data is written to both the Filestore and the Logstore. It is recommended to use 'auto'.                                                                                                                                                                                                                                                                                                                |
| mixed-format.emit.auto-write-to-logstore.watermark-gap | (none)        | Duration | No                                                                                                                               | This feature is only enabled when 'mixed-format.emit.mode'='auto'. If the watermark of the Amoro writers is greater than the current system timestamp minus a specific value, the writers will also write data to the Logstore. The default setting is to enable the Logstore writer immediately after the job starts. The value for this feature must be greater than 0.                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| log.version                                      | v1            | String   | No                                                                                                                               | The log data format currently has only one version, so it can be left empty                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| sink.parallelism                                 | (none)        | String   | No                                                                                                                               | The parallelism for writing to the Filestore and Logstore is determined separately. The parallelism for submitting the file operator is always 1.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| write.distribution-mode                          | hash          | String   | No                                                                                                                               | The distribution modes for writing to the Amoro table include: none and hash.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| write.distribution.hash-mode                     | auto          | String   | No                                                                                                                               | The hash strategy for writing to an Amoro table only takes effect when write.distribution-mode=hash. The available options are: primary-key, partition-key, primary-partition-key, and auto. primary-key: Shuffle by primary key partition-key: Shuffle by partition key primary-partition-key: Shuffle by primary key and partition key auto: If the table has both a primary key and partitions, use primary-partition-key; if the table has a primary key but no partitions, use primary-key; if the table has partitions but no primary key, use partition-key. Otherwise, use none.                                                                                                                                                                                                                                           |
| properties.pulsar.admin.adminUrl                 | (none)        | String   | If the LogStore is Pulsar and it is required for querying, it must be filled in, otherwise it can be left empty.<img width=100/> | The HTTP URL for Pulsar Admin is in the format: http://my-broker.example.com:8080.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| properties.*                                     | (none)        | String   | No                                                                                                                               | Parameters for Logstore: For Logstore with Kafka ('log-store.type'='kafka' default value), all other parameters supported by the Kafka Consumer can be set by prefixing properties. to the parameter name, for example, 'properties.batch.size'='16384'. The complete parameter information can be found in the [Kafka official documentation](https://kafka.apache.org/documentation/#consumerconfigs); For LogStore set to Pulsar ('log-store.type'='pulsar'), all relevant configurations supported by Pulsar can be set by prefixing properties. to the parameter name, for example: 'properties.pulsar.client.requestTimeoutMs'='60000'. For complete parameter information, refer to the [Flink-Pulsar-Connector documentation](https://nightlies.apache.org/flink/flink-docs-release-1.16/docs/connectors/datastream/pulsar) |
| other table parameters                           | (none)        | String   | No                                                                                                                               | All parameters of an Amoro table can be dynamically modified through SQL Hints, but they only take effect for this specific task. For the specific parameter list, please refer to the [Table Configuration](../configurations/). For permissions-related configurations on the catalog, they can also be configured in Hint using parameters such as [properties.auth.XXX in catalog DDL](../flink-ddl/#flink-sql)                                                                                                                                                                                                                                                                                                                                                                                                             |

## Lookup join with SQL

A Lookup Join is used to enrich a table with data that is queried from Amoro Table. The join requires one table to have a processing time attribute and the other table to be backed by a lookup source connector.

The following example shows the syntax to specify a lookup join.

```sql
-- amoro flink connector and can be used for lookup joins
CREATE TEMPORARY TABLE Customers (
    id INT,
    name STRING,
    country STRING,
    zip STRING
) WITH (
    'connector' = 'mixed-format',
    'metastore.url' = '',
    'mixed-format.catalog' = '',
    'mixed-format.database' = '',
    'mixed-format.table' = '',
    'lookup.cache.max-rows' = ''
);
       
-- Create a temporary left table, like from kafka
CREATE TEMPORARY TABLE orders (
    order_id INT,
    total INT,
    customer_id INT,
    proc_time AS PROCTIME()
) WITH (
    'connector' = 'kafka',
    'topic' = '...',
    'properties.bootstrap.servers' = '...',
    'format' = 'json'
    ...
);

-- enrich each order with customer information
SELECT o.order_id, o.total, c.country, c.zip
FROM Orders AS o
JOIN Customers FOR SYSTEM_TIME AS OF o.proc_time AS c
ON o.customer_id = c.id;
```

Lookup Options

| Key                                                | Default Value | Type     | Required | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
|----------------------------------------------------|---------------|----------|----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| lookup.cache.max-rows                              | 10000         | Long     | No       | The maximum number of rows in the lookup cache, beyond which the oldest row will expire.                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| lookup.reloading.interval                          | 10s           | Duration | No       | Configuration option for specifying the interval in seconds to reload lookup data in RocksDB.                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| lookup.cache.ttl-after-write                       | 0s            | Duration | No       | The TTL after which the row will expire in the lookup cache.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| rocksdb.auto-compactions                           | false         | Boolean  | No       | Enable automatic compactions during the initialization process. After the initialization completed, will enable the auto_compaction.                                                                                                                                                                                                                                                                                                                                                                                                                |
| rocksdb.writing-threads                            | 5             | Int      | No       | Writing data into rocksDB thread number.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| rocksdb.block-cache.capacity                       | 1048576       | Long     | No       | Use the LRUCache strategy for blocks, the size of the BlockCache can be configured based on your memory requirements and available system resources.                                                                                                                                                                                                                                                                                                                                                                                                |
| rocksdb.block-cache.numShardBits                   | -1            | Int      | No       | Use the LRUCache strategy for blocks. The cache is sharded to 2^numShardBits shards, by hash of the key. Default is -1, means it is automatically determined: every shard will be at least 512KB and number of shard bits will not exceed 6.                                                                                                                                                                                                                                                                                                        |
| other table parameters                             | (none)        | String   | No       | All parameters of an Amoro table can be dynamically modified through SQL Hints, but they only take effect for this specific task. For the specific parameter list, please refer to the [Table Configuration](../configurations/). For permissions-related configurations on the catalog, they can also be configured in Hint using parameters such as [properties.auth.XXX in catalog DDL](../flink-ddl/#flink-sql)                                                                                                                                 |
