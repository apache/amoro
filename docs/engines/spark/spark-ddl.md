---
title: "Spark DDL"
url: spark-ddl
aliases:
    - "spark/ddl"
menu:
    main:
        parent: Spark
        weight: 300
---
# Spark DDL

## CREATE TABLE

To create an MixedFormat table under an Amoro Catalog, you can use `USING ARCTIC` to specify the provider in the
`CREATE TABLE` statement. If the Catalog type is Hive, the created table will be a Hive-compatible table.

```sql
CREATE TABLE arctic_catalog.db.sample (
    id bigint  COMMENT "unique id",
    data string
) USING arctic 
```

### PRIMARY KEY

You can use `PRIMARY KEY` in the `CREATE TABLE` statement to specify the primary key column.
MixedFormat ensures the uniqueness of the primary key column through MOR (Merge on Read) and Self-Optimizing.

```sql
CREATE TABLE arctic_catalog.db.sample (
    id bigint  COMMENT "unique id",
    data string ,
    PRIMARY KEY (id)
) USING arctic 
```

### PARTITIONED BY

Using `PARTITIONED BY` in the `CREATE TABLE` statement to create a table with partition spec.

```sql
CREATE TABLE arctic_catalog.db.sample (
    id bigint,
    data string,
    category string)
USING arctic
PARTITIONED BY (category)
```

In the `PARTITIONED BY` clause, you can define partition expressions, and Mixed-Iceberg format supports all partition
expressions in Iceberg.

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

Supported transformations are:

* years(ts): partition by year
* months(ts): partition by month
* days(ts) or date(ts): equivalent to dating partitioning
* hours(ts) or date_hour(ts): equivalent to dating and hour partitioning
* bucket(N, col): partition by hashed value mod N buckets
* truncate(L, col): partition by value truncated to L

      Strings are truncated to the given length

      Integers and longs truncate to bins: truncate(10, i) produces partitions 0, 10, 20, 30, …

> Mixed-Hive format doesn't support transform.

## CREATE TABLE ... AS SELECT

``` 
CREATE TABLE arctic_catalog.db.sample
USING arctic
AS SELECT ...
```

> The `CREATE TABLE ... AS SELECT` syntax is used to create a table and write the query results to the table. Primary
> keys, partitions, and properties are not inherited from the source table and need to be configured separately.

> You can enable uniqueness check for the primary key in the source table by setting set
> `spark.sql.arctic.check-source-data-uniqueness.enabled = true` in Spark SQL. If there are duplicate primary keys, an
> error will be raised during the write operation.


You can use the following syntax to create a table with primary keys, partitions, and properties:

```
CREATE TABLE arctic_catalog.db.sample
PRIMARY KEY(id) USING arctic 
PARTITIONED BY (pt)  
TBLPROPERTIES (''prop1''=''val1'', ''prop2''=''val2'')
AS SELECT ...
```

{{< hint info >}}
In the current version, `CREATE TABLE ... AS SELECT` does not provide atomicity guarantees.
{{< /hint >}}

## CREATE TABLE ... LIKE

The `CREATE TABLE ... LIKE` syntax copies the structure of a table, including primary keys and partitions, to a new
table, but it does not copy the data.

``` 
USE arctic_catalog;
CREATE TABLE db.sample
LIKE db.sample2
USING arctic
TBLPROPERTIES ('owner'='xxxx');
```

> Since `PRIMARY KEY` is not a standard Spark syntax, if the source table is a MixedFormat table with primary keys, the
> new table can copy the schema information with the primary keys. Otherwise, only schema could be copied.

{{< hint info >}}
`Create Table Like` only supports the binary form of `db.table` and in the same catalog
{{< /hint >}}

## REPLACE TABLE ... AS SELECT

> The `REPLACE TABLE ... AS SELECT` syntax only supports tables without primary keys in the current version.

``` 
REPLACE TABLE arctic_catalog.db.sample
USING arctic
AS SELECT ...
```

> In the current version, `REPLACE TABLE ... AS SELECT` does not provide atomicity guarantees.

## DROP TABLE

```sql
DROP TABLE arctic_catalog.db.sample;
```

## TRUNCATE TABLE

The `TRUNCATE TABLE` statement could delete all data in the table.

```sql
TRUNCATE TABLE arctic_catalog.db.sample;
```

## ALTER TABLE

The ALTER TABLE syntax supported by Mixed-Format includes:

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

Using `UNSET` to remove properties:

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

You can add multiple columns at once, separated by commas.

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

You can add columns at any position by using the `FIRST` or `AFTER` clause.

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

`"`ALTER COLUMN` can be used to widen types, make fields nullable, set comments, and reorder fields.

```sql
ALTER TABLE arctic_catalog.db.sample ALTER COLUMN measurement TYPE double;
```

To add or remove columns from a structure, use `ADD COLUMN` or `DROP COLUMN` with nested column names.

Column comments can also be updated using `ALTER COLUMN`.

```sql
ALTER TABLE arctic_catalog.db.sample ALTER COLUMN measurement TYPE double COMMENT 'unit is bytes per second';
ALTER TABLE arctic_catalog.db.sample ALTER COLUMN measurement COMMENT 'unit is kilobytes per second';
```

You can use the `FIRST` and `AFTER` clauses to reorder top-level or nested columns within a structure.

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

`DESCRIBE TABLE` returns basic metadata information about a table, including the primary key information for tables that
have a primary key

```sql
 { DESC | DESCRIBE }  TABLE  arctic_catalog.db.sample;
```
