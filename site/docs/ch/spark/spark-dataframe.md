# Spark DataFrame Api

## load table
支持 DataFrame 读取，现在可以使用 `spark.table` 通过表名 load 表:

```scala
val df = spark.read.table("arctic_catalog.db.sample")
df.count
```


## write to table

### Appending data

要向 Arctic 表添加数据，使用 `append()`:
```sql
val data: DataFrame = ...
data.writeTo("arctic_catalog.db.sample").append()
```

> append 只支持无主键表



### Overwriting data

要动态覆盖分区，使用 `overwritePartitions()` :

```sql
val data: DataFrame = ...
data.writeTo("arctic_catalog.db.sample").overwritePartitions()
```

### Creating tables
创建表请使用 `create` 操作
```sql
val data: DataFrame = ...
data.writeTo("arctic_catalog.db.sample").create()
```

创建表操作支持表配置方法，如 `partitionBy`，并且arctic支持使用 `option("primary.keys", "'xxx'")` 来指定主键:
```sql
val data: DataFrame = ...
data.write().format("arctic")
    .partitionBy("data")
    .option("primary.keys", "'xxx'")
    .save("arctic_catalog.db.sample")
```



