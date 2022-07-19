# Flink

Arctic 集成了 [Apache Flink](https://flink.apache.org/) 的 DataStream API 与 Table API，以方便的使用 Flink 从 Arctic 表中读取数据， 或将数据写入
Arctic 表中， 现阶段 Arctic 集成的 Flink 版本为：1.12.x, 1.14.x 。

## Create Catalogs
### Flink Sql 支持创建 flink catalog。
通过执行下述语句可以创建 flink catalog

```sql
CREATE CATALOG <catalog_name> WITH (
  'type'='arctic',
  `<config_key>`=`<config_value>`
); 
```

其中 `<catalog_name>` 为用户自定义的 flink catalog 名称， `<config_key>`=`<config_value>` 有如下配置：

- metastore.url：必填。Arctic Metastore 的 URL，thrift://`<ip>`:`<port>`/`<catalog_name_in_metastore>`
- default-database：非必填，默认 default。默认使用的数据库。

### 通过 Flink Sql Client YAML 配置
参考 Flink Sql Client [官方配置](https://nightlies.apache.org/flink/flink-docs-release-1.12/dev/table/sqlClient.html#environment-files)。
修改 flink 目录中的 conf/sql-client-defaults.yaml 文件。
```yaml
catalogs:
- name: <catalog_name>
  type: arctic
  metastore.url: ...
  ...
```

## DDL 语句

### CREATE DATABASE
默认使用创建 catalog 时的 default-database 配置（默认值：default）。可使用下述例子创建数据库：
    
```sql
CREATE DATABASE [catalog_name.]arctic_db;

USE arctic_db;
```
### DROP DATABASE
删除库：
    
```sql
DROP DATABASE catalog_name.arctic_db
```

### <a name='CREATE TABLE'>CREATE TABLE</a>

```sql
CREATE TABLE `arctic_catalog`.`arctic_db`.`test_table` (
    id BIGINT,
    name STRING,
    op_time TIMESTAMP,
    PRIMARY KEY (id) NOT ENFORCED
) WITH (
    'key' = 'value'
);
```

目前支持 [Flink Sql 建表](https://nightlies.apache.org/flink/flink-docs-release-1.12/dev/table/sql/create.html#create-table)的大多数语法，包括：

- PARTITION BY (column1, column2, ...)：配置 Flink 分区字段，但 Flink 还未支持隐藏分区
- PRIMARY KEY (column1, column2, ...)：配置主键
- WITH ('key'='value', ...)：配置 Arctic Table 的属性

目前不支持计算列、watermark 字段的配置
    
### PARTITIONED BY
使用 PARTITIONED BY 创建分区表。
```sql
CREATE TABLE `arctic_catalog`.`arctic_db`.`test_table` (
    id BIGINT,
    name STRING,
    op_time TIMESTAMP
) PARTITIONED BY(op_time) WITH (
    'key' = 'value'
);
```
Arctic 表支持隐藏分区，但 Flink 不支持函数计算的分区，因此目前通过 Flink Sql 只能创建相同值的分区
### CREATE TABLE LIKE
创建一个与已有表相同表结构、分区、表属性的表，可使用 CREATE TABLE LIKE

```sql
CREATE TABLE `arctic_catalog`.`arctic_db`.`test_table` (
    id BIGINT,
    name STRING,
    op_time TIMESTAMP
);

CREATE TABLE  `arctic_catalog`.`arctic_db`.`test_table_like` LIKE `arctic_catalog`.`arctic_db`.`test_table`;
```
更多细节可参考 [Flink create table like](https://nightlies.apache.org/flink/flink-docs-release-1.12/dev/table/sql/create.html#like)

### DROP TABLE
删除表：
```sql
DROP TABLE `arctic_catalog`.`arctic_db`.`test_table`;
```

## SHOW 语句

### SHOW DATABASES
查看当前 catalog 下所有数据库名称：
```sql
SHOW DATABASES;
```

### SHOW TABLES
查看当前数据库的所有表名称：
```sql
SHOW TABLES;
```
## <a name="log">LOG 实时数据</a>
参考整体介绍[Log store](table-structure.md#Log store)

Arctic 表提供了 File 和 Log 的存储，File 存储海量的全量数据，Log 存储实时的增量数据。
实时数据可以提供毫秒级的数据可见性，并能保证数据一致性。可在不开启 Kafka 事务的情况下，保证数据的一致性。

其底层存储可以对接外部消息队列中间件，当前仅支持 Kafka。用户可以通过在创建 Arctic 表时[配置](#CREATE TABLE)下述参数开启 Log：
    
- log-store.enable: 是否打开 Log，默认 false。打开 Log 时必填。
- log-store.type: 底层存储中间件，默认 kafka。当前仅支持 Kafka。非必填。
- log-store.address: log-store 的地址。打开 Log 时必填。当前为 kafka 的 bootstrap servers 的地址。如 broker1:port1, broker2:port2
- log-store.topic: log-store 的 topic。打开 Log 时必填
- log-store.data-format: log-store 的数据存储格式。默认 json。非必填。
- log-store.data-version: log-store 的数据格式版本。默认 v1。非必填。该格式是为了实现数据低延迟、并保证端到端的一致性所使用。 

详细的使用参考 [sql 读](#sql-read)、[java 读](#datastream-read)、[sql 写](#sql-write)、[java 写](#datastream-write)
## Changelog 数据
参考整体介绍[Change store](table-structure.md#Change store)

对于 Arctic 主键表，分为全量数据和 Changelog 增量数据两部分，其中的 Changelog 存放实时写入的 CDC 数据。这部分数据会定期并入 Arctic 全量数据中，详见 [结构优化](table-structure.md#结构优化)。
Changelog 数据根据 Flink checkpoint 的周期进行提交，提交后才对用户可见。可以直接读 Changelog 数据作计算分析。

详细的使用参考 [sql 读](#sql-read-changelog)、[java 读](#datastream-read)、[sql 写](#sql-write)、[java 写](#datastream-write)
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
非主键表支持以 Batch 模式读取全量数据、指定 snapshot-id/时间戳的快照数据、指定 snapshot 区间的增量数据
    
```sql
-- 在当前 session 中以批的模式运行 Flink 任务
SET execution.runtime-mode = batch;

-- 打开动态表参数配置开关，让 Flink SQL 中配置的 hint options 生效
SET table.dynamic-table-options.enabled=true;
```

非主键表读 bounded 数据：
```sql
-- 读非主键表全量数据
SELECT * FROM unkeyed;
-- 读非主键表指定快照数据
SELECT * FROM unkeyed /*+ OPTIONS('snapshot-id'='4411985347497777546')*/;
```
支持的参数包括：

- case-sensitive：是否区分大小写，默认为 false，不区分大小写
- snapshot-id：读指定 snapshot 的全量数据
- as-of-timestamp：读小于该时间戳的最近一次 snapshot 的全量数据
- start-snapshot-id：配合 end-snapshot-id，读两个区间的增量数据(snapshot1, snapshot2]
- end-snapshot-id

主键表读当前全量及之后的 CDC 数据：
```sql
SELECT * FROM keyed/*+ OPTIONS('read.distribution-mode'='hash', 'read.distribution.hash-mode'='primary-key')*/;
```
支持 <a name="read-distribution">read distribution</a> 配置参数：

**注意：如果读主键表 cdc/log 的数据不对 key 进行 hash，会导致下游 cdc 数据乱序，数据的准确性将无法保证**

- read.distribution-mode: 读 Arctic 表的 distribution 模式。包括：none、hash。默认 hash
- read.distribution.hash-mode: 读 Arctic 表的 hash 策略。只有当 read.distribution-mode=hash 时才生效。
  primary-key、partition-key、primary-partition-key、auto。 默认auto
  - primary-key: 按主键 shuffle
  - partition-key: 按分区 shuffle
  - primary-partition-key: 按主键+分区 shuffle
  - auto: 如果是有主键且有分区表，则为 primary-partition-key；如果是有主键且无分区表，则为 primary-key；如果是无主键且有分区表，则为 partition-key。否则为 none

### Streaming Mode
Arctic 支持以 Streaming 模式读 File 或 Log 中的增量数据

#### <a name='sql-read'>Unbounded Source</a>
读 Arctic Log 中的实时数据：
    
```sql
-- 在当前 session 中以流的模式运行 Flink 任务
SET execution.runtime-mode = streaming;

-- 打开动态表参数配置开关，让 Flink SQL 中配置的 hint options 生效
SET table.dynamic-table-options.enabled=true;

SELECT * FROM test_table /*+ OPTIONS('arctic.read.mode'='log') */;
```
支持以下 Kafka 相关的参数配置：

- properties.group.id：读取 Kafka Topic 时使用的 group id
- scan.startup.mode：Kafka 消费者初次启动时获取 offset 的模式，合法的取值包括：earliest-offset、latest-offset、
group-offsets、timestamp、specific-offsets， 具体取值的含义可以参考
[Flink官方手册](https://ci.apache.org/projects/flink/flink-docs-release-1.12/dev/table/connectors/kafka.html#start-reading-position)
- scan.startup.specific-offsets：scan.startup.mode 取值为 specific-offsets 时，为每个分区设置的起始 offset, 参考格式：partition:0,offset:
42;partition:1,offset:300
- scan.startup.timestamp-millis：scan.startup.mode 取值为 timestamp 时，初次启动时获取数据的起始时间戳（毫秒级）
- properties.*：Kafka Consumer 支持的其他所有参数都可以通过在前面拼接 `properties.` 的前缀来设置，如：
`'properties.batch.size'='16384'`，完整的参数信息可以参考 [Kafka官方手册](https://kafka.apache.org/documentation/#consumerconfigs)
- [read distribution 参数](#read-distribution)

读 Arctic 非主键表 File 中的增量数据：
        
```sql
-- 在当前 session 中以流的模式运行 Flink 任务
SET execution.runtime-mode = streaming;

-- 打开动态表参数配置开关，让 Flink SQL 中配置的 hint options 生效
SET table.dynamic-table-options.enabled = true;

-- 读当前快照之后的增量数据
SELECT * FROM unkeyed /*+ OPTIONS('streaming'='true', 'monitor-interval'='1s')*/;
```
支持 Hint 参数有：

- streaming: 是否以流的方式读取数据（unbounded），默认 false
- monitor-interval: 监控新提交数据文件的时间间隔，默认 10s
- start-snapshot-id: 从指定的 snapshot 开始读取增量数据（不包括 start-snapshot-id 的快照数据），不指定则读当前快照之后（不包含当前）的增量数据

<a name='sql-read-changelog'>读 Arctic 主键表 File 中的数据：</a>

```sql
-- 在当前 session 中以流的模式运行 Flink 任务
SET execution.runtime-mode = streaming;

-- 打开动态表参数配置开关，让 Flink SQL 中配置的 hint options 生效
SET table.dynamic-table-options.enabled = true;

-- 读全量数据及 change 表中的 CDC 数据
SELECT * FROM keyed /*+ OPTIONS('streaming'='true')*/;

-- 读增量 CDC 数据
SELECT * FROM keyed /*+ OPTIONS('streaming'='true', 'scan.startup.mode'='latest')*/;
```

- [read distribution 参数](#read-distribution)

#### Hint Options

<table class="table table-bordered">
  <thead>
    <tr>
        <th class="text-left" style="width: 20%">Key</th>
        <th class="text-left" style="width: 15%">默认值</th>
        <th class="text-left" style="width: 10%">类型</th>
        <th class="text-left" style="width: 10%">是否必填</th>
        <th class="text-left" style="width: 55%">描述</th>
    </tr>
  </thead>
  <tbody>
    <tr>
        <td>case-sensitive</td>
        <td style="word-wrap: break-word;">false</td>
        <td>String</td>
        <td>否</td>
        <td>是否区分大小写。true：区分，false：不区分</td>
    </tr>
    <tr>
        <td>streaming</td>
        <td style="word-wrap: break-word;">false</td>
        <td>String</td>
        <td>否</td>
        <td>是否以流的方式读取数据（unbounded）</td>
    </tr>
    <tr>
        <td>snapshot-id</td>
        <td style="word-wrap: break-word;">无</td>
        <td>String</td>
        <td>否</td>
        <td>仅对无主键表有效。读指定 snapshot 的全量数据，只有在 streaming 为 false 或不配置时生效</td>
    </tr>
    <tr>
        <td>as-of-timestamp</td>
        <td style="word-wrap: break-word;">无</td>
        <td>String</td>
        <td>否</td>
        <td>仅对无主键表有效。读小于该时间戳的最近一次 snapshot 的全量数据，只有在 streaming 为 false 或不配置时生效</td>
    </tr>
    <tr>
        <td>start-snapshot-id</td>
        <td style="word-wrap: break-word;">无</td>
        <td>String</td>
        <td>否</td>
        <td>仅对无主键表有效。在 streaming 为 false 时，需配合 end-snapshot-id，读两个区间的增量数据(snapshot1, snapshot2]。
        在 streaming 为 true 时，读该 snapshot 之后的增量数据，不指定则读当前快照之后（不包含当前）的增量数据</td>
    </tr>
    <tr>
        <td>end-snapshot-id</td>
        <td style="word-wrap: break-word;">无</td>
        <td>String</td>
        <td>否</td>
        <td>仅对无主键表有效。需配合 start-snapshot-id，读两个区间的增量数据(snapshot1, snapshot2]</td>
    </tr>
    <tr>
        <td>arctic.read.mode</td>
        <td style="word-wrap: break-word;">file</td>
        <td>String</td>
        <td>否</td>
        <td>指定读 Arctic 表 File 或 Log 的数据。当值为 log 时，必须 <a href="#log">开启 Log 配置</a></td>
    </tr>
    <tr>
        <td>properties.group.id</td>
        <td style="word-wrap: break-word;">无</td>
        <td>String</td>
        <td>否</td>
        <td>arctic.read.mode 为 log 时才生效。读取 Kafka Topic 时使用的 group id</td>
    </tr>
    <tr>
        <td>scan.startup.mode</td>
        <td style="word-wrap: break-word;">group-offsets</td>
        <td>String</td>
        <td>否</td>
        <td>arctic.read.mode 为 log 时表示：Kafka 消费者初次启动时获取 offset 的模式，合法的取值包括：earliest-offset、latest-offset、
    group-offsets、timestamp、specific-offsets， 具体取值的含义可以参考 <a href="https://ci.apache.org/projects/flink/flink-docs-release-1.12/dev/table/connectors/kafka.html#start-reading-position">Flink官方手册</a>
        <br> arctic.read.mode 为 file 时仅对有主键表有效，默认 'earliest'，表示读取整个表当前snapshot的所有数据；'latest'，表示读取 change 表增量数据，从当前 snapshot 开始(不包括当前 snapshot)。
        </td>
    </tr>
    <tr>
        <td>scan.startup.specific-offsets</td>
        <td style="word-wrap: break-word;">无</td>
        <td>String</td>
        <td>否</td>
        <td>arctic.read.mode 为 log 时才生效。scan.startup.mode 取值为 specific-offsets 时，为每个分区设置的起始 offset, 参考格式：partition:0,offset:
    42;partition:1,offset:300</td>
    </tr>
    <tr>
        <td>scan.startup.timestamp-millis</td>
        <td style="word-wrap: break-word;">无</td>
        <td>String</td>
        <td>否</td>
        <td>arctic.read.mode 为 log 时才生效。scan.startup.mode 取值为 timestamp 时，初次启动时获取数据的起始时间戳（毫秒级）</td>
    </tr>
    <tr>
        <td>properties.*</td>
        <td style="word-wrap: break-word;">无</td>
        <td>String</td>
        <td>否</td>
        <td>arctic.read.mode 为 log 时才生效。Kafka Consumer 支持的其他所有参数都可以通过在前面拼接 `properties.` 的前缀来设置，如：
    `'properties.batch.size'='16384'`，完整的参数信息可以参考 <a href="https://kafka.apache.org/documentation/#consumerconfigs">Kafka官方手册</a></td>
    </tr>
    <tr>
        <td>monitor-interval</td>
        <td style="word-wrap: break-word;">10s</td>
        <td>String</td>
        <td>否</td>
        <td>arctic.read.mode 为 file 时才生效。监控新提交数据文件的时间间隔</td>
    </tr>
    <tr>
        <td><h5>read.distribution-mode</h5></td>
        <td style="word-wrap: break-word;">hash</td>
        <td>String</td>
        <td>否</td>
        <td>读 Arctic 表的 distribution 模式。包括：none、hash。注意：如果读主键表 cdc/log 的数据不对 key 进行 hash，会导致下游 cdc 数据乱序，数据的准确性将无法保证</td>
    </tr>
    <tr>
        <td><h5>read.distribution.hash-mode</h5></td>
        <td style="word-wrap: break-word;">auto</td>
        <td>String</td>
        <td>否</td>
        <td>读 Arctic 表的 hash 策略。只有当 read.distribution-mode=hash 时才生效。
        primary-key、partition-key、primary-partition-key、auto。
        <p> primary-key: 按主键 shuffle
        <p> partition-key: 按分区 shuffle
        <p> primary-partition-key: 按主键+分区 shuffle
        <p> auto: 如果是有主键且有分区表，则为 primary-partition-key；如果是有主键且无分区表，则为 primary-key；如果是无主键且有分区表，则为 partition-key。否则为 none</td>
    </tr>
  </tbody>
</table>

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
### <a name='sql-write'>INSERT INTO</a>
对于 Arctic 表，可以指定往 File 或 Log（需在建表时[开启 Log 配置](#log)）写入数据。

对于 Arctic 主键表，写 File 会将 CDC 数据写入 [Changelog](#Changelog) 中

```sql
INSERT INTO `arctic_catalog`.`arctic_db`.`test_table` /*+ OPTIONS('arctic.emit.mode'='log,file') */
SELECT id, name from `source`;
```
支持的参数有：

- arctic.emit.mode: 数据写入模式，现阶段支持：file、log，默认为 file，支持同时写入，用逗号分割， 如：`'arctic.emit.mode' = 'file,log'`
- case-sensitive: 是否区分大小写。默认 false，不区分
- log.version: log 数据格式。默认 v1。当前只有一个版本，可不填
- properties.*: [kafka client 配置](https://nightlies.apache.org/flink/flink-docs-release-1.12/dev/table/connectors/kafka.html#properties)，详细参数见 [kafka producer 配置](https://kafka.apache.org/documentation/#producerconfigs)
- sink.parallelism: sink 并行度，file commit 的并行度始终为 1
- write.distribution-mode: 写入 Arctic 表的 distribution 模式。包括：none、hash。默认 hash
- write.distribution.hash-mode: 写入 Arctic 表的 hash 策略。只有当 write.distribution-mode=hash 时才生效。
    primary-key、partition-key、primary-partition-key、auto。 默认auto
    - primary-key: 按主键 shuffle
    - partition-key: 按分区 shuffle
    - primary-partition-key: 按主键+分区 shuffle
    - auto: 如果是有主键且有分区表，则为 primary-partition-key；如果是有主键且无分区表，则为 primary-key；如果是无主键且有分区表，则为 partition-key。否则为 none
- write.upsert.enabled: 写主键表 file 时，提供 upsert 语义，保证主键的唯一性。默认 false。
- 其他表参数：Arctic 表的所有参数都可以通过 SQL Hint 动态修改，当然只针对此任务生效，具体的参数列表可以参考 [表配置](table-properties.md)

#### Hint Options

<table class="table table-bordered">
  <thead>
    <tr>
        <th class="text-left" style="width: 20%">Key</th>
        <th class="text-left" style="width: 15%">默认值</th>
        <th class="text-left" style="width: 10%">类型</th>
        <th class="text-left" style="width: 10%">是否必填</th>
        <th class="text-left" style="width: 55%">描述</th>
    </tr>
  </thead>
  <tbody>
    <tr>
        <td>case-sensitive</td>
        <td style="word-wrap: break-word;">false</td>
        <td>String</td>
        <td>否</td>
        <td>是否区分大小写。true：区分，false：不区分</td>
    </tr>
    <tr>
        <td>arctic.emit.mode</td>
        <td style="word-wrap: break-word;">file</td>
        <td>String</td>
        <td>否</td>
        <td>数据写入模式，现阶段支持：file、log，默认为 file，支持同时写入，用逗号分割， 如：`'arctic.emit.mode' = 'file,log'`</td>
    </tr>
    <tr>
        <td>log.version</td>
        <td style="word-wrap: break-word;">v1</td>
        <td>String</td>
        <td>否</td>
        <td>log 数据格式。当前只有一个版本，可不填</td>
    </tr>
    <tr>
        <td>sink.parallelism</td>
        <td style="word-wrap: break-word;">无</td>
        <td>String</td>
        <td>否</td>
        <td>sink 并行度，file commit 的并行度始终为 1</td>
    </tr>
    <tr>
        <td>write.distribution-mode</td>
        <td style="word-wrap: break-word;">hash</td>
        <td>String</td>
        <td>否</td>
        <td>写入 Arctic 表的 distribution 模式。包括：none、hash</td>
    </tr>
    <tr>
        <td>write.distribution.hash-mode</td>
        <td style="word-wrap: break-word;">auto</td>
        <td>String</td>
        <td>否</td>
        <td>写入 Arctic 表的 hash 策略。只有当 write.distribution-mode=hash 时才生效。
        primary-key、partition-key、primary-partition-key、auto。
        <p> primary-key: 按主键 shuffle
        <p> partition-key: 按分区 shuffle
        <p> primary-partition-key: 按主键+分区 shuffle
        <p> auto: 如果是有主键且有分区表，则为 primary-partition-key；如果是有主键且无分区表，则为 primary-key；如果是无主键且有分区表，则为 partition-key。否则为 none</td>
    </tr>
    <tr>
        <td>properties.*</td>
        <td style="word-wrap: break-word;">无</td>
        <td>String</td>
        <td>否</td>
        <td>Kafka Producer 支持的其他所有参数都可以通过在前面拼接 `properties.` 的前缀来设置，如：
    `'properties.batch.size'='16384'`，完整的参数信息可以参考 <a href="https://kafka.apache.org/documentation/#producerconfigs">kafka producer 配置</a></td>
    </tr>
    <tr>
        <td>write.upsert.enabled</td>
        <td style="word-wrap: break-word;">false</td>
        <td>String</td>
        <td>否</td>
        <td>写主键表 file 时，提供 upsert 语义，保证主键的唯一性</td>
    </tr>
    <tr>
        <td>其他表参数</td>
        <td style="word-wrap: break-word;">无</td>
        <td>String</td>
        <td>否</td>
        <td>Arctic 表的所有参数都可以通过 SQL Hint 动态修改，当然只针对此任务生效，具体的参数列表可以参考 <a href="table-properties.md">表配置</a></td>
    </tr>
  </tbody>
</table>

## Reading with DataStream
Arctic 支持通过 Java API 以 Batch 或 Streaming 的方式读数据
### Batch Mode
使用 Batch 模式读 File 中的全量和增量数据
#### Bounded Source
非主键表支持以批模式读取全量数据、指定 snapshot-id/时间戳的快照数据、指定 snapshot 区间的增量数据；
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
// 默认为 false。
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

- case-sensitive：是否区分大小写，默认为 false，不区分大小写
- snapshot-id：读指定 snapshot 的全量数据
- as-of-timestamp：读小于该时间戳的最近一次 snapshot 的全量数据
- start-snapshot-id：配合 end-snapshot-id，读两个区间的增量数据(snapshot1, snapshot2]
- end-snapshot-id

### Streaming Mode
Arctic 支持以 Streaming 模式通过 JAVA API 读 File 或 Log 中的增量数据

#### <a name='datastream-read'>Unbounded Source</a>
读 Arctic Log 中的实时数据：

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

RowType flinkUserSchema = FlinkSchemaUtil.convert(schema);

JsonRowDataDeserializationSchema deserializationSchema =
    new JsonRowDataDeserializationSchema(
        flinkUserSchema,
        InternalTypeInfo.of(flinkUserSchema), false, false, TimestampFormat.ISO_8601);

List<String> topics = new ArrayList<>();
topics.add("topic_name");

// 配置 kafka consumer 参数。详见 https://kafka.apache.org/documentation/#consumerconfigs
Properties properties = new Properties();
properties.put("group.id", groupId);
properties.put("properties.bootstrap.servers", "broker1:port1, broker2:port2 ...");

Configuration configuration = new Configuration();
// 开启保证数据一致性的低延迟读
configuration.set(ARCTIC_LOG_CONSISTENCY_GUARANTEE_ENABLE, true);

LogKafkaConsumer kafkaConsumer = new LogKafkaConsumer(
            topics,
            new KafkaDeserializationSchemaWrapper<>(deserializationSchema),
            properties,
            schema,
            configuration
        );
// 配置读的起始位置
kafkaConsumer.setStartFromEarliest();

DataStream<RowData> stream = env.addSource(kafkaConsumer);
// 打印读出的所有数据
stream.print();

// 提交并执行任务
env.execute("Test Arctic Stream Read");
```

读 Arctic File 中的增量数据：
    
```java
StreamExecutionEnvironment env = ...;
InternalCatalogBuilder catalogBuilder = ...;
TableIdentifier tableId = ...;
ArcticTableLoader tableLoader = ...;

Map<String, String> properties = new HashMap<>();
// 默认为 false
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
properties 支持的参数，**当前只对非主键表生效**:

- case-sensitive：是否区分大小写，默认为 false，不区分大小写
- streaming: 是否以流的方式读取数据（unbounded），默认 false
- monitor-interval: 监控新提交数据文件的时间间隔，默认 10s
- start-snapshot-id: 从指定的 snapshot 开始读取增量数据（不包括 start-snapshot-id 的快照数据），不指定则读当前快照之后（不包含当前）的增量数据

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

### <a name='datastream-write'>Appending data</a>

对于 Arctic 表，支持通过 JAVA API 指定往 File 或 Log（需在建表时[开启 Log 配置](#log)）写入数据。注意：主键表只会往 Changelog 写。

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

其中 table.properties 中支持写入的参数有：

- arctic.emit.mode: 数据写入模式，现阶段支持：file、log，默认为 file，支持同时写入，用逗号分割， 如：`'arctic.emit.mode' = 'file,log'`
- log.version: log 数据格式。默认 v1。当前只有一个版本，可不填
- properties.*: [kafka client 配置](https://nightlies.apache.org/flink/flink-docs-release-1.12/dev/table/connectors/kafka.html#properties)，详细参数见 [kafka producer 配置](https://kafka.apache.org/documentation/#producerconfigs)
- sink.parallelism: sink 并行度，file commit 的并行度始终为 1
- write.distribution-mode: 写入 Arctic 表的 distribution 模式。包括：none、hash。默认 hash
- write.distribution.hash-mode: 写入 Arctic 表的 hash 策略。只有当 write.distribution-mode=hash 时才生效。
    primary-key、partition-key、primary-partition-key、auto。 默认auto。
    - primary-key: 按主键 shuffle。
    - partition-key: 按分区 shuffle。
    - primary-partition-key: 按主键+分区 shuffle。
    - auto: 如果是有主键且有分区表，则为 primary-partition-key；如果是有主键且无分区表，则为 primary-key；如果是无主键且有分区表，则为 partition-key。否则为 none
- write.upsert.enabled: 写主键表 file 时，提供 upsert 语义，保证主键的唯一性。默认 false。
- 其他表参数：Arctic 表的所有参数都可以通过 SQL Hint 动态修改，当然只针对此任务生效，具体的参数列表可以参考 [表配置](table-properties.md)

# 常见问题
## 写 Arctic 表 File 数据不可见
需要打开 Flink checkpoint，修改 Flink conf 中的 [Flink checkpoint 配置](https://nightlies.apache.org/flink/flink-docs-release-1.12/deployment/config.html#execution-checkpointing-interval)，数据只有在 checkpoint 时才会提交。
