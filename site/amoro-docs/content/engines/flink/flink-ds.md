---
title: "Flink DataStream"
url: flink-datastream
aliases:
    - "flink/datastream"
menu:
    main:
        parent: Flink
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
# Flink DataStream

## Add maven dependency
To add a dependency on Mixed-format flink connector in Maven, add the following to your pom.xml:
```xml
<dependencies>
  ...
  <dependency>
    <groupId>org.apache.amoro</groupId>
    <!-- For example: amoro-format-mixed-flink-runtime-1.15 -->
    <artifactId>amoro-format-mixed-flink-runtime-${flink.minor-version}</artifactId>
    <!-- For example: 0.7.0-incubating -->
    <version>${amoro-format-mixed-flink.version}</version>
  </dependency>
  ...
</dependencies>
```

## Reading with DataStream
Amoro supports reading data in Batch or Streaming mode through Java API.

### Batch mode
Using Batch mode to read the full and incremental data in the FileStore.

- Non-primary key tables support reading full data in batch mode, snapshot data with a specified snapshot-id or timestamp, and incremental data with a specified snapshot interval.
- The primary key table temporarily only supports reading the current full amount and later CDC data.

```java
import org.apache.amoro.flink.InternalCatalogBuilder;
import org.apache.amoro.flink.table.FlinkSource;
import org.apache.amoro.flink.table.MixedFormatTableLoader;
import org.apache.amoro.table.TableIdentifier;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.data.RowData;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
        InternalCatalogBuilder catalogBuilder =
                InternalCatalogBuilder
                        .builder()
                        .metastoreUrl("thrift://<url>:<port>/<catalog_name>");

        TableIdentifier tableId = TableIdentifier.of("catalog_name", "database_name", "test_table");
        MixedFormatTableLoader tableLoader = MixedFormatTableLoader.of(tableId, catalogBuilder);

        Map<String, String> properties = new HashMap<>();
        // Default is true
        properties.put("streaming", "false");

        DataStream<RowData> batch =
                FlinkSource.forRowData()
                        .env(env)
                        .tableLoader(tableLoader)
                        .properties(properties)
                        .build();

        // print all data read
        batch.print();

        // Submit and execute the task
        env.execute("Test Mixed-format table batch read");
    }
}
``` 

The map properties contain below keys, **currently only valid for non-primary key tables**:

| Key|Default|Type|Required|Description| 
|--- |--- |--- |--- |--- |
|case-sensitive|false|Boolean|No|Case-sensitive| 
|snapshot-id |(none)|Long|No|Read the full amount of data of the specified snapshot, only effective when streaming is false or not configured| 
|as-of-timestamp|(none)|String|No|Read the last time less than the timestamp The full amount of snapshot data is valid only when streaming is false or not configured | 
|start-snapshot-id|(none)|String|No| When streaming is false, end-snapshot-id needs to be used to read the two intervals Incremental data (snapshot1, snapshot2]. When streaming is true, read the incremental data after the snapshot, if not specified, read the incremental data after the current snapshot (not including the current one) | 
|end-snapshot-id|(none )|String|No|Need to cooperate with start-snapshot-id to read incremental data in two intervals (snapshot1, snapshot2]| 

### Streaming mode
Amoro supports reading incremental data in FileStore or LogStore through Java API in Streaming mode

### Streaming mode (LogStore)
```java 
import org.apache.amoro.flink.InternalCatalogBuilder;
import org.apache.amoro.flink.read.source.log.kafka.LogKafkaSource;
import org.apache.amoro.flink.table.MixedFormatTableLoader;
import org.apache.amoro.flink.util.MixedFormatUtils;
import org.apache.amoro.shade.org.apache.iceberg.Schema;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableIdentifier;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.data.RowData;


public class Main {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
        InternalCatalogBuilder catalogBuilder =
                InternalCatalogBuilder
                        .builder()
                        .metastoreUrl("thrift://<url>:<port>/<catalog_name>");

        TableIdentifier tableId = TableIdentifier.of("catalog_name", "database_name", "test_table");
        MixedFormatTableLoader tableLoader = MixedFormatTableLoader.of(tableId, catalogBuilder);

        MixedTable table = MixedFormatUtils.loadMixedTable(tableLoader);
        // Read table All fields. If you only read some fields, you can construct the schema yourself, for example:
        // Schema userSchema = new Schema(new ArrayList<Types.NestedField>() {{
        //   add(Types.NestedField.optional(0, "f_boolean", Types.BooleanType.get()));
        //   add(Types.NestedField.optional(1, "f_int", Types.IntegerType.get()));
        // }});
        Schema schema = table.schema();

        // -----------Hidden Kafka--------------
        LogKafkaSource source = LogKafkaSource.builder(schema, table.properties()).build();

        DataStream<RowData> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Log Source");

        // Print all the read data
        stream.print();

        // Submit and execute the task
        env.execute("Test Mixed-format table streaming read");
    }
}
```

### Streaming mode (FileStore)
```java 
import org.apache.amoro.flink.InternalCatalogBuilder;
import org.apache.amoro.flink.table.FlinkSource;
import org.apache.amoro.flink.table.MixedFormatTableLoader;
import org.apache.amoro.table.TableIdentifier;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.data.RowData;

import java.util.HashMap;
import java.util.Map;


public class Main {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
        InternalCatalogBuilder catalogBuilder =
                InternalCatalogBuilder
                        .builder()
                        .metastoreUrl("thrift://<url>:<port>/<catalog_name>");

        TableIdentifier tableId = TableIdentifier.of("catalog_name", "database_name", "test_table");
        MixedFormatTableLoader tableLoader = MixedFormatTableLoader.of(tableId, catalogBuilder);

        Map<String, String> properties = new HashMap<>();
        // Default value is true
        properties.put("streaming", "true");

        DataStream<RowData> stream =
                FlinkSource.forRowData()
                        .env(env)
                        .tableLoader(tableLoader)
                        .properties(properties)
                        .build();

        // Print all read data
        stream.print();

        // Submit and execute the task
        env.execute("Test Mixed-format table streaming Read");
    }
}
``` 
DataStream API supports reading primary key tables and non-primary key tables. The configuration items supported by properties can refer to Querying With SQL [chapter Hint Option](../flink-dml/)

## Writing with DataStream
Amoro table supports writing data to LogStore or FileStore through Java API

### Overwrite data
Amoro table currently Only supports the existing data in the dynamic Overwrite table of the non-primary key table

```java
import org.apache.amoro.flink.InternalCatalogBuilder;
import org.apache.amoro.flink.table.MixedFormatTableLoader;
import org.apache.amoro.flink.write.FlinkSink;
import org.apache.amoro.table.TableIdentifier;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.data.RowData;



public class Main {
    public static void main(String[] args) throws Exception {
        // Build your data stream
        DataStream<RowData> input = null;
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
        InternalCatalogBuilder catalogBuilder =
                InternalCatalogBuilder
                        .builder()
                        .metastoreUrl("thrift://<url>:<port>/<catalog_name>");

        TableIdentifier tableId = TableIdentifier.of("catalog_name", "database_name", "test_table");
        MixedFormatTableLoader tableLoader = MixedFormatTableLoader.of(tableId, catalogBuilder);

        TableSchema flinkSchema = TableSchema.builder()
                .field("id", DataTypes.INT())
                .field("name", DataTypes.STRING())
                .field("op_time", DataTypes.TIMESTAMP_WITH_LOCAL_TIME_ZONE())
                .build();

        FlinkSink
                .forRowData(input)
                .tableLoader(tableLoader)
                .overwrite(true)
                .flinkSchema(flinkSchema)
                .build();

        // Submit and execute the task
        env.execute("Test Mixed-format table overwrite");
    }
} 
```

### Appending data
For the Amoro table, it supports specifying to write data to FileStore or LogStore through Java API.

```java
import org.apache.amoro.flink.InternalCatalogBuilder;
import org.apache.amoro.flink.table.MixedFormatTableLoader;
import org.apache.amoro.flink.util.MixedFormatUtils;
import org.apache.amoro.flink.write.FlinkSink;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableIdentifier;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.data.RowData;



public class Main {
    public static void main(String[] args) throws Exception {
        // Build your data stream
        DataStream<RowData> input = null;
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
        InternalCatalogBuilder catalogBuilder =
                InternalCatalogBuilder
                        .builder()
                        .metastoreUrl("thrift://<url>:<port>/<catalog_name>");

        TableIdentifier tableId = TableIdentifier.of("catalog_name", "database_name", "test_table");
        MixedFormatTableLoader tableLoader = MixedFormatTableLoader.of(tableId, catalogBuilder);

        TableSchema flinkSchema = TableSchema.builder()
                .field("id", DataTypes.INT())
                .field("name", DataTypes.STRING())
                .field("op_time", DataTypes.TIMESTAMP_WITH_LOCAL_TIME_ZONE())
                .build();

        MixedTable table = MixedFormatUtils.loadMixedTable(tableLoader);

        table.properties().put("mixed-format.emit.mode", "log,file");

        FlinkSink
                .forRowData(input)
                .table(table)
                .tableLoader(tableLoader)
                .flinkSchema(flinkSchema)
                .build();

        env.execute("Test Mixed-format table append");
    }
}
```
The DataStream API supports writing to primary key tables and non-primary key tables. The configuration items supported by properties can refer to Writing With SQL [chapter Hint Options](../flink-dml/)

> **TIPS**
>
> mixed-format.emit.mode contains log, you need to configure log-store.enabled = true [Enable Log Configuration](../flink-dml/)
>
> mixed-format.emit.mode When file is included, the primary key table will only be written to ChangeStore, and the non-primary key table will be directly written to BaseStore.