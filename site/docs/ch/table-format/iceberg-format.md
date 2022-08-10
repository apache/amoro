# Iceberg Format

正如 [Tablestore](table-store.md) 中所述，Arctic 在数据湖中的数据分为 Basestore 与 Changestore。
无论是 Basestore 还是 Changestore 都默认支持 Iceberg 的实现。而在使用上除了使用 Arctic 封装好的使用方式之外，用户仍然可以使用 Iceberg 原生的方式来读、写、配置这张表。

## Iceberg 原生读写

这里以 Spark 为例，介绍如何使用 Iceberg Spark Datasource 来操作 [quickstart](../docker-quickstart.md) 环境下 Arctic 创建管理的 Iceberg 表。
我们使用下面的命令打开一个 Spark SQL 客户端：

```shel
spark-sql --packages org.apache.iceberg:iceberg-spark-runtime-3.2_2.12:0.14.0\
    --conf spark.sql.extensions=org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions \
    --conf spark.sql.catalog.spark_catalog=org.apache.iceberg.spark.SparkSessionCatalog \
    --conf spark.sql.catalog.spark_catalog.type=hive \
    --conf spark.sql.catalog.local=org.apache.iceberg.spark.SparkCatalog \
    --conf spark.sql.catalog.local.type=hadoop \
    --conf spark.sql.catalog.local.warehouse=/tmp/arctic/warehouse
```

之后即可使用如下命令读取、写入这些 Arctic 创建管理的 Iceberg 表：

```sql
-- 切换到 Iceberg catalog 下
use local;

-- 查看所有的 Iceberg 表
show tables;

-- 查看 Basestore
select * from local.test_db.test_table.base;

-- 查看 Changestore
select * from local.test_db.test_table.change;

-- 写入 Basestore
insert into local.test_db.test_table.base value(10, 'tony', timestamp('2022-07-03 12:10:30'));

```

更多有关 Spark 下的 Iceberg 表操作可以在 [Iceberg Spark](https://iceberg.apache.org/docs/latest/getting-started/) 中找到。

???+ 注意

    在 Iceberg 原生访问方式下用户可以自由的操作 Basestore 与 Changestore，但是一些危险的操作（比如向 Changestore 中写入数据，或者只更新 Basestore 或者 Change store的表结构）可能破坏 Arctic 的元数据体系，导致最终在使用 Arctic 的特性时出现错误。
    所以只建议你在必要的情况下用这种方式读取 Iceberg 内的数据，而写入与变更则最好通过 Arctic 的方式。