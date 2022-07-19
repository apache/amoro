# Spark DataFrame Api

## load table
支持 DataFrame 读取，现在可以使用`spark.table`通过表名load表:

```scala
val df = spark.read.table("arctic_catalog.db.sample")
df.count
```


## write to table

### Appending data

要向 Arcitc 表添加数据，使用`append()`:
```sql
val data: DataFrame = ...
data.writeTo("arctic_catalog.db.sample").append()
```

???+note "append 只支持无主键表"



### Overwriting data

要动态覆盖分区，使用`overwritePartitions()`:

```sql
val data: DataFrame = ...
data.writeTo("arctic_catalog.db.sample").overwritePartitions()
```