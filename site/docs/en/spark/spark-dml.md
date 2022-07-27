# Spark DML

## Query

使用 `Select` 语句查询 arctic 表

```sql 
SELECT * FROM arctic_catalog.db.sample
```


### Select from change table

对于主键表，可以通过 `.change` 查询 ChangeStore 的信息

```sql
SELECT * FROM arctic_catalog.db.sample.change
```

## Write

### Insert overwrite 

`INSERT OVERWRITE`可以用查询的结果替换表中的数据

Spark默认的覆盖模式是static

Dynamic覆盖模式通过设置`spark.sql.sources.partitionOverwriteMode=dynamic`

为了演示`dynamic overwrite`和`static overwrite`的行为，由以下DDL定义一张测试表:

```sql
CREATE TABLE arctic_catalog.db.sample (
    id int,
    data string,
    ts timestamp,
    primary key (id))
USING arctic
PARTITIONED BY (days(ts))
```

当 Spark 的覆盖模式是 dynamic 时，由`SELECT`查询生成的行的分区将被替换。

```sql
INSERT OVERWRITE arctic_catalog.db.sample values 
(1, 'aaa',  timestamp(' 2022-1-1 09:00:00 ')), 
(2, 'bbb',  timestamp(' 2022-1-2 09:00:00 ')), 
(3, 'ccc',  timestamp(' 2022-1-3 09:00:00 '))
```

当 Spark 的覆盖模式为static 时，该`PARTITION`子句将转换为从表中`SELECT`的结果集。如果`PARTITION`省略该子句，则将替换所有分区

```sql
INSERT OVERWRITE arctic_catalog.db.sample 
partition( dt = '2021-1-1')  values 
(1, 'aaa'), (2, 'bbb'), (3, 'ccc') 
```

???+note "在static 模式下，不支持在分区字段上定义 transform"


### Insert into

要向表添加新数据，请使用`INSERT INTO`

```sql
INSERT INTO arctic_catalog.db.sample VALUES (1, 'a'), (2, 'b')

INSERT INTO prod.db.table SELECT ...
```

???+note "INSERT INTO 语法在当前版本只支持无主键表"


### Delete from

Arctic Spark 支持对无主键表的 `DELETE FROM` 语法用于删除表中数据

```sql
DELETE FROM arctic_catalog.db.sample
WHERE ts >= '2020-05-01 00:00:00' and ts < '2020-06-01 00:00:00'

DELETE FROM arctic_catalog.db.sample
WHERE session_time < (SELECT min(session_time) FROM prod.db.good_events)

DELETE FROM arctic_catalog.db.sample AS t1
WHERE EXISTS (SELECT oid FROM prod.db.returned_orders WHERE t1.oid = oid)
```

???+note "DELETE FROM 语法在当前版本只支持无主键表"

### Update 

支持`UPDATE`语句对无主键表的进行更新

更新语句使用`SELECT`来匹配要更新的行

```sql
UPDATE arctic_catalog.db.sample
SET c1 = 'update_c1', c2 = 'update_c2'
WHERE ts >= '2020-05-01 00:00:00' and ts < '2020-06-01 00:00:00'

UPDATE arctic_catalog.db.sample
SET session_time = 0, ignored = true
WHERE session_time < (SELECT min(session_time) FROM prod.db.good_events)

UPDATE arctic_catalog.db.sample AS t1
SET order_status = 'returned'
WHERE EXISTS (SELECT oid FROM prod.db.returned_orders WHERE t1.oid = oid)
```

???+note "UPDATE 语法在当前版本只支持无主键表"

### MERGE INTO

支持使用 `MERGE INTO` 语句对无主键表进行更新

```sql 
MERGE INTO prod.db.target t   -- a target table
USING (SELECT ...) s          -- the source updates
ON t.id = s.id                -- condition to find updates for target rows
WHEN ...                      -- updates
```

支持多个 `WHEN MATCHED ... THEN ...` 语法执行 `UPDATE`, `DELETE`, `INSERT` 等操作。

```sql 

MERGE INTO prod.db.target t   
USING prod.db.source s       
ON t.id = s.id             
WHEN MATCHED AND s.op = 'delete' THEN DELETE
WHEN MATCHED AND t.count IS NULL AND s.op = 'increment' THEN UPDATE SET t.count = 0
WHEN MATCHED AND s.op = 'increment' THEN UPDATE SET t.count = t.count + 1          
WHEN NOT MATCHED THEN INSERT *

```

???+note "UPDATE 语法在当前版本只支持无主键表"