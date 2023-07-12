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
# Flink DataStream
## Reading with DataStream
Arctic supports reading data in Batch or Streaming mode through Java API.

### Batch Mode
Using Batch mode to read the full and incremental data in the FileStore.

- Non-primary key tables support reading full data in batch mode, snapshot data with a specified snapshot-id or timestamp, and incremental data with a specified snapshot interval.
- The primary key table temporarily only supports reading the current full amount and later CDC data.

```java
StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
InternalCatalogBuilder catalogBuilder =
    InternalCatalogBuilder
        .builder()
        .metastoreUrl("thrift://<url>:<port>/<catalog_name>");

TableIdentifier tableId = TableIdentifier.of("catalog_name", "database_name", "test_table");
ArcticTableLoader tableLoader = ArcticTableLoader.of(tableId, catalogBuilder);

Map<String, String> properties = new HashMap<>();
//  Default is true.
properties.put("streaming", "false");

DataStream<RowData> batch = 
    FlinkSource.forRowData()
        .env(env)
        .tableLoader(tableLoader)
        // The primary key table only supports reading the current full amount and later CDC data temporarily, without the properties parameter .
        .properties(properties)
        .build();

// print All data read
batch.print();

// Submit and execute the task
env.execute("Test Arctic Batch Read");
``` 

The map properties contain below keys, **currently only valid for non-primary key tables**:

| Key|Default|Type|Required|Description| 
|--- |--- |--- |--- |--- |
|case-sensitive|false|Boolean|No|Case-sensitive| 
|snapshot-id |(none)|Long|No|Read the full amount of data of the specified snapshot, only effective when streaming is false or not configured| 
|as-of-timestamp|(none)|String|No|Read the last time less than the timestamp The full amount of snapshot data is valid only when streaming is false or not configured | 
|start-snapshot-id|(none)|String|No| When streaming is false, end-snapshot-id needs to be used to read the two intervals Incremental data (snapshot1, snapshot2]. When streaming is true, read the incremental data after the snapshot, if not specified, read the incremental data after the current snapshot (not including the current one) | 
|end-snapshot-id|(none )|String|No|Need to cooperate with start-snapshot-id to read incremental data in two intervals (snapshot1, snapshot2]| 

### Streaming Mode
Arctic supports reading incremental data in FileStore or LogStore through Java API in Streaming mode

### Streaming Mode (LogStore)
```java 
StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
InternalCatalogBuilder catalogBuilder = 
    InternalCatalogBuilder
        .builder()
        .metastoreUrl("thrift://<url>:<port>/<catalog_name>");

TableIdentifier tableId = TableIdentifier.of("catalog_name", "database_name", "test_table");
ArcticTableLoader tableLoader = ArcticTableLoader.of(tableId, catalogBuilder);

ArcticTable table = ArcticUtils.load(tableLoader);
// Read table All fields. If you only read some fields, you can construct the schema yourself, for example: 
// Schema userSchema = new Schema(new ArrayList<Types.NestedField>() {{
//   add(Types.NestedField.optional(0, "f_boolean", Types.BooleanType.get()));
//   add(Types.NestedField.optional(1, "f_int", Types.IntegerType.get()));
// }});
Schema schema = table.schema();

// -----------Hidden Kafka--------------
LogKafkaSource source = LogKafkaSource.builder(schema, table.properties()).build();

or

// -----------Hidden Pulsar--------------
LogPulsarSource source = LogPulsarSource.builder(schema, table.properties()).build();

DataStream<RowData> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Log Source");

// Print all the read data
stream.print();

// Submit and execute the task
env.execute("Test Arctic Stream Read");
```

### Streaming Mode (FileStore)
```java 
StreamExecutionEnvironment env = ...;
InternalCatalogBuilder catalogBuilder = ...;
TableIdentifier tableId = ...;
ArcticTableLoader tableLoader = ...;

Map<String, String> properties = new HashMap<>();
// default is true 
properties.put("streaming", "true");

DataStream<RowData> stream = 
    FlinkSource.forRowData()
        .env(env)
        .tableLoader(tableLoader)
        // The primary key table only supports reading the current full amount and later CDC data for the time being, without the properties parameter
        .properties(properties)
        .build();

// Print All read data 
stream.print();

// Submit and execute the task
env.execute("Test Arctic Stream Read");

StreamExecutionEnvironment env = ...; 
InternalCatalogBuilder catalogBuilder = ...; 
TableIdentifier tableId = ...; 
ArcticTableLoader tableLoader = ...; 
Map properties = new HashMap<>(); 
// default is true properties.put("streaming", "true"); 
DataStream stream = 
    FlinkSource.forRowData() 
        .env(env) 
        .tableLoader(tableLoader) 
        // The primary key table only supports reading the current full amount and later CDC data for the time being, without the properties parameter
        .properties(properties) 
        .build(); 

// print All read data 
stream.print(); 

// Submit and execute the task 
env.execute("Test Arctic Stream Read"); 
``` 
DataStream API supports reading primary key tables and non-primary key tables. The configuration items supported by properties can refer to Querying With SQL [chapter Hint Option](flink-dml.md#filestore)

## Writing With DataStream
Arctic table supports writing data to LogStore or FileStore through Java API

### Overwrite Data
Arctic table currently Only supports the existing data in the dynamic Overwrite table of the non-primary key table

```java
DataStream<RowData> input = ...;
InternalCatalogBuilder catalogBuilder = ...;
TableIdentifier tableId = ...;
ArcticTableLoader tableLoader = ...;

TableSchema FLINK_SCHEMA = TableSchema.builder()
    .field("id", DataTypes.INT())
    .field("name", DataTypes.STRING())
    .field("op_time", DataTypes.TIMESTAMP_WITH_LOCAL_TIME_ZONE())
    .build();

FlinkSink
    .forRowData(input)
    .tableLoader(tableLoader)
    .overwrite(true)
    .flinkSchema(FLINK_SCHEMA)
    .build();

// Submit and execute the task
env.execute("Test Arctic Overwrite");
DataStream input = ...; InternalCatalogBuilder catalogBuilder = ...; TableIdentifier tableId = ...; ArcticTableLoader tableLoader = ...; TableSchema FLINK_SCHEMA = TableSchema.builder() .field("id", DataTypes.INT()) .field ("name", DataTypes.STRING()) .field("op_time", DataTypes.TIMESTAMP_WITH_LOCAL_TIME_ZONE()) .build(); FlinkSink .forRowData(input) .tableLoader(tableLoader) .overwrite(true) .flinkSchema(FLINK_SCHEMA) .build(); // Submit and execute the task env.execute(“Test Arctic Overwrite”); 
```

### Appending Data
For the Arctic table, it supports specifying to write data to FileStore or LogStore through Java API.

```java
DataStream<RowData> input = ...;
InternalCatalogBuilder catalogBuilder = ...;
TableIdentifier tableId = ...;
ArcticTableLoader tableLoader = ...;

TableSchema FLINK_SCHEMA = TableSchema.builder()
    .field("id", DataTypes.INT())
    .field("name", DataTypes.STRING())
    .field("op_time", DataTypes.TIMESTAMP_WITH_LOCAL_TIME_ZONE())
    .build();

ArcticTable table = ArcticUtils.loadArcticTable(tableLoader);

table.properties().put("arctic.emit.mode", "log,file");

FlinkSink
    .forRowData(input)
    .table(table)
    .tableLoader(tableLoader)
    .flinkSchema(FLINK_SCHEMA)
    .build();

env.execute("Test Arctic Append");
```
The DataStream API supports writing to primary key tables and non-primary key tables. The configuration items supported by properties can refer to Writing With SQL [chapter Hint Options](flink-dml.md#insert-into)

> **TIPS**
>
> arctic.emit.mode contains log, you need to configure log-store.enabled = true [Enable Log Configuration](flink-dml.md#log)
>
> arctic.emit.mode When file is included, the primary key table will only be written to ChangeStore, and the non-primary key table will be directly written to BaseStore.

