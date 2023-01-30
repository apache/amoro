## LOG 实时数据
Arctic 表提供了 File 和 Log 的存储，File 存储海量的全量数据，Log 存储实时的增量数据。
实时数据可以提供毫秒级的数据可见性，可在不开启 Logstore 事务的情况下，保证数据的一致性。

其底层存储可以对接外部消息队列中间件，当前仅支持 Kafka、Pulsar。

用户可以通过在创建 Arctic 表时配置下述参数开启 Logstore，具体配置可以参考 [Logstore 相关配置](../configurations.md#logstore)

使用 Apache Kafka 作为 Logstore 存储系统[案例分享](hidden-kafka.md)

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
使用 Batch 模式读 File 中的全量和增量数据
#### Bounded Source
非主键表支持以 Batch 模式读取全量数据，指定 snapshot-id/timestamp 的快照数据，指定 snapshot 区间的增量数据。

> **TIPS**
> 
> Logstore 不支持有界读取.
    
```sql
-- 在当前 session 中以批的模式运行 Flink 任务
SET execution.runtime-mode = batch;

-- 打开动态表参数配置开关，让 Flink SQL 中配置的 hint options 生效
SET table.dynamic-table-options.enabled=true;
```

####非主键表 bounded 数据
```sql
-- 读非主键表全量数据
SELECT * FROM unkeyed /*+ OPTIONS('streaming'='false')*/;
-- 读非主键表指定快照数据
SELECT * FROM unkeyed /*+ OPTIONS('snapshot-id'='4411985347497777546')*/;
```
非主键表有界读取 BaseStore 支持的参数包括：

|Key|默认值|类型|是否必填|描述|
|--- |--- |--- |--- |--- |
|snapshot-id<img width=100/>|(none)|Long|否|读指定 snapshot 的全量数据，只有在 streaming 为 false 或不配置时生效|
|as-of-timestamp|(none)|Long|否|读小于该时间戳的最近一次 snapshot 的全量数据，只有在 streaming 为 false 或不配置时生效|
|start-snapshot-id|(none)|Long|否|在 streaming 为 false 时，需配合 end-snapshot-id，读两个区间的增量数据(snapshot1, snapshot2]。<br>在 streaming 为 true 时，读该 snapshot 之后的增量数据，不指定则读当前快照之后（不包含当前）的增量数据|
|end-snapshot-id|(none)|Long|否|在 streaming 为 false 时 需配合 start-snapshot-id，读两个区间的增量数据(snapshot1, snapshot2]|
|其他表参数|(none)|String|否|Arctic 表的所有参数都可以通过 SQL Hint 动态修改，当然只针对此任务生效，具体的参数列表可以参考 [表配置](../configurations.md)。对于Catalog上的权限相关配置，也可以配置在Hint中，参数见 [catalog ddl 中的 properties.auth.XXX](./flink-ddl.md#Flink SQL)|

####主键表 bounded 数据
```sql
--读当前全量及部分可能未合并的 CDC 数据
SELECT * FROM keyed /*+ OPTIONS('streaming'='false')*/;
```

### Streaming Mode
Arctic 支持以 Streaming 模式读 File 或 Log 中的增量数据。

####Logstore 实时数据
    
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
|scan.startup.mode<img width=90/>| latest |String|否|有效值为earliest、latest、timestamp（读file暂未支持）。当arctic.read.mode = file 时仅支持earliest、latest。'earliest'表示读取全量表数据，在streaming=true时会继续incremental pull；'latest'：表示读取当前snapshot之后的数据，不包括当前snapshot数据。当arctic.read.mode = log 时，表示 Kafka 消费者初次启动时获取 offset 的模式，'earliest'表示从Kafka中最早的位置读取，'latest'表示从最新的位置读取，'timestamp'表示从Kafka中指定时间位置读取，需配置参数 'scan.startup.timestamp-millis'|
|scan.startup.timestamp-millis|(none)|Long|否|当'scan.startup.mode'='timestamp'时有效，从指定的Kafka时间读取数据，值为从1970 1月1日 00:00:00.000 GMT 开始的毫秒时间戳|
|properties.group.id| (none) |String|Logstore 是 kafka 并且是查询时必填，否则可不填| 读取 Kafka Topic 时使用的 group id|
|properties.pulsar.admin.adminUrl| (none) |String|Logstore 是 pulsar 时必填，否则可不填| Pulsar admin 的 HTTP URL，如：http://my-broker.example.com:8080|
|properties.*| (none) |String|否| Logstore的参数。<br><br>对于 Logstore 为 Kafka ('log-store.type'='kafka' 默认值)时，Kafka Consumer 支持的其他所有参数都可以通过在前面拼接 `properties.` 的前缀来设置，<br>如：`'properties.batch.size'='16384'`，<br>完整的参数信息可以参考 [Kafka官方手册](https://kafka.apache.org/documentation/#consumerconfigs); <br><br>对于 Logstore 为 Pulsar ('log-store.type'='pulsar')时，Pulsar 支持的相关配置都可以通过在前面拼接 `properties.` 的前缀来设置，<br>如：`'properties.pulsar.client.requestTimeoutMs'='60000'`，<br>完整的参数信息可以参考 [Flink-Pulsar-Connector文档](https://nightlies.apache.org/flink/flink-docs-release-1.16/docs/connectors/datastream/pulsar)|

####非主键表 Filestore 数据
        
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

####主键表 Filestore 数据

```sql
-- 在当前 session 中以流的模式运行 Flink 任务
SET execution.runtime-mode = streaming;

-- 打开动态表参数配置开关，让 Flink SQL 中配置的 hint options 生效
SET table.dynamic-table-options.enabled = true;

-- 读全量数据及 changelog 中的 CDC 数据
SELECT * FROM keyed;
```

Hint Options

|Key| 默认值 |类型|是否必填|描述|
|--- |---|--- |--- |--- |
|streaming| true |String|否|以流的方式读取有界数据还是无解数据，false：读取有界数据，true：读取无界数据|
|arctic.read.mode| file |String|否|指定读 Arctic 表 File 或 Log 的数据。当值为 log 时，必须 开启 Log 配置|
|monitor-interval| 10s |String|否|arctic.read.mode = file 时才生效。监控新提交数据文件的时间间隔|
|scan.startup.mode| latest |String|否|有效值为earliest、latest、timestamp（读file暂未支持）。当arctic.read.mode = file 时仅支持earliest、latest。'earliest'表示读取全量表数据，在streaming=true时会继续incremental pull；'latest'：表示读取当前snapshot之后的数据，不包括当前snapshot数据。当arctic.read.mode = log 时，表示 Kafka 消费者初次启动时获取 offset 的模式，'earliest'表示从Kafka中最早的位置读取，'latest'表示从最新的位置读取，'timestamp'表示从Kafka中指定时间位置读取，需配置参数 'scan.startup.timestamp-millis'|
|其他表参数|(none)|String|否|Arctic 表的所有参数都可以通过 SQL Hint 动态修改，当然只针对此任务生效，具体的参数列表可以参考 [表配置](../configurations.md)。对于Catalog上的权限相关配置，也可以配置在Hint中，参数见 [catalog ddl 中的 properties.auth.XXX](./flink-ddl.md#Flink SQL)|

## Writing With SQL
Arctic 表支持通过 Flink Sql 往 Log 或 File 写入数据
### INSERT OVERWRITE
当前仅支持非主键表的 INSERT OVERWRITE。INSERT OVERWRITE 只允许以 Flink Batch 的模式运行。
替换表中的数据，Overwrite 为原子操作。

分区会由查询语句中动态生成，这些分区的数据会被全量覆盖。
```sql
INSERT OVERWRITE unkeyed VALUES (1, 'a', '2022-07-01');
```
也支持覆盖指定分区的数据
```sql
INSERT OVERWRITE `arctic_catalog`.`arctic_db`.`unkeyed` PARTITION(data='2022-07-01') SELECT 5, 'b';
```
对于无分区的表，INSERT OVERWRITE 将覆盖表里的全量数据
### INSERT INTO
对于 Arctic 表，可以指定往 File 或 Log（需在建表时[开启 Log 配置](#log)）写入数据。

对于 Arctic 主键表，写 File 会将 CDC 数据写入 ChangeStore 中.
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
|properties.pulsar.admin.adminUrl| (none) |String|Logstore 是 pulsar 并且是查询时必填，否则可不填| Pulsar admin 的 HTTP URL，如：http://my-broker.example.com:8080|
|properties.*|(none)|String|否| Logstore的参数。<br><br>对于 Logstore 为 Kafka ('log-store.type'='kafka' 默认值)时，Kafka Consumer 支持的其他所有参数都可以通过在前面拼接 `properties.` 的前缀来设置，<br>如：`'properties.batch.size'='16384'`，<br>完整的参数信息可以参考 [Kafka官方手册](https://kafka.apache.org/documentation/#consumerconfigs); <br><br>对于 Logstore 为 Pulsar ('log-store.type'='pulsar')时，Pulsar 支持的相关配置都可以通过在前面拼接 `properties.` 的前缀来设置，<br>如：`'properties.pulsar.client.requestTimeoutMs'='60000'`，<br>完整的参数信息可以参考 [Flink-Pulsar-Connector文档](https://nightlies.apache.org/flink/flink-docs-release-1.16/docs/connectors/datastream/pulsar)|
|其他表参数|(none)|String|否|Arctic 表的所有参数都可以通过 SQL Hint 动态修改，当然只针对此任务生效，具体的参数列表可以参考 [表配置](../configurations.md)。对于Catalog上的权限相关配置，也可以配置在Hint中，参数见 [catalog ddl 中的 properties.auth.XXX](./flink-ddl.md#Flink SQL)|
