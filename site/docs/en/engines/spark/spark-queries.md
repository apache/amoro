## Querying with SQL

### Querying Mixed-Format table by merge on read

Using `Select` statement to query on Mixed-Format tables.

```sql 
SELECT * FROM arctic_catalog.db.sample
```

The Mixed-Format connector will merge the data from `BaseStore` and `ChangeStore`.

### Query on change store

For a Mixed-Format table with primary keys. you can query on `ChangeStore` by `.change`.

```sql
SELECT * FROM arctic_catalog.db.sample.change

+---+----+----+---------------+------------+--------------+
| id|name|data|_transaction_id|_file_offset|_change_action|
+---+----+----+---------------+------------+--------------+
|  1|dddd|abcd|              3|           1|        INSERT|
|  1|dddd|abcd|              3|           2|        DELETE|
+---+----+----+---------------+------------+--------------+
```

The addition columns are:

- _transaction_id: The transaction ID allocated by AMS during data write is assigned per SQL execution in batch mode and
  per checkpoint in streaming mode.
- _file_offset：The order of data written with the same `_transaction_id`.
- _change_action：The type of change record, `INSERT` or `DELETE`.

## Querying with DataFrames

You can read the Mixed-Format table by Spark DataFrames:

```scala
val df = spark.read.table("arctic_catalog.db.sample")
df.count
```

And visit the `ChangeStore` by `.change`.

```scala
val df = spark.read.table("arctic_catalog.db.sample.change")
df.count
```
