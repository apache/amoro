# 表管理
[AMS Dashboard](http://localhost:1630)中提供了命令行工具`Terminal`帮组用户快速完成表的创建、修改与删除操作。
同时还可以在[Spark](#flink.md)和[Flink](#spark/spark-ddl.md)等引擎中使用SQL完成表的管理。

## 新建表
登录[AMS Dashboard](http://localhost:1630)后，进入`Terminal`，输入建表语句并执行即可完成表的创建。
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
  'log-store.topic' = 'local_catalog.test_db.test_log_store.log_store');
```

当前Terminal使用Spark完成SQL的执行，更多有关建表的语法参考[Spark DDL](../spark/spark-ddl.md#create-table)。

### 配置Logstore
如[Tablestore](../table-structure.md)所述，一张Arctic表可能由几部分共同组成，一般来说basestore与changestore会随着表的创建而自动创建，
logstore作为可选组件则需要另外的配置来指明，有关logstore的完整配置可以参考[Logstore相关配置](table-properties.md#logstore)。

上面的例子中将kafka集群：`127.0.0.1:9092`中的topic：`local_catalog.test_db.test_log_store.log_store`作为了新建表的`logstore`,
而在执行上面的语句之前你还需要手动去对应的kafka集群中创建对应的topic，或者打开集群的自动创建topic功能。

### 参数配置
Logstore的相关配置是通过表的额外属性在表创建之时写入表的配置当中的，[Table Config](table-properties.md)中有当前表的所有的可用配置，
它们大部分都已经被设置了合理的默认值，你也可以按需在创建语句中进行设置。

## 修改表

登录[AMS Dashboard](http://localhost:1630)后，进入`Terminal`，输入修改语句并执行即可完成表的修改。
下面是一个新增字段的的例子：

```sql
ALTER TABLE test_db.test_log_store ADD COLUMN new_column string comment 'new_column docs';
```

当前Terminal使用Spark完成SQL的执行，更多有关修改表的语法参考[Spark DDL](../spark/spark-ddl.md#alter-table)。

## 删除表

登录[AMS Dashboard](http://localhost:1630)后，进入`Terminal`，输入修改语句并执行即可完成表的修改。
下面是一个删除表的的例子：

```sql
DROP TABLE test_db.test_log_store;
```

当前Terminal使用Spark完成SQL的执行，更多有关删除表的语法参考[Spark DDL](../spark/spark-ddl.md#drop-table)。