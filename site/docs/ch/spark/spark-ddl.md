# DDL

## CREATE TABLE

在 Arctic Catalog 下使用 `USING ARCTIC` 指定使用 Arctic 数据源即可通过 `CREATE TABLE` 语句创建 Arctic 表。 
如果 Catalog 的类型是 Hive ，则创建的是 Hive 兼容表。

```sql
CREATE TABLE arctic_catalog.db.sample (
    id bigint  COMMENT "unique id",
    data string
) USING arctic 
```

### PRIMARY KEY

在 `CREATE TABLE` 语句中使用 `PRIMARY KEY` 指定主键列，这样即可创建有主键表。
Arctic 将通过 MOR(Merge on read) 和 Optimize 实现主键列上的唯一性。

```sql
CREATE TABLE arctic_catalog.db.sample (
    id bigint  COMMENT "unique id",
    data string ,
    PRIMARY KEY (id)
) USING arctic 
```

### PARTITIONED BY

在 `CREATE TABLE` 语句中使用 `PARTITIONED BY` 指定分区方式，这样即可创建分区表。

```sql
CREATE TABLE arctic_catalog.db.sample (
    id bigint,
    data string,
    category string)
USING arctic
PARTITIONED BY (category)
```

在 `PARTITIONED BY` 子句中可以定义 分区表达式， arctic 支持 iceberg 中全部分区表达式

```sql
CREATE TABLE arctic_catalog.db.sample (
    id bigint,
    data string,
    category string,
    ts timestamp, 
    PRIMARY KEY (id) )
USING arctic
PARTITIONED BY (bucket(16, id), days(ts), category)
```

可使用的 transform 有:

* year(ts): 截取时间类型字段作为分区值，精度到 year
* month(ts): 截取时间类型字段作为分区值， 精度到 month
* days(ts) or date(ts): 截取时间类型字段作为分区值，精度到 day
* hours(ts) or date_hour(ts): 截图时间类型字段作为分区值，精度到 hour
* bucket(N, col): 取某一列上的 hash 值作为分区值
* truncate(L, col): 截取某一列上前 L 个字符作为分区值

> Hive 类型的 Catalog 不支持分区表达式。

## CREATE TABLE ... AS SELECT 


``` 
CREATE TABLE arctic_catalog.db.sample
USING arctic
AS SELECT ...
```
> CREATE TABLE ... AS SELECT 语法作用为创建表并将查询结果写入表中，主键、分区、以及 properties 不会从源表中继承，需单独配置。
> 可以通过 SPARK SQL`set spark.sql.arctic.check-source-data-uniqueness.enabled = true` 开启对源表主键的唯一性校验，若存在相同主键，写入时会报错提示。

创建带主键、分区、preoperties 的表，可以使用如下语法：

```
CREATE TABLE arctic_catalog.db.sample
PRIMARY KEY(id) USING arctic 
PARTITIONED BY (pt)  
TBLPROPERTIES (''prop1''=''val1'', ''prop2''=''val2'')
AS SELECT ...
```

???+danger "CREATE TABLE ... AS SELECT 在当前版本没有原子性保证"

## CREATE TABLE ... LIKE

`CREATE TABLE ... LIKE` 语法会将表结构包括主键、分区以及表配置复制到新表中，但不会复制数据。

``` 
CREATE TABLE arctic_catalog.db.sample
LIKE arctic_catalog.db.sample2
USING arctic
```

> 因为 primary key 不是 Spark 标准语法，所以如果源表是 Arctic 表，且有主键，新建表可以复制主键这部分的 schema 信息，如果是其他类型的表，则无法复制
> 
## REPLACE TABLE ... AS SELECT

> REPLACE TABLE ... AS SELECT 语法在当前版本只支持无主键表 

``` 
REPLACE TABLE arctic_catalog.db.sample
USING arctic
AS SELECT ...
```

> REPLACE TABLE ... AS SELECT 在当前版本没有原子性保证

## DROP TABLE

```sql
DROP TABLE arctic_catalog.db.sample;
```

## TRUNCATE TABLE

Arctic Spark 支持 `TRUNCATE TABLE` 语法用于删除表中所有行

```sql
TRUNCATE TABLE arctic_catalog.db.sample;
```

## ALTER TABLE
Arctic 支持的 `ALTER TABLE` 语法包括：

* ALTER TABLE ... SET TBLPROPERTIES
* ALTER TABLE ... ADD COLUMN  
* ALTER TABLE ... RENAME COLUMN
* ALTER TABLE ... ALTER COLUMN
* ALTER TABLE ... DROP COLUMN
* ALTER TABLE ... DROP PARTITION

### ALTER TABLE ... SET TBLPROPERTIES
```sql
ALTER TABLE arctic_catalog.db.sample SET TBLPROPERTIES (
    'read.split.target-size'='268435456'
);
```
使用 `UNSET` 可以移除 properties:
```sql
ALTER TABLE arctic_catalog.db.sample UNSET TBLPROPERTIES ('read.split.target-size');
```
### ALTER TABLE ... ADD COLUMN
```sql
ALTER TABLE arctic_catalog.db.sample
ADD COLUMNS (
    new_column string comment 'new_column docs'
  );
```
可以同时添加多个列，用逗号分隔。 
```sql
-- create a struct column
ALTER TABLE arctic_catalog.db.sample
ADD COLUMN point struct<x: double, y: double>;

-- add a field to the struct
ALTER TABLE arctic_catalog.db.sample
ADD COLUMN point.z double;
```
```sql
-- create a nested array column of struct
ALTER TABLE arctic_catalog.db.sample
ADD COLUMN points array<struct<x: double, y: double>>;

-- add a field to the struct within an array. Using keyword 'element' to access the array's element column.
ALTER TABLE arctic_catalog.db.sample
ADD COLUMN points.element.z double;
```
```sql
-- create a map column of struct key and struct value
ALTER TABLE arctic_catalog.db.sample
ADD COLUMN points map<struct<x: int>, struct<a: int>>;

-- add a field to the value struct in a map. Using keyword 'value' to access the map's value column.
ALTER TABLE arctic_catalog.db.sample
ADD COLUMN points.value.b int;
```
可以通过添加 `FIRST` 或 `AFTER` 子句在任何位置添加列:
```sql
ALTER TABLE arctic_catalog.db.sample
ADD COLUMN new_column bigint AFTER other_column;
```
```sql
ALTER TABLEarctic_catalog.db.sample
ADD COLUMN nested.new_column bigint FIRST;
```
### ALTER TABLE ... RENAME COLUMN
```sql
ALTER TABLE arctic_catalog.db.sample RENAME COLUMN data TO payload;
```
### ALTER TABLE ... ALTER COLUMN
Alter COLUMN 可以用于加宽类型，使字段成为可选字段，设置注释和重新排序字段。
```sql
ALTER TABLE arctic_catalog.db.sample ALTER COLUMN measurement TYPE double;
```
若要从结构中添加或删除列，请使用带有嵌套列名的 `ADD COLUMN` 或 `DROP COLUMN`。

Column 注释也可以使用 `ALTER COLUMN` 更新:
```sql
ALTER TABLE arctic_catalog.db.sample ALTER COLUMN measurement TYPE double COMMENT 'unit is bytes per second';
ALTER TABLE arctic_catalog.db.sample ALTER COLUMN measurement COMMENT 'unit is kilobytes per second';
```
允许使用 `FIRST` 和 `AFTER` 子句对结构中的顶级列或列进行重新排序:
```sql
ALTER TABLE arctic_catalog.db.sample ALTER COLUMN col FIRST;
```
```sql
ALTER TABLE arctic_catalog.db.sample ALTER COLUMN nested.col AFTER other_col;
```
### ALTER TABLE ... DROP COLUMN
```sql
ALTER TABLE arctic_catalog.db.sample DROP COLUMN id;
ALTER TABLE arctic_catalog.db.sample DROP COLUMN point.z;
```
### ALTER TABLE ... DROP PARTITION
```sql
ALTER TABLE arctic_catalog.db.sample DROP IF EXISTS PARTITION (dt=2022);
```



## DESC TABLE
`DESCRIBE TABLE`返回表的基本元数据信息。 对于有主键表，也会展示主键信息。
```sql
 { DESC | DESCRIBE }  TABLE  arctic_catalog.db.sample;
```