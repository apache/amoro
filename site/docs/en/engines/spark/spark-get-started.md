# Iceberg Format

The Iceberg Format can be accessed using the Connector provided by Iceberg.
Refer to the documentation at [Iceberg Spark Connector](https://iceberg.apache.org/docs/latest/getting-started/#) 
for more information.

# Mixed Format

## 环境准备

当前 Arctic-Spark-Connector 支持与 Spark 3.1  版本使用。在开始使用前，
[下载](https://github.com/NetEase/arctic/releases/download/v0.4.0/arctic-spark-3.1-runtime-0.4.0.jar)并将 
arctic-spark-3.1-runtime.jar 复制到 `${SPARK_HOME}/jars` 目录下，然后通过 Bash 启动Spark-Sql 客户端。

```
${SPARK_HOME}/bin/spark-sql \
    --conf spark.sql.extensions=com.netease.arctic.spark.ArcticSparkExtensions \
    --conf spark.sql.catalog.local_catalog=com.netease.arctic.spark.ArcticSparkCatalog \
    --conf spark.sql.catalog.local_catalog.url=thrift://${AMS_HOST}:${AMS_PORT}/${AMS_CATALOG_NAME}
```

> Arctic 通过 ArcticMetaService 管理 Catalog, Spark catalog 需要通过URL映射到 Arctic Catalog, 格式为:
> `thrift://${AMS_HOST}:${AMS_PORT}/${AMS_CATALOG_NAME}`, arctic-spark-connector 会通过 thrift 协议自动
> 下载 hadoop site 配置文件用于访问 hdfs 集群.
>
> AMS_PORT 为AMS服务 thrift api接口端口号，默认值为 1260
> AMS_CATALOG_NAME 为启动AMS 服务时配置的 Catalog, 默认值为 local_catalog

关于 Spark 下的详细配置，请参考 [Spark Configurations](spark-conf.md)


## 创建表

在 Spark SQL 命令行中，可以通过 `CREATE TABLE` 语句执行建表命令。

在执行建表操作前，请先创建 database。

```
-- switch to arctic catalog defined in spark conf
use local_catalog;

-- create databsae first 
create database if not exists test_db;
```

然后切换到刚建立的 database 下进行建表操作

```
use test_db;

-- create a table with 3 columns
create table test1 (id int, data string, ts timestamp) using arctic;

-- create a table with hidden partition
create table test2 (id int, data string, ts timestamp) using arctic partitioned by (days(ts));

-- create a table with hidden partition and primary key
create table test3 (id int, data string, ts timestamp, primary key(id)) using arctic partitioned by (days(ts));
```

更多表相关 DDL，请参考 [Spark DDL](spark-ddl.md)

## 写入

如果您使用 SparkSQL, 可以通过 `INSERT OVERWRITE` 或 `INSERT` SQL语句向 Arctic 表写入数据。

```
-- insert values into unkeyed table
insert into test2 values 
( 1, "aaa", timestamp('2022-1-1 00:00:00')),
( 2, "bbb", timestamp('2022-1-2 00:00:00')),
( 3, "bbb", timestamp('2022-1-3 00:00:00'));

-- dynamic overwrite table 
insert overwrite test3 values 
( 1, "aaa", timestamp('2022-1-1 00:00:00')),
( 2, "bbb", timestamp('2022-1-2 00:00:00')),
( 3, "bbb", timestamp('2022-1-3 00:00:00'));
```


> 如果使用 Static 类型的 Overwrite, 不能在分区上定义函数。

或者可以在 jar 任务中使用 DataFrame Api 向 Arctic 表写入数据

``` 
val df = spark.read().load("/path-to-table")
df.writeTo('test_db.table1').overwritePartitions()
```

更多表写入相关，请参考 [Spark Writes](spark-writes.md)

## 读取

使用 `SELECT` SQL语句查询 Arctic 表

``` 
select count(1) as count, data 
from test2 
group by data;
```

对于有主键表，支持通过 `.change` 的方式访问 `ChangeStore`

``` 
select count(1) as count, data
from test_db.test3.change group by data;
```
> 此处 change 表没有数据，结果返回空

更多表读取相关，请参考 [Spark Queries](spark-queries.md)
