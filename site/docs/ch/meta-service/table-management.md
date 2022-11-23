# 表管理
[AMS Dashboard](http://localhost:1630) 中提供了命令行工具 `Terminal` 帮组用户快速完成表的创建、修改与删除操作。
同时还可以在 [Spark](../spark/spark-ddl.md) 和 [Flink](../flink/flink-ddl.md) 等引擎中使用 SQL 完成表的管理。

## 升级 Hive 表
Arctic 从 v0.3.1 版本开始支持了 Hive 表，可以对已有的 Hive 表进行升级以获得 Arctic 提供的流批一体能力。有关升级后 Hive 表拥有的功能可以参考 [Hive Format](../table-format/hive-format.md)。

登录 [AMS Dashboard](http://localhost:1630) 后，从菜单中的 `Tables` 中选择 Hive 集群中的一张表，即可进行升级操作。

![Hive Table Detail](../images/meta-service/hive-table-detail.png)

点击表详情右上角的 `Upgrade` 按钮（已经完成升级的 Hive 表不再显示此按钮）。

![Hive Table Upgrade](../images/meta-service/hive-table-upgrade.png)

在升级界面为表选择主键，并添加额外的参数，之后点击 `OK` 完成 Hive 表的升级操作。

## 新建表
登录 [AMS Dashboard](http://localhost:1630) 后，进入 `Terminal`，输入建表语句并执行即可完成表的创建。
下面是一个建表的例子：

```sql
create table test_db.test_log_store(
  id int,
  name string,
  op_time timestamp,
  primary key(id)
) using arctic
partitioned by(days(op_time))
tblproperties(
  'log-store.enable' = 'true',
  'log-store.type' = 'kafka',
  'log-store.address' = '127.0.0.1:9092',
  'log-store.topic' = 'local_catalog.test_db.test_log_store.log_store',
  'table.event-time-field ' = 'op_time',
  'table.watermark-allowed-lateness-second' = '60');
```

当前 Terminal 使用 Spark Engine 完成 SQL 的执行，更多有关建表的语法参考 [Spark DDL](../spark/spark-ddl.md#create-table)。

### 配置 Logstore
如 [Tablestore](../table-format/table-store.md) 所述，一张 Arctic 表可能由几部分共同组成，一般来说 Basestore 与 Changestore 会随着表的创建而自动创建，
Logstore 作为可选组件则需要另外的配置来指明，有关 Logstore 的完整配置可以参考 [Logstore相关配置](table-properties.md#logstore)。

上面的例子中将 kafka 集群：`127.0.0.1:9092`中的 topic：`local_catalog.test_db.test_log_store.log_store`作为了新建表的 Logstore,
而在执行上面的语句之前你还需要手动去对应的 Kafka 集群中创建对应的 topic，或者打开集群的自动创建 topic 功能。

### 配置 Watermark
水位（watermark） 用于表述表的写入进度，具体来说它是表上的一个类型为时间戳的属性，意为小于这个时间戳的数据都已经写入到表中，它一般用来观察表的写入进度，也可以作为下游批量计算任务的触发指标。

上面的例子中将 `op_time` 设置为表的事件时间字段，在数据写入时会用写入数据的 `op_time` 来计算表的水位，同时为了应对写入乱序的问题，设置在计算水位时允许的数据迟到时间为一分钟。
可以在 [AMS Dashboard](http://localhost:1630) 的表详情中看到表当前的水位，也可以在`Terminal`中输入下面的 SQL 来查询表的水位：

```sql
SHOW TBLPROPERTIES test_db.test_log_store ('watermark.table');
```

预期你将得到以下的结果：

```text
+-----------------+---------------+
| key             | value         |
+-----------------+---------------+
| watermark.table | 1668579055000 |
+-----------------+---------------+
```

### 参数配置
Logstore 的相关配置是通过表的额外属性在表创建之时写入表的配置当中的，[Table Config](table-properties.md) 中有当前表的所有的可用配置，
它们大部分都已经被设置了合理的默认值，你也可以按需在创建语句中进行设置。

## 修改表

登录 [AMS Dashboard](http://localhost:1630) 后，进入 `Terminal`，输入修改语句并执行即可完成表的修改。
下面是一个新增字段的的例子：

```sql
ALTER TABLE test_db.test_log_store ADD COLUMN new_column string comment 'new_column docs';
```

当前 Terminal 使用 Spark Engine 完成 SQL 的执行，更多有关修改表的语法参考 [Spark DDL](../spark/spark-ddl.md#alter-table)。

## 配置结构优化

Arctic 提供了自动结构优化的功能，触发表的结构优化的前提是 [启动 Optimizer](../docker-quickstart.md#ams)。

### 修改 Optimize Group
如果要使用在特定的 [Optimizer Group](../optimizers.md#optimizer-group) 下启动的 Optimizer 执行结构优化，则需要修改表的 `optimize.group` 参数，为表指定特定的资源池，设置方式如下：

```sql
ALTER TABLE test_db.test_log_store set tblproperties (
    'optimize.group' = 'group_name');
```

默认情况下，`'optimize.group' = 'default'`。

### 调整 Optimize 资源

如果同一个 Optimizer Group 下有多张表要执行 Optimize，可以通过调整 `quota` 来手动调整每张表的资源占比：

```sql
ALTER TABLE test_db.test_log_store set tblproperties (
    'optimize.quota' = '0.1');
```

更多信息请参考 [共享 Optimizer 资源的均衡](../optimizers.md#optimizer_2)。

### 设置 Optimize 参数

可以手动设置 Optimize 的执行间隔、任务大小、执行超时时间等参数，比如：

```sql
ALTER TABLE test_db.test_log_store set tblproperties (
    'optimize.major.trigger.max-interval' = '3600000');
```

更多 Optimize 参数调整参考 [结构优化配置](../meta-service/table-properties.md#_4)。

### 开启和关闭 Optimize

表的 Optimize 默认即处于开启状态，如果要关闭 Optimize 功能，执行以下命令，反之可以重新开启：

```sql
ALTER TABLE test_db.test_log_store set tblproperties (
    'optimize.enable' = 'false');
```

## 删除表

登录 [AMS Dashboard](http://localhost:1630) 后，进入 `Terminal`，输入修改语句并执行即可完成表的修改。
下面是一个删除表的的例子：

```sql
DROP TABLE test_db.test_log_store;
```

当前 Terminal 使用 Spark Engine 完成 SQL 的执行，更多有关删除表的语法参考 [Spark DDL](../spark/spark-ddl.md#drop-table)。

