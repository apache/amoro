
## Reading with DataStream
Arctic 支持通过 Java API 以 Batch 或 Streaming 的方式读数据。
### Batch Mode
使用 Batch 模式读 File 中的全量和增量数据。
#### Bounded Source
非主键表支持以批模式读取全量数据、指定 snapshot-id/时间戳的快照数据、指定 snapshot 区间的增量数据。

主键表暂时只支持读当前全量及之后的 CDC 数据。
    
```java
StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
InternalCatalogBuilder catalogBuilder = 
    InternalCatalogBuilder
        .builder()
        .metastoreUrl("thrift://<url>:<port>/<catalog_name>");

TableIdentifier tableId = TableIdentifier.of("catalog_name", "database_name", "test_table");
ArcticTableLoader tableLoader = ArcticTableLoader.of(tableId, catalogBuilder);

Map<String, String> properties = new HashMap<>();
// 默认为 true。
properties.put("streaming", "false");

DataStream<RowData> batch = FlinkSource.forRowData()
.env(env)
.tableLoader(tableLoader)
// 主键表暂时只支持读当前全量及之后的 CDC 数据，可无需 properties 参数
.properties(properties)
.build();

// 打印读出的所有数据
batch.print();

// 提交并执行任务
env.execute("Test Arctic Batch Read");
```
    
properties 支持的参数，**当前只对非主键表生效**:

|Key|默认值|类型|是否必填|描述|
|--- |--- |--- |--- |--- |
|case-sensitive|false|Boolean|否|是否区分大小写|
|snapshot-id<img width=100/>|(none)|Long|否|读指定 snapshot 的全量数据，只有在 streaming 为 false 或不配置时生效|
|as-of-timestamp|(none)|String|否|读小于该时间戳的最近一次 snapshot 的全量数据，只有在 streaming 为 false 或不配置时生效|
|start-snapshot-id|(none)|String|否|在 streaming 为 false 时，需配合 end-snapshot-id，读两个区间的增量数据(snapshot1, snapshot2]。在 streaming 为 true 时，读该 snapshot 之后的增量数据，不指定则读当前快照之后（不包含当前）的增量数据|
|end-snapshot-id|(none)|String|否|需配合 start-snapshot-id，读两个区间的增量数据(snapshot1, snapshot2]|

### Streaming Mode
Arctic 支持以 Streaming 模式通过 JAVA API 读 File 或 Log 中的增量数据

####读 Logstore 实时数据

```java
StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
InternalCatalogBuilder catalogBuilder = 
    InternalCatalogBuilder
        .builder()
        .metastoreUrl("thrift://<url>:<port>/<catalog_name>");

TableIdentifier tableId = TableIdentifier.of("catalog_name", "database_name", "test_table");
ArcticTableLoader tableLoader = ArcticTableLoader.of(tableId, catalogBuilder);

ArcticTable table = ArcticUtils.load(tableLoader);
// 读表中所有字段。如果只读部分字段，可自行构造 schema，例：
// Schema userSchema = new Schema(new ArrayList<Types.NestedField>() {{
//   add(Types.NestedField.optional(0, "f_boolean", Types.BooleanType.get()));
//   add(Types.NestedField.optional(1, "f_int", Types.IntegerType.get()));
// }});
Schema schema = table.schema();

List<String> topics = new ArrayList<>();
topics.add("topic_name");

// -----------Hidden Kafka--------------
// 配置 kafka consumer 参数。详见 https://kafka.apache.org/documentation/#consumerconfigs
Properties properties = new Properties();
properties.put("group.id", groupId);
properties.put("properties.bootstrap.servers", "broker1:port1, broker2:port2 ...");

Configuration configuration = new Configuration();
// 开启保证数据一致性的低延迟读
configuration.set(ARCTIC_LOG_CONSISTENCY_GUARANTEE_ENABLE, true);

LogKafkaSource source = LogKafkaSource.builder(schema, configuration)
    .setTopics(topics)
    .setStartingOffsets(OffsetsInitializer.earliest())
    .setProperties(properties)
    .build();
// -----------Hidden Kafka--------------

// -----------Hidden Pulsar--------------
Map<String, String> tableProperties = new HashMap<>();
tableProperties.put(TableProperties.LOG_STORE_ADDRESS, logPulsarHelper.op().serviceUrl());
tableProperties.put(ArcticValidator.ARCTIC_LOG_CONSISTENCY_GUARANTEE_ENABLE.key(), 
    String.valueOf(logRetractionEnabled));

Properties properties = new Properties();
properties.put(PULSAR_ADMIN_URL.key(), logPulsarHelper.op().adminUrl());
properties.put(PULSAR_SUBSCRIPTION_NAME.key(), "log-source");
    
LogPulsarSource source = LogPulsarSource.builder(schema, tableProperties)
    .setProperties(properties)
    .setTopics(topic)
    .setStartCursor(StartCursor.earliest())
    .build();
// -----------Hidden Pulsar--------------

DataStream<RowData> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Log Source");
// 打印读出的所有数据
stream.print();

// 提交并执行任务
env.execute("Test Arctic Stream Read");
```

####读 Filestore 数据
    
```java
StreamExecutionEnvironment env = ...;
InternalCatalogBuilder catalogBuilder = ...;
TableIdentifier tableId = ...;
ArcticTableLoader tableLoader = ...;

Map<String, String> properties = new HashMap<>();
// 默认为 true
properties.put("streaming", "true");

DataStream<RowData> stream = FlinkSource.forRowData()
.env(env)
.tableLoader(tableLoader)
// 主键表暂时只支持读当前全量及之后的 CDC 数据，可无需 properties 参数
.properties(properties)
.build();

// 打印读出的所有数据
stream.print();

// 提交并执行任务
env.execute("Test Arctic Stream Read");
```
DataStream API 支持读取主键表和非主键表。properties 支持的配置项可以参考 Querying With SQL [章节 Hint Option](flink-dml.md#filestore)

## Writing With DataStream
Arctic 表支持通过 JAVA API 往 Log 或 File 写入数据
### Overwrite Data
Arctic 表目前仅支持非主键表的动态 Overwrite 表中已有的数据

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

// 提交并执行任务
env.execute("Test Arctic Overwrite");
```

### Appending Data

对于 Arctic 表，支持通过 JAVA API 指定往 File 或 Log 写入数据。

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
DataStream API 支持写主键表和非主键表。properties 支持的配置项可以参考 Writing With SQL [章节 Hint Options](flink-dml.md#insert-into)

> **TIPS**
> 
> arctic.emit.mode 包含 log 时，需要配置 log-store.enabled = true [开启 Log 配置](flink-dml.md#log)
>
> arctic.emit.mode 包含 file 时，主键表只会写入 ChangeStore，非主键表会直接写入 BaseStore。
