## LogStore 实时数据
Arctic 表提供了 FileStore 和 LogStore 的存储，FileStore 存储海量的全量数据，LogStore 存储实时的增量数据。
实时数据可以提供秒级的数据可见性，可在不开启 LogStore 事务的情况下，保证数据的一致性。

其底层存储可以对接外部消息队列中间件，当前仅支持 Kafka、Pulsar。

用户可以通过在创建 Arctic 表时配置下述参数开启 LogStore，具体配置可以参考 [LogStore 相关配置](../configurations.md#logstore)

使用 Apache Kafka 作为 LogStore 存储系统[案例分享](using-logstore.md)

详细的使用参考 [sql 读](#logstore)、[java 读](flink-ds.md#logstore)、[sql 写](#insert-into)、[java 写](flink-ds.md#appending-data)
## Changelog 数据

对于 Arctic 主键表，FileStore 分为 BaseStore 和 ChangeStore 两部分数据，其中的 ChangeStore 存放实时写入的 CDC 数据，这部分数据会定期并入 Arctic BaseStore 中，详见 [Optimize](../concepts/table-formats.md)。
ChangeStore 数据依赖 Flink checkpoint 的周期时间进行提交，数据的可见性会有分钟级别延迟。

后续可以通过 Flink 引擎读取 ChangeStore 进行数据回放用作计算分析，支持 +I，-D，-U 和 +U 四种 changelog 数据类型。

对于 Arctic 非主键表，FileStore 只有 BaseStore 数据。

详细的使用参考 [sql 读](#sql-change-store-read)、[java 读](flink-ds.md#filestore)、[sql 写](#insert-into)、[java 写](flink-ds.md#appending-data)
## Querying With SQL
Arctic 表支持通过 Flink SQL 以流或批的模式读取数据。可以用下述方式来切换模式：
```sql
-- 在当前 session 中以流的模式运行 Flink 任务
SET execution.runtime-mode = streaming;

-- 在当前 session 中以批的模式运行 Flink 任务
SET execution.runtime-mode = batch;
```
### Batch Mode
使用 Batch 模式读 FileStore 中的全量和增量数据
#### Bounded Source
非主键表支持以 Batch 模式读取全量数据，指定 snapshot-id 或 timestamp 的快照数据，指定 snapshot 区间的增量数据。

> **TIPS**
> 
> LogStore 不支持有界读取.
    
```sql
-- 在当前 session 中以批的模式运行 Flink 任务
SET execution.runtime-mode = batch;

-- 打开动态表参数配置开关，让 Flink SQL 中配置的 hint options 生效
SET table.dynamic-table-options.enabled=true;
```

#### Bounded Source(非主键表)
```sql
-- 全量数据读取
SELECT * FROM unkeyed /*+ OPTIONS('streaming'='false')*/;

-- 指定快照数据读取
SELECT * FROM unkeyed /*+ OPTIONS('snapshot-id'='4411985347497777546')*/;
```
非主键表有界读取 BaseStore 支持的参数包括：

|Key|默认值|类型|是否必填|描述|
|--- |--- |--- |--- |--- |
|snapshot-id<img width=100/>|(none)|Long|否|读指定 snapshot 的全量数据，只有在 streaming 为 false 时生效|
|as-of-timestamp|(none)|Long|否|读小于该时间戳的最近一次 snapshot 的全量数据，只有在 streaming 为 false 时生效|
|start-snapshot-id|(none)|Long|否|在 streaming 为 false 时，需配合 end-snapshot-id，读两个区间的增量数据(snapshot1, snapshot2]。<br>在 streaming 为 true 时，读该 snapshot 之后的增量数据，不指定则读当前快照之后（不包含当前）的增量数据|
|end-snapshot-id|(none)|Long|否|在 streaming 为 false 时 需配合 start-snapshot-id，读两个区间的增量数据(snapshot1, snapshot2]|
|其他表参数|(none)|String|否|Arctic 表的所有参数都可以通过 SQL Hint 动态修改，当然只针对此任务生效，具体的参数列表可以参考 [表配置](../configurations.md)。对于Catalog上的权限相关配置，也可以配置在Hint中，参数见 [catalog ddl 中的 properties.auth.XXX](./flink-ddl.md#Flink SQL)|

#### Bounded Source(主键表)
```sql
-- 读取当前全量及部分可能未合并的 ChangeStore 数据
-- TODO 未来会采用 MOR 方式读取有界全量数据
SELECT * FROM keyed /*+ OPTIONS('streaming'='false', 'scan.startup.mode'='earliest')*/;
```

### Streaming Mode
Arctic 支持以 Streaming 模式读 FileStore 或 LogStore 中的增量数据。

#### LogStore 数据
    
```sql
-- 在当前 session 中以流的模式运行 Flink 任务
SET execution.runtime-mode = streaming;

-- 打开动态表参数配置开关，让 Flink SQL 中配置的 hint options 生效
SET table.dynamic-table-options.enabled=true;

SELECT * FROM test_table /*+ OPTIONS('arctic.read.mode'='log') */;
```
支持以下 Hint Options ：

|Key|默认值|类型|是否必填|描述|
|--- |--- |--- |--- |---|
|arctic.read.mode| file |String|否| 指定读 Arctic 表 File 或 Log 的数据。当值为 log 时，必须 开启 Log 配置|
|scan.startup.mode<img width=90/>| latest |String|否|有效值为earliest、latest、timestamp（读 fileStore 暂未支持）。<br>当arctic.read.mode = file 时仅支持earliest、latest。'earliest'表示读取全量表数据，在streaming=true 时会继续增量读取；'latest'：表示读取当前snapshot之后的数据，不包括当前snapshot数据。<br>当 arctic.read.mode = log 时，表示 logStore 消费者初次启动时获取 offset 的模式，'earliest'表示从Kafka中最早的位置读取，'latest'表示从最新的位置读取，'timestamp'表示从Kafka中指定时间位置读取，需配置参数 'scan.startup.timestamp-millis'。|
|scan.startup.timestamp-millis|(none)|Long|否|当'scan.startup.mode'='timestamp'时有效，从指定的Kafka时间读取数据，值为从1970 1月1日 00:00:00.000 GMT 开始的毫秒时间戳|
|properties.group.id| (none) |String|LogStore 是 kafka 并且是查询时必填，否则可不填| 读取 Kafka Topic 时使用的 group id|
|properties.pulsar.admin.adminUrl| (none) |String|LogStore 是 pulsar 时必填，否则可不填| Pulsar admin 的 HTTP URL，如：http://my-broker.example.com:8080|
|properties.*| (none) |String|否| Logstore的参数。<br><br>对于 LogStore 为 Kafka ('log-store.type'='kafka' 默认值)时，Kafka Consumer 支持的其他所有参数都可以通过在前面拼接 `properties.` 的前缀来设置，<br>如：`'properties.batch.size'='16384'`，<br>完整的参数信息可以参考 [Kafka官方手册](https://kafka.apache.org/documentation/#consumerconfigs); <br><br>对于 LogStore 为 Pulsar ('log-store.type'='pulsar')时，Pulsar 支持的相关配置都可以通过在前面拼接 `properties.` 的前缀来设置，<br>如：`'properties.pulsar.client.requestTimeoutMs'='60000'`，<br>完整的参数信息可以参考 [Flink-Pulsar-Connector文档](https://nightlies.apache.org/flink/flink-docs-release-1.16/docs/connectors/datastream/pulsar)|
|log-store.kafka.compatible.enabled| false | Boolean | 否 | 兼容 LogStore Kafka 废弃的 Source API。当读取 logstore kafka 的 Flink 任务需要带状态升级到 0.4.1 及以上 Arctic 版本时，必须将该参数设为 true，否则会遇到状态不兼容的异常。<br>该参数将于 0.7.0 版本删除，届时废弃的 Source API 也会被删除。|

> **注意事项**
>
> - 当 log-store.type = pulsar 时，Flink 任务的并行度不能小于 pulsar topic 的分区数，否则部分分区的数据无法读取
> - 当 log-store 的 Topic 分区数小于 Flink 任务的并行度时，部分 Flink subtask 将闲置。此时，如果任务有 watermark，必须配置参数 table.exec.source.idle-timeout，否则 watermark 将无法推进，详见 [官方文档](https://nightlies.apache.org/flink/flink-docs-release-1.16/docs/dev/table/config/#table-exec-source-idle-timeout)



#### FileStore 数据(非主键表)
        
```sql
-- 在当前 session 中以流的模式运行 Flink 任务
SET execution.runtime-mode = streaming;

-- 打开动态表参数配置开关，让 Flink SQL 中配置的 hint options 生效
SET table.dynamic-table-options.enabled = true;

-- 读当前快照之后的增量数据
SELECT * FROM unkeyed /*+ OPTIONS('monitor-interval'='1s')*/ ;
```
Hint Options

|Key|默认值|类型|是否必填|描述|
|--- |--- |--- |--- |--- |
|streaming|true|Boolean|否|以流的方式读取有界数据还是无解数据，false：读取有界数据，true：读取无界数据|
|arctic.read.mode|file|String|否|指定读 Arctic 表 File 或 Log 的数据。当值为 log 时，必须 开启 Log 配置|
|monitor-interval<img width=120/>|10s|Duration|否|arctic.read.mode = file 时才生效。监控新提交数据文件的时间间隔|
|start-snapshot-id|(none)|Long|否|从指定的 snapshot 开始读取增量数据（不包括 start-snapshot-id 的快照数据），不指定则读当前快照之后（不包含当前）的增量数据|
|其他表参数|(none)|String|否|Arctic 表的所有参数都可以通过 SQL Hint 动态修改，当然只针对此任务生效，具体的参数列表可以参考 [表配置](../configurations.md)。对于Catalog上的权限相关配置，也可以配置在Hint中，参数见 [catalog ddl 中的 properties.auth.XXX](./flink-ddl.md#Flink SQL)|

#### FileStore 数据(主键表)

使用 CDC（Change Data Capture）将数据入湖后，您可以通过 Flink 引擎在一个任务中同时读取存量数据和增量数据，
无需重启任务，并且能够保证数据一致性读取。Arctic Source 会将文件偏移信息保存在 Flink 状态中。

这样，任务可以从上次读取的偏移位置继续读取数据，确保数据一致性，并且能够处理新到达的增量数据。
```sql
-- 在当前 session 中以流的模式运行 Flink 任务
SET execution.runtime-mode = streaming;

-- 打开动态表参数配置开关，让 Flink SQL 中配置的 hint options 生效
SET table.dynamic-table-options.enabled = true;

-- 增量一体化读取 BaseStore 和 ChangeStore
SELECT * FROM keyed /*+ OPTIONS('streaming'='true', 'scan.startup.mode'='earliest')*/;
```

Hint Options

|Key| 默认值 |类型|是否必填|描述|
|--- |---|--- |--- |--- |
|streaming| true |String|否|以流的方式读取有界数据还是无解数据，false：读取有界数据，true：读取无界数据|
|arctic.read.mode| file |String|否|指定读 Arctic 表 File 或 Log 的数据。当值为 log 时，必须 开启 Log 配置|
|monitor-interval| 10s |String|否|arctic.read.mode = file 时才生效。监控新提交数据文件的时间间隔|
|scan.startup.mode| latest |String|否|有效值为earliest、latest、timestamp（读 fileStore 暂未支持）。<br>当arctic.read.mode = file 时仅支持earliest、latest。'earliest'表示读取全量表数据，在streaming=true 时会继续增量读取；'latest'：表示读取当前snapshot之后的数据，不包括当前snapshot数据。<br>当 arctic.read.mode = log 时，表示 logStore 消费者初次启动时获取 offset 的模式，'earliest'表示从Kafka中最早的位置读取，'latest'表示从最新的位置读取，'timestamp'表示从Kafka中指定时间位置读取，需配置参数 'scan.startup.timestamp-millis'。|
|其他表参数|(none)|String|否|Arctic 表的所有参数都可以通过 SQL Hint 动态修改，当然只针对此任务生效，具体的参数列表可以参考 [表配置](../configurations.md)。对于Catalog上的权限相关配置，也可以配置在Hint中，参数见 [catalog ddl 中的 properties.auth.XXX](./flink-ddl.md#Flink SQL)|

## Writing With SQL
Arctic 表支持通过 Flink SQL 往 LogStore 或 FileStore 写入数据
### INSERT OVERWRITE
当前仅支持非主键表的 INSERT OVERWRITE。替换表中的数据，Overwrite 为原子操作。分区会由查询语句中动态生成，这些分区的数据会被全量覆盖。

INSERT OVERWRITE 只允许以 Flink Batch 的模式运行。

```sql
INSERT OVERWRITE unkeyed VALUES (1, 'a', '2022-07-01');
```

```sql
-- 也支持覆盖指定分区的数据：

INSERT OVERWRITE `arctic_catalog`.`arctic_db`.`unkeyed` PARTITION(data='2022-07-01') SELECT 5, 'b';
```
对于无分区的表，INSERT OVERWRITE 将覆盖表里的全量数据
### INSERT INTO
对于 Arctic 表，可以指定往 FileStore 或 LogStore（需在建表时[开启 Log 配置](#log)）写入数据。

对于 Arctic 主键表，写 FileStore 会将 CDC 数据写入 ChangeStore 中.
```sql
INSERT INTO `arctic_catalog`.`arctic_db`.`test_table` 
    /*+ OPTIONS('arctic.emit.mode'='log,file') */
SELECT id, name from `source`;
```

Hint Options

|Key|默认值|类型|是否必填|描述|
|--- |--- |--- |--- |--- |
|arctic.emit.mode| auto   | String   |否| 数据写入模式，现阶段支持：file、log、auto 例如<br>'file' 表示仅将数据写入 filestore。<br>'log' 表示仅将数据写入 logstore。<br>'file,log' 表示将数据写入 filestore 和 logstore。<br>'auto' 表示如果 arctic 表的 logstore 被禁用，则只将数据写入 filestore；如果启用了 arctic 表的 logstore，意味着将数据写入 filestore 和 logstore。<br>推荐使用 'auto'。|
|arctic.emit.auto-write-to-logstore.watermark-gap| (none) | Duration |否| 仅在'arctic.emit.mode'='auto'时启用，如果arctic writers的watermark大于当前系统时间戳减去特定值，writers 也会将数据写入 logstore。 默认：启动作业后立即启用 logstore 写入器。<br>该值必须大于 0。|
|log.version|v1|String|否|log 数据格式。当前只有一个版本，可不填|
|sink.parallelism|(none)|String|否|写入 file/log 并行度，file 提交算子的并行度始终为 1|
|write.distribution-mode|hash|String|否|写入 Arctic 表的 distribution 模式。包括：none、hash|
|write.distribution.hash-mode|auto|String|否|写入 Arctic 表的 hash 策略。只有当 write.distribution-mode=hash 时才生效。<br>primary-key、partition-key、primary-partition-key、auto。<br>primary-key: 按主键 shuffle<br>partition-key: 按分区 shuffle<br>primary-partition-key: 按主键+分区 shuffle<br>auto: 如果是有主键且有分区表，则为 primary-partition-key；如果是有主键且无分区表，则为 primary-key；如果是无主键且有分区表，则为 partition-key。否则为 none|
|properties.pulsar.admin.adminUrl| (none) |String|LogStore 是 pulsar 并且是查询时必填，否则可不填| Pulsar admin 的 HTTP URL，如：http://my-broker.example.com:8080|
|properties.*|(none)|String|否| Logstore的参数。<br><br>对于 LogStore 为 Kafka ('log-store.type'='kafka' 默认值)时，Kafka Producer 支持的其他所有参数都可以通过在前面拼接 `properties.` 的前缀来设置，<br>如：`'properties.batch.size'='16384'`，<br>完整的参数信息可以参考 [Kafka官方手册](https://kafka.apache.org/documentation/#producerconfigs); <br><br>对于 LogStore 为 Pulsar ('log-store.type'='pulsar')时，Pulsar 支持的相关配置都可以通过在前面拼接 `properties.` 的前缀来设置，<br>如：`'properties.pulsar.client.requestTimeoutMs'='60000'`，<br>完整的参数信息可以参考 [Flink-Pulsar-Connector文档](https://nightlies.apache.org/flink/flink-docs-release-1.16/docs/connectors/datastream/pulsar)|
|其他表参数|(none)|String|否|Arctic 表的所有参数都可以通过 SQL Hint 动态修改，当然只针对此任务生效，具体的参数列表可以参考 [表配置](../configurations.md)。对于Catalog上的权限相关配置，也可以配置在Hint中，参数见 [catalog ddl 中的 properties.auth.XXX](./flink-ddl.md#Flink SQL)|

### CDC 入湖
以下是一个简单的示例，展示如何将 MySQL CDC（Change Data Capture）数据写入到 Arctic 数据湖：

首先，通过 Flink 引擎创建一个入湖任务，该任务会自动将数据写入到 LogStore，而无需手动重启任务。
这种方案适用于数据库的存量数据和增量数据入湖，其中存量数据写入到 FileStore 进行批处理计算，而最新的数据则写入到 LogStore 进行实时计算。

此外，当启用 UPSERT 功能时，具有相同主键的多条插入数据会在表结构优化过程中进行合并，并保留最后插入的数据。
```sql
CREATE TABLE user_info (
    id int,
    name string,
    insert_time timestamp,
    primary key (id) not enforced)
WITH (
 'connector'='mysql-cdc',
 'hostname'='localhost',
 'port' = '3306',
 'username' = 'root',
 'password' = '123456',
 'database-name' = 'testdb',
 'table-name'='user_info');
 
CREATE TABLE IF NOT EXISTS arctic.db.user_info(
    id int,
    name string, 
    insert_time timestamp,
    primary key (id) not enforced
);

INSERT INTO arctic.db.user_info 
/*+ OPTIONS(
    'arctic.emit.mode'='auto',
    'arctic.emit.auto-write-to-logstore.watermark-gap'='60s',
    'write.upsert.enabled'='true') */
 SELECT * FROM source;
```