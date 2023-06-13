
# Querying with SQL

# Querying Mixed-Format table by merge on read

使用 `Select` 语句查询 Arctic 表

```sql 
SELECT * FROM arctic_catalog.db.sample
```

Spark 引擎会将 BaseStore 和 ChangeStore 的数据 Merge 后返回

# Query on change store

对于主键表，可以通过 `.change` 查询 ChangeStore 的信息

```sql
SELECT * FROM arctic_catalog.db.sample.change

+---+----+----+---------------+------------+--------------+
| id|name|data|_transaction_id|_file_offset|_change_action|
+---+----+----+---------------+------------+--------------+
|  1|dddd|abcd|              3|           1|        INSERT|
|  1|dddd|abcd|              3|           2|        DELETE|
+---+----+----+---------------+------------+--------------+
```

查出来结果会多三列数据分别是：

- _transaction_id: 数据写入时AMS分配的 transaction id。批模式下为每条SQL执行时分配，流模式下为每次checkpoint 分配。
- _file_offset：大小可以表示同一批 _transaction_id 中数据写入的先后顺序。
- _change_action：表示数据的类型有 INSERT，DELETE 两种



# Querying with DataFrames

支持 DataFrame 读取，现在可以使用 `spark.read.table` 通过表名 load 表:

```scala
val df = spark.read.table("arctic_catalog.db.sample")
df.count
```

当然也可以在 DataFrame 任务中通过 `.change` 来访问 ChangeStore

```scala
val df = spark.read.table("arctic_catalog.db.sample.change")
df.count
```
